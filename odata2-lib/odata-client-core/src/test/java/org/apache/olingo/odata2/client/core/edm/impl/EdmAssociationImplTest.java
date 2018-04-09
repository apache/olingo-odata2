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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.client.core.edm.EdmMetadataAssociationEnd;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAnnotationsImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAssociationEndImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAssociationImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmDocumentationImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmReferentialConstraintImpl;
import org.junit.Test;

public class EdmAssociationImplTest {

  @Test
  public void associationTest() throws EdmException {
    EdmAssociationImpl association= new EdmAssociationImpl();
    ArrayList<EdmMetadataAssociationEnd> ends = new ArrayList<EdmMetadataAssociationEnd>();
    ends.add(new EdmAssociationEndImpl());
    ends.add(new EdmAssociationEndImpl());
    association.setAnnotations(new EdmAnnotationsImpl());
    association.setAssociationEnds(ends);
    association.setDocumentation(new EdmDocumentationImpl());
    association.setEdm(new EdmImpl());
    association.setName("name");
    association.setNamespace("namespace");
    association.setReferentialConstraint(new EdmReferentialConstraintImpl());
    assertNotNull(association);
    assertNotNull(association.getAnnotations());
    assertNotNull(association.getAssociationEnds());
    assertNotNull(association.getDocumentation());
    assertNull(association.getEnd("role"));
    assertNotNull(association.getEnd1());
    assertNotNull(association.getEnd2());
    assertNull(association.getEndMultiplicity("role"));
    assertNotNull(association.getKind());
    assertNotNull(association.getName());
    assertNotNull(association.getNamespace());
    assertNotNull(association.getReferentialConstraint());
  }
}
