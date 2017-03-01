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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmConcurrencyMode;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmTyped;
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
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.JsonEntityProvider;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

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

  @SuppressWarnings("unchecked")
  @Test
  public void omitETagTestPropertyPresent() throws Exception {
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).omitETag(true).omitJsonWrapper(true).build();

    Map<String, Object> localRoomData = new HashMap<String, Object>();
    localRoomData.put("Id", "1");
    localRoomData.put("Name", "Neu Schwanstein");
    localRoomData.put("Seats", new Integer(20));
    localRoomData.put("Version", new Integer(3));
    final ODataResponse response =
        new JsonEntityProvider().writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"),
            localRoomData, properties);
    Map<String, Object> room =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);

    room = (Map<String, Object>) room.get("__metadata");
    assertFalse(room.containsKey("etag"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void omitETagTestPropertyNOTPresentMustNotResultInException() throws Exception {
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).omitETag(true).omitJsonWrapper(true).build();

    Map<String, Object> localRoomData = new HashMap<String, Object>();
    localRoomData.put("Id", "1");
    localRoomData.put("Name", "Neu Schwanstein");
    localRoomData.put("Seats", new Integer(20));
    final ODataResponse response =
        new JsonEntityProvider().writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"),
            localRoomData, properties);
    Map<String, Object> room =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);

    room = (Map<String, Object>) room.get("__metadata");
    assertFalse(room.containsKey("etag"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void omitETagTestNonNullablePropertyNOTPresentMustNotResultInException() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    EdmProperty versionProperty = (EdmProperty) entitySet.getEntityType().getProperty("Version");
    EdmFacets facets = versionProperty.getFacets();
    when(facets.isNullable()).thenReturn(new Boolean(false));
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("Id");
    selectedPropertyNames.add("Name");
    selectedPropertyNames.add("Seats");
    ExpandSelectTreeNode selectNode =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedPropertyNames).build();
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).omitETag(true).omitJsonWrapper(true)
            .expandSelectTree(selectNode).build();
    Map<String, Object> localRoomData = new HashMap<String, Object>();
    localRoomData.put("Id", "1");
    localRoomData.put("Name", "Neu Schwanstein");
    localRoomData.put("Seats", new Integer(20));
    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, localRoomData, properties);
    Map<String, Object> room =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);

    room = (Map<String, Object>) room.get("__metadata");
    assertFalse(room.containsKey("etag"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void includeMetadataWithoutContentOnlyMustMakeNoDifference() throws Exception {
    HashMap<String, Object> employeeData = new HashMap<String, Object>();
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

    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).omitJsonWrapper(true)
            .includeMetadataInContentOnly(true).build();
    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, employeeData, properties);
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNotNull(employee.get("__metadata"));
    assertNotNull(employee.get("ne_Manager"));
    assertNotNull(employee.get("ne_Team"));
    assertNotNull(employee.get("ne_Room"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void contentOnlyWithMetadata() throws Exception {
    HashMap<String, Object> employeeData = new HashMap<String, Object>();
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

    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).omitJsonWrapper(true).contentOnly(true)
            .includeMetadataInContentOnly(true).build();
    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, employeeData, properties);
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNotNull(employee.get("__metadata"));
    assertNull(employee.get("ne_Manager"));
    assertNull(employee.get("ne_Team"));
    assertNull(employee.get("ne_Room"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void contentOnly() throws Exception {
    HashMap<String, Object> employeeData = new HashMap<String, Object>();
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

    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).omitJsonWrapper(true).contentOnly(true)
            .build();
    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, employeeData, properties);
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNull(employee.get("__metadata"));
    assertNull(employee.get("ne_Manager"));
    assertNull(employee.get("ne_Team"));
    assertNull(employee.get("ne_Room"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void contentOnlyWithoutKey() throws Exception {
    HashMap<String, Object> employeeData = new HashMap<String, Object>();
    employeeData.put("ManagerId", "1");
    employeeData.put("Age", new Integer(52));
    employeeData.put("RoomId", "1");
    employeeData.put("TeamId", "42");

    List<String> selectedProperties = new ArrayList<String>();
    selectedProperties.add("ManagerId");
    selectedProperties.add("Age");
    selectedProperties.add("RoomId");
    selectedProperties.add("TeamId");
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");

    ExpandSelectTreeNode expandSelectTreeNode =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedProperties).build();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).omitJsonWrapper(true).contentOnly(true)
            .expandSelectTree(expandSelectTreeNode).build();
    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, employeeData, properties);
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNull(employee.get("__metadata"));
    assertNull(employee.get("ne_Manager"));
    assertNull(employee.get("ne_Team"));
    assertNull(employee.get("ne_Room"));

    assertEquals("1", employee.get("ManagerId"));
    assertEquals("1", employee.get("RoomId"));
    assertEquals("42", employee.get("TeamId"));
    assertEquals(new Double(52), employee.get("Age"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void contentOnlySelectedOrExpandedLinksMustBeIgnored() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");

    HashMap<String, Object> employeeData = new HashMap<String, Object>();
    employeeData.put("ManagerId", "1");

    List<String> selectedProperties = new ArrayList<String>();
    selectedProperties.add("ManagerId");

    List<String> expandedLinks = new ArrayList<String>();
    expandedLinks.add("ne_Manager");
    expandedLinks.add("ne_Team");
    expandedLinks.add("ne_Room");

    ExpandSelectTreeNode expandSelectTreeNode =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedProperties).expandedLinks(expandedLinks)
            .build();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).omitJsonWrapper(true).contentOnly(true)
            .expandSelectTree(expandSelectTreeNode).build();
    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, employeeData, properties);
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNull(employee.get("__metadata"));
    assertNull(employee.get("ne_Manager"));
    assertNull(employee.get("ne_Team"));
    assertNull(employee.get("ne_Room"));

    assertEquals("1", employee.get("ManagerId"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void contentOnlyWithAdditinalLink() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    HashMap<String, Object> employeeData = new HashMap<String, Object>();
    employeeData.put("ManagerId", "1");

    List<String> selectedProperties = new ArrayList<String>();
    selectedProperties.add("ManagerId");

    ExpandSelectTreeNode expandSelectTreeNode =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedProperties).build();

    Map<String, Map<String, Object>> additinalLinks = new HashMap<String, Map<String, Object>>();
    Map<String, Object> managerLink = new HashMap<String, Object>();
    managerLink.put("EmployeeId", "1");
    additinalLinks.put("ne_Manager", managerLink);

    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).omitJsonWrapper(true).contentOnly(true)
            .expandSelectTree(expandSelectTreeNode).additionalLinks(additinalLinks).build();
    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, employeeData, properties);
    // System.out.println(StringHelper.inputStreamToString((InputStream) response.getEntity()));
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNull(employee.get("__metadata"));
    assertNull(employee.get("ne_Team"));
    assertNull(employee.get("ne_Room"));

    assertEquals("1", employee.get("ManagerId"));
    Map<String, Object> map = (Map<String, Object>) employee.get("ne_Manager");
    map = (Map<String, Object>) map.get("__deferred");
    assertEquals("http://host:80/service/Managers('1')", map.get("uri"));
  }

  @Test
  public void omitJsonWrapper() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    Map<String, Object> teamData = new HashMap<String, Object>();
    teamData.put("Id", "1");
    teamData.put("isScrumTeam", true);

    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).omitJsonWrapper(true).build();
    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, teamData, properties);
    final String json = verifyResponse(response);
    assertEquals("{\"__metadata\":{\"id\":\"" + BASE_URI + "Teams('1')\","
        + "\"uri\":\"" + BASE_URI + "Teams('1')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"1\",\"Name\":null,\"isScrumTeam\":true,"
        + "\"nt_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Teams('1')/nt_Employees\"}}}",
        json);
  }

  @Test(expected = EdmSimpleTypeException.class)
  public void serializeWithFacetsValidation() throws Throwable {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped roomNameProperty = edm.getEntityType("RefScenario", "Room").getProperty("Name");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(3);
    when(((EdmProperty) roomNameProperty).getFacets()).thenReturn(facets);
    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");

    String name = "1234567";
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "4711");
    roomData.put("Name", name);
    EntityProviderWriteProperties properties = EntityProviderWriteProperties
        .fromProperties(DEFAULT_PROPERTIES).validatingFacets(true).build();
    try {
      final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, roomData, properties);
      final String json = verifyResponse(response);
      assertNotNull(response);
      assertEquals("{\"__metadata\":{\"id\":\"" + BASE_URI + "Teams('1')\","
          + "\"uri\":\"" + BASE_URI + "Teams('1')\",\"type\":\"RefScenario.Team\"},"
          + "\"Id\":\"1\",\"Name\":null,\"isScrumTeam\":true,"
          + "\"nt_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Teams('1')/nt_Employees\"}}}",
          json);
    } catch (EntityProviderException e) {
      throw e.getCause();
    }
  }

  @Test
  public void serializeWithoutFacetsValidation() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped roomNameProperty = edm.getEntityType("RefScenario", "Room").getProperty("Name");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(3);
    when(((EdmProperty) roomNameProperty).getFacets()).thenReturn(facets);
    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");

    String name = "1234567890";
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "4711");
    roomData.put("Name", name);
    EntityProviderWriteProperties properties = EntityProviderWriteProperties
        .fromProperties(DEFAULT_PROPERTIES).validatingFacets(false).build();
    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, roomData, properties);
    final String json = verifyResponse(response);
    assertNotNull(response);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"http://host:80/service/Rooms('4711')\"," +
        "\"uri\":\"http://host:80/service/Rooms('4711')\",\"type\":\"RefScenario.Room\"}," +
        "\"Id\":\"4711\",\"Name\":\"1234567890\",\"Seats\":null,\"Version\":null," +
        "\"nr_Employees\":{\"__deferred\":{\"uri\":\"http://host:80/service/Rooms('4711')/nr_Employees\"}}," +
        "\"nr_Building\":{\"__deferred\":{\"uri\":\"http://host:80/service/Rooms('4711')/nr_Building\"}}}}",
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
    photoData.put("getType", "image/png");

    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, photoData, DEFAULT_PROPERTIES);
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{"
        + "\"id\":\"" + BASE_URI + "Container2.Photos(Id=1,Type='image%2Fpng')\","
        + "\"uri\":\"" + BASE_URI + "Container2.Photos(Id=1,Type='image%2Fpng')\","
        + "\"type\":\"RefScenario2.Photo\",\"etag\":\"W/\\\"1\\\"\",\"content_type\":\"image/png\","
        + "\"media_src\":\"" + BASE_URI + "Container2.Photos(Id=1,Type='image%2Fpng')/$value\","
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
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).serviceRoot(URI.create(BASE_URI))
            .expandSelectTree(node).build();

    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, employeeData,
        writeProperties);
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{"
        + "\"id\":\"" + BASE_URI + "Employees('1')\","
        + "\"uri\":\"" + BASE_URI + "Employees('1')\","
        + "\"type\":\"RefScenario.Employee\",\"content_type\":\"image/jpeg\","
        + "\"media_src\":\"" + BASE_URI + "Employees('1')/$value\","
        + "\"edit_media\":\"" + BASE_URI + "Employees('1')/$value\"},"
        + "\"EntryDate\":\"\\/Date(0)\\/\"}}",
        json);
  }

  @Test
  public void entryWithExpandedEntryButNullData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);
    ExpandSelectTreeNode node1 = createRoomNode();
    
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", new NullEntryCallback());

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, roomData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> roomEntry = checkRoom(response);
    assertTrue(roomEntry.containsKey("nr_Building"));
    assertNull(roomEntry.get("nr_Building"));
  }
  
  class NullEntryCallback implements OnWriteEntryContent {
    @Override
    public WriteEntryCallbackResult retrieveEntryResult(final WriteEntryCallbackContext context)
        throws ODataApplicationException {
      WriteEntryCallbackResult result = new WriteEntryCallbackResult();
      result.setEntryData(null);
      result.setInlineProperties(DEFAULT_PROPERTIES);
      return result;
    }
  }

  private ExpandSelectTreeNode createRoomNode() {
    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nr_Building", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);
    return node1;
  }

  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedEntryButNullDataOmitData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);

    ExpandSelectTreeNode node1 = createRoomNode();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", new NullEntryCallback());

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, roomData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
            .callbacks(callbacks).omitInlineForNullData(true).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> roomEntry = checkRoom(response);
    assertTrue(roomEntry.containsKey("nr_Building"));
    assertNotNull(roomEntry.get("nr_Building"));
    assertTrue(((Map<String, Object>) roomEntry.get("nr_Building")).size() == 1);
    assertTrue(((Map<String, Object>) roomEntry.get("nr_Building")).containsKey("__deferred"));
  }

  @Test
  public void entryWithExpandedEntryButEmptyData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);

    ExpandSelectTreeNode node1 = createRoomNode();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", new EmptyEntryCallback());

    ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, roomData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> roomEntry = checkRoom(response);
    assertTrue(roomEntry.containsKey("nr_Building"));
    assertNull(roomEntry.get("nr_Building"));
  }

  class EmptyEntryCallback implements OnWriteEntryContent {
    @Override
    public WriteEntryCallbackResult retrieveEntryResult(final WriteEntryCallbackContext context)
        throws ODataApplicationException {
      WriteEntryCallbackResult result = new WriteEntryCallbackResult();
      result.setEntryData(new HashMap<String, Object>());
      result.setInlineProperties(DEFAULT_PROPERTIES);
      return result;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedEntryButEmptyDataOmitInline() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);

    ExpandSelectTreeNode node1 = createRoomNode();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", new EmptyEntryCallback());

    ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, roomData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .omitInlineForNullData(true).callbacks(callbacks).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> roomEntry = checkRoom(response);
    assertTrue(roomEntry.containsKey("nr_Building"));
    assertNotNull(roomEntry.get("nr_Building"));
    assertTrue(((Map<String, Object>) roomEntry.get("nr_Building")).size() == 1);
    assertTrue(((Map<String, Object>) roomEntry.get("nr_Building")).containsKey("__deferred"));
  }

  @Test
  public void entryWithExpandedEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);

    ExpandSelectTreeNode node1 = createRoomNode();

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

  @Test(expected = EntityProviderException.class)
  public void entryWithExpandedEntryWithFacets() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Name", "Neu Schwanstein");
    roomData.put("Seats", new Integer(20));
    roomData.put("Version", new Integer(3));

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nr_Employees", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    class EntryCallback implements OnWriteFeedContent {
      @Override
      public WriteFeedCallbackResult retrieveFeedResult(final WriteFeedCallbackContext context)
          throws ODataApplicationException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("EmployeeId", "1");
        data.put("ImageUrl", "hhtp://url");
        WriteFeedCallbackResult result = new WriteFeedCallbackResult();
        result.setFeedData(Collections.singletonList(data));
        result.setInlineProperties(DEFAULT_PROPERTIES);
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
                .callbacks(callbacks).build());
    assertNotNull(response);
  }

  @Test
  public void entryWithExpandedEntryIgnoreFacets() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Name", "Neu Schwanstein");
    roomData.put("Seats", new Integer(20));
    roomData.put("Version", new Integer(3));

    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nr_Employees", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);

    class EntryCallback implements OnWriteFeedContent {
      @Override
      public WriteFeedCallbackResult retrieveFeedResult(final WriteFeedCallbackContext context)
          throws ODataApplicationException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("EmployeeId", "1");
        data.put("ImageUrl", "hhtp://url");
        WriteFeedCallbackResult result = new WriteFeedCallbackResult();
        result.setFeedData(Collections.singletonList(data));
        result.setInlineProperties(DEFAULT_PROPERTIES);
        EntityProviderWriteProperties properties =
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI))
                .validatingFacets(context.getCurrentWriteProperties().isValidatingFacets()).build();
        result.setInlineProperties(properties);
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
                .validatingFacets(false)
                .callbacks(callbacks).build());
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"http://host:80/service/Rooms('1')\"," +
        "\"uri\":\"http://host:80/service/Rooms('1')\",\"type\":\"RefScenario.Room\",\"etag\":\"W/\\\"3\\\"\"}," +
        "\"nr_Employees\":{\"results\":[{\"__metadata\":{\"id\":\"http://host:80/service/Employees('1')\"," +
        "\"uri\":\"http://host:80/service/Employees('1')\",\"type\":\"RefScenario.Employee\"," +
        "\"content_type\":\"application/octet-stream\"," +
        "\"media_src\":\"http://host:80/service/Employees('1')/$value\"," +
        "\"edit_media\":\"http://host:80/service/Employees('1')/$value\"},\"EmployeeId\":\"1\",\"EmployeeName\":null," +
        "\"ManagerId\":null,\"RoomId\":null,\"TeamId\":null," +
        "\"Location\":{\"__metadata\":{\"type\":\"RefScenario.c_Location\"}," +
        "\"City\":{\"__metadata\":{\"type\":\"RefScenario.c_City\"}," +
        "\"PostalCode\":null,\"CityName\":null},\"Country\":null}," +
        "\"Age\":null,\"EntryDate\":null,\"ImageUrl\":\"hhtp://url\"," +
        "\"ne_Manager\":{\"__deferred\":{\"uri\":\"http://host:80/service/Employees('1')/ne_Manager\"}}," +
        "\"ne_Team\":{\"__deferred\":{\"uri\":\"http://host:80/service/Employees('1')/ne_Team\"}}," +
        "\"ne_Room\":{\"__deferred\":{\"uri\":\"http://host:80/service/Employees('1')/ne_Room\"}}}]}}}",
        json);
  }

  @Test
  public void entryWithExpandedEntryButNoRegisteredCallback() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);

    ExpandSelectTreeNode node1 = createRoomNode();

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

    ExpandSelectTreeNode node1 = createRoomNode();

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

    ExpandSelectTreeNode node1 = createBuildingNode();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", new DataFeedCallback());

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
  
  class DataFeedCallback implements OnWriteFeedContent {
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


  @Test
  public void entryWithExpandedFeedInClientUseCase() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node1 = createBuildingNode();

    DataFeedCallback callback = new DataFeedCallback();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", callback);

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).responsePayload(false).build());
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"" + BASE_URI + "Buildings('1')\","
        + "\"uri\":\"" + BASE_URI + "Buildings('1')\",\"type\":\"RefScenario.Building\"},"
        + "\"nb_Rooms\":[{\"__metadata\":{\"id\":\"" + BASE_URI + "Rooms('1')\","
        + "\"uri\":\"" + BASE_URI + "Rooms('1')\",\"type\":\"RefScenario.Room\",\"etag\":\"W/\\\"1\\\"\"},"
        + "\"Id\":\"1\",\"Name\":null,\"Seats\":null,\"Version\":1,"
        + "\"nr_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Rooms('1')/nr_Employees\"}},"
        + "\"nr_Building\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Rooms('1')/nr_Building\"}}}]}}",
        json);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedFeedButNullData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node1 = createBuildingNode();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", new NullFeedCallback());

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> buildingEntry = checkRoom(response);
    assertTrue(buildingEntry.containsKey("nb_Rooms"));
    Map<String, Object> roomsFeed = (Map<String, Object>) buildingEntry.get("nb_Rooms");
    assertNotNull(roomsFeed);
    List<Object> roomsFeedEntries = (List<Object>) roomsFeed.get("results");
    assertEquals(0, roomsFeedEntries.size());
  }
  public class NullFeedCallback implements OnWriteFeedContent {
    @Override
    public WriteFeedCallbackResult retrieveFeedResult(final WriteFeedCallbackContext context)
        throws ODataApplicationException {
      WriteFeedCallbackResult result = new WriteFeedCallbackResult();
      result.setFeedData(null);
      result.setInlineProperties(DEFAULT_PROPERTIES);
      return result;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedFeedButNullDataOmitInline() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node1 = createBuildingNode();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", new NullFeedCallback());

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .omitInlineForNullData(true).callbacks(callbacks).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> buildingEntry = checkRoom(response);
    assertTrue(buildingEntry.containsKey("nb_Rooms"));
    assertNotNull(buildingEntry.get("nb_Rooms"));
    Map<String, Object> navContent = (Map<String, Object>) buildingEntry.get("nb_Rooms");
    assertTrue(navContent.size() == 1);
    assertTrue(navContent.containsKey("__deferred"));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedFeedButNullDataClientUseCase() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node1 = createBuildingNode();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", new NullFeedCallback());

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).responsePayload(false).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> buildingEntry = checkRoom(response);
    
    assertTrue(buildingEntry.containsKey("nb_Rooms"));
    List<Object> roomsFeed = (List<Object>) buildingEntry.get("nb_Rooms");
    assertNotNull(roomsFeed);
    assertEquals(0, roomsFeed.size());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> checkRoom(final ODataResponse response) {
    Map<String, Object> buildingEntry =
        new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    // remove d wrapper
    buildingEntry = (Map<String, Object>) buildingEntry.get("d");
    assertEquals(2, buildingEntry.size());
    return buildingEntry;
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedFeedButNullDataClientUseCaseOmitInline() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node1 = createBuildingNode();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", new NullFeedCallback());

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .omitInlineForNullData(true).callbacks(callbacks).responsePayload(false).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> buildingEntry = checkRoom(response);

    assertTrue(buildingEntry.containsKey("nb_Rooms"));
    assertNotNull(buildingEntry.get("nb_Rooms"));
    assertTrue(((Map<String, Object>) buildingEntry.get("nb_Rooms")).size() == 1);
    assertTrue(((Map<String, Object>) buildingEntry.get("nb_Rooms")).containsKey("__deferred"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedFeedButEmptyData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node1 = createBuildingNode();

   
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", new EmptyFeedCallback());
    
    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> buildingEntry = checkRoom(response);
    assertTrue(buildingEntry.containsKey("nb_Rooms"));
    Map<String, Object> roomsFeed = (Map<String, Object>) buildingEntry.get("nb_Rooms");
    assertNotNull(roomsFeed);
    List<Object> roomsFeedEntries = (List<Object>) roomsFeed.get("results");
    assertEquals(0, roomsFeedEntries.size());
  }
  private ExpandSelectTreeNode createBuildingNode() {
    ExpandSelectTreeNode node2 = Mockito.mock(ExpandSelectTreeNode.class);
    Map<String, ExpandSelectTreeNode> links = new HashMap<String, ExpandSelectTreeNode>();
    links.put("nb_Rooms", node2);
    ExpandSelectTreeNode node1 = Mockito.mock(ExpandSelectTreeNode.class);
    Mockito.when(node1.getLinks()).thenReturn(links);
    return node1;
  }
  
  class EmptyFeedCallback implements OnWriteFeedContent {
    @Override
    public WriteFeedCallbackResult retrieveFeedResult(final WriteFeedCallbackContext context)
        throws ODataApplicationException {
      WriteFeedCallbackResult result = new WriteFeedCallbackResult();
      result.setFeedData(new ArrayList<Map<String, Object>>());
      result.setInlineProperties(DEFAULT_PROPERTIES);
      return result;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedFeedButEmptyDataOmitInline() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node1 = createBuildingNode();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", new EmptyFeedCallback());

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .omitInlineForNullData(true).callbacks(callbacks).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> buildingEntry = checkRoom(response);
    assertTrue(buildingEntry.containsKey("nb_Rooms"));
    assertNotNull(buildingEntry.get("nb_Rooms"));
    assertTrue(((Map<String, Object>) buildingEntry.get("nb_Rooms")).size() == 1);
    assertTrue(((Map<String, Object>) buildingEntry.get("nb_Rooms")).containsKey("__deferred"));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedFeedButEmptyDataClientCase() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node1 = createBuildingNode();

    
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", new EmptyFeedCallback());

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).responsePayload(false).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> buildingEntry = checkRoom(response);

    assertTrue(buildingEntry.containsKey("nb_Rooms"));
    List<Object> roomsFeed = (List<Object>) buildingEntry.get("nb_Rooms");
    assertNotNull(roomsFeed);
    assertEquals(0, roomsFeed.size());
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedFeedButEmptyDataClientCaseOmitInline() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node1 = createBuildingNode();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nb_Rooms", new EmptyFeedCallback());

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, buildingData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .omitInlineForNullData(true).callbacks(callbacks).responsePayload(false).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> buildingEntry = checkRoom(response);

    assertTrue(buildingEntry.containsKey("nb_Rooms"));
    assertNotNull(buildingEntry.get("nb_Rooms"));
    assertTrue(((Map<String, Object>) buildingEntry.get("nb_Rooms")).size() == 1);
    assertTrue(((Map<String, Object>) buildingEntry.get("nb_Rooms")).containsKey("__deferred"));
  }

  @Test
  public void entryWithExpandedFeedButNoRegisteredCallback() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Map<String, Object> buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");

    ExpandSelectTreeNode node1 = createBuildingNode();

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

    ExpandSelectTreeNode node1 = createBuildingNode();

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

    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmMapping mapping = employeesSet.getEntityType().getMapping();
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);

    ODataResponse response = new JsonEntityProvider().writeEntry(employeesSet, employeeData, DEFAULT_PROPERTIES);
    String jsonString = verifyResponse(response);
    Gson gson = new Gson();
    LinkedTreeMap<String, Object> jsonMap = gson.fromJson(jsonString, LinkedTreeMap.class);
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("d");
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("__metadata");

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
    String mediaResourceMimeTypeKey = "~type";
    employeeData.put(mediaResourceMimeTypeKey, "image/jpeg");

    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmMapping mapping = employeesSet.getEntityType().getMapping();
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);
    when(mapping.getMediaResourceMimeTypeKey()).thenReturn(mediaResourceMimeTypeKey);

    ODataResponse response = new JsonEntityProvider().writeEntry(employeesSet, employeeData, DEFAULT_PROPERTIES);
    String jsonString = verifyResponse(response);

    Gson gson = new Gson();
    LinkedTreeMap<String, Object> jsonMap = gson.fromJson(jsonString, LinkedTreeMap.class);
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("d");
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("__metadata");

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

    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    EdmEntityType roomType = roomsSet.getEntityType();
    EdmMapping mapping = mock(EdmMapping.class);
    when(roomType.getMapping()).thenReturn(mapping);
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);

    ODataResponse response = new JsonEntityProvider().writeEntry(roomsSet, roomData, DEFAULT_PROPERTIES);
    String jsonString = verifyResponse(response);
    Gson gson = new Gson();
    LinkedTreeMap<String, Object> jsonMap = gson.fromJson(jsonString, LinkedTreeMap.class);
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("d");
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("__metadata");

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
    String mediaResourceMimeTypeKey = "~type";
    roomData.put(mediaResourceMimeTypeKey, "image/jpeg");

    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    EdmEntityType roomType = roomsSet.getEntityType();
    EdmMapping mapping = mock(EdmMapping.class);
    when(roomType.getMapping()).thenReturn(mapping);
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);
    when(mapping.getMediaResourceMimeTypeKey()).thenReturn(mediaResourceMimeTypeKey);

    ODataResponse response = new JsonEntityProvider().writeEntry(roomsSet, roomData, DEFAULT_PROPERTIES);
    String jsonString = verifyResponse(response);
    Gson gson = new Gson();
    LinkedTreeMap<String, Object> jsonMap = gson.fromJson(jsonString, LinkedTreeMap.class);
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("d");
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("__metadata");

    assertNull(jsonMap.get("media_src"));
    assertNull(jsonMap.get("content_type"));
    assertNull(jsonMap.get("edit_media"));
  }

