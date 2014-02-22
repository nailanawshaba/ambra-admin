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

<@s.form name="importUsersComplete" action="importUsersComplete" method="post" namespace="/">
<fieldset>
  <legend><b>32 New accounts to create:</b></legend>

  <b>Send a password reset message</b><br/>

  <@s.textfield name="emailTite" label="Email Title" requiredLabel="true" size="50" value="PLOS New account password Reset" /><br/>

  <@s.textarea name="emailBody" rows="15" cols="120" label="Email Body" value="A new account has been created for you at PLOS.

  Please click here or go to the following URL to confirm your email address and enter a new password:

  https://register-stage.plos.org/ambra-registration/forgotPasswordVerify.action?email={email}&verificationToken={verificationToken}
  "/>

  <@s.submit value="Save" />
</fieldset>
</@s.form>

</body>
</html>



