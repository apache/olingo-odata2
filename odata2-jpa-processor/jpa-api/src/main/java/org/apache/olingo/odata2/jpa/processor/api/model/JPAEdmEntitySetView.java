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

import java.util.List;

import org.apache.olingo.odata2.api.edm.provider.EntitySet;

/**
 * A view on Java Persistence entity type and EDM entity sets. Java persistence
 * entity types are converted into EDM entity types and EDM entity sets.
 * <p>
 * The implementation of the view provides access to EDM entity sets for the
 * given JPA EDM entity type. The view acts as a container for consistent list
 * of EDM entity sets. An EDM entity set is said to be consistent only if it has
 * consistent EDM entity types.
 * 
 * 
 * <p>
 * @org.apache.olingo.odata2.DoNotImplement
 * @see org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView
 * 
 */
public interface JPAEdmEntitySetView extends JPAEdmBaseView {
  /**
   * The method returns an EDM entity set that is currently being processed.
   * 
   * @return an instance of type {@link org.apache.olingo.odata2.api.edm.provider.EntitySet}
   */
  public EntitySet getEdmEntitySet();

  /**
   * The method returns a list of consistent EDM entity sets.
   * 
   * @return a list of EDM entity sets
   */
  public List<EntitySet> getConsistentEdmEntitySetList();

  /**
   * The method returns a JPA EDM entity type view that is currently being
   * processed. JPA EDM entity set view is built from JPA EDM entity type
   * view.
   * 
   * @return an instance of type {@link org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView}
   */
  public JPAEdmEntityTypeView getJPAEdmEntityTypeView();

}
