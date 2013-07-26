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
package org.apache.olingo.odata2.core.edm.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.testutil.fit.BaseTest;

/**
 * @author SAP AG
 */
public class EdmEntityContainerImplProvTest extends BaseTest {

  private static EdmEntityContainerImplProv edmEntityContainer;

  @BeforeClass
  public static void getEdmEntityContainerImpl() throws Exception {
    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);
    when(edmProvider.getEntityContainerInfo("Container")).thenReturn(new EntityContainerInfo().setName("Container"));

    EntityContainerInfo entityContainer = new EntityContainerInfo().setName("Container1").setExtendz("Container");

    EntitySet entitySetFooFromParent = new EntitySet().setName("fooFromParent");
    when(edmProvider.getEntitySet("Container", "fooFromParent")).thenReturn(entitySetFooFromParent);

    EntitySet entitySetFoo = new EntitySet().setName("foo");
    when(edmProvider.getEntitySet("Container1", "foo")).thenReturn(entitySetFoo);

    EntitySet entitySetBar = new EntitySet().setName("foo");
    when(edmProvider.getEntitySet("Container1", "foo")).thenReturn(entitySetBar);

    AssociationSet associationSet = new AssociationSet().setName("4711");
    FullQualifiedName assocFQName = new FullQualifiedName("AssocNs", "AssocName");
    when(edmProvider.getAssociationSet("Container1", assocFQName, "foo", "fromRole")).thenReturn(associationSet);

    FunctionImport functionImportFoo = new FunctionImport().setName("foo");
    when(edmProvider.getFunctionImport("Container1", "foo")).thenReturn(functionImportFoo);

    FunctionImport functionImportBar = new FunctionImport().setName("foo");
    when(edmProvider.getFunctionImport("Container1", "foo")).thenReturn(functionImportBar);

    edmEntityContainer = new EdmEntityContainerImplProv(edmImplProv, entityContainer);
  }

  @Test
  public void testEntityContainerName() throws EdmException {
    assertEquals("Container1", edmEntityContainer.getName());
  }

  @Test
  public void testEntityContainerInheritance() throws EdmException {
    assertEquals("fooFromParent", edmEntityContainer.getEntitySet("fooFromParent").getName());

    EdmEntitySet sourceEntitySet = mock(EdmEntitySet.class);
    when(sourceEntitySet.getName()).thenReturn("foo");

    EdmAssociation edmAssociation = mock(EdmAssociation.class);
    when(edmAssociation.getNamespace()).thenReturn("AssocNs");
    when(edmAssociation.getName()).thenReturn("AssocName");

    EdmNavigationProperty edmNavigationProperty = mock(EdmNavigationProperty.class);
    when(edmNavigationProperty.getRelationship()).thenReturn(edmAssociation);
    when(edmNavigationProperty.getFromRole()).thenReturn("wrongRole");

    boolean failed = false;
    try {
      edmEntityContainer.getAssociationSet(sourceEntitySet, edmNavigationProperty);
    } catch (EdmException e) {
      failed = true;
    }

    assertTrue(failed);
  }

  @Test
  public void testEntitySetCache() throws EdmException {
    assertEquals(edmEntityContainer.getEntitySet("foo"), edmEntityContainer.getEntitySet("foo"));
    assertNotSame(edmEntityContainer.getEntitySet("foo"), edmEntityContainer.getEntitySet("bar"));
  }

  @Test
  public void testAssociationSetCache() throws EdmException {
    EdmEntitySet sourceEntitySet = mock(EdmEntitySet.class);
    when(sourceEntitySet.getName()).thenReturn("foo");

    EdmAssociation edmAssociation = mock(EdmAssociation.class);
    when(edmAssociation.getNamespace()).thenReturn("AssocNs");
    when(edmAssociation.getName()).thenReturn("AssocName");

    EdmNavigationProperty edmNavigationProperty = mock(EdmNavigationProperty.class);
    when(edmNavigationProperty.getRelationship()).thenReturn(edmAssociation);
    when(edmNavigationProperty.getFromRole()).thenReturn("fromRole");

    assertNotNull(edmEntityContainer.getAssociationSet(sourceEntitySet, edmNavigationProperty));
    assertEquals(edmEntityContainer.getAssociationSet(sourceEntitySet, edmNavigationProperty), edmEntityContainer.getAssociationSet(sourceEntitySet, edmNavigationProperty));
  }

  @Test
  public void testFunctionImportCache() throws EdmException {
    assertEquals(edmEntityContainer.getFunctionImport("foo"), edmEntityContainer.getFunctionImport("foo"));
    assertNotSame(edmEntityContainer.getFunctionImport("foo"), edmEntityContainer.getFunctionImport("bar"));
  }

  @Test
  public void getAnnotations() throws Exception {
    EdmAnnotatable annotatable = edmEntityContainer;
    EdmAnnotations annotations = annotatable.getAnnotations();
    assertNull(annotations.getAnnotationAttributes());
    assertNull(annotations.getAnnotationElements());
  }
}
