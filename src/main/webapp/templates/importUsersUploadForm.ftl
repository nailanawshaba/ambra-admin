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

<fieldset>
  <legend><b>Upload Users:</b></legend>
  <p>Upload a .CSV file<p>
  <p>Please see <a href="exampleUsers.csv">this example</a> of the necessary fields.
  <i>If the user already has an existing PLOS Account the name will not be selectable</i></p>
  <@s.form name="importUsersUpload" action="importUsersUpload" namespace="/" method="post" enctype="multipart/form-data">
    <@s.file name="file" label="Select file to upload:"/><br/>
    <br/>
    <@s.submit value="Upload File" />
  </@s.form>
</fieldset>

</body>
</html>

