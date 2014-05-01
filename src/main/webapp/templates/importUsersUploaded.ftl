<#--
  Copyright (c) 2007-2014 by Public Library of Science

  http://plos.org
  http://ambraproject.org

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<#include "includes/globals.ftl">
<html>
<head>
  <title>Ambra: Administration: Manage User Upload</title>
  <#include "includes/header.ftl">
  <script type="text/javascript" src="${request.contextPath}/javascript/admin.js"></script>
</head>
<body>
<h1 style="text-align: center">Ambra: Administration: Manage Users</h1>
<#include "includes/navigation.ftl">

<#--<@messages />-->

<@s.form name="importUsersPermission" action="importUsersPermission" method="post" namespace="/">
<fieldset>
  <legend><b>Account Creation</b></legend>
  Select the user(s) for account creation:<br/>
  <br/>
  System only accepts rows with the status of "VALID", for help with error codes <a name="help" id="userImportHelp"
    onClick="showPopup('userImportHelpPopup');">click here</a><br/>
  <br/>
  <table id="userImport">
    <tr>
      <td>Import?</td>
      <th>Email</th>
      <th>First Name</th>
      <th>Last Name</th>
      <th>Display Name</th>
      <th>City</th>
      <th>Status</th>
      <th>Meta data</th>
    </tr>
    <#list users as user>
      <tr <#if user.state != "VALID"> class="error"</#if>>
        <td>
          <#if user.state == "VALID">
            <input type="checkbox" name="hashCodes" label="User" value="${user.hashCode()?c}" checked="checked" />
          <#else>
            <input type="checkbox" name="hashCodes" label="User" value="${user.hashCode()?c}" disabled />
          </#if>
        </td>
        <td>${user.email}</td>
        <td>${user.givenNames}</td>
        <td>${user.surName}</td>
        <td>${user.displayName}</td>
        <td>${user.city}</td>
        <td>${user.state}</td>
        <td class="meta">
          <#if user.metaData??>
            <#assign keys = user.metaData?keys>
            <#list keys as key><b>${key}:</b>${user.metaData[key]}<br/></#list>
          </#if>
        </td>
      </tr>
    </#list>
  </table>
  <div class="btnwrap"><input type="button" value="Back" onclick="history.go(-1);" /></div>
  <@s.submit value="Next" />

  <div id="userImportHelpPopup" class="hide">
    <span class="error">DUPE_DISPLAYNAME</span><br/>
    <br/>
    If a user has the error DUPE_DISPLAYNAME please add a “_” (underscore) to the end of the user’s first name in the
    native excel spreadsheet. Save the .CSV file and upload again.<br/>
    <br/>
    After successful importing of users (i.e. Status = VALID):<br/>
    Please go to “manage users” and search via email address for adjusted users. Go into their profile, and take the “_”
    out of their first name, so their first name will remain unaltered.<br/>
    <br/>
    Example:<br/>
    <br/>
    Original<br/>
    First Name Last Name<br/>
    Gillian Welsch<br/>
    <br/>
    Adjusted<br/>
    First Name Last Name<br/>
    Gillian_ Welsch<br/>
    <br/>
    Final Display Name<br/>
    Gillian_Welsh<br/>
    <br/>
    <span class="error">DUPE_EMAIL</span>
    <br/>
    If a user has the error DUPE_EMAIL this means they already have an existing Ambra account. Please send their email
    addresses to Lindsay so she can email them.<br/>
    <br/>
    <a name="close" id="userImportHelpPopupClose" onClick="hidePopup('userImportHelpPopup');">(Close)</a>
  </div>
</fieldset>

</@s.form>

</body>
</html>
