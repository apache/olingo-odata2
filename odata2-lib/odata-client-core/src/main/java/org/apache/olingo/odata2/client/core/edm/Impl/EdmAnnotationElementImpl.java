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

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;

/**
 * Objects of this class represent AnnotationElement
 *
 */
public class EdmAnnotationElementImpl implements EdmAnnotationElement {

  List<EdmAnnotationElement> childElements;
  List<EdmAnnotationAttribute> attributes; 
  private String namespace;
  private String prefix;
  private String name;
  private String text;

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setText(String text) {
    this.text = text;
  }
 
  public void setChildElements(List<EdmAnnotationElement> childElements) {
    this.childElements = childElements;
  }
  public void setAttributes(List<EdmAnnotationAttribute> attributes) {
    this.attributes = attributes;
  }
  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  @Override
  public String getPrefix() {
    return prefix;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public List<EdmAnnotationElement> getChildElements() {
    return childElements;
  }

  @Override
  public List<EdmAnnotationAttribute> getAttributes() {
    return attributes;
  }
  
  @Override
  public String toString() {
    return namespace + Edm.DELIMITER + name;
  }
}
