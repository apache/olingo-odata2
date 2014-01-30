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
 * ****************************************************************************
 */
package org.apache.olingo.odata2.annotation.processor.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.odata2.api.annotation.edm.EdmComplexType;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty.Multiplicity;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *
 */
public class AnnotationHelper {

  public static final String DEFAULT_CONTAINER_NAME = "DefaultContainer";

  /**
   * Compare keys of both instances.
   * 
   * @param firstInstance
   * @param secondInstance
   * @return
   */
  public boolean keyMatch(final Object firstInstance, final Object secondInstance) {
    if (firstInstance == null || secondInstance == null) {
      return false;
    } else if (firstInstance.getClass() != secondInstance.getClass()) {
      return false;
    }

    Map<String, Object> firstKeyFields = getValueForAnnotatedFields(firstInstance, EdmKey.class);
    Map<String, Object> secondKeyFields = getValueForAnnotatedFields(secondInstance, EdmKey.class);
    if(firstKeyFields.isEmpty() && secondKeyFields.isEmpty()) {
      throw new ODataRuntimeException("Both object instances does not have EdmKey fields defined ["
          + "firstClass=" + firstInstance.getClass().getName()
          + " secondClass=" + secondInstance.getClass().getName() + "].");
    }

    return keyValuesMatch(firstKeyFields, secondKeyFields);
  }

  /**
   * Compare keys of instance with key values in map.
   * 
   * @param instance
   * @param keyName2Value
   * @return
   */
  public boolean keyMatch(final Object instance, final Map<String, Object> keyName2Value) {
    Map<String, Object> instanceKeyFields = getValueForAnnotatedFields(instance, EdmKey.class);
    return keyValuesMatch(instanceKeyFields, keyName2Value);
  }

  private boolean keyValuesMatch(final Map<String, Object> firstKeyValues, final Map<String, Object> secondKeyValues) {
    if (firstKeyValues.size() != secondKeyValues.size()) {
      return false;
    } else if(firstKeyValues.isEmpty()) {
      throw new ODataRuntimeException("No keys given for key value matching.");
    } else {
      Set<Map.Entry<String, Object>> entries = firstKeyValues.entrySet();
      for (Map.Entry<String, Object> entry : entries) {
        Object firstKey = entry.getValue();
        Object secondKey = secondKeyValues.get(entry.getKey());
        if (!isEqual(firstKey, secondKey)) {
          return false;
        }
      }
      return true;
    }
  }

