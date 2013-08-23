/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.batch;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;

public class BatchSingleResponseImpl implements BatchSingleResponse {

  private String statusCode;
  private String statusInfo;
  private String body;
  private Map<String, String> headers = new HashMap<String, String>();
  private String contentId;

  @Override
  public String getStatusCode() {
    return statusCode;
  }

  @Override
  public String getStatusInfo() {
    return statusInfo;
  }

  @Override
  public String getBody() {
    return body;
  }

  @Override
  public Map<String, String> getHeaders() {
    return headers;
  }

  @Override
  public String getContentId() {
    return contentId;
  }

  @Override
  public String getHeader(final String name) {
    return headers.get(name);
  }

  @Override
  public Set<String> getHeaderNames() {
    return headers.keySet();
  }

  public void setStatusCode(final String statusCode) {
    this.statusCode = statusCode;
  }

  public void setStatusInfo(final String statusInfo) {
    this.statusInfo = statusInfo;
  }

  public void setBody(final String body) {
    this.body = body;
  }

  public void setHeaders(final Map<String, String> headers) {
    this.headers = headers;
  }

  public void setContentId(final String contentId) {
    this.contentId = contentId;
  }

}
