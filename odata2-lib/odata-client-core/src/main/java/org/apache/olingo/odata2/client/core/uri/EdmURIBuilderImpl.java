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
package org.apache.olingo.odata2.client.core.uri;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmParameter;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.client.api.uri.EdmURIBuilder;
import org.apache.olingo.odata2.client.api.uri.QueryOption;
import org.apache.olingo.odata2.client.api.uri.SegmentType;
import org.apache.olingo.odata2.client.core.uri.util.UriUtil;
import org.apache.olingo.odata2.core.commons.Encoder;

/**
 * This is a builder class that constructs URI with edm validations
 *
 */
public class EdmURIBuilderImpl implements EdmURIBuilder{
  protected final List<Segment> segments = new ArrayList<Segment>();
  private SegmentType state = SegmentType.INITIAL;
  
  /**
   * Insertion-order map of query options.
   */
  protected final Map<String, String> queryOptions = new LinkedHashMap<String, String>();
  
  /**
   * Insertion-order map of custom query options.
   */
  protected final Map<String, String> customQueryOptions = new LinkedHashMap<String, String>();
  
  /**
   * Insertion-order map of function import parameters.
   */
  protected final Map<String, Object> functionImportParameters = new LinkedHashMap<String, Object>();
  
  /**
   * Constructor.
   *
   * @param serviceRoot absolute URL (schema, host and port included) representing the location of the root of the data
   * service.
   */
  public EdmURIBuilderImpl(final String serviceRoot) {
    state = SegmentType.INITIAL;
    segments.add(new Segment(SegmentType.INITIAL, serviceRoot));
  }
  
  @Override
  public EdmURIBuilder appendCountSegment() {
    switch (state) {
    case INITIAL:
    case SIMPLEPROPERTY:
    case COMPLEXPROPERTY:
    case NAVIGATION_TO_ONE:
    case NAVIGATION_TO_MANY_WITH_KEY:
    case FUNCTIONIMPORT_WITH_KEY:
    case ENTITY:
        throw new RuntimeException("Can't specify a key at this position");//NOSONAR
    case ENTITYSET:
        appendCount();
        break;
    case NAVIGATION_TO_MANY:
        appendCount();
        break;
    case FUNCTIONIMPORT_MANY:
      appendCount();
      break;
    default:
        throw new RuntimeException("Unkown state:" + state);//NOSONAR
    }
    return this;
  }

  private void appendCount() {
    segments.add(new Segment(SegmentType.COUNT, SegmentType.COUNT.getValue()));
    state = SegmentType.COUNT;
  }

  @Override
  public EdmURIBuilder appendValueSegment() {
    switch (state) {
    case INITIAL:
    case COMPLEXPROPERTY:
    case ENTITYSET:
    case NAVIGATION_TO_MANY:
    case ENTITY:
    case NAVIGATION_TO_MANY_WITH_KEY:
    case NAVIGATION_TO_ONE:
        throw new RuntimeException("Can't specify a navigation at this position");//NOSONAR
    case SIMPLEPROPERTY:
      addValueSegment();
      break;
    default:
        throw new RuntimeException("Unkown state:" + state);//NOSONAR
    }
    return this;
  }
  
  private void addValueSegment() {
    segments.add(new Segment(SegmentType.VALUE, SegmentType.VALUE.getValue()));
  }

  @Override
  public EdmURIBuilder appendMetadataSegment() {
    segments.add(new Segment(SegmentType.METADATA, SegmentType.METADATA.getValue()));
    return this;
  }

  @Override
  public EdmURIBuilder format(String format) { //NOSONAR
    switch (state) {
    case INITIAL:
    case COUNT:
    case VALUE:
      throw new RuntimeException("Can't specify a format at this position");//NOSONAR
    case NAVIGATION_TO_ONE:
    case SIMPLEPROPERTY:
    case COMPLEXPROPERTY:
    case ENTITYSET:
    case NAVIGATION_TO_MANY:
    case ENTITY:
    case NAVIGATION_TO_MANY_WITH_KEY:
        addFormat(format);
        break;
    default:
        throw new RuntimeException("Unkown state:" + state);//NOSONAR
    }
    return this;
  }

