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
package org.apache.olingo.odata2.core.rest;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.processor.ODataErrorCallback;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.ep.ProviderFacadeImpl;

/**
 * Creates an error response according to the format defined by the OData standard
 * if an exception occurs that is not handled elsewhere.
 * 
 */
@Provider
public class ODataExceptionMapperImpl implements ExceptionMapper<Exception> {

  private static final String DOLLAR_FORMAT = "$format";
  private static final String DOLLAR_FORMAT_JSON = "json";

  private static final Locale DEFAULT_RESPONSE_LOCALE = Locale.ENGLISH;

  @Context
  UriInfo uriInfo;
  @Context
  HttpHeaders httpHeaders;
  @Context
  ServletConfig servletConfig;
  @Context
  HttpServletRequest servletRequest;
  @Context
  Application app;

  @Override
  public Response toResponse(final Exception exception) {
    ODataResponse response;
    try {
      if (exception instanceof WebApplicationException) {
        response = handleWebApplicationException(exception);
      } else {
        response = handleException(exception);
      }
    } catch (Exception e) {
      response = ODataResponse.entity("Exception during error handling occured!")
          .contentHeader(ContentType.TEXT_PLAIN.toContentTypeString())
          .status(HttpStatusCodes.INTERNAL_SERVER_ERROR).build();
    }
    // Convert OData response to JAX-RS response.
    return RestUtil.convertResponse(response);
  }

  private ODataResponse handleException(final Exception exception) {

    ODataExceptionWrapper exceptionWrapper =
        new ODataExceptionWrapper(uriInfo, httpHeaders, getErrorHandlerCallback());
    return exceptionWrapper.wrapInExceptionResponse(exception);
  }

  private ODataResponse handleWebApplicationException(final Exception exception) throws ClassNotFoundException,
      InstantiationException, IllegalAccessException, EntityProviderException {
    ODataErrorContext errorContext = createErrorContext((WebApplicationException) exception);
    ODataErrorCallback callback = getErrorHandlerCallback();
    return callback == null ?
        new ProviderFacadeImpl().writeErrorDocument(errorContext) : executeErrorCallback(errorContext, callback);
  }

  private ODataResponse executeErrorCallback(final ODataErrorContext errorContext, final ODataErrorCallback callback) {
    ODataResponse oDataResponse;
    try {
      oDataResponse = callback.handleError(errorContext);
    } catch (ODataApplicationException e) {
      oDataResponse = handleException(e);
    }
    return oDataResponse;
  }

  private ODataErrorContext createErrorContext(final WebApplicationException exception) {
    ODataErrorContext context = new ODataErrorContext();
    if (uriInfo != null) {
      context.setRequestUri(uriInfo.getRequestUri());
    }
    if (httpHeaders != null && httpHeaders.getRequestHeaders() != null) {
      MultivaluedMap<String, String> requestHeaders = httpHeaders.getRequestHeaders();
      Set<Entry<String, List<String>>> entries = requestHeaders.entrySet();
      for (Entry<String, List<String>> entry : entries) {
        context.putRequestHeader(entry.getKey(), entry.getValue());
      }
    }
    context.setContentType(getContentType().toContentTypeString());
    context.setException(exception);
    context.setErrorCode(null);
    context.setMessage(exception.getMessage());
    context.setLocale(DEFAULT_RESPONSE_LOCALE);
    HttpStatusCodes statusCode = HttpStatusCodes.fromStatusCode(exception.getResponse().getStatus());
    context.setHttpStatus(statusCode);
    if (statusCode == HttpStatusCodes.METHOD_NOT_ALLOWED) {
      // RFC 2616, 5.1.1: " An origin server SHOULD return the status code
      // 405 (Method Not Allowed) if the method is known by the origin server
      // but not allowed for the requested resource, and 501 (Not Implemented)
      // if the method is unrecognized or not implemented by the origin server."
      // Since all recognized methods are handled elsewhere, we unconditionally
      // switch to 501 here for not-allowed exceptions thrown directly from
      // JAX-RS implementations.
      context.setHttpStatus(HttpStatusCodes.NOT_IMPLEMENTED);
      context.setMessage("The request dispatcher does not allow the HTTP method used for the request.");
      context.setLocale(Locale.ENGLISH);
    }
    return context;
  }

  private ContentType getContentType() {
    ContentType contentType = getContentTypeByUriInfo();
    if (contentType == null) {
      contentType = getContentTypeByAcceptHeader();
    }
    return contentType;
  }

  private ContentType getContentTypeByUriInfo() {
    ContentType contentType = null;
    if (uriInfo != null && uriInfo.getQueryParameters() != null) {
      MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
      if (queryParameters.containsKey(DOLLAR_FORMAT)) {
        String contentTypeString = queryParameters.getFirst(DOLLAR_FORMAT);
        if (DOLLAR_FORMAT_JSON.equals(contentTypeString)) {
          contentType = ContentType.APPLICATION_JSON;
        } else {
          // Any format mentioned in the $format parameter other than json results in an application/xml content type
          // for error messages due to the OData V2 Specification.
          contentType = ContentType.APPLICATION_XML;
        }
      }
    }
    return contentType;
  }

  private ContentType getContentTypeByAcceptHeader() {
    for (MediaType type : httpHeaders.getAcceptableMediaTypes()) {
      if (ContentType.isParseable(type.toString())) {
        ContentType convertedContentType = ContentType.create(type.toString());
        if (convertedContentType.isWildcard()
            || ContentType.APPLICATION_XML.equals(convertedContentType)
            || ContentType.APPLICATION_XML_CS_UTF_8.equals(convertedContentType)
            || ContentType.APPLICATION_ATOM_XML.equals(convertedContentType)
            || ContentType.APPLICATION_ATOM_XML_CS_UTF_8.equals(convertedContentType)) {
          return ContentType.APPLICATION_XML;
        } else if (ContentType.APPLICATION_JSON.equals(convertedContentType)
            || ContentType.APPLICATION_JSON_CS_UTF_8.equals(convertedContentType)) {
          return ContentType.APPLICATION_JSON;
        }
      }
    }
    return ContentType.APPLICATION_XML;
  }

  private ODataErrorCallback getErrorHandlerCallback() {
    final ODataServiceFactory serviceFactory =
        ODataRootLocator.createServiceFactoryFromContext(app, servletRequest, servletConfig);
    return serviceFactory.getCallback(ODataErrorCallback.class);
  }
}
