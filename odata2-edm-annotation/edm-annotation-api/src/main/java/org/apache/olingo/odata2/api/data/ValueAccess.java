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
package org.apache.olingo.odata2.api.data;

import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.exception.ODataException;

/**
 * This interface is intended to access values in a Java object.
 */
public interface ValueAccess {

  /**
   * Retrieves the value of an EDM property for the given data object.
   * @param data     the Java data object
   * @param property the requested {@link EdmProperty}
   * @return the requested property value
   */
  public <T> Object getPropertyValue(final T data, final EdmProperty property) throws ODataException;

  /**
   * Sets the value of an EDM property for the given data object.
   * @param data     the Java data object
   * @param property the {@link EdmProperty}
   * @param value    the new value of the property
   */
  public <T, V> void setPropertyValue(T data, final EdmProperty property, final V value) throws ODataException;

  /**
   * Retrieves the Java type of an EDM property for the given data object.
   * @param data     the Java data object
   * @param property the requested {@link EdmProperty}
   * @return the requested Java type
   */
  public <T> Class<?> getPropertyType(final T data, final EdmProperty property) throws ODataException;

  /**
   * Retrieves the value defined by a mapping object for the given data object.
   * @param data     the Java data object
   * @param mapping  the requested {@link EdmMapping}
   * @return the requested value
   */
  public <T> Object getMappingValue(final T data, final EdmMapping mapping) throws ODataException;

  /**
   * Sets the value defined by a mapping object for the given data object.
   * @param data     the Java data object
   * @param mapping  the {@link EdmMapping}
   * @param value    the new value
   */
  public <T, V> void setMappingValue(T data, final EdmMapping mapping, final V value) throws ODataException;
}
