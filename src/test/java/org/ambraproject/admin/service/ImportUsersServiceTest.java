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
import org.ambraproject.models.UserRole;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import java.util.Iterator;
import java.util.List;
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
  public void setImportUsersService() {
    UserRole role = new UserRole();

    role.setRoleName("ROLENAME");
    dummyDataStore.store(role);

    ImportedUserView userView = ImportedUserView.builder()
      .setCity("CITY")
      .setEmail("EMAIL@MAIL.COM")
      .setGivenNames("GNAME")
      .setSurName("SNAME")
      .setDisplayName("FOO")
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
