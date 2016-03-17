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

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataServiceVersion;
import org.apache.olingo.odata2.api.commons.ODataHttpHeaders;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.ep.JsonEntityProvider;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *  
 */
public class JsonPropertyProducerTest extends BaseTest {

  @Test
  public void serializeString() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("EmployeeName");

    final ODataResponse response = new JsonEntityProvider().writeProperty(property, "\"Игорь\tНиколаевич\tЛарионов\"");
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntityProvider must not set content header", response.getContentHeader());
    assertEquals(ODataServiceVersion.V10, response.getHeader(ODataHttpHeaders.DATASERVICEVERSION));

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"EmployeeName\":\"\\\"Игорь\\tНиколаевич\\tЛарионов\\\"\"}}", json);
  }

  @Test
  public void serializeVeryLongString() throws Exception {
    char[] chars = new char[32768];
    Arrays.fill(chars, 0, 32768, 'a');
    String propertyValue = new String(chars);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("EmployeeName");

    final ODataResponse response = new JsonEntityProvider().writeProperty(property, propertyValue);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntityProvider must not set content header", response.getContentHeader());
    assertEquals(ODataServiceVersion.V10, response.getHeader(ODataHttpHeaders.DATASERVICEVERSION));

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"EmployeeName\":\"" + propertyValue + "\"}}", json);
  }

  @Test
  public void serializeRoomIdWithFacets() throws Exception {
    EdmTyped edmTyped = MockFacade.getMockEdm().getEntityType("RefScenario", "Room").getProperty("Id");
    EdmProperty edmProperty = (EdmProperty) edmTyped;

    String id = StringHelper.generateData(1000);
    try {
      final ODataResponse response = new JsonEntityProvider().writeProperty(edmProperty, id);
      assertNotNull(response);
    } catch(EntityProviderException e) {
      assertNotNull(e.getCause());
      assertTrue(e.getCause() instanceof EdmSimpleTypeException);
    }
  }


  @Test
  public void serializeNumber() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");
    final ODataResponse response = new JsonEntityProvider().writeProperty(property, 42);
    assertEquals("{\"d\":{\"Age\":42}}", StringHelper.inputStreamToString((InputStream) response.getEntity()));
  }

  @Test
  public void serializeBinary() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Building").getProperty("Image");
    final ODataResponse response = new JsonEntityProvider().writeProperty(property, new byte[] { 42, -42 });
    assertEquals("{\"d\":{\"Image\":\"KtY=\"}}", StringHelper.inputStreamToString((InputStream) response.getEntity()));
  }

  @Test
  public void serializeBinaryWithContentType() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario2", "Photo").getProperty("Image");
    Map<String, Object> content = new HashMap<String, Object>();
    content.put("getImageType", "image/jpeg");
    content.put("Image", new byte[] { 1, 2, 3 });
    final ODataResponse response = new JsonEntityProvider().writeProperty(property, content);
    assertEquals("{\"d\":{\"Image\":\"AQID\"}}", StringHelper.inputStreamToString((InputStream) response.getEntity()));
  }

  @Test
  public void serializeBoolean() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Team").getProperty("isScrumTeam");
    final ODataResponse response = new JsonEntityProvider().writeProperty(property, false);
    assertEquals("{\"d\":{\"isScrumTeam\":false}}", StringHelper
        .inputStreamToString((InputStream) response.getEntity()));
  }

  @Test
  public void serializeNull() throws Exception {
    EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    ODataResponse response = new JsonEntityProvider().writeProperty(property, null);
    assertEquals("{\"d\":{\"ImageUrl\":null}}", StringHelper.inputStreamToString((InputStream) response.getEntity()));

    property = (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");
    response = new JsonEntityProvider().writeProperty(property, null);
    assertEquals("{\"d\":{\"Age\":null}}", StringHelper.inputStreamToString((InputStream) response.getEntity()));

    property = (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("EntryDate");
    response = new JsonEntityProvider().writeProperty(property, null);
    assertEquals("{\"d\":{\"EntryDate\":null}}", StringHelper.inputStreamToString((InputStream) response.getEntity()));

    property = (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Building").getProperty("Image");
    response = new JsonEntityProvider().writeProperty(property, null);
    assertEquals("{\"d\":{\"Image\":null}}", StringHelper.inputStreamToString((InputStream) response.getEntity()));
  }

  @Test
  public void serializeDateTime() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("EntryDate");
    Calendar dateTime = Calendar.getInstance();
    dateTime.setTimeInMillis(-42);
    final ODataResponse response = new JsonEntityProvider().writeProperty(property, dateTime);
    assertEquals("{\"d\":{\"EntryDate\":\"\\/Date(-42)\\/\"}}", StringHelper.inputStreamToString((InputStream) response
        .getEntity()));
  }

  @Test
  public void serializeComplexProperty() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");
    Map<String, Object> cityData = new LinkedHashMap<String, Object>();
    cityData.put("PostalCode", "8392");
    cityData.put("CityName", "Å");
    Map<String, Object> locationData = new LinkedHashMap<String, Object>();
    locationData.put("City", cityData);
    locationData.put("Country", "NO");

    final ODataResponse response = new JsonEntityProvider().writeProperty(property, locationData);
    assertEquals("{\"d\":{\"Location\":{\"__metadata\":{\"type\":\"RefScenario.c_Location\"},"
        + "\"City\":{\"__metadata\":{\"type\":\"RefScenario.c_City\"},"
        + "\"PostalCode\":\"8392\",\"CityName\":\"Å\"},\"Country\":\"NO\"}}}",
        StringHelper.inputStreamToString((InputStream) response.getEntity()));
  }

  @Test
  public void serializeComplexPropertyNull() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");
    final ODataResponse response = new JsonEntityProvider().writeProperty(property, null);
    assertEquals("{\"d\":{\"Location\":{\"__metadata\":{\"type\":\"RefScenario.c_Location\"},"
        + "\"City\":{\"__metadata\":{\"type\":\"RefScenario.c_City\"},"
        + "\"PostalCode\":null,\"CityName\":null},\"Country\":null}}}",
        StringHelper.inputStreamToString((InputStream) response.getEntity()));
  }
}
