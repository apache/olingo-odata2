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

import java.util.HashMap;
import java.util.Map;

/**
 * Data representation for a single entity.
 */
public class Entity {

  private final Map<String, Object> properties = new HashMap<String, Object>();
  private final Map<String, Object> navigations = new HashMap<String, Object>();
  private EntitySerializerProperties writeProperties;
 
  public Map<String, Object> getNavigations() {
    return navigations;
  }
  
  /**
   * Add Navigation
   * @param link
   * @param value
   */
  public void addNavigation(final String link, final Object value) {
    navigations.put(link, value);
  }

  /**
   * 
   * @param link
   * @return Object
   */
  public Object getNavigation(String link) {
    return navigations.get(link);
  }
 
  /**
   * Return properties of an entity
   * @return Properties
   */
  public Map<String, Object> getProperties() {
    return properties;
  }
  
  /**
   * Add a property to entity
   * @param property
   * @param value
   */
  public void addProperty(final String property, final Object value) {
    properties.put(property, value);
  }

  /**
   * Get Property of an entity
   * @param property
   * @return Object
   */
  public Object getProperty(String property) {
    return properties.get(property);
  }

  /**
   * Returns the Write properties
   * @return EntitySerializerProperties
   */
  public EntitySerializerProperties getWriteProperties() {
    return writeProperties;
  }

  /**
   * Sets the write properties for an entity
   * @param writeProperties
   */
  public void setWriteProperties(EntitySerializerProperties writeProperties) {
    this.writeProperties = writeProperties;
  }
  
}
