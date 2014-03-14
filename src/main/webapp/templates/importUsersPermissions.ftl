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

<@s.form name="importUsersEmail" action="importUsersEmail" method="post" namespace="/">
<fieldset>
  <legend><b>Role Assignment</b></legend>

  ${accountsToImport} user(s) selected<br/>
  <br/>
  Assign roles to these accounts:<br/>
  <br/>
  <#list userRoles as role>
    <input type="checkbox" name="roleIDs" value="${role.ID}" id="editRolesAssign_roleID_${role.ID}"/>
    <label for="editRolesAssign_roleID_${role.ID}" class="checkboxLabel">${role.roleName}</label><br/>
  </#list>
  <br/>

  <div class="btnwrap"><input type="button" value="Back" onclick="history.go(-1);" /></div>
  <@s.submit value="Next" />
</fieldset>
</@s.form>

</body>
</html>



