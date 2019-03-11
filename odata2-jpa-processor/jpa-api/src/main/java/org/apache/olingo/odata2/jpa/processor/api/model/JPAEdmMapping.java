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
package org.apache.olingo.odata2.jpa.processor.api.model;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneEntityListener;

/**
 * The interface acts a container for storing Java persistence column name. The
 * JPA EDM mapping instance can be associated with any EDM simple, EDM complex
 * property to denote the properties Java persistence column name.
 * 
 * 
 * 
 */
public interface JPAEdmMapping {
  /**
   * The method sets the Java persistence column name into the mapping
   * container.
   * 
   * @param name
   * is the Java persistence column name
   */
  public void setJPAColumnName(String name);

  /**
   * The method gets the Java persistence column name from the mapping
   * container.
   * 
   * @return a String representing the Java persistence column name set into
   * the container
   */
  public String getJPAColumnName();

  /**
   * The method sets the Java persistence entity/property type.
   * 
   * @param type
   * is an instance of type Class<?>
   */
  public void setJPAType(Class<?> type);

  /**
   * The method returns the Java persistence entity/property type.
   * 
   * @return type
   */
  public Class<?> getJPAType();

  /**
   * The method sets a stateless JPA EntityListner type. The entity listener type
   * should inherit from {@link com.sap.core.odata.processor.api.jpa.ODataJPATombstoneEntityListener}.
   * @param entityListner
   * is an instance of type Class<?>
   */
  public void setODataJPATombstoneEntityListener(Class<? extends ODataJPATombstoneEntityListener> entityListener);

  /**
   * The method returns a stateless JPA EntityListener type.
   * @return JPA EntityListener type
   */
  public Class<? extends ODataJPATombstoneEntityListener> getODataJPATombstoneEntityListener();
  
  public boolean isVirtualAccess();
  
  public void setVirtualAccess(boolean virtualAccess);

  public Class getOriginaType();

  public void setOriginalType(Class type);

}
