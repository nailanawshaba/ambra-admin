package org.ambraproject.admin.service.impl;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import io.swagger.client.ApiClient;
import io.swagger.client.api.IndividualsApi;

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

    this.apiClient = new ApiClient();
    apiClient.setDebugging(true);
    apiClient.setBasePath(baseUri);

    String str = (username == null ? "" : username) + ":" + (password == null ? "" : password);

    try {
      apiClient.addDefaultHeader("Authorization", "Basic " + DatatypeConverter.printBase64Binary(str.getBytes
          ("UTF-8")));
      individualsApi = new IndividualsApi(apiClient);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
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