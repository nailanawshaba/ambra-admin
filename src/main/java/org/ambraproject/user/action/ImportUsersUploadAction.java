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

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
import org.ambraproject.admin.action.BaseAdminActionSupport;
import org.ambraproject.admin.views.ImportedUserView;
import org.ambraproject.models.UserProfile;
import org.ambraproject.search.service.SearchUserService;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Action to support uploading of a file/CSV of users
 *
 * Accepts a CSV in a very specific format
 *
 * Stores these new users in session and begins to walk the user through the user flow of creating new accounts.
 *
 * Note: One day we should enhance this to display a paged form for larger uploads.  To keep this initial release simple
 * I always assume the upload CSV will be relatively small (less then a few hundred accounts)
 *
 * TODO: Refine / define format
 */
public class ImportUsersUploadAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ImportUsersUploadAction.class);

  private SearchUserService searchUserService;

  private File file;
  private String contentType;
  private String filename;

  List<ImportedUserView> users;

  @Override
  @SuppressWarnings("unchecked")
  public String execute() {
    Map<String, Object> session = ServletActionContext.getContext().getSession();

    try {
      CSVReader<ImportedUserView> csvParser = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(file)))
        .strategy(new CSVStrategy(',', '\"', '#', true, true))
        .entryParser(new UserProfileViewParser())
        .build();

      users = csvParser.readAll();

      log.debug("Parsed {} records", users.size());

      for(ImportedUserView importUserView : users) {
        //Confirm items in list are valid
        ImportedUserView.USER_STATES state = ImportedUserView.USER_STATES.VALID;

        //Confirm email is not already in the database
        List<UserProfile> matchingUsers = searchUserService.findUsersByEmail(importUserView.getEmail());
        if(!matchingUsers.isEmpty()) {
          state = ImportedUserView.USER_STATES.DUPE_EMAIL;
        }

        //Confirm display name is not already in the database
        matchingUsers = searchUserService.findUsersByDisplayName(importUserView.getDisplayName());
        if(!matchingUsers.isEmpty()) {
          state = ImportedUserView.USER_STATES.DUPE_DISPLAYNAME;
        }

        //Confirm display and email are unique for the given set
        for(ImportedUserView importUserView2 : users) {
          if(importUserView2.hashCode() != importUserView.hashCode()) {
            if(importUserView2.getEmail().equals(importUserView.getEmail())) {
              state = ImportedUserView.USER_STATES.DUPE_EMAIL;
            }

            if(importUserView2.getDisplayName().equals(importUserView.getDisplayName())) {
              state = ImportedUserView.USER_STATES.DUPE_DISPLAYNAME;
            }
          }
        }

        importUserView.setState(state);
      }

      session.put(IMPORT_USER_LIST, users);
    } catch(FileNotFoundException ex) {
      log.error(ex.getMessage(), ex);
      addActionError(ex.getMessage());
      return INPUT;
    } catch(UserProfileParserException ex) {
      log.error(ex.getMessage(), ex);
      addActionError(ex.getMessage());
      return INPUT;
    } catch(IOException ex) {
      log.error(ex.getMessage(), ex);
      addActionError(ex.getMessage());
      return ERROR;
    }

    return SUCCESS;
  }

  /**
   * A parser class for the csv engine
   */
  private class UserProfileViewParser implements CSVEntryParser<ImportedUserView> {
    public ImportedUserView parseEntry(String[] line) {
      //TODO: How do we handle EM Ids and make sure systems are linked?
      //TODO: Do we need to store their EM user names for disambiguation?
      if(line.length != 10) {
        throw new UserProfileParserException("Bad CSV received, wrong number of columns: " + line.length);
      } else {
        return ImportedUserView.builder()
          .setEmail(line[0].trim())
          .setSurName(line[1].trim())
          .setGivenNames(line[2].trim())
          //TODO: Set Display name to be the first and last names once file format is more defined
          .setDisplayName(line[3].trim())
          .setCity(line[9].trim())
          .build();
      }
    }
  }

  private class UserProfileParserException extends RuntimeException {
    public UserProfileParserException(String message) {
      super(message);
    }
  }

  public List<ImportedUserView> getUsers() {
    return users;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setFileContentType(String contentType) {
    this.contentType = contentType;
  }

  public void setFileFileName(String filename) {
    this.filename = filename;
  }

  @Required
  public void setSearchUserService(SearchUserService searchUserService) {
    this.searchUserService = searchUserService;
  }
}
