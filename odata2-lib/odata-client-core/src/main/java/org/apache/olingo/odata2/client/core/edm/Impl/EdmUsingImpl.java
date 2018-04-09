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

import java.util.Collection;
import java.util.List;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;
import org.apache.olingo.odata2.client.api.edm.EdmUsing;

/**
 *  Objects of this class represent EdmUsing
 */
public class EdmUsingImpl implements EdmUsing{

  private String namespace;
  private String alias;
  private EdmDocumentationImpl documentation;
  private List<EdmAnnotationAttribute> annotationAttributes;
  private List<EdmAnnotationElement> annotationElements;

  /**
   * Sets the namespace for this {@link EdmUsingImpl}
   * @param namespace
   * @return {@link EdmUsingImpl} for method chaining
   */
  public EdmUsingImpl setNamespace(final String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * Sets the alias for this {@link EdmUsingImpl}
   * @param alias
   * @return {@link EdmUsingImpl} for method chaining
   */
  public EdmUsingImpl setAlias(final String alias) {
    this.alias = alias;
    return this;
  }

  /**
   * Sets the {@link EdmDocumentation} for this {@link EdmUsingImpl}
   * @param documentation
   * @return {@link EdmUsingImpl} for method chaining
   */
  public EdmUsingImpl setDocumentation(final EdmDocumentationImpl documentation) {
    this.documentation = documentation;
    return this;
  }

  /**
   * Sets the collection of {@link AnnotationAttribute} for this {@link EdmUsingImpl}
   * @param annotationAttributes
   * @return {@link EdmUsingImpl} for method chaining
   */
  public EdmUsingImpl setAnnotationAttributes(final List<EdmAnnotationAttribute> annotationAttributes) {
    this.annotationAttributes = annotationAttributes;
    return this;
  }

  /**
   * Sets the collection of {@link AnnotationElement} for this {@link EdmUsingImpl}
   * @param annotationElements
   * @return {@link EdmUsingImpl} for method chaining
   */
  public EdmUsingImpl setAnnotationElements(final List<EdmAnnotationElement> annotationElements) {
    this.annotationElements = annotationElements;
    return this;
  }

  /**
   * @return <b>String</b> namespace
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * @return <b>String</b> alias
   */
  public String getAlias() {
    return alias;
  }

  /**
   * @return {@link EdmDocumentation} documentation
   */
  public EdmDocumentationImpl getDocumentation() {
    return documentation;
  }

  /**
   * @return collection of {@link AnnotationAttribute} annotation attributes
   */
  public Collection<EdmAnnotationAttribute> getAnnotationAttributes() {
    return annotationAttributes;
  }

  /**
   * @return collection of {@link AnnotationElement} annotation elements
   */
  public Collection<EdmAnnotationElement> getAnnotationElements() {
    return annotationElements;
  }
  
  @Override
  public String toString() {
    return namespace + Edm.DELIMITER + alias;
  }
}
