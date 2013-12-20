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
package org.apache.olingo.odata2.annotation.processor.core.datasource;

import org.apache.olingo.odata2.annotation.processor.core.util.AnnotationHelper;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;

/**
 *
 */
public class AnnotationValueAccess implements ValueAccess {
  private final AnnotationHelper annotationHelper = new AnnotationHelper();

  /**
   * Retrieves the value of an EDM property for the given data object.
   * @param data the Java data object
   * @param property the requested {@link EdmProperty}
   * @return the requested property value
   */
  @Override
  public <T> Object getPropertyValue(final T data, final EdmProperty property) throws ODataException {
    if (data == null) {
      return null;
    } else if (annotationHelper.isEdmAnnotated(data)) {
      return annotationHelper.getValueForProperty(data, property.getName());
    }
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);
  }

  /**
   * Sets the value of an EDM property for the given data object.
   * @param data the Java data object
   * @param property the {@link EdmProperty}
   * @param value the new value of the property
   */
  @Override
  public <T, V> void setPropertyValue(final T data, final EdmProperty property, final V value) throws ODataException {
    if (annotationHelper.isEdmAnnotated(data)) {
      annotationHelper.setValueForProperty(data, property.getName(), value);
    } else {
      throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);
    }
  }

  /**
   * Retrieves the Java type of an EDM property for the given data object.
   * @param data the Java data object
   * @param property the requested {@link EdmProperty}
   * @return the requested Java type
   */
  @Override
  public <T> Class<?> getPropertyType(final T data, final EdmProperty property) throws ODataException {
    if (annotationHelper.isEdmAnnotated(data)) {
      Class<?> fieldType = annotationHelper.getFieldTypeForProperty(data, property.getName());
      if (fieldType == null) {
        throw new ODataException("No field type found for property " + property);
      }
      return fieldType;
    }
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);
  }

  /**
   * Retrieves the value defined by a mapping object for the given data object.
   * @param data the Java data object
   * @param mapping the requested {@link EdmMapping}
   * @return the requested value
   */
  @Override
  public <T> Object getMappingValue(final T data, final EdmMapping mapping) throws ODataException {
    if (mapping != null && mapping.getMediaResourceMimeTypeKey() != null) {
      return annotationHelper.getValueForProperty(data, mapping.getMediaResourceMimeTypeKey());
    }
    return null;
  }

  /**
   * Sets the value defined by a mapping object for the given data object.
   * @param data the Java data object
   * @param mapping the {@link EdmMapping}
   * @param value the new value
   */
  @Override
  public <T, V> void setMappingValue(final T data, final EdmMapping mapping, final V value) throws ODataException {
    if (mapping != null && mapping.getMediaResourceMimeTypeKey() != null) {
      annotationHelper.setValueForProperty(data, mapping.getMediaResourceMimeTypeKey(), value);
    }
  }
}
