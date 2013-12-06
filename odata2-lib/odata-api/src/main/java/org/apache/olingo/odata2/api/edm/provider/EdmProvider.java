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
package org.apache.olingo.odata2.api.edm.provider;

import java.util.List;

import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.exception.ODataException;

/**
 * Default EDM Provider which is to be extended by the application
 * 
 * 
 */
public abstract class EdmProvider {

  /**
   * This method should return an {@link EntityContainerInfo} or <b>null</b> if nothing is found
   * @param name (null for default container)
   * @return {@link EntityContainerInfo} for the given name
   * @throws ODataException
   */
  public EntityContainerInfo getEntityContainerInfo(final String name) throws ODataException {
    return null;
  }

  /**
   * This method should return an {@link EntityType} or <b>null</b> if nothing is found
   * @param edmFQName
   * @return {@link EntityType} for the given name
   * @throws ODataException
   */
  public EntityType getEntityType(final FullQualifiedName edmFQName) throws ODataException {
    return null;
  }

  /**
   * This method should return a {@link ComplexType} or <b>null</b> if nothing is found
   * @param edmFQName
   * @return {@link ComplexType} for the given name
   * @throws ODataException
   */
  public ComplexType getComplexType(final FullQualifiedName edmFQName) throws ODataException {
    return null;
  }

  /**
   * This method should return an {@link Association} or <b>null</b> if nothing is found
   * @param edmFQName
   * @return {@link Association} for the given name
   * @throws ODataException
   */
  public Association getAssociation(final FullQualifiedName edmFQName) throws ODataException {
    return null;
  }

  /**
   * This method should return an {@link EntitySet} or <b>null</b> if nothing is found
   * @param entityContainer
   * @param name
   * @return {@link EntitySet} for the given container name and entity set name
   * @throws ODataException
   */
  public EntitySet getEntitySet(final String entityContainer, final String name) throws ODataException {
    return null;
  }

  /**
   * This method should return an {@link AssociationSet} or <b>null</b> if nothing is found
   * @param entityContainer
   * @param association
   * @param sourceEntitySetName
   * @param sourceEntitySetRole
   * @return {@link AssociationSet} for the given container name, association name, source entity set name and source
   * entity set role
   * @throws ODataException
   */
  public AssociationSet getAssociationSet(final String entityContainer, final FullQualifiedName association,
      final String sourceEntitySetName, final String sourceEntitySetRole) throws ODataException {
    return null;
  }

  /**
   * This method should return a {@link FunctionImport} or <b>null</b> if nothing is found
   * @param entityContainer
   * @param name
   * @return {@link FunctionImport} for the given container name and function import name
   * @throws ODataException
   */
  public FunctionImport getFunctionImport(final String entityContainer, final String name) throws ODataException {
    return null;
  }

  /**
   * This method should return a collection of all {@link Schema} or <b>null</b> if nothing is found
   * @return List<{@link Schema}>
   * @throws ODataException
   */
  public List<Schema> getSchemas() throws ODataException {
    return null;
  }

  /**
   * This method should return a list of all defined aliases and their associated namespace. If not implemented aliases
   * cannot be resolved
   * @return List<{@link AliasInfo}>
   * @throws ODataException
   */
  public List<AliasInfo> getAliasInfos() throws ODataException {
    return null;
  }

}
