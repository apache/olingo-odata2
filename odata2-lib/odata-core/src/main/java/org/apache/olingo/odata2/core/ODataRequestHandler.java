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
package org.apache.olingo.odata2.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataDebugResponseWrapperCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.ODataServiceVersion;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.ODataHttpHeaders;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.edm.EdmConcurrencyMode;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataMethodNotAllowedException;
import org.apache.olingo.odata2.api.exception.ODataPreconditionRequiredException;
import org.apache.olingo.odata2.api.exception.ODataUnsupportedMediaTypeException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataResponse.ODataResponseBuilder;
import org.apache.olingo.odata2.api.processor.part.EntityLinkProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityLinksProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityMediaProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySetProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySimplePropertyValueProcessor;
import org.apache.olingo.odata2.api.processor.part.FunctionImportProcessor;
import org.apache.olingo.odata2.api.processor.part.FunctionImportValueProcessor;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.ContentType.ODataFormat;
import org.apache.olingo.odata2.core.debug.ODataDebugResponseWrapper;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;
import org.apache.olingo.odata2.core.rest.ODataExceptionWrapper;
import org.apache.olingo.odata2.core.uri.UriInfoImpl;
import org.apache.olingo.odata2.core.uri.UriParserImpl;
import org.apache.olingo.odata2.core.uri.UriType;

/**
 *  
 */
public class ODataRequestHandler {

  private final ODataServiceFactory serviceFactory;
  private final ODataService service;
  private final ODataContext context;

  public ODataRequestHandler(final ODataServiceFactory factory, final ODataService service,
      final ODataContext context) {
    serviceFactory = factory;
    this.service = service;
    this.context = context;
  }

  /**
   * <p>Handles the {@link ODataRequest} in a way that it results in a corresponding {@link ODataResponse}.</p>
   * <p>This includes delegation of URI parsing and dispatching of the request internally.
   * Building of the {@link ODataContext} takes place outside of this method.</p>
   * @param request the incoming request
   * @return the corresponding result
   */
  public ODataResponse handle(final ODataRequest request) {
    UriInfoImpl uriInfo = null;
    Exception exception = null;
    ODataResponse odataResponse;
    final int timingHandle = context.startRuntimeMeasurement("ODataRequestHandler", "handle");
    try {
      UriParser uriParser = new UriParserImpl(service.getEntityDataModel());
      Dispatcher dispatcher = new Dispatcher(serviceFactory, service);

      final String serverDataServiceVersion = getServerDataServiceVersion();
      final String requestDataServiceVersion = context.getRequestHeader(ODataHttpHeaders.DATASERVICEVERSION);
      validateDataServiceVersion(serverDataServiceVersion, requestDataServiceVersion);

      final List<PathSegment> pathSegments = context.getPathInfo().getODataSegments();
      int timingHandle2 = context.startRuntimeMeasurement("UriParserImpl", "parse");
      uriInfo = (UriInfoImpl) uriParser.parseAll(pathSegments, request.getAllQueryParameters());
      context.stopRuntimeMeasurement(timingHandle2);

      final ODataHttpMethod method = request.getMethod();
      validateMethodAndUri(method, uriInfo);

      if (method == ODataHttpMethod.POST || method == ODataHttpMethod.PUT || method == ODataHttpMethod.PATCH
          || method == ODataHttpMethod.MERGE) {
        checkRequestContentType(uriInfo, request.getContentType());
      }

      List<String> supportedContentTypes = getSupportedContentTypes(uriInfo, method);
      ContentType acceptContentType =
          new ContentNegotiator().doContentNegotiation(request, uriInfo, supportedContentTypes);

      checkConditions(method, uriInfo,
          context.getRequestHeader(HttpHeaders.IF_MATCH),
          context.getRequestHeader(HttpHeaders.IF_NONE_MATCH),
          context.getRequestHeader(HttpHeaders.IF_MODIFIED_SINCE),
          context.getRequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE));

      timingHandle2 = context.startRuntimeMeasurement("Dispatcher", "dispatch");
      odataResponse =
          dispatcher.dispatch(method, uriInfo, request.getBody(), request.getContentType(), acceptContentType
              .toContentTypeString());
      context.stopRuntimeMeasurement(timingHandle2);

      ODataResponseBuilder extendedResponse = ODataResponse.fromResponse(odataResponse);
      final UriType uriType = uriInfo.getUriType();
      final String location =
          (method == ODataHttpMethod.POST && (uriType == UriType.URI1 || uriType == UriType.URI6B)) ? odataResponse
              .getIdLiteral() : null;
      final HttpStatusCodes s = getStatusCode(odataResponse, method, uriType);
      extendedResponse = extendedResponse.idLiteral(location).status(s);

      if (!odataResponse.containsHeader(ODataHttpHeaders.DATASERVICEVERSION)) {
        extendedResponse = extendedResponse.header(ODataHttpHeaders.DATASERVICEVERSION, serverDataServiceVersion);
      }
      if (!HttpStatusCodes.NO_CONTENT.equals(s) && !odataResponse.containsHeader(HttpHeaders.CONTENT_TYPE)) {
        extendedResponse.header(HttpHeaders.CONTENT_TYPE, acceptContentType.toContentTypeString());
      }

      odataResponse = extendedResponse.build();
    } catch (final Exception e) {
      exception = e;
      odataResponse = new ODataExceptionWrapper(context, request.getQueryParameters(), request.getAcceptHeaders())
          .wrapInExceptionResponse(e);
    }
    context.stopRuntimeMeasurement(timingHandle);

