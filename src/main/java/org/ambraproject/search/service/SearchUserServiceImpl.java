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

package org.ambraproject.search.service;


import org.ambraproject.models.UserProfile;
import org.ambraproject.admin.service.impl.NedServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;

import org.plos.ned_client.ApiClient;
import org.plos.ned_client.api.IndividualsApi;
import org.plos.ned_client.model.IndividualComposite;
import org.plos.ned_client.model.Individualprofile;
import org.plos.ned_client.model.Auth;
import org.plos.ned_client.model.Email;
import org.plos.ned_client.ApiException;

/**
 * Simple implementation of {@link SearchUserService} that uses SQL like restrictions to find users
 *
 * @author Alex Kudlick 2/17/12
 */
public class SearchUserServiceImpl implements SearchUserService {

  private static final Logger log = LoggerFactory.getLogger(SearchUserServiceImpl.class);

  private enum NedEntity {AUTH, EMAIL, INDIVIDUAL_PROFILE};

  private NedServiceImpl nedService;

  public void setNedService(NedServiceImpl nedService) {
    this.nedService = nedService;
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public boolean isDisplayNameInUse(String displayName) {
    // to call NED-API.
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public boolean isEmailInUse(String email) {
    // to call NED-API.
    return false;
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<UserProfile> findUsersByAuthId(String authId) {

    log.debug("Searching for users with authId like {}", authId);

    List<UserProfile> upList = new ArrayList<UserProfile>();
    try {
      upList = findUsersViaNed(NedEntity.AUTH, authId);
    }
    catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException("Failed to findUsersByAuthId:  " + authId);
    }
    return upList;
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<UserProfile> findUsersByEmail(String emailAddress) {

    log.debug("Searching for users with email address like {}", emailAddress);

    List<UserProfile> upList = new ArrayList<UserProfile>();
    try {
      upList = findUsersViaNed(NedEntity.EMAIL, emailAddress);
    }
    catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException("Failed to findUsersByEmail:  " + emailAddress);
    }
    return upList;
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<UserProfile> findUsersByDisplayName(String displayName) {

    log.debug("Searching for users with displayName like {}", displayName);

    List<UserProfile> upList = new ArrayList<UserProfile>();
    try {
      upList = findUsersViaNed(NedEntity.INDIVIDUAL_PROFILE, displayName);
    }
    catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException("Failed to findUsersByDisplayName:  " + displayName);
    }
    return upList;
  }

  public List<UserProfile> findUsersViaNed(NedEntity nedEntity, String nedValue) {
    List<UserProfile> upList = new ArrayList<UserProfile>();

    try {
      IndividualsApi individualsApi = nedService.getIndividualsApi();
      List<IndividualComposite> icList = new ArrayList<IndividualComposite>();

      if (nedEntity == NedEntity.AUTH) {
        icList = individualsApi.findIndividuals("auth", "authid", nedValue);
      } else if (nedEntity == NedEntity.EMAIL) {
        icList = individualsApi.findIndividuals("email", "emailaddress", nedValue);
      } else if (nedEntity == NedEntity.INDIVIDUAL_PROFILE) {
        icList = individualsApi.findIndividuals("individualprofile", "displayname", nedValue);
      }

      if ( icList.size() > 0 ) {
        IndividualComposite individualComposite = icList.get(0);
        List<Individualprofile> individualprofileList = individualComposite.getIndividualprofiles();

        for (Individualprofile ip : individualprofileList) {
          UserProfile up = new UserProfile();
          up.setID((ip.getNedid().longValue()));
          up.setDisplayName(ip.getDisplayname());
          up.setGivenNames(ip.getFirstname());
          up.setSurname(ip.getLastname());

          List<Email> emailList = individualComposite.getEmails();
          for (Email e : emailList) {
            if (e.getIsactive()) {
              up.setEmail(e.getEmailaddress());
            }
          }

          List<Auth> authList = individualComposite.getAuth();
          for (Auth a : authList) {
            if (a.getIsactive().equals("1")) {
              up.setAuthId(a.getAuthid());
            }
          }
          upList.add(up);
        }
      }
    }
    catch (ApiException apiEx) {
      log.error("findUsersViaNed() code: " + apiEx.getCode());
      log.error("findUsersViaNed() responseBody: " + apiEx.getResponseBody());
      log.error("findUsersViaNed() nedValue: " + nedValue);
    }
    catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }

    return upList;

  }
}