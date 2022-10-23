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
package org.apache.olingo.odata2.client.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataRuntimeApplicationException;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.UriNotMatchingException;
import org.apache.olingo.odata2.api.uri.UriSyntaxException;
import org.apache.olingo.odata2.client.api.edm.EdmDataServices;
import org.apache.olingo.odata2.client.api.ep.ContentTypeBasedDeserializer;
import org.apache.olingo.odata2.client.api.ep.ContentTypeBasedSerializer;
import org.apache.olingo.odata2.client.api.uri.EdmURIBuilder;
import org.apache.olingo.odata2.client.api.uri.URIBuilder;

/**
 * This class provides an instance of odata Client
 *
 */
public abstract class ODataClient {
  
  private static final String IMPLEMENTATION = "org.apache.olingo.odata2.client.core.ODataClientImpl";

  /**
   * Use this method to create a new OData instance. Each thread/request should keep its own instance.
   * @return a new OData instance
   */
  public static ODataClient newInstance() {
    try {
      final Class<?> clazz = Class.forName(ODataClient.IMPLEMENTATION);

      /*
       * We explicitly do not use the singleton pattern to keep the server state free
       * and avoid class loading issues also during hot deployment.
       */
      final Object object = clazz.newInstance();

      return (ODataClient) object;

    } catch (final Exception e) {
      //TODO: Change the exception
      throw new ODataRuntimeApplicationException(null, null, null, null, e);
    }
  }
  
  /**
   * Creates a new serializer object for rendering content in the specified format.
   * 
   * @param contentType any format supported by Olingo (XML, JSON ...)
   * @return ContentTypeBasedSerializer
   */
  public abstract ContentTypeBasedSerializer createSerializer(String contentType) throws EntityProviderException;

  /**
   * Creates a new deserializer object for reading content in the specified format.
   * 
   * @param contentType any content type supported by Olingo (XML, JSON ...)
   * @return ContentTypeBasedDeserializer
   */
  public abstract ContentTypeBasedDeserializer createDeserializer(String contentType) throws EntityProviderException;
  
  /**
   * Reads the metadata file and validates it if validate parameter is set to true
   * @param content inputStream
   * @param validate
   * @return EdmDataServices
   * @throws EntityProviderException
   */
  public abstract EdmDataServices readMetadata(InputStream content, boolean validate) 
      throws EntityProviderException, EdmException;  //NOPMD  - suppressed
  
  /**
   * Parses the uri and returns UriInfo
   * @param edm
   * @param pathSegments
   * @param queryParameters
   * @return UriInfo
   * @throws EntityProviderException
   * @throws EdmException
   */
  
  public abstract UriInfo parseUri(final Edm edm, final List<PathSegment> pathSegments, 
      final Map<String, List<String>> queryParameters) 
      throws UriSyntaxException, UriNotMatchingException, EdmException; //NOPMD  - suppressed
  
  /**
   * Parses the uri and returns UriInfo
   * @param edm
   * @param pathSegments
   * @param queryParameters
   * @param strictFilter
   * @return UriInfo
   * @throws EntityProviderException
   * @throws EdmException
   */

  public abstract UriInfo parseUri(final Edm edm, final List<PathSegment> pathSegments,
      final Map<String, String> queryParameters, boolean strictFilter)
      throws UriSyntaxException, UriNotMatchingException, EdmException; //NOPMD  - suppressed

  /**
   * Parses the uri and returns UriInfo
   * @param edm
   * @param uri
   * @return UriInfo
   * @throws UriSyntaxException
   * @throws UriNotMatchingException
   * @throws EdmException
   */

  public abstract UriInfo parseUri(final Edm edm, final String uri)
      throws UriSyntaxException, UriNotMatchingException, EdmException; //NOPMD  - suppressed

  /**
   * Parses the uri and returns UriInfo
   * @param edm
   * @param uri
   * @param strictFilter
   * @return UriInfo
   * @throws UriSyntaxException
   * @throws UriNotMatchingException
   * @throws EdmException
   */

  public abstract UriInfo parseUri(final Edm edm, final String uri, boolean strictFilter)
      throws UriSyntaxException, UriNotMatchingException, EdmException; //NOPMD  - suppressed

  
  
  /**
   * Constructs the edm uri based on segments appended
   * @param serviceRoot
   * @return EdmURIBuilder
   */
  public abstract EdmURIBuilder edmUriBuilder(String serviceRoot);
  
  /**
   * Constructs the uri based on segments appended
   * @param serviceRoot
   * @return URIBuilder
   */
  public abstract URIBuilder uriBuilder(String serviceRoot);
}
