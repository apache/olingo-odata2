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
package org.apache.olingo.odata2.jpa.processor.core.access.model;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;

import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmMappingModelAccess;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationEndView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationSetView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmBaseView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmComplexPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityContainerView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntitySetView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmNavigationPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmComplexType;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;

public class JPAEdmNameBuilder {
  private static final String ENTITY_CONTAINER_SUFFIX = "Container";
  private static final String ENTITY_SET_SUFFIX = "s";
  private static final String ASSOCIATIONSET_SUFFIX = "Set";
  private static final String NAVIGATION_NAME = "Details";
  private static final String UNDERSCORE = "_";
  private static final String FK_PREFIX = "FK";

  public static FullQualifiedName build(final JPAEdmBaseView view, final String name) {
    FullQualifiedName fqName = new FullQualifiedName(buildNamespace(view), name);
    return fqName;
  }

  /*
   * ************************************************************************
   * EDM EntityType Name - RULES
   * ************************************************************************
   * EDM Entity Type Name = JPA Entity Name EDM Entity Type Internal Name =
   * JPA Entity Name
   * ************************************************************************
   * EDM Entity Type Name - RULES
   * ************************************************************************
   */
  public static void build(final JPAEdmEntityTypeView view) {

    EntityType edmEntityType = view.getEdmEntityType();
    String jpaEntityName = view.getJPAEntityType().getName();
    JPAEdmMappingModelAccess mappingModelAccess = view.getJPAEdmMappingModelAccess();
    String edmEntityTypeName = null;
    if (mappingModelAccess != null && mappingModelAccess.isMappingModelExists()) {
      edmEntityTypeName = mappingModelAccess.mapJPAEntityType(jpaEntityName);
    }

    JPAEdmMapping mapping = new JPAEdmMappingImpl();
    mapping.setJPAType(view.getJPAEntityType().getJavaType());

    if (edmEntityTypeName == null) {
      edmEntityTypeName = jpaEntityName;
    }
    // Setting the mapping object
    edmEntityType.setMapping(((Mapping) mapping).setInternalName(jpaEntityName));

    edmEntityType.setName(edmEntityTypeName);

  }

  /*
   * ************************************************************************
   * EDM Schema Name - RULES
   * ************************************************************************
   * Java Persistence Unit name is set as Schema's Namespace
   * ************************************************************************
   * EDM Schema Name - RULES
   * ************************************************************************
   */
  public static void build(final JPAEdmSchemaView view) throws ODataJPAModelException {
    view.getEdmSchema().setNamespace(buildNamespace(view));
  }

