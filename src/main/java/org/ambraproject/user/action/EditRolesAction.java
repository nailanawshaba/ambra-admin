/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.user.action;

import org.ambraproject.admin.action.BaseAdminActionSupport;
import org.ambraproject.admin.service.AdminRolesService;
import org.ambraproject.admin.views.UserRoleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Edits a user's role. User must be logged in already.
 */
public class EditRolesAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(EditRolesAction.class);
  private List<UserRoleView> userRoles;
  private Long userId;
  private String userAuthId;
  private String displayName;
  private String email;
  private Long[] roleIDs;

  /**
   * Do input verification and setup the role list
   *
   * @return status code from webwork
   *
   * @throws Exception Exception
   */
  public String execute() throws Exception {

    if (userId == null) {
      addFieldError("userId", "userId is required");
      return INPUT;
    }

    this.userRoles = adminRolesService.getAllRoles(userId);

    return SUCCESS;
  }

  /**
   * Assign the roles passed in through the form submit
   *
   * @return status code from webwork
   *
   * @throws Exception
   */
  public String assignRoles() throws Exception {

    if (userId== null) {
      addFieldError("userId", "userId is required");
      return INPUT;
    }

    //Revoke all roles and then reassign them
    this.adminRolesService.revokeAllRoles(userId);

    if(this.roleIDs != null) {
      for(Long roleID : roleIDs) {
        this.adminRolesService.grantRole(userId, roleID);
      }
    }

    this.userRoles = adminRolesService.getAllRoles(userId);

    addActionMessage("Roles Updated Successfully");

    return SUCCESS;
  }

  /**
   * Get the list of roles attached to the passed in user
   *
   * @return
   */
  public List<UserRoleView> getUserRoles()
  {
    return this.userRoles;
  }

  /**
   * Struts setter for the user Roles
   * @param roleIDs the roles to assign to the current user
   */
  public void setRoleIDs(final Long[] roleIDs) {
    this.roleIDs = roleIDs;
  }

  /**
   * Struts setter for the user's profile ID
   * @return
   */
  public void setUserId(Long userId)
  {
    this.userId = userId;
  }

  /**
   * Get the user's profile ID
   * @return
   */
  public Long getUserId()
  {
    return this.userId;
  }

  /**
   * Struts setter for the user's authorization ID
   * @return
   */
  public void setUserAuthId(String userAuthId)
  {
    this.userAuthId = userAuthId;
  }

  /**
   * Get the user's authorization ID
    * @return
   */
  public String getUserAuthId()
  {
    return this.userAuthId;
  }

  /**
   * Get the user's email address
   * @return
   */
  public String getEmail()
  {
    return this.email;
  }

  /**
   * Struts setter for the user's email
   * @return
   */
  public void setEmail(String email)
  {
    this.email = email;
  }

  /**
   * Get the user's display name
   *
   * @return
   */
  public String getDisplayName()
  {
    return this.displayName;
  }

  /**
   * Struts setter for the user's displayName
   * @return
   */
  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

}
