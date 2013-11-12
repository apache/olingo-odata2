/**
 * *****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *****************************************************************************
 */
package org.apache.olingo.odata2.core.annotation.edm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.olingo.odata2.api.annotation.edm.EdmComplexEntity;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.core.annotation.ds.DataStore;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *
 */
public class AnnotationHelper {

  public boolean keyMatch(Object firstInstance, Object secondInstance) {
    if (firstInstance == null || secondInstance == null) {
      return false;
    } else if (firstInstance.getClass() != secondInstance.getClass()) {
      return false;
    }

    Map<String, Object> firstKeyFields = getValueForAnnotatedFields(firstInstance, EdmKey.class);
    Map<String, Object> secondKeyFields = getValueForAnnotatedFields(secondInstance, EdmKey.class);

    if (firstKeyFields.size() != secondKeyFields.size()) {
      return false;
    } else {
      Set<Map.Entry<String, Object>> entries = firstKeyFields.entrySet();
      for (Map.Entry<String, Object> entry : entries) {
        Object firstKey = entry.getValue();
        Object secondKey = secondKeyFields.get(entry.getKey());
        if (!isEqual(firstKey, secondKey)) {
          return false;
        }
      }
      return true;
    }
  }

  private boolean isEqual(Object firstKey, Object secondKey) {
    if (firstKey == null) {
      if (secondKey == null) {
        return true;
      } else {
        return secondKey.equals(firstKey);
      }
    } else {
      return firstKey.equals(secondKey);
    }
  }

  public static final class ODataAnnotationException extends ODataException {

    public ODataAnnotationException(String message) {
      super(message);
    }
  }
  
  public <T> T setKeyFields(T instance, Object[] keyValues) {
    List<Field> fields = getAnnotatedFields(instance, EdmKey.class);
    if (fields.size() != keyValues.length) {
      throw new IllegalStateException("Wrong amount of key properties. Expected read keys = "
              + fields + " given key predicates = " + Arrays.toString(keyValues));
    }

    String propertyName = getCanonicalName(fields.get(0));
    setValueForProperty(instance, propertyName, keyValues[0]);

    return instance;
  }

  
  public Field getCommonNavigationFieldFromTarget(Class<?> sourceClass, Class<?> targetClass) {
    List<Field> sourceFields = getAnnotatedFields(sourceClass, EdmNavigationProperty.class);
    List<Field> targetFields = getAnnotatedFields(targetClass, EdmNavigationProperty.class);

    for (Field targetField : targetFields) {
      for (Field sourcField : sourceFields) {
        EdmNavigationProperty sourceNav = sourcField.getAnnotation(EdmNavigationProperty.class);
        EdmNavigationProperty targetNav = targetField.getAnnotation(EdmNavigationProperty.class);
        if (sourceNav.relationship().equals(targetNav.relationship())) {
          return targetField;
        }
      }
    }
    return null;
  }


  public Class<?> getFieldTypeForProperty(Object instance, String propertyName) throws ODataAnnotationException {
    if (instance == null) {
      return null;
    }

    Field field = getFieldForPropertyName(instance, propertyName, instance.getClass(), true);
    if (field == null) {
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
    if (field == null) {
      throw new ODataAnnotationException("No field for property '" + propertyName
              + "' found at class '" + instance.getClass() + "'.");
    }
    return getFieldValue(instance, field);
  }

  public void setValueForProperty(Object instance, String propertyName, Object propertyValue) {
    if (instance != null) {
      Field field = getFieldForPropertyName(instance, propertyName, instance.getClass(), true);
      if (field != null) {
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
        if (property.name().isEmpty() && getCanonicalName(field).equals(propertyName)) {
          return field;
        } else if (property.name().equals(propertyName)) {
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
    List<Field> fields = getAnnotatedFields(instance, annotation);

    // XXX: refactore
    for (Field field : fields) {
      final String canonicalName = getCanonicalName(field);
      if (fieldName2Value.containsKey(canonicalName)) {
        Object value = fieldName2Value.get(canonicalName);
        setFieldValue(instance, field, value);
      }
    }
  }

  public List<Field> getAnnotatedFields(Object instance, Class<? extends Annotation> annotation) {
    if(instance == null) {
      return null;
    }
    return getAnnotatedFields(instance.getClass(), annotation, true);
  }

  public List<Field> getAnnotatedFields(Class<?> fieldClass, Class<? extends Annotation> annotation) {
    return getAnnotatedFields(fieldClass, annotation, true);
  }

  /**
   *
   * @param instance
   * @param resultClass
   * @param annotation
   * @param inherited
   * @return
   */
  private List<Field> getAnnotatedFields(Class<?> resultClass,
          Class<? extends Annotation> annotation, boolean inherited) {
    if (resultClass == null) {
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
      List<Field> tmp = getAnnotatedFields(superClass, annotation, true);
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
      if (propertyValue != null
              && field.getType() != propertyValue.getClass()
              && propertyValue.getClass() == String.class) {
        propertyValue = convert(field, (String) propertyValue);
      }
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

  public Object convert(Field field, String propertyValue) {
    Class fieldClass = field.getType();
    try {
      EdmProperty property = field.getAnnotation(EdmProperty.class);
      EdmSimpleTypeKind type = property.type();
      return type.getEdmSimpleTypeInstance().valueOfString(propertyValue,
              EdmLiteralKind.DEFAULT, null, fieldClass);
    } catch (EdmSimpleTypeException ex) {
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
