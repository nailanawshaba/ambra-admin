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
import org.ambraproject.models.*;
import org.ambraproject.modelsdeprecated.UserProfile;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA. User: alex Date: 3/29/12 Time: 12:11 PM To change this template use File | Settings |
 * File Templates.
 */
public class SaveAnnotationActionTest extends AdminWebTest {
  @Autowired
  protected SaveAnnotationAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @BeforeMethod
  public void resetAction() {
    action.setModel(new Annotation());
  }

  @Test
  public void testExecute() throws Exception {
    Long creatorID = 6648L;
    Article article = new Article("id:doi-SaveAnnotationActionTest");
    dummyDataStore.store(article);

    Annotation original = new Annotation(creatorID, AnnotationType.COMMENT, article.getID());
    original.setBody("After surviving the horrific car crash, Elena, still shaken by her resemblance to " +
        "Katherine Pierce, is rescued by Damon who takes on her on a road trip to Georgia where he meets " +
        "with an old friend of his, a witch/barmaid named Bree, to ask for her help on a spell that could " +
        "free Katherine from her tomb. Back in Mystic Falls, Stefan tries to help Bonnie understand her " +
        "wican powers and gets to meet her grandmother Tituba. Meanwhile, Jeremy meets a new local girl, " +
        "named Anna, who give him insight on the vampire legends about the town as he continues to research his ...");
    original.setAnnotationUri("id:annotationUriToChange");

    dummyDataStore.store(original);

    action.setAnnotationId(original.getID());
    action.getModel().setBody("New body");
    action.getModel().setAnnotationUri("new annotation uri");

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));
    assertEquals(action.getAnnotationId(), original.getID(), "Action changed annotation id");

    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, action.getAnnotationId());
    assertNotNull(storedAnnotation, "Annotation got deleted");
    assertEquals(storedAnnotation.getArticleID(), original.getArticleID(), "Action changed article id");
    assertEquals(storedAnnotation.getType(), original.getType(), "Action changed type");
    assertEquals(storedAnnotation.getParentID(), original.getParentID(), "Action changed parent id");

    assertEquals(storedAnnotation.getBody(), action.getModel().getBody(), "Action didn't update annotation body");
    assertEquals(storedAnnotation.getAnnotationUri(), action.getModel().getAnnotationUri(), "Action didn't update annotation uri");
  }
}
