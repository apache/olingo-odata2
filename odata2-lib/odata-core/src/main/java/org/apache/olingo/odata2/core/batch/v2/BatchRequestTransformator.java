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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchParserResult;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataRequest.ODataRequestBuilder;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.core.batch.BatchHelper;
import org.apache.olingo.odata2.core.batch.BatchRequestPartImpl;
import org.apache.olingo.odata2.core.batch.v2.BufferedReaderIncludingLineEndings.Line;
import org.apache.olingo.odata2.core.batch.v2.Header.HeaderField;

public class BatchRequestTransformator implements BatchTransformator {

  private static final Set<String> HTTP_BATCH_METHODS = new HashSet<String>(Arrays.asList(new String[] { "GET" }));
  private static final Set<String> HTTP_CHANGE_SET_METHODS = new HashSet<String>(Arrays.asList(new String[] { "POST",
      "PUT", "DELETE", "MERGE", "PATCH" }));

  @Override
  public List<BatchParserResult> transform(final BatchBodyPart bodyPart, final PathInfo pathInfo, final String baseUri)
      throws BatchException {

    final List<ODataRequest> requests = new LinkedList<ODataRequest>();
    final List<BatchParserResult> resultList = new ArrayList<BatchParserResult>();

    validateHeader(bodyPart, false);

    for (BatchQueryOperation queryOperation : bodyPart.getRequests()) {
      requests.add(processQueryOperation(bodyPart, pathInfo, baseUri, queryOperation));
    }

    resultList.add(new BatchRequestPartImpl(bodyPart.isChangeSet(), requests));
    return resultList;
  }

  private void validateHeader(final BatchPart bodyPart, boolean isChangeSet) throws BatchException {
    Header headers = bodyPart.getHeaders();

    BatchTransformatorCommon.validateContentType(headers);
    BatchTransformatorCommon.validateContentTransferEncoding(headers, isChangeSet);
  }

  private ODataRequest processQueryOperation(final BatchBodyPart bodyPart, final PathInfo pathInfo,
      final String baseUri, final BatchQueryOperation queryOperation) throws BatchException {

    if (bodyPart.isChangeSet()) {
      BatchQueryOperation encapsulatedQueryOperation = ((BatchChangeSetPart) queryOperation).getRequest();
      Header headers = transformHeader(encapsulatedQueryOperation, queryOperation);
      validateHeader(queryOperation, true);

      return createRequest(queryOperation, headers, pathInfo, baseUri, bodyPart.isChangeSet());
    } else {

      Header headers = transformHeader(queryOperation, bodyPart);
      return createRequest(queryOperation, headers, pathInfo, baseUri, bodyPart.isChangeSet());
    }
  }

  private ODataRequest createRequest(final BatchQueryOperation operation, final Header headers,
      final PathInfo pathInfo, final String baseUri, final boolean isChangeSet) throws BatchException {

    final int httpLineNumber = operation.getHttpStatusLine().getLineNumber();
    ODataHttpMethod httpMethod = getHttpMethod(operation.getHttpStatusLine());
    validateHttpMethod(httpMethod, isChangeSet, httpLineNumber);
    validateBody(httpMethod, operation, httpLineNumber);
    InputStream bodyStrean = getBodyStream(operation, headers, httpMethod);

    ODataRequestBuilder requestBuilder = ODataRequest.method(httpMethod)
        .acceptableLanguages(getAcceptLanguageHeaders(headers))
        .acceptHeaders(headers.getHeaders(HttpHeaders.ACCEPT))
        .allQueryParameters(BatchParserCommon.parseQueryParameter(operation.getHttpStatusLine()))
        .body(bodyStrean)
        .requestHeaders(headers.toMultiMap())
        .pathInfo(BatchParserCommon.parseRequestUri(operation.getHttpStatusLine(), pathInfo, baseUri, 0));

    final String contentType = headers.getHeader(HttpHeaders.CONTENT_TYPE);
    if (contentType != null) {
      requestBuilder.contentType(contentType);
    }

    return requestBuilder.build();
  }

