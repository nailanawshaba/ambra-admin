<#--
  $HeadURL:: http://ambraproject.org/svn/ambra/ambra/branches/create-permissions/webapp#$
  $Id: reset.ftl 10266 2012-02-09 19:41:41Z akudlick $
  
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
<tr>
    <td colspan="2"><div <#rt/>
<#if parameters.align?exists>
    align="${parameters.align?html}"<#t/>
</#if>
><#t/>
<#include "/${parameters.templateDir}/simple/reset.ftl" />
</div><#t/>
<#include "/${parameters.templateDir}/xhtml/controlfooter.ftl" />
