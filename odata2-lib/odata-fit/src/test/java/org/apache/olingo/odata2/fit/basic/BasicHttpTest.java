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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import jakarta.ws.rs.HttpMethod;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.processor.part.MetadataProcessor;
import org.apache.olingo.odata2.api.processor.part.ServiceDocumentProcessor;
import org.apache.olingo.odata2.api.uri.info.GetMetadataUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetServiceDocumentUriInfo;
import org.apache.olingo.odata2.testutil.helper.HttpMerge;
import org.apache.olingo.odata2.testutil.helper.HttpSomethingUnsupported;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

/**
 *  
 */
public class BasicHttpTest extends AbstractBasicTest {

  public BasicHttpTest(final ServletType servletType) {
    super(servletType);
  }

  @Override
  protected ODataSingleProcessor createProcessor() throws ODataException {
    final ODataSingleProcessor processor = mock(ODataSingleProcessor.class);
    when(((MetadataProcessor) processor).readMetadata(any(GetMetadataUriInfo.class), any(String.class)))
        .thenReturn(ODataResponse.entity("metadata").status(HttpStatusCodes.OK).build());
    when(
        ((ServiceDocumentProcessor) processor).readServiceDocument(any(GetServiceDocumentUriInfo.class),
            any(String.class)))
        .thenReturn(ODataResponse.entity("service document").status(HttpStatusCodes.OK).build());
    return processor;
  }

  @Test
  public void getServiceDocument() throws Exception {
    final HttpResponse response = executeGetRequest("/");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    assertEquals("service document", StringHelper.inputStreamToString(response.getEntity().getContent()));
  }

  @Test
  public void getServiceDocumentWithRedirect() throws Exception {
    final HttpResponse response = executeGetRequest("");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    assertEquals("service document", StringHelper.inputStreamToString(response.getEntity().getContent()));
  }

  @Test
  public void get() throws Exception {
    HttpResponse response = executeGetRequest("$metadata");

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    assertEquals("metadata", StringHelper.inputStreamToString(response.getEntity().getContent()));

    response = executeGetRequest("//////$metadata");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    StringHelper.inputStreamToString(response.getEntity().getContent());
    response = executeGetRequest("/./$metadata");
    assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatusLine().getStatusCode());
    StringHelper.inputStreamToString(response.getEntity().getContent());
    response = executeGetRequest("$metadata/./");
    assertEquals(HttpStatusCodes.BAD_REQUEST.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void head() throws Exception {
    HttpResponse response = executeHeadRequest("/");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    assertNull(response.getEntity());

    response = executeHeadRequest("$metadata");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    assertNull(response.getEntity());

    response = executeHeadRequest("//////$metadata");
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    assertNull(response.getEntity());
    response = executeHeadRequest("/./$metadata");
    assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatusLine().getStatusCode());
    assertNull(response.getEntity());
    response = executeHeadRequest("$metadata/./");
    assertEquals(HttpStatusCodes.BAD_REQUEST.getStatusCode(), response.getStatusLine().getStatusCode());
  }


