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
import static org.junit.Assert.assertTrue;

import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

/**
 * Tests employing the reference scenario changing properties in JSON format.
 * 
 */
public class PropertyJsonChangeTest extends AbstractRefTest {

  public PropertyJsonChangeTest(final ServletType servletType) {
    super(servletType);
  }

  @Test
  public void simplePropertyWithoutAcceptHeader() throws Exception {
    final String url = "Employees('2')/Age";
    putUri(url, "{\"Age\":17}", HttpContentType.APPLICATION_JSON, HttpStatusCodes.NO_CONTENT);
    assertEquals("{\"d\":{\"Age\":17}}", getBody(callUri(url + "?$format=json")));
    putUri(url, "{\"Age\":\"17\"}", HttpContentType.APPLICATION_JSON, HttpStatusCodes.BAD_REQUEST);

    final String urlForName = "Employees('2')/EmployeeName";
    putUri(urlForName, "{\"EmployeeName\":\"NewName\"}", HttpContentType.APPLICATION_JSON, HttpStatusCodes.NO_CONTENT);
    assertEquals("{\"d\":{\"EmployeeName\":\"NewName\"}}", getBody(callUri(urlForName + "?$format=json")));
    putUri(urlForName, "{\"EmployeeName\":NewName}", HttpContentType.APPLICATION_JSON, HttpStatusCodes.BAD_REQUEST);
  }

  @Test
  public void simplePropertyWithAcceptHeader() throws Exception {
    final String url = "Employees('2')/Age";
    putUri(url, HttpContentType.WILDCARD, "{\"Age\":17}", HttpContentType.APPLICATION_JSON, HttpStatusCodes.NO_CONTENT);
    assertEquals("{\"d\":{\"Age\":17}}", getBody(callUri(url + "?$format=json")));
    putUri(url, "{\"Age\":\"17\"}", HttpContentType.APPLICATION_JSON, HttpStatusCodes.BAD_REQUEST);

    final String urlForName = "Employees('2')/EmployeeName";
    putUri(urlForName, HttpContentType.APPLICATION_JSON, "{\"EmployeeName\":\"NewName\"}",
        HttpContentType.APPLICATION_JSON, HttpStatusCodes.NO_CONTENT);
    assertEquals("{\"d\":{\"EmployeeName\":\"NewName\"}}", getBody(callUri(urlForName + "?$format=json")));
    putUri(urlForName, "{\"EmployeeName\":NewName}", HttpContentType.APPLICATION_JSON, HttpStatusCodes.BAD_REQUEST);
  }

  @Test
  public void simplePropertyWithInvalidAcceptHeader() throws Exception {
    final String url = "Employees('2')/Age";
    putUri(url, "", "{\"Age\":17}", HttpContentType.APPLICATION_JSON, HttpStatusCodes.NOT_ACCEPTABLE);
    putUri(url, null, "{\"Age\":17}", HttpContentType.APPLICATION_JSON, HttpStatusCodes.NOT_ACCEPTABLE);

    final String urlForName = "Employees('2')/EmployeeName";
    putUri(urlForName, "", "{\"EmployeeName\":\"NewName\"}", HttpContentType.APPLICATION_JSON,
        HttpStatusCodes.NOT_ACCEPTABLE);
    putUri(urlForName, null, "{\"EmployeeName\":\"NewName\"}", HttpContentType.APPLICATION_JSON,
        HttpStatusCodes.NOT_ACCEPTABLE);
  }

  @Test
  public void complexProperty() throws Exception {
    final String url1 = "Employees('2')/Location";
    String requestBody = "{\"Location\":{\"City\":{\"PostalCode\":\"69190\","
        + "\"CityName\":\"" + CITY_2_NAME + "\"},\"Country\":\"Germany\"}}";
    putUri(url1, requestBody, HttpContentType.APPLICATION_JSON, HttpStatusCodes.NO_CONTENT);
    assertTrue(getBody(callUri(url1 + "?$format=json")).contains(CITY_2_NAME));

    final String url2 = "Employees('2')/Location/City";
    requestBody = "{\"City\":{\"PostalCode\":\"69185\"}}";
    putUri(url2, requestBody, HttpContentType.APPLICATION_JSON, HttpStatusCodes.NO_CONTENT);
    assertTrue(getBody(callUri(url2 + "?$format=json")).contains("69185"));
  }
}
