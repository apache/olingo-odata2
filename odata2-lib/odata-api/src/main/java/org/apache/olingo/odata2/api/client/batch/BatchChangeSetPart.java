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
 * A BatchChangeSetPart
 * <p> BatchChangeSetPart represents a change request within a Change Set
 */
public abstract class BatchChangeSetPart {

  public abstract Map<String, String> getHeaders();

  public abstract Object getBody();

  public abstract byte[] getBodyAsBytes();

  public abstract String getUri();

  public abstract String getMethod();

  public abstract String getContentId();

  /**
   * @param headers
   * @return a new builder object
   */
  public static BatchChangeSetPartBuilder headers(final Map<String, String> headers) {
    return newBuilder().headers(headers);
  }

  /**
   * @param body a change request body
   * @return a new builder object
   */
  public static BatchChangeSetPartBuilder body(final String body) {
    return newBuilder().body(body);
  }
  
  /**
   * @param body a change request body
   * @return a new builder object
   */
  public static BatchChangeSetPartBuilder body(final byte[] body) {
    return newBuilder().body(body);
  }

  /**
   * @param uri should not be null
   * @return a new builder object
   */
  public static BatchChangeSetPartBuilder uri(final String uri) {
    return newBuilder().uri(uri);
  }

  /**
   * @param method MUST be the PUT, POST, MERGE, DELETE or PATCH method
   * @return a new builder object
   */
  public static BatchChangeSetPartBuilder method(final String method) {
    return newBuilder().method(method);
  }

  /**
   * @param contentId can be used to identify the different request within a the batch
   * @return a new builder object
   */
  public static BatchChangeSetPartBuilder contentId(final String contentId) {
    return newBuilder().contentId(contentId);
  }

  /**
   * @return returns a new builder object
   */
  public static BatchChangeSetPartBuilder newBuilder() {
    return BatchChangeSetPartBuilder.newInstance();
  }

  public static abstract class BatchChangeSetPartBuilder {

    protected BatchChangeSetPartBuilder() {}

    private static BatchChangeSetPartBuilder newInstance() {
      return RuntimeDelegate.createBatchChangeSetPartBuilder();
    }

    public abstract BatchChangeSetPart build();

    public abstract BatchChangeSetPartBuilder headers(Map<String, String> headers);

    public abstract BatchChangeSetPartBuilder body(String body);
    
    public abstract BatchChangeSetPartBuilder body(byte[] body);

    public abstract BatchChangeSetPartBuilder uri(String uri);

    public abstract BatchChangeSetPartBuilder method(String method);

    public abstract BatchChangeSetPartBuilder contentId(String contentId);

  }

}
