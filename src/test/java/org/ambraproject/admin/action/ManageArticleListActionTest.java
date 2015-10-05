package org.ambraproject.admin.action;

import com.google.common.collect.ImmutableSet;
import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.models.ArticleList;
import org.ambraproject.models.Journal;
import org.ambraproject.web.VirtualJournalContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ManageArticleListActionTest extends AdminWebTest {

  private static Map<String, ArticleList> byListCode(Collection<ArticleList> articleLists) {
    Map<String, ArticleList> map = new TreeMap<String, ArticleList>();
    for (ArticleList articleList : articleLists) {
      map.put(articleList.getListCode(), articleList);
    }
    return map;
  }

  @Autowired
  protected ManageArticleListAction action;

  @DataProvider(name = "basicInfo")
  public Object[][] getCurrentArticleList() {
    Journal journal = new Journal();
    journal.setJournalKey("journalForTestManageArticleLists");
    journal.seteIssn("eIssnjournalForTestManageArticleLists");
    journal.setArticleLists(new ArrayList<ArticleList>(3));

    for (int i = 1; i <= 3; i++) {
      ArticleList articleList = new ArticleList();
      articleList.setListType(ArticleManagementAction.ARTICLE_LIST_TYPE);
      articleList.setDisplayName("news" + i);
      articleList.setListCode("id:fake-list-for-manage-journals" + i);
      dummyDataStore.store(articleList);
      journal.getArticleLists().add(dummyDataStore.get(ArticleList.class, articleList.getID()));
    }

    dummyDataStore.store(journal);

    return new Object[][]{
        {journal}
    };
  }

  @Test(dataProvider = "basicInfo")
  public void testExecute(Journal journal) throws Exception {
    //make sure to use a journal for this test
    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");

    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default execute");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    Map<String, ArticleList> actualLists = byListCode(action.getArticleList());
    Map<String, ArticleList> expectedLists = byListCode(journal.getArticleLists());
    assertEquals(actualLists.keySet(), expectedLists.keySet(), "Action returned incorrect article lists");
    for (String listCode : expectedLists.keySet()) {
      ArticleList actual = actualLists.get(listCode);
      ArticleList expected = expectedLists.get(listCode);
      assertEquals(actual.getDisplayName(), expected.getDisplayName(),
          "Article List " + listCode + " didn't have correct display name");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testCreateArticleList(Journal journal) throws Exception {
    Map<String, ArticleList> initialArticleLists = byListCode(dummyDataStore.get(Journal.class, journal.getID()).getArticleLists());
    String listCode = "id:new-list-for-create-articlelist";
    String articleListDisplayName = "News";
    //set properties on the action
    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);
    action.setCommand("CREATE_LIST");
    action.setListCode(listCode);
    action.setDisplayName(articleListDisplayName);

    //run the action
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    //check action's return values
    Map<String, ArticleList> actualLists = byListCode(action.getArticleList());
    assertEquals(actualLists.keySet(), ImmutableSet.builder().addAll(initialArticleLists.keySet()).add(listCode).build(),
        "action didn't add new articleList to list");
    ArticleList actualList = actualLists.get(listCode);
    assertEquals(actualList.getListCode(), listCode, "Article List didn't have correct listCode");
    assertEquals(actualList.getDisplayName(), articleListDisplayName, "Article List didn't have correct name");

    assertTrue(action.getActionMessages().size() > 0, "Action didn't return a message indicating success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    //check values stored to the database
    Journal storedJournal = dummyDataStore.get(Journal.class, journal.getID());
    Map<String, ArticleList> storedLists = byListCode(storedJournal.getArticleLists());
    assertEquals(storedLists.keySet(), actualLists.keySet(),
        "journal didn't get article list added in the database");

    //try creating a duplicate article list and see if we get an error message
    action.execute();
    assertEquals(action.getActionErrors().size(), 1, "action didn't add error when trying to save duplicate article " +
        "list");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testRemoveArticleList(Journal journal) throws Exception {
    Map<String, ArticleList> initialArticleLists = byListCode(dummyDataStore.get(Journal.class, journal.getID()).getArticleLists());

    List<ArticleList> listToDelete = new ArrayList<ArticleList>(2);
    List<String> listCodeToDelete = new ArrayList<String>(2);
    for (ArticleList articleList : initialArticleLists.values()) {
      if (listToDelete.size() < 2) {
        listToDelete.add(articleList);
        listCodeToDelete.add(articleList.getListCode());
      }
    }

    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);
    action.setCommand("REMOVE_LIST");
    action.setListToDelete(listCodeToDelete.toArray(new String[0]));

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    //check the return values on the action
    assertEquals(action.getArticleList().size(), initialArticleLists.size() - 2, "action didn't remove article list");
    assertTrue(action.getActionMessages().size() > 0, "Action didn't add message for deleting article list");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");


    Collection<ArticleList> storedArticleList = dummyDataStore.get(Journal.class, journal.getID()).getArticleLists();
    for (ArticleList deletedArticleList : listToDelete) {
      assertFalse(storedArticleList.contains(deletedArticleList), "Article List " + deletedArticleList + " didn't get " +
          "removed " + "from " + "journal");
      assertNull(dummyDataStore.get(ArticleList.class, deletedArticleList.getID()), "Article List didn't get removed from " +
          "the database");
    }
  }

  private VirtualJournalContext makeVirtualJournalContext(Journal journal) {
    return new VirtualJournalContext(
        journal.getJournalKey(),
        "dfltJournal",
        "http",
        80,
        "localhost",
        "ambra-webapp",
        new ArrayList<String>());
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }


}
