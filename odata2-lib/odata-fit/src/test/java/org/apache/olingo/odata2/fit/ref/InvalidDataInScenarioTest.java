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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetSimplePropertyUriInfo;
import org.apache.olingo.odata2.core.processor.ODataSingleProcessorService;
import org.apache.olingo.odata2.ref.edm.ScenarioEdmProvider;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Before;
import org.junit.Test;

/**
 * Based on OLINGO-763 we changed the behaviour of serializer exceptions. Now they must result in 500 internal server
 * errors if an application provides false data. This test is to ensure that the serializer throws the correct exception
 * which then results in the correct status code.
 */
public class InvalidDataInScenarioTest extends AbstractRefTest {

  public InvalidDataInScenarioTest(ServletType servletType) {
    super(servletType);
  }

  @Override
  protected ODataSingleProcessorService createService() {
    ODataSingleProcessor processor = new LocalProcessor();
    EdmProvider provider = new ScenarioEdmProvider();

    return new ODataSingleProcessorService(provider, processor) {};
  }

  @Before
  public void showStacktrace() {
    disableLogging();
  }

  @Test
  public void nullKeyInEntryData() throws Exception {
    HttpResponse response = callUri("Employees('1')", HttpStatusCodes.INTERNAL_SERVER_ERROR);
    System.out.println(getBody(response));
    response = callUri("Employees('1')?$format=json", HttpStatusCodes.INTERNAL_SERVER_ERROR);
    assertTrue(getBody(response).contains("null value"));
  }

  @Test
  public void violatedFacetsInEntry() throws Exception {
    HttpResponse response = callUri("Employees('2')", HttpStatusCodes.INTERNAL_SERVER_ERROR);
    assertTrue(getBody(response).contains("metadata constraints"));
    response = callUri("Employees('2')?$format=json", HttpStatusCodes.INTERNAL_SERVER_ERROR);
    assertTrue(getBody(response).contains("metadata constraints"));
  }

  @Test
  public void nullKeyInFeedData() throws Exception {
    HttpResponse response = callUri("Employees", HttpStatusCodes.INTERNAL_SERVER_ERROR);
    assertTrue(getBody(response).contains("null value"));
    response = callUri("Employees?$format=json", HttpStatusCodes.INTERNAL_SERVER_ERROR);
    assertTrue(getBody(response).contains("null value"));
  }

  @Test
  public void wrongPropertyValueIsNull() throws Exception {
    HttpResponse response = callUri("Employees('1')/EmployeeId", HttpStatusCodes.INTERNAL_SERVER_ERROR);
    assertTrue(getBody(response).contains("null value"));
    response = callUri("Employees('1')/EmployeeId?$format=json", HttpStatusCodes.INTERNAL_SERVER_ERROR);
    assertTrue(getBody(response).contains("null value"));
  }

  @Test
  public void wrongPropertyValueWithFacets() throws Exception {
    HttpResponse response = callUri("Employees('2')/TeamId", HttpStatusCodes.INTERNAL_SERVER_ERROR);
    assertTrue(getBody(response).contains("metadata constraints"));
    response = callUri("Employees('2')/TeamId?$format=json", HttpStatusCodes.INTERNAL_SERVER_ERROR);
    assertTrue(getBody(response).contains("metadata constraints"));
  }

  public class LocalProcessor extends ODataSingleProcessor {

    @Override
    public ODataResponse readEntity(GetEntityUriInfo uriInfo, String contentType) throws ODataException {
      HashMap<String, Object> data = new HashMap<String, Object>();

      if ("Employees".equals(uriInfo.getTargetEntitySet().getName())) {
        if ("2".equals(uriInfo.getKeyPredicates().get(0).getLiteral())) {
          data.put("EmployeeId", "1");
          data.put("TeamId", "420");
        }

        ODataContext context = getContext();
        EntityProviderWriteProperties writeProperties =
            EntityProviderWriteProperties.serviceRoot(context.getPathInfo().getServiceRoot()).build();

        return EntityProvider.writeEntry(contentType, uriInfo.getTargetEntitySet(), data, writeProperties);
      } else {
        throw new ODataApplicationException("Wrong testcall", Locale.getDefault(), HttpStatusCodes.NOT_IMPLEMENTED);
      }
    }

    @Override
    public ODataResponse readEntitySet(GetEntitySetUriInfo uriInfo, String contentType) throws ODataException {
      if ("Employees".equals(uriInfo.getTargetEntitySet().getName())) {
        ODataContext context = getContext();
        EntityProviderWriteProperties writeProperties =
            EntityProviderWriteProperties.serviceRoot(context.getPathInfo().getServiceRoot()).build();
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        data.add(new HashMap<String, Object>());
        return EntityProvider.writeFeed(contentType, uriInfo.getTargetEntitySet(), data, writeProperties);
      } else {
        throw new ODataApplicationException("Wrong testcall", Locale.getDefault(), HttpStatusCodes.NOT_IMPLEMENTED);
      }
    }

    @Override
    public ODataResponse readEntitySimpleProperty(GetSimplePropertyUriInfo uriInfo, String contentType)
        throws ODataException {
      EdmProperty edmProperty = uriInfo.getPropertyPath().get(0);
      Object value = null;
      if ("EmployeeId".equals(edmProperty.getName())) {
        // must be null for a specific test
        value = null;
      } else if ("TeamId".equals(edmProperty.getName())) {
        value = new Integer(520);
      }

      return EntityProvider.writeProperty(contentType, edmProperty, value);
    }
  }
}
