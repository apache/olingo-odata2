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
package org.apache.olingo.odata2.core.ep.producer;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.ep.AbstractProviderTest;
import org.apache.olingo.odata2.core.ep.AtomEntityProvider;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.consumer.XmlEntityConsumer;
import org.apache.olingo.odata2.core.uri.ExpandSelectTreeCreator;
import org.apache.olingo.odata2.core.uri.UriParserImpl;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.EdmTestProvider;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *  
 */
public class AtomFeedProducerTest extends AbstractProviderTest {

  private String employeeXPathString = "/a:entry/a:link[@href=\"Rooms('1')/nr_Employees\" and @title='nr_Employees']";
  
  public AtomFeedProducerTest(final StreamWriterImplType type) {
    super(type);
  }

  private GetEntitySetUriInfo view;

  @Before
  public void before() throws Exception {
    initializeRoomData(1);

    view = mock(GetEntitySetUriInfo.class);

    EdmEntitySet set = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    when(view.getTargetEntitySet()).thenReturn(set);
  }

  @Test
  public void testWithIncludeSimplePropertyTypes() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).includeSimplePropertyType(true).build();
    ODataResponse response = ser.writeFeed(view.getTargetEntitySet(), roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Id[@m:type=\"Edm.String\"]", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Name[@m:type=\"Edm.String\"]", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Seats[@m:type=\"Edm.Int16\"]", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Version[@m:type=\"Edm.Int16\"]", xmlString);
  }

  @Test
  public void testFeedNamespaces() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    ODataResponse response = ser.writeFeed(view.getTargetEntitySet(), roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:feed/@xml:base", xmlString);
  }

  @Test
  public void testSelfLink() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    ODataResponse response = ser.writeFeed(view.getTargetEntitySet(), roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed/a:link[@rel='self']", xmlString);
    assertXpathEvaluatesTo("Rooms", "/a:feed/a:link[@rel='self']/@href", xmlString);
    assertXpathEvaluatesTo("Rooms", "/a:feed/a:link[@rel='self']/@title", xmlString);
  }

  @Test
  public void testCustomSelfLink() throws Exception {
    String customLink = "Test";
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).selfLink(
            new URI(customLink)).build();
    ODataResponse response = ser.writeFeed(view.getTargetEntitySet(), roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed/a:link[@rel='self']", xmlString);
    assertXpathEvaluatesTo(customLink, "/a:feed/a:link[@rel='self']/@href", xmlString);
    assertXpathEvaluatesTo("Rooms", "/a:feed/a:link[@rel='self']/@title", xmlString);
  }

  @Test
  public void testFeedMandatoryParts() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    ODataResponse response = ser.writeFeed(view.getTargetEntitySet(), roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed/a:id", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString() + "Rooms", "/a:feed/a:id/text()", xmlString);

    assertXpathExists("/a:feed/a:title", xmlString);
    assertXpathEvaluatesTo("Rooms", "/a:feed/a:title/text()", xmlString);

    assertXpathExists("/a:feed/a:updated", xmlString);
    assertXpathExists("/a:feed/a:author", xmlString);
    assertXpathExists("/a:feed/a:author/a:name", xmlString);
  }

  private String verifyResponse(final ODataResponse response) throws IOException {
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntityProvider should not set content header", response.getContentHeader());
    String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());
    return xmlString;
  }

  @Test
  public void testInlineCountAllpages() throws Exception {
    initializeRoomData(20);

    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI)
        .inlineCount(Integer.valueOf(103))
        .inlineCountType(InlineCount.ALLPAGES)
        .build();
    ODataResponse response = ser.writeFeed(view.getTargetEntitySet(), roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed/m:count", xmlString);
    assertXpathEvaluatesTo("103", "/a:feed/m:count/text()", xmlString);
  }

  @Test
  public void testInlineCountNone() throws Exception {
    when(view.getInlineCount()).thenReturn(InlineCount.NONE);

    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    ODataResponse response = ser.writeFeed(view.getTargetEntitySet(), roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathNotExists("/a:feed/m:count", xmlString);
  }

  @Test
  public void testNextLink() throws Exception {
    when(view.getInlineCount()).thenReturn(InlineCount.NONE);

    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI)
        .nextLink("http://thisisanextlink")
        .build();
    ODataResponse response = ser.writeFeed(view.getTargetEntitySet(), roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed/a:link[@rel='next']", xmlString);
    assertXpathEvaluatesTo("http://thisisanextlink", "/a:feed/a:link[@rel='next']/@href", xmlString);
  }

  @Test(expected = EntityProviderException.class)
  public void testInlineCountInvalid() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).inlineCountType(
            InlineCount.ALLPAGES).build();
    ser.writeFeed(view.getTargetEntitySet(), roomsData, properties);
  }

  @Test
  public void testEntries() throws Exception {
    initializeRoomData(103);

    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    ODataResponse response = ser.writeFeed(view.getTargetEntitySet(), roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed/a:entry[1]", xmlString);
    assertXpathExists("/a:feed/a:entry[2]", xmlString);
    assertXpathExists("/a:feed/a:entry[103]", xmlString);
  }

  @Test
  public void unbalancedPropertyFeed() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Companys");
    List<Map<String, Object>> originalData = createData(true);
    final ODataResponse response = new AtomEntityProvider().writeFeed(entitySet, originalData,
        EntityProviderWriteProperties.serviceRoot(BASE_URI).isDataBasedPropertySerialization(true).build());

    EntityProviderReadProperties readProperties = EntityProviderReadProperties.init().mergeSemantic(false).build();
    XmlEntityConsumer consumer = new XmlEntityConsumer();
    ODataFeed feed = consumer.readFeed(entitySet, (InputStream) response.getEntity(), readProperties);

    compareList(originalData, feed.getEntries());
  }

  @Test
  public void unbalancedPropertyFeedWithInvalidProperty() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Companys");
    List<Map<String, Object>> originalData = createDataWithInvalidProperty(true);
    final ODataResponse response = new AtomEntityProvider().writeFeed(entitySet, originalData,
        EntityProviderWriteProperties.serviceRoot(BASE_URI).isDataBasedPropertySerialization(true).build());

    EntityProviderReadProperties readProperties = EntityProviderReadProperties.init().mergeSemantic(false).build();
    XmlEntityConsumer consumer = new XmlEntityConsumer();
    ODataFeed feed = consumer.readFeed(entitySet, (InputStream) response.getEntity(), readProperties);
    originalData.get(0).remove("Address");
    compareList(originalData, feed.getEntries());
  }
  
  @Test(expected = EntityProviderProducerException.class)
  public void unbalancedPropertyFeedWithNullKey() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Companys");
    List<Map<String, Object>> originalData = createDataWithKeyNull(true);
    new AtomEntityProvider().writeFeed(entitySet, originalData,
        EntityProviderWriteProperties.serviceRoot(BASE_URI).isDataBasedPropertySerialization(true).build());
  }
  
  @Test(expected = EntityProviderProducerException.class)
  public void unbalancedPropertyFeedWithoutKeys() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Companys");
    List<Map<String, Object>> originalData = createDataWithoutKey(true);
    new AtomEntityProvider().writeFeed(entitySet, originalData,
        EntityProviderWriteProperties.serviceRoot(BASE_URI).isDataBasedPropertySerialization(true).build());
  }
  
  @Test(expected = EntityProviderProducerException.class)
  public void unbalancedPropertyFeedWithEmptyData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Companys");
    List<Map<String, Object>> feedData = new ArrayList<Map<String, Object>>();
    Map<String, Object> entryData = new HashMap<String, Object>();
    feedData.add(entryData);
    new AtomEntityProvider().writeFeed(entitySet, feedData,
        EntityProviderWriteProperties.serviceRoot(BASE_URI).isDataBasedPropertySerialization(true).build());
  }
  
  @Test
  public void unbalancedPropertyFeedWithSelect() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Companys");
    List<Map<String, Object>> originalData = createData(true);
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("Id");
    selectedPropertyNames.add("Location");
    ExpandSelectTreeNode select =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedPropertyNames).build();
    
    ODataResponse response = new AtomEntityProvider().writeFeed(entitySet, originalData,
        EntityProviderWriteProperties.serviceRoot(BASE_URI).expandSelectTree(select).
        isDataBasedPropertySerialization(true).build());

    EntityProviderReadProperties readProperties = EntityProviderReadProperties.init().mergeSemantic(false).build();
    XmlEntityConsumer consumer = new XmlEntityConsumer();
    ODataFeed feed = consumer.readFeed(entitySet, (InputStream) response.getEntity(), readProperties);

    compareList(originalData, feed.getEntries());
  }
  
  @Test
  public void unbalancedPropertyEntryWithInlineFeed() throws Exception {
    ExpandSelectTreeNode selectTree = getSelectExpandTree("Rooms('1')", "nr_Employees", "nr_Employees");

    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Name", "Neu Schwanstein");
    roomData.put("Seats", new Integer(20));
    
    class EntryCallback implements OnWriteFeedContent {
      @Override
      public WriteFeedCallbackResult retrieveFeedResult(final WriteFeedCallbackContext context)
          throws ODataApplicationException {
        List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("EmployeeId", "1");
        data.put("EmployeeName", "EmpName1");
        data.put("RoomId", "1");
        listData.add(data);
        
        data = new HashMap<String, Object>();
        data.put("EmployeeId", "1");
        data.put("RoomId", "1");
        listData.add(data);
        
        WriteFeedCallbackResult result = new WriteFeedCallbackResult();
        result.setFeedData(listData);
        EntityProviderWriteProperties inlineProperties =
            EntityProviderWriteProperties.serviceRoot(BASE_URI).expandSelectTree(
                context.getCurrentExpandSelectTreeNode()).build();
        result.setInlineProperties(inlineProperties);
        return result;
      }
    }
    EntryCallback callback = new EntryCallback();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Employees", callback);
    
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).expandSelectTree(selectTree).callbacks(callbacks).
        isDataBasedPropertySerialization(true).build();
    AtomEntityProvider provider = createAtomEntityProvider();
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData,
            properties);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyEmployees(employeeXPathString, xmlString);

    callbacks.clear();
    callbacks.put("Room.nr_Employees", callback);
    properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).expandSelectTree(selectTree).
        callbacks(callbacks).isDataBasedPropertySerialization(true).build();
    provider = createAtomEntityProvider();
    response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData,
            properties);

    xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyEmployees(employeeXPathString, xmlString);
  }
    
  private ExpandSelectTreeNode getSelectExpandTree(final String pathSegment, final String selectString,
      final String expandString) throws Exception {

    Edm edm = RuntimeDelegate.createEdm(new EdmTestProvider());
    UriParserImpl uriParser = new UriParserImpl(edm);

    List<PathSegment> pathSegments = new ArrayList<PathSegment>();
    pathSegments.add(new ODataPathSegmentImpl(pathSegment, null));

    Map<String, String> queryParameters = new HashMap<String, String>();
    if (selectString != null) {
      queryParameters.put("$select", selectString);
    }
    if (expandString != null) {
      queryParameters.put("$expand", expandString);
    }
    UriInfo uriInfo = uriParser.parse(pathSegments, queryParameters);

    ExpandSelectTreeCreator expandSelectTreeCreator =
        new ExpandSelectTreeCreator(uriInfo.getSelect(), uriInfo.getExpand());
    ExpandSelectTreeNode expandSelectTree = expandSelectTreeCreator.create();
    assertNotNull(expandSelectTree);
    return expandSelectTree;
  }
  
  private void verifyEmployees(final String path, final String xmlString) throws XpathException, IOException,
  SAXException {
  assertXpathExists(path, xmlString);
  assertXpathExists(path + "/m:inline", xmlString);
  
  assertXpathExists(path + "/m:inline/a:feed[@xml:base='" + BASE_URI + "']", xmlString);
  assertXpathExists(path + "/m:inline/a:feed/a:entry", xmlString);
  assertXpathExists(path + "/m:inline/a:feed/a:entry/a:id", xmlString);
  assertXpathExists(path + "/m:inline/a:feed/a:entry/a:title", xmlString);
  assertXpathExists(path + "/m:inline/a:feed/a:entry/a:updated", xmlString);
  
  assertXpathExists(path + "/m:inline/a:feed/a:entry/a:category", xmlString);
  assertXpathExists(path + "/m:inline/a:feed/a:entry/a:link", xmlString);
  
  assertXpathExists(path + "/m:inline/a:feed/a:entry/a:content", xmlString);
  assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties", xmlString);
  assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:EmployeeId", xmlString);
  assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:EmployeeName", xmlString);
  assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:RoomId", xmlString);
  
  assertXpathExists("/a:entry/a:content/m:properties/d:Id", xmlString);
  assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
  assertXpathExists("/a:entry/a:content/m:properties/d:Seats", xmlString);
  
  }
  
  private void compareList(List<Map<String, Object>> expectedList, List<ODataEntry> actualList) {
    assertEquals(expectedList.size(), actualList.size());

    for (int i = 0; i < expectedList.size(); i++) {
      Map<String, Object> expected = expectedList.get(i);
      Map<String, Object> actual = actualList.get(i).getProperties();
      compareMap(i, expected, actual);
    }
  }

  @SuppressWarnings("unchecked")
  private void compareMap(int index, Map<String, Object> expected, Map<String, Object> actual) {
    
    assertEquals("Entry: " + index + " does not contain the same amount of properties", expected.size(),
        actual.size());
    for (Map.Entry<String, Object> entry : expected.entrySet()) {
      String key = entry.getKey();
      assertTrue("Entry " + index + " should contain key: " + key, actual.containsKey(key));

      if (entry.getValue() instanceof Map<?, ?>) {
        assertTrue("Entry " + index + " Value: " + key + " should be a map", actual.get(key) instanceof Map<?, ?>);
        compareMap(index, (Map<String, Object>) entry.getValue(), (Map<String, Object>) actual.get(key));
      } else {
        assertEquals("Entry: " + index + " values are not the same: " + key, entry.getValue(), actual.get(key));
      }
    }
  }

  private List<Map<String, Object>> createData(boolean includeKeys) {
    List<Map<String, Object>> feedData = new ArrayList<Map<String, Object>>();
    Map<String, Object> entryData = new HashMap<String, Object>();
    entryData.put("Id", "1");
    feedData.add(entryData);

    entryData = new HashMap<String, Object>();
    entryData.put("Id", "2");
    entryData.put("Name", "Company2");
    entryData.put("Location", null);
    feedData.add(entryData);

    entryData = new HashMap<String, Object>();
    entryData.put("Id", "3");
    entryData.put("NGO", false);
    Map<String, Object> locationData = new HashMap<String, Object>();
    Map<String, Object> cityData = new HashMap<String, Object>();
    cityData.put("PostalCode", "code3");
    locationData.put("City", cityData);

    entryData.put("Location", locationData);
    feedData.add(entryData);

    entryData = new HashMap<String, Object>();
    entryData.put("Id", "4");
    entryData.put("Kind", "Holding4");
    entryData.put("NGO", null);
    Map<String, Object> locationData2 = new HashMap<String, Object>();
    Map<String, Object> cityData2 = new HashMap<String, Object>();
    cityData2.put("PostalCode", "code4");
    cityData2.put("CityName", null);
    locationData2.put("City", cityData2);
    locationData2.put("Country", null);

    entryData.put("Location", locationData2);
    feedData.add(entryData);

    entryData = new HashMap<String, Object>();
    entryData.put("Id", "5");
    entryData.put("Name", "Company5");
    entryData.put("Kind", "Holding5");
    entryData.put("NGO", true);
    Map<String, Object> locationData3 = new HashMap<String, Object>();
    Map<String, Object> cityData3 = new HashMap<String, Object>();
    cityData3.put("PostalCode", "code5");
    cityData3.put("CityName", "city5");
    locationData3.put("City", cityData3);
    locationData3.put("Country", "country5");

    entryData.put("Location", locationData3);
    feedData.add(entryData);

    return feedData;
  }
  
  private List<Map<String, Object>> createDataWithInvalidProperty(boolean includeKeys) {
    List<Map<String, Object>> feedData = new ArrayList<Map<String, Object>>();
    Map<String, Object> entryData = new HashMap<String, Object>();
    entryData.put("Id", "1");
    entryData.put("Address", "1");
    feedData.add(entryData);

    entryData = new HashMap<String, Object>();
    entryData.put("Id", "2");
    entryData.put("Name", "Company2");
    entryData.put("Location", null);
    feedData.add(entryData);

    return feedData;
  }
  
  private List<Map<String, Object>> createDataWithKeyNull(boolean includeKeys) {
    List<Map<String, Object>> feedData = new ArrayList<Map<String, Object>>();
    Map<String, Object> entryData = new HashMap<String, Object>();
    entryData.put("Id", null);
    feedData.add(entryData);

    entryData = new HashMap<String, Object>();
    entryData.put("Id", null);
    entryData.put("Name", "Company2");
    entryData.put("Location", null);
    feedData.add(entryData);

    return feedData;
  }
  
  private List<Map<String, Object>> createDataWithoutKey(boolean includeKeys) {
    List<Map<String, Object>> feedData = new ArrayList<Map<String, Object>>();
    Map<String, Object> entryData = new HashMap<String, Object>();
    entryData.put("Id", "1");
    feedData.add(entryData);

    entryData = new HashMap<String, Object>();
    entryData.put("Name", "Company2");
    entryData.put("Location", null);
    feedData.add(entryData);

    entryData = new HashMap<String, Object>();
    entryData.put("Kind", "Holding4");
    entryData.put("NGO", null);
    Map<String, Object> locationData2 = new HashMap<String, Object>();
    Map<String, Object> cityData2 = new HashMap<String, Object>();
    cityData2.put("PostalCode", "code4");
    cityData2.put("CityName", null);
    locationData2.put("City", cityData2);
    locationData2.put("Country", null);

    entryData.put("Location", locationData2);
    feedData.add(entryData);

    return feedData;
  }
}
