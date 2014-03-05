/*
 * Copyright (c) 2006-2010 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.ambraproject.admin.service;

import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.admin.views.ImportedUserView;
import org.ambraproject.admin.views.UserRoleView;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserProfileMetaData;
import org.ambraproject.models.UserRole;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/*
* Test the import users service
* */
public class ImportUsersServiceTest extends AdminBaseTest {
  @Autowired
  protected ImportUsersService importUsersService;

  @Test
  @SuppressWarnings("unchecked")
  public void setImportUsersService() {
    UserRole role = new UserRole();

    role.setRoleName("ROLENAME");
    dummyDataStore.store(role);

    Map<String, String> metaData = new HashMap<String, String>();
    metaData.put("key1", "value1");
    metaData.put("key2", "value2");
    metaData.put("key3", null);

    ImportedUserView userView = ImportedUserView.builder()
      .setCity("CITY")
      .setEmail("EMAIL@MAIL.COM")
      .setGivenNames("GNAME")
      .setSurName("SNAME")
      .setDisplayName("FOO")
      .setMetaData(metaData)
      .build();

    userView = importUsersService.saveAccount(userView, new long[] { role.getID() });

    assertNotNull(userView.getToken());

    List<UserProfile> userList = dummyDataStore.findByCriteria(
      DetachedCriteria.forClass(UserProfile.class)
        .add(Restrictions.eq("email", userView.getEmail())
        )
    );

    assertEquals(userList.size(), 1);
    assertEquals(userList.get(0).getRoles().size(), 1);
    Iterator<UserRole> roles = userList.get(0).getRoles().iterator();
    while(roles.hasNext()) {
      assertEquals(roles.next().getRoleName(), "ROLENAME");
    }

    assertNotNull(userList.get(0).getAuthId());
    assertNotNull(userList.get(0).getPassword());
    assertNotNull(userList.get(0).getVerificationToken());

    List<UserProfileMetaData> userProfileMetaDatas = dummyDataStore.findByCriteria(
      DetachedCriteria.forClass(UserProfileMetaData.class)
        .add(Restrictions.eq("userProfileID", userList.get(0).getID())
        )
    );

    assertEquals(userProfileMetaDatas.size(), 3);

    Map<String, String> metaData2 = new HashMap<String, String>();
    metaData2.put(userProfileMetaDatas.get(0).getMetaKey(), userProfileMetaDatas.get(0).getMetaValue());
    metaData2.put(userProfileMetaDatas.get(1).getMetaKey(), userProfileMetaDatas.get(1).getMetaValue());
    metaData2.put(userProfileMetaDatas.get(2).getMetaKey(), userProfileMetaDatas.get(2).getMetaValue());

    assertEquals(metaData, metaData2);
  }

  @Test
  public void setImportUsersServiceGetRole() {
    UserRole role = new UserRole();

    role.setRoleName("ROLENAME");
    dummyDataStore.store(role);

    UserRoleView userRoleView = importUsersService.getRole(role.getID());

    assertEquals(userRoleView.getID(), role.getID());
    assertEquals(userRoleView.getRoleName(), role.getRoleName());
    assertEquals(userRoleView.getAssigned(), false);

    //Test to make sure bad role ID is handled
    userRoleView = importUsersService.getRole(40404);
    assertNull(userRoleView);
  }
}
