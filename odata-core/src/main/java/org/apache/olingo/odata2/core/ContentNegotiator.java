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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotAcceptableException;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.ContentType.ODataFormat;
import org.apache.olingo.odata2.core.uri.UriInfoImpl;
import org.apache.olingo.odata2.core.uri.UriType;

/**
 * Handles content negotiation with handling of OData special cases.
 */
public class ContentNegotiator {
  private static final String URI_INFO_FORMAT_JSON = "json";
  private static final String URI_INFO_FORMAT_ATOM = "atom";
  private static final String URI_INFO_FORMAT_XML = "xml";
  static final String DEFAULT_CHARSET = "utf-8";
  
  private UriInfoImpl uriInfo;
  private ODataRequest odataRequest;

  /**
   * Creates a {@link ContentNegotiator} for given {@link ODataRequest} and {@link UriInfoImpl}
   * which then can be used for a <code>accept content type</code> ({@link #doAcceptContentNegotiation(List)})
   * and <code>response content type</code> ({@link #doResponseContentNegotiation(ContentType)}) negotiation.
   * 
   * @param request specific request
   * @param uriInfo specific uri information
   * @throws IllegalArgumentException if at least one of both parameters is <code>NULL</code>
   */
  public ContentNegotiator(ODataRequest request, UriInfoImpl uriInfo) {
    if(request == null) {
      throw new IllegalArgumentException("Parameter ODataRequest MUST NOT be null.");
    }
    if(uriInfo == null) {
      throw new IllegalArgumentException("Parameter UriInfoImpl MUST NOT be null.");
    }
    this.odataRequest = request;
    this.uriInfo = uriInfo;
  }
  
  /**
   * Do the content negotiation for <code>accept header value</code> based on 
   * requested content type (in HTTP accept header from {@link ODataRequest} 
   * set on {@link ContentNegotiator} creation) in combination with uri information 
   * (from {@link UriInfo} set on {@link ContentNegotiator} creation)
   * and from given supported content types (via <code>supportedContentTypes</code>).
   * 
   * @param supportedContentTypes list of supported content types
   * @return best fitting content type or <code>NULL</code> if content type is not set and for given {@link UriInfo} is ignored
   * @throws ODataException if no supported content type was found
   */
  public ContentType doAcceptContentNegotiation(List<String> supportedContentTypes) throws ODataException {

    if(uriInfo.isCount() || uriInfo.isValue()) {
      String rawAcceptHeader = odataRequest.getRequestHeaderValue("Accept");
      if(rawAcceptHeader == null) {
        return ContentType.WILDCARD;
      }
      return ContentType.createAsCustom(rawAcceptHeader);
    } 

    List<String> usedContentTypes = supportedContentTypes;
    if(ODataHttpMethod.POST.equals(odataRequest.getMethod()) && 
        (uriInfo.getUriType() == UriType.URI1 || uriInfo.getUriType() == UriType.URI6B)) {

      usedContentTypes = new LinkedList<String>(supportedContentTypes);
      usedContentTypes.add(0, ContentType.APPLICATION_ATOM_XML_ENTRY_CS_UTF_8.toContentTypeString());
      usedContentTypes.add(1, ContentType.APPLICATION_ATOM_XML_ENTRY.toContentTypeString());
      usedContentTypes.remove(ContentType.APPLICATION_ATOM_XML_FEED.toContentTypeString());
      usedContentTypes.remove(ContentType.APPLICATION_ATOM_XML_FEED_CS_UTF_8.toContentTypeString());
    }
    
    if (uriInfo.getFormat() == null) {
      return doContentNegotiationForAcceptHeader(odataRequest.getAcceptHeaders(), ContentType.create(usedContentTypes));
    } else {
      return doContentNegotiationForFormat(uriInfo, ContentType.createAsCustom(usedContentTypes));
    }
  }

  /**
   * Do the content negotiation for <code>response content type header value</code> based on 
   * HTTP request method (from {@link ODataRequest} set on {@link ContentNegotiator} creation) 
   * in combination with uri information (from {@link UriInfo} set on {@link ContentNegotiator} creation)
   * and from given <code>accepted content type</code> (via <code>acceptContentType</code> parameter).
   * 
   * @param acceptContentType accepted content type for {@link ODataRequest} and {@link UriInfo} combination (which both 
   *                          were set on {@link ContentNegotiator} creation).
   * @return best correct response content type based on accepted content type, {@link ODataRequest} and {@link UriInfo} combination 
   */
  public ContentType doResponseContentNegotiation(ContentType acceptContentType) {
      ContentType contentType = acceptContentType;
      UriType uriType = uriInfo.getUriType();
      if(contentType != null && contentType.getODataFormat() == ODataFormat.ATOM) {
        if(uriType == UriType.URI1 || uriType == UriType.URI6B) {
          if(ODataHttpMethod.GET.equals(odataRequest.getMethod())) {
            contentType = ContentType.create(contentType, ContentType.PARAMETER_TYPE, "feed");
          } else {
            contentType = ContentType.create(contentType, ContentType.PARAMETER_TYPE, "entry");          
          }
        } else if(uriType == UriType.URI2 || uriType == UriType.URI6A) {
          contentType = ContentType.create(contentType, ContentType.PARAMETER_TYPE, "entry");
        }
      } 

    return contentType;
  }


  private ContentType doContentNegotiationForFormat(final UriInfoImpl uriInfo, final List<ContentType> supportedContentTypes) throws ODataException {
    validateFormatQuery(uriInfo);
    ContentType formatContentType = mapFormat(uriInfo);
    formatContentType = ensureCharset(formatContentType);

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

    return ContentType.createAsCustom(format);
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
        contentType = ensureCharset(contentType);
        final ContentType match = contentType.match(supportedContentTypes);
        if (match != null) {
          return match;
        }
      }
    }

    throw new ODataNotAcceptableException(ODataNotAcceptableException.NOT_SUPPORTED_ACCEPT_HEADER.addContent(acceptedContentTypes.toString()));
  }

  private ContentType ensureCharset(ContentType contentType) {
    if(ContentType.APPLICATION_ATOM_XML.isCompatible(contentType) 
        || ContentType.APPLICATION_ATOM_SVC.isCompatible(contentType) 
        || ContentType.APPLICATION_XML.isCompatible(contentType)) {
      return contentType.receiveWithCharsetParameter(DEFAULT_CHARSET);
    }
    return contentType;
  }
}
