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
package org.apache.olingo.odata2.processor.api.jpa.access;

import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPAModelException;
import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPARuntimeException;

/**
 * JPAEdmBuilder interface provides methods for building elements of an Entity Data Model (EDM) from
 * a Java Persistence Model.
 * 
 * @author
 * 
 */
public interface JPAEdmBuilder {
  /**
   * The Method builds EDM Elements by transforming JPA MetaModel. The method
   * processes EDM JPA Containers which could be accessed using the following
   * views,
   * <ul>
   * <li>
   * {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmAssociationSetView}
   * </li>
   * <li>
   * {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmAssociationView}</li>
   * <li> {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmBaseView}</li>
   * <li>
   * {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmComplexPropertyView}
   * </li>
   * <li>
   * {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmComplexTypeView}</li>
   * <li>
   * {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmEntityContainerView}
   * </li>
   * <li>
   * {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmEntitySetView}</li>
   * <li>
   * {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmEntityTypeView}</li>
   * <li> {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmKeyView}</li>
   * <li> {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmModelView}</li>
   * <li>
   * {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmNavigationPropertyView}
   * </li>
   * <li>
   * {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmPropertyView}</li>
   * <li>
   * {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmReferentialConstraintRoleView}
   * </li>
   * <li>
   * {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmReferentialConstraintView}
   * </li>
   * <li> {@link org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmSchemaView}</li>
   * </ul>
   * 
   * @throws ODataJPARuntimeException
   **/
  public void build() throws ODataJPAModelException, ODataJPARuntimeException;
}