  private boolean isEqual(final Object firstKey, final Object secondKey) {
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

  public String extractEntitTypeName(final EdmNavigationProperty enp, final Class<?> fallbackClass) {
    Class<?> entityTypeClass = enp.toType();
    return extractEntityTypeName(entityTypeClass == Object.class ? fallbackClass : entityTypeClass);
  }

  public String extractEntitTypeName(final EdmNavigationProperty enp, final Field field) {
    Class<?> entityTypeClass = enp.toType();
    if (entityTypeClass == Object.class) {
      Class<?> toClass = field.getType();
      return extractEntityTypeName((toClass.isArray() || Collection.class.isAssignableFrom(toClass) ?
          (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0] : toClass));
    } else {
      return extractEntityTypeName(entityTypeClass);
    }
  }

  /**
   * Returns <code>NULL</code> if given class is not annotated. If annotated the set entity type name is returned and if
   * no name is set the default name is generated from the simple class name.
   * 
   * @param annotatedClass
   * @return
   */
  public String extractEntityTypeName(final Class<?> annotatedClass) {
    return extractTypeName(annotatedClass, EdmEntityType.class);
  }

  /**
   * Returns <code>NULL</code> if given class is not annotated.
   * If annotated the entity set name is returned and if
   * no name is set a default name is generated based on the simple class name.
   * 
   * @param annotatedClass
   * @return
   */
  public String extractEntitySetName(final Class<?> annotatedClass) {
    if (annotatedClass == Object.class) {
      return null;
    }
    EdmEntitySet entitySet = annotatedClass.getAnnotation(EdmEntitySet.class);
    if (entitySet == null) {
      return null;
    }

    String name = entitySet.name();
    if (name.isEmpty()) {
      return getCanonicalName(annotatedClass) + "Set";
    }
    return name;
  }

  public FullQualifiedName extractEntityTypeFqn(final EdmEntityType type, final Class<?> annotatedClass) {
    if (type.namespace().isEmpty()) {
      return new FullQualifiedName(generateNamespace(annotatedClass), extractEntityTypeName(annotatedClass));
    }
    return new FullQualifiedName(type.namespace(), extractEntityTypeName(annotatedClass));
  }

  public FullQualifiedName extractEntityTypeFqn(final Class<?> annotatedClass) {
    EdmEntityType type = annotatedClass.getAnnotation(EdmEntityType.class);
    if (type == null) {
      return null;
    }
    return extractEntityTypeFqn(type, annotatedClass);
  }

  public FullQualifiedName extractComplexTypeFqn(final Class<?> annotatedClass) {
    EdmComplexType type = annotatedClass.getAnnotation(EdmComplexType.class);
    if (type == null) {
      return null;
    }
    return extractComplexTypeFqn(type, annotatedClass);
  }

  public FullQualifiedName extractComplexTypeFqn(final EdmComplexType type, final Class<?> annotatedClass) {
    if (type.namespace().isEmpty()) {
      return new FullQualifiedName(generateNamespace(annotatedClass), extractComplexTypeName(annotatedClass));
    }
    return new FullQualifiedName(type.namespace(), extractComplexTypeName(annotatedClass));
  }

  public String extractComplexTypeName(final Class<?> annotatedClass) {
    return extractTypeName(annotatedClass, EdmComplexType.class);
  }

  public String generateNamespace(final Class<?> annotatedClass) {
    String packageName = annotatedClass.getPackage().getName();
    return packageName;
  }

  /**
   * 
   * 
   * @param <T> must be EdmEntityType or EdmComplexType annotation
   * @param annotatedClass
   * @param typeAnnotation
   * @return null if annotatedClass is not annotated or name set via annotation or generated via
   * {@link #getCanonicalName(java.lang.Class)}
   */
  private <T extends Annotation> String extractTypeName(final Class<?> annotatedClass, final Class<T> typeAnnotation) {
    if (annotatedClass == Object.class) {
      return null;
    }
    T type = annotatedClass.getAnnotation(typeAnnotation);
    if (type == null) {
      return null;
    }

    String name;
    if (typeAnnotation == EdmEntityType.class) {
      name = ((EdmEntityType) type).name();
    } else if (typeAnnotation == EdmComplexType.class) {
      name = ((EdmComplexType) type).name();
    } else {
      return null;
    }

    if (name.isEmpty()) {
      return getCanonicalName(annotatedClass);
    }
    return name;
  }

  /**
   * Get the set property name from an EdmProperty or EdmNavigationProperty annotation.
   * 
   * @param field
   * @return
   */
  public String getPropertyNameFromAnnotation(final Field field) {
    EdmProperty property = field.getAnnotation(EdmProperty.class);
    if (property == null) {
      EdmNavigationProperty navProperty = field.getAnnotation(EdmNavigationProperty.class);
      if (navProperty == null) {
        throw new EdmAnnotationException("Given field '" + field
            + "' has no EdmProperty or EdmNavigationProperty annotation.");
      }
      return navProperty.name();
    }
    return property.name();
  }

  public String getPropertyName(final Field field) {
    String propertyName = getPropertyNameFromAnnotation(field);
    if (propertyName.isEmpty()) {
      propertyName = getCanonicalName(field);
    }
    return propertyName;
  }

  public String extractFromRoleName(final EdmNavigationProperty enp, final Field field) {
    return getCanonicalRole(field.getDeclaringClass());
  }

  public String extractToRoleName(final EdmNavigationProperty enp, final Field field) {
    String role = enp.toRole();
    if (role.isEmpty()) {
      role = getCanonicalRole(
          field.getType().isArray() || Collection.class.isAssignableFrom(field.getType()) ?
              (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0] : field.getType());
    }
    return role;
  }

  public String getCanonicalRole(final Class<?> fallbackClass) {
    String toRole = extractEntityTypeName(fallbackClass);
    return "r_" + toRole;
  }

  public String extractRelationshipName(final EdmNavigationProperty enp, final Field field) {
    String relationshipName = enp.association();
    if (relationshipName.isEmpty()) {
      final String fromRole = extractFromRoleName(enp, field);
      final String toRole = extractToRoleName(enp, field);
      if (fromRole.compareTo(toRole) > 0) {
        relationshipName = toRole + "-" + fromRole;
      } else {
        relationshipName = fromRole + "-" + toRole;
      }
    }
    return relationshipName;
  }

  public EdmMultiplicity getMultiplicity(final EdmNavigationProperty enp, final Field field) {
    EdmMultiplicity multiplicity = mapMultiplicity(enp.toMultiplicity());
    final boolean isCollectionType = field.getType().isArray() || Collection.class.isAssignableFrom(field.getType());

    if (multiplicity == EdmMultiplicity.ONE && isCollectionType) {
      return EdmMultiplicity.MANY;
    }
    return multiplicity;
  }

  /**
   * Set key fields based on values in map.
   * If an key field is not available or <code>NULL</code> in the map
   * it will be not set as <code>NULL</code> at the instance object.
   * 
   * @param instance
   * @param keys
   * @return
   */
  public <T> T setKeyFields(final T instance, final Map<String, Object> keys) {
    List<Field> fields = getAnnotatedFields(instance, EdmKey.class);

    for (Field field : fields) {
      String propertyName = getPropertyName(field);
      Object keyValue = keys.get(propertyName);
      setValueForProperty(instance, propertyName, keyValue);
    }

    return instance;
  }

  public static final class ODataAnnotationException extends ODataException {
    private static final long serialVersionUID = 42L;

    public ODataAnnotationException(final String message) {
      super(message);
    }

    public ODataAnnotationException(final String message, final Exception cause) {
      super(message, cause);
    }
  }

  public class AnnotatedNavInfo {
    private final Field fromField;
    private final Field toField;
    private final EdmNavigationProperty fromNavigation;
    private final EdmNavigationProperty toNavigation;

    public AnnotatedNavInfo(final Field fromField, final Field toField, final EdmNavigationProperty fromNavigation,
        final EdmNavigationProperty toNavigation) {
      this.fromField = fromField;
      this.toField = toField;
      this.fromNavigation = fromNavigation;
      this.toNavigation = toNavigation;
    }

    public Field getFromField() {
      return fromField;
    }

    public Field getToField() {
      return toField;
    }

    public EdmMultiplicity getFromMultiplicity() {
      return getMultiplicity(toNavigation, toField);
    }

    public EdmMultiplicity getToMultiplicity() {
      return getMultiplicity(fromNavigation, fromField);
    }

    public boolean isBiDirectional() {
      return toNavigation != null;
    }
  }

  public AnnotatedNavInfo getCommonNavigationInfo(final Class<?> sourceClass, final Class<?> targetClass) {
    List<Field> sourceFields = getAnnotatedFields(sourceClass, EdmNavigationProperty.class);
    List<Field> targetFields = getAnnotatedFields(targetClass, EdmNavigationProperty.class);

    // first try via association name to get full navigation information
    for (Field sourceField : sourceFields) {
      final EdmNavigationProperty sourceNav = sourceField.getAnnotation(EdmNavigationProperty.class);
      final String sourceAssociation = extractRelationshipName(sourceNav, sourceField);
      for (Field targetField : targetFields) {
        final EdmNavigationProperty targetNav = targetField.getAnnotation(EdmNavigationProperty.class);
        final String targetAssociation = extractRelationshipName(targetNav, targetField);
        if (sourceAssociation.equals(targetAssociation)) {
          return new AnnotatedNavInfo(sourceField, targetField, sourceNav, targetNav);
        }
      }
    }

    // if nothing was found assume none bi-directinal navigation
    String targetEntityTypeName = extractEntityTypeName(targetClass);
    for (Field sourceField : sourceFields) {
      final EdmNavigationProperty sourceNav = sourceField.getAnnotation(EdmNavigationProperty.class);
      final String navTargetEntityName = extractEntitTypeName(sourceNav, sourceField);

      if (navTargetEntityName.equals(targetEntityTypeName)) {
        return new AnnotatedNavInfo(sourceField, null, sourceNav, null);
      }
    }

    return null;
  }

  public Class<?> getFieldTypeForProperty(final Class<?> clazz, final String propertyName)
      throws ODataAnnotationException {
    if (clazz == null) {
      return null;
    }

    Field field = getFieldForPropertyName(propertyName, clazz, true);
    if (field == null) {
      throw new ODataAnnotationException("No field for property '" + propertyName
          + "' found at class '" + clazz + "'.");
    }
    return field.getType();
  }

  public Class<?> getFieldTypeForProperty(final Object instance, final String propertyName)
      throws ODataAnnotationException {
    if (instance == null) {
      return null;
    }

    return getFieldTypeForProperty(instance.getClass(), propertyName);
  }

  public Object getValueForProperty(final Object instance, final String propertyName) throws ODataAnnotationException {
    if (instance == null) {
      return null;
    }

    Field field = getFieldForPropertyName(propertyName, instance.getClass(), true);
    if (field == null) {
      throw new ODataAnnotationException("No field for property '" + propertyName
          + "' found at class '" + instance.getClass() + "'.");
    }
    return getFieldValue(instance, field);
  }

  public void setValueForProperty(final Object instance, final String propertyName, final Object propertyValue) {
    if (instance != null) {
      Field field = getFieldForPropertyName(propertyName, instance.getClass(), true);
      if (field != null) {
        setFieldValue(instance, field, propertyValue);
      }
    }
  }

  private Field getFieldForPropertyName(final String propertyName,
      final Class<?> resultClass, final boolean inherited) {
    
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
      return getFieldForPropertyName(propertyName, superClass, true);
    }

    return null;
  }

