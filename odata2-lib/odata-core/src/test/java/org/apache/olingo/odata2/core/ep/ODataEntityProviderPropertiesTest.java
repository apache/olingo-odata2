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
  public void buildPropertiesDefaults() throws Exception {
    URI serviceRoot = new URI("http://localhost:80/");
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("aCallback", new MyCallback(null, null));
    ExpandSelectTreeNode expandSelectTree = new ExpandSelectTreeNodeImpl();
    URI selfLink = new URI("http://some.uri");
    Map<String, Map<String, Object>> links = new HashMap<String, Map<String,Object>>();
    links.put("aNavigationProperty", Collections.<String, Object> emptyMap());
    final EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(serviceRoot)
        .callbacks(callbacks)
        .expandSelectTree(expandSelectTree)
        .inlineCount(1)
        .inlineCountType(InlineCount.ALLPAGES)
        .mediaResourceMimeType("image/png")
        .nextLink("http://localhost")
        .selfLink(selfLink)
        .includeSimplePropertyType(true)
        .additionalLinks(links)
        .build();

    assertEquals("Wrong amount of callbacks.", 1, properties.getCallbacks().size());
    assertTrue("No callback found.", properties.getCallbacks().containsKey("aCallback"));
    assertEquals("Wrong expand select tree.", expandSelectTree, properties.getExpandSelectTree());
    assertEquals("Wrong self link.", selfLink, properties.getSelfLink());
    assertEquals("Wrong media resource mime type.", "image/png", properties.getMediaResourceMimeType());
    assertEquals("Wrong base uri.", "http://localhost:80/", properties.getServiceRoot().toASCIIString());
    assertEquals("Wrong inline count type.", InlineCount.ALLPAGES, properties.getInlineCountType());
    assertEquals("Wrong inline count.", Integer.valueOf(1), properties.getInlineCount());
    assertEquals("Wrong nextLink", "http://localhost", properties.getNextLink());
    assertTrue("Simple property types should be true", properties.isIncludeSimplePropertyType());
    assertEquals(Collections.emptyMap(), properties.getAdditionalLinks().get("aNavigationProperty"));
  }

  @Test
  public void buildEntryProperties() throws Exception {
    final String mediaResourceMimeType = "text/html";
    final URI serviceRoot = new URI("http://localhost:80/");
    final EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(serviceRoot)
        .mediaResourceMimeType(mediaResourceMimeType)
        .build();
    assertEquals("Wrong mime type.", "text/html", properties.getMediaResourceMimeType());
  }

  @Test
  public void buildEntryPropertiesFromExisting() throws Exception {
    URI serviceRoot = new URI("http://localhost:80/");
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("aCallback", new MyCallback(null, null));
    ExpandSelectTreeNode expandSelectTree = new ExpandSelectTreeNodeImpl();
    URI selfLink = new URI("http://some.uri");
    Map<String, Map<String, Object>> links = new HashMap<String, Map<String,Object>>();
    links.put("aNavigationProperty", Collections.<String, Object> emptyMap());
    final EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(serviceRoot)
        .callbacks(callbacks)
        .expandSelectTree(expandSelectTree)
        .inlineCount(1)
        .inlineCountType(InlineCount.ALLPAGES)
        .mediaResourceMimeType("image/png")
        .nextLink("http://localhost")
        .selfLink(selfLink)
        .includeSimplePropertyType(true)
        .additionalLinks(links)
        .build();

    //
    final EntityProviderWriteProperties fromProperties =
        EntityProviderWriteProperties.fromProperties(properties).build();

    //
    assertEquals(1, fromProperties.getCallbacks().size());
    assertTrue(fromProperties.getCallbacks().containsKey("aCallback"));
    assertEquals(expandSelectTree, fromProperties.getExpandSelectTree());
    assertEquals(selfLink, fromProperties.getSelfLink());
    assertEquals("image/png", fromProperties.getMediaResourceMimeType());
    assertEquals("Wrong base uri.", "http://localhost:80/", fromProperties.getServiceRoot().toASCIIString());
    assertEquals("Wrong inline count type.", InlineCount.ALLPAGES, fromProperties.getInlineCountType());
    assertEquals("Wrong inline count.", Integer.valueOf(1), fromProperties.getInlineCount());
    assertEquals("Wrong nextLink", "http://localhost", fromProperties.getNextLink());
    assertTrue("Simple property types should be true", fromProperties.isIncludeSimplePropertyType());
    assertEquals(Collections.emptyMap(), fromProperties.getAdditionalLinks().get("aNavigationProperty"));
  }
}
