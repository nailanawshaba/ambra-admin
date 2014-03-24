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
package org.ambraproject.admin.service;

import freemarker.template.Template;
import org.ambraproject.admin.views.ImportedUserView;
import org.ambraproject.admin.views.UserRoleView;

import javax.mail.MessagingException;
import java.io.IOException;

/**
 * Method for creating new users automatically from a list / CSV
 */
public interface ImportUsersService {

  /**
   * Create a new account
   *
   * Once the account is created, the verification token is assigned to the passed in object
   * and returned.
   *
   * The account is created with a dummy token as a password
   *
   * @param user the user to create
   * @param roleIDs the roleIDs to assign to the user
   *
   * @return the userView (modified)
   */
  public ImportedUserView saveAccount(ImportedUserView user, long[] roleIDs);

  /**
   * Get a view of a user role record
   *
   * @param roleID

   * @return immutable view of user role
   */
  public UserRoleView getRole(long roleID);

  /**
   * Send the the current user view the passed in email templates
   *
   * @param user
   * @param emailFrom
   * @param subject
   * @param textTemplate
   * @param htmlTemplate
   */
  public void sendEmailInvite(ImportedUserView user, String emailFrom, String subject, Template textTemplate, Template htmlTemplate)
    throws IOException, MessagingException;
}
