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
package org.apache.olingo.odata2.core.batch;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchQueryPart;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.core.batch.v2.BatchParser;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.BeforeClass;
import org.junit.Test;

public class BatchRequestWriterITTest {
  private static final String POST = "POST";
  private static final String GET = "GET";
  private static final String BOUNDARY = "batch_123";
  private static final String CONTENT_TYPE = "multipart/mixed ;boundary=" + BOUNDARY;
  private static final String SERVICE_ROOT = "http://localhost/odata/";
  private static EntityProviderBatchProperties batchProperties;

  @BeforeClass
  public static void setProperties() throws URISyntaxException {
    PathInfoImpl pathInfo = new PathInfoImpl();
    pathInfo.setServiceRoot(new URI(SERVICE_ROOT));
    batchProperties = EntityProviderBatchProperties.init().pathInfo(pathInfo).build();
  }

  @Test
  public void testQueryPart() throws Exception {
    List<BatchPart> batch = new ArrayList<BatchPart>();
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Accept", "application/json");
    BatchPart request = BatchQueryPart.method(GET).uri("Employees").headers(headers).build();
    batch.add(request);

    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream stream = writer.writeBatchRequest(batch, BOUNDARY);

    List<BatchRequestPart> parsedRequestParts = parseBatchRequest(stream);
    assertEquals(1, parsedRequestParts.size());
    BatchRequestPart part = parsedRequestParts.get(0);

    assertFalse(part.isChangeSet());
    assertEquals(1, part.getRequests().size());
    ODataRequest oDataRequest = part.getRequests().get(0);
    assertEquals("Employees", oDataRequest.getPathInfo().getODataSegments().get(0).getPath());
    assertEquals("application/json", oDataRequest.getAcceptHeaders().get(0));
  }

  @Test
  public void testChangeSet() throws Exception {
    List<BatchPart> batch = new ArrayList<BatchPart>();
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Accept", "application/json");
    BatchPart request = BatchQueryPart.method(GET).uri("Employees").headers(headers).contentId("000").build();
    batch.add(request);

    Map<String, String> changeSetHeaders = new HashMap<String, String>();
    changeSetHeaders.put("content-type", "application/json");
    String body = "/9j/4AAQSkZJRgABAQEBLAEsAAD/4RM0RXhpZgAATU0AKgAAAAgABwESAAMAAAABAAEA";
    BatchChangeSetPart changeRequest = BatchChangeSetPart.method(POST)
        .uri("Employees")
        .body(body)
        .headers(changeSetHeaders)
        .contentId("111")
        .build();
    BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
    changeSet.add(changeRequest);
    batch.add(changeSet);
    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream stream = writer.writeBatchRequest(batch, BOUNDARY);

    final List<BatchRequestPart> parsedRequestParts = parseBatchRequest(stream);
    assertEquals(2, parsedRequestParts.size());

    // Get Request
    final BatchRequestPart partGet = parsedRequestParts.get(0);
    assertFalse(partGet.isChangeSet());
    assertEquals(1, partGet.getRequests().size());
    final ODataRequest oDataRequestGet = partGet.getRequests().get(0);
    assertEquals("Employees", oDataRequestGet.getPathInfo().getODataSegments().get(0).getPath());
    assertEquals("application/json", oDataRequestGet.getAcceptHeaders().get(0));

    // Change set
    final BatchRequestPart partChangeSet = parsedRequestParts.get(1);
    assertTrue(partChangeSet.isChangeSet());
    assertEquals(1, partChangeSet.getRequests().size());
    final ODataRequest oDataRequestPost = partChangeSet.getRequests().get(0);
    assertEquals("Employees", oDataRequestGet.getPathInfo().getODataSegments().get(0).getPath());
    assertEquals("111", oDataRequestPost.getRequestHeaderValue(BatchHelper.MIME_HEADER_CONTENT_ID));
    assertEquals(body, streamToString(oDataRequestPost.getBody()));
    assertEquals("application/json", oDataRequestPost.getRequestHeaderValue(HttpHeaders.CONTENT_TYPE));
  }

  @Test
  public void testChangeSetIso() throws Exception {
    testChangeSetWithCharset("iso-8859-1");
  }