  private void addFormat(String format) {
    addQueryOption(QueryOption.FORMAT.toString(), format, true);
  }

  @Override
  public EdmURIBuilder appendEntitySetSegment(EdmEntitySet entitySet) {
    state = SegmentType.ENTITYSET;
    try {
      segments.add(new Segment(SegmentType.ENTITYSET, entitySet.getName()));
    } catch (EdmException e) {
      throw new RuntimeException("Unexpected EDM Exception: ", e);//NOSONAR
    }
    return this;
  }

  @Override
  public EdmURIBuilder appendNavigationSegment(EdmNavigationProperty property) {
    switch (state) {
    case INITIAL:
    case SIMPLEPROPERTY:
    case COMPLEXPROPERTY:
    case ENTITYSET:
    case NAVIGATION_TO_MANY:
    case FUNCTIONIMPORT_MANY:
        throw new RuntimeException("Can't specify a navigation at this position");//NOSONAR
    case NAVIGATION_TO_ONE:
        addNavigationSegment(property);
        break;
    case ENTITY:
    case NAVIGATION_TO_MANY_WITH_KEY:
    case FUNCTIONIMPORT_WITH_KEY:
        addNavigationSegment(property);
        break;
    default:
        throw new RuntimeException("Unkown state:" + state);//NOSONAR
    }
    return this;
  }
  
  private void addNavigationSegment(EdmNavigationProperty property) {
    try {
      state = property.getMultiplicity() == EdmMultiplicity.MANY? SegmentType.NAVIGATION_TO_MANY: 
        SegmentType.NAVIGATION_TO_ONE;
      segments.add(new Segment(state, property.getName()));
    } catch (EdmException e) {
      throw new RuntimeException("Unexpected EDM Exception: ", e);//NOSONAR
    }
  }

  @Override
  public EdmURIBuilder appendKeySegment(EdmProperty property, Object value) {
    switch (state) {
    case INITIAL:
    case SIMPLEPROPERTY:
    case COMPLEXPROPERTY:
    case NAVIGATION_TO_ONE:
    case NAVIGATION_TO_MANY_WITH_KEY:
    case FUNCTIONIMPORT_WITH_KEY:
    case FUNCTIONIMPORT:
    case ENTITY:
        throw new RuntimeException("Can't specify a key at this position");//NOSONAR
    case ENTITYSET:
        state = SegmentType.ENTITY;
        appendKey(property, value);
        break;
    case NAVIGATION_TO_MANY:
        state = SegmentType.NAVIGATION_TO_MANY_WITH_KEY;
        appendKey(property, value);
        break;
    case FUNCTIONIMPORT_MANY:
      state = SegmentType.FUNCTIONIMPORT_WITH_KEY;
      appendKey(property, value);
      break;
    default:
        throw new RuntimeException("Unkown state:" + state);//NOSONAR
    }
    return this;
  }

  private void appendKey(EdmProperty property, Object value) {
    String key = "";
    try {
      key = getKey(property, value, false);
    } catch (EdmSimpleTypeException e) {
      throw new RuntimeException("Unexpected EDM Exception: ", e);//NOSONAR
    } catch (EdmException e) {
      throw new RuntimeException("Unexpected EDM Exception: ", e);//NOSONAR
    }
    segments.add(new Segment(SegmentType.KEY, key));
  }

  /**
   * @param property
   * @param value
   * @return
   * @throws EdmException
   * @throws EdmSimpleTypeException
   */
  private String getKey(EdmProperty property, Object value, boolean isSegment) 
      throws EdmException {
    String key = "";
    EdmSimpleType edmType = (EdmSimpleType) property.getType();
    if (value instanceof String) {
      value = Encoder.encode(value.toString()); //NOSONAR
    }
    if (!isSegment) {
      key = "(" + edmType.valueToString(value, EdmLiteralKind.URI, property.getFacets()) + ")";
    } else {
      key = edmType.valueToString(value, EdmLiteralKind.URI, property.getFacets());
    }
    return key;
  }

