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
package org.apache.olingo.odata2.jpa.processor.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;

public final class ODataEntityParser {

  private ODataJPAContext context;
  private Edm edm;

  public ODataEntityParser(final ODataJPAContext context) {
    this.context = context;
  }

  public final ODataEntry parseEntry(final EdmEntitySet entitySet,
      final InputStream content, final String requestContentType, final boolean merge)
      throws ODataBadRequestException {
    ODataEntry entryValues;
    try {
      EntityProviderReadProperties entityProviderProperties =
          EntityProviderReadProperties.init().mergeSemantic(merge).build();
      entryValues = EntityProvider.readEntry(requestContentType, entitySet, content, entityProviderProperties);
    } catch (EntityProviderException e) {
      throw new ODataBadRequestException(ODataBadRequestException.BODY, e);
    }
    return entryValues;

  }

  public final UriInfo parseURISegmentWithCustomOptions(final int segmentFromIndex, final int segmentToIndex,
      final Map<String, String> options) throws ODataJPARuntimeException {
    UriInfo uriInfo = null;
    if (segmentFromIndex == segmentToIndex || segmentFromIndex > segmentToIndex || segmentFromIndex < 0) {
      return uriInfo;
    }
    try {
      edm = getEdm();
      List<PathSegment> pathSegments = context.getODataContext().getPathInfo().getODataSegments();
      List<PathSegment> subPathSegments = pathSegments.subList(segmentFromIndex, segmentToIndex);
      uriInfo = UriParser.parse(edm, subPathSegments, options);
    } catch (ODataException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
    }
    return uriInfo;
  }

  public final UriInfo parseURISegment(final int segmentFromIndex, final int segmentToIndex)
      throws ODataJPARuntimeException {
    UriInfo uriInfo = null;
    if (segmentFromIndex == segmentToIndex || segmentFromIndex > segmentToIndex || segmentFromIndex < 0) {
      return uriInfo;
    }
    try {
      edm = getEdm();
      List<PathSegment> pathSegments = context.getODataContext().getPathInfo().getODataSegments();
      List<PathSegment> subPathSegments = pathSegments.subList(segmentFromIndex, segmentToIndex);
      uriInfo = UriParser.parse(edm, subPathSegments, Collections.<String, String> emptyMap());
    } catch (ODataException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
    }
    return uriInfo;
  }

  public final UriInfo parseLink(final EdmEntitySet entitySet, final InputStream content, final String contentType)
      throws ODataJPARuntimeException {

    String uriString = null;
    UriInfo uri = null;
    try {
      uriString = EntityProvider.readLink(contentType, entitySet, content);
      ODataContext odataContext = context.getODataContext();
      final String serviceRoot = odataContext.getPathInfo().getServiceRoot().toString();
      final String path =
          uriString.startsWith(serviceRoot.toString()) ? uriString.substring(serviceRoot.length()) : uriString;
      final PathSegment pathSegment = getPathSegment(path);
      edm = getEdm();
      uri = UriParser.parse(edm, Arrays.asList(pathSegment), Collections.<String, String> emptyMap());
    } catch (ODataException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
    }
    return uri;
  }

  public UriInfo parseLinkSegments(final List<String> linkSegments, final Map<String, String> options)
      throws ODataJPARuntimeException {
    List<PathSegment> pathSegments = new ArrayList<PathSegment>();
    for (String link : linkSegments) {
      PathSegment pathSegment = getPathSegment(link);
      pathSegments.add(pathSegment);
    }
    UriInfo uriInfo = null;
    try {
      edm = getEdm();
      uriInfo = UriParser.parse(edm, pathSegments, options);
    } catch (ODataException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
    }
    return uriInfo;
  }

  public UriInfo parseBindingLink(final String link, final Map<String, String> options) throws ODataJPARuntimeException {
    final PathSegment pathSegment = getPathSegment(link);
    UriInfo uriInfo = null;
    try {
      edm = getEdm();
      uriInfo = UriParser.parse(edm, Arrays.asList(pathSegment), options);
    } catch (ODataException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
    }
    return uriInfo;
  }

  private Edm getEdm() throws ODataException {
    if (edm == null) {
      edm = context.getODataContext().getService().getEntityDataModel();
    }
    return edm;
  }

  private PathSegment getPathSegment(final String path) {
    final PathSegment pathSegment = new PathSegment() {

      @Override
      public String getPath() {
        return path;
      }

      @Override
      public Map<String, List<String>> getMatrixParameters() {
        return null;
      }
    };
    return pathSegment;
  }
}
