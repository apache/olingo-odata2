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

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityContainerView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntitySetView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAEntityTypeMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAMetaModelMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPASingularAttributeMock;
import org.junit.Before;
import org.junit.Test;

public class JPAEdmEntityTypeTest extends JPAEdmTestModelView {

  private static JPAEdmEntityTypeTest objJPAEdmEntityTypeTest;
  private static JPAEdmEntityType objJPAEdmEntityType;

  @Before
  public void setUp() {
    objJPAEdmEntityTypeTest = new JPAEdmEntityTypeTest();
    objJPAEdmEntityType = new JPAEdmEntityType(objJPAEdmEntityTypeTest);
    try {
      objJPAEdmEntityType.getBuilder().build();
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testGetBuilder() {
    assertNotNull(objJPAEdmEntityType.getBuilder());
  }

  @Test
  public void testGetBuilderIdempotent() {
    JPAEdmBuilder builder1 = objJPAEdmEntityType.getBuilder();
    JPAEdmBuilder builder2 = objJPAEdmEntityType.getBuilder();

    assertEquals(builder1.hashCode(), builder2.hashCode());
  }

  @Test
  public void testGetEdmEntityType() {
    assertNotNull(objJPAEdmEntityType.getEdmEntityType());
    assertNotNull(objJPAEdmEntityType.getEdmEntityType().getKey());
  }

  @Test
  public void testGetJPAEntityType() {
    assertNotNull(objJPAEdmEntityType.getJPAEntityType());

  }

  @Test
  public void testGetConsistentEdmEntityTypes() {
    assertTrue(objJPAEdmEntityType.getConsistentEdmEntityTypes().size() > 0);
  }

  @Test
  public void testSearchEdmEntityType() {
    assertNotNull(objJPAEdmEntityType.searchEdmEntityType("SalesOrderHeader"));
  }

  @Test
  public void testIsConsistent() {
    assertTrue(objJPAEdmEntityType.isConsistent());
  }

  @Override
  public Metamodel getJPAMetaModel() {
    return new JPAEdmMetaModel();
  }

  @Override
  public JPAEdmEntitySetView getJPAEdmEntitySetView() {
    return this;
  }

  @Override
  public JPAEdmEntityContainerView getJPAEdmEntityContainerView() {
    return this;
  }

  @Override
  public EntityType<?> getJPAEntityType() {
    return new JPAEdmEntityTypeLocal<String>();
  }

  @Override
  public JPAEdmEntityTypeView getJPAEdmEntityTypeView() {
    return this;
  }

  private class JPAEdmMetaModel extends JPAMetaModelMock {
    Set<EntityType<?>> entities;

    public JPAEdmMetaModel() {
      entities = new HashSet<EntityType<?>>();
    }

    @Override
    public Set<EntityType<?>> getEntities() {
      entities.add(new JPAEdmEntityType());
      return entities;
    }

    private class JPAEdmEntityType extends JPAEntityTypeMock<String> {
      @Override
      public String getName() {
        return "SalesOrderHeader";
      }

      @Override
      public Class<String> getJavaType() {
        return (Class<String>) java.lang.String.class;
      }
    }
  }

  @SuppressWarnings("hiding")
  private class JPAEdmEntityTypeLocal<String> extends JPAEntityTypeMock<String> {
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
        return true;
      }
    }
  }
}
