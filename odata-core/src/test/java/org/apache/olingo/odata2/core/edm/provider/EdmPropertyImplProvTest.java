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
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.CustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.core.edm.EdmSimpleTypeFacadeImpl;
import org.apache.olingo.odata2.testutil.fit.BaseTest;

/**
 * @author
 */
public class EdmPropertyImplProvTest extends BaseTest {

  private static EdmProvider edmProvider;
  private static EdmPropertyImplProv propertySimpleProvider;
  private static EdmPropertyImplProv propertySimpleWithFacetsProvider;
  private static EdmPropertyImplProv propertySimpleWithFacetsProvider2;
  private static EdmPropertyImplProv propertyComplexProvider;

  @BeforeClass
  public static void setup() throws Exception {

    edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    Mapping propertySimpleMapping = new Mapping().setMimeType("mimeType2").setInternalName("value");
    CustomizableFeedMappings propertySimpleFeedMappings = new CustomizableFeedMappings().setFcKeepInContent(true);
    SimpleProperty propertySimple = new SimpleProperty().setName("PropertyName").setType(EdmSimpleTypeKind.String)
        .setMimeType("mimeType").setMapping(propertySimpleMapping).setCustomizableFeedMappings(propertySimpleFeedMappings);
    propertySimpleProvider = new EdmSimplePropertyImplProv(edmImplProv, propertySimple);

    Facets facets = new Facets().setNullable(false);
    SimpleProperty propertySimpleWithFacets = new SimpleProperty().setName("PropertyName").setType(EdmSimpleTypeKind.String).setFacets(facets);
    propertySimpleWithFacetsProvider = new EdmSimplePropertyImplProv(edmImplProv, propertySimpleWithFacets);

    Facets facets2 = new Facets().setNullable(true);
    SimpleProperty propertySimpleWithFacets2 = new SimpleProperty().setName("PropertyName").setType(EdmSimpleTypeKind.String).setFacets(facets2);
    propertySimpleWithFacetsProvider2 = new EdmSimplePropertyImplProv(edmImplProv, propertySimpleWithFacets2);

    ComplexType complexType = new ComplexType().setName("complexType");
    FullQualifiedName complexName = new FullQualifiedName("namespace", "complexType");
    when(edmProvider.getComplexType(complexName)).thenReturn(complexType);

    ComplexProperty propertyComplex = new ComplexProperty().setName("complexProperty").setType(complexName);
    propertyComplexProvider = new EdmComplexPropertyImplProv(edmImplProv, propertyComplex);

  }

  @Test
  public void testPropertySimple() throws Exception {
    assertNotNull(propertySimpleProvider);
    assertEquals("PropertyName", propertySimpleProvider.getName());
    assertNotNull(propertySimpleProvider.getType());
    assertEquals(EdmSimpleTypeFacadeImpl.getEdmSimpleType(EdmSimpleTypeKind.String), propertySimpleProvider.getType());
    assertEquals("mimeType", propertySimpleProvider.getMimeType());
    assertNotNull(propertySimpleProvider.getMapping());
    assertEquals("mimeType2", propertySimpleProvider.getMapping().getMimeType());
    assertNotNull(propertySimpleProvider.getCustomizableFeedMappings());
    assertEquals("value", propertySimpleProvider.getMapping().getInternalName());
    assertNull(propertySimpleProvider.getFacets());
    assertNotNull(propertySimpleProvider.getMultiplicity());
    assertEquals(EdmMultiplicity.ZERO_TO_ONE, propertySimpleProvider.getMultiplicity());
  }

  @Test
  public void testPropertySimpleWithFacets() throws Exception {
    assertNotNull(propertySimpleWithFacetsProvider.getFacets());
    assertNotNull(propertySimpleWithFacetsProvider.getMultiplicity());
    assertEquals(EdmMultiplicity.ONE, propertySimpleWithFacetsProvider.getMultiplicity());

    assertNotNull(propertySimpleWithFacetsProvider2.getFacets());
    assertNotNull(propertySimpleWithFacetsProvider2.getMultiplicity());
    assertEquals(EdmMultiplicity.ZERO_TO_ONE, propertySimpleWithFacetsProvider2.getMultiplicity());
  }

  @Test
  public void testPropertyComplex() throws Exception {
    assertNotNull(propertyComplexProvider);
    assertEquals("complexProperty", propertyComplexProvider.getName());
    assertEquals(EdmTypeKind.COMPLEX, propertyComplexProvider.getType().getKind());
    assertEquals("complexType", propertyComplexProvider.getType().getName());
  }

  @Test
  public void getAnnotations() throws Exception {
    EdmAnnotatable annotatable = propertySimpleProvider;
    EdmAnnotations annotations = annotatable.getAnnotations();
    assertNull(annotations.getAnnotationAttributes());
    assertNull(annotations.getAnnotationElements());
  }
}