  @Override
  public EdmURIBuilder appendKeySegment(Map<EdmProperty, Object> segmentValues) {
    switch (state) {
    case INITIAL:
    case SIMPLEPROPERTY:
    case COMPLEXPROPERTY:
    case NAVIGATION_TO_ONE:
    case ENTITY:
    case NAVIGATION_TO_MANY_WITH_KEY:
    case FUNCTIONIMPORT:
    case FUNCTIONIMPORT_WITH_KEY:
        throw new RuntimeException("Can't specify a key at this position");//NOSONAR
    case ENTITYSET:
        state = SegmentType.ENTITY;
        appendKey(segmentValues);
        break;
    case NAVIGATION_TO_MANY:
        state = SegmentType.NAVIGATION_TO_MANY_WITH_KEY;
        appendKey(segmentValues);
        break;
    case FUNCTIONIMPORT_MANY:
      state = SegmentType.FUNCTIONIMPORT_WITH_KEY;
      appendKey(segmentValues);
      break;
    default:
        throw new RuntimeException("Unkown state:" + state);//NOSONAR
    }
    return this;
  }
  
  private void appendKey(Map<EdmProperty, Object> segmentValues) {
    String key = "";
    try {
      key = buildMultiKeySegment(segmentValues, ',');
    } catch (EdmSimpleTypeException e) {
      throw new RuntimeException("Unexpected EDM Exception: ", e);//NOSONAR
    } catch (EdmException e) {
      throw new RuntimeException("Unexpected EDM Exception: ", e);//NOSONAR
    }
    segments.add(new Segment(SegmentType.KEY, key));    
  }

  protected String buildMultiKeySegment(final Map<EdmProperty, Object> segmentValues,
      final char separator) throws EdmException {
    final StringBuilder keyBuilder = new StringBuilder().append('(');
    for (Map.Entry<EdmProperty, Object> entry : segmentValues.entrySet()) {
      keyBuilder.append(entry.getKey().getName()).append('=').append(
          getKey((EdmProperty)entry.getKey(), entry.getValue(), true));
      keyBuilder.append(separator);
    }
    keyBuilder.deleteCharAt(keyBuilder.length() - 1).append(')');

    return keyBuilder.toString();
  }
  
  @Override
  public EdmURIBuilder filter(String filter) {
    return replaceQueryOption(QueryOption.FILTER, filter);
  }

  @Override
  public EdmURIBuilder select(String... selectItems) {
    return addQueryOption(QueryOption.SELECT, UriUtil.join(selectItems, ","));
  }

  @Override
  public EdmURIBuilder orderBy(String property) {
    return addQueryOption(QueryOption.ORDERBY, property);
  }

  @Override
  public EdmURIBuilder top(int top) {
    return replaceQueryOption(QueryOption.TOP, String.valueOf(top));
  }

  @Override
  public EdmURIBuilder skip(int skip) {
    return replaceQueryOption(QueryOption.SKIP, String.valueOf(skip));
  }
  
  @Override
  public URI build() {
    return UriUtil.getUri(segments, queryOptions, customQueryOptions, functionImportParameters);
  }

  @Override
  public EdmURIBuilder addQueryOption(QueryOption option, String value) {
    return addQueryOption(option.toString(), value, false);
  }

  public EdmURIBuilder replaceQueryOption(QueryOption option, String value) {
    return addQueryOption(option.toString(), value, true);
  }

  public EdmURIBuilder addQueryOption(String option, String value, boolean replace) { //NOSONAR
    if (option.equalsIgnoreCase(QueryOption.EXPAND.toString())) {
      if (state == SegmentType.COUNT) {
        throw new RuntimeException("Can't specify a query option " + option + " at this position");//NOSONAR
      } else {
        UriUtil.appendQueryOption(option, value, queryOptions, replace);
      }
    } else {
      switch (state) {
      case INITIAL:
      case SIMPLEPROPERTY:
      case COMPLEXPROPERTY:
        throw new RuntimeException("Can't specify a query option " + option + " at this position");//NOSONAR
      case NAVIGATION_TO_ONE:
      case NAVIGATION_TO_MANY_WITH_KEY:
      case ENTITY: 
        entityQueryOption(option, value, replace);
        break;
      case ENTITYSET:
      case NAVIGATION_TO_MANY:
        UriUtil.appendQueryOption(option, value, queryOptions, replace);
        break;
      case COUNT:
        countQueryOption(option, value, replace);
        break;
      default:
          throw new RuntimeException("Unkown state:" + state);//NOSONAR
      }
    }
    return this;
  }

  /**
   * @param option
   * @param value
   * @param replace
   */
  private void countQueryOption(String option, String value, boolean replace) {
    if (option.equalsIgnoreCase(QueryOption.SELECT.toString())) {
      throw new RuntimeException("Can't specify a query option " + option + " at this position");//NOSONAR
    } else {
      UriUtil.appendQueryOption(option, value, queryOptions, replace);
    }
  }

  /**
   * @param option
   * @param value
   * @param replace
   */
  private void entityQueryOption(String option, String value, boolean replace) {
    if (option.equalsIgnoreCase(QueryOption.SELECT.toString())) {
      UriUtil.appendQueryOption(option, value, queryOptions, replace);
    } else {
      throw new RuntimeException("Can't specify a query option " + option + " at this position");//NOSONAR
    }
  }
  
  @Override
  public EdmURIBuilder appendPropertySegment(EdmProperty property, String segmentValue) {
    switch (state) {
    case INITIAL:
    case SIMPLEPROPERTY:
    case NAVIGATION_TO_ONE:
    case ENTITYSET:
    case NAVIGATION_TO_MANY:
    case FUNCTIONIMPORT_MANY:
        throw new RuntimeException("Can't specify a property at this position");//NOSONAR
    case COMPLEXPROPERTY:
        appendProperty(property, segmentValue);
        break;
    case ENTITY:
        appendProperty(property, segmentValue);
        break;
    case NAVIGATION_TO_MANY_WITH_KEY:
    case FUNCTIONIMPORT_WITH_KEY:
    case FUNCTIONIMPORT:
        appendProperty(property, segmentValue);
        break;
    default:
        throw new RuntimeException("Unkown state:" + state);//NOSONAR
    }
    return this;
  }

  private void appendProperty(EdmProperty property, String segmentValue) {
    try {
      state = property.getType().getKind() == EdmTypeKind.SIMPLE? SegmentType.SIMPLEPROPERTY : 
        SegmentType.COMPLEXPROPERTY;
    } catch (EdmException e) {
      throw new RuntimeException("Unexpected EDM Exception: ", e);//NOSONAR
    }
    segments.add(new Segment(state, segmentValue));
  }

  @Override
  public EdmURIBuilder expand(String... expandItems) {
    return addQueryOption(QueryOption.EXPAND, UriUtil.join(expandItems, ","));
  }

  @Override
  public EdmURIBuilder addCustomQueryOption(String paramName, Object paramValue) {
    UriUtil.appendQueryOption(paramName, paramValue.toString(), 
        customQueryOptions, true);
    return this;
  }

  @Override
  public EdmURIBuilder appendFunctionImportSegment(EdmFunctionImport functionImport) {
    try {
      state = functionImport.getReturnType() != null ? 
          (functionImport.getReturnType().getMultiplicity() == EdmMultiplicity.MANY && 
          functionImport.getReturnType().getType().getKind() == EdmTypeKind.ENTITY
          ? SegmentType.FUNCTIONIMPORT_MANY : SegmentType.FUNCTIONIMPORT) : SegmentType.FUNCTIONIMPORT;
      segments.add(new Segment(state, functionImport.getName()));
    } catch (EdmException e) {
      throw new RuntimeException("Unexpected EDM Exception: ", e);//NOSONAR
    }
    return this;
  }

  @Override
  public EdmURIBuilder appendFunctionImportParameters(Map<EdmParameter, Object> functionImportParams) {
    try {
      if (functionImportParams != null) {
        for (Map.Entry<EdmParameter, Object> param : functionImportParams.entrySet()) {
          EdmParameter edmParam = param.getKey();
          EdmSimpleType edmType = (EdmSimpleType) edmParam.getType();
          Object value = param.getValue();
          if (value instanceof String) {
            value = value.toString();
          } 
          value = edmType.valueToString(value, EdmLiteralKind.URI, edmParam.getFacets());
          functionImportParameters.put(edmParam.getName(), value);
        }
      }
    } catch (EdmException e) {
      throw new RuntimeException("Unexpected EDM Exception: ", e);//NOSONAR
    }
    return this;
  }
}
