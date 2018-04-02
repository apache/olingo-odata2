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

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmDocumentationImpl;
import org.junit.Test;

public class EdmDocumentationImplTest {

  @Test
  public void testDocumentation(){
    EdmDocumentationImpl doc = new EdmDocumentationImpl();
    List<EdmAnnotationAttribute> annotationAttributes = 
        new ArrayList<EdmAnnotationAttribute>();
    doc.setAnnotationAttributes(annotationAttributes);
    List<EdmAnnotationElement> annotationElements = 
        new ArrayList<EdmAnnotationElement>();
    doc.setAnnotationElements(annotationElements );
    doc.setLongDescription("longDescription");
    doc.setSummary("summary");
    assertNotNull(doc.getAnnotationAttributes());
    assertNotNull(doc.getAnnotationElements());
    assertNotNull(doc.getLongDescription());
    assertNotNull(doc.getClass());
    assertEquals(doc.getLongDescription(),"longDescription");
    assertEquals(doc.getSummary(), "summary");
  }

}
