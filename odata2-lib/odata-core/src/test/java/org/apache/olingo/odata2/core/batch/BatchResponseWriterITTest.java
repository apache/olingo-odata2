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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.batch.v2.BatchParser;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.junit.Test;

public class BatchResponseWriterITTest {

  @Test
  public void testSimpleRequest() throws Exception {
    // Create batch response
    List<BatchResponsePart> parts = new ArrayList<BatchResponsePart>();
    ODataResponse response =
        ODataResponse.entity("Walter Winter").status(HttpStatusCodes.OK).contentHeader("application/json").build();
    List<ODataResponse> responses = new ArrayList<ODataResponse>(1);
    responses.add(response);
    parts.add(BatchResponsePart.responses(responses).changeSet(false).build());
    BatchResponseWriter writer = new BatchResponseWriter();
    ODataResponse batchResponse = writer.writeResponse(parts);

    assertEquals(202, batchResponse.getStatus().getStatusCode());
    assertNotNull(batchResponse.getEntity());
    String body = (String) batchResponse.getEntity();
    // Get boundary
    int lineEndingIndex = body.indexOf("\r\n");
    String boundary = body.substring(2, lineEndingIndex);

    // Parse response and test outputs
    final BatchParser parser = new BatchParser("multipart/mixed;boundary=" + boundary, true);
    List<BatchSingleResponse> parserResponses = parser.parseBatchResponse(new ByteArrayInputStream(body.getBytes()));
    for (BatchSingleResponse parserResponse : parserResponses) {
      assertEquals("200", parserResponse.getStatusCode());
      assertEquals("OK", parserResponse.getStatusInfo());
      assertEquals("application/json", parserResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
      assertEquals("13", parserResponse.getHeaders().get(HttpHeaders.CONTENT_LENGTH));
      assertEquals("Walter Winter", parserResponse.getBody());
    }
  }

  @Test
  public void testNoContent() throws Exception {
    // Create batch response
    List<BatchResponsePart> parts = new ArrayList<BatchResponsePart>();
    ODataResponse response =
        ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
    List<ODataResponse> responses = new ArrayList<ODataResponse>(1);
    responses.add(response);
    parts.add(BatchResponsePart.responses(responses).changeSet(false).build());
    BatchResponseWriter writer = new BatchResponseWriter();
    ODataResponse batchResponse = writer.writeResponse(parts);

    assertEquals(202, batchResponse.getStatus().getStatusCode());
    assertNotNull(batchResponse.getEntity());
    String body = (String) batchResponse.getEntity();
    // Get boundary
    int lineEndingIndex = body.indexOf("\r\n");
    String boundary = body.substring(2, lineEndingIndex);

    // Parse response and test outputs
    final BatchParser parser = new BatchParser("multipart/mixed;boundary=" + boundary, true);
    List<BatchSingleResponse> parserResponses = parser.parseBatchResponse(new ByteArrayInputStream(body.getBytes()));
    for (BatchSingleResponse parserResponse : parserResponses) {
      assertEquals("204", parserResponse.getStatusCode());
      assertEquals("No Content", parserResponse.getStatusInfo());
    }
  }

  @Test
  public void testChangeSet() throws Exception {
    List<BatchResponsePart> parts = new ArrayList<BatchResponsePart>();
    ODataResponse response = ODataResponse.entity("Walter Winter")
        .status(HttpStatusCodes.OK)
        .contentHeader("application/json")
        .build();
    List<ODataResponse> responses = new ArrayList<ODataResponse>(1);
    responses.add(response);
    parts.add(BatchResponsePart.responses(responses).changeSet(false).build());

    ODataResponse changeSetResponse =
        ODataResponse.status(HttpStatusCodes.NO_CONTENT).header(BatchHelper.MIME_HEADER_CONTENT_ID, "1").build();
    responses = new ArrayList<ODataResponse>(2);
    ODataResponse changeSetResponseEntity =
        ODataResponse.status(HttpStatusCodes.OK).contentHeader(ContentType.APPLICATION_JSON.toContentTypeString())
            .header(BatchHelper.MIME_HEADER_CONTENT_ID, "2")
            .entity("Test\r\n").build();
    ODataResponse changeSetResponseEntity2 =
        ODataResponse.status(HttpStatusCodes.OK).contentHeader(ContentType.APPLICATION_JSON.toContentTypeString())
            .header(BatchHelper.MIME_HEADER_CONTENT_ID, "2")
            .entity("Test\n").build();
    ODataResponse changeSetResponseEntity3 =
        ODataResponse.status(HttpStatusCodes.OK).contentHeader(ContentType.APPLICATION_JSON.toContentTypeString())
            .header(BatchHelper.MIME_HEADER_CONTENT_ID, "2")
            .entity("Test").build();
    responses.add(changeSetResponse);
    responses.add(changeSetResponseEntity);
    responses.add(changeSetResponseEntity2);
    responses.add(changeSetResponseEntity3);

    parts.add(BatchResponsePart.responses(responses).changeSet(true).build());

    BatchResponseWriter writer = new BatchResponseWriter();
    ODataResponse batchResponse = writer.writeResponse(parts);

    assertEquals(202, batchResponse.getStatus().getStatusCode());
    assertNotNull(batchResponse.getEntity());
    String body = (String) batchResponse.getEntity();

    // Get boundary
    int lineEndingIndex = body.indexOf("\r\n");
    String boundary = body.substring(2, lineEndingIndex);

    // Parse response and test outputs
    final BatchParser parser = new BatchParser("multipart/mixed;boundary=" + boundary, true);
    List<BatchSingleResponse> parserResponses = parser.parseBatchResponse(new ByteArrayInputStream(body.getBytes()));
    assertEquals(5, parserResponses.size());

    BatchSingleResponse parserResponse = parserResponses.get(0);
    assertEquals("200", parserResponse.getStatusCode());
    assertEquals("OK", parserResponse.getStatusInfo());
    assertEquals("application/json", parserResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    assertEquals("13", parserResponse.getHeaders().get(HttpHeaders.CONTENT_LENGTH));
    assertEquals("Walter Winter", parserResponse.getBody());

    parserResponse = parserResponses.get(1);
    assertEquals("204", parserResponse.getStatusCode());
    assertEquals("1", parserResponse.getContentId());
    assertEquals("No Content", parserResponse.getStatusInfo());

    parserResponse = parserResponses.get(2);
    assertEquals("200", parserResponse.getStatusCode());
    assertEquals("OK", parserResponse.getStatusInfo());
    assertEquals("application/json", parserResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    assertEquals("6", parserResponse.getHeaders().get(HttpHeaders.CONTENT_LENGTH));
    assertEquals("Test\r\n", parserResponse.getBody());

    parserResponse = parserResponses.get(3);
    assertEquals("200", parserResponse.getStatusCode());
    assertEquals("OK", parserResponse.getStatusInfo());
    assertEquals("application/json", parserResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    assertEquals("5", parserResponse.getHeaders().get(HttpHeaders.CONTENT_LENGTH));
    assertEquals("Test\n", parserResponse.getBody());

    parserResponse = parserResponses.get(4);
    assertEquals("200", parserResponse.getStatusCode());
    assertEquals("OK", parserResponse.getStatusInfo());
    assertEquals("application/json", parserResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    assertEquals("4", parserResponse.getHeaders().get(HttpHeaders.CONTENT_LENGTH));
    assertEquals("Test", parserResponse.getBody());
  }
}
