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

import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;

/**
 * Objects of this class represent ComplexType
 *
 */
public class EdmComplexTypeImpl extends EdmStructuralTypeImpl implements EdmComplexType{

  private boolean isAbstract;

  private FullQualifiedName baseType;
  private EdmMapping mapping;
  
  public EdmMapping getMapping() {
    return mapping;
  }
  public void setMapping(EdmMapping mapping) {
    this.mapping = mapping;
  }
  public void setAbstract(boolean isAbstract) {
    this.isAbstract = isAbstract;
  }
  @Override
  public EdmComplexType getBaseType() throws EdmException {
    if(edmBaseType!=null){
      return (EdmComplexTypeImpl) edmBaseType;
    } else {
      return null;
    }
  }
  public FullQualifiedName getEdmBaseTypeName() {
    return baseType;
  }
  public void setBaseTypeName(FullQualifiedName baseType) {
    this.baseType = baseType;
  }
  
  public boolean isAbstract() {
    return isAbstract;
  }
  
  @Override
  public String toString() {
    return name;
  }
  
  @Override
  public EdmTypeKind getKind() {
    return EdmTypeKind.COMPLEX;
  }
}
