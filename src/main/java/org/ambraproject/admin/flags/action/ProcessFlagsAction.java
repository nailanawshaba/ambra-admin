/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.flags.action;

import org.ambraproject.admin.action.BaseAdminActionSupport;
import org.ambraproject.admin.flags.service.FlagService;
import org.ambraproject.models.AnnotationType;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Alex Kudlick 3/26/12
 */
public class ProcessFlagsAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ProcessFlagsAction.class);
  private FlagService flagService;

  private Long[] commentsToUnflag;
  private Long[] commentsToDelete;

  @Override
  public String execute() throws Exception {
    try {
      if (!ArrayUtils.isEmpty(commentsToUnflag)) {
        flagService.deleteFlags(commentsToUnflag);
        addActionMessage("Successfully deleted " + commentsToUnflag.length + "  flags");
      }
      if (!ArrayUtils.isEmpty(commentsToDelete)) {
        flagService.deleteFlagAndComment(commentsToDelete);
        addActionMessage("Successfully deleted " + commentsToDelete.length + "  comments");
      }
    } catch (Exception e) {
      log.error("error processing flags", e);
      addActionError("Error processing flags: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  @Required
  public void setFlagService(FlagService flagService) {
    this.flagService = flagService;
  }

  public void setCommentsToUnflag(Long[] commentsToUnflag) {
    this.commentsToUnflag = commentsToUnflag;
  }

  public void setCommentsToDelete(Long[] commentsToDelete) {
    this.commentsToDelete = commentsToDelete;
  }
}
