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
package org.apache.olingo.odata2.jpa.processor.core.access.model;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.metamodel.Attribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;

/**
 * This class holds utility methods for Type conversions between JPA and OData Types.
 * 
 * 
 * 
 */
public class JPATypeConverter {

  /**
   * This utility method converts a given jpa Type to equivalent
   * EdmSimpleTypeKind for maintaining compatibility between Java and OData
   * Types.
   * 
   * @param jpaType
   * The JPA Type input.
   * @return The corresponding EdmSimpleTypeKind.
   * @throws ODataJPAModelException
   * @throws org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException
   * 
   * @see EdmSimpleTypeKind
   */

  public static EdmSimpleTypeKind
      convertToEdmSimpleType(final Class<?> jpaType, final Attribute<?, ?> currentAttribute)
          throws ODataJPAModelException {
    if (jpaType.equals(String.class) || jpaType.equals(Character.class) || jpaType.equals(char.class)
        || jpaType.equals(char[].class) ||
        jpaType.equals(Character[].class)) {
      return EdmSimpleTypeKind.String;
    } else if (jpaType.equals(Long.class) || jpaType.equals(long.class)) {
      return EdmSimpleTypeKind.Int64;
    } else if (jpaType.equals(Short.class) || jpaType.equals(short.class)) {
      return EdmSimpleTypeKind.Int16;
    } else if (jpaType.equals(Integer.class) || jpaType.equals(int.class)) {
      return EdmSimpleTypeKind.Int32;
    } else if (jpaType.equals(Double.class) || jpaType.equals(double.class)) {
      return EdmSimpleTypeKind.Double;
    } else if (jpaType.equals(Float.class) || jpaType.equals(float.class)) {
      return EdmSimpleTypeKind.Single;
    } else if (jpaType.equals(BigDecimal.class)) {
      return EdmSimpleTypeKind.Decimal;
    } else if (jpaType.equals(byte[].class)) {
      return EdmSimpleTypeKind.Binary;
    } else if (jpaType.equals(Byte.class) || jpaType.equals(byte.class)) {
      return EdmSimpleTypeKind.Byte;
    } else if (jpaType.equals(Boolean.class) || jpaType.equals(boolean.class)) {
      return EdmSimpleTypeKind.Boolean;
    } else if (jpaType.equals(java.sql.Time.class)) {
      return EdmSimpleTypeKind.Time;
    } else if (jpaType.equals(Date.class) || jpaType.equals(Calendar.class) ||
        jpaType.equals(Timestamp.class) || jpaType.equals(java.util.Date.class)) {
      try {
        if ((currentAttribute != null)
            && (determineTemporalType(currentAttribute)
              == TemporalType.TIME)) {
          return EdmSimpleTypeKind.Time;
        } else {
          return EdmSimpleTypeKind.DateTime;
        }
      } catch (SecurityException e) {
        throw ODataJPAModelException.throwException(ODataJPAModelException.GENERAL.addContent(e.getMessage()), e);
      }
    } else if (jpaType.equals(UUID.class)) {
      return EdmSimpleTypeKind.Guid;
    } else if (jpaType.equals(Byte[].class)) {
      return EdmSimpleTypeKind.Binary;
    } else if (jpaType.equals(Blob.class) && isBlob(currentAttribute)) {
      return EdmSimpleTypeKind.Binary;
    } else if (jpaType.equals(Clob.class) && isBlob(currentAttribute)) {
      return EdmSimpleTypeKind.String;
    } else if (jpaType.isEnum()) {
      return EdmSimpleTypeKind.String;
    } else {
        // https://issues.apache.org/jira/browse/OLINGO-605
    	// if we cannot find a generic JPA type we try to use the XmlJavaTypeAdapter
    	// to find a property that we can serialize
    	if(currentAttribute == null) {
    		throw ODataJPAModelException.throwException(ODataJPAModelException.TYPE_NOT_SUPPORTED
			        .addContent(jpaType.toString()), null);
    	}
	    String propertyName = currentAttribute.getName();
	    if(propertyName == null) {
	    	throw ODataJPAModelException.throwException(ODataJPAModelException.TYPE_NOT_SUPPORTED
			        .addContent(jpaType.toString()), null);
	    }
	    String getterName = "get"+propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
	    try {
        Method method = currentAttribute.getDeclaringType().getJavaType().getMethod(getterName);
        XmlJavaTypeAdapter xmlAdapterAnnotation = method.getAnnotation(XmlJavaTypeAdapter.class);
        if(xmlAdapterAnnotation == null) {
          throw ODataJPAModelException.throwException(ODataJPAModelException.TYPE_NOT_SUPPORTED
                  .addContent(jpaType.toString()), null);
			}
			@SuppressWarnings("unchecked")
			Class<XmlAdapter<?,?>> xmlAdapterClass = (Class<XmlAdapter<?, ?>>) xmlAdapterAnnotation.value();
			
			ParameterizedType genericSuperClass =
          (ParameterizedType) xmlAdapterClass.getGenericSuperclass();
			Class<?> converterTargetType = (Class<?>) genericSuperClass.getActualTypeArguments()[0];
			return convertToEdmSimpleType(converterTargetType, currentAttribute);
		} catch (NoSuchMethodException e) {
			throw ODataJPAModelException.throwException(
			    ODataJPAModelException.GENERAL.addContent(e.getMessage()), e);
		} catch (SecurityException e) {
			throw ODataJPAModelException.throwException(
			    ODataJPAModelException.GENERAL.addContent(e.getMessage()), e);
		}
    }
  }

  private static boolean isBlob(final Attribute<?, ?> currentAttribute) {
    if (currentAttribute != null) {
      AnnotatedElement annotatedElement = (AnnotatedElement) currentAttribute.getJavaMember();
      if (annotatedElement != null && annotatedElement.getAnnotation(Lob.class) != null) {
        return true;
      }
    }
    return false;
  }

  private static TemporalType determineTemporalType(final Attribute<?, ?> currentAttribute)
      throws ODataJPAModelException {
    if (currentAttribute != null) {
      AnnotatedElement annotatedElement = (AnnotatedElement) currentAttribute.getJavaMember();
      if (annotatedElement != null && annotatedElement.getAnnotation(Temporal.class) != null) {
        return annotatedElement.getAnnotation(Temporal.class).value();
      }
    }
    return null;

  }
}
