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
package org.apache.olingo.odata2.core.ep.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.EntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.MediaMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

/**
 *  
 */
public class JsonEntryConsumerTest extends AbstractConsumerTest {

  private static final String SIMPLE_ENTRY_BUILDING = "JsonBuilding.json";
  private static final String SIMPLE_ENTRY_ROOM = "JsonRoom.json";
  private static final String SIMPLE_ENTRY_EMPLOYEE = "JsonEmployee.json";
  private static final String SIMPLE_ENTRY_TEAM = "JsonTeam.json";
  private static final String INVALID_ENTRY_TEAM_DOUBLE_NAME_PROPERTY = "JsonInvalidTeamDoubleNameProperty.json";
  private static final String SIMPLE_ENTRY_BUILDING_WITHOUT_D = "JsonBuildingWithoutD.json";

  // Negative Test jsonStart
  private static final String negativeJsonStart_1 = "{ \"abc\": {";
  private static final String negativeJsonStart_2 = "{ \"d\": [a: 1, b: 2] }";

  @Test
  public void readContentOnlyEmployee() throws Exception {
    // prepare
    String content = readFile("JsonEmployeeContentOnly.json");
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataEntry result =
        xec.readEntry(entitySet, contentBody, EntityProviderReadProperties.init().mergeSemantic(true).build());

    // verify
    assertEquals(9, result.getProperties().size());
  }

  @Test
  public void readContentOnlyRoom() throws Exception {
    // prepare
    String content = readFile("JsonRoomContentOnly.json");
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataEntry result =
        xec.readEntry(entitySet, contentBody, EntityProviderReadProperties.init().mergeSemantic(true).build());

    // verify
    assertEquals(4, result.getProperties().size());
  }

  @Test
  public void readContentOnlyEmployeeWithAdditionalLink() throws Exception {
    // prepare
    String content = readFile("JsonEmployeeContentOnlyWithAdditionalLink.json");
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataEntry result =
        xec.readEntry(entitySet, contentBody, EntityProviderReadProperties.init().mergeSemantic(true).build());

    // verify
    assertEquals(9, result.getProperties().size());
    List<String> associationUris = result.getMetadata().getAssociationUris("ne_Manager");
    assertEquals(1, associationUris.size());
    assertEquals("http://host:8080/ReferenceScenario.svc/Managers('1')", associationUris.get(0));
  }

  @Test
  public void readContentOnlyRoomWithAdditionalLink() throws Exception {
    // prepare
    String content = readFile("JsonRoomContentOnlyWithAdditionalLink.json");
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataEntry result =
        xec.readEntry(entitySet, contentBody, EntityProviderReadProperties.init().mergeSemantic(true).build());

    // verify
    assertEquals(4, result.getProperties().size());
    List<String> associationUris = result.getMetadata().getAssociationUris("nr_Building");
    assertEquals(1, associationUris.size());
    assertEquals("http://host:8080/ReferenceScenario.svc/Buildings('1')", associationUris.get(0));
  }

