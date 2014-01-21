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
package org.apache.olingo.odata2.jpa.processor.core.access.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmAssociationEnd;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmStructuralType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.uri.NavigationPropertySegment;
import org.apache.olingo.odata2.api.uri.SelectItem;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;

public final class JPAEntityParser {

  /*
   * List of buffers used by the Parser
   */
  private static short MAX_SIZE = 10;
  public static final String ACCESS_MODIFIER_GET = "get";
  public static final String ACCESS_MODIFIER_SET = "set";
  private static final String ACCESS_MODIFIER_IS = "is";

  private HashMap<String, HashMap<String, Method>> jpaEntityAccessMap = null;
  private HashMap<String, HashMap<String, String>> jpaEmbeddableKeyMap = null;

  public JPAEntityParser() {
    jpaEntityAccessMap = new HashMap<String, HashMap<String, Method>>(
        MAX_SIZE);
    jpaEmbeddableKeyMap = new HashMap<String, HashMap<String, String>>();
  };

  public HashMap<String, Method> getJPAEntityAccessMap(final String jpaEntityName) {
    return jpaEntityAccessMap.get(jpaEntityName);
  }

  public HashMap<String, String> getJPAEmbeddableKeyMap(final String jpaEntityName) {
    return jpaEmbeddableKeyMap.get(jpaEntityName);
  }

  public List<Map<String, Object>> parse2EdmEntityList(final Collection<Object> jpaEntityList,
      final List<EdmProperty> properties)
      throws ODataJPARuntimeException {

    if (jpaEntityList == null) {
      return null;
    }
    List<Map<String, Object>> edmEntityList = new ArrayList<Map<String, Object>>();
    for (Object item : jpaEntityList) {
      edmEntityList.add(parse2EdmPropertyValueMap(item, properties));
    }

    return edmEntityList;
  }

  @SuppressWarnings("unchecked")
  public final <T> HashMap<String, Object> parse2EdmPropertyValueMap(final Object jpaEntity,
      final List<T> selectPropertyList) throws ODataJPARuntimeException {

    HashMap<String, Object> edmEntity = new HashMap<String, Object>();
    HashMap<String, Method> accessModifierMap = null;
    Object propertyValue = null;
    EdmProperty property = null;
    String jpaEntityAccessKey = null;

    if (selectPropertyList.get(0) instanceof EdmProperty) {
      jpaEntityAccessKey = jpaEntity.getClass().getName();
      if (!jpaEntityAccessMap.containsKey(jpaEntityAccessKey)) {
        accessModifierMap =
            getAccessModifiers((List<EdmProperty>) selectPropertyList, jpaEntity.getClass(), ACCESS_MODIFIER_GET);
      } else {
        accessModifierMap = jpaEntityAccessMap.get(jpaEntityAccessKey);
      }
    }

    for (Object item : selectPropertyList) {
      propertyValue = jpaEntity;
      if (item instanceof SelectItem) {
        SelectItem selectItem = (SelectItem) item;
        for (NavigationPropertySegment navPropSegement : selectItem.getNavigationPropertySegments()) {
          Method method =
              getAccessModifier(propertyValue.getClass(), navPropSegement.getNavigationProperty(), ACCESS_MODIFIER_GET);
          propertyValue = getPropertyValue(method, propertyValue);
        }
        property = selectItem.getProperty();
      } else if (item instanceof EdmProperty) {
        property = (EdmProperty) item;
      }
      try {
        String propertyName = property.getName();
        Method method = accessModifierMap.get(propertyName);
        if (method == null) {
          String methodName = jpaEmbeddableKeyMap.get(jpaEntityAccessKey).get(propertyName);
          if (methodName != null) {
            propertyValue = getEmbeddablePropertyValue(methodName, propertyValue);
          }
        } else {
          propertyValue = getPropertyValue(accessModifierMap.get(propertyName), propertyValue);
        }
        if (property.getType().getKind()
            .equals(EdmTypeKind.COMPLEX)) {
          propertyValue = parse2EdmPropertyValueMap(propertyValue, (EdmStructuralType) property.getType());
        }
        edmEntity.put(propertyName, propertyValue);
      } catch (EdmException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
      } catch (SecurityException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
      } catch (IllegalArgumentException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
      }
    }

    return edmEntity;
  }

  public final List<Map<String, Object>> parse2EdmEntityList(final Collection<Object> jpaEntityList,
      final EdmStructuralType structuralType) throws ODataJPARuntimeException {
    if (jpaEntityList == null || structuralType == null) {
      return null;
    }
    List<EdmProperty> edmProperties = getEdmProperties(structuralType);
    List<Map<String, Object>> edmEntityList = new ArrayList<Map<String, Object>>();
    for (Object jpaEntity : jpaEntityList) {
      edmEntityList.add(parse2EdmPropertyValueMap(jpaEntity, edmProperties));
    }

    return edmEntityList;
  }

