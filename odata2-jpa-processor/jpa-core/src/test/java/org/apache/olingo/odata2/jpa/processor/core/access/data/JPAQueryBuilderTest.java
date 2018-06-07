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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationEnd;
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
  public void buildDeleteEntityTestWithoutListener() {
    try {
      EdmMapping mapping = (EdmMapping) mockMapping();
      assertNotNull(builder.build((DeleteUriInfo) mockURIInfoForDeleteAndPut(mapping)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildPutEntityTestWithoutListener() {
    try {
      EdmMapping mapping = (EdmMapping) mockMapping();
      assertNotNull(builder.build((PutMergePatchUriInfo) mockURIInfoForDeleteAndPut(mapping)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  private DeleteUriInfo mockURIInfoForDeleteAndPut(EdmMapping mapping) throws EdmException {
    UriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
    EasyMock.expect(uriInfo.getNavigationSegments()).andStubReturn(navSegments);
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(edmEntityType.getMapping()).andStubReturn(mapping);
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(edmEntitySet.getEntityType()).andStubReturn(edmEntityType);
    EasyMock.expect(uriInfo.getTargetEntitySet()).andStubReturn(edmEntitySet);
    List<KeyPredicate> keyPreds = new ArrayList<KeyPredicate>();
    
    EdmProperty edmProperty1 = mockEdmProperty(mapping, "Decimal");
    keyPreds.add(mockKeyPredicate(edmProperty1, "1234.7"));
    
    
    EdmProperty edmProperty2 = mockEdmProperty(mapping, "Int64");
    keyPreds.add(mockKeyPredicate(edmProperty2, "1234567899"));
    
    EdmProperty edmProperty3 = mockEdmProperty(mapping, "Double");
    keyPreds.add(mockKeyPredicate(edmProperty3, "12349"));
    
    EdmProperty edmProperty4 = mockEdmProperty(mapping, "Int32");
    keyPreds.add(mockKeyPredicate(edmProperty4, "12349"));
    
    EdmProperty edmProperty5 = mockEdmProperty(mapping, "Single");
    keyPreds.add(mockKeyPredicate(edmProperty5, "12349"));
    
    EdmProperty edmProperty6 = mockEdmProperty(mapping, "SByte");
    keyPreds.add(mockKeyPredicate(edmProperty6, "-123"));
    
    EdmProperty edmProperty7 = mockEdmProperty(mapping, "Binary");
    keyPreds.add(mockKeyPredicate(edmProperty7, getBinaryData()));
    
    EdmProperty edmProperty8 = mockEdmProperty(mapping, "uuid");
    keyPreds.add(mockKeyPredicate(edmProperty8, "56fe79b1-1c88-465b-b309-32bf8b8f6800"));
    
    EasyMock.expect(uriInfo.getKeyPredicates()).andStubReturn(keyPreds); 
    EasyMock.replay(edmEntityType, edmEntitySet, uriInfo);
    return uriInfo;
  }

  private String getBinaryData() {
    byte[] content = new byte[Byte.MAX_VALUE - Byte.MIN_VALUE + 1];
    // binary content, not a valid UTF-8 representation of a string
    for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
      content[i - Byte.MIN_VALUE] = (byte) i;
    }
    return content.toString();
  }
  /**
   * @param edmProperty1
   * @return
   */
  private KeyPredicate mockKeyPredicate(EdmProperty edmProperty, String value) {
    KeyPredicate keyPredicate = EasyMock.createMock(KeyPredicate.class);
    EasyMock.expect(keyPredicate.getLiteral()).andReturn(value).anyTimes();
    EasyMock.expect(keyPredicate.getProperty()).andReturn(edmProperty).anyTimes();
    EasyMock.replay(keyPredicate);
    return keyPredicate;
  }

  /**
   * @param mapping
   * @return
   * @throws EdmException
   */
  private EdmProperty mockEdmProperty(EdmMapping mapping, String type) throws EdmException {
    EdmProperty edmProperty = EasyMock.createMock(EdmProperty.class);
    EasyMock.expect(edmProperty.getMapping()).andStubReturn(mapping);
    if (type.equals("Decimal")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.Decimal.getEdmSimpleTypeInstance());
    } else if (type.equals("Double")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.Double.getEdmSimpleTypeInstance());
    } else if (type.equals("Int64")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.Int64.getEdmSimpleTypeInstance());
    } else if (type.equals("Int32")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.Int32.getEdmSimpleTypeInstance());
    } else if (type.equals("Single")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.Single.getEdmSimpleTypeInstance());
    } else if (type.equals("Int16")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.Int16.getEdmSimpleTypeInstance());
    } else if (type.equals("SByte")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.SByte.getEdmSimpleTypeInstance());
    } else if (type.equals("String")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    } else if (type.equals("DateTime")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.DateTime.getEdmSimpleTypeInstance());
    } else if (type.equals("Time")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.Time.getEdmSimpleTypeInstance());
    } else if (type.equals("Binary")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.Binary.getEdmSimpleTypeInstance());
    } else if (type.equals("uuid")) {
      EasyMock.expect(edmProperty.getType()).andStubReturn(EdmSimpleTypeKind.Guid.getEdmSimpleTypeInstance());
    }
    EasyMock.replay(edmProperty);
    return edmProperty;
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
  
  @Test
  public void buildQueryCountEntitySet() {
    EdmMapping mapping = (EdmMapping) mockMapping();
    try {
      assertNotNull(builder.build((GetEntitySetCountUriInfo) mockURIInfoForEntitySetCount(mapping)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryCountEntity() {
    EdmMapping mapping = (EdmMapping) mockMapping();
    try {
      assertNotNull(builder.build((GetEntityCountUriInfo) mockURIInfoForEntityCount(mapping)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryWithMultipleKeys() {
    EdmMapping mapping = (EdmMapping) mockMapping();
    try {
      assertNotNull(builder.build((GetEntityUriInfo) mockURIInfoWithMultipleKeyPredicates(mapping)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryWithTopSkip() {
    EdmMapping mapping = (EdmMapping) mockMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) mockURIInfoWithTopSkip(mapping)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @Test
  public void buildQueryWithKeyNavSegmentAndFilter() {
    EdmMapping mapping = (EdmMapping) mockMapping();
    try {
      assertNotNull(builder.build((GetEntitySetUriInfo) mockURIInfoWithKeyNavSegAndFilter(mapping)));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }
  
  @SuppressWarnings("unchecked")
  private GetEntityUriInfo mockURIInfoWithTopSkip(EdmMapping mapping) throws EdmException {
    UriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
    EasyMock.expect(uriInfo.getNavigationSegments()).andStubReturn(navSegments);
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(edmEntityType.getMapping()).andStubReturn(mapping);
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(edmEntitySet.getEntityType()).andStubReturn(edmEntityType);
    EasyMock.expect(uriInfo.getTargetEntitySet()).andStubReturn(edmEntitySet);
    List<KeyPredicate> keyPreds = EasyMock.createMock(ArrayList.class);
    EasyMock.expect(uriInfo.getKeyPredicates()).andStubReturn(keyPreds); 
    EasyMock.expect(uriInfo.getOrderBy()).andStubReturn(null);
    EasyMock.expect(uriInfo.getTop()).andStubReturn(1);
    EasyMock.expect(uriInfo.getSkip()).andStubReturn(2);
    EdmProperty edmProperty = EasyMock.createMock(EdmProperty.class);
    EasyMock.expect(edmProperty.getMapping()).andStubReturn(mapping);
    EasyMock.expect(edmEntityType.getKeyProperties()).andStubReturn(Arrays.asList(edmProperty));
    EasyMock.expect(uriInfo.getFilter()).andStubReturn(null);
    EasyMock.replay(edmEntityType, edmEntitySet, uriInfo, keyPreds, edmProperty);
    return uriInfo;
  }

  private UriInfo mockURIInfoWithListener(boolean isNavigationEnabled) throws EdmException {
    UriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    if (isNavigationEnabled) {
      List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
      navSegments.add(null);
      EasyMock.expect(uriInfo.getNavigationSegments()).andStubReturn(navSegments);
      EasyMock.replay(uriInfo);
      return uriInfo;
    }
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(edmEntityType.getMapping()).andStubReturn((EdmMapping) mockEdmMapping());
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(edmEntitySet.getEntityType()).andStubReturn(edmEntityType);
    EasyMock.expect(uriInfo.getTargetEntitySet()).andStubReturn(edmEntitySet);
    EasyMock.expect(uriInfo.getNavigationSegments()).andReturn(null);
    EasyMock.expect(uriInfo.getKeyPredicates()).andStubReturn(null);
    EasyMock.expect(uriInfo.getStartEntitySet()).andStubReturn(edmEntitySet);
    EasyMock.expect(uriInfo.getOrderBy()).andStubReturn(null);
    EasyMock.expect(uriInfo.getFilter()).andStubReturn(null);
    EasyMock.expect(uriInfo.getTop()).andStubReturn(null);
    EasyMock.expect(uriInfo.getSkip()).andStubReturn(null);
    EasyMock.replay(edmEntityType, edmEntitySet, uriInfo);
    return uriInfo;

  }
  
  private UriInfo mockURIInfoWithKeyNavSegAndFilter(EdmMapping mapping) throws EdmException {
    UriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    
    EdmEntityType startEntityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(startEntityType.getMapping()).andStubReturn(mapping);
    
    EdmEntitySet startEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(startEntitySet.getEntityType()).andStubReturn(startEntityType);
    EasyMock.expect(uriInfo.getStartEntitySet()).andStubReturn(startEntitySet);
    
    EdmEntityType targetEntityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(targetEntityType.getMapping()).andStubReturn((EdmMapping) mockNavEdmMappingForProperty());
    
    EdmEntitySet targetEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(targetEntitySet.getEntityType()).andStubReturn(targetEntityType);
    EasyMock.expect(uriInfo.getTargetEntitySet()).andStubReturn(targetEntitySet);
    
    List<KeyPredicate> keyPreds = new ArrayList<KeyPredicate>();
    EdmProperty edmProperty = mockEdmProperty((EdmMapping) mockMappingWithType("uuid"), "uuid");
    keyPreds.add(mockKeyPredicate(edmProperty, "56fe79b1-1c88-465b-b309-33bf8b8f6800"));
    EasyMock.expect(uriInfo.getKeyPredicates()).andStubReturn(keyPreds); 
    
    List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
    EasyMock.expect(uriInfo.getNavigationSegments()).andStubReturn(navSegments);
    NavigationSegment navSegment = EasyMock.createMock(NavigationSegment.class);
    EasyMock.expect(navSegment.getEntitySet()).andStubReturn(targetEntitySet);
    List<KeyPredicate> navKeyPreds = new ArrayList<KeyPredicate>();
    EasyMock.expect(navSegment.getKeyPredicates()).andStubReturn(navKeyPreds);
    EdmNavigationProperty navEdmProperty = EasyMock.createMock(EdmNavigationProperty.class);
    EasyMock.expect(navSegment.getNavigationProperty()).andStubReturn(navEdmProperty);
    EasyMock.expect(navEdmProperty.getMapping()).andStubReturn((EdmMapping)mockNavEdmMappingForProperty());
    EasyMock.expect(navEdmProperty.getFromRole()).andStubReturn("Customers");
    EasyMock.expect(navEdmProperty.getToRole()).andStubReturn("SalesOrderHeader");
    EdmAssociation association = EasyMock.createMock(EdmAssociation.class);
    EasyMock.expect(navEdmProperty.getRelationship()).andStubReturn(association);
    EdmAssociationEnd associationEnd = EasyMock.createMock(EdmAssociationEnd.class);
    EasyMock.expect(associationEnd.getEntityType()).andStubReturn(startEntityType);
    EasyMock.expect(association.getEnd("Customers")).andStubReturn(associationEnd);
    navSegments.add(navSegment);
    
    FilterExpression filterExpression = EasyMock.createMock(FilterExpression.class);
    PropertyExpression propExp = EasyMock.createMock(PropertyExpression.class);
    LiteralExpression literalExp = EasyMock.createMock(LiteralExpression.class);
    EasyMock.expect(uriInfo.getFilter()).andStubReturn(filterExpression);
    BinaryExpression commonExpression = EasyMock.createMock(BinaryExpression.class);
    EasyMock.expect(commonExpression.getOperator()).andStubReturn(BinaryOperator.EQ);
    EasyMock.expect(commonExpression.getKind()).andStubReturn(ExpressionKind.BINARY);
    EasyMock.expect(filterExpression.getExpression()).andStubReturn(commonExpression);
    EasyMock.expect(filterExpression.getKind()).andStubReturn(ExpressionKind.FILTER); 
    EasyMock.expect(filterExpression.getExpressionString()).andStubReturn(
        "Customer eq '56fe79b1-1c88-465b-b309-32bf8b8f7800'"); 
    
    EasyMock.expect(commonExpression.getLeftOperand()).andStubReturn(propExp);
    EasyMock.expect(propExp.getEdmProperty()).andStubReturn(mockEdmProperty(
        (EdmMapping) mockMappingWithType("uuid"), "uuid"));
    EasyMock.expect(propExp.getKind()).andStubReturn(ExpressionKind.PROPERTY);
    EasyMock.expect(propExp.getEdmType()).andStubReturn(EdmSimpleTypeKind.Guid.getEdmSimpleTypeInstance());
    
    EasyMock.expect(commonExpression.getRightOperand()).andStubReturn(literalExp);
    EasyMock.expect(literalExp.getUriLiteral()).andStubReturn("guid'56fe79b1-1c88-465b-b309-32bf8b8f7800'");
    EasyMock.expect(literalExp.getKind()).andStubReturn(ExpressionKind.LITERAL);
    EasyMock.expect(literalExp.getEdmType()).andStubReturn(EdmSimpleTypeKind.Guid.getEdmSimpleTypeInstance());
    
    EasyMock.expect(uriInfo.getOrderBy()).andStubReturn(null);
    EasyMock.expect(uriInfo.getTop()).andStubReturn(null);
    EasyMock.expect(uriInfo.getSkip()).andStubReturn(null);
    
    EasyMock.replay(startEntityType, targetEntityType, startEntitySet, targetEntitySet, uriInfo,
        filterExpression, propExp, literalExp, navSegment, navEdmProperty, 
        commonExpression, association, associationEnd);
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
  
  private UriInfo mockURIInfoWithMultipleKeyPredicates(EdmMapping mapping) throws EdmException {
    
    UriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
    EasyMock.expect(uriInfo.getNavigationSegments()).andStubReturn(navSegments);
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(edmEntityType.getMapping()).andStubReturn(mapping);
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(edmEntitySet.getEntityType()).andStubReturn(edmEntityType);
    EasyMock.expect(uriInfo.getTargetEntitySet()).andStubReturn(edmEntitySet);
    
    List<KeyPredicate> keyPreds = new ArrayList<KeyPredicate>();
    EdmProperty edmProperty1 = mockEdmProperty(mapping, "DateTime");
    keyPreds.add(mockKeyPredicate(edmProperty1, "2012-10-31T18:31:00"));
    
    EdmProperty edmProperty2 = mockEdmProperty(mapping, "Time");
    keyPreds.add(mockKeyPredicate(edmProperty2, "PT0H0M23S"));
    
    EdmProperty edmProperty3 = mockEdmProperty((EdmMapping) mockMappingWithType("Character"), "String");
    keyPreds.add(mockKeyPredicate(edmProperty3, "A"));
    
    EdmProperty edmProperty4 = mockEdmProperty((EdmMapping) mockMappingWithType("char"), "String");
    keyPreds.add(mockKeyPredicate(edmProperty4, "A"));
    
    EdmProperty edmProperty5 = mockEdmProperty((EdmMapping) mockMappingWithType("characterArray"), "String");
    keyPreds.add(mockKeyPredicate(edmProperty5, "ABC"));
    
    EdmProperty edmProperty6 = mockEdmProperty((EdmMapping) mockMappingWithType("charArray"), "String");
    keyPreds.add(mockKeyPredicate(edmProperty6, "ABC"));
    
    EdmProperty edmProperty7 = mockEdmProperty(mapping, "String");
    keyPreds.add(mockKeyPredicate(edmProperty7, "ABC"));
    
    EdmProperty edmProperty8 = mockEdmProperty((EdmMapping) mockMappingWithType("uuid"), "uuid");
    keyPreds.add(mockKeyPredicate(edmProperty8, "56fe79b1-1c88-465b-b309-33bf8b8f6800"));
    
    EasyMock.expect(uriInfo.getKeyPredicates()).andStubReturn(keyPreds); 
    EasyMock.replay(edmEntityType, edmEntitySet, uriInfo);
    return uriInfo;

  }
  
  private List<NavigationSegment> mockNavigationSegments(EdmEntityType edmEntityType, 
      UriInfo uriInfo, EdmEntitySet navEntitySet, EdmEntityType navEntityType) throws EdmException {
    List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
    NavigationSegment navSegment = EasyMock.createMock(NavigationSegment.class);
    EasyMock.expect(navSegment.getEntitySet()).andStubReturn(navEntitySet);
    EasyMock.expect(navSegment.getKeyPredicates()).andStubReturn(new ArrayList<KeyPredicate>());
    EdmNavigationProperty edmNavProperty = EasyMock.createMock(EdmNavigationProperty.class);
    EasyMock.expect(navSegment.getNavigationProperty()).andStubReturn(edmNavProperty);
    EasyMock.expect(edmNavProperty.getFromRole()).andStubReturn("Customers");
    EasyMock.expect(edmNavProperty.getToRole()).andStubReturn("SalesOrderHeader");
    EasyMock.expect(edmNavProperty.getMapping()).andStubReturn((EdmMapping) mockNavEdmMappingForProperty());
    EdmAssociation association = EasyMock.createMock(EdmAssociation.class);
    EasyMock.expect(edmNavProperty.getRelationship()).andStubReturn(association);
    EdmAssociationEnd associationEnd = EasyMock.createMock(EdmAssociationEnd.class);
    EasyMock.expect(associationEnd.getEntityType()).andStubReturn(edmEntityType);
    EasyMock.expect(association.getEnd("Customers")).andStubReturn(associationEnd);
    navSegments.add(navSegment);
    EasyMock.expect(uriInfo.getNavigationSegments()).andStubReturn(navSegments);
    EasyMock.replay(navSegment, edmNavProperty, association, associationEnd);
    return navSegments;
  }
  
  private UriInfo mockURIInfoForEntityCount(EdmMapping mapping) throws EdmException {
    
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    UriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    EdmEntitySet navEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EdmEntityType navEntityType = EasyMock.createMock(EdmEntityType.class);
    List<NavigationSegment> navSegments = mockNavigationSegments(edmEntityType, 
        uriInfo, navEntitySet, navEntityType);
    EasyMock.expect(uriInfo.getNavigationSegments()).andStubReturn(navSegments);
    
    EasyMock.expect(edmEntityType.getMapping()).andStubReturn(mapping);
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(edmEntitySet.getEntityType()).andStubReturn(edmEntityType);
    EasyMock.expect(uriInfo.getStartEntitySet()).andStubReturn(edmEntitySet);
    EasyMock.expect(uriInfo.getTargetEntitySet()).andStubReturn(navEntitySet);
    EasyMock.expect(navEntitySet.getEntityType()).andStubReturn(navEntityType);
    EasyMock.expect(navEntityType.getMapping()).andStubReturn((EdmMapping) mockNavEdmMappingForProperty());
    List<KeyPredicate> keyPreds = new ArrayList<KeyPredicate>();
    EdmProperty edmProperty = mockEdmProperty(mapping, "String");
    keyPreds.add(mockKeyPredicate(edmProperty, "Id"));
    EasyMock.expect(uriInfo.getKeyPredicates()).andStubReturn(keyPreds); 
    
    OrderByExpression orderbyExpression = mockOrderByExpressions(uriInfo);
    EasyMock.replay(edmEntityType, edmEntitySet, uriInfo,  
        navEntitySet, orderbyExpression);
    return uriInfo;

  }

  /**
   * @param uriInfo
   * @return
   */
  private OrderByExpression mockOrderByExpressions(UriInfo uriInfo) {
    OrderByExpression orderbyExpression = EasyMock.createMock(OrderByExpression.class);
    EasyMock.expect(orderbyExpression.getOrders()).andStubReturn(new ArrayList<OrderExpression>());
    EasyMock.expect(uriInfo.getOrderBy()).andStubReturn(orderbyExpression);
    EasyMock.expect(uriInfo.getFilter()).andStubReturn(null);
    return orderbyExpression;
  }
  
  @SuppressWarnings("unchecked")
  private UriInfo mockURIInfoForEntitySetCount(EdmMapping mapping) throws EdmException {
    
    UriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
    EasyMock.expect(uriInfo.getNavigationSegments()).andStubReturn(navSegments);
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(edmEntityType.getMapping()).andStubReturn(mapping);
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EasyMock.expect(edmEntitySet.getEntityType()).andStubReturn(edmEntityType);
    EasyMock.expect(uriInfo.getTargetEntitySet()).andStubReturn(edmEntitySet);
    List<KeyPredicate> keyPreds =EasyMock.createMock(ArrayList.class);
    EasyMock.expect(uriInfo.getKeyPredicates()).andStubReturn(keyPreds); 
    OrderByExpression orderbyExpression = mockOrderByExpressions(uriInfo);
    EasyMock.replay(edmEntityType, edmEntitySet, uriInfo, keyPreds,orderbyExpression);
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
    } else if ("startsWith".equals(methodName)) {
      EasyMock.expect(commonExpression.getMethod()).andStubReturn(MethodOperator.STARTSWITH);
      EasyMock.expect(commonExpression.getParameterCount()).andStubReturn(2);
      EasyMock.expect(propExp.getEdmProperty()).andStubReturn(edmProperty);
      EasyMock.expect(propExp.getKind()).andStubReturn(ExpressionKind.PROPERTY);
      parameterList.add(propExp);
      EasyMock.expect(literalExp.getUriLiteral()).andStubReturn("'a.b.c'");
      EasyMock.expect(literalExp.getKind()).andStubReturn(ExpressionKind.LITERAL);
      EasyMock.expect(literalExp.getEdmType()).andStubReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
      parameterList.add(literalExp);
    } else if ("endsWith".equals(methodName)) {
      EasyMock.expect(commonExpression.getMethod()).andStubReturn(MethodOperator.ENDSWITH);
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
  
  private JPAEdmMapping mockMappingWithType(String type) {
    JPAEdmMappingImpl mockedEdmMapping = new JPAEdmMappingImpl();
    mockedEdmMapping.setInternalName("Customer");
    if (type.equals("Character")) {
      mockedEdmMapping.setJPAType(Character.class);
    } else if (type.equals("char")) {
      mockedEdmMapping.setJPAType(char.class);
    } else if (type.equals("charArray")) {
      mockedEdmMapping.setJPAType(char[].class);
    } else if (type.equals("characterArray")) {
      mockedEdmMapping.setJPAType(Character[].class);
    } else if (type.equals("uuid")) {
      mockedEdmMapping.setJPAType(UUID.class);
    }
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