  @Test(expected = EntityProviderException.class)
  public void doubleClosingBracketsAtTheEnd() throws Exception {
    String invalidJson = "{ \"Id\" : \"1\", \"Seats\" : 1, \"Version\" : 1}}";
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream contentBody = createContentAsStream(invalidJson);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    xec.readEntry(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test
  public void readSimpleRoomEntry() throws Exception {
    ODataEntry roomEntry = prepareAndExecuteEntry(SIMPLE_ENTRY_ROOM, "Rooms", DEFAULT_PROPERTIES);

    // verify
    Map<String, Object> properties = roomEntry.getProperties();
    assertEquals(4, properties.size());

    assertEquals("1", properties.get("Id"));
    assertEquals("Room 1", properties.get("Name"));
    assertEquals((short) 1, properties.get("Seats"));
    assertEquals((short) 1, properties.get("Version"));

    List<String> associationUris = roomEntry.getMetadata().getAssociationUris("nr_Employees");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Rooms('1')/nr_Employees", associationUris.get(0));

    associationUris = roomEntry.getMetadata().getAssociationUris("nr_Building");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Rooms('1')/nr_Building", associationUris.get(0));

    EntryMetadata metadata = roomEntry.getMetadata();
    assertEquals("W/\"1\"", metadata.getEtag());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void readSimpleEmployeeEntry() throws Exception {
    ODataEntry result = prepareAndExecuteEntry(SIMPLE_ENTRY_EMPLOYEE, "Employees", DEFAULT_PROPERTIES);

    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(9, properties.size());

    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    assertEquals(Integer.valueOf(52), properties.get("Age"));
    Calendar entryDate = (Calendar) properties.get("EntryDate");
    assertEquals(915148800000L, entryDate.getTimeInMillis());
    assertEquals(TimeZone.getTimeZone("GMT"), entryDate.getTimeZone());
    assertEquals("Employees('1')/$value", properties.get("ImageUrl"));

    List<String> associationUris = result.getMetadata().getAssociationUris("ne_Manager");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Employees('1')/ne_Manager", associationUris.get(0));

    associationUris = result.getMetadata().getAssociationUris("ne_Team");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Employees('1')/ne_Team", associationUris.get(0));

    associationUris = result.getMetadata().getAssociationUris("ne_Room");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Employees('1')/ne_Room", associationUris.get(0));

    MediaMetadata mediaMetadata = result.getMediaMetadata();
    assertEquals("image/jpeg", mediaMetadata.getContentType());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Employees('1')/$value", mediaMetadata.getEditLink());
    assertEquals("Employees('1')/$value", mediaMetadata.getSourceLink());
    assertNull(mediaMetadata.getEtag());
  }

  @Test
  public void readSimpleTeamEntry() throws Exception {
    ODataEntry result = prepareAndExecuteEntry(SIMPLE_ENTRY_TEAM, "Teams", DEFAULT_PROPERTIES);

    Map<String, Object> properties = result.getProperties();
    assertNotNull(properties);
    assertEquals("1", properties.get("Id"));
    assertEquals("Team 1", properties.get("Name"));
    assertEquals(Boolean.FALSE, properties.get("isScrumTeam"));
    assertNull(properties.get("nt_Employees"));

    List<String> associationUris = result.getMetadata().getAssociationUris("nt_Employees");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Teams('1')/nt_Employees", associationUris.get(0));

    checkMediaDataInitial(result.getMediaMetadata());
  }

  @Test
  public void readSimpleBuildingEntry() throws Exception {
    ODataEntry result = prepareAndExecuteEntry(SIMPLE_ENTRY_BUILDING, "Buildings", DEFAULT_PROPERTIES);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertNotNull(properties);
    assertEquals("1", properties.get("Id"));
    assertEquals("Building 1", properties.get("Name"));
    assertNull(properties.get("Image"));
    assertNull(properties.get("nb_Rooms"));

    List<String> associationUris = result.getMetadata().getAssociationUris("nb_Rooms");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Buildings('1')/nb_Rooms", associationUris.get(0));

    checkMediaDataInitial(result.getMediaMetadata());
  }

  @Test
  public void readSimpleBuildingEntryWithoutD() throws Exception {
    ODataEntry result = prepareAndExecuteEntry(SIMPLE_ENTRY_BUILDING_WITHOUT_D, "Buildings", DEFAULT_PROPERTIES);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertNotNull(properties);
    assertEquals("1", properties.get("Id"));
    assertEquals("Building 1", properties.get("Name"));
    assertNull(properties.get("Image"));
    assertNull(properties.get("nb_Rooms"));

    List<String> associationUris = result.getMetadata().getAssociationUris("nb_Rooms");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Buildings('1')/nb_Rooms", associationUris.get(0));

    checkMediaDataInitial(result.getMediaMetadata());
  }

  @Test
  public void readMinimalEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    final ODataEntry result =
        new JsonEntityConsumer().readEntry(entitySet, createContentAsStream("{\"Id\":\"99\"}"), DEFAULT_PROPERTIES);

    final Map<String, Object> properties = result.getProperties();
    assertNotNull(properties);
    assertEquals(1, properties.size());
    assertEquals("99", properties.get("Id"));

    assertTrue(result.getMetadata().getAssociationUris("nt_Employees").isEmpty());
    checkMediaDataInitial(result.getMediaMetadata());
  }

  @Test
  public void readEntryWithNullProperty() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    final String content = "{\"Id\":\"99\",\"Seats\":null}";

    for (final boolean merge : new boolean[] { false, true }) {
      final ODataEntry result = new JsonEntityConsumer().readEntry(entitySet, createContentAsStream(content),
          EntityProviderReadProperties.init().mergeSemantic(merge).build());

      final Map<String, Object> properties = result.getProperties();
      assertNotNull(properties);
      assertEquals(2, properties.size());
      assertEquals("99", properties.get("Id"));
      assertTrue(properties.containsKey("Seats"));
      assertNull(properties.get("Seats"));

      assertTrue(result.getMetadata().getAssociationUris("nr_Employees").isEmpty());
      checkMediaDataInitial(result.getMediaMetadata());
    }
  }

  @Test
  public void readWithDoublePropertyOnTeam() throws Exception {
    // The file contains the name property two times
    try {
      prepareAndExecuteEntry(INVALID_ENTRY_TEAM_DOUBLE_NAME_PROPERTY, "Teams", DEFAULT_PROPERTIES);
      fail("Exception has to be thrown");
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.DOUBLE_PROPERTY.getKey(), e.getMessageReference().getKey());
    }
  }

  @Test
  public void entryWithMetadataElementProperties() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream(
        "{\"__metadata\":{\"properties\":{\"nt_Employees\":{\"associationuri\":"
            + "\"http://some.host.com/service.root/Teams('1')/$links/nt_Employees\"}}},"
            + "\"Id\":\"1\"}");
    ODataEntry result = new JsonEntityConsumer().readEntry(entitySet, contentBody, DEFAULT_PROPERTIES);
    checkMediaDataInitial(result.getMediaMetadata());
  }

  private void checkMediaDataInitial(final MediaMetadata mediaMetadata) {
    assertNull(mediaMetadata.getContentType());
    assertNull(mediaMetadata.getEditLink());
    assertNull(mediaMetadata.getEtag());
    assertNull(mediaMetadata.getSourceLink());
  }

  @Test(expected = EntityProviderException.class)
  public void emptyEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    new JsonEntityConsumer().readEntry(entitySet, createContentAsStream("{}"), DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void wrongStart() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream(negativeJsonStart_1);
    new JsonEntityConsumer().readEntry(entitySet, contentBody, DEFAULT_PROPERTIES);
  }

  @Test(expected = EntityProviderException.class)
  public void wrongStart2() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream contentBody = createContentAsStream(negativeJsonStart_2);
    new JsonEntityConsumer().readEntry(entitySet, contentBody, DEFAULT_PROPERTIES);
  }
}