  @Test
  public void testChangeSetUtf8() throws Exception {
    testChangeSetWithCharset("utf-8");
  }

  private void testChangeSetWithCharset(final String charset) throws Exception {
    List<BatchPart> batch = new ArrayList<BatchPart>();
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Accept", "application/json");
    headers.put("CustomHeader", "HeäderVälüe");
    BatchPart request = BatchQueryPart.method(GET).uri("Employees").headers(headers).contentId("000").build();
    batch.add(request);

    Map<String, String> changeSetHeaders = new HashMap<String, String>();
    changeSetHeaders.put("content-type", "application/json; charset=" + charset);
    String body = "äöü/9j/4AAQSkZJRgABAQEBLAEsAAD/4RM0RXhpZgAATU0AKgAAAAgABwESAAMAAAABAAEA";
    StringHelper.Stream stBody = StringHelper.toStream(body, charset);
    BatchChangeSetPart changeRequest = BatchChangeSetPart.method(POST)
        .uri("Employees")
        .body(stBody.asString(charset))
        .headers(changeSetHeaders)
        .contentId("111")
        .build();
    BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
    changeSet.add(changeRequest);
    batch.add(changeSet);
    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream stream = writer.writeBatchRequest(batch, BOUNDARY);

    final List<BatchRequestPart> parsedRequestParts = parseBatchRequest(stream);
    assertEquals(2, parsedRequestParts.size());

    // Get Request
    final BatchRequestPart partGet = parsedRequestParts.get(0);
    assertFalse(partGet.isChangeSet());
    assertEquals(1, partGet.getRequests().size());
    final ODataRequest oDataRequestGet = partGet.getRequests().get(0);
    assertEquals("Employees", oDataRequestGet.getPathInfo().getODataSegments().get(0).getPath());
    validateHeader(oDataRequestGet, "Accept", "application/json");
    validateHeader(oDataRequestGet, "CustomHeader", "HeäderVälüe");

    // Change set
    final BatchRequestPart partChangeSet = parsedRequestParts.get(1);
    assertTrue(partChangeSet.isChangeSet());
    assertEquals(1, partChangeSet.getRequests().size());
    final ODataRequest oDataRequestPost = partChangeSet.getRequests().get(0);
    assertEquals("Employees", oDataRequestGet.getPathInfo().getODataSegments().get(0).getPath());
    assertEquals("111", oDataRequestPost.getRequestHeaderValue(BatchHelper.MIME_HEADER_CONTENT_ID));
    StringHelper.Stream st = StringHelper.toStream(oDataRequestPost.getBody());
    assertEquals(body, st.asString(charset));
    assertEquals("application/json; charset=" + charset,
        oDataRequestPost.getRequestHeaderValue(HttpHeaders.CONTENT_TYPE));
  }

  @Test
  public void testTwoChangeSets() throws Exception {
    List<BatchPart> batch = new ArrayList<BatchPart>();

    // Get request
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Accept", "application/json");
    BatchPart request = BatchQueryPart.method(GET).uri("Employees").headers(headers).contentId("000").build();
    batch.add(request);

    Map<String, String> headerPostRequest = new HashMap<String, String>();
    headerPostRequest.put("content-type", "application/json");

    // Changeset 1
    String bodyEmployee = "/9j/4AAQSkZJRgABAQEBLAEsAAD/4RM0RXhpZgAATU0AKgAAAAgABwESAAMAAAABAAEA";
    BatchChangeSetPart postRequest = BatchChangeSetPart.method(POST)
        .uri("Employees")
        .body(bodyEmployee)
        .headers(headerPostRequest)
        .contentId("111")
        .build();

    String bodyEmployee2 = "TestString\r\n";
    BatchChangeSetPart postRequest2 = BatchChangeSetPart.method(POST)
        .uri("Employees")
        .body(bodyEmployee2)
        .headers(headerPostRequest)
        .contentId("222")
        .build();
    BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
    changeSet.add(postRequest);
    changeSet.add(postRequest2);
    batch.add(changeSet);

    // Changeset 2
    BatchChangeSet changeSet2 = BatchChangeSet.newBuilder().build();
    postRequest2 = BatchChangeSetPart.method(POST)
        .uri("Employees")
        .body(bodyEmployee2)
        .headers(headerPostRequest)
        .contentId("222")
        .build();
    changeSet2.add(postRequest2);
    postRequest = BatchChangeSetPart.method(POST)
        .uri("Employees")
        .body(bodyEmployee)
        .headers(headerPostRequest)
        .contentId("111")
        .build();
    changeSet2.add(postRequest);
    batch.add(changeSet2);

    // Write requests
    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream stream = writer.writeBatchRequest(batch, BOUNDARY);
    // Read requests
    final List<BatchRequestPart> parsedRequestParts = parseBatchRequest(stream);
    assertEquals(3, parsedRequestParts.size());

    // Get request
    final BatchRequestPart partGet = parsedRequestParts.get(0);
    assertFalse(partGet.isChangeSet());
    assertEquals(1, partGet.getRequests().size());
    final ODataRequest oDataRequestGet = partGet.getRequests().get(0);
    assertEquals("Employees", oDataRequestGet.getPathInfo().getODataSegments().get(0).getPath());
    assertEquals("application/json", oDataRequestGet.getAcceptHeaders().get(0));

    // Changeset 1
    BatchRequestPart parsedChangeSet1 = parsedRequestParts.get(1);
    assertTrue(parsedChangeSet1.isChangeSet());
    assertEquals(2, parsedChangeSet1.getRequests().size());
    ODataRequest oDataRequestPost1 = parsedChangeSet1.getRequests().get(0);
    assertEquals("111", oDataRequestPost1.getRequestHeaderValue(BatchHelper.MIME_HEADER_CONTENT_ID));
    assertEquals(bodyEmployee, streamToString(oDataRequestPost1.getBody()));
    assertEquals("application/json", oDataRequestPost1.getRequestHeaderValue(HttpHeaders.CONTENT_TYPE));

    ODataRequest oDataRequestPost12 = parsedChangeSet1.getRequests().get(1);
    assertEquals("222", oDataRequestPost12.getRequestHeaderValue(BatchHelper.MIME_HEADER_CONTENT_ID));
    assertEquals(bodyEmployee2, streamToString(oDataRequestPost12.getBody()));
    assertEquals("application/json", oDataRequestPost12.getRequestHeaderValue(HttpHeaders.CONTENT_TYPE));

    // Changeset 2
    BatchRequestPart parsedChangeSet2 = parsedRequestParts.get(2);
    assertTrue(parsedChangeSet2.isChangeSet());
    assertEquals(2, parsedChangeSet2.getRequests().size());
    ODataRequest oDataRequestPost21 = parsedChangeSet2.getRequests().get(0);
    assertEquals("222", oDataRequestPost21.getRequestHeaderValue(BatchHelper.MIME_HEADER_CONTENT_ID));
    assertEquals(bodyEmployee2, streamToString(oDataRequestPost21.getBody()));
    assertEquals("application/json", oDataRequestPost21.getRequestHeaderValue(HttpHeaders.CONTENT_TYPE));

    ODataRequest oDataRequestPost22 = parsedChangeSet2.getRequests().get(1);
    assertEquals("111", oDataRequestPost22.getRequestHeaderValue(BatchHelper.MIME_HEADER_CONTENT_ID));
    assertEquals(bodyEmployee, streamToString(oDataRequestPost22.getBody()));
    assertEquals("application/json", oDataRequestPost22.getRequestHeaderValue(HttpHeaders.CONTENT_TYPE));
  }

  private List<BatchRequestPart> parseBatchRequest(InputStream batchRequest) throws BatchException {
    final BatchParser parser = new BatchParser(CONTENT_TYPE, batchProperties, true);
    return parser.parseBatchRequest(batchRequest);
  }

  private String streamToString(final InputStream in) throws IOException {
    return StringHelper.toStream(in).asString();
  }

  private void validateHeader(ODataRequest request, String headerName, String expectedValue) {
    String actualValue = request.getRequestHeaderValue(headerName);
    assertNotNull("Expected header '" + headerName + "' is not available.", actualValue);
    assertEquals("Header '" + headerName + "' has value '" + actualValue
        + "' instead of expected '" + expectedValue + "'.", expectedValue, actualValue);
  }
}
