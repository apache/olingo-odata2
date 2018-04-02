/*
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
 */
package org.apache.olingo.odata2.client.api.ep;

import java.io.InputStream;

/**
 * Data representation for a single entity.
 */
public class EntityStream {

  private InputStream content ;
  private DeserializerProperties readProperties;
  public InputStream getContent() {
    return content;
  }
  /**
   * Set the content
   * @param content
   */
  public void setContent(InputStream content) {
    this.content = content;
  }
  /**
   * Get Read properties
   * @return DeserializerProperties
   */
  public DeserializerProperties getReadProperties() {
    return readProperties;
  }
  /**
   * Set read properties
   * @param readProperties
   */
  public void setReadProperties(DeserializerProperties readProperties) {
    this.readProperties = readProperties;
  }
 
}
