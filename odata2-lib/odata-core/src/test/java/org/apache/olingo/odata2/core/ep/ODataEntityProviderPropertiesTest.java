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
package org.apache.olingo.odata2.core.ep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.core.ep.producer.MyCallback;
import org.apache.olingo.odata2.core.uri.ExpandSelectTreeNodeImpl;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.junit.Test;

/**
 *  
 */
public class ODataEntityProviderPropertiesTest extends BaseTest {

  @Test
  public void buildFeedProperties() throws Exception {
    URI serviceRoot = new URI("http://localhost:80/");
    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(serviceRoot)
        .inlineCountType(InlineCount.ALLPAGES)
        .inlineCount(1)
        .nextLink("http://localhost")
        .build();

    assertEquals("Wrong base uri.", "http://localhost:80/", properties.getServiceRoot().toASCIIString());
    assertEquals("Wrong inline count type.", InlineCount.ALLPAGES, properties.getInlineCountType());
    assertEquals("Wrong inline count.", Integer.valueOf(1), properties.getInlineCount());
    assertEquals("Wrong nextLink", "http://localhost", properties.getNextLink());
  }

  @Test
  public void buildPropertiesDefault() throws Exception {
    URI serviceRoot = new URI("http://localhost:80/");
    final EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(serviceRoot).build();
    assertNotNull(properties.getCallbacks());
    assertTrue("Default callbacks should be empty", properties.getCallbacks().isEmpty());
    assertNull(properties.getExpandSelectTree());
    assertNull(properties.getSelfLink());
    assertNull(properties.getInlineCount());
    assertNull(properties.getInlineCountType());
    assertNull(properties.getNextLink());
    assertNull(properties.getAdditionalLinks());

    assertFalse(properties.isIncludeSimplePropertyType());
    assertFalse(properties.isOmitJsonWrapper());
    assertFalse(properties.isContentOnly());
    assertFalse(properties.isOmitETag());
    assertFalse(properties.isIncludeMetadataInContentOnly());
    assertTrue(properties.isResponsePayload());
    assertFalse(properties.isDataBasedPropertySerialization());
    assertFalse(properties.isOmitInlineForNullData());
  }

  @Test
  public void buildPropertiesAllSet() throws Exception {
    URI serviceRoot = new URI("http://localhost:80/");
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("aCallback", new MyCallback(null, null));
    ExpandSelectTreeNode expandSelectTree = new ExpandSelectTreeNodeImpl();
    URI selfLink = new URI("http://some.uri");
    Map<String, Map<String, Object>> links = new HashMap<String, Map<String, Object>>();
    links.put("aNavigationProperty", Collections.<String, Object> emptyMap());
    final EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(serviceRoot)
        .callbacks(callbacks)
        .expandSelectTree(expandSelectTree)
        .inlineCount(1)
        .inlineCountType(InlineCount.ALLPAGES)
        .nextLink("http://localhost")
        .selfLink(selfLink)
        .includeSimplePropertyType(true)
        .additionalLinks(links)
        .omitJsonWrapper(true)
        .contentOnly(true)
        .omitETag(true)
        .includeMetadataInContentOnly(true)
        .responsePayload(true)
        .isDataBasedPropertySerialization(true)
        .omitInlineForNullData(true)
        .build();

    assertEquals("Wrong amount of callbacks.", 1, properties.getCallbacks().size());
    assertTrue("No callback found.", properties.getCallbacks().containsKey("aCallback"));
    assertEquals("Wrong expand select tree.", expandSelectTree, properties.getExpandSelectTree());
    assertEquals("Wrong self link.", selfLink, properties.getSelfLink());
    assertEquals("Wrong base uri.", "http://localhost:80/", properties.getServiceRoot().toASCIIString());
    assertEquals("Wrong inline count type.", InlineCount.ALLPAGES, properties.getInlineCountType());
    assertEquals("Wrong inline count.", Integer.valueOf(1), properties.getInlineCount());
    assertEquals("Wrong nextLink", "http://localhost", properties.getNextLink());
    assertTrue("Simple property types should be true", properties.isIncludeSimplePropertyType());
    assertEquals(Collections.emptyMap(), properties.getAdditionalLinks().get("aNavigationProperty"));
    assertTrue("Json Wrapper should be omitted", properties.isOmitJsonWrapper());
    assertTrue("ContentOnlyFlag should be set", properties.isContentOnly());
    assertTrue("OmitETag should be set", properties.isOmitETag());

    assertTrue("includeMetadataInContentOnly should be set", properties.isIncludeMetadataInContentOnly());
    assertTrue("responsePayload flag should be set", properties.isResponsePayload());
    assertTrue("isDataBasedPropertySerialization should be set", properties.isDataBasedPropertySerialization());
    assertTrue("omitInlineForNullData should be set", properties.isOmitInlineForNullData());
  }

  @Test
  public void buildEntryPropertiesFromExisting() throws Exception {
    URI serviceRoot = new URI("http://localhost:80/");
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("aCallback", new MyCallback(null, null));
    ExpandSelectTreeNode expandSelectTree = new ExpandSelectTreeNodeImpl();
    URI selfLink = new URI("http://some.uri");
    Map<String, Map<String, Object>> links = new HashMap<String, Map<String, Object>>();
    links.put("aNavigationProperty", Collections.<String, Object> emptyMap());
    final EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(serviceRoot)
        .callbacks(callbacks)
        .expandSelectTree(expandSelectTree)
        .inlineCount(1)
        .inlineCountType(InlineCount.ALLPAGES)
        .nextLink("http://localhost")
        .selfLink(selfLink)
        .includeSimplePropertyType(true)
        .additionalLinks(links)
        .omitJsonWrapper(true)
        .contentOnly(true)
        .omitETag(true)
        .includeMetadataInContentOnly(true)
        .responsePayload(true)
        .isDataBasedPropertySerialization(true)
        .omitInlineForNullData(true)
        .build();

    //
    final EntityProviderWriteProperties fromProperties =
        EntityProviderWriteProperties.fromProperties(properties).build();

    //
    assertEquals(1, fromProperties.getCallbacks().size());
    assertTrue(fromProperties.getCallbacks().containsKey("aCallback"));
    assertEquals(expandSelectTree, fromProperties.getExpandSelectTree());
    assertEquals(selfLink, fromProperties.getSelfLink());
    assertEquals("Wrong base uri.", "http://localhost:80/", fromProperties.getServiceRoot().toASCIIString());
    assertEquals("Wrong inline count type.", InlineCount.ALLPAGES, fromProperties.getInlineCountType());
    assertEquals("Wrong inline count.", Integer.valueOf(1), fromProperties.getInlineCount());
    assertEquals("Wrong nextLink", "http://localhost", fromProperties.getNextLink());
    assertTrue("Simple property types should be true", fromProperties.isIncludeSimplePropertyType());
    assertEquals(Collections.emptyMap(), fromProperties.getAdditionalLinks().get("aNavigationProperty"));
    assertTrue("Json Wrapper should be omitted", fromProperties.isOmitJsonWrapper());
    assertTrue("ContentOnlyFlag should be set", fromProperties.isContentOnly());
    assertTrue("OmitETag should be set", fromProperties.isOmitETag());
    assertTrue("includeMetadataInContentOnly should be set", fromProperties.isIncludeMetadataInContentOnly());
    assertTrue("responsePayload flag should be set", fromProperties.isResponsePayload());
    assertTrue("isDataBasedPropertySerialization should be set", properties.isDataBasedPropertySerialization());
    assertTrue("omitInlineForNullData should be set", properties.isOmitInlineForNullData());
  }
}
