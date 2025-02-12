<#--
 $$HeadURL:: $
 $$Id$

 Copyright (c) 2006-2010 by Public Library of Science
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
    <title>Volume Management</title>
    <#include "includes/header.ftl">
  </head>
  <body>
    <h1 style="text-align: center">Volume Management</h1>
    <hr>
    <#include "includes/navigation.ftl">
    <@messages />

    <!-- Update a Volume -->
    <fieldset>
     <legend>Update Volume</legend>
      <@s.form method="post" namespace="/" action="volumeManagement"
          name="updateVolume" id="update_volume" >
      <@s.hidden name="command" value="UPDATE_VOLUME"/>
      <table border="0" cellpadding="10" cellspacing="0">
        <tr>
          <th align="center">Volume (URI)</th>
            <@s.hidden name="volumeURI" value="${volume.volumeUri}"/>
            <td>${volume.volumeUri}</td>
          </tr>
          <tr>
            <th align="center">Display Name</th>
            <td>
                <@s.textfield name="displayName" value="${volume.displayName}" size="50" requiredLabel="true"/>
            </td>
          </tr>
          <tr>
            <th align="center">Issues (re-order only)</th>
            <td><@s.textfield name="issuesCSV" value="${issuesCSV!''}" size="75" /></td>
          </tr>
        </table>
        <@s.submit align="right" value="Update"/>
      </@s.form>
    </fieldset>

    <!-- Create a Issue -->
    <fieldset>
     <legend>Create Issue</legend>
      <@s.form method="post" namespace="/" action="volumeManagement"
          name="createIssue" id="create_issue">
      <@s.hidden name="command" value="CREATE_ISSUE"/>
      <@s.hidden name="volumeURI" value="${volume.volumeUri}"/>
      <table border="0" cellpadding="10" cellspacing="0">
        <tr>
          <th align="center">Issue (URI)</th>
            <td><@s.textfield name="issueURI" size="50" requiredLabel="true" /></td>
          </tr>
          <tr>
            <th align="center">Display Name</th>
            <td>
                <@s.textfield name="displayName" size="50" requiredLabel="true"/>
            </td>
          </tr>
          <tr>
            <th align="center">Image Article (URI)</th>
            <td><@s.textfield name="imageURI" size="50" /></td>
          </tr>
        </table>
        <@s.submit align="right" value="Create"/>
      </@s.form>
    </fieldset>

    <!-- list Existing Issues For this Volume-->
    <fieldset>
      <legend>Existing Issues</legend>

      <#if (issues?size > 0)>
      <@s.form method="post" namespace="/" action="volumeManagement" id="removeIssues"
          name="removeIssues"  >
      <@s.hidden name="command" value="REMOVE_ISSUES"/>
      <@s.hidden name="volumeURI" value="${volume.volumeUri}"/>
      <table border="1" cellpadding="10" cellspacing="0">
        <tr>
            <th>Delete</th>
            <th>Image</th>
            <th>Display Name</th>
            <th>Issue URI</th>
            <th>&nbsp;</th>
        </tr>
        <#list issues as i>
        <tr>
          <td align="center">
              <@s.checkbox name="issuesToDelete" fieldValue="${i.issueUri}"/>
          </td>
          <td align="center">
            <@s.url namespace="/article" action="browseIssue" issue="${i.issueUri}" id="browseIssue"/>
            <#if i.imageUri?has_content>
              <@s.url namespace="/article" action="fetchObject" uri="${i.imageUri}.g001"
                representation="PNG_S" includeParams="none" id="issueImage"/>
              <#assign altText="Issue Image" />
            <#else>
              <@s.url value="" id="issueImage"/>
              <#assign altText="No Issue Image" />
            </#if>
            <@s.a href="%{browseIssue}"><img src="${issueImage}" alt="${altText}" height=50/></@s.a>
          </td>
          <td>
            <@s.a href="${browseIssue}">${i.displayName}</@s.a>
          </td>
          <td>
             ${i.issueUri}
          </td>
          <td>
            <@s.url namespace="/" action="issueManagement"
                issueURI="${i.issueUri}" volumeURI="${volume.volumeUri}" id="issueMangement"/>
            <@s.a href="${issueMangement}">Update</@s.a>
          </td>
         </tr>
         </#list>
      </table>
      <@s.submit value="Delete Selected Issues"/>
      </@s.form>
    <#else>
       There are no issues associated with this volume.
    </#if>
    </fieldset>

   </body>
</html>