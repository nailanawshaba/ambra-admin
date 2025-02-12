<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2010 by Public Library of Science
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
    <title>Ambra: Administration: Edit User Roles</title>
    <#include "includes/header.ftl">
  </head>

  <body>
    <h1 style="text-align: center">Ambra: Administration: Edit User Roles</h1>
    <#include "includes/navigation.ftl">

    <@messages />

    <fieldset>
      <legend><strong>Edit User Roles</strong></legend>

      <p>User: <b>${displayName}</b>, Email: <b>${email}</b></p>

      <@s.form action="editRolesAssign" namespace="/" method="post" cssClass="ambra-form"
        method="post" title="Roles Form" name="userRoles">

        <@s.hidden name="userId" />
        <@s.hidden name="userAuthId" />
        <@s.hidden name="displayName" />
        <@s.hidden name="email" />

          <#list userRoles as role>
            <@s.checkbox name="roleIDs" label="${role.roleName}" fieldValue="${role.ID}"
              value="${role.assigned?string}"/><br/>
          </#list>

        <br/>
        <@s.submit value="Save" />
      </@s.form>

    </fieldset>
  </body>
</html>