  private void validateBody(final ODataHttpMethod httpStatusLine, final BatchQueryOperation operation, final int line)
      throws BatchException {
    if (HTTP_BATCH_METHODS.contains(httpStatusLine.toString()) && isUnvalidGetRequestBody(operation)) {
      throw new BatchException(BatchException.INVALID_REQUEST_LINE.addContent(httpStatusLine).addContent(line));
    }
  }

  private boolean isUnvalidGetRequestBody(final BatchQueryOperation operation) {
    return (operation.getBody().size() > 1)
        || (operation.getBody().size() == 1 && !operation.getBody().get(0).toString().trim().equals(""));
  }

  private InputStream getBodyStream(final BatchQueryOperation operation, Header headers,
      final ODataHttpMethod httpMethod) throws BatchException {

    if (HTTP_BATCH_METHODS.contains(httpMethod.toString())) {
      return new ByteArrayInputStream(new byte[0]);
    } else {
      int contentLength = BatchTransformatorCommon.getContentLength(headers);

      if (contentLength == -1) {
        return BatchParserCommon.convertMessageToInputStream(operation.getBody());
      } else {
        return BatchParserCommon.convertMessageToInputStream(operation.getBody(), contentLength);
      }
    }
  }

  private Header transformHeader(final BatchPart operation, final BatchPart parentPart) {
    final Header headers = operation.getHeaders().clone();
    headers.removeHeader(BatchHelper.HTTP_CONTENT_ID);
    final HeaderField operationHeader = operation.getHeaders().getHeaderField(BatchHelper.HTTP_CONTENT_ID);
    final HeaderField parentHeader = parentPart.getHeaders().getHeaderField(BatchHelper.HTTP_CONTENT_ID);

    if (operationHeader != null && operationHeader.getValues().size() != 0) {
      headers.addHeader(BatchHelper.REQUEST_HEADER_CONTENT_ID, operationHeader.getValues(), operationHeader
          .getLineNumber());
    }

    if (parentHeader != null && parentHeader.getValues().size() != 0) {
      headers.addHeader(BatchHelper.MIME_HEADER_CONTENT_ID, parentHeader.getValues(), parentHeader.getLineNumber());
    }

    return headers;
  }

  private void validateHttpMethod(final ODataHttpMethod httpMethod, final boolean isChangeSet, final int line)
      throws BatchException {
    Set<String> validMethods = (isChangeSet) ? HTTP_CHANGE_SET_METHODS : HTTP_BATCH_METHODS;

    if (!validMethods.contains(httpMethod.toString())) {
      if (isChangeSet) {
        throw new BatchException(BatchException.INVALID_CHANGESET_METHOD.addContent(line));
      } else {
        throw new BatchException(BatchException.INVALID_QUERY_OPERATION_METHOD.addContent(line));
      }
    }
  }

  private List<Locale> getAcceptLanguageHeaders(final Header headers) {
    final List<String> acceptLanguageValues = headers.getHeaders(HttpHeaders.ACCEPT_LANGUAGE);
    List<Locale> acceptLanguages = new ArrayList<Locale>();

    for (String acceptLanguage : acceptLanguageValues) {
      String[] part = acceptLanguage.split("-");
      String language = part[0];
      String country = "";
      if (part.length == 2) {
        country = part[part.length - 1];
      }
      Locale locale = new Locale(language, country);
      acceptLanguages.add(locale);
    }

    return acceptLanguages;
  }

  private ODataHttpMethod getHttpMethod(final Line httpRequest) throws BatchException {
    ODataHttpMethod result = null;

    String[] parts = httpRequest.toString().split(" ");

    if (parts.length == 3) {
      try {
        result = ODataHttpMethod.valueOf(parts[0]);
      } catch (IllegalArgumentException e) {
        throw new BatchException(BatchException.MISSING_METHOD.addContent(httpRequest.getLineNumber()), e);
      }
    } else {
      throw new BatchException(BatchException.INVALID_REQUEST_LINE.addContent(httpRequest.toString()).addContent(
          httpRequest.getLineNumber()));
    }

    return result;
  }

}
