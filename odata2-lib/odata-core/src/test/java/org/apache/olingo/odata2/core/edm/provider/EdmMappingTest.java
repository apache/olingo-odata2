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
import static org.mockito.Mockito.mock;

import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.CustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.junit.BeforeClass;
import org.junit.Test;

public class EdmMappingTest extends BaseTest {

  private static EdmPropertyImplProv propertySimpleProvider;
  private static EdmNavigationPropertyImplProv navPropertyProvider;
  private static EdmMappingTest mappedObject;

  @BeforeClass
  public static void setup() throws Exception {

    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    mappedObject = new EdmMappingTest();

    Mapping propertySimpleMapping =
        new Mapping().setMimeType("mimeType").setInternalName("value").setObject(mappedObject);
    CustomizableFeedMappings propertySimpleFeedMappings = new CustomizableFeedMappings().setFcKeepInContent(true);
    SimpleProperty propertySimple =
        new SimpleProperty().setName("PropertyName").setType(EdmSimpleTypeKind.String)
            .setMimeType("mimeType").setMapping(propertySimpleMapping).setCustomizableFeedMappings(
                propertySimpleFeedMappings);
    propertySimpleProvider = new EdmSimplePropertyImplProv(edmImplProv, propertySimple);

    NavigationProperty navProperty =
        new NavigationProperty().setName("navProperty").setFromRole("fromRole").setToRole("toRole").setMapping(
            propertySimpleMapping);
    navPropertyProvider = new EdmNavigationPropertyImplProv(edmImplProv, navProperty);
  }

  @Test
  public void testMappingProperty() throws Exception {
    EdmMapping mapping = propertySimpleProvider.getMapping();
    assertNotNull(mapping);
    assertEquals("value", mapping.getInternalName());
    assertEquals("mimeType", mapping.getMimeType());
    assertEquals(mappedObject, mapping.getObject());
  }

  @Test
  public void testMappingNavigationProperty() throws Exception {
    EdmMapping mapping = navPropertyProvider.getMapping();
    assertNotNull(mapping);
    assertEquals("value", mapping.getInternalName());
    assertEquals("mimeType", mapping.getMimeType());
    assertEquals(mappedObject, mapping.getObject());
  }

}
