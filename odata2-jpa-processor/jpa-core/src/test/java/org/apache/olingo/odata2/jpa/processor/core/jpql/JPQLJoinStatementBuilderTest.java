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
package org.apache.olingo.odata2.jpa.processor.core.jpql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.jpa.processor.api.access.JPAJoinClause;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLJoinContextView;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class JPQLJoinStatementBuilderTest {
  JPQLJoinContextView context = null;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  public void setUp(final List<JPAJoinClause> joinClauseList) throws Exception {
    context = EasyMock.createMock(JPQLJoinContextView.class);
    EasyMock.expect(context.getJPAEntityAlias()).andStubReturn("mat");
    EasyMock.expect(context.getJPAEntityName()).andStubReturn("SOHeader");
    EasyMock.expect(context.getType()).andStubReturn(JPQLContextType.SELECT);
    EasyMock.expect(context.getSelectExpression()).andStubReturn("mat");
    EasyMock.expect(context.getWhereExpression()).andStubReturn("soh.buyerId = 2");
    String orderByMap = new String("mat.buyerId asc , mat.city desc");
    EasyMock.expect(context.getOrderByCollection()).andStubReturn(orderByMap);
    EasyMock.expect(context.getJPAJoinClauses()).andStubReturn(joinClauseList);
    EasyMock.replay(context);
  }

  private List<JPAJoinClause> getJoinClauseList() {
    List<JPAJoinClause> joinClauseList = new ArrayList<JPAJoinClause>();
    JPAJoinClause jpaOuterJoinClause =
        new JPAJoinClause("SOHeader", "soh", null, null, "soh.createdBy = 'Peter'", JPAJoinClause.JOIN.LEFT);
    joinClauseList.add(jpaOuterJoinClause);
    jpaOuterJoinClause =
        new JPAJoinClause("SOHeader", "soh", "soItem", "soi", "soi.shId = soh.soId", JPAJoinClause.JOIN.LEFT);
    joinClauseList.add(jpaOuterJoinClause);
    jpaOuterJoinClause =
        new JPAJoinClause("SOItem", "si", "material", "mat", "mat.id = 'abc'", JPAJoinClause.JOIN.LEFT);
    joinClauseList.add(jpaOuterJoinClause);
    return joinClauseList;
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testBuild() throws Exception {
    setUp(getJoinClauseList());
    JPQLJoinStatementBuilder jpqlJoinStatementBuilder = new JPQLJoinStatementBuilder(context);
    try {
      JPQLStatement jpqlStatement = jpqlJoinStatementBuilder.build();
      assertEquals(
          "SELECT mat FROM SOHeader soh JOIN soh.soItem soi JOIN soi.material mat WHERE soh.buyerId = 2 AND "
              +
              "soh.createdBy = 'Peter' AND soi.shId = soh.soId AND mat.id = 'abc' "
              +
              "ORDER BY mat.buyerId asc , mat.city desc",
              jpqlStatement.toString());
    } catch (ODataJPARuntimeException e) {
      fail("Should not have come here");
    }

  }

  @Test
  public void testJoinClauseAsNull() throws Exception {
    setUp(null);
    JPQLJoinStatementBuilder jpqlJoinStatementBuilder = new JPQLJoinStatementBuilder(context);
    try {
      jpqlJoinStatementBuilder.build();
      fail("Should not have come here");
    } catch (ODataJPARuntimeException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testJoinClauseListAsEmpty() throws Exception {
    setUp(new ArrayList<JPAJoinClause>());
    JPQLJoinStatementBuilder jpqlJoinStatementBuilder = new JPQLJoinStatementBuilder(context);
    try {
      jpqlJoinStatementBuilder.build();
      fail("Should not have come here");
    } catch (ODataJPARuntimeException e) {
      assertTrue(true);
    }
  }

}
