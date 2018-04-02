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
import org.apache.olingo.odata2.api.edm.EdmCustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;

/**
 * Objects of this class represent EdmEntityType
 *
 */
public class EdmEntityTypeImpl extends EdmStructuralTypeImpl implements EdmEntityType {

  private List<EdmProperty> edmKeyProperties;
  private List<String> edmKeyPropertyNames;
  private List<EdmNavigationProperty> navigationProperties;
  private List<String> edmNavigationPropertyNames;
  private boolean hasStream;
  private boolean isAbstract;
  private FullQualifiedName baseType;
  private EdmCustomizableFeedMappings customizableFeedMappings;

  public void setBaseType(FullQualifiedName baseType) {
    this.baseType = baseType;
  }

  public void setAbstract(boolean isAbstract) {
    this.isAbstract = isAbstract;
  }

   public void setEdmKeyProperties(List<EdmProperty> edmKeyProperties) {
    this.edmKeyProperties = edmKeyProperties;
  }

  public void setNavigationProperties(List<EdmNavigationProperty> navigationProperties) {
    this.navigationProperties = navigationProperties;
  }

  public void setEdmNavigationPropertyNames(List<String> edmNavigationPropertyNames) {
    this.edmNavigationPropertyNames = edmNavigationPropertyNames;
  }

  public void setHasStream(boolean hasStream) {
    this.hasStream = hasStream;
  }

  @Override
  public List<String> getKeyPropertyNames() throws EdmException {
    return edmKeyPropertyNames;
  }

  public void setEdmKeyPropertyNames(List<String> edmKeyPropertyNames) {
    this.edmKeyPropertyNames = edmKeyPropertyNames;
  }

  @Override
  public List<EdmProperty> getKeyProperties() throws EdmException {
    return edmKeyProperties;
  }

  @Override
  public boolean hasStream() throws EdmException {
    return hasStream;
  }

  @Override
  public EdmCustomizableFeedMappings getCustomizableFeedMappings() throws EdmException {
    return customizableFeedMappings;
  }

  @Override
  public List<String> getNavigationPropertyNames() throws EdmException {
    return edmNavigationPropertyNames;
  }

  @Override
  public EdmEntityType getBaseType() throws EdmException {
    return (EdmEntityType) edmBaseType;
  }

  public FullQualifiedName getBaseTypeName() throws EdmException {
    return baseType;
  }

  @Override
  protected EdmTyped getPropertyInternal(final String name) throws EdmException {
    EdmTyped edmProperty = super.getPropertyInternal(name);

    if (edmProperty != null) {
      return edmProperty;
    }
    for (EdmNavigationProperty navigations : navigationProperties) {
      if (navigations.getName().equals(name)) {
        return navigations;
      }
    }
    return edmProperty;
  }

  public void setCustomizableFeedMappings(EdmCustomizableFeedMappings edmCustomizableFeedMappings) {
    this.customizableFeedMappings = edmCustomizableFeedMappings;
  }
  
  @Override
  public String toString() {
      return String.format(namespace+ Edm.DELIMITER +name);
  }

}
