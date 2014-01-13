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
package org.apache.olingo.odata2.core.ep;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

public class ProducerConsumerIntegrationTest {
  protected static final URI BASE_URI;

  static {
    try {
      BASE_URI = new URI("http://host:80/service/");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
  private static final EntityProviderReadProperties DEFAULT_READ_PROPERTIES = EntityProviderReadProperties.init()
      .build();
  private static final EntityProviderWriteProperties DEFAULT_WRITE_PROPERTIES = EntityProviderWriteProperties
      .serviceRoot(
          BASE_URI).build();
  private static final String XML = "application/xml";
  private static final String JSON = "application/json";

  @Test
  public void produceRoomAndThenConsumeIt() throws Exception {
    EdmEntitySet roomSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> localRoomData = new HashMap<String, Object>();
    localRoomData.put("Id", "1");
    localRoomData.put("Name", "Neu \n Schwanstein蝴蝶");

    Map<String, Object> properties = execute(localRoomData, roomSet, XML);
    assertEquals("1", properties.get("Id"));
    assertEquals("Neu \n Schwanstein蝴蝶", properties.get("Name"));

    Map<String, Object> properties2 = execute(localRoomData, roomSet, JSON);
    assertEquals("1", properties2.get("Id"));
    assertEquals("Neu \n Schwanstein蝴蝶", properties2.get("Name"));
  }

  private Map<String, Object> execute(Map<String, Object> localRoomData, EdmEntitySet roomSet, String contentType)
      throws EntityProviderException {
    ODataResponse response = EntityProvider.writeEntry(contentType, roomSet, localRoomData, DEFAULT_WRITE_PROPERTIES);
    InputStream content = (InputStream) response.getEntity();

    ODataEntry entry = EntityProvider.readEntry(contentType, roomSet, content, DEFAULT_READ_PROPERTIES);
    Map<String, Object> properties = entry.getProperties();
    return properties;
  }

}
