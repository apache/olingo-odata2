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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.core.batch.v2.BatchParser;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.Test;

public class BatchResponseParserTest {

  private static final String CRLF = "\r\n";
  private static final String LF = "\n";

  @Test
  public void testSimpleBatchResponse() throws BatchException {
    String getResponse = "--batch_123" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + "Content-ID: 1" + CRLF
        + CRLF
        + "HTTP/1.1 200 OK" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + "Content-Type: text/plain;charset=utf-8" + CRLF
        + "Content-length: 22" + CRLF
        + CRLF
        + "Frederic Fall MODIFIED" + CRLF
        + "--batch_123--";

    InputStream in = new ByteArrayInputStream(getResponse.getBytes());
    BatchParser parser = new BatchParser("multipart/mixed;boundary=batch_123", true);
    List<BatchSingleResponse> responses = parser.parseBatchResponse(in);
    for (BatchSingleResponse response : responses) {
      assertEquals("200", response.getStatusCode());
      assertEquals("OK", response.getStatusInfo());
      assertEquals("text/plain;charset=utf-8", response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
      assertEquals("22", response.getHeaders().get("Content-length"));
      assertEquals("1", response.getContentId());
      assertEquals("Frederic Fall MODIFIED", response.getBody());
    }
  }

  @Test
  public void testSimpleBatchResponseWithLinebreak() throws BatchException {
    String getResponse = "--batch_123" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + "Content-ID: 1" + CRLF
        + CRLF
        + "HTTP/1.1 200 OK" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + "Content-Type: text/plain;charset=utf-8" + CRLF
        + "Content-length: 24" + CRLF
        + CRLF
        + "Frederic Fall MODIFIED" + CRLF
        + CRLF
        + "--batch_123--";

    InputStream in = new ByteArrayInputStream(getResponse.getBytes());
    BatchParser parser = new BatchParser("multipart/mixed;boundary=batch_123", true);
    List<BatchSingleResponse> responses = parser.parseBatchResponse(in);
    for (BatchSingleResponse response : responses) {
      assertEquals("200", response.getStatusCode());
      assertEquals("OK", response.getStatusInfo());
      assertEquals("text/plain;charset=utf-8", response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
      assertEquals("24", response.getHeaders().get("Content-length"));
      assertEquals("1", response.getContentId());
      assertEquals("Frederic Fall MODIFIED\r\n", response.getBody());
    }
  }

  @Test
  public void testNoContentResponse() throws Exception {
    String responseContent =
        "--ejjeeffe1\r\n" +
            "Content-Type: application/http\r\n" +
            "Content-Length: 96\r\n" +
            "content-transfer-encoding: binary\r\n" +
            "\r\n" +
            "HTTP/1.1 204 No Content\r\n" +
            "Content-Type: text/html\r\n" +
            "dataserviceversion: 2.0\r\n" +
            "\r\n" +
            "\r\n" +
            "--ejjeeffe1--\r\n";

    InputStream in = new ByteArrayInputStream(responseContent.getBytes());
    BatchParser parser = new BatchParser("multipart/mixed;boundary=ejjeeffe1", true);
    List<BatchSingleResponse> responses = parser.parseBatchResponse(in);
    for (BatchSingleResponse response : responses) {
      assertEquals("204", response.getStatusCode());
      assertEquals("No Content", response.getStatusInfo());
      assertEquals("text/html", response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }
  }

  @Test
  public void testBatchResponse() throws BatchException, IOException {
    String fileName = "/batchResponse.batch";
    InputStream in = ClassLoader.class.getResourceAsStream(fileName);
    if (in == null) {
      throw new IOException("Requested file '" + fileName + "' was not found.");
    }
    BatchParser parser = new BatchParser("multipart/mixed;boundary=batch_123", true);
    List<BatchSingleResponse> responses =
        parser.parseBatchResponse(StringHelper.toStream(in).asStreamWithLineSeparation("\r\n"));
    for (BatchSingleResponse response : responses) {
      if ("1".equals(response.getContentId())) {
        assertEquals("204", response.getStatusCode());
        assertEquals("No Content", response.getStatusInfo());
      } else if ("3".equals(response.getContentId())) {
        assertEquals("200", response.getStatusCode());
        assertEquals("OK", response.getStatusInfo());
      }
    }
  }

  @Test
  public void testResponseToChangeSet() throws BatchException {
    String putResponse = "--batch_123" + CRLF
        + "Content-Type: " + HttpContentType.MULTIPART_MIXED + ";boundary=changeset_12ks93js84d" + CRLF
        + CRLF
        + "--changeset_12ks93js84d" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + "Content-ID: 1" + CRLF
        + CRLF
        + "HTTP/1.1 204 No Content" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + CRLF
        + CRLF
        + "--changeset_12ks93js84d--" + CRLF
        + CRLF
        + "--batch_123--";

    InputStream in = new ByteArrayInputStream(putResponse.getBytes());
    BatchParser parser = new BatchParser("multipart/mixed;boundary=batch_123", true);
    List<BatchSingleResponse> responses = parser.parseBatchResponse(in);
    for (BatchSingleResponse response : responses) {
      assertEquals("204", response.getStatusCode());
      assertEquals("No Content", response.getStatusInfo());
      assertEquals("1", response.getContentId());
    }
  }
  
  @Test
  public void testResponseChangeSetBodyWithoutCRLF() throws BatchException {
    String putResponse = "--batch_123" + CRLF
        + "Content-Type: " + HttpContentType.MULTIPART_MIXED + ";boundary=changeset_12ks93js84d" + CRLF
        + CRLF
        + "--changeset_12ks93js84d" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + "Content-ID: 1" + CRLF
        + CRLF
        + "HTTP/1.1 200 Ok" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + "Content-Length: 19" + CRLF
        + CRLF
        + "TestBodyWithoutCRLF" + CRLF
        + "--changeset_12ks93js84d--" + CRLF
        + CRLF
        + "--batch_123--";

    InputStream in = new ByteArrayInputStream(putResponse.getBytes());
    BatchParser parser = new BatchParser("multipart/mixed;boundary=batch_123", true);
    List<BatchSingleResponse> responses = parser.parseBatchResponse(in);
    for (BatchSingleResponse response : responses) {
      assertEquals("200", response.getStatusCode());
      assertEquals("Ok", response.getStatusInfo());
      assertEquals("19", response.getHeader(HttpHeaders.CONTENT_LENGTH));
      assertEquals("TestBodyWithoutCRLF", response.getBody());
      assertEquals("1", response.getContentId());
    }
  }
  
  @Test
  public void testResponseChangeSetBodyWithCRLF() throws BatchException {
    String putResponse = "--batch_123" + CRLF
        + "Content-Type: " + HttpContentType.MULTIPART_MIXED + ";boundary=changeset_12ks93js84d" + CRLF
        + CRLF
        + "--changeset_12ks93js84d" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + "Content-ID: 1" + CRLF
        + CRLF
        + "HTTP/1.1 200 Ok" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + "Content-Length: 18" + CRLF
        + CRLF
        + "TestBodyWithCRLF" + CRLF
        + CRLF
        + "--changeset_12ks93js84d--" + CRLF
        + CRLF
        + "--batch_123--";

    InputStream in = new ByteArrayInputStream(putResponse.getBytes());
    BatchParser parser = new BatchParser("multipart/mixed;boundary=batch_123", true);
    List<BatchSingleResponse> responses = parser.parseBatchResponse(in);
    for (BatchSingleResponse response : responses) {
      assertEquals("200", response.getStatusCode());
      assertEquals("Ok", response.getStatusInfo());
      assertEquals("18", response.getHeader(HttpHeaders.CONTENT_LENGTH));
      assertEquals("TestBodyWithCRLF" + CRLF, response.getBody());
      assertEquals("1", response.getContentId());
    }
  }
  
  @Test
  public void testResponseToChangeSetNoContentButContentLength() throws BatchException {
    String putResponse =
        "--batch_123" + CRLF
            + "Content-Type: " + HttpContentType.MULTIPART_MIXED + ";boundary=changeset_12ks93js84d" + CRLF
            + CRLF
            + "--changeset_12ks93js84d" + CRLF
            + "Content-Type: application/http" + CRLF
            + "Content-Transfer-Encoding: binary" + CRLF
            + "Content-ID: 1" + CRLF
            + CRLF
            + "HTTP/1.1 204 No Content" + CRLF
            + "Content-Length: 0" + CRLF
            + "DataServiceVersion: 2.0" + CRLF
            + CRLF
            + CRLF
            + "--changeset_12ks93js84d--" + CRLF
            + CRLF
            + "--batch_123--";

    InputStream in = new ByteArrayInputStream(putResponse.getBytes());
    BatchParser parser = new BatchParser("multipart/mixed;boundary=batch_123", true);
    List<BatchSingleResponse> responses = parser.parseBatchResponse(in);
    for (BatchSingleResponse response : responses) {
      assertEquals("204", response.getStatusCode());
      assertEquals("No Content", response.getStatusInfo());
      assertEquals("1", response.getContentId());
    }
  }

  @Test(expected = BatchException.class)
  public void testInvalidMimeHeader() throws BatchException {
    String putResponse = "--batch_123" + CRLF
        + "Content-Type: " + HttpContentType.MULTIPART_MIXED + ";boundary=changeset_12ks93js84d" + CRLF
        + CRLF
        + "--changeset_12ks93js84d" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: 7bit" + CRLF // Content-Transfer-Encoding must be binary
        + CRLF
        + "HTTP/1.1 No Content" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + CRLF
        + CRLF
        + "--changeset_12ks93js84d--" + CRLF
        + CRLF
        + "--batch_123--";

    parseInvalidBatchResponseBody(putResponse);
  }

  @Test(expected = BatchException.class)
  public void testMissingMimeHeader() throws BatchException {
    String putResponse = "--batch_123" + CRLF
        + "Content-Type: " + HttpContentType.MULTIPART_MIXED + ";boundary=changeset_12ks93js84d" + CRLF
        + CRLF
        + "--changeset_12ks93js84d" + CRLF
        + CRLF
        + "HTTP/1.1 No Content" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + CRLF
        + CRLF
        + "--changeset_12ks93js84d--" + CRLF
        + CRLF
        + "--batch_123--";

    parseInvalidBatchResponseBody(putResponse);
  }

  @Test(expected = BatchException.class)
  public void testInvalidContentType() throws BatchException {
    String putResponse = "--batch_123" + CRLF
        + "Content-Type: " + HttpContentType.MULTIPART_MIXED + CRLF // Missing boundary parameter
        + CRLF
        + "--changeset_12ks93js84d" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + CRLF
        + "HTTP/1.1 No Content" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + CRLF
        + CRLF
        + "--changeset_12ks93js84d--" + CRLF
        + CRLF
        + "--batch_123--";

    parseInvalidBatchResponseBody(putResponse);
  }

  @Test(expected = BatchException.class)
  public void testInvalidStatusLine() throws BatchException {
    String putResponse = "--batch_123" + CRLF
        + "Content-Type: " + HttpContentType.MULTIPART_MIXED + ";boundary=changeset_12ks93js84d" + CRLF
        + CRLF
        + "--changeset_12ks93js84d" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + CRLF
        + "HTTP/1.1 No Content" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + CRLF
        + CRLF
        + "--changeset_12ks93js84d--" + CRLF
        + CRLF
        + "--batch_123--";

    parseInvalidBatchResponseBody(putResponse);

  }

  @Test(expected = BatchException.class)
  public void testMissingCloseDelimiter() throws BatchException {
    String putResponse = "--batch_123" + CRLF
        + "Content-Type: " + HttpContentType.MULTIPART_MIXED + ";boundary=changeset_12ks93js84d" + CRLF
        + CRLF
        + "--changeset_12ks93js84d" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + CRLF
        + "HTTP/1.1 204 No Content" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + CRLF
        + CRLF
        + "--changeset_12ks93js84d--" + CRLF
        + CRLF;

    parseInvalidBatchResponseBody(putResponse);

  }

  @Test
  public void tooBigContentLegthDoesNotResultInException() throws BatchException {
    String getResponse = "--batch_123" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + "Content-ID: 1" + CRLF
        + CRLF
        + "HTTP/1.1 200 OK" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + "Content-Type: text/plain;charset=utf-8" + CRLF
        + "Content-Length: 100" + CRLF
        + CRLF
        + "Frederic Fall" + CRLF
        + "--batch_123--";

    InputStream in = new ByteArrayInputStream(getResponse.getBytes());
    List<BatchSingleResponse> batchResponse =
        EntityProvider.parseBatchResponse(in, "multipart/mixed;boundary=batch_123");
    BatchSingleResponse response = batchResponse.get(0);
    assertEquals("100", response.getHeader("Content-Length"));
    assertEquals("Frederic Fall", response.getBody());
  }

  @Test(expected = BatchException.class)
  public void testInvalidBoundary() throws BatchException {
    String getResponse = "--batch_321" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + "Content-ID: 1" + CRLF
        + CRLF
        + "HTTP/1.1 200 OK" + CRLF
        + "DataServiceVersion: 2.0" + CRLF
        + "Content-Type: text/plain;charset=utf-8" + CRLF
        + CRLF
        + "Frederic Fall" + CRLF
        + CRLF
        + "--batch_123--";

    parseInvalidBatchResponseBody(getResponse);
  }

  @Test
  public void boundaryInBodyMustBeIgnored() throws BatchException {
    String getResponse = "--batch_123" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + CRLF
        + "HTTP/1.1 200 OK" + CRLF
        + "Content-Type: text/plain;charset=utf-8" + CRLF
        + "Content-Length: 13" + CRLF
        + CRLF
        + "Frederic Fall" + CRLF
        + CRLF
        + "batch_123" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Transfer-Encoding: binary" + CRLF
        + CRLF
        + "HTTP/1.1 200 OK" + CRLF
        + "Content-Type: text/plain;charset=utf-8" + CRLF
        + CRLF
        + "Walter Winter" + CRLF
        + CRLF
        + "--batch_123--";
    InputStream in = new ByteArrayInputStream(getResponse.getBytes());
    List<BatchSingleResponse> batchResponse =
        EntityProvider.parseBatchResponse(in, "multipart/mixed;boundary=batch_123");
    BatchSingleResponse response = batchResponse.get(0);
    assertEquals("13", response.getHeader("Content-Length"));
    assertEquals("Frederic Fall", response.getBody());
  }

  @Test
  public void parseWithAdditionalLineEndingAtTheEnd() throws Exception {
    String fileString = readFile("BatchResponseWithAdditionalLineEnding.batch");
    assertTrue(fileString.contains("\r\n--batch_123--"));
    InputStream stream = new ByteArrayInputStream(fileString.getBytes());
    BatchSingleResponse response =
        EntityProvider.parseBatchResponse(stream, "multipart/mixed;boundary=batch_123").get(0);
    assertEquals("This is the body we need to parse. The trailing line ending is part of the body." + CRLF, response
        .getBody());

  }

  @Test
  public void parseWithWindowsLineEndingsInBody() throws Exception {
    InputStream stream = getFileAsStream("BatchResponseWithLinesInBodyWin.batch");
    BatchSingleResponse response =
        EntityProvider.parseBatchResponse(stream, "multipart/mixed;boundary=batch_123").get(0);
    String body =
        "This is the body we need to parse. The line spaces in the body " + CRLF + CRLF + CRLF + "are " + CRLF + CRLF
            + "part of the body and must not be ignored or filtered.";

    assertEquals(body, response.getBody());
  }

  @Test
  public void parseWithUnixLineEndingsInBody() throws Exception {
    String body =
        "This is the body we need to parse. The line spaces in the body " + LF + LF + LF + "are " + LF + LF
            + "part of the body and must not be ignored or filtered.";
    String responseString = "--batch_123" + CRLF
        + "Content-Type: application/http" + CRLF
        + "Content-Length: 234" + CRLF
        + "content-transfer-encoding: binary" + CRLF
        + CRLF
        + "HTTP/1.1 500 Internal Server Error" + CRLF
        + "Content-Type: application/xml;charset=utf-8" + CRLF
        + "Content-Length: 125" + CRLF
        + CRLF
        + body
        + CRLF
        + "--batch_123--";
    InputStream stream = new ByteArrayInputStream(responseString.getBytes());
    BatchSingleResponse response =
        EntityProvider.parseBatchResponse(stream, "multipart/mixed;boundary=batch_123").get(0);

    assertEquals(body, response.getBody());
  }

  protected String readFile(final String filename) throws IOException {
    InputStream in = getFileAsStream(filename);

    byte[] tmp = new byte[8192];
    int count = in.read(tmp);
    StringBuilder b = new StringBuilder();
    while (count >= 0) {
      b.append(new String(tmp, 0, count));
      count = in.read(tmp);
    }

    return b.toString();
  }

  private InputStream getFileAsStream(final String filename) throws IOException {
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    if (in == null) {
      throw new IOException("Requested file '" + filename + "' was not found.");
    }
    return in;
  }

  private void parseInvalidBatchResponseBody(final String putResponse) throws BatchException {
    InputStream in = new ByteArrayInputStream(putResponse.getBytes());
    BatchParser parser = new BatchParser("multipart/mixed;boundary=batch_123", true);
    parser.parseBatchResponse(in);
  }
}
