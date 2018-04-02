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

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmStructuralType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTyped;

/**
 *  Objects of this class represent structural type of the entity 
 */
public abstract class EdmStructuralTypeImpl extends EdmNamedImpl implements EdmStructuralType, EdmAnnotatable {

  protected EdmStructuralType edmBaseType;
  protected EdmComplexType structuralType;
  private EdmTypeKind edmTypeKind;
  protected String namespace;
  private List<EdmProperty> properties;
  private List<String> edmPropertyNames;
  private EdmAnnotations annotations;


  public EdmStructuralType getEdmBaseType() {
    return edmBaseType;
  }

  public void setEdmBaseType(EdmStructuralType edmBaseType) {
    this.edmBaseType = edmBaseType;
  }

  public EdmComplexType getStructuralType() {
    return structuralType;
  }

  public void setStructuralType(EdmComplexType structuralType) {
    this.structuralType = structuralType;
  }

  public EdmTypeKind getEdmTypeKind() {
    return edmTypeKind;
  }

  public void setEdmTypeKind(EdmTypeKind edmTypeKind) {
    this.edmTypeKind = edmTypeKind;
  }

  public void setEdmPropertyNames(List<String> edmPropertyNames) {
    this.edmPropertyNames = edmPropertyNames;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setAnnotations(EdmAnnotations annotations) {
    this.annotations = annotations;
  }


  public List<EdmProperty> getProperties() {
    return properties;
  }

  public void setProperties(List<EdmProperty> properties) {
    this.properties = properties;
  }

  @Override
  public String getNamespace() throws EdmException {
    return namespace;
  }

  @Override
  public EdmTyped getProperty(final String name) throws EdmException {
    EdmTyped property = getPropertyInternal(name);
    if (property == null && edmBaseType != null) {
      property = edmBaseType.getProperty(name);
    }
    return property;
  }

  @Override
  public List<String> getPropertyNames() throws EdmException {
    return edmPropertyNames;
  }

  @Override
  public EdmStructuralType getBaseType() throws EdmException {
    return edmBaseType;
  }

  @Override
  public EdmTypeKind getKind() {
    return edmTypeKind;
  }

  @Override
  public EdmMapping getMapping() throws EdmException {
    return structuralType.getMapping();
  }

  protected EdmTyped getPropertyInternal(final String name) throws EdmException {
    EdmTyped edmProperty = null;
    for (EdmProperty property : properties) {
      if (property.getName().equals(name)) {
        return property;
      } 
    }
    if (edmBaseType!=null) {
      edmProperty = edmBaseType.getProperty(name);
    }
    return edmProperty;
  }

  @Override
  public String toString() {
    try {
      return namespace + Edm.DELIMITER + getName();
    } catch (final EdmException e) {
      return null; //NOSONAR
    }
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return annotations;
  }
  
}
