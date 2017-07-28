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
package org.apache.olingo.odata2.core.uri.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.uri.expression.CommonExpression;
import org.apache.olingo.odata2.api.uri.expression.ExceptionVisitExpression;
import org.apache.olingo.odata2.api.uri.expression.ExpressionKind;
import org.apache.olingo.odata2.api.uri.expression.ExpressionParserException;
import org.apache.olingo.odata2.api.uri.expression.ExpressionVisitor;
import org.apache.olingo.odata2.api.uri.expression.SortOrder;
import org.apache.olingo.odata2.core.exception.MessageService;
import org.apache.olingo.odata2.core.exception.MessageService.Message;
import org.junit.Test;

/**
 *  Helper for testing the expression parser.
 */
public class ParserTool {

  private String expression;
  private CommonExpression tree;
  private CommonExpression curNode;
  private Exception curException;
  private static final Locale DEFAULT_LANGUAGE = new Locale("test", "FOO");

  public ParserTool(final String expression, final boolean isOrder, final boolean addTestfunctions,
      final boolean allowOnlyBinary, final EdmEntityType resourceEntityType) {
    this.expression = expression;

    try {
      if (isOrder) {
        tree = new OrderByParserImpl(resourceEntityType).parseOrderByString(expression);
      } else {
        FilterParserImplTool parser = new FilterParserImplTool(resourceEntityType);
        if (addTestfunctions) {
          parser.addTestfunctions();
        }
        tree = parser.parseFilterString(expression, allowOnlyBinary).getExpression();
      }
    } catch (ExpressionParserException e) {
      curException = e;
    } catch (ExpressionParserInternalError e) {
      curException = e;
    }

    curNode = tree;
  }

  ParserTool aKind(final ExpressionKind kind) {
    String info = "GetInfoKind(" + expression + ")-->";
    assertEquals(info, kind, curNode.getKind());
    return this;
  }

  public ParserTool aUriLiteral(final String uriLiteral) {
    String info = "GetUriLiteral(" + expression + ")-->";
    assertEquals(info, uriLiteral, curNode.getUriLiteral());
    return this;
  }

  /**
   * Verifies that the thrown exception is of {@paramref expected}
   * 
   * @param expected
   * Expected Exception class
   * @return ParserTool
   */
  public ParserTool aExType(final Class<? extends Exception> expected) {
    String info = "GetExceptionType(" + expression + ")-->";

    assertNotNull(info, curException);
    assertTrue(info, expected.isAssignableFrom(curException.getClass()));
    return this;
  }

  /**
   * Verifies that the message text of the thrown exception serialized is {@paramref messageText}
   * 
   * @param messageText
   * Expected message text
   * @return this
   */
  public ParserTool aExMsgText(final String messageText) {
    String info = "aExMessageText(" + expression + ")-->";

    aExType(ODataMessageException.class);

    info = "  " + info + "Expected: '" + messageText + "' Actual: '" + getExceptionText() + "'";
    assertEquals(info, messageText, getExceptionText());

    return this;
  }

  /**
   * Verifies that all place holders in the message text definition of the
   * thrown exception are provided with content
   * 
   * @return ParserTool
   */
  public ParserTool aExMsgContentAllSet() {
    String info = "aExMessageTextNoEmptyTag(" + expression + ")-->";

    aExType(ODataMessageException.class);

    info = "  " + info + "Messagetext: '" + getExceptionText() + "contains [%";
    assertFalse(info, getExceptionText().contains("[%"));

    return this;
  }

  /**
   * Verifies that the message text of the thrown exception is not empty
   * 
   * @return ParserTool
   */
  public ParserTool aExMsgNotEmpty() {
    String info = "aExMsgNotEmpty(" + expression + ")-->";

    aExType(ODataMessageException.class);

    info = "  " + info + "check if Messagetext is empty";
    assertFalse(info, getExceptionText().isEmpty());

    return this;
  }

  public ParserTool aExKey(final MessageReference expressionExpectedAtPos) {
    String info = "GetExceptionType(" + expression + ")-->";

    aExType(ODataMessageException.class);

    assertEquals(info, expressionExpectedAtPos.getKey(),
        ((ODataMessageException) curException).getMessageReference().getKey());
    return this;
  }

  public String getExceptionText() {
    ODataMessageException messageException = (ODataMessageException) curException;
    Message ms = MessageService.getMessage(DEFAULT_LANGUAGE, messageException.getMessageReference());

    return ms.getText();
  }

  public ParserTool aEdmType(final EdmType type) {
    assertNull("aEdmType", curException);
    String info = "GetEdmType(" + expression + ")-->";

    assertNotNull(curNode.getEdmType());
    assertEquals(info, type, curNode.getEdmType());
    return this;
  }

  public ParserTool aSortOrder(final SortOrder orderType) {
    String info = "GetSortOrder(" + expression + ")-->";

    aKind(ExpressionKind.ORDER);
    assertEquals(info, orderType, ((OrderExpressionImpl) curNode).getSortOrder());
    return this;
  }

