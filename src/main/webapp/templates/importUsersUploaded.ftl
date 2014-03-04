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
  <legend><b>New accounts to create:</b></legend>

  <table>
    <tr>
      <td>Import?</td>
      <th>First Name</th>
      <th>Last Name</th>
      <th>Display Name</th>
      <th>Email</th>
      <th>City</th>
      <th>Status</th>
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
        <td>${user.givenNames}</td>
        <td>${user.surName}</td>
        <td>${user.displayName}</td>
        <td>${user.email}</td>
        <td>${user.city}</td>
        <td>${user.state}</td>
      </tr>
    </#list>
  </table>

  <@s.submit value="Next" />
</fieldset>



</@s.form>

</body>
</html>
