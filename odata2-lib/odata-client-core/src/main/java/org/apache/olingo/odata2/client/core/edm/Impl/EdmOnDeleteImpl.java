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
package org.apache.olingo.odata2.client.core.edm.Impl;

import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmAction;
import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;
import org.apache.olingo.odata2.client.api.edm.EdmDocumentation;
import org.apache.olingo.odata2.client.core.edm.EdmOnDelete;

/**
 * Objects of this class represent an OnDelete Action
 * 
 */
public class EdmOnDeleteImpl implements EdmOnDelete{

  private EdmAction action;
  private EdmDocumentation documentation;
  private List<EdmAnnotationAttribute> annotationAttributes;
  private List<EdmAnnotationElement> annotationElements;

  /**
   * @return {@link EdmAction} action
   */
  public EdmAction getAction() {
    return action;
  }

  /**
   * @return {@link Documentation} documentation
   */
  public EdmDocumentation getDocumentation() {
    return documentation;
  }

  /**
   * @return List of {@link AnnotationAttribute} annotation attributes
   */
  public List<EdmAnnotationAttribute> getAnnotationAttributes() {
    return annotationAttributes;
  }

  /**
   * @return List of {@link AnnotationElement} annotation elements
   */
  public List<EdmAnnotationElement> getAnnotationElements() {
    return annotationElements;
  }

  /**
   * Sets the {@link EdmAction} for this {@link EdmOnDeleteImpl}
   * @param action
   * @return {@link EdmOnDeleteImpl} for method chaining
   */
  public EdmOnDeleteImpl setAction(final EdmAction action) {
    this.action = action;
    return this;
  }

  /**
   * Sets the {@link Documentation} for this {@link EdmOnDeleteImpl}
   * @param documentation
   * @return {@link EdmOnDeleteImpl} for method chaining
   */
  public EdmOnDeleteImpl setDocumentation(final EdmDocumentation documentation) {
    this.documentation = documentation;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationAttribute} for this {@link EdmOnDeleteImpl}
   * @param annotationAttributes
   * @return {@link EdmOnDeleteImpl} for method chaining
   */
  public EdmOnDeleteImpl setAnnotationAttributes(final List<EdmAnnotationAttribute> annotationAttributes) {
    this.annotationAttributes = annotationAttributes;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationElement} for this {@link EdmOnDeleteImpl}
   * @param annotationElements
   * @return {@link EdmOnDeleteImpl} for method chaining
   */
  public EdmOnDeleteImpl setAnnotationElements(final List<EdmAnnotationElement> annotationElements) {
    this.annotationElements = annotationElements;
    return this;
  }
  
  @Override
  public String toString() {
      return String.format(action.name());
  }
}
