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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchQueryPart;
import org.apache.olingo.odata2.core.batch.v2.BufferedReaderIncludingLineEndings;
import org.apache.olingo.odata2.core.batch.v2.Line;
import org.junit.Ignore;
import org.junit.Test;

public class BatchRequestWriterTest {

  private static final String POST = "POST";
  private static final String GET = "GET";
  private static final String PUT = "PUT";
  private static final String BOUNDARY = "batch_123";
  private static final Object CRLF = "\r\n";

  @Test
  public void testBatchQueryPart() throws BatchException, IOException {
    List<BatchPart> batch = new ArrayList<BatchPart>();
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Accept", "application/json");
    BatchPart request = BatchQueryPart.method(GET).uri("Employees").headers(headers).build();
    batch.add(request);

    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream batchRequest = writer.writeBatchRequest(batch, BOUNDARY);
    
    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchRequest);
    List<Line> lines = reader.toLineList();
    reader.close();
    int index = 0;
    
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("GET Employees HTTP/1.1" + CRLF, lines.get(index++).toString());
    assertEquals("Accept: application/json" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }

  @Test
  public void testBatchChangeSet() throws IOException, BatchException {
    List<BatchPart> batch = new ArrayList<BatchPart>();
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("content-type", "application/json");
    BatchChangeSetPart request = BatchChangeSetPart.method(PUT)
        .uri("Employees('2')")
        .body("{\"Age\":40}")
        .headers(headers)
        .contentId("111")
        .build();
    BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
    changeSet.add(request);
    batch.add(changeSet);

    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream batchRequest = writer.writeBatchRequest(batch, BOUNDARY);

    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchRequest);
    List<Line> lines = reader.toLineList();
    reader.close();
    int index = 0;
     
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertTrue(lines.get(index++).toString().startsWith("Content-Type: multipart/mixed; boundary=changeset_"));
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Id: 111" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("PUT Employees('2') HTTP/1.1" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 10" + CRLF, lines.get(index++).toString());
    assertEquals("content-type: application/json" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("{\"Age\":40}" + CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }

  @Test
  public void testBatchChangeSetUtf8() throws IOException, BatchException {
    List<BatchPart> batch = new ArrayList<BatchPart>();
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("content-type", "application/json; charset=utf-8");
    BatchChangeSetPart request = BatchChangeSetPart.method(PUT)
        .uri("Employees('2')")
        .body("{\"Age\":40, \"Name\": \"Wälter Winter\"}")
        .headers(headers)
        .contentId("111")
        .build();
    BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
    changeSet.add(request);
    batch.add(changeSet);

    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream batchRequest = writer.writeBatchRequest(batch, BOUNDARY);

    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchRequest);
    List<Line> lines = reader.toLineList();
    reader.close();
    int index = 0;

    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertTrue(lines.get(index++).toString().startsWith("Content-Type: multipart/mixed; boundary=changeset_"));
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Id: 111" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("PUT Employees('2') HTTP/1.1" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 36" + CRLF, lines.get(index++).toString());
    assertEquals("content-type: application/json; charset=utf-8" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("{\"Age\":40, \"Name\": \"Wälter Winter\"}" + CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }

  @Test
  public void testBatchChangeSetIso() throws IOException, BatchException {
    List<BatchPart> batch = new ArrayList<BatchPart>();
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("content-type", "application/json; charset=iso-8859-1");
    BatchChangeSetPart request = BatchChangeSetPart.method(PUT)
        .uri("Employees('2')")
        .body("{\"Age\":40, \"Name\": \"Wälter Winter\"}")
        .headers(headers)
        .contentId("111")
        .build();
    BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
    changeSet.add(request);
    batch.add(changeSet);

    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream batchRequest = writer.writeBatchRequest(batch, BOUNDARY);

    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchRequest);
    List<Line> lines = reader.toLineList();
    reader.close();
    int index = 0;

    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertTrue(lines.get(index++).toString().startsWith("Content-Type: multipart/mixed; boundary=changeset_"));
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Id: 111" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("PUT Employees('2') HTTP/1.1" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 35" + CRLF, lines.get(index++).toString());
    assertEquals("content-type: application/json; charset=iso-8859-1" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("{\"Age\":40, \"Name\": \"Wälter Winter\"}" + CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }

  @Test
  public void testBatchWithGetAndPost() throws BatchException, IOException {
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
    InputStream batchRequest = writer.writeBatchRequest(batch, BOUNDARY);

    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchRequest);
    List<Line> lines = reader.toLineList();
    reader.close();
    int index = 0;

    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Id: 000" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("GET Employees HTTP/1.1" + CRLF, lines.get(index++).toString());
    assertEquals("Accept: application/json" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertTrue(lines.get(index++).toString().startsWith("Content-Type: multipart/mixed; boundary=changeset_"));
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Id: 111" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("POST Employees HTTP/1.1" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 68" + CRLF, lines.get(index++).toString());
    assertEquals("content-type: application/json" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals(body + CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }

  @Test
  public void testGetRequest() throws IOException {
    List<BatchPart> batch = new ArrayList<BatchPart>();

    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Accept", "application/json");
    BatchPart request = BatchQueryPart.method(GET).uri("Employees").headers(headers).contentId("123").build();

    batch.add(request);
    batch.add(request);

    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream batchRequest = writer.writeBatchRequest(batch, BOUNDARY);

    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchRequest);
    List<Line> lines = reader.toLineList();
    reader.close();

    int line = 0;
    assertEquals("--" + BOUNDARY + CRLF, lines.get(line++).toString());
    assertEquals("Content-Type: application/http" + CRLF, lines.get(line++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(line++).toString());
    assertEquals("Content-Id: 123" + CRLF, lines.get(line++).toString());
    assertEquals(CRLF, lines.get(line++).toString());
    assertEquals("GET Employees HTTP/1.1" + CRLF, lines.get(line++).toString());
    assertEquals("Accept: application/json" + CRLF, lines.get(line++).toString());
    assertEquals(CRLF, lines.get(line++).toString()); // Belongs to the GET request [OData Protocol - 2.2.7.2.1]

    assertEquals(CRLF, lines.get(line++).toString()); // Belongs conceptually to the boundary [RFC 2046 - 5.1.1]
    assertEquals("--" + BOUNDARY + CRLF, lines.get(line++).toString());
    assertEquals("Content-Type: application/http" + CRLF, lines.get(line++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(line++).toString());
    assertEquals("Content-Id: 123" + CRLF, lines.get(line++).toString());
    assertEquals(CRLF, lines.get(line++).toString());
    assertEquals("GET Employees HTTP/1.1" + CRLF, lines.get(line++).toString());
    assertEquals("Accept: application/json" + CRLF, lines.get(line++).toString());
    assertEquals(CRLF, lines.get(line++).toString()); // Belongs to the GET request [OData Protocol - 2.2.7.2.1]

    assertEquals(CRLF, lines.get(line++).toString()); // Belongs conceptually to the boundary [RFC 2046 - 5.1.1]
    assertEquals("--" + BOUNDARY + "--", lines.get(line++).toString());
    assertEquals(19, lines.size());
  }

  @Test
  public void testChangeSetWithContentIdReferencing() throws BatchException, IOException {
    List<BatchPart> batch = new ArrayList<BatchPart>();

    Map<String, String> changeSetHeaders = new HashMap<String, String>();
    changeSetHeaders.put("content-type", "application/json");
    String body = "/9j/4AAQSkZJRgABAQEBLAEsAAD/4RM0RXhpZgAATU0AKgAAAAgABwESAAMAAAABAAEA";
    BatchChangeSetPart changeRequest = BatchChangeSetPart.method(POST)
        .uri("Employees('2')")
        .body(body)
        .headers(changeSetHeaders)
        .contentId("1")
        .build();
    BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
    changeSet.add(changeRequest);

    changeSetHeaders = new HashMap<String, String>();
    changeSetHeaders.put("content-type", "application/json;odata=verbose");
    BatchChangeSetPart changeRequest2 = BatchChangeSetPart.method(PUT)
        .uri("$1/ManagerId")
        .body("{\"ManagerId\":1}")
        .headers(changeSetHeaders)
        .contentId("2")
        .build();
    changeSet.add(changeRequest2);
    batch.add(changeSet);

    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream batchRequest = writer.writeBatchRequest(batch, BOUNDARY);

    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchRequest);
    List<Line> lines = reader.toLineList();
    reader.close();

    int index = 0;
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertTrue(lines.get(index++).toString().startsWith("Content-Type: multipart/mixed; boundary=changeset_"));
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Id: 1" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("POST Employees('2') HTTP/1.1" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 68" + CRLF, lines.get(index++).toString());
    assertEquals("content-type: application/json" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals(body + CRLF, lines.get(index++).toString());
    
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Id: 2" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("PUT $1/ManagerId HTTP/1.1" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 15" + CRLF, lines.get(index++).toString());
    assertEquals("content-type: application/json;odata=verbose" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("{\"ManagerId\":1}" + CRLF, lines.get(index++).toString());
    
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }

  @Test
  public void testBatchWithTwoChangeSets() throws BatchException, IOException {
    List<BatchPart> batch = new ArrayList<BatchPart>();

    Map<String, String> changeSetHeaders = new HashMap<String, String>();
    changeSetHeaders.put("content-type", "application/json");
    changeSetHeaders.put("content-Id", "111");
    String body = "/9j/4AAQSkZJRgABAQEBLAEsAAD/4RM0RXhpZgAATU0AKgAAAAgABwESAAMAAAABAAEA";
    BatchChangeSetPart changeRequest = BatchChangeSetPart.method(POST)
        .uri("Employees")
        .body(body)
        .headers(changeSetHeaders)
        .build();
    BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
    changeSet.add(changeRequest);
    batch.add(changeSet);

    Map<String, String> changeSetHeaders2 = new HashMap<String, String>();
    changeSetHeaders2.put("content-type", "application/json;odata=verbose");
    changeSetHeaders2.put("content-Id", "222");
    BatchChangeSetPart changeRequest2 = BatchChangeSetPart.method(PUT)
        .uri("Employees('2')/ManagerId")
        .body("{\"ManagerId\":1}")
        .headers(changeSetHeaders2)
        .build();
    BatchChangeSet changeSet2 = BatchChangeSet.newBuilder().build();
    changeSet2.add(changeRequest2);
    batch.add(changeSet2);

    BatchRequestWriter writer = new BatchRequestWriter();
    InputStream batchRequest = writer.writeBatchRequest(batch, BOUNDARY);
    
    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(batchRequest);
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
    assertEquals("POST Employees HTTP/1.1" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 68" + CRLF, lines.get(index++).toString());
    assertEquals("content-type: application/json" + CRLF, lines.get(index++).toString());
    assertEquals("content-Id: 111" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals(body + CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertTrue(lines.get(index++).toString().startsWith("Content-Type: multipart/mixed; boundary=changeset_"));
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("PUT Employees('2')/ManagerId HTTP/1.1" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 15" + CRLF, lines.get(index++).toString());
    assertEquals("content-type: application/json;odata=verbose" + CRLF, lines.get(index++).toString());
    assertEquals("content-Id: 222" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("{\"ManagerId\":1}" + CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBatchQueryPartWithInvalidMethod() throws BatchException, IOException {
    BatchQueryPart.method(PUT).uri("Employees").build();

  }

  @Test(expected = IllegalArgumentException.class)
  public void testBatchChangeSetPartWithInvalidMethod() throws BatchException, IOException {
    BatchChangeSetPart.method(GET).uri("Employees('2')").build();

  }
}
