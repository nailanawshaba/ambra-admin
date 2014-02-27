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
  <title>Ambra: Administration: Manage Annotations</title>
<#include "includes/header.ftl">
  <script type="text/javascript" src="${request.contextPath}/javascript/edit_annotation.js"></script>
</head>
<body>
<h1 style="text-align: center">Ambra: Administration: Manage Annotations</h1>
<#include "includes/navigation.ftl">

<@messages />

<fieldset>
  <legend><b>Load Annotation</b></legend>
<#if annotation??>
  <#assign annotationIdValue = annotation.ID?c />
  <#assign annotationUriValue = "info:doi/${annotation.annotationUri}" />
<#else>
  <#assign annotationIdValue = "" />
  <#assign annotationUriValue = "" />
</#if>
<@s.form name="manageAnnotationLoadById" action="manageAnnotationLoad" namespace="/" method="get">
  <table>
    <tr>
      <td><b>Annotation ID (numeric)</b></td>
      <td><@s.textfield name="annotationId" size="60" value="${annotationIdValue}"/></td>
    </tr>
    <tr>
      <td colspan="2"><@s.submit value="Load Annotation" /></td>
    </tr>
  </table>
</@s.form>
  <br/>
<@s.form name="manageAnnotationLoadByUri" action="manageAnnotationLoad" namespace="/" method="get">
  <table>
    <tr>
      <td><b>Annotation URI</b></td>
      <td><@s.textfield name="annotationUri" size="60" value="${annotationUriValue}"/></td>
    </tr>
    <tr>
      <td colspan="2"><@s.submit value="Load Annotation" /></td>
    </tr>
  </table>
</@s.form>
</fieldset>

<#if annotation??>
  <@s.form name="manageAnnotationSave" action="manageAnnotationSave" namespace="/" method="post">

  <#--If there's no citation for this annotation, we need a hidden field for annotation uri. If there is a citation, it's an  editable textfield-->
  <#if !annotation.citation??>
    <@s.hidden name="annotationUri" value="${annotationUriValue}"/>
  </#if>

  <fieldset>
    <legend><b>Annotation Details</b></legend>
    <@s.hidden name="annotationId" label="hiddenAnnotationId" requiredLabel="true" value="${annotation.ID?c}"/>
    <table>
      <tr>
        <td><b>Title</b></td>
        <@s.hidden name="title" label="title" requiredLabel="true" value="${annotation.originalTitle!}"/>
        <td>${annotation.originalTitle!"No Title for this Annotation"}</td>
      </tr>
      <tr>
        <td valign="top"><b>Body</b></td>
        <td><@s.textarea name="body" value="${annotation.originalBody!}" rows="9" cols="100"/></td>
      </tr>
      <tr>
        <td><b>Id</b></td>
        <#assign annotationURL = "annotation/listThread.action?root=${annotation.ID?c!}" />
        <td>
          <a href="${annotationURL}">${annotation.ID?c!}</a>
        </td>
      </tr>
      <tr>
        <td><b>Type</b></td>
        <td>${annotation.type!"No Type"}</td>
      </tr>
      <tr>
        <td><b>Created</b></td>
        <td>${annotation.created?string("EEEE, MMMM dd, yyyy, hh:mm:ss a '('zzz')'")!"No Creation Date"}</td>
      </tr>
      <tr>
        <td><b>Creator</b></td>
        <@s.url id="showUser" namespace="/user" action="showUser" userId="${annotation.creatorID?c!}"/>
        <td><@s.a href="${showUser}">${annotation.creatorDisplayName!"No Creator"}</@s.a></td>
      </tr>
      <tr>
        <td><b>Annotates</b></td>
        <td>
          <a href="article/${annotation.articleDoi}">${annotation.articleDoi}</a>
        </td>
      </tr>
      <tr>
        <#if annotation.competingInterestStatement?has_content>
          <#assign ciStatement = annotation.competingInterestStatement/>
        <#else>
          <#assign ciStatement = "No Conflict of Interest Statement"/>
        </#if>
        <td><b>Conflict of Interest</b></td>
        <td>${ciStatement}</td>
      </tr>
    </table>
  </fieldset>
    <@s.submit value="Save Annotation" />
  </@s.form>
</#if>
</body>
</html>