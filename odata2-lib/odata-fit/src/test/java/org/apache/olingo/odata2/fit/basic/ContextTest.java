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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.processor.part.MetadataProcessor;
import org.apache.olingo.odata2.api.processor.part.ServiceDocumentProcessor;
import org.apache.olingo.odata2.api.uri.info.GetMetadataUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetServiceDocumentUriInfo;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

/**
 *  
 */
public class ContextTest extends AbstractBasicTest {

  public ContextTest(final ServletType servletType) {
    super(servletType);
  }

  @Override
  protected ODataSingleProcessor createProcessor() throws ODataException {
    final ODataSingleProcessor processor = mock(ODataSingleProcessor.class);
    when(((MetadataProcessor) processor).readMetadata(any(GetMetadataUriInfo.class), any(String.class))).thenReturn(
        ODataResponse.entity("metadata").status(HttpStatusCodes.OK).build());
    when(
        ((ServiceDocumentProcessor) processor).readServiceDocument(any(GetServiceDocumentUriInfo.class),
            any(String.class))).thenReturn(ODataResponse.entity("service document").status(HttpStatusCodes.OK).build());
    return processor;
  }

  @Test
  public void checkContextExists() throws ClientProtocolException, IOException, ODataException {
    assertNull(getService().getProcessor().getContext());
    final HttpResponse response = executeGetRequest("$metadata");

    final ODataContext context = getService().getProcessor().getContext();
    assertNotNull(context);

    final ODataService service = context.getService();
    assertNotNull(service);

    assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    assertEquals("$metadata", context.getPathInfo().getODataSegments().get(0).getPath());
  }

  @Test
  public void checkBaseUriForServiceDocument() throws ClientProtocolException, IOException, ODataException {
    executeGetRequest("");

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);
    assertEquals(getEndpoint().toString(), ctx.getPathInfo().getServiceRoot().toASCIIString());
  }

  @Test
  public void checkServiceFactoryIsSet() throws ClientProtocolException, IOException, ODataException {
    executeGetRequest("");

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);
    assertNotNull(ctx.getServiceFactory());
  }

  @Test
  public void checkServiceIsSet() throws ClientProtocolException, IOException, ODataException {
    executeGetRequest("");

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);
    assertNotNull(ctx.getService());
  }

  @Test
  public void checkBaseUriForMetadata() throws ClientProtocolException, IOException, ODataException {
    executeGetRequest("$metadata");

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);
    assertEquals(getEndpoint().toString(), ctx.getPathInfo().getServiceRoot().toASCIIString());
  }

  @Test
  public void checkAcceptuablesLanguage() throws ODataException, ClientProtocolException, IOException {
    final HttpGet get = new HttpGet(URI.create(getEndpoint().toString() + "/$metadata"));
    get.setHeader("Accept-Language", "de, en");

    getHttpClient().execute(get);

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("[de, en]", ctx.getAcceptableLanguages().toString());
  }

  @Test
  public void checkAcceptuablesLanguagesNoHeader() throws ODataException, ClientProtocolException, IOException {
    final HttpGet get = new HttpGet(URI.create(getEndpoint().toString() + "/$metadata"));
    getHttpClient().execute(get);

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("[*]", ctx.getAcceptableLanguages().toString());
  }

  @Test
  public void checkRequestHeader() throws ClientProtocolException, IOException, ODataException {
    final HttpGet get = new HttpGet(URI.create(getEndpoint().toString() + "/$metadata"));
    get.setHeader("ConTenT-laNguaGe", "de, en");
    getHttpClient().execute(get);

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("de, en", ctx.getRequestHeader(HttpHeaders.CONTENT_LANGUAGE));
    assertNull(ctx.getRequestHeader("nonsens"));
  }

  @Test
  public void checkRequestHeaders() throws ClientProtocolException, IOException, ODataException {
    final HttpGet get = new HttpGet(URI.create(getEndpoint().toString() + "/$metadata"));
    get.setHeader("ConTenT-laNguaGe", "de, en");
    getHttpClient().execute(get);

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);

    final Map<String, List<String>> header = ctx.getRequestHeaders();
    assertEquals("de, en", header.get(HttpHeaders.CONTENT_LANGUAGE).get(0));
  }

  @Test
  public void checkNewRequestHeader() throws ClientProtocolException, IOException, ODataException {
    final HttpGet get = new HttpGet(URI.create(getEndpoint().toString() + "/$metadata"));
    get.setHeader("ConTenT-laNguaGe", "de, en");
    getHttpClient().execute(get);

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("de, en", ctx.getRequestHeader(HttpHeaders.CONTENT_LANGUAGE));
    assertNull(ctx.getRequestHeader("nonsens"));
  }

  @Test
  public void checkNewRequestHeaders() throws ClientProtocolException, IOException, ODataException {
    final HttpGet get = new HttpGet(URI.create(getEndpoint().toString() + "/$metadata"));
    get.setHeader("ConTenT-laNguaGe", "de, en");
    getHttpClient().execute(get);

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);

    final Map<String, List<String>> header = ctx.getRequestHeaders();
    assertNotNull(header);
    assertNotNull(header.get(HttpHeaders.CONTENT_LANGUAGE));
    assertEquals(1, header.get(HttpHeaders.CONTENT_LANGUAGE).size());
    assertEquals("de, en", header.get(HttpHeaders.CONTENT_LANGUAGE).get(0));
  }

  @Test
  public void checkHttpMethod() throws ClientProtocolException, IOException, ODataException {
    final HttpGet get = new HttpGet(URI.create(getEndpoint().toString() + "/$metadata"));
    getHttpClient().execute(get);

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);

    final String httpMethod = ctx.getHttpMethod();
    assertEquals("GET", httpMethod);
  }

  @Test
  public void checkHttpRequest() throws ClientProtocolException, IOException, ODataException {
    final HttpGet get = new HttpGet(URI.create(getEndpoint().toString() + "/$metadata"));
    getHttpClient().execute(get);

    final ODataContext ctx = getService().getProcessor().getContext();
    assertNotNull(ctx);

    final Object requestObject = ctx.getParameter(ODataContext.HTTP_SERVLET_REQUEST_OBJECT);
    assertNotNull(requestObject);
    assertTrue(requestObject instanceof HttpServletRequest);
  }
}
