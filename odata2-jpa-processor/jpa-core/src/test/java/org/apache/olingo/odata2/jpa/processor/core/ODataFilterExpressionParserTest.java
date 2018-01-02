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
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.api.uri.expression.ExpressionParserException;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement;
import org.junit.BeforeClass;
import org.junit.Test;

public class ODataFilterExpressionParserTest {
  private static final short INPUT = 0;
  private static final short OUTPUT = 1;
  private static final String TABLE_ALIAS = "E1";
  private static final String NAMESPACE = "SalesOrderProcessing";
  private static final String ENTITY_NOTE = "Note";
  // Index 0 - Is test input and Index 1 - Is expected output
  private static final String[] EXPRESSION_EQ = { "id eq '123'", "(E1.id LIKE '123' ESCAPE '\\')" };
  private static final String[] EXPRESSION_NE = { "id ne '123'", "(E1.id <> '123')" };
  private static final String[] EXPRESSION_ESCAPE = { "id ne '123''22'", "(E1.id <> '123''22')" };
  private static final String[] EXPRESSION_BINARY_AND =
  {
      "id le '123' and soId eq 123L and not (substringof(id,'123') eq false) eq true",
      "(((E1.id <= '123') AND (E1.soId = 123)) AND (NOT(((CASE WHEN ('123' LIKE CONCAT('%',CONCAT(E1.id,'%')"
      + ") ESCAPE '\\') "
          + "THEN TRUE ELSE FALSE END) = false)) = true))" };
  private static final String[] EXPRESSION_BINARY_OR = { "id ge '123' or soId gt 123L",
      "((E1.id >= '123') OR (E1.soId > 123))" };
  private static final String[] EXPRESSION_MEMBER_OR = { "id lt '123' or oValue/Currency eq 'INR'",
      "((E1.id < '123') OR (E1.oValue.Currency LIKE 'INR' ESCAPE '\\'))" };
  private static final String[] EXPRESSION_STARTS_WITH = { "startswith(oValue/Currency,'INR')",
      "E1.oValue.Currency LIKE CONCAT('INR','%') ESCAPE '\\'" };
  private static final String[] EXPRESSION_STARTS_WITH_EQUAL = { "startswith(oValue/Currency,'INR') eq true",
      "(E1.oValue.Currency LIKE CONCAT('INR','%') ESCAPE '\\' )" };
  private static final String[] EXPRESSION_NOT_STARTS_WITH = { "startswith(oValue/Currency,'INR') eq false",
      "(E1.oValue.Currency NOT LIKE CONCAT('INR','%') ESCAPE '\\' )" };
  private static final String[] EXPRESSION_NOT_ENDS_WITH = { "endswith(oValue/Currency,tolower('INR')) eq false",
      "(E1.oValue.Currency NOT LIKE CONCAT('%',LOWER('INR')) ESCAPE '\\' )" };
  private static final String[] EXPRESSION_NESTED_METHOD = {
      "endswith(substring(oValue/Currency,2),'INR') eq false",
      "(SUBSTRING(E1.oValue.Currency, 2 + 1 ) NOT LIKE CONCAT('%','INR') ESCAPE '\\' )" };
  private static final String[] EXPRESSION_SUBSTRING_OF = {
      "substringof(id,'123') ne true",
      "((CASE WHEN ('123' LIKE CONCAT('%',CONCAT(E1.id,'%')) ESCAPE '\\') THEN TRUE ELSE FALSE END) <> true)" };
  private static final String[] EXPRESSION_STARTS_WITH_WRONG_OP = { "startswith(oValue/Currency,'INR') lt true", "" };
  private static final String[] EXPRESSION_SUBSTRING_ALL_OP = { "substring(oValue/Currency,1,3) eq 'INR'",
      "(SUBSTRING(E1.oValue.Currency, 1 + 1 , 3) LIKE 'INR' ESCAPE '\\')" };
  private static final String[] EXPRESSION_SUBSTRINGOF_INJECTION1 = {
      "substringof('a'' OR 1=1 OR E1.id LIKE ''b',id) eq true",
      "((CASE WHEN (E1.id LIKE CONCAT('%',CONCAT('a'' OR 1=1 OR E1.id LIKE ''b','%')) ESCAPE '\\') "
          + "THEN TRUE ELSE FALSE END) = true)" };
  private static final String[] EXPRESSION_SUBSTRINGOF_INJECTION2 =
  {
      "substringof('substringof(''a'' OR 1=1 OR E1.id LIKE ''b'',id)',id) eq true",
      "((CASE WHEN (E1.id LIKE CONCAT('%',CONCAT('substringof(''a'' OR 1=1 OR E1.id LIKE ''b'',id)','%')) ESCAPE '\\') "
          + "THEN TRUE ELSE FALSE END) = true)" };
  private static final String[] EXPRESSION_SUBSTRINGOF_INJECTION3 =
  {
      "substringof( substring(' ) OR execute_my_sql OR '' LIKE ',3),'de''') eq true",
      "((CASE WHEN ('de''' LIKE CONCAT('%',CONCAT(SUBSTRING(' ) OR execute_my_sql OR '' LIKE ', 3 + 1 ),'%')"
      + ") ESCAPE '\\') "
          + "THEN TRUE ELSE FALSE END) = true)" };
  private static final String[] EXPRESSION_ENDSWITH_INJECTION1 = { "endswith(id,'Str''eet') eq true",
      "(E1.id LIKE CONCAT('%','Str''eet') ESCAPE '\\' )" };
  private static final String[] EXPRESSION_PRECEDENCE = {
      "id eq '123' and id ne '123' or (id eq '123' and id ne '123')",
      "(((E1.id LIKE '123' ESCAPE '\\') AND (E1.id <> '123')) OR ((E1.id LIKE '123' ESCAPE '\\') "
      + "AND (E1.id <> '123')))" };
  private static final String[] EXPRESSION_DATETIME = { "date eq datetime'2000-01-01T00:00:00'",
      "(E1.date = 2000-01-01 00:00:00.000)" };
  
  private static final String[] EXPRESSION_NULL = { "date eq null", "(E1.date IS null)" };

  private static final String[] EXPRESSION_NOT_NULL = { "date ne null", "(E1.date IS NOT null)" };
  
  private static final String[] EXPRESSION_STARTSWITH_EQBINARY = { "startswith(id,'123') and text eq 'abc'", 
      "(E1.id LIKE CONCAT('123','%') ESCAPE '\\' AND (E1.text LIKE 'abc' ESCAPE '\\'))" };
  
  private static final String[] EXPRESSION_STARTSWITHEQ_EQBINARY = { "startswith(id,'123') eq true and text eq 'abc'", 
  "((E1.id LIKE CONCAT('123','%') ESCAPE '\\' ) AND (E1.text LIKE 'abc' ESCAPE '\\'))" };
  
  private static final String[] EXPRESSION_EQBINARY_STARTSWITH = { "text eq 'abc' and startswith(id,'123')", 
      "((E1.text LIKE 'abc' ESCAPE '\\') AND E1.id LIKE CONCAT('123','%') ESCAPE '\\')" };
  
  private static final String[] EXPRESSION_EQBINARY_STARTSWITHEQ = { "text eq 'abc' and startswith(id,'123') eq true", 
  "((E1.text LIKE 'abc' ESCAPE '\\') AND (E1.id LIKE CONCAT('123','%') ESCAPE '\\' ))" };

  private static final String[] EXPRESSION_STARTSWITH_STARTSWITH = { "startswith(text,'abc') and startswith(id,'123')", 
  "(E1.text LIKE CONCAT('abc','%') ESCAPE '\\' AND E1.id LIKE CONCAT('123','%') ESCAPE '\\')" };
  
  private static final String[] EXPRESSION_STARTSWITHEQ_STARTSWITHEQ = { 
      "startswith(text,'abc') eq true and startswith(id,'123') eq true", 
  "((E1.text LIKE CONCAT('abc','%') ESCAPE '\\' ) AND (E1.id LIKE CONCAT('123','%') ESCAPE '\\' ))" };
  
  private static final String[] EXPRESSION_STARTSWITH_ANDTRUE = {"startswith(text,'abc') and true", 
      "(E1.text LIKE CONCAT('abc','%') ESCAPE '\\' AND true)"};
  
  private static final String[] EXPRESSION_STARTSWITHEQTRUE_ANDTRUE = {"startswith(text,'abc') eq true and true", 
      "((E1.text LIKE CONCAT('abc','%') ESCAPE '\\' ) AND true)"};
  
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
    String whereExpression = parseWhereExpression(EXPRESSION_DATETIME[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_DATETIME[OUTPUT], whereExpression);
  }

  @Test
  public void testPrecedence() {
    String whereExpression = parseWhereExpression(EXPRESSION_PRECEDENCE[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_PRECEDENCE[OUTPUT], whereExpression);
  }

  @Test
  public void testSubStringOfSQLInjection() {
    String whereExpression = parseWhereExpression(EXPRESSION_SUBSTRINGOF_INJECTION1[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_SUBSTRINGOF_INJECTION1[OUTPUT], whereExpression);

    whereExpression = parseWhereExpression(EXPRESSION_SUBSTRINGOF_INJECTION2[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_SUBSTRINGOF_INJECTION2[OUTPUT], whereExpression);

    whereExpression = parseWhereExpression(EXPRESSION_SUBSTRINGOF_INJECTION3[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_SUBSTRINGOF_INJECTION3[OUTPUT], whereExpression);
  }

  @Test
  public void testEndsWithSQLInjection() {
    String whereExpression = parseWhereExpression(EXPRESSION_ENDSWITH_INJECTION1[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_ENDSWITH_INJECTION1[OUTPUT], whereExpression);
  }

  @Test
  public void testSubStringWithAllOperator() {
    String whereExpression = parseWhereExpression(EXPRESSION_SUBSTRING_ALL_OP[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_SUBSTRING_ALL_OP[OUTPUT], whereExpression);
  }

  @Test
  public void testStartsWithWrongOperator() {
    parseWhereExpression(EXPRESSION_STARTS_WITH_WRONG_OP[INPUT], true);
  }

  @Test
  public void testSubStringOf() {
    String whereExpression = parseWhereExpression(EXPRESSION_SUBSTRING_OF[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_SUBSTRING_OF[OUTPUT], whereExpression);
  }

  @Test
  public void testStartsWithEqual() {
    String whereExpression = parseWhereExpression(EXPRESSION_STARTS_WITH_EQUAL[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_STARTS_WITH_EQUAL[OUTPUT], whereExpression);
  }

  @Test
  public void testEscapeCharacters() {
    String whereExpression = parseWhereExpression(EXPRESSION_ESCAPE[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_ESCAPE[OUTPUT], whereExpression);
  }

  @Test
  public void testNotEndsWithToLowerMethod() {
    String whereExpression = parseWhereExpression(EXPRESSION_NOT_ENDS_WITH[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_NOT_ENDS_WITH[OUTPUT], whereExpression);
  }

  @Test
  public void testNestedMethod() {
    String whereExpression = parseWhereExpression(EXPRESSION_NESTED_METHOD[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_NESTED_METHOD[OUTPUT], whereExpression);
  }

  @Test
  public void testNotStartsWith() {
    String whereExpression = parseWhereExpression(EXPRESSION_NOT_STARTS_WITH[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_NOT_STARTS_WITH[OUTPUT], whereExpression);
  }

  @Test
  public void testStartsWith() {
    String whereExpression = parseWhereExpression(EXPRESSION_STARTS_WITH[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_STARTS_WITH[OUTPUT], whereExpression);
  }

  @Test
  public void testSimpleEqRelation() {
    String whereExpression = parseWhereExpression(EXPRESSION_EQ[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_EQ[OUTPUT], whereExpression);
  
  }

  @Test
  public void testSimpleNeRelation() {
    String whereExpression = parseWhereExpression(EXPRESSION_NE[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_NE[OUTPUT], whereExpression);
  }

  @Test
  public void testBinaryAnd() {
    String whereExpression = parseWhereExpression(EXPRESSION_BINARY_AND[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_BINARY_AND[OUTPUT], whereExpression);
  }

  @Test
  public void testBinaryOr() {
    String whereExpression = parseWhereExpression(EXPRESSION_BINARY_OR[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_BINARY_OR[OUTPUT], whereExpression);
  }

  @Test
  public void testMemberOr() {
    String whereExpression = parseWhereExpression(EXPRESSION_MEMBER_OR[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_MEMBER_OR[OUTPUT], whereExpression);
  }

  @Test
  public void testNull() {
    String whereExpression = parseWhereExpression(EXPRESSION_NULL[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_NULL[OUTPUT], whereExpression);
  }

  @Test
  public void testNotNull() {
    String whereExpression = parseWhereExpression(EXPRESSION_NOT_NULL[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_NOT_NULL[OUTPUT], whereExpression);
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
  
  @Test
  public void testStartsWith_BinaryEq() {
    String whereExpression = parseWhereExpression(
        EXPRESSION_STARTSWITH_EQBINARY[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_STARTSWITH_EQBINARY[OUTPUT], whereExpression);
  }
  
  @Test
  public void testBinaryEq_StartsWith() {
    String whereExpression = parseWhereExpression(
        EXPRESSION_EQBINARY_STARTSWITH[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_EQBINARY_STARTSWITH[OUTPUT], whereExpression);
  }
  
  public void testStartsWithEq_BinaryEq() {
    String whereExpression = parseWhereExpression(
        EXPRESSION_STARTSWITHEQ_EQBINARY[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_STARTSWITHEQ_EQBINARY[OUTPUT], whereExpression);
  }
  
  @Test
  public void testBinaryEq_StartsWithEq() {
    String whereExpression = parseWhereExpression(
        EXPRESSION_EQBINARY_STARTSWITHEQ[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_EQBINARY_STARTSWITHEQ[OUTPUT], whereExpression);
  }
  
  @Test
  public void testStartsWith_StartsWith() {
    String whereExpression = parseWhereExpression(
        EXPRESSION_STARTSWITH_STARTSWITH[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_STARTSWITH_STARTSWITH[OUTPUT], whereExpression);
  }
  
  @Test
  public void testStartsWithEq_StartsWithEq() {
    String whereExpression = parseWhereExpression(
        EXPRESSION_STARTSWITHEQ_STARTSWITHEQ[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_STARTSWITHEQ_STARTSWITHEQ[OUTPUT], whereExpression);
  }
  
  @Test
  public void testStartsWithEq_AndTrue() {
    String whereExpression = parseWhereExpression(
        EXPRESSION_STARTSWITHEQTRUE_ANDTRUE[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_STARTSWITHEQTRUE_ANDTRUE[OUTPUT], whereExpression);
  }
  
  @Test
  public void testStarts_AndTrue() {
    String whereExpression = parseWhereExpression(
        EXPRESSION_STARTSWITH_ANDTRUE[INPUT], false);
    whereExpression = replacePositionalParameters(whereExpression);
    assertEquals(EXPRESSION_STARTSWITH_ANDTRUE[OUTPUT], whereExpression);
  }
  
  private String replacePositionalParameters(String whereExpression) {
    Map<Integer, Object> positionalParameters = ODataExpressionParser.getPositionalParameters();
    for (Entry<Integer, Object> param : positionalParameters.entrySet()) {
      Integer key = param.getKey();
      if (param.getValue() instanceof String) {
        whereExpression = whereExpression.replaceAll("\\?" + String.valueOf(key), "\'" + param.getValue() + "\'");
      } else if (param.getValue() instanceof Timestamp || param.getValue() instanceof Calendar){
        Calendar datetime = (Calendar) param.getValue();
        String year = String.format("%04d", datetime.get(Calendar.YEAR));
        String month = String.format("%02d", datetime.get(Calendar.MONTH) + 1);
        String day = String.format("%02d", datetime.get(Calendar.DAY_OF_MONTH));
        String hour = String.format("%02d", datetime.get(Calendar.HOUR_OF_DAY));
        String min = String.format("%02d", datetime.get(Calendar.MINUTE));
        String sec = String.format("%02d", datetime.get(Calendar.SECOND));
        String value =
            year + JPQLStatement.DELIMITER.HYPHEN + month + JPQLStatement.DELIMITER.HYPHEN + day
                + JPQLStatement.DELIMITER.SPACE + hour + JPQLStatement.DELIMITER.COLON + min
                + JPQLStatement.DELIMITER.COLON + sec + JPQLStatement.KEYWORD.OFFSET;
        whereExpression = whereExpression.replaceAll("\\?" + String.valueOf(key), value);
      } else if(param.getValue() instanceof Byte[]){
        byte[] byteValue = convertToByte((Byte[])param.getValue());
        whereExpression = whereExpression.replaceAll("\\?" + String.valueOf(key), new String(byteValue));
      }else {
        whereExpression = whereExpression.replaceAll("\\?" + String.valueOf(key), param.getValue().toString());
      }
    }
    ODataExpressionParser.reInitializePositionalParameters();
    return whereExpression;
  }
  
  private byte[] convertToByte(Byte[] value) {
    int length =  value.length;
    if (length == 0) {
        return new byte[0];
    }
    final byte[] result = new byte[length];
    for (int i = 0; i < length; i++) {
        result[i] = value[i];
    }
    return result;
 }
}
