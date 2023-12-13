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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.processor.part.MetadataProcessor;
import org.apache.olingo.odata2.api.processor.part.ServiceDocumentProcessor;
import org.apache.olingo.odata2.api.uri.info.GetMetadataUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetServiceDocumentUriInfo;
import org.apache.olingo.odata2.core.processor.ODataSingleProcessorService;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.apache.olingo.odata2.testutil.server.TestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *  
 */
public class ServiceResolutionTest extends BaseTest {

  private final HttpClient httpClient = new DefaultHttpClient();
  private final TestServer server = new TestServer(ServletType.JAXRS_SERVLET);
  private ODataContext context;
  private ODataSingleProcessorService service;

  @Before
  public void before() {
    try {
      final ODataSingleProcessor processor = mock(ODataSingleProcessor.class);
      final EdmProvider provider = mock(EdmProvider.class);

      service = new ODataSingleProcessorService(provider, processor) {};
      // FitStaticServiceFactory.setService(service);

      // science fiction (return context after setContext)
      // see http://www.planetgeek.ch/2010/07/20/mockito-answer-vs-return/

      doAnswer(new Answer<Object>() {
        @Override
        public Object answer(final InvocationOnMock invocation) throws Throwable {
          context = (ODataContext) invocation.getArguments()[0];
          return null;
        }
      }).when(processor).setContext(any(ODataContext.class));

      when(processor.getContext()).thenAnswer(new Answer<ODataContext>() {
        @Override
        public ODataContext answer(final InvocationOnMock invocation) throws Throwable {
          return context;
        }
      });

      when(((MetadataProcessor) processor).readMetadata(any(GetMetadataUriInfo.class), any(String.class))).thenReturn(
          ODataResponse.entity("metadata").status(HttpStatusCodes.OK).build());
      when(
          ((ServiceDocumentProcessor) processor).readServiceDocument(any(GetServiceDocumentUriInfo.class),
              any(String.class)))
          .thenReturn(ODataResponse.entity("servicedocument").status(HttpStatusCodes.OK).build());
    } catch (final ODataException e) {
      throw new RuntimeException(e);
    }
  }

  private void startServer() {
    server.startServer(service);
  }

  @After
  public void after() {
    if (server != null) {
      server.stopServer();
    }
  }

  @Test
  public void testSplit0() throws IOException, ODataException {
    server.setPathSplit(0);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "/$metadata"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);

    assertTrue(ctx.getPathInfo().getPrecedingSegments().isEmpty());
    assertEquals("$metadata", ctx.getPathInfo().getODataSegments().get(0).getPath());
  }

  @Test
  public void testSplit1() throws IOException, ODataException {
    server.setPathSplit(1);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "/aaa/$metadata"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("aaa", ctx.getPathInfo().getPrecedingSegments().get(0).getPath());
    assertEquals("$metadata", ctx.getPathInfo().getODataSegments().get(0).getPath());
  }

  @Test
  public void testSplit2() throws IOException, ODataException {
    server.setPathSplit(2);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "/aaa/bbb/$metadata"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("aaa", ctx.getPathInfo().getPrecedingSegments().get(0).getPath());
    assertEquals("bbb", ctx.getPathInfo().getPrecedingSegments().get(1).getPath());
    assertEquals("$metadata", ctx.getPathInfo().getODataSegments().get(0).getPath());
  }

  @Test
  public void testSplitUrlToShort() throws IOException, ODataException {
    server.setPathSplit(3);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "/aaa/$metadata"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.BAD_REQUEST.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void testSplitUrlServiceDocument() throws IOException, ODataException {
    server.setPathSplit(1);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "/aaa/"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("", ctx.getPathInfo().getODataSegments().get(0).getPath());
    assertEquals("aaa", ctx.getPathInfo().getPrecedingSegments().get(0).getPath());
  }

  @Test
  public void testMatrixParameterInNonODataPath() throws IOException, ODataException {
    server.setPathSplit(1);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "aaa;n=2/"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("", ctx.getPathInfo().getODataSegments().get(0).getPath());
    assertEquals("aaa", ctx.getPathInfo().getPrecedingSegments().get(0).getPath());

    assertNotNull(ctx.getPathInfo().getPrecedingSegments().get(0).getMatrixParameters());

    String key, value;
    key = ctx.getPathInfo().getPrecedingSegments().get(0).getMatrixParameters().keySet().iterator().next();
    assertEquals("n", key);
    value = ctx.getPathInfo().getPrecedingSegments().get(0).getMatrixParameters().get(key).get(0);
    assertEquals("2", value);
  }

  @Test
  public void testNoMatrixParameterInODataPath() throws IOException, ODataException {
    server.setPathSplit(0);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "$metadata;matrix"));
    final HttpResponse response = httpClient.execute(get);

    final InputStream stream = response.getEntity().getContent();
    final String body = StringHelper.inputStreamToString(stream);

    assertTrue(body.contains("metadata"));
    assertTrue(body.contains("matrix"));
    assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void testBaseUriWithMatrixParameter() throws IOException, ODataException,
      URISyntaxException {
    server.setPathSplit(3);
    startServer();

    final String endpoint = server.getEndpoint().toString();
    final HttpGet get = new HttpGet(URI.create(endpoint + "aaa/bbb;n=2,3;m=1/ccc/"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);
    validateServiceRoot(ctx.getPathInfo().getServiceRoot().toASCIIString(),
        endpoint + "aaa/bbb;", "/ccc/", "n=2,3", "m=1");
  }

  @Test
  public void testMetadataUriWithMatrixParameter() throws IOException, ODataException,
      URISyntaxException {
    server.setPathSplit(3);
    startServer();

    final String endpoint = server.getEndpoint().toString();
    final HttpGet get = new HttpGet(URI.create(endpoint + "aaa/bbb;n=2,3;m=1/ccc/$metadata"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);
    validateServiceRoot(ctx.getPathInfo().getServiceRoot().toASCIIString(),
        endpoint + "aaa/bbb;", "/ccc/", "n=2,3", "m=1");
    assertEquals("$metadata", ctx.getPathInfo().getODataSegments().get(0).getPath());
  }

  @Test
  public void testBaseUriWithEncoding() throws IOException, ODataException,
      URISyntaxException {
    server.setPathSplit(3);
    startServer();

    final URI uri =
        new URI(server.getEndpoint().getScheme(), null, server.getEndpoint().getHost(), server.getEndpoint().getPort(),
            server.getEndpoint().getPath() + "/aaa/äдержb;n=2, 3;m=1/c c/", null, null);

    final HttpGet get = new HttpGet(uri);
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext context = service.getProcessor().getContext();
    assertNotNull(context);
    validateServiceRoot(context.getPathInfo().getServiceRoot().toASCIIString(),
        server.getEndpoint() + "aaa/%C3%A4%D0%B4%D0%B5%D1%80%D0%B6b;", "/c%20c/", "n=2,%203", "m=1");
  }

  private void validateServiceRoot(String serviceRoot, String prefix, String postfix, String ... matrixParameter) {
    assertTrue("Service root '" + serviceRoot + "' does not start with '" + prefix + "'.",
        serviceRoot.startsWith(prefix));
    assertTrue("Service root '" + serviceRoot + "' does not end with '" + postfix + "'.", serviceRoot.endsWith
        (postfix));
    for (String s : matrixParameter) {
      assertTrue("Service root '" + serviceRoot + "' misses matrix parameter '" + s + "'", serviceRoot.contains(s));
    }
  }
}
