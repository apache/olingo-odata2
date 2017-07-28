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
package org.apache.olingo.odata2.core.edm.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationSet;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.junit.Before;
import org.junit.Test;

/**
 *  
 */
public class EdmEntityContainerImplProvTest extends BaseTest {

  private EdmEntityContainer edmEntityContainer;

  @Before
  public void getEdmEntityContainerImpl() throws Exception {
    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);
    String containerParentName = "ContainerParent";
    String containerName = "Container";

    List<Schema> schemas = new ArrayList<Schema>();
    Schema mockedSchema = mock(Schema.class);
    List<EntityContainer> entityContainers = new ArrayList<EntityContainer>();
    List<EntitySet> entitySetsParent = new ArrayList<EntitySet>();
    EntityContainer parentEntityContainer = new EntityContainer()
        .setName(containerParentName)
        .setEntitySets(entitySetsParent);
    EntityContainer entityContainer = mock(EntityContainer.class);
    when(entityContainer.getName()).thenReturn(containerName);
    when(entityContainer.getExtendz()).thenReturn(containerParentName);
    entityContainers.add(entityContainer);
    entityContainers.add(parentEntityContainer);
    when(mockedSchema.getEntityContainers()).thenReturn(entityContainers);
    schemas.add(mockedSchema);
    when(edmProvider.getSchemas()).thenReturn(schemas);

    List<AssociationSet> associationSets = new ArrayList<AssociationSet>();
    when(entityContainer.getAssociationSets()).thenReturn(associationSets);
    List<EntitySet> entitySets = new ArrayList<EntitySet>();
    when(entityContainer.getEntitySets()).thenReturn(entitySets);

    when(edmProvider.getEntityContainerInfo(containerParentName))
        .thenReturn(new EntityContainerInfo().setName(containerParentName));
    EntityContainerInfo entityContainerInfo =
        new EntityContainerInfo().setName(containerName).setExtendz(containerParentName);

    EntitySet entitySetFooFromParent = new EntitySet().setName("fooFromParent");
    entitySetsParent.add(entitySetFooFromParent);
    when(edmProvider.getEntitySet(containerParentName, "fooFromParent")).thenReturn(entitySetFooFromParent);

    EntitySet entitySetFoo = new EntitySet().setName("foo");
    entitySets.add(entitySetFoo);
    when(edmProvider.getEntitySet(containerName, "foo")).thenReturn(entitySetFoo);

    EntitySet entitySetBar = new EntitySet().setName("bar");
    entitySets.add(entitySetBar);
    when(edmProvider.getEntitySet(containerName, "bar")).thenReturn(entitySetBar);

    AssociationSet associationSet = new AssociationSet().setName("Name4711");
    FullQualifiedName assocFQName = new FullQualifiedName("AssocNs", "AssocName");
    associationSets.add(associationSet);
    when(edmProvider.getAssociationSet(containerName, assocFQName, "foo", "fromRole")).thenReturn(associationSet);

    AssociationSet parentAssociationSet = new AssociationSet().setName("Name42");
    FullQualifiedName parentAssocFQName = new FullQualifiedName("AssocNs", "AssocNameParent");
    when(edmProvider.getAssociationSet(containerParentName,
        parentAssocFQName, "fooFromParent", "fromParentRole")).thenReturn(parentAssociationSet);
    parentEntityContainer.setAssociationSets(Arrays.asList(parentAssociationSet));

    FunctionImport functionImportFoo = new FunctionImport().setName("foo");
    when(edmProvider.getFunctionImport(containerName, "foo")).thenReturn(functionImportFoo);

    FunctionImport functionImportBar = new FunctionImport().setName("foo");
    when(edmProvider.getFunctionImport(containerName, "foo")).thenReturn(functionImportBar);

    edmEntityContainer = new EdmEntityContainerImplProv(edmImplProv, entityContainerInfo);
  }

  @Test
  public void testEntityContainerName() throws EdmException {
    assertEquals("Container", edmEntityContainer.getName());
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
    assertEquals(edmEntityContainer.getAssociationSet(sourceEntitySet, edmNavigationProperty), edmEntityContainer
        .getAssociationSet(sourceEntitySet, edmNavigationProperty));
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

  @Test
  public void testGetEntitySets() throws EdmException {

    List<EdmEntitySet> entitySets = edmEntityContainer.getEntitySets();
    assertNotNull(entitySets);
    assertEquals(3, entitySets.size());

    for (EdmEntitySet entitySet : entitySets) {
      String name = entitySet.getName();
      boolean expectedName = "fooFromParent".equals(name)
          || "foo".equals(name)
          || "bar".equals(name);
      assertTrue("Found not expected name: " + name, expectedName);
    }
  }

  @Test
  public void testGetAssociationSets() throws EdmException {
    List<EdmAssociationSet> associationSets = edmEntityContainer.getAssociationSets();
    assertNotNull(associationSets);
    assertEquals(2, associationSets.size());

    for (EdmAssociationSet assoSet : associationSets) {
      String name = assoSet.getName();
      boolean expectedName = "Name4711".equals(name)
          || "Name42".equals(name);
      assertTrue("Found not expected name: " + name, expectedName);
    }
  }
}
