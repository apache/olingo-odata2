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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.Metamodel;

import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAEmbeddableMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAMetaModelMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPASingularAttributeMock;
import org.junit.BeforeClass;
import org.junit.Test;

public class JPAEdmComplexTypeTest extends JPAEdmTestModelView {

  private static JPAEdmComplexType objComplexType = null;
  private static JPAEdmComplexTypeTest localView = null;

  @BeforeClass
  public static void setup() {
    localView = new JPAEdmComplexTypeTest();
    objComplexType = new JPAEdmComplexType(localView);
    try {
      objComplexType.getBuilder().build();
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public EmbeddableType<?> getJPAEmbeddableType() {
    @SuppressWarnings("hiding")
    class JPAComplexAttribute<Long> extends JPAEmbeddableMock<Long> {

      @SuppressWarnings("unchecked")
      @Override
      public Class<Long> getJavaType() {

        return (Class<Long>) java.lang.Long.class;
      }

    }
    return new JPAComplexAttribute();
  }

  @Override
  public String getpUnitName() {
    return "salesorderprocessing";
  }

  @Override
  public Metamodel getJPAMetaModel() {
    return new JPAEdmMetaModel();
  }

  @Test
  public void testGetBuilder() {

    assertNotNull(objComplexType.getBuilder());
  }

  @Test
  public void testGetEdmComplexType() {
    assertEquals(objComplexType.getEdmComplexType().getName(), "String");
  }

  @Test
  public void testSearchComplexTypeString() {
    assertNotNull(objComplexType.searchEdmComplexType("java.lang.String"));

  }

  @Test
  public void testGetJPAEmbeddableType() {
    assertTrue(objComplexType.getJPAEmbeddableType().getAttributes().size() > 0);

  }

  @Test
  public void testGetConsistentEdmComplexTypes() {
    assertTrue(objComplexType.getConsistentEdmComplexTypes().size() > 0);
  }

  @Test
  public void testSearchComplexTypeFullQualifiedName() {
    assertNotNull(objComplexType.searchEdmComplexType(new FullQualifiedName("salesorderprocessing", "String")));

  }

  @Test
  public void testSearchComplexTypeFullQualifiedNameNegative() {
    assertNull(objComplexType.searchEdmComplexType(new FullQualifiedName("salesorderprocessing", "lang.String")));
  }

  @Test
  public void testGetBuilderIdempotent() {
    JPAEdmBuilder builder1 = objComplexType.getBuilder();
    JPAEdmBuilder builder2 = objComplexType.getBuilder();

    assertEquals(builder1.hashCode(), builder2.hashCode());
  }

  @Test
  public void testAddCompleTypeView() {
    localView = new JPAEdmComplexTypeTest();
    objComplexType = new JPAEdmComplexType(localView);
    try {
      objComplexType.getBuilder().build();
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    objComplexType.addJPAEdmCompleTypeView(localView);
    assertTrue(objComplexType.getConsistentEdmComplexTypes().size() > 1);
  }

  @Test
  public void testExpandEdmComplexType() {
    ComplexType complexType = new ComplexType();
    List<Property> properties = new ArrayList<Property>();
    JPAEdmMapping mapping1 = new JPAEdmMappingImpl();
    mapping1.setJPAColumnName("LINEITEMID");
    ((Mapping) mapping1).setInternalName("LineItemKey.LiId");
    JPAEdmMapping mapping2 = new JPAEdmMappingImpl();
    mapping2.setJPAColumnName("LINEITEMNAME");
    ((Mapping) mapping2).setInternalName("LineItemKey.LiName");
    properties.add(new SimpleProperty().setName("LIID").setMapping((Mapping) mapping1));
    properties.add(new SimpleProperty().setName("LINAME").setMapping((Mapping) mapping2));
    complexType.setProperties(properties);
    List<Property> expandedList = null;
    try {
      objComplexType.expandEdmComplexType(complexType, expandedList, "SalesOrderItemKey");
    } catch (ClassCastException e) {
      assertTrue(false);
    }
    assertTrue(true);

  }

  @Test
  public void testComplexTypeCreation() {
    try {
      objComplexType.getBuilder().build();
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertEquals(objComplexType.pUnitName, "salesorderprocessing");
  }

  private class JPAEdmMetaModel extends JPAMetaModelMock {
    Set<EmbeddableType<?>> embeddableSet;

    public JPAEdmMetaModel() {
      embeddableSet = new HashSet<EmbeddableType<?>>();
    }

    @Override
    public Set<EmbeddableType<?>> getEmbeddables() {
      embeddableSet.add(new JPAEdmEmbeddable<String>());
      return embeddableSet;
    }

  }

  @SuppressWarnings("hiding")
  private class JPAEdmEmbeddable<String> extends JPAEmbeddableMock<String> {

    Set<Attribute<? super String, ?>> attributeSet = new HashSet<Attribute<? super String, ?>>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setValuesToSet() {
      attributeSet.add((Attribute<? super String, String>) new JPAEdmAttribute(java.lang.String.class, "SOID"));
      attributeSet.add((Attribute<? super String, String>) new JPAEdmAttribute(java.lang.String.class, "SONAME"));
    }

    @Override
    public Set<Attribute<? super String, ?>> getAttributes() {
      setValuesToSet();
      return attributeSet;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<String> getJavaType() {
      return (Class<String>) java.lang.String.class;
    }

  }

  @SuppressWarnings("hiding")
  private class JPAEdmAttribute<Object, String> extends JPASingularAttributeMock<Object, String> {

    @Override
    public PersistentAttributeType getPersistentAttributeType() {
      return PersistentAttributeType.BASIC;
    }

    Class<String> clazz;
    java.lang.String attributeName;

    public JPAEdmAttribute(final Class<String> javaType, final java.lang.String name) {
      this.clazz = javaType;
      this.attributeName = name;

    }

    @Override
    public Class<String> getJavaType() {
      return clazz;
    }

    @Override
    public java.lang.String getName() {
      return this.attributeName;
    }

    @Override
    public boolean isId() {
      return false;
    }

  }
}
