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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchParserResult;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataRequest.ODataRequestBuilder;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.core.batch.BatchHelper;
import org.apache.olingo.odata2.core.batch.BatchRequestPartImpl;
import org.apache.olingo.odata2.core.batch.v2.BatchTransformatorCommon.HttpRequestStatusLine;
import org.apache.olingo.odata2.core.batch.v2.Header.HeaderField;

public class BatchRequestTransformator implements BatchTransformator {

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

  private void validateHeader(final BatchPart bodyPart, final boolean isChangeSet) throws BatchException {
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

    final HttpRequestStatusLine statusLine = new HttpRequestStatusLine( operation.getHttpStatusLine(), 
                                                                        baseUri, 
                                                                        pathInfo);
    statusLine.validateHttpMethod(isChangeSet);
    BatchTransformatorCommon.validateHost(headers, baseUri);

    validateBody(statusLine, operation);
    InputStream bodyStream = getBodyStream(operation, headers, statusLine);

    ODataRequestBuilder requestBuilder = ODataRequest.method(statusLine.getMethod())
        .acceptableLanguages(getAcceptLanguageHeaders(headers))
        .acceptHeaders(headers.getHeaders(HttpHeaders.ACCEPT))
        .allQueryParameters(BatchParserCommon.parseQueryParameter(operation.getHttpStatusLine()))
        .body(bodyStream)
        .requestHeaders(headers.toMultiMap())
        .pathInfo(statusLine.getPathInfo());

    final String contentType = headers.getHeader(HttpHeaders.CONTENT_TYPE);
    if (contentType != null) {
      requestBuilder.contentType(contentType);
    }

    return requestBuilder.build();
  }

  private void validateBody(final HttpRequestStatusLine httpStatusLine, final BatchQueryOperation operation)
      throws BatchException {
    if (httpStatusLine.getMethod().equals(ODataHttpMethod.GET) && isUnvalidGetRequestBody(operation)) {
      throw new BatchException(BatchException.INVALID_BODY_FOR_REQUEST
          .addContent(httpStatusLine.getLineNumber()));
    }
  }

  private boolean isUnvalidGetRequestBody(final BatchQueryOperation operation) {
    return (operation.getBody().size() > 1)
        || (operation.getBody().size() == 1 && !"".equals(operation.getBody().get(0).toString().trim()));
  }

  private InputStream getBodyStream(final BatchQueryOperation operation, final Header headers,
      final HttpRequestStatusLine httpStatusLine) throws BatchException {

    if (httpStatusLine.getMethod().equals(ODataHttpMethod.GET)) {
      return new ByteArrayInputStream(new byte[0]);
    } else {
      int contentLength = BatchTransformatorCommon.getContentLength(headers);
      String contentType = headers.getHeader(HttpHeaders.CONTENT_TYPE);
      return BatchParserCommon.convertToInputStream(contentType, operation, contentLength);
    }
  }

  private Header transformHeader(final BatchPart operation, final BatchPart parentPart) {
    final Header headers = operation.getHeaders().clone();
    headers.removeHeader(BatchHelper.HTTP_CONTENT_ID);
    final HeaderField operationHeader = operation.getHeaders().getHeaderField(BatchHelper.HTTP_CONTENT_ID);
    final HeaderField parentHeader = parentPart.getHeaders().getHeaderField(BatchHelper.HTTP_CONTENT_ID);

    if (operationHeader != null && !operationHeader.getValues().isEmpty()) {
      headers.addHeader(BatchHelper.REQUEST_HEADER_CONTENT_ID, operationHeader.getValues(), operationHeader
          .getLineNumber());
    }

    if (parentHeader != null && !parentHeader.getValues().isEmpty()) {
      headers.addHeader(BatchHelper.MIME_HEADER_CONTENT_ID, parentHeader.getValues(), parentHeader.getLineNumber());
    }

    return headers;
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

}
