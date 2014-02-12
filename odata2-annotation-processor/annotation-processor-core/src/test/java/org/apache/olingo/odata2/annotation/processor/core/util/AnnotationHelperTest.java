/*
 * Copyright 2013 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.olingo.odata2.annotation.processor.core.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.olingo.odata2.annotation.processor.core.model.Location;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmType;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.junit.Test;

/**
 *
 */
public class AnnotationHelperTest {

  private final AnnotationHelper annotationHelper;

  public AnnotationHelperTest() {
    annotationHelper = new AnnotationHelper();
  }

  @Test
  public void keyMatchMapPositive() throws ODataException {
    SimpleEntity firstInstance = new SimpleEntity(42l, "Another Name");
    Map<String, Object> keyName2Value = new HashMap<String, Object>();
    keyName2Value.put("Id", Long.valueOf(42));

    boolean result = annotationHelper.keyMatch(firstInstance, keyName2Value);

    Assert.assertTrue(result);
  }

  @Test
  public void keyMatchMapNegativeWrongClass() throws ODataException {
    SimpleEntity firstInstance = new SimpleEntity(42l, "Another Name");
    Map<String, Object> keyName2Value = new HashMap<String, Object>();
    keyName2Value.put("Id", 42);

    boolean result = annotationHelper.keyMatch(firstInstance, keyName2Value);

    Assert.assertFalse(result);
  }

  @Test
  public void keyMatchMapNegativeDifferentValues() throws ODataException {
    SimpleEntity firstInstance = new SimpleEntity(99l, "Another Name");
    Map<String, Object> keyName2Value = new HashMap<String, Object>();
    keyName2Value.put("Id", 42);

    boolean result = annotationHelper.keyMatch(firstInstance, keyName2Value);

    Assert.assertFalse(result);
  }

  @Test
  public void keyMatchMapNegativeDifferentValueCount() throws ODataException {
    SimpleEntity firstInstance = new SimpleEntity(99l, "Another Name");
    Map<String, Object> keyName2Value = new HashMap<String, Object>();

    boolean result = annotationHelper.keyMatch(firstInstance, keyName2Value);

    Assert.assertFalse(result);
  }

  @Test
  public void keyMatchPositive() throws ODataException {
    SimpleEntity firstInstance = new SimpleEntity(42l, "A Name");
    SimpleEntity secondInstance = new SimpleEntity(42l, "Another Name");

    boolean result = annotationHelper.keyMatch(firstInstance, secondInstance);

    Assert.assertTrue(result);
  }

  @Test
  public void keyMatchPositiveWithNull() throws ODataException {
    SimpleEntity firstInstance = new SimpleEntity();
    SimpleEntity secondInstance = new SimpleEntity();

    boolean result = annotationHelper.keyMatch(firstInstance, secondInstance);

    Assert.assertTrue(result);
  }

  @Test
  public void keyMatchNegative() throws ODataException {
    SimpleEntity firstInstance = new SimpleEntity(99l, "A Name");
    SimpleEntity secondInstance = new SimpleEntity(42l, "A Name");

    boolean result = annotationHelper.keyMatch(firstInstance, secondInstance);

    Assert.assertFalse(result);
  }

  @Test
  public void keyMatchNegativeWithNull() throws ODataException {
    SimpleEntity firstInstance = new SimpleEntity();
    SimpleEntity secondInstance = new SimpleEntity(42l, "A Name");

    boolean result = annotationHelper.keyMatch(firstInstance, secondInstance);

    Assert.assertFalse(result);
  }

  @Test
  public void keyMatchNegativeWithNullInstance() throws ODataException {
    SimpleEntity firstInstance = null;
    SimpleEntity secondInstance = new SimpleEntity(42l, "A Name");

    boolean result = annotationHelper.keyMatch(firstInstance, secondInstance);
    Assert.assertFalse(result);

    result = annotationHelper.keyMatch(secondInstance, firstInstance);
    Assert.assertFalse(result);
  }

  @Test
  public void keyMatchNegativeOneNotAnnotated() throws ODataException {
    NotAnnotatedBean firstInstance = new NotAnnotatedBean();
    SimpleEntity secondInstance = new SimpleEntity(42l, "A Name");

    boolean result = annotationHelper.keyMatch(firstInstance, secondInstance);
    Assert.assertFalse(result);

    boolean result2 = annotationHelper.keyMatch(secondInstance, firstInstance);
    Assert.assertFalse(result2);
  }

  @Test(expected = AnnotationRuntimeException.class)
  public void keyMatchNegativeNotAnnotated() throws ODataException {
    NotAnnotatedBean firstInstance = new NotAnnotatedBean();
    NotAnnotatedBean secondInstance = new NotAnnotatedBean();

    boolean result = annotationHelper.keyMatch(firstInstance, secondInstance);

    Assert.assertFalse(result);
  }

  @Test
  public void extractEntitTypeNameViaNavigation() throws Exception {
    Field field = NavigationAnnotated.class.getDeclaredField("navigationPropertySimpleEntity");
    EdmNavigationProperty enp = field.getAnnotation(EdmNavigationProperty.class);

    String name = annotationHelper.extractEntitTypeName(enp, SimpleEntity.class);

    Assert.assertEquals("SimpleEntity", name);
  }

  @Test
  public void extractEntitTypeNameViaNavigationField() throws Exception {
    Field field = NavigationAnnotated.class.getDeclaredField("navigationPropertyDefault");
    EdmNavigationProperty enp = field.getAnnotation(EdmNavigationProperty.class);

    String name = annotationHelper.extractEntitTypeName(enp, field);

    Assert.assertEquals("SimpleEntity", name);
  }

  @Test
  public void getFieldTypeForPropertyNullInstance() throws Exception {
    Object result = annotationHelper.getFieldTypeForProperty(null, "");
    Assert.assertNull(result);
  }

  @Test
  public void getValueForPropertyNullInstance() throws Exception {
    Object result = annotationHelper.getValueForProperty(null, "");
    Assert.assertNull(result);
  }

  @Test
  public void setValueForPropertyNullInstance() throws Exception {
    annotationHelper.setValueForProperty(null, "", null);
  }

  @Test
  public void extractEntitySetNameObject() {
    Assert.assertNull(annotationHelper.extractEntitySetName(Object.class));
  }

  @Test
  public void extractComplexTypeFqnObject() {
    Assert.assertNull(annotationHelper.extractComplexTypeFqn(Object.class));
  }

  @Test
  public void extractComplexTypeFqn() {
    FullQualifiedName fqn = annotationHelper.extractComplexTypeFqn(Location.class);
    Assert.assertEquals("RefScenario", fqn.getNamespace());
    Assert.assertEquals("c_Location", fqn.getName());
  }

  @Test
  public void convert() throws Exception {
    ConversionProperty cp = new ConversionProperty();
    annotationHelper.setValueForProperty(cp, "StringProp", "42");
    annotationHelper.setValueForProperty(cp, "IntegerProp", "420");
    annotationHelper.setValueForProperty(cp, "LongProp", "4200");
    annotationHelper.setValueForProperty(cp, "FloatProp", "43");
    annotationHelper.setValueForProperty(cp, "DoubleProp", "42.00");
    annotationHelper.setValueForProperty(cp, "ByteProp", "1");

    Assert.assertEquals("42", cp.stringProp);
    Assert.assertEquals(Integer.valueOf(420), cp.integerProp);
    Assert.assertEquals(Long.valueOf("4200"), cp.longProp);
    Assert.assertEquals(new Float(43), cp.floatProp);
    Assert.assertEquals(new Double(42.00), cp.doubleProp);
    Assert.assertEquals(Byte.valueOf("1"), cp.byteProp);
  }

  @EdmEntityType
  private class SimpleEntity {
    @EdmKey
    @EdmProperty
    Long id;
    @EdmProperty
    String name;

    public SimpleEntity() {}

    public SimpleEntity(final Long id, final String name) {
      this.id = id;
      this.name = name;
    }
  }

  private class NavigationAnnotated {
    @EdmNavigationProperty(toType = SimpleEntity.class)
    SimpleEntity navigationPropertySimpleEntity;
    @EdmNavigationProperty
    SimpleEntity navigationPropertyDefault;
  }

  private class ConversionProperty {
    @EdmProperty(type = EdmType.STRING)
    String stringProp;
    @EdmProperty(type = EdmType.INT32)
    Integer integerProp;
    @EdmProperty(type = EdmType.INT64)
    Long longProp;
    @EdmProperty(type = EdmType.DECIMAL)
    Float floatProp;
    @EdmProperty(type = EdmType.DOUBLE)
    Double doubleProp;
    @EdmProperty(type = EdmType.BYTE)
    Byte byteProp;
  }

  private class NotAnnotatedBean {}
}
