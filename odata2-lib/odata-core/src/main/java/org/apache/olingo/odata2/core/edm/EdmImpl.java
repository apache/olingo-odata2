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
package org.apache.olingo.odata2.core.edm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmServiceMetadata;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.exception.ODataException;

/**
 *  
 */
public abstract class EdmImpl implements Edm {

  private Map<String, EdmEntityContainer> edmEntityContainers;
  private Map<FullQualifiedName, EdmEntityType> edmEntityTypes;
  private Map<FullQualifiedName, EdmComplexType> edmComplexTypes;
  private Map<FullQualifiedName, EdmAssociation> edmAssociations;
  private Map<String, String> aliasToNamespaceInfo;
  private List<EdmEntitySet> edmEntitySets;
  private List<EdmFunctionImport> edmFunctionImports;

  protected EdmServiceMetadata edmServiceMetadata;

  public EdmImpl(final EdmServiceMetadata edmServiceMetadata) {
    edmEntityContainers = new HashMap<String, EdmEntityContainer>();
    edmEntityTypes = new HashMap<FullQualifiedName, EdmEntityType>();
    edmComplexTypes = new HashMap<FullQualifiedName, EdmComplexType>();
    edmAssociations = new HashMap<FullQualifiedName, EdmAssociation>();
    this.edmServiceMetadata = edmServiceMetadata;
  }

  @Override
  public EdmEntityContainer getEntityContainer(final String name) throws EdmException {
    if (edmEntityContainers.containsKey(name)) {
      return edmEntityContainers.get(name);
    }

    EdmEntityContainer edmEntityContainer = null;

    try {
      edmEntityContainer = createEntityContainer(name);
      if (edmEntityContainer != null) {
        if (name == null && edmEntityContainers.containsKey(edmEntityContainer.getName())) {
          // ensure that the same default entity container is stored in the HashMap under null and its name
          edmEntityContainer = edmEntityContainers.get(edmEntityContainer.getName());
          edmEntityContainers.put(name, edmEntityContainer);
        } else if (edmEntityContainers.containsKey(null) && edmEntityContainers.get(null) != null
            && name != null && name.equals(edmEntityContainers.get(null).getName())) {
          // ensure that the same default entity container is stored in the HashMap under null and its name
          edmEntityContainer = edmEntityContainers.get(null);
          edmEntityContainers.put(name, edmEntityContainer);
        } else {
          edmEntityContainers.put(name, edmEntityContainer);
        }
      }
    } catch (EdmException e) {
      throw e;
    } catch (ODataException e) {
      throw new EdmException(EdmException.COMMON, e);
    }

    return edmEntityContainer;
  }

  @Override
  public EdmEntityType getEntityType(final String namespaceOrAlias, final String name) throws EdmException {
    String finalNamespace = getNamespaceForAlias(namespaceOrAlias);

    FullQualifiedName fqName = new FullQualifiedName(finalNamespace, name);
    if (edmEntityTypes.containsKey(fqName)) {
      return edmEntityTypes.get(fqName);
    }

    EdmEntityType edmEntityType = null;

    try {
      edmEntityType = createEntityType(fqName);
      if (edmEntityType != null) {
        edmEntityTypes.put(fqName, edmEntityType);
      }
    } catch (EdmException e) {
      throw e;
    } catch (ODataException e) {
      throw new EdmException(EdmException.COMMON, e);
    }

    return edmEntityType;
  }

  private String getNamespaceForAlias(final String namespaceOrAlias) throws EdmException {
    if (aliasToNamespaceInfo == null) {
      try {
        aliasToNamespaceInfo = createAliasToNamespaceInfo();
        if (aliasToNamespaceInfo == null) {
          aliasToNamespaceInfo = new HashMap<String, String>();
        }
      } catch (EdmException e) {
        throw e;
      } catch (ODataException e) {
        throw new EdmException(EdmException.COMMON, e);
      }
    }
    String namespace = aliasToNamespaceInfo.get(namespaceOrAlias);
    // If not contained in info it must be a namespace
    if (namespace == null) {
      namespace = namespaceOrAlias;
    }
    return namespace;
  }

  @Override
  public EdmComplexType getComplexType(final String namespaceOrAlias, final String name) throws EdmException {
    String finalNamespace = getNamespaceForAlias(namespaceOrAlias);
    FullQualifiedName fqName = new FullQualifiedName(finalNamespace, name);
    if (edmComplexTypes.containsKey(fqName)) {
      return edmComplexTypes.get(fqName);
    }

    EdmComplexType edmComplexType = null;

    try {
      edmComplexType = createComplexType(fqName);
      if (edmComplexType != null) {
        edmComplexTypes.put(fqName, edmComplexType);
      }
    } catch (EdmException e) {
      throw e;
    } catch (ODataException e) {
      throw new EdmException(EdmException.COMMON, e);
    }

    return edmComplexType;
  }

  @Override
  public EdmAssociation getAssociation(final String namespaceOrAlias, final String name) throws EdmException {
    String finalNamespace = getNamespaceForAlias(namespaceOrAlias);
    FullQualifiedName fqName = new FullQualifiedName(finalNamespace, name);
    if (edmAssociations.containsKey(fqName)) {
      return edmAssociations.get(fqName);
    }

    EdmAssociation edmAssociation = null;

    try {
      edmAssociation = createAssociation(fqName);
      if (edmAssociation != null) {
        edmAssociations.put(fqName, edmAssociation);
      }
    } catch (EdmException e) {
      throw e;
    } catch (ODataException e) {
      throw new EdmException(EdmException.COMMON, e);
    }

    return edmAssociation;
  }

  @Override
  public EdmServiceMetadata getServiceMetadata() {
    return edmServiceMetadata;
  }

  @Override
  public EdmEntityContainer getDefaultEntityContainer() throws EdmException {
    return getEntityContainer(null);
  }

  @Override
  public List<EdmEntitySet> getEntitySets() throws EdmException {
    try {
      if (edmEntitySets == null) {
        edmEntitySets = createEntitySets();
      }
    } catch (EdmException e) {
      throw e;
    } catch (ODataException e) {
      throw new EdmException(EdmException.COMMON, e);
    }
    return edmEntitySets;
  }

  @Override
  public List<EdmFunctionImport> getFunctionImports() throws EdmException {
    try {
      if (edmFunctionImports == null) {
        edmFunctionImports = createFunctionImports();
      }
    } catch (EdmException e) {
      throw e;
    } catch (ODataException e) {
      throw new EdmException(EdmException.COMMON, e);
    }
    return edmFunctionImports;
  }

  protected abstract EdmEntityContainer createEntityContainer(String name) throws ODataException;

  protected abstract EdmEntityType createEntityType(FullQualifiedName fqName) throws ODataException;

  protected abstract EdmComplexType createComplexType(FullQualifiedName fqName) throws ODataException;

  protected abstract EdmAssociation createAssociation(FullQualifiedName fqName) throws ODataException;

  protected abstract List<EdmEntitySet> createEntitySets() throws ODataException;

  protected abstract List<EdmFunctionImport> createFunctionImports() throws ODataException;

  protected abstract Map<String, String> createAliasToNamespaceInfo() throws ODataException;
}
