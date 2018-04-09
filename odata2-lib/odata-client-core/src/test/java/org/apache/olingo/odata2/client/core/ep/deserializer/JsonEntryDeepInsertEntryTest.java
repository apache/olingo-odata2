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
package org.apache.olingo.odata2.client.core.ep.deserializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.client.api.ep.DeserializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntityStream;
import org.apache.olingo.odata2.client.api.ep.callback.OnDeserializeInlineContent;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

public class JsonEntryDeepInsertEntryTest extends AbstractDeserializerTest {

  private static final String EMPLOYEE_WITH_INLINE_TEAM = "JsonEmployeeWithInlineTeam.json";
  private static final String INLINE_ROOM_WITH_INLINE_BUILDING = "JsonInlineRoomWithInlineBuilding.json";
  private static final String INLINE_ROOM_WITH_INLINE_NULL = "JsonInlineRoomWithInlineNull.json";

  @Test
  public void innerEntryNoMediaResource() throws Exception {
    ODataEntry outerEntry = prepareAndExecuteEntry(EMPLOYEE_WITH_INLINE_TEAM, "Employees", DEFAULT_PROPERTIES);
    assertTrue(outerEntry.containsInlineEntry());

    ODataEntry innerTeam = (ODataEntry) outerEntry.getProperties().get("ne_Team");
    assertNotNull(innerTeam);
    assertFalse(innerTeam.containsInlineEntry());

    Map<String, Object> innerTeamProperties = innerTeam.getProperties();

    assertEquals("1", innerTeamProperties.get("Id"));
    assertEquals("Team 1", innerTeamProperties.get("Name"));
    assertEquals(Boolean.FALSE, innerTeamProperties.get("isScrumTeam"));
    assertNull(innerTeamProperties.get("nt_Employees"));

    List<String> associationUris = innerTeam.getMetadata().getAssociationUris("nt_Employees");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Teams('1')/nt_Employees", associationUris.get(0));
  }

  @Test
  public void innerEntryWithOptionalNavigationProperty() throws Exception {
    // prepare
    DeserializerProperties readProperties =
        DeserializerProperties.init().build();
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    // modify edm for test case (change multiplicity to ZERO_TO_ONE)
    EdmType navigationType = mock(EdmType.class);
    when(navigationType.getKind()).thenReturn(EdmTypeKind.ENTITY);

    EdmNavigationProperty navigationProperty = mock(EdmNavigationProperty.class);
    when(navigationProperty.getName()).thenReturn("ne_Team");
    when(navigationProperty.getType()).thenReturn(navigationType);
    when(navigationProperty.getMultiplicity()).thenReturn(EdmMultiplicity.ZERO_TO_ONE);

    when(entitySet.getEntityType().getProperty("ne_Team")).thenReturn(navigationProperty);
    EdmEntitySet targetEntitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    when(entitySet.getRelatedEntitySet(navigationProperty)).thenReturn(targetEntitySet);

    // execute
    JsonEntityDeserializer xec = new JsonEntityDeserializer();
    InputStream contentBody = getFileAsStream(EMPLOYEE_WITH_INLINE_TEAM);
    EntityStream entityStream = new EntityStream();
    entityStream.setContent(contentBody);
    entityStream.setReadProperties(readProperties);
    ODataEntry outerEntry = xec.readEntry(entitySet, entityStream);

    ODataEntry innerTeam = (ODataEntry) outerEntry.getProperties().get("ne_Team");
    Map<String, Object> innerTeamProperties = innerTeam.getProperties();

    assertEquals("1", innerTeamProperties.get("Id"));
    assertEquals("Team 1", innerTeamProperties.get("Name"));
    assertEquals(Boolean.FALSE, innerTeamProperties.get("isScrumTeam"));
    assertNull(innerTeamProperties.get("nt_Employees"));

    List<String> associationUris = innerTeam.getMetadata().getAssociationUris("nt_Employees");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Teams('1')/nt_Employees", associationUris.get(0));
  }

  @Test
  public void inlineRoomWithInlineBuilding() throws Exception {
    OnDeserializeInlineContent callback = new Callback();
    DeserializerProperties readProperties = DeserializerProperties.init().callback(callback).build();
    ODataEntry outerEntry = prepareAndExecuteEntry(INLINE_ROOM_WITH_INLINE_BUILDING, "Employees", readProperties);
    assertTrue(outerEntry.containsInlineEntry());

    ODataEntry innerRoom = (ODataEntry) outerEntry.getProperties().get("ne_Room");
    assertNotNull(innerRoom);
    assertTrue(innerRoom.containsInlineEntry());

    Map<String, Object> innerRoomProperties = innerRoom.getProperties();

    assertEquals(5, innerRoomProperties.size());
    assertEquals("1", innerRoomProperties.get("Id"));
    assertEquals("Room 1", innerRoomProperties.get("Name"));
    assertEquals(Short.valueOf("1"), innerRoomProperties.get("Version"));
    assertEquals(Short.valueOf("1"), innerRoomProperties.get("Seats"));
    assertNull(innerRoomProperties.get("nr_Employees"));

    List<String> associationUris = innerRoom.getMetadata().getAssociationUris("nr_Employees");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Rooms('1')/nr_Employees", associationUris.get(0));

    associationUris = innerRoom.getMetadata().getAssociationUris("nr_Building");
    assertEquals(Collections.emptyList(), associationUris);

    assertEquals("W/\"1\"", innerRoom.getMetadata().getEtag());

    ODataEntry innerBuilding = (ODataEntry) innerRoomProperties.get("nr_Building");
    assertNotNull(innerBuilding);
    assertFalse(innerBuilding.containsInlineEntry());

    Map<String, Object> innerBuildingProperties = innerBuilding.getProperties();
    assertEquals(3, innerBuildingProperties.size());
    assertEquals("1", innerBuildingProperties.get("Id"));
    assertEquals("Building 1", innerBuildingProperties.get("Name"));
    assertEquals(null, innerBuildingProperties.get("Image"));
    assertNull(innerBuildingProperties.get("nb_Rooms"));

    associationUris = innerBuilding.getMetadata().getAssociationUris("nb_Rooms");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Buildings('1')/nb_Rooms", associationUris.get(0));
  }

  @Test
  public void inlineRoomWithInlineNull() throws Exception {
    DeserializerProperties readProperties =
        DeserializerProperties.init().build();
    ODataEntry outerEntry = prepareAndExecuteEntry(INLINE_ROOM_WITH_INLINE_NULL, "Employees", readProperties);

    ODataEntry innerRoom = (ODataEntry) outerEntry.getProperties().get("ne_Room");
    assertNull(innerRoom);
  }
  
}
