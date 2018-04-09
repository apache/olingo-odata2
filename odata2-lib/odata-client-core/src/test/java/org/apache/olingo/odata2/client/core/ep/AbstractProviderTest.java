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
package org.apache.olingo.odata2.client.core.ep;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallback;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.client.api.ep.Entity;
import org.apache.olingo.odata2.client.api.ep.EntityCollection;
import org.apache.olingo.odata2.client.api.ep.EntitySerializerProperties;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  
*/
public abstract class AbstractProviderTest extends AbstractXmlProducerTestHelper{


  public AbstractProviderTest(final StreamWriterImplType type) {
    super(type);
  }

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  protected static final URI BASE_URI;

  static {
    try {
      BASE_URI = new URI("http://host:80/service/");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
  protected static final EntitySerializerProperties DEFAULT_PROPERTIES =
      EntitySerializerProperties.serviceRoot(
      BASE_URI).includeMetadata(true).build();

  protected Entity employeeData;

  protected EntityCollection employeesData;

  protected Entity photoData;

  protected Entity roomData;

  protected Entity buildingData;

  protected EntityCollection roomsData;

  {
    employeeData = new Entity();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    employeeData.addProperty("EmployeeId", "1");
    employeeData.addProperty("ImmageUrl", null);
    employeeData.addProperty("ManagerId", "1");
    employeeData.addProperty("Age", new Integer(52));
    employeeData.addProperty("RoomId", "1");
    employeeData.addProperty("EntryDate", date);
    employeeData.addProperty("TeamId", "42");
    employeeData.addProperty("EmployeeName", "Walter Winter");
    // employeeData.put("getImageType", "abc");

    Entity locationData = new Entity();
    Entity cityData = new Entity();
    cityData.addProperty("PostalCode", "33470");
    cityData.addProperty("CityName", "Duckburg");
    locationData.addProperty("City", cityData);
    locationData.addProperty("Country", "Calisota");

    employeeData.addProperty("Location", locationData);

    Entity employeeData2 = new Entity();
    employeeData2.addProperty("EmployeeId", "1");
    employeeData2.addProperty("ImmageUrl", null);
    employeeData2.addProperty("ManagerId", "1");
    employeeData2.addProperty("Age", new Integer(52));
    employeeData2.addProperty("RoomId", "1");
    employeeData2.addProperty("EntryDate", date);
    employeeData2.addProperty("TeamId", "42");
    employeeData2.addProperty("EmployeeName", "Walter Winter");

    Entity locationData2 = new Entity();
    Entity cityData2 = new Entity();
    cityData2.addProperty("PostalCode", "33470");
    cityData2.addProperty("CityName", "Duckburg");
    locationData2.addProperty("City", cityData2);
    locationData2.addProperty("Country", "Calisota");

    employeeData2.addProperty("Location", locationData2);

    employeesData = new EntityCollection();
    employeesData.addEntity(employeeData);
    employeesData.addEntity(employeeData2);

    photoData = new Entity();
    photoData.addProperty("Id", Integer.valueOf(1));
    photoData.addProperty("Name", "Mona Lisa");
    photoData.addProperty("Type", "image/png");
    photoData
        .addProperty(
            "ImageUrl",
            "http://www.mopo.de/image/view/2012/6/4/16548086,13385561,medRes,maxh,234,maxw,234," +
                "Parodia_Mona_Lisa_Lego_Hamburger_Morgenpost.jpg");
    Entity imageData = new Entity();
    imageData.addProperty("Image", new byte[] { 1, 2, 3, 4 });
    imageData.addProperty("getImageType", "image/png");
    photoData.addProperty("Image", imageData);
    photoData.addProperty("BinaryData", new byte[] { -1, -2, -3, -4 });
    photoData.addProperty("Содержание", "В лесу шумит водопад. Если он не торопится просп воды");

    roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    roomData.addProperty("Version", new Integer(3));

    buildingData = new Entity();
    buildingData.addProperty("Id", "1");
    buildingData.addProperty("Name", "WDF03");
    buildingData.addProperty("Image", "image");
  }

  protected void initializeRoomData(final int count) {
    roomsData = new EntityCollection();
    for (int i = 1; i <= count; i++) {
      Entity tmp = new Entity();
      tmp.addProperty("Id", "" + i);
      tmp.addProperty("Name", "Neu Schwanstein" + i);
      tmp.addProperty("Seats", new Integer(20));
      tmp.addProperty("Version", new Integer(3));
      roomsData.addEntity(tmp);
    }
  }

  @Before
  public void setXmlNamespacePrefixes() throws Exception {
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_ATOM_2005);
    prefixMap.put("d", Edm.NAMESPACE_D_2007_08);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    prefixMap.put("xml", Edm.NAMESPACE_XML_1998);
    prefixMap.put("Ãƒâ€˜Ã¢â€šÂ¬Ãƒâ€˜Ã†â€™", "http://localhost");
    prefixMap.put("custom", "http://localhost");
    prefixMap.put("at", TombstoneCallback.NAMESPACE_TOMBSTONE);
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
  }

  protected ODataContext createContextMock() throws ODataException {
    PathInfo pathInfo = mock(PathInfo.class);
    when(pathInfo.getServiceRoot()).thenReturn(BASE_URI);
    ODataContext ctx = mock(ODataContext.class);
    when(ctx.getPathInfo()).thenReturn(pathInfo);
    return ctx;
  }

  protected AtomSerializerDeserializer createAtomEntityProvider() throws EntityProviderException {
    return new AtomSerializerDeserializer();
  }

  public Entity getEmployeeData() {
    return employeeData;
  }

  public EntityCollection getEmployeesData() {
    return employeesData;
  }

  public Entity getRoomData() {
    return roomData;
  }

  public EntityCollection getRoomsData() {
    return roomsData;
  }


}
