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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationSet;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.client.api.edm.ClientEdm;
import org.apache.olingo.odata2.client.api.edm.EdmDocumentation;

/**
 *  Objects of this class represent EdmEntityContainer
 */
public class EdmEntityContainerImpl implements EdmEntityContainer, EdmAnnotatable {

  private EdmImpl edm;
  private List<EdmEntityContainer> entityContainerHierachy;
  private List<EdmEntitySet> edmEntitySets;
  private Map<String, EdmAssociationSet> edmAssociationSetMap;
  private List<EdmAssociationSet> edmAssociationSets;
  private List<EdmFunctionImport> edmFunctionImports;
  private EdmEntityContainer edmExtendedEntityContainer;
  private boolean isDefaultContainer;
  private EdmAnnotations annotations;
  private EdmDocumentation documentation;
  private String name;
  private String extendz;

  public Map<String, EdmAssociationSet> getEdmAssociationSetMap() {
    return edmAssociationSetMap;
  }

  public void setEdmAssociationSetMap(Map<String, EdmAssociationSet> associationSetMap) {
    this.edmAssociationSetMap = associationSetMap;
  }

  public EdmDocumentation getDocumentation() {
    return documentation;
  }

  public void setDocumentation(EdmDocumentation documentation) {
    this.documentation = documentation;
  }

  public void setEdm(EdmImpl edm) {
    this.edm = edm;
  }

  public ClientEdm getEdm() {
    return edm;
  }

  public EdmEntityContainer getEdmExtendedEntityContainer() {
    return edmExtendedEntityContainer;
  }

  public void setEdmExtendedEntityContainer(EdmEntityContainer edmExtendedEntityContainer) {
    this.edmExtendedEntityContainer = edmExtendedEntityContainer;
  }

  public void setEntityContainerHierachy(List<EdmEntityContainer> entityContainerHierachy) {
    this.entityContainerHierachy = entityContainerHierachy;
  }

  public String getExtendz() {
    return extendz;
  }

  public void setExtendz(String extendz) {
    this.extendz = extendz;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAnnotations(EdmAnnotations annotations) {
    this.annotations = annotations;
  }
  public EdmEntityContainerImpl(final EdmImpl edm)
      throws EdmException {
    this.edm = edm;
    edmEntitySets = new ArrayList<EdmEntitySet>();
    edmAssociationSets = new ArrayList<EdmAssociationSet>();
    edmFunctionImports = new ArrayList<EdmFunctionImport>();
  }

  public boolean isDefaultContainer() {
    return isDefaultContainer;
  }

  public void setDefaultContainer(boolean isDefaultContainer) {
    this.isDefaultContainer = isDefaultContainer;
  }
  @Override
  public String getName() throws EdmException {
    return name;
  }

  @Override
  public EdmEntitySet getEntitySet(final String name) throws EdmException {
    EdmEntitySet edmEntitySet = null;
      for(EdmEntitySet entity:edmEntitySets){
        if(name.equals(entity.getName())){
          edmEntitySet = entity;
                
      }
    }
    return edmEntitySet;
   
  }

  @Override
  public EdmFunctionImport getFunctionImport(final String name) throws EdmException {
    for (EdmFunctionImport edmFunctionImport : edmFunctionImports) {
      if (edmFunctionImport.getName().equalsIgnoreCase(name)) {
        return edmFunctionImport;
      }
    }
    return null;
  }

  @Override
  public EdmAssociationSet getAssociationSet(final EdmEntitySet sourceEntitySet,
      final EdmNavigationProperty navigationProperty) throws EdmException {
    EdmAssociation edmAssociation = navigationProperty.getRelationship();
    String association = edmAssociation.getNamespace() + "." + edmAssociation.getName();
    String entitySetName = sourceEntitySet.getName();
    String entitySetFromRole = navigationProperty.getFromRole();

    String key = entitySetName + ">>" + association + ">>" + entitySetFromRole;

    for (Entry<String, EdmAssociationSet> edmAssociationSet : edmAssociationSetMap.entrySet()) {
      if (edmAssociationSet.getKey().equalsIgnoreCase(key)) {
        return edmAssociationSet.getValue();
      }
    }
   return null;

  }

  @Override
  public boolean isDefaultEntityContainer() {
    return isDefaultContainer;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return annotations;
  }

  @Override
  public List<EdmEntitySet> getEntitySets() throws EdmException {
    return edmEntitySets;
  }

 

  @Override
  public List<EdmAssociationSet> getAssociationSets() throws EdmException {
    return edmAssociationSets;
  }

  public  List<EdmEntitySet> getEdmEntitySets() {
    return edmEntitySets;
  }

  public EdmEntityContainerImpl setEdmEntitySets( List<EdmEntitySet> edmEntitySets) {
    this.edmEntitySets = edmEntitySets;
    return this;
  }

  public List<EdmAssociationSet> getEdmAssociationSets() {
    return edmAssociationSets;
  }

  public EdmEntityContainerImpl setEdmAssociationSets(List<EdmAssociationSet> edmAssociationSets) {
    this.edmAssociationSets = edmAssociationSets;
    return this;
  }

  public List<EdmFunctionImport> getEdmFunctionImports() {
    return edmFunctionImports;
  }

  public EdmEntityContainerImpl setEdmFunctionImports(List<EdmFunctionImport> edmFunctionImports) {
    this.edmFunctionImports = edmFunctionImports;
    return this;
  }  
  @Override
  public String toString() {
      return String.format(name);
  }
}
