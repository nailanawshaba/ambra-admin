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

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.ambraproject.admin.action.BaseAdminActionSupport;
import org.ambraproject.admin.service.ImportUsersService;
import org.ambraproject.admin.views.ImportedUserView;
import org.ambraproject.email.TemplateMailer;
import org.apache.camel.spi.Required;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
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
  private TemplateMailer mailer;
  private Configuration freeMarkerConfig;

  private String subject;
  private String emailFrom;
  private String htmlBody;
  private String textBody;

  @Override
  public String execute() throws IOException, MessagingException {
    Map<String, Object> session = ServletActionContext.getContext().getSession();

    long[] roleIDs = new long[] {};
    List<ImportedUserView> users = (List<ImportedUserView>)session.get(IMPORT_USER_LIST);

     if(session.get(IMPORT_USER_LIST_PERMISSIONS) != null) {
      roleIDs = (long[])session.get(IMPORT_USER_LIST_PERMISSIONS);
    }

    Template textTemplate = null;
    Template htmlTemplate = null;

    //TODO: Validate input freemarker in both templates, make sure has the right variables?
    try {
      textTemplate = new Template("textMail", new StringReader(textBody), freeMarkerConfig);
    } catch(Exception ex) {
      log.error(ex.getMessage(), ex);
      //Would like to just pass the exception message, but it's obnoxiously large
      addFieldError("textBody", "Invalid freemarker syntax");
    }

    try {
      htmlTemplate = new Template("htmlMail", new StringReader(htmlBody), freeMarkerConfig);
    } catch(Exception ex) {
      log.error(ex.getMessage(), ex);
      //Would like to just pass the exception message, but it's obnoxiously large
      addFieldError("htmlBody", "Invalid freemarker syntax");
    }

    if(this.hasFieldErrors()) {
      return INPUT;
    }

    for(ImportedUserView user : users) {
      if(user.getState().equals(ImportedUserView.USER_STATES.VALID)) {
        user = importUsersService.saveAccount(user, roleIDs);
        sendEmailInvite(user, textTemplate, htmlTemplate);
      }
    }

    session.remove(IMPORT_USER_LIST_PERMISSIONS);
    session.remove(IMPORT_USER_LIST);

    return SUCCESS;
  }

  private void sendEmailInvite(ImportedUserView user, Template textTemplate, Template htmlTemplate)
    throws IOException, MessagingException {

    Map<String, Object> fieldMap = new HashMap<String, Object>();

    fieldMap.put("url", configuration.getString(IMPORT_PROFILE_PASSWORD_URL));
    fieldMap.put("verificationToken", user.getToken());
    fieldMap.put("displayName", user.getGivenNames() + " " + user.getSurName());
    fieldMap.put("email", user.getEmail());

    Multipart content = mailer.createContent(textTemplate, htmlTemplate, fieldMap);
    mailer.mail(user.getEmail(), this.emailFrom, this.subject, fieldMap, content);
  }

  //TODO: Input validation
  public void setSubject(String subject) {
    this.subject = subject;
  }

  @RequiredStringValidator(message="Subject is missing")
  public String getSubject() {
    return subject;
  }

  @RequiredStringValidator(message="From email is missing")
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

  @Required
  public void setAmbraMailer(TemplateMailer ambraMailer) {
    this.mailer = ambraMailer;
  }

  @Required
  public void setFreemarkerConfig(final FreeMarkerConfigurer freemarkerConfig) {
    this.freeMarkerConfig = freemarkerConfig.getConfiguration();
  }

  @Required
  public void setImportUsersService(ImportUsersService importUsersService) {
    this.importUsersService = importUsersService;
  }
}

