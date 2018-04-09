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
package org.apache.olingo.odata2.client.api.uri;

import java.net.URI;
import java.util.Map;

/**
 * This is a URIBuilder class
 *
 */
public interface URIBuilder {
  
  /**
   * Appends EntitySet segment to the URI.
   *
   * @param entitySet String entity set.
   * @return current URIBuilder instance
   */
  URIBuilder appendEntitySetSegment(String entitySet);
  
  /**
   * Appends navigation segment to the URI.
   *
   * @param navigationProperty String navigation property.
   * @return current URIBuilder instance
   */
  URIBuilder appendNavigationSegment(String navigationProperty);

  /**
   * Appends key segment to the URI.
   *
   * @param val segment value.
   * @return current URIBuilder instance
   */
  URIBuilder appendKeySegment(Object val);
  
  /**
   * Appends key segment to the URI, for multiple keys.
   *
   * @param segmentValues segment values.
   * @return current URIBuilder instance
   */
  URIBuilder appendKeySegment(Map<String, Object> segmentValues);
  
  /**
   * Appends property segment to the URI.
   *
   * @param segmentValue String segment value.
   * @return current URIBuilder instance
   */
  URIBuilder appendPropertySegment(String segmentValue);
  
  /**
   * Adds expand query option.
   *
   * @param expandItems items to be expanded in-line
   * @return current URIBuilder instance
   * @see QueryOption#EXPAND
   */
  URIBuilder expand(String... expandItems);
  
  /**
   * Adds select query option.
   *
   * @param selectItems select items
   * @return current URIBuilder instance
   * @see QueryOption#SELECT
   */
  URIBuilder select(String... selectItems);
  
  /**
   * Adds orderby query option.
   *
   * @param order order string.
   * @return current URIBuilder instance
   * @see QueryOption#ORDERBY
   */
  URIBuilder orderBy(String order);
  
  /**
   * Appends count segment to the URI.
   * @return current URIBuilder instance
   */
  URIBuilder appendCountSegment();
  
  /**
   * Appends metadata segment to the URI.
   *
   * @return current URIBuilder instance
   */
  URIBuilder appendMetadataSegment();
  
  /**
   * Adds format query option.
   *
   * @param format media type acceptable in a response.
   * @return current URIBuilder instance
   * @see QueryOption#FORMAT
   */
  URIBuilder format(String format);
  
  /**
   * Appends value segment to the URI.
   *
   * @return current URIBuilder instance
   */
  URIBuilder appendValueSegment();
  
  /**
   * Adds the specified query option to the URI.
   * <br />
   * Concatenates value if the specified query option already exists.
   *
   * @param option query option.
   * @param value query option value.
   * @return current URIBuilder instance
   */
  URIBuilder addQueryOption(QueryOption option, String value);
  
  /**
   * Adds filter query option.
   *
   * @param filter filter string.
   * @return current URIBuilder instance
   * @see QueryOption#FILTER
   */
  URIBuilder filter(String filter);

  /**
   * Adds top query option.
   *
   * @param top maximum number of entities to be returned.
   * @return current URIBuilder instance
   * @see QueryOption#TOP
   */
  URIBuilder top(int top);

  /**
   * Adds skip query option.
   *
   * @param skip number of entities to be skipped into the response.
   * @return current URIBuilder instance
   * @see QueryOption#SKIP
   */
  URIBuilder skip(int skip);
  
  /**
   * Adds custom query option
   * @param paramName parameter name 
   * @param paramValue parameter value
   * @return current URIBuilder instance
   */
  URIBuilder addCustomQueryOption(String paramName, Object paramValue);
  
  /**
   * Appends function import to the uri
   * @param functionImport
   * @return current URIBuilder instance
   */
  URIBuilder appendFunctionImportSegment(String functionImport);
  
  /**
   * Appends function import parameters to the uri
   * @param functionImportParams
   * @return current URIBuilder instance
   */
  URIBuilder appendFunctionImportParameters(Map<String, Object> functionImportParams);
  
  /**
   * Build OData URI.
   *
   * @return OData URI.
   */
  URI build();
}
