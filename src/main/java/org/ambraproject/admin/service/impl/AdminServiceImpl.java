/*
 * Copyright (c) 2006-2014 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.admin.service.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.ambraproject.ApplicationException;
import org.ambraproject.admin.service.AdminService;
import org.ambraproject.admin.service.OnCrossPubListener;
import org.ambraproject.admin.service.OnPublishListener;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleList;
import org.ambraproject.models.Category;
import org.ambraproject.models.Issue;
import org.ambraproject.models.Journal;
import org.ambraproject.models.Volume;
import org.ambraproject.queue.MessageSender;
import org.ambraproject.routes.CrossRefLookupRoutes;
import org.ambraproject.routes.SavedSearchEmailRoutes;
import org.ambraproject.search.SavedSearchRetriever;
import org.ambraproject.service.article.ArticleClassifier;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.FetchArticleService;
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.util.XPathUtil;
import org.ambraproject.views.TOCArticle;
import org.ambraproject.views.TOCArticleGroup;
import org.ambraproject.views.article.ArticleInfo;
import org.ambraproject.views.article.ArticleType;
import org.apache.camel.CamelExecutionException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ambraproject.admin.action.ArticleManagementAction.ARTICLE_LIST_TYPE;

public class AdminServiceImpl extends HibernateServiceImpl implements AdminService, OnPublishListener {
  private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

  private MessageSender messageSender;
  private FetchArticleService fetchArticleService;
  private ArticleService articleService;
  private ArticleClassifier articleClassifier;
  private Configuration configuration;
  private List<OnCrossPubListener> onCrossPubListener;
  private final static Set<String> ARTICLE_TYPE = new HashSet<String>();

  static {
    ARTICLE_TYPE.add("correction");
    ARTICLE_TYPE.add("expression-of-concern");
    ARTICLE_TYPE.add("retraction");
  }

  public void setOnCrossPubListener(List<OnCrossPubListener> onCrossPubListener) {
    this.onCrossPubListener = onCrossPubListener;
  }

  @Required
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  @Required
  public void setMessageSender(MessageSender messageSender) {
    this.messageSender = messageSender;
  }

  @Required
  public void setFetchArticleService(FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  @Required
  public void setArticleClassifier(ArticleClassifier articleClassifier) {
    this.articleClassifier = articleClassifier;
  }

  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<ArticleInfo> getPublishableArticles(String eIssn, String orderField,
                                                  boolean isOrderAscending) throws ApplicationException {

    List<ArticleInfo> articlesInfo = new ArrayList<ArticleInfo>();
    Order order = isOrderAscending ? Order.asc(orderField) : Order.desc(orderField);
    List<Object[]> results = (List<Object[]>) hibernateTemplate.findByCriteria(DetachedCriteria.forClass(Article.class)
        .add(Restrictions.eq("eIssn", eIssn))
        .add(Restrictions.eq("state", Article.STATE_UNPUBLISHED))
        .addOrder(order)
        .setProjection(Projections.projectionList()
            .add(Projections.property("doi"))
            .add(Projections.property("date"))));

    for (Object[] rows : results) {
      ArticleInfo articleInfo = new ArticleInfo();
      articleInfo.setDoi(rows[0].toString());
      articleInfo.setDate((Date) rows[1]);
      articlesInfo.add(articleInfo);
    }

    return articlesInfo;
  }

  @Override
  @Transactional
  public void crossPubArticle(final String articleDoi, final String journalKey) throws Exception {
    log.debug("Cross publishing {} in {}", articleDoi, journalKey);
    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Journal journal = (Journal) session.createCriteria(Journal.class)
            .add(Restrictions.eq("journalKey", journalKey))
            .uniqueResult();
        Article article = (Article) session.createCriteria(Article.class)
            .add(Restrictions.eq("doi", articleDoi))
            .uniqueResult();
        if (!article.getJournals().contains(journal)) {
          article.getJournals().add(journal);
        }
        session.update(article);
        return null;
      }
    });
    invokeOnCrossPubListeners(articleDoi);
  }

  @Override
  public void articlePublished(String articleId, String authID) throws Exception {
    refreshReferences(articleId, authID);
  }

  @Override
  public void refreshReferences(final String articleDoi, final String authID) {
    log.debug("Sending message to: {}, ({},{})", new Object[]{
        "activemq:plos.updatedCitedArticles?transacted=true", articleDoi, authID});

    String refreshCitedArticlesQueue = configuration.getString("ambra.services.queue.refreshCitedArticles", null);
    if (refreshCitedArticlesQueue != null) {
      try {
        messageSender.sendMessage(refreshCitedArticlesQueue, articleDoi, new HashMap() {{
          put(CrossRefLookupRoutes.HEADER_AUTH_ID, authID);
        }});
      } catch (CamelExecutionException ex) {
        log.error(ex.getMessage(), ex);
        throw new RuntimeException("Failed to queue job for refreshing article references, is the queue running?");
      }
    } else {
      throw new RuntimeException("Refresh cited articles queue not defined. No route created.");
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public void sendJournalAlerts(SavedSearchRetriever.AlertType type, Date startTime, Date endTime) {
    log.debug("Sending message to send alerts for type: {}", type);

    String sendSearchAlertsQueue = configuration.getString("ambra.services.queue.sendSearchAlerts", null);
    if (sendSearchAlertsQueue != null) {
      Map<String, Object> headers = new HashMap<String, Object>();
      SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

      //The queue expects dates to be in a specific format
      headers.put(SavedSearchEmailRoutes.HEADER_STARTTIME, (startTime == null ? null : formatter.format(startTime)));
      headers.put(SavedSearchEmailRoutes.HEADER_ENDTIME, (endTime == null ? null : formatter.format(endTime)));

      messageSender.sendMessage(sendSearchAlertsQueue, type.toString(), headers);
    } else {
      throw new RuntimeException("No message sent to send alerts, No route created.");
    }
  }

  @Override
  @Transactional
  public void removeArticleFromJournal(final String articleDoi, final String journalKey) throws Exception {
    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Article article = (Article) session.createCriteria(Article.class)
            .add(Restrictions.eq("doi", articleDoi))
            .uniqueResult();
        for (Iterator<Journal> iterator = article.getJournals().iterator(); iterator.hasNext(); ) {
          if (journalKey.equals(iterator.next().getJournalKey())) {
            iterator.remove();
            break;
          }
        }
        session.update(article);
        return null;
      }
    });
    invokeOnCrossPubListeners(articleDoi);
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<String> getCrossPubbedArticles(Journal journal) {
    return (List<String>) hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Article.class)
            .add(Restrictions.ne("eIssn", journal.geteIssn()))
            .createAlias("journals", "j")
            .add(Restrictions.eq("j.eIssn", journal.geteIssn()))
            .setProjection(Projections.property("doi"))
    );
  }

  @Transactional
  @Override
  public void setCurrentIssue(String journalKey, String issueUri) {
    Issue issue;
    try {
      issue = (Issue) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Issue.class)
              .add(Restrictions.eq("issueUri", issueUri))
              .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Issue '" + issueUri + "' didn't exist");
    }
    Journal journal;
    try {
      journal = (Journal) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Journal.class)
              .add(Restrictions.eq("journalKey", journalKey))
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Journal '" + journalKey + "' didn't exist");
    }
    journal.setCurrentIssue(issue);
    hibernateTemplate.update(journal);
  }

  @Override
  @Transactional(readOnly = true)
  public Volume getVolume(String volumeUri) {
    try {
      return (Volume) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Volume.class)
              .add(Restrictions.eq("volumeUri", volumeUri))
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional(readOnly = true)
  public List<Volume> getVolumes(final String journalKey) {
    //volumes are lazy so we need to access them in a session
    return (List<Volume>) hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Journal journal = (Journal) session.createCriteria(Journal.class)
            .add(Restrictions.eq("journalKey", journalKey))
            .uniqueResult();
        if (journal == null) {
          log.debug("No journal existed for key: " + journalKey);
          return Collections.emptyList();
        } else {
          //bring up all the volumes
          for (int i = 0; i < journal.getVolumes().size(); i++) {
            journal.getVolumes().get(i);
          }
          return journal.getVolumes();
        }
      }
    });
  }

  @Transactional
  @Override
  public Volume createVolume(final String journalKey, final String volumeUri, final String displyName) {
    if (StringUtils.isEmpty(journalKey)) {
      throw new IllegalArgumentException("No journal specified");
    } else if (StringUtils.isEmpty(volumeUri)) {
      throw new IllegalArgumentException("No Volume Uri specified");
    }
    return (Volume) hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Journal journal = (Journal) session.createCriteria(Journal.class)
            .add(Restrictions.eq("journalKey", journalKey))
            .uniqueResult();
        //if the journal doesn't exist, return null
        if (journal == null) {
          return null;
        } else {
          //check if a volume with the same uri exists, and if so, return null
          for (Volume existingVolume : journal.getVolumes()) {
            if (existingVolume.getVolumeUri().equals(volumeUri)) {
              return null;
            }
          }
          Volume newVolume = new Volume();
          newVolume.setVolumeUri(volumeUri);
          newVolume.setDisplayName(displyName);
          journal.getVolumes().add(newVolume);
          session.update(journal);
          return newVolume;
        }
      }
    });
  }

  @Transactional
  @Override
  public String[] deleteVolumes(final String journalKey, final String... volumeUris) {
    //volumes are lazy, so we have to access them in a session
    return (String[]) hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Journal journal = (Journal) session.createCriteria(Journal.class)
            .add(Restrictions.eq("journalKey", journalKey))
            .uniqueResult();
        if (journal == null) {
          throw new IllegalArgumentException("No such journal: " + journalKey);
        }
        List<String> deletedVolumes = new ArrayList<String>(volumeUris.length);
        Iterator<Volume> iterator = journal.getVolumes().iterator();
        while (iterator.hasNext()) {
          Volume volume = iterator.next();
          if (ArrayUtils.indexOf(volumeUris, volume.getVolumeUri()) != -1) {
            iterator.remove();
            session.delete(volume);
            deletedVolumes.add(volume.getVolumeUri());
          }
        }
        session.update(journal);
        return deletedVolumes.toArray(new String[deletedVolumes.size()]);
      }
    });
  }

  @Override
  @Transactional
  public void updateVolume(final String volumeUri, final String displayName, final String issueCsv)
      throws IllegalArgumentException {
    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Volume volume = (Volume) session.createCriteria(Volume.class)
            .add(Restrictions.eq("volumeUri", volumeUri))
            .setFetchMode("issues", FetchMode.JOIN)
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
            .uniqueResult();

        if (volume != null) {
          //check that we've only reordered the issue csv, not added or deleted
          String existingIssues = AdminServiceImpl.this.formatIssueCsv(volume.getIssues());
          for (String oldIssue : existingIssues.split(",")) {
            if (!issueCsv.contains(oldIssue)) {
              throw new IllegalArgumentException("Removed issue '" + oldIssue + "' from csv when updating volume");
            }
          }
          for (String newIssue : issueCsv.split(",")) {
            if (!existingIssues.contains(newIssue)) {
              throw new IllegalArgumentException("Added issue '" + newIssue + "' to csv when updating volume");
            }
          }

          volume.getIssues().clear();
          for (String issueUri : issueCsv.split(",")) {
            Issue issue = (Issue) session.createCriteria(Issue.class)
                .add(Restrictions.eq("issueUri", issueUri))
                .setFetchMode("articleDois", FetchMode.SELECT)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .uniqueResult();
            if (issue != null) {
              volume.getIssues().add(issue);
            }
          }
          volume.setDisplayName(displayName);
          session.update(volume);
        }
        return null;
      }
    });
  }

  @Override
  @Transactional
  public void deleteIssue(final String issueUri) {
    log.debug("Deleting issue '{}'", issueUri);
    //using hibernateTemplate.execute() instead of hibernateTemplate.findByCriteria() here
    //because for some reason adding the issue restriction causes issues to be lazy-loaded,
    //even with fetchMode = JOIN
    hibernateTemplate.execute(new HibernateCallback<Void>() {
      @Override
      public Void doInHibernate(Session session) throws HibernateException, SQLException {
        Volume volume = (Volume) DataAccessUtils.uniqueResult(
            session.createCriteria(Volume.class)
                .setFetchMode("issues", FetchMode.JOIN)
                .createAlias("issues", "i")
                .add(Restrictions.eq("i.issueUri", issueUri))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list()
        );
        if (volume != null) {
          Iterator<Issue> iterator = volume.getIssues().iterator();
          while (iterator.hasNext()) {
            Issue issue = iterator.next();
            if (issue.getIssueUri().equals(issueUri)) {
              iterator.remove();
              session.delete(issue);
              break;
            }
          }
          session.update(volume);
        }
        return null;
      }
    });
  }

  @Override
  @Transactional(readOnly = true)
  public Issue getIssue(String issueUri) {
    log.debug("Retrieving issue with uri '{}'", issueUri);
    try {
      return (Issue) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Issue.class)
              .add(Restrictions.eq("issueUri", issueUri))
              .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Issue> getIssues(final String volumeUri) {
    log.debug("Retrieving issues for '{}'", volumeUri);
    try {
      return ((Volume) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Volume.class)
              .add(Restrictions.eq("volumeUri", volumeUri))
              .setFetchMode("issues", FetchMode.JOIN)
              .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
      ).get(0)).getIssues();
    } catch (IndexOutOfBoundsException e) {
      return Collections.emptyList();
    }
  }

  @Override
  public String formatIssueCsv(final List<Issue> issues) {
    List<String> issueUris = new ArrayList<String>(issues.size());
    for (Issue issue : issues) {
      issueUris.add(issue.getIssueUri());
    }
    return StringUtils.join(issueUris, ",");
  }

  @Override
  @Transactional
  public void addIssueToVolume(final String volumeUri, final Issue issue) {
    if (StringUtils.isEmpty(issue.getIssueUri())) {
      throw new IllegalArgumentException("Must specify an Issue URI");
    }
    log.debug("Creating an issue with uri: '{}' and adding it to volume: '{}'", issue.getIssueUri(), volumeUri);
    if (((Number) hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Issue.class)
            .add(Restrictions.eq("issueUri", issue.getIssueUri()))
            .setProjection(Projections.rowCount())
    ).get(0)).intValue() > 0) {
      throw new IllegalArgumentException("An issue with uri '" + issue.getIssueUri() + "' already exists");
    }
    Volume storedVolume;
    try {
      storedVolume = (Volume) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Volume.class)
              .add(Restrictions.eq("volumeUri", volumeUri))
              .setFetchMode("issues", FetchMode.JOIN)
              .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Volume '" + volumeUri + "' doesn't exist");
    }
    if (StringUtils.isEmpty(issue.getTitle()) || StringUtils.isEmpty(issue.getDescription())) {
      try {
        Object[] titleAndDescription = (Object[]) hibernateTemplate.findByCriteria(DetachedCriteria.forClass(Article.class)
            .add(Restrictions.eq("doi", issue.getImageUri()))
            .setProjection(Projections.projectionList()
                    .add(Projections.property("title"))
                    .add(Projections.property("description"))
            )).get(0);
        issue.setTitle(StringUtils.isEmpty(issue.getTitle()) ? (String) titleAndDescription[0] : issue.getTitle());
        issue.setDescription(StringUtils.isEmpty(issue.getDescription()) ? (String) titleAndDescription[1] : issue.getDescription());
      } catch (IndexOutOfBoundsException e) {
        //it's fine if the image article doesn't exist
      }
    }
    storedVolume.getIssues().add(issue);
    hibernateTemplate.update(storedVolume);
  }

  @Override
  @Transactional
  public void updateIssue(String issueUri, String imageUri, String displayName,
                          boolean respectOrder, List<String> articleDois) {
    log.debug("Updating issue '{}'", issueUri);
    Issue issue;
    try {
      issue = (Issue) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Issue.class)
              .add(Restrictions.eq("issueUri", issueUri))
              .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      //if the issue doesn't exist, just return
      return;
    }
    //check that we aren't adding or removing an article here
    for (String oldDoi : issue.getArticleDois()) {
      if (!articleDois.contains(oldDoi)) {
        throw new IllegalArgumentException("Removed article '" + oldDoi + "' when updating issue");
      }
    }
    for (String newDoi : articleDois) {
      if (!issue.getArticleDois().contains(newDoi)) {
        throw new IllegalArgumentException("Added article '" + newDoi + "' when updating issue");
      }
    }

    issue.getArticleDois().clear();
    issue.getArticleDois().addAll(articleDois);
    issue.setDisplayName(displayName);
    issue.setRespectOrder(respectOrder);
    issue.setImageUri(imageUri);
    //pull down title and description from the image article, if it exists
    try {
      Object[] titleAndDescription = (Object[]) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Article.class)
              .add(Restrictions.eq("doi", imageUri))
              .setProjection(Projections.projectionList()
                  .add(Projections.property("title"))
                  .add(Projections.property("description")))
      ).get(0);
      issue.setTitle((String) titleAndDescription[0]);
      issue.setDescription((String) titleAndDescription[1]);
    } catch (IndexOutOfBoundsException e) {
      //it's ok if image article doesn't exist
    }
    hibernateTemplate.update(issue);
  }

  @Override
  @Transactional
  public void removeArticlesFromIssue(String issueUri, String... articleDois) {
    log.debug("Removing articles {} to issue '{}'", Arrays.toString(articleDois), issueUri);
    Issue issue;
    try {
      issue = (Issue) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Issue.class)
              .add(Restrictions.eq("issueUri", issueUri))
              .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      //it's ok if the issue doesn't exist
      return;
    }
    for (String doi : articleDois) {
      issue.getArticleDois().remove(doi);
    }
    hibernateTemplate.update(issue);
  }

  @Override
  @Transactional
  public void addArticlesToIssue(String issueUri, String... articleDois) {
    log.debug("Adding articles {} to issue '{}'", Arrays.toString(articleDois), issueUri);
    Issue issue;
    try {
      issue = (Issue) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Issue.class)
              .add(Restrictions.eq("issueUri", issueUri))
              .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      //it's ok if the issue doesn't exist
      return;
    }
    for (String doi : articleDois) {
      if (!doi.isEmpty()) {
        //Trim off extra spaces.  AMEC-2225
        doi = doi.trim();
        if (!issue.getArticleDois().contains(doi)) {
          issue.getArticleDois().add(doi);
        }
      }
    }
    hibernateTemplate.update(issue);
  }

  @Override
  public String formatArticleCsv(List<TOCArticleGroup> issueArticleGroups) {
    if (issueArticleGroups.isEmpty()) {
      return "";
    }
    String csv = "";
    for (TOCArticleGroup group : issueArticleGroups) {
      for (TOCArticle article : group.getArticles()) {
        csv += article.getDoi() + ",";
      }
    }
    return csv.substring(0, csv.lastIndexOf(","));
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<TOCArticleGroup> getArticleGroupList(final Issue issue) {
    //if the issue doesn't have any dois, return an empty list of groups
    if (issue.getArticleDois() == null || issue.getArticleDois().isEmpty()) {
      return Collections.emptyList();
    }

    //Create a comparator to sort articles in groups depending on if the issue has manual ordering enabled
    Comparator<TOCArticle> comparator;
    if (issue.isRespectOrder()) {
      comparator = new Comparator<TOCArticle>() {
        @Override
        public int compare(TOCArticle left, TOCArticle right) {
          Integer leftIndex = issue.getArticleDois().indexOf(left.getDoi());
          Integer rightIndex = issue.getArticleDois().indexOf(right.getDoi());
          return leftIndex.compareTo(rightIndex);
        }
      };
    } else {
      comparator = new Comparator<TOCArticle>() {
        @Override
        public int compare(TOCArticle left, TOCArticle right) {
          if (left.getDate().after(right.getDate())) {
            return -1;
          }
          if (left.getDate().before(right.getDate())) {
            return 1;
          }
          return left.getDoi().compareTo(right.getDoi());
        }
      };
    }

    //keep track of dois in a separate list so we can remove them as we find articles and then keep track of the orphans at the end
    List<String> dois = new ArrayList<String>(issue.getArticleDois());

    log.debug("Loading up article groups for issue '{}'", issue.getIssueUri());
    List<TOCArticleGroup> groups = new ArrayList<TOCArticleGroup>(ArticleType.getOrderedListForDisplay().size());

    List<Object[]> rows = (List<Object[]>) hibernateTemplate.findByNamedParam(
        "select a.doi, a.title, a.date, t from Article a inner join a.types t where a.doi in :dois",
        new String[]{"dois"},
        new Object[]{issue.getArticleDois()}
    );

    //results will be row of [doi, title, date, type] with an entry for each type of each article
    //i.e. we'll see duplicate results for articles

    for (ArticleType type : ArticleType.getOrderedListForDisplay()) {
      TOCArticleGroup group = new TOCArticleGroup(type);
      //using an explicit iterator so we can remove rows as we find matches
      Iterator<Object[]> iterator = rows.iterator();
      while (iterator.hasNext()) {
        Object[] row = iterator.next();
        //check if this row is of the correct type, and that we haven't added the article
        if (type.getUri().toString().equals(row[3]) && dois.contains(row[0])) {
          TOCArticle articleInfo = TOCArticle.builder()
              .setDoi((String) row[0])
              .setTitle((String) row[1])
              .setDate((Date) row[2])
              .build();
          group.addArticle(articleInfo);

          //remove the row so we don't have to check it again later
          iterator.remove();
          //remove the doi so we can keep track of orphans
          dois.remove(articleInfo.getDoi());
        }
      }
      Collections.sort(group.getArticles(), comparator);
      //only add a group if there are articles for it
      if (group.getCount() > 0) {
        groups.add(group);
        log.debug("Found {} articles of type '{}' for issue '{}",
            new Object[]{group.getCount(), type.getHeading(), issue.getIssueUri()});
      }
    }

    //create a group for orphaned articles
    TOCArticleGroup orphans = new TOCArticleGroup(null);
    orphans.setHeading("Orphaned Article");
    orphans.setPluralHeading("Orphaned Articles");

    //anything left in the doi list is an orphan
    for (String doi : dois) {
      TOCArticle article = TOCArticle.builder()
          .setDoi(doi)
          .setDate(Calendar.getInstance().getTime())
          .build();

      orphans.addArticle(article);
    }
    Collections.sort(orphans.getArticles(), comparator);

    groups.add(orphans);
    return groups;
  }

  @Override
  @Transactional(readOnly = true)
  public Journal getJournal(String journalKey) {
    try {
      return (Journal) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Journal.class)
              .add(Restrictions.eq("journalKey", journalKey))
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  @Override
  @Transactional
  public Set<Category> refreshSubjectCategories(String articleDoi, String authID) throws NoSuchArticleIdException, XPathExpressionException {
    // Attempt to assign categories to the article based on the taxonomy server.

    Document articleXml = fetchArticleService.getArticleDocument(new ArticleInfo(articleDoi));
    // update categories for non-amendment articles
    if (articleXml != null && !isAmendment(articleXml)) {
      Map<String, Integer> terms = null;

      try {
        terms = articleClassifier.classifyArticle(articleXml);
      } catch (Exception e) {
        log.warn("Taxonomy server not responding, but ingesting article anyway", e);
      }

      if (terms != null && terms.size() > 0) {
        Article article = articleService.getArticle(articleDoi, authID);
        return articleService.setArticleCategories(article, terms).keySet();
      }
    }

    return Collections.emptySet();
  }

  private void invokeOnCrossPubListeners(String articleDoi) throws Exception {
    if (onCrossPubListener != null) {
      for (OnCrossPubListener listener : onCrossPubListener) {
        listener.articleCrossPublished(articleDoi);
      }
    }
  }

  /**
   * @inheritDoc
   */
  @Transactional
  @Override
  public ArticleList createArticleList(final String journalKey, final String listKey, final String displayName) {
    if (StringUtils.isEmpty(journalKey)) {
      throw new IllegalArgumentException("No journal specified");
    } else if (StringUtils.isEmpty(listKey)) {
      throw new IllegalArgumentException("No listKey specified");
    }
    return hibernateTemplate.execute(new HibernateCallback<ArticleList>() {
      @Override
      public ArticleList doInHibernate(Session session) throws HibernateException, SQLException {
        Journal journal = (Journal) session.createCriteria(Journal.class)
            .add(Restrictions.eq("journalKey", journalKey))
            .uniqueResult();
        //if the journal doesn't exist, return null
        if (journal == null) {
          return null;
        } else {
          //check if a list with the same listKey exists, and if so, return null
          for (ArticleList existingList : journal.getArticleLists()) {
            if (existingList.getListKey().equals(listKey)) {
              return null;
            }
          }
          ArticleList newArticleList = new ArticleList();
          newArticleList.setListType(ARTICLE_LIST_TYPE);
          newArticleList.setListKey(listKey);
          newArticleList.setDisplayName(displayName);
          journal.getArticleLists().add(newArticleList);
          session.update(journal);
          return newArticleList;
        }
      }
    });
  }

  /**
   * @inheritDoc
   */
  @SuppressWarnings("unchecked")
  @Override
  @Transactional(readOnly = true)
  public Collection<ArticleList> getArticleLists(final String journalKey) {
    return hibernateTemplate.execute(new HibernateCallback<List<ArticleList>>() {
      @Override
      public List<ArticleList> doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.createQuery("" +
            "select l from Journal j inner join j.articleLists l " +
            "where (j.journalKey=:journalKey) and (l.listType=:listType) " +
            "order by l.listKey");
        query.setParameter("journalKey", journalKey);
        query.setParameter("listType", ARTICLE_LIST_TYPE);
        return query.list();
      }
    });
  }

  /**
   * @inheritDoc
   */
  @Transactional
  @Override
  public String[] deleteArticleList(final String journalKey, final String... listKey) {
    //article list are lazy, so we have to access them in a session
    return hibernateTemplate.execute(new HibernateCallback<String[]>() {
      @Override
      public String[] doInHibernate(Session session) throws HibernateException, SQLException {
        Journal journal = (Journal) session.createCriteria(Journal.class)
            .add(Restrictions.eq("journalKey", journalKey))
            .uniqueResult();
        if (journal == null) {
          throw new IllegalArgumentException("No such journal: " + journalKey);
        }
        List<String> deletedArticleList = new ArrayList<String>(listKey.length);
        Iterator<ArticleList> iterator = journal.getArticleLists().iterator();
        while (iterator.hasNext()) {
          ArticleList articleList = iterator.next();
          if (ArrayUtils.indexOf(listKey, articleList.getListKey()) != -1) {
            iterator.remove();
            session.delete(articleList);
            deletedArticleList.add(articleList.getListKey());
          }
        }
        session.update(journal);
        return deletedArticleList.toArray(new String[deletedArticleList.size()]);
      }
    });
  }

  /**
   * @inheritDoc
   */
  @Override
  @Transactional(readOnly = true)
  public ArticleList getList(String listKey) {
    log.debug("Retrieving list with listKey '{}'", listKey);
    return (ArticleList) DataAccessUtils.uniqueResult(hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(ArticleList.class)
            .add(Restrictions.eq("listKey", listKey))
            .add(Restrictions.eq("listType", ARTICLE_LIST_TYPE))
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
    ));
  }

  private boolean containsDoi(Collection<Article> articles, String doi) {
    for (Article article : articles) {
      if (article.getDoi().equals(doi)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @inheritDoc
   */
  @Override
  @Transactional
  public Collection<String> addArticlesToList(String listKey, String... articleDois) {
    log.debug("Adding articles {} to list '{}'", Arrays.toString(articleDois), listKey);
    ArticleList articleList = getList(listKey);
    Collection<String> orphanedDois = new ArrayList<String>();
    for (String doi : articleDois) {
      if (!doi.isEmpty()) {
        //Trim off extra spaces.  AMEC-2225
        doi = doi.trim();
        if (!containsDoi(articleList.getArticles(), doi)) {
          Article article = (Article) DataAccessUtils.uniqueResult(hibernateTemplate.findByCriteria(
              DetachedCriteria.forClass(Article.class)
                  .add(Restrictions.eq("doi", doi))));
          if (article == null) {
            orphanedDois.add(doi);
          } else {
            articleList.getArticles().add(article);
          }
        }
      }
    }
    hibernateTemplate.update(articleList);
    return orphanedDois;
  }

  /**
   * @inheritDoc
   */
  @Override
  @Transactional
  public void removeArticlesFromList(String listKey, String... articleDois) {
    log.debug("Removing articles {} to article list '{}'", Arrays.toString(articleDois), listKey);
    ArticleList articleList = getList(listKey);
    Set<String> doisToRemove = Sets.newLinkedHashSetWithExpectedSize(articleDois.length);
    for (String doi : articleDois) {
      doi = doi.trim(); //Trim off extra spaces.  AMEC-2225
      doisToRemove.add(doi);
    }
    for (Iterator<Article> iterator = articleList.getArticles().iterator(); iterator.hasNext(); ) {
      if (doisToRemove.contains(iterator.next().getDoi())) {
        iterator.remove();
      }
    }
    hibernateTemplate.update(articleList);
  }

  /**
   * @inheritDoc
   */
  @Override
  @Transactional
  public void updateList(String listKey, String displayName, List<String> articleDois) {
    log.debug("Updating list '{}'", listKey);
    ArticleList articleList = getList(listKey);

    final Map<String, Integer> orderedDois = Maps.newHashMapWithExpectedSize(articleDois.size());
    int index = 0;
    for (String doi : articleDois) {
      if (!doi.isEmpty()) {
        doi = doi.trim(); //Trim off extra spaces.  AMEC-2225
        orderedDois.put(doi, index++);
      }
    }

    Set<String> oldDois = Sets.newHashSetWithExpectedSize(articleList.getArticles().size());
    for (Article article : articleList.getArticles()) {
      oldDois.add(article.getDoi());
    }

    //check that we aren't adding or removing an article here
    for (String oldDoi : Sets.difference(oldDois, orderedDois.keySet())) {
      throw new IllegalArgumentException("Removed article '" + oldDoi + "' when updating list");
    }
    for (String newDoi : Sets.difference(orderedDois.keySet(), oldDois)) {
      throw new IllegalArgumentException("Added article '" + newDoi + "' when updating list");
    }

    Collections.sort(articleList.getArticles(), new Comparator<Article>() {
      @Override
      public int compare(Article o1, Article o2) {
        return orderedDois.get(o1.getDoi()) - orderedDois.get(o2.getDoi());
      }
    });

    articleList.setDisplayName(displayName);

    hibernateTemplate.update(articleList);
  }

  /**
   * @inheritDoc
   */
  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<ArticleInfo> getArticleList(final ArticleList articleList) {
    //if the list doesn't have any articles, return an empty list of groups
    if (articleList.getArticles() == null || articleList.getArticles().isEmpty()) {
      return Collections.emptyList();
    }

    List<ArticleInfo> result = new ArrayList<ArticleInfo>();

    for (Article article : articleList.getArticles()) {
      ArticleInfo articleInfo = new ArticleInfo();
      articleInfo.setDoi(article.getDoi());
      articleInfo.setTitle(article.getTitle());
      result.add(articleInfo);
    }

    return result;
  }

  /**
   * Checks whether an article is an amendment using the article-type attribute in the article xml
   *
   * @param articleXml the article xml
   * @return true if the article is an amendment; false, otherwise
   * @throws XPathExpressionException
   */
  private boolean isAmendment(Document articleXml) throws XPathExpressionException {
    XPathUtil xPathUtil = new XPathUtil();
    String expression = "/article/@article-type";
    String articleType = xPathUtil.evaluate(articleXml, expression);
    if (ARTICLE_TYPE.contains(articleType.toLowerCase())) {
      return true;
    }
    return false;
  }
}
