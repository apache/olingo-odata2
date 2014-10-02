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
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.core.batch.v2.BatchParserCommon.HeaderField;

public class BatchBodyPart implements BatchPart {
  final private String boundary;
  final private boolean isStrict;
  final List<String> remainingMessage = new LinkedList<String>();

  private Map<String, HeaderField> headers;
  private boolean isChangeSet;
  private List<BatchQueryOperation> requests;

  public BatchBodyPart(final List<String> bodyPartMessage, final String boundary, final boolean isStrict)
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

  private boolean isChangeSet(final Map<String, HeaderField> headers) throws BatchException {
    final HeaderField contentTypeField = headers.get(HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.ENGLISH));
    boolean isChangeSet = false;

    if (contentTypeField == null || contentTypeField.getValues().size() == 0) {
      throw new BatchException(BatchException.MISSING_CONTENT_TYPE);
    }

    for (String contentType : contentTypeField.getValues()) {
      if (isContentTypeMultiPartMixed(contentType)) {
        isChangeSet = true;
      }
    }

    return isChangeSet;
  }

  private boolean isContentTypeMultiPartMixed(final String contentType) {
    return BatchParserCommon.PATTERN_MULTIPART_BOUNDARY.matcher(contentType).matches();
  }

  private List<BatchQueryOperation> consumeRequest(final List<String> remainingMessage) throws BatchException {
    if (isChangeSet) {
      return consumeChangeSet(remainingMessage);
    } else {
      return consumeQueryOperation(remainingMessage);
    }
  }

  private List<BatchQueryOperation> consumeChangeSet(final List<String> remainingMessage)
      throws BatchException {
    final List<List<String>> changeRequests = splitChangeSet(remainingMessage);
    final List<BatchQueryOperation> requestList = new LinkedList<BatchQueryOperation>();

    for (List<String> changeRequest : changeRequests) {
      requestList.add(new BatchChangeSetPart(changeRequest, isStrict).parse());
    }

    return requestList;
  }

  private List<List<String>> splitChangeSet(final List<String> remainingMessage)
      throws BatchException {

    final String changeSetBoundary = BatchParserCommon.getBoundary(getContentType());
    validateChangeSetBoundary(changeSetBoundary);

    return BatchParserCommon.splitMessageByBoundary(remainingMessage, changeSetBoundary);
  }

  private List<BatchQueryOperation> consumeQueryOperation(final List<String> remainingMessage)
      throws BatchException {
    final List<BatchQueryOperation> requestList = new LinkedList<BatchQueryOperation>();
    requestList.add(new BatchQueryOperation(remainingMessage, isStrict).parse());

    return requestList;
  }

  private void validateChangeSetBoundary(final String changeSetBoundary) throws BatchException {
    if (changeSetBoundary.equals(boundary)) {
      throw new BatchException(BatchException.INVALID_BOUNDARY);
    }
  }

  private String getContentType() {
    HeaderField contentTypeField = headers.get(HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.ENGLISH));

    return (contentTypeField != null && contentTypeField.getValues().size() > 0) ? contentTypeField.getValues().get(0)
        : "";
  }

  @Override
  public Map<String, HeaderField> getHeaders() {
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