  @Test
  public void put() throws Exception {
    final HttpPut put = new HttpPut(URI.create(getEndpoint().toString() + "aaa/bbb/ccc"));
    final HttpResponse response = getHttpClient().execute(put);

    assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatusLine().getStatusCode());
    final String payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertTrue(payload.contains("error"));
  }

  @Test
  public void putWithContent() throws Exception {
    final HttpPut put = new HttpPut(URI.create(getEndpoint().toString()));
    final String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<entry xmlns=\"" + Edm.NAMESPACE_ATOM_2005 + "\"" +
            " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\"" +
            " xmlns:d=\"" + Edm.NAMESPACE_D_2007_08 + "\"" +
            " xml:base=\"https://server.at.some.domain.com/path.to.some.service/ReferenceScenario.svc/\">" +
            "</entry>";
    final HttpEntity entity = new StringEntity(xml);
    put.setEntity(entity);
    final HttpResponse response = getHttpClient().execute(put);

    assertEquals(HttpStatusCodes.METHOD_NOT_ALLOWED.getStatusCode(), response.getStatusLine().getStatusCode());
    final String payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertTrue(payload.contains("error"));
  }

  @Test
  public void postMethodNotAllowedWithContent() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString()));
    final String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<entry xmlns=\"" + Edm.NAMESPACE_ATOM_2005 + "\"" +
            " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\"" +
            " xmlns:d=\"" + Edm.NAMESPACE_D_2007_08 + "\"" +
            " xml:base=\"https://server.at.some.domain.com/path.to.some.service/ReferenceScenario.svc/\">" +
            "</entry>";
    final HttpEntity entity = new StringEntity(xml);
    post.setEntity(entity);
    final HttpResponse response = getHttpClient().execute(post);

    assertEquals(HttpStatusCodes.METHOD_NOT_ALLOWED.getStatusCode(), response.getStatusLine().getStatusCode());
    final String payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertTrue(payload.contains("error"));
  }

  @Test
  public void postNotFound() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "aaa/bbb/ccc"));
    final HttpResponse response = getHttpClient().execute(post);

    assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatusLine().getStatusCode());
    final String payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertTrue(payload.contains("error"));
  }

  @Test
  public void delete() throws Exception {
    final HttpDelete delete = new HttpDelete(URI.create(getEndpoint().toString() + "aaa/bbb/ccc"));
    final HttpResponse response = getHttpClient().execute(delete);

    assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatusLine().getStatusCode());
    final String payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertTrue(payload.contains("error"));
  }

  @Test
  public void merge() throws Exception {
    final HttpMerge merge = new HttpMerge(URI.create(getEndpoint().toString() + "aaa/bbb/ccc"));
    final HttpResponse response = getHttpClient().execute(merge);

    assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatusLine().getStatusCode());
    final String payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertTrue(payload.contains("error"));
  }

  @Test
  public void patch() throws Exception {
    HttpPatch get = new HttpPatch(URI.create(getEndpoint().toString() + "aaa/bbb/ccc"));
    final HttpResponse response = getHttpClient().execute(get);

    assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatusLine().getStatusCode());
    final String payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertTrue(payload.contains("error"));
  }

  @Test
  public void unsupportedMethod() throws Exception {
    HttpResponse response = getHttpClient().execute(new HttpOptions(getEndpoint()));
    assertEquals(HttpStatusCodes.NOT_IMPLEMENTED.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void unknownMethod() throws Exception {
    HttpSomethingUnsupported request = new HttpSomethingUnsupported(getEndpoint() + "aaa/bbb/ccc");
    final HttpResponse response = getHttpClient().execute(request);
    assertEquals(HttpStatusCodes.NOT_IMPLEMENTED.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void tunneledByPost() throws Exception {
    tunnelPost("X-HTTP-Method", ODataHttpMethod.MERGE);
    tunnelPost("X-HTTP-Method", ODataHttpMethod.PATCH);
    tunnelPost("X-HTTP-Method", ODataHttpMethod.DELETE);
    tunnelPost("X-HTTP-Method", ODataHttpMethod.PUT);
    tunnelPost("X-HTTP-Method", ODataHttpMethod.GET);
    tunnelPost("X-HTTP-Method", ODataHttpMethod.POST);
    tunnelPost("X-HTTP-Method", "HEAD", HttpStatusCodes.NOT_FOUND);

    tunnelPost("X-HTTP-Method-Override", ODataHttpMethod.MERGE);
    tunnelPost("X-HTTP-Method-Override", ODataHttpMethod.PATCH);
    tunnelPost("X-HTTP-Method-Override", ODataHttpMethod.DELETE);
    tunnelPost("X-HTTP-Method-Override", ODataHttpMethod.PUT);
    tunnelPost("X-HTTP-Method-Override", ODataHttpMethod.GET);
    tunnelPost("X-HTTP-Method-Override", ODataHttpMethod.POST);
    tunnelPost("X-HTTP-Method-Override", "HEAD", HttpStatusCodes.NOT_FOUND);
  }

  private void tunnelPost(final String header, final ODataHttpMethod method) throws IOException {
    tunnelPost(header, method.toString(), HttpStatusCodes.NOT_FOUND);
  }

  private void tunnelPost(final String header, final String method, final HttpStatusCodes expectedStatus)
      throws IOException {
    HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "aaa/bbb/ccc"));
    post.setHeader(header, method);
    final HttpResponse response = getHttpClient().execute(post);
    assertEquals(expectedStatus.getStatusCode(), response.getStatusLine().getStatusCode());

    final String payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertTrue(payload.contains("error"));
  }

  @Test
  public void tunneledBadRequest() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "aaa/bbb/ccc"));
    post.setHeader("X-HTTP-Method", "MERGE");
    post.setHeader("X-HTTP-Method-Override", "PATCH");
    final HttpResponse response = getHttpClient().execute(post);
    assertEquals(HttpStatusCodes.BAD_REQUEST.getStatusCode(), response.getStatusLine().getStatusCode());

    final String payload = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertTrue(payload.contains("error"));
  }

  @Test
  public void tunneledUnsupportedMethod() throws Exception {
    tunnelPost("X-HTTP-Method", HttpMethod.OPTIONS, HttpStatusCodes.NOT_IMPLEMENTED);
    tunnelPost("X-HTTP-Method-Override", HttpMethod.OPTIONS, HttpStatusCodes.NOT_IMPLEMENTED);
  }

  @Test
  public void tunneledUnknownMethod() throws Exception {
    tunnelPost("X-HTTP-Method", "xxx", HttpStatusCodes.NOT_IMPLEMENTED);
  }

  @Test
  public void tunneledUnknownMethodOverride() throws Exception {
    tunnelPost("X-HTTP-Method-Override", "xxx", HttpStatusCodes.NOT_IMPLEMENTED);
  }

  protected HttpResponse executeHeadRequest(final String request) throws IOException {
    final HttpHead head = new HttpHead(URI.create(getEndpoint().toString() + request));
    return getHttpClient().execute(head);
  }
}
