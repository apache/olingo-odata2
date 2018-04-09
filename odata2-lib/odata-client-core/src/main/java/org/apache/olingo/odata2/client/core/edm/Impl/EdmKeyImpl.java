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

import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmProperty;

/**
 * Objects of this class represent a key for an entity type
 * 
 */
public class EdmKeyImpl {

  private List<EdmProperty> keys;
  private List<EdmAnnotationAttribute> annotationAttributes;
  private List<EdmAnnotationElement> annotationElements;

  /**
   * @return List<{@link PropertyRef}> references to the key properties
   */
  public List<EdmProperty> getKeys() {
    return keys;
  }

  /**
   * @return List of {@link AnnotationAttribute} annotation attributes
   */
  public List<EdmAnnotationAttribute> getAnnotationAttributes() {
    return annotationAttributes;
  }

  /**
   * @return List of {@link AnnotationElement} annotation elements
   */
  public List<EdmAnnotationElement> getAnnotationElements() {
    return annotationElements;
  }

  /**
   * Sets the {@link Property}s by their {@link PropertyRef} for this {@link EdmKeyImpl}
   * @param keys
   * @return {@link EdmKeyImpl} for method chaining
   */
  public EdmKeyImpl setKeys(final List<EdmProperty> keys) {
    this.keys = keys;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationAttribute} for this {@link EdmKeyImpl}
   * @param annotationAttributes
   * @return {@link EdmKeyImpl} for method chaining
   */
  public EdmKeyImpl setAnnotationAttributes(final List<EdmAnnotationAttribute> annotationAttributes) {
    this.annotationAttributes = annotationAttributes;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationElement} for this {@link EdmKeyImpl}
   * @param annotationElements
   * @return {@link EdmKeyImpl} for method chaining
   */
  public EdmKeyImpl setAnnotationElements(final List<EdmAnnotationElement> annotationElements) {
    this.annotationElements = annotationElements;
    return this;
  }
  @Override
  public String toString() {
    StringBuffer keyValues = new StringBuffer();
    for(EdmProperty key:keys){
      try {
        keyValues.append(key.getName().toString());
      } catch (EdmException e) {
        keyValues.append("null");
      }
      keyValues.append(",");
    }
    return String.format((keyValues.substring(0, keyValues.length()-1)).toString());
  }
}
