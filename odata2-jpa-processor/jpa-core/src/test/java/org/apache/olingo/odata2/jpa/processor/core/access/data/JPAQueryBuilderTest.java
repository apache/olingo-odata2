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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.expression.BinaryExpression;
import org.apache.olingo.odata2.api.uri.expression.BinaryOperator;
import org.apache.olingo.odata2.api.uri.expression.CommonExpression;
import org.apache.olingo.odata2.api.uri.expression.ExpressionKind;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.LiteralExpression;
import org.apache.olingo.odata2.api.uri.expression.MemberExpression;
import org.apache.olingo.odata2.api.uri.expression.MethodExpression;
import org.apache.olingo.odata2.api.uri.expression.MethodOperator;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderExpression;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAQueryExtensionEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAQueryBuilder.JPAQueryInfo;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAQueryBuilder.UriInfoType;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataContextMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataJPAContextMock;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class JPAQueryBuilderTest {
  JPAQueryBuilder builder = null;

  @Before
  public void setup() {
    ODataContextMock odataContextMock = new ODataContextMock();
    ODataContext context;
    try {
      context = odataContextMock.mock();
      ODataJPAContext odataJPAContext = ODataJPAContextMock.mockODataJPAContext(context);
      builder = new JPAQueryBuilder(odataJPAContext);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void buildGetEntityTest() {
    try {
      assertNotNull(builder.build((GetEntityUriInfo) mockURIInfoWithListener(false)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void buildGetEntitySetTest() {
    try {
      JPAQueryInfo info = builder.build((GetEntitySetUriInfo) mockURIInfoWithListener(false));
      assertNotNull(info.getQuery());
      assertEquals(true, info.isTombstoneQuery());
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void buildDeleteEntityTest() {
    try {
      assertNotNull(builder.build((DeleteUriInfo) mockURIInfoWithListener(false)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void buildGetEntitySetCountTest() {
    try {
      assertNotNull(builder.build((GetEntitySetCountUriInfo) mockURIInfoWithListener(false)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void buildGetEntityCountTest() {
    try {
      assertNotNull(builder.build((GetEntityCountUriInfo) mockURIInfoWithListener(false)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void buildPutMergePatchTest() {
    try {
      assertNotNull(builder.build((PutMergePatchUriInfo) mockURIInfoWithListener(false)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void determineGetEntityTest() {
    try {
      assertEquals(JPQLContextType.JOIN_SINGLE, builder.determineJPQLContextType(mockURIInfoWithListener(true),
          UriInfoType.GetEntity));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void determineGetEntityCountTest() {
    try {
      assertEquals(JPQLContextType.JOIN_COUNT, builder.determineJPQLContextType(mockURIInfoWithListener(true),
          UriInfoType.GetEntityCount));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void determineGetEntitySetTest() {
    try {
      assertEquals(JPQLContextType.JOIN, builder.determineJPQLContextType(mockURIInfoWithListener(true),
          UriInfoType.GetEntitySet));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void determineGetEntitySetCountTest() {
    try {
      assertEquals(JPQLContextType.JOIN_COUNT, builder.determineJPQLContextType(mockURIInfoWithListener(true),
          UriInfoType.GetEntitySetCount));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void determinePutMergePatchTest() {
    try {
      assertEquals(JPQLContextType.JOIN_SINGLE, builder.determineJPQLContextType(mockURIInfoWithListener(true),
          UriInfoType.PutMergePatch));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void determineDeleteTest() {
    try {
      assertEquals(JPQLContextType.JOIN_SINGLE, builder.determineJPQLContextType(mockURIInfoWithListener(true),
          UriInfoType.Delete));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void buildQueryGetEntityTest() {
    EdmMapping mapping = (EdmMapping) mockMapping();
    try {
      assertNotNull(builder.build((GetEntityUriInfo) mockURIInfo(mapping)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryGetEntitySetTestWithNoNormalizationWithSubstringof() {
    EdmMapping mapping = (EdmMapping) mockNormalizedValueMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) mockURIInfoForEntitySet(mapping, "substringof")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryGetEntitySetTestWithNormalizationWithSubstringof() {
    EdmMapping mapping = (EdmMapping) mockNormalizedMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) mockURIInfoForEntitySet(mapping, "substringof")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryGetEntitySetTestWithNormalizationWithStartsWith() {
    EdmMapping mapping = (EdmMapping) mockNormalizedMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) mockURIInfoForEntitySet(mapping, "startsWith")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryGetEntitySetTestWithNoNormalizationWithStartsWith() {
    EdmMapping mapping = (EdmMapping) mockNormalizedValueMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) mockURIInfoForEntitySet(mapping, "startsWith")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryGetEntitySetTestWithNormalizationWithEndsWith() {
    EdmMapping mapping = (EdmMapping) mockNormalizedMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) mockURIInfoForEntitySet(mapping, "endsWith")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryGetEntitySetTestWithNoNormalizationWithEndsWith() {
    EdmMapping mapping = (EdmMapping) mockNormalizedValueMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) mockURIInfoForEntitySet(mapping, "endsWith")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryGetEntitySetTestWithNormalizationWithSubstring() {
    EdmMapping mapping = (EdmMapping) mockNormalizedMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) 
          mockURIInfoForEntitySetWithBinaryFilterExpression(mapping, "substring")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryGetEntitySetTestWithNoNormalizationWithSubstring() {
    EdmMapping mapping = (EdmMapping) mockNormalizedValueMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) 
          mockURIInfoForEntitySetWithBinaryFilterExpression(mapping, "substring")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void buildQueryGetEntitySetTestWithNormalizationWithtoLower() {
    EdmMapping mapping = (EdmMapping) mockNormalizedMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) 
          mockURIInfoForEntitySetWithBinaryFilterExpression(mapping, "toLower")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryGetEntitySetTestWithNoNormalizationWithtoLower() {
    EdmMapping mapping = (EdmMapping) mockNormalizedValueMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) 
          mockURIInfoForEntitySetWithBinaryFilterExpression(mapping, "toLower")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryGetEntitySetTestWithNormalizationWithSubstringof1() {
    EdmMapping mapping = (EdmMapping) mockNormalizedMapping1();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) 
          mockURIInfoForEntitySet(mapping, "substringof_1")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryValueNormalizeTest() {
    EdmMapping mapping = (EdmMapping) mockNormalizedValueMapping();
    try {
      assertNotNull(builder.build((GetEntityUriInfo) mockURIInfo(mapping)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void buildQueryNormalizeTest() {
    EdmMapping mapping = (EdmMapping) mockNormalizedMapping();
    try {
      assertNotNull(builder.build((GetEntityUriInfo) mockURIInfo(mapping)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  private UriInfo mockURIInfoWithListener(boolean isNavigationEnabled) throws EdmException {
    UriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    if (isNavigationEnabled) {
      List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
      navSegments.add(null);
      EasyMock.expect(uriInfo.getNavigationSegments()).andReturn(navSegments);
      EasyMock.replay(uriInfo);
      return uriInfo;
    }
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(edmEntityType.getMapping()).andReturn((EdmMapping) mockEdmMapping());
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(edmEntitySet.getEntityType()).andReturn(edmEntityType);
    EasyMock.expect(uriInfo.getTargetEntitySet()).andReturn(edmEntitySet);
    EasyMock.replay(edmEntityType, edmEntitySet, uriInfo);
    return uriInfo;

  }
  
  @SuppressWarnings("unchecked")
  private UriInfo mockURIInfo(EdmMapping mapping) throws EdmException {
    
    UriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
    EasyMock.expect(uriInfo.getNavigationSegments()).andStubReturn(navSegments);
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(edmEntityType.getMapping()).andStubReturn(mapping);
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(edmEntitySet.getEntityType()).andStubReturn(edmEntityType);
    EasyMock.expect(uriInfo.getTargetEntitySet()).andStubReturn(edmEntitySet);
    List<KeyPredicate> keyPreds =EasyMock.createMock(ArrayList.class);
    EdmProperty edmProperty = EasyMock.createMock(EdmProperty.class);
    EasyMock.expect(edmProperty.getMapping()).andStubReturn(mapping);
    KeyPredicate keyPredicate = EasyMock.createMock(KeyPredicate.class);
    EasyMock.expect(keyPredicate.getLiteral()).andReturn("Id").anyTimes();
    EasyMock.expect(keyPredicate.getProperty()).andReturn(edmProperty).anyTimes();
    EasyMock.expect(keyPreds.size()).andStubReturn(1);
    Iterator<KeyPredicate> keyPredicateitr =  EasyMock.createMock(Iterator.class);
    EasyMock.expect(keyPredicateitr.next()).andStubReturn(keyPredicate);
    EasyMock.expect(keyPredicateitr.hasNext()).andStubReturn(true);
    EasyMock.expect(keyPreds.iterator()).andStubReturn(keyPredicateitr);
    EasyMock.expect(keyPreds.isEmpty()).andStubReturn(false);
    EasyMock.expect(keyPreds.get(0)).andStubReturn(keyPredicate);
    EasyMock.expect(uriInfo.getKeyPredicates()).andStubReturn(keyPreds); 
    EasyMock.replay(edmEntityType, edmEntitySet, uriInfo, keyPredicate, keyPreds, edmProperty);
    return uriInfo;

  }
  
  private UriInfo mockURIInfoForEntitySet(EdmMapping mapping, String methodName) throws EdmException {
    
    UriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
    EasyMock.expect(uriInfo.getNavigationSegments()).andStubReturn(navSegments);
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(edmEntityType.getMapping()).andStubReturn(mapping);
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(edmEntitySet.getEntityType()).andStubReturn(edmEntityType);
    EasyMock.expect(uriInfo.getTargetEntitySet()).andStubReturn(edmEntitySet);
    EdmProperty edmProperty = EasyMock.createMock(EdmProperty.class);
    EasyMock.expect(edmProperty.getMapping()).andStubReturn((EdmMapping)mockEdmMappingForProperty());
    
    EdmProperty edmProperty1 = EasyMock.createMock(EdmProperty.class);
    EasyMock.expect(edmProperty1.getMapping()).andStubReturn((EdmMapping)mockEdmMappingForProperty1());
    
    EdmNavigationProperty navEdmProperty = EasyMock.createMock(EdmNavigationProperty.class);
    EasyMock.expect(navEdmProperty.getMapping()).andStubReturn((EdmMapping)mockNavEdmMappingForProperty());
    
    OrderByExpression orderbyExpression = EasyMock.createMock(OrderByExpression.class);
    List<OrderExpression> orders = new ArrayList<OrderExpression>();
    EasyMock.expect(orderbyExpression.getOrders()).andStubReturn(orders);
    EasyMock.expect(uriInfo.getOrderBy()).andStubReturn(orderbyExpression);
    FilterExpression filterExpression = EasyMock.createMock(FilterExpression.class);
    MethodExpression commonExpression = EasyMock.createMock(MethodExpression.class);
    EasyMock.expect(commonExpression.getKind()).andStubReturn(ExpressionKind.METHOD);
    PropertyExpression propExp = EasyMock.createMock(PropertyExpression.class);
    LiteralExpression literalExp = EasyMock.createMock(LiteralExpression.class);
    MemberExpression memberExp = EasyMock.createMock(MemberExpression.class);
    List<CommonExpression> parameterList = new ArrayList<CommonExpression>();
    
    PropertyExpression navPropExp = EasyMock.createMock(PropertyExpression.class);
    if ("substringof".equals(methodName)) {
      EasyMock.expect(commonExpression.getMethod()).andStubReturn(MethodOperator.SUBSTRINGOF);
      EasyMock.expect(commonExpression.getParameterCount()).andStubReturn(2);
      EasyMock.expect(literalExp.getUriLiteral()).andStubReturn("'a.b.c'");
      EasyMock.expect(literalExp.getKind()).andStubReturn(ExpressionKind.LITERAL);
      EasyMock.expect(literalExp.getEdmType()).andStubReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
      parameterList.add(literalExp);
      EasyMock.expect(propExp.getEdmProperty()).andStubReturn(edmProperty);
      EasyMock.expect(propExp.getKind()).andStubReturn(ExpressionKind.PROPERTY);
      parameterList.add(propExp);
    } else if ("startsWith".equals(methodName) || "endsWith".equals(methodName)) {
      EasyMock.expect(commonExpression.getMethod()).andStubReturn(MethodOperator.STARTSWITH);
      EasyMock.expect(commonExpression.getParameterCount()).andStubReturn(2);
      EasyMock.expect(propExp.getEdmProperty()).andStubReturn(edmProperty);
      EasyMock.expect(propExp.getKind()).andStubReturn(ExpressionKind.PROPERTY);
      parameterList.add(propExp);
      EasyMock.expect(literalExp.getUriLiteral()).andStubReturn("'a.b.c'");
      EasyMock.expect(literalExp.getKind()).andStubReturn(ExpressionKind.LITERAL);
      EasyMock.expect(literalExp.getEdmType()).andStubReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
      parameterList.add(literalExp);
    } else if ("substringof_1".equals(methodName)) {
      EasyMock.expect(commonExpression.getMethod()).andStubReturn(MethodOperator.SUBSTRINGOF);
      EasyMock.expect(commonExpression.getParameterCount()).andStubReturn(2);
      EasyMock.expect(literalExp.getUriLiteral()).andStubReturn("'a.b.c'");
      EasyMock.expect(literalExp.getKind()).andStubReturn(ExpressionKind.LITERAL);
      EasyMock.expect(literalExp.getEdmType()).andStubReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
      parameterList.add(literalExp);
      EasyMock.expect(memberExp.getKind()).andStubReturn(ExpressionKind.MEMBER);
      EasyMock.expect(memberExp.getProperty()).andStubReturn(propExp);
      
      EasyMock.expect(memberExp.getPath()).andStubReturn(navPropExp);
      EasyMock.expect(navPropExp.getKind()).andStubReturn(ExpressionKind.PROPERTY);
      EasyMock.expect(navPropExp.getEdmProperty()).andStubReturn(navEdmProperty);
      EasyMock.expect(propExp.getKind()).andStubReturn(ExpressionKind.PROPERTY);
      EasyMock.expect(propExp.getEdmProperty()).andStubReturn(edmProperty1);
      
      parameterList.add(propExp);
    }
    
    EasyMock.expect(commonExpression.getParameters()).andStubReturn(parameterList);
    
    EasyMock.expect(filterExpression.getExpression()).andStubReturn(commonExpression);
    EasyMock.expect(filterExpression.getKind()).andStubReturn(ExpressionKind.FILTER);
    EasyMock.expect(filterExpression.getExpressionString()).andStubReturn("substringof('a.b.c',Id)");
    EasyMock.expect(uriInfo.getFilter()).andStubReturn(filterExpression);
    EasyMock.replay(edmEntityType, edmEntitySet, orderbyExpression, filterExpression, 
        commonExpression, literalExp, propExp, uriInfo, edmProperty, memberExp,
        edmProperty1, navEdmProperty);
    return uriInfo;

  }
  
  private UriInfo mockURIInfoForEntitySetWithBinaryFilterExpression
  (EdmMapping mapping, String methodName) throws EdmException {
    
    UriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
    EasyMock.expect(uriInfo.getNavigationSegments()).andStubReturn(navSegments);
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(edmEntityType.getMapping()).andStubReturn(mapping);
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(edmEntitySet.getEntityType()).andStubReturn(edmEntityType);
    EasyMock.expect(uriInfo.getTargetEntitySet()).andStubReturn(edmEntitySet);
    EdmProperty edmProperty = EasyMock.createMock(EdmProperty.class);
    EasyMock.expect(edmProperty.getMapping()).andStubReturn((EdmMapping)mockEdmMappingForProperty());
    OrderByExpression orderbyExpression = EasyMock.createMock(OrderByExpression.class);
    List<OrderExpression> orders = new ArrayList<OrderExpression>();
    EasyMock.expect(orderbyExpression.getOrders()).andStubReturn(orders);
    EasyMock.expect(uriInfo.getOrderBy()).andStubReturn(orderbyExpression);
    FilterExpression filterExpression = EasyMock.createMock(FilterExpression.class);
    BinaryExpression commonExpression = EasyMock.createMock(BinaryExpression.class);
    EasyMock.expect(commonExpression.getOperator()).andStubReturn(BinaryOperator.EQ);
    EasyMock.expect(commonExpression.getKind()).andStubReturn(ExpressionKind.BINARY);
    MethodExpression methodExp = EasyMock.createMock(MethodExpression.class);
    EasyMock.expect(commonExpression.getLeftOperand()).andStubReturn(methodExp);
    EdmSimpleType type = EasyMock.createMock(EdmSimpleType.class);;
    EasyMock.expect(methodExp.getEdmType()).andStubReturn(type );
    
    LiteralExpression literalValueExp = EasyMock.createMock(LiteralExpression.class);
    EasyMock.expect(commonExpression.getRightOperand()).andStubReturn(literalValueExp);
    EasyMock.expect(literalValueExp.getUriLiteral()).andStubReturn("'a%.b*.c'");
    EasyMock.expect(literalValueExp.getKind()).andStubReturn(ExpressionKind.LITERAL);
    EasyMock.expect(literalValueExp.getEdmType()).andStubReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    
    PropertyExpression propExp = EasyMock.createMock(PropertyExpression.class);
    LiteralExpression literalExp1 = EasyMock.createMock(LiteralExpression.class);
    LiteralExpression literalExp2 = EasyMock.createMock(LiteralExpression.class);
    List<CommonExpression> parameterList = new ArrayList<CommonExpression>();
    if ("substring".equals(methodName)) {
      EasyMock.expect(methodExp.getMethod()).andStubReturn(MethodOperator.SUBSTRING);
      EasyMock.expect(methodExp.getKind()).andStubReturn(ExpressionKind.METHOD);
      EasyMock.expect(methodExp.getParameterCount()).andStubReturn(3);
      EasyMock.expect(propExp.getEdmProperty()).andStubReturn(edmProperty);
      EasyMock.expect(propExp.getKind()).andStubReturn(ExpressionKind.PROPERTY);
      parameterList.add(propExp);
      EasyMock.expect(literalExp1.getUriLiteral()).andStubReturn("1");
      EasyMock.expect(literalExp1.getKind()).andStubReturn(ExpressionKind.LITERAL);
      EasyMock.expect(literalExp1.getEdmType()).andStubReturn(EdmSimpleTypeKind.Int16.getEdmSimpleTypeInstance());
      parameterList.add(literalExp1);
      EasyMock.expect(literalExp2.getUriLiteral()).andStubReturn("2");
      EasyMock.expect(literalExp2.getKind()).andStubReturn(ExpressionKind.LITERAL);
      EasyMock.expect(literalExp2.getEdmType()).andStubReturn(EdmSimpleTypeKind.Int16.getEdmSimpleTypeInstance());
      parameterList.add(literalExp2);
      EasyMock.expect(methodExp.getParameters()).andStubReturn(parameterList);
    } else if ("toLower".equals(methodName)) {
      EasyMock.expect(methodExp.getMethod()).andStubReturn(MethodOperator.TOLOWER);
      EasyMock.expect(methodExp.getKind()).andStubReturn(ExpressionKind.METHOD);
      EasyMock.expect(methodExp.getParameterCount()).andStubReturn(1);
      EasyMock.expect(propExp.getEdmProperty()).andStubReturn(edmProperty);
      EasyMock.expect(propExp.getKind()).andStubReturn(ExpressionKind.PROPERTY);
      parameterList.add(propExp);
      EasyMock.expect(methodExp.getParameters()).andStubReturn(parameterList);
    }
    EasyMock.expect(filterExpression.getExpression()).andStubReturn(commonExpression);
    EasyMock.expect(filterExpression.getKind()).andStubReturn(ExpressionKind.FILTER);
    EasyMock.expect(filterExpression.getExpressionString()).andStubReturn("substring(CompanyName,1,2) eq 'a.b.c'");
    EasyMock.expect(uriInfo.getFilter()).andStubReturn(filterExpression);
    EasyMock.replay(edmEntityType, edmEntitySet, orderbyExpression, filterExpression, 
        commonExpression, literalExp1, literalExp2, propExp, uriInfo, edmProperty, 
        methodExp, literalValueExp);
    return uriInfo;

  }

  private JPAEdmMapping mockEdmMapping() {
    JPAEdmMappingImpl mockedEdmMapping = new JPAEdmMappingImpl();
    mockedEdmMapping.setODataJPATombstoneEntityListener(JPAQueryExtensionMock.class);
    return mockedEdmMapping;
  }
  
  private JPAEdmMapping mockEdmMappingForProperty() {
    JPAEdmMappingImpl mockedEdmMapping = new JPAEdmMappingImpl();
    mockedEdmMapping.setInternalName("CustomerName");
    return mockedEdmMapping;
  }
  
  private JPAEdmMapping mockEdmMappingForProperty1() {
    JPAEdmMappingImpl mockedEdmMapping = new JPAEdmMappingImpl();
    mockedEdmMapping.setInternalName("SalesOrderHeader.CustomerName");
    return mockedEdmMapping;
  }
  
  private JPAEdmMapping mockNavEdmMappingForProperty() {
    JPAEdmMappingImpl mockedEdmMapping = new JPAEdmMappingImpl();
    mockedEdmMapping.setInternalName("SalesOrderHeader");
    return mockedEdmMapping;
  }
  
  private JPAEdmMapping mockMapping() {
    JPAEdmMappingImpl mockedEdmMapping = new JPAEdmMappingImpl();
    mockedEdmMapping.setInternalName("Customer");
    return mockedEdmMapping;
  }
  

  private JPAEdmMapping mockNormalizedMapping() {
    JPAEdmMappingImpl mockedEdmMapping = new JPAEdmMappingImpl();
    mockedEdmMapping.setInternalName("C1.Customer.Name");
    return mockedEdmMapping;
  }
  
  private JPAEdmMapping mockNormalizedMapping1() {
    JPAEdmMappingImpl mockedEdmMapping = new JPAEdmMappingImpl();
    mockedEdmMapping.setInternalName("E1.SalesOrderItem");
    return mockedEdmMapping;
  }
  
  private JPAEdmMapping mockNormalizedValueMapping() {
    JPAEdmMappingImpl mockedEdmMapping = new JPAEdmMappingImpl();
    mockedEdmMapping.setInternalName("'C1.Customer.Name'");
    return mockedEdmMapping;
  }
  
  public static final class JPAQueryExtensionMock extends ODataJPAQueryExtensionEntityListener {
    Query query = EasyMock.createMock(Query.class);

    @Override
    public Query getQuery(GetEntityUriInfo uriInfo, EntityManager em) {
      return query;
    }

    @Override
    public Query getQuery(GetEntitySetUriInfo uriInfo, EntityManager em) {
      return query;
    }

    @Override
    public Query getQuery(GetEntitySetCountUriInfo uriInfo, EntityManager em) {
      return query;
    }

    @Override
    public Query getQuery(DeleteUriInfo uriInfo, EntityManager em) {
      return query;
    }

    @Override
    public Query getQuery(GetEntityCountUriInfo uriInfo, EntityManager em) {
      return query;
    }

    @Override
    public Query getQuery(PutMergePatchUriInfo uriInfo, EntityManager em) {
      return query;
    }

    @Override
    public boolean isTombstoneSupported() {
      return false;
    }
  }
}
