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

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociationSet;
import org.apache.olingo.odata2.api.edm.EdmAssociationSetEnd;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.client.api.edm.EdmDocumentation;

/**
 * Objects of this class represent EdmEntitySet
 *
 */
public class EdmEntitySetImpl extends EdmNamedImpl implements EdmEntitySet, EdmAnnotatable {

  private EdmEntityContainer edmEntityContainer;
  private EdmEntityType edmEntityType;  
  private EdmAnnotationsImpl annotations;
  private FullQualifiedName entityTypeName;
  
  private EdmMapping mapping;
  private EdmDocumentation documentation;  

 
  public FullQualifiedName getEntityTypeName() {
    return entityTypeName;
  }

  public void setEntityTypeName(FullQualifiedName entityTypeName) {
    this.entityTypeName = entityTypeName;
  }

  @Override
  public EdmEntityType getEntityType() throws EdmException {
    return edmEntityType;
  }
  
  public EdmEntityContainer getEdmEntityContainer() {
    return edmEntityContainer;
  }

  public void setEdmEntityContainer(EdmEntityContainer edmEntityContainer) {
    this.edmEntityContainer = edmEntityContainer;
  }

  public EdmEntityType getEdmEntityType() {
    return edmEntityType;
  }

  public void setEdmEntityType(EdmEntityType edmEntityType) {
    this.edmEntityType = edmEntityType;
  }

  public void setAnnotations(EdmAnnotationsImpl annotations) {
    this.annotations = annotations;
  }

  public EdmDocumentation getDocumentation() {
    return documentation;
  }

  public void setDocumentation(EdmDocumentation documentation) {
    this.documentation = documentation;
  }

  public void setMapping(EdmMapping mapping) {
    this.mapping = mapping;
  }

  @Override
  public EdmEntitySet getRelatedEntitySet(final EdmNavigationProperty navigationProperty) throws EdmException {
    EdmAssociationSet associationSet =
        edmEntityContainer.getAssociationSet(edmEntityContainer.getEntitySet(name), navigationProperty);
    if(associationSet == null){
      return null;
    }
    EdmAssociationSetEnd toEnd = associationSet.getEnd(navigationProperty.getToRole());
    if (toEnd == null) {
      throw new EdmException(EdmException.NAVIGATIONPROPERTYNOTFOUND,navigationProperty.getName());
    }
    EdmEntitySet targetEntitySet = toEnd.getEntitySet();
    if (targetEntitySet == null) {
      throw new EdmException(EdmException.NAVIGATIONPROPERTYNOTFOUND,navigationProperty.getName());
    }
    return targetEntitySet;
  }

  @Override
  public EdmEntityContainer getEntityContainer() throws EdmException {
    return edmEntityContainer;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return annotations;
  }

  @Override
  public EdmMapping getMapping() throws EdmException {
    return mapping;
  }

  public void setEdmEntityTypeName(FullQualifiedName fqName) {
    this.entityTypeName = fqName;    
  }
  
  @Override
  public String toString() {
      return String.format(name);
  }
  
}
