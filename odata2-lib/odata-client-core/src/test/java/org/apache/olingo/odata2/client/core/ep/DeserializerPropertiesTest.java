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
package org.apache.olingo.odata2.client.core.ep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.client.api.ep.DeserializerProperties;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.junit.Test;

/**
 *  
 */
public class DeserializerPropertiesTest extends BaseTest {

  @Test
  public void buildReadProperties() throws Exception {
    DeserializerProperties properties = DeserializerProperties.init()
        .build();

    assertNotNull(properties.getTypeMappings());
    assertEquals(0, properties.getTypeMappings().size());
    assertNotNull(properties.getValidatedPrefixNamespaceUris());
    assertEquals(0, properties.getValidatedPrefixNamespaceUris().size());
   }

  @Test
  public void buildPropertiesAllSet() throws Exception {
    Map<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("aNamespace", new String());
    Map<String, Object> typeMappings = new HashMap<String, Object>();
    typeMappings.put("Property", Timestamp.class);
    final DeserializerProperties properties = DeserializerProperties.init()
        .addValidatedPrefixes(namespaces)
        .isValidatingFacets(true)
        .addTypeMappings(typeMappings)
        .build();

    assertNotNull(properties.getValidatedPrefixNamespaceUris());
    assertNotNull(properties.getTypeMappings());
    assertTrue("validating facets should be set", properties.isValidatingFacets());
  }
}
