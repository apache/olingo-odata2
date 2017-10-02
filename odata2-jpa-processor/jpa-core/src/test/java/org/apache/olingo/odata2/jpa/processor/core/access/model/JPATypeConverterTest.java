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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.metamodel.ManagedType;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.EntityWithXmlAdapterOnProperty;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAAttributeMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.JPAJavaMemberMock;
import org.easymock.EasyMock;
import org.junit.Test;

public class JPATypeConverterTest {

  private static String testCase = "datetime";

  private EdmSimpleTypeKind edmSimpleKindTypeString;
  private EdmSimpleTypeKind edmSimpleKindTypeCharacter;
  private EdmSimpleTypeKind edmSimpleKindTypeByteArr;
  private EdmSimpleTypeKind edmSimpleKindTypeLong;
  private EdmSimpleTypeKind edmSimpleKindTypeShort;
  private EdmSimpleTypeKind edmSimpleKindTypeInteger;
  private EdmSimpleTypeKind edmSimpleKindTypeDouble;
  private EdmSimpleTypeKind edmSimpleKindTypeFloat;
  private EdmSimpleTypeKind edmSimpleKindTypeBigDecimal;
  private EdmSimpleTypeKind edmSimpleKindTypeByte;
  private EdmSimpleTypeKind edmSimpleKindTypeBoolean;
  private EdmSimpleTypeKind edmSimpleKindTypeUUID;
  private EdmSimpleTypeKind edmSimpleKindTypeStringFromEnum;

  enum SomeEnum {TEST}

  @Test
  public void testConvertToEdmSimpleType() {
    String str = "entity";
    byte[] byteArr = new byte[3];
    Long longObj = new Long(0);
    Short shortObj = new Short((short) 0);
    Integer integerObj = new Integer(0);
    Double doubleObj = new Double(0);
    Float floatObj = new Float(0);
    BigDecimal bigDecimalObj = new BigDecimal(0);
    Byte byteObj = new Byte((byte) 0);
    Boolean booleanObj = Boolean.TRUE;
    UUID uUID = new UUID(0, 0);
    SomeEnum someEnum = SomeEnum.TEST;
    Character charObj = new Character('c');

    try {
      edmSimpleKindTypeString = JPATypeConverter.convertToEdmSimpleType(str.getClass(), null);
      edmSimpleKindTypeByteArr = JPATypeConverter.convertToEdmSimpleType(byteArr.getClass(), null);
      edmSimpleKindTypeLong = JPATypeConverter.convertToEdmSimpleType(longObj.getClass(), null);
      edmSimpleKindTypeShort = JPATypeConverter.convertToEdmSimpleType(shortObj.getClass(), null);
      edmSimpleKindTypeInteger = JPATypeConverter.convertToEdmSimpleType(integerObj.getClass(), null);
      edmSimpleKindTypeDouble = JPATypeConverter.convertToEdmSimpleType(doubleObj.getClass(), null);
      edmSimpleKindTypeFloat = JPATypeConverter.convertToEdmSimpleType(floatObj.getClass(), null);
      edmSimpleKindTypeBigDecimal = JPATypeConverter.convertToEdmSimpleType(bigDecimalObj.getClass(), null);
      edmSimpleKindTypeByte = JPATypeConverter.convertToEdmSimpleType(byteObj.getClass(), null);
      edmSimpleKindTypeBoolean = JPATypeConverter.convertToEdmSimpleType(booleanObj.getClass(), null);
      edmSimpleKindTypeStringFromEnum = JPATypeConverter.convertToEdmSimpleType(someEnum.getClass(), null);
      edmSimpleKindTypeCharacter = JPATypeConverter.convertToEdmSimpleType(charObj.getClass(), null);
      edmSimpleKindTypeUUID = JPATypeConverter.convertToEdmSimpleType(uUID.getClass(), null);
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    assertEquals(EdmSimpleTypeKind.String, edmSimpleKindTypeString);
    assertEquals(EdmSimpleTypeKind.Binary, edmSimpleKindTypeByteArr);
    assertEquals(EdmSimpleTypeKind.Int64, edmSimpleKindTypeLong);
    assertEquals(EdmSimpleTypeKind.Int16, edmSimpleKindTypeShort);
    assertEquals(EdmSimpleTypeKind.Int32, edmSimpleKindTypeInteger);
    assertEquals(EdmSimpleTypeKind.Double, edmSimpleKindTypeDouble);
    assertEquals(EdmSimpleTypeKind.Single, edmSimpleKindTypeFloat);
    assertEquals(EdmSimpleTypeKind.Decimal, edmSimpleKindTypeBigDecimal);
    assertEquals(EdmSimpleTypeKind.Byte, edmSimpleKindTypeByte);
    assertEquals(EdmSimpleTypeKind.Boolean, edmSimpleKindTypeBoolean);
    assertEquals(EdmSimpleTypeKind.String, edmSimpleKindTypeCharacter);
    assertEquals(EdmSimpleTypeKind.Guid, edmSimpleKindTypeUUID);
    assertEquals(EdmSimpleTypeKind.String, edmSimpleKindTypeStringFromEnum);
  }

  @Test
  public void testConvertTypeCharacter() {
    try {
      assertEquals(EdmSimpleTypeKind.String, JPATypeConverter.convertToEdmSimpleType(Character[].class, null));
      assertEquals(EdmSimpleTypeKind.String, JPATypeConverter.convertToEdmSimpleType(char[].class, null));
      assertEquals(EdmSimpleTypeKind.String, JPATypeConverter.convertToEdmSimpleType(char.class, null));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testConvertTypeNumbers() {
    try {
      assertEquals(EdmSimpleTypeKind.Int64, JPATypeConverter.convertToEdmSimpleType(long.class, null));
      assertEquals(EdmSimpleTypeKind.Int16, JPATypeConverter.convertToEdmSimpleType(short.class, null));
      assertEquals(EdmSimpleTypeKind.Int32, JPATypeConverter.convertToEdmSimpleType(int.class, null));
      assertEquals(EdmSimpleTypeKind.Double, JPATypeConverter.convertToEdmSimpleType(double.class, null));
      assertEquals(EdmSimpleTypeKind.Single, JPATypeConverter.convertToEdmSimpleType(float.class, null));
      assertEquals(EdmSimpleTypeKind.Byte, JPATypeConverter.convertToEdmSimpleType(byte.class, null));
      assertEquals(EdmSimpleTypeKind.Boolean, JPATypeConverter.convertToEdmSimpleType(boolean.class, null));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testConvertTypeByteArray() {
    try {
      assertEquals(EdmSimpleTypeKind.Binary, JPATypeConverter.convertToEdmSimpleType(Byte[].class, null));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testConvertTypeBlob() {
    testCase = "lob";
    try {
      assertEquals(EdmSimpleTypeKind.Binary, JPATypeConverter.convertToEdmSimpleType(Blob.class,
          new JPASimpleAttribute()));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testConvertTypeClob() {
    testCase = "lob";
    try {
      assertEquals(EdmSimpleTypeKind.String, JPATypeConverter.convertToEdmSimpleType(Clob.class,
          new JPASimpleAttribute()));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testConvertTypeBLobNegative() {
    try {
      JPATypeConverter.convertToEdmSimpleType(Blob.class, null);
    } catch (ODataJPAModelException e) {
      assertTrue(true);
      return;
    }
    fail("ExceptionExpected");
  }

  @Test
  public void testConvertTypeClobNegative() {
    try {
      JPATypeConverter.convertToEdmSimpleType(Clob.class, null);
    } catch (ODataJPAModelException e) {
      assertTrue(true);
      return;
    }
    fail("ExceptionExpected");
  }

  @Test
  public void testConvertTypeCalendar() {
    try {
      assertEquals(EdmSimpleTypeKind.DateTime, JPATypeConverter.convertToEdmSimpleType(Calendar.class, null));
      assertEquals(EdmSimpleTypeKind.Time, JPATypeConverter.convertToEdmSimpleType(Time.class, null));
      assertEquals(EdmSimpleTypeKind.DateTime, JPATypeConverter.convertToEdmSimpleType(Date.class, null));
      assertEquals(EdmSimpleTypeKind.DateTime, JPATypeConverter.convertToEdmSimpleType(Timestamp.class, null));
      assertEquals(EdmSimpleTypeKind.DateTime, JPATypeConverter.convertToEdmSimpleType(java.sql.Date.class, null));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
      ;
    }
  }

  @Test
  public void testConvertTypeTemporal() {
    testCase = "datetime";
    try {
      EdmSimpleTypeKind edmDateType = JPATypeConverter.convertToEdmSimpleType(Calendar.class, new JPASimpleAttribute());
      assertEquals(EdmSimpleTypeKind.DateTime, edmDateType);
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testConvertTypeTemporalTime() {
    testCase = "time";
    try {
      EdmSimpleTypeKind edmTimeType = JPATypeConverter.convertToEdmSimpleType(Calendar.class, new JPASimpleAttribute());
      assertEquals(EdmSimpleTypeKind.Time, edmTimeType);
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testConvertTypeTemporalNull() {
    testCase = "temporalnull";
    try {
      EdmSimpleTypeKind edmDateType = JPATypeConverter.convertToEdmSimpleType(Calendar.class, new JPASimpleAttribute());
      assertEquals(EdmSimpleTypeKind.DateTime, edmDateType);
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testConvertTypeTemporalNull2() {
    testCase = "temporalnull2";
    try {
      EdmSimpleTypeKind edmDateType =
    		  JPATypeConverter.convertToEdmSimpleType(Calendar.class,new JPASimpleAttribute());
      assertEquals(EdmSimpleTypeKind.DateTime, edmDateType);
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void testConvertPropertyWithXmlAdapter() {
	  try {
		 EdmSimpleTypeKind edmDateType =
				 JPATypeConverter
				 .convertToEdmSimpleType(EntityWithXmlAdapterOnProperty.class,
						 new JPAAttributeWithXmlAdapterType());
		 assertEquals(EdmSimpleTypeKind.String, edmDateType);
	} catch (ODataJPAModelException e) {
		fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() +
				ODataJPATestConstants.EXCEPTION_MSG_PART_2);
	}
  }

  private static class JPASimpleAttribute extends JPAAttributeMock<Object, String> {

    @Override
    public Member getJavaMember() {
      if (testCase.equals("temporalNull2")) {
        return null;
      }
      return new JPAJavaMember();
    }
  }

  private static class JPAJavaMember extends JPAJavaMemberMock {

    private Temporal temporal = null;
    private Lob lob = null;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {

      if (testCase.equals("temporalnull")) {
        return null;
      }

      if (annotationClass.equals(Temporal.class)) {
        if (temporal == null) {
          temporal = EasyMock.createMock(Temporal.class);
          if (testCase.equals("datetime")) {
            EasyMock.expect(temporal.value()).andReturn(TemporalType.TIMESTAMP).anyTimes();
            EasyMock.replay(temporal);
          } else if (testCase.equals("time")) {
            EasyMock.expect(temporal.value()).andReturn(TemporalType.TIME).anyTimes();
            EasyMock.replay(temporal);
          }
        }
        return (T) temporal;
      } else if (annotationClass.equals(Lob.class)) {
        if (testCase.equals("lob")) {
          lob = EasyMock.createMock(Lob.class);
          EasyMock.replay(lob);
        }
        return (T) lob;
      }
      return null;

    }
  }
  
  private static class JPAAttributeWithXmlAdapterType extends JPAAttributeMock<EntityWithXmlAdapterOnProperty, String> {
	  @Override
	  public String getName() {
		  return "self";
	  }
	  
	  public ManagedType<EntityWithXmlAdapterOnProperty> getDeclaringType() {
		ManagedType<EntityWithXmlAdapterOnProperty> mock = EasyMock.createMock(ManagedType.class);
		EasyMock.expect(mock.getJavaType()).andStubReturn(EntityWithXmlAdapterOnProperty.class);
		EasyMock.replay(mock);
		return mock;
	  }
  }
}
