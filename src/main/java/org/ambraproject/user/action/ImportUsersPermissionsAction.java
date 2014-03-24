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
package org.ambraproject.user.action;

import org.ambraproject.admin.action.BaseAdminActionSupport;
import org.ambraproject.admin.views.ImportedUserView;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole;
import org.ambraproject.service.user.UserService;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Allow the user to set up roles to be assigned to the new accounts
 */
public class ImportUsersPermissionsAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ImportUsersPermissionsAction.class);

  private UserService userService;
  private int accountsToImport = 0;
  private Long[] hashCodes;
  private Set<UserRole> userRoles;

  @Override
  public String execute() {
    Map<String, Object> session = ServletActionContext.getContext().getSession();

    if(hashCodes == null) {
      addActionError("No users selected to import");
      return INPUT;
    }

    List<ImportedUserView> users = (List<ImportedUserView>)session.get(IMPORT_USER_LIST);
    List<Long> hashCodeList = Arrays.asList(hashCodes);

    for(ImportedUserView user : users) {
      if(!hashCodeList.contains(Long.valueOf(user.hashCode()))) {
        user.setState(ImportedUserView.USER_STATES.IGNORE);
        log.debug("Ignoring user: {}", user.getDisplayName());
      }
    }

    accountsToImport = hashCodes.length;

    UserProfile userProfile = userService.getUserByAuthId(this.getAuthId());
    //Only allow the user to assign the new user roles that they belong to already
    userRoles = userProfile.getRoles();

    return SUCCESS;
  }

  public void setHashCodes(Long[] hashCodes) {
    this.hashCodes = hashCodes;
  }

  public Set<UserRole> getUserRoles() {
    return userRoles;
  }

  public int getAccountsToImport() {
    return accountsToImport;
  }

  @Required
  public void setUserService(UserService userService)
  {
    this.userService = userService;
  }
}

