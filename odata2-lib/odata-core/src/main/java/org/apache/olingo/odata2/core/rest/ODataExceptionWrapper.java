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

import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataHttpException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.exception.ODataRuntimeApplicationException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataErrorCallback;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.ep.ProviderFacadeImpl;
import org.apache.olingo.odata2.core.exception.MessageService;
import org.apache.olingo.odata2.core.exception.MessageService.Message;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *  
 */
public class ODataExceptionWrapper {

  private static final String DOLLAR_FORMAT = "$format";
  private static final String DOLLAR_FORMAT_JSON = "json";
  private static final Locale DEFAULT_RESPONSE_LOCALE = Locale.ENGLISH;

  private final String contentType;
  private final Locale messageLocale;
  private final Map<String, List<String>> httpRequestHeaders;
  private final ODataErrorCallback callback;
  private final ODataErrorContext errorContext = new ODataErrorContext();
  private final URI requestUri;

  public ODataExceptionWrapper(final ODataContext context, final Map<String, String> queryParameters,
      final List<String> acceptHeaderContentTypes) {
    contentType = getContentType(queryParameters, acceptHeaderContentTypes).toContentTypeString();
    messageLocale = MessageService.getSupportedLocale(getLanguages(context), DEFAULT_RESPONSE_LOCALE);
    httpRequestHeaders = context.getRequestHeaders();
    try {
      requestUri = context.getPathInfo().getRequestUri();
      errorContext.setPathInfo(context.getPathInfo());
      callback = getErrorHandlerCallbackFromContext(context);
    } catch (Exception e) {
      throw new ODataRuntimeException("Exception occurred", e);
    }
  }

  public ODataExceptionWrapper(final UriInfo uriInfo, final HttpHeaders httpHeaders,
      final ODataErrorCallback errorCallback) {
    contentType = getContentType(uriInfo, httpHeaders).toContentTypeString();
    messageLocale = MessageService.getSupportedLocale(getLanguages(httpHeaders), DEFAULT_RESPONSE_LOCALE);
    httpRequestHeaders = httpHeaders.getRequestHeaders();
    requestUri = uriInfo.getRequestUri();
    callback = errorCallback;
  }

  public ODataResponse wrapInExceptionResponse(final Exception exception) {
    try {
      final Exception toHandleException = extractException(exception);
      fillErrorContext(toHandleException);
      if (toHandleException instanceof ODataApplicationException) {
        enhanceContextWithApplicationException((ODataApplicationException) toHandleException);
      } else if (toHandleException instanceof ODataRuntimeApplicationException) {
        enhanceContextWithRuntimeApplicationException((ODataRuntimeApplicationException) toHandleException);
      } else if (toHandleException instanceof ODataMessageException) {
        enhanceContextWithMessageException((ODataMessageException) toHandleException);
      }

      ODataResponse oDataResponse;
      if (callback != null) {
        oDataResponse = handleErrorCallback(callback);
      } else {
        oDataResponse = EntityProvider.writeErrorDocument(errorContext);
      }
      if (!oDataResponse.containsHeader(org.apache.olingo.odata2.api.commons.HttpHeaders.CONTENT_TYPE)) {
        oDataResponse = ODataResponse.fromResponse(oDataResponse).contentHeader(contentType).build();
      }
      return oDataResponse;
    } catch (Exception e) {
      ODataResponse response = ODataResponse.entity("Exception during error handling occured!")
          .contentHeader(ContentType.TEXT_PLAIN.toContentTypeString())
          .status(HttpStatusCodes.INTERNAL_SERVER_ERROR).build();
      return response;
    }
  }

  private void enhanceContextWithRuntimeApplicationException(ODataRuntimeApplicationException toHandleException) {
    errorContext.setHttpStatus(toHandleException.getHttpStatus());
    errorContext.setErrorCode(toHandleException.getCode());
  }

  private ODataResponse handleErrorCallback(final ODataErrorCallback callback) throws EntityProviderException {
    ODataResponse oDataResponse;
    try {
      oDataResponse = callback.handleError(errorContext);
    } catch (ODataApplicationException e) {
      fillErrorContext(e);
      enhanceContextWithApplicationException(e);
      oDataResponse = new ProviderFacadeImpl().writeErrorDocument(errorContext);
    }
    return oDataResponse;
  }

  private void enhanceContextWithApplicationException(final ODataApplicationException toHandleException) {
    errorContext.setHttpStatus(toHandleException.getHttpStatus());
    errorContext.setErrorCode(toHandleException.getCode());
  }

  private void enhanceContextWithMessageException(final ODataMessageException toHandleException) {
    errorContext.setErrorCode(toHandleException.getErrorCode());
    MessageReference messageReference = toHandleException.getMessageReference();
    Message localizedMessage = messageReference == null ? null : extractEntity(messageReference);
    if (localizedMessage != null) {
      errorContext.setMessage(localizedMessage.getText());
      errorContext.setLocale(localizedMessage.getLocale());
    }
    if (toHandleException instanceof ODataHttpException) {
      errorContext.setHttpStatus(((ODataHttpException) toHandleException).getHttpStatus());
    } else if (toHandleException instanceof EntityProviderException) {
      errorContext.setHttpStatus(HttpStatusCodes.BAD_REQUEST);
    } else if (toHandleException instanceof BatchException) {
      errorContext.setHttpStatus(HttpStatusCodes.BAD_REQUEST);
    }

  }