  public Object getValueForField(final Object instance, final String fieldName,
      final Class<? extends Annotation> annotation) {
    if (instance == null) {
      return null;
    }
    return getValueForField(instance, fieldName, instance.getClass(), annotation, true);
  }

  public Object getValueForField(final Object instance, final Class<? extends Annotation> annotation) {
    if (instance == null) {
      return null;
    }
    return getValueForField(instance, instance.getClass(), annotation, true);
  }

  private Object getValueForField(final Object instance, final Class<?> resultClass,
      final Class<? extends Annotation> annotation, final boolean inherited) {
    return getValueForField(instance, null, resultClass, annotation, inherited);
  }

  public Map<String, Object> getValueForAnnotatedFields(final Object instance,
      final Class<? extends Annotation> annotation) {
    return getValueForAnnotatedFields(instance, instance.getClass(), annotation, true);
  }

  private Map<String, Object> getValueForAnnotatedFields(final Object instance, final Class<?> resultClass,
      final Class<? extends Annotation> annotation, final boolean inherited) {
    if (instance == null) {
      return null;
    }

    Field[] fields = resultClass.getDeclaredFields();
    Map<String, Object> fieldName2Value = new HashMap<String, Object>();

    for (Field field : fields) {
      if (field.getAnnotation(annotation) != null) {
        Object value = getFieldValue(instance, field);
        final String name = extractPropertyName(field);
        fieldName2Value.put(name, value);
      }
    }

    Class<?> superClass = resultClass.getSuperclass();
    if (inherited && superClass != Object.class) {
      Map<String, Object> tmp = getValueForAnnotatedFields(instance, superClass, annotation, true);
      fieldName2Value.putAll(tmp);
    }

    return fieldName2Value;
  }

  private String extractPropertyName(Field field) {
    final EdmProperty property = field.getAnnotation(EdmProperty.class);
    if(property == null || property.name().isEmpty()) {
      return getCanonicalName(field);
    } else {
      return property.name();
    }
  }

  public void setValueForAnnotatedField(final Object instance, final Class<? extends Annotation> annotation,
      final Object value)
      throws ODataAnnotationException {
    List<Field> fields = getAnnotatedFields(instance, annotation);

    if (fields.isEmpty()) {
      throw new ODataAnnotationException("No field found for annotation '" + annotation
          + "' on instance '" + instance + "'.");
    } else if (fields.size() > 1) {
      throw new ODataAnnotationException("More then one field found for annotation '" + annotation
          + "' on instance '" + instance + "'.");
    }

    setFieldValue(instance, fields.get(0), value);
  }

  public void setValuesToAnnotatedFields(final Object instance,
      final Class<? extends Annotation> annotation, final Map<String, Object> fieldName2Value) {
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

  public List<Field> getAnnotatedFields(final Object instance, final Class<? extends Annotation> annotation) {
    if (instance == null) {
      return null;
    }
    return getAnnotatedFields(instance.getClass(), annotation, true);
  }

  public List<Field> getAnnotatedFields(final Class<?> fieldClass, final Class<? extends Annotation> annotation) {
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
  private List<Field> getAnnotatedFields(final Class<?> resultClass,
      final Class<? extends Annotation> annotation, final boolean inherited) {
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

  private Object getValueForField(final Object instance, final String fieldName, final Class<?> resultClass,
      final Class<? extends Annotation> annotation, final boolean inherited) {
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

  private Object getFieldValue(final Object instance, final Field field) {
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

  private void setFieldValue(final Object instance, final Field field, final Object value) {
    try {
      Object usedValue = value;
      if (value != null
          && field.getType() != value.getClass()
          && value.getClass() == String.class) {
        usedValue = convert(field, (String) value);
      }
      boolean access = field.isAccessible();
      field.setAccessible(true);
      field.set(instance, usedValue);
      field.setAccessible(access);
    } catch (IllegalArgumentException ex) { // should never happen
      throw new ODataRuntimeException(ex);
    } catch (IllegalAccessException ex) { // should never happen
      throw new ODataRuntimeException(ex);
    }
  }

  private Object convert(final Field field, final String propertyValue) {
    Class<?> fieldClass = field.getType();
    try {
      EdmProperty property = field.getAnnotation(EdmProperty.class);
      EdmSimpleTypeKind type = mapTypeKind(property.type());
      return type.getEdmSimpleTypeInstance().valueOfString(propertyValue,
          EdmLiteralKind.DEFAULT, null, fieldClass);
    } catch (EdmSimpleTypeException ex) {
      throw new ODataRuntimeException("Conversion failed for string property [" 
          + propertyValue + "] on field ["
          + field + "] with error: " + ex.getMessage(), ex);
    }
  }

  public boolean isEdmAnnotated(final Object object) {
    if (object == null) {
      return false;
    }
    return isEdmAnnotated(object.getClass());
  }

  public boolean isEdmTypeAnnotated(final Class<?> clazz) {
    boolean isComplexEntity = clazz.getAnnotation(EdmComplexType.class) != null;
    boolean isEntity = clazz.getAnnotation(EdmEntityType.class) != null;
    return isComplexEntity || isEntity;
  }

  public boolean isEdmAnnotated(final Class<?> clazz) {
    if (clazz == null) {
      return false;
    } else {
      final boolean isEntity = null != clazz.getAnnotation(EdmEntityType.class);
      final boolean isEntitySet = null != clazz.getAnnotation(EdmEntitySet.class);
      final boolean isComplexEntity = null != clazz.getAnnotation(EdmComplexType.class);
      return isEntity || isComplexEntity || isEntitySet;
    }
  }

  public String getCanonicalName(final Field field) {
    return firstCharToUpperCase(field.getName());
  }

  public String getCanonicalName(final Class<?> clazz) {
    return firstCharToUpperCase(clazz.getSimpleName());
  }

  private String firstCharToUpperCase(final String content) {
    if (content == null || content.isEmpty()) {
      return content;
    }
    return content.substring(0, 1).toUpperCase(Locale.ENGLISH) + content.substring(1);
  }

  public EdmSimpleTypeKind mapTypeKind(final org.apache.olingo.odata2.api.annotation.edm.EdmType type) {
    switch (type) {
    case BINARY:
      return EdmSimpleTypeKind.Binary;
    case BOOLEAN:
      return EdmSimpleTypeKind.Boolean;
    case BYTE:
      return EdmSimpleTypeKind.Byte;
    case COMPLEX:
      return EdmSimpleTypeKind.Null;
    case DATE_TIME:
      return EdmSimpleTypeKind.DateTime;
    case DATE_TIME_OFFSET:
      return EdmSimpleTypeKind.DateTimeOffset;
    case DECIMAL:
      return EdmSimpleTypeKind.Decimal;
    case DOUBLE:
      return EdmSimpleTypeKind.Double;
    case GUID:
      return EdmSimpleTypeKind.Guid;
    case INT16:
      return EdmSimpleTypeKind.Int16;
    case INT32:
      return EdmSimpleTypeKind.Int32;
    case INT64:
      return EdmSimpleTypeKind.Int64;
    case NULL:
      return EdmSimpleTypeKind.Null;
    case SBYTE:
      return EdmSimpleTypeKind.SByte;
    case SINGLE:
      return EdmSimpleTypeKind.Single;
    case STRING:
      return EdmSimpleTypeKind.String;
    case TIME:
      return EdmSimpleTypeKind.Time;
    default:
      throw new ODataRuntimeException("Unknown type '" + type
          + "' for mapping to EdmSimpleTypeKind.");
    }
  }

  public EdmMultiplicity mapMultiplicity(final Multiplicity multiplicity) {
    switch (multiplicity) {
    case ZERO_OR_ONE:
      return EdmMultiplicity.ZERO_TO_ONE;
    case ONE:
      return EdmMultiplicity.ONE;
    case MANY:
      return EdmMultiplicity.MANY;
    default:
      throw new ODataRuntimeException("Unknown type '" + multiplicity + "' for mapping to EdmMultiplicity.");
    }
  }

  /**
   * 
   */
  private static class EdmAnnotationException extends RuntimeException {

    private static final long serialVersionUID = 42L;

    public EdmAnnotationException(final String message) {
      super(message);
    }
  }

  public String getCanonicalNamespace(final Class<?> aClass) {
    return generateNamespace(aClass);
  }

  public String extractContainerName(final Class<?> aClass) {
    EdmEntitySet entitySet = aClass.getAnnotation(EdmEntitySet.class);
    if (entitySet != null) {
      String containerName = entitySet.container();
      if (!containerName.isEmpty()) {
        return containerName;
      }
    }
    return DEFAULT_CONTAINER_NAME;
  }
}
