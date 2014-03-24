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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action to support uploading of a file/CSV of users
 *
 * The uploaded file should be in CSV format and contain "email, last name, first name, city, other field, other field, etc"
 *
 * Anything beyond the 4th column is stored as meta information in TODO:??
 *
 * Stores these new users in session and begins to walk the user through the user flow of creating new accounts.
 *
 * TODO: One day we might enhance this to display a paged form for larger uploads.  To keep this initial release simple
 * I always assume the upload CSV will be relatively small (less then a few hundred accounts)
 *
 */
public class ImportUsersUploadAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ImportUsersUploadAction.class);

  private SearchUserService searchUserService;

  private File file;
  private String contentType;
  private String filename;

  List<ImportedUserView> users;

  @Override
  public String execute() {
    Map<String, Object> session = ServletActionContext.getContext().getSession();

    try {
      users = parseCSV();

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
    } catch(ArrayIndexOutOfBoundsException ex) {
      log.error(ex.getMessage(), ex);
      addActionError("Bad CSV received, wrong number of columns");
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

  @SuppressWarnings("unchecked")
  private List<ImportedUserView> parseCSV() throws IOException {
    //First parse out headers, they are needed to capture meta data
    CSVReader<ImportedUserView> csvHeaderParser = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(file)))
      .strategy(new CSVStrategy(',', '\"', '#', true, true))
      .entryParser(new UserProfileViewParser(null))
      .build();

    List<String> headers = csvHeaderParser.readHeader();
    csvHeaderParser.close();

    CSVReader<ImportedUserView> csvParser = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(file)))
      .strategy(new CSVStrategy(',', '\"', '#', true, true))
      .entryParser(new UserProfileViewParser(headers))
      .build();

    return csvParser.readAll();
  }

  /**
   * A parser class for the csv engine
   */
  private class UserProfileViewParser implements CSVEntryParser<ImportedUserView> {
    final List<String> headers;

    public UserProfileViewParser(List<String> headers) {
      this.headers = headers;
    }

    public ImportedUserView parseEntry(String[] line) {
      if(line.length < 4) {
        throw new UserProfileParserException("Bad CSV received, wrong number of columns: " + line.length);
      } else {
        String email = line[0].trim();
        String surName = line[1].trim();
        String givenName = line[2].trim();
        String displayName = givenName + surName;
        displayName = displayName.replace(" ", "");
        
        String city = line[3].trim();
        Map<String, String> metaData = null;

        if(line.length > 4) {
          metaData = new HashMap<String, String>();
          //Fetch extra columns and store as meta data
          for(int a = 4; a < line.length; a++) {
            metaData.put(headers.get(a), line[a]);
          }
        }

        return ImportedUserView.builder()
          .setEmail(email)
          .setSurName(surName)
          .setGivenNames(givenName)
          .setDisplayName(displayName)
          .setCity(city)
          .setMetaData(metaData)
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
