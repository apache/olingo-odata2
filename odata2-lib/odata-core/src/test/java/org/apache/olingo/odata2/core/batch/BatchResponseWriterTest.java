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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.batch.v2.BufferedReaderIncludingLineEndings;
import org.apache.olingo.odata2.core.batch.v2.Line;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.Test;

public class BatchResponseWriterTest {

  private static final String CRLF = "\r\n";

  @Test
  public void testBatchResponse() throws ODataException, IOException {
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
//    StringHelper.Stream stream = StringHelper.toStream((InputStream) batchResponse.getEntity());
//    String body = stream.toString();

//    BufferedReaderIncludingLineEndings reader =
//        new BufferedReaderIncludingLineEndings(new InputStreamReader(new ByteArrayInputStream(body.getBytes())));
    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchResponse.getEntityAsStream());
    List<Line> lines = reader.toLineList();
    reader.close();
    int index = 0;

    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("HTTP/1.1 200 OK" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Type: application/json" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 13" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("Walter Winter" + CRLF, lines.get(index++).toString());
    
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertTrue(lines.get(index++).toString().startsWith("Content-Type: multipart/mixed; boundary=changeset_"));
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("HTTP/1.1 204 No Content" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }

  @Test
  public void testResponse() throws Exception {
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
//    String body = (String) batchResponse.getEntity();
    
    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchResponse.getEntityAsStream());
    List<Line> lines = reader.toLineList();
    reader.close();
    int index = 0;
    
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("HTTP/1.1 200 OK" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Type: application/json" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 13" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("Walter Winter" + CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }

  @Test
  public void testChangeSetResponse() throws Exception {
    List<BatchResponsePart> parts = new ArrayList<BatchResponsePart>();
    ODataResponse changeSetResponse = ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
    List<ODataResponse> responses = new ArrayList<ODataResponse>(1);
    responses.add(changeSetResponse);
    parts.add(BatchResponsePart.responses(responses).changeSet(true).build());

    BatchResponseWriter writer = new BatchResponseWriter();
    ODataResponse batchResponse = writer.writeResponse(parts);

    assertEquals(202, batchResponse.getStatus().getStatusCode());
    assertNotNull(batchResponse.getEntity());

    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchResponse.getEntityAsStream());
    List<Line> lines = reader.toLineList();
    reader.close();
    int index = 0;
    
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertTrue(lines.get(index++).toString().startsWith("Content-Type: multipart/mixed; boundary=changeset_"));
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("HTTP/1.1 204 No Content" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }

  @Test
  public void testContentIdEchoing() throws Exception {
    List<BatchResponsePart> parts = new ArrayList<BatchResponsePart>();
    ODataResponse response = ODataResponse.entity("Walter Winter")
        .status(HttpStatusCodes.OK)
        .contentHeader("application/json")
        .header(BatchHelper.MIME_HEADER_CONTENT_ID, "mimeHeaderContentId123")
        .header(BatchHelper.REQUEST_HEADER_CONTENT_ID, "requestHeaderContentId123")
        .build();
    List<ODataResponse> responses = new ArrayList<ODataResponse>(1);
    responses.add(response);
    parts.add(BatchResponsePart.responses(responses).changeSet(false).build());
    BatchResponseWriter writer = new BatchResponseWriter();
    ODataResponse batchResponse = writer.writeResponse(parts);

    assertEquals(202, batchResponse.getStatus().getStatusCode());
    assertNotNull(batchResponse.getEntity());

    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchResponse.getEntityAsStream());
    List<Line> lines = reader.toLineList();
    reader.close();
    int index = 0;

    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Id: mimeHeaderContentId123" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("HTTP/1.1 200 OK" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Id: requestHeaderContentId123" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Type: application/json" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 13" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("Walter Winter" + CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }

}
