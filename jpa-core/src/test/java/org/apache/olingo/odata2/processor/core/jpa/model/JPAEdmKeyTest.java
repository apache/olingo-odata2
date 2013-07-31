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
package org.apache.olingo.odata2.processor.core.jpa.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.metamodel.Attribute;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.processor.api.jpa.access.JPAEdmBuilder;
import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPAModelException;
import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmKeyView;
import org.apache.olingo.odata2.processor.core.jpa.common.ODataJPATestConstants;
import org.apache.olingo.odata2.processor.core.jpa.mock.ODataJPAContextMock;
import org.apache.olingo.odata2.processor.core.jpa.mock.model.JPAAttributeMock;
import org.apache.olingo.odata2.processor.core.jpa.mock.model.JPAEdmMockData;
import org.apache.olingo.odata2.processor.core.jpa.mock.model.JPAEdmMockData.ComplexType.ComplexTypeA;
import org.junit.BeforeClass;
import org.junit.Test;

public class JPAEdmKeyTest extends JPAEdmTestModelView {

  private static JPAEdmKeyView keyView;
  private static JPAEdmKeyTest objJpaEdmKeyTest;

  @BeforeClass
  public static void setup() {
    objJpaEdmKeyTest = new JPAEdmKeyTest();
    keyView = new JPAEdmKey(objJpaEdmKeyTest, objJpaEdmKeyTest);
  }

  @SuppressWarnings("hiding")
  private class JPAAttributeA<Object, ComplexTypeA> extends JPAAttributeMock<Object, ComplexTypeA> {
    @SuppressWarnings("unchecked")
    @Override
    public Class<ComplexTypeA> getJavaType() {
      return (Class<ComplexTypeA>) JPAEdmMockData.ComplexType.ComplexTypeA.class;
    }
  }

  @Test
  public void testBuildComplexKey() {
    try {
      keyView.getBuilder().build();
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    Key key = keyView.getEdmKey();

    assertEquals(JPAEdmMockData.ComplexType.ComplexTypeA.Property.PROPERTY_A, key.getKeys().get(0).getName());
    assertEquals(JPAEdmMockData.ComplexType.ComplexTypeA.Property.PROPERTY_B, key.getKeys().get(1).getName());
    assertEquals(JPAEdmMockData.ComplexType.ComplexTypeB.Property.PROPERTY_D, key.getKeys().get(2).getName());
    assertEquals(JPAEdmMockData.ComplexType.ComplexTypeB.Property.PROPERTY_E, key.getKeys().get(3).getName());

  }

  @Test
  public void testGetBuilderIdempotent() {
    JPAEdmBuilder builder1 = keyView.getBuilder();
    JPAEdmBuilder builder2 = keyView.getBuilder();

    assertEquals(builder1.hashCode(), builder2.hashCode());
  }

  @Override
  public Attribute<?, ?> getJPAAttribute() {
    return new JPAAttributeA<Object, ComplexTypeA>();

  }

  @Override
  public ComplexType searchEdmComplexType(final FullQualifiedName arg0) {
    return searchEdmComplexType(arg0.getName());
  }

  @Override
  public ComplexType searchEdmComplexType(final String arg0) {
    if (arg0.equals(JPAEdmMockData.ComplexType.ComplexTypeA.class.getName())) {
      return buildComplexTypeA();
    } else if (arg0.equals(JPAEdmMockData.ComplexType.ComplexTypeB.class.getSimpleName())) {
      return buildComplexTypeB();
    }

    return null;

  }

  private ComplexType buildComplexTypeB() {
    ComplexType complexType = new ComplexType();
    complexType.setProperties(buildPropertiesB());

    return complexType;
  }

  private List<Property> buildPropertiesB() {
    List<Property> propertyList = new ArrayList<Property>();

    SimpleProperty property = new SimpleProperty();
    property.setName(JPAEdmMockData.ComplexType.ComplexTypeB.Property.PROPERTY_D);
    property.setType(EdmSimpleTypeKind.Int16);

    propertyList.add(property);

    property = new SimpleProperty();
    property.setName(JPAEdmMockData.ComplexType.ComplexTypeB.Property.PROPERTY_E);
    property.setType(EdmSimpleTypeKind.Int16);

    propertyList.add(property);

    return propertyList;
  }

  private ComplexType buildComplexTypeA() {
    ComplexType complexType = new ComplexType();
    complexType.setProperties(buildPropertiesA());

    return complexType;
  }

  private List<Property> buildPropertiesA() {

    List<Property> propertyList = new ArrayList<Property>();

    SimpleProperty property = new SimpleProperty();
    property.setName(JPAEdmMockData.ComplexType.ComplexTypeA.Property.PROPERTY_A);
    property.setType(EdmSimpleTypeKind.Int16);

    propertyList.add(property);

    property = new SimpleProperty();
    property.setName(JPAEdmMockData.ComplexType.ComplexTypeA.Property.PROPERTY_B);
    property.setType(EdmSimpleTypeKind.Int16);

    propertyList.add(property);

    ComplexProperty complexProperty = new ComplexProperty();
    complexProperty.setName(JPAEdmMockData.ComplexType.ComplexTypeA.Property.PROPERTY_C);
    complexProperty.setType(new FullQualifiedName(ODataJPAContextMock.NAMESPACE, JPAEdmMockData.ComplexType.ComplexTypeB.name));

    propertyList.add(complexProperty);
    return propertyList;

  }

}