//  @SuppressWarnings("unchecked")
//  @Test
//  public void assureGetMimeTypeWinsOverGetMediaResourceMimeTypeKey() throws Exception {
//    // Keep this test till version 1.2
//    Map<String, Object> employeeData = new HashMap<String, Object>();
//
//    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//    date.clear();
//    date.set(1999, 0, 1);
//
//    employeeData.put("EmployeeId", "1");
//    employeeData.put("ImmageUrl", null);
//    employeeData.put("ManagerId", "1");
//    employeeData.put("Age", new Integer(52));
//    employeeData.put("RoomId", "1");
//    employeeData.put("EntryDate", date);
//    employeeData.put("TeamId", "42");
//    employeeData.put("EmployeeName", "Walter Winter");
//
//    Map<String, Object> locationData = new HashMap<String, Object>();
//    Map<String, Object> cityData = new HashMap<String, Object>();
//    cityData.put("PostalCode", "33470");
//    cityData.put("CityName", "Duckburg");
//    locationData.put("City", cityData);
//    locationData.put("Country", "Calisota");
//
//    employeeData.put("Location", locationData);
//    String mediaResourceMimeTypeKey = "~type";
//    employeeData.put(mediaResourceMimeTypeKey, "wrong");
//    String originalMimeTypeKey = "~originalType";
//    employeeData.put(originalMimeTypeKey, "right");
//
//    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
//    EdmMapping mapping = employeesSet.getEntityType().getMapping();
//    when(mapping.getMediaResourceMimeTypeKey()).thenReturn(mediaResourceMimeTypeKey);
//    when(mapping.getMimeType()).thenReturn(originalMimeTypeKey);
//
//    ODataResponse response = new JsonEntityProvider().writeEntry(employeesSet, employeeData, DEFAULT_PROPERTIES);
//    String jsonString = verifyResponse(response);
//
//    Gson gson = new Gson();
//    StringMap<Object> jsonMap = gson.fromJson(jsonString, StringMap.class);
//    jsonMap = (StringMap<Object>) jsonMap.get("d");
//    jsonMap = (StringMap<Object>) jsonMap.get("__metadata");
//
//    assertEquals("right", jsonMap.get("content_type"));
//  }

  @Test
  public void additionalLink() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("Id", "1");
    Map<String, Object> key = new HashMap<String, Object>();
    key.put("Id", "3");
    Map<String, Map<String, Object>> links = new HashMap<String, Map<String, Object>>();
    links.put("nr_Building", key);
    final EntityProviderWriteProperties properties = EntityProviderWriteProperties
        .serviceRoot(URI.create(BASE_URI))
        .additionalLinks(links)
        .build();

    final ODataResponse response = new JsonEntityProvider().writeEntry(entitySet, data, properties);
    final String json = verifyResponse(response);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"" + BASE_URI + "Rooms('1')\","
        + "\"uri\":\"" + BASE_URI + "Rooms('1')\",\"type\":\"RefScenario.Room\"},"
        + "\"Id\":\"1\",\"Name\":null,\"Seats\":null,\"Version\":null,"
        + "\"nr_Employees\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Rooms('1')/nr_Employees\"}},"
        + "\"nr_Building\":{\"__deferred\":{\"uri\":\"" + BASE_URI + "Buildings('3')\"}}}}",
        json);
  }

  private String verifyResponse(final ODataResponse response) throws IOException {
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    return json;
  }
  
  @Test
  public void unbalancedPropertyEntryWithInlineEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Version", 1);

    ExpandSelectTreeNode node1 = createRoomNode();

    class EntryCallback implements OnWriteEntryContent {
      @Override
      public WriteEntryCallbackResult retrieveEntryResult(final WriteEntryCallbackContext context)
          throws ODataApplicationException {
        Map<String, Object> buildingData = new HashMap<String, Object>();
        buildingData.put("Id", "1");
        buildingData.put("Name", "Building1");
        WriteEntryCallbackResult result = new WriteEntryCallbackResult();
        result.setEntryData(buildingData);
        result.setInlineProperties(context.getCurrentWriteProperties());
        return result;
      }
    }
    EntryCallback callback = new EntryCallback();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", callback);

    final ODataResponse response =
        new JsonEntityProvider().writeEntry(entitySet, roomData,
            EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(node1)
                .callbacks(callbacks).isDataBasedPropertySerialization(true).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"" + BASE_URI + "Rooms('1')\",\"uri\":\"" + BASE_URI + "Rooms('1')\","
        + "\"type\":\"RefScenario.Room\",\"etag\":\"W/\\\"1\\\"\"},\"Id\":\"1\",\"Version\":1,"
        + "\"nr_Building\":{\"__metadata\":{\"id\":\"" + BASE_URI + "Buildings('1')\","
        + "\"uri\":\"" + BASE_URI + "Buildings('1')\",\"type\":\"RefScenario.Building\"},"
        + "\"Id\":\"1\",\"Name\":\"Building1\"}}}", json);
  }
  
  @Test
  public void contentOnlyWithoutKeyWithoutSelectedProperties() throws Exception {
    HashMap<String, Object> employeeData = new HashMap<String, Object>();
    employeeData.put("ManagerId", "1");
    employeeData.put("Age", new Integer(52));
    employeeData.put("RoomId", "1");
    employeeData.put("TeamId", "42");

    List<String> selectedProperties = new ArrayList<String>();
    selectedProperties.add("ManagerId");
    selectedProperties.add("Age");
    selectedProperties.add("RoomId");
    selectedProperties.add("TeamId");
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");

    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).omitJsonWrapper(true).contentOnly(true)
            .build();
    try {
      new JsonEntityProvider().writeEntry(entitySet, employeeData, properties);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'EmployeeId'"));
    }
  }
  
  @Test
  public void testWithoutKey() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("ManagerId");
    ExpandSelectTreeNode select =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedPropertyNames).build();

    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).expandSelectTree(select).build();

    Map<String, Object> localEmployeeData = new HashMap<String, Object>();
    localEmployeeData.put("ManagerId", "1");

    JsonEntityProvider ser = new JsonEntityProvider();
    try {
    ser.writeEntry(entitySet, localEmployeeData, properties);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'EmployeeId'"));
    }
  }
  
  @Test
  public void testWithoutCompositeKey() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");
    
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).build();

    Map<String, Object> photoData = new HashMap<String, Object>();
    photoData.put("Name", "Mona Lisa");

    JsonEntityProvider ser = new JsonEntityProvider();
    try {
    ser.writeEntry(entitySet, photoData, properties);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'Id'"));
    }
  }
  
  @Test
  public void testWithoutCompositeKeyWithOneKeyNull() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmEntitySet entitySet = edm.getEntityContainer("Container2").getEntitySet("Photos");
    
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).build();

    Map<String, Object> photoData = new HashMap<String, Object>();
    photoData.put("Name", "Mona Lisa");
    photoData.put("Id", Integer.valueOf(1));
    
    EdmTyped typeProperty = edm.getEntityContainer("Container2").getEntitySet("Photos").
        getEntityType().getProperty("Type");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getConcurrencyMode()).thenReturn(EdmConcurrencyMode.Fixed);
    when(facets.getMaxLength()).thenReturn(3);
    when(((EdmProperty) typeProperty).getFacets()).thenReturn(facets);

    JsonEntityProvider ser = new JsonEntityProvider();
    try {
    ser.writeEntry(entitySet, photoData, properties);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'Type'"));
    }
  }
  
  
  @Test
  public void testExceptionWithNonNullablePropertyIsNull() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Organizations");
    EdmProperty nameProperty = (EdmProperty) entitySet.getEntityType().getProperty("Name");
    EdmFacets facets = nameProperty.getFacets();
    when(facets.isNullable()).thenReturn(new Boolean(false));
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).omitETag(true).
        isDataBasedPropertySerialization(true).build();
    
    Map<String, Object> orgData = new HashMap<String, Object>();
    orgData.put("Id", "1");
    try {
      new JsonEntityProvider().writeEntry(entitySet, orgData, properties);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'Name'"));
    }
  }
  
  @Test
  public void testExceptionWithNonNullablePropertyIsNull1() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Organizations");
    EdmProperty kindProperty = (EdmProperty) entitySet.getEntityType().getProperty("Kind");
    EdmFacets facets = kindProperty.getFacets();
    when(facets.isNullable()).thenReturn(new Boolean(false));
    
    EdmProperty nameProperty = (EdmProperty) entitySet.getEntityType().getProperty("Name");
    when(nameProperty.getFacets()).thenReturn(null);
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).omitETag(true).
        isDataBasedPropertySerialization(true).build();
    
    Map<String, Object> orgData = new HashMap<String, Object>();
    orgData.put("Id", "1");
    orgData.put("Name", "Org1");
    try {
      new JsonEntityProvider().writeEntry(entitySet, orgData, properties);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'Kind'"));
    }
  }
  
  @Test
  public void testExceptionWithNonNullablePropertyIsNull2() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Organizations");
    EdmProperty kindProperty = (EdmProperty) entitySet.getEntityType().getProperty("Kind");
    EdmFacets facets = kindProperty.getFacets();
    when(facets.isNullable()).thenReturn(new Boolean(false));
    
    EdmProperty nameProperty = (EdmProperty) entitySet.getEntityType().getProperty("Name");
    EdmFacets facets1 = nameProperty.getFacets();
    when(facets1.isNullable()).thenReturn(new Boolean(false));
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).omitETag(true).
        isDataBasedPropertySerialization(true).build();
    
    Map<String, Object> orgData = new HashMap<String, Object>();
    orgData.put("Id", "1");
    orgData.put("Name", "Org1");
    try {
      new JsonEntityProvider().writeEntry(entitySet, orgData, properties);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("do not allow to format the value 'Org1' for property 'Name'."));
    }
  }
}
