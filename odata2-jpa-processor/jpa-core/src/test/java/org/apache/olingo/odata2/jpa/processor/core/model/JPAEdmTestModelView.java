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
package org.apache.olingo.odata2.jpa.processor.core.model;

import java.util.HashMap;
import java.util.List;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.Metamodel;

import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraint;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmMappingModelAccess;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationEndView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationSetView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmBaseView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmComplexPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmComplexTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityContainerView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntitySetView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmKeyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmModelView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmNavigationPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmReferentialConstraintView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;

public class JPAEdmTestModelView implements JPAEdmAssociationEndView, JPAEdmAssociationSetView, JPAEdmAssociationView,
    JPAEdmBaseView, JPAEdmComplexPropertyView, JPAEdmComplexTypeView, JPAEdmEntityContainerView, JPAEdmEntitySetView,
    JPAEdmEntityTypeView, JPAEdmKeyView, JPAEdmModelView, JPAEdmNavigationPropertyView, JPAEdmPropertyView,
    JPAEdmReferentialConstraintView, JPAEdmSchemaView {

  protected JPAEdmMappingModelAccess mappingModelAccess;

  @Override
  public Schema getEdmSchema() {
    return null;
  }

  @Override
  public JPAEdmAssociationView getJPAEdmAssociationView() {
    return null;
  }

  @Override
  public JPAEdmComplexTypeView getJPAEdmComplexTypeView() {
    return null;
  }

  @Override
  public JPAEdmEntityContainerView getJPAEdmEntityContainerView() {
    return null;
  }

  @Override
  public Attribute<?, ?> getJPAAttribute() {
    return null;
  }

  @Override
  public JPAEdmKeyView getJPAEdmKeyView() {
    return null;
  }

  @Override
  public List<Property> getEdmPropertyList() {
    return null;
  }

  @Override
  public SimpleProperty getEdmSimpleProperty() {
    return null;
  }

  @Override
  public JPAEdmSchemaView getEdmSchemaView() {
    return null;
  }

  @Override
  public Key getEdmKey() {
    return null;
  }

  @Override
  public List<EntityType> getConsistentEdmEntityTypes() {
    return null;
  }

  @Override
  public EntityType getEdmEntityType() {
    return null;
  }

  @Override
  public jakarta.persistence.metamodel.EntityType<?> getJPAEntityType() {
    return null;
  }

  @Override
  public List<EntitySet> getConsistentEdmEntitySetList() {
    return null;
  }

  @Override
  public EntitySet getEdmEntitySet() {
    return null;
  }

  @Override
  public JPAEdmEntityTypeView getJPAEdmEntityTypeView() {
    return null;
  }

  @Override
  public List<EntityContainer> getConsistentEdmEntityContainerList() {
    return null;
  }

  @Override
  public JPAEdmAssociationSetView getEdmAssociationSetView() {
    return null;
  }

  @Override
  public EntityContainer getEdmEntityContainer() {
    return null;
  }

  @Override
  public JPAEdmEntitySetView getJPAEdmEntitySetView() {
    return null;
  }

  @Override
  public void addJPAEdmCompleTypeView(final JPAEdmComplexTypeView arg0) {

  }

  @Override
  public List<ComplexType> getConsistentEdmComplexTypes() {
    return null;
  }

  @Override
  public ComplexType getEdmComplexType() {
    return null;
  }

  @Override
  public EmbeddableType<?> getJPAEmbeddableType() {
    return null;
  }

  @Override
  public ComplexType searchEdmComplexType(final String arg0) {
    return null;
  }

  @Override
  public ComplexType searchEdmComplexType(final FullQualifiedName arg0) {
    return null;
  }

  @Override
  public ComplexProperty getEdmComplexProperty() {
    return null;
  }

  @Override
  public void clean() {

  }

  @Override
  public JPAEdmBuilder getBuilder() {
    return null;
  }

  @Override
  public Metamodel getJPAMetaModel() {
    return null;
  }

  @Override
  public String getpUnitName() {
    return null;
  }

  @Override
  public boolean isConsistent() {
    return false;
  }

  @Override
  public void addJPAEdmRefConstraintView(final JPAEdmReferentialConstraintView arg0) {

  }

  @Override
  public ReferentialConstraint getEdmReferentialConstraint() {
    return null;
  }

  @Override
  public String getEdmRelationShipName() {
    return null;
  }

  @Override
  public boolean isExists() {
    return false;
  }

  @Override
  public EntityType searchEdmEntityType(final String arg0) {
    return null;
  }

  @Override
  public JPAEdmReferentialConstraintView getJPAEdmReferentialConstraintView() {
    return null;
  }

  @Override
  public List<Association> getConsistentEdmAssociationList() {
    return null;
  }

  @Override
  public Association searchAssociation(final JPAEdmAssociationEndView arg0) {
    return null;
  }

  @Override
  public List<AssociationSet> getConsistentEdmAssociationSetList() {
    return null;
  }

  @Override
  public Association getEdmAssociation() {
    return null;
  }

  @Override
  public AssociationSet getEdmAssociationSet() {
    return null;
  }

  @Override
  public boolean compare(final AssociationEnd arg0, final AssociationEnd arg1) {
    return false;
  }

  @Override
  public AssociationEnd getEdmAssociationEnd1() {
    return null;
  }

  @Override
  public AssociationEnd getEdmAssociationEnd2() {
    return null;
  }

  @Override
  public JPAEdmNavigationPropertyView getJPAEdmNavigationPropertyView() {
    return null;
  }

  @Override
  public void addJPAEdmNavigationPropertyView(final JPAEdmNavigationPropertyView view) {

  }

  @Override
  public List<NavigationProperty> getConsistentEdmNavigationProperties() {
    return null;
  }

  @Override
  public NavigationProperty getEdmNavigationProperty() {
    return null;
  }

  @Override
  public void expandEdmComplexType(final ComplexType complexType, final List<Property> expandedPropertyList,
      final String embeddablePropertyName) {

  }

  @Override
  public JPAEdmMappingModelAccess getJPAEdmMappingModelAccess() {
    return null;
  }

  @Override
  public void registerOperations(final Class<?> customClass, final String[] methodNames) {
    // Do nothing
  }

  @Override
  public HashMap<Class<?>, String[]> getRegisteredOperations() {
    return null;
  }

  @Override
  public JPAEdmExtension getJPAEdmExtension() {
    return null;
  }

  @Override
  public void addJPAEdmAssociationView(final JPAEdmAssociationView associationView,
      final JPAEdmAssociationEndView associationEndView) {
    // TODO Auto-generated method stub

  }

  @Override
  public int getNumberOfAssociationsWithSimilarEndPoints(final JPAEdmAssociationEndView view) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String[] getJoinColumnNames() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getJoinColumnReferenceColumnNames() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getMappedByName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getOwningPropertyName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isDefaultNamingSkipped() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isReferencedInKey(final String complexTypeName) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setReferencedInKey(final String complexTypeName) {
    // TODO Auto-generated method stub

  }

  @Override
  public Attribute<?, ?> getJPAReferencedAttribute() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String[]> getJPAJoinColumns() {
    // TODO Auto-generated method stub
    return null;
  }

}
