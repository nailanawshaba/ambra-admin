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

<@messages />

<@s.form name="importUsersComplete" action="importUsersComplete" method="post" namespace="/">
<fieldset>
  <legend><b>Message to new account holders</b></legend>
  Send a password reset email message to initiate the account.</br>
  <br/>
  ${usersToImport?size} New accounts to create.<br/>
  <br/>
  <b>To:</b> <#list usersToImport as user>${user.email}<#if user_has_next>, </#if></#list><br/>
  <br/>
  <@s.textfield name="subject" label="Email Title" size="50" value="${subject}" /><br/>
  <@s.textfield name="emailFrom" label="Email From" size="25" value="${emailFrom}" /><br/>
  <@s.textarea name="textBody" rows="15" cols="120" label="Text Body" value="${textBody}"/>
  <@s.textarea name="htmlBody" rows="15" cols="120" label="HTML Body" value="${htmlBody}"/>

  <div class="btnwrap"><input type="button" value="Back" onclick="history.go(-1);" /></div>
  <@s.submit value="Create Accounts and Send Emails" />
</fieldset>
</@s.form>

</body>
</html>



