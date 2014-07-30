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
package org.apache.olingo.odata2.jpa.processor.api.model;

import java.io.InputStream;

/**
 * The interface provides methods to extend JPA EDM containers.
 * 
 * 
 * 
 */
public interface JPAEdmExtension {

  /**
   * The method is used to extend the JPA EDM schema view with custom operations. Use this method to
   * register custom operations.
   * 
   * @param view
   * is the schema view
   * @see org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView#registerOperations(Class, String[])
   * 
   */
  public void extendWithOperation(JPAEdmSchemaView view);

  /**
   * The method is used to extend the JPA EDM schema view with Entities, Entity Sets, Navigation Property and
   * Association.
   * 
   * @param view
   * is the schema view
   * 
   */
  public void extendJPAEdmSchema(JPAEdmSchemaView view);

  /**
   * Implement this method to provide a stream of Mapping model.
   * @return
   * a stream of mapping model XML as per JPAEDMMappingModel.xsd
   */
  public InputStream getJPAEdmMappingModelStream();

}
