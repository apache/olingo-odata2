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

import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type;

public class JPASingularAttributeMock<X, T> implements SingularAttribute<X, T> {

  @Override
  public ManagedType<X> getDeclaringType() {
    return null;
  }

  @Override
  public Member getJavaMember() {
    return null;
  }

  @Override
  public Class<T> getJavaType() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public jakarta.persistence.metamodel.Attribute.PersistentAttributeType getPersistentAttributeType() {
    return null;
  }

  @Override
  public boolean isAssociation() {
    return false;
  }

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public Class<T> getBindableJavaType() {
    return null;
  }

  @Override
  public jakarta.persistence.metamodel.Bindable.BindableType getBindableType() {
    return null;
  }

  @Override
  public Type<T> getType() {
    return null;
  }

  @Override
  public boolean isId() {
    return false;
  }

  @Override
  public boolean isOptional() {
    return false;
  }

  @Override
  public boolean isVersion() {
    return false;
  }

}
