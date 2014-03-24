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

import freemarker.template.Template;
import org.ambraproject.admin.service.ImportUsersService;
import org.ambraproject.admin.views.ImportedUserView;
import org.ambraproject.admin.views.UserRoleView;
import org.ambraproject.email.TemplateMailer;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserProfileMetaData;
import org.ambraproject.models.UserRole;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.util.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.configuration.Configuration;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @inheritDoc
 */
public class ImportUsersServiceImpl extends HibernateServiceImpl implements ImportUsersService {
  private static final Logger log = LoggerFactory.getLogger(ImportUsersServiceImpl.class);

  private static final String IMPORT_PROFILE_PASSWORD_URL = "ambra.services.registration.url.change-password-imported-profile";

  private TemplateMailer mailer;
  private Configuration configuration;
  /**
   * @inheritDoc
   */
  @Transactional
  public ImportedUserView saveAccount(ImportedUserView user, long[] roleIDs) {
    UserProfile up = new UserProfile();

    up.setSurname(user.getSurName());
    up.setGivenNames(user.getGivenNames());
    up.setCity(user.getCity());
    up.setEmail(user.getEmail());
    up.setDisplayName(user.getDisplayName());
    up.setPassword(TokenGenerator.getUniqueToken());

    Set<UserRole> roles = new HashSet<UserRole>();

    for(long roleID : roleIDs) {
      roles.add(hibernateTemplate.load(UserRole.class, roleID));
    }

    up.setRoles(roles);

    hibernateTemplate.save(up);

    if(user.getMetaData() != null) {
      for(String key : user.getMetaData().keySet()) {
        UserProfileMetaData metaData = new UserProfileMetaData();
        metaData.setMetaKey(key);
        metaData.setMetaValue(user.getMetaData().get(key));
        metaData.setUserProfileID(up.getID());
        hibernateTemplate.save(metaData);
      }
    }

    //Return user with the created token
    user.setToken(up.getVerificationToken());

    return user;
  }

  @Transactional
  public UserRoleView getRole(long roleID) {
    UserRole role = null;

    try {
      role = hibernateTemplate.load(UserRole.class, roleID);
      return new UserRoleView(role.getID(), role.getRoleName(), false);
    } catch(org.hibernate.ObjectNotFoundException ex) {
      log.debug(ex.getMessage());
      return null;
    }
  }

  public void sendEmailInvite(ImportedUserView user, String emailFrom, String subject, Template textTemplate, Template htmlTemplate)
    throws IOException, MessagingException {

    Map<String, Object> fieldMap = new HashMap<String, Object>();

    fieldMap.put("url", configuration.getString(IMPORT_PROFILE_PASSWORD_URL));
    fieldMap.put("verificationToken", user.getToken());
    fieldMap.put("displayName", user.getGivenNames() + " " + user.getSurName());
    fieldMap.put("email", user.getEmail());

    //Append meta keys / values to the email template value stack
    if(user.getMetaData() != null) {
      for(String key : user.getMetaData().keySet()) {
        fieldMap.put("meta_" + key, user.getMetaData().get(key));
      }
    }

    Multipart content = mailer.createContent(textTemplate, htmlTemplate, fieldMap);
    mailer.mail(user.getEmail(), emailFrom, subject, fieldMap, content);
  }

  public void setMailer(TemplateMailer mailer) {
    this.mailer = mailer;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
}

