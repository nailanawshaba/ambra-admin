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

package org.ambraproject.admin.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.modelsdeprecated.UserProfile;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alex Kudlick 3/27/12
 */
public class LoadAnnotationActionTest extends AdminWebTest {

  @Autowired
  protected LoadAnnotationAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @DataProvider(name = "annotation")
  public Object[][] getAnnotation() throws ParseException {
    Long creatorID = 6628L;

    Article article = new Article("id:doi-for-LoadAnnotationActionTest");
    article.setIssue("issue");
    article.setVolume("volume");
    article.setTitle("The Night of the Comet");
    article.setJournal("Vampire Diaries");
    article.setDate(new SimpleDateFormat("yyyy-mm-dd").parse("2009-09-17"));
    article.setAuthors(Arrays.asList(
        new ArticleAuthor("Jeremy", "Gilbert", "Std."),
        new ArticleAuthor("Bonnie", "Bennet", "Wtch."))
    );
    article.seteIssn("1234");
    dummyDataStore.store(article);

    Annotation comment = new Annotation(creatorID, AnnotationType.COMMENT, article.getID());
    comment.setAnnotationUri("id:comment-for-LoadAnnotationActionTest");
    comment.setTitle("Test annotation title");
    comment.setBody("This is a test comment");
    dummyDataStore.store(comment);

    Annotation reply = new Annotation(creatorID, AnnotationType.REPLY, article.getID());
    reply.setAnnotationUri("id:reply-for-LoadAnnotationActionTest");
    reply.setTitle("Test reply title");
    reply.setBody("This is a test reply");
    reply.setParentID(comment.getID());
    dummyDataStore.store(reply);

    return new Object[][]{
        {comment},
        {reply}
    };
  }

  @Test(dataProvider = "annotation")
  public void testLoadById(Annotation annotation) throws Exception {
    action.setAnnotationUri(null);
    action.setAnnotationId(annotation.getID());

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));
    assertNotNull(action.getAnnotation(), "Action had null annotation");
    checkAnnotationProperties(action.getAnnotation(), annotation);
  }

  @Test(dataProvider = "annotation")
  public void testLoadByUri(Annotation annotation) throws Exception {
    action.setAnnotationUri(annotation.getAnnotationUri());
    action.setAnnotationId(null);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));
    assertNotNull(action.getAnnotation(), "Action had null annotation");
    checkAnnotationProperties(action.getAnnotation(), annotation);
  }
}
