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

package org.ambraproject.admin.flags.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.Flag;
import org.ambraproject.models.FlagReasonCode;
import org.ambraproject.modelsdeprecated.UserProfile;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author Alex Kudlick 3/26/12
 */
public class ProcessFlagsActionTest extends AdminWebTest {

  private static final Long[] EMPTY_ARRAY = new Long[0];
  @Autowired
  protected ProcessFlagsAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @BeforeMethod
  public void resetAction() {
    action.setCommentsToDelete(EMPTY_ARRAY);
    action.setCommentsToUnflag(EMPTY_ARRAY);
  }

  @DataProvider(name = "flags")
  public Object[][] getFlags() {
    Long creatorID = 6630L;

    Article article = new Article("id:doi-for-ProcessFlagsActionTest");
    article.setAuthors(Arrays.asList(
        new ArticleAuthor("Stefan", "Salvatore", "Vmp."),
        new ArticleAuthor("Damon", "Salvatore", "Vmp.")
    ));
    dummyDataStore.store(article);

    List<Long> annotationIds = new ArrayList<Long>(4);
    List<Long> flagIds = new ArrayList<Long>(4);

    Annotation comment = new Annotation(creatorID, AnnotationType.COMMENT, article.getID());
    dummyDataStore.store(comment);
    annotationIds.add(comment.getID());

    Flag flagComment = new Flag(creatorID, FlagReasonCode.INAPPROPRIATE, comment);
    dummyDataStore.store(flagComment);
    flagIds.add(flagComment.getID());

    Annotation reply = new Annotation(creatorID, AnnotationType.REPLY, article.getID());
    dummyDataStore.store(reply);
    annotationIds.add(reply.getID());

    Flag flagReply = new Flag(creatorID, FlagReasonCode.SPAM, reply);
    dummyDataStore.store(flagReply);
    flagIds.add(flagReply.getID());

    return new Object[][]{
        {flagIds, annotationIds}
    };
  }

  @Test(dataProvider = "flags")
  public void testDeleteFlags(List<Long> flagIds, List<Long> annotationIds) throws Exception {
    action.setCommentsToUnflag(flagIds.toArray(new Long[flagIds.size()]));

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));

    for (Long flagId : flagIds) {
      assertNull(dummyDataStore.get(Flag.class, flagId), "Didn't delete flag: " + flagId);
    }
    for (Long annotationId : annotationIds) {
      assertNotNull(dummyDataStore.get(Annotation.class, annotationId), "Deleted annotation: " + annotationId);
    }
  }
}
