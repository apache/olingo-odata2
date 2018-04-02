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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmServiceMetadata;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.client.api.edm.ClientEdm;
import org.apache.olingo.odata2.client.api.edm.EdmSchema;

/**
 * Objects of this class represent Edm
 */
public class EdmImpl implements ClientEdm {

  protected Map<FullQualifiedName, EdmEntityContainer> edmEntityContainers;
  protected Map<FullQualifiedName, EdmEntityType> edmEntityTypes;
  private Map<FullQualifiedName, EdmComplexType> edmComplexTypes;
  private Map<FullQualifiedName, EdmAssociation> edmAssociations;
  private Map<String, String> aliasToNamespaceInfo;
  private List<EdmEntitySet> edmEntitySets;
  private List<EdmFunctionImport> edmFunctionImports;
  private List<EdmSchema> edmSchema;
  private EdmEntityContainer defaultEdmEntityContainer;

  public EdmImpl() {
    edmEntityContainers = new HashMap<FullQualifiedName, EdmEntityContainer>();
    edmEntityTypes = new HashMap<FullQualifiedName, EdmEntityType>();
    edmComplexTypes = new HashMap<FullQualifiedName, EdmComplexType>();
    edmAssociations = new HashMap<FullQualifiedName, EdmAssociation>();
    aliasToNamespaceInfo = new HashMap<String, String>();
  }

  @Override
  public EdmEntityContainer getEntityContainer(final String name)
      throws EdmException {
    for (Entry<FullQualifiedName, EdmEntityContainer> entry : edmEntityContainers.entrySet()) {
      if (entry.getKey().getName().equals(name)) {
        return entry.getValue();
      }
    }
    return null;
  }

  public EdmImpl setEdmSchemas(List<EdmSchema> edmSchema) {
    this.edmSchema = edmSchema;
    return this;
  }

  @Override
  public EdmEntityType getEntityType(final String namespaceOrAlias,
      final String name) throws EdmException {
    FullQualifiedName fqName = getNamespaceForAlias(namespaceOrAlias, name);
    return edmEntityTypes.get(fqName);
  }

  private FullQualifiedName getNamespaceForAlias(final String namespaceOrAlias, String name)
      throws EdmException {
    String namespace = aliasToNamespaceInfo.get(namespaceOrAlias);
    if (namespace != null) {
      //Namespace to alias mapping found
      return new FullQualifiedName(namespace, name);
    } else {
      //No mapping found. Parameter must be the namespace
      return new FullQualifiedName(namespaceOrAlias, name);
    }
  }

  @Override
  public EdmComplexType getComplexType(final String namespaceOrAlias,
      final String name) throws EdmException {
    FullQualifiedName fqName = getNamespaceForAlias(namespaceOrAlias, name);
    return edmComplexTypes.get(fqName);
  }

  @Override
  public EdmAssociation getAssociation(final String namespaceOrAlias,
      final String name) throws EdmException {
    FullQualifiedName fqName = getNamespaceForAlias(namespaceOrAlias, name);
    return edmAssociations.get(fqName);
  }

  public EdmImpl setDefaultEntityContainer(EdmEntityContainer defaultEdmEntityContainer) throws EdmException {
    this.defaultEdmEntityContainer = defaultEdmEntityContainer;
    return this;
  }

  @Override
  public EdmEntityContainer getDefaultEntityContainer() throws EdmException {
    return defaultEdmEntityContainer;
  }

  @Override
  public List<EdmEntitySet> getEntitySets() throws EdmException {
    return edmEntitySets;
  }

  @Override
  public List<EdmFunctionImport> getFunctionImports() throws EdmException {
    return edmFunctionImports;
  }

  @Override
  public EdmServiceMetadata getServiceMetadata() {
    return null;
  }

  public Map<FullQualifiedName, EdmEntityContainer> getEdmEntityContainers() {
    return edmEntityContainers;
  }

  public EdmImpl setEdmEntityContainers(Map<FullQualifiedName, EdmEntityContainer> edmEntityContainers) {
    this.edmEntityContainers = edmEntityContainers;
    return this;
  }

  public Map<FullQualifiedName, EdmEntityType> getEdmEntityTypes() {
    return edmEntityTypes;
  }

  public EdmImpl setEdmEntityTypes(Map<FullQualifiedName, EdmEntityType> edmEntityTypes) {
    this.edmEntityTypes = edmEntityTypes;
    return this;
  }

  public Map<FullQualifiedName, EdmComplexType> getEdmComplexTypes() {
    return edmComplexTypes;
  }

  public EdmImpl setEdmComplexTypes(Map<FullQualifiedName, EdmComplexType> edmComplexTypes) {
    this.edmComplexTypes = edmComplexTypes;
    return this;
  }

  public Map<FullQualifiedName, EdmAssociation> getEdmAssociations() {
    return edmAssociations;
  }

  public EdmImpl setEdmAssociations(Map<FullQualifiedName, EdmAssociation> edmAssociations) {
    this.edmAssociations = edmAssociations;
    return this;
  }

  public Map<String, String> getAliasToNamespaceInfo() {
    return aliasToNamespaceInfo;
  }

  public EdmImpl setAliasToNamespaceInfo(Map<String, String> aliasToNamespaceInfo) {
    this.aliasToNamespaceInfo = aliasToNamespaceInfo;
    return this;
  }

  public List<EdmEntitySet> getEdmEntitySets() {
    return edmEntitySets;
  }

  public EdmImpl setEdmEntitySets(List<EdmEntitySet> edmEntitySets) {
    this.edmEntitySets = edmEntitySets;
    return this;
  }

  public List<EdmFunctionImport> getEdmFunctionImports() {
    return edmFunctionImports;
  }

  public EdmImpl setEdmFunctionImports(List<EdmFunctionImport> edmFunctionImports) {
    this.edmFunctionImports = edmFunctionImports;
    return this;
  }

  @Override
  public List<EdmSchema> getSchemas() {
    return edmSchema;
  }

  @Override
  public String toString() {
    return String.format("EdmImpl");
  }
}
