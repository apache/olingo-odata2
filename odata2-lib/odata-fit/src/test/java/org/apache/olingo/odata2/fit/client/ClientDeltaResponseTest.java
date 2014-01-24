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
package org.apache.olingo.odata2.fit.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySetInfo;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallback;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallbackResult;
import org.apache.olingo.odata2.api.ep.entry.DeletedEntryMetadata;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;
import org.apache.olingo.odata2.core.processor.ODataSingleProcessorService;
import org.apache.olingo.odata2.fit.client.util.Client;
import org.apache.olingo.odata2.ref.edm.ScenarioEdmProvider;
import org.apache.olingo.odata2.testutil.fit.AbstractFitTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ClientDeltaResponseTest extends AbstractFitTest {

  private static final String DELTATOKEN_1234 = "!deltatoken=1234";

  private static int roomDataCount = 2;
  private static int deletedRoomDataCount = 2;

  private Client client;
  StubProcessor processor;

  @Before
  @Override
  public void before() {
    super.before();
    try {
      client = new Client(getEndpoint().toASCIIString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected ODataService createService() throws ODataException {
    EdmProvider provider = new ScenarioEdmProvider();
    processor = new StubProcessor();

    return new ODataSingleProcessorService(provider, processor);
  }

  private class StubProcessor extends ODataSingleProcessor {

    @Override
    public ODataResponse readEntitySet(final GetEntitySetUriInfo uriInfo, final String contentType)
        throws ODataException {
      try {
        ArrayList<Map<String, Object>> deletedRoomData = null;
        ODataResponse response = null;
        EntityProviderWriteProperties properties = null;

        URI requestUri = getContext().getPathInfo().getRequestUri();

        if (requestUri.getQuery() != null && requestUri.getQuery().contains(DELTATOKEN_1234)) {
          deletedRoomData = createDeletedRoomData();
        }

        URI deltaLink;
        deltaLink =
            new URI(requestUri.getScheme(), requestUri.getUserInfo(), requestUri.getHost(), requestUri.getPort(),
                requestUri.getPath(), DELTATOKEN_1234, requestUri.getFragment());

        TombstoneCallback tombstoneCallback =
            new TombstoneCallbackImpl(deletedRoomData, deltaLink.toASCIIString());

        HashMap<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
        callbacks.put(TombstoneCallback.CALLBACK_KEY_TOMBSTONE, tombstoneCallback);

        properties =
            EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).callbacks(callbacks)
                .build();

        response = EntityProvider.writeFeed(contentType, uriInfo.getTargetEntitySet(), createRoomData(), properties);

        return response;
      } catch (URISyntaxException e) {
        throw new ODataRuntimeException(e);

      }
    }

    private ArrayList<Map<String, Object>> createRoomData() {
      ArrayList<Map<String, Object>> roomsData = new ArrayList<Map<String, Object>>();

      for (int i = 1; i <= roomDataCount; i++) {
        Map<String, Object> roomData = new HashMap<String, Object>();
        roomData.put("Id", String.valueOf(i));
        roomData.put("Seats", i);
        roomData.put("Version", i);
        roomsData.add(roomData);
      }

      return roomsData;
    }

    private ArrayList<Map<String, Object>> createDeletedRoomData() {
      ArrayList<Map<String, Object>> deletedRoomData = new ArrayList<Map<String, Object>>();

      for (int i = roomDataCount + 1; i < roomDataCount + 1 + deletedRoomDataCount; i++) {
        Map<String, Object> roomData = new HashMap<String, Object>();
        roomData.put("Id", String.valueOf(i));
        roomData.put("Seats", i);
        roomData.put("Version", i);

        deletedRoomData.add(roomData);
      }

      return deletedRoomData;
    }
  }

  @Test
  public void dummy() throws Exception {}

  @Test
  public void testEdm() throws Exception {
    Edm edm = client.getEdm();
    assertNotNull(edm);
    assertNotNull(edm.getDefaultEntityContainer());

    System.out.println(edm.getDefaultEntityContainer().getName());
  }

  @Test
  public void testEntitySets() throws Exception {
    List<EdmEntitySetInfo> sets = client.getEntitySets();
    assertNotNull(sets);
    assertEquals(6, sets.size());
  }

  private void testDeltaFeedWithDeltaLink(final String contentType) throws Exception {
    roomDataCount = 3;
    deletedRoomDataCount = 4;

    ODataFeed feed = client.readFeed("Container1", "Rooms", contentType);
    String deltaLink = feed.getFeedMetadata().getDeltaLink();

    assertNotNull(feed);
    assertEquals(roomDataCount, feed.getEntries().size());
    assertEquals(getEndpoint().toASCIIString() + "Rooms?" + DELTATOKEN_1234, feed.getFeedMetadata().getDeltaLink());

    ODataDeltaFeed deltaFeed = client.readDeltaFeed("Container1", "Rooms", contentType, deltaLink);

    assertNotNull(deltaFeed);
    assertEquals(roomDataCount, deltaFeed.getEntries().size());
    assertEquals(deltaLink, deltaFeed.getFeedMetadata().getDeltaLink());

    List<DeletedEntryMetadata> deletedEntries = deltaFeed.getDeletedEntries();
    assertNotNull(deletedEntries);
    assertEquals(deletedRoomDataCount, deletedEntries.size());

    for (int i = 0; i < deletedRoomDataCount; i++) {
      assertEquals("http://localhost:19000/abc/ClientDeltaResponseTest/Rooms('" + (roomDataCount + i + 1) + "')",
          deletedEntries.get(i).getUri());
      assertNotNull(deletedEntries.get(i).getWhen());
    }
  }

  private void testDeltaFeedWithZeroEntries(final String contentType) throws Exception {
    roomDataCount = 0;
    deletedRoomDataCount = 0;

    ODataFeed feed = client.readFeed("Container1", "Rooms", contentType);
    String deltaLink = feed.getFeedMetadata().getDeltaLink();

    assertNotNull(feed);
    assertEquals(roomDataCount, feed.getEntries().size());
    assertEquals(getEndpoint().toASCIIString() + "Rooms?" + DELTATOKEN_1234, feed.getFeedMetadata().getDeltaLink());

    ODataDeltaFeed deltaFeed = client.readDeltaFeed("Container1", "Rooms", contentType, deltaLink);

    assertNotNull(deltaFeed);
    assertEquals(roomDataCount, deltaFeed.getEntries().size());
    assertEquals(deltaLink, deltaFeed.getFeedMetadata().getDeltaLink());

    List<DeletedEntryMetadata> deletedEntries = deltaFeed.getDeletedEntries();
    assertNotNull(deletedEntries);
    assertEquals(deletedRoomDataCount, deletedEntries.size());
  }

  @Test
  public void testDeltaFeedWithDeltaLinkXml() throws Exception {
    testDeltaFeedWithDeltaLink("application/atom+xml");
  }

  @Test
  @Ignore
  public void testFeedWithDeltaLinkJson() throws Exception {
    testDeltaFeedWithDeltaLink("application/json");
  }

  @Test
  public void testDeltaFeedWithZeroEntriesXml() throws Exception {
    testDeltaFeedWithZeroEntries("application/atom+xml");
  }

  @Test
  @Ignore
  public void testFeedWithZeroEntriesJson() throws Exception {
    testDeltaFeedWithZeroEntries("application/json");
  }

  static public class TombstoneCallbackImpl implements TombstoneCallback {

    private ArrayList<Map<String, Object>> deletedEntriesData;
    private String deltaLink = null;

    public TombstoneCallbackImpl(final ArrayList<Map<String, Object>> deletedEntriesData, final String deltaLink) {
      this.deletedEntriesData = deletedEntriesData;
      this.deltaLink = deltaLink;
    }

    @Override
    public TombstoneCallbackResult getTombstoneCallbackResult() {
      TombstoneCallbackResult result = new TombstoneCallbackResult();
      result.setDeletedEntriesData(deletedEntriesData);
      result.setDeltaLink(deltaLink);
      return result;
    }

  }

}