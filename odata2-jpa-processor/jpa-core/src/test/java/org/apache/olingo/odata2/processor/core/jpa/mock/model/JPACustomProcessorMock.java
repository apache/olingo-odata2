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
package org.apache.olingo.odata2.processor.core.jpa.mock.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport.ReturnType;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport.ReturnType.Type;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;

public class JPACustomProcessorMock {

  public static final String className = "JPACustomProcessorMock";
  public static final String edmName = "JPACustomProcessor";

  @EdmFunctionImport(name = "Method1", entitySet = "MockSet", returnType = @ReturnType(type = Type.ENTITY,
      isCollection = true))
  public List<JPACustomProcessorMock> method1(@EdmFunctionImportParameter(name = "Param1", facets = @EdmFacets(
      nullable = true,
      maxLength = 2)) final String param1, final int param2, @EdmFunctionImportParameter(name = "Param3",
      facets = @EdmFacets(precision = 10, scale = 2)) final double param3) {
    return new ArrayList<JPACustomProcessorMock>();
  }

  @EdmFunctionImport(name = "Method2", entitySet = "MockSet", returnType = @ReturnType(type = Type.ENTITY,
      isCollection = true))
  public List<JPACustomProcessorMock> method2(
      @EdmFunctionImportParameter(facets = @EdmFacets(maxLength = 2), name = "Param2") final String param2) {
    return new ArrayList<JPACustomProcessorMock>();
  }

  @EdmFunctionImport(returnType = @ReturnType(type = Type.SIMPLE))
  public int method3(@EdmFunctionImportParameter(name = "Param3") final String param3) {
    return 0;
  }

  @EdmFunctionImport(returnType = @ReturnType(type = Type.ENTITY,
      isCollection = false), entitySet = "MockSet")
  public JPACustomProcessorMock method7() {
    return null;
  }

  @EdmFunctionImport(returnType = @ReturnType(type = Type.COMPLEX,
      isCollection = false))
  public JPACustomProcessorMock method9() {
    return null;
  }

  @EdmFunctionImport(returnType = @ReturnType(type = Type.COMPLEX,
      isCollection = true))
  public List<JPACustomProcessorMock> method10() {
    return null;
  }

}
