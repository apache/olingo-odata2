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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.api.uri.expression.ExpressionParserException;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ODataSelectExpressionParserTest {
  private static final String TABLE_ALIAS = "E1";
  private static final short INPUT = 0;
  private static final short OUTPUT = 1;
  private static final String NAMESPACE = "SalesOrderProcessing";
  private static final String ENTITY_NOTE = "Note";
  private static Edm edm = null;
  private static final String[] EXPRESSION_ORDERBY = { "id,soId", "E1.id , E1.soId" };
  private static final String[] EXPRESSION_ORDERBY_ASC = { "id asc,soId", "E1.id , E1.soId" };
  private static final String[] EXPRESSION_ORDERBY_DESC = { "id desc,soId", "E1.id DESC , E1.soId" };
  private static final String[] EXPRESSION_ORDERBY_ASC_DESC = { "id desc,soId asc", "E1.id DESC , E1.soId" };
  private static final String[] EXPRESSION_ORDERBY_SINGLE = { "id desc", "E1.id DESC" };

  @BeforeClass
  public static void setup() {
    InputStream metadataStream =
        ODataFilterExpressionParserTest.class.getClassLoader().getResourceAsStream("metadata.xml");
    try {
      edm = EntityProvider.readMetadata(metadataStream, true);
    } catch (EntityProviderException e) {
      fail("Not expected");
    }
  }

  @Test
  public void testSelectExpression() {
    ArrayList<String> selectedFields = new ArrayList<String>();
    selectedFields.add("id");
    selectedFields.add("oValue/Currency");
    assertEquals("E1.id, E1.oValue/Currency",
        ODataExpressionParser.parseToJPASelectExpression(TABLE_ALIAS, selectedFields));
  }

  @Test
  public void testOrderByExpressionSingle() {
    assertEquals(EXPRESSION_ORDERBY_SINGLE[OUTPUT], parseOrderByExpression(EXPRESSION_ORDERBY_SINGLE[INPUT], false));
  }

  @Test
  public void testOrderByExpression() {
    assertEquals(EXPRESSION_ORDERBY[OUTPUT], parseOrderByExpression(EXPRESSION_ORDERBY[INPUT], false));
  }

  @Test
  public void testOrderByExpressionAsc() {
    assertEquals(EXPRESSION_ORDERBY_ASC[OUTPUT], parseOrderByExpression(EXPRESSION_ORDERBY_ASC[INPUT], false));
  }

  @Test
  public void testOrderByExpressionDesc() {
    assertEquals(EXPRESSION_ORDERBY_DESC[OUTPUT], parseOrderByExpression(EXPRESSION_ORDERBY_DESC[INPUT], false));
  }

  @Test
  public void testOrderByExpressionAscDesc() {
    assertEquals(EXPRESSION_ORDERBY_ASC_DESC[OUTPUT], parseOrderByExpression(EXPRESSION_ORDERBY_ASC_DESC[INPUT], 
        false));
  }

  @Test
  public void testOrderByKeyPredicates() {
    try {
      assertEquals("E1.id", ODataExpressionParser.parseKeyPropertiesToJPAOrderByExpression(edm.getEntityType(NAMESPACE,
          ENTITY_NOTE)
          .getKeyProperties(), TABLE_ALIAS));
    } catch (ODataJPARuntimeException e) {
      fail("Not expected");
    } catch (EdmException e) {
      fail("Not expected");
    }
  }

  @Test
  public void testOrderByKeyPredicatesNull() {
    try {
      assertEquals("", ODataExpressionParser.parseKeyPropertiesToJPAOrderByExpression(null, TABLE_ALIAS));
    } catch (ODataJPARuntimeException e) {
      fail("Not expected");
    }
  }

  private String parseOrderByExpression(final String input, final boolean isExceptionExpected) {
    OrderByExpression expression;
    try {
      expression = UriParser.parseOrderBy(edm, edm.getEntityType(NAMESPACE, ENTITY_NOTE), input);
      String expressionString = ODataExpressionParser.parseToJPAOrderByExpression(expression, TABLE_ALIAS);
      return expressionString;
    } catch (ExpressionParserException e) {
      fail("Not expected");
    } catch (EdmException e) {
      fail("Not expected");
    } catch (ODataMessageException e) {
      fail("Not expected");
    } catch (ODataException e) {
      if (isExceptionExpected) {
        assertTrue(true);
      } else {
        fail("Not expected");
      }
    }
    return "";
  }
}
