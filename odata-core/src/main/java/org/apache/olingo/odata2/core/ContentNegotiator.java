/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotAcceptableException;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.ContentType.ODataFormat;
import org.apache.olingo.odata2.core.uri.UriInfoImpl;
import org.apache.olingo.odata2.core.uri.UriType;

/**
 *  
 */
public class ContentNegotiator {
  private static final String URI_INFO_FORMAT_JSON = "json";
  private static final String URI_INFO_FORMAT_ATOM = "atom";
  private static final String URI_INFO_FORMAT_XML = "xml";
  static final String DEFAULT_CHARSET = "utf-8";
  
  /**
   * Do the content negotiation based on requested content type (in <code>acceptHeaderContentTypes</code> list) 
   * and supported content types (in <code>supportedContentTypes</code> list).
   * 
   * @param uriInfo additional uri informations especially <code>$format</code> 
   * @param acceptHeaderContentTypes list of requested content types
   * @param supportedContentTypes list of supported content types
   * @return best fitting content type or <code>NULL</code> if content type is not set and for given {@link UriInfo} is ignored
   * @throws ODataException if no supported content type was found
   */
  public String doContentNegotiation(final UriInfoImpl uriInfo, final List<String> acceptHeaderContentTypes, final List<String> supportedContentTypes) throws ODataException {
    ContentType contentType;
    if (uriInfo.getFormat() == null) {
      contentType = doContentNegotiationForAcceptHeader(acceptHeaderContentTypes, ContentType.create(supportedContentTypes));
    } else {
      contentType = doContentNegotiationForFormat(uriInfo, ContentType.createCustom(supportedContentTypes));
    }

    if (contentType.getODataFormat() == ODataFormat.CUSTOM) {
      return contentType.getType();
    }
    return contentType.toContentTypeString();
  }

  private ContentType doContentNegotiationForFormat(final UriInfoImpl uriInfo, final List<ContentType> supportedContentTypes) throws ODataException {
    validateFormatQuery(uriInfo);
    ContentType formatContentType = mapFormat(uriInfo);
    formatContentType = formatContentType.receiveWithCharsetParameter(DEFAULT_CHARSET);

    for (final ContentType contentType : supportedContentTypes) {
      if (contentType.equals(formatContentType)) {
        return formatContentType;
      }
    }

    throw new ODataNotAcceptableException(ODataNotAcceptableException.NOT_SUPPORTED_CONTENT_TYPE.addContent(uriInfo.getFormat()));
  }

  /**
   * Validates that <code>dollar format query/syntax</code> is correct for further processing.
   * If some validation error occurs an exception is thrown.
   * 
   * @param uriInfo
   * @throws ODataBadRequestException
   */
  private void validateFormatQuery(final UriInfoImpl uriInfo) throws ODataBadRequestException {
    if (uriInfo.isValue()) {
      throw new ODataBadRequestException(ODataBadRequestException.INVALID_SYNTAX);
    }
  }

  private ContentType mapFormat(final UriInfoImpl uriInfo) {
    final String format = uriInfo.getFormat();
    if (URI_INFO_FORMAT_XML.equals(format)) {
      return ContentType.APPLICATION_XML;
    } else if (URI_INFO_FORMAT_ATOM.equals(format)) {
      if (uriInfo.getUriType() == UriType.URI0) {
        // special handling for serviceDocument uris (UriType.URI0)
        return ContentType.APPLICATION_ATOM_SVC;
      }
      return ContentType.APPLICATION_ATOM_XML;
    } else if (URI_INFO_FORMAT_JSON.equals(format)) {
      return ContentType.APPLICATION_JSON;
    }

    return ContentType.createCustom(format);
  }

  private ContentType doContentNegotiationForAcceptHeader(final List<String> acceptHeaderContentTypes, final List<ContentType> supportedContentTypes) throws ODataException {
    return contentNegotiation(extractAcceptHeaders(acceptHeaderContentTypes), supportedContentTypes);
  }

  private List<ContentType> extractAcceptHeaders(final List<String> acceptHeaderValues) throws ODataBadRequestException {
    final List<ContentType> mediaTypes = new ArrayList<ContentType>();
    if (acceptHeaderValues != null) {
      for (final String mediaType : acceptHeaderValues) {
        try {
          mediaTypes.add(ContentType.create(mediaType.toString()));
        } catch (IllegalArgumentException e) {
          throw new ODataBadRequestException(ODataBadRequestException.INVALID_HEADER.addContent("Accept")
              .addContent(mediaType.toString()), e);
        }
      }
    }

    return mediaTypes;
  }

  ContentType contentNegotiation(final List<ContentType> acceptedContentTypes, final List<ContentType> supportedContentTypes) throws ODataException {
    final Set<ContentType> setSupported = new HashSet<ContentType>(supportedContentTypes);

    if (acceptedContentTypes.isEmpty()) {
      if (!setSupported.isEmpty()) {
        return supportedContentTypes.get(0);
      }
    } else {
      for (ContentType contentType : acceptedContentTypes) {
        contentType = contentType.receiveWithCharsetParameter(DEFAULT_CHARSET);
        final ContentType match = contentType.match(supportedContentTypes);
        if (match != null) {
          return match;
        }
      }
    }

    throw new ODataNotAcceptableException(ODataNotAcceptableException.NOT_SUPPORTED_ACCEPT_HEADER.addContent(acceptedContentTypes.toString()));
  }

}
