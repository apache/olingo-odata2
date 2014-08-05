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
package org.apache.olingo.odata2.fit.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

/**
 * Tests employing the reference scenario reading function-import output in JSON format.
 * 
 */
public class FunctionImportJsonTest extends AbstractRefTest {

  public FunctionImportJsonTest(final ServletType servletType) {
    super(servletType);
  }

  private EdmEntityContainer getEntityContainer() throws Exception {
    final HttpResponse response = callUri("$metadata"); 
    final EdmEntityContainer entityContainer = EntityProvider.readMetadata(response.getEntity().getContent(), false)
        .getDefaultEntityContainer();
    getBody(response);
    return entityContainer;
  }

  @Test
  public void entityCollection() throws Exception {
    final HttpResponse response = callUri("EmployeeSearch?q='nat'&$format=json");
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    final String body = getBody(response);
    assertEquals(getBody(callUri("Employees?$filter=substringof('nat',EmployeeName)&$format=json")), body);
  }

  @Test
  public void complexTypeCollection() throws Exception {
    final HttpResponse response = callUri("AllLocations?$format=json");
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    final String body = getBody(response);
    assertEquals("{\"d\":{\"__metadata\":{\"type\":\"Collection(RefScenario.c_Location)\"},"
        + "\"results\":[{\"__metadata\":{\"type\":\"RefScenario.c_Location\"},"
        + "\"City\":{\"__metadata\":{\"type\":\"RefScenario.c_City\"},"
        + "\"PostalCode\":\"69124\",\"CityName\":\"Heidelberg\"},\"Country\":\"Germany\"},"
        + "{\"__metadata\":{\"type\":\"RefScenario.c_Location\"},"
        + "\"City\":{\"__metadata\":{\"type\":\"RefScenario.c_City\"},"
        + "\"PostalCode\":\"69190\",\"CityName\":\"Walldorf\"},\"Country\":\"Germany\"}]}}",
        body);
    final Object result = EntityProvider.readFunctionImport(HttpContentType.APPLICATION_JSON,
        getEntityContainer().getFunctionImport("AllLocations"), StringHelper.encapsulate(body),
        EntityProviderReadProperties.init().build());
    assertNotNull(result);
    final List<?> collection = (List<?>) result;
    @SuppressWarnings("unchecked")
    final Map<String, Object> secondLocation = (Map<String, Object>) collection.get(1);
    @SuppressWarnings("unchecked")
    final Map<String, Object> secondCity = (Map<String, Object>) secondLocation.get("City");
    assertEquals(CITY_2_NAME, secondCity.get("CityName"));
  }

  @Test
  public void simpleTypeCollection() throws Exception {
    final HttpResponse response = callUri("AllUsedRoomIds?$format=json");
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    final String body = getBody(response);
    assertEquals("{\"d\":{\"__metadata\":{\"type\":\"Collection(Edm.String)\"},"
        + "\"results\":[\"1\",\"2\",\"3\"]}}",
        body);
    final Object result = EntityProvider.readFunctionImport(HttpContentType.APPLICATION_JSON,
        getEntityContainer().getFunctionImport("AllUsedRoomIds"), StringHelper.encapsulate(body),
        EntityProviderReadProperties.init().build());
    assertNotNull(result);
    assertEquals(Arrays.asList("1", "2", "3"), result);
  }

  @Test
  public void simpleType() throws Exception {
    final HttpResponse response = callUri("MaximalAge?$format=json");
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    final String body = getBody(response);
    assertEquals("{\"d\":{\"MaximalAge\":56}}", body);
    final Object result = EntityProvider.readFunctionImport(HttpContentType.APPLICATION_JSON,
        getEntityContainer().getFunctionImport("MaximalAge"), StringHelper.encapsulate(body),
        EntityProviderReadProperties.init().build());
    assertNotNull(result);
    assertEquals(Short.valueOf(EMPLOYEE_3_AGE), result);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void complexType() throws Exception {
    final HttpResponse response = callUri("MostCommonLocation?$format=json");
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    final String body = getBody(response);
    assertEquals("{\"d\":{\"MostCommonLocation\":"
        + "{\"__metadata\":{\"type\":\"RefScenario.c_Location\"},"
        + "\"City\":{\"__metadata\":{\"type\":\"RefScenario.c_City\"},"
        + "\"PostalCode\":\"69190\",\"CityName\":\"" + CITY_2_NAME + "\"},"
        + "\"Country\":\"Germany\"}}}",
        body);
    final Object result = EntityProvider.readFunctionImport(HttpContentType.APPLICATION_JSON,
        getEntityContainer().getFunctionImport("MostCommonLocation"), StringHelper.encapsulate(body),
        EntityProviderReadProperties.init().build());
    assertNotNull(result);
    Map<String, Object> resultMap = (Map<String, Object>) result;
    assertNotNull(resultMap);
    assertFalse(resultMap.isEmpty());
    resultMap = (Map<String, Object>) resultMap.get("City");
    assertNotNull(resultMap);
    assertFalse(resultMap.isEmpty());
    assertEquals(CITY_2_NAME, resultMap.get("CityName"));
  }

  @Test
  public void binary() throws Exception {
    final HttpResponse response = callUri("ManagerPhoto?Id='1'&$format=json");
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    assertTrue(getBody(response).startsWith("{\"d\":{\"ManagerPhoto\":\"iVBORw0KGgoAAAAN"));
  }

  @Test
  public void entity() throws Exception {
    final String expected = getBody(callUri("Employees('3')?$format=json"));
    final HttpResponse response = callUri("OldestEmployee", HttpHeaders.ACCEPT, HttpContentType.APPLICATION_JSON);
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    final String body = getBody(response);
    assertEquals(expected, body);
    final Object result = EntityProvider.readFunctionImport(HttpContentType.APPLICATION_JSON,
        getEntityContainer().getFunctionImport("OldestEmployee"), StringHelper.encapsulate(body),
        EntityProviderReadProperties.init().build());
    assertNotNull(result);
    final ODataEntry entry = (ODataEntry) result;
    assertEquals("3", entry.getProperties().get("EmployeeId"));
  }
}
