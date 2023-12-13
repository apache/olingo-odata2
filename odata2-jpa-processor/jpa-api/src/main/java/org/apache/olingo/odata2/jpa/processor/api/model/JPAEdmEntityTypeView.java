/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.apache.olingo.odata2.jpa.processor.api.model;

import java.util.List;
import org.apache.olingo.odata2.api.edm.provider.EntityType;

/**
 * A view on Java Persistence entity types and EDM entity types. Java persistence entity types are
 * converted into EDM entity types.
 * <p>
 * The implementation of the view provides access to EDM entity types for the given JPA EDM model.
 * The view acts as a container for consistent list of EDM entity types. An EDM entity type is said
 * to be consistent only if it has at least one consistent EDM property and at least one consistent
 * EDM key.
 *
 *
 * <p>
 *
 * @org.apache.olingo.odata2.DoNotImplement
 * @see org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView
 * @see org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmKeyView
 * @see org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmNavigationPropertyView
 *
 */
public interface JPAEdmEntityTypeView extends JPAEdmBaseView {
    /**
     * The method returns an EDM entity currently being processed.
     *
     * @return an instance of type {@link org.apache.olingo.odata2.api.edm.provider.EntityType}
     */
    EntityType getEdmEntityType();

    /**
     * The method returns java persistence Entity type currently being processed.
     *
     * @return an instance of type {@link javax.persistence.metamodel.EntityType}
     */
    jakarta.persistence.metamodel.EntityType<?> getJPAEntityType();

    /**
     * The method returns a consistent list of EDM entity types for a given java persistence meta model.
     *
     * @return a list of {@link org.apache.olingo.odata2.api.edm.provider.EntityType}
     */
    List<EntityType> getConsistentEdmEntityTypes();

    /**
     * The method searches in the consistent list of EDM entity types for the given EDM entity type's
     * name.
     *
     * @param jpaEntityTypeName is the name of EDM entity type
     * @return a reference to EDM entity type if found else null
     */
    EntityType searchEdmEntityType(String jpaEntityTypeName);

}
