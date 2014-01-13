package org.apache.olingo.odata2.core.ep.producer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
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
import com.google.gson.internal.StringMap;
import com.google.gson.reflect.TypeToken;

public class JsonFeedWithDeltaLinkProducerTest extends BaseTest {

  protected static final String BASE_URI = "http://host:80/service/";
  protected static final EntityProviderWriteProperties DEFAULT_PROPERTIES =
      EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).build();

  private ArrayList<Map<String, Object>> deletedRoomData;
  protected ArrayList<Map<String, Object>> roomsData;

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
    for (int i = 2; i <= roomsData.size(); i = i + 2) {
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
        new TombstoneCallbackImpl(deletedRoomData, BASE_URI + "Rooms?!deltatoken=1234");

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

    assertTrue("Delta Link missing or wrong!", json.contains("__delta\":\"http://host:80/service/Rooms?!deltatoken=1234"));
    
  }

}
