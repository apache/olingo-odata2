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
package org.apache.olingo.odata2.client.core.uri.util;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.client.api.uri.QueryOption;
import org.apache.olingo.odata2.client.api.uri.SegmentType;
import org.apache.olingo.odata2.client.core.uri.Segment;
import org.apache.olingo.odata2.core.commons.Encoder;

/**
 * Util class
 *
 */
public class UriUtil {

  private UriUtil() {
    
  }
  
  /**
   * 
   * @param segments
   * @param queryOptions
   * @param customQueryOptions
   * @param functionImportParameters 
   * @return URI
   */
  public static URI getUri(List<Segment> segments, Map<String, String> queryOptions, 
      Map<String, String> customQueryOptions, Map<String, Object> functionImportParameters) { //NOPMD  - suppressed
    final StringBuilder segmentsBuilder = new StringBuilder();

    if (segments.size() == 1 && segments.get(0).getType() == SegmentType.INITIAL
        && customQueryOptions.isEmpty()
        && queryOptions.isEmpty() && functionImportParameters.isEmpty()) {
      segmentsBuilder.append(segments.get(0).getValue());
      if (segmentsBuilder.charAt(segmentsBuilder.length() - 1) != '/') {
        segmentsBuilder.append('/');
      }
      return URI.create(segmentsBuilder.toString());
    }
    for (Segment seg : segments) {
      if (segmentsBuilder.length() > 0 && seg.getType() != SegmentType.KEY &&
          seg.getType() != SegmentType.NAVIGATION_TO_MANY_WITH_KEY && 
          seg.getType() != SegmentType.FUNCTIONIMPORT_WITH_KEY &&
          segmentsBuilder.charAt(segmentsBuilder.length() - 1) != '/') {
            segmentsBuilder.append('/');
         }
      segmentsBuilder.append(seg.getValue());
    }

    try {
      if (!queryOptions.isEmpty()) {
        appendQuerySegmentDelimiter(true, true, segmentsBuilder);
        int i = 0;
        for (Map.Entry<String, String> option : queryOptions.entrySet()) {
        //Appends a system query option to uri builder
          i++;
          appendQuerySegments(QueryOption.valueOf(option.getKey()).getValue(), option.getValue(), 
              segmentsBuilder, true);
          if (i < queryOptions.size()) {
            segmentsBuilder.append("&");
          }
        }
      }
      
      if (!customQueryOptions.isEmpty()) {
        appendQuerySegmentDelimiter(queryOptions.isEmpty(), true, segmentsBuilder);
        int i = 0;
        for (Map.Entry<String, String> option : customQueryOptions.entrySet()) {
          //Appends a custom query option to uri builder
          i++;
          appendQuerySegments(option.getKey(), option.getValue(), 
              segmentsBuilder, false);
          if (i < customQueryOptions.size()) {
            segmentsBuilder.append("&");
          }
        }
      }
      
      if (!functionImportParameters.isEmpty()) {
        appendQuerySegmentDelimiter(queryOptions.isEmpty(), customQueryOptions.isEmpty(), segmentsBuilder);
        int i = 0;
        for (Map.Entry<String, Object> funcParam : functionImportParameters.entrySet()) {
          //Appends a function import URI with parameters to uri builder
          i++;
          appendQuerySegments(funcParam.getKey(), funcParam.getValue(), 
              segmentsBuilder, false);
          if (i < functionImportParameters.size()) {
            segmentsBuilder.append("&");
          }
        }
      }

      return URI.create(segmentsBuilder.toString());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Could not build valid URI", e);
    }
  }
  
  /**
   * 
   * @param isQueryOptions
   * @param isCustomQueryOptions
   * @param segmentsBuilder
   */
  private static void appendQuerySegmentDelimiter(boolean isQueryOptions, 
      boolean isCustomQueryOptions, StringBuilder segmentsBuilder) {
    if (!isQueryOptions || !isCustomQueryOptions) {
      segmentsBuilder.append("&");
    } else {
      segmentsBuilder.append("?");
    }
  }
  
  /**
   * 
   * @param name
   * @param value
   * @param segmentsBuilder
   */
  private static void appendQuerySegments(String name, Object value, 
      StringBuilder segmentsBuilder, boolean isQueryOption) {
    if (isQueryOption) {
      segmentsBuilder.append(name); 
    } else {
      segmentsBuilder.append(Encoder.encode(name)); 
    }
    segmentsBuilder.append("="); 
    segmentsBuilder.append(Encoder.encode(value.toString()));
  }

  /**
   * 
   * @param items
   * @param separator
   * @return String
   */
  public static String join(String[] items, String separator) {
    return join(items, separator, 0, items.length);
  }

  private static String join(String[] items, String separator, int startIndex, int endIndex) {
    if (items == null) {
      return null;
    }
    if (separator == null) {
      separator = ""; //NOSONAR
    }
    
    final int noOfItems = endIndex - startIndex;
    if (noOfItems <= 0) {
      return "";
    }

    final StringBuilder buf = new StringBuilder(noOfItems * 16);

    for (int i = startIndex; i < endIndex; i++) {
      if (i > startIndex) {
        buf.append(separator);
      }
      if (items[i] != null) {
        buf.append(items[i]);
      }
    }
    return buf.toString();
  }
  
  /**
   * 
   * @param paramName
   * @param paramValue
   * @param queryOptions
   * @param replace
   */
  public static void appendQueryOption(String paramName, String paramValue, Map<String, 
      String> queryOptions, boolean replace) {
    final StringBuilder builder = new StringBuilder();
    if (!replace && queryOptions.containsKey(paramName)) {
      builder.append(queryOptions.get(paramName)).append(',');
    }
    builder.append(paramValue);
    queryOptions.put(paramName, builder.toString());
  }
}
