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

package org.ambraproject.admin.action;

import com.opensymphony.xwork2.ModelDriven;
import org.ambraproject.admin.service.AdminAnnotationService;
import org.ambraproject.models.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Alex Kudlick 3/28/12
 */
public class SaveAnnotationAction extends BaseAdminActionSupport implements ModelDriven<Annotation> {
  private static final Logger log = LoggerFactory.getLogger(SaveAnnotationAction.class);

  private AdminAnnotationService adminAnnotationService;

  private Annotation annotation;
  private Long annotationId;

  public SaveAnnotationAction() {
    super();
    this.annotation = new Annotation();
  }

  @Override
  public String execute() throws Exception {
    //perform the update
    try {
      adminAnnotationService.editAnnotation(annotationId, annotation);
      addActionMessage("Successfully updated annotation");
      return SUCCESS;
    } catch (Exception e) {
      log.error("Error updating annotation", e);
      addActionError("Error updating annotation: " + e.getMessage());
      return ERROR;
    }
  }

  @Required
  public void setAdminAnnotationService(AdminAnnotationService adminAnnotationService) {
    this.adminAnnotationService = adminAnnotationService;
  }

  @Override
  public Annotation getModel() {
    return annotation;
  }

  //setter for tests to reset properties
  public void setModel(Annotation annotation) {
    this.annotation = annotation;
  }

  public void setAnnotationId(Long annotationId) {
    this.annotationId = annotationId;
  }

  //Getter so we can pass the id along to the next action
  public Long getAnnotationId() {
    return annotationId;
  }
}
