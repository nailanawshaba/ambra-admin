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
  private String htmlBody;
  private String textBody;

  @Override
  public String execute() throws IOException, MessagingException {
    Map<String, Object> session = ServletActionContext.getContext().getSession();

    List<ImportedUserView> users = (List<ImportedUserView>)session.get(IMPORT_USER_LIST);
    long[] roleIDs = (long[])session.get(IMPORT_USER_LIST_PERMISSIONS);

    //TODO: Validate freemarker in both templates
    Template textTemplate = new Template("textMail", new StringReader(textBody), freeMarkerConfig);
    Template htmlTemplate = new Template("htmlMail", new StringReader(htmlBody), freeMarkerConfig);

    for(ImportedUserView user : users) {
      //TODO: Move to a constant
      if(user.getStatus().equals("GOOD TO GO")) {
        user = importUsersService.saveAccount(user, roleIDs);
        sendEmailInvite(user, textTemplate, htmlTemplate);
      }
    }

    //TODO: Clean up session

    return SUCCESS;
  }

  private void sendEmailInvite(ImportedUserView user, Template textTemplate, Template htmlTemplate)
    throws IOException, MessagingException {

    Map<String, Object> fieldMap = new HashMap<String, Object>();

    //TODO: Put in real value for URL
    fieldMap.put("url", "http://foo.org");
    fieldMap.put("verificationToken", user.getToken());
    fieldMap.put("name", user.getGivenNames() + " " + user.getSurName());
    fieldMap.put("email", user.getEmail());

    Multipart content = mailer.createContent(textTemplate, htmlTemplate, fieldMap);

    //TODO fix from address
    mailer.mail(user.getEmail(), "from@todo.com", this.subject, fieldMap, content);
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setHtmlBody(String htmlBody) {
    this.htmlBody = htmlBody;
  }

  public void setTextBody(String textBody) {
    this.textBody = textBody;
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

