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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.client.api.ep.Entity;
import org.apache.olingo.odata2.client.api.ep.EntityCollection;
import org.apache.olingo.odata2.client.api.ep.EntityCollectionSerializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntitySerializerProperties;
import org.apache.olingo.odata2.client.core.ep.JsonSerializerDeserializer;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

/**
 *  
 */
public class JsonFeedEntitySerializerTest extends BaseTest { 
  protected static final String BASE_URI = "http://host:80/service/";
  protected static final EntitySerializerProperties DEFAULT_PROPERTIES =
      EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).build();
  protected static final String ERROR_MSG = "Entity or expanded entity cannot have null value.";

  @Test
  public void unbalancedPropertyEntryWithInlineFeed() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .build());
    EntityCollection innerData = new EntityCollection();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    data.setWriteProperties(DEFAULT_PROPERTIES);
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    data.setWriteProperties(DEFAULT_PROPERTIES);
    innerData.addEntity(data);
    roomData.addNavigation("nr_Employees", innerData);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,\"nr_Employees\":"
        + "[{\"EmployeeId\":\"1\",\"EmployeeName\":\"EmpName1\",\"RoomId\":\"1\"},"
        + "{\"EmployeeId\":\"1\",\"RoomId\":\"1\"}]}", json);
  }
  
  @Test
  public void unbalancedPropertyEntryWithInlineFeedWithPropertiesInParent() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .build());
    EntityCollection innerData = new EntityCollection();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    roomData.addNavigation("nr_Employees", innerData);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,\"nr_Employees\":"
        + "[{\"EmployeeId\":\"1\",\"EmployeeName\":\"EmpName1\",\"RoomId\":\"1\"},"
        + "{\"EmployeeId\":\"1\",\"RoomId\":\"1\"}]}", json);
  }
  
  @Test
  public void unbalancedPropertyEntryWithEmptyInlineFeed() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .build());
    EntityCollection innerData = new EntityCollection();
    roomData.addNavigation("nr_Employees", innerData);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals("{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,\"nr_Employees\":"
        + "[]}", json);
  }
  
  @Test
  public void unbalancedPropertyEntryWithPartialEmptyInlineFeed() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .build());
    EntityCollection innerData = new EntityCollection();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    data.setWriteProperties(DEFAULT_PROPERTIES);
    innerData.addEntity(data);
    
    data = new Entity();
    innerData.addEntity(data);
    roomData.addNavigation("nr_Employees", innerData);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals("{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,\"nr_Employees\":"
        + "[{\"EmployeeId\":\"1\",\"EmployeeName\":\"EmpName1\",\"RoomId\":\"1\"},{}]}", json);
  }
  
  @Test
  public void entryWithExpandedFeedButEmptyEntityCollection() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Entity buildingData = new Entity();
    buildingData.addProperty("Id", "1");
    buildingData.setWriteProperties(DEFAULT_PROPERTIES);
    
    EntityCollection roomCollection = new EntityCollection();
    Entity roomData = new Entity();
    roomCollection.addEntity(roomData);
    buildingData.addNavigation("nb_Rooms", roomCollection);

    ODataResponse response = new JsonSerializerDeserializer().writeEntry(entitySet, buildingData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals("{\"Id\":\"1\",\"nb_Rooms\":[{}]}", json);
  }

  @Test
  public void entryWithExpandedFeedButNullEntityCollection() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    Entity buildingData = new Entity();
    buildingData.addProperty("Id", "1");
    buildingData.setWriteProperties(DEFAULT_PROPERTIES);
    
    EntityCollection roomCollection = new EntityCollection();
    roomCollection.addEntity(null);
    buildingData.addNavigation("nb_Rooms", roomCollection);

    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, buildingData);
    } catch (EntityProviderException e) {
      assertEquals(ERROR_MSG, e.getMessage());
    }
  }
  
  @Test
  public void unbalancedPropertyEntryWithNullInlineFeed() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .build());
    roomData.addNavigation("nr_Employees", null);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    } catch (EntityProviderException e) {
      assertEquals(ERROR_MSG, e.getMessage());
    }
  }
  
  @Test
  public void entryWithExpandedEntryWithWritePropertiesOnCollection() throws Exception {
    Edm edm = MockFacade.getMockEdm();
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
    employeeCollection.addEntity(employeeData);
    employeeCollection.setCollectionProperties
    (EntityCollectionSerializerProperties.
        serviceRoot(URI.create(BASE_URI)).build());
    roomData.addNavigation("nr_Employees", employeeCollection);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    final String json = verifyResponse(response);
    assertEquals("{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,\"Version\":3,"
        + "\"nr_Employees\":[{\"EmployeeId\":\"1\",\"ImageUrl\":\"hhtp://url\"}]}",
        json);
  }
  
  @Test
  public void entryWithExpandedEntryWithGlobalWritePropertiesOnCollection() throws Exception {
    Edm edm = MockFacade.getMockEdm();
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
    employeeCollection.addEntity(employeeData);
    employeeCollection.setGlobalEntityProperties(EntitySerializerProperties.
        serviceRoot(URI.create(BASE_URI)).validatingFacets(true).build());
    employeeCollection.setCollectionProperties
    (EntityCollectionSerializerProperties.
        serviceRoot(URI.create(BASE_URI)).build());
    roomData.addNavigation("nr_Employees", employeeCollection);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    final String json = verifyResponse(response);
    assertEquals("{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,\"Version\":3,"
        + "\"nr_Employees\":[{\"EmployeeId\":\"1\",\"ImageUrl\":\"hhtp://url\"}]}",
        json);
  }
  
  @Test
  public void entryWithExpandedEntryWithoutKeyWithGlobalWritePropertiesOnCollection() throws Exception {
    Edm edm = MockFacade.getMockEdm();
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
    employeeCollection.addEntity(employeeData);
    employeeCollection.setGlobalEntityProperties(EntitySerializerProperties.
        serviceRoot(URI.create(BASE_URI)).validatingFacets(true).includeMetadata(true).build());
    employeeCollection.setCollectionProperties
    (EntityCollectionSerializerProperties.
        serviceRoot(URI.create(BASE_URI)).build());
    roomData.addNavigation("nr_Employees", employeeCollection);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    final String json = verifyResponse(response);
    assertEquals("{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,\"Version\":3,"
        + "\"nr_Employees\":[{\"__metadata\":{\"id\":\"http://host:80/service/Employees('1')\",\"uri\":"
        + "\"http://host:80/service/Employees('1')\",\"type\":\"RefScenario.Employee\",\"content_type\":"
        + "\"application/octet-stream\",\"media_src\":\"http://host:80/service/Employees('1')/$value\","
        + "\"edit_media\":\"http://host:80/service/Employees('1')/$value\"},\"EmployeeId\":\"1\","
        + "\"ImageUrl\":\"hhtp://url\"}]}",
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
  public void unbalancedPropertyEntryWithInlineFeedWithoutKey() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .build());
    EntityCollection innerData = new EntityCollection();
    Entity data = new Entity();
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    data.setWriteProperties(DEFAULT_PROPERTIES);
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("RoomId", "1");
    data.setWriteProperties(DEFAULT_PROPERTIES);
    innerData.addEntity(data);
    roomData.addNavigation("nr_Employees", innerData);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    assertNotNull(response);
    assertNotNull(response.getEntity());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,\"nr_Employees\":"
        + "[{\"EmployeeName\":\"EmpName1\",\"RoomId\":\"1\"},"
        + "{\"RoomId\":\"1\"}]}", json);
  }
  
  @Test
  public void unbalancedPropertyEntryWithoutKeyWithInlineFeed() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Entity roomData = new Entity();
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .build());
    EntityCollection innerData = new EntityCollection();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    data.setWriteProperties(DEFAULT_PROPERTIES);
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("EmployeeId", "2");
    data.addProperty("RoomId", "1");
    data.setWriteProperties(DEFAULT_PROPERTIES);
    innerData.addEntity(data);
    roomData.addNavigation("nr_Employees", innerData);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    final String json = verifyResponse(response);
    assertEquals("{\"Name\":\"Neu Schwanstein\",\"Seats\":20,\"nr_Employees\":"
        + "[{\"EmployeeId\":\"1\",\"EmployeeName\":\"EmpName1\",\"RoomId\":\"1\"},"
        + "{\"EmployeeId\":\"2\",\"RoomId\":\"1\"}]}",
        json);
  }
  
  @Test(expected=EntityProviderException.class)
  public void unbalancedPropertyEntryWithoutKeyWithEmptyInlineFeedIncludingMetadata() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Entity roomData = new Entity();
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .includeMetadata(true).build());
    EntityCollection innerData = new EntityCollection();
    roomData.addNavigation("nr_Employees", innerData);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
  }
  
  @Test
  public void unbalancedPropertyEntryWithoutKeyWithNullInlineFeed() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    Entity roomData = new Entity();
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    roomData.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI))
        .build());
    roomData.addNavigation("nr_Employees", null);

    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    try {
      new JsonSerializerDeserializer().writeEntry(entitySet, roomData);
    } catch (EntityProviderException e) {
      assertEquals(ERROR_MSG, e.getMessage());
    }
  }
  
  @Test
  public void feed() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    EntityCollection teamsData = new EntityCollection();
    Entity team1Data = new Entity();
    team1Data.addProperty("Id", "1");
    team1Data.addProperty("isScrumTeam", true);
    Entity team2Data = new Entity();
    team2Data.addProperty("Id", "2");
    team2Data.addProperty("isScrumTeam", false);
    teamsData.addEntity(team1Data);
    teamsData.addEntity(team2Data);
    teamsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(URI.create(BASE_URI)).build());

    final ODataResponse response = new JsonSerializerDeserializer().writeFeed(entitySet, teamsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"results\":[{\"Id\":\"1\",\"isScrumTeam\":true},{\"Id\":\"2\",\"isScrumTeam\":false}]}",
        json);
  }
  
  @Test
  public void feedWithEmptyData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    EntityCollection teamsData = new EntityCollection();
    Entity team1Data = new Entity();
    team1Data.addProperty("Id", "1");
    team1Data.addProperty("isScrumTeam", true);
    Entity team2Data = new Entity();
    teamsData.addEntity(team1Data);
    teamsData.addEntity(team2Data);
    teamsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(URI.create(BASE_URI)).build());

    final ODataResponse response = new JsonSerializerDeserializer().writeFeed(entitySet, teamsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"results\":[{\"Id\":\"1\",\"isScrumTeam\":true},{}]}",
        json);
  }
  
  @Test
  public void feedWithNullData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    
    try {
      new JsonSerializerDeserializer().writeFeed(entitySet, null);
    } catch (EntityProviderException e) {
      assertEquals(ERROR_MSG, e.getMessage());
    }
  }
  
  @Test
  public void feedWithMetadata() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    EntityCollection teamsData = new EntityCollection();
    Entity team1Data = new Entity();
    team1Data.addProperty("Id", "1");
    team1Data.addProperty("isScrumTeam", true);
    team1Data.setWriteProperties(EntitySerializerProperties.
        serviceRoot(URI.create(BASE_URI)).includeMetadata(true).build());
    Entity team2Data = new Entity();
    team2Data.addProperty("Id", "2");
    team2Data.addProperty("isScrumTeam", false);
    teamsData.addEntity(team1Data);
    teamsData.addEntity(team2Data);
    teamsData.setCollectionProperties(EntityCollectionSerializerProperties.
        serviceRoot(URI.create(BASE_URI)).build());

    final ODataResponse response = new JsonSerializerDeserializer().writeFeed(entitySet, teamsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"results\":[{\"__metadata\":{\"id\":\"http://host:80/service/Teams('1')\","
        + "\"uri\":\"http://host:80/service/Teams('1')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"1\",\"isScrumTeam\":true},{\"Id\":\"2\",\"isScrumTeam\":false}]}",
        json);
  }
  
  @Test
  public void feedWithGlobalEntityProperties() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    EntityCollection teamsData = new EntityCollection();
    Entity team1Data = new Entity();
    team1Data.addProperty("Id", "1");
    team1Data.addProperty("isScrumTeam", true);
    Entity team2Data = new Entity();
    team2Data.addProperty("Id", "2");
    team2Data.addProperty("isScrumTeam", false);
    teamsData.addEntity(team1Data);
    teamsData.addEntity(team2Data);
    teamsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(URI.create(BASE_URI)).build());
    teamsData.setGlobalEntityProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        includeMetadata(true).build());
    
    final ODataResponse response = new JsonSerializerDeserializer().writeFeed(entitySet, teamsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"results\":[{\"__metadata\":{\"id\":\"http://host:80/service/Teams('1')\","
        + "\"uri\":\"http://host:80/service/Teams('1')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"1\",\"isScrumTeam\":true},{\"__metadata\":"
        + "{\"id\":\"http://host:80/service/Teams('2')\",\"uri\":"
        + "\"http://host:80/service/Teams('2')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"2\",\"isScrumTeam\":false}]}",
        json);
  }
  
  @Test
  public void feedWithPropertiesForEveryEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    EntityCollection teamsData = new EntityCollection();
    Entity team1Data = new Entity();
    team1Data.addProperty("Id", "1");
    team1Data.addProperty("isScrumTeam", true);
    team1Data.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        includeMetadata(true).build());
    Entity team2Data = new Entity();
    team2Data.addProperty("Id", "2");
    team2Data.addProperty("isScrumTeam", false);
    team2Data.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        includeMetadata(true).build());
    teamsData.addEntity(team1Data);
    teamsData.addEntity(team2Data);
    teamsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(URI.create(BASE_URI)).build());
    
    final ODataResponse response = new JsonSerializerDeserializer().writeFeed(entitySet, teamsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"results\":[{\"__metadata\":{\"id\":\"http://host:80/service/Teams('1')\","
        + "\"uri\":\"http://host:80/service/Teams('1')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"1\",\"isScrumTeam\":true},{\"__metadata\":"
        + "{\"id\":\"http://host:80/service/Teams('2')\",\"uri\":"
        + "\"http://host:80/service/Teams('2')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"2\",\"isScrumTeam\":false}]}",
        json);
  }
  
  @Test
  public void unbalancedPropertyFeedWithInlineFeed() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    EntityCollection roomsData = new EntityCollection();
    Entity room1Data = new Entity();
    room1Data.addProperty("Id", "1");
    room1Data.addProperty("Name", "Neu Schwanstein");
    room1Data.addProperty("Seats", new Integer(20));
    room1Data.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        validatingFacets(true).build());
    
    EntityCollection innerData = new EntityCollection();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    room1Data.addNavigation("nr_Employees", innerData);
    roomsData.addEntity(room1Data);
    
    Entity room2Data = new Entity();
    room2Data.addProperty("Id", "2");
    room2Data.addProperty("Name", "John");
    room2Data.addProperty("Seats", new Integer(10));
    room2Data.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        validatingFacets(true).build());
    
    innerData = new EntityCollection();
    data = new Entity();
    data.addProperty("EmployeeId", "2");
    data.addProperty("EmployeeName", "EmpName2");
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("EmployeeId", "2");
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    room2Data.addNavigation("nr_Employees", innerData);
    roomsData.addEntity(room2Data);
    
    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeFeed(entitySet, roomsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"results\":[{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,"
        + "\"nr_Employees\":[{\"EmployeeId\":\"1\",\"EmployeeName\":\"EmpName1\",\"RoomId\":\"1\"},"
        + "{\"EmployeeId\":\"1\",\"RoomId\":\"1\"}]},"
        + "{\"Id\":\"2\",\"Name\":\"John\",\"Seats\":10,"
        + "\"nr_Employees\":[{\"EmployeeId\":\"2\",\"EmployeeName\":\"EmpName2\",\"RoomId\":\"2\"},"
        + "{\"EmployeeId\":\"2\",\"RoomId\":\"2\"}]}]}", json);
  }
  
  @Test
  public void unbalancedPropertyFeedWithInlineFeedAndNavigationLink() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    EntityCollection roomsData = new EntityCollection();
    Entity room1Data = new Entity();
    room1Data.addProperty("Id", "1");
    room1Data.addProperty("Name", "Neu Schwanstein");
    room1Data.addProperty("Seats", new Integer(20));
    room1Data.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        validatingFacets(true).build());
    
    EntityCollection innerData = new EntityCollection();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    room1Data.addNavigation("nr_Employees", innerData);
    Map<String, Object> link1 = new HashMap<String, Object>();
    link1.put("Id", 1);
    room1Data.addNavigation("nr_Building", link1);
    roomsData.addEntity(room1Data);
    
    Entity room2Data = new Entity();
    room2Data.addProperty("Id", "2");
    room2Data.addProperty("Name", "John");
    room2Data.addProperty("Seats", new Integer(10));
    room2Data.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        validatingFacets(true).build());
    
    innerData = new EntityCollection();
    data = new Entity();
    data.addProperty("EmployeeId", "2");
    data.addProperty("EmployeeName", "EmpName2");
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("EmployeeId", "2");
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    room2Data.addNavigation("nr_Employees", innerData);
    Map<String, Object> link2 = new HashMap<String, Object>();
    link2.put("Id", 2);
    room2Data.addNavigation("nr_Building", link2);
    roomsData.addEntity(room2Data);
    
    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeFeed(entitySet, roomsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"results\":[{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,"
        + "\"nr_Employees\":[{\"EmployeeId\":\"1\",\"EmployeeName\":\"EmpName1\",\"RoomId\":\"1\"},"
        + "{\"EmployeeId\":\"1\",\"RoomId\":\"1\"}],\"nr_Building\":{\"__deferred\":"
        + "{\"uri\":\"http://host:80/service/Buildings('1')\"}}},"
        + "{\"Id\":\"2\",\"Name\":\"John\",\"Seats\":10,"
        + "\"nr_Employees\":[{\"EmployeeId\":\"2\",\"EmployeeName\":\"EmpName2\",\"RoomId\":\"2\"},"
        + "{\"EmployeeId\":\"2\",\"RoomId\":\"2\"}],\"nr_Building\":{\"__deferred\":"
        + "{\"uri\":\"http://host:80/service/Buildings('2')\"}}}]}", json);
  }
  
  @Test
  public void unbalancedPropertyFeedWithInlineEntryAndNavigationLink() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    EntityCollection roomsData = new EntityCollection();
    Entity room1Data = new Entity();
    room1Data.addProperty("Id", "1");
    room1Data.addProperty("Name", "Neu Schwanstein");
    room1Data.addProperty("Seats", new Integer(20));
    room1Data.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        validatingFacets(true).build());
    
    Map<String, Object> link1 = new HashMap<String, Object>();
    link1.put("Id", 1);
    room1Data.addNavigation("nr_Building", link1);
    roomsData.addEntity(room1Data);
    
    Entity room2Data = new Entity();
    room2Data.addProperty("Id", "2");
    room2Data.addProperty("Name", "John");
    room2Data.addProperty("Seats", new Integer(10));
    room2Data.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        validatingFacets(true).build());
    
    Entity data = new Entity();
    data.addProperty("Id", "2");
    data.addProperty("Name", "Team2");
    
    room2Data.addNavigation("nr_Building", data);
    roomsData.addEntity(room2Data);
    
    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeFeed(entitySet, roomsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"results\":[{\"Id\":\"1\",\"Name\":\"Neu Schwanstein\",\"Seats\":20,"
        + "\"nr_Building\":{\"__deferred\":{\"uri\":\"http://host:80/service/Buildings('1')\"}}},"
        + "{\"Id\":\"2\",\"Name\":\"John\",\"Seats\":10,\"nr_Building\":"
        + "{\"Id\":\"2\",\"Name\":\"Team2\"}}]}", json);
  }
  
  @Test
  public void unbalancedPropertyFeedWithInlineFeedAndNavigationLinkWithoutKeys() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    EntityCollection roomsData = new EntityCollection();
    Entity room1Data = new Entity();
    room1Data.addProperty("Name", "Neu Schwanstein");
    room1Data.addProperty("Seats", new Integer(20));
    room1Data.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        validatingFacets(false).isKeyAutoGenerated(true).build());
    
    EntityCollection innerData = new EntityCollection();
    Entity data = new Entity();
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    room1Data.addNavigation("nr_Employees", innerData);
    Map<String, Object> link1 = new HashMap<String, Object>();
    link1.put("Id", 1);
    room1Data.addNavigation("nr_Building", link1);
    roomsData.addEntity(room1Data);
    
    Entity room2Data = new Entity();
    room2Data.addProperty("Name", "John");
    room2Data.addProperty("Seats", new Integer(10));
    room2Data.setWriteProperties(EntitySerializerProperties.serviceRoot(URI.create(BASE_URI)).
        validatingFacets(false).isKeyAutoGenerated(true).build());
    
    innerData = new EntityCollection();
    data = new Entity();
    data.addProperty("EmployeeName", "EmpName2");
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    room2Data.addNavigation("nr_Employees", innerData);
    Map<String, Object> link2 = new HashMap<String, Object>();
    link2.put("Id", 2);
    room2Data.addNavigation("nr_Building", link2);
    roomsData.addEntity(room2Data);
    
    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new JsonSerializerDeserializer().writeFeed(entitySet, roomsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"results\":[{\"Name\":\"Neu Schwanstein\",\"Seats\":20,"
        + "\"nr_Employees\":[{\"EmployeeName\":\"EmpName1\",\"RoomId\":\"1\"},"
        + "{\"RoomId\":\"1\"}],\"nr_Building\":{\"__deferred\":"
        + "{\"uri\":\"http://host:80/service/Buildings('1')\"}}},"
        + "{\"Name\":\"John\",\"Seats\":10,"
        + "\"nr_Employees\":[{\"EmployeeName\":\"EmpName2\",\"RoomId\":\"2\"},"
        + "{\"RoomId\":\"2\"}],\"nr_Building\":{\"__deferred\":"
        + "{\"uri\":\"http://host:80/service/Buildings('2')\"}}}]}", json);
  }

}
