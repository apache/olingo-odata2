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
import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.core.batch.BatchRequestWriter;
import org.apache.olingo.odata2.ref.processor.Util;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

/**
 * 
 *  
 */
public class BatchTest extends AbstractRefTest {

  private static final String PUT = "PUT";
  private static final String POST = "POST";
  private static final String BOUNDARY = "batch_123";
  
  public BatchTest(final ServletType servletType) {
    super(servletType);
  }

  @Test
  public void testSimpleBatch() throws Exception {
    String responseBody = execute("/simple.batch");
    assertFalse(responseBody
        .contains("<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">"));
    assertTrue(responseBody.contains(
        "<edmx:Edmx xmlns:edmx=\"http://schemas.microsoft.com/ado/2007/06/edmx\" Version=\"1.0\""));
  }

  @Test
  public void functionImportBatch() throws Exception {
    String responseBody = execute("/functionImport.batch");
    assertFalse(responseBody
        .contains("<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">"));
    assertTrue(responseBody.contains("HTTP/1.1 200 OK"));
    assertTrue(responseBody.contains("<?xml version='1.0' encoding='utf-8'?><ManagerPhoto xmlns="));
  }

  @Test
  public void employeesWithFilterBatch() throws Exception {
    String responseBody = execute("/employeesWithFilter.batch");
    assertFalse(responseBody
        .contains("<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">"));
    assertTrue(responseBody.contains("HTTP/1.1 200 OK"));
    assertTrue(responseBody.contains("<d:EmployeeName>Walter Winter</d:EmployeeName>"));
  }

  @Test
  public void testChangeSetBatch() throws Exception {
    String responseBody = execute("/changeset.batch");
    assertTrue(responseBody.contains("Frederic Fall MODIFIED"));
  }

  @Test
  public void testContentIdReferencing() throws Exception {
    String responseBody = execute("/batchWithContentId.batch");
    assertTrue(responseBody.contains("HTTP/1.1 201 Created"));
    assertTrue(responseBody.contains("HTTP/1.1 204 No Content"));
    assertTrue(responseBody.contains("HTTP/1.1 200 OK"));
    assertTrue(responseBody.contains("\"EmployeeName\":\"Frederic Fall MODIFIED\""));
    assertTrue(responseBody.contains("\"Age\":40"));
  }

  @Test
  public void testContentIdEchoing() throws Exception {
    String responseBody = execute("/batchWithContentId.batch");
    assertTrue(responseBody.contains("Content-Id: 1"));
    assertTrue(responseBody.contains("Content-Id: 2"));
    assertTrue(responseBody.contains("Content-Id: 3"));
    assertTrue(responseBody.contains("Content-Id: 4"));
    assertTrue(responseBody.contains("Content-Id: AAA"));
    assertTrue(responseBody.contains("Content-Id: newEmployee"));
  }

  @Test
  public void testWrongContentId() throws Exception {
    HttpResponse response = execute("/batchWithWrongContentId.batch", "batch_cf90-46e5-1246");
    String responseBody = StringHelper.inputStreamToString(response.getEntity().getContent(), true);
    assertTrue(responseBody.contains("HTTP/1.1 404 Not Found"));
  }

  @Test
  public void testFailFirstRequest() throws Exception {
    HttpResponse response = execute("/batchFailFirstCreateRequest.batch", "batch_cf90-46e5-1246");
    String responseBody = StringHelper.inputStreamToString(response.getEntity().getContent(), true);
    assertTrue(responseBody.contains("HTTP/1.1 404 Not Found"));
  }

  @Test
  public void testGPPG() throws Exception {
    HttpResponse response = execute("/batchWithContentIdPart2.batch", "batch_cf90-46e5-1246");
    String responseBody = StringHelper.inputStreamToString(response.getEntity().getContent(), true);

    assertContentContainValues(responseBody,
        "{\"d\":{\"EmployeeName\":\"Frederic Fall\"}}",
        "HTTP/1.1 201 Created",
        "Content-Id: employee",
        "Content-Type: application/json;odata=verbose",
        "\"EmployeeId\":\"7\",\"EmployeeName\":\"Employee 7\",",
        "HTTP/1.1 204 No Content",
        "Content-Id: AAA",
        "{\"d\":{\"EmployeeName\":\"Robert Fall\"}}");

    // validate that response for PUT does not contains a Content Type
    int indexNoContent = responseBody.indexOf("HTTP/1.1 204 No Content");
    int indexBoundary = responseBody.indexOf("--changeset_", indexNoContent);

    int indexContentType = responseBody.indexOf("Content-Type:", indexNoContent);
    Assert.assertTrue(indexBoundary < indexContentType);
  }

  @Test
  public void testErrorBatch() throws Exception {
    String responseBody = execute("/error.batch");
    assertTrue(responseBody.contains("HTTP/1.1 404 Not Found"));
  }

  /**
   * Validate that given <code>content</code> contains all <code>values</code> in the given order.
   * 
   * @param content
   * @param containingValues
   */
  private void assertContentContainValues(final String content, final String... containingValues) {
    int index = -1;
    for (String value : containingValues) {
      int newIndex = content.indexOf(value, index);
      Assert.assertTrue("Value '" + value + "' not found after index position '" + index + "'.", newIndex >= 0);
      index = newIndex;
    }
  }

  private String execute(final String batchResource) throws Exception {
    HttpResponse response = execute(batchResource, "batch_123");

    String responseBody = StringHelper.inputStreamToStringCRLFLineBreaks(response.getEntity().getContent());
    return responseBody;
  }

  private HttpResponse execute(final String batchResource, final String boundary) throws IOException,
      UnsupportedEncodingException, ClientProtocolException {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "$batch"));
    post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);

    String body = StringHelper.inputStreamToStringCRLFLineBreaks(this.getClass().getResourceAsStream(batchResource));
    HttpEntity entity = new StringEntity(body);
    post.setEntity(entity);
    HttpResponse response = getHttpClient().execute(post);

    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    return response;
  }
  
  /**
   * @param method
   * @param data
   * @param contentType 
   * @return
   */
  private InputStream createBatchRequest(String method, byte[] data, String contentType) {
    List<BatchPart> batch = new ArrayList<BatchPart>();
    Map<String, String> headers = new HashMap<String, String>();
    
    BatchChangeSetPart request = null;
    if (method.equalsIgnoreCase(PUT)) {
      headers.put("content-type", contentType);
      request = BatchChangeSetPart.method(PUT)
          .uri("Employees('2')/$value")
          .body(data)
          .headers(headers)
          .contentId("1")
          .build();
    } else if (method.equalsIgnoreCase(POST)) {
      headers.put("content-type", contentType);
      request = BatchChangeSetPart.method(POST)
          .uri("Employees")
          .body(data)
          .headers(headers)
          .contentId("1")
          .build();
    }
    
    BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
    changeSet.add(request);
    batch.add(changeSet);

    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream batchRequest = writer.writeBatchRequest(batch, BOUNDARY);
    
    return batchRequest;
  }
  
  @Test
  public void testBatchWithChangesetWithRawBytesInPutOperation() throws Exception {
    InputStream requestPayload = createBatchRequestWithRawBytes(PUT);
    final HttpPost put = new HttpPost(URI.create(getEndpoint().toString() + "$batch"));
    put.setHeader("Content-Type", "multipart/mixed;boundary=" + BOUNDARY);
    HttpEntity entity = new InputStreamEntity(requestPayload, -1);
    put.setEntity(entity);
    HttpResponse response = getHttpClient().execute(put);
    byte[] actualData = Util.getInstance().getBinaryContent();
    byte[] expectedData = rawBytes();
    // Comparing data stored in the data source and the data sent in the request
    assertArrayEquals(actualData, expectedData);
    
    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    String responseBody = StringHelper.inputStreamToStringCRLFLineBreaks(response.getEntity().getContent());
    assertTrue(responseBody.contains("204 No Content"));
    
    HttpResponse resp = execute("/simpleGet.batch", BOUNDARY);
    InputStream in = resp.getEntity().getContent();
    StringHelper.Stream batchRequestStream = StringHelper.toStream(in);
    String requestBody = batchRequestStream.asString();
    
    String contentType = resp.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
    List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(
        new ByteArrayInputStream(requestBody.getBytes("iso-8859-1")), contentType);
    for (BatchSingleResponse batchResp : responses) {
      assertEquals("200", batchResp.getStatusCode());
      assertEquals("OK", batchResp.getStatusInfo());
      assertArrayEquals(batchResp.getBody().getBytes("iso-8859-1"), actualData);
    }
  }
  
  @Test
  public void testBatchWithChangesetWithRawBytesInPOSTOperation() throws Exception {
    InputStream requestPayload = createBatchRequestWithRawBytes(POST);
    final HttpPost put = new HttpPost(URI.create(getEndpoint().toString() + "$batch"));
    put.setHeader("Content-Type", "multipart/mixed;boundary=" + BOUNDARY);
    HttpEntity entity = new InputStreamEntity(requestPayload, -1);
    put.setEntity(entity);
    HttpResponse response = getHttpClient().execute(put);
    byte[] actualData = Util.getInstance().getBinaryContent();
    byte[] expectedData = rawBytes();
    // Comparing data stored in the data source and the data sent in the request
    assertArrayEquals(actualData, expectedData);
    
    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    String responseBody = StringHelper.inputStreamToStringCRLFLineBreaks(response.getEntity().getContent());
    assertTrue(responseBody.contains("201 Created"));
    
    HttpResponse resp = execute("/simpleGet1.batch", BOUNDARY);
    InputStream in = resp.getEntity().getContent();
    StringHelper.Stream batchRequestStream = StringHelper.toStream(in);
    String requestBody = batchRequestStream.asString();
    
    String contentType = resp.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
    List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(
        new ByteArrayInputStream(requestBody.getBytes("iso-8859-1")), contentType);
    for (BatchSingleResponse batchResp : responses) {
      assertEquals("200", batchResp.getStatusCode());
      assertEquals("OK", batchResp.getStatusInfo());
      assertArrayEquals(batchResp.getBody().getBytes("iso-8859-1"), expectedData);
    }
  }
  
  @Test
  public void testBatchWithChangesetWithImageObjectInPutOperation() throws Exception {
    InputStream requestPayload = createBatchRequestWithImage("/Employee_1.png", PUT);
    
    final HttpPost put = new HttpPost(URI.create(getEndpoint().toString() + "$batch"));
    put.setHeader("Content-Type", "multipart/mixed;boundary=" + BOUNDARY);
    HttpEntity entity = new InputStreamEntity(requestPayload, -1);
    put.setEntity(entity);
    HttpResponse response = getHttpClient().execute(put);
    byte[] actualData = Util.getInstance().getBinaryContent();
    byte[] expectedData = getImageData("/Employee_1.png");
    // Comparing data stored in the data source and the data sent in the request
    assertArrayEquals(actualData, expectedData);
    
    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    String responseBody = StringHelper.inputStreamToStringCRLFLineBreaks(response.getEntity().getContent());
    assertTrue(responseBody.contains("204 No Content"));
    
    HttpResponse resp = execute("/simpleGet.batch", BOUNDARY);
    InputStream in = resp.getEntity().getContent();
    StringHelper.Stream batchRequestStream = StringHelper.toStream(in);
    String requestBody = batchRequestStream.asString();
    
    String contentType = resp.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
    List<BatchSingleResponse> responses = EntityProvider.parseBatchResponse(
        new ByteArrayInputStream(requestBody.getBytes("iso-8859-1")), contentType);
    for (BatchSingleResponse batchResp : responses) {
      assertEquals("200", batchResp.getStatusCode());
      assertEquals("OK", batchResp.getStatusInfo());
      assertArrayEquals(batchResp.getBody().getBytes("iso-8859-1"), actualData);
    }
  }
  
  private InputStream createBatchRequestWithImage(String imageUrl, String method) throws IOException {
    byte[] data = getImageData(imageUrl);
    return createBatchRequest(method, data, "image/jpeg");
  }
  
  private InputStream createBatchRequestWithRawBytes(String method) {
    byte[] data = rawBytes();
    return createBatchRequest(method, data, "application/octect-stream");
  }

  /**
   * @return
   */
  private byte[] rawBytes() {
    byte[] data = new byte[Byte.MAX_VALUE - Byte.MIN_VALUE + 1];
    // binary content, not a valid UTF-8 representation of a string
    for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
      data[i - Byte.MIN_VALUE] = (byte) i;
    }
    return data;
  }

  /**
   * @param imageUrl
   * @return
   * @throws IOException 
   */
  private byte[] getImageData(String imageUrl) throws IOException {
    byte[] data = null;
    try {
      InputStream in = this.getClass().getResourceAsStream(imageUrl);
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      int b = 0;
      while ((b = in.read()) != -1) {
        stream.write(b);
      }

      data = stream.toByteArray();
    } catch (IOException e) {
      throw new IOException(e);
    }
    return data;
  }
}