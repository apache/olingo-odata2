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
package org.apache.olingo.odata2.processor.api.jpa.model;

/**
 * A view on JPA meta model and EDM meta model. The view acts as the base for
 * the construction of EDM meta model from a JPA meta model.
 * <p>
 * The implementation of the view acts as the container for JPA meta model and
 * EDM meta model. The instance of JPA EDM meta model can be created using
 * {@link org.apache.olingo.odata2.processor.api.jpa.factory.JPAAccessFactory}. The
 * instance thus obtained can be used for constructing other elements of the
 * meta model using
 * {@link org.apache.olingo.odata2.processor.api.jpa.access.JPAEdmBuilder}.
 * 
 *  
 * @org.apache.olingo.odata2.DoNotImplement
 * @see org.apache.olingo.odata2.processor.api.jpa.factory.JPAAccessFactory
 */
public interface JPAEdmModelView extends JPAEdmBaseView {
  /**
   * The method returns a consistent JPA EDM schema view created from the JPA
   * meta model.
   * 
   * @return an instance of type
   *         {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmSchemaView}
   */
  public JPAEdmSchemaView getEdmSchemaView();
}
