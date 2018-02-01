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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmLiteral;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.expression.BinaryExpression;
import org.apache.olingo.odata2.api.uri.expression.BinaryOperator;
import org.apache.olingo.odata2.api.uri.expression.CommonExpression;
import org.apache.olingo.odata2.api.uri.expression.ExpressionKind;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.LiteralExpression;
import org.apache.olingo.odata2.api.uri.expression.MemberExpression;
import org.apache.olingo.odata2.api.uri.expression.MethodExpression;
import org.apache.olingo.odata2.api.uri.expression.MethodOperator;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderExpression;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;
import org.apache.olingo.odata2.api.uri.expression.SortOrder;
import org.apache.olingo.odata2.api.uri.expression.UnaryExpression;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;

/**
 * This class contains utility methods for parsing the filter expressions built by core library from user OData Query.
 *
 *
 *
 */
public class ODataExpressionParser {

  public static final String EMPTY = ""; //$NON-NLS-1$
  public static final ThreadLocal<Integer> methodFlag = new ThreadLocal<Integer>();
  public static final Character[] EMPTY_CHARACTER_ARRAY = new Character[0];
  private static int index = 1;
  private static Map<Integer, Object> positionalParameters = new HashMap<Integer, Object>();  
  
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
        reInitializePositionalParameters();
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
      index++;
      final String right = parseToJPAWhereExpression(binaryExpression.getRightOperand(), tableAlias);

