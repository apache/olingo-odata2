/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.api.edm.provider;

import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmAction;

/**
 * Objects of this class represent an OnDelete Action
 * 
 */
public class OnDelete {

  private EdmAction action;
  private Documentation documentation;
  private List<AnnotationAttribute> annotationAttributes;
  private List<AnnotationElement> annotationElements;

  /**
   * @return {@link EdmAction} action
   */
  public EdmAction getAction() {
    return action;
  }

  /**
   * @return {@link Documentation} documentation
   */
  public Documentation getDocumentation() {
    return documentation;
  }

  /**
   * @return List of {@link AnnotationAttribute} annotation attributes
   */
  public List<AnnotationAttribute> getAnnotationAttributes() {
    return annotationAttributes;
  }

  /**
   * @return List of {@link AnnotationElement} annotation elements
   */
  public List<AnnotationElement> getAnnotationElements() {
    return annotationElements;
  }

  /**
   * Sets the {@link EdmAction} for this {@link OnDelete}
   * @param action
   * @return {@link OnDelete} for method chaining
   */
  public OnDelete setAction(final EdmAction action) {
    this.action = action;
    return this;
  }

  /**
   * Sets the {@link Documentation} for this {@link OnDelete}
   * @param documentation
   * @return {@link OnDelete} for method chaining
   */
  public OnDelete setDocumentation(final Documentation documentation) {
    this.documentation = documentation;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationAttribute} for this {@link OnDelete}
   * @param annotationAttributes
   * @return {@link OnDelete} for method chaining
   */
  public OnDelete setAnnotationAttributes(final List<AnnotationAttribute> annotationAttributes) {
    this.annotationAttributes = annotationAttributes;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationElement} for this {@link OnDelete}
   * @param annotationElements
   * @return {@link OnDelete} for method chaining
   */
  public OnDelete setAnnotationElements(final List<AnnotationElement> annotationElements) {
    this.annotationElements = annotationElements;
    return this;
  }
}
