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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.core.commons.ContentType;

/**
 *  
 */
public class ODataRequestImpl extends ODataRequest {

  private ODataHttpMethod method;
  private String httpMethod;
  private CaseInsensitiveMap requestHeaders = new CaseInsensitiveMap();
  private InputStream body;
  private PathInfo pathInfo;
  private Map<String, String> queryParameters;
  private Map<String, List<String>> allQueryParameters;
  private List<String> acceptHeaders;
  private ContentType contentType;
  private List<Locale> acceptableLanguages;

  @Override
  public Map<String, String> getQueryParameters() {
    return queryParameters;
  }

  @Override
  public List<String> getAcceptHeaders() {
    return acceptHeaders;
  }

  @Override
  public String getContentType() {
    return contentType == null ? null : contentType.toContentTypeString();
  }

  @Override
  public List<Locale> getAcceptableLanguages() {
    return acceptableLanguages;
  }

  @Override
  public String getRequestHeaderValue(final String name) {
    final List<String> headerList = requestHeaders.get(name);
    return headerList == null || headerList.isEmpty() ? null : headerList.get(0);
  }

  @Override
  public Map<String, List<String>> getRequestHeaders() {
    return requestHeaders;
  }

  @Override
  public InputStream getBody() {
    return body;
  }

  @Override
  public ODataHttpMethod getMethod() {
    return method;
  }

  @Override
  public String getHttpMethod() {
    return httpMethod;
  }

  @Override
  public PathInfo getPathInfo() {
    return pathInfo;
  }

  @Override
  public Map<String, List<String>> getAllQueryParameters() {
    return allQueryParameters;
  }

  public class ODataRequestBuilderImpl extends ODataRequestBuilder {
    private ODataHttpMethod method;
    private String httpMethod;
    private CaseInsensitiveMap requestHeaders = new CaseInsensitiveMap();
    private InputStream body;
    private PathInfo pathInfo;
    private Map<String, List<String>> allQueryParameters = new HashMap<String, List<String>>();
    private List<String> acceptHeaders;
    private ContentType contentType;
    private List<Locale> acceptableLanguages;

    @Override
    public ODataRequest build() {
      ODataRequestImpl.this.method = method;
      ODataRequestImpl.this.httpMethod = httpMethod;
      ODataRequestImpl.this.requestHeaders = requestHeaders;
      ODataRequestImpl.this.body = body;
      ODataRequestImpl.this.pathInfo = pathInfo;
      queryParameters = convertMultiMaptoSingleMap(allQueryParameters);
      ODataRequestImpl.this.allQueryParameters = allQueryParameters;
      ODataRequestImpl.this.acceptHeaders = acceptHeaders;
      ODataRequestImpl.this.contentType = contentType;
      ODataRequestImpl.this.acceptableLanguages = acceptableLanguages;

      return ODataRequestImpl.this;
    }

    @Override
    public ODataRequestBuilder requestHeaders(final Map<String, List<String>> headers) {
      requestHeaders = new CaseInsensitiveMap();
      for (Entry<String, List<String>> set : headers.entrySet()) {
        requestHeaders.put(set.getKey(), set.getValue());
      }

      return this;
    }

    @Override
    public ODataRequestBuilder httpMethod(String httpMethod) {
      this.httpMethod = httpMethod;
      return this;
    }

    @Override
    public ODataRequestBuilder body(final InputStream body) {
      this.body = body;
      return this;
    }

    @Override
    public ODataRequestBuilder pathInfo(final PathInfo pathInfo) {
      this.pathInfo = pathInfo;
      return this;
    }

    @Override
    public ODataRequestBuilder method(final ODataHttpMethod method) {
      this.method = method;
      return this;
    }

    @Override
    public ODataRequestBuilder acceptableLanguages(final List<Locale> acceptableLanguages) {
      this.acceptableLanguages = acceptableLanguages;
      return this;
    }

    @Override
    public ODataRequestBuilder acceptHeaders(final List<String> acceptHeaders) {
      this.acceptHeaders = acceptHeaders;
      return this;
    }

    @Override
    public ODataRequestBuilder queryParameters(final Map<String, String> queryParameters) {
      for (Entry<String, String> queryParam : queryParameters.entrySet()) {
        List<String> parameterValues = new LinkedList<String>();
        parameterValues.add(queryParam.getValue());

        allQueryParameters.put(queryParam.getKey(), parameterValues);
      }
      return this;
    }

    @Override
    public ODataRequestBuilder allQueryParameters(final Map<String, List<String>> allQueryParameters) {
      this.allQueryParameters = new HashMap<String, List<String>>(allQueryParameters);
      return this;
    }

    @Override
    public ODataRequestBuilder contentType(final String contentType) {
      this.contentType = ContentType.create(contentType);
      return this;
    }

    @Override
    public ODataRequestBuilder fromRequest(final ODataRequest request) {
      pathInfo = request.getPathInfo();
      method = request.getMethod();
      httpMethod = request.getHttpMethod();
      body = request.getBody();
      if (request.getContentType() != null) {
        contentType = ContentType.create(request.getContentType());
      }

      requestHeaders = new CaseInsensitiveMap();
      for (Entry<String, List<String>> set : request.getRequestHeaders().entrySet()) {
        requestHeaders.put(set.getKey(), set.getValue());
      }

      if (request.getAcceptHeaders() != null) {
        acceptHeaders = new ArrayList<String>();
        for (String acceptHeader : request.getAcceptHeaders()) {
          acceptHeaders.add(acceptHeader);
        }
      }
      if (request.getAcceptableLanguages() != null) {
        acceptableLanguages = new ArrayList<Locale>();
        for (Locale acceptLanguage : request.getAcceptableLanguages()) {
          acceptableLanguages.add(acceptLanguage);
        }
      }
      if (request.getAllQueryParameters() != null) {
        allQueryParameters = new HashMap<String, List<String>>();
        for (Map.Entry<String, List<String>> queryParameter : request.getAllQueryParameters().entrySet()) {
          String queryParameterName = queryParameter.getKey();
          allQueryParameters.put(queryParameterName, request.getAllQueryParameters().get(queryParameterName));
        }
      }
      return this;
    }

    private <T, K> Map<T, K> convertMultiMaptoSingleMap(final Map<T, List<K>> multiMap) {
      final Map<T, K> singleMap = new HashMap<T, K>();

      for (Entry<T, List<K>> entry : multiMap.entrySet()) {
        singleMap.put(entry.getKey(), entry.getValue().get(0));
      }

      return singleMap;
    }
  }

  private class CaseInsensitiveMap extends HashMap<String, List<String>> {

    private static final long serialVersionUID = 1L;

    @Override
    public List<String> put(final String key, final List<String> value) {
      return super.put(key.toLowerCase(), value);
    }

    // not @Override because that would require the key parameter to be of type Object
    public List<String> get(final String key) {
      return super.get(key.toLowerCase());
    }

    @Override
    public List<String> get(final Object key) {
      String skey = (String) key;
      return super.get(skey.toLowerCase());
    }
  }
}