/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.apache.olingo.odata2.jpa.processor.core.access.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmStructuralType;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.easymock.EasyMock;
import org.junit.Test;

public class JPAEntityParserTest {
    /*
     * TestCase - JPAResultParser is a singleton class Check if the same instance is returned when
     * create method is called
     */
    @Test
    public void testCreate() {
        JPAEntityParser resultParser1 = new JPAEntityParser();
        JPAEntityParser resultParser2 = new JPAEntityParser();

        if (resultParser1.equals(resultParser2)) {
            fail();
        }
    }

    @Test
    public void testparse2EdmPropertyValueMap() {
        JPAEntityParser resultParser = new JPAEntityParser();
        Object jpaEntity = new demoItem("abc", 10);
        EdmStructuralType structuralType = EasyMock.createMock(EdmStructuralType.class);
        EdmProperty edmTyped = EasyMock.createMock(EdmProperty.class);
        EdmType edmType = EasyMock.createMock(EdmType.class);
        EdmProperty edmTyped01 = EasyMock.createMock(EdmProperty.class);
        EdmType edmType01 = EasyMock.createMock(EdmType.class);
        EdmMapping edmMapping = EasyMock.createMock(JPAEdmMappingImpl.class);
        EdmMapping edmMapping01 = EasyMock.createMock(JPAEdmMappingImpl.class);

        try {
            EasyMock.expect(edmType.getKind())
                    .andStubReturn(EdmTypeKind.SIMPLE);
            EasyMock.expect(edmTyped.getName())
                    .andStubReturn("identifier");
            EasyMock.replay(edmType);
            EasyMock.expect(edmMapping.getInternalName())
                    .andStubReturn("id");
            EasyMock.expect(((JPAEdmMappingImpl) edmMapping).isVirtualAccess())
                    .andStubReturn(false);
            EasyMock.replay(edmMapping);
            EasyMock.expect(edmTyped.getType())
                    .andStubReturn(edmType);
            EasyMock.expect(edmTyped.getMapping())
                    .andStubReturn(edmMapping);
            EasyMock.replay(edmTyped);
            EasyMock.expect(structuralType.getProperty("identifier"))
                    .andStubReturn(edmTyped);

            EasyMock.expect(edmType01.getKind())
                    .andStubReturn(EdmTypeKind.SIMPLE);
            EasyMock.expect(edmTyped01.getName())
                    .andStubReturn("Value");
            EasyMock.replay(edmType01);
            EasyMock.expect(edmMapping01.getInternalName())
                    .andStubReturn("value");
            EasyMock.expect(((JPAEdmMappingImpl) edmMapping01).isVirtualAccess())
                    .andStubReturn(false);
            EasyMock.replay(edmMapping01);
            EasyMock.expect(edmTyped01.getType())
                    .andStubReturn(edmType01);
            EasyMock.expect(edmTyped01.getMapping())
                    .andStubReturn(edmMapping01);
            EasyMock.replay(edmTyped01);
            EasyMock.expect(structuralType.getProperty("value"))
                    .andStubReturn(edmTyped01);

            List<String> propNames = new ArrayList<String>();
            propNames.add("identifier");
            propNames.add("value");
            EasyMock.expect(structuralType.getPropertyNames())
                    .andReturn(propNames);
            EasyMock.replay(structuralType);

        } catch (EdmException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }

        try {
            Map<String, Object> result = resultParser.parse2EdmPropertyValueMap(jpaEntity, structuralType);
            assertEquals(2, result.size());
        } catch (ODataJPARuntimeException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }

    }

    @Test
    public void testparse2EdmPropertyValueMapEdmExcep() {
        JPAEntityParser resultParser = new JPAEntityParser();
        Object jpaEntity = new demoItem("abc", 10);
        EdmStructuralType structuralType = EasyMock.createMock(EdmStructuralType.class);
        EdmProperty edmTyped = EasyMock.createMock(EdmProperty.class);
        EdmType edmType = EasyMock.createMock(EdmType.class);
        EdmProperty edmTyped01 = EasyMock.createMock(EdmProperty.class);
        EdmType edmType01 = EasyMock.createMock(EdmType.class);
        EdmMapping edmMapping = EasyMock.createMock(JPAEdmMappingImpl.class);
        EdmMapping edmMapping01 = EasyMock.createMock(JPAEdmMappingImpl.class);

        try {
            EasyMock.expect(edmType.getKind())
                    .andStubReturn(EdmTypeKind.SIMPLE);
            EasyMock.expect(edmType.getName())
                    .andReturn("identifier");
            EasyMock.replay(edmType);
            EasyMock.expect(edmMapping.getInternalName())
                    .andStubReturn("id");
            EasyMock.expect(((JPAEdmMappingImpl) edmMapping).isVirtualAccess())
                    .andStubReturn(false);
            EasyMock.replay(edmMapping);
            EasyMock.expect(edmTyped.getType())
                    .andStubThrow(new EdmException(null));
            EasyMock.expect(edmTyped.getMapping())
                    .andStubReturn(edmMapping);
            EasyMock.expect(edmTyped.getName())
                    .andReturn("identifier")
                    .anyTimes();
            EasyMock.replay(edmTyped);
            EasyMock.expect(structuralType.getProperty("identifier"))
                    .andStubReturn(edmTyped);

            EasyMock.expect(edmType01.getKind())
                    .andStubReturn(EdmTypeKind.SIMPLE);
            EasyMock.expect(edmType01.getName())
                    .andStubReturn("value");
            EasyMock.replay(edmType01);
            EasyMock.expect(edmMapping01.getInternalName())
                    .andStubReturn("value");
            EasyMock.expect(((JPAEdmMappingImpl) edmMapping01).isVirtualAccess())
                    .andStubReturn(false);
            EasyMock.replay(edmMapping01);
            EasyMock.expect(edmTyped01.getName())
                    .andReturn("value")
                    .anyTimes();
            EasyMock.expect(edmTyped01.getType())
                    .andStubReturn(edmType01);
            EasyMock.expect(edmTyped01.getMapping())
                    .andStubReturn(edmMapping01);
            EasyMock.replay(edmTyped01);
            EasyMock.expect(structuralType.getProperty("value"))
                    .andStubReturn(edmTyped01);

            List<String> propNames = new ArrayList<String>();
            propNames.add("identifier");
            propNames.add("value");
            EasyMock.expect(structuralType.getPropertyNames())
                    .andReturn(propNames);
            EasyMock.replay(structuralType);

        } catch (EdmException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2); // assertTrue(false);
        }

        try {
            resultParser.parse2EdmPropertyValueMap(jpaEntity, structuralType);
        } catch (ODataJPARuntimeException e) {
            assertTrue(true);
        }

    }

    @Test
    public void testparse2EdmPropertyListMap() {
        JPAEntityParser resultParser = new JPAEntityParser();
        Map<String, Object> edmEntity = new HashMap<String, Object>();
        edmEntity.put("SoId", 1);
        DemoRelatedEntity relatedEntity = new DemoRelatedEntity("NewOrder");
        demoItem jpaEntity = new demoItem("laptop", 1);
        jpaEntity.setRelatedEntity(relatedEntity);
        List<EdmNavigationProperty> navigationPropertyList = new ArrayList<EdmNavigationProperty>();
        // Mocking a navigation property and its mapping object
        EdmNavigationProperty navigationProperty = EasyMock.createMock(EdmNavigationProperty.class);
        JPAEdmMappingImpl edmMapping = EasyMock.createMock(JPAEdmMappingImpl.class);
        try {
            EasyMock.expect(edmMapping.getInternalName())
                    .andStubReturn("relatedEntity");
            EasyMock.expect(edmMapping.isVirtualAccess())
                    .andStubReturn(false);
            EasyMock.replay(edmMapping);
            EasyMock.expect(navigationProperty.getName())
                    .andStubReturn("RelatedEntities");
            EasyMock.expect(navigationProperty.getMapping())
                    .andStubReturn(edmMapping);
            EasyMock.replay(navigationProperty);
        } catch (EdmException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }

        navigationPropertyList.add(navigationProperty);
        try {
            HashMap<String, Object> result = resultParser.parse2EdmNavigationValueMap(jpaEntity, navigationPropertyList);
            assertEquals(relatedEntity, result.get("RelatedEntities"));

        } catch (ODataJPARuntimeException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }
    }

    @Test
    public void testparse2EdmPropertyListMapWithVirtualAccess() {
        JPAEntityParser resultParser = new JPAEntityParser();
        Map<String, Object> edmEntity = new HashMap<String, Object>();
        edmEntity.put("SoId", 1);
        DemoRelatedEntity relatedEntity = new DemoRelatedEntity("NewOrder");
        demoItem jpaEntity = new demoItem("laptop", 1);
        jpaEntity.setRelatedEntity(relatedEntity);
        List<EdmNavigationProperty> navigationPropertyList = new ArrayList<EdmNavigationProperty>();
        // Mocking a navigation property and its mapping object
        EdmNavigationProperty navigationProperty = EasyMock.createMock(EdmNavigationProperty.class);
        JPAEdmMappingImpl edmMapping = EasyMock.createMock(JPAEdmMappingImpl.class);
        try {
            EasyMock.expect(edmMapping.getInternalName())
                    .andStubReturn("relatedEntity");
            EasyMock.expect(edmMapping.isVirtualAccess())
                    .andStubReturn(true);
            EasyMock.replay(edmMapping);
            EasyMock.expect(navigationProperty.getName())
                    .andStubReturn("RelatedEntities");
            EasyMock.expect(navigationProperty.getMapping())
                    .andStubReturn(edmMapping);
            EasyMock.replay(navigationProperty);
        } catch (EdmException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }

        navigationPropertyList.add(navigationProperty);
        try {
            HashMap<String, Object> result = resultParser.parse2EdmNavigationValueMap(jpaEntity, navigationPropertyList);
            assertEquals(relatedEntity, result.get("RelatedEntities"));

        } catch (ODataJPARuntimeException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }
    }

    // This unit tests when there is a complex type in the select list
    @SuppressWarnings("unchecked")
    @Test
    public void testparse2EdmPropertyValueMapFromListComplex() {
        JPAEntityParser resultParser = new JPAEntityParser();
        demoItem jpaEntity = new demoItem("laptop", 1);
        DemoRelatedEntity relatedEntity = new DemoRelatedEntity("DemoOrder");
        jpaEntity.setRelatedEntity(relatedEntity);
        List<EdmProperty> selectPropertyList = new ArrayList<EdmProperty>();
        // Mocking EdmProperties
        EdmProperty edmProperty1 = EasyMock.createMock(EdmProperty.class);
        EdmProperty edmProperty2 = EasyMock.createMock(EdmProperty.class);
        EdmProperty edmComplexProperty = EasyMock.createMock(EdmProperty.class);
        EdmType edmType1 = EasyMock.createMock(EdmType.class);
        EdmStructuralType edmType2 = EasyMock.createMock(EdmStructuralType.class);
        EdmType edmComplexType = EasyMock.createMock(EdmType.class);
        JPAEdmMappingImpl mapping1 = EasyMock.createMock(JPAEdmMappingImpl.class);
        JPAEdmMappingImpl mapping2 = EasyMock.createMock(JPAEdmMappingImpl.class);
        JPAEdmMappingImpl complexMapping = EasyMock.createMock(JPAEdmMappingImpl.class);
        try {
            EasyMock.expect(edmType1.getKind())
                    .andStubReturn(EdmTypeKind.SIMPLE);
            EasyMock.replay(edmType1);
            EasyMock.expect(mapping1.getInternalName())
                    .andStubReturn("id");
            EasyMock.expect(mapping1.isVirtualAccess())
                    .andStubReturn(false);
            EasyMock.replay(mapping1);
            EasyMock.expect(edmProperty1.getName())
                    .andStubReturn("Id");
            EasyMock.expect(edmProperty1.getMapping())
                    .andStubReturn(mapping1);
            EasyMock.expect(edmProperty1.getType())
                    .andStubReturn(edmType1);
            EasyMock.replay(edmProperty1);
            // Mocking the complex properties
            EasyMock.expect(edmComplexType.getKind())
                    .andStubReturn(EdmTypeKind.SIMPLE);
            EasyMock.replay(edmComplexType);
            EasyMock.expect(complexMapping.getInternalName())
                    .andStubReturn("order");
            EasyMock.expect(complexMapping.isVirtualAccess())
                    .andStubReturn(false);
            EasyMock.replay(complexMapping);
            EasyMock.expect(edmComplexProperty.getName())
                    .andStubReturn("OrderName");
            EasyMock.expect(edmComplexProperty.getMapping())
                    .andStubReturn(complexMapping);
            EasyMock.expect(edmComplexProperty.getType())
                    .andStubReturn(edmComplexType);
            EasyMock.replay(edmComplexProperty);
            EasyMock.expect(edmType2.getKind())
                    .andStubReturn(EdmTypeKind.COMPLEX);
            EasyMock.expect(edmType2.getProperty("OrderName"))
                    .andStubReturn(edmComplexProperty);
            List<String> propertyNames = new ArrayList<String>();
            propertyNames.add("OrderName");
            EasyMock.expect(edmType2.getPropertyNames())
                    .andStubReturn(propertyNames);
            EasyMock.replay(edmType2);
            EasyMock.expect(mapping2.getInternalName())
                    .andStubReturn("relatedEntity");
            EasyMock.expect(mapping2.isVirtualAccess())
                    .andStubReturn(false);
            EasyMock.replay(mapping2);
            EasyMock.expect(edmProperty2.getName())
                    .andStubReturn("Order");
            EasyMock.expect(edmProperty2.getMapping())
                    .andStubReturn(mapping2);
            EasyMock.expect(edmProperty2.getType())
                    .andStubReturn(edmType2);
            EasyMock.replay(edmProperty2);

        } catch (EdmException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }
        selectPropertyList.add(edmProperty1);
        selectPropertyList.add(edmProperty2);
        try {
            Map<String, Object> result = resultParser.parse2EdmPropertyValueMap(jpaEntity, selectPropertyList);
            assertEquals(1, ((HashMap<String, Object>) result.get("Order")).size());
        } catch (ODataJPARuntimeException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }

    }

    /*
     * TestCase - getGetterName is a private method in JPAResultParser. The method is uses reflection to
     * derive the property access methods from EdmProperty
     */
    @Test
    public void testGetGettersWithOutMapping() {
        JPAEntityParser resultParser = new JPAEntityParser();
        try {

            /*
             * Case 1 - Property having No mapping
             */
            Class<?>[] pars = {String.class, EdmMapping.class, String.class};
            Object[] params = {"Field1", null, "get"};
            Method getGetterName = resultParser.getClass()
                                               .getDeclaredMethod("getAccessModifierName", pars);
            getGetterName.setAccessible(true);
            String name = (String) getGetterName.invoke(resultParser, params);

            assertEquals("getField1", name);

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);

        }
    }

    @Test
    public void testGetGettersWithNullPropname() {
        JPAEntityParser resultParser = new JPAEntityParser();
        try {

            /*
             * Case 1 - Property having No mapping and no name
             */
            Class<?>[] pars = {String.class, EdmMapping.class, String.class};
            Object[] params = {null, null, null};
            Method getGetterName = resultParser.getClass()
                                               .getDeclaredMethod("getAccessModifierName", pars);
            getGetterName.setAccessible(true);

            String name = (String) getGetterName.invoke(resultParser, params);
            assertNull(name);

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);

        }
    }

    /*
     * TestCase - getGetterName is a private method in JPAResultParser. The method is uses reflection to
     * derive the property access methods from EdmProperty
     * 
     * EdmProperty name could have been modified. Then mapping object of EdmProperty should be used for
     * deriving the name
     */
    @Test
    public void testGetGettersWithMapping() {
        JPAEntityParser resultParser = new JPAEntityParser();
        EdmMapping edmMapping = EasyMock.createMock(EdmMapping.class);
        EasyMock.expect(edmMapping.getInternalName())
                .andStubReturn("field1");
        EasyMock.replay(edmMapping);
        try {

            Class<?>[] pars = {String.class, EdmMapping.class, String.class};
            Object[] params = {"myField", edmMapping, "get"};
            Method getGetterName = resultParser.getClass()
                                               .getDeclaredMethod("getAccessModifierName", pars);
            getGetterName.setAccessible(true);

            String name = (String) getGetterName.invoke(resultParser, params);
            assertEquals("getField1", name);

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);

        }
    }

    @Test
    public void testGetGettersNoSuchMethodException() {
        JPAEntityParser resultParser = new JPAEntityParser();
        try {

            Method getGetterName = resultParser.getClass()
                                               .getDeclaredMethod("getGetterName1", EdmProperty.class);
            getGetterName.setAccessible(true);

        } catch (NoSuchMethodException e) {
            assertEquals("org.apache.olingo.odata2.jpa.processor.core.access.data.JPAEntityParser.getGetterName1"
                    + "(org.apache.olingo.odata2.api.edm.EdmProperty)", e.getMessage());
        } catch (SecurityException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);

        }
    }

    @Test
    public void testParse2EdmPropertyValueMap() {
        JPAEntityParser resultParser = new JPAEntityParser();
        Object jpaEntity = new DemoItem2("abc");
        try {
            resultParser.parse2EdmPropertyValueMap(jpaEntity, getEdmPropertyList());
        } catch (ODataJPARuntimeException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }
    }

    @Test
    public void testParse2EdmPropertyValueMapWithVirtualAccess() {
        JPAEntityParser resultParser = new JPAEntityParser();
        Object jpaEntity = new DemoItem2("abc");
        try {
            resultParser.parse2EdmPropertyValueMap(jpaEntity, getEdmPropertyListWithVirtualAccess());
        } catch (ODataJPARuntimeException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }
    }

    @Test
    public void testGetGetterEdmException() {
        JPAEntityParser resultParser = new JPAEntityParser();
        Object jpaEntity = new demoItem("abc", 10);
        EdmStructuralType structuralType = EasyMock.createMock(EdmStructuralType.class);
        try {
            EasyMock.expect(structuralType.getPropertyNames())
                    .andStubThrow(new EdmException(null));
            EasyMock.replay(structuralType);
            Method getGetters = resultParser.getClass()
                                            .getDeclaredMethod("getGetters", Object.class, EdmStructuralType.class);
            getGetters.setAccessible(true);
            try {
                getGetters.invoke(resultParser, jpaEntity, structuralType);
            } catch (IllegalAccessException e) {
                fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
            } catch (IllegalArgumentException e) {
                fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
            } catch (InvocationTargetException e) {
                assertTrue(true);
            }
        } catch (NoSuchMethodException e) {
            assertEquals("org.apache.olingo.odata2.jpa.processor.core.access.data.JPAEntityParser.getGetters(java.lang.Object, "
                    + "org.apache.olingo.odata2.api.edm.EdmStructuralType)", e.getMessage());
        } catch (SecurityException | EdmException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }
    }

    @Test
    public void testForNullJPAEntity() {
        JPAEntityParser resultParser = new JPAEntityParser();
        EdmStructuralType structuralType = EasyMock.createMock(EdmStructuralType.class);
        Object map;
        try {
            map = resultParser.parse2EdmPropertyValueMap(null, structuralType);
            assertNull(map);
        } catch (ODataJPARuntimeException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }
    }

    class demoItem {
        private String id;
        private int value;
        private DemoRelatedEntity relatedEntity;

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public DemoRelatedEntity getRelatedEntity() {
            return relatedEntity;
        }

        public void setRelatedEntity(final DemoRelatedEntity relatedEntity) {
            this.relatedEntity = relatedEntity;
        }

        public int getValue() {
            return value;
        }

        public void set(String property, Object value)
                throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
            this.getClass()
                .getDeclaredField(property)
                .set(this, value);
        }

        public Object get(String property)
                throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
            return this.getClass()
                       .getDeclaredField(property)
                       .get(this);
        }

        public void setValue(final int value) {
            this.value = value;
        }

        demoItem(final String id, final int value) {
            this.id = id;
            this.value = value;
        }

    }

    class DemoRelatedEntity {
        String order;

        public String getOrder() {
            return order;
        }

        public void setOrder(final String order) {
            this.order = order;
        }

        public DemoRelatedEntity(final String order) {
            this.order = order;
        }

    }

    private List<EdmProperty> getEdmPropertyList() {
        List<EdmProperty> properties = new ArrayList<EdmProperty>();
        properties.add(getEdmProperty());
        return properties;
    }

    private List<EdmProperty> getEdmPropertyListWithVirtualAccess() {
        List<EdmProperty> properties = new ArrayList<EdmProperty>();
        properties.add(getEdmPropertyWithVirtualAccess());
        return properties;
    }

    class DemoItem2 {
        private String field1;

        public String getField1() {
            return field1;
        }

        public void setField1(final String field) {
            field1 = field;
        }

        // Getter for Dynamic Entity
        public String get(String fieldName) {
            return field1;
        }

        // Setter for Dynamic Entity
        public void set(final String fieldName, Object fieldValue) {
            field1 = (String) fieldValue;
        }

        public DemoItem2(final String field) {
            field1 = field;
        }

    }

    private EdmProperty getEdmProperty() {
        EdmProperty edmTyped = EasyMock.createMock(EdmProperty.class);

        JPAEdmMappingImpl edmMapping = EasyMock.createMock(JPAEdmMappingImpl.class);
        EasyMock.expect(edmMapping.getInternalName())
                .andStubReturn("Field1");
        EasyMock.expect(edmMapping.isVirtualAccess())
                .andStubReturn(false);
        EasyMock.replay(edmMapping);

        EdmType edmType = EasyMock.createMock(EdmType.class);

        try {
            EasyMock.expect(edmType.getKind())
                    .andStubReturn(EdmTypeKind.SIMPLE);
            EasyMock.expect(edmType.getName())
                    .andStubReturn("identifier");
            EasyMock.expect(edmTyped.getName())
                    .andStubReturn("SalesOrderHeader");
            EasyMock.expect(edmTyped.getMapping())
                    .andStubReturn(edmMapping);

            EasyMock.expect(edmTyped.getType())
                    .andStubReturn(edmType);
            EasyMock.expect(edmTyped.getMapping())
                    .andStubReturn(edmMapping);

        } catch (EdmException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }
        EasyMock.replay(edmType);
        EasyMock.replay(edmTyped);
        return edmTyped;
    }

    private EdmProperty getEdmPropertyWithVirtualAccess() {
        EdmProperty edmTyped = EasyMock.createMock(EdmProperty.class);

        JPAEdmMappingImpl edmMapping = EasyMock.createMock(JPAEdmMappingImpl.class);
        EasyMock.expect(edmMapping.getInternalName())
                .andStubReturn("Field1");
        EasyMock.expect(edmMapping.isVirtualAccess())
                .andStubReturn(true);
        EasyMock.replay(edmMapping);

        EdmSimpleType edmType = EasyMock.createMock(EdmSimpleType.class);

        try {
            EasyMock.expect(edmType.getKind())
                    .andStubReturn(EdmTypeKind.SIMPLE);
            EasyMock.expect(edmType.getName())
                    .andStubReturn("identifier");
            EasyMock.expect(edmTyped.getName())
                    .andStubReturn("SalesOrderHeader");
            EasyMock.expect(edmTyped.getMapping())
                    .andStubReturn(edmMapping);

            EasyMock.expect(edmTyped.getType())
                    .andStubReturn(edmType);
            EasyMock.expect(edmTyped.getMapping())
                    .andStubReturn(edmMapping);

        } catch (EdmException e) {
            fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
        }
        EasyMock.replay(edmType);
        EasyMock.replay(edmTyped);
        return edmTyped;
    }
}
