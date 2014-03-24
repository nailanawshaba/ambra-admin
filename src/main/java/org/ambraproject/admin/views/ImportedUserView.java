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
package org.ambraproject.admin.views;

import java.util.Map;

/**
 * Used for importing new users
 */
public class ImportedUserView {
  public enum USER_STATES {
    VALID,
    DUPE_EMAIL,
    DUPE_DISPLAYNAME,
    IGNORE
  }

  private final String email;
  private final String givenNames;
  private final String surName;
  private final String displayName;
  private final String city;
  private final Map<String, String> metaData;

  private USER_STATES state;
  private String token;

  private ImportedUserView(final Builder builder) {
    this.email = builder.email;
    this.givenNames = builder.givenNames;
    this.surName = builder.surName;
    this.displayName = builder.displayName;
    this.city = builder.city;
    this.metaData = builder.metaData;
  }

  public String getEmail() {
    return email;
  }

  public String getGivenNames() {
    return givenNames;
  }

  public String getSurName() {
    return surName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getCity() {
    return city;
  }

  public Map<String, String> getMetaData() {
    return metaData;
  }

  public USER_STATES getState() { return this.state; }

  public void setState(USER_STATES state) {
    this.state = state;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ImportedUserView)) return false;

    ImportedUserView userView = (ImportedUserView) o;

    if (city != null ? !city.equals(userView.city) : userView.city != null) return false;
    if (!displayName.equals(userView.displayName)) return false;
    if (!email.equals(userView.email)) return false;
    if (!givenNames.equals(userView.givenNames)) return false;
    if (metaData != null ? !metaData.equals(userView.metaData) : userView.metaData != null) return false;
    if (state != userView.state) return false;
    if (!surName.equals(userView.surName)) return false;
    if (token != null ? !token.equals(userView.token) : userView.token != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = email.hashCode();
    result = 31 * result + givenNames.hashCode();
    result = 31 * result + surName.hashCode();
    result = 31 * result + displayName.hashCode();
    result = 31 * result + (city != null ? city.hashCode() : 0);
    result = 31 * result + (metaData != null ? metaData.hashCode() : 0);
    result = 31 * result + (state != null ? state.hashCode() : 0);
    result = 31 * result + (token != null ? token.hashCode() : 0);
    return result;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Builder() {
      super();
    }

    private String email;
    private String givenNames;
    private String surName;
    private String displayName;
    private String city;
    private Map<String, String> metaData;

    public Builder setEmail(String email) {
      this.email = email;
      return this;
    }

    public Builder setGivenNames(String givenNames) {
      this.givenNames = givenNames;
      return this;
    }

    public Builder setSurName(String surName) {
      this.surName = surName;
      return this;
    }

    public Builder setDisplayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public Builder setCity(String city) {
      this.city = city;
      return this;
    }

    public Builder setMetaData(Map<String, String> metaData) {
      this.metaData = metaData;
      return this;
    }

    public ImportedUserView build() {
      return new ImportedUserView(this);
    }
  }

}
