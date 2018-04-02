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
import org.apache.olingo.odata2.api.edm.EdmAnnotations;

/**
 * Objects of this class represent annotation elements and attributes
 *
 */
public class EdmAnnotationsImpl implements EdmAnnotations {

  private List<EdmAnnotationAttribute> annotationAttributes;
  private List<EdmAnnotationElement> annotationElements;

  @Override
  public List<EdmAnnotationElement> getAnnotationElements() {
    return annotationElements;
  }

  @Override
  public EdmAnnotationElement getAnnotationElement(final String name, final String namespace) {
    if (annotationElements != null) {
      for (EdmAnnotationElement element : annotationElements) {
        if (element.getName().equals(name) && element.getNamespace().equals(namespace)) {
          return element;
        }
      }
    }
    return null;
  }

  @Override
  public List<EdmAnnotationAttribute> getAnnotationAttributes() {
    return annotationAttributes;
  }

  @Override
  public EdmAnnotationAttribute getAnnotationAttribute(final String name, final String namespace) {
    if (annotationAttributes != null) {
      for (EdmAnnotationAttribute attribute : annotationAttributes) {
        if (attribute.getName().equals(name) && attribute.getNamespace().equals(namespace)) {
          return attribute;
        }
      }
    }
    return null;
  }

  public void setAnnotationAttributes(List<EdmAnnotationAttribute> annotationAttributes) {
    this.annotationAttributes = annotationAttributes;
  }

  public void setAnnotationElements(List<EdmAnnotationElement> annotationElements) {
    this.annotationElements = annotationElements;
  }
  @Override
  public String toString() {
      return String.format(annotationAttributes + " " +annotationElements);
  }

}
