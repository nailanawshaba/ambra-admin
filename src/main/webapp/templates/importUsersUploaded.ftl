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
</head>
<body>
<h1 style="text-align: center">Ambra: Administration: Manage Users</h1>
<#include "includes/navigation.ftl">

<#--<@messages />-->

<@s.form name="importUsersPermission" action="importUsersPermission" method="post" namespace="/">
<fieldset>
  <legend><b>Account Creation</b></legend>
  Select the user(s) for account creation:<br/>
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
      <tr>
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
</fieldset>

</@s.form>

</body>
</html>
