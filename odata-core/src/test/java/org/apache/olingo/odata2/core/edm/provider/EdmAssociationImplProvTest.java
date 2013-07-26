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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.testutil.fit.BaseTest;

/**
 * @author
 */
public class EdmAssociationImplProvTest extends BaseTest {

  private static EdmAssociationImplProv associationProv;
  private static EdmProvider edmProvider;

  @BeforeClass
  public static void getEdmEntityContainerImpl() throws Exception {

    edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    AssociationEnd end1 = new AssociationEnd().setRole("end1Role").setMultiplicity(EdmMultiplicity.ONE).setType(EdmSimpleTypeKind.String.getFullQualifiedName());
    AssociationEnd end2 = new AssociationEnd().setRole("end2Role").setMultiplicity(EdmMultiplicity.ONE).setType(EdmSimpleTypeKind.String.getFullQualifiedName());

    List<PropertyRef> propRef = new ArrayList<PropertyRef>();
    propRef.add(new PropertyRef().setName("prop1"));
    List<PropertyRef> propRef2 = new ArrayList<PropertyRef>();
    propRef2.add(new PropertyRef().setName("prop2"));

    Association association = new Association().setName("association").setEnd1(end1).setEnd2(end2);

    associationProv = new EdmAssociationImplProv(edmImplProv, association, "namespace");
  }

  @Test
  public void testAssociation() throws Exception {
    EdmAssociation association = associationProv;

    assertEquals(EdmTypeKind.ASSOCIATION, association.getKind());
    assertEquals("end1Role", association.getEnd("end1Role").getRole());
    assertEquals("end2Role", association.getEnd("end2Role").getRole());
    assertEquals("namespace", association.getNamespace());
    assertEquals(null, association.getEnd("endWrongRole"));
  }

  @Test
  public void getAnnotations() throws Exception {
    EdmAnnotatable annotatable = associationProv;
    EdmAnnotations annotations = annotatable.getAnnotations();
    assertNull(annotations.getAnnotationAttributes());
    assertNull(annotations.getAnnotationElements());
  }

}
