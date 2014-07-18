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

import junit.framework.Assert;
import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAPaging;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import javax.persistence.metamodel.Metamodel;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.fail;

public class JPAProcessorImplTest {

  // -------------------------------- Common Start ------------------------------------common in
  // ODataJPAProcessorDefaultTest as well
  private static final String STR_LOCAL_URI = "http://localhost:8080/org.apache.olingo.odata2.processor.ref.web/";
  private static final String SALESORDERPROCESSING_CONTAINER = "salesorderprocessingContainer";
  private static final String SO_ID = "SoId";
  private static final String SALES_ORDER = "SalesOrder";
  private static final String SALES_ORDER_HEADERS = "SalesOrderHeaders";
  // -------------------------------- Common End ------------------------------------

  JPAProcessorImpl objJPAProcessorImpl;

  @Before
  public void setUp() throws Exception {
    objJPAProcessorImpl = new JPAProcessorImpl(getLocalmockODataJPAContext());
  }

  @Test
  public void testProcessGetEntitySetCountUriInfo() {
    try {
      Assert.assertEquals(11, objJPAProcessorImpl.process(getEntitySetCountUriInfo()));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testProcessGetEntityCountUriInfo() {
    try {
      Assert.assertEquals(11, objJPAProcessorImpl.process(getEntityCountUriInfo()));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testProcessGetEntitySetUriInfo() {
    try {
      Assert.assertNotNull(objJPAProcessorImpl.process(getEntitySetUriInfo()));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testProcessDeleteUriInfo() {
    try {
      Assert.assertNotNull(objJPAProcessorImpl.process(getDeletetUriInfo(), "application/xml"));
      Assert.assertEquals(new Address(), objJPAProcessorImpl.process(getDeletetUriInfo(), "application/xml"));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testProcessDeleteUriInfoNegative() {
    try {
      Assert.assertNotNull(objJPAProcessorImpl.process(getDeletetUriInfo(), "application/xml"));
      Assert.assertNotSame(new Object(), objJPAProcessorImpl.process(getDeletetUriInfo(), "application/xml"));
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  // ---------------------------- Common Code Start ---------------- TODO - common in ODataJPAProcessorDefaultTest as
  // well

  private DeleteUriInfo getDeletetUriInfo() {
    UriInfo objUriInfo = EasyMock.createMock(UriInfo.class);
    EasyMock.expect(objUriInfo.getStartEntitySet()).andStubReturn(getLocalEdmEntitySet());
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

  private GetEntitySetCountUriInfo getEntitySetCountUriInfo() {
    return getLocalUriInfo();
  }

  private GetEntityCountUriInfo getEntityCountUriInfo() {
    return getLocalUriInfo();
  }

  private GetEntitySetUriInfo getEntitySetUriInfo() {

    UriInfo objUriInfo = EasyMock.createMock(UriInfo.class);
    EasyMock.expect(objUriInfo.getStartEntitySet()).andStubReturn(getLocalEdmEntitySet());
    EasyMock.expect(objUriInfo.getTargetEntitySet()).andStubReturn(getLocalEdmEntitySet());
    EasyMock.expect(objUriInfo.getSelect()).andStubReturn(null);
    EasyMock.expect(objUriInfo.getOrderBy()).andStubReturn(getOrderByExpression());
    EasyMock.expect(objUriInfo.getTop()).andStubReturn(getTop());
    EasyMock.expect(objUriInfo.getSkip()).andStubReturn(getSkip());
    EasyMock.expect(objUriInfo.getSkipToken()).andReturn("5");
    EasyMock.expect(objUriInfo.getInlineCount()).andStubReturn(getInlineCount());
    EasyMock.expect(objUriInfo.getFilter()).andStubReturn(getFilter());
    EasyMock.expect(objUriInfo.getFunctionImport()).andStubReturn(null);
    EasyMock.expect(objUriInfo.getCustomQueryOptions()).andStubReturn(null);
    EasyMock.replay(objUriInfo);
    return objUriInfo;
  }

  /**
   * @return
   */
  private UriInfo getLocalUriInfo() {
    UriInfo objUriInfo = EasyMock.createMock(UriInfo.class);
    EasyMock.expect(objUriInfo.getStartEntitySet()).andStubReturn(getLocalEdmEntitySet());
    EasyMock.expect(objUriInfo.getTargetEntitySet()).andStubReturn(getLocalEdmEntitySet());
    EasyMock.expect(objUriInfo.getSelect()).andStubReturn(null);
    EasyMock.expect(objUriInfo.getOrderBy()).andStubReturn(getOrderByExpression());
    EasyMock.expect(objUriInfo.getTop()).andStubReturn(getTop());
    EasyMock.expect(objUriInfo.getSkip()).andStubReturn(getSkip());
    EasyMock.expect(objUriInfo.getInlineCount()).andStubReturn(getInlineCount());
    EasyMock.expect(objUriInfo.getFilter()).andStubReturn(getFilter());
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
      EasyMock.expect(edmEntityType.getMapping()).andStubReturn(getEdmMappingMockedObj(SALES_ORDER));// ID vs Salesorder
                                                                                                     // ID
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
    EasyMock.expect(odataJPAContext.getPersistenceUnitName()).andStubReturn("salesorderprocessing");
    EasyMock.expect(odataJPAContext.getEntityManagerFactory()).andStubReturn(mockEntityManagerFactory());
    EasyMock.expect(odataJPAContext.getODataContext()).andStubReturn(getLocalODataContext());
    EasyMock.expect(odataJPAContext.getEntityManager()).andStubReturn(getLocalEntityManager());
    EasyMock.expect(odataJPAContext.getPageSize()).andReturn(10).anyTimes();
    odataJPAContext.setPaging(EasyMock.isA(JPAPaging.class));
    EasyMock.expectLastCall();
    EasyMock.replay(odataJPAContext);
    return odataJPAContext;
  }

  private EntityManagerFactory mockEntityManagerFactory() {
    EntityManagerFactory emf = EasyMock.createMock(EntityManagerFactory.class);
    EasyMock.expect(emf.getMetamodel()).andStubReturn(mockMetaModel());
    EasyMock.expect(emf.createEntityManager()).andStubReturn(getLocalEntityManager());
    EasyMock.replay(emf);
    return emf;
  }

  public EntityManager getLocalEntityManager() {
    EntityManager em = EasyMock.createMock(EntityManager.class);
    EasyMock.expect(em.createQuery("SELECT E1 FROM SalesOrderHeaders E1")).andStubReturn(getQuery());
    EasyMock.expect(em.createQuery("SELECT COUNT ( E1 ) FROM SalesOrderHeaders E1")).andStubReturn(
        getQueryForSelectCount());
    EasyMock.expect(em.getTransaction()).andStubReturn(getLocalTransaction()); // For Delete
    EasyMock.expect(em.getTransaction()).andStubReturn(getLocalTransaction()); // For Delete
    em.flush();
    em.flush();
    Address obj = new Address();
    em.remove(obj);// testing void method
    em.remove(obj);// testing void method
    EasyMock.replay(em);
    return em;
  }

  private EntityTransaction getLocalTransaction() {
    EntityTransaction entityTransaction = EasyMock.createMock(EntityTransaction.class);
    entityTransaction.begin(); // testing void method
    entityTransaction.begin(); // testing void method
    entityTransaction.commit();// testing void method
    entityTransaction.commit();// testing void method
    EasyMock.expect(entityTransaction.isActive()).andStubReturn(false);// testing finally clause
    EasyMock.replay(entityTransaction);
    return entityTransaction;
  }

  private Query getQuery() {
    return new Query() {

      private int maxResults;
      private int firstResult;

      @Override
      public Query setFirstResult(final int arg0) {
        firstResult = arg0;
        return this;
      }

      @Override
      public Query setMaxResults(final int arg0) {
        maxResults = arg0;
        return this;
      }

      @Override
      public int getMaxResults() {
        return maxResults;
      }

      @Override
      public int getFirstResult() {
        return firstResult;
      }

      @SuppressWarnings("unchecked")
      @Override
      public List<Object> getResultList() {
        return (List<Object>) getResultListL();
      }

      @Override
      public <T> T unwrap(final Class<T> arg0) {
        return null;
      }

      @Override
      public Query setParameter(final int arg0, final Date arg1, final TemporalType arg2) {
        return null;
      }

      @Override
      public Query setParameter(final int arg0, final Calendar arg1, final TemporalType arg2) {
        return null;
      }

      @Override
      public Query setParameter(final String arg0, final Date arg1, final TemporalType arg2) {
        return null;
      }

      @Override
      public Query setParameter(final String arg0, final Calendar arg1, final TemporalType arg2) {
        return null;
      }

      @Override
      public Query setParameter(final Parameter<Date> arg0, final Date arg1, final TemporalType arg2) {
        return null;
      }

      @Override
      public Query setParameter(final Parameter<Calendar> arg0, final Calendar arg1, final TemporalType arg2) {
        return null;
      }

      @Override
      public Query setParameter(final int arg0, final Object arg1) {
        return null;
      }

      @Override
      public Query setParameter(final String arg0, final Object arg1) {
        return null;
      }

      @Override
      public <T> Query setParameter(final Parameter<T> arg0, final T arg1) {
        return null;
      }

      @Override
      public Query setLockMode(final LockModeType arg0) {
        return null;
      }

      @Override
      public Query setHint(final String arg0, final Object arg1) {
        return null;
      }

      @Override
      public Query setFlushMode(final FlushModeType arg0) {
        return null;
      }

      @Override
      public boolean isBound(final Parameter<?> arg0) {
        return false;
      }

      @Override
      public Object getSingleResult() {
        return null;
      }

      @Override
      public Set<Parameter<?>> getParameters() {
        return null;
      }

      @Override
      public Object getParameterValue(final int arg0) {
        return null;
      }

      @Override
      public Object getParameterValue(final String arg0) {
        return null;
      }

      @Override
      public <T> T getParameterValue(final Parameter<T> arg0) {
        return null;
      }

      @Override
      public <T> Parameter<T> getParameter(final int arg0, final Class<T> arg1) {
        return null;
      }

      @Override
      public <T> Parameter<T> getParameter(final String arg0, final Class<T> arg1) {
        return null;
      }

      @Override
      public Parameter<?> getParameter(final int arg0) {
        return null;
      }

      @Override
      public Parameter<?> getParameter(final String arg0) {
        return null;
      }

      @Override
      public LockModeType getLockMode() {
        return null;
      }

      @Override
      public Map<String, Object> getHints() {
        return null;
      }

      @Override
      public FlushModeType getFlushMode() {
        return null;
      }

      @Override
      public int executeUpdate() {
        return 0;
      }
    };
  }

  private Query getQueryForSelectCount() {
    Query query = EasyMock.createMock(Query.class);
    EasyMock.expect(query.getResultList()).andStubReturn(getResultListForSelectCount());
    EasyMock.replay(query);
    return query;
  }

  private List<?> getResultListL() {
    List<Object> list = new ArrayList<Object>();
    list.add(new Address());
    return list;
  }

  private List<?> getResultListForSelectCount() {
    List<Object> list = new ArrayList<Object>();
    list.add(new Long(11));
    return list;
  }

  private class Address {
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
    EasyMock.replay(metaModel);
    return metaModel;
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
      EasyMock.expect(mockedEdmProperty.getMapping()).andStubReturn(getEdmMappingMockedObj(propertyName));
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

  private EdmMapping getEdmMappingMockedObj(final String propertyName) {
    EdmMapping mockedEdmMapping = new JPAEdmMappingImpl();
    if (propertyName.equalsIgnoreCase(SALES_ORDER)) {
      ((Mapping) mockedEdmMapping).setInternalName(SALES_ORDER_HEADERS);
    } else {
      ((Mapping) mockedEdmMapping).setInternalName(propertyName);
    }

    return mockedEdmMapping;
  }

  private List<String> getLocalPropertyNames() {
    List<String> list = new ArrayList<String>();
    list.add(SO_ID);
    return list;
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

  // -------------------------------- Common End ------------------------------------

}