  public ParserTool aExpr() {
    String info = "GetExpr(" + expression + ")-->";

    aKind(ExpressionKind.ORDER);
    curNode = ((OrderExpressionImpl) curNode).getExpression();
    assertNotNull(info, curNode);

    return this;
  }

  public ParserTool aEdmProperty(final EdmProperty property) {
    String info = "GetEdmProperty(" + expression + ")-->";

    aKind(ExpressionKind.PROPERTY);
    assertEquals(info, property, ((PropertyExpressionImpl) curNode).getEdmProperty());
    return this;
  }

  public ParserTool aSerializedCompr(final String expected) {
    aSerialized(compress(expected));
    return this;
  }

  public ParserTool aSerialized(final String expected) {
    assertNull("aSerialized", curException);

    String actual = null;
    ExpressionVisitor visitor = new VisitorTool();
    try {
      actual = tree.accept(visitor).toString();
    } catch (ExceptionVisitExpression e) {
      fail("Error in visitor:" + e.getLocalizedMessage());
    } catch (ODataApplicationException e) {
      fail("Error in visitor:" + e.getLocalizedMessage());
    }

    String info = "GetSerialized(" + expression + ")-->";
    assertEquals(info, expected, actual);
    return this;
  }

  public ParserTool left() {
    String info = "param(" + expression + ")-->";

    assertTrue(info, curNode.getKind() == ExpressionKind.BINARY
        || curNode.getKind() == ExpressionKind.UNARY
        || curNode.getKind() == ExpressionKind.MEMBER);

    if (curNode.getKind() == ExpressionKind.BINARY) {
      curNode = ((BinaryExpressionImpl) curNode).getLeftOperand();
    } else if (curNode.getKind() == ExpressionKind.UNARY) {
      curNode = ((UnaryExpressionImpl) curNode).getOperand();
    } else if (curNode.getKind() == ExpressionKind.MEMBER) {
      curNode = ((MemberExpressionImpl) curNode).getPath();
    }
    return this;
  }

  public ParserTool right() {
    String info = "param(" + expression + ")-->";

    assertTrue(info, curNode.getKind() == ExpressionKind.BINARY
        || curNode.getKind() == ExpressionKind.UNARY
        || curNode.getKind() == ExpressionKind.MEMBER);

    if (curNode.getKind() == ExpressionKind.BINARY) {
      curNode = ((BinaryExpressionImpl) curNode).getRightOperand();
    } else if (curNode.getKind() == ExpressionKind.UNARY) {
      curNode = ((UnaryExpressionImpl) curNode).getOperand();
    } else if (curNode.getKind() == ExpressionKind.MEMBER) {
      curNode = ((MemberExpressionImpl) curNode).getProperty();
    }
    return this;
  }

  public ParserTool order(final int i) {
    String info = "param(" + expression + ")-->";
    aKind(ExpressionKind.ORDERBY);

    OrderByExpressionImpl orderByExpressionImpl = (OrderByExpressionImpl) curNode;
    assertTrue(info, i < orderByExpressionImpl.getOrdersCount());

    curNode = orderByExpressionImpl.getOrders().get(i);
    return this;
  }

  public ParserTool param(final int i) {
    String info = "param(" + expression + ")-->";
    aKind(ExpressionKind.METHOD);

    MethodExpressionImpl methodExpressionImpl = (MethodExpressionImpl) curNode;
    assertTrue(info, i < methodExpressionImpl.getParameterCount());

    curNode = methodExpressionImpl.getParameters().get(i);
    return this;
  }

  public ParserTool root() {
    curNode = tree;
    return this;
  }

  static public String compress(final String expression) {
    String ret = "";
    char[] charArray = expression.trim().toCharArray();
    Character oldChar = null;
    for (char x : charArray) {
      if ((x != ' ') || (oldChar == null) || (oldChar != ' ')) {
        ret += x;
      }

      oldChar = x;
    }
    ret = ret.replace("{ ", "{");
    ret = ret.replace(" }", "}");
    return ret;
  }

  public static class testParserTool {
    @Test
    public void testCompr() {
      // leading and trailing spaces
      assertEquals("Error in parsertool", compress("   a"), "a");
      assertEquals("Error in parsertool", compress("a    "), "a");
      assertEquals("Error in parsertool", compress("   a    "), "a");

      assertEquals("Error in parsertool", compress("{ a}"), "{a}");
      assertEquals("Error in parsertool", compress("{   a}"), "{a}");
      assertEquals("Error in parsertool", compress("{a   }"), "{a}");
      assertEquals("Error in parsertool", compress("{   a   }"), "{a}");

      assertEquals("Error in parsertool", compress("{ a }"), "{a}");
      assertEquals("Error in parsertool", compress("{ a a }"), "{a a}");
      assertEquals("Error in parsertool", compress("{ a   a }"), "{a a}");

      assertEquals("Error in parsertool", compress("   {   a   a   }   "), "{a a}");

      assertEquals("Error in parsertool", compress("   {   a { }  a   }   "), "{a {} a}");
    }
  }
}
