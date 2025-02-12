<#--
  Copyright (c) 2006-2014 by Public Library of Science

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
<@s.url id="adminTop" namespace="/" action="adminTop"/>
<@s.url id="manageFlags" namespace="/" action="manageFlags"/>
<@s.url id="manageAnnotation" namespace="/" action="manageAnnotation"/>
<@s.url id="manageUsersURL" namespace="/" action="findUser" />
<@s.url id="importUsersURL" namespace="/" action="importUsers" />
<@s.url id="manageVirtualJournalsURL" namespace="/" action="manageVirtualJournals" />
<@s.url id="manageSearchIndexing" namespace="/" action="manageSearchIndexing" />
<@s.url id="manageCaches" namespace="/" action="manageCaches" />
<@s.url id="manageArticleList" namespace="/" action="manageArticleList"/>
<@s.url id="manageEmailAlerts" namespace="/" action="manageEmailAlerts" />
<@s.url id="manageRoles" namespace="/" action="manageRoles" />
<@s.url id="deleteArticle" namespace="/" action="deleteArticle" />
<@s.url id="manageFeaturedArticles" namespace="/" action="featuredArticle"/>
<@s.url id="testThesaurus" namespace="/" action="testThesaurus"/>

<@s.url id="logout" includeParams="none" namespace="/" action="secureRedirect"
  goTo="${freemarker_config.casLogoutURL}?" +
  "service=${Request[freemarker_config.journalContextAttributeKey].baseUrl}/" +
  "${request.contextPath}/logout.action"/>
<#if journal??><@s.url id="crossPubManagement" namespace="/" action="crossPubManagement" journalKey="${journal.journalKey}" journalEIssn="${journal.eIssn}" /></#if>
<p style="text-align:center;">
  <@s.a href="/admin/">Admin Top</@s.a>&nbsp; |&nbsp;

  <strong>Manage:</strong>

  <#if permissions?seq_contains("MANAGE_FLAGS")>
    <@s.a href="${manageFlags}">Flags</@s.a>,&nbsp;
  </#if>

  <#if permissions?seq_contains("MANAGE_ANNOTATIONS")>
    <@s.a href="${manageAnnotation}">Annotations</@s.a>,&nbsp;
  </#if>

  <#if permissions?seq_contains("MANAGE_USERS")>
    <@s.a href="${manageUsersURL}">Manage Users</@s.a>,&nbsp;
  </#if>

  <#if permissions?seq_contains("MANAGE_JOURNALS")>
    <@s.a href="${manageVirtualJournalsURL}">Virtual Journals</@s.a>,&nbsp;
  </#if>
  <#if permissions?seq_contains("MANAGE_SEARCH")>
    <@s.a href="${manageSearchIndexing}">Search Indexes</@s.a>,&nbsp;
  </#if>
  <#if permissions?seq_contains("MANAGE_CACHES")>
    <@s.a href="${manageCaches}">Caches</@s.a>,&nbsp;
  </#if>
  <#if permissions?seq_contains("MANAGE_ARTICLE_LISTS")>
    <@s.a href="${manageArticleList}">Article Lists</@s.a>,&nbsp;
  </#if>
  <#if permissions?seq_contains("MANAGE_FEATURED_ARTICLES")>
    <@s.a href="${manageFeaturedArticles}">Featured Articles</@s.a>,&nbsp;
  </#if>
  <#if permissions?seq_contains("RESEND_EMAIL_ALERTS")>
    <@s.a href="${manageEmailAlerts}">Manage Email Alerts</@s.a>,&nbsp;
  </#if>
  <#if permissions?seq_contains("TEST_THESAURUS")>
    <@s.a href="${testThesaurus}">Test Thesaurus</@s.a>,&nbsp;
  </#if>
  <#if permissions?seq_contains("MANAGE_ROLES")>
    <@s.a href="${manageRoles}">Roles</@s.a>
  </#if>
  <#if permissions?seq_contains("CROSS_PUB_ARTICLES")>
    <#if journal??>&nbsp;| &nbsp;<@s.a href="${crossPubManagement}">Cross Publish Articles</@s.a></#if>
  |&nbsp;
  </#if>
  <#if permissions?seq_contains("DELETE_ARTICLES")>
    <@s.a href="${deleteArticle}">Delete Article</@s.a>&nbsp;|&nbsp;
  </#if>
  <@s.a href="${logout}">Logout</@s.a>
</p>
<hr/>
<#if journal??><h2>${journal.journalKey} (${journal.eIssn!""})</h2></#if>