  public final HashMap<String, Object> parse2EdmPropertyValueMap(final Object jpaEntity,
      final EdmStructuralType structuralType) throws ODataJPARuntimeException {
    if (jpaEntity == null || structuralType == null) {
      return null;
    }
    return parse2EdmPropertyValueMap(jpaEntity, getEdmProperties(structuralType));
  }

  public final HashMap<String, Object> parse2EdmNavigationValueMap(
      final Object jpaEntity, final List<EdmNavigationProperty> navigationPropertyList)
      throws ODataJPARuntimeException {
    Object result = null;
    String methodName = null;
    HashMap<String, Object> navigationMap = new HashMap<String, Object>();
    if (navigationPropertyList != null
        && navigationPropertyList.size() != 0) {

      try {
        for (EdmNavigationProperty navigationProperty : navigationPropertyList) {
          methodName = getAccessModifierName(navigationProperty.getName(),
              navigationProperty.getMapping(), ACCESS_MODIFIER_GET);
          Method getterMethod = jpaEntity.getClass()
              .getDeclaredMethod(methodName, (Class<?>[]) null);
          getterMethod.setAccessible(true);
          result = getPropertyValue(getterMethod, jpaEntity);
          navigationMap.put(navigationProperty.getName(), result);
        }
      } catch (IllegalArgumentException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
      } catch (EdmException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
      } catch (SecurityException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
      } catch (NoSuchMethodException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
      }
    }
    return navigationMap;
  }

