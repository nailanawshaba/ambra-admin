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

/**
 * Used for importing new users
 */
public class ImportedUserView {
  private final String email;
  private final String givenNames;
  private final String surName;
  private final String displayName;
  private final String city;
  private String status;
  private String token;

  private ImportedUserView(final Builder builder) {
    this.email = builder.email;
    this.givenNames = builder.givenNames;
    this.surName = builder.surName;
    this.displayName = builder.displayName;
    this.city = builder.city;
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

  public String getStatus() { return status; }

  public void setStatus(String status) {
    this.status = status;
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
    if (o == null || getClass() != o.getClass()) return false;

    ImportedUserView that = (ImportedUserView) o;

    if (city != null ? !city.equals(that.city) : that.city != null) return false;
    if (!displayName.equals(that.displayName)) return false;
    if (!email.equals(that.email)) return false;
    if (!givenNames.equals(that.givenNames)) return false;
    if (status != null ? !status.equals(that.status) : that.status != null) return false;
    if (!surName.equals(that.surName)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = email.hashCode();
    result = 31 * result + givenNames.hashCode();
    result = 31 * result + surName.hashCode();
    result = 31 * result + displayName.hashCode();
    result = 31 * result + (city != null ? city.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
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

    public ImportedUserView build() {
      return new ImportedUserView(this);
    }
  }

}
