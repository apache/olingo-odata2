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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.odata2.api.annotation.edm.EdmComplexEntity;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *
 */
public class AnnotationHelper {
  
  public static final class ODataAnnotationException extends ODataException {
    public ODataAnnotationException(String message) {
      super(message);
    }
  }
  
  public Class<?> getFieldTypeForProperty(Object instance, String propertyName) throws ODataAnnotationException {
    if (instance == null) {
      return null;
    }
    
    Field field = getFieldForPropertyName(instance, propertyName, instance.getClass(), true);
    if(field == null) {
      throw new ODataAnnotationException("No field for property '" + propertyName 
              + "' found at class '" + instance.getClass() + "'.");
    }
    return field.getType();
  }

  public Object getValueForProperty(Object instance, String propertyName) throws ODataAnnotationException {
    if (instance == null) {
      return null;
    }
    
    Field field = getFieldForPropertyName(instance, propertyName, instance.getClass(), true);
    if(field == null) {
      throw new ODataAnnotationException("No field for property '" + propertyName 
              + "' found at class '" + instance.getClass() + "'.");
    }
    return getFieldValue(instance, field);
  }

  public void setValueForProperty(Object instance, String propertyName, Object propertyValue) {
    if (instance != null) {
      Field field = getFieldForPropertyName(instance, propertyName, instance.getClass(), true);
      if(field != null) {
        setFieldValue(instance, field, propertyValue);
      }
    }
  }

//  private Object getValueForPropertyName(Object instance, String propertyName, 
//      Class<?> resultClass, boolean inherited) {
//    if (instance == null) {
//      return null;
//    }
//
//    Field[] fields = resultClass.getDeclaredFields();
//    for (Field field : fields) {
//      EdmProperty property = field.getAnnotation(EdmProperty.class);
//      if (property != null) {
//        if(property.name().equals(propertyName)) {
//          return getFieldValue(instance, field);
//        } else if(field.getName().equals(propertyName)) {
//          return getFieldValue(instance, field);
//        }
//      }
//    }
//
//    Class<?> superClass = resultClass.getSuperclass();
//    if (inherited && superClass != Object.class) {
//      return getValueForPropertyName(instance, propertyName, superClass, true);
//    }
//
//    return null;
//  }


  private Field getFieldForPropertyName(Object instance, String propertyName, 
      Class<?> resultClass, boolean inherited) {
    if (instance == null) {
      return null;
    }

    Field[] fields = resultClass.getDeclaredFields();
    for (Field field : fields) {
      EdmProperty property = field.getAnnotation(EdmProperty.class);
      if (property != null) {
        if(property.name().isEmpty() && getCanonicalName(field).equals(propertyName)) {
          return field;
        } else if(property.name().equals(propertyName)) {
          return field;
        }
      }
    }

    Class<?> superClass = resultClass.getSuperclass();
    if (inherited && superClass != Object.class) {
      return getFieldForPropertyName(instance, propertyName, superClass, true);
    }

    return null;
  }

  
  public Object getValueForField(Object instance, String fieldName, Class<? extends Annotation> annotation) {
    if (instance == null) {
      return null;
    }
    return getValueForField(instance, fieldName, instance.getClass(), annotation, true);
  }

  public Object getValueForField(Object instance, Class<? extends Annotation> annotation) {
    if (instance == null) {
      return null;
    }
    return getValueForField(instance, instance.getClass(), annotation, true);
  }

  private Object getValueForField(Object instance, Class<?> resultClass,
      Class<? extends Annotation> annotation, boolean inherited) {
    return getValueForField(instance, null, resultClass, annotation, inherited);
  }

  public Map<String, Object> getValueForAnnotatedFields(Object instance,
          Class<? extends Annotation> annotation) {
    return getValueForAnnotatedFields(instance, instance.getClass(), annotation, true);
  }
    
