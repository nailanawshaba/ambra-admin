package org.ambraproject.modelsdeprecated;

public class UserProfile {
  private Long id;
  private String displayName;
  private String email;
  private String authId;
  private String givenNames;
  private String surname;

  public UserProfile() {
  }

  public UserProfile(String authId, String email, String displayName) {
    this.authId = authId;
    this.email = email;
    this.displayName = displayName;
  }

  public Long getID() {
    return id;
  }

  public void setID(Long id) {
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAuthId() {
    return authId;
  }

  public void setAuthId(String authId) {
    this.authId = authId;
  }

  public String getGivenNames() {
    return givenNames;
  }

  public void setGivenNames(String givenNames) {
    this.givenNames = givenNames;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }


}