      // Special handling for STARTSWITH and ENDSWITH method expression
      if (operator != null && (operator == MethodOperator.STARTSWITH || operator == MethodOperator.ENDSWITH)) {
        if (!binaryExpression.getOperator().equals(BinaryOperator.EQ) && 
            !(binaryExpression.getRightOperand() instanceof LiteralExpression) && 
            ("true".equals(right) || "false".equals(right))) {
          reInitializePositionalParameters();
          throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.OPERATOR_EQ_NE_MISSING
              .addContent(binaryExpression.getOperator().toString()), null);
        } else if (binaryExpression.getOperator().equals(BinaryOperator.EQ)) {
          if ("false".equals(right)) {
            return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left.replaceFirst("LIKE", "NOT LIKE")
                + JPQLStatement.DELIMITER.SPACE
                + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
          } else if ("true".equals(right)){
            return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left
                + JPQLStatement.DELIMITER.SPACE
                + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
          }
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
        EdmSimpleType type = (EdmSimpleType)((BinaryExpression)whereExpression).getLeftOperand().getEdmType();
        if(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance().isCompatible(type)){
          return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
              + (!"null".equals(right) ? JPQLStatement.Operator.LIKE : "IS") + JPQLStatement.DELIMITER.SPACE + right
              + " ESCAPE '\\'" + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
        }
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
        reInitializePositionalParameters();
        throw new ODataNotImplementedException();
      default:
        reInitializePositionalParameters();
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
      return evaluateComparingExpression(uriLiteral.getLiteral(), literalType, null);

    case METHOD:
      final MethodExpression methodExpression = (MethodExpression) whereExpression;
      String first = parseToJPAWhereExpression(methodExpression.getParameters().get(0), tableAlias);
      index++;
      String second =
          methodExpression.getParameterCount() > 1 ? parseToJPAWhereExpression(methodExpression.getParameters().get(1),
              tableAlias) : null;
      index++;
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
          return String.format("(CASE WHEN (%s LIKE CONCAT('%%',CONCAT(%s,'%%')) ESCAPE '\\') "
              + "THEN TRUE ELSE FALSE END)",
              second, first);
        } else {
          return String.format("(CASE WHEN (%s LIKE CONCAT('%%',CONCAT(%s,'%%')) ESCAPE '\\') "
              + "THEN TRUE ELSE FALSE END) = true",
              second, first);
        }
      case TOLOWER:
        return String.format("LOWER(%s)", first);
      case STARTSWITH:
        return String.format("%s LIKE CONCAT(%s,'%%') ESCAPE '\\'", first, second);
      case ENDSWITH:
        return String.format("%s LIKE CONCAT('%%',%s) ESCAPE '\\'", first, second);
      default:
        reInitializePositionalParameters();
        throw new ODataNotImplementedException();
      }

    default:
      throw new ODataNotImplementedException();
    }
  }
  
  public static Map<Integer, Object> getPositionalParameters() {
    return positionalParameters;
  }
  
  public static void reInitializePositionalParameters() {
    index = 1;
    positionalParameters = new HashMap<Integer, Object>();
  }

  /**
   * This method converts String to Byte array
   * @param uriLiteral
   */  
  public static Byte[] toByteArray(String uriLiteral) {
    int length =  uriLiteral.length();
    if (length == 0) {
        return new Byte[0];
    }
    byte[] byteValues = uriLiteral.getBytes();
    final Byte[] result = new Byte[length];
    for (int i = 0; i < length; i++) {
        result[i] = new Byte(byteValues[i]);
    }
    return result;
 }
  
  /**
   * This method escapes the wildcards
   * @param first
   */
  private static String updateValueIfWildcards(String value) {
    if (value != null) {
      value = value.replace("\\", "\\\\");
      value = value.replace("%", "\\%");
      value = value.replace("_", "\\_");
    }
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

    if ((selectedFields == null) || (selectedFields.isEmpty())) {
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
    Class<?> edmMappedType = null;
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
        edmMappedType = ((JPAEdmMappingImpl)keyPredicate.getProperty().getMapping()).getJPAType();
      } catch (EdmException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
      }

      literal = evaluateComparingExpression(literal, edmSimpleType, edmMappedType);

      if(edmSimpleType == EdmSimpleTypeKind.String.getEdmSimpleTypeInstance()){
        keyFilters.append(tableAlias + JPQLStatement.DELIMITER.PERIOD + propertyName + JPQLStatement.DELIMITER.SPACE
            + JPQLStatement.Operator.LIKE + JPQLStatement.DELIMITER.SPACE + literal +  " ESCAPE '\\'");
       
      }else{
        keyFilters.append(tableAlias + JPQLStatement.DELIMITER.PERIOD + propertyName + JPQLStatement.DELIMITER.SPACE
            + JPQLStatement.Operator.EQ + JPQLStatement.DELIMITER.SPACE + literal);
      }
    }
    if (keyFilters.length() > 0) {
      Map<String, Map<Integer, Object>> parameterizedExpressionMap = 
          new HashMap<String, Map<Integer,Object>>();
      parameterizedExpressionMap.put(keyFilters.toString(), ODataExpressionParser.getPositionalParameters());
      ODataParameterizedWhereExpressionUtil.setParameterizedQueryMap(parameterizedExpressionMap);
      return keyFilters.toString();
    } else {
      return null;
    }
  }
  
  /**
   * Convert char array to Character Array
   * */
  public static Character[] toCharacterArray(char[] array) {
    if (array == null) {
        return null;
    } else if (array.length == 0) {
        return EMPTY_CHARACTER_ARRAY;
    }
    final Character[] result = new Character[array.length];
    for (int i = 0; i < array.length; i++) {
        result[i] = new Character(array[i]);
    }
    return result;
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
   * @param edmMappedType 
   * @return the evaluated expression
   * @throws ODataJPARuntimeException
   */
  private static String evaluateComparingExpression(String uriLiteral, final EdmSimpleType edmSimpleType,
      Class<?> edmMappedType) throws ODataJPARuntimeException {

    if (EdmSimpleTypeKind.String.getEdmSimpleTypeInstance().isCompatible(edmSimpleType)
        || EdmSimpleTypeKind.Guid.getEdmSimpleTypeInstance().isCompatible(edmSimpleType)) {
      uriLiteral = uriLiteral.replaceAll("'", "''");
      uriLiteral = updateValueIfWildcards(uriLiteral);
      if (!positionalParameters.containsKey(index)) {
        if(edmMappedType != null){
          evaluateExpressionForString(uriLiteral, edmMappedType);
        }else{
          positionalParameters.put(index, String.valueOf(uriLiteral));
        }
      }
      uriLiteral = "?" + index;
      index++;
    } else if (EdmSimpleTypeKind.DateTime.getEdmSimpleTypeInstance().isCompatible(edmSimpleType)
        || EdmSimpleTypeKind.DateTimeOffset.getEdmSimpleTypeInstance().isCompatible(edmSimpleType)) {
      try {
        Calendar datetime =
            (Calendar) edmSimpleType.valueOfString(uriLiteral, EdmLiteralKind.DEFAULT, null, edmSimpleType
                .getDefaultType());

        if (!positionalParameters.containsKey(index)) {
          positionalParameters.put(index, datetime);
        }
        uriLiteral = "?" + index;
        index++;

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
            hourValue + JPQLStatement.DELIMITER.COLON + minValue + JPQLStatement.DELIMITER.COLON + secValue;
       if (!positionalParameters.containsKey(index)) {
         positionalParameters.put(index, Time.valueOf(uriLiteral));
       }
       uriLiteral = "?" + index;
       index++;
      } catch (EdmSimpleTypeException e) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
      }

    } else {
      uriLiteral = evaluateExpressionForNumbers(uriLiteral, edmSimpleType, edmMappedType);
    }
    return uriLiteral;
  }

  private static String evaluateExpressionForNumbers(String uriLiteral, EdmSimpleType edmSimpleType,
      Class<?> edmMappedType) {
    Class<? extends Object> type = edmMappedType==null? edmSimpleType.getDefaultType():
      edmMappedType;
    int size = positionalParameters.size();
    if (Long.class.equals(type)) {
      if (!positionalParameters.containsKey(index)) {
        positionalParameters.put(index, Long.valueOf(uriLiteral));
      }
    } else if (Double.class.equals(type)) {
      if (!positionalParameters.containsKey(index)) {
        positionalParameters.put(index, Double.valueOf(uriLiteral));
      }
    } else if (Integer.class.equals(type)) {
      if (!positionalParameters.containsKey(index)) {
        positionalParameters.put(index, Integer.valueOf(uriLiteral));
      }
    } else if (Byte.class.equals(type)) {
      if (!positionalParameters.containsKey(index)) {
        positionalParameters.put(index, Byte.valueOf(uriLiteral));
      }
    }  else if (Byte[].class.equals(type)) {
      if (!positionalParameters.containsKey(index)) {
        positionalParameters.put(index, toByteArray(uriLiteral));
      }
    } else if (Short.class.equals(type)) {
      if (!positionalParameters.containsKey(index)) {
        positionalParameters.put(index, Short.valueOf(uriLiteral));
      }
    } else if (BigDecimal.class.equals(type)) {
      if (!positionalParameters.containsKey(index)) {
        positionalParameters.put(index, new BigDecimal(uriLiteral));
      }
    } else if (BigInteger.class.equals(type)) {
      if (!positionalParameters.containsKey(index)) {
        positionalParameters.put(index, new BigInteger(uriLiteral));
      }
    } else if (Float.class.equals(type)) {
      if (!positionalParameters.containsKey(index)) {
        positionalParameters.put(index, Float.valueOf(uriLiteral));
      }
    }
    if(size+1 == positionalParameters.size()){
      uriLiteral = "?" + index;
      index++;
    }
    return uriLiteral;
  }

  private static void evaluateExpressionForString(String uriLiteral, Class<?> edmMappedType) {

    if(edmMappedType.equals(char[].class)){
      positionalParameters.put(index, uriLiteral.toCharArray());
    }else if(edmMappedType.equals(char.class)){
      positionalParameters.put(index, uriLiteral.charAt(0));
    }else if(edmMappedType.equals(Character[].class)){
      char[] charArray = uriLiteral.toCharArray();
      Character[] charObjectArray =toCharacterArray(charArray);
      positionalParameters.put(index, charObjectArray);
    }else if(edmMappedType.equals(Character.class)){
      positionalParameters.put(index, (Character)uriLiteral.charAt(0));
    }else {
      positionalParameters.put(index, String.valueOf(uriLiteral));
    }
  
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
