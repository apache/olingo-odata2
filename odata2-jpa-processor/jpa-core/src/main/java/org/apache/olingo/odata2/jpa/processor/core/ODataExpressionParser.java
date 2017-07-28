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

import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.expression.*;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains utility methods for parsing the filter expressions built by core library from user OData Query.
 *
 *
 *
 */
public class ODataExpressionParser {

  public static final String EMPTY = ""; //$NON-NLS-1$
  public static final ThreadLocal<Integer> methodFlag = new ThreadLocal<Integer>();

  /**
   * This method returns the parsed where condition corresponding to the filter input in the user query.
   *
   * @param whereExpression
   *
   * @return Parsed where condition String
   * @throws ODataException
   */

  public static String parseToJPAWhereExpression(final CommonExpression whereExpression, final String tableAlias)
      throws ODataException {
    switch (whereExpression.getKind()) {
    case UNARY:
      final UnaryExpression unaryExpression = (UnaryExpression) whereExpression;
      final String operand = parseToJPAWhereExpression(unaryExpression.getOperand(), tableAlias);

      switch (unaryExpression.getOperator()) {
      case NOT:
        return JPQLStatement.Operator.NOT + JPQLStatement.DELIMITER.PARENTHESIS_LEFT + operand
            + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT; //$NON-NLS-1$ //$NON-NLS-2$
      case MINUS:
        if (operand.startsWith("-")) {
          return operand.substring(1);
        } else {
          return "-" + operand; //$NON-NLS-1$
        }
      default:
        throw new ODataNotImplementedException();
      }

    case FILTER:
      return parseToJPAWhereExpression(((FilterExpression) whereExpression).getExpression(), tableAlias);
    case BINARY:
      final BinaryExpression binaryExpression = (BinaryExpression) whereExpression;
      MethodOperator operator = null;
      if (binaryExpression.getLeftOperand().getKind() == ExpressionKind.METHOD) {
        operator = ((MethodExpression) binaryExpression.getLeftOperand()).getMethod();
      }
      if (operator != null && ((binaryExpression.getOperator() == BinaryOperator.EQ) ||
          (binaryExpression.getOperator() == BinaryOperator.NE))) {
        if (operator == MethodOperator.SUBSTRINGOF) {
          methodFlag.set(1);
        }
      }
      final String left = parseToJPAWhereExpression(binaryExpression.getLeftOperand(), tableAlias);
      final String right = parseToJPAWhereExpression(binaryExpression.getRightOperand(), tableAlias);

      // Special handling for STARTSWITH and ENDSWITH method expression
      if (operator != null && (operator == MethodOperator.STARTSWITH || operator == MethodOperator.ENDSWITH)) {
        if (!binaryExpression.getOperator().equals(BinaryOperator.EQ)) {
          throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.OPERATOR_EQ_NE_MISSING
              .addContent(binaryExpression.getOperator().toString()), null);
        } else if (right.equals("false")) {
          return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left.replaceFirst("LIKE", "NOT LIKE")
              + JPQLStatement.DELIMITER.SPACE
              + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
        } else {
          return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left
              + JPQLStatement.DELIMITER.SPACE
              + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
        }
      }
      switch (binaryExpression.getOperator()) {
      case AND:
        return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
            + JPQLStatement.Operator.AND + JPQLStatement.DELIMITER.SPACE
            + right + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
      case OR:
        return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
            + JPQLStatement.Operator.OR + JPQLStatement.DELIMITER.SPACE + right
            + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
      case EQ:
        return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
            + (!"null".equals(right) ? JPQLStatement.Operator.EQ : "IS") + JPQLStatement.DELIMITER.SPACE + right
            + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
      case NE:
        return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
            + (!"null".equals(right) ?
                JPQLStatement.Operator.NE :
                "IS" + JPQLStatement.DELIMITER.SPACE + JPQLStatement.Operator.NOT)
            + JPQLStatement.DELIMITER.SPACE + right
            + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
      case LT:
        return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
            + JPQLStatement.Operator.LT + JPQLStatement.DELIMITER.SPACE + right
            + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
      case LE:
        return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
            + JPQLStatement.Operator.LE + JPQLStatement.DELIMITER.SPACE + right
            + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
      case GT:
        return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
            + JPQLStatement.Operator.GT + JPQLStatement.DELIMITER.SPACE + right
            + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
      case GE:
        return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
            + JPQLStatement.Operator.GE + JPQLStatement.DELIMITER.SPACE + right
            + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
      case PROPERTY_ACCESS:
        throw new ODataNotImplementedException();
      default:
        throw new ODataNotImplementedException();

      }

    case PROPERTY:
      String returnStr = tableAlias + JPQLStatement.DELIMITER.PERIOD
          + getPropertyName(whereExpression);
      return returnStr;

    case MEMBER:
      String memberExpStr = EMPTY;
      int i = 0;
      MemberExpression member = null;
      CommonExpression tempExp = whereExpression;
      while (tempExp != null && tempExp.getKind() == ExpressionKind.MEMBER) {
        member = (MemberExpression) tempExp;
        if (i > 0) {
          memberExpStr = JPQLStatement.DELIMITER.PERIOD + memberExpStr;
        }
        i++;
        memberExpStr = getPropertyName(member.getProperty()) + memberExpStr;
        tempExp = member.getPath();
      }
      memberExpStr =
          getPropertyName(tempExp) + JPQLStatement.DELIMITER.PERIOD + memberExpStr;
      return tableAlias + JPQLStatement.DELIMITER.PERIOD + memberExpStr;

    case LITERAL:
      final LiteralExpression literal = (LiteralExpression) whereExpression;
      final EdmSimpleType literalType = (EdmSimpleType) literal.getEdmType();
      EdmLiteral uriLiteral = EdmSimpleTypeKind.parseUriLiteral(literal.getUriLiteral());
      return evaluateComparingExpression(uriLiteral.getLiteral(), literalType);

    case METHOD:
      final MethodExpression methodExpression = (MethodExpression) whereExpression;
      String first = parseToJPAWhereExpression(methodExpression.getParameters().get(0), tableAlias);
      String second =
          methodExpression.getParameterCount() > 1 ? parseToJPAWhereExpression(methodExpression.getParameters().get(1),
              tableAlias) : null;
      String third =
          methodExpression.getParameterCount() > 2 ? parseToJPAWhereExpression(methodExpression.getParameters().get(2),
              tableAlias) : null;

      switch (methodExpression.getMethod()) {
      case SUBSTRING:
        third = third != null ? ", " + third : "";
        return String.format("SUBSTRING(%s, %s + 1 %s)", first, second, third);
      case SUBSTRINGOF:
        if (methodFlag.get() != null && methodFlag.get() == 1) {
          methodFlag.set(null);
          updateValueIfWildcards(first);
          return String.format("(CASE WHEN (%s LIKE CONCAT('%%',CONCAT(%s,'%%')) ESCAPE '\\') "
              + "THEN TRUE ELSE FALSE END)",
              second, first);
        } else {
          first = updateValueIfWildcards(first);
          return String.format("(CASE WHEN (%s LIKE CONCAT('%%',CONCAT(%s,'%%')) ESCAPE '\\') "
              + "THEN TRUE ELSE FALSE END) = true",
              second, first);
        }
      case TOLOWER:
        return String.format("LOWER(%s)", first);
      case STARTSWITH:
        // second = second.substring(1, second.length() - 1);
        second = updateValueIfWildcards(second);
        return String.format("%s LIKE CONCAT(%s,'%%') ESCAPE '\\'", first, second);
      case ENDSWITH:
        // second = second.substring(1, second.length() - 1);
        second = updateValueIfWildcards(second);
        return String.format("%s LIKE CONCAT('%%',%s) ESCAPE '\\'", first, second);
      default:
        throw new ODataNotImplementedException();
      }

    default:
      throw new ODataNotImplementedException();
    }
  }

  /**
   * This method escapes the wildcards
   * @param first
   */
  private static String updateValueIfWildcards(String value) {
    value = value.replace("\\", "\\\\");
    value = value.replace("%", "\\%");
    value = value.replace("_", "\\_");
    return value;
  }
  /**
   * This method parses the select clause
   *
   * @param tableAlias
   * @param selectedFields
   * @return a select expression
   */
  public static String parseToJPASelectExpression(final String tableAlias, final ArrayList<String> selectedFields) {

    if ((selectedFields == null) || (selectedFields.size() == 0)) {
      return tableAlias;
    }

    String selectClause = EMPTY;
    Iterator<String> itr = selectedFields.iterator();
    int count = 0;

    while (itr.hasNext()) {
      selectClause = selectClause + tableAlias + JPQLStatement.DELIMITER.PERIOD + itr.next();
      count++;

      if (count < selectedFields.size()) {
        selectClause = selectClause + JPQLStatement.DELIMITER.COMMA + JPQLStatement.DELIMITER.SPACE;
      }
    }
    return selectClause;
  }

  /**
   * This method parses the order by condition in the query.
   *
   * @param orderByExpression
   * @return a map of JPA attributes and their sort order
   * @throws ODataJPARuntimeException
   */
  public static String parseToJPAOrderByExpression(final OrderByExpression orderByExpression,
      final String tableAlias) throws ODataJPARuntimeException {
    String jpqlOrderByExpression = "";
    if (orderByExpression != null && orderByExpression.getOrders() != null) {
      List<OrderExpression> orderBys = orderByExpression.getOrders();
      String orderByField = null;
      String orderByDirection = null;
      for (OrderExpression orderBy : orderBys) {

        try {
          if (orderBy.getExpression().getKind() == ExpressionKind.MEMBER) {
            orderByField = parseToJPAWhereExpression(orderBy.getExpression(), tableAlias);
          } else {
            orderByField = tableAlias + JPQLStatement.DELIMITER.PERIOD + getPropertyName(orderBy.getExpression());
          }
          orderByDirection = (orderBy.getSortOrder() == SortOrder.asc) ? EMPTY :
              JPQLStatement.DELIMITER.SPACE + "DESC"; //$NON-NLS-1$
          jpqlOrderByExpression += orderByField + orderByDirection + " , ";
        } catch (EdmException e) {
          throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
        } catch (ODataException e) {
          throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
        }
      }
    }
    return normalizeOrderByExpression(jpqlOrderByExpression);
  }

  private static String normalizeOrderByExpression(final String jpqlOrderByExpression) {
    if (jpqlOrderByExpression != "") {
      return jpqlOrderByExpression.substring(0, jpqlOrderByExpression.length() - 3);
    } else {
      return jpqlOrderByExpression;
    }
  }

  /**
   * This method evaluated the where expression for read of an entity based on the keys specified in the query.
   *
   * @param keyPredicates
   * @return the evaluated where expression
   */

  public static String parseKeyPredicates(final List<KeyPredicate> keyPredicates, final String tableAlias)
      throws ODataJPARuntimeException {
    String literal = null;
    String propertyName = null;
    EdmSimpleType edmSimpleType = null;
    StringBuilder keyFilters = new StringBuilder();
    int i = 0;
    for (KeyPredicate keyPredicate : keyPredicates) {
      if (i > 0) {
        keyFilters.append(JPQLStatement.DELIMITER.SPACE + JPQLStatement.Operator.AND + JPQLStatement.DELIMITER.SPACE);
      }
      i++;
      literal = keyPredicate.getLiteral();
      try {
        propertyName = keyPredicate.getProperty().getMapping().getInternalName();
        edmSimpleType = (EdmSimpleType) keyPredicate.getProperty().getType();
      } catch (EdmException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
      }

      literal = evaluateComparingExpression(literal, edmSimpleType);

      if (edmSimpleType == EdmSimpleTypeKind.DateTime.getEdmSimpleTypeInstance()
          || edmSimpleType == EdmSimpleTypeKind.DateTimeOffset.getEdmSimpleTypeInstance()) {
        literal = literal.substring(literal.indexOf('\''), literal.indexOf('}'));
      }

      keyFilters.append(tableAlias + JPQLStatement.DELIMITER.PERIOD + propertyName + JPQLStatement.DELIMITER.SPACE
          + JPQLStatement.Operator.EQ + JPQLStatement.DELIMITER.SPACE + literal);
    }
    if (keyFilters.length() > 0) {
      return keyFilters.toString();
    } else {
      return null;
    }
  }

  public static String parseKeyPropertiesToJPAOrderByExpression(
      final List<EdmProperty> edmPropertylist, final String tableAlias) throws ODataJPARuntimeException {
    String propertyName = null;
    String orderExpression = "";
    if (edmPropertylist == null) {
      return orderExpression;
    }
    for (EdmProperty edmProperty : edmPropertylist) {
      try {
        EdmMapping mapping = edmProperty.getMapping();
        if (mapping != null && mapping.getInternalName() != null) {
          propertyName = mapping.getInternalName();// For embedded/complex keys
        } else {
          propertyName = edmProperty.getName();
        }
      } catch (EdmException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
      }
      orderExpression += tableAlias + JPQLStatement.DELIMITER.PERIOD + propertyName + " , ";
    }
    return normalizeOrderByExpression(orderExpression);
  }

  /**
   * This method evaluates the expression based on the type instance. Used for adding escape characters where necessary.
   *
   * @param uriLiteral
   * @param edmSimpleType
   * @return the evaluated expression
   * @throws ODataJPARuntimeException
   */
  private static String evaluateComparingExpression(String uriLiteral, final EdmSimpleType edmSimpleType)
      throws ODataJPARuntimeException {

    if (EdmSimpleTypeKind.String.getEdmSimpleTypeInstance().isCompatible(edmSimpleType)
        || EdmSimpleTypeKind.Guid.getEdmSimpleTypeInstance().isCompatible(edmSimpleType)) {
      uriLiteral = uriLiteral.replaceAll("'", "''");
      uriLiteral = "'" + uriLiteral + "'"; //$NON-NLS-1$	//$NON-NLS-2$
    } else if (EdmSimpleTypeKind.DateTime.getEdmSimpleTypeInstance().isCompatible(edmSimpleType)
        || EdmSimpleTypeKind.DateTimeOffset.getEdmSimpleTypeInstance().isCompatible(edmSimpleType)) {
      try {
        Calendar datetime =
            (Calendar) edmSimpleType.valueOfString(uriLiteral, EdmLiteralKind.DEFAULT, null, edmSimpleType
                .getDefaultType());

        String year = String.format("%04d", datetime.get(Calendar.YEAR));
        String month = String.format("%02d", datetime.get(Calendar.MONTH) + 1);
        String day = String.format("%02d", datetime.get(Calendar.DAY_OF_MONTH));
        String hour = String.format("%02d", datetime.get(Calendar.HOUR_OF_DAY));
        String min = String.format("%02d", datetime.get(Calendar.MINUTE));
        String sec = String.format("%02d", datetime.get(Calendar.SECOND));

        uriLiteral =
            JPQLStatement.DELIMITER.LEFT_BRACE + JPQLStatement.KEYWORD.TIMESTAMP + JPQLStatement.DELIMITER.SPACE + "\'"
                + year + JPQLStatement.DELIMITER.HYPHEN + month + JPQLStatement.DELIMITER.HYPHEN + day
                + JPQLStatement.DELIMITER.SPACE + hour + JPQLStatement.DELIMITER.COLON + min
                + JPQLStatement.DELIMITER.COLON + sec + JPQLStatement.KEYWORD.OFFSET + "\'"
                + JPQLStatement.DELIMITER.RIGHT_BRACE;

      } catch (EdmSimpleTypeException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
      }

    } else if (EdmSimpleTypeKind.Time.getEdmSimpleTypeInstance().isCompatible(edmSimpleType)) {
      try {
        Calendar time =
            (Calendar) edmSimpleType.valueOfString(uriLiteral, EdmLiteralKind.DEFAULT, null, edmSimpleType
                .getDefaultType());

        String hourValue = String.format("%02d", time.get(Calendar.HOUR_OF_DAY));
        String minValue = String.format("%02d", time.get(Calendar.MINUTE));
        String secValue = String.format("%02d", time.get(Calendar.SECOND));

        uriLiteral =
            "\'" + hourValue + JPQLStatement.DELIMITER.COLON + minValue + JPQLStatement.DELIMITER.COLON + secValue
                + "\'";
      } catch (EdmSimpleTypeException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
      }

    } else if (Long.class.equals(edmSimpleType.getDefaultType())) {
      uriLiteral = uriLiteral + JPQLStatement.DELIMITER.LONG; //$NON-NLS-1$
    }
    return uriLiteral;
  }

  private static String getPropertyName(final CommonExpression whereExpression) throws EdmException,
      ODataJPARuntimeException {
    EdmTyped edmProperty = ((PropertyExpression) whereExpression).getEdmProperty();
    EdmMapping mapping;
    if (edmProperty instanceof EdmNavigationProperty) {
      EdmNavigationProperty edmNavigationProperty = (EdmNavigationProperty) edmProperty;
      mapping = edmNavigationProperty.getMapping();
    } else if(edmProperty instanceof EdmProperty) {
      EdmProperty property = (EdmProperty) edmProperty;
      mapping = property.getMapping();
    } else {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL, null);
    }

    return mapping != null ? mapping.getInternalName() : edmProperty.getName();
  }
}
