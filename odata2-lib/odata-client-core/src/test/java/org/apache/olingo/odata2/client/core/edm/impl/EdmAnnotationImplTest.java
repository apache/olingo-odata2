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
package org.apache.olingo.odata2.client.core.edm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAnnotationAttributeImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAnnotationElementImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAnnotationsImpl;
import org.junit.Test;

public class EdmAnnotationImplTest {

  @Test
  public void annotationAttributeTest(){
    EdmAnnotationAttributeImpl annotation = new EdmAnnotationAttributeImpl();
    annotation.setName("name");
    annotation.setNamespace("namespace");
    annotation.setPrefix("prefix");
    annotation.setText("text");
    assertEquals("name", annotation.getName());
    assertEquals("namespace", annotation.getNamespace());
    assertEquals("prefix", annotation.getPrefix());
    assertEquals("text", annotation.getText());
  }
  
  @Test
  public void annotationElementTest(){
    EdmAnnotationElementImpl annotation = new EdmAnnotationElementImpl();
    annotation.setName("name");
    annotation.setNamespace("namespace");
    annotation.setPrefix("prefix");
    annotation.setText("text");
    annotation.setAttributes(null);
    annotation.setChildElements(null);
    assertEquals("name", annotation.getName());
    assertEquals("namespace", annotation.getNamespace());
    assertEquals("prefix", annotation.getPrefix());
    assertEquals("text", annotation.getText());
    assertNull(annotation.getAttributes());
    assertNull(annotation.getChildElements());
  }
  
  @Test
  public void annotationTest(){
    EdmAnnotationsImpl annotation = new EdmAnnotationsImpl();
    EdmAnnotationElementImpl annotationElement = new EdmAnnotationElementImpl();
    EdmAnnotationAttributeImpl annotationAttribute = new EdmAnnotationAttributeImpl();
    List<EdmAnnotationAttribute> annotationAttributeList = 
        new ArrayList<EdmAnnotationAttribute>();  
    List<EdmAnnotationElement> annotationElementList = 
        new ArrayList<EdmAnnotationElement>();        
    annotationAttributeList.add(annotationAttribute);
    annotationElementList.add(annotationElement);
    annotationAttribute.setName("name");
    annotationAttribute.setNamespace("namespace");
    annotationAttribute.setPrefix("prefix");
    annotationAttribute.setText("text");
    annotationElement.setName("name");
    annotationElement.setNamespace("namespace");
    annotationElement.setPrefix("prefix");
    annotationElement.setText("text");
    annotationElement.setAttributes(annotationAttributeList);
    annotationElement.setChildElements(null);
    annotation.setAnnotationAttributes(annotationAttributeList);
    annotation.setAnnotationElements(annotationElementList);
    assertNotNull(annotation.getAnnotationAttribute("name", "namespace"));
    assertNotNull(annotation.getAnnotationAttributes());
    assertNotNull(annotation.getAnnotationElement("name", "namespace"));
    assertNotNull(annotation.getAnnotationElements());
  }
}
