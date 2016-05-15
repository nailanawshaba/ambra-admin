/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.flags.service;

import org.ambraproject.admin.service.impl.NedServiceImpl;
import org.ambraproject.admin.views.FlagView;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.Article;
import org.ambraproject.models.Flag;
import org.ambraproject.models.FlagReasonCode;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.service.cache.Cache;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.plos.ned_client.ApiException;
import org.plos.ned_client.api.IndividualsApi;
import org.plos.ned_client.model.Auth;
import org.plos.ned_client.model.Email;
import org.plos.ned_client.model.IndividualComposite;
import org.plos.ned_client.model.Individualprofile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.math.BigInteger;

/**
 * @author Alex Kudlick 3/23/12
 */
public class FlagServiceImpl extends HibernateServiceImpl implements FlagService {

  private static final Logger log = LoggerFactory.getLogger(FlagServiceImpl.class);

  private Cache articleHtmlCache;

  private NedServiceImpl nedService;

  public void setNedService(NedServiceImpl nedService) {
    this.nedService = nedService;
  }

  @Required
  public void setArticleHtmlCache(Cache articleHtmlCache) {
    this.articleHtmlCache = articleHtmlCache;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<FlagView> getFlaggedComments() {
    log.info("Loading up all flagged annotations");

    return (List<FlagView>) hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {

        StringBuilder sqlQuery = new StringBuilder();

        sqlQuery.append("SELECT a.annotationFlagID, a.created, a.reason, a.userProfileID, " +
            "b.annotationID, b.type, b.title, a.comment " +
            "FROM annotationFlag a, annotation b WHERE a.annotationID=b.annotationID ORDER BY created");

        SQLQuery query = session.createSQLQuery(sqlQuery.toString());
        List<Object[]> flagList = query.list();
        log.info("Found {} flagged annotations", flagList.size());

        List<FlagView> results = new ArrayList(flagList.size());

        for (Object[] obj : flagList) {
          Flag f = new Flag();
          f.setID(((Number) obj[0]).longValue());
          f.setCreated((java.util.Date) obj[1]);
          f.setReason(FlagReasonCode.fromString((String) obj[2]));
          f.setUserProfileID(((Number) obj[3]).longValue());
          f.setAnnotationID(((Number) obj[4]).longValue());
          f.setAnnotationType(AnnotationType.fromString((String) obj[5]));
          f.setAnnotationTitle((String) obj[6]);
          f.setComment((String) obj[7]);

          try {
            IndividualsApi individualsApi = nedService.getIndividualsApi();
            List<Individualprofile> ipList = new ArrayList<Individualprofile>();

            ipList = individualsApi.getProfiles(f.getUserProfileID().intValue());

            if ( ipList.size() > 0 ) {
              Individualprofile ip = ipList.get(0);
              f.setDisplayName(ip.getDisplayname());
            }
          }
          catch (ApiException apiEx) {
            log.error("getFlaggedComments() code: " + apiEx.getCode());
            log.error("getFlaggedComments() responseBody: " + apiEx.getResponseBody());
            log.error("getFlaggedComments() f.getUserProfileID(): " + f.getUserProfileID());
          }
          catch (Exception ex) {
            log.error(ex.getMessage(), ex);
          }

          if ( f.getDisplayName() == null ) {
            continue;
          }

          results.add(new FlagView(f));
        };

        return results;
      }
    });

  }

  @Override
  public void deleteFlags(Long... flagIds) {
    log.info("Removing flags: {}", Arrays.toString(flagIds));

    final String flags = Arrays.toString(flagIds);

    hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        session.createSQLQuery("DELETE FROM annotationFlag WHERE annotationFlagID in (" +
                flags.substring(1,flags.length()-1) + ")").executeUpdate();
        return null;
      }
    });

  }

  /**
   * Delete comment by id
   * @param commentIds the ids of the comment to be deleted
   */

  @Override
  public void deleteFlagAndComment(Long... commentIds) {
    log.info("Removing comments and associated flags for flagId: {}", Arrays.toString(commentIds));

    final String comments = Arrays.toString(commentIds);

    hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        StringBuilder sqlQuery = new StringBuilder();

        sqlQuery.append("SELECT annotationID, created FROM annotationFlag WHERE annotationFlagId IN (" +
            comments.substring(1,comments.length()-1) + ")");
        log.info(sqlQuery.toString());
        SQLQuery query = session.createSQLQuery(sqlQuery.toString());
        List<Object[]> annotationList = query.list();
        log.info("Found {} annotations", annotationList.size());

        StringBuilder sb = new StringBuilder();
        for (Object[] obj : annotationList) {
          sb.append(obj[0]);
          sb.append(",");
        }

        String str1 = "DELETE FROM annotationFlag WHERE annotationID IN (" + sb.substring(0,sb.length()-1) + ")";
        log.info(str1);
        session.createSQLQuery(str1).executeUpdate();

        String str2 = "DELETE FROM annotation WHERE parentID IN (" + sb.substring(0,sb.length()-1) + ")";
        log.info(str2);
        session.createSQLQuery(str2).executeUpdate();

        String str3 = "DELETE FROM annotation WHERE annotationID IN (" + sb.substring(0,sb.length()-1) + ")";
        log.info(str3);
        session.createSQLQuery(str3).executeUpdate();

        return null;
      }
    });
  }

  private String getArticleDoi(Annotation annotation) {
    return (String) hibernateTemplate.findByCriteria(
              DetachedCriteria.forClass(Article.class)
                  .add(Restrictions.eq("ID", annotation.getArticleID()))
                  .setProjection(Projections.property("doi"))
          ).get(0);
  }

  /**
   * Delete the comment and their children
   * @param annotation
   */
  public void listDeleteCommentTree(Annotation annotation) {

    //get all the children of the this annotation   
    List<Annotation> annotationChild = (List<Annotation>) hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Annotation.class)
            .add(Restrictions.eq("parentID", annotation.getID()))
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
    );

    //check if child has more child and if yes again call this method to get the child list
    if(annotationChild.size() > 0) {
      for(Annotation annotation1: annotationChild) {
        listDeleteCommentTree(annotation1);
      }
    }

    //now delete the annotation
    hibernateTemplate.delete(annotation);
  }
}