    if (context.isInDebugMode()) {
      final String debugValue = getQueryDebugValue(request.getQueryParameters());
      if (debugValue == null) {
        ODataDebugResponseWrapperCallback callback =
            context.getServiceFactory().getCallback(ODataDebugResponseWrapperCallback.class);
        return callback == null ? odataResponse : callback.handle(context, request, odataResponse, uriInfo, exception);
      } else {
        return new ODataDebugResponseWrapper(context, odataResponse, uriInfo, exception, debugValue).wrapResponse();
      }
    } else {
      return odataResponse;
    }
  }

  private HttpStatusCodes getStatusCode(final ODataResponse odataResponse, final ODataHttpMethod method,
      final UriType uriType) {
    if (odataResponse.getStatus() == null) {
      if (method == ODataHttpMethod.POST) {
        if (uriType == UriType.URI9) {
          return HttpStatusCodes.OK;
        } else if (uriType == UriType.URI7B) {
          return HttpStatusCodes.NO_CONTENT;
        }
        return HttpStatusCodes.CREATED;
      } else if (method == ODataHttpMethod.PUT
          || method == ODataHttpMethod.PATCH
          || method == ODataHttpMethod.MERGE
          || method == ODataHttpMethod.DELETE) {
        return HttpStatusCodes.NO_CONTENT;
      }
      return HttpStatusCodes.OK;
    }
    return odataResponse.getStatus();
  }

  private String getServerDataServiceVersion() throws ODataException {
    return service.getVersion() == null ? ODataServiceVersion.V20 : service.getVersion();
  }

  private static void validateDataServiceVersion(final String serverDataServiceVersion,
      final String requestDataServiceVersion) throws ODataException {
    if (requestDataServiceVersion != null) {
      try {
        final boolean isValid = ODataServiceVersion.validateDataServiceVersion(requestDataServiceVersion);
        if (!isValid || ODataServiceVersion.isBiggerThan(requestDataServiceVersion, serverDataServiceVersion)) {
          throw new ODataBadRequestException(ODataBadRequestException.VERSIONERROR
              .addContent(requestDataServiceVersion));
        }
      } catch (final IllegalArgumentException e) {
        throw new ODataBadRequestException(ODataBadRequestException.PARSEVERSIONERROR
            .addContent(requestDataServiceVersion), e);
      }
    }
  }

  private static void validateMethodAndUri(final ODataHttpMethod method, final UriInfoImpl uriInfo)
      throws ODataException {
    validateUriMethod(method, uriInfo);
    checkFunctionImport(method, uriInfo);
    if (method != ODataHttpMethod.GET) {
      checkNotGetSystemQueryOptions(method, uriInfo);
      checkNumberOfNavigationSegments(uriInfo);
      checkProperty(method, uriInfo);
    }
  }

  private static void validateUriMethod(final ODataHttpMethod method, final UriInfoImpl uriInfo) throws ODataException {
    switch (uriInfo.getUriType()) {
    case URI0:
    case URI8:
    case URI15:
    case URI16:
    case URI50A:
    case URI50B:
      if (method != ODataHttpMethod.GET) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      break;

    case URI1:
    case URI6B:
    case URI7B:
      if (method != ODataHttpMethod.GET && method != ODataHttpMethod.POST) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      break;

    case URI2:
    case URI6A:
    case URI7A:
      if (method != ODataHttpMethod.GET && method != ODataHttpMethod.PUT && method != ODataHttpMethod.DELETE
          && method != ODataHttpMethod.PATCH && method != ODataHttpMethod.MERGE) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      break;

    case URI3:
      if (method != ODataHttpMethod.GET && method != ODataHttpMethod.PUT && method != ODataHttpMethod.PATCH
          && method != ODataHttpMethod.MERGE) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      break;

    case URI4:
    case URI5:
      if (method != ODataHttpMethod.GET && method != ODataHttpMethod.PUT && method != ODataHttpMethod.DELETE
          && method != ODataHttpMethod.PATCH && method != ODataHttpMethod.MERGE) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      } else if (method == ODataHttpMethod.DELETE && !uriInfo.isValue()) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      break;

    case URI9:
      if (method != ODataHttpMethod.POST) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      break;

    case URI10:
    case URI11:
    case URI12:
    case URI13:
    case URI14:
      break;

    case URI17:
      if (method != ODataHttpMethod.GET && method != ODataHttpMethod.PUT && method != ODataHttpMethod.DELETE) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      } else {
        if (uriInfo.getFormat() != null) {
          throw new ODataBadRequestException(ODataBadRequestException.INVALID_SYNTAX);
        }
      }
      break;

    default:
      throw new ODataRuntimeException("Unknown or not implemented URI type: " + uriInfo.getUriType());
    }
  }

  private static void checkFunctionImport(final ODataHttpMethod method, final UriInfoImpl uriInfo)
      throws ODataException {
    if (uriInfo.getFunctionImport() != null && uriInfo.getFunctionImport().getHttpMethod() != null
        && !uriInfo.getFunctionImport().getHttpMethod().equals(method.toString())) {
      throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
    }
  }

  private static void checkNotGetSystemQueryOptions(final ODataHttpMethod method, final UriInfoImpl uriInfo)
      throws ODataException {
    switch (uriInfo.getUriType()) {
    case URI1:
    case URI6B:
      if (uriInfo.getFormat() != null || uriInfo.getFilter() != null || uriInfo.getInlineCount() != null
          || uriInfo.getOrderBy() != null || uriInfo.getSkipToken() != null || uriInfo.getSkip() != null
          || uriInfo.getTop() != null || !uriInfo.getExpand().isEmpty() || !uriInfo.getSelect().isEmpty()) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      break;

    case URI2:
      if (uriInfo.getFormat() != null || !uriInfo.getExpand().isEmpty() || !uriInfo.getSelect().isEmpty()) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      if (method == ODataHttpMethod.DELETE) {
        if (uriInfo.getFilter() != null) {
          throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
        }
      }
      break;

    case URI3:
      if (uriInfo.getFormat() != null) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      break;

    case URI4:
    case URI5:
      if (method == ODataHttpMethod.PUT || method == ODataHttpMethod.PATCH || method == ODataHttpMethod.MERGE) {
        if (!uriInfo.isValue() && uriInfo.getFormat() != null) {
          throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
        }
      }
      break;

    case URI7A:
      if (uriInfo.getFormat() != null || uriInfo.getFilter() != null) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      break;

    case URI7B:
      if (uriInfo.getFormat() != null || uriInfo.getFilter() != null || uriInfo.getInlineCount() != null
          || uriInfo.getOrderBy() != null || uriInfo.getSkipToken() != null || uriInfo.getSkip() != null
          || uriInfo.getTop() != null) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      break;

    case URI17:
      if (uriInfo.getFormat() != null || uriInfo.getFilter() != null) {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }
      break;

    default:
      break;
    }
  }

  private static void checkNumberOfNavigationSegments(final UriInfoImpl uriInfo) throws ODataException {
    switch (uriInfo.getUriType()) {
    case URI1:
    case URI6B:
    case URI7A:
    case URI7B:
      if (uriInfo.getNavigationSegments().size() > 1) {
        throw new ODataBadRequestException(ODataBadRequestException.NOTSUPPORTED);
      }
      break;

    case URI3:
    case URI4:
    case URI5:
    case URI17:
      if (!uriInfo.getNavigationSegments().isEmpty()) {
        throw new ODataBadRequestException(ODataBadRequestException.NOTSUPPORTED);
      }
      break;

    default:
      break;
    }
  }

  private static void checkProperty(final ODataHttpMethod method, final UriInfoImpl uriInfo) throws ODataException {
    if ((uriInfo.getUriType() == UriType.URI4 || uriInfo.getUriType() == UriType.URI5)
        && (isPropertyKey(uriInfo) || method == ODataHttpMethod.DELETE && !isPropertyNullable(getProperty(uriInfo)))) {
      throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
    }
  }

  private static EdmProperty getProperty(final UriInfo uriInfo) {
    final List<EdmProperty> propertyPath = uriInfo.getPropertyPath();
    return propertyPath == null || propertyPath.isEmpty() ? null : propertyPath.get(propertyPath.size() - 1);
  }

  private static boolean isPropertyKey(final UriInfo uriInfo) throws EdmException {
    return uriInfo.getTargetEntitySet().getEntityType().getKeyProperties().contains(getProperty(uriInfo));
  }

  private static boolean isPropertyNullable(final EdmProperty property) throws EdmException {
    return property != null && (property.getFacets() == null || property.getFacets().isNullable());
  }

  /**
   * <p>Checks if <code>content type</code> is a valid request content type for the given {@link UriInfoImpl}.</p>
   * <p>If the combination of <code>content type</code> and {@link UriInfoImpl} is not valid, an
   * {@link ODataUnsupportedMediaTypeException} is thrown.</p>
   * @param uriInfo information about request URI
   * @param contentType request content type
   * @throws ODataException in the case of an error during {@link UriInfoImpl} access;
   * if the combination of <code>content type</code> and {@link UriInfoImpl} is invalid, as
   * {@link ODataUnsupportedMediaTypeException}
   */
  private void checkRequestContentType(final UriInfoImpl uriInfo, final String contentType) throws ODataException {
    Class<? extends ODataProcessor> processorFeature = Dispatcher.mapUriTypeToProcessorFeature(uriInfo);

    // Don't check the request content type for function imports
    // because the request body is not used at all.
    if (processorFeature == FunctionImportProcessor.class || processorFeature == FunctionImportValueProcessor.class) {
      return;
    }

    // Adjust processor feature.
    if (processorFeature == EntitySetProcessor.class) {
      processorFeature = uriInfo.getTargetEntitySet().getEntityType().hasStream() ? EntityMediaProcessor.class :
          EntityProcessor.class; // The request must contain a single entity!
    } else if (processorFeature == EntityLinksProcessor.class) {
      processorFeature = EntityLinkProcessor.class; // The request must contain a single link!
    }

    final ContentType parsedContentType = ContentType.parse(contentType);
    if (parsedContentType == null || parsedContentType.hasWildcard()) {
      throw new ODataUnsupportedMediaTypeException(ODataUnsupportedMediaTypeException.NOT_SUPPORTED
          .addContent(parsedContentType));
    }

    // Get list of supported content types based on processor feature.
    final List<ContentType> supportedContentTypes =
        processorFeature == EntitySimplePropertyValueProcessor.class ? getSupportedContentTypes(getProperty(uriInfo))
            : getSupportedContentTypes(processorFeature);

    if (!hasMatchingContentType(parsedContentType, supportedContentTypes)) {
      throw new ODataUnsupportedMediaTypeException(ODataUnsupportedMediaTypeException.NOT_SUPPORTED
          .addContent(parsedContentType));
    }
  }

  /**
   * Checks if the given list of {@link ContentType}s contains a matching {@link ContentType} for the given
   * <code>contentType</code> parameter.
   * @param contentType for which a matching content type is searched
   * @param allowedContentTypes list against which is checked for possible matching {@link ContentType}s
   * @return <code>true</code> if a matching content type is in given list, otherwise <code>false</code>
   */
  private static boolean hasMatchingContentType(final ContentType contentType,
      final List<ContentType> allowedContentTypes) {
    final ContentType requested = contentType.receiveWithCharsetParameter(ContentNegotiator.DEFAULT_CHARSET);
    if (requested.getODataFormat() == ODataFormat.CUSTOM || requested.getODataFormat() == ODataFormat.MIME) {
      return requested.hasCompatible(allowedContentTypes);
    }
    return requested.hasMatch(allowedContentTypes);
  }

  private static List<ContentType> getSupportedContentTypes(final EdmProperty property) throws EdmException {
    if (property != null) {
      return property.getType() == EdmSimpleTypeKind.Binary.getEdmSimpleTypeInstance()
          ? Collections.singletonList(property.getMimeType() == null
            ? ContentType.WILDCARD : ContentType.create(property.getMimeType()))
            : Arrays.asList(ContentType.TEXT_PLAIN, ContentType.TEXT_PLAIN_CS_UTF_8);
    } else {
      return null;
    }
    
  }

  private List<String> getSupportedContentTypes(final UriInfoImpl uriInfo, final ODataHttpMethod method)
      throws ODataException {
    Class<? extends ODataProcessor> processorFeature = Dispatcher.mapUriTypeToProcessorFeature(uriInfo);
    UriType uriType = uriInfo.getUriType();
    //
    if (uriType == UriType.URI11) {
      processorFeature = EntitySetProcessor.class;
    } else if ((uriType == UriType.URI10)) {
      processorFeature = EntityProcessor.class;
    } else if (ODataHttpMethod.POST.equals(method)) {
      if (uriType == UriType.URI1 || uriType == UriType.URI6B) {
        processorFeature = EntityProcessor.class;
      }
    }
    return service.getSupportedContentTypes(processorFeature);
  }

  private List<ContentType> getSupportedContentTypes(final Class<? extends ODataProcessor> processorFeature)
      throws ODataException {
    return ContentType.createAsCustom(service.getSupportedContentTypes(processorFeature));
  }

  /**
   * A modifying request that targets an entity with enabled concurrency control
   * must contain at least one concurrency-control HTTP request header field.
   */
  private static void checkConditions(final ODataHttpMethod method, final UriInfoImpl uriInfo,
      final String ifMatch, final String ifNoneMatch, final String ifModifiedSince, final String ifUnmodifiedSince)
      throws ODataException {
    if ((method == ODataHttpMethod.PUT || method == ODataHttpMethod.PATCH || method == ODataHttpMethod.MERGE
        || method == ODataHttpMethod.DELETE)
        && ifMatch == null && ifNoneMatch == null && ifModifiedSince == null && ifUnmodifiedSince == null
        && checkUriType(uriInfo.getUriType())
        && hasConcurrencyControl(uriInfo.getTargetEntitySet().getEntityType())) {
      throw new ODataPreconditionRequiredException(ODataPreconditionRequiredException.COMMON);
    }
  }

  private static boolean checkUriType(UriType uriType) {
    return uriType == UriType.URI2 || uriType == UriType.URI6A || uriType == UriType.URI3
         || uriType == UriType.URI4 || uriType == UriType.URI5 || uriType == UriType.URI17;
  }

  private static boolean hasConcurrencyControl(final EdmEntityType entityType) throws EdmException {
    boolean concurrency = false;
    for (final String propertyName : entityType.getPropertyNames()) {
      final EdmFacets facets = ((EdmProperty) entityType.getProperty(propertyName)).getFacets();
      if (facets != null && facets.getConcurrencyMode() != null
          && facets.getConcurrencyMode() == EdmConcurrencyMode.Fixed) {
        concurrency = true;
        break;
      }
    }
    return concurrency;
  }

  private static String getQueryDebugValue(final Map<String, String> queryParameters) {
    final String debugValue = queryParameters.get(ODataDebugResponseWrapper.ODATA_DEBUG_QUERY_PARAMETER);
    return ODataDebugResponseWrapper.ODATA_DEBUG_JSON.equals(debugValue)
        || ODataDebugResponseWrapper.ODATA_DEBUG_HTML.equals(debugValue)
        || ODataDebugResponseWrapper.ODATA_DEBUG_DOWNLOAD.equals(debugValue) ? debugValue : null;
  }
}