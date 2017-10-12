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
package org.apache.olingo.odata2.core.edm.provider;


import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmParameter;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.FunctionImportParameter;
import org.apache.olingo.odata2.api.edm.provider.ReturnType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  
 */
public class EdmFunctionImportImplProv extends EdmNamedImplProv implements EdmFunctionImport, EdmAnnotatable {

  private FunctionImport functionImport;
  private EdmEntityContainer edmEntityContainer;
  private Map<String, EdmParameter> edmParameters;
  private Map<String, FunctionImportParameter> parameters;
  private List<String> parametersList;
  private EdmAnnotations annotations;
  private EdmTyped edmReturnType;

  public EdmFunctionImportImplProv(final EdmImplProv edm, final FunctionImport functionImport,
      final EdmEntityContainer edmEntityContainer) throws EdmException {
    super(edm, functionImport.getName());
    this.functionImport = functionImport;
    this.edmEntityContainer = edmEntityContainer;

    buildFunctionImportParametersInternal();

    edmParameters = new HashMap<String, EdmParameter>();
  }

  private void buildFunctionImportParametersInternal() {
    parameters = new HashMap<String, FunctionImportParameter>();

    List<FunctionImportParameter> functionImportParameters = functionImport.getParameters();
    if (functionImportParameters != null) {
      for (FunctionImportParameter parameter : functionImportParameters) {
        this.parameters.put(parameter.getName(), parameter);
      }
    }
  }

  @Override
  public EdmParameter getParameter(final String name) throws EdmException {
    EdmParameter parameter = null;
    if (edmParameters.containsKey(name)) {
      parameter = edmParameters.get(name);
    } else {
      parameter = createParameter(name);
    }

    return parameter;
  }

  private EdmParameter createParameter(final String name) throws EdmException {
    EdmParameter edmParameter = null;
    if (parameters.containsKey(name)) {
      FunctionImportParameter parameter = parameters.get(name);
      edmParameter = new EdmParameterImplProv(edm, parameter);
      edmParameters.put(name, edmParameter);
    }
    return edmParameter;
  }

  @Override
  public List<String> getParameterNames() throws EdmException {
    if (parametersList == null) {
      parametersList = new ArrayList<String>();

      List<FunctionImportParameter> functionImportParameters = functionImport.getParameters();
      if(functionImportParameters != null) {
        for (FunctionImportParameter parameter : functionImportParameters) {
          parametersList.add(parameter.getName());
        }
      }
    }

    return parametersList;
  }

  @Override
  public EdmEntitySet getEntitySet() throws EdmException {
    return edmEntityContainer.getEntitySet(functionImport.getEntitySet());
  }

  @Override
  public String getHttpMethod() throws EdmException {
    return functionImport.getHttpMethod();
  }

  @Override
  public EdmTyped getReturnType() throws EdmException {
    if (edmReturnType == null) {
      final ReturnType returnType = functionImport.getReturnType();
      if (returnType != null) {
        edmReturnType =
            new EdmTypedImplProv(edm, functionImport.getName(), returnType.getTypeName(), returnType.getMultiplicity());
      }
    }
    return edmReturnType;
  }

  @Override
  public EdmEntityContainer getEntityContainer() throws EdmException {
    return edmEntityContainer;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    if (annotations == null) {
      annotations =
          new EdmAnnotationsImplProv(functionImport.getAnnotationAttributes(), functionImport.getAnnotationElements());
    }
    return annotations;
  }

  @Override
  public EdmMapping getMapping() throws EdmException {
    return functionImport.getMapping();
  }
}
