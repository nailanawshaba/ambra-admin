package org.ambraproject.admin.service.impl;

import org.plos.ned_client.ApiClient;
import org.plos.ned_client.api.IndividualsApi;

public class NedServiceImpl {
  private String baseUri;
  private String username;
  private String password;
  private ApiClient apiClient;
  private IndividualsApi individualsApi;

  public NedServiceImpl(String baseUri, String username, String password) {
    this.baseUri = baseUri;
    this.username = username;
    this.password = password;

    apiClient = new ApiClient();
    apiClient.setBasePath(baseUri);
    apiClient.setUsername(username);
    apiClient.setPassword(password);
    apiClient.setDebugging(true);

    individualsApi = new IndividualsApi(apiClient);
  }

  protected NedServiceImpl() {

  }

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public IndividualsApi getIndividualsApi() {
    return individualsApi;
  }
}