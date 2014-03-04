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

import com.opensymphony.xwork2.validator.annotations.RegexFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.ambraproject.admin.action.BaseAdminActionSupport;
import org.ambraproject.admin.service.ImportUsersService;
import org.ambraproject.admin.views.ImportedUserView;
import org.ambraproject.admin.views.UserRoleView;
import org.apache.camel.spi.Required;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import javax.mail.MessagingException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The last step in processing import of users.
 *
 * Save the users and send them a password reset email
 */
public class ImportUsersCompleteAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ImportUsersCompleteAction.class);

  private ImportUsersService importUsersService;
  private Configuration freeMarkerConfig;

  private int accountsToImported = 0;
  private String subject;
  private String emailFrom;
  private String htmlBody;
  private String textBody;
  private List<UserRoleView> userRoleViews = new ArrayList<UserRoleView>();

  @Override
  public String execute() throws IOException, MessagingException {
    Map<String, Object> session = ServletActionContext.getContext().getSession();

    long[] roleIDs = new long[] {};
    List<ImportedUserView> users = (List<ImportedUserView>)session.get(IMPORT_USER_LIST);

    if(users == null) {
      addActionError("No users to import found in session, please start again to import the list of users.");
      return INPUT;
    }

    if(session.get(IMPORT_USER_LIST_PERMISSIONS) != null) {
      roleIDs = (long[])session.get(IMPORT_USER_LIST_PERMISSIONS);
    }

    Template htmlTemplate = null;
    Template textTemplate = null;

    checkEmailBody(htmlBody, "htmlBody");

    try {
      htmlTemplate = new Template("htmlMail", new StringReader(htmlBody), freeMarkerConfig);
    } catch(Exception ex) {
      log.error(ex.getMessage(), ex);
      //Would like to just pass the exception message, but it's obnoxiously large
      addFieldError("htmlBody", "Invalid freemarker syntax");
    }

    checkEmailBody(textBody, "textBody");

    try {
      textTemplate = new Template("textMail", new StringReader(textBody), freeMarkerConfig);
    } catch(Exception ex) {
      log.error(ex.getMessage(), ex);
      //Would like to just pass the exception message, but it's obnoxiously large
      addFieldError("textBody", "Invalid freemarker syntax");
    }

    if(this.hasFieldErrors()) {
      return INPUT;
    }

    for(ImportedUserView user : users) {
      if(user.getState().equals(ImportedUserView.USER_STATES.VALID)) {
        user = importUsersService.saveAccount(user, roleIDs);
        importUsersService.sendEmailInvite(user, this.emailFrom, this.subject, textTemplate, htmlTemplate);
        accountsToImported++;
      }
    }

    for(long roleID : roleIDs) {
      UserRoleView view = importUsersService.getRole(roleID);

      if(view != null) {
        userRoleViews.add(view);
      }
    }

    session.remove(IMPORT_USER_LIST_PERMISSIONS);
    session.remove(IMPORT_USER_LIST);

    return SUCCESS;
  }

  private void checkEmailBody(String text, String field) {
    if(!text.contains("${url}")) {
      addFieldError(field, field + " is missing ${url} variable, email will not function correctly.");
    }

    if(!text.contains("${email?url}")) {
      addFieldError(field, field + " is missing ${email?url} variable, email will not function correctly.");
    }

    if(!text.contains("${verificationToken?url}")) {
      addFieldError(field, field + " is missing ${verificationToken?url} variable, email will not function correctly.");
    }
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  @RequiredStringValidator(message="Subject is missing")
  public String getSubject() {
    return subject;
  }

  @RequiredStringValidator(message="From email is missing")
  @RegexFieldValidator(message = "You must enter a valid from email", regexExpression = EMAIL_REGEX)
  public String getEmailFrom() {
    return emailFrom;
  }

  public void setEmailFrom(String emailFrom) {
    this.emailFrom = emailFrom;
  }

  @RequiredStringValidator(message="HTML Body is missing")
  public String getHtmlBody() {
    return htmlBody;
  }

  public void setHtmlBody(String htmlBody) {
    this.htmlBody = htmlBody;
  }

  @RequiredStringValidator(message="Text Body is missing")
  public String getTextBody() {
    return textBody;
  }

  public void setTextBody(String textBody) {
    this.textBody = textBody;
  }

  public int getAccountsToImport() {
    Map<String, Object> session = ServletActionContext.getContext().getSession();
    List<ImportedUserView> users = (List<ImportedUserView>)session.get(IMPORT_USER_LIST);

    int accountsToImport = 0;

    for(ImportedUserView user : users) {
      if(user.getState().equals(ImportedUserView.USER_STATES.VALID)) {
        accountsToImport++;
      }
    }

    return accountsToImport;
  }

  public int getAccountsToImported() {
    return accountsToImported;
  }

  @Required
  public void setFreemarkerConfig(final FreeMarkerConfigurer freemarkerConfig) {
    this.freeMarkerConfig = freemarkerConfig.getConfiguration();
  }

  @Required
  public void setImportUsersService(ImportUsersService importUsersService) {
    this.importUsersService = importUsersService;
  }

  public List<UserRoleView> getUserRoles() {
    return userRoleViews;
  }
}

