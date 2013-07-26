/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.edm.provider;

import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationSet;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.exception.ODataException;

/**
 * @author SAP AG
 */
public class EdmEntityContainerImplProv implements EdmEntityContainer, EdmAnnotatable {

  private EdmImplProv edm;
  private EntityContainerInfo entityContainer;
  private Map<String, EdmEntitySet> edmEntitySets;
  private Map<String, EdmAssociationSet> edmAssociationSets;
  private Map<String, EdmFunctionImport> edmFunctionImports;
  private EdmEntityContainer edmExtendedEntityContainer;
  private boolean isDefaultContainer;

  public EdmEntityContainerImplProv(final EdmImplProv edm, final EntityContainerInfo entityContainer) throws EdmException {
    this.edm = edm;
    this.entityContainer = entityContainer;
    edmEntitySets = new HashMap<String, EdmEntitySet>();
    edmAssociationSets = new HashMap<String, EdmAssociationSet>();
    edmFunctionImports = new HashMap<String, EdmFunctionImport>();
    isDefaultContainer = entityContainer.isDefaultEntityContainer();

    if (entityContainer.getExtendz() != null) {
      edmExtendedEntityContainer = edm.getEntityContainer(entityContainer.getExtendz());
      if (edmExtendedEntityContainer == null) {
        throw new EdmException(EdmException.COMMON);
      }
    }
  }

  @Override
  public String getName() throws EdmException {
    return entityContainer.getName();
  }

  @Override
  public EdmEntitySet getEntitySet(final String name) throws EdmException {
    EdmEntitySet edmEntitySet = edmEntitySets.get(name);
    if (edmEntitySet != null) {
      return edmEntitySet;
    }

    EntitySet entitySet;
    try {
      entitySet = edm.edmProvider.getEntitySet(entityContainer.getName(), name);
    } catch (ODataException e) {
      throw new EdmException(EdmException.PROVIDERPROBLEM, e);
    }

    if (entitySet != null) {
      edmEntitySet = createEntitySet(entitySet);
      edmEntitySets.put(name, edmEntitySet);
    } else if (edmExtendedEntityContainer != null) {
      edmEntitySet = edmExtendedEntityContainer.getEntitySet(name);
      if (edmEntitySet != null) {
        edmEntitySets.put(name, edmEntitySet);
      }
    }

    return edmEntitySet;
  }

  @Override
  public EdmFunctionImport getFunctionImport(final String name) throws EdmException {
    EdmFunctionImport edmFunctionImport = edmFunctionImports.get(name);
    if (edmFunctionImport != null) {
      return edmFunctionImport;
    }

    FunctionImport functionImport;
    try {
      functionImport = edm.edmProvider.getFunctionImport(entityContainer.getName(), name);
    } catch (ODataException e) {
      throw new EdmException(EdmException.PROVIDERPROBLEM, e);
    }

    if (functionImport != null) {
      edmFunctionImport = createFunctionImport(functionImport);
      edmFunctionImports.put(name, edmFunctionImport);
    } else if (edmExtendedEntityContainer != null) {
      edmFunctionImport = edmExtendedEntityContainer.getFunctionImport(name);
      if (edmFunctionImport != null) {
        edmFunctionImports.put(name, edmFunctionImport);
      }
    }

    return edmFunctionImport;
  }

  @Override
  public EdmAssociationSet getAssociationSet(final EdmEntitySet sourceEntitySet, final EdmNavigationProperty navigationProperty) throws EdmException {
    EdmAssociation edmAssociation = navigationProperty.getRelationship();
    String association = edmAssociation.getNamespace() + "." + edmAssociation.getName();
    String entitySetName = sourceEntitySet.getName();
    String entitySetFromRole = navigationProperty.getFromRole();

    String key = entitySetName + ">>" + association + ">>" + entitySetFromRole;

    EdmAssociationSet edmAssociationSet = edmAssociationSets.get(key);
    if (edmAssociationSet != null) {
      return edmAssociationSet;
    }

    AssociationSet associationSet;
    FullQualifiedName associationFQName = new FullQualifiedName(edmAssociation.getNamespace(), edmAssociation.getName());
    try {
      associationSet = edm.edmProvider.getAssociationSet(entityContainer.getName(), associationFQName, entitySetName, entitySetFromRole);
    } catch (ODataException e) {
      throw new EdmException(EdmException.PROVIDERPROBLEM, e);
    }

    if (associationSet != null) {
      edmAssociationSet = createAssociationSet(associationSet);
      edmAssociationSets.put(key, edmAssociationSet);
      return edmAssociationSet;
    } else if (edmExtendedEntityContainer != null) {
      edmAssociationSet = edmExtendedEntityContainer.getAssociationSet(sourceEntitySet, navigationProperty);
      edmAssociationSets.put(key, edmAssociationSet);
      return edmAssociationSet;
    } else {
      throw new EdmException(EdmException.COMMON);
    }
  }

  private EdmEntitySet createEntitySet(final EntitySet entitySet) throws EdmException {
    return new EdmEntitySetImplProv(edm, entitySet, this);
  }

  private EdmFunctionImport createFunctionImport(final FunctionImport functionImport) throws EdmException {
    return new EdmFunctionImportImplProv(edm, functionImport, this);
  }

  private EdmAssociationSet createAssociationSet(final AssociationSet associationSet) throws EdmException {
    return new EdmAssociationSetImplProv(edm, associationSet, this);
  }

  @Override
  public boolean isDefaultEntityContainer() {
    return isDefaultContainer;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return new EdmAnnotationsImplProv(entityContainer.getAnnotationAttributes(), entityContainer.getAnnotationElements());
  }
}
