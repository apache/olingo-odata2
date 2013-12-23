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
package org.apache.olingo.odata2.processor.core.jpa.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.processor.api.jpa.access.JPAEdmBuilder;
import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPAModelException;
import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmAssociationView;
import org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmComplexTypeView;
import org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmEntityContainerView;
import org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmEntitySetView;
import org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmExtension;
import org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmFunctionImportView;
import org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmModelView;
import org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.processor.core.jpa.access.model.JPAEdmNameBuilder;

public class JPAEdmSchema extends JPAEdmBaseViewImpl implements JPAEdmSchemaView {

  private Schema schema;
  private JPAEdmComplexTypeView complexTypeView;
  private JPAEdmEntityContainerView entityContainerView;
  private JPAEdmAssociationView associationView = null;
  private HashMap<Class<?>, String[]> customOperations = null;

  public JPAEdmSchema(final JPAEdmModelView modelView) {
    super(modelView);
  }

  @Override
  public Schema getEdmSchema() {
    return schema;
  }

  @Override
  public JPAEdmEntityContainerView getJPAEdmEntityContainerView() {
    return entityContainerView;
  }

  @Override
  public JPAEdmComplexTypeView getJPAEdmComplexTypeView() {
    return complexTypeView;
  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmSchemaBuilder();
    }

    return builder;
  }

  @Override
  public void clean() {
    super.clean();
    schema = null;
  }

  private class JPAEdmSchemaBuilder implements JPAEdmBuilder {
    /*
     * 
     * Each call to build method creates a new EDM Schema. The newly created
     * schema is built with Entity Containers, associations, Complex Types
     * and Entity Types.
     * 
     * ************************************************************ Build
     * EDM Schema - STEPS
     * ************************************************************ 1) Build
     * Name for EDM Schema 2) Build EDM Complex Types from JPA Embeddable
     * Types 3) Add EDM Complex Types to EDM Schema 4) Build EDM Entity
     * Container 5) Add EDM Entity Container to EDM Schema 6) Fetch Built
     * EDM Entity Types from EDM Entity Container 7) Add EDM Entity Types to
     * EDM Schema 8) Fetch Built EDM Association Sets from EDM Entity
     * Container 9) Fetch Built EDM Associations from EDM Association Set
     * 10) Add EDM Association to EDM Schema
     * ************************************************************ Build
     * EDM Schema - STEPS
     * ************************************************************
     */
    @Override
    public void build() throws ODataJPAModelException, ODataJPARuntimeException {

      schema = new Schema();
      JPAEdmNameBuilder.build(JPAEdmSchema.this);

      associationView = new JPAEdmAssociation(JPAEdmSchema.this);

      complexTypeView = new JPAEdmComplexType(JPAEdmSchema.this);
      complexTypeView.getBuilder().build();

      entityContainerView = new JPAEdmEntityContainer(JPAEdmSchema.this);
      entityContainerView.getBuilder().build();
      schema.setEntityContainers(entityContainerView.getConsistentEdmEntityContainerList());

      JPAEdmEntitySetView entitySetView = entityContainerView.getJPAEdmEntitySetView();
      if (entitySetView.isConsistent() && entitySetView.getJPAEdmEntityTypeView() != null) {
        JPAEdmEntityTypeView entityTypeView = entitySetView.getJPAEdmEntityTypeView();
        if (entityTypeView.isConsistent() && !entityTypeView.getConsistentEdmEntityTypes().isEmpty()) {
          schema.setEntityTypes(entityTypeView.getConsistentEdmEntityTypes());
        }
      }
      if (complexTypeView.isConsistent()) {
        List<ComplexType> complexTypes = complexTypeView.getConsistentEdmComplexTypes();
        List<ComplexType> existingComplexTypes = new ArrayList<ComplexType>();
        for (ComplexType complexType : complexTypes) {
          if (complexType != null && complexTypeView.isReferencedInKey(complexType.getName())) {// null check for
                                                                                                // exclude
            existingComplexTypes.add(complexType);
          }
        }
        if (!existingComplexTypes.isEmpty()) {
          schema.setComplexTypes(existingComplexTypes);
        }
      }

      List<String> existingAssociationList = new ArrayList<String>();
      if (associationView.isConsistent() && !associationView.getConsistentEdmAssociationList().isEmpty()) {

        List<Association> consistentAssociationList = associationView.getConsistentEdmAssociationList();
        schema.setAssociations(consistentAssociationList);
        for (Association association : consistentAssociationList) {
          existingAssociationList.add(association.getName());
        }

      }
      List<EntityType> entityTypes =
          entityContainerView.getJPAEdmEntitySetView().getJPAEdmEntityTypeView().getConsistentEdmEntityTypes();
      List<NavigationProperty> navigationProperties;
      if (entityTypes != null && !entityTypes.isEmpty()) {
        for (EntityType entityType : entityTypes) {

          List<NavigationProperty> consistentNavigationProperties = null;
          navigationProperties = entityType.getNavigationProperties();
          if (navigationProperties != null) {
            consistentNavigationProperties = new ArrayList<NavigationProperty>();
            for (NavigationProperty navigationProperty : navigationProperties) {
              if (existingAssociationList.contains(navigationProperty.getRelationship().getName())) {
                consistentNavigationProperties.add(navigationProperty);
              }
            }
            if (consistentNavigationProperties.isEmpty()) {
              entityType.setNavigationProperties(null);
            } else {
              entityType.setNavigationProperties(consistentNavigationProperties);
            }
          }

        }
      }

      JPAEdmExtension edmExtension = getJPAEdmExtension();
      if (edmExtension != null) {
        edmExtension.extendJPAEdmSchema(JPAEdmSchema.this);
        edmExtension.extendWithOperation(JPAEdmSchema.this);

        JPAEdmFunctionImportView functionImportView = new JPAEdmFunctionImport(JPAEdmSchema.this);
        functionImportView.getBuilder().build();
        if (functionImportView.getConsistentFunctionImportList() != null) {
          entityContainerView.getEdmEntityContainer().setFunctionImports(
              functionImportView.getConsistentFunctionImportList());
        }

      }
    }

  }

  @Override
  public final JPAEdmAssociationView getJPAEdmAssociationView() {
    return associationView;
  }

  @Override
  public void registerOperations(final Class<?> customClass, final String[] methodNames) {
    if (customOperations == null) {
      customOperations = new HashMap<Class<?>, String[]>();
    }

    customOperations.put(customClass, methodNames);

  }

  @Override
  public HashMap<Class<?>, String[]> getRegisteredOperations() {
    return customOperations;
  }
}
