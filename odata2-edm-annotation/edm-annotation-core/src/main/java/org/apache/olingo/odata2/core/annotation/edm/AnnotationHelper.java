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
package org.apache.olingo.odata2.core.annotation.edm;

import java.lang.reflect.Field;
import java.util.Locale;
import org.apache.olingo.odata2.api.annotation.edm.EdmComplexEntity;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;

/**
 *
 */
public class AnnotationHelper {

  public boolean isEdmAnnotated(Object object) {
    if(object == null) {
      return false;
    }
    return isEdmAnnotated(object.getClass());
  }
  
  public boolean isEdmAnnotated(Class<?> clazz) {
    if (clazz == null) {
      return false;
    } else {
      final boolean isEntity = null != clazz.getAnnotation(EdmEntityType.class);
      final boolean isComplexEntity = null != clazz.getAnnotation(EdmComplexEntity.class);
      return isEntity || isComplexEntity;
    }
  }
  
  public String getCanonicalName(Field field) {
    return firstCharToUpperCase(field.getName());
  }
  
  public String getCanonicalName(Class<?> clazz) {
    return firstCharToUpperCase(clazz.getSimpleName());
  }
  
  private String firstCharToUpperCase(String content) {
    if(content == null || content.isEmpty()) {
      return content;
    }
    return content.substring(0, 1).toUpperCase(Locale.ENGLISH) + content.substring(1);
  }
}
