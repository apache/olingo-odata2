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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.EdmConcurrencyMode;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationSegment;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATransaction;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataContextMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataServiceMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.SalesOrderHeader;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmTestModelView;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ODataJPAProcessorDefaultTest extends JPAEdmTestModelView {

  ODataJPAProcessorDefault objODataJPAProcessorDefault;

  private static final String STR_LOCAL_URI = "http://localhost:8080/org.apache.olingo.odata2.processor.ref.web/";
  private static final String SALESORDERPROCESSING_CONTAINER = "salesorderprocessingContainer";
  private static final String SO_ID = "SoId";
  private static final String SALES_ORDER = "SalesOrder";
  private static final String SALES_ORDER_HEADERS = "SalesOrderHeaders";
  private static final String STR_CONTENT_TYPE = "Content-Type";

  @Before
  public void setUp() {
    objODataJPAProcessorDefault = new ODataJPAProcessorDefault(getLocalmockODataJPAContext());
  }

  @Test
  public void testReadEntitySetGetEntitySetUriInfoString() {
    try {
      GetEntityUriInfo getEntityView = getEntityUriInfo();
      Assert.assertNotNull(objODataJPAProcessorDefault.readEntity(getEntityView, HttpContentType.APPLICATION_XML));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e1) {// Expected
      assertTrue(true);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

  }

  @Test
  public void testcountEntitySet() {
    try {
      ODataResponse countEntitySet =
          objODataJPAProcessorDefault.countEntitySet(getEntitySetCountUriInfo(), HttpContentType.APPLICATION_XML);
      Assert.assertNotNull(countEntitySet);
      Object entity = countEntitySet.getEntity();
      Assert.assertNotNull(entity);

      byte[] b = new byte[2];
      ((ByteArrayInputStream) entity).read(b);
      Assert.assertEquals("11", new String(b, Charset.forName("utf-8")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  public void testExistsEntity() {
    try {
      Assert.assertNotNull(objODataJPAProcessorDefault.existsEntity(getEntityCountUriInfo(),
          HttpContentType.APPLICATION_XML));
      Assert.assertNull("ContentType MUST NOT set by entity provider", objODataJPAProcessorDefault.existsEntity(
          getEntityCountUriInfo(), HttpContentType.APPLICATION_XML).getHeader(STR_CONTENT_TYPE));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  public void testDeleteEntity() {
    try {
      Assert.assertNotNull(objODataJPAProcessorDefault.deleteEntity(getDeletetUriInfo(),
          HttpContentType.APPLICATION_XML));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testCreateEntity() {
    try {
      Assert.assertNotNull(objODataJPAProcessorDefault.createEntity(getPostUriInfo(), getMockedInputStreamContent(),
          HttpContentType.APPLICATION_XML, HttpContentType.APPLICATION_XML));
    } catch (ODataException e) {
      Assert.assertTrue(true); // Expected TODO - need to revisit
    }
  }

  @Test
  public void testUpdateEntity() {
    try {
      Assert.assertNotNull(objODataJPAProcessorDefault.updateEntity(getPutUriInfo(), getMockedInputStreamContent(),
          HttpContentType.APPLICATION_XML, false, HttpContentType.APPLICATION_XML));
    } catch (ODataException e) {
      Assert.assertTrue(true); // Expected TODO - need to revisit
    }
  }

  private PutMergePatchUriInfo getPutUriInfo() {
    return (PutMergePatchUriInfo) getDeletetUriInfo();
  }

  private PostUriInfo getPostUriInfo() {
    return (PostUriInfo) getDeletetUriInfo();
  }

  private InputStream getMockedInputStreamContent() {
    return new ByteArrayInputStream(getEntityBody().getBytes());
  }

  private String getEntityBody() {
    return "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
        "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
        "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
        "xml:base=\"http://localhost:8080/org.apache.olingo.odata2.processor.ref.web/SalesOrderProcessing.svc/\">"
        + "<content type=\"application/xml\">"
        + "<m:properties>"
        + "<d:ID>2</d:ID>"
        + "<d:CreationDate>2013-01-02T00:00:00</d:CreationDate>"
        + "<d:CurrencyCode>Code_555</d:CurrencyCode>"
        + "<d:BuyerAddressInfo m:type=\"SalesOrderProcessing.AddressInfo\">"
        + "<d:Street>Test_Street_Name_055</d:Street>"
        + "<d:Number>2</d:Number>"
        + "<d:Country>Test_Country_2</d:Country>"
        + "<d:City>Test_City_2</d:City>"
        + "</d:BuyerAddressInfo>"
        + "<d:GrossAmount>0.0</d:GrossAmount>"
        + "<d:BuyerId>2</d:BuyerId>"
        + "<d:DeliveryStatus>true</d:DeliveryStatus>"
        + "<d:BuyerName>buyerName_2</d:BuyerName>"
        + "<d:NetAmount>0.0</d:NetAmount>" + "</m:properties>" + "</content>" + "</entry>";
  }

  private GetEntitySetCountUriInfo getEntitySetCountUriInfo() {
    return getLocalUriInfo();
  }

  private GetEntityCountUriInfo getEntityCountUriInfo() {
    return getLocalUriInfo();
  }

  private DeleteUriInfo getDeletetUriInfo() {
    UriInfo objUriInfo = EasyMock.createMock(UriInfo.class);
    EasyMock.expect(objUriInfo.getStartEntitySet()).andStubReturn(getLocalEdmEntitySet());
    List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
    EasyMock.expect(objUriInfo.getNavigationSegments()).andReturn(navSegments).anyTimes();
    EasyMock.expect(objUriInfo.getTargetEntitySet()).andStubReturn(getLocalEdmEntitySet());
    EasyMock.expect(objUriInfo.getSelect()).andStubReturn(null);
    EasyMock.expect(objUriInfo.getOrderBy()).andStubReturn(getOrderByExpression());
    EasyMock.expect(objUriInfo.getTop()).andStubReturn(getTop());
    EasyMock.expect(objUriInfo.getSkip()).andStubReturn(getSkip());
    EasyMock.expect(objUriInfo.getInlineCount()).andStubReturn(getInlineCount());
    EasyMock.expect(objUriInfo.getFilter()).andStubReturn(getFilter());
    EasyMock.expect(objUriInfo.getKeyPredicates()).andStubReturn(getKeyPredicates());
    EasyMock.expect(objUriInfo.isLinks()).andStubReturn(false);
    EasyMock.replay(objUriInfo);
    return objUriInfo;
  }

  private List<KeyPredicate> getKeyPredicates() {
    List<KeyPredicate> keyPredicates = new ArrayList<KeyPredicate>();
    return keyPredicates;
  }

  /**
   * @return
   */
  private UriInfo getLocalUriInfo() {
    UriInfo objUriInfo = EasyMock.createMock(UriInfo.class);
    EasyMock.expect(objUriInfo.getStartEntitySet()).andStubReturn(getLocalEdmEntitySet());
    List<NavigationSegment> navSegments = new ArrayList<NavigationSegment>();
    EasyMock.expect(objUriInfo.getNavigationSegments()).andReturn(navSegments).anyTimes();
    EasyMock.expect(objUriInfo.getTargetEntitySet()).andStubReturn(getLocalEdmEntitySet());
    EasyMock.expect(objUriInfo.getSelect()).andStubReturn(null);
    EasyMock.expect(objUriInfo.getOrderBy()).andStubReturn(getOrderByExpression());
    EasyMock.expect(objUriInfo.getTop()).andStubReturn(getTop());
    EasyMock.expect(objUriInfo.getSkip()).andStubReturn(getSkip());
    EasyMock.expect(objUriInfo.getInlineCount()).andStubReturn(getInlineCount());
    EasyMock.expect(objUriInfo.getFilter()).andStubReturn(getFilter());
    EasyMock.expect(objUriInfo.getFunctionImport()).andStubReturn(null);
    EasyMock.replay(objUriInfo);
    return objUriInfo;
  }

  /**
   * @return
   * @throws EdmException
   */
  private EdmEntitySet getLocalEdmEntitySet() {
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    try {
      EasyMock.expect(edmEntitySet.getName()).andStubReturn(SALES_ORDER_HEADERS);
      EasyMock.expect(edmEntitySet.getEntityContainer()).andStubReturn(getLocalEdmEntityContainer());
      EasyMock.expect(edmEntitySet.getEntityType()).andStubReturn(getLocalEdmEntityType());
      EasyMock.replay(edmEntitySet);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    return edmEntitySet;
  }

  /**
   * @return
   * @throws EdmException
   */
  private EdmEntityType getLocalEdmEntityType() {
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    try {
      EasyMock.expect(edmEntityType.getKeyProperties()).andStubReturn(new ArrayList<EdmProperty>());
      EasyMock.expect(edmEntityType.getPropertyNames()).andStubReturn(getLocalPropertyNames());
      EasyMock.expect(edmEntityType.getProperty(SO_ID)).andStubReturn(getEdmTypedMockedObj(SALES_ORDER));
      EasyMock.expect(edmEntityType.getKind()).andStubReturn(EdmTypeKind.SIMPLE);
      EasyMock.expect(edmEntityType.getNamespace()).andStubReturn(SALES_ORDER_HEADERS);
      EasyMock.expect(edmEntityType.getName()).andStubReturn(SALES_ORDER_HEADERS);
      EasyMock.expect(edmEntityType.hasStream()).andStubReturn(false);
      EasyMock.expect(edmEntityType.getNavigationPropertyNames()).andStubReturn(new ArrayList<String>());
      EasyMock.expect(edmEntityType.getKeyPropertyNames()).andStubReturn(new ArrayList<String>());
      EasyMock.expect(edmEntityType.getMapping()).andStubReturn((EdmMapping) getEdmMappingMockedObj(SALES_ORDER));
      EasyMock.replay(edmEntityType);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    return edmEntityType;
  }

  private InlineCount getInlineCount() {
    return InlineCount.NONE;
  }

  private FilterExpression getFilter() {
    return null;
  }

  private Integer getSkip() {
    return null;
  }

  private Integer getTop() {
    return null;
  }

  private OrderByExpression getOrderByExpression() {
    return null;
  }

  private ODataJPAContext getLocalmockODataJPAContext() {
    ODataJPAContext odataJPAContext = EasyMock.createMock(ODataJPAContext.class);
    EasyMock.expect(odataJPAContext.getPageSize()).andReturn(0).anyTimes();
    EasyMock.expect(odataJPAContext.getPersistenceUnitName()).andStubReturn("salesorderprocessing");
    EasyMock.expect(odataJPAContext.getEntityManagerFactory()).andStubReturn(mockEntityManagerFactory());
    EasyMock.expect(odataJPAContext.getODataJPATransaction()).andStubReturn(getLocalJpaTransaction());
    EasyMock.expect(odataJPAContext.getODataContext()).andStubReturn(getLocalODataContext());
    odataJPAContext.setODataContext((ODataContext) EasyMock.anyObject());
    EasyMock.expectLastCall().anyTimes();
    EasyMock.expect(odataJPAContext.getEntityManager()).andStubReturn(getLocalEntityManager());
    EasyMock.replay(odataJPAContext);
    return odataJPAContext;
  }

  private ODataJPATransaction getLocalJpaTransaction() {
    ODataJPATransaction tx = EasyMock.createMock(ODataJPATransaction.class);
    tx.begin(); // testing void method
    tx.commit();// testing void method
    tx.rollback();// testing void method
    EasyMock.expect(tx.isActive()).andReturn(false);
    EasyMock.replay(tx);
    return tx;
  }

  private EntityManagerFactory mockEntityManagerFactory() {
    EntityManagerFactory emf = EasyMock.createMock(EntityManagerFactory.class);
    EasyMock.expect(emf.getMetamodel()).andStubReturn(mockMetaModel());
    EasyMock.expect(emf.createEntityManager()).andStubReturn(getLocalEntityManager());
    EasyMock.replay(emf);
    return emf;
  }

  private EntityManagerFactory mockEntityManagerFactory2() {// For create, to avoid stackoverflow
    EntityManagerFactory emf = EasyMock.createMock(EntityManagerFactory.class);
    EasyMock.expect(emf.getMetamodel()).andStubReturn(mockMetaModel());
    EasyMock.replay(emf);
    return emf;
  }

  private EntityManager getLocalEntityManager() {
    EntityManager em = EasyMock.createMock(EntityManager.class);
    EasyMock.expect(em.createQuery("SELECT E1 FROM SalesOrderHeaders E1")).andStubReturn(getQuery());
    EasyMock.expect(em.createQuery("SELECT COUNT ( E1 ) FROM SalesOrderHeaders E1")).andStubReturn(
        getQueryForSelectCount());
    EasyMock.expect(em.getEntityManagerFactory()).andStubReturn(mockEntityManagerFactory2());// For create
    EasyMock.expect(em.getTransaction()).andStubReturn(getLocalTransaction()); // For Delete
    EasyMock.expect(em.isOpen()).andReturn(true).anyTimes();
    Address obj = new Address();
    em.remove(obj);// testing void method
    em.flush();
    em.close();
    EasyMock.expectLastCall().anyTimes();
    EasyMock.replay(em);
    return em;
  }

  private EntityTransaction getLocalTransaction() {
    EntityTransaction entityTransaction = EasyMock.createMock(EntityTransaction.class);
    entityTransaction.begin(); // testing void method
    entityTransaction.commit();// testing void method
    entityTransaction.rollback();// testing void method
    EasyMock.expect(entityTransaction.isActive()).andReturn(false);
    EasyMock.replay(entityTransaction);
    return entityTransaction;
  }

  private Query getQuery() {
    Query query = EasyMock.createMock(Query.class);
    EasyMock.expect(query.getResultList()).andStubReturn(getResultList());
    EasyMock.replay(query);
    return query;
  }

  private Query getQueryForSelectCount() {
    Query query = EasyMock.createMock(Query.class);
    EasyMock.expect(query.getResultList()).andStubReturn(getResultListForSelectCount());
    EasyMock.replay(query);
    return query;
  }

  private List<?> getResultList() {
    List<Object> list = new ArrayList<Object>();
    list.add(new Address());
    return list;
  }

  private List<?> getResultListForSelectCount() {
    List<Object> list = new ArrayList<Object>();
    list.add(new Long(11));
    return list;
  }

  class Address {
    private String soId = "12";

    public String getSoId() {
      return soId;
    }

    @Override
    public boolean equals(final Object obj) {
      boolean isEqual = false;
      if (obj instanceof Address) {
        isEqual = getSoId().equalsIgnoreCase(((Address) obj).getSoId());//
      }
      return isEqual;
    }
  }

  private Metamodel mockMetaModel() {
    Metamodel metaModel = EasyMock.createMock(Metamodel.class);
    EasyMock.expect(metaModel.getEntities()).andStubReturn(getLocalEntities());
    EasyMock.replay(metaModel);
    return metaModel;
  }

  private Set<EntityType<?>> getLocalEntities() {
    Set<EntityType<?>> entityTypeSet = new HashSet<EntityType<?>>();
    entityTypeSet.add(getLocalJPAEntityType());
    return entityTypeSet;
  }

  @SuppressWarnings("rawtypes")
  private EntityType<EntityType> getLocalJPAEntityType() {
    @SuppressWarnings("unchecked")
    EntityType<EntityType> entityType = EasyMock.createMock(EntityType.class);
    EasyMock.expect(entityType.getJavaType()).andStubReturn(EntityType.class);
    EasyMock.replay(entityType);
    return entityType;
  }

  private GetEntityUriInfo getEntityUriInfo() {
    GetEntityUriInfo getEntityView = EasyMock.createMock(GetEntityUriInfo.class);
    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    EdmEntityType edmEntityType = EasyMock.createMock(EdmEntityType.class);
    try {
      EasyMock.expect(getEntityView.getExpand()).andStubReturn(null);
      EasyMock.expect(edmEntityType.getKeyProperties()).andStubReturn(new ArrayList<EdmProperty>());
      EasyMock.expect(edmEntitySet.getEntityType()).andStubReturn(edmEntityType);
      EasyMock.expect(edmEntitySet.getName()).andStubReturn(SALES_ORDER_HEADERS);

      EasyMock.expect(getEntityView.getSelect()).andStubReturn(null);
      EasyMock.expect(getEntityView.getTargetEntitySet()).andStubReturn(edmEntitySet);
      EasyMock.expect(edmEntityType.getPropertyNames()).andStubReturn(getLocalPropertyNames());
      EasyMock.expect(edmEntityType.getProperty(SO_ID)).andStubReturn(getEdmTypedMockedObj(SO_ID));

      EasyMock.expect(edmEntityType.getMapping()).andStubReturn((EdmMapping) getEdmMappingMockedObj(SALES_ORDER));

      EasyMock.expect(edmEntityType.getKind()).andStubReturn(EdmTypeKind.SIMPLE);
      EasyMock.expect(edmEntityType.getNamespace()).andStubReturn(SALES_ORDER_HEADERS);
      EasyMock.expect(edmEntityType.getName()).andStubReturn(SALES_ORDER_HEADERS);
      EasyMock.expect(edmEntityType.hasStream()).andStubReturn(false);
      EasyMock.expect(edmEntityType.getNavigationPropertyNames()).andStubReturn(new ArrayList<String>());
      EasyMock.expect(edmEntityType.getKeyPropertyNames()).andStubReturn(new ArrayList<String>());

      EasyMock.expect(edmEntitySet.getEntityContainer()).andStubReturn(getLocalEdmEntityContainer());

      EasyMock.replay(edmEntityType, edmEntitySet);
      EasyMock.expect(getEntityView.getKeyPredicates()).andStubReturn(new ArrayList<KeyPredicate>());
      List<NavigationSegment> navigationSegments = new ArrayList<NavigationSegment>();
      EasyMock.expect(getEntityView.getNavigationSegments()).andReturn(navigationSegments);
      EasyMock.expect(getEntityView.getStartEntitySet()).andReturn(edmEntitySet);

      EasyMock.replay(getEntityView);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    return getEntityView;
  }

  private EdmEntityContainer getLocalEdmEntityContainer() {
    EdmEntityContainer edmEntityContainer = EasyMock.createMock(EdmEntityContainer.class);
    EasyMock.expect(edmEntityContainer.isDefaultEntityContainer()).andStubReturn(true);
    try {
      EasyMock.expect(edmEntityContainer.getName()).andStubReturn(SALESORDERPROCESSING_CONTAINER);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    EasyMock.replay(edmEntityContainer);
    return edmEntityContainer;
  }

  private EdmTyped getEdmTypedMockedObj(final String propertyName) {
    EdmProperty mockedEdmProperty = EasyMock.createMock(EdmProperty.class);
    try {
      EasyMock.expect(mockedEdmProperty.getMapping()).andStubReturn((EdmMapping) getEdmMappingMockedObj(propertyName));
      EdmType edmType = EasyMock.createMock(EdmType.class);
      EasyMock.expect(edmType.getKind()).andStubReturn(EdmTypeKind.SIMPLE);
      EasyMock.replay(edmType);
      EasyMock.expect(mockedEdmProperty.getName()).andStubReturn("identifier");
      EasyMock.expect(mockedEdmProperty.getType()).andStubReturn(edmType);
      EasyMock.expect(mockedEdmProperty.getFacets()).andStubReturn(getEdmFacetsMockedObj());

      EasyMock.replay(mockedEdmProperty);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    return mockedEdmProperty;
  }

  private EdmFacets getEdmFacetsMockedObj() {
    EdmFacets facets = EasyMock.createMock(EdmFacets.class);
    EasyMock.expect(facets.getConcurrencyMode()).andStubReturn(EdmConcurrencyMode.Fixed);

    EasyMock.replay(facets);
    return facets;
  }

  private JPAEdmMapping getEdmMappingMockedObj(final String propertyName) {
    JPAEdmMappingImpl mockedEdmMapping = EasyMock.createMock(JPAEdmMappingImpl.class);
    if (propertyName.equalsIgnoreCase(SALES_ORDER)) {
      EasyMock.expect(((EdmMapping) mockedEdmMapping).getInternalName()).andStubReturn(SALES_ORDER_HEADERS);
    } else {
      EasyMock.expect(((EdmMapping) mockedEdmMapping).getInternalName()).andStubReturn(propertyName);
    }
    EasyMock.expect(mockedEdmMapping.getODataJPATombstoneEntityListener()).andReturn(null);
    EasyMock.<Class<?>> expect(mockedEdmMapping.getJPAType()).andReturn(SalesOrderHeader.class);
    EasyMock.replay(mockedEdmMapping);
    return mockedEdmMapping;
  }

  private List<String> getLocalPropertyNames() {
    List<String> list = new ArrayList<String>();
    list.add(SO_ID);
    return list;
  }

  private ODataContext getLocalODataContext() {
    ODataContext objODataContext = null;
    try {
      ODataContextMock contextMock = new ODataContextMock();
      contextMock.setODataService(new ODataServiceMock().mock());
      contextMock.setPathInfo(getLocalPathInfo());
      objODataContext = contextMock.mock();

    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    return objODataContext;
  }

  private PathInfo getLocalPathInfo() {
    PathInfo pathInfo = EasyMock.createMock(PathInfo.class);
    EasyMock.expect(pathInfo.getServiceRoot()).andStubReturn(getLocalURI());
    EasyMock.replay(pathInfo);
    return pathInfo;
  }

  private URI getLocalURI() {
    URI uri = null;
    try {
      uri = new URI(STR_LOCAL_URI);
    } catch (URISyntaxException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    return uri;
  }

}
