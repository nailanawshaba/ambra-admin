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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.admin.action;

import com.google.common.base.Charsets;
import org.ambraproject.service.article.ArticleClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.ByteArrayOutputStream;

/**
 * A simple class to expose underlying data relating to the taxonomy server to the end user
 */
public class TestThesaurusAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(TestThesaurusAction.class);

  String articleDOI;
  String thesaurus;
  String results;

  ArticleClassifier articleClassifier;

  @Override
  public String execute() {
    //TODO: Consider improving security here
    return SUCCESS;
  }

  public String test() {
    try {
      if((articleDOI != null && articleDOI.trim().length() > 0) && (thesaurus != null && thesaurus.trim().length() > 0)) {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        //TODO: Input verification on the thesaurus?  Currently error handling is not so great

        articleClassifier.testThesaurus(bs, this.articleDOI, this.thesaurus);

        //TODO: We could probably do a better job at representing this data in the UI versus
        //dumping all the output to a textarea

        results = new String(bs.toByteArray(), Charsets.UTF_8);
      } else {
        addActionError("You must fill in values for articleID and thesaurus");
      }
    } catch (Exception ex) {
      log.warn(ex.getMessage(), ex);
      addActionError(ex.getMessage());
      return ERROR;
    }

    return SUCCESS;
  }

  public String getArticleDOI() {
    return articleDOI;
  }

  public void setArticleDOI(String articleDOI) {
    this.articleDOI = articleDOI;
  }

  public String getThesaurus() {
    return thesaurus;
  }

  public void setThesaurus(String thesaurus) {
    this.thesaurus = thesaurus;
  }

  public String getResults() {
    return results;
  }

  @Required
  public void setArticleClassifier(ArticleClassifier articleClassifier) {
    this.articleClassifier = articleClassifier;
  }
}

