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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAPage.JPAPageBuilder;
import org.junit.Test;

public class JPAPageBuilderTest {

  private static final int PAGE_SIZE = 10;

  @Test
  public void testBuildDefault() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());
    assertEquals(1, page.getPagedEntities().size());

    assertEquals(10, query.getFirstResult());
    assertEquals(10, query.getMaxResults());
  }



  @Test
  public void testBuildDefaultZeroPage() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(0)
        .skipToken("10")
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(0, page.getPageSize());
    assertNotNull(page.getPagedEntities());
    assertEquals(0, page.getPagedEntities().size());

    assertEquals(0, query.getFirstResult());
    assertEquals(0, query.getMaxResults());
  }

   @Test
  public void testBuildWithNoSkipToken() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken("0")
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());
    assertEquals(1, page.getPagedEntities().size());

    assertEquals(0, query.getFirstResult());
    assertEquals(10, query.getMaxResults());
  }

  @Test
  public void testBuildWithNullSkipToken() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken(null)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(0, query.getFirstResult());
    assertEquals(10, query.getMaxResults());
  }

  @Test
  public void testBuildWithInvalidSkipToken() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    try {
      pageBuilder.query(query)
          .skipToken("AB");
    } catch (NumberFormatException e) {
      return;
    }
    fail("Exception Expected");
  }

  @Test
  public void testBuildWithTop() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(5)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(10, query.getFirstResult());
    assertEquals(5, query.getMaxResults());
  }

  @Test
  public void testBuildWithTopZeroPage() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(0)
        .skipToken("10")
        .top(5)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(0, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(0, query.getFirstResult());
    assertEquals(5, query.getMaxResults());
  }

  @Test
  public void testBuildWithSkipZeroPage() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(0)
        .skipToken("10")
        .skip(5)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(0, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(5, query.getFirstResult());
    assertEquals(0, query.getMaxResults());
  }

  @Test
  public void testBuildWithTopSkipZeroPage() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(0)
        .skipToken("10")
        .skip(5)
        .top(5)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(0, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(5, query.getFirstResult());
    assertEquals(5, query.getMaxResults());
  }

  @Test
  public void testBuildWithTopExceeds() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(15)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(10, query.getFirstResult());
    assertEquals(10, query.getMaxResults());
  }

  @Test
  public void testBuildWithTopSkipExceeds() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(5)
        .skip(10)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertEquals(0, page.getPagedEntities().size());

    assertEquals(0, query.getFirstResult());
    assertEquals(0, query.getMaxResults());
  }

  @Test
  public void testBuildWithTopSkipMore() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(5)
        .skip(9)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(19, query.getFirstResult());
    assertEquals(1, query.getMaxResults());
  }

  @Test
  public void testBuildWithTopMoreSkip() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(15)
        .skip(9)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(19, query.getFirstResult());
    assertEquals(1, query.getMaxResults());
  }

  @Test
  public void testBuildWithTopXSkipX() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(15)
        .skip(15)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(0, query.getFirstResult());
    assertEquals(0, query.getMaxResults());
  }

  @Test
  public void testBuildWithNegativeTop() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(-5)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(10, query.getFirstResult());
    assertEquals(10, query.getMaxResults());
  }

  @Test
  public void testBuildWithNegativeTopSkipToken() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(false);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken("-10")
        .top(-5)
        .skip(-1)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(0, query.getFirstResult());
    assertEquals(10, query.getMaxResults());
  }

  @Test
  public void testBuildWithNoRecords() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    Query query = mockQuery(true);

    JPAPage page = pageBuilder.query(query)
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(1)
        .skip(1)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(11, query.getFirstResult());
    assertEquals(1, query.getMaxResults());
  }

  private Query mockQuery(final boolean setNoRecords) {

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

      @Override
      public List<Object> getResultList() {
        List<Object> list = new ArrayList<Object>();
        if (maxResults > 0 && setNoRecords == false) {
          list.add(new Integer(1));
        }
        return list;
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


}
