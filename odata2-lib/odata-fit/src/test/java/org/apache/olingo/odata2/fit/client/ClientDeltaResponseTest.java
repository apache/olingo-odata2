package org.apache.olingo.odata2.fit.client;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.apache.olingo.odata2.api.ep.entry.EntryMetadata;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.core.ep.producer.TombstoneCallbackImpl;
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

  private Client client;

  private ArrayList<Map<String, Object>> createRoomData() {

    Map<String, Object> roomData1 = new HashMap<String, Object>();
    roomData1.put("Id", "1");
    roomData1.put("Seats", 123);
    roomData1.put("Version", 1);
    Map<String, Object> roomData2 = new HashMap<String, Object>();
    roomData2.put("Id", "2");
    roomData2.put("Seats", 66);
    roomData2.put("Version", 2);

    ArrayList<Map<String, Object>> roomsData = new ArrayList<Map<String, Object>>();
    roomsData.add(roomData1);
    roomsData.add(roomData2);

    return roomsData;
  }

  private ArrayList<Map<String, Object>> createDeletedRoomData() {
    Map<String, Object> roomData1 = new HashMap<String, Object>();
    roomData1.put("Id", "3");
    roomData1.put("Seats", 123);
    roomData1.put("Version", 1);
    Map<String, Object> roomData2 = new HashMap<String, Object>();
    roomData2.put("Id", "4");
    roomData2.put("Seats", 66);
    roomData2.put("Version", 2);

    ArrayList<Map<String, Object>> deletedRoomData = new ArrayList<Map<String, Object>>();
    deletedRoomData.add(roomData1);
    deletedRoomData.add(roomData2);
    return deletedRoomData;
  }

  @Before
  @Override
  public void before() {
    super.before();
    try {
      client = new Client(getEndpoint().toASCIIString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    createRoomData();
  }

  private class StubProcessor extends ODataSingleProcessor {

    @Override
    public ODataResponse readEntitySet(GetEntitySetUriInfo uriInfo, String contentType) throws ODataException {
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
  }

  @Override
  protected ODataService createService() throws ODataException {
    EdmProvider provider = new ScenarioEdmProvider();
    ODataSingleProcessor processor = new StubProcessor();

    return new ODataSingleProcessorService(provider, processor);
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

  private void testDeltaFeedWithDeltaLink(String contentType) throws Exception {
    ODataFeed feed = client.readFeed("Container1", "Rooms", contentType);
    String deltaLink = feed.getFeedMetadata().getDeltaLink();

    assertNotNull(feed);
    assertEquals(2, feed.getEntries().size());
    assertEquals(getEndpoint().toASCIIString() + "Rooms?" + DELTATOKEN_1234, feed.getFeedMetadata().getDeltaLink());
    assertFalse(feed.isDeltaFeed());

    URI uri = new URI(deltaLink);
    String query = uri.getQuery();
    ODataFeed deltaFeed = client.readFeed("Container1", "Rooms", contentType, query);

    assertNotNull(deltaFeed);
    assertEquals(2, deltaFeed.getEntries().size());
    assertEquals(uri.toASCIIString(), deltaFeed.getFeedMetadata().getDeltaLink());

    assertTrue(deltaFeed.isDeltaFeed());
    List<EntryMetadata> deletedEntries = deltaFeed.getDeletedEntries();
    assertNotNull(deletedEntries);
    assertEquals(2, deletedEntries.size());

    assertEquals("3", deletedEntries.get(0).getId());
    assertEquals("uri3", deletedEntries.get(0).getUri());

    assertEquals("4", deletedEntries.get(1).getId());
    assertEquals("uri4", deletedEntries.get(1).getUri());
  }

  @Test
  public void testDeltaFeedWithDeltaLinkXml() throws Exception {
    testDeltaFeedWithDeltaLink("application/atom+xml");
  }

  @Test
  @Ignore("not implemented")
  public void testFeedWithDeltaLinkJson() throws Exception {
    testDeltaFeedWithDeltaLink("application/json");
  }

}