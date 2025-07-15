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

import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchInputResource;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BatchChangeSetPartImpl extends BatchChangeSetPart {
  private String method;
  private Map<String, String> headers = new HashMap<String, String>();
  private BatchInputResource batchInputResource;
  private String uri;
  private String cntId;
  private static final String CHANGE_METHODS = "(PUT|POST|DELETE|MERGE|PATCH)";

  @Override
  public Map<String, String> getHeaders() {
    return Collections.unmodifiableMap(headers);
  }

  @Override
  public BatchInputResource getBatchInputResource() {
    return batchInputResource;
  }

  private Charset getCharset() {
    return BatchHelper.extractCharset(this.headers);
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
    return cntId;
  }

  public class BatchChangeSetRequestBuilderImpl extends BatchChangeSetPartBuilder {
    private String method;
    private Map<String, String> headers = new HashMap<String, String>();
    private BatchInputResource batchInputResource;
    private String uri;
    private String contentId;
    private String stringBody;

    @Override
    public BatchChangeSetPart build() {
      if (method == null || uri == null) {
        throw new IllegalArgumentException();
      }
      BatchChangeSetPartImpl.this.method = method;
      BatchChangeSetPartImpl.this.headers = headers;
      BatchChangeSetPartImpl.this.batchInputResource = batchInputResource != null ?
        batchInputResource : stringBodyToBatchInputResource();
      BatchChangeSetPartImpl.this.uri = uri;
      BatchChangeSetPartImpl.this.cntId = contentId;
      return BatchChangeSetPartImpl.this;
    }

    @Override
    public BatchChangeSetPartBuilder headers(final Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    @Override
    public BatchChangeSetPartBuilder body(final String body) {
      this.stringBody = body;
      return this;
    }

    private BatchInputResource stringBodyToBatchInputResource() {
      byte[] bytes = stringBody.getBytes(getCharset());
      return new BatchInputResource(new ByteArrayInputStream(bytes), bytes.length);
    }
    
    @Override
    public BatchChangeSetPartBuilder body(byte[] body) {
      this.batchInputResource = new BatchInputResource(new ByteArrayInputStream(body), body.length);
      return this;
    }

    @Override
    public BatchChangeSetPartBuilder body(BatchInputResource batchInputStream) {
      this.batchInputResource = batchInputStream;
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
