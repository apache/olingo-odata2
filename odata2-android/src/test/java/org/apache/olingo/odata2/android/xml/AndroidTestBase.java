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
package org.apache.olingo.odata2.android.xml;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallback;
import org.apache.olingo.odata2.core.ep.AtomEntityProvider;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.apache.olingo.odata2.api.xml.XMLStreamReaderFactory.XML_STREAM_READER_FACTORY_CLASS;
import static org.apache.olingo.odata2.api.xml.XMLStreamWriterFactory.XML_STREAM_WRITER_FACTORY_CLASS;

/**
 *  
*/
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public abstract class AndroidTestBase {

  protected static final URI BASE_URI;

  static {
    try {
      BASE_URI = new URI("http://host:80/service/");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
  protected static final EntityProviderWriteProperties DEFAULT_PROPERTIES = EntityProviderWriteProperties.serviceRoot(
      BASE_URI).build();

  protected Map<String, Object> employeeData;

  protected ArrayList<Map<String, Object>> employeesData;

  protected Map<String, Object> photoData;

  protected Map<String, Object> roomData;

  protected Map<String, Object> buildingData;

  protected ArrayList<Map<String, Object>> roomsData;

  {
    employeeData = new HashMap<String, Object>();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    employeeData.put("EmployeeId", "1");
    employeeData.put("ImmageUrl", null);
    employeeData.put("ManagerId", "1");
    employeeData.put("Age", new Integer(52));
    employeeData.put("RoomId", "1");
    employeeData.put("EntryDate", date);
    employeeData.put("TeamId", "42");
    employeeData.put("EmployeeName", "Walter Winter");

    Map<String, Object> locationData = new HashMap<String, Object>();
    Map<String, Object> cityData = new HashMap<String, Object>();
    cityData.put("PostalCode", "33470");
    cityData.put("CityName", "Duckburg");
    locationData.put("City", cityData);
    locationData.put("Country", "Calisota");

    employeeData.put("Location", locationData);

    Map<String, Object> employeeData2 = new HashMap<String, Object>();
    employeeData2.put("EmployeeId", "1");
    employeeData2.put("ImmageUrl", null);
    employeeData2.put("ManagerId", "1");
    employeeData2.put("Age", new Integer(52));
    employeeData2.put("RoomId", "1");
    employeeData2.put("EntryDate", date);
    employeeData2.put("TeamId", "42");
    employeeData2.put("EmployeeName", "Walter Winter");

    Map<String, Object> locationData2 = new HashMap<String, Object>();
    Map<String, Object> cityData2 = new HashMap<String, Object>();
    cityData2.put("PostalCode", "33470");
    cityData2.put("CityName", "Duckburg");
    locationData2.put("City", cityData2);
    locationData2.put("Country", "Calisota");

    employeeData2.put("Location", locationData2);

    employeesData = new ArrayList<Map<String, Object>>();
    employeesData.add(employeeData);
    employeesData.add(employeeData2);

    photoData = new HashMap<String, Object>();
    photoData.put("Id", Integer.valueOf(1));
    photoData.put("Name", "Mona Lisa");
    photoData.put("Type", "image/png");
    photoData.put(
                "ImageUrl",
                "http://www.mopo.de/image/view/2012/6/4/16548086,13385561,medRes,maxh,234,maxw,234," +
                        "Parodia_Mona_Lisa_Lego_Hamburger_Morgenpost.jpg");
    Map<String, Object> imageData = new HashMap<String, Object>();
    imageData.put("Image", new byte[] { 1, 2, 3, 4 });
    imageData.put("getImageType", "image/png");
    photoData.put("Image", imageData);
    photoData.put("BinaryData", new byte[] { -1, -2, -3, -4 });
    photoData.put("Содержание", "В лесу шумит водопад. Если он не торопится просп воды");

    roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Name", "Neu Schwanstein");
    roomData.put("Seats", new Integer(20));
    roomData.put("Version", new Integer(3));

    buildingData = new HashMap<String, Object>();
    buildingData.put("Id", "1");
    buildingData.put("Name", "WDF03");
    buildingData.put("Image", "image");
  }

  protected void initializeRoomData(final int count) {
    roomsData = new ArrayList<Map<String, Object>>();
    for (int i = 1; i <= count; i++) {
      HashMap<String, Object> tmp = new HashMap<String, Object>();
      tmp.put("Id", "" + i);
      tmp.put("Name", "Neu Schwanstein" + i);
      tmp.put("Seats", new Integer(20));
      tmp.put("Version", new Integer(3));
      roomsData.add(tmp);
    }
  }

  // CHECKSTYLE:OFF
  @Before
  public void setXmlFactory() throws Exception {
    //
    System.setProperty(XML_STREAM_WRITER_FACTORY_CLASS, AndroidXmlFactory.class.getName()); // NOSONAR
    System.setProperty(XML_STREAM_READER_FACTORY_CLASS, AndroidXmlFactory.class.getName()); // NOSONAR
  }
  // CHECKSTYLE:ON

  @Before
  public void setXmlNamespacePrefixes() throws Exception {
    //
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_ATOM_2005);
    prefixMap.put("d", Edm.NAMESPACE_D_2007_08);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    prefixMap.put("xml", Edm.NAMESPACE_XML_1998);
    prefixMap.put("ру", "http://localhost");
    prefixMap.put("custom", "http://localhost");
    prefixMap.put("at", TombstoneCallback.NAMESPACE_TOMBSTONE);
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
  }

  protected AtomEntityProvider createAtomEntityProvider() throws EntityProviderException {
    return new AtomEntityProvider();
  }

  protected String readFile(final String filename) throws IOException {
    InputStream in = getFileAsStream(filename);

    byte[] tmp = new byte[8192];
    int count = in.read(tmp);
    StringBuilder b = new StringBuilder();
    while (count >= 0) {
      b.append(new String(tmp, 0, count));
      count = in.read(tmp);
    }

    return b.toString();
  }

  protected InputStream getFileAsStream(final String filename) throws IOException {
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    if (in == null) {
      throw new IOException("Requested file '" + filename + "' was not found.");
    }
    return in;
  }

  protected InputStream createContentAsStream(final String content) throws UnsupportedEncodingException {
    return new ByteArrayInputStream(content.getBytes("UTF-8"));
  }

  /**
   *
   * @param content
   * @param replaceWhitespaces if <code>true</code> all XML not necessary whitespaces between tags are
   * @return
   * @throws UnsupportedEncodingException
   */
  protected InputStream createContentAsStream(final String content, final boolean replaceWhitespaces)
          throws UnsupportedEncodingException {
    String contentForStream = content;
    if (replaceWhitespaces) {
      contentForStream = content.replaceAll(">\\s.<", "><");
    }

    return new ByteArrayInputStream(contentForStream.getBytes("UTF-8"));
  }

  /**
   * Create a map with a 'String' to 'Class<?>' mapping based on given parameters.
   * Therefore parameters MUST be a set of such pairs.
   * As example an correct method call would be:
   * <p>
   * <code>
   * createTypeMappings("someKey", Integer.class, "anotherKey", Long.class);
   * </code>
   * </p>
   *
   * @param firstKeyThenMappingClass
   * @return
   */
  protected Map<String, Object> createTypeMappings(final Object... firstKeyThenMappingClass) {
    Map<String, Object> typeMappings = new HashMap<String, Object>();
    if (firstKeyThenMappingClass.length % 2 != 0) {
      throw new IllegalArgumentException("Got odd number of parameters. Please read javadoc.");
    }
    for (int i = 0; i < firstKeyThenMappingClass.length; i += 2) {
      String key = (String) firstKeyThenMappingClass[i];
      Class<?> mappingClass = (Class<?>) firstKeyThenMappingClass[i + 1];
      typeMappings.put(key, mappingClass);
    }
    return typeMappings;
  }
}
