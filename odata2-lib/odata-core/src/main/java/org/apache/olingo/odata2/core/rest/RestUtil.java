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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.exception.ODataUnsupportedMediaTypeException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.Decoder;

/**
 *  
 */
public class RestUtil {
  public static Response convertResponse(final ODataResponse odataResponse) {
    return convertResponse(odataResponse, false);
  }

  public static Response convertResponse(final ODataResponse odataResponse, final boolean omitResponseBody) {
    try {
      ResponseBuilder responseBuilder =
          Response.noContent().status(odataResponse.getStatus().getStatusCode());
      if(!omitResponseBody) {
        responseBuilder.entity(odataResponse.getEntity());
      }

      for (final String name : odataResponse.getHeaderNames()) {
        responseBuilder = responseBuilder.header(name, odataResponse.getHeader(name));
      }

      return responseBuilder.build();
    } catch (RuntimeException e) {
      if (odataResponse != null) {
        try {
          odataResponse.close();
        } catch (IOException inner) {
          // if close throw an exception we ignore these and re-throw our exception
          throw e;
        }
      }
      throw e;
    }
  }

  /**
   * Return http header value.
   * consider first header value only
   * avoid using jax-rs 2.0 (getHeaderString())
   * @param name header name
   * @param headers JAX-RS header map
   * @return header value or null
   */
  private static String getSafeHeader(final String name, final jakarta.ws.rs.core.HttpHeaders headers) {
    List<String> header = headers.getRequestHeader(name);
    if (header != null && !header.isEmpty()) {
      return header.get(0);
    } else {
      return null;
    }
  }

  public static ContentType extractRequestContentType(final SubLocatorParameter param)
      throws ODataUnsupportedMediaTypeException {

    String contentType = getSafeHeader(HttpHeaders.CONTENT_TYPE, param.getHttpHeaders());
    if (contentType == null || contentType.isEmpty()) {
      // RFC 2616, 7.2.1:
      // "Any HTTP/1.1 message containing an entity-body SHOULD include a
      // Content-Type header field defining the media type of that body. [...]
      // If the media type remains unknown, the recipient SHOULD treat it
      // as type "application/octet-stream"."
      return ContentType.APPLICATION_OCTET_STREAM;
    } else if (ContentType.isParseable(contentType)) {
      return ContentType.create(contentType);
    } else {
      throw new ODataUnsupportedMediaTypeException(
          ODataUnsupportedMediaTypeException.NOT_SUPPORTED_CONTENT_TYPE.addContent(contentType));
    }
  }

  /**
   * Extracts the request content from the servlet as input stream.
   * @param param initialization parameters
   * @return the request content as input stream
   * @throws ODataException
   */
  public static ServletInputStream extractRequestContent(final SubLocatorParameter param) throws ODataException {
    try {
      return param.getServletRequest().getInputStream();
    } catch (final IOException e) {
      throw new ODataException("Error getting request content as ServletInputStream.", e);
    }
  }

  public static <T> InputStream contentAsStream(final T content) throws ODataException {
    if (content == null) {
      throw new ODataBadRequestException(ODataBadRequestException.COMMON);
    }

    InputStream inputStream;
    if (content instanceof InputStream) {
      inputStream = (InputStream) content;
    } else if (content instanceof String) {
      try {
        inputStream = new ByteArrayInputStream(((String) content).getBytes("UTF-8"));
      } catch (final UnsupportedEncodingException e) {
        throw new ODataBadRequestException(ODataBadRequestException.COMMON, e);
      }
    } else {
      throw new ODataBadRequestException(ODataBadRequestException.COMMON);
    }
    return inputStream;
  }

  public static List<String> extractAcceptHeaders(final SubLocatorParameter param) throws ODataBadRequestException {
    List<String> acceptHeaders = param.getHttpHeaders().getRequestHeader(HttpHeaders.ACCEPT);

    List<String> toSort = new LinkedList<String>();
    if (acceptHeaders != null) {
      for (String acceptHeader : acceptHeaders) {
        String[] contentTypes = acceptHeader.split(",");
        for (String contentType : contentTypes) {
          toSort.add(contentType.trim());
        }
      }
    }

    ContentType.sortForQParameter(toSort);
    return toSort;
  }

  public static Map<String, String> extractRequestHeaders(final jakarta.ws.rs.core.HttpHeaders httpHeaders) {
    final MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();
    Map<String, String> headerMap = new HashMap<String, String>();

    for (final String key : headers.keySet()) {
      String value = getSafeHeader(key, httpHeaders);
      if (value != null && !"".equals(value)) {
        headerMap.put(key, value);
      }
    }
    return headerMap;
  }

