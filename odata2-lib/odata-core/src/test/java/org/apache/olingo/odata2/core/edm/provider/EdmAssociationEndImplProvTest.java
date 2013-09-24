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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociationEnd;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *  
 */
public class EdmAssociationEndImplProvTest extends BaseTest {
  private static EdmAssociationEndImplProv associationEndProv;
  private static EdmProvider edmProvider;

  @BeforeClass
  public static void getEdmEntityContainerImpl() throws Exception {

    edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    AssociationEnd end1 =
        new AssociationEnd().setRole("end1Role").setMultiplicity(EdmMultiplicity.ONE).setType(
            EdmSimpleTypeKind.String.getFullQualifiedName());

    associationEndProv = new EdmAssociationEndImplProv(edmImplProv, end1);
  }

  @Test
  public void testAssociationEnd() throws Exception {
    EdmAssociationEnd associationEnd = associationEndProv;

    assertEquals("end1Role", associationEnd.getRole());
    assertEquals(EdmMultiplicity.ONE, associationEnd.getMultiplicity());
  }

  @Test(expected = EdmException.class)
  public void testAssociationEntityType() throws Exception {
    EdmAssociationEnd associationEnd = associationEndProv;
    associationEnd.getEntityType();
  }

  @Test
  public void getAnnotations() throws Exception {
    EdmAnnotatable annotatable = associationEndProv;
    EdmAnnotations annotations = annotatable.getAnnotations();
    assertNull(annotations.getAnnotationAttributes());
    assertNull(annotations.getAnnotationElements());
  }
}
