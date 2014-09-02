/*******************************************************************************
 * 
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.metamodel.Attribute;

import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationEndView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmReferentialConstraintView;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAAttributeMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAEdmMockData.SimpleType;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAEdmMockData.SimpleType.SimpleTypeA;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class JPAEdmAssociationTest extends JPAEdmTestModelView {

  private JPAEdmAssociation objAssociation = null;
  private static String ASSOCIATION_NAME = "SalesOrderHeader_String";
  private JPAEdmAssociationTest localView = null;
  private static final String PUNIT_NAME = "salesorderprocessing";
  private int variant;
  private List<String[]> joinColumnNames = null;

  @Before
  public void setup() {
    localView = new JPAEdmAssociationTest();
    objAssociation = new JPAEdmAssociation(localView, localView, localView, 1);
    try {
      objAssociation.getBuilder().build();
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Override
  public AssociationEnd getEdmAssociationEnd1() {
    AssociationEnd associationEnd = new AssociationEnd();
    associationEnd.setType(new FullQualifiedName("salesorderprocessing", "SalesOrderHeader"));
    associationEnd.setRole("SalesOrderHeader");
    associationEnd.setMultiplicity(EdmMultiplicity.ONE);
    return associationEnd;
  }

  @Override
  public AssociationEnd getEdmAssociationEnd2() {
    AssociationEnd associationEnd = new AssociationEnd();
    associationEnd.setType(new FullQualifiedName("salesorderprocessing", "String"));
    associationEnd.setRole("String");
    associationEnd.setMultiplicity(EdmMultiplicity.MANY);
    return associationEnd;
  }

  @Override
  public Association getEdmAssociation() {
    Association association = new Association();
    association
        .setEnd1(new AssociationEnd().setType(new FullQualifiedName("salesorderprocessing", "SalesOrderHeader")));
    association.setEnd2(new AssociationEnd().setType(new FullQualifiedName("salesorderprocessing", "String")));

    return association;
  }

  @Override
  public boolean isExists() {
    return true;
  }

  @Override
  public JPAEdmReferentialConstraintView getJPAEdmReferentialConstraintView() {
    JPAEdmReferentialConstraint refConstraintView = new JPAEdmReferentialConstraint(localView, localView, localView);
    return refConstraintView;
  }

  @Override
  public Attribute<?, ?> getJPAAttribute() {
    return getJPAAttributeLocal();
  }

  @Override
  public String getpUnitName() {
    return PUNIT_NAME;
  }

  @Override
  public EntityType getEdmEntityType() {
    EntityType entityType = new EntityType();
    entityType.setName(SimpleTypeA.NAME);
    return entityType;
  }

  // The inner class which gives us an replica of the jpa attribute
  @SuppressWarnings("hiding")
  private class AttributeMock<Object, String> extends JPAAttributeMock<Object, String> {

    @SuppressWarnings("unchecked")
    @Override
    public Class<String> getJavaType() {
      return (Class<String>) SimpleType.SimpleTypeA.clazz;
    }

    @Override
    public PersistentAttributeType getPersistentAttributeType() {
      if (variant == 1) {
        return PersistentAttributeType.ONE_TO_MANY;
      } else if (variant == 2) {
        return PersistentAttributeType.ONE_TO_ONE;
      } else if (variant == 2) {
        return PersistentAttributeType.MANY_TO_ONE;
      } else {
        return PersistentAttributeType.MANY_TO_MANY;
      }

    }
  }

  private Attribute<?, ?> getJPAAttributeLocal() {
    AttributeMock<Object, String> attr = new AttributeMock<Object, String>();
    return attr;
  }

  @Test
  public void testGetBuilder() {
    assertNotNull(objAssociation.getBuilder());
  }

  @Test
  public void testGetEdmAssociation() {
    assertNotNull(objAssociation.getEdmAssociation());
    assertEquals(objAssociation.getEdmAssociation().getName(), ASSOCIATION_NAME);
  }

  @Test
  public void testGetConsistentEdmAssociationList() {
    assertTrue(objAssociation.getConsistentEdmAssociationList().size() > 0);
  }

  @Override
  public String getEdmRelationShipName() {
    return "Association_SalesOrderHeader_String";
  }

  @Test
  public void testSearchAssociation1() {
    class TestAssociationEndView extends JPAEdmTestModelView {

      @Override
      public List<String[]> getJPAJoinColumns() {
        if (joinColumnNames == null) {

          joinColumnNames = new ArrayList<String[]>();
          String[] names = { "SOID", "DEMO_ID" };
          joinColumnNames.add(names);
        }
        return joinColumnNames;
      }

      @Override
      public String getEdmRelationShipName() {
        return "SalesOrderHeader_String1";
      }

      private Attribute<?, ?> getJPAAttributeLocal() {
        AttributeMock<Object, String> attr = new AttributeMock<Object, String>();
        return attr;
      }

      @Override
      public Attribute<?, ?> getJPAAttribute() {
        return getJPAAttributeLocal();
      }

      @Override
      public String[] getJoinColumnNames() {
        return new String[] { "SO_ID" };
      }

      @Override
      public String[] getJoinColumnReferenceColumnNames() {
        return new String[] { "DEMO_ID" };
      }

      @Override
      public String getMappedByName() {
        return "demo";
      }

      @Override
      public String getOwningPropertyName() {
        return "salesOrder";
      }

      @Override
      public int getNumberOfAssociationsWithSimilarEndPoints(final JPAEdmAssociationEndView view) {
        return 1;
      }

      @Override
      public String getpUnitName() {
        return "salesorderprocessing";
      }

      @Override
      public EntityType getEdmEntityType() {
        EntityType entityType = new EntityType();
        entityType.setName("SalesOrderHeader");
        return entityType;
      }

      @SuppressWarnings("hiding")
      class AttributeMock<Object, String> extends JPAAttributeMock<Object, String> {

        @SuppressWarnings("unchecked")
        @Override
        public Class<String> getJavaType() {
          return (Class<String>) SimpleType.SimpleTypeA.clazz;
        }

        @Override
        public PersistentAttributeType getPersistentAttributeType() {

          return PersistentAttributeType.ONE_TO_MANY;

        }

        @Override
        public Member getJavaMember() {
          return new AnnotatedElementMock();
        }

        @Override
        public java.lang.String getName() {
          // TODO Auto-generated method stub
          return super.getName();
        }

        class AnnotatedElementMock implements AnnotatedElement, Member {

          @Override
          public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
            return true;
          }

          @SuppressWarnings("unchecked")
          @Override
          public Annotation getAnnotation(@SuppressWarnings("rawtypes") final Class annotationClass) {
            if (annotationClass.equals(JoinColumn.class)) {
              JoinColumn joinColumn = EasyMock.createMock(JoinColumn.class);
              EasyMock.expect(joinColumn.name()).andStubReturn("SO_ID");
              EasyMock.expect(joinColumn.referencedColumnName()).andStubReturn("DEMO_ID");
              EasyMock.replay(joinColumn);
              return joinColumn;
            } else {
              OneToMany oneToMany = EasyMock.createMock(OneToMany.class);
              EasyMock.expect(oneToMany.mappedBy()).andStubReturn("demo");
              EasyMock.replay(oneToMany);
              return oneToMany;
            }
          }

          @Override
          public Annotation[] getAnnotations() {
            return null;
          }

          @Override
          public Annotation[] getDeclaredAnnotations() {
            return null;
          }

          @Override
          public Class<?> getDeclaringClass() {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public java.lang.String getName() {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public int getModifiers() {
            // TODO Auto-generated method stub
            return 0;
          }

          @Override
          public boolean isSynthetic() {
            // TODO Auto-generated method stub
            return false;
          }

        }

      }
    }
    TestAssociationEndView objJPAEdmAssociationEndTest = new TestAssociationEndView();
    JPAEdmAssociationEnd objJPAEdmAssociationEnd =
        new JPAEdmAssociationEnd(objJPAEdmAssociationEndTest, objJPAEdmAssociationEndTest);
    try {
      objJPAEdmAssociationEnd.getBuilder().build();
      Field field = objAssociation.getClass().getDeclaredField("associationEndMap");
      field.setAccessible(true);
      Map<String, JPAEdmAssociationEndView> associationEndMap = new HashMap<String, JPAEdmAssociationEndView>();
      associationEndMap.put("SalesOrderHeader_String", objJPAEdmAssociationEnd);
      field.set(objAssociation, associationEndMap);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    assertEquals("SalesOrderHeader_String", objAssociation.searchAssociation(objJPAEdmAssociationEnd).getName());

  }

  @Test
  public void testAddJPAEdmAssociationView() {

    class LocalJPAAssociationView extends JPAEdmTestModelView {
      @Override
      public AssociationEnd getEdmAssociationEnd1() {
        AssociationEnd associationEnd = new AssociationEnd();
        associationEnd.setType(new FullQualifiedName("salesorderprocessing", "SalesOrderHeader"));
        associationEnd.setRole("SalesOrderHeader");
        associationEnd.setMultiplicity(EdmMultiplicity.ONE);
        return associationEnd;
      }

      @Override
      public AssociationEnd getEdmAssociationEnd2() {
        AssociationEnd associationEnd = new AssociationEnd();
        associationEnd.setType(new FullQualifiedName("salesorderprocessing", "SalesOrderItem"));
        associationEnd.setRole("SalesOrderItem");
        associationEnd.setMultiplicity(EdmMultiplicity.MANY);
        return associationEnd;
      }

      @Override
      public Association getEdmAssociation() {
        Association association = new Association();
        association.setEnd1(new AssociationEnd().setType(new FullQualifiedName("salesorderprocessing",
            "SalesOrderHeader")));
        association.setEnd2(new AssociationEnd()
            .setType(new FullQualifiedName("salesorderprocessing", "SalesOrderItem")));

        return association;
      }
    }
    LocalJPAAssociationView assocViewObj = new LocalJPAAssociationView();
    JPAEdmAssociation objLocalAssociation = new JPAEdmAssociation(assocViewObj, assocViewObj, assocViewObj, 1);
    try {
      objLocalAssociation.getBuilder().build();
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    objAssociation.addJPAEdmAssociationView(objLocalAssociation, localView);

  }

  @Test
  public void testAddJPAEdmRefConstraintView() {

    localView = new JPAEdmAssociationTest();
    objAssociation = new JPAEdmAssociation(localView, localView, localView, 1);
    try {
      objAssociation.getBuilder().build();
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    objAssociation.addJPAEdmRefConstraintView(localView);
    assertTrue(objAssociation.getConsistentEdmAssociationList().size() > 0);
  }

  @Test
  public void testGetJPAEdmReferentialConstraintView() {

  }
}
