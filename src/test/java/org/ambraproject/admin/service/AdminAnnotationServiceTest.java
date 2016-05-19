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

package org.ambraproject.admin.service;

import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.modelsdeprecated.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alex Kudlick 3/28/12
 */
public class AdminAnnotationServiceTest extends AdminBaseTest {

  @Autowired
  protected AdminAnnotationService annotationService;

  @DataProvider(name = "annotationsToEdit")
  public Object[][] getAnnotationsToEdit() {
    Long userProfileID = 6623L;

    Article article = new Article("id:doi-for-editAnnotation");
    article.setTitle("Article title");
    article.seteIssn("1241-213875");
    article.setDate(Calendar.getInstance().getTime());
    article.setJournal("Test journal");
    article.setCollaborativeAuthors(Arrays.asList("The Skoll Foundation", "The Fu Foundation"));
    article.setVolume("volume");
    article.setUrl("http://dx.doi.org/foo");
    article.setAuthors(Arrays.asList(
        new ArticleAuthor("Foo","McFoo","F.o.o."), 
        new ArticleAuthor("James","McCoy","Mr."))
    );
    dummyDataStore.store(article);

    Annotation originalComment = new Annotation(userProfileID, AnnotationType.COMMENT, article.getID());
    originalComment.setTitle("Old comment title");
    originalComment.setAnnotationUri("old comment annotation uri");
    originalComment.setBody("Old comment annotation body");
    originalComment.setCompetingInterestBody("old comment competing interest");
    dummyDataStore.store(originalComment);

    //Just change the basic annotation properties
    Annotation changeBasicProperties = new Annotation(userProfileID, AnnotationType.COMMENT, article.getID());
    changeBasicProperties.setTitle("Change Basic Properties Title");
    changeBasicProperties.setAnnotationUri("Change Basic Properties annotation uri");
    changeBasicProperties.setBody("Change Basic Properties body");
    changeBasicProperties.setCompetingInterestBody("Change Basic Properties competing interest");

    //just want to check that parent ids don't get overwritten
    Annotation originalReply = new Annotation(userProfileID, AnnotationType.REPLY, article.getID());
    originalReply.setParentID(originalComment.getID());
    dummyDataStore.store(originalReply);

    Annotation newReply = new Annotation(userProfileID, AnnotationType.REPLY, article.getID());
    newReply.setParentID(originalReply.getID());

    return new Object[][]{
        {originalComment, changeBasicProperties},
        {originalReply, newReply}
    };
  }

  @Test(dataProvider = "annotationsToEdit")
  public void testEditAnnotation(Annotation originalAnnotation, Annotation newAnnotation) {
    annotationService.editAnnotation(originalAnnotation.getID(), newAnnotation);
    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, originalAnnotation.getID());
    assertNotNull(storedAnnotation, "deleted stored annotation");
    //properties that shouldn't have changed
    assertEquals(storedAnnotation.getArticleID(), originalAnnotation.getArticleID(), "Edit changed article Id");
    assertEquals(storedAnnotation.getType(), originalAnnotation.getType(), "Edit changed type");
    assertEquals(storedAnnotation.getParentID(), originalAnnotation.getParentID(), "Edit changed parent id");
    assertNotNull(storedAnnotation.getUserProfileID(), "Edit deleted creator");
    assertEquals(storedAnnotation.getUserProfileID(), originalAnnotation.getUserProfileID(), "Edit changed creator");

    //properties that should have changed
    assertEquals(storedAnnotation.getTitle(), newAnnotation.getTitle(), "Edit didn't update title");
    assertEquals(storedAnnotation.getBody(), newAnnotation.getBody(), "Edit didn't update body");
    assertEquals(storedAnnotation.getCompetingInterestBody(), newAnnotation.getCompetingInterestBody(),
        "Edit didn't update competing interest statement");
  }
}