  /*
   * ************************************************************************
   * EDM Property Name - RULES
   * ************************************************************************
   * OData Property Names are represented in Camel Case. The first character
   * of JPA Attribute Name is converted to an UpperCase Character and set as
   * OData Property Name. JPA Attribute Name is set as Internal Name for OData
   * Property. The Column name (annotated as @Column(name="x")) is set as
   * column name in the mapping object.
   * ************************************************************************
   * EDM Property Name - RULES
   * ************************************************************************
   */
  public static void build(final JPAEdmPropertyView view, final boolean isComplexMode,
      final boolean skipDefaultNaming, final boolean isForeignKey) {
    Attribute<?, ?> jpaAttribute = view.getJPAAttribute();
    String jpaAttributeName = jpaAttribute.getName();
    String propertyName = null;
    String[] joinColumnNames = null;

    JPAEdmMappingModelAccess mappingModelAccess = view.getJPAEdmMappingModelAccess();
    if (mappingModelAccess != null && mappingModelAccess.isMappingModelExists()) {
      if (isComplexMode) {
        propertyName =
            mappingModelAccess.mapJPAEmbeddableTypeAttribute(view.getJPAEdmComplexTypeView().getJPAEmbeddableType()
                .getJavaType().getSimpleName(), jpaAttributeName);
      } else {
        propertyName =
            mappingModelAccess.mapJPAAttribute(view.getJPAEdmEntityTypeView().getJPAEntityType().getName(),
                jpaAttributeName);
      }
    }
    if (isForeignKey == true) {
      joinColumnNames = view.getJPAJoinColumns().get(view.getJPAJoinColumns().size() - 1);
    }

    if (skipDefaultNaming == false && propertyName == null) {
      propertyName = Character.toUpperCase(jpaAttributeName.charAt(0)) + jpaAttributeName.substring(1);
    } else if (propertyName == null) {
      propertyName = jpaAttributeName;
      if (isForeignKey) {
        if (mappingModelAccess != null) {
          propertyName =
              mappingModelAccess.mapJPAAttribute(view.getJPAEdmEntityTypeView().getJPAEntityType().getName(),
                  joinColumnNames[0]);
        }
        if (propertyName == null) {
          propertyName = FK_PREFIX + UNDERSCORE + joinColumnNames[0];
        }
      }
    }

    view.getEdmSimpleProperty().setName(propertyName);

    JPAEdmMapping mapping = new JPAEdmMappingImpl();
    mapping.setJPAType(jpaAttribute.getJavaType());

    AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
    if (annotatedElement != null) {
      Column column = annotatedElement.getAnnotation(Column.class);
      if (column != null) {
        mapping.setJPAColumnName(column.name());
      } else if (joinColumnNames != null) {
        mapping.setJPAColumnName(joinColumnNames[0]);
      } else {
        mapping.setJPAColumnName(jpaAttributeName);
      }
      if (isForeignKey) {
        jpaAttributeName += "." + view.getJPAReferencedAttribute().getName();
      }
    } else {
      ManagedType<?> managedType = jpaAttribute.getDeclaringType();
      if (managedType != null) {
        Class<?> clazz = managedType.getJavaType();
        try {
          Field field = clazz.getField(jpaAttributeName);
          Column column = field.getAnnotation(Column.class);
          if (column != null) {
            mapping.setJPAColumnName(column.name());
          }
        } catch (SecurityException e) {

        } catch (NoSuchFieldException e) {

        }
      }

    }
    ((Mapping) mapping).setInternalName(jpaAttributeName);
    view.getEdmSimpleProperty().setMapping((Mapping) mapping);
  }

  /*
   * ************************************************************************
   * EDM EntityContainer Name - RULES
   * ************************************************************************
   * Entity Container Name = EDM Namespace + Literal "Container"
   * ************************************************************************
   * EDM EntityContainer Name - RULES
   * ************************************************************************
   */
  public static void build(final JPAEdmEntityContainerView view) {
    view.getEdmEntityContainer().setName(buildEntityContainerName(view));
  }

  /*
   * ************************************************************************
   * EDM EntitySet Name - RULES
   * ************************************************************************
   * Entity Set Name = JPA Entity Type Name + Literal "s"
   * ************************************************************************
   * EDM EntitySet Name - RULES
   * ************************************************************************
   */
  public static void build(final JPAEdmEntitySetView view, final JPAEdmEntityTypeView entityTypeView) {
    FullQualifiedName fQname = view.getEdmEntitySet().getEntityType();
    JPAEdmMappingModelAccess mappingModelAccess = view.getJPAEdmMappingModelAccess();
    String entitySetName = null;
    if (mappingModelAccess != null && mappingModelAccess.isMappingModelExists()) {
      Mapping mapping = entityTypeView.getEdmEntityType().getMapping();
      if (mapping != null) {
        entitySetName = mappingModelAccess.mapJPAEntitySet(mapping.getInternalName());
      }
    }

    if (entitySetName == null) {
      entitySetName = fQname.getName() + ENTITY_SET_SUFFIX;
    }

    view.getEdmEntitySet().setName(entitySetName);
  }

