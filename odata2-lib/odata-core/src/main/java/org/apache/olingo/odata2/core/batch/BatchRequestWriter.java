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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchQueryPart;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;

public class BatchRequestWriter {
  private static final String REG_EX_BOUNDARY =
      "([a-zA-Z0-9_\\-\\.'\\+]{1,70})|\"([a-zA-Z0-9_\\-\\.'\\+\\s\\" +
          "(\\),/:=\\?]{1,69}[a-zA-Z0-9_\\-\\.'\\+\\(\\),/:=\\?])\""; // See RFC 2046

  private static final String COLON = ":";
  private static final String SP = " ";
  private static final String CRLF = "\r\n";
  public static final String BOUNDARY_PREAMBLE = "changeset";
  public static final String HTTP_1_1 = "HTTP/1.1";
  private String batchBoundary;
  private BodyBuilder writer = new BodyBuilder();

  public InputStream writeBatchRequest(final List<BatchPart> batchParts, final String boundary) {
    if (boundary.matches(REG_EX_BOUNDARY)) {
      batchBoundary = boundary;
    } else {
      throw new IllegalArgumentException();
    }
    for (BatchPart batchPart : batchParts) {
      writer.append("--").append(boundary).append(CRLF);
      if (batchPart instanceof BatchChangeSet) {
        appendChangeSet((BatchChangeSet) batchPart);
      } else if (batchPart instanceof BatchQueryPart) {
        BatchQueryPart request = (BatchQueryPart) batchPart;
        appendRequestBodyPart(request);
      }
      
    }
    writer.append("--").append(boundary).append("--");
    InputStream batchRequestBody;
    batchRequestBody = new ByteArrayInputStream(writer.getContent());
    return batchRequestBody;
  }


  private void appendChangeSet(final BatchChangeSet batchChangeSet) {
    String boundary = BatchHelper.generateBoundary(BOUNDARY_PREAMBLE);
    while (boundary.equals(batchBoundary) || !boundary.matches(REG_EX_BOUNDARY)) {
      boundary = BatchHelper.generateBoundary(BOUNDARY_PREAMBLE);
    }
    writer.append(HttpHeaders.CONTENT_TYPE).append(COLON).append(SP).append(
        HttpContentType.MULTIPART_MIXED + "; boundary=" + boundary).append(CRLF);
    for (BatchChangeSetPart request : batchChangeSet.getChangeSetParts()) {
      writer.append(CRLF).append("--").append(boundary).append(CRLF);
      appendRequestBodyPart(request);
    }
    writer.append(CRLF).append("--").append(boundary).append("--").append(CRLF);
  }

  private void appendRequestBodyPart(BatchQueryPart request) {
    appendRequestBodyPart(request.getMethod(), request.getUri(), request.getHeaders(),
        new Body(null), request.getContentId());
  }

  private void appendRequestBodyPart(BatchChangeSetPart request) {
    appendRequestBodyPart(request.getMethod(), request.getUri(), request.getHeaders(),
        new Body(request), request.getContentId());
  }

  private void appendRequestBodyPart(String method, String uri,
                                     Map<String, String> headers, Body body, String contentId) {

    boolean isContentLengthPresent = false;
    writer.append(HttpHeaders.CONTENT_TYPE).append(COLON).append(SP).append(HttpContentType.APPLICATION_HTTP)
        .append(CRLF);
    writer.append(BatchHelper.HTTP_CONTENT_TRANSFER_ENCODING).append(COLON).append(SP)
        .append(BatchHelper.BINARY_ENCODING).append(CRLF);
    if (contentId != null) {
      writer.append(BatchHelper.HTTP_CONTENT_ID).append(COLON).append(SP).append(contentId).append(CRLF);
    }
    String contentLength = getHeaderValue(headers, HttpHeaders.CONTENT_LENGTH);
    if (contentLength != null && !contentLength.isEmpty()) {
      isContentLengthPresent = true;
    }
    writer.append(CRLF);
    writer.append(method).append(SP).append(uri).append(SP).append(HTTP_1_1);
    writer.append(CRLF);

    if (!isContentLengthPresent && !body.isEmpty()) {
      writer.append(HttpHeaders.CONTENT_LENGTH).append(COLON).append(SP).append(body.getLength()).append(CRLF);
    }
    appendHeader(headers);
    writer.append(CRLF);

    if (!body.isEmpty()) {
      writer.append(body);
    } else {
      writer.append(CRLF);
    }
  }

  private void appendHeader(final Map<String, String> headers) {
    for (Map.Entry<String, String> headerMap : headers.entrySet()) {
      String name = headerMap.getKey();
      writer.append(name).append(COLON).append(SP).append(headerMap.getValue()).append(CRLF);
    }
  }

  private String getHeaderValue(final Map<String, String> headers, final String headerName) {
    for (Map.Entry<String, String> header : headers.entrySet()) {
      if (headerName.equalsIgnoreCase(header.getKey())) {
        return header.getValue();
      }
    }
    return null;
  }

  /**
   * Builder class to create the body and the header.
   */
  private class BodyBuilder {
    private final Charset CHARSET_ISO_8859_1 = Charset.forName("iso-8859-1");
    private ByteBuffer buffer = ByteBuffer.allocate(8192);
    private boolean isClosed = false;

    public byte[] getContent() {
      isClosed = true;
      byte[] tmp = new byte[buffer.position()];
      buffer.flip();
      buffer.get(tmp, 0, buffer.limit());
      return tmp;
    }

    public BodyBuilder append(String string) {
      byte [] b = string.getBytes(CHARSET_ISO_8859_1);
      put(b);
      return this;
    }

    private void put(byte[] b) {
      if(isClosed) {
        throw new RuntimeException("BodyBuilder is closed.");
      }
      if(buffer.remaining() < b.length) {
        buffer.flip();
        ByteBuffer tmp = ByteBuffer.allocate(buffer.limit() *2);
        tmp.put(buffer);
        buffer = tmp;
      }
      buffer.put(b);
    }

    public BodyBuilder append(int statusCode) {
      return append(String.valueOf(statusCode));
    }

    public BodyBuilder append(Body body) {
      put(body.getContent());
      return this;
    }

    public String toString() {
      return new String(buffer.array(), 0, buffer.position());
    }
  }

  /**
   * Body part which is read and stored as bytes (no charset conversion).
   */
  private class Body {
    private final byte[] content;

    public Body(BatchChangeSetPart response) {
      this.content = getBody(response);
    }

    public int getLength() {
      return content.length;
    }

    public byte[] getContent() {
      return content;
    }

    public boolean isEmpty() {
      return content.length == 0;
    }

    private byte[] getBody(final BatchChangeSetPart response) {
      if (response == null || response.getBodyAsBytes() == null) {
        return new byte[0];
      }

      return response.getBodyAsBytes();
    }
  }
}
