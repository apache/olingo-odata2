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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

import jakarta.persistence.JoinColumns;

public class JPAJavaMemberMock implements Member, AnnotatedElement, Annotation {

  @Override
  public Class<?> getDeclaringClass() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public int getModifiers() {
    return 0;
  }

  @Override
  public boolean isSynthetic() {
    return false;
  }

  @Override
  public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
    return false;
  }

  @Override
  public Annotation[] getAnnotations() {
    return null;
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    return null;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return JoinColumns.class;
  }

  @Override
  public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
    return null;
  }

}
