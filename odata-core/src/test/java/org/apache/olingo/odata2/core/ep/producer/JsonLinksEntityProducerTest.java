/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.ep.producer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.ep.JsonEntityProvider;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

/**
 *  
 */
public class JsonLinksEntityProducerTest extends BaseTest {
  protected static final String BASE_URI = "http://host:80/service/";
  protected static final EntityProviderWriteProperties DEFAULT_PROPERTIES =
      EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI)).build();

  @Test
  public void serializeEmployeeLink() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    Map<String, Object> employeeData = new HashMap<String, Object>();
    employeeData.put("EmployeeId", "1");
    ArrayList<Map<String, Object>> employeesData = new ArrayList<Map<String, Object>>();
    employeesData.add(employeeData);

    final ODataResponse response = new JsonEntityProvider().writeLinks(entitySet, employeesData, DEFAULT_PROPERTIES);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":[{\"uri\":\"" + BASE_URI + "Employees('1')\"}]}", json);
  }

  @Test
  public void serializeEmployeeLinks() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    Map<String, Object> employee1 = new HashMap<String, Object>();
    employee1.put("EmployeeId", "1");
    Map<String, Object> employee2 = new HashMap<String, Object>();
    employee2.put("EmployeeId", "2");
    Map<String, Object> employee3 = new HashMap<String, Object>();
    employee3.put("EmployeeId", "3");
    ArrayList<Map<String, Object>> employeesData = new ArrayList<Map<String, Object>>();
    employeesData.add(employee1);
    employeesData.add(employee2);
    employeesData.add(employee3);

    final ODataResponse response = new JsonEntityProvider().writeLinks(entitySet, employeesData, DEFAULT_PROPERTIES);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":[{\"uri\":\"" + BASE_URI + "Employees('1')\"},"
        + "{\"uri\":\"" + BASE_URI + "Employees('2')\"},"
        + "{\"uri\":\"" + BASE_URI + "Employees('3')\"}]}",
        json);
  }

  @Test
  public void serializeEmptyList() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

    final ODataResponse response = new JsonEntityProvider().writeLinks(entitySet, data, DEFAULT_PROPERTIES);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":[]}", json);
  }

  @Test
  public void serializeLinksAndInlineCount() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    Map<String, Object> employee1 = new HashMap<String, Object>();
    employee1.put("EmployeeId", "1");
    Map<String, Object> employee2 = new HashMap<String, Object>();
    employee2.put("EmployeeId", "2");
    ArrayList<Map<String, Object>> employeesData = new ArrayList<Map<String, Object>>();
    employeesData.add(employee1);
    employeesData.add(employee2);

    final ODataResponse response = new JsonEntityProvider().writeLinks(entitySet, employeesData,
        EntityProviderWriteProperties.serviceRoot(URI.create(BASE_URI))
            .inlineCountType(InlineCount.ALLPAGES).inlineCount(42).build());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"__count\":\"42\",\"results\":["
        + "{\"uri\":\"" + BASE_URI + "Employees('1')\"},"
        + "{\"uri\":\"" + BASE_URI + "Employees('2')\"}]}}",
        json);
  }
}
