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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.client.api.ep.EntityCollectionSerializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntitySerializerProperties;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.junit.Test;

/**
 *  
 */
public class SerializerPropertiesTest extends BaseTest {

  @Test
  public void buildFeedProperties() throws Exception {
    URI serviceRoot = new URI("http://localhost:80/");
    EntitySerializerProperties properties = EntitySerializerProperties.serviceRoot(serviceRoot)
        .build();

    assertEquals("Wrong base uri.", "http://localhost:80/", properties.getServiceRoot().toASCIIString());
   }

  @Test
  public void buildPropertiesDefault() throws Exception {
    URI serviceRoot = new URI("http://localhost:80/");
    final EntitySerializerProperties properties 
    = EntitySerializerProperties.serviceRoot(serviceRoot).build();

    assertFalse(properties.isIncludeMetadata());
    assertTrue(properties.isValidatingFacets());
  }

  @Test
  public void buildPropertiesAllSet() throws Exception {
    URI serviceRoot = new URI("http://localhost:80/");
    Map<String, Map<String, Object>> links = new HashMap<String, Map<String, Object>>();
    links.put("aNavigationProperty", Collections.<String, Object> emptyMap());
    final EntitySerializerProperties properties = EntitySerializerProperties
        .serviceRoot(serviceRoot)
        .includeMetadata(true)
        .validatingFacets(false)
        .build();

    assertEquals("Wrong base uri.", "http://localhost:80/", properties.getServiceRoot().toASCIIString());
    assertTrue("Metadata in content should be set", properties.isIncludeMetadata());
    assertFalse("validating facets should be not set", properties.isValidatingFacets());
  }

  @Test
  public void buildPropertiesForEntityCollectionAllSet() throws Exception {
    URI serviceRoot = new URI("http://localhost:80/");
    URI selfLink = new URI("http://some.uri");
    final EntityCollectionSerializerProperties properties = 
        EntityCollectionSerializerProperties.serviceRoot(serviceRoot)
        .selfLink(selfLink)
        .build();

    assertEquals("Wrong base uri.", "http://localhost:80/", properties.getServiceRoot().toASCIIString());
    assertEquals(selfLink, properties.getSelfLink());
  }
}
