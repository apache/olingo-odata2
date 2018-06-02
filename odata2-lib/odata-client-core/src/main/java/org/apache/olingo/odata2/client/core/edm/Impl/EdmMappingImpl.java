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
package org.apache.olingo.odata2.client.core.edm.Impl;

import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.provider.Mapping;

/**
 * Object of this class represent the mapping of a value to a MIME type.
 * 
 */
public class EdmMappingImpl implements EdmMapping {

  private String value;
  private Object object;
  private String mediaResourceSourceKey;
  private String mediaResourceMimeTypeKey;
  private String expression;
  private boolean calculated;

  @Override
  public String getInternalName() {
    return value;
  }

  @Override
  public String getInternalExpression() {
    return this.expression;
  }

  public EdmMappingImpl setInternalExpression(final String value) {
    this.expression = value;
    return this;
  }

  @Override
  public Object getObject() {
    return object;
  }

  @Override
  public boolean isCalculated() {
    return calculated;
  }

  public void setCalculated(boolean calculated) {
    this.calculated = calculated;
  }

  @Override
  public String getMediaResourceSourceKey() {
    return mediaResourceSourceKey;
  }

  @Override
  public String getMediaResourceMimeTypeKey() {
    return mediaResourceMimeTypeKey;
  }

  /**
   * Sets the value for this {@link EdmMappingImpl}.
   * @param value
   * @return {@link EdmMappingImpl} for method chaining
   */
  public EdmMappingImpl setInternalName(final String value) {
    this.value = value;
    return this;
  }

  /**
   * Sets an object. This method can be used by a provider to set whatever it wants to associate with this.
   * @param object
   * @return {@link EdmMappingImpl} for method chaining
   */
  public EdmMappingImpl setObject(final Object object) {
    this.object = object;
    return this;
  }

  /**
   * Sets the key for the resource source key which is used for the lookup in the data map
   * @param mediaResourceSourceKey under which the source can be found in the data map
   * @return {@link EdmMappingImpl} for method chaining
   */
  public EdmMappingImpl setMediaResourceSourceKey(final String mediaResourceSourceKey) {
    this.mediaResourceSourceKey = mediaResourceSourceKey;
    return this;
  }

  /**
   * Sets the key for the resource mime type key which is used for the lookup in the data map
   * @param mediaResourceMimeTypeKey under which the mime type can be found in the data map
   * @return {@link EdmMappingImpl} for method chaining
   */
  public EdmMappingImpl setMediaResourceMimeTypeKey(final String mediaResourceMimeTypeKey) {
    this.mediaResourceMimeTypeKey = mediaResourceMimeTypeKey;
    return this;
  }
  
  @Override
  public String toString() {
      return String.format(value);
  }

}
