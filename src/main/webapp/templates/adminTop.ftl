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
    <#if permissions?seq_contains("INGEST_ARTICLE")>
      <#if uploadableFiles?has_content>
        <fieldset>
          <legend><strong>Ingestable Archives</strong></legend>
          <@s.form name="ingestArchives" action="ingestArchives" method="post" namespace="/">
            <#if (uploadableFiles.size() > 1)>
              Select: <a href="#" onClick="setCheckBoxes('ingestArchives','[name=filesToIngest]',true); return false;">All</a>, <a href="#" onClick="setCheckBoxes('ingestArchives','[name=filesToIngest]',false); return false;">None</a>
            </#if>
            <ul class="ingestible">
            <#list uploadableFiles as file>
              <li><@s.checkbox name="filesToIngest" label="${file}" fieldValue="${file}"/></li>
            </#list>
            </ul>
            <div class="forceIngest"><@s.checkbox name="force" label="Force ingestion even if article(s) already exist" fieldValue="true"/></div>
            <@s.submit name="action" value="Ingest Selected Archives" />
          </@s.form>
        </fieldset>
      </#if>
      <#if publishableArticles?has_content>
        <fieldset>
          <legend><strong>Publishable Articles</strong></legend>
          <@s.form id="processArticles" name="processArticles" action="processArticles" method="post" namespace="/">
            <@s.submit name="action" value="Publish and Syndicate"/><input type="submit" name="action" value="Disable and Revert Ingest" onClick="return confirmDisableArticles();"/>
            <#if (publishableArticles.size() > 1)>
              <div style="margin:5px 0;">Select: <a href="#" onClick="setCheckBoxes('processArticles','[name=articles]',true); return false;">All</a>, <a href="#" onClick="setCheckBoxes('processArticles','[name=articles]',false); return false;">None</a></div>
            </#if>
            <table border="1" cellpadding="2" cellspacing="0">
              <tr>
                <th>
                  Articles
                  <#if orderByPublishableArticles == "doi desc" >
                    <button onclick="articleSort(this)">Asc</button>
                  <#elseif orderByPublishableArticles == "doi asc" >
                    <button onclick="articleSort(this)">Desc</button>
                  <#else>
                    <button onclick="articleSort(this)">Desc</button>
                  </#if>
                </th>
                <th>
                  Pub Date
                  <#if orderByPublishableArticles == "pubdate desc" >
                    <button onclick="pubDateSort(this)">Asc</button>
                  <#elseif orderByPublishableArticles == "pubdate asc" >
                    <button onclick="pubDateSort(this)">Desc</button>
                  <#else>
                    <button onclick="pubDateSort(this)">Desc</button>
                  </#if>
                </th>
                <th>Syndicate<#if (publishableArticles.size() > 1)> <a href="#" onClick="setCheckBoxes('processArticles','[name=syndicates]',true); return false;">All</a>, <a href="#"  onClick="setCheckBoxes('processArticles','[name=syndicates]',false); return false;">None</a></#if></th>
              </tr>
              <#list publishableArticles as article>
                <#assign doi = article.getDoi() >
                <tr id="tr_${doi?url}" >
                  <@s.url id="articleURL" includeParams="none" namespace="/article" action="fetchArticle" articleURI="${doi}"/>
                  <td><input type="checkbox" name="articles" value="${doi}" id="${doi?url}" onClick="checkValues(this);"/>
                    <a target="_article" href="${articleURL}">${doi}</a></td>
                  <td>
                    ${article.getDate()?string("MM/dd/yyyy")} <br>
                  </td>
                  <td>
                    <#if publishableSyndications[doi]?? &&  publishableSyndications[doi]?has_content>
                      <#list publishableSyndications[doi] as syndication>
                        <#if syndication.status == "PENDING">
                          <input type="checkBox" name="syndicates" id="${doi?url}_${syndication.target}" value="${doi?url}::${syndication.target}" onClick="checkRow(this.id, this.checked);"/>${syndication.target}
                        <#else>
                          &nbsp; &nbsp; <i>${syndication.target}</i>
                        </#if>
                      </#list>
                    <#else>
                      n/a
                    </#if>
                  </td>
                </tr>
              </#list>

            </table>
            <#if (publishableArticles.size() > 1)>
              <div style="margin:5px 0;">Select: <a href="#" onClick="setCheckBoxes('processArticles','[name=articles]',true); return false;">All</a>, <a href="#" onClick="setCheckBoxes('processArticles','[name=articles]',false); return false;">None</a></div>
            </#if>
            <@s.submit name="action" value="Publish and Syndicate"/><input type="submit" name="action" value="Disable and Revert Ingest" onClick="return confirmDisableArticles();"/>
          </@s.form>
        </fieldset>
      </#if>

      <#if syndications?? && syndications.size() gt 0>

        <fieldset>
          <legend><strong>Syndication Statuses</strong></legend>
            <@s.form name="resyndicateFailedArticles" action="resyndicateFailedArticles" method="post" namespace="/">

              <#if isFailedSyndications>
                <input type="submit" name="action" value="Resyndicate failed articles" />
              </#if>

                <table border="1" cellpadding="2" cellspacing="0" class="syndications">
                  <tr>
                    <th>Articles</th>
                    <th>Messages</th>
                  </tr>
              <#list syndications as syndication>
                    <tr>
                      <td>
                        ${syndication.doi}
                      </td>
                      <td>
                    <#if syndication.status == 'PENDING'>
                        <span class="published">Published</span> <span class="pending">Syndication is pending</span>
                    </#if>
                    <#if syndication.status == 'IN_PROGRESS'>
                        <span class="published">Published.</span> <span class="inprogress">Syndication in progress</span>
                    </#if>
                    <#if syndication.status == 'SUCCESS'>
                        <span class="success">Syndication Succeeded</span>
                    </#if>
                    <#if syndication.status == 'FAILURE'>
                        <span class="published">Published</span> <span class="failure">Syndication to ${syndication.target} Failed: &nbsp;
                          <#if syndication.errorMessage??>
                            ${syndication.errorMessage}
                          <#else>
                            No error message for this syndication failure
                          </#if>
                        </span>
                        <input type="hidden" name="resyndicates" id="resyndicate_${syndication.doi?url}_${syndication.target}" value="${syndication.doi?url}::${syndication.target}"/>
                    </#if>
                    </td>
                  </tr>
              </#list>
                </table>

            <#if isFailedSyndications>
              <input type="submit" name="action" value="Resyndicate failed articles" />
            </#if>

          </@s.form>
        </fieldset>
      </#if>

      <fieldset>
        <legend><strong>Get Article Syndication History</strong></legend>
        <@s.form name="articleSyndicationHistory" action="articleSyndicationHistory" method="post" namespace="/">
          Article Uri: <input type="article" name="article" label="Article Uri" size="80" value=""/>&nbsp;
          <input type="submit" name="action" value="Go" />
        </@s.form>
      </fieldset>

      <fieldset>
        <legend><strong>Unpublish Article</strong></legend>
        <@s.form name="unpublishArticle" action="unpublishArticle" method="post" namespace="/" onsubmit="return confirmUnpublishArticle();">
          Article Uri: <input type="article" name="article" label="Article Uri" size="80" value=""/>&nbsp;
          <input type="submit" name="action" value="Unpublish" />
        </@s.form>
      </fieldset>

      <fieldset>
        <legend><strong>Refresh Article References</strong></legend>
        <@s.form name="refreshReferences" action="refreshReferences" method="post" namespace="/">
          Article Uri: <input type="article" name="article" label="Article Uri" size="80" value=""/>&nbsp;
          <input type="submit" name="action" value="Fetch References" />
        </@s.form>
      </fieldset>

      <fieldset>
        <legend><strong>Refresh Article Subject Categories</strong></legend>
        <@s.form name="refreshSubjectCategories" action="refreshSubjectCategories" method="post" namespace="/">
          Article Uri: <input type="article" name="article" label="Article Uri" size="80" value=""/>&nbsp;
          <input type="submit" name="action" value="Refresh Subject Categories" />
        </@s.form>
      </fieldset>

      <fieldset>
        <legend><strong>Disable and Revert Ingest for Article</strong></legend>
        <@s.form name="disableArticle" action="disableArticle" method="post" namespace="/" onsubmit="return confirmDisableArticle();">
          Article Uri: <input type="article" name="article" label="Article Uri" size="80" value=""/>&nbsp;
          <input type="submit" name="action" value="Disable Article" />
        </@s.form>
      </fieldset>

    </#if>
    <br/>
    <br/>
    <br/>

  </body>
</html>
