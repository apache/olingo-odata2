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

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test creation of a batch response with BatchResponseWriter and
 * then parsing this response again with BatchResponseParser.
 */
public class BatchResponseTest {

  @Test
  public void testBatchResponse() throws BatchException, IOException {
    List<BatchResponsePart> parts = new ArrayList<BatchResponsePart>();
    ODataResponse response = ODataResponse.entity("Walter Winter")
        .status(HttpStatusCodes.OK)
        .contentHeader("application/json")
        .build();
    List<ODataResponse> responses = new ArrayList<ODataResponse>(1);
    responses.add(response);
    parts.add(BatchResponsePart.responses(responses).changeSet(false).build());

    ODataResponse changeSetResponse = ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
    responses = new ArrayList<ODataResponse>(1);
    responses.add(changeSetResponse);
    parts.add(BatchResponsePart.responses(responses).changeSet(true).build());

    BatchResponseWriter writer = new BatchResponseWriter();
    ODataResponse batchResponse = writer.writeResponse(parts);

    assertEquals(202, batchResponse.getStatus().getStatusCode());
    assertNotNull(batchResponse.getEntity());
    String body = (String) batchResponse.getEntity();

    assertTrue(body.contains("--batch"));
    assertTrue(body.contains("--changeset"));
    assertTrue(body.contains("HTTP/1.1 200 OK"));
    assertTrue(body.contains("Content-Type: application/http"));
    assertTrue(body.contains("Content-Transfer-Encoding: binary"));
    assertTrue(body.contains("Walter Winter"));
    assertTrue(body.contains("multipart/mixed; boundary=changeset"));
    assertTrue(body.contains("HTTP/1.1 204 No Content"));

    String contentHeader = batchResponse.getContentHeader();
    BatchResponseParser parser = new BatchResponseParser(contentHeader);
    List<BatchSingleResponse> result = parser.parse(new ByteArrayInputStream(body.getBytes()));
    assertEquals(2, result.size());
  }

  @Test
  public void testChangeSetResponse() throws BatchException, IOException {
    List<BatchResponsePart> parts = new ArrayList<BatchResponsePart>();
    ODataResponse changeSetResponse = ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
    List<ODataResponse> responses = new ArrayList<ODataResponse>(1);
    responses.add(changeSetResponse);
    parts.add(BatchResponsePart.responses(responses).changeSet(true).build());

    BatchResponseWriter writer = new BatchResponseWriter();
    ODataResponse batchResponse = writer.writeResponse(parts);

    assertEquals(202, batchResponse.getStatus().getStatusCode());
    assertNotNull(batchResponse.getEntity());
    String body = (String) batchResponse.getEntity();
    assertTrue(body.contains("--batch"));
    assertTrue(body.contains("--changeset"));
    assertTrue(body.indexOf("--changeset") != body.lastIndexOf("--changeset"));
    assertFalse(body.contains("HTTP/1.1 200 OK" + "\r\n"));
    assertTrue(body.contains("Content-Type: application/http" + "\r\n"));
    assertTrue(body.contains("Content-Transfer-Encoding: binary" + "\r\n"));
    assertTrue(body.contains("HTTP/1.1 204 No Content" + "\r\n"));
    assertTrue(body.contains("Content-Type: multipart/mixed; boundary=changeset"));

    String contentHeader = batchResponse.getContentHeader();
    BatchResponseParser parser = new BatchResponseParser(contentHeader);
    List<BatchSingleResponse> result = parser.parse(new ByteArrayInputStream(body.getBytes()));
    assertEquals(1, result.size());
  }

  @Test
  public void testTwoChangeSetResponse() throws BatchException, IOException {
    List<BatchResponsePart> parts = new ArrayList<BatchResponsePart>();
    ODataResponse changeSetResponse = ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
    ODataResponse changeSetResponseTwo = ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
    List<ODataResponse> responses = new ArrayList<ODataResponse>(1);
    responses.add(changeSetResponse);
    responses.add(changeSetResponseTwo);
    parts.add(BatchResponsePart.responses(responses).changeSet(true).build());

    BatchResponseWriter writer = new BatchResponseWriter();
    ODataResponse batchResponse = writer.writeResponse(parts);

    assertEquals(202, batchResponse.getStatus().getStatusCode());
    assertNotNull(batchResponse.getEntity());
    String body = (String) batchResponse.getEntity();
    assertTrue(body.contains("--batch"));
    assertTrue(body.contains("--changeset"));
    assertTrue(body.indexOf("--changeset") != body.lastIndexOf("--changeset"));
    assertFalse(body.contains("HTTP/1.1 200 OK" + "\r\n"));
    assertTrue(body.contains("Content-Type: application/http" + "\r\n"));
    assertTrue(body.contains("Content-Transfer-Encoding: binary" + "\r\n"));
    assertTrue(body.contains("HTTP/1.1 204 No Content" + "\r\n"));
    assertTrue(body.contains("Content-Type: multipart/mixed; boundary=changeset"));

    String contentHeader = batchResponse.getContentHeader();
    BatchResponseParser parser = new BatchResponseParser(contentHeader);
    StringHelper.Stream content = StringHelper.toStream(body);
    List<BatchSingleResponse> result = parser.parse(content.asStream());
    assertEquals(2, result.size());
    assertEquals("Failing content:\n" + content.asString(), 20, content.linesCount());
  }
}