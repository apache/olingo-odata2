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

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.api.uri.expression.ExpressionParserException;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.junit.BeforeClass;
import org.junit.Test;

public class ODataFilterExpressionParserTest {
  private static final short INPUT = 0;
  private static final short OUTPUT = 1;
  private static final String TABLE_ALIAS = "E1";
  private static final String NAMESPACE = "SalesOrderProcessing";
  private static final String ENTITY_NOTE = "Note";
  // Index 0 - Is test input and Index 1 - Is expected output
  private static final String[] EXPRESSION_EQ = { "id eq '123'", "(E1.id = '123')" };
  private static final String[] EXPRESSION_NE = { "id ne '123'", "(E1.id <> '123')" };
  private static final String[] EXPRESSION_ESCAPE = { "id ne '123''22'", "(E1.id <> '123''22')" };
  private static final String[] EXPRESSION_BINARY_AND =
  {
      "id le '123' and soId eq 123L and not (substringof(id,'123') eq false) eq true",
      "(((E1.id <= '123') AND (E1.soId = 123L)) AND (NOT(((CASE WHEN ('123' LIKE CONCAT('%',E1.id,'%')) "
          + "THEN TRUE ELSE FALSE END) = false)) = true))" };
  private static final String[] EXPRESSION_BINARY_OR = { "id ge '123' or soId gt 123L",
      "((E1.id >= '123') OR (E1.soId > 123L))" };
  private static final String[] EXPRESSION_MEMBER_OR = { "id lt '123' or oValue/Currency eq 'INR'",
      "((E1.id < '123') OR (E1.oValue.Currency = 'INR'))" };
  private static final String[] EXPRESSION_STARTS_WITH = { "startswith(oValue/Currency,'INR')",
      "E1.oValue.Currency LIKE CONCAT('INR','%')" };
  private static final String[] EXPRESSION_STARTS_WITH_EQUAL = { "startswith(oValue/Currency,'INR') eq true",
      "(E1.oValue.Currency LIKE CONCAT('INR','%') )" };
  private static final String[] EXPRESSION_NOT_STARTS_WITH = { "startswith(oValue/Currency,'INR') eq false",
      "(E1.oValue.Currency NOT LIKE CONCAT('INR','%') )" };
  private static final String[] EXPRESSION_NOT_ENDS_WITH = { "endswith(oValue/Currency,tolower('INR')) eq false",
      "(E1.oValue.Currency NOT LIKE CONCAT('%',LOWER('INR')) )" };
  private static final String[] EXPRESSION_NESTED_METHOD = {
      "endswith(substring(oValue/Currency,2),'INR') eq false",
      "(SUBSTRING(E1.oValue.Currency, 2 + 1 ) NOT LIKE CONCAT('%','INR') )" };
  private static final String[] EXPRESSION_SUBSTRING_OF = {
      "substringof(id,'123') ne true",
      "((CASE WHEN ('123' LIKE CONCAT('%',E1.id,'%')) THEN TRUE ELSE FALSE END) <> true)" };
  private static final String[] EXPRESSION_STARTS_WITH_WRONG_OP = { "startswith(oValue/Currency,'INR') lt true", "" };
  private static final String[] EXPRESSION_SUBSTRING_ALL_OP = { "substring(oValue/Currency,1,3) eq 'INR'",
      "(SUBSTRING(E1.oValue.Currency, 1 + 1 , 3) = 'INR')" };
  private static final String[] EXPRESSION_SUBSTRINGOF_INJECTION1 = {
      "substringof('a'' OR 1=1 OR E1.id LIKE ''b',id) eq true",
      "((CASE WHEN (E1.id LIKE CONCAT('%','a'' OR 1=1 OR E1.id LIKE ''b','%')) THEN TRUE ELSE FALSE END) = true)" };
  private static final String[] EXPRESSION_SUBSTRINGOF_INJECTION2 =
  {
      "substringof('substringof(''a'' OR 1=1 OR E1.id LIKE ''b'',id)',id) eq true",
      "((CASE WHEN (E1.id LIKE CONCAT('%','substringof(''a'' OR 1=1 OR E1.id LIKE ''b'',id)','%')) "
          + "THEN TRUE ELSE FALSE END) = true)" };
  private static final String[] EXPRESSION_SUBSTRINGOF_INJECTION3 =
  {
      "substringof( substring(' ) OR execute_my_sql OR '' LIKE ',3),'de''') eq true",
      "((CASE WHEN ('de''' LIKE CONCAT('%',SUBSTRING(' ) OR execute_my_sql OR '' LIKE ', 3 + 1 ),'%')) "
          + "THEN TRUE ELSE FALSE END) = true)" };
  private static final String[] EXPRESSION_ENDSWITH_INJECTION1 = { "endswith(id,'Str''eet') eq true",
      "(E1.id LIKE CONCAT('%','Str''eet') )" };
  private static final String[] EXPRESSION_PRECEDENCE = {
      "id eq '123' and id ne '123' or (id eq '123' and id ne '123')",
      "(((E1.id = '123') AND (E1.id <> '123')) OR ((E1.id = '123') AND (E1.id <> '123')))" };
  private static final String[] EXPRESSION_DATETIME = { "date eq datetime'2000-01-01T00:00:00'",
      "(E1.date = {ts '2000-01-01 00:00:00.000'})" };
  private static Edm edm = null;

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
  public void testDateTime() {
    assertEquals(EXPRESSION_DATETIME[OUTPUT], parseWhereExpression(
        EXPRESSION_DATETIME[INPUT], false));
  }

  @Test
  public void testPrecedence() {
    assertEquals(EXPRESSION_PRECEDENCE[OUTPUT], parseWhereExpression(
        EXPRESSION_PRECEDENCE[INPUT], false));
  }

  @Test
  public void testSubStringOfSQLInjection() {
    assertEquals(EXPRESSION_SUBSTRINGOF_INJECTION1[OUTPUT], parseWhereExpression(
        EXPRESSION_SUBSTRINGOF_INJECTION1[INPUT], false));
    assertEquals(EXPRESSION_SUBSTRINGOF_INJECTION2[OUTPUT], parseWhereExpression(
        EXPRESSION_SUBSTRINGOF_INJECTION2[INPUT], false));
    assertEquals(EXPRESSION_SUBSTRINGOF_INJECTION3[OUTPUT], parseWhereExpression(
        EXPRESSION_SUBSTRINGOF_INJECTION3[INPUT], false));
  }

  @Test
  public void testEndsWithSQLInjection() {
    assertEquals(EXPRESSION_ENDSWITH_INJECTION1[OUTPUT], parseWhereExpression(
        EXPRESSION_ENDSWITH_INJECTION1[INPUT], false));
  }

  @Test
  public void testSubStringWithAllOperator() {
    assertEquals(EXPRESSION_SUBSTRING_ALL_OP[OUTPUT], parseWhereExpression(EXPRESSION_SUBSTRING_ALL_OP[INPUT], false));
  }

  @Test
  public void testStartsWithWrongOperator() {
    parseWhereExpression(EXPRESSION_STARTS_WITH_WRONG_OP[INPUT], true);
  }

  @Test
  public void testSubStringOf() {
    assertEquals(EXPRESSION_SUBSTRING_OF[OUTPUT], parseWhereExpression(EXPRESSION_SUBSTRING_OF[INPUT], false));
  }

  @Test
  public void testStartsWithEqual() {
    assertEquals(EXPRESSION_STARTS_WITH_EQUAL[OUTPUT], parseWhereExpression(EXPRESSION_STARTS_WITH_EQUAL[INPUT],
        false));
  }

  @Test
  public void testEscapeCharacters() {
    assertEquals(EXPRESSION_ESCAPE[OUTPUT], parseWhereExpression(EXPRESSION_ESCAPE[INPUT], false));
  }

  @Test
  public void testNotEndsWithToLowerMethod() {
    assertEquals(EXPRESSION_NOT_ENDS_WITH[OUTPUT], parseWhereExpression(EXPRESSION_NOT_ENDS_WITH[INPUT], false));
  }

  @Test
  public void testNestedMethod() {
    assertEquals(EXPRESSION_NESTED_METHOD[OUTPUT], parseWhereExpression(EXPRESSION_NESTED_METHOD[INPUT], false));
  }

  @Test
  public void testNotStartsWith() {
    assertEquals(EXPRESSION_NOT_STARTS_WITH[OUTPUT], parseWhereExpression(EXPRESSION_NOT_STARTS_WITH[INPUT], false));
  }

  @Test
  public void testStartsWith() {
    assertEquals(EXPRESSION_STARTS_WITH[OUTPUT], parseWhereExpression(EXPRESSION_STARTS_WITH[INPUT], false));
  }

  @Test
  public void testSimpleEqRelation() {
    assertEquals(EXPRESSION_EQ[OUTPUT], parseWhereExpression(EXPRESSION_EQ[INPUT], false));
  }

  @Test
  public void testSimpleNeRelation() {
    assertEquals(EXPRESSION_NE[OUTPUT], parseWhereExpression(EXPRESSION_NE[INPUT], false));
  }

  @Test
  public void testBinaryAnd() {
    assertEquals(EXPRESSION_BINARY_AND[OUTPUT], parseWhereExpression(EXPRESSION_BINARY_AND[INPUT], false));
  }

  @Test
  public void testBinaryOr() {
    assertEquals(EXPRESSION_BINARY_OR[OUTPUT], parseWhereExpression(EXPRESSION_BINARY_OR[INPUT], false));
  }

  @Test
  public void testMemberOr() {
    assertEquals(EXPRESSION_MEMBER_OR[OUTPUT], parseWhereExpression(EXPRESSION_MEMBER_OR[INPUT], false));
  }

  private String parseWhereExpression(final String input, final boolean isExceptionExpected) {
    FilterExpression expression;
    try {
      expression = UriParser.parseFilter(edm, edm.getEntityType(NAMESPACE, ENTITY_NOTE), input);
      String expressionString = ODataExpressionParser.parseToJPAWhereExpression(expression, TABLE_ALIAS);
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
