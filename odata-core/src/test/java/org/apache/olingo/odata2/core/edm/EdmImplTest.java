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
package org.apache.olingo.odata2.core.edm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.junit.Before;
import org.junit.Test;

/**
 *  
 */
public class EdmImplTest extends BaseTest {

  private ForEdmImplTest edm;

  @Before
  public void getEdmImpl() throws EdmException {
    edm = new ForEdmImplTest();
  }

  @Test
  public void testEntityContainerCache() throws EdmException {
    assertEquals(edm.getEntityContainer("foo"), edm.getEntityContainer("foo"));
    assertNotSame(edm.getEntityContainer("foo"), edm.getEntityContainer("bar"));
    assertEquals(edm.getDefaultEntityContainer(), edm.getEntityContainer(null));
    assertNotSame(edm.getDefaultEntityContainer(), edm.getEntityContainer(""));
  }

  @Test
  public void testEntityTypeCache() throws EdmException {
    assertEquals(edm.getEntityType("foo", "bar"), edm.getEntityType("foo", "bar"));
    assertNotSame(edm.getEntityType("foo", "bar"), edm.getEntityType("bar", "foo"));
  }

  @Test
  public void testComplexTypeCache() throws EdmException {
    assertEquals(edm.getComplexType("foo", "bar"), edm.getComplexType("foo", "bar"));
    assertNotSame(edm.getComplexType("foo", "bar"), edm.getComplexType("bar", "foo"));
  }

  @Test
  public void testAssociationCache() throws EdmException {
    assertEquals(edm.getAssociation("foo", "bar"), edm.getAssociation("foo", "bar"));
    assertNotSame(edm.getAssociation("foo", "bar"), edm.getAssociation("bar", "foo"));
  }

  @Test
  public void testEntitySetsCache() throws EdmException {
    assertEquals(edm.getEntitySets(), edm.getEntitySets());
  }

  @Test
  public void testFunctionImportCache() throws EdmException {
    assertEquals(edm.getFunctionImports(), edm.getFunctionImports());
  }

  private class ForEdmImplTest extends EdmImpl {

    public ForEdmImplTest() {
      super(null);
    }

    @Override
    protected EdmEntityContainer createEntityContainer(final String name) throws ODataException {
      EdmEntityContainer edmEntityContainer = mock(EdmEntityContainer.class);
      when(edmEntityContainer.getName()).thenReturn(name);
      return edmEntityContainer;
    }

    @Override
    protected EdmEntityType createEntityType(final FullQualifiedName fqName) throws ODataException {
      EdmEntityType edmEntityType = mock(EdmEntityType.class);
      when(edmEntityType.getNamespace()).thenReturn(fqName.getNamespace());
      when(edmEntityType.getName()).thenReturn(fqName.getName());
      return edmEntityType;
    }

    @Override
    protected EdmComplexType createComplexType(final FullQualifiedName fqName) throws ODataException {
      EdmComplexType edmComplexType = mock(EdmComplexType.class);
      when(edmComplexType.getNamespace()).thenReturn(fqName.getNamespace());
      when(edmComplexType.getName()).thenReturn(fqName.getName());
      return edmComplexType;
    }

    @Override
    protected EdmAssociation createAssociation(final FullQualifiedName fqName) throws ODataException {
      EdmAssociation edmAssociation = mock(EdmAssociation.class);
      when(edmAssociation.getNamespace()).thenReturn(fqName.getNamespace());
      when(edmAssociation.getName()).thenReturn(fqName.getName());
      return edmAssociation;
    }

    @Override
    protected List<EdmEntitySet> createEntitySets() throws ODataException {
      List<EdmEntitySet> edmEntitySets = new ArrayList<EdmEntitySet>();
      return edmEntitySets;
    }

    @Override
    protected List<EdmFunctionImport> createFunctionImports() throws ODataException {
      List<EdmFunctionImport> edmFunctionImports = new ArrayList<EdmFunctionImport>();
      return edmFunctionImports;
    }
  }
}
