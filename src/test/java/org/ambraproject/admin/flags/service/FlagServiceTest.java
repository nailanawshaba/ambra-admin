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

import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.admin.views.FlagView;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.Flag;
import org.ambraproject.models.FlagReasonCode;
import org.ambraproject.models.UserProfile;
import org.ambraproject.service.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;
import java.util.Calendar;
import java.util.Collection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author Alex Kudlick 3/23/12
 */
public class FlagServiceTest extends AdminBaseTest {

  @Autowired
  protected FlagService flagService;
  
  @Autowired
  @Qualifier("articleHtmlCache")
  protected Cache articleHtmlCache; //just used to check that articles get kicked out of the cache when they should

  @Test
  public void testGetFlaggedComments() {
    //make sure there aren't any other flags in the db stored by other tests
    dummyDataStore.deleteAll(Flag.class);

    //set up data
    UserProfile commentCreator = new UserProfile(
        "id:creatorForFlagManagementServiceTest",
        "email@FlagManagementServiceTest.org",
        "displaynameForFlagManagementServiceTest");
    dummyDataStore.store(commentCreator);

    UserProfile flagCreator = new UserProfile(
        "flagCreatorForFlagManagementServiceTest",
        "flagCreator@FlagManagementServiceTest.org",
        "flagCreatorForFlagManagementServiceTest");
    dummyDataStore.store(flagCreator);

    Annotation comment = new Annotation(commentCreator, AnnotationType.COMMENT, 123l);
    comment.setTitle("test title for flagManagementServiceTest");
    dummyDataStore.store(comment);

    Annotation reply = new Annotation(commentCreator, AnnotationType.REPLY, 123l);
    reply.setTitle("test title for reply on flagManagementServiceTest");
    dummyDataStore.store(reply);

    Calendar lastYear = Calendar.getInstance();
    lastYear.add(Calendar.YEAR, -1);

    Calendar lastMonth = Calendar.getInstance();
    lastMonth.add(Calendar.MONTH, -1);

    Flag firstFlag = new Flag(flagCreator, FlagReasonCode.SPAM, comment);
    firstFlag.setComment("Spamalot");
    firstFlag.setCreated(lastYear.getTime());
    dummyDataStore.store(firstFlag);

    Flag secondFlag = new Flag(flagCreator, FlagReasonCode.INAPPROPRIATE, reply);
    secondFlag.setComment("inappropriate");
    secondFlag.setCreated(lastMonth.getTime());
    dummyDataStore.store(secondFlag);

    //call the service method
    Collection<FlagView> list = flagService.getFlaggedComments();

    assertNotNull(list, "returned null list of flagged comments");
    assertEquals(list.toArray(), new Object[]{
        new FlagView(firstFlag),
        new FlagView(secondFlag)
      //  new FlagView(thirdFlag)
    }, "Incorrect flags");

  }

  @Test
  public void testDeleteFlags() {
    UserProfile creator = new UserProfile(
        "id:creatorForDeleteFlagsServiceTest",
        "email@DeleteFlagsServiceTest.org",
        "displaynameForDeleteFlagsServiceTest");
    dummyDataStore.store(creator);

    Annotation annotation = new Annotation(creator, AnnotationType.COMMENT, 12l);
    dummyDataStore.store(annotation);

    Flag flag1 = new Flag(creator, FlagReasonCode.SPAM, annotation);
    Long id1 = Long.valueOf(dummyDataStore.store(flag1));

    Flag flag2 = new Flag(creator, FlagReasonCode.OFFENSIVE, annotation);
    Long id2 = Long.valueOf(dummyDataStore.store(flag2));

    flagService.deleteFlags(id1, id2);

    assertNull(dummyDataStore.get(Flag.class, id1), "didn't delete first flag");
    assertNull(dummyDataStore.get(Flag.class, id2), "didn't delete second flag");
  }

  @Test
  public void testDeleteComment(){

    dummyDataStore.deleteAll(Flag.class);
    dummyDataStore.deleteAll(Annotation.class);

    UserProfile creator = new UserProfile(
        "id:creatorForDeleteFlagsServiceTest",
        "email@DeleteFlagsServiceTest.org",
        "displaynameForDeleteFlagsServiceTest");
    dummyDataStore.store(creator);

    Article article = new Article("id:doi-for-delete-comment-by-service");
    dummyDataStore.store(article);

    Article noteArticle = new Article("id:article-with-note-to-delete");
    dummyDataStore.store(noteArticle);

    Annotation comment1 = new Annotation(creator, AnnotationType.COMMENT, article.getID());
    dummyDataStore.store(comment1);
    
    Annotation reply = new Annotation(creator, AnnotationType.REPLY, article.getID());
    reply.setParentID(comment1.getID());
    dummyDataStore.store(reply);

    Annotation note = new Annotation(creator, AnnotationType.COMMENT, noteArticle.getID());
    dummyDataStore.store(note);

    Flag flag1 = new Flag(creator, FlagReasonCode.OFFENSIVE, comment1);
    flag1.setFlaggedAnnotation(comment1);
    Long id1 = Long.valueOf(dummyDataStore.store(flag1));

    Flag flag2 = new Flag(creator, FlagReasonCode.OTHER, note);
    flag2.setFlaggedAnnotation(note);
    Long id2 = Long.valueOf(dummyDataStore.store(flag2));

    Flag flag3 = new Flag(creator, FlagReasonCode.SPAM, note);
    flag3.setFlaggedAnnotation(note);
    Long id3 = Long.valueOf(dummyDataStore.store(flag3));

    flagService.deleteFlagAndComment(id1, id2);

    assertNull(dummyDataStore.get(Flag.class, id1), "didn't delete first flag");
    assertNull(dummyDataStore.get(Flag.class, id2), "didn't delete second flag");
    assertNull(dummyDataStore.get(Flag.class, id3), "didn't delete third flag");

    assertNull(dummyDataStore.get(Annotation.class, comment1.getID()), "didn't delete first annotation");
    assertNull(dummyDataStore.get(Annotation.class, reply.getID()), "didn't delete second annotation");
    assertNull(dummyDataStore.get(Annotation.class, note.getID()), "didn't delete third annotation");
  }

}
