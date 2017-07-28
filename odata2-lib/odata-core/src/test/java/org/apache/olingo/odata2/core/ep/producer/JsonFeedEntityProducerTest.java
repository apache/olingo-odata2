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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.JsonEntityProvider;
import org.apache.olingo.odata2.core.ep.consumer.JsonEntityConsumer;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *  
 */
public class JsonFeedEntityProducerTest extends BaseTest {
  protected static final String BASE_URI = "http://host:80/service/";
  protected static final EntityProviderWriteProperties DEFAULT_PROPERTIES =
      EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).build();

  @Test
  public void feed() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    Map<String, Object> team1Data = new HashMap<String, Object>();
    team1Data.put("Id", "1");
    team1Data.put("isScrumTeam", true);
    Map<String, Object> team2Data = new HashMap<String, Object>();
    team2Data.put("Id", "2");
    team2Data.put("isScrumTeam", false);
    List<Map<String, Object>> teamsData = new ArrayList<Map<String, Object>>();
    teamsData.add(team1Data);
    teamsData.add(team2Data);

    final ODataResponse response = new JsonEntityProvider().writeFeed(entitySet, teamsData, DEFAULT_PROPERTIES);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"results\":[{\"__metadata\":{\"id\":\"" + BASE_URI + "Teams('1')\","
        + "\"uri\":\"" + BASE_URI + "Teams('1')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"1\",\"Name\":null,\"isScrumTeam\":true,"
        + "\"nt_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Teams('1')/nt_Employees\"}}},"
        + "{\"__metadata\":{\"id\":\"" + BASE_URI + "Teams('2')\","
        + "\"uri\":\"" + BASE_URI + "Teams('2')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"2\",\"Name\":null,\"isScrumTeam\":false,"
        + "\"nt_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Teams('2')/nt_Employees\"}}}]}}",
        json);
  }

  @Test
  public void omitJsonWrapperMustHaveNoEffect() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    Map<String, Object> team1Data = new HashMap<String, Object>();
    team1Data.put("Id", "1");
    team1Data.put("isScrumTeam", true);
    Map<String, Object> team2Data = new HashMap<String, Object>();
    team2Data.put("Id", "2");
    team2Data.put("isScrumTeam", false);
    List<Map<String, Object>> teamsData = new ArrayList<Map<String, Object>>();
    teamsData.add(team1Data);
    teamsData.add(team2Data);

    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).omitJsonWrapper(true).build();
    final ODataResponse response = new JsonEntityProvider().writeFeed(entitySet, teamsData, properties);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"results\":[{\"__metadata\":{\"id\":\"" + BASE_URI + "Teams('1')\","
        + "\"uri\":\"" + BASE_URI + "Teams('1')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"1\",\"Name\":null,\"isScrumTeam\":true,"
        + "\"nt_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Teams('1')/nt_Employees\"}}},"
        + "{\"__metadata\":{\"id\":\"" + BASE_URI + "Teams('2')\","
        + "\"uri\":\"" + BASE_URI + "Teams('2')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"2\",\"Name\":null,\"isScrumTeam\":false,"
        + "\"nt_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Teams('2')/nt_Employees\"}}}]}}",
        json);
  }
  
  @Test
  public void clientFlagMustNotHaveAnEffect() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    Map<String, Object> team1Data = new HashMap<String, Object>();
    team1Data.put("Id", "1");
    team1Data.put("isScrumTeam", true);
    Map<String, Object> team2Data = new HashMap<String, Object>();
    team2Data.put("Id", "2");
    team2Data.put("isScrumTeam", false);
    List<Map<String, Object>> teamsData = new ArrayList<Map<String, Object>>();
    teamsData.add(team1Data);
    teamsData.add(team2Data);

    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).responsePayload(true).build();
    final ODataResponse response = new JsonEntityProvider().writeFeed(entitySet, teamsData, properties);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"results\":[{\"__metadata\":{\"id\":\"" + BASE_URI + "Teams('1')\","
        + "\"uri\":\"" + BASE_URI + "Teams('1')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"1\",\"Name\":null,\"isScrumTeam\":true,"
        + "\"nt_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Teams('1')/nt_Employees\"}}},"
        + "{\"__metadata\":{\"id\":\"" + BASE_URI + "Teams('2')\","
        + "\"uri\":\"" + BASE_URI + "Teams('2')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"2\",\"Name\":null,\"isScrumTeam\":false,"
        + "\"nt_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Teams('2')/nt_Employees\"}}}]}}",
        json);
  }

  @Test
  public void inlineCount() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    final ODataResponse response = new JsonEntityProvider().writeFeed(entitySet, new ArrayList<Map<String, Object>>(),
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI))
            .inlineCountType(InlineCount.ALLPAGES).inlineCount(42)
            .build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"__count\":\"42\",\"results\":[]}}", json);
  }

  @Test
  public void nextLink() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Seats", 123);
    roomData.put("Version", 1);
    List<Map<String, Object>> roomsData = new ArrayList<Map<String, Object>>();
    roomsData.add(roomData);

    final ODataResponse response = new JsonEntityProvider().writeFeed(entitySet, roomsData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).nextLink("Rooms?$skiptoken=2").build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"results\":[{\"__metadata\":{\"id\":\"" + BASE_URI + "Rooms('1')\","
        + "\"uri\":\"" + BASE_URI + "Rooms('1')\",\"type\":\"RefScenario.Room\",\"etag\":\"W/\\\"1\\\"\"},"
        + "\"Id\":\"1\",\"Name\":null,\"Seats\":123,\"Version\":1,"
        + "\"nr_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Rooms('1')/nr_Employees\"}},"
        + "\"nr_Building\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Rooms('1')/nr_Building\"}}}],"
        + "\"__next\":\"Rooms?$skiptoken=2\"}}",
        json);
  }
  
  @Test
  public void unbalancedPropertyFeed() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Companys");
    List<Map<String, Object>> originalData = createData(true);
    final ODataResponse response = new JsonEntityProvider().writeFeed(entitySet, originalData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).isDataBasedPropertySerialization(true).build());

    EntityProviderReadProperties readProperties = EntityProviderReadProperties.init().mergeSemantic(false).build();
    JsonEntityConsumer consumer = new JsonEntityConsumer();
    ODataFeed feed = consumer.readFeed(entitySet, (InputStream) response.getEntity(), readProperties);

    compareList(originalData, feed.getEntries());
  }
  
  @Test
  public void unbalancedPropertyFeedWithInvalidProperty() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Companys");
    List<Map<String, Object>> originalData = createDataWithInvalidProperty(true);
    final ODataResponse response = new JsonEntityProvider().writeFeed(entitySet, originalData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).isDataBasedPropertySerialization(true).build());

    EntityProviderReadProperties readProperties = EntityProviderReadProperties.init().mergeSemantic(false).build();
    JsonEntityConsumer consumer = new JsonEntityConsumer();
    ODataFeed feed = consumer.readFeed(entitySet, (InputStream) response.getEntity(), readProperties);
    originalData.get(0).remove("Address");
    compareList(originalData, feed.getEntries());
  }
  
  @Test(expected = EntityProviderProducerException.class)
  public void unbalancedPropertyFeedWithNullKey() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Companys");
    List<Map<String, Object>> originalData = createDataWithKeyNull(true);
    new JsonEntityProvider().writeFeed(entitySet, originalData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).isDataBasedPropertySerialization(true).build());
  }

  @Test(expected = EntityProviderProducerException.class)
  public void unbalancedPropertyFeedWithoutKeys() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Companys");
    List<Map<String, Object>> originalData = createDataWithoutKey(true);
    new JsonEntityProvider().writeFeed(entitySet, originalData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).isDataBasedPropertySerialization(true).build());
  }
  
  @Test(expected = EntityProviderProducerException.class)
  public void unbalancedPropertyFeedWithEmptyData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Companys");
    List<Map<String, Object>> feedData = new ArrayList<Map<String, Object>>();
    Map<String, Object> entryData = new HashMap<String, Object>();
    feedData.add(entryData);
    new JsonEntityProvider().writeFeed(entitySet, feedData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).isDataBasedPropertySerialization(true).build());
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
    
    final ODataResponse response = new JsonEntityProvider().writeFeed(entitySet, originalData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(select).
        isDataBasedPropertySerialization(true).build());

    EntityProviderReadProperties readProperties = EntityProviderReadProperties.init().mergeSemantic(false).build();
    JsonEntityConsumer consumer = new JsonEntityConsumer();
    ODataFeed feed = consumer.readFeed(entitySet, (InputStream) response.getEntity(), readProperties);

    compareList(originalData, feed.getEntries());
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
        if ("Location".equals(key) || "City".equals(key)) {
          assertTrue("Entry " + index + " null complex value should result in map", 
              actual.get(key) instanceof Map<?, ?>);
          assertEquals("Entry " + index + " null complex value should result in empty map", 
              0, ((Map<String, Object>) actual.get(key)).size());
        } else {
          assertEquals("Entry: " + index + " values are not the same: " + key, entry.getValue(), actual.get(key));
        }
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
  
  @Test
  public void unbalancedPropertyEntryWithInlineFeed() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Name", "Neu Schwanstein");
    roomData.put("Seats", new Integer(20));

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nr_Employees", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

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
        result.setInlineProperties(context.getCurrentWriteProperties());
        return result;
      }
    }

    EntryCallback callback = new EntryCallback();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Employees", callback);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, roomData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).isDataBasedPropertySerialization(true).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\""+BASE_URI+"Rooms('1')\",\"uri\":\""+BASE_URI+"Rooms('1')\","
        + "\"type\":\"RefScenario.Room\"},\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,\"nr_Employees\":"
        + "{\"results\":[{\"__metadata\":{\"id\":\""+BASE_URI+"Employees('1')\",\"uri\":\""+BASE_URI+"Employees('1')\","
        + "\"type\":\"RefScenario.Employee\",\"content_type\":\"application/octet-stream\",\"media_src\":\""
        + BASE_URI+"Employees('1')/$value\",\"edit_media\":\""+BASE_URI+"Employees('1')/$value\"},\"EmployeeId\":\"1\","
        + "\"EmployeeName\":\"EmpName1\",\"RoomId\":\"1\"},{\"__metadata\":{\"id\":\""+BASE_URI+"Employees('1')\","
        + "\"uri\":\""+BASE_URI+"Employees('1')\","
        + "\"type\":\"RefScenario.Employee\",\"content_type\":\"application/octet-stream\",\"media_src\":\""
        +BASE_URI+"Employees('1')/$value\",\"edit_media\":\""+BASE_URI+"Employees('1')/$value\"},\"EmployeeId\":\"1\","
        + "\"RoomId\":\"1\"}]}}}", json);
  }
}
