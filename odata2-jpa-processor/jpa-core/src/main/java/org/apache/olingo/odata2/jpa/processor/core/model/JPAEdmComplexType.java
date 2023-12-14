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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EmbeddableType;

import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmMappingModelAccess;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmComplexTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmNameBuilder;

public class JPAEdmComplexType extends JPAEdmBaseViewImpl implements JPAEdmComplexTypeView {

  private JPAEdmSchemaView schemaView;
  private ComplexType currentComplexType = null;
  private EmbeddableType<?> currentEmbeddableType = null;
  private HashMap<String, ComplexType> searchMap = null;
  private List<ComplexType> consistentComplextTypes = null;
  private boolean directBuild;
  private EmbeddableType<?> nestedComplexType = null;
  private List<String> nonKeyComplexList = null;

  public JPAEdmComplexType(final JPAEdmSchemaView view) {
    super(view);
    schemaView = view;
    directBuild = true;
    if (nonKeyComplexList == null) {
      nonKeyComplexList = new ArrayList<String>();
    }
  }

  public JPAEdmComplexType(final JPAEdmSchemaView view, final Attribute<?, ?> complexAttribute) {
    super(view);
    schemaView = view;
    for (EmbeddableType<?> jpaEmbeddable : schemaView.getJPAMetaModel().getEmbeddables()) {
      if (jpaEmbeddable.getJavaType().getName().equals(complexAttribute.getJavaType().getName())) {
        nestedComplexType = jpaEmbeddable;
        break;
      }
    }
    directBuild = false;
    if (nonKeyComplexList == null) {
      nonKeyComplexList = new ArrayList<String>();
    }
  }

  @Override
  public boolean isReferencedInKey(final String complexTypeName) {
    return nonKeyComplexList.contains(complexTypeName);
  }

  @Override
  public void setReferencedInKey(final String complexTypeName) {
    nonKeyComplexList.add(complexTypeName);
  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmComplexTypeBuilder();
    }

