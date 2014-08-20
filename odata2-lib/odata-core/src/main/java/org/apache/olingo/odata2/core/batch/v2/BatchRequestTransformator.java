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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchParserResult;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataRequest.ODataRequestBuilder;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.core.batch.BatchHelper;
import org.apache.olingo.odata2.core.batch.BatchRequestPartImpl;
import org.apache.olingo.odata2.core.batch.v2.BatchParserCommon.HeaderField;

public class BatchRequestTransformator implements BatchTransformator {

  private static final Set<String> HTTP_BATCH_METHODS = new HashSet<String>(Arrays.asList(new String[] { "GET" }));
  private static final Set<String> HTTP_CHANGE_SET_METHODS = new HashSet<String>(Arrays.asList(new String[] { "POST",
      "PUT", "DELETE", "MERGE", "PATCH" }));

  @Override
  public List<BatchParserResult> transform(final BatchBodyPart bodyPart, final PathInfo pathInfo, final String baseUri)
      throws BatchException {

    final List<ODataRequest> requests = new LinkedList<ODataRequest>();
    final List<BatchParserResult> resultList = new ArrayList<BatchParserResult>();

    BatchTransformatorCommon.parsePartSyntax(bodyPart);
    validateBodyPartHeaders(bodyPart);

    for (BatchQueryOperation queryOperation : bodyPart.getRequests()) {
      requests.add(processQueryOperation(bodyPart, pathInfo, baseUri, queryOperation));
    }

    resultList.add(new BatchRequestPartImpl(bodyPart.isChangeSet(), requests));
    return resultList;
  }

  private void validateBodyPartHeaders(final BatchBodyPart bodyPart) throws BatchException {
    Map<String, HeaderField> headers = bodyPart.getHeaders();

    BatchTransformatorCommon.validateContentType(headers);
    BatchTransformatorCommon.validateContentTransferEncoding(headers, false);
  }

  private ODataRequest processQueryOperation(final BatchBodyPart bodyPart, final PathInfo pathInfo,
      final String baseUri, final BatchQueryOperation queryOperation) throws BatchException {

    if (bodyPart.isChangeSet()) {
      BatchQueryOperation encapsulatedQueryOperation = ((BatchChangeSet) queryOperation).getRequest();
      Map<String, HeaderField> headers = transformHeader(encapsulatedQueryOperation, queryOperation);
      validateChangeSetMultipartMimeHeaders(queryOperation, encapsulatedQueryOperation);

      return createRequest(queryOperation, headers, pathInfo, baseUri, bodyPart.isChangeSet());
    } else {

      Map<String, HeaderField> headers = transformHeader(queryOperation, bodyPart);
      return createRequest(queryOperation, headers, pathInfo, baseUri, bodyPart.isChangeSet());
    }
  }

  private void validateChangeSetMultipartMimeHeaders(final BatchQueryOperation queryOperation,
      final BatchQueryOperation encapsulatedQueryOperation) throws BatchException {
    BatchTransformatorCommon.validateContentType(queryOperation.getHeaders());
    BatchTransformatorCommon.validateContentTransferEncoding(queryOperation.getHeaders(), true);
  }

  private ODataRequest createRequest(final BatchQueryOperation operation, final Map<String, HeaderField> headers,
      final PathInfo pathInfo, final String baseUri, final boolean isChangeSet) throws BatchException {

    ODataHttpMethod httpMethod = getHttpMethod(operation.getHttpMethod());
    validateHttpMethod(httpMethod, isChangeSet);
    validateBody(httpMethod, operation);
    InputStream bodyStrean = getBodyStream(operation, headers, httpMethod);

    ODataRequestBuilder requestBuilder = ODataRequest.method(httpMethod)
        .acceptableLanguages(getAcceptLanguageHeaders(headers))
        .acceptHeaders(getAcceptHeaders(headers))
        .allQueryParameters(BatchParserCommon.parseQueryParameter(operation.getHttpMethod()))
        .body(bodyStrean)
        .requestHeaders(BatchParserCommon.headerFieldMapToMultiMap(headers))
        .pathInfo(BatchParserCommon.parseRequestUri(operation.getHttpMethod(), pathInfo, baseUri));

    addContentTypeHeader(requestBuilder, headers);

    return requestBuilder.build();
  }

  private void validateBody(final ODataHttpMethod httpMethod, final BatchQueryOperation operation)
      throws BatchException {
    if (HTTP_BATCH_METHODS.contains(httpMethod.toString()) && isUnvalidGetRequestBody(operation)) {
      throw new BatchException(BatchException.INVALID_REQUEST_LINE);
    }
  }

  private boolean isUnvalidGetRequestBody(final BatchQueryOperation operation) {
    return (operation.getBody().size() > 1)
        || (operation.getBody().size() == 1 && !operation.getBody().get(0).trim().equals(""));
  }

