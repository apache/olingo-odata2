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
import org.apache.olingo.odata2.client.api.edm.EdmSchema;
import org.apache.olingo.odata2.client.api.edm.EdmUsing;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;

/**
 * Objects of this class represent a schema
 * 
 */
public class EdmSchemaImpl implements EdmSchema{

  private String namespace;
  private String alias;
  private List<EdmUsing > usings;
  private List<EdmEntityType> entityTypes;
  private List<EdmComplexType> complexTypes;
  private List<EdmAssociation> associations;
  private List<EdmEntityContainer> entityContainers;
  private List<EdmAnnotationAttribute> annotationAttributes;
  private List<EdmAnnotationElement> annotationElements;

  /**
   * Sets the namespace for this {@link EdmSchemaImpl}
   * @param namespace
   * @return {@link EdmSchemaImpl} for method chaining
   */
  public EdmSchemaImpl setNamespace(final String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * Sets the alias for this {@link EdmSchemaImpl}
   * @param alias
   * @return {@link EdmSchemaImpl} for method chaining
   */
  public EdmSchemaImpl setAlias(final String alias) {
    this.alias = alias;
    return this;
  }

  /**
   * Sets the {@link Using} for this {@link EdmSchemaImpl}
   * @param usings
   * @return {@link EdmSchemaImpl} for method chaining
   */
  public EdmSchemaImpl setUsings(final List<EdmUsing> usings) {
    this.usings = usings;
    return this;
  }

  /**
   * Sets the {@link EntityType}s for this {@link EdmSchemaImpl}
   * @param entityTypes
   * @return {@link EdmSchemaImpl} for method chaining
   */
  public EdmSchemaImpl setEntityTypes(final List<EdmEntityType> entityTypes) {
    this.entityTypes = entityTypes;
    return this;
  }

  /**
   * Sets the {@link ComplexType}s for this {@link EdmSchemaImpl}
   * @param complexTypes
   * @return {@link EdmSchemaImpl} for method chaining
   */
  public EdmSchemaImpl setComplexTypes(final List<EdmComplexType> complexTypes) {
    this.complexTypes = complexTypes;
    return this;
  }

  /**
   * Sets the {@link Association}s for this {@link EdmSchemaImpl}
   * @param associations
   * @return {@link EdmSchemaImpl} for method chaining
   */
  public EdmSchemaImpl setAssociations(final List<EdmAssociation> associations) {
    this.associations = associations;
    return this;
  }

  /**
   * Sets the {@link EntityContainer}s for this {@link EdmSchemaImpl}
   * @param entityContainers
   * @return {@link EdmSchemaImpl} for method chaining
   */
  public EdmSchemaImpl setEntityContainers(final List<EdmEntityContainer> entityContainers) {
    this.entityContainers = entityContainers;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationAttribute} for this {@link EdmSchemaImpl}
   * @param annotationAttributes
   * @return {@link EdmSchemaImpl} for method chaining
   */
  public EdmSchemaImpl setAnnotationAttributes(final List<EdmAnnotationAttribute> annotationAttributes) {
    this.annotationAttributes = annotationAttributes;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationElement} for this {@link EdmSchemaImpl}
   * @param annotationElements
   * @return {@link EdmSchemaImpl} for method chaining
   */
  public EdmSchemaImpl setAnnotationElements(final List<EdmAnnotationElement> annotationElements) {
    this.annotationElements = annotationElements;
    return this;
  }

  /**
   * @return <b>String</b> namespace of this {@link EdmSchemaImpl}
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * @return <b>String</b> alias of this {@link EdmSchemaImpl}
   */
  public String getAlias() {
    return alias;
  }

  /**
   * @return List<{@link Using}> of this {@link EdmSchemaImpl}
   */
  public List<EdmUsing> getUsings() {
    return usings;
  }

  /**
   * @return List<{@link EntityType}> of this {@link EdmSchemaImpl}
   */
  public List<EdmEntityType> getEntityTypes() {
    return entityTypes;
  }

  /**
   * @return List<{@link ComplexType}> of this {@link EdmSchemaImpl}
   */
  public List<EdmComplexType> getComplexTypes() {
    return complexTypes;
  }

  /**
   * @return List<{@link Association}> of this {@link EdmSchemaImpl}
   */
  public List<EdmAssociation> getAssociations() {
    return associations;
  }

  /**
   * @return List<{@link EntityContainer}> of this {@link EdmSchemaImpl}
   */
  public List<EdmEntityContainer> getEntityContainers() {
    return entityContainers;
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
  
  @Override
  public String toString() {
      return String.format(namespace + Edm.DELIMITER  + alias);
  }
}
