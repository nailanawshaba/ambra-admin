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
import org.apache.commons.io.IOUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Presents the user with a sample email to send the user during account creation.  The user can override the contents
 * of the email
 */
public class ImportUsersEmailAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ImportUsersEmailAction.class);

  private long[] roleIDs;
  private List<ImportedUserView> accountsToImport = new ArrayList<ImportedUserView>();

  private String subject;
  private String emailFrom;
  private String emailBcc;
  private String htmlBody;
  private String textBody;

  @Override
  public String execute() throws IOException {
    Map<String, Object> session = ServletActionContext.getContext().getSession();
    List<ImportedUserView> users = (List<ImportedUserView>)session.get(IMPORT_USER_LIST);

    if(log.isDebugEnabled() && this.roleIDs != null) {
      for(long roleID : this.roleIDs) {
        log.debug("New users will be assigned RoleID : {}", roleID);
      }
    }

    session.put(IMPORT_USER_LIST_PERMISSIONS, this.roleIDs);

    for(ImportedUserView user : users) {
      if(user.getState().equals(ImportedUserView.USER_STATES.VALID)) {
        accountsToImport.add(user);
      }
    }

    subject = configuration.getString(IMPORT_USERS_EMAIL_TITLE);
    emailFrom = configuration.getString(IMPORT_USERS_EMAIL_FROM);
    emailBcc = configuration.getString(IMPORT_USERS_EMAIL_BCC);
    htmlBody = loadResource("email/templates/newAccountEmail-html.ftl");
    textBody = loadResource("email/templates/newAccountEmail-text.ftl");

    return SUCCESS;
  }

  private String loadResource(String location) throws IOException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(location);
    return IOUtils.toString(is);
  }

  public void setRoleIDs(long[] roleIDs) {
    this.roleIDs = roleIDs;
  }

  public String getSubject() {
    return subject;
  }

  public String getEmailFrom() {
    return emailFrom;
  }

  public String getEmailBcc() {
    return emailBcc;
  }

  public String getHtmlBody() {
    return htmlBody;
  }

  public String getTextBody() {
    return textBody;
  }

  public ImportedUserView[] getUsersToImport() {
    return accountsToImport.toArray(new ImportedUserView[accountsToImport.size()]);
  }
}

