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
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.uri.NavigationSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
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

  private JPAEdmMapping mockEdmMapping() {
    JPAEdmMappingImpl mockedEdmMapping = new JPAEdmMappingImpl();
    mockedEdmMapping.setODataJPATombstoneEntityListener(JPAQueryExtensionMock.class);
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
