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
package org.apache.olingo.odata2.jpa.processor.core.mock.model;

import java.util.List;

import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport.ReturnType;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport.ReturnType.Type;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;

public class JPACustomProcessorNegativeMock {

  @EdmFunctionImport(name = "Method5", entitySet = "MockSet", returnType = @ReturnType(type = Type.ENTITY,
      isCollection = false))
  public List<JPACustomProcessorNegativeMock> method5() {
    return null;
  }

  @EdmFunctionImport(entitySet = "MockSet", returnType = @ReturnType(type = Type.SIMPLE, isCollection = true))
  public void method6() {
    return;
  }

  @EdmFunctionImport(returnType = @ReturnType(type = Type.SIMPLE, isCollection = true), entitySet = "MockSet")
  public JPACustomProcessorNegativeMock method8() {
    return null;
  }

  @EdmFunctionImport(returnType = @ReturnType(type = Type.COMPLEX))
  public JPACustomProcessorNegativeMock method11() {
    return null;
  }

  @EdmFunctionImport(returnType = @ReturnType(type = Type.SIMPLE))
  public JPACustomProcessorMock method12() {
    return null;
  }

  @EdmFunctionImport(returnType = @ReturnType(type = Type.SIMPLE))
  public int method13(@EdmFunctionImportParameter(name = "") final int y) {
    return 0;
  }

  @EdmFunctionImport(returnType = @ReturnType(type = Type.SIMPLE))
  public void method16(@EdmFunctionImportParameter(name = "") final int y) {
    return;
  }

  @EdmFunctionImport(returnType = @ReturnType(type = Type.COMPLEX))
  public void method17(@EdmFunctionImportParameter(name = "") final int y) {
    return;
  }
}
