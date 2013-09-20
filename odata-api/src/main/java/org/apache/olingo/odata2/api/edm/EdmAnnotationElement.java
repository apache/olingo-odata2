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
package org.apache.olingo.odata2.api.edm;

import java.util.List;

import org.apache.olingo.odata2.api.edm.provider.AnnotationAttribute;
import org.apache.olingo.odata2.api.edm.provider.AnnotationElement;

/**
 * @org.apache.olingo.odata2.DoNotImplement
 * A CSDL AnnotationElement element
 * <p>EdmAnnotationElement is a custom XML element which can be applied to a CSDL element.
 * 
 */
public interface EdmAnnotationElement {

  /**
   * Get the namespace of the custom element
   * 
   * @return String
   */
  String getNamespace();

  /**
   * Get the prefix of the custom element
   * 
   * @return String
   */
  String getPrefix();

  /**
   * Get the name of the custom element
   * 
   * @return String
   */
  String getName();

  /**
   * Get the XML data of the custom element
   * 
   * @return String
   */
  String getText();

  /**
   * Get the child elements of the custom element
   * 
   * @return child elements of this {@link EdmAnnotationElement}
   */
  List<AnnotationElement> getChildElements();

  /**
   * Get the attributes of this custom element
   * 
   * @return the attributes of this {@link EdmAnnotationElement}
   */
  List<AnnotationAttribute> getAttributes();

}
