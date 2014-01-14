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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallback;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.ep.JsonEntityProvider;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

public class JsonFeedWithDeltaLinkProducerTest extends BaseTest {

  protected static final String BASE_URI = "http://host:80/service/";
  protected static final EntityProviderWriteProperties DEFAULT_PROPERTIES =
      EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).build();

  private ArrayList<Map<String, Object>> deletedRoomData;
  protected ArrayList<Map<String, Object>> roomsData;

  private Gson gson = new Gson();

  private void initializeRoomData() {
    Map<String, Object> roomData1 = new HashMap<String, Object>();
    roomData1.put("Id", "1");
    roomData1.put("Seats", 123);
    roomData1.put("Version", 1);
    Map<String, Object> roomData2 = new HashMap<String, Object>();
    roomData2.put("Id", "2");
    roomData2.put("Seats", 66);
    roomData2.put("Version", 2);

    roomsData = new ArrayList<Map<String, Object>>();
    roomsData.add(roomData1);
    roomsData.add(roomData2);
  }

  private void initializeDeletedRoomData() {
    deletedRoomData = new ArrayList<Map<String, Object>>();
    for (int i = roomsData.size() + 1; i <= roomsData.size() + 1 + 2; i++) {
      HashMap<String, Object> tmp = new HashMap<String, Object>();
      tmp.put("Id", "" + i);
      tmp.put("Name", "Neu Schwanstein" + i);
      tmp.put("Seats", new Integer(20));
      tmp.put("Version", new Integer(3));
      deletedRoomData.add(tmp);
    }
  }

  @Before
  public void before() {
    initializeRoomData();
    initializeDeletedRoomData();
  }

  @Test
  public void deltaLink() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    TombstoneCallback tombstoneCallback =
        new TombstoneCallbackImpl(null, BASE_URI + "Rooms?!deltatoken=1234");

    final String json = writeRoomData(entitySet, tombstoneCallback);

    assertTrue("Delta Link missing or wrong!", json
        .contains("__delta\":\"http://host:80/service/Rooms?!deltatoken=1234"));
  }

  @Test
  public void deletedEntries() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    TombstoneCallback tombstoneCallback =
        new TombstoneCallbackImpl(deletedRoomData, null);

    final String json = writeRoomData(entitySet, tombstoneCallback);

    assertDeletedEntries(json);
  }

  @Test
  public void deletedEntriesAndDeltaLink() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    TombstoneCallback tombstoneCallback =
        new TombstoneCallbackImpl(deletedRoomData, BASE_URI + "Rooms?!deltatoken=1234");

    final String json = writeRoomData(entitySet, tombstoneCallback);

    assertTrue("Delta Link missing or wrong!", json
        .contains("__delta\":\"http://host:80/service/Rooms?!deltatoken=1234"));
    assertDeletedEntries(json);
  }

  private void assertDeletedEntries(final String json) {
    assertTrue("Somthing wrong with @odata.context!", json
        .contains("{\"@odata.context\":\"$metadata#Rooms/$deletedEntity\",\""));
    assertTrue(
        "Somthing wrong with deleted entry!",
        json.contains("{\"@odata.context\":\"$metadata#Rooms/$deletedEntity\",\"id\":\"http://host:80/service/Rooms('3')\"}"));
    assertTrue(
        "Somthing wrong with deleted entry!",
        json.contains("{\"@odata.context\":\"$metadata#Rooms/$deletedEntity\",\"id\":\"http://host:80/service/Rooms('4')\"}"));
    assertTrue(
        "Somthing wrong with deleted entry!",
        json.contains("{\"@odata.context\":\"$metadata#Rooms/$deletedEntity\",\"id\":\"http://host:80/service/Rooms('5')\"}"));
  }

  private String writeRoomData(final EdmEntitySet entitySet, TombstoneCallback tombstoneCallback)
      throws URISyntaxException, EntityProviderException, IOException {
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put(TombstoneCallback.CALLBACK_KEY_TOMBSTONE, tombstoneCallback);

    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(new URI(BASE_URI)).callbacks(callbacks).build();

    final ODataResponse response = new JsonEntityProvider().writeFeed(entitySet, roomsData,
        properties);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);

    validate(json);

    return json;
  }

  private void validate(String json) {
    Object obj = gson.fromJson(json, Object.class);
    assertNotNull(obj);
  }

}