  /*
   * ************************************************************************
   * EDM Complex Type Name - RULES
   * ************************************************************************
   * Complex Type Name = JPA Embeddable Type Simple Name.
   * ************************************************************************
   * EDM Complex Type Name - RULES
   * ************************************************************************
   */
  public static void build(final JPAEdmComplexType view) {

    JPAEdmMappingModelAccess mappingModelAccess = view.getJPAEdmMappingModelAccess();
    String jpaEmbeddableTypeName = view.getJPAEmbeddableType().getJavaType().getSimpleName();
    String edmComplexTypeName = null;
    if (mappingModelAccess != null && mappingModelAccess.isMappingModelExists()) {
      edmComplexTypeName = mappingModelAccess.mapJPAEmbeddableType(jpaEmbeddableTypeName);
    }

    if (edmComplexTypeName == null) {
      edmComplexTypeName = jpaEmbeddableTypeName;
    }

    view.getEdmComplexType().setName(edmComplexTypeName);
    ComplexType complexType = view.getEdmComplexType();
    complexType.setName(edmComplexTypeName);
    JPAEdmMapping mapping = new JPAEdmMappingImpl();
    mapping.setJPAType(view.getJPAEmbeddableType().getJavaType());
    complexType.setMapping((Mapping) mapping);

  }

  /*
   * ************************************************************************
   * EDM Complex Property Name - RULES
   * ************************************************************************
   * The first character of JPA complex attribute name is converted to
   * uppercase. The modified JPA complex attribute name is assigned as EDM
   * complex property name. The unmodified JPA complex attribute name is
   * assigned as internal name.
   * ************************************************************************
   * EDM Complex Property Name - RULES
   * ************************************************************************
   */
  public static void build(final JPAEdmComplexPropertyView complexView,
      final JPAEdmPropertyView propertyView, final boolean skipDefaultNaming) {

    ComplexProperty complexProperty = complexView.getEdmComplexProperty();

    String jpaAttributeName = propertyView.getJPAAttribute().getName();
    String jpaEntityTypeName = propertyView.getJPAEdmEntityTypeView().getJPAEntityType().getName();

    JPAEdmMappingModelAccess mappingModelAccess = complexView.getJPAEdmMappingModelAccess();
    String propertyName = null;

    if (mappingModelAccess != null && mappingModelAccess.isMappingModelExists()) {
      propertyName = mappingModelAccess.mapJPAAttribute(jpaEntityTypeName, jpaAttributeName);
    }

    if (skipDefaultNaming == false && propertyName == null) {
      propertyName = Character.toUpperCase(jpaAttributeName.charAt(0)) + jpaAttributeName.substring(1);
    } else if (propertyName == null) {
      propertyName = jpaAttributeName;
    }

    // change for navigation property issue
    JPAEdmMapping mapping = new JPAEdmMappingImpl();
    ((Mapping) mapping).setInternalName(jpaAttributeName);
    mapping.setJPAType(propertyView.getJPAAttribute().getJavaType());
    complexProperty.setMapping((Mapping) mapping);

    complexProperty.setName(propertyName);

  }

  public static void build(final JPAEdmComplexPropertyView complexView,
      final String parentComplexTypeName, final boolean skipDefaultNaming) {
    ComplexProperty complexProperty = complexView.getEdmComplexProperty();

    JPAEdmMappingModelAccess mappingModelAccess = complexView.getJPAEdmMappingModelAccess();
    JPAEdmPropertyView propertyView = ((JPAEdmPropertyView) complexView);
    String jpaAttributeName = propertyView.getJPAAttribute().getName();
    String propertyName = null;
    if (mappingModelAccess != null && mappingModelAccess.isMappingModelExists()) {
      propertyName = mappingModelAccess.mapJPAEmbeddableTypeAttribute(parentComplexTypeName, jpaAttributeName);
    }
    if (skipDefaultNaming == false && propertyName == null) {
      propertyName = Character.toUpperCase(jpaAttributeName.charAt(0)) + jpaAttributeName.substring(1);
    } else if (propertyName == null) {
      propertyName = jpaAttributeName;
    }

    JPAEdmMapping mapping = new JPAEdmMappingImpl();
    ((Mapping) mapping).setInternalName(jpaAttributeName);
    mapping.setJPAType(propertyView.getJPAAttribute().getJavaType());
    complexProperty.setMapping((Mapping) mapping);
    complexProperty.setName(propertyName);

  }