  public Method getAccessModifierSet(final Object jpaEntity, final String methodName) throws ODataJPARuntimeException {
    Class<?> jpaType = jpaEntity.getClass();
    String methodNameGet = ACCESS_MODIFIER_GET + methodName.substring(3);
    Method method = null;

    try {
      method = jpaType.getMethod(methodNameGet, (Class<?>[]) null);
      Class<?> parameterType = method.getReturnType();
      method = jpaType.getMethod(methodName, new Class<?>[] { parameterType });
    } catch (NoSuchMethodException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (SecurityException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    }

    return method;
  }

  public HashMap<String, Method> getAccessModifiers(final Object jpaEntity,
      final EdmStructuralType structuralType, final String accessModifier) throws ODataJPARuntimeException {
    return getAccessModifiers(getEdmProperties(structuralType), jpaEntity.getClass(), accessModifier);
  }

  public static Object getPropertyValue(final Method method, final Object entity) throws ODataJPARuntimeException {
    Object propertyValue = null;
    if (method == null) {
      return null;
    }
    try {
      method.setAccessible(true);
      Class<?> returnType = method.getReturnType();

      if (returnType.equals(char[].class)) {
        char[] ch = (char[]) method.invoke(entity);
        if (ch != null) {
          propertyValue = (String) String.valueOf((char[]) method.invoke(entity));
        }
      } else if (returnType.equals(Character[].class)) {
        propertyValue = (String) toString((Character[]) method.invoke(entity));
      } else if (returnType.equals(char.class)) {
        char c = (Character) method.invoke(entity);
        if (c != '\u0000') {
          propertyValue = (String) String.valueOf(c);
        }
      } else if (returnType.equals(Character.class)) {
        Character c = (Character) method.invoke(entity);
        if (c != null) {
          propertyValue = toString(new Character[] { c });
        }
      } else {
        propertyValue = method.invoke(entity);
      }
    } catch (IllegalAccessException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (IllegalArgumentException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (InvocationTargetException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (SecurityException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    }

    return propertyValue;
  }

  public Object getEmbeddablePropertyValue(final String methodName, final Object jpaEntity)
      throws ODataJPARuntimeException {

    String[] nameParts = methodName.split("\\.");
    Object propertyValue = jpaEntity;
    Method method = null;
    try {
      for (String namePart : nameParts) {
        if (propertyValue == null) {
          break;
        }
        method = propertyValue.getClass().getMethod(namePart, (Class<?>[]) null);
        method.setAccessible(true);
        propertyValue = getPropertyValue(method, propertyValue);
      }
    } catch (NoSuchMethodException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (SecurityException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    }
    return propertyValue;
  }

  public static String toString(final Character[] input) {
    if (input == null) {
      return null;
    }

    StringBuilder builder = new StringBuilder();
    for (Character element : input) {
      if (element == null) {
        continue;
      }
      builder.append(element.charValue());
    }
    return builder.toString();

  }

  public static Character[] toCharacterArray(final String input) {
    if (input == null) {
      return null;
    }

    Character[] characters = new Character[input.length()];
    char[] chars = ((String) input).toCharArray();
    for (int i = 0; i < input.length(); i++) {
      characters[i] = new Character(chars[i]);
    }

    return characters;
  }

  public static String getAccessModifierName(final String propertyName, final EdmMapping mapping,
      final String accessModifier)
      throws ODataJPARuntimeException {
    String name = null;
    StringBuilder builder = new StringBuilder();
    String[] nameParts = {};
    if (mapping == null || mapping.getInternalName() == null) {
      name = propertyName;
    } else {
      name = mapping.getInternalName();
    }
    if (name != null) {
      nameParts = name.split("\\.");
    }
    if (nameParts.length == 1) {
      if (name != null) {
        char c = Character.toUpperCase(name.charAt(0));

        builder.append(accessModifier).append(c).append(name.substring(1))
            .toString();
      }
    } else if (nameParts.length > 1) {

      for (int i = 0; i < nameParts.length; i++) {
        name = nameParts[i];
        char c = Character.toUpperCase(name.charAt(0));
        if (i == 0) {
          builder.append(accessModifier).append(c).append(name.substring(1));
        } else {
          builder.append(".").append(accessModifier).append(c)
              .append(name.substring(1));
        }
      }
    } else {
      return null;
    }

    if (builder.length() > 0) {
      return builder.toString();
    } else {
      return null;
    }

  }

  public Method getAccessModifier(final Class<?> jpaEntityType, final EdmNavigationProperty navigationProperty,
      final String accessModifier)
      throws ODataJPARuntimeException {

    try {

      JPAEdmMapping navPropMapping = (JPAEdmMapping) navigationProperty.getMapping();
      String name = getAccessModifierName(navigationProperty.getName(), (EdmMapping) navPropMapping, accessModifier);

      Class<?>[] params = null;
      if (accessModifier.equals(ACCESS_MODIFIER_SET)) {
        EdmAssociationEnd end = navigationProperty.getRelationship().getEnd(navigationProperty.getToRole());
        switch (end.getMultiplicity()) {
        case MANY:
          params = new Class<?>[] { navPropMapping.getJPAType() };
          break;
        case ONE:
          params = new Class<?>[] { ((JPAEdmMapping) end.getEntityType().getMapping()).getJPAType() };
        default:
          break;
        }
      }
      return jpaEntityType.getMethod(name, params);

    } catch (NoSuchMethodException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (SecurityException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (EdmException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    }
  }

  private HashMap<String, Method> getAccessModifiers(final List<EdmProperty> edmProperties,
      final Class<?> jpaEntityType,
      final String accessModifier) throws ODataJPARuntimeException {

    HashMap<String, Method> accessModifierMap = jpaEntityAccessMap.get(jpaEntityType.getName());
    if (accessModifierMap == null) {
      accessModifierMap = new HashMap<String, Method>();
      jpaEntityAccessMap.put(jpaEntityType.getName(), accessModifierMap);
    }
    HashMap<String, String> embeddableKey = jpaEmbeddableKeyMap.get(jpaEntityType.getName());
    if (embeddableKey == null) {
      embeddableKey = new HashMap<String, String>();
    }

    Method method = null;
    try {
      for (EdmProperty property : edmProperties) {
        String propertyName = property.getName();
        if (accessModifierMap.containsKey(propertyName)) {
          continue;
        }
        String methodName = getAccessModifierName(property.getName(), property.getMapping(), accessModifier);
        String[] nameParts = methodName.split("\\.");
        try {
          if (nameParts.length > 1) {
            if (!embeddableKey.containsKey(propertyName)) {
              embeddableKey.put(propertyName, methodName);
              continue;
            }
          } else {
            if (accessModifier.equals(ACCESS_MODIFIER_SET)) {
              JPAEdmMapping jpaEdmMapping = (JPAEdmMapping) property.getMapping();
              accessModifierMap.put(propertyName, jpaEntityType.getMethod(methodName,
                  new Class<?>[] { jpaEdmMapping.getJPAType() }));
            } else {
              method = jpaEntityType.getMethod(methodName, (Class<?>[]) null);
            }
          }
        } catch (EdmException exp) {
          throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, exp);
        } catch (NoSuchMethodException e1) {
          try {
            EdmSimpleType edmSimpleType = (EdmSimpleType) property.getType();
            if (edmSimpleType == EdmSimpleTypeKind.Boolean.getEdmSimpleTypeInstance()
                && accessModifier.equals(ACCESS_MODIFIER_GET)) {
              String nameWithIs = getAccessModifierName(property.getName(),
                  property.getMapping(), ACCESS_MODIFIER_IS);
              method = jpaEntityType.getMethod(nameWithIs, (Class<?>[]) null);
            }
          } catch (EdmException exp) {
            throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, exp);
          } catch (NoSuchMethodException exp) {
            throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, exp);
          } catch (SecurityException exp) {
            throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, exp);
          }
        } catch (SecurityException e1) {
          throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e1);
        }
        if (method != null) {
          accessModifierMap.put(propertyName, method);
        }
      }
    } catch (EdmException exp) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, exp);
    }
    if (!embeddableKey.isEmpty()) {
      jpaEmbeddableKeyMap.put(jpaEntityType.getName(), embeddableKey);
    }
    return accessModifierMap;
  }

  private List<EdmProperty> getEdmProperties(final EdmStructuralType structuralType) throws ODataJPARuntimeException {
    List<EdmProperty> edmProperties = new ArrayList<EdmProperty>();
    try {
      for (String propertyName : structuralType.getPropertyNames()) {
        edmProperties.add((EdmProperty) structuralType.getProperty(propertyName));
      }
    } catch (EdmException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    }
    return edmProperties;
  }

}