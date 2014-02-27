/*
 * Copyright (c) 2006-2014 by Public Library of Science
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
package org.ambraproject.admin.service.impl;

import org.ambraproject.admin.service.ImportUsersService;
import org.ambraproject.admin.views.ImportedUserView;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole;
import org.ambraproject.service.hibernate.HibernateServiceImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * @inheritDoc
 */
public class ImportUsersServiceImpl extends HibernateServiceImpl implements ImportUsersService {

  /**
   * @inheritDoc
   */
  public ImportedUserView saveAccount(ImportedUserView user, long[] roleIDs) {
    UserProfile up = new UserProfile();

    up.setSurname(user.getSurName());
    up.setGivenNames(user.getGivenNames());
    up.setCity(user.getCity());
    up.setEmail(user.getEmail());
    up.setDisplayName(user.getDisplayName());

    Set<UserRole> roles = new HashSet<UserRole>();

    for(long roleID : roleIDs) {
      roles.add(getRole(roleID));
    }

    up.setRoles(roles);

    //TODO: Implement me
    //hibernateTemplate.save(up);

    //Return user with the created token
    user.setToken(up.getVerificationToken());

    return user;
  }

  private UserRole getRole(long roleID) {
    return null;
    //TODO: Fix this
    //return hibernateTemplate.load(UserRole.class, roleID);
  }
}

