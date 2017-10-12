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
package org.apache.olingo.odata2.core.batch.v2;

import java.util.LinkedList;
import java.util.List;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.core.batch.v2.Header.HeaderField;

public class BatchBodyPart implements BatchPart {
  final private String boundary;
  final private boolean isStrict;
  final List<Line> remainingMessage = new LinkedList<Line>();

  private Header headers;
  private boolean isChangeSet;
  private List<BatchQueryOperation> requests;

  public BatchBodyPart(final List<Line> bodyPartMessage, final String boundary, final boolean isStrict)
      throws BatchException {
    this.boundary = boundary;
    this.isStrict = isStrict;

    remainingMessage.addAll(bodyPartMessage);
  }

  public BatchBodyPart parse() throws BatchException {
    headers = BatchParserCommon.consumeHeaders(remainingMessage);
    BatchParserCommon.consumeBlankLine(remainingMessage, isStrict);
    isChangeSet = isChangeSet(headers);
    requests = consumeRequest(remainingMessage);

    return this;
  }

  private boolean isChangeSet(final Header headers) throws BatchException {
    final List<String> contentTypes = headers.getHeaders(HttpHeaders.CONTENT_TYPE);
    boolean isChgSet = false;

    if (contentTypes.isEmpty()) {
      throw new BatchException(BatchException.MISSING_CONTENT_TYPE.addContent(headers.getLineNumber()));
    }

    for (String contentType : contentTypes) {
      if (isContentTypeMultiPartMixed(contentType)) {
        isChgSet = true;
      }
    }

    return isChgSet;
  }

  private boolean isContentTypeMultiPartMixed(final String contentType) {
    return BatchParserCommon.PATTERN_MULTIPART_MIXED.matcher(contentType).matches();
  }

  private List<BatchQueryOperation> consumeRequest(final List<Line> remainingMessage) throws BatchException {
    if (isChangeSet) {
      return consumeChangeSet(remainingMessage);
    } else {
      return consumeQueryOperation(remainingMessage);
    }
  }

  private List<BatchQueryOperation> consumeChangeSet(final List<Line> remainingMessage)
      throws BatchException {
    final List<List<Line>> changeRequests = splitChangeSet(remainingMessage);
    final List<BatchQueryOperation> requestList = new LinkedList<BatchQueryOperation>();

    for (List<Line> changeRequest : changeRequests) {
      requestList.add(new BatchChangeSetPart(changeRequest, isStrict).parse());
    }

    return requestList;
  }

  private List<List<Line>> splitChangeSet(final List<Line> remainingMessage)
      throws BatchException {

    final HeaderField contentTypeField = headers.getHeaderField(HttpHeaders.CONTENT_TYPE);
    final String changeSetBoundary =
        BatchParserCommon.getBoundary(contentTypeField.getValueNotNull(), contentTypeField.getLineNumber());
    validateChangeSetBoundary(changeSetBoundary, headers);

    return BatchParserCommon.splitMessageByBoundary(remainingMessage, changeSetBoundary);
  }

  private List<BatchQueryOperation> consumeQueryOperation(final List<Line> remainingMessage)
      throws BatchException {
    final List<BatchQueryOperation> requestList = new LinkedList<BatchQueryOperation>();
    requestList.add(new BatchQueryOperation(remainingMessage, isStrict).parse());

    return requestList;
  }

  private void validateChangeSetBoundary(final String changeSetBoundary, final Header header) throws BatchException {
    if (changeSetBoundary.equals(boundary)) {
      throw new BatchException(BatchException.INVALID_BOUNDARY.addContent(header.getHeaderField(
          HttpHeaders.CONTENT_TYPE).getLineNumber()));
    }
  }

  @Override
  public Header getHeaders() {
    return headers;
  }

  @Override
  public boolean isStrict() {
    return isStrict;
  }

  public boolean isChangeSet() {
    return isChangeSet;
  }

  public List<BatchQueryOperation> getRequests() {
    return requests;
  }
}
