/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.action;

import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleList;
import org.ambraproject.views.article.ArticleInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Manage list of articles in a article list
 */
public class ArticleManagementAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ArticleManagementAction.class);

  public static final String ARTICLE_LIST_TYPE = "admin"; // From Ambra's migrate_ambra_1007.sql

  // Fields set by templates
  private String command;
  private String listKey;
  private String displayName;
  private String articlesToAddCsv;
  private String[] articlesToRemove;

  // Fields Used by template
  private ArticleList articleList;
  private String articleOrderCSV;
  private List<ArticleInfo> articleInfoList;

  public enum ImCommands {
    ADD_ARTICLE,
    REMOVE_ARTICLES,
    UPDATE_LIST,
    INVALID;

    public static ImCommands toCommand(String command) {
      ImCommands a;
      try {
        a = valueOf(command);
      } catch (Exception e) {
        // It's ok just return invalid.
        a = INVALID;
      }
      return a;
    }
  }

  @Override
  public String execute() throws Exception {

    switch (ImCommands.toCommand(command)) {
      case ADD_ARTICLE:
        addArticles();
        break;

      case REMOVE_ARTICLES:
        removeArticles();
        break;

      case UPDATE_LIST:
        updateList();
        break;

      case INVALID:
        repopulate();
        break;
    }
    return SUCCESS;
  }

  /**
   * add articles to article list
   */
  private void addArticles() {
    if (articlesToAddCsv != null && !articlesToAddCsv.isEmpty()) {
      Collection<String> orphanDois = Collections.emptyList();
      try {
        orphanDois = adminService.addArticlesToList(listKey, articlesToAddCsv.split(","));
      } catch (IllegalArgumentException e) {
        log.error("Failed to add article(s) '" + articlesToAddCsv + "' to list " + listKey, e);
        addActionMessage("Article(s) not added due to the following error: " + e.getMessage());
      }
      if (orphanDois.size() < articlesToAddCsv.length()) {
        addActionMessage("Successfully added articles to list");
      }
      for (String orphanDoi : orphanDois) {
        addActionError("Article not found: " + orphanDoi);
      }
    }
    repopulate();
  }

  /**
   * remove articles from article list
   */
  private void removeArticles() {
    try {
      adminService.removeArticlesFromList(listKey, articlesToRemove);
      addActionMessage("Removed the following article(s) from list: " + Arrays.toString(articlesToRemove));
    } catch (IllegalArgumentException e) {
      log.error("Failed to remove articles " + Arrays.toString(articlesToRemove) + " from list " + listKey, e);
      addActionMessage("Article(s) not removed due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  /**
   * update order of articles in article list
   */
  private void updateList() {
    try {
      adminService.updateList(listKey, displayName, Arrays.asList(articleOrderCSV.split(",")));
      addActionMessage("Successfully updated list " + listKey);
    } catch (IllegalArgumentException e) {
      log.error("Failed to update list '" + listKey + "'", e);
      addActionError("Article List not updated due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  private void repopulate() {
    articleList = adminService.getList(listKey);
    articleInfoList = adminService.getArticleList(articleList);
    articleOrderCSV = formatArticleDoiCsv(articleList.getArticles());
    initJournal();
  }

  public String formatArticleDoiCsv(List<Article> articleDois) {
    if (articleDois.isEmpty()) {
      return "";
    }
    String[] dois = new String[articleDois.size()];
    int i = 0;
    for (Article article : articleDois) {
      dois[i++] = article.getDoi();
    }

    return StringUtils.join(dois, ',');
  }

  public String getListKey() {
    return listKey;
  }

  public void setListKey(String listKey) {
    this.listKey = listKey;
  }

  public ArticleList getArticleList() {
    return articleList;
  }

  public void setArticleList(ArticleList articleList) {
    this.articleList = articleList;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String name) {
    displayName = name;
  }

  public List<ArticleInfo> getArticleInfoList() {
    return articleInfoList;
  }

  public void setArticleInfoList(List<ArticleInfo> articleInfoList) {
    this.articleInfoList = articleInfoList;
  }

  public String getArticleOrderCSV() {
    return articleOrderCSV;
  }

  public void setArticleOrderCSV(String articleOrderCSV) {
    this.articleOrderCSV = articleOrderCSV;
  }

  public void setArticlesToAddCsv(String articlesToAddCsv) {
    this.articlesToAddCsv = articlesToAddCsv;
  }

  public void setArticlesToRemove(String[] articlesToRemove) {
    if (articlesToRemove != null) {
      this.articlesToRemove = articlesToRemove.clone();
    } else {
      this.articlesToRemove = new String[0];
    }
  }

  public void setCommand(String command) {
    this.command = command;
  }
}