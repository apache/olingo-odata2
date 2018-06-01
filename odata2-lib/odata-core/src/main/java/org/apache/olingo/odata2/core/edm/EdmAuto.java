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
package org.apache.olingo.odata2.core.edm;

import org.apache.olingo.odata2.api.edm.*;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Implementation of the EDM simple type Auto.
 */
public class EdmAuto extends AbstractSimpleType {

  private static final EdmAuto instance = new EdmAuto();

  private EdmString internal = new EdmString();

  public static EdmAuto getInstance() {
    return instance;
  }

  public EdmSimpleType getType(Object value) {
    if (value == null) {
      return EdmString.getInstance();
    } else {

      Class jpaType = value.getClass();

      if (jpaType.equals(String.class) || jpaType.equals(Character.class) || jpaType.equals(char.class)
          || jpaType.equals(char[].class) ||
          jpaType.equals(Character[].class)) {
        return EdmString.getInstance();
      } else if (jpaType.equals(Long.class) || jpaType.equals(long.class)) {
        return EdmInt64.getInstance();
      } else if (jpaType.equals(Short.class) || jpaType.equals(short.class)) {
        return EdmInt16.getInstance();
      } else if (jpaType.equals(Integer.class) || jpaType.equals(int.class)) {
        return EdmInt32.getInstance();
      } else if (jpaType.equals(Double.class) || jpaType.equals(double.class)) {
        return EdmDouble.getInstance();
      } else if (jpaType.equals(Float.class) || jpaType.equals(float.class)) {
        return EdmSingle.getInstance();
      } else if (jpaType.equals(BigDecimal.class)) {
        return EdmDecimal.getInstance();
      } else if (jpaType.equals(byte[].class)) {
        return EdmString.getInstance();
      } else if (jpaType.equals(Byte.class) || jpaType.equals(byte.class)) {
        return EdmByte.getInstance();
      } else if (jpaType.equals(Boolean.class) || jpaType.equals(boolean.class)) {
        return EdmBoolean.getInstance();
      } else if (jpaType.equals(java.sql.Time.class)) {
        return EdmTime.getInstance();
      } else if (jpaType.equals(Date.class) || value instanceof Calendar ||
          jpaType.equals(Timestamp.class) || jpaType.equals(java.util.Date.class)) {
        return EdmDateTime.getInstance();
      } else if (jpaType.equals(UUID.class)) {
        return EdmGuid.getInstance();
      } else if (jpaType.equals(Byte[].class)) {
        return EdmBinary.getInstance();
      } else if (jpaType.equals(Blob.class)) {
        return EdmBinary.getInstance();
      } else if (jpaType.equals(Clob.class)) {
        return EdmString.getInstance();
      } else if (jpaType.isEnum()) {
        return EdmString.getInstance();
      } else {
        return EdmString.getInstance();
      }
    }
  }

  @Override
  protected <T> T internalValueOfString(String value, EdmLiteralKind literalKind, EdmFacets facets, Class<T> returnType) throws EdmSimpleTypeException {
    return internal.internalValueOfString(value, literalKind, facets, returnType);
  }

  @Override
  protected <T> String internalValueToString(T value, EdmLiteralKind literalKind, EdmFacets facets) throws EdmSimpleTypeException {
    return internal.internalValueToString(value, literalKind, facets);
  }

  @Override
  public Class<?> getDefaultType() {
    return internal.getDefaultType();
  }
}
