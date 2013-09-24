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
package org.apache.olingo.odata2.api.client.batch;

import java.util.Map;

import org.apache.olingo.odata2.api.rt.RuntimeDelegate;

/**
 * A BatchQueryPart
 * <p>BatchQueryPart represents a single retrieve request
 */
public abstract class BatchQueryPart implements BatchPart {

  public abstract Map<String, String> getHeaders();

  public abstract String getUri();

  public abstract String getMethod();

  public abstract String getContentId();

  /**
   * @param headers
   * @return a new builder object
   */
  public static BatchQueryPartBuilder headers(final Map<String, String> headers) {
    return newBuilder().headers(headers);
  }

  /**
   * @param uri should not be null
   * @return a new builder object
   */
  public static BatchQueryPartBuilder uri(final String uri) {
    return newBuilder().uri(uri);
  }

  /**
   * @param method MUST be the HTTP GET method
   * @return a new builder object
   */
  public static BatchQueryPartBuilder method(final String method) {
    return newBuilder().method(method);
  }

  /**
   * @param contentId can be used to identify the different request within a the batch
   * @return a new builder object
   */
  public static BatchQueryPartBuilder contentId(final String contentId) {
    return newBuilder().contentId(contentId);
  }

  /**
   * @return returns a new builder object
   */
  public static BatchQueryPartBuilder newBuilder() {
    return BatchQueryPartBuilder.newInstance();
  }

  public static abstract class BatchQueryPartBuilder {

    protected BatchQueryPartBuilder() {}

    private static BatchQueryPartBuilder newInstance() {
      return RuntimeDelegate.createBatchQueryPartBuilder();
    }

    public abstract BatchQueryPart build();

    public abstract BatchQueryPartBuilder headers(Map<String, String> headers);

    public abstract BatchQueryPartBuilder uri(String uri);

    public abstract BatchQueryPartBuilder method(String method);

    public abstract BatchQueryPartBuilder contentId(String contentId);

  }
}
