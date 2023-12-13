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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.persistence.metamodel.Attribute;

import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAAttributeMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAEdmMockData.SimpleType;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAEdmMockData.SimpleType.SimpleTypeA;
import org.junit.BeforeClass;
import org.junit.Test;

public class JPAEdmAssociationEndTest extends JPAEdmTestModelView {

  private final static int VARIANT1 = 1;
  private final static int VARIANT2 = 2;
  private final static int VARIANT3 = 3;

  private static final String PUNIT_NAME = "salesorderprocessing";
  private static JPAEdmAssociationEnd objJPAEdmAssociationEnd = null;

  @BeforeClass
  public static void setup() {
    InnerMock objJPAEdmAssociationEndTest = new InnerMock(Attribute.PersistentAttributeType.MANY_TO_MANY);
    objJPAEdmAssociationEnd = new JPAEdmAssociationEnd(objJPAEdmAssociationEndTest, objJPAEdmAssociationEndTest);
    try {
      objJPAEdmAssociationEnd.getBuilder().build();
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testGetBuilder() {
    JPAEdmBuilder builder = objJPAEdmAssociationEnd.getBuilder();
    assertNotNull(builder);

  }

  @Test
  public void testGetBuilderIdempotent() {
    JPAEdmBuilder builder1 = objJPAEdmAssociationEnd.getBuilder();
    JPAEdmBuilder builder2 = objJPAEdmAssociationEnd.getBuilder();

    assertEquals(builder1.hashCode(), builder2.hashCode());
  }

  @Test
  public void testGetAssociationEnd1() {
    AssociationEnd associationEnd = objJPAEdmAssociationEnd.getEdmAssociationEnd1();
    assertEquals(associationEnd.getType().getName(), "SOID");
  }

  @Test
  public void testGetAssociationEnd2() {
    AssociationEnd associationEnd = objJPAEdmAssociationEnd.getEdmAssociationEnd2();
    assertEquals(associationEnd.getType().getName(), "String");
  }

  @Test
  public void testCompare() {
    assertTrue(objJPAEdmAssociationEnd.compare(getAssociationEnd("SOID", 1), getAssociationEnd("String", 1)));
    assertFalse(objJPAEdmAssociationEnd.compare(getAssociationEnd("String", 2), getAssociationEnd("SOID", 1)));
  }

  @Test
  public void testBuildAssociationEnd() {
    assertEquals("SOID", objJPAEdmAssociationEnd.getEdmAssociationEnd1().getType().getName());
    assertEquals(new FullQualifiedName("salesorderprocessing", "SOID"), objJPAEdmAssociationEnd.getEdmAssociationEnd1()
        .getType());
    assertTrue(objJPAEdmAssociationEnd.isConsistent());
  }

  @Test
  public void testBuildAssociationEndManyToOne() throws Exception {
    InnerMock mockFirst = new InnerMock(Attribute.PersistentAttributeType.ONE_TO_MANY);
    InnerMock mockSecond = new InnerMock(Attribute.PersistentAttributeType.MANY_TO_ONE);
    JPAEdmAssociationEnd associationEnd = new JPAEdmAssociationEnd(mockFirst, mockSecond);
    associationEnd.getBuilder().build();
    assertEquals(EdmMultiplicity.MANY, associationEnd.getEdmAssociationEnd1().getMultiplicity());
    assertEquals(EdmMultiplicity.ONE, associationEnd.getEdmAssociationEnd2().getMultiplicity());
    assertEquals("SOID", associationEnd.getEdmAssociationEnd1().getType().getName());
    assertEquals(new FullQualifiedName("salesorderprocessing", "SOID"), associationEnd.getEdmAssociationEnd1()
        .getType());
    assertTrue(associationEnd.isConsistent());
  }

  @Test
  public void testBuildAssociationEndOneToMany() throws Exception {
    InnerMock mockFirst = new InnerMock(Attribute.PersistentAttributeType.MANY_TO_ONE);
    InnerMock mockSecond = new InnerMock(Attribute.PersistentAttributeType.ONE_TO_MANY);
    JPAEdmAssociationEnd associationEnd = new JPAEdmAssociationEnd(mockFirst, mockSecond);
    associationEnd.getBuilder().build();
    assertEquals(EdmMultiplicity.ONE, associationEnd.getEdmAssociationEnd1().getMultiplicity());
    assertEquals(EdmMultiplicity.MANY, associationEnd.getEdmAssociationEnd2().getMultiplicity());
    assertEquals("SOID", associationEnd.getEdmAssociationEnd1().getType().getName());
    assertEquals(new FullQualifiedName("salesorderprocessing", "SOID"), associationEnd.getEdmAssociationEnd1()
        .getType());
    assertTrue(associationEnd.isConsistent());
  }

  @Test
  public void testBuildAssociationEndOneToOne() throws Exception {
    InnerMock mockFirst = new InnerMock(Attribute.PersistentAttributeType.ONE_TO_ONE);
    InnerMock mockSecond = new InnerMock(Attribute.PersistentAttributeType.ONE_TO_ONE);
    JPAEdmAssociationEnd associationEnd = new JPAEdmAssociationEnd(mockFirst, mockSecond);
    associationEnd.getBuilder().build();
    assertEquals(EdmMultiplicity.ONE, associationEnd.getEdmAssociationEnd1().getMultiplicity());
    assertEquals(EdmMultiplicity.ONE, associationEnd.getEdmAssociationEnd2().getMultiplicity());
    assertEquals("SOID", associationEnd.getEdmAssociationEnd1().getType().getName());
    assertEquals(new FullQualifiedName("salesorderprocessing", "SOID"), associationEnd.getEdmAssociationEnd1()
        .getType());
    assertTrue(associationEnd.isConsistent());
  }

  private AssociationEnd getAssociationEnd(final String typeName, final int variant) {
    AssociationEnd associationEnd = new AssociationEnd();
    associationEnd.setType(getFullQualifiedName(typeName));
    if (variant == VARIANT1) {
      associationEnd.setMultiplicity(EdmMultiplicity.MANY);
    } else if (variant == VARIANT2) {
      associationEnd.setMultiplicity(EdmMultiplicity.ONE);
    } else if (variant == VARIANT3) {
      associationEnd.setMultiplicity(EdmMultiplicity.ZERO_TO_ONE);
    } else {
      associationEnd.setMultiplicity(EdmMultiplicity.MANY);//
    }
    return associationEnd;
  }

  private FullQualifiedName getFullQualifiedName(final String typeName) {
    FullQualifiedName fullQualifiedName = new FullQualifiedName(PUNIT_NAME, typeName);
    return fullQualifiedName;
  }


  private static class InnerMock extends JPAEdmTestModelView {

    private final AttributeMock<Object, String> mock;

    public InnerMock(Attribute.PersistentAttributeType variant) {
      this.mock = new AttributeMock<Object, String>(variant);
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
    private Attribute<?, ?> getJPAAttributeLocal() {
      return mock;
    }

  }

    // The inner class which gives us an replica of the jpa attribute
    @SuppressWarnings("hiding")
    private static class AttributeMock<Object, String> extends JPAAttributeMock<Object, String> {

      final private PersistentAttributeType variant;

      public AttributeMock(PersistentAttributeType variant) {
        this.variant = variant;
      }

      @SuppressWarnings("unchecked")
      @Override
      public Class<String> getJavaType() {
        return (Class<String>) SimpleType.SimpleTypeA.clazz;
      }

      @Override
      public PersistentAttributeType getPersistentAttributeType() {
        return variant;
      }
    }
}