  public static PathInfoImpl buildODataPathInfo(final SubLocatorParameter param) throws ODataException {
    PathInfoImpl pathInfo = splitPath(param);

    pathInfo.setServiceRoot(buildBaseUri(param.getUriInfo(),
        param.getServletRequest(), pathInfo.getPrecedingSegments()));
    pathInfo.setRequestUri(buildRequestUri(param.getServletRequest()));

    return pathInfo;
  }

  private static PathInfoImpl splitPath(final SubLocatorParameter param) throws ODataException {
    PathInfoImpl pathInfo = new PathInfoImpl();

    List<jakarta.ws.rs.core.PathSegment> precedingPathSegments;
    List<jakarta.ws.rs.core.PathSegment> pathSegments;

    if (param.getPathSplit() == 0) {
      precedingPathSegments = Collections.emptyList();
      pathSegments = param.getPathSegments();
    } else {
      if (param.getPathSegments().size() < param.getPathSplit()) {
        throw new ODataBadRequestException(ODataBadRequestException.URLTOOSHORT);
      }

      precedingPathSegments = param.getPathSegments().subList(0, param.getPathSplit());
      final int pathSegmentCount = param.getPathSegments().size();
      pathSegments = param.getPathSegments().subList(param.getPathSplit(), pathSegmentCount);
    }

    // Percent-decode only the preceding path segments.
    // The OData path segments are decoded during URI parsing.
    pathInfo.setPrecedingPathSegment(convertPathSegmentList(precedingPathSegments));

    List<PathSegment> odataSegments = new ArrayList<PathSegment>();
    for (final jakarta.ws.rs.core.PathSegment segment : pathSegments) {
      if (segment.getMatrixParameters() == null || segment.getMatrixParameters().isEmpty()) {
        odataSegments.add(new ODataPathSegmentImpl(segment.getPath(), null));
      } else {
        // post condition: we do not allow matrix parameters in OData path segments
        throw new ODataNotFoundException(ODataNotFoundException.MATRIX.addContent(segment.getMatrixParameters()
            .keySet(), segment.getPath()));
      }
    }
    pathInfo.setODataPathSegment(odataSegments);

    return pathInfo;
  }

  private static URI buildBaseUri(final UriInfo uriInfo, final HttpServletRequest request,
      final List<PathSegment> precedingPathSegments) throws ODataException {
    try {
      String path = uriInfo.getBaseUri().getPath();
      UriBuilder uriBuilder = UriBuilder.fromUri(path);
      for (final PathSegment ps : precedingPathSegments) {
        uriBuilder = uriBuilder.path(ps.getPath());
        for (final String key : ps.getMatrixParameters().keySet()) {
          final Object[] v = ps.getMatrixParameters().get(key).toArray();
          uriBuilder = uriBuilder.matrixParam(key, v);
        }
      }

      /*
       * workaround because of host name is cached by uriInfo
       */
      uriBuilder.host(request.getServerName()).port(request.getServerPort());
      uriBuilder.scheme(request.getScheme());

      String uriString = uriBuilder.build().toString();
      if (!uriString.endsWith("/")) {
        uriString = uriString + "/";
      }

      return new URI(uriString);
    } catch (final URISyntaxException e) {
      throw new ODataException(e);
    }
  }

  private static URI buildRequestUri(final HttpServletRequest servletRequest) {
    URI requestUri;

    StringBuffer buf = servletRequest.getRequestURL();
    String queryString = servletRequest.getQueryString();

    if (queryString != null) {
      buf.append("?");
      buf.append(queryString);
    }

    String requestUriString = buf.toString();

    requestUri = URI.create(requestUriString);
    return requestUri;
  }

  private static List<PathSegment> convertPathSegmentList(final List<jakarta.ws.rs.core.PathSegment> pathSegments) {
    ArrayList<PathSegment> converted = new ArrayList<PathSegment>();
    for (final jakarta.ws.rs.core.PathSegment pathSegment : pathSegments) {
      final PathSegment segment =
          new ODataPathSegmentImpl(Decoder.decode(pathSegment.getPath()), pathSegment.getMatrixParameters());
      converted.add(segment);
    }
    return converted;
  }

  public static Map<String, String> convertToSinglevaluedMap(final MultivaluedMap<String, String> multi) {
    final Map<String, String> single = new HashMap<String, String>();

    for (final String key : multi.keySet()) {
      final String value = multi.getFirst(key);
      single.put(key, value);
    }

    return single;
  }

}
