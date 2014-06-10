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
  <title>Ambra administration</title>
  <#include "includes/header.ftl">
  <script type="text/javascript" src="${request.contextPath}/javascript/admin.js"></script>
</head>
<body>
<h1 style="text-align: center">Ambra administration</h1>
<#include "includes/navigation.ftl">

<div id="messages">
<@messages />
</div>

<fieldset>
  <legend><strong>Test Thesaurus</strong></legend>
  <@s.form name="testThesaurusSubmit" action="testThesaurusSubmit" method="post" namespace="/">
    Article Uri: <input type="article" name="articleDOI" label="Article Uri" size="40" value="${articleDOI!""}"/><br/>
    Thesaurus: <input type="thesaurus" name="thesaurus" label="Thesaurus Version" size="15" value="${thesaurus!""}"/><br/>
    <br/>
    <input type="submit" value="Test Subject Categories" />
  </@s.form>
</fieldset>

<br/>

</body>
</html>
