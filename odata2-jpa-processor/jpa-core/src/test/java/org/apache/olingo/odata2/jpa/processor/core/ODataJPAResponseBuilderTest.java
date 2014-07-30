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
package org.apache.olingo.odata2.jpa.processor.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.NavigationPropertySegment;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.SelectItem;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAResponseBuilder;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAPaging;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmTestModelView;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class ODataJPAResponseBuilderTest extends JPAEdmTestModelView {

  private ODataJPAResponseBuilder responseBuilder;

  @Before
  public void init() {
    responseBuilder = new ODataJPAResponseBuilderDefault(getODataJPAContext());
  }

  /*
   * This Unit is supposed to test the building of Entity Provider Properties for query with $expand
   */
  @Test
  public void testGetEntityProviderPropertiesQuery() {
    GetEntitySetUriInfo getEntitySetUriInfo = mockEntitySetUriInfoForExpand();
    ODataJPAContext oDataJPAContext = getODataJPAContext();
    // Building the edm entity
    List<Map<String, Object>> edmEntityList = new ArrayList<Map<String, Object>>();
    Map<String, Object> edmEntity = new HashMap<String, Object>();
    edmEntity.put("ID", 1);
    edmEntityList.add(edmEntity);
    // Invoking the private static method using reflection
    Class<?> clazz = ODataJPAResponseBuilderDefault.class;
    Object[] actualParameters = { oDataJPAContext, getEntitySetUriInfo, edmEntityList };
    Class<?>[] formalParameters = { ODataJPAContext.class, GetEntitySetUriInfo.class, List.class };
    EntityProviderWriteProperties providerProperties = null;
    try {
      Method method = clazz.getDeclaredMethod("getEntityProviderProperties", formalParameters);
      method.setAccessible(true);
      providerProperties = (EntityProviderWriteProperties) method.invoke(responseBuilder, actualParameters);
      assertEquals(1, providerProperties.getExpandSelectTree().getLinks().size());
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchMethodException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (InvocationTargetException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  /*
   * This Unit is supposed to test the building of Entity Provider Properties for read with $expand
   */
  @Test
  public void testGetEntityProviderPropertiesRead() {

    // Getting the EntityUriInfo
    GetEntityUriInfo getEntityUriInfo = mockEntityUriInfoForExpand();
    ODataJPAContext oDataJPAContext = getODataJPAContext();
    Class<?> clazz = ODataJPAResponseBuilderDefault.class;
    Object[] actualParameters = { oDataJPAContext, getEntityUriInfo };
    Class<?>[] formalParameters = { ODataJPAContext.class, GetEntityUriInfo.class };
    EntityProviderWriteProperties providerProperties = null;
    try {
      Method method = clazz.getDeclaredMethod("getEntityProviderProperties", formalParameters);
      method.setAccessible(true);
      providerProperties = (EntityProviderWriteProperties) method.invoke(responseBuilder, actualParameters);
      assertEquals(1, providerProperties.getExpandSelectTree().getLinks().size());
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchMethodException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (InvocationTargetException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

  }

  @SuppressWarnings("unchecked")
  @Test
  public void testConstructListofNavProperty() {
    List<ArrayList<NavigationPropertySegment>> expand = new ArrayList<ArrayList<NavigationPropertySegment>>();
    ArrayList<NavigationPropertySegment> navPropList1 = new ArrayList<NavigationPropertySegment>();
    navPropList1.add(getNavigationPropertySegment("DemoNavigationProperties11"));
    navPropList1.add(getNavigationPropertySegment("DemoNavigationProperties12"));
    expand.add(navPropList1);
    ArrayList<NavigationPropertySegment> navPropList2 = new ArrayList<NavigationPropertySegment>();
    navPropList2.add(getNavigationPropertySegment("DemoNavigationProperties21"));
    navPropList2.add(getNavigationPropertySegment("DemoNavigationProperties22"));
    expand.add(navPropList2);
    Class<?> clazz = ODataJPAResponseBuilderDefault.class;
    Object[] actualParameters = { expand };
    Class<?>[] formalParameters = { List.class };
    List<EdmNavigationProperty> navigationProperties = null;
    try {
      Method method = clazz.getDeclaredMethod("constructListofNavProperty", formalParameters);
      method.setAccessible(true);
      navigationProperties = (List<EdmNavigationProperty>) method.invoke(responseBuilder, actualParameters);
      assertEquals("DemoNavigationProperties21", navigationProperties.get(1).getName());
      assertEquals("DemoNavigationProperties11", navigationProperties.get(0).getName());
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchMethodException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (InvocationTargetException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

  }

  @Test
  public void testBuildListOfTGetEntitySetUriInfoStringODataJPAContext() {
    try {
      assertNotNull(responseBuilder.build(getResultsView(), getJPAEntities(), "application/xml"));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

  }

  @Test
  public void testBuildNegatives() {// Bad content type
    try {
      EntityType entity = new EntityType();
      entity.setName("SalesOrderHeader");
      try {
        assertNotNull(responseBuilder.build(getLocalGetURIInfo(), getEntity(), "xml"));
      } catch (ODataNotFoundException e) {
        assertTrue(true);
      }
    } catch (ODataJPARuntimeException e) {
      assertTrue(true);// Nothing to do, Expected.
    }
    try {// Bad content type
      assertNotNull(responseBuilder.build(getResultsView(), getJPAEntities(), "xml"));
    } catch (ODataJPARuntimeException e) {
      assertTrue(true);// Nothing to do, Expected.
    }

  }

  @Test
  public void testBuildObjectGetEntityUriInfoStringODataJPAContext() throws ODataNotFoundException {
    try {
      assertNotNull(responseBuilder.build(getLocalGetURIInfo(), (Object) new SalesOrderHeader(2, 10),
          "application/xml"));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testBuildNullSelects() {// Bad content type
    try {
      responseBuilder.build(getResultsViewWithNullSelects(), getJPAEntities(), "xml");
    } catch (ODataJPARuntimeException e) {
      assertTrue(true);// Nothing to do, Expected.
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  public void testBuildGetCount() {
    ODataResponse objODataResponse = null;
    try {
      objODataResponse = responseBuilder.build(1);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertNotNull(objODataResponse);
  }

  private ODataJPAContext getODataJPAContext() {
    ODataJPAContext objODataJPAContext = EasyMock.createMock(ODataJPAContext.class);
    EasyMock.expect(objODataJPAContext.getODataContext()).andStubReturn(getLocalODataContext());
    EasyMock.expect(objODataJPAContext.getPageSize()).andReturn(10);
    EasyMock.expect(objODataJPAContext.getPaging()).andReturn(mockJPAPaging()).anyTimes();
    EasyMock.replay(objODataJPAContext);
    return objODataJPAContext;
  }

  private ODataContext getLocalODataContext() {
    ODataContext objODataContext = EasyMock.createMock(ODataContext.class);
    try {
      EasyMock.expect(objODataContext.getPathInfo()).andStubReturn(getLocalPathInfo());
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    EasyMock.replay(objODataContext);
    return objODataContext;
  }

  private JPAPaging mockJPAPaging() {
    JPAPaging paging = new JPAPaging() {

      @Override
      public int getStartPage() {
        return 0;
      }

      @Override
      public List<Object> getPagedEntities() {
        return null;
      }

      @Override
      public int getPageSize() {
        return 10;
      }

      @Override
      public int getNextPage() {
        return 10;
      }
    };

    return paging;
  }

  private PathInfo getLocalPathInfo() {
    PathInfo pathInfo = EasyMock.createMock(PathInfo.class);
    EasyMock.expect(pathInfo.getServiceRoot()).andStubReturn(getLocalURI());
    EasyMock.expect(pathInfo.getRequestUri()).andStubReturn(getRequestURI());
    EasyMock.replay(pathInfo);
    return pathInfo;
  }

  private URI getRequestURI() {
    URI uri = null;
    try {
      uri = new URI("SalesOrders");
    } catch (URISyntaxException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    return uri;
  }

  private URI getLocalURI() {
    URI uri = null;
    try {
      uri = new URI("http://localhost:8080/org.apache.olingo.odata2.processor.ref.web/");
    } catch (URISyntaxException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    return uri;
  }

  private GetEntitySetUriInfo getResultsView() {
    GetEntitySetUriInfo objGetEntitySetUriInfo = EasyMock.createMock(GetEntitySetUriInfo.class);
    EasyMock.expect(objGetEntitySetUriInfo.getInlineCount()).andStubReturn(getLocalInlineCount());
    EasyMock.expect(objGetEntitySetUriInfo.getTargetEntitySet()).andStubReturn(getLocalTargetEntitySet());
    EasyMock.expect(objGetEntitySetUriInfo.getSelect()).andStubReturn(getSelectItemList());
    EasyMock.expect(objGetEntitySetUriInfo.getExpand()).andStubReturn(getExpandList());
    EasyMock.expect(objGetEntitySetUriInfo.getSkip()).andStubReturn(new Integer(1));
    EasyMock.replay(objGetEntitySetUriInfo);
    return objGetEntitySetUriInfo;
  }

  private List<ArrayList<NavigationPropertySegment>> getExpandList() {
    List<ArrayList<NavigationPropertySegment>> expandList = new ArrayList<ArrayList<NavigationPropertySegment>>();
    return expandList;
  }

  private GetEntitySetUriInfo getResultsViewWithNullSelects() {
    GetEntitySetUriInfo objGetEntitySetUriInfo = EasyMock.createMock(GetEntitySetUriInfo.class);
    EasyMock.expect(objGetEntitySetUriInfo.getInlineCount()).andStubReturn(getLocalInlineCount());
    EasyMock.expect(objGetEntitySetUriInfo.getTargetEntitySet()).andStubReturn(getLocalTargetEntitySet());
    EasyMock.expect(objGetEntitySetUriInfo.getSelect()).andStubReturn(null);
    EasyMock.expect(objGetEntitySetUriInfo.getExpand()).andStubReturn(null);
    EasyMock.expect(objGetEntitySetUriInfo.getSkip()).andStubReturn(new Integer(1));

    EasyMock.replay(objGetEntitySetUriInfo);
    return objGetEntitySetUriInfo;
  }

  private GetEntityUriInfo getLocalGetURIInfo() {
    GetEntityUriInfo objGetEntityUriInfo = EasyMock.createMock(GetEntityUriInfo.class);
    EasyMock.expect(objGetEntityUriInfo.getSelect()).andStubReturn(getSelectItemList());
    EasyMock.expect(objGetEntityUriInfo.getTargetEntitySet()).andStubReturn(getLocalTargetEntitySet());
    EasyMock.expect(objGetEntityUriInfo.getExpand()).andStubReturn(getExpandList());
    EasyMock.replay(objGetEntityUriInfo);
    return objGetEntityUriInfo;
  }

  private List<SelectItem> getSelectItemList() {
    List<SelectItem> selectItems = new ArrayList<SelectItem>();
    selectItems.add(getSelectItem());
    return selectItems;
  }

  private SelectItem getSelectItem() {
    SelectItem selectItem = EasyMock.createMock(SelectItem.class);
    EasyMock.expect(selectItem.getProperty()).andStubReturn(getEdmPropertyForSelect());
    List<NavigationPropertySegment> navigationSegmentList = new ArrayList<NavigationPropertySegment>();
    EasyMock.expect(selectItem.getNavigationPropertySegments()).andStubReturn(navigationSegmentList);
    EasyMock.expect(selectItem.isStar()).andReturn(false).anyTimes();
    EasyMock.replay(selectItem);
    return selectItem;
  }

  private EdmProperty getEdmPropertyForSelect() {
    EdmSimpleType edmType = EasyMock.createMock(EdmSimpleType.class);
    EasyMock.expect(edmType.getKind()).andStubReturn(EdmTypeKind.SIMPLE);
    Facets facets = new Facets().setNullable(false);
    try {
      EasyMock.expect(edmType.valueToString(new Integer(2), EdmLiteralKind.URI, facets)).andStubReturn("2");
      EasyMock.expect(edmType.valueToString(new Integer(2), EdmLiteralKind.DEFAULT, facets)).andStubReturn("2");
    } catch (EdmSimpleTypeException e1) {
      fail("There is an exception in mocking EdmType object " + e1.getMessage());
    }
    EasyMock.replay(edmType);
    EdmProperty edmProperty = EasyMock.createMock(EdmProperty.class);
    EdmMapping edmMapping = EasyMock.createMock(EdmMapping.class);
    EasyMock.expect(edmMapping.getInternalName()).andStubReturn("soId");
    EasyMock.expect(edmMapping.getMediaResourceMimeTypeKey()).andReturn(null);
    EasyMock.replay(edmMapping);
    try {
      EasyMock.expect(edmProperty.getName()).andStubReturn("ID");
      EasyMock.expect(edmProperty.getType()).andStubReturn(edmType);
      EasyMock.expect(edmProperty.getMapping()).andStubReturn(edmMapping);
      EasyMock.expect(edmProperty.getFacets()).andStubReturn(facets);
      EasyMock.expect(edmProperty.getCustomizableFeedMappings()).andStubReturn(null);
      EasyMock.expect(edmProperty.getMimeType()).andStubReturn(null);
      EasyMock.replay(edmProperty);

    } catch (EdmException e) {
      fail("There is an exception in mocking some object " + e.getMessage());
    }

    return edmProperty;

  }

  private EdmEntitySet getLocalTargetEntitySet() {
    EdmEntitySet objEdmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    try {
      EasyMock.expect(objEdmEntitySet.getEntityType()).andStubReturn(getLocalEdmEntityType());
      EasyMock.expect(objEdmEntitySet.getName()).andStubReturn("SalesOderHeaders");
      EasyMock.expect(objEdmEntitySet.getEntityContainer()).andStubReturn(getLocalEdmEntityContainer());
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    EasyMock.replay(objEdmEntitySet);
    return objEdmEntitySet;
  }

  private EdmEntityContainer getLocalEdmEntityContainer() {
    EdmEntityContainer edmEntityContainer = EasyMock.createMock(EdmEntityContainer.class);
    EasyMock.expect(edmEntityContainer.isDefaultEntityContainer()).andStubReturn(true);
    try {
      EasyMock.expect(edmEntityContainer.getName()).andStubReturn("salesorderprocessingContainer");
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    EasyMock.replay(edmEntityContainer);
    return edmEntityContainer;
  }

  private EdmEntityType getLocalEdmEntityType() {
    EdmEntityType objEdmEntityType = EasyMock.createMock(EdmEntityType.class);
    try {
      EasyMock.expect(objEdmEntityType.getName()).andStubReturn("SalesOderHeaders");
      EasyMock.expect(objEdmEntityType.getNamespace()).andStubReturn("SalesOderHeaders");
      EasyMock.expect(objEdmEntityType.hasStream()).andStubReturn(false);
      EasyMock.expect(objEdmEntityType.hasStream()).andStubReturn(false);
      ArrayList<String> propertyNames = new ArrayList<String>();
      propertyNames.add("ID");
      EasyMock.expect(objEdmEntityType.getProperty("ID")).andStubReturn(getEdmPropertyForSelect());
      EasyMock.expect(objEdmEntityType.getPropertyNames()).andStubReturn(propertyNames);
      EasyMock.expect(objEdmEntityType.getNavigationPropertyNames()).andStubReturn(new ArrayList<String>());
      EasyMock.expect(objEdmEntityType.getKeyPropertyNames()).andStubReturn(propertyNames);
      EasyMock.expect(objEdmEntityType.getKeyProperties()).andStubReturn(getKeyProperties());
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    EasyMock.replay(objEdmEntityType);
    return objEdmEntityType;
  }

  private List<EdmProperty> getKeyProperties() {
    List<EdmProperty> edmProperties = new ArrayList<EdmProperty>();
    EdmType edmType = EasyMock.createMock(EdmType.class);
    EasyMock.expect(edmType.getKind()).andStubReturn(EdmTypeKind.SIMPLE);
    EasyMock.replay(edmType);
    EdmProperty edmProperty = EasyMock.createMock(EdmProperty.class);
    EdmMapping edmMapping = EasyMock.createMock(EdmMapping.class);
    EasyMock.expect(edmMapping.getInternalName()).andStubReturn("soId");
    EasyMock.replay(edmMapping);
    try {
      EasyMock.expect(edmProperty.getName()).andStubReturn("ID");
      EasyMock.expect(edmProperty.getType()).andStubReturn(edmType);
      EasyMock.expect(edmProperty.getMapping()).andStubReturn(edmMapping);
      EasyMock.replay(edmProperty);
    } catch (EdmException e) {
      fail("There is an exception is mocking some object " + e.getMessage());
    }

    edmProperties.add(edmProperty);
    return edmProperties;
  }

  private InlineCount getLocalInlineCount() {
    return InlineCount.NONE;
  }

  class SalesOrderHeader {
    private int soId;
    private int Field1;

    public SalesOrderHeader(final int soId, final int field) {
      this.soId = soId;
      Field1 = field;
    }

    public int getField1() {
      return Field1;
    }

    public void setField1(final int field1) {
      Field1 = field1;
    }

    public int getSoId() {
      return soId;
    }

    public void setSoId(final int soId) {
      this.soId = soId;
    }

  }

  private List<Object> getJPAEntities() {
    List<Object> listJPAEntities = new ArrayList<Object>();
    SalesOrderHeader entity;
    entity = new SalesOrderHeader(2, 10);
    listJPAEntities.add(entity);
    return listJPAEntities;
  }

  private Object getEntity() {
    SalesOrderHeader sHeader = new SalesOrderHeader(10, 34);
    return sHeader;
  }

  private GetEntityUriInfo mockEntityUriInfoForExpand() {

    List<SelectItem> selectItemList = new ArrayList<SelectItem>();
    List<ArrayList<NavigationPropertySegment>> expandList = new ArrayList<ArrayList<NavigationPropertySegment>>();
    ArrayList<NavigationPropertySegment> navigationPropertyList = new ArrayList<NavigationPropertySegment>();
    // Mocking the navigation property
    EdmNavigationProperty navigationProperty = EasyMock.createMock(EdmNavigationProperty.class);
    try {
      EasyMock.expect(navigationProperty.getName()).andStubReturn("SalesOrderItemDetails");
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    EasyMock.replay(navigationProperty);
    // Mocking the navigation property segments and adding to expand list
    NavigationPropertySegment navigationPropertySegment = EasyMock.createMock(NavigationPropertySegment.class);
    EasyMock.expect(navigationPropertySegment.getNavigationProperty()).andStubReturn(navigationProperty);
    EasyMock.expect(navigationPropertySegment.getTargetEntitySet()).andStubReturn(getTargetEntitySetForExpand());
    EasyMock.replay(navigationPropertySegment);
    navigationPropertyList.add(navigationPropertySegment);
    expandList.add(navigationPropertyList);
    // Mocking EntityUriInfo
    GetEntityUriInfo entityUriInfo = EasyMock.createMock(GetEntityUriInfo.class);
    EasyMock.expect(entityUriInfo.getSelect()).andStubReturn(selectItemList);
    EasyMock.expect(entityUriInfo.getExpand()).andStubReturn(expandList);
    EasyMock.replay(entityUriInfo);
    return entityUriInfo;
  }

  private GetEntitySetUriInfo mockEntitySetUriInfoForExpand() {

    List<SelectItem> selectItemList = new ArrayList<SelectItem>();
    List<ArrayList<NavigationPropertySegment>> expandList = new ArrayList<ArrayList<NavigationPropertySegment>>();
    ArrayList<NavigationPropertySegment> navigationPropertyList = new ArrayList<NavigationPropertySegment>();
    // Mocking the navigation property
    EdmNavigationProperty navigationProperty = EasyMock.createMock(EdmNavigationProperty.class);
    try {
      EasyMock.expect(navigationProperty.getName()).andStubReturn("SalesOrderItemDetails");
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    EasyMock.replay(navigationProperty);
    // Mocking the navigation property segments and adding to expand list
    NavigationPropertySegment navigationPropertySegment = EasyMock.createMock(NavigationPropertySegment.class);
    EasyMock.expect(navigationPropertySegment.getNavigationProperty()).andStubReturn(navigationProperty);
    EasyMock.expect(navigationPropertySegment.getTargetEntitySet()).andStubReturn(getTargetEntitySetForExpand());
    EasyMock.replay(navigationPropertySegment);
    navigationPropertyList.add(navigationPropertySegment);
    expandList.add(navigationPropertyList);
    // Mocking EntityUriInfo
    GetEntitySetUriInfo entitySetUriInfo = EasyMock.createMock(GetEntitySetUriInfo.class);
    EasyMock.expect(entitySetUriInfo.getSelect()).andStubReturn(selectItemList);
    EasyMock.expect(entitySetUriInfo.getExpand()).andStubReturn(expandList);
    EasyMock.expect(entitySetUriInfo.getInlineCount()).andStubReturn(InlineCount.ALLPAGES);
    EasyMock.expect(entitySetUriInfo.getSkip()).andStubReturn(new Integer(1));
    EasyMock.expect(entitySetUriInfo.getTop()).andStubReturn(new Integer(2));
    EasyMock.replay(entitySetUriInfo);
    return entitySetUriInfo;
  }

  private EdmEntitySet getTargetEntitySetForExpand() {
    EdmEntitySet entitySet = EasyMock.createMock(EdmEntitySet.class);
    try {
      EasyMock.expect(entitySet.getName()).andStubReturn("SalesOrderHeaders");
      EasyMock.expect(entitySet.getEntityType()).andStubReturn(null);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    EasyMock.replay(entitySet);
    return entitySet;
  }

  private NavigationPropertySegment getNavigationPropertySegment(final String navPropertyName) {
    EdmNavigationProperty navigationProperty = EasyMock.createMock(EdmNavigationProperty.class);
    try {
      EasyMock.expect(navigationProperty.getName()).andStubReturn(navPropertyName);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    EasyMock.replay(navigationProperty);
    NavigationPropertySegment navPropertySegment = EasyMock.createMock(NavigationPropertySegment.class);
    EasyMock.expect(navPropertySegment.getNavigationProperty()).andStubReturn(navigationProperty);
    EasyMock.replay(navPropertySegment);
    return navPropertySegment;
  }

}
