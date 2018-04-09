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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmParameter;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.client.api.edm.EdmDocumentation;

/**
 *  Objects of this class represent EdmFunctionImport
 */
public class EdmFunctionImportImpl extends EdmNamedImpl implements EdmFunctionImport, EdmAnnotatable {

  private EdmEntityContainer edmEntityContainer;
  private Map<String, EdmParameter> edmParameters;
  private Map<String, ArrayList<EdmFunctionImportParameter>> parameters;
  private List<String> parametersList;
  private EdmAnnotations annotations;
  private EdmTyped edmReturnType;
  private String entitySet;
  private String httpMethod;
  private EdmMapping mapping;
  private EdmDocumentation documentation;

  public EdmTyped getEdmReturnType() {
    return edmReturnType;
  }

  public void setEdmReturnType(EdmTyped edmReturnType) {
    this.edmReturnType = edmReturnType;
  }

  public EdmDocumentation getDocumentation() {
    return documentation;
  }

  public void setDocumentation(EdmDocumentation documentation) {
    this.documentation = documentation;
  }

  public void setMapping(EdmMapping mapping) {
    this.mapping = mapping;
  }

  /**
   * @param returnType the returnType to set
   */
  public void setReturnType(EdmTyped returnType) {
    this.edmReturnType = returnType;
  }

  /**
   * @param edmEntitySet the entitySet to set
   */
  public void setEntitySet(String edmEntitySet) {
    this.entitySet = edmEntitySet;
  }

  /**
   * @param httpMethod the httpMethod to set
   */
  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public EdmEntityContainer getEdmEntityContainer() {
    return edmEntityContainer;
  }

  public void setEdmEntityContainer(EdmEntityContainer edmEntityContainer) {
    this.edmEntityContainer = edmEntityContainer;
  }

  public Map<String, EdmParameter> getEdmParameters() {
    return edmParameters;
  }

  public void setEdmParameters(Map<String, EdmParameter> edmParameters) {
    this.edmParameters = edmParameters;
  }

  public Map<String, ArrayList<EdmFunctionImportParameter>> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, ArrayList<EdmFunctionImportParameter>> parameters) {
    this.parameters = parameters;
  }

  public List<String> getParametersList() {
    return parametersList;
  }

  public void setParametersList(List<String> parametersList) {
    this.parametersList = parametersList;
  }

  public void setAnnotations(EdmAnnotations annotations) {
    this.annotations = annotations;
  }

  @Override
  public EdmParameter getParameter(final String name) throws EdmException {
    for (Entry<String, EdmParameter> param : edmParameters.entrySet()) {
      if (param.getKey().equalsIgnoreCase(name)) {
        return param.getValue();
      }
    }
    return null;
  }

  @Override
  public List<String> getParameterNames() throws EdmException {
    return parametersList;
  }

  @Override
  public EdmEntitySet getEntitySet() throws EdmException {
    return edmEntityContainer.getEntitySet(entitySet);
  }

  @Override
  public String getHttpMethod() throws EdmException {
    return this.httpMethod;
  }

  @Override
  public EdmTyped getReturnType() throws EdmException {
    return edmReturnType;
  }

  @Override
  public EdmEntityContainer getEntityContainer() throws EdmException {
    return edmEntityContainer;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return annotations;
  }

  @Override
  public EdmMapping getMapping() throws EdmException {
    return mapping;
  }  
  @Override
  public String toString() {
    return String.format(name);
  }

}
