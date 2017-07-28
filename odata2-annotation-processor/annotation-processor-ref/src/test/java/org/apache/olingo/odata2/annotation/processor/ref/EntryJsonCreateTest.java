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
package org.apache.olingo.odata2.annotation.processor.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

import com.google.gson.internal.LinkedTreeMap;

/**
 *  
 */
public class EntryJsonCreateTest extends AbstractRefJsonTest {

  public EntryJsonCreateTest(final ServletType servletType) {
    super(servletType);
  }

  @Test
  public void createEntryRoom() throws Exception {
    String id = UUID.randomUUID().toString();
    String content = "{\"d\":{\"__metadata\":{\"id\":\"" + getEndpoint() + "Rooms('1')\","
        + "\"uri\":\"" + getEndpoint() + "Rooms('1')\",\"type\":\"RefScenario.Room\","
        + "\"etag\":\"W/\\\"3\\\"\"},"
        + "\"Id\":\"" + id + "\",\"Name\":\"Room 104\",\"Seats\":4,\"Version\":2,"
        + "\"nr_Employees\":{\"__deferred\":{\"uri\":\"" + getEndpoint() + "Rooms('1')/nr_Employees\"}},"
        + "\"nr_Building\":{\"__deferred\":{\"uri\":\"" + getEndpoint() + "Rooms('1')/nr_Building\"}}}}";
    assertNotNull(content);
    HttpResponse response =
        postUri("Rooms", content, HttpContentType.APPLICATION_JSON, HttpHeaders.ACCEPT,
            HttpContentType.APPLICATION_JSON, HttpStatusCodes.CREATED);
    checkMediaType(response, HttpContentType.APPLICATION_JSON);

    String body = getBody(response);
    LinkedTreeMap<?,?> map = getStringMap(body);
    assertEquals(id, map.get("Id"));
    assertEquals("Room 104", map.get("Name"));
    @SuppressWarnings("unchecked")
    LinkedTreeMap<String, String> metadataMap = (LinkedTreeMap<String, String>) map.get("__metadata");
    assertNotNull(metadataMap);
    String expectedRoomId = getEndpoint() + "Rooms('" + id + "')";
    assertEquals(expectedRoomId, metadataMap.get("id"));
    assertEquals("RefScenario.Room", metadataMap.get("type"));
    assertEquals(expectedRoomId, metadataMap.get("uri"));

    response = callUri("Rooms('" + id + "')/Seats/$value");
    body = getBody(response);
    assertEquals("4", body);
  }

  @Test
  public void createEntryRoomWithLink() throws Exception {
    final String id = UUID.randomUUID().toString();
    String content = "{\"d\":{\"__metadata\":{\"id\":\"" + getEndpoint() + "Rooms('1')\","
        + "\"uri\":\"" + getEndpoint() + "Rooms('1')\",\"type\":\"RefScenario.Room\","
        + "\"etag\":\"W/\\\"3\\\"\"},"
        + "\"Id\":\"" + id + "\",\"Name\":\"Room 104\","
        + "\"nr_Employees\":{\"__deferred\":{\"uri\":\"" + getEndpoint() + "Rooms('1')/nr_Employees\"}},"
        + "\"nr_Building\":{\"__deferred\":{\"uri\":\"" + getEndpoint() + "Rooms('1')/nr_Building\"}}}}";
    assertNotNull(content);
    HttpResponse response =
        postUri("Rooms", content, HttpContentType.APPLICATION_JSON, HttpHeaders.ACCEPT,
            HttpContentType.APPLICATION_JSON, HttpStatusCodes.CREATED);
    checkMediaType(response, HttpContentType.APPLICATION_JSON);

    String body = getBody(response);
    LinkedTreeMap<?,?> map = getStringMap(body);
    assertEquals(id, map.get("Id"));
    assertEquals("Room 104", map.get("Name"));
    @SuppressWarnings("unchecked")
    LinkedTreeMap<String, Object> employeesMap = (LinkedTreeMap<String, Object>) map.get("nr_Employees");
    assertNotNull(employeesMap);
    @SuppressWarnings("unchecked")
    LinkedTreeMap<String, String> deferredMap = (LinkedTreeMap<String, String>) employeesMap.get("__deferred");
    assertNotNull(deferredMap);
    assertEquals(getEndpoint() + "Rooms('" + id + "')/nr_Employees", deferredMap.get("uri"));
  }

  @Test
  public void createAndModifyEntryEmployee() throws Exception {
    String content = "{iVBORw0KGgoAAAANSUhEUgAAAB4AAAAwCAIAAACJ9F2zAAAAA}";

    assertNotNull(content);
    HttpResponse createResponse =
        postUri("Employees", content, HttpContentType.TEXT_PLAIN, HttpHeaders.ACCEPT, HttpContentType.APPLICATION_JSON,
            HttpStatusCodes.CREATED);
    checkMediaType(createResponse, HttpContentType.APPLICATION_JSON);

    String body = getBody(createResponse);
    LinkedTreeMap<?,?> map = getStringMap(body);
    String id = (String) map.get("EmployeeId");
    assertNull(map.get("EmployeeName"));

    putUri("Employees('" + id + "')", JSON_EMPLOYEE, HttpContentType.APPLICATION_JSON, HttpStatusCodes.NO_CONTENT);

    HttpResponse updateResponse = callUri("Employees('" + id + "')", "Accept", HttpContentType.APPLICATION_JSON);
    checkMediaType(updateResponse, HttpContentType.APPLICATION_JSON);
    String updatedBody = getBody(updateResponse);
    LinkedTreeMap<?,?> updatedMap = getStringMap(updatedBody);
    assertNotNull(updatedMap.get("EmployeeId"));
    assertEquals("Douglas", updatedMap.get("EmployeeName"));
    assertNull(updatedMap.get("EntryData"));

    LinkedTreeMap<?,?> location = (LinkedTreeMap<?,?>) updatedMap.get("Location");
    assertEquals("Britian", location.get("Country"));

    LinkedTreeMap<?,?> city = (LinkedTreeMap<?,?>) location.get("City");
    assertEquals("12345", city.get("PostalCode"));
    assertEquals("Sample", city.get("CityName"));
  }

  private static final String JSON_EMPLOYEE = "{" +
      "    \"d\": {" +
      // "        \"__metadata\": {" +
      // "            \"id\": \"http://localhost:19000/abc/EntryJsonCreateTest/Employees('1')\"," +
      // "            \"uri\": \"http://localhost:19000/abc/EntryJsonCreateTest/Employees('1')\"," +
      // "            \"type\": \"RefScenario.Employee\"," +
      // "            \"content_type\": \"application/octet-stream\"," +
      // "            \"media_src\": \"Employees('1')/$value\"," +
      // "            \"edit_media\": \"http://localhost:19000/abc/EntryJsonCreateTest/Employees('1')/$value\"" +
      // "        }," +
      "        \"EmployeeId\": \"1\"," +
      "        \"EmployeeName\": \"Douglas\"," +
      "        \"Age\": 42," +
      "        \"Location\": {" +
      "            \"__metadata\": {" +
      "                \"type\": \"RefScenario.c_Location\"" +
      "            }," +
      "            \"Country\": \"Britian\"," +
      "            \"City\": {" +
      "                \"__metadata\": {" +
      "                    \"type\": \"RefScenario.c_City\"" +
      "                }," +
      "                \"PostalCode\": \"12345\"," +
      "                \"CityName\": \"Sample\"" +
      "            }" +
      "        }" +
      "    }" +
      "}";
}