  private Map<String, Object> getValueForAnnotatedFields(Object instance, Class<?> resultClass,
    Class<? extends Annotation> annotation, boolean inherited) {
    if (instance == null) {
      return null;
    }

    Field[] fields = resultClass.getDeclaredFields();
    Map<String, Object> fieldName2Value = new HashMap<String, Object>();
    
    for (Field field : fields) {
      if (field.getAnnotation(annotation) != null) {
        Object value = getFieldValue(instance, field);
        fieldName2Value.put(field.getName(), value);
      }
    }

    Class<?> superClass = resultClass.getSuperclass();
    if (inherited && superClass != Object.class) {
      Map<String, Object> tmp = getValueForAnnotatedFields(instance, superClass, annotation, true);
      fieldName2Value.putAll(tmp);
    }

    return fieldName2Value;
  }

  public void setValuesToAnnotatedFields(Map<String, Object> fieldName2Value, Object instance, 
          Class<? extends Annotation> annotation) {
    List<Field> fields = getAnnotatedFields(instance, instance.getClass(), annotation, true);
    
    // XXX: refactore
    for (Field field : fields) {
      final String canonicalName = getCanonicalName(field);
      if(fieldName2Value.containsKey(canonicalName)) {
        Object value = fieldName2Value.get(canonicalName);
        setFieldValue(instance, field, value);
      }
    }
  }
          
  public List<Field> getAnnotatedFields(Object instance, Class<? extends Annotation> annotation) {
    return getAnnotatedFields(instance, instance.getClass(), annotation, true);
  } 
  
  /**
   * 
   * @param instance
   * @param resultClass
   * @param annotation
   * @param inherited
   * @return 
   */
  private List<Field> getAnnotatedFields(Object instance, Class<?> resultClass,
    Class<? extends Annotation> annotation, boolean inherited) {
    if (instance == null) {
      return null;
    }

    Field[] fields = resultClass.getDeclaredFields();
    List<Field> annotatedFields = new ArrayList<Field>();
    
    for (Field field : fields) {
      if (field.getAnnotation(annotation) != null) {
        annotatedFields.add(field);
      }
    }

    Class<?> superClass = resultClass.getSuperclass();
    if (inherited && superClass != Object.class) {
      List<Field> tmp = getAnnotatedFields(instance, superClass, annotation, true);
      annotatedFields.addAll(tmp);
    }

    return annotatedFields;
  }

  private Object getValueForField(Object instance, String fieldName, Class<?> resultClass,
      Class<? extends Annotation> annotation, boolean inherited) {
    if (instance == null) {
      return null;
    }

    Field[] fields = resultClass.getDeclaredFields();
    for (Field field : fields) {
      if (field.getAnnotation(annotation) != null
          && (fieldName == null || field.getName().equals(fieldName))) {
        return getFieldValue(instance, field);
      }
    }

    Class<?> superClass = resultClass.getSuperclass();
    if (inherited && superClass != Object.class) {
      return getValueForField(instance, fieldName, superClass, annotation, true);
    }

    return null;
  }

  private Object getFieldValue(Object instance, Field field) {
    try {
      boolean access = field.isAccessible();
      field.setAccessible(true);
      Object value = field.get(instance);
      field.setAccessible(access);
      return value;
    } catch (IllegalArgumentException ex) { // should never happen
      throw new ODataRuntimeException(ex);
    } catch (IllegalAccessException ex) { // should never happen
      throw new ODataRuntimeException(ex);
    }
  }

  private void setFieldValue(Object instance, Field field, Object propertyValue) {
    try {
      boolean access = field.isAccessible();
      field.setAccessible(true);
      field.set(instance, propertyValue);
      field.setAccessible(access);
    } catch (IllegalArgumentException ex) { // should never happen
      throw new ODataRuntimeException(ex);
    } catch (IllegalAccessException ex) { // should never happen
      throw new ODataRuntimeException(ex);
    }
  }

  
  public boolean isEdmAnnotated(Object object) {
    if (object == null) {
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
    if (content == null || content.isEmpty()) {
      return content;
    }
    return content.substring(0, 1).toUpperCase(Locale.ENGLISH) + content.substring(1);
  }
}
