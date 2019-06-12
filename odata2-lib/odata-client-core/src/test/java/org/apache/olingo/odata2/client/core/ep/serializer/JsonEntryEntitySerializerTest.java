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
package org.apache.olingo.odata2.client.core.ep.serializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.stream.XMLStreamException;

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
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.client.api.edm.ClientEdm;
import org.apache.olingo.odata2.client.api.edm.EdmDataServices;
import org.apache.olingo.odata2.client.api.ep.Entity;
import org.apache.olingo.odata2.client.api.ep.EntityCollection;
import org.apache.olingo.odata2.client.api.ep.EntitySerializerProperties;
import org.apache.olingo.odata2.client.core.ep.JsonSerializerDeserializer;
import org.apache.olingo.odata2.client.core.ep.deserializer.XmlMetadataDeserializer;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

/**
 *  
 */
public class JsonEntryEntitySerializerTest extends BaseTest { 
  protected static final String BASE_URI = "http://host:80/service/";
  protected static final EntitySerializerProperties DEFAULT_PROPERTIES =
      EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).build();
  protected static final String ERROR_MSG = "Entity or expanded entity cannot have null value.";
  protected static final String ERROR_MSG1 = "Navigation has to be either an Entity or a Map.";
  

  @Test
  public void entry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    Entity entity = new Entity();
    entity.addProperty("Id", "1");
    entity.addProperty("isScrumTeam", true);
    entity.setWriteProperties(DEFAULT_PROPERTIES);

    final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, entity);
    final String json = verifyResponse(response);
    assertEquals("{\"Id\":\"1\",\"isScrumTeam\":true}", json);
  }

  @Test
  public void entryWithoutKey() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    Entity entity = new Entity();
    entity.addProperty("isScrumTeam", true);
    entity.setWriteProperties(DEFAULT_PROPERTIES);

    final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, entity);
    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"isScrumTeam\":true}", json);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void includeMetadata() throws Exception {
    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);
    Entity entity = new Entity();
    entity.addProperty("EmployeeId", "1");
    entity.addProperty("ImmageUrl", null);
    entity.addProperty("ManagerId", "1");
    entity.addProperty("Age", new Integer(52));
    entity.addProperty("RoomId", "1");
    entity.addProperty("EntryDate", date);
    entity.addProperty("TeamId", "42");
    entity.addProperty("EmployeeName", "Walter Winter");
    Map<String, Object> locationData = new HashMap<String, Object>();
    Map<String, Object> cityData = new HashMap<String, Object>();
    cityData.put("PostalCode", "33470");
    cityData.put("CityName", "Duckburg");
    locationData.put("City", cityData);
    locationData.put("Country", "Calisota");
    entity.addProperty("Location", locationData);
    
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EntitySerializerProperties properties =
        EntitySerializerProperties.fromProperties(DEFAULT_PROPERTIES).includeMetadata(true).build();
    entity.setWriteProperties(properties);
    final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, entity);
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNotNull(employee.get("__metadata"));
    assertNull(employee.get("ne_Manager"));
    assertNull(employee.get("ne_Team"));
    assertNull(employee.get("ne_Room"));
  }

  @Test
  public void includeMetadataWithoutKey() throws Exception {
    Entity employeeData = new Entity();
    employeeData.addProperty("ManagerId", "1");
    employeeData.addProperty("Age", new Integer(52));
    employeeData.addProperty("RoomId", "1");
    employeeData.addProperty("TeamId", "42");

    List<String> selectedProperties = new ArrayList<String>();
    selectedProperties.add("ManagerId");
    selectedProperties.add("Age");
    selectedProperties.add("RoomId");
    selectedProperties.add("TeamId");
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");

    EntitySerializerProperties properties =
        EntitySerializerProperties.fromProperties(DEFAULT_PROPERTIES).includeMetadata(true)
            .build();
    employeeData.setWriteProperties(properties);
    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, employeeData);
    } catch (EntityProviderException e) {
      assertEquals("The metadata do not allow a null value for property 'EmployeeId'.", e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testNavigationLink() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    Entity employeeData = new Entity();
    employeeData.addProperty("EmployeeId", "1");

    Map<String, Object> managerLink = new HashMap<String, Object>();
    managerLink.put("EmployeeId", "1");
    employeeData.addNavigation("ne_Manager", managerLink);

    EntitySerializerProperties properties =
        EntitySerializerProperties.fromProperties(DEFAULT_PROPERTIES).build();
    employeeData.setWriteProperties(properties);
    final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, employeeData);
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNull(employee.get("__metadata"));
    assertNull(employee.get("ne_Team"));
    assertNull(employee.get("ne_Room"));

    assertEquals("1", employee.get("EmployeeId"));
    Map<String, Object> map = (Map<String, Object>) employee.get("ne_Manager");
    map = (Map<String, Object>) map.get("__deferred");
    assertEquals("http://host:80/service/Managers('1')", map.get("uri"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void addEntityToNavigation() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    Entity employeeData = new Entity();
    employeeData.addProperty("EmployeeId", "1");

    Entity managerLink = new Entity();
    managerLink.addProperty("EmployeeId", "1");
    employeeData.addNavigation("ne_Manager", managerLink);

    EntitySerializerProperties properties =
        EntitySerializerProperties.fromProperties(DEFAULT_PROPERTIES).build();
    employeeData.setWriteProperties(properties);
    final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, employeeData);
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNull(employee.get("__metadata"));
    assertNull(employee.get("ne_Team"));
    assertNull(employee.get("ne_Room"));

    assertEquals("1", employee.get("EmployeeId"));
    Map<String, Object> map = (Map<String, Object>) employee.get("ne_Manager");
    assertEquals(map.get("EmployeeId"), "1");
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void addEntityAndMapToNavigation() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    Entity employeeData = new Entity();
    employeeData.addProperty("EmployeeId", "1");

    Entity managerLink = new Entity();
    managerLink.addProperty("EmployeeId", "1");
    employeeData.addNavigation("ne_Manager", managerLink);
    
    Map<String, Object> navigationLink = new HashMap<String, Object>();
    navigationLink.put("Id", "1");
    employeeData.addNavigation("ne_Room", navigationLink);

    EntitySerializerProperties properties =
        EntitySerializerProperties.fromProperties(DEFAULT_PROPERTIES).build();
    employeeData.setWriteProperties(properties);
    final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, employeeData);
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNull(employee.get("__metadata"));
    assertNull(employee.get("ne_Team"));
    assertNotNull(employee.get("ne_Room"));

    assertEquals("1", employee.get("EmployeeId"));
    Map<String, Object> map = (Map<String, Object>) employee.get("ne_Manager");
    assertEquals(map.get("EmployeeId"), "1");
    
    Map<String, Object> roomMap = (Map<String, Object>) employee.get("ne_Room");
    assertEquals(((Map<String, Object>)roomMap.get("__deferred")).get("uri"), 
        "http://host:80/service/Rooms('1')");
  }
  
  @Test
  public void addNullToNavigation() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    Entity employeeData = new Entity();
    employeeData.addProperty("EmployeeId", "1");
    employeeData.addNavigation("ne_Manager", null); 

    EntitySerializerProperties properties =
        EntitySerializerProperties.fromProperties(DEFAULT_PROPERTIES).build();
    employeeData.setWriteProperties(properties);
    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, employeeData);
    } catch (EntityProviderException e) {
      assertEquals(ERROR_MSG, e.getMessage());
    }
  }
  
  @Test
  public void addEmptyEntityToNavigation() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    Entity employeeData = new Entity();
    employeeData.addProperty("EmployeeId", "1");
    Entity managerLink = new Entity();
    employeeData.addNavigation("ne_Manager", managerLink); 

    EntitySerializerProperties properties =
        EntitySerializerProperties.fromProperties(DEFAULT_PROPERTIES).build();
    employeeData.setWriteProperties(properties);
    final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, employeeData);
    @SuppressWarnings("unchecked")
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNull(employee.get("__metadata"));
    assertNull(employee.get("ne_Team"));
    assertNull(employee.get("ne_Room"));

    assertEquals("1", employee.get("EmployeeId"));
    assertNotNull(employee.get("ne_Manager"));
  }
  
  @Test
  public void addEmptyMapToNavigation() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    Entity employeeData = new Entity();
    employeeData.addProperty("EmployeeId", "1");
    Map<String, Object> managerLink = new HashMap<String, Object>();
    employeeData.addNavigation("ne_Manager", managerLink); 

    EntitySerializerProperties properties =
        EntitySerializerProperties.fromProperties(DEFAULT_PROPERTIES).build();
    employeeData.setWriteProperties(properties);
    final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, employeeData);
    @SuppressWarnings("unchecked")
    Map<String, Object> employee =
        (Map<String, Object>) new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertNull(employee.get("__metadata"));
    assertNull(employee.get("ne_Team"));
    assertNull(employee.get("ne_Room"));

    assertEquals("1", employee.get("EmployeeId"));
    assertNull(employee.get("ne_Manager"));
  }
  
  @Test
  public void addIncorrectTypeToNavigation() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    Entity employeeData = new Entity();
    employeeData.addProperty("EmployeeId", "1");
    List<String> managerLink = new ArrayList<String>();
    employeeData.addNavigation("ne_Manager", managerLink); 

    EntitySerializerProperties properties =
        EntitySerializerProperties.fromProperties(DEFAULT_PROPERTIES).build();
    employeeData.setWriteProperties(properties);
    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, employeeData);
    } catch (EntityProviderException e) {
      assertEquals(ERROR_MSG1, e.getMessage());
    }
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
    Entity roomData = new Entity();
    roomData.addProperty("Id", "4711");
    roomData.addProperty("Name", name);
    EntitySerializerProperties properties = EntitySerializerProperties
        .fromProperties(DEFAULT_PROPERTIES).validatingFacets(true).build();
    roomData.setWriteProperties(properties);
    try {
      final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
      final String json = verifyResponse(response);
      assertNotNull(response);
      assertEquals("{\"Id\":\"1\",\"Name\":null,\"isScrumTeam\":true}", json);
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
    Entity roomData = new Entity();
    roomData.addProperty("Id", "4711");
    roomData.addProperty("Name", name);
    EntitySerializerProperties properties = EntitySerializerProperties
        .fromProperties(DEFAULT_PROPERTIES).validatingFacets(false).build();
    roomData.setWriteProperties(properties);
    final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    final String json = verifyResponse(response);
    assertNotNull(response);
    assertEquals("{\"Id\":\"4711\",\"Name\":\"1234567890\"}", json);
  }

  @Test
  public void entryWithNullData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, null);
    } catch (EntityProviderException e) {
      assertEquals(ERROR_MSG, e.getMessage());
    }
  }
  
  @Test
  public void entryWithEmptyData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    Entity entity = new Entity();
    entity.setWriteProperties(DEFAULT_PROPERTIES);
    final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, entity);
    final String json = verifyResponse(response);
    assertNotNull(json);
    assertEquals("{}", json);
  }

  @Test
  public void mediaLinkEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");
    Entity photoData = new Entity();
    photoData.addProperty("Id", 1);
    photoData.addProperty("Type", "image/png");
    photoData.addProperty("BinaryData", new byte[] { -1, 0, 1, 2 });
    photoData.addProperty("getType", "image/png");
    photoData.setWriteProperties(DEFAULT_PROPERTIES);

    final ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, photoData);
    final String json = verifyResponse(response);
    assertEquals("{\"Id\":1,\"Type\":\"image/png\",\"BinaryData\":\"/wABAg==\"}",
        json);
  }

  @Test
  public void entryWithExpandedEntryButNullData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Version", 1);
    roomData.addNavigation("nr_Building", null);
    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
                .build());
    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    } catch (EntityProviderException e) {
      assertEquals(ERROR_MSG, e.getMessage());
    }
  }
 
  @SuppressWarnings("unchecked")
  @Test
  public void entryWithExpandedEntryButEmptyData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Version", 1);
    roomData.addNavigation("nr_Building", new Entity());
    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .build());

    ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    Map<String, Object> roomEntry =
        new Gson().fromJson(new InputStreamReader((InputStream) response.getEntity()), Map.class);
    assertEquals(3, roomEntry.size());
    assertTrue(roomEntry.containsKey("nr_Building"));
    assertNotNull(roomEntry.get("nr_Building"));
    assertTrue(((Map<String, Object>) roomEntry.get("nr_Building")).size() == 0);
  }

  @Test
  public void entryWithExpandedEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Version", 1);
    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
                .build());
    
    Entity buildingData = new Entity();
    buildingData.addProperty("Id", "1");
    buildingData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).build());
    roomData.addNavigation("nr_Building", buildingData);

    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    final String json = verifyResponse(response);
    assertEquals("{\"Id\":\"1\",\"Version\":1,"
        + "\"nr_Building\":{\"Id\":\"1\"}}",
        json);
  }

  @Test(expected = EntityProviderException.class)
  public void entryWithExpandedEntryWithFacets() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    roomData.addProperty("Version", new Integer(3));
    
    Entity employeeData = new Entity();
    employeeData.addProperty("EmployeeId", "1");
    employeeData.addProperty("ImageUrl", "hhtp://url");
    employeeData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .validatingFacets(true).build());
    roomData.addNavigation("nr_Employees", employeeData);
    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
                .build());

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
  }

  @Test
  public void entryWithExpandedEntryIgnoreFacets() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    roomData.addProperty("Version", new Integer(3));
    roomData.setWriteProperties(DEFAULT_PROPERTIES);
    
    EntityCollection employeeCollection = new EntityCollection();
    Entity employeeData = new Entity();
    employeeData.addProperty("EmployeeId", "1");
    employeeData.addProperty("ImageUrl", "hhtp://url");
    employeeData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .validatingFacets(false).build());
    employeeCollection.addEntity(employeeData);
    roomData.addNavigation("nr_Employees", employeeCollection);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    final String json = verifyResponse(response);
    assertEquals("{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,\"Version\":3,"
        + "\"nr_Employees\":[{\"EmployeeId\":\"1\",\"ImageUrl\":\"hhtp://url\"}]}",
        json);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void serializeWithCustomSrcAttributeOnEmployee() throws Exception {
    Entity employeeData = new Entity();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    employeeData.addProperty("EmployeeId", "1");
    employeeData.addProperty("ImmageUrl", null);
    employeeData.addProperty("ManagerId", "1");
    employeeData.addProperty("Age", new Integer(52));
    employeeData.addProperty("RoomId", "1");
    employeeData.addProperty("EntryDate", date);
    employeeData.addProperty("TeamId", "42");
    employeeData.addProperty("EmployeeName", "Walter Winter");

    Map<String, Object> locationData = new HashMap<String, Object>();
    Map<String, Object> cityData = new HashMap<String, Object>();
    cityData.put("PostalCode", "33470");
    cityData.put("CityName", "Duckburg");
    locationData.put("City", cityData);
    locationData.put("Country", "Calisota");

    employeeData.addProperty("Location", locationData);

    String mediaResourceSourceKey = "~src";
    employeeData.addProperty(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    employeeData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        includeMetadata(true).build());

    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmMapping mapping = employeesSet.getEntityType().getMapping();
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);

    ODataResponse response = new JsonSerializerDeserializer().writeEntry(employeesSet, employeeData);
    String jsonString = verifyResponse(response);
    Gson gson = new Gson();
    LinkedTreeMap<String, Object> jsonMap = gson.fromJson(jsonString, LinkedTreeMap.class);
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("__metadata");

    assertEquals("http://localhost:8080/images/image1", jsonMap.get("media_src"));
    assertEquals("application/octet-stream", jsonMap.get("content_type"));
    assertEquals("http://host:80/service/Employees('1')/$value", jsonMap.get("edit_media"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void serializeWithCustomSrcAndTypeAttributeOnEmployee() throws Exception {
    Entity employeeData = new Entity();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    employeeData.addProperty("EmployeeId", "1");
    employeeData.addProperty("ImmageUrl", null);
    employeeData.addProperty("ManagerId", "1");
    employeeData.addProperty("Age", new Integer(52));
    employeeData.addProperty("RoomId", "1");
    employeeData.addProperty("EntryDate", date);
    employeeData.addProperty("TeamId", "42");
    employeeData.addProperty("EmployeeName", "Walter Winter");

    Map<String, Object> locationData = new HashMap<String, Object>();
    Map<String, Object> cityData = new HashMap<String, Object>();
    cityData.put("PostalCode", "33470");
    cityData.put("CityName", "Duckburg");
    locationData.put("City", cityData);
    locationData.put("Country", "Calisota");

    employeeData.addProperty("Location", locationData);
    String mediaResourceSourceKey = "~src";
    employeeData.addProperty(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    String mediaResourceMimeTypeKey = "~type";
    employeeData.addProperty(mediaResourceMimeTypeKey, "image/jpeg");
    employeeData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        includeMetadata(true).build());

    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmMapping mapping = employeesSet.getEntityType().getMapping();
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);
    when(mapping.getMediaResourceMimeTypeKey()).thenReturn(mediaResourceMimeTypeKey);

    ODataResponse response = new JsonSerializerDeserializer().writeEntry(employeesSet, employeeData);
    String jsonString = verifyResponse(response);

    Gson gson = new Gson();
    LinkedTreeMap<String, Object> jsonMap = gson.fromJson(jsonString, LinkedTreeMap.class);
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("__metadata");

    assertEquals("http://localhost:8080/images/image1", jsonMap.get("media_src"));
    assertEquals("image/jpeg", jsonMap.get("content_type"));
    assertEquals("http://host:80/service/Employees('1')/$value", jsonMap.get("edit_media"));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void serializeWithCustomSrcAttributeOnRoom() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    roomData.addProperty("Version", new Integer(3));

    String mediaResourceSourceKey = "~src";
    roomData.addProperty(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        includeMetadata(true).build());

    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    EdmEntityType roomType = roomsSet.getEntityType();
    EdmMapping mapping = mock(EdmMapping.class);
    when(roomType.getMapping()).thenReturn(mapping);
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);

    ODataResponse response = new JsonSerializerDeserializer().writeEntry(roomsSet, roomData);
    String jsonString = verifyResponse(response);
    Gson gson = new Gson();
    LinkedTreeMap<String, Object> jsonMap = gson.fromJson(jsonString, LinkedTreeMap.class);
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("__metadata");

    assertNull(jsonMap.get("media_src"));
    assertNull(jsonMap.get("content_type"));
    assertNull(jsonMap.get("edit_media"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void serializeWithCustomSrcAndTypeAttributeOnRoom() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    roomData.addProperty("Version", new Integer(3));

    String mediaResourceSourceKey = "~src";
    roomData.addProperty(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    String mediaResourceMimeTypeKey = "~type";
    roomData.addProperty(mediaResourceMimeTypeKey, "image/jpeg");
    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        includeMetadata(true).build());

    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    EdmEntityType roomType = roomsSet.getEntityType();
    EdmMapping mapping = mock(EdmMapping.class);
    when(roomType.getMapping()).thenReturn(mapping);
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);
    when(mapping.getMediaResourceMimeTypeKey()).thenReturn(mediaResourceMimeTypeKey);

    ODataResponse response = new JsonSerializerDeserializer().writeEntry(roomsSet, roomData);
    String jsonString = verifyResponse(response);
    Gson gson = new Gson();
    LinkedTreeMap<String, Object> jsonMap = gson.fromJson(jsonString, LinkedTreeMap.class);
    jsonMap = (LinkedTreeMap<String, Object>) jsonMap.get("__metadata");

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
  
  @Test
  public void unbalancedPropertyEntryWithInlineEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Version", 1);
    roomData.setWriteProperties(DEFAULT_PROPERTIES);
    
    Entity buildingData = new Entity();
    buildingData.addProperty("Id", "1");
    buildingData.addProperty("Name", "Building1");
    roomData.addNavigation("nr_Building", buildingData);
    
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"Id\":\"1\",\"Version\":1,\"nr_Building\":{\"Id\":\"1\",\"Name\":\"Building1\"}}", json);
  }
  
  @Test
  public void unbalancedPropertyEntryWithMultipleInlineEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Version", 1);
    
    Entity nrBuildingData = new Entity();
    EntityCollection roomsCollection = new EntityCollection();
    Entity nbRoomData = new Entity();
    nbRoomData.addProperty("Id", "1");
    nbRoomData.addProperty("Version", 1);
    roomsCollection.addEntity(nbRoomData);
    nrBuildingData.addNavigation("nb_Rooms", roomsCollection);
    roomData.addNavigation("nr_Building", nrBuildingData);
    
    EntityCollection nrEmployeeCollection = new EntityCollection();
    Entity employeeData = new Entity();
    Entity managerData = new Entity();
    managerData.addProperty("EmployeeId", "1");
    managerData.addProperty("ImageUrl", "hhtp://url");
    employeeData.addNavigation("ne_Manager", managerData);
    Entity neRoomData = new Entity();
    neRoomData.addProperty("Id", "1");
    neRoomData.addProperty("Version", 1);
    employeeData.addNavigation("ne_Room", neRoomData);
    nrEmployeeCollection.addEntity(employeeData);
    nrEmployeeCollection.addEntity(managerData);
    roomData.addNavigation("nr_Employees", nrEmployeeCollection);
    
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"Id\":\"1\",\"Version\":1,\"nr_Employees\":[{" +
        "\"ne_Manager\":{\"EmployeeId\":\"1\"},\"ne_Room\":{\"Id\":\"1\",\"Version\":1}},"+
        "{\"EmployeeId\":\"1\",\"ImageUrl\":\"hhtp://url\"}],"+
        "\"nr_Building\":{\"nb_Rooms\":[{\"Id\":\"1\",\"Version\":1}]}}", json);
  }
  
  @Test
  public void entryWithEmptyInlineEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Version", 1);
    roomData.setWriteProperties(DEFAULT_PROPERTIES);
    
    Entity buildingData = new Entity();
    roomData.addNavigation("nr_Building", buildingData);
    
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"Id\":\"1\",\"Version\":1,\"nr_Building\":"
        + "{}}", json);
  }
  
  @Test
  public void entryWithEmptyInlineEntryWithKeyAutoGenFlag() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Version", 1);
    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        isKeyAutoGenerated(true).build());
    
    Entity buildingData = new Entity();
    roomData.addNavigation("nr_Building", buildingData);
    
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"Id\":\"1\",\"Version\":1,\"nr_Building\":"
        + "{}}", json);
  }
  
  @Test
  public void entryWithInlineEntryWithoutKey() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity roomData = new Entity();
    roomData.addProperty("Version", 1);
    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        isKeyAutoGenerated(true).build());
    
    Entity buildingData = new Entity();
    buildingData.addProperty("Name", "Building1");
    roomData.addNavigation("nr_Building", buildingData);
    
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"Version\":1,\"nr_Building\":{\"Name\":\"Building1\"}}", json);
  }
  
  @Test
  public void entryWithInlineEntryWithoutKeyWithMetadata() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity roomData = new Entity();
    roomData.addProperty("Version", 1);
    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        isKeyAutoGenerated(true).includeMetadata(true).build());
    
    Entity buildingData = new Entity();
    buildingData.addProperty("Name", "Building1");
    roomData.addNavigation("nr_Building", buildingData);
    
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"__metadata\":{\"id\":\"http://host:80/service/Rooms('A')\","
        + "\"uri\":\"http://host:80/service/Rooms('A')\",\"type\":\"RefScenario.Room\"},"
        + "\"Version\":1,\"nr_Building\":{\"Name\":\"Building1\"}}", json);
  }
  
  @Test
  public void contentOnlyWithoutKeyWithoutSelectedProperties() throws Exception {
    Entity employeeData = new Entity();
    employeeData.addProperty("ManagerId", "1");
    employeeData.addProperty("Age", new Integer(52));
    employeeData.addProperty("RoomId", "1");
    employeeData.addProperty("TeamId", "42");

    employeeData.setWriteProperties(
        EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).build());
    
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");

    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, employeeData);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'EmployeeId'"));
    }
  }
  
  @Test
  public void testWithoutCompositeKey() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");
    
    Entity photoData = new Entity();
    photoData.addProperty("Name", "Mona Lisa");
    photoData.setWriteProperties(
        EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).build());

    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, photoData);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'Id'"));
    }
  }
  
  @Test
  public void testWithoutCompositeKeyWithOneKeyNull() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmEntitySet entitySet = edm.getEntityContainer("Container2").getEntitySet("Photos");
    
    Entity photoData = new Entity();
    photoData.addProperty("Name", "Mona Lisa");
    photoData.addProperty("Id", Integer.valueOf(1));
    photoData.setWriteProperties(
        EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).build());
    
    EdmTyped typeProperty = edm.getEntityContainer("Container2").getEntitySet("Photos").
        getEntityType().getProperty("Type");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getConcurrencyMode()).thenReturn(EdmConcurrencyMode.Fixed);
    when(facets.getMaxLength()).thenReturn(3);
    when(((EdmProperty) typeProperty).getFacets()).thenReturn(facets);

    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, photoData);
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
    
    Entity orgData = new Entity();
    orgData.addProperty("Id", "1");
    orgData.setWriteProperties(
        EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).build());
    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, orgData);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'Name'"));
    }
  }
  
  private InputStream createStreamReader(final String xml) throws
  XMLStreamException, UnsupportedEncodingException {
    return new ByteArrayInputStream(xml.getBytes("UTF-8"));
  }
  
  @Test
  public void deepInsertEndToEnd() throws Exception {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    String xml = readFile("metadataForDeepInsert.xml");
    InputStream reader = createStreamReader(xml);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals(1, result.getEdm().getSchemas().size());
    ClientEdm edm = result.getEdm();
    
    Entity descMap = new Entity();
    descMap.addProperty("Product", "CRPROD2");
    descMap.addProperty("Language", "ES");
    descMap.addProperty("ProductDescription", "Hola2");
    EntityCollection descList =  new EntityCollection();

    descList.addEntity(descMap);

    Entity prodCreateFakeMap = new Entity();
    prodCreateFakeMap.addProperty("Product", "CRPROD2");
    prodCreateFakeMap.addProperty("ProductType", "HALB");
    prodCreateFakeMap.addProperty("BaseUnit", "PC");

    prodCreateFakeMap.addNavigation("to_Description", descList);
   
    EntitySerializerProperties properties = EntitySerializerProperties
        .serviceRoot(URI.create(BASE_URI)).build();
    prodCreateFakeMap.setWriteProperties(properties);
    JsonSerializerDeserializer provider = new JsonSerializerDeserializer();
    ODataResponse response =provider
        .writeEntry(edm.getDefaultEntityContainer().getEntitySet("A_Product"), prodCreateFakeMap);

    assertNotNull(response);
    assertNotNull(response.getEntity());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"Product\":\"CRPROD2\",\"BaseUnit\":\"PC\",\"ProductType\":\"HALB\","
        + "\"to_Description\":[{\"Product\":\"CRPROD2\",\"Language\":\"ES\",\"ProductDescription\":\"Hola2\"}]}", json);
 
  }
  
  protected String readFile(final String filename) throws IOException {
    InputStream in = getFileAsStream(filename);

    byte[] tmp = new byte[8192];
    int count = in.read(tmp);
    StringBuilder b = new StringBuilder();
    while (count >= 0) {
      b.append(new String(tmp, 0, count));
      count = in.read(tmp);
    }

    return b.toString();
  }
  protected InputStream getFileAsStream(final String filename) throws IOException {
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    if (in == null) {
      throw new IOException("Requested file '" + filename + "' was not found.");
    }
    return in;
  }
  
  @Test
  public void testExceptionWithNonNullablePropertyIsNull1() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Organizations");
    EdmProperty kindProperty = (EdmProperty) entitySet.getEntityType().getProperty("Kind");
    EdmFacets facets = kindProperty.getFacets();
    when(facets.isNullable()).thenReturn(new Boolean(false));
    
    EdmProperty nameProperty = (EdmProperty) entitySet.getEntityType().getProperty("Name");
    when(nameProperty.getFacets()).thenReturn(null);
    
    Entity orgData = new Entity();
    orgData.addProperty("Id", "1");
    orgData.addProperty("Name", "Org1");
    orgData.setWriteProperties(
        EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).build());
    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, orgData);
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
     
    Entity orgData = new Entity();
    orgData.addProperty("Id", "1");
    orgData.addProperty("Name", "Org1");
    orgData.setWriteProperties(
        EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).build());
    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, orgData);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("do not allow to format the value 'Org1' for property 'Name'."));
    }
  }
}
