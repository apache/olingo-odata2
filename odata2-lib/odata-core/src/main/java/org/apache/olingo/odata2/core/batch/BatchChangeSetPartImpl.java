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
package org.apache.olingo.odata2.core.batch;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.core.commons.ContentType;

public class BatchChangeSetPartImpl extends BatchChangeSetPart {
  private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");
  private static final String CONTENT_TYPE = "content-type";
  private String method;
  private Map<String, String> headers = new HashMap<String, String>();
  private String body;
  private String uri;
  public String contentId;
  private static final String CHANGE_METHODS = "(PUT|POST|DELETE|MERGE|PATCH)";

  @Override
  public Map<String, String> getHeaders() {
    return Collections.unmodifiableMap(headers);
  }

  @Override
  public String getBody() {
    return body;
  }

  @Override
  public byte[] getBodyAsBytes() {
    if(body == null) {
      return new byte[0];
    }
    Charset charset = getCharset();
    return body.getBytes(charset);
  }

  private Charset getCharset() {
//    String contentType = headers.get(HttpHeaders.CONTENT_TYPE);
    String contentType = null;
    for (Map.Entry<String, String> s : headers.entrySet()) {
      if(s.getKey().equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
        contentType = s.getValue();
        break;
      }
    }
    ContentType ct = ContentType.parse(contentType);
    if(ct != null) {
      String charsetString = ct.getParameters().get(ContentType.PARAMETER_CHARSET);
      if (charsetString != null && Charset.isSupported(charsetString)) {
        return Charset.forName(charsetString);
      }
    }
    return DEFAULT_CHARSET;
  }

  @Override
  public String getMethod() {
    return method;
  }

  @Override
  public String getUri() {
    return uri;
  }

  @Override
  public String getContentId() {
    return contentId;
  }

  public class BatchChangeSetRequestBuilderImpl extends BatchChangeSetPartBuilder {
    private String method;
    private Map<String, String> headers = new HashMap<String, String>();
    private String body;
    private String uri;
    private String contentId;

    @Override
    public BatchChangeSetPart build() {
      if (method == null || uri == null) {
        throw new IllegalArgumentException();
      }
      BatchChangeSetPartImpl.this.method = method;
      BatchChangeSetPartImpl.this.headers = headers;
      BatchChangeSetPartImpl.this.body = body;
      BatchChangeSetPartImpl.this.uri = uri;
      BatchChangeSetPartImpl.this.contentId = contentId;
      return BatchChangeSetPartImpl.this;
    }

    @Override
    public BatchChangeSetPartBuilder headers(final Map<String, String> headers) {
//      this.headers = new HashMap<String, String>(headers.size());
//      for (Map.Entry<String, String> entry : headers.entrySet()) {
//        this.headers.put(entry.getKey().toLowerCase(Locale.ENGLISH), entry.getValue());
//      }
      this.headers = headers;
      return this;
    }

    @Override
    public BatchChangeSetPartBuilder body(final String body) {
      this.body = body;
      return this;
    }

    @Override
    public BatchChangeSetPartBuilder uri(final String uri) {
      this.uri = uri;
      return this;
    }

    @Override
    public BatchChangeSetPartBuilder method(final String method) {
      if (method != null && method.matches(CHANGE_METHODS)) {
        this.method = method;
      } else {
        throw new IllegalArgumentException();
      }
      return this;
    }

    @Override
    public BatchChangeSetPartBuilder contentId(final String contentId) {
      this.contentId = contentId;
      return this;
    }

  }

}
