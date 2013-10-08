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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteEntryContent;
import org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackResult;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.core.ep.JsonEntityProvider;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

/**
 *  
 */
public class JsonEntryEntityProducerTest extends BaseTest {
  protected static final String BASE_URI = "http://host:80/service/";
  protected static final EntityProviderWriteProperties DEFAULT_PROPERTIES =
      EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).build();

  @Test
  public void entry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    Map<String, Object> teamData = new HashMap<String, Object>();
    teamData.put("Id", "1");
    teamData.put("isScrumTeam", true);

    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, teamData, DEFAULT_PROPERTIES);
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"" + BASE_URI + "Teams('1')\","
        + "\"uri\":\"" + BASE_URI + "Teams('1')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"1\",\"Name\":null,\"isScrumTeam\":true,"
        + "\"nt_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Teams('1')/nt_Employees\"}}}}",
        json);
  }

  @Test(expected = EntityProviderException.class)
  public void entryWithNullData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");

    new JsonEntityProvider().writeEntry(entitySet, null, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void entryWithEmptyData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");

    new JsonEntityProvider().writeEntry(entitySet, new HashMap<String, Object>(), DEFAULT_PROPERTIES);
  }

  @Test
  public void entryWithSelect() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nb_Rooms", null);
    Mockito.when(node.getLinks()).thenReturn(links);

    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, buildingData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node).build());
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"" + BASE_URI + "Buildings('1')\","
        + "\"uri\":\"" + BASE_URI + "Buildings('1')\",\"type\":\"RefScenario.Building\"},"
        + "\"nb_Rooms\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Buildings('1')/nb_Rooms\"}}}}",
        json);
  }

  @Test
  public void mediaLinkEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");
    Map<String, Object> photoData = new HashMap<String, Object>();
    photoData.put("Id", 1);
    photoData.put("Type", "image/png");
    photoData.put("BinaryData", new byte[] { -1, 0, 1, 2 });

    EntityProviderWriteProperties writeProperties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).mediaResourceTypeKey("Type").build();

    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, photoData, writeProperties);
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{"
        + "\"id\":\"" + BASE_URI + "Container2.Photos(Id=1,Type='image%2Fpng')\","
        + "\"uri\":\"" + BASE_URI + "Container2.Photos(Id=1,Type='image%2Fpng')\","
        + "\"type\":\"RefScenario2.Photo\",\"etag\":\"W/\\\"1\\\"\",\"content_type\":\"image/png\","
        + "\"media_src\":\"Container2.Photos(Id=1,Type='image%2Fpng')/$value\","
        + "\"edit_media\":\"" + BASE_URI + "Container2.Photos(Id=1,Type='image%2Fpng')/$value\"},"
        + "\"Id\":1,\"Name\":null,\"Type\":\"image/png\",\"Image\":null,"
        + "\"BinaryData\":\"/wABAg==\",\"Содержание\":null,\"CustomProperty\":null}}",
        json);
  }

  @Test
  public void mediaLinkEntryWithSelect() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    Map<String, Object> employeeData = new HashMap<String, Object>();
    employeeData.put("EmployeeId", "1");
    employeeData.put("EntryDate", 0L);
    employeeData.put("getImageType", "image/jpeg");

    final EdmProperty property = (EdmProperty) entitySet.getEntityType().getProperty("EntryDate");
    ExpandSelectTreeNode node = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node.getProperties()).thenReturn(Arrays.asList(property));

    EntityProviderWriteProperties writeProperties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).mediaResourceTypeKey("getImageType")
            .serviceRoot(URI.create(BASE_URI)).expandSelectTree(node).build();

    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, employeeData,
        writeProperties);
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{"
        + "\"id\":\"" + BASE_URI + "Employees('1')\","
        + "\"uri\":\"" + BASE_URI + "Employees('1')\","
        + "\"type\":\"RefScenario.Employee\",\"content_type\":\"image/jpeg\","
        + "\"media_src\":\"Employees('1')/$value\","
        + "\"edit_media\":\"" + BASE_URI + "Employees('1')/$value\"},"
        + "\"EntryDate\":\"\\/Date(0)\\/\"}}",
        json);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedEntryButNullData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nr_Building", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    class EntryCallback implements OnWriteEntryContent {
      @Override
      public WriteEntryCallbackResult retrieveEntryResult(final WriteEntryCallbackContext context)
          throws ODataApplicationException {
        WriteEntryCallbackResult result = new WriteEntryCallbackResult();
        result.setEntryData(null);
        result.setInlineProperties(DEFAULT_PROPERTIES);
        return result;
      }
    }
    EntryCallback callback = new EntryCallback();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", callback);

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, roomData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> roomEntry =
        new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    // remove d wrapper
    roomEntry = (Map<String, Object>) roomEntry.get("d");
    assertEquals(2, roomEntry.size());
    assertTrue(roomEntry.containsKey("nr_Building"));
    assertNull(roomEntry.get("nr_Building"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedEntryButEmptyData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nr_Building", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    class EntryCallback implements OnWriteEntryContent {
      @Override
      public WriteEntryCallbackResult retrieveEntryResult(final WriteEntryCallbackContext context)
          throws ODataApplicationException {
        WriteEntryCallbackResult result = new WriteEntryCallbackResult();
        result.setEntryData(new HashMap<String, Object>());
        result.setInlineProperties(DEFAULT_PROPERTIES);
        return result;
      }
    }
    EntryCallback callback = new EntryCallback();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", callback);

    ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, roomData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> roomEntry =
        new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    // remove d wrapper
    roomEntry = (Map<String, Object>) roomEntry.get("d");
    assertEquals(2, roomEntry.size());
    assertTrue(roomEntry.containsKey("nr_Building"));
    assertNull(roomEntry.get("nr_Building"));
  }

  @Test
  public void entryWithExpandedEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nr_Building", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    class EntryCallback implements OnWriteEntryContent {
      @Override
      public WriteEntryCallbackResult retrieveEntryResult(final WriteEntryCallbackContext context)
          throws ODataApplicationException {
        Map<String, Object> buildingData = new HashMap<String, Object>();
        buildingData.put("Id", "1");
        WriteEntryCallbackResult result = new WriteEntryCallbackResult();
        result.setEntryData(buildingData);
        result.setInlineProperties(DEFAULT_PROPERTIES);
        return result;
      }
    }
    EntryCallback callback = new EntryCallback();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", callback);

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, roomData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).build());
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"" + BASE_URI + "Rooms('1')\","
        + "\"uri\":\"" + BASE_URI + "Rooms('1')\",\"type\":\"RefScenario.Room\",\"etag\":\"W/\\\"1\\\"\"},"
        + "\"nr_Building\":{\"__metadata\":{\"id\":\"" + BASE_URI + "Buildings('1')\","
        + "\"uri\":\"" + BASE_URI + "Buildings('1')\",\"type\":\"RefScenario.Building\"},"
        + "\"Id\":\"1\",\"Name\":null,\"Image\":null,"
        + "\"nb_Rooms\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Buildings('1')/nb_Rooms\"}}}}}",
        json);
  }

  @Test
  public void entryWithExpandedEntryButNoRegisteredCallback() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nr_Building", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, roomData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1).build());
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"" + BASE_URI + "Rooms('1')\","
        + "\"uri\":\"" + BASE_URI + "Rooms('1')\",\"type\":\"RefScenario.Room\",\"etag\":\"W/\\\"1\\\"\"},"
        + "\"nr_Building\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Rooms('1')/nr_Building\"}}}}",
        json);
  }

  @Test(expected = EntityProviderException.class)
  public void entryWithExpandedEntryWithRegisteredNullCallback() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nr_Building", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", null);

    new JsonEntityProvider().writeEntry(entitySet, roomData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1).callbacks(callbacks)
            .build());

  }

  @Test
  public void entryWithExpandedFeed() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nb_Rooms", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    class FeedCallback implements OnWriteFeedContent {
      @Override
      public WriteFeedCallbackResult retrieveFeedResult(final WriteFeedCallbackContext context)
          throws ODataApplicationException {
        Map<String, Object> roomData = new HashMap<String, Object>();
        roomData.put("Id", "1");
        roomData.put("Version", 1);
        List<Map<String, Object>> roomsData = new ArrayList<Map<String, Object>>();
        roomsData.add(roomData);
        WriteFeedCallbackResult result = new WriteFeedCallbackResult();
        result.setFeedData(roomsData);
        result.setInlineProperties(DEFAULT_PROPERTIES);
        return result;
      }
    }
    FeedCallback callback = new FeedCallback();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", callback);

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).build());
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"" + BASE_URI + "Buildings('1')\","
        + "\"uri\":\"" + BASE_URI + "Buildings('1')\",\"type\":\"RefScenario.Building\"},"
        + "\"nb_Rooms\":{\"results\":[{\"__metadata\":{\"id\":\"" + BASE_URI + "Rooms('1')\","
        + "\"uri\":\"" + BASE_URI + "Rooms('1')\",\"type\":\"RefScenario.Room\",\"etag\":\"W/\\\"1\\\"\"},"
        + "\"Id\":\"1\",\"Name\":null,\"Seats\":null,\"Version\":1,"
        + "\"nr_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Rooms('1')/nr_Employees\"}},"
        + "\"nr_Building\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Rooms('1')/nr_Building\"}}}]}}}",
        json);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedFeedButNullData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nb_Rooms", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    class FeedCallback implements OnWriteFeedContent {
      @Override
      public WriteFeedCallbackResult retrieveFeedResult(final WriteFeedCallbackContext context)
          throws ODataApplicationException {
        WriteFeedCallbackResult result = new WriteFeedCallbackResult();
        result.setFeedData(null);
        result.setInlineProperties(DEFAULT_PROPERTIES);
        return result;
      }
    }
    FeedCallback callback = new FeedCallback();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", callback);

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> buildingEntry =
        new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    // remove d wrapper
    buildingEntry = (Map<String, Object>) buildingEntry.get("d");
    assertEquals(2, buildingEntry.size());
    assertTrue(buildingEntry.containsKey("nb_Rooms"));
    Map<String, Object> roomsFeed = (Map<String, Object>) buildingEntry.get("nb_Rooms");
    assertNotNull(roomsFeed);
    List<Object> roomsFeedEntries = (List<Object>) roomsFeed.get("results");
    assertEquals(0, roomsFeedEntries.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedFeedButEmptyData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nb_Rooms", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    class FeedCallback implements OnWriteFeedContent {
      @Override
      public WriteFeedCallbackResult retrieveFeedResult(final WriteFeedCallbackContext context)
          throws ODataApplicationException {
        WriteFeedCallbackResult result = new WriteFeedCallbackResult();
        result.setFeedData(new ArrayList<Map<String, Object>>());
        result.setInlineProperties(DEFAULT_PROPERTIES);
        return result;
      }
    }
    FeedCallback callback = new FeedCallback();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", callback);

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> buildingEntry =
        new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    // remove d wrapper
    buildingEntry = (Map<String, Object>) buildingEntry.get("d");
    assertEquals(2, buildingEntry.size());
    assertTrue(buildingEntry.containsKey("nb_Rooms"));
    Map<String, Object> roomsFeed = (Map<String, Object>) buildingEntry.get("nb_Rooms");
    assertNotNull(roomsFeed);
    List<Object> roomsFeedEntries = (List<Object>) roomsFeed.get("results");
    assertEquals(0, roomsFeedEntries.size());
  }

  @Test
  public void entryWithExpandedFeedButNoRegisteredCallback() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nb_Rooms", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, buildingData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1).build());
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"" + BASE_URI + "Buildings('1')\","
        + "\"uri\":\"" + BASE_URI + "Buildings('1')\",\"type\":\"RefScenario.Building\"},"
        + "\"nb_Rooms\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Buildings('1')/nb_Rooms\"}}}}",
        json);
  }

  @Test(expected = EntityProviderException.class)
  public void entryWithExpandedFeedWithRegisteredNullCallback() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nb_Rooms", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", null);

    new JsonEntityProvider().writeEntry(entitySet, buildingData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1).callbacks(callbacks)
            .build());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void serializeWithCustomSrcAttributeOnEmployee() throws Exception {
    Map<String, Object> employeeData = new HashMap<String, Object>();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    employeeData.put("EmployeeId", "1");
    employeeData.put("ImmageUrl", null);
    employeeData.put("ManagerId", "1");
    employeeData.put("Age", new Integer(52));
    employeeData.put("RoomId", "1");
    employeeData.put("EntryDate", date);
    employeeData.put("TeamId", "42");
    employeeData.put("EmployeeName", "Walter Winter");

    Map<String, Object> locationData = new HashMap<String, Object>();
    Map<String, Object> cityData = new HashMap<String, Object>();
    cityData.put("PostalCode", "33470");
    cityData.put("CityName", "Duckburg");
    locationData.put("City", cityData);
    locationData.put("Country", "Calisota");

    employeeData.put("Location", locationData);

    String mediaResourceSourceKey = "~src";
    employeeData.put(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    EntityProviderWriteProperties localProperties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).mediaResourceSourceKey(mediaResourceSourceKey
            ).build();
    ODataResponse response =
        new JsonEntityProvider().writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet(
            "Employees"),
            employeeData,
            localProperties);
    String jsonString = verifyResponse(response);
    Gson gson = new Gson();
    StringMap<Object> jsonMap = gson.fromJson(jsonString, StringMap.class);
    jsonMap = (StringMap<Object>) jsonMap.get("d");
    jsonMap = (StringMap<Object>) jsonMap.get("__metadata");

    assertEquals("http://localhost:8080/images/image1", jsonMap.get("media_src"));
    assertEquals("application/octet-stream", jsonMap.get("content_type"));
    assertEquals("http://host:80/service/Employees('1')/$value", jsonMap.get("edit_media"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void serializeWithCustomSrcAndTypeAttributeOnEmployee() throws Exception {
    Map<String, Object> employeeData = new HashMap<String, Object>();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    employeeData.put("EmployeeId", "1");
    employeeData.put("ImmageUrl", null);
    employeeData.put("ManagerId", "1");
    employeeData.put("Age", new Integer(52));
    employeeData.put("RoomId", "1");
    employeeData.put("EntryDate", date);
    employeeData.put("TeamId", "42");
    employeeData.put("EmployeeName", "Walter Winter");

    Map<String, Object> locationData = new HashMap<String, Object>();
    Map<String, Object> cityData = new HashMap<String, Object>();
    cityData.put("PostalCode", "33470");
    cityData.put("CityName", "Duckburg");
    locationData.put("City", cityData);
    locationData.put("Country", "Calisota");

    employeeData.put("Location", locationData);
    String mediaResourceSourceKey = "~src";
    employeeData.put(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    String mediaResourceTypeKey = "~type";
    employeeData.put(mediaResourceTypeKey, "image/jpeg");
    EntityProviderWriteProperties localProperties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).mediaResourceSourceKey(mediaResourceSourceKey
            ).mediaResourceTypeKey(mediaResourceTypeKey).build();
    ODataResponse response =
        new JsonEntityProvider().writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet(
            "Employees"),
            employeeData,
            localProperties);
    String jsonString = verifyResponse(response);

    Gson gson = new Gson();
    StringMap<Object> jsonMap = gson.fromJson(jsonString, StringMap.class);
    jsonMap = (StringMap<Object>) jsonMap.get("d");
    jsonMap = (StringMap<Object>) jsonMap.get("__metadata");

    assertEquals("http://localhost:8080/images/image1", jsonMap.get("media_src"));
    assertEquals("image/jpeg", jsonMap.get("content_type"));
    assertEquals("http://host:80/service/Employees('1')/$value", jsonMap.get("edit_media"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void serializeWithCustomSrcAttributeOnRoom() throws Exception {
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Name", "Neu Schwanstein");
    roomData.put("Seats", new Integer(20));
    roomData.put("Version", new Integer(3));

    String mediaResourceSourceKey = "~src";
    roomData.put(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    EntityProviderWriteProperties localProperties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).mediaResourceSourceKey(mediaResourceSourceKey
            ).build();
    ODataResponse response =
        new JsonEntityProvider().writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"),
            roomData,
            localProperties);
    String jsonString = verifyResponse(response);
    Gson gson = new Gson();
    StringMap<Object> jsonMap = gson.fromJson(jsonString, StringMap.class);
    jsonMap = (StringMap<Object>) jsonMap.get("d");
    jsonMap = (StringMap<Object>) jsonMap.get("__metadata");

    assertNull(jsonMap.get("media_src"));
    assertNull(jsonMap.get("content_type"));
    assertNull(jsonMap.get("edit_media"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void serializeWithCustomSrcAndTypeAttributeOnRoom() throws Exception {
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Name", "Neu Schwanstein");
    roomData.put("Seats", new Integer(20));
    roomData.put("Version", new Integer(3));

    String mediaResourceSourceKey = "~src";
    roomData.put(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    String mediaResourceTypeKey = "~type";
    roomData.put(mediaResourceTypeKey, "image/jpeg");
    EntityProviderWriteProperties localProperties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).mediaResourceSourceKey(mediaResourceSourceKey
            ).mediaResourceTypeKey(mediaResourceTypeKey).build();
    ODataResponse response =
        new JsonEntityProvider().writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"),
            roomData,
            localProperties);
    String jsonString = verifyResponse(response);
    Gson gson = new Gson();
    StringMap<Object> jsonMap = gson.fromJson(jsonString, StringMap.class);
    jsonMap = (StringMap<Object>) jsonMap.get("d");
    jsonMap = (StringMap<Object>) jsonMap.get("__metadata");

    assertNull(jsonMap.get("media_src"));
    assertNull(jsonMap.get("content_type"));
    assertNull(jsonMap.get("edit_media"));
  }

  private String verifyResponse(final ODataResponse response) throws IOException {
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    return json;
  }
}
