/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.processor.core.jpa.cud;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmStructuralType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPAModelException;
import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.processor.core.jpa.access.model.EdmTypeConvertor;
import org.apache.olingo.odata2.processor.core.jpa.model.JPAEdmMappingImpl;

public class JPAWriteRequest {
  protected HashMap<String, HashMap<String, Method>> jpaEntityAccessMap = null;
  protected HashMap<String, Object> jpaComplexObjectMap = null;
  protected HashMap<String, HashMap<String, String>> jpaEmbeddableKeyMap = null;
  protected HashMap<String, Class<?>> jpaEmbeddableKeyObjectMap = null;

  public JPAWriteRequest() {
    jpaEntityAccessMap = new HashMap<String, HashMap<String, Method>>();
    jpaComplexObjectMap = new HashMap<String, Object>();
  }

  protected HashMap<String, Method> getSetters(final Object jpaEntity,
      final EdmStructuralType structuralType, final boolean isCreate) throws ODataJPARuntimeException {

    HashMap<String, Method> setters = new HashMap<String, Method>();
    HashMap<String, String> embeddableKey = new HashMap<String, String>();
    try {
      for (String propertyName : structuralType.getPropertyNames()) {

        EdmProperty property = (EdmProperty) structuralType
            .getProperty(propertyName);
        Class<?> propertyClass = null;
        try {
          if (property.getMapping() != null && ((JPAEdmMappingImpl) property.getMapping()).getJPAType() != null) {
            propertyClass = ((JPAEdmMappingImpl) property.getMapping()).getJPAType();
            if (property.getType().getKind().equals(EdmTypeKind.COMPLEX)) {
              try {
                if (((JPAEdmMappingImpl) property.getMapping()).getInternalName() != null) {
                  jpaComplexObjectMap.put(((JPAEdmMappingImpl) property.getMapping()).getInternalName(), propertyClass.newInstance());
                } else {
                  jpaComplexObjectMap.put(propertyName, propertyClass.newInstance());
                }
              } catch (InstantiationException e) {
                throw ODataJPARuntimeException
                    .throwException(ODataJPARuntimeException.GENERAL
                        .addContent(e.getMessage()), e);
              } catch (IllegalAccessException e) {
                throw ODataJPARuntimeException
                    .throwException(ODataJPARuntimeException.GENERAL
                        .addContent(e.getMessage()), e);
              }
            }
          } else {
            propertyClass = EdmTypeConvertor.convertToJavaType(property.getType());
          }
        } catch (ODataJPAModelException e) {
          throw ODataJPARuntimeException
              .throwException(ODataJPARuntimeException.GENERAL
                  .addContent(e.getMessage()), e);
        }
        String name = getSetterName(property);
        String[] nameParts = name.split("\\.");
        if (nameParts.length > 1) {
          if (isCreate) {
            jpaEmbeddableKeyObjectMap.put(propertyName, propertyClass);
            embeddableKey.put(propertyName, name);
          }
        } else {
          setters.put(
              propertyName,
              jpaEntity.getClass().getMethod(name, propertyClass));
        }
      }
    } catch (NoSuchMethodException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    } catch (SecurityException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    } catch (EdmException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    }

    if (isCreate && !embeddableKey.isEmpty()) {
      jpaEmbeddableKeyMap.put(jpaEntity.getClass().getName(),
          embeddableKey);
    }
    return setters;
  }

  private String getSetterName(final EdmProperty property)
      throws ODataJPARuntimeException {
    EdmMapping mapping = null;
    String name = null;
    try {
      mapping = property.getMapping();
      if (mapping == null || mapping.getInternalName() == null) {
        name = property.getName();
      } else {
        name = mapping.getInternalName();
      }

    } catch (EdmException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    }

    String[] nameParts = name.split("\\."); //$NON-NLS-1$
    StringBuilder builder = new StringBuilder();

    if (nameParts.length == 1) {
      if (name != null) {
        char c = Character.toUpperCase(name.charAt(0));

        builder.append("set").append(c).append(name.substring(1)) //$NON-NLS-1$
            .toString();
      }
    } else if (nameParts.length > 1) {

      for (int i = 0; i < nameParts.length; i++) {
        name = nameParts[i];
        char c = Character.toUpperCase(name.charAt(0));
        if (i == 0) {
          builder.append("set").append(c).append(name.substring(1)); //$NON-NLS-1$
        } else {
          builder.append(".").append("set").append(c) //$NON-NLS-1$ //$NON-NLS-2$
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

  protected ODataEntry parseEntry(final EdmEntitySet entitySet, final InputStream content, final String requestContentType, final boolean merge) throws ODataBadRequestException {
    ODataEntry entryValues;
    try {
      EntityProviderReadProperties entityProviderProperties = EntityProviderReadProperties.init().mergeSemantic(merge).build();
      entryValues = EntityProvider.readEntry(requestContentType, entitySet, content, entityProviderProperties);
    } catch (EntityProviderException e) {
      throw new ODataBadRequestException(ODataBadRequestException.BODY, e);
    }
    return entryValues;
  }
}
