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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.AliasInfo;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.junit.Before;
import org.junit.Test;

public class EdmImplProvTest extends BaseTest {

  private static EdmImplProv edm;

  @Before
  public void getEdmImpl() throws Exception {
    EdmProvider edmProvider = mock(EdmProvider.class);

    List<AliasInfo> aliasInfos = new ArrayList<AliasInfo>();

    EntityType entityType = new EntityType().setName("EntityType1");
    when(edmProvider.getEntityType(new FullQualifiedName("EntityType1Ns", "EntityType1"))).thenReturn(entityType);
    AliasInfo aliasInfo1 = new AliasInfo().setAlias("et1").setNamespace("EntityType1Ns");
    aliasInfos.add(aliasInfo1);

    ComplexType complexType = new ComplexType().setName("ComplexType1");
    when(edmProvider.getComplexType(new FullQualifiedName("ComplexType1Ns", "ComplexType1"))).thenReturn(complexType);
    AliasInfo aliasInfo2 = new AliasInfo().setAlias("ct1").setNamespace("ComplexType1Ns");
    aliasInfos.add(aliasInfo2);

    Association association = new Association().setName("Association1");
    when(edmProvider.getAssociation(new FullQualifiedName("Association1Ns", "Association1"))).thenReturn(association);
    AliasInfo aliasInfo3 = new AliasInfo().setAlias("at1").setNamespace("Association1Ns");
    aliasInfos.add(aliasInfo3);

    when(edmProvider.getAliasInfos()).thenReturn(aliasInfos);

    EntityContainerInfo defaultEntityContainer = new EntityContainerInfo().setName("Container1");
    when(edmProvider.getEntityContainerInfo(null)).thenReturn(defaultEntityContainer);
    when(edmProvider.getEntityContainerInfo("Container1")).thenReturn(defaultEntityContainer);

    edm = new EdmImplProv(edmProvider);
  }

  @Test
  public void assertCallWithAliasResultsInRightCaching() throws Exception{
    EdmEntityType entityTypeWithAlias = edm.getEntityType("et1", "EntityType1");
    assertEquals("EntityType1", entityTypeWithAlias.getName());
    EdmEntityType entityType = edm.getEntityType("EntityType1Ns", "EntityType1");
    assertEquals("EntityType1", entityType.getName());
    assertEquals(entityType, entityTypeWithAlias);
    
    EdmComplexType complexTypeWithAlias = edm.getComplexType("ct1", "ComplexType1");
    assertEquals("ComplexType1", complexTypeWithAlias.getName());
    EdmComplexType complexType = edm.getComplexType("ComplexType1Ns", "ComplexType1");
    assertEquals("ComplexType1", complexType.getName());
    assertEquals(complexType, complexTypeWithAlias);
    
    EdmAssociation associationWithAlias = edm.getAssociation("at1", "Association1");
    assertEquals("Association1", associationWithAlias.getName());
    EdmAssociation association = edm.getAssociation("Association1Ns", "Association1");
    assertEquals("Association1", association.getName());
    assertEquals(association, associationWithAlias);
  }
  
  @Test
  public void testEntityType() throws EdmException {
    EdmEntityType entityType = edm.getEntityType("EntityType1Ns", "EntityType1");
    assertEquals("EntityType1", entityType.getName());

    EdmEntityType entityTypeWithAlias = edm.getEntityType("et1", "EntityType1");
    assertEquals("EntityType1", entityTypeWithAlias.getName());

    assertEquals(entityType, entityTypeWithAlias);
  }

  @Test
  public void testComplexType() throws EdmException {
    EdmComplexType complexType = edm.getComplexType("ComplexType1Ns", "ComplexType1");
    assertEquals("ComplexType1", complexType.getName());

    EdmComplexType complexTypeWithAlias = edm.getComplexType("ct1", "ComplexType1");
    assertEquals("ComplexType1", complexTypeWithAlias.getName());

    assertEquals(complexType, complexTypeWithAlias);
  }

  @Test
  public void testAssociation() throws EdmException {
    EdmAssociation association = edm.getAssociation("Association1Ns", "Association1");
    assertEquals("Association1", association.getName());

    EdmAssociation associationWithAlias = edm.getAssociation("at1", "Association1");
    assertEquals("Association1", associationWithAlias.getName());

    assertEquals(association, associationWithAlias);
  }

  @Test
  public void testDefaultEntityContainer() throws EdmException {
    assertEquals(edm.getEntityContainer("Container1"), edm.getDefaultEntityContainer());
  }
}