  /*
   * ************************************************************************
   * EDM Association End Name - RULES
   * ************************************************************************
   * Association End name = Namespace + Entity Type Name
   * ************************************************************************
   * EDM Association End Name - RULES
   * ************************************************************************
   */
  public static void build(final JPAEdmAssociationEndView assocaitionEndView,
      final JPAEdmEntityTypeView entityTypeView, final JPAEdmPropertyView propertyView) {

    String namespace = buildNamespace(assocaitionEndView);

    String name = entityTypeView.getEdmEntityType().getName();
    FullQualifiedName fQName = new FullQualifiedName(namespace, name);
    assocaitionEndView.getEdmAssociationEnd1().setType(fQName);

    name = null;
    String jpaEntityTypeName = null;
    Attribute<?, ?> jpaAttribute = propertyView.getJPAAttribute();
    if (jpaAttribute.isCollection()) {
      jpaEntityTypeName = ((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType().getJavaType()
          .getSimpleName();
    } else {
      jpaEntityTypeName = propertyView.getJPAAttribute().getJavaType()
          .getSimpleName();
    }

    JPAEdmMappingModelAccess mappingModelAccess = assocaitionEndView.getJPAEdmMappingModelAccess();

    if (mappingModelAccess != null && mappingModelAccess.isMappingModelExists()) {
      name = mappingModelAccess.mapJPAEntityType(jpaEntityTypeName);
    }

    if (name == null) {
      name = jpaEntityTypeName;
    }

    fQName = new FullQualifiedName(namespace, name);
    assocaitionEndView.getEdmAssociationEnd2().setType(fQName);

  }

  /*
   * ************************************************************************
   * EDM Association Name - RULES
   * ************************************************************************
   * Association name = Association + End1 Name + End2 Name
   * ************************************************************************
   * EDM Association Name - RULES
   * ************************************************************************
   */

  public static void build(final JPAEdmAssociationView view, final int count) {
    Association association = view.getEdmAssociation();
    String associationName = null;
    String end1Name = association.getEnd1().getType().getName();
    String end2Name = association.getEnd2().getType().getName();

    associationName = end1Name + UNDERSCORE + end2Name;
    associationName =
        associationName + UNDERSCORE + multiplicityToString(association.getEnd1().getMultiplicity()) + UNDERSCORE
            + multiplicityToString(association.getEnd2().getMultiplicity()) + Integer.toString(count);
    association.setName(associationName);
  }

  /*
   * ************************************************************************
   * EDM Association Set Name - RULES
   * ************************************************************************
   * Association Set name = Association Name + "Set"
   * ************************************************************************
   * EDM Association Set Name - RULES
   * ************************************************************************
   */
  public static void build(final JPAEdmAssociationSetView view) {
    AssociationSet associationSet = view.getEdmAssociationSet();

    String name = view.getEdmAssociation().getName();
    associationSet.setName(name + ASSOCIATIONSET_SUFFIX);

  }

  public static void build(final JPAEdmAssociationView associationView,
      final JPAEdmPropertyView propertyView,
      final JPAEdmNavigationPropertyView navPropertyView, final boolean skipDefaultNaming, final int count) {

    boolean overrideSkipDefaultNaming = false;

    String toName = null;
    String fromName = null;
    String navPropName = null;
    NavigationProperty navProp = navPropertyView.getEdmNavigationProperty();
    String namespace = buildNamespace(associationView);

    Association association = associationView.getEdmAssociation();
    navProp.setRelationship(new FullQualifiedName(namespace, association
        .getName()));

    FullQualifiedName associationEndTypeOne = association.getEnd1()
        .getType();
    FullQualifiedName associationEndTypeTwo = association.getEnd2()
        .getType();

    Attribute<?, ?> jpaAttribute = propertyView.getJPAAttribute();
    JPAEdmMapping mapping = new JPAEdmMappingImpl();
    ((Mapping) mapping).setInternalName(jpaAttribute.getName());
    mapping.setJPAType(jpaAttribute.getJavaType());
    navProp.setMapping((Mapping) mapping);

    String jpaEntityTypeName = propertyView.getJPAEdmEntityTypeView()
        .getJPAEntityType().getName();
    JPAEdmMappingModelAccess mappingModelAccess = navPropertyView
        .getJPAEdmMappingModelAccess();

    String targetEntityTypeName = null;
    if (jpaAttribute.isCollection()) {
      targetEntityTypeName = ((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType().getJavaType().getSimpleName();
    } else {
      targetEntityTypeName = jpaAttribute.getJavaType().getSimpleName();
    }

    if (mappingModelAccess != null
        && mappingModelAccess.isMappingModelExists()) {
      navPropName = mappingModelAccess.mapJPARelationship(
          jpaEntityTypeName, jpaAttribute.getName());
      toName = mappingModelAccess.mapJPAEntityType(targetEntityTypeName);
      fromName = mappingModelAccess
          .mapJPAEntityType(jpaEntityTypeName);
      if (navPropName != null) {
        overrideSkipDefaultNaming = true;
      }
    }
    if (toName == null) {
      toName = targetEntityTypeName;
    }

    if (fromName == null) {
      fromName = jpaEntityTypeName;
    }
    /*
     * Navigation Property name was provided in mapping then don't try to default the name
     */
    if (overrideSkipDefaultNaming == false && skipDefaultNaming == false) {
      if (navPropName == null) {
        navPropName = toName.concat(NAVIGATION_NAME);
      }
      if (count > 1) {
        navPropName = navPropName + Integer.toString(count - 1);
      }
    } else if (navPropName == null) {
      navPropName = jpaAttribute.getName();
    }

    navProp.setName(navPropName);

    // Condition for self join
    if (associationEndTypeOne.getName().equals(associationEndTypeTwo.getName())) {
      if (jpaAttribute.isCollection()) {
        if (association.getEnd2().getMultiplicity().equals(EdmMultiplicity.MANY)) {
          navProp.setToRole(association.getEnd2().getRole());
          navProp.setFromRole(association.getEnd1().getRole());
        } else {
          navProp.setToRole(association.getEnd1().getRole());
          navProp.setFromRole(association.getEnd2().getRole());
        }
      } else {
        if (association.getEnd2().getMultiplicity().equals(EdmMultiplicity.ONE)
            || association.getEnd2().getMultiplicity().equals(EdmMultiplicity.ZERO_TO_ONE)) {
          navProp.setToRole(association.getEnd2().getRole());
          navProp.setFromRole(association.getEnd1().getRole());
        } else {
          navProp.setToRole(association.getEnd1().getRole());
          navProp.setFromRole(association.getEnd2().getRole());
        }
      }
    } else if (toName.equals(associationEndTypeOne.getName())) {
      navProp.setFromRole(association.getEnd2().getRole());
      navProp.setToRole(association.getEnd1().getRole());
    } else if (toName.equals(associationEndTypeTwo.getName())) {

      navProp.setToRole(association.getEnd2().getRole());
      navProp.setFromRole(association.getEnd1().getRole());
    }
  }

  private static String multiplicityToString(EdmMultiplicity multiplicity) {
    if(multiplicity == null) {
      return "";
    }

    switch (multiplicity) {
    case MANY:
      return "Many";
    case ONE:
      return "One";
    case ZERO_TO_ONE:
      return "ZeroToOne";
    default:
      return "";
    }
  }

  private static String buildNamespace(final JPAEdmBaseView view) {
    JPAEdmMappingModelAccess mappingModelAccess = view.getJPAEdmMappingModelAccess();
    String namespace = null;
    if (mappingModelAccess != null && mappingModelAccess.isMappingModelExists()) {
      namespace = mappingModelAccess.mapJPAPersistenceUnit(view.getpUnitName());
    }
    if (namespace == null) {
      namespace = view.getpUnitName();
    }

    return namespace;
  }

  private static String buildEntityContainerName(final JPAEdmBaseView view) {
    JPAEdmMappingModelAccess mappingModelAccess = view.getJPAEdmMappingModelAccess();
    String name = null;
    if (mappingModelAccess != null && mappingModelAccess.isMappingModelExists()) {
      name = mappingModelAccess.mapJPAPersistenceUnit(view.getpUnitName());
    }
    if (name == null) {
      return normalizeName(view.getpUnitName()) + ENTITY_CONTAINER_SUFFIX;
    }
    return name;
  }

  /**
   * Replace `.` with `_` as
   * @param name
   * @return
   */
  private static String normalizeName(String name) {
    return name.replace('.', '_');
  }

}
