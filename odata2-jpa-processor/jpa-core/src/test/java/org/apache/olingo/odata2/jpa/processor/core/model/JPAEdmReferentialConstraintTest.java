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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAAttributeMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAJavaMemberMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAManagedTypeMock;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class JPAEdmReferentialConstraintTest extends JPAEdmTestModelView {

  private static JPAEdmReferentialConstraint objJPAEdmReferentialConstraint = null;
  private static JPAEdmReferentialConstraintTest objJPAEdmReferentialConstraintTest = null;
  private List<String[]> joinColumnNames = null;

  @Before
  public void setUp() {
    objJPAEdmReferentialConstraintTest = new JPAEdmReferentialConstraintTest();
    objJPAEdmReferentialConstraint =
        new JPAEdmReferentialConstraint(objJPAEdmReferentialConstraintTest, objJPAEdmReferentialConstraintTest,
            objJPAEdmReferentialConstraintTest);
    try {
      objJPAEdmReferentialConstraint.getBuilder().build();
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testGetBuilder() {
    assertNotNull(objJPAEdmReferentialConstraint.getBuilder());
  }

  @Test
  public void testGetBuilderIdempotent() {
    JPAEdmBuilder builder1 = objJPAEdmReferentialConstraint.getBuilder();
    JPAEdmBuilder builder2 = objJPAEdmReferentialConstraint.getBuilder();

    assertEquals(builder1.hashCode(), builder2.hashCode());
  }

  @Test
  public void testGetEdmReferentialConstraint() {
    assertNotNull(objJPAEdmReferentialConstraint.getEdmReferentialConstraint());
  }

  @Test
  public void testIsExistsTrue() {
    objJPAEdmReferentialConstraintTest = new JPAEdmReferentialConstraintTest();
    objJPAEdmReferentialConstraint =
        new JPAEdmReferentialConstraint(objJPAEdmReferentialConstraintTest, objJPAEdmReferentialConstraintTest,
            objJPAEdmReferentialConstraintTest);
    try {
      objJPAEdmReferentialConstraint.getBuilder().build();
      objJPAEdmReferentialConstraint.getBuilder().build();
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertTrue(objJPAEdmReferentialConstraint.isExists());
  }

  @Test
  public void testGetRelationShipName() {
    assertEquals("Assoc_SalesOrderHeader_SalesOrderItem", objJPAEdmReferentialConstraint.getEdmRelationShipName());
  }

  @Override
  public Association getEdmAssociation() {
    Association association = new Association();
    association.setName("Assoc_SalesOrderHeader_SalesOrderItem");
    association.setEnd1(new AssociationEnd().setType(new FullQualifiedName("salesorderprocessing", "String")).setRole(
        "SalesOrderHeader"));
    association.setEnd2(new AssociationEnd().setType(new FullQualifiedName("salesorderprocessing", "SalesOrderItem"))
        .setRole("SalesOrderItem"));
    return association;
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
  public List<String[]> getJPAJoinColumns() {
    if (joinColumnNames == null) {

      joinColumnNames = new ArrayList<String[]>();
      String[] names = { "SOID", "SOID" };
      joinColumnNames.add(names);
    }
    return joinColumnNames;
  }

  @SuppressWarnings("hiding")
  private class AttributeMock<Object, String> extends JPAAttributeMock<Object, String> {

    @Override
    public Member getJavaMember() {
      return new JavaMemberMock();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<String> getJavaType() {
      return (Class<String>) java.lang.String.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ManagedType<Object> getDeclaringType() {
      return (ManagedType<Object>) getManagedTypeLocal();
    }

    private ManagedType<?> getManagedTypeLocal() {
      ManagedTypeMock<String> managedTypeMock = new ManagedTypeMock<String>();
      return managedTypeMock;
    }
  }

  @SuppressWarnings("hiding")
  private class ManagedTypeMock<String> extends JPAManagedTypeMock<String> {

    @SuppressWarnings("unchecked")
    @Override
    public Class<String> getJavaType() {
      return (Class<String>) java.lang.String.class;
    }
  }

  private class JavaMemberMock extends JPAJavaMemberMock {
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
      JoinColumn joinColumn = EasyMock.createMock(JoinColumn.class);
      EasyMock.expect(joinColumn.referencedColumnName()).andReturn("SOID");
      EasyMock.expect(joinColumn.name()).andReturn("SOID");

      EasyMock.replay(joinColumn);
      return (T) joinColumn;
    }
  }

}
