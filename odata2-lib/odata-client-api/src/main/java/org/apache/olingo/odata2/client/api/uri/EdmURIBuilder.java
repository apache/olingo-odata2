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
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmParameter;
import org.apache.olingo.odata2.api.edm.EdmProperty;

/**
 * 
 * This is an Edm Uri Builder
 *
 */
public interface EdmURIBuilder {
  /**
   * Appends EntitySet segment to the URI.
   *
   * @param entitySet Edm entity set.
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder appendEntitySetSegment(EdmEntitySet entitySet);
  
  /**
   * Appends navigation segment to the URI.
   *
   * @param property navigation property.
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder appendNavigationSegment(EdmNavigationProperty property);
  
  /**
   * Appends key segment to the URI.
   *
   * @param property edm property
   * @param value key value
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder appendKeySegment(EdmProperty property, Object value);
  
  /**
   * Appends key segment to the URI, for multiple keys.
   *
   * @param segmentValues segment values.
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder appendKeySegment(Map<EdmProperty, Object> segmentValues);
  
  /**
   * Appends property segment to the URI.
   *
   * @param property edmProperty
   * @param segmentValue segment value.
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder appendPropertySegment(EdmProperty property, String segmentValue);
  
  /**
   * Adds expand query option.
   *
   * @param expandItems items to be expanded in-line
   * @return current EdmURIBuilder instance
   * @see QueryOption#EXPAND
   */
  EdmURIBuilder expand(String... expandItems);
  
  /**
   * Adds select query option.
   *
   * @param selectItems select items
   * @return current EdmURIBuilder instance
   * @see QueryOption#SELECT
   */
  EdmURIBuilder select(String... selectItems);
  
  /**
   * Adds orderby query option.
   *
   * @param property property string.
   * @return current EdmURIBuilder instance
   * @see QueryOption#ORDERBY
   */
  EdmURIBuilder orderBy(String property);
  
  /**
   * Appends count segment to the URI.
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder appendCountSegment();
  
  /**
   * Appends metadata segment to the URI.
   *
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder appendMetadataSegment();
  
  /**
   * Adds format query option.
   *
   * @param format media type acceptable in a response.
   * @return current EdmURIBuilder instance
   * @see QueryOption#FORMAT
   */
  EdmURIBuilder format(String format);
  
  /**
   * Appends value segment to the URI.
   *
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder appendValueSegment();
  
  /**
   * Adds the specified query option to the URI.
   * <br />
   * Concatenates value if the specified query option already exists.
   *
   * @param option query option.
   * @param value query option value.
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder addQueryOption(QueryOption option, String value);
  
  /**
   * Adds filter query option.
   *
   * @param filter filter string.
   * @return current EdmURIBuilder instance
   * @see QueryOption#FILTER
   */
  EdmURIBuilder filter(String filter);

  /**
   * Adds top query option.
   *
   * @param top maximum number of entities to be returned.
   * @return current EdmURIBuilder instance
   * @see QueryOption#TOP
   */
  EdmURIBuilder top(int top);

  /**
   * Adds skip query option.
   *
   * @param skip number of entities to be skipped into the response.
   * @return current EdmURIBuilder instance
   * @see QueryOption#SKIP
   */
  EdmURIBuilder skip(int skip);
  
  /**
   * Adds custom query option
   * @param paramName parameter name
   * @param paramValue parameter value
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder addCustomQueryOption(String paramName, Object paramValue);
  
  /**
   * Appends Function import to the uri
   * @param functionImport
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder appendFunctionImportSegment(EdmFunctionImport functionImport);
  
  /**
   * Appends function import parameters
   * @param functionImportParams
   * @return current EdmURIBuilder instance
   */
  EdmURIBuilder appendFunctionImportParameters(Map<EdmParameter, Object> functionImportParams);
  
  /**
   * Build OData URI.
   *
   * @return OData URI.
   */
  URI build();
  
}
