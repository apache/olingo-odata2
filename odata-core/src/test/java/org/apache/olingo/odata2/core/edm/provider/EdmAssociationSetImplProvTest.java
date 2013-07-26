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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociationSet;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.AssociationSetEnd;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.testutil.fit.BaseTest;

public class EdmAssociationSetImplProvTest extends BaseTest {

  private static EdmAssociationSet edmAssociationSet;
  private static EdmProvider edmProvider;

  @BeforeClass
  public static void getEdmEntityContainerImpl() throws Exception {

    edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    EntityContainerInfo entityContainer = new EntityContainerInfo().setName("Container");
    EdmEntityContainer edmEntityContainer = new EdmEntityContainerImplProv(edmImplProv, entityContainer);

    AssociationEnd end1 = new AssociationEnd().setRole("end1Role").setMultiplicity(EdmMultiplicity.ONE).setType(EdmSimpleTypeKind.String.getFullQualifiedName());
    AssociationEnd end2 = new AssociationEnd().setRole("end2Role").setMultiplicity(EdmMultiplicity.ONE).setType(EdmSimpleTypeKind.String.getFullQualifiedName());
    Association association = new Association().setName("association").setEnd1(end1).setEnd2(end2);
    FullQualifiedName assocName = new FullQualifiedName("namespace", "association");
    when(edmProvider.getAssociation(assocName)).thenReturn(association);

    AssociationSetEnd endSet1 = new AssociationSetEnd().setRole("end1Role").setEntitySet("entitySetRole1");
    when(edmProvider.getEntitySet("Container", "entitySetRole1")).thenReturn(new EntitySet().setName("entitySetRole1"));
    AssociationSetEnd endSet2 = new AssociationSetEnd().setRole("end2Role");

    AssociationSet associationSet = new AssociationSet().setName("associationSetName").setAssociation(assocName).setEnd1(endSet1).setEnd2(endSet2);

    edmAssociationSet = new EdmAssociationSetImplProv(edmImplProv, associationSet, edmEntityContainer);
  }

  @Test
  public void testAssociationSet() throws Exception {
    EdmAssociationSet associationSet = edmAssociationSet;

    assertEquals("associationSetName", associationSet.getName());

    assertEquals("end1Role", associationSet.getEnd("end1Role").getRole());
    assertEquals(null, associationSet.getEnd("endWrongRole"));
  }

  @Test(expected = EdmException.class)
  public void testAssociationSetNoEntity() throws Exception {
    EdmAssociationSet associationSet = edmAssociationSet;
    associationSet.getEnd("end2Role");
  }

  @Test
  public void testAssociationExists() throws Exception {
    EdmAssociationSet associationSet = edmAssociationSet;
    assertNotNull(associationSet.getAssociation());
  }

  @Test
  public void testEntityContainer() throws Exception {
    EdmAssociationSet associationSet = edmAssociationSet;
    assertNotNull(associationSet.getEntityContainer());
  }

  @Test
  public void getAnnotations() throws Exception {
    EdmAnnotatable annotatable = (EdmAnnotatable) edmAssociationSet;
    EdmAnnotations annotations = annotatable.getAnnotations();
    assertNull(annotations.getAnnotationAttributes());
    assertNull(annotations.getAnnotationElements());
  }
}
