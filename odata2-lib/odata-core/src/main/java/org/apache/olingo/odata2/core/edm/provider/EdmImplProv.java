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
package org.apache.olingo.odata2.core.edm.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.AliasInfo;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EdmProviderAccessor;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.core.edm.EdmImpl;

public class EdmImplProv extends EdmImpl implements EdmProviderAccessor {

  protected EdmProvider edmProvider;
  private List<Schema> schemas;

  public EdmImplProv(final EdmProvider edmProvider) {
    super(new EdmServiceMetadataImplProv(edmProvider));
    this.edmProvider = edmProvider;
  }

  @Override
  protected EdmEntityContainer createEntityContainer(final String name) throws ODataException {
    EntityContainerInfo enitityContainerInfo = edmProvider.getEntityContainerInfo(name);
    if (enitityContainerInfo == null) {
      return null;
    }
    return new EdmEntityContainerImplProv(this, enitityContainerInfo);
  }

  @Override
  protected EdmEntityType createEntityType(final FullQualifiedName fqName) throws ODataException {
    EntityType entityType = edmProvider.getEntityType(fqName);
    if (entityType == null) {
      return null;
    }

    return new EdmEntityTypeImplProv(this, entityType, fqName.getNamespace());
  }

  @Override
  protected EdmComplexType createComplexType(final FullQualifiedName fqName) throws ODataException {
    ComplexType complexType = edmProvider.getComplexType(fqName);
    if (complexType == null) {
      return null;
    }
    return new EdmComplexTypeImplProv(this, complexType, fqName.getNamespace());
  }

  @Override
  protected EdmAssociation createAssociation(final FullQualifiedName fqName) throws ODataException {
    Association association = edmProvider.getAssociation(fqName);
    if (association == null) {
      return null;
    }
    return new EdmAssociationImplProv(this, association, fqName.getNamespace());
  }

  @Override
  public EdmProvider getEdmProvider() {
    return edmProvider;
  }

  @Override
  protected List<EdmEntitySet> createEntitySets() throws ODataException {
    List<EdmEntitySet> edmEntitySets = new ArrayList<EdmEntitySet>();
    if (schemas == null) {
      schemas = edmProvider.getSchemas();
    }
    for (Schema schema : schemas) {
      for (EntityContainer entityContainer : schema.getEntityContainers()) {
        for (EntitySet entitySet : entityContainer.getEntitySets()) {
          EdmEntityContainer edmEntityContainer = createEntityContainer(entityContainer.getName());
          edmEntitySets.add(new EdmEntitySetImplProv(this, entitySet, edmEntityContainer));
        }
      }
    }
    return edmEntitySets;
  }

  @Override
  protected List<EdmFunctionImport> createFunctionImports() throws ODataException {
    List<EdmFunctionImport> edmFunctionImports = new ArrayList<EdmFunctionImport>();
    if (schemas == null) {
      schemas = edmProvider.getSchemas();
    }
    for (Schema schema : schemas) {
      for (EntityContainer entityContainer : schema.getEntityContainers()) {
        for (FunctionImport functionImport : entityContainer.getFunctionImports()) {
          EdmEntityContainer edmEntityContainer = createEntityContainer(entityContainer.getName());
          edmFunctionImports.add(new EdmFunctionImportImplProv(this, functionImport, edmEntityContainer));
        }
      }
    }
    return edmFunctionImports;
  }

  @Override
  protected Map<String, String> createAliasToNamespaceInfo() throws ODataException {
    List<AliasInfo> aliasInfos = edmProvider.getAliasInfos();
    Map<String, String> aliasToNamespaceInfo = new HashMap<String, String>();
    if (aliasInfos != null) {
      for (AliasInfo info : aliasInfos) {
        aliasToNamespaceInfo.put(info.getAlias(), info.getNamespace());
      }
    }
    return aliasToNamespaceInfo;
  }
}
