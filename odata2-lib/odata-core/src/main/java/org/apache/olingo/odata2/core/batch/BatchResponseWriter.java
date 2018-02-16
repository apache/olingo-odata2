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
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.commons.ContentType;

import java.util.List;

public class BatchResponseWriter {
  private static final String COLON = ":";
  private static final String SP = " ";
  private static final String CRLF = "\r\n";
  private final boolean writeEntityAsInputStream;
  private BatchHelper.BodyBuilder writer = new BatchHelper.BodyBuilder();

  /**
   * Creates a BatchResponseWriter which write the <code>entity</code> as a String with
   * default charset (see BatchHelper.DEFAULT_CHARSET).
   */
  public BatchResponseWriter() {
    this(false);
  }

  /**
   * Creates a BatchResponseWriter
   *
   * @param writeEntityAsInputStream
   *        if <code>true</code> the <code>entity</code> is set a InputStream.
   *        if <code>false</code> the <code>entity</code> is set a String with
   *        default charset (see BatchHelper.DEFAULT_CHARSET).
   */
  public BatchResponseWriter(boolean writeEntityAsInputStream) {
    this.writeEntityAsInputStream = writeEntityAsInputStream;
  }

  public ODataResponse writeResponse(final List<BatchResponsePart> batchResponseParts) throws BatchException {
    String boundary = BatchHelper.generateBoundary("batch");
    appendResponsePart(batchResponseParts, boundary);
    final Object batchResponseBody;
    int length = 0;
    if(writeEntityAsInputStream) {
      batchResponseBody = writer.getContentAsStream();
      length = writer.calculateLength(batchResponseBody);
    } else {
      batchResponseBody = writer.getContentAsString(BatchHelper.DEFAULT_CHARSET);
      length = writer.calculateLength(batchResponseBody);
    }
    return ODataResponse.entity(batchResponseBody).status(HttpStatusCodes.ACCEPTED)
        .header(HttpHeaders.CONTENT_TYPE, HttpContentType.MULTIPART_MIXED + "; boundary=" + boundary)
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(length))
        .build();
  }

  private void appendChangeSet(final BatchResponsePart batchResponsePart) throws BatchException {
    String boundary = BatchHelper.generateBoundary("changeset");
    writer.append(HttpHeaders.CONTENT_TYPE).append(COLON).append(SP)
        .append("multipart/mixed; boundary=" + boundary).append(CRLF).append(CRLF);
    for (ODataResponse response : batchResponsePart.getResponses()) {
      writer.append("--").append(boundary).append(CRLF);
      appendResponsePartBody(response);
    }
    writer.append("--").append(boundary).append("--").append(CRLF);
  }

  private void appendResponsePart(final List<BatchResponsePart> batchResponseParts, final String boundary)
      throws BatchException {
    for (BatchResponsePart batchResponsePart : batchResponseParts) {
      writer.append("--").append(boundary).append(CRLF);
      if (batchResponsePart.isChangeSet()) {
        appendChangeSet(batchResponsePart);
      } else {
        ODataResponse response = batchResponsePart.getResponses().get(0);
        appendResponsePartBody(response);
      }
    }
    writer.append("--").append(boundary).append("--");
  }

  private void appendResponsePartBody(final ODataResponse response) throws BatchException {
    writer.append(HttpHeaders.CONTENT_TYPE).append(COLON).append(SP)
        .append(HttpContentType.APPLICATION_HTTP).append(CRLF);
    writer.append(BatchHelper.HTTP_CONTENT_TRANSFER_ENCODING).append(COLON).append(SP)
        .append(BatchHelper.BINARY_ENCODING).append(CRLF);
    if (response.getHeader(BatchHelper.MIME_HEADER_CONTENT_ID) != null) {
      writer.append(BatchHelper.HTTP_CONTENT_ID).append(COLON).append(SP)
          .append(response.getHeader(BatchHelper.MIME_HEADER_CONTENT_ID)).append(CRLF);
    }
    writer.append(CRLF);
    writer.append("HTTP/1.1").append(SP).append(String.valueOf(response.getStatus().getStatusCode())).append(SP)
        .append(response.getStatus().getInfo()).append(CRLF);
    if (response.getHeader("Content-Type") != null) {
      BatchHelper.extractCharset(ContentType.parse(response.getHeader("Content-Type")));
    }
    appendHeader(response);
    if (!HttpStatusCodes.NO_CONTENT.equals(response.getStatus())) {
      BatchHelper.Body body = new BatchHelper.Body(response);
      writer.append(HttpHeaders.CONTENT_LENGTH).append(COLON).append(SP)
          .append(String.valueOf(body.getLength())).append(CRLF).append(CRLF);
      writer.append(body);
    } else {
      // No header if status code equals to 204 (No content)
      writer.append(CRLF);
    }
    writer.append(CRLF);
  }

  private void appendHeader(final ODataResponse response) {
    for (String name : response.getHeaderNames()) {
      if (!BatchHelper.MIME_HEADER_CONTENT_ID.equalsIgnoreCase(name)
          && !BatchHelper.REQUEST_HEADER_CONTENT_ID.equalsIgnoreCase(name)) {
        writer.append(name).append(COLON).append(SP).append(response.getHeader(name)).append(CRLF);
      } else if (BatchHelper.REQUEST_HEADER_CONTENT_ID.equalsIgnoreCase(name)) {
        writer.append(BatchHelper.HTTP_CONTENT_ID).append(COLON).append(SP)
            .append(response.getHeader(name)).append(CRLF);
      }
    }
  }
}
