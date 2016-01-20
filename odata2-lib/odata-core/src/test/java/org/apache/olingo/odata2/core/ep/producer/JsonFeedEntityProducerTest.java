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

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.ep.JsonEntityProvider;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

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
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).clientRequest(true).build();
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
}
