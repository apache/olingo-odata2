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
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.FeedMetadata;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.client.api.ep.EntityStream;
import org.apache.olingo.odata2.client.core.ep.deserializer.JsonEntityDeserializer;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

/**
 *  
 */
public class JsonEntryDeepInsertFeedTest extends AbstractDeserializerTest {

  private static final String BUILDING_WITH_INLINE_ROOMS = "JsonBuildingWithInlineRooms.json";
  private static final String BUILDING_WITH_INLINE_ROOMS_CLIENT_CASE = "JsonBuildingWithInlineRoomsClientCase.json";
  private static final String TEAM_WITH_INLINE_EMPLOYEES = "JsonTeamsWithInlineEmployees.json";
  private static final String BUILDING_WITH_INLINE_ROOMS_NEXTLINK_AND_COUNT =
      "JsonBuildingWithInlineRoomsAndNextLinkAndCount.json";

  @Test
  public void innerFeedWithoutResultsWrapperClientUseCase() throws Exception {
    ODataEntry outerEntry = prepareAndExecuteEntry(
        BUILDING_WITH_INLINE_ROOMS_CLIENT_CASE, "Buildings", DEFAULT_PROPERTIES);

    ODataFeed innerRoomFeed = (ODataFeed) outerEntry.getProperties().get("nb_Rooms");
    assertNotNull(innerRoomFeed);

    List<ODataEntry> rooms = innerRoomFeed.getEntries();
    assertNotNull(rooms);
    assertEquals(1, rooms.size());

    ODataEntry room = rooms.get(0);
    Map<String, Object> roomProperties = room.getProperties();

    assertEquals(4, roomProperties.size());
    assertEquals("1", roomProperties.get("Id"));
    assertEquals("Room 1", roomProperties.get("Name"));
    assertEquals(Short.valueOf("1"), roomProperties.get("Version"));
    assertEquals(Short.valueOf("1"), roomProperties.get("Seats"));

    List<String> associationUris = room.getMetadata().getAssociationUris("nr_Employees");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Rooms('1')/nr_Employees", associationUris.get(0));

    associationUris = room.getMetadata().getAssociationUris("nr_Building");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Rooms('1')/nr_Building", associationUris.get(0));
  }
  
  @Test
  public void innerFeedNoMediaResource() throws Exception {
    ODataEntry outerEntry = prepareAndExecuteEntry(BUILDING_WITH_INLINE_ROOMS, "Buildings", DEFAULT_PROPERTIES);

    ODataFeed innerRoomFeed = (ODataFeed) outerEntry.getProperties().get("nb_Rooms");
    assertNotNull(innerRoomFeed);

    List<ODataEntry> rooms = innerRoomFeed.getEntries();
    assertNotNull(rooms);
    assertEquals(1, rooms.size());

    ODataEntry room = rooms.get(0);
    Map<String, Object> roomProperties = room.getProperties();

    assertEquals(4, roomProperties.size());
    assertEquals("1", roomProperties.get("Id"));
    assertEquals("Room 1", roomProperties.get("Name"));
    assertEquals(Short.valueOf("1"), roomProperties.get("Version"));
    assertEquals(Short.valueOf("1"), roomProperties.get("Seats"));

    List<String> associationUris = room.getMetadata().getAssociationUris("nr_Employees");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Rooms('1')/nr_Employees", associationUris.get(0));

    associationUris = room.getMetadata().getAssociationUris("nr_Building");
    assertEquals(1, associationUris.size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Rooms('1')/nr_Building", associationUris.get(0));
  }

  @Test
  public void innerFeedNoMediaResourceContainsNextLinkAndCount() throws Exception {
    ODataEntry outerEntry =
        prepareAndExecuteEntry(BUILDING_WITH_INLINE_ROOMS_NEXTLINK_AND_COUNT, "Buildings", DEFAULT_PROPERTIES);

    ODataFeed innerRoomFeed = (ODataFeed) outerEntry.getProperties().get("nb_Rooms");
    assertNotNull(innerRoomFeed);

    List<ODataEntry> rooms = innerRoomFeed.getEntries();
    assertNotNull(rooms);
    assertEquals(1, rooms.size());

    FeedMetadata roomsMetadata = innerRoomFeed.getFeedMetadata();
    assertEquals(Integer.valueOf(1), roomsMetadata.getInlineCount());
    assertEquals("nextLink", roomsMetadata.getNextLink());
  }

  @Test
  public void innerFeedNoMediaResourceSimpleArray() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
    String content = "{\"d\":{\"Id\":\"1\",\"Name\":\"Building 1\","
        + "\"nb_Rooms\":[{\"Id\":\"1\",\"Name\":\"Room 1\"}]}}";
    InputStream contentBody = createContentAsStream(content);
    EntityStream entityStream = new EntityStream();
    entityStream.setContent(contentBody);
    entityStream.setReadProperties(DEFAULT_PROPERTIES);
    final ODataEntry outerEntry = new JsonEntityDeserializer().readEntry(entitySet, entityStream);
    assertNotNull(outerEntry);
    final ODataFeed innerRoomFeed = (ODataFeed) outerEntry.getProperties().get("nb_Rooms");
    assertNotNull(innerRoomFeed);

    final List<ODataEntry> rooms = innerRoomFeed.getEntries();
    assertNotNull(rooms);
    assertEquals(1, rooms.size());
  }

  @Test
  public void innerFeedMediaResource() throws Exception {
    ODataEntry outerEntry = prepareAndExecuteEntry(TEAM_WITH_INLINE_EMPLOYEES, "Teams", DEFAULT_PROPERTIES);

    ODataFeed innerEmployeeFeed = (ODataFeed) outerEntry.getProperties().get("nt_Employees");
    assertNotNull(innerEmployeeFeed);

    List<ODataEntry> employees = innerEmployeeFeed.getEntries();
    assertNotNull(employees);
    assertEquals(3, employees.size());
  }
}
