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
package org.apache.olingo.odata2.fit.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.http.HttpResponse;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.processor.part.ServiceDocumentProcessor;
import org.apache.olingo.odata2.api.uri.info.GetServiceDocumentUriInfo;
import org.apache.olingo.odata2.testutil.fit.DebugCallbackFactoryFlase;
import org.apache.olingo.odata2.testutil.fit.DebugCallbackFactoryTrue;
import org.apache.olingo.odata2.testutil.fit.FitStaticServiceFactory;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DebugDisabledTest extends AbstractBasicTest {

  public DebugDisabledTest(final ServletType servletType) {
    super(servletType);
  }

  @Test
  public void checkNoDebugCallbackMustResultInNoDebugResponse() throws Exception {
    startCustomServer(FitStaticServiceFactory.class);
    HttpResponse response = executeGetRequest("/?odata-debug=json");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    String payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><servicedocument/>", payload);

    response = executeGetRequest("/?odata-debug=html");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><servicedocument/>", payload);

    response = executeGetRequest("/?odata-debug=download");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><servicedocument/>", payload);

    stopCustomServer();
  }

  @Test
  public void checkDebugCallbackFalseMustResultInNoDebugResponse() throws Exception {
    startCustomServer(DebugCallbackFactoryFlase.class);
    HttpResponse response = executeGetRequest("/?odata-debug=json");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    String payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><servicedocument/>", payload);

    response = executeGetRequest("/?odata-debug=html");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><servicedocument/>", payload);

    response = executeGetRequest("/?odata-debug=download");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><servicedocument/>", payload);

    stopCustomServer();
  }

  @Test
  public void checkDebugCallbackTrueMustResultInDebugResponse() throws Exception {
    startCustomServer(DebugCallbackFactoryTrue.class);
    HttpResponse response = null;
    String payload = null;
    response = executeGetRequest("/?odata-debug=json");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertFalse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><servicedocument/>".equals(payload));
    assertTrue(payload.startsWith("{\"request\":{\"method\":\"GET\""));
    assertTrue(payload.contains("<servicedocument/>"));

    response = executeGetRequest("/?odata-debug=html");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertFalse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><servicedocument/>".equals(payload));
    assertTrue(payload.startsWith("<!DOCTYPE html"));
    assertTrue(payload.contains("&lt;servicedocument/&gt"));

    response = executeGetRequest("/?odata-debug=download");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertFalse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><servicedocument/>".equals(payload));
    assertTrue(payload.contains("&lt;servicedocument/&gt"));

    stopCustomServer();
  }

  @Override
  @Before
  public void before() {
    // Do nothing here to stop default server from starting
  }

  @Override
  @After
  public void after() {
    stopCustomServer();
  }

  @Override
  protected ODataSingleProcessor createProcessor() throws ODataException {
    final ODataSingleProcessor processor = mock(ODataSingleProcessor.class);
    when(
        ((ServiceDocumentProcessor) processor).readServiceDocument(any(GetServiceDocumentUriInfo.class),
            any(String.class)))
        .thenReturn(
            ODataResponse.entity("<?xml version=\"1.0\" encoding=\"UTF-8\"?><servicedocument/>").status(
                HttpStatusCodes.OK).build());
    return processor;
  }

}
