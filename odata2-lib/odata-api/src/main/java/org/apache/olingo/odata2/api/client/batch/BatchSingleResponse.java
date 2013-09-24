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
import java.util.Set;

/**
 * A BatchSingleResponse
 * <p> BatchSingleResponse represents a single response of a Batch Response body. It can be a response to a change
 * request of ChangeSet or a response to a retrieve request
 */
public interface BatchSingleResponse {
  /**
   * @return a result code of the attempt to understand and satisfy the request
   */
  public String getStatusCode();

  /**
   * @return a short textual description of the status code
   */
  public String getStatusInfo();

  /**
   * @return a value of the Content-Id header
   */
  public String getContentId();

  /**
   * @return a body part of a response message
   */
  public String getBody();

  /**
   * @return all available headers
   */
  public Map<String, String> getHeaders();

  /**
   * @param name HTTP response header name
   * @return a header value or null if not set
   */
  public String getHeader(final String name);

  /**
   * @return a set of all available header names
   */
  public Set<String> getHeaderNames();

}