  private InputStream getBodyStream(final BatchQueryOperation operation, final Map<String, HeaderField> headers,
      final ODataHttpMethod httpMethod) throws BatchException {

    if (HTTP_BATCH_METHODS.contains(httpMethod.toString())) {
      return new ByteArrayInputStream(new byte[0]);
    } else {
      int contentLength = BatchTransformatorCommon.getContentLength(headers);
      contentLength = (contentLength >= 0) ? contentLength : Integer.MAX_VALUE;

      return BatchParserCommon.convertMessageToInputStream(operation.getBody(), contentLength);
    }
  }

  private Map<String, HeaderField> transformHeader(final BatchPart operation, final BatchPart parentPart) {
    final Map<String, HeaderField> headers = new HashMap<String, HeaderField>();
    final Map<String, HeaderField> operationHeader = operation.getHeaders();
    final Map<String, HeaderField> parentHeaders = parentPart.getHeaders();

    for (final String key : operation.getHeaders().keySet()) {
      headers.put(key, operation.getHeaders().get(key).clone());
    }

    headers.remove(BatchHelper.HTTP_CONTENT_ID.toLowerCase(Locale.ENGLISH));

    if (operationHeader.containsKey(BatchHelper.HTTP_CONTENT_ID.toLowerCase(Locale.ENGLISH))) {
      HeaderField operationContentField = operationHeader.get(BatchHelper.HTTP_CONTENT_ID.toLowerCase());
      headers.put(BatchHelper.REQUEST_HEADER_CONTENT_ID.toLowerCase(Locale.ENGLISH), new HeaderField(
          BatchHelper.REQUEST_HEADER_CONTENT_ID, operationContentField.getValues()));
    }

    if (parentHeaders.containsKey(BatchHelper.HTTP_CONTENT_ID.toLowerCase(Locale.ENGLISH))) {
      HeaderField parentContentField = parentHeaders.get(BatchHelper.HTTP_CONTENT_ID.toLowerCase());
      headers.put(BatchHelper.MIME_HEADER_CONTENT_ID.toLowerCase(Locale.ENGLISH), new HeaderField(
          BatchHelper.MIME_HEADER_CONTENT_ID, parentContentField.getValues()));
    }

    return headers;
  }

  private void validateHttpMethod(final ODataHttpMethod httpMethod, final boolean isChangeSet) throws BatchException {
    Set<String> validMethods = (isChangeSet) ? HTTP_CHANGE_SET_METHODS : HTTP_BATCH_METHODS;

    if (!validMethods.contains(httpMethod.toString())) {
      MessageReference message =
          (isChangeSet) ? BatchException.INVALID_CHANGESET_METHOD : BatchException.INVALID_QUERY_OPERATION_METHOD;
      throw new BatchException(message);
    }
  }

  private void addContentTypeHeader(final ODataRequestBuilder requestBuilder, final Map<String, HeaderField> header) {
    String contentType = getContentTypeHeader(header);

    if (contentType != null) {
      requestBuilder.contentType(contentType);
    }
  }

  private String getContentTypeHeader(final Map<String, HeaderField> headers) {
    HeaderField contentTypeField = headers.get(HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.ENGLISH));
    String contentType = null;
    if (contentTypeField != null) {
      for (String requestContentType : contentTypeField.getValues()) {
        contentType = contentType != null ? contentType + "," + requestContentType : requestContentType;
      }
    }

    return contentType;
  }

  private List<String> getAcceptHeaders(final Map<String, HeaderField> headers) {
    List<String> acceptHeaders = new ArrayList<String>();
    HeaderField requestAcceptHeaderField = headers.get(HttpHeaders.ACCEPT.toLowerCase(Locale.ENGLISH));

    if (requestAcceptHeaderField != null) {
      acceptHeaders = requestAcceptHeaderField.getValues();
    }

    return acceptHeaders;
  }

  private List<Locale> getAcceptLanguageHeaders(final Map<String, HeaderField> headers) {
    final HeaderField requestAcceptLanguageField = headers.get(HttpHeaders.ACCEPT_LANGUAGE.toLowerCase(Locale.ENGLISH));
    List<Locale> acceptLanguages = new ArrayList<Locale>();

    if (requestAcceptLanguageField != null) {
      for (String acceptLanguage : requestAcceptLanguageField.getValues()) {
        String[] part = acceptLanguage.split("-");
        String language = part[0];
        String country = "";
        if (part.length == 2) {
          country = part[part.length - 1];
        }
        Locale locale = new Locale(language, country);
        acceptLanguages.add(locale);
      }
    }

    return acceptLanguages;
  }

  private ODataHttpMethod getHttpMethod(final String httpRequest) throws BatchException {
    ODataHttpMethod result = null;

    if (httpRequest != null) {
      String[] parts = httpRequest.split(" ");

      if (parts.length == 3) {
        try {
          result = ODataHttpMethod.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
          throw new BatchException(BatchException.MISSING_METHOD, e);
        }
      } else {
        throw new BatchException(BatchException.INVALID_REQUEST_LINE);
      }
    } else {
      throw new BatchException(BatchException.INVALID_REQUEST_LINE);
    }

    return result;
  }

}
