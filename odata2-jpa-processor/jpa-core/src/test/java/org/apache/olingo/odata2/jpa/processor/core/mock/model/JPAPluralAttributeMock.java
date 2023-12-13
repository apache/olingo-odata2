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

import java.lang.reflect.Member;
import java.util.ArrayList;

import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.Type;

public class JPAPluralAttributeMock implements PluralAttribute<Object, ArrayList<String>, String> {

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public jakarta.persistence.metamodel.Attribute.PersistentAttributeType getPersistentAttributeType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ManagedType<Object> getDeclaringType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<ArrayList<String>> getJavaType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Member getJavaMember() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isAssociation() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public jakarta.persistence.metamodel.Bindable.BindableType getBindableType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<String> getBindableJavaType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public jakarta.persistence.metamodel.PluralAttribute.CollectionType getCollectionType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Type<String> getElementType() {
    // TODO Auto-generated method stub
    return null;
  }

}