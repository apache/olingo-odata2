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
package org.apache.olingo.odata2.api.edm;

import java.util.List;

/**
 * @org.apache.olingo.odata2.DoNotImplement
 * A CSDL EntityContainer element
 * 
 * <p>EdmEntityContainer hold the information of EntitySets, FunctionImports and AssociationSets contained
 * 
 */
public interface EdmEntityContainer extends EdmNamed, EdmAnnotatable {

  /**
   * @return <b>boolean</b> true if this is the default container
   */
  boolean isDefaultEntityContainer();

  /**
   * Get contained EntitySet by name
   * 
   * @param name
   * @return {@link EdmEntitySet}
   * @throws EdmException
   */
  EdmEntitySet getEntitySet(String name) throws EdmException;

  /**
   * <b>ATTENTION:</b> This method does not support <b>LAZY LOADING</b>.
   * <br/>
   * Get list of all contained EntitySets.
   * 
   * @return with all contained {@link EdmEntitySet}
   * @throws EdmException
   */
  List<EdmEntitySet> getEntitySets() throws EdmException;

  /**
   * Get contained FunctionImport by name
   * 
   * @param name
   * @return {@link EdmFunctionImport}
   * @throws EdmException
   */
  EdmFunctionImport getFunctionImport(String name) throws EdmException;

  /**
   * Get contained AssociationSet by providing the source entity set and the navigation property
   * 
   * @param sourceEntitySet of type {@link EdmEntitySet}
   * @param navigationProperty of type {@link EdmNavigationProperty}
   * @return {@link EdmAssociationSet}
   * @throws EdmException
   */
  EdmAssociationSet getAssociationSet(EdmEntitySet sourceEntitySet, EdmNavigationProperty navigationProperty)
      throws EdmException;

  /**
   * <b>ATTENTION:</b> This method does not support <b>LAZY LOADING</b>.
   * <br/>
   * Get list of all contained AssociationSets
   * 
   * @return list with all contained {@link EdmAssociationSet}
   * @throws EdmException
   */
  List<EdmAssociationSet> getAssociationSets() throws EdmException;
}