    return builder;
  }

  @Override
  public ComplexType getEdmComplexType() {
    return currentComplexType;
  }

  @Override
  public ComplexType searchEdmComplexType(final String embeddableTypeName) {
    return searchMap.get(embeddableTypeName);
  }

  @Override
  public EmbeddableType<?> getJPAEmbeddableType() {
    return currentEmbeddableType;
  }

  @Override
  public List<ComplexType> getConsistentEdmComplexTypes() {
    return consistentComplextTypes;
  }

  @Override
  public ComplexType searchEdmComplexType(final FullQualifiedName type) {
    String name = type.getName();
    return searchComplexTypeByName(name);

  }

  private ComplexType searchComplexTypeByName(final String name) {
    for (ComplexType complexType : consistentComplextTypes) {
      if (null != complexType && null != complexType.getName() && complexType.getName().equals(name)) {
        return complexType;
      }
    }

    return null;
  }

  @Override
  public void addJPAEdmCompleTypeView(final JPAEdmComplexTypeView view) {
    String searchKey = view.getJPAEmbeddableType().getJavaType().getName();

    if (!searchMap.containsKey(searchKey)) {
      consistentComplextTypes.add(view.getEdmComplexType());
      searchMap.put(searchKey, view.getEdmComplexType());
    }
  }

  @Override
  public void expandEdmComplexType(final ComplexType complexType, List<Property> expandedList,
      final String embeddablePropertyName) {

    if (expandedList == null) {
      expandedList = new ArrayList<Property>();
    }
    for (Property property : complexType.getProperties()) {
      try {
        SimpleProperty newSimpleProperty = new SimpleProperty();
        SimpleProperty oldSimpleProperty = (SimpleProperty) property;
        newSimpleProperty.setAnnotationAttributes(oldSimpleProperty.getAnnotationAttributes());
        newSimpleProperty.setAnnotationElements(oldSimpleProperty.getAnnotationElements());
        newSimpleProperty.setCustomizableFeedMappings(oldSimpleProperty.getCustomizableFeedMappings());
        newSimpleProperty.setDocumentation(oldSimpleProperty.getDocumentation());
        newSimpleProperty.setFacets(oldSimpleProperty.getFacets());
        newSimpleProperty.setMimeType(oldSimpleProperty.getMimeType());
        newSimpleProperty.setName(oldSimpleProperty.getName());
        newSimpleProperty.setType(oldSimpleProperty.getType());
        JPAEdmMappingImpl newMapping = new JPAEdmMappingImpl();
        Mapping mapping = oldSimpleProperty.getMapping();
        JPAEdmMapping oldMapping = (JPAEdmMapping) mapping;
        newMapping.setJPAColumnName(oldMapping.getJPAColumnName());
        newMapping.setInternalName(embeddablePropertyName + "." + mapping.getInternalName());
        newMapping.setObject(mapping.getObject());
        newMapping.setJPAType(oldMapping.getJPAType());
        newSimpleProperty.setMapping(newMapping);
        expandedList.add(newSimpleProperty);
      } catch (ClassCastException e) {
        ComplexProperty complexProperty = (ComplexProperty) property;
        String name = embeddablePropertyName + "." + complexProperty.getMapping().getInternalName();
        expandEdmComplexType(searchComplexTypeByName(complexProperty.getName()), expandedList, name);
      }
    }

  }

  private class JPAEdmComplexTypeBuilder implements JPAEdmBuilder {
    /*
     * 
     * Each call to build method creates a new Complex Type.
     * The Complex Type is created only if it is not created
     * earlier. A local buffer is maintained to track the list
     * of complex types created.
     * 
     * ************************************************************
     * Build EDM Complex Type - STEPS
     * ************************************************************
     * 1) Fetch list of embeddable types from JPA Model
     * 2) Search local buffer if there exists already a Complex
     * type for the embeddable type.
     * 3) If the complex type was already been built continue with
     * the next embeddable type, else create new EDM Complex Type.
     * 4) Create a Property view with Complex Type
     * 5) Get Property Builder and build the Property with Complex
     * type.
     * 6) Set EDM complex type with list of properties built by
     * the property view
     * 7) Provide name for EDM complex type.
     * 
     * ************************************************************
     * Build EDM Complex Type - STEPS
     * ************************************************************
     */
    @Override
    public void build() throws ODataJPAModelException, ODataJPARuntimeException {
      Set<EmbeddableType<?>> embeddables = new HashSet<EmbeddableType<?>>();

      if (consistentComplextTypes == null) {
        consistentComplextTypes = new ArrayList<ComplexType>();
      }

      if (searchMap == null) {
        searchMap = new HashMap<String, ComplexType>();
      }

      if (directBuild) {
        embeddables = schemaView.getJPAMetaModel().getEmbeddables();
      } else {
        embeddables.add(nestedComplexType);
      }

      for (EmbeddableType<?> embeddableType : embeddables) {

        currentEmbeddableType = embeddableType;
        String searchKey = embeddableType.getJavaType().getName();

        if (searchMap.containsKey(searchKey)) {
          continue;
        }

        // Check for need to Exclude
        if (isExcluded(JPAEdmComplexType.this)) {
          continue;
        }

        JPAEdmPropertyView propertyView = new JPAEdmProperty(schemaView, JPAEdmComplexType.this);
        propertyView.getBuilder().build();

        currentComplexType = new ComplexType();
        currentComplexType.setProperties(propertyView.getEdmPropertyList());
        JPAEdmNameBuilder.build(JPAEdmComplexType.this);

        searchMap.put(searchKey, currentComplexType);
        consistentComplextTypes.add(currentComplexType);

      }

    }

    private boolean isExcluded(final JPAEdmComplexType jpaEdmComplexType) {

      JPAEdmMappingModelAccess mappingModelAccess = jpaEdmComplexType.getJPAEdmMappingModelAccess();
      if (mappingModelAccess != null
          && mappingModelAccess.isMappingModelExists()
          && mappingModelAccess.checkExclusionOfJPAEmbeddableType(jpaEdmComplexType.getJPAEmbeddableType()
              .getJavaType().getSimpleName())) {
        return true;
      }
      return false;
    }

  }
}