  /**
   * Fill current error context ({@link #errorContext}) with values from given {@link Exception} parameter.
   * 
   * @param exception exception with values to be set on error context
   */
  private void fillErrorContext(final Exception exception) {
    errorContext.setContentType(contentType);
    errorContext.setException(exception);
    errorContext.setHttpStatus(HttpStatusCodes.INTERNAL_SERVER_ERROR);
    errorContext.setErrorCode(null);
    errorContext.setMessage(exception.getMessage());
    errorContext.setLocale(DEFAULT_RESPONSE_LOCALE);
    errorContext.setRequestUri(requestUri);

    if (httpRequestHeaders != null) {
      for (Entry<String, List<String>> entry : httpRequestHeaders.entrySet()) {
        errorContext.putRequestHeader(entry.getKey(), entry.getValue());
      }
    }
  }

  private Exception extractException(final Exception exception) {
    if (exception instanceof ODataException) {
      ODataException odataException = (ODataException) exception;
      if (odataException.isCausedByApplicationException()) {
        return odataException.getApplicationExceptionCause();
      } else if (odataException.isCausedByHttpException()) {
        return odataException.getHttpExceptionCause();
      } else if (odataException.isCausedByMessageException()) {
        return odataException.getMessageExceptionCause();
      }
    }
    return exception;
  }

  private Message extractEntity(final MessageReference context) {
    return MessageService.getMessage(messageLocale, context);
  }

  private List<Locale> getLanguages(final ODataContext context) {
    try {
      if (context.getAcceptableLanguages().isEmpty()) {
        return Arrays.asList(DEFAULT_RESPONSE_LOCALE);
      }
      return context.getAcceptableLanguages();
    } catch (WebApplicationException e) {
      if (e.getCause() != null && e.getCause().getClass() == ParseException.class) {
        // invalid accept-language string in http header
        // compensate exception with using default locale
        return Arrays.asList(DEFAULT_RESPONSE_LOCALE);
      }
      // not able to compensate exception -> re-throw
      throw e;
    }
  }

  private List<Locale> getLanguages(final HttpHeaders httpHeaders) {
    try {
      if (httpHeaders.getAcceptableLanguages().isEmpty()) {
        return Arrays.asList(DEFAULT_RESPONSE_LOCALE);
      }
      return httpHeaders.getAcceptableLanguages();
    } catch (WebApplicationException e) {
      if (e.getCause() != null && e.getCause().getClass() == ParseException.class) {
        // invalid accept-language string in http header
        // compensate exception with using default locale
        return Arrays.asList(DEFAULT_RESPONSE_LOCALE);
      }
      // not able to compensate exception -> re-throw
      throw e;
    }
  }

  private ContentType getContentType(final Map<String, String> queryParameters,
      final List<String> acceptHeaderContentTypes) {
    ContentType contentType = getContentTypeByUriInfo(queryParameters);
    if (contentType == null) {
      contentType = getContentTypeByAcceptHeader(acceptHeaderContentTypes);
    }
    return contentType;
  }

  private ContentType getContentTypeByUriInfo(final Map<String, String> queryParameters) {
    ContentType contentType = null;
    if (queryParameters != null) {
      if (queryParameters.containsKey(DOLLAR_FORMAT)) {
        String contentTypeString = queryParameters.get(DOLLAR_FORMAT);
        if (DOLLAR_FORMAT_JSON.equals(contentTypeString)) {
          contentType = ContentType.APPLICATION_JSON;
        } else {
          // Any format mentioned in the $format parameter other than json results in an application/xml content type
          // for error messages
          // due to the OData V2 Specification
          contentType = ContentType.APPLICATION_XML;
        }
      }
    }
    return contentType;
  }

  private ContentType getContentTypeByAcceptHeader(final List<String> acceptHeaderContentTypes) {
    for (String acceptContentType : acceptHeaderContentTypes) {
      if (ContentType.isParseable(acceptContentType)) {
        ContentType convertedContentType = ContentType.create(acceptContentType);
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

  private ContentType getContentType(final UriInfo uriInfo, final HttpHeaders httpHeaders) {
    ContentType contentType = getContentTypeByUriInfo(uriInfo);
    if (contentType == null) {
      contentType = getContentTypeByAcceptHeader(httpHeaders);
    }
    return contentType;
  }

  private ContentType getContentTypeByUriInfo(final UriInfo uriInfo) {
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

  private ContentType getContentTypeByAcceptHeader(final HttpHeaders httpHeaders) {
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

  private ODataErrorCallback getErrorHandlerCallbackFromContext(final ODataContext context)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    ODataErrorCallback callback = null;
    ODataServiceFactory serviceFactory = context.getServiceFactory();
    callback = serviceFactory.getCallback(ODataErrorCallback.class);
    return callback;
  }

}
