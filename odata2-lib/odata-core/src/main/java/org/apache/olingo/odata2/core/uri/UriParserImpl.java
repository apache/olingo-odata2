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
package org.apache.olingo.odata2.core.uri;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmLiteral;
import org.apache.olingo.odata2.api.edm.EdmLiteralException;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmParameter;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeFacade;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationPropertySegment;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.SelectItem;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.UriNotMatchingException;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.api.uri.UriSyntaxException;
import org.apache.olingo.odata2.api.uri.expression.ExpressionParserException;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.commons.Decoder;
import org.apache.olingo.odata2.core.edm.EdmSimpleTypeFacadeImpl;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;
import org.apache.olingo.odata2.core.uri.expression.FilterParserImpl;
import org.apache.olingo.odata2.core.uri.expression.OrderByParserImpl;

/**
 * Parser for the OData part of the URL.
 * 
 */
public class UriParserImpl extends UriParser {

  private static final Pattern INITIAL_SEGMENT_PATTERN = Pattern
      .compile("(?:([^.()]+)\\.)?([^.()]+)(?:\\((.+)\\)|(\\(\\)))?");
  private static final Pattern NAVIGATION_SEGMENT_PATTERN = Pattern.compile("([^()]+)(?:\\((.+)\\)|(\\(\\)))?");
  private static final Pattern NAMED_VALUE_PATTERN = Pattern.compile("(?:([^=]+)=)?([^=]+)");
  private static final char COMMA = ',';
  private static final char SQUOTE = '\'';
  private static final String ACCEPT_FORM_ENCODING = "odata-accept-forms-encoding";

  private final Edm edm;
  private final EdmSimpleTypeFacade simpleTypeFacade;
  private List<String> pathSegments;
  private String currentPathSegment;
  private UriInfoImpl uriResult;
  private Map<SystemQueryOption, String> systemQueryOptions;
  private Map<String, String> otherQueryParameters;

  public UriParserImpl(final Edm edm) {
    this.edm = edm;
    simpleTypeFacade = new EdmSimpleTypeFacadeImpl();
  }

  /**
   * Parse the URI part after an OData service root,
   * already splitted into path segments and query parameters.
   * @param pathSegments the {@link PathSegment}s of the resource path,
   * potentially percent-encoded
   * @param queryParameters the query parameters, already percent-decoded
   * @return a {@link UriInfoImpl} instance containing the parsed information
   */
  @Override
  public UriInfo parse(final List<PathSegment> pathSegments, final Map<String, String> queryParameters)
      throws UriSyntaxException, UriNotMatchingException, EdmException {

    return parseAll(pathSegments, convertFromSingleMapToMultiMap(queryParameters));
  }

  @Override
  public UriInfo parseAll(final List<PathSegment> pathSegments, final Map<String, List<String>> allQueryParameters)
      throws UriSyntaxException, UriNotMatchingException, EdmException {

    this.pathSegments = copyPathSegmentList(pathSegments);
    systemQueryOptions = new HashMap<SystemQueryOption, String>();
    otherQueryParameters = new HashMap<String, String>();
    uriResult = new UriInfoImpl();

    preparePathSegments();

    handleResourcePath();

    distributeQueryParameters(allQueryParameters);
    checkSystemQueryOptionsCompatibility();
    handleSystemQueryOptions();
    handleOtherQueryParameters();

    return uriResult;
  }

  private <T, K> Map<T, List<K>> convertFromSingleMapToMultiMap(final Map<T, K> singleMap) {
    Map<T, List<K>> multiMap = new HashMap<T, List<K>>();

    for (Entry<T, K> entry : singleMap.entrySet()) {
      List<K> valueList = new LinkedList<K>();
      valueList.add(entry.getValue());

      multiMap.put(entry.getKey(), valueList);
    }

    return multiMap;
  }

  private void preparePathSegments() throws UriSyntaxException {
    // Remove an empty path segment at the start of the OData part of the resource path.
    if (!pathSegments.isEmpty() && "".equals(pathSegments.get(0))) {
      pathSegments.remove(0);
    }

    // Remove an empty path segment at the end of the resource path,
    // although there is nothing in the OData specification that would allow that.
    if (!pathSegments.isEmpty() && "".equals(pathSegments.get(pathSegments.size() - 1))) {
      pathSegments.remove(pathSegments.size() - 1);
    }

    // Intermediate empty path segments are an error, however.
    for (String pathSegment : pathSegments) {
      if ("".equals(pathSegment)) {
        throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
      }
    }
  }

  private void handleResourcePath() throws UriSyntaxException, UriNotMatchingException, EdmException {
    if (pathSegments.isEmpty()) {
      uriResult.setUriType(UriType.URI0);
    } else {

      currentPathSegment = pathSegments.remove(0);
      final String decodedPath = percentDecode(currentPathSegment);

      if ("$metadata".equals(decodedPath)) {
        ensureLastSegment();
        uriResult.setUriType(UriType.URI8);

      } else if ("$batch".equals(decodedPath)) {
        ensureLastSegment();
        uriResult.setUriType(UriType.URI9);

      } else {
        handleNormalInitialSegment();
      }
    }
  }

  private void handleNormalInitialSegment() throws UriSyntaxException, UriNotMatchingException, EdmException {
    final Matcher matcher = INITIAL_SEGMENT_PATTERN.matcher(currentPathSegment);
    if (!matcher.matches()) {
      throw new UriNotMatchingException(UriNotMatchingException.MATCHPROBLEM.addContent(currentPathSegment));
    }

    final String entityContainerName = percentDecode(matcher.group(1));
    final String segmentName = percentDecode(matcher.group(2));
    final String keyPredicate = matcher.group(3);
    final String emptyParentheses = matcher.group(4);

    final EdmEntityContainer entityContainer =
        entityContainerName == null ? edm.getDefaultEntityContainer() : edm.getEntityContainer(entityContainerName);
    if (entityContainer == null) {
      throw new UriNotMatchingException(UriNotMatchingException.CONTAINERNOTFOUND.addContent(entityContainerName));
    }
    uriResult.setEntityContainer(entityContainer);

    final EdmEntitySet entitySet = entityContainer.getEntitySet(segmentName);
    if (entitySet != null) {
      uriResult.setStartEntitySet(entitySet);
      handleEntitySet(entitySet, keyPredicate);
    } else {
      final EdmFunctionImport functionImport = entityContainer.getFunctionImport(segmentName);
      if (functionImport == null) {
        throw new UriNotMatchingException(UriNotMatchingException.NOTFOUND.addContent(segmentName));
      }
      uriResult.setFunctionImport(functionImport);
      handleFunctionImport(functionImport, emptyParentheses, keyPredicate);
    }
  }

  private void handleEntitySet(final EdmEntitySet entitySet, final String keyPredicate) throws UriSyntaxException,
      UriNotMatchingException, EdmException {
    final EdmEntityType entityType = entitySet.getEntityType();

    uriResult.setTargetType(entityType);
    uriResult.setTargetEntitySet(entitySet);

    if (keyPredicate == null) {
      if (pathSegments.isEmpty()) {
        uriResult.setUriType(UriType.URI1);
      } else {
        currentPathSegment = pathSegments.remove(0);
        checkCount();
        if (uriResult.isCount()) {
          uriResult.setUriType(UriType.URI15);
        } else {
          throw new UriSyntaxException(UriSyntaxException.ENTITYSETINSTEADOFENTITY.addContent(entitySet.getName()));
        }
      }
    } else {
      uriResult.setKeyPredicates(parseKey(keyPredicate, entityType));
      if (pathSegments.isEmpty()) {
        uriResult.setUriType(UriType.URI2);
      } else {
        handleNavigationPathOptions();
      }
    }
  }

  private void handleNavigationPathOptions() throws UriSyntaxException, UriNotMatchingException, EdmException {
    currentPathSegment = pathSegments.remove(0);
    final String decodedPath = percentDecode(currentPathSegment);

    checkCount();
    if (uriResult.isCount()) {
      uriResult.setUriType(UriType.URI16); // Count of multiple entities is handled elsewhere

    } else if ("$value".equals(decodedPath)) {
      if (uriResult.getTargetEntitySet().getEntityType().hasStream()) {
        ensureLastSegment();
        uriResult.setUriType(UriType.URI17);
        uriResult.setValue(true);
      } else {
        throw new UriSyntaxException(UriSyntaxException.NOMEDIARESOURCE);
      }

    } else if ("$links".equals(decodedPath)) {
      uriResult.setLinks(true);
      if (pathSegments.isEmpty()) {
        throw new UriSyntaxException(UriSyntaxException.MUSTNOTBELASTSEGMENT.addContent(currentPathSegment));
      }
      currentPathSegment = pathSegments.remove(0);
      handleNavigationProperties();

    } else {
      handleNavigationProperties();
    }
  }

  private void handleNavigationProperties() throws UriSyntaxException, UriNotMatchingException, EdmException {

    final Matcher matcher = NAVIGATION_SEGMENT_PATTERN.matcher(currentPathSegment);
    if (!matcher.matches()) {
      throw new UriNotMatchingException(UriNotMatchingException.MATCHPROBLEM.addContent(currentPathSegment));
    }

    final String navigationPropertyName = percentDecode(matcher.group(1));
    final String keyPredicateName = matcher.group(2);
    final String emptyParentheses = matcher.group(3);

    final EdmTyped property = uriResult.getTargetEntitySet().getEntityType().getProperty(navigationPropertyName);
    if (property == null) {
      throw new UriNotMatchingException(UriNotMatchingException.PROPERTYNOTFOUND.addContent(navigationPropertyName));
    }

    switch (property.getType().getKind()) {
    case SIMPLE:
    case COMPLEX:
      if (keyPredicateName != null || emptyParentheses != null) {
        throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(currentPathSegment));
      }
      if (uriResult.isLinks()) {
        throw new UriSyntaxException(UriSyntaxException.NONAVIGATIONPROPERTY.addContent(property));
      }

      handlePropertyPath((EdmProperty) property);
      break;

    case ENTITY: // navigation properties point to entities
      final EdmNavigationProperty navigationProperty = (EdmNavigationProperty) property;
      if (keyPredicateName != null || emptyParentheses != null) {
        if (navigationProperty.getMultiplicity() != EdmMultiplicity.MANY) {
          throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(currentPathSegment));
        }
      }

      addNavigationSegment(keyPredicateName, navigationProperty);

      boolean many = false;
      if (navigationProperty.getMultiplicity() == EdmMultiplicity.MANY) {
        many = keyPredicateName == null;
      }

      if (pathSegments.isEmpty()) {
        if (many) {
          if (uriResult.isLinks()) {
            uriResult.setUriType(UriType.URI7B);
          } else {
            uriResult.setUriType(UriType.URI6B);
          }
        } else if (uriResult.isLinks()) {
          uriResult.setUriType(UriType.URI7A);
        } else {
          uriResult.setUriType(UriType.URI6A);
        }
      } else if (many || uriResult.isLinks()) {
        currentPathSegment = pathSegments.remove(0);
        checkCount();
        if (!uriResult.isCount()) {
          throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(currentPathSegment));
        }
        if (many) {
          if (uriResult.isLinks()) {
            uriResult.setUriType(UriType.URI50B);
          } else {
            uriResult.setUriType(UriType.URI15);
          }
        } else {
          uriResult.setUriType(UriType.URI50A);
        }
      } else {
        handleNavigationPathOptions();
      }
      break;

    default:
      throw new UriSyntaxException(UriSyntaxException.INVALIDPROPERTYTYPE.addContent(property.getType().getKind()));
    }
  }

  private void addNavigationSegment(final String keyPredicateName, final EdmNavigationProperty navigationProperty)
      throws UriSyntaxException, EdmException {
    final EdmEntitySet targetEntitySet = uriResult.getTargetEntitySet().getRelatedEntitySet(navigationProperty);
    final EdmEntityType targetEntityType = targetEntitySet.getEntityType();
    uriResult.setTargetEntitySet(targetEntitySet);
    uriResult.setTargetType(targetEntityType);

    NavigationSegmentImpl navigationSegment = new NavigationSegmentImpl();
    navigationSegment.setEntitySet(targetEntitySet);
    navigationSegment.setNavigationProperty(navigationProperty);
    if (keyPredicateName != null) {
      navigationSegment.setKeyPredicates(parseKey(keyPredicateName, targetEntityType));
    }
    uriResult.addNavigationSegment(navigationSegment);
  }

  private void handlePropertyPath(final EdmProperty property) throws UriSyntaxException, UriNotMatchingException,
      EdmException {
    uriResult.addProperty(property);
    final EdmType type = property.getType();

    if (pathSegments.isEmpty()) {
      if (type.getKind() == EdmTypeKind.SIMPLE) {
        if (uriResult.getPropertyPath().size() == 1) {
          uriResult.setUriType(UriType.URI5);
        } else {
          uriResult.setUriType(UriType.URI4);
        }
      } else if (type.getKind() == EdmTypeKind.COMPLEX) {
        uriResult.setUriType(UriType.URI3);
      } else {
        throw new UriSyntaxException(UriSyntaxException.INVALIDPROPERTYTYPE.addContent(type.getKind()));
      }
      uriResult.setTargetType(type);
    } else {

      currentPathSegment = percentDecode(pathSegments.remove(0));
      switch (type.getKind()) {
      case SIMPLE:
        if ("$value".equals(percentDecode(currentPathSegment))) {
          ensureLastSegment();
          uriResult.setValue(true);
          if (uriResult.getPropertyPath().size() == 1) {
            uriResult.setUriType(UriType.URI5);
          } else {
            uriResult.setUriType(UriType.URI4);
          }
        } else {
          throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(currentPathSegment));
        }
        uriResult.setTargetType(type);
        break;

      case COMPLEX:
        final EdmProperty nextProperty = (EdmProperty) ((EdmComplexType) type).getProperty(currentPathSegment);
        if (nextProperty == null) {
          throw new UriNotMatchingException(UriNotMatchingException.PROPERTYNOTFOUND.addContent(currentPathSegment));
        }

        handlePropertyPath(nextProperty);
        break;

      default:
        throw new UriSyntaxException(UriSyntaxException.INVALIDPROPERTYTYPE.addContent(type.getKind()));
      }
    }
  }

  private void ensureLastSegment() throws UriSyntaxException {
    if (!pathSegments.isEmpty()) {
      throw new UriSyntaxException(UriSyntaxException.MUSTBELASTSEGMENT.addContent(currentPathSegment));
    }
  }

  private void checkCount() throws UriSyntaxException {
    if ("$count".equals(percentDecode(currentPathSegment))) {
      if (pathSegments.isEmpty()) {
        uriResult.setCount(true);
      } else {
        throw new UriSyntaxException(UriSyntaxException.MUSTBELASTSEGMENT.addContent(currentPathSegment));
      }
    }
  }

  private ArrayList<KeyPredicate> parseKey(final String keyPredicate, final EdmEntityType entityType)
      throws UriSyntaxException, EdmException {
    final List<EdmProperty> keyProperties = entityType.getKeyProperties();
    ArrayList<EdmProperty> parsedKeyProperties = new ArrayList<EdmProperty>();
    ArrayList<KeyPredicate> keyPredicates = new ArrayList<KeyPredicate>();

    final List<String> keys = splitKeyPredicate(keyPredicate);
    for (final String key : keys) {
      final Matcher matcher = NAMED_VALUE_PATTERN.matcher(key);
      if (!matcher.matches()) {
        throw new UriSyntaxException(UriSyntaxException.INVALIDKEYPREDICATE.addContent(keyPredicate));
      }

      String name = percentDecode(matcher.group(1));
      final String value = percentDecode(matcher.group(2));

      if (name == null) {
        if (keyProperties.size() == 1) {
          name = keyProperties.get(0).getName();
        } else {
          throw new UriSyntaxException(UriSyntaxException.MISSINGKEYPREDICATENAME.addContent(key));
        }
      }

      EdmProperty keyProperty = null;
      for (final EdmProperty testKeyProperty : keyProperties) {
        if (testKeyProperty.getName().equals(name)) {
          keyProperty = testKeyProperty;
          break;
        }
      }
      if (keyProperty == null) {
        throw new UriSyntaxException(UriSyntaxException.INVALIDKEYPREDICATE.addContent(keyPredicate));
      }
      if (parsedKeyProperties.contains(keyProperty)) {
        throw new UriSyntaxException(UriSyntaxException.DUPLICATEKEYNAMES.addContent(keyPredicate));
      }
      parsedKeyProperties.add(keyProperty);

      final EdmLiteral uriLiteral = parseLiteral(value, (EdmSimpleType) keyProperty.getType());
      keyPredicates.add(new KeyPredicateImpl(uriLiteral.getLiteral(), keyProperty));
    }

    if (parsedKeyProperties.size() != keyProperties.size()) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDKEYPREDICATE.addContent(keyPredicate));
    }

    return keyPredicates;
  }

  /**
   * Split the <code>keyPredicate</code> string into separate keys (named keys).
   * e.g. <b>EmployeeId='1,,,2',Test='as'</b> will result in a list with two elements
   * <b>EmployeeId='1,,,2'</b> and <b>Test='as'</b>.
   *
   * e.g. <b>'42'</b> will result in a list with onw element <b>'42'</b>.
   *
   * Snippets from ABNF (odata-abnf-construction-rules)
   *
   * <code>
   * keyPredicate = simpleKey / compoundKey
   * simpleKey = OPEN keyPropertyValue CLOSE
   * compoundKey = OPEN keyValuePair *( COMMA keyValuePair ) CLOSE
   * keyValuePair = ( primitiveKeyProperty / keyPropertyAlias ) EQ keyPropertyValue
   * keyPropertyValue = primitiveLiteral
   * keyPropertyAlias = odataIdentifier
   * </code>
   *
   * <code>
   * string = SQUOTE *( SQUOTE-in-string / pchar-no-SQUOTE ) SQUOTE
   * SQUOTE-in-string = SQUOTE SQUOTE ; two consecutive single quotes represent one within a string literal
   * </code>
   *
   * @param keyPredicate keyPredicate to split
   * @return list of separate (named) key values
   */
  private List<String> splitKeyPredicate(String keyPredicate) {
    StringBuilder b = new StringBuilder();
    final List<String> keys = new ArrayList<String>();
    boolean inStringKeyValue = false;
    for (int i = 0; i < keyPredicate.length(); i++) {
      final char curChar = keyPredicate.charAt(i);
      if (SQUOTE == curChar) {
        // also works with SQUOTE-in-string
        inStringKeyValue = !inStringKeyValue;
        b.append(curChar);
      } else if (COMMA == curChar && !inStringKeyValue) {
        keys.add(b.toString());
        b = new StringBuilder();
      } else {
        b.append(curChar);
      }
    }
    keys.add(b.toString());

    return keys;
  }

  private void handleFunctionImport(final EdmFunctionImport functionImport, final String emptyParentheses,
      final String keyPredicate) throws UriSyntaxException, UriNotMatchingException, EdmException {
    final EdmTyped returnType = functionImport.getReturnType();
    final EdmType type = returnType.getType();
    final boolean isCollection = returnType.getMultiplicity() == EdmMultiplicity.MANY;

    if (type.getKind() == EdmTypeKind.ENTITY && isCollection) {
      handleEntitySet(functionImport.getEntitySet(), keyPredicate);
      return;
    }

    if (emptyParentheses != null) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(emptyParentheses));
    }

    uriResult.setTargetType(type);
    switch (type.getKind()) {
    case SIMPLE:
      uriResult.setUriType(isCollection ? UriType.URI13 : UriType.URI14);
      break;
    case COMPLEX:
      uriResult.setUriType(isCollection ? UriType.URI11 : UriType.URI12);
      break;
    case ENTITY:
      uriResult.setUriType(UriType.URI10);
      break;
    default:
      throw new UriSyntaxException(UriSyntaxException.INVALIDRETURNTYPE.addContent(type.getKind()));
    }

    if (!pathSegments.isEmpty()) {
      if (uriResult.getUriType() == UriType.URI14) {
        currentPathSegment = pathSegments.remove(0);
        if ("$value".equals(percentDecode(currentPathSegment))) {
          uriResult.setValue(true);
        } else {
          throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(currentPathSegment));
        }
      }
    }
    ensureLastSegment();
  }

  private void distributeQueryParameters(final Map<String, List<String>> queryParameters) throws UriSyntaxException {
    boolean formEncoding = false;
    if(queryParameters.containsKey(ACCEPT_FORM_ENCODING)){
      formEncoding=Boolean.parseBoolean(queryParameters.get(ACCEPT_FORM_ENCODING).get(0));
      queryParameters.remove(ACCEPT_FORM_ENCODING);
    }
    for (final String queryOptionString : queryParameters.keySet()) {
      final String decodedString = percentDecode(queryOptionString);
      final List<String> valueList = queryParameters.get(queryOptionString);

      if (valueList.size() >= 1) {
        String value = valueList.get(0);
        if(formEncoding){
          value = getFormEncodedValue(value);
        }
        if (decodedString.startsWith("$")) {
          SystemQueryOption queryOption;
          try {
            queryOption = SystemQueryOption.valueOf(decodedString);
          } catch (IllegalArgumentException e) {
            throw new UriSyntaxException(UriSyntaxException.INVALIDSYSTEMQUERYOPTION.addContent(queryOptionString), e);
          }
          if ("".equals(value)) {
            throw new UriSyntaxException(UriSyntaxException.INVALIDNULLVALUE.addContent(queryOptionString));
          } else {
            if (valueList.size() == 1 && !systemQueryOptions.containsKey(queryOption)) {
              systemQueryOptions.put(queryOption, value);
            } else {
              throw new UriSyntaxException(UriSyntaxException.DUPLICATESYSTEMQUERYPARAMETES
                  .addContent(queryOptionString));
            }
          }
        } else {
          otherQueryParameters.put(decodedString, value);
        }
      } else {
        throw new UriSyntaxException(UriSyntaxException.INVALIDNULLVALUE.addContent(queryOptionString));
      }
    }
  }

  private String getFormEncodedValue(String value) {
    if(value.contains("+")){
      value = value.replaceAll("\\+", " ");
    }
    return value;    
  }

  private void checkSystemQueryOptionsCompatibility() throws UriSyntaxException {
    final UriType uriType = uriResult.getUriType();

    for (SystemQueryOption queryOption : systemQueryOptions.keySet()) {

      if (queryOption == SystemQueryOption.$format && (uriType == UriType.URI4 || uriType == UriType.URI5)
          && uriResult.isValue()) {
        throw new UriSyntaxException(UriSyntaxException.INCOMPATIBLESYSTEMQUERYOPTION.addContent(queryOption));
      }

      if (!uriType.isCompatible(queryOption)) {
        throw new UriSyntaxException(UriSyntaxException.INCOMPATIBLESYSTEMQUERYOPTION.addContent(queryOption));
      }
    }
  }

  private void handleSystemQueryOptions() throws UriSyntaxException, UriNotMatchingException, EdmException {

    for (SystemQueryOption queryOption : systemQueryOptions.keySet()) {
      switch (queryOption) {
      case $format:
        handleSystemQueryOptionFormat(systemQueryOptions.get(SystemQueryOption.$format));
        break;
      case $filter:
        handleSystemQueryOptionFilter(systemQueryOptions.get(SystemQueryOption.$filter));
        break;
      case $inlinecount:
        handleSystemQueryOptionInlineCount(systemQueryOptions.get(SystemQueryOption.$inlinecount));
        break;
      case $orderby:
        handleSystemQueryOptionOrderBy(systemQueryOptions.get(SystemQueryOption.$orderby));
        break;
      case $skiptoken:
        handleSystemQueryOptionSkipToken(systemQueryOptions.get(SystemQueryOption.$skiptoken));
        break;
      case $skip:
        handleSystemQueryOptionSkip(systemQueryOptions.get(SystemQueryOption.$skip));
        break;
      case $top:
        handleSystemQueryOptionTop(systemQueryOptions.get(SystemQueryOption.$top));
        break;
      case $expand:
        handleSystemQueryOptionExpand(systemQueryOptions.get(SystemQueryOption.$expand));
        break;
      case $select:
        handleSystemQueryOptionSelect(systemQueryOptions.get(SystemQueryOption.$select));
        break;
      default:
        throw new ODataRuntimeException("Invalid System Query Option " + queryOption);
      }
    }
  }

  private void handleSystemQueryOptionFormat(final String format) throws UriSyntaxException {
    uriResult.setFormat(format);
  }

  private void handleSystemQueryOptionFilter(final String filter) throws UriSyntaxException {
    final EdmType targetType = uriResult.getTargetType();
    if (targetType instanceof EdmEntityType) {
      try {
        uriResult.setFilter(new FilterParserImpl((EdmEntityType) targetType).parseFilterString(filter, true));
      } catch (ExpressionParserException e) {
        throw new UriSyntaxException(UriSyntaxException.INVALIDFILTEREXPRESSION.addContent(filter), e);
      } catch (ODataMessageException e) {
        throw new UriSyntaxException(UriSyntaxException.INVALIDFILTEREXPRESSION.addContent(filter), e);
      }
    }
  }

  private void handleSystemQueryOptionOrderBy(final String orderBy) throws UriSyntaxException {
    final EdmType targetType = uriResult.getTargetType();
    if (targetType instanceof EdmEntityType) {
      try {
        uriResult.setOrderBy(parseOrderByString((EdmEntityType) targetType, orderBy));
      } catch (ExpressionParserException e) {
        throw new UriSyntaxException(UriSyntaxException.INVALIDORDERBYEXPRESSION.addContent(orderBy), e);
      } catch (ODataMessageException e) {
        throw new UriSyntaxException(UriSyntaxException.INVALIDORDERBYEXPRESSION.addContent(orderBy), e);
      }
    }
  }

  private void handleSystemQueryOptionInlineCount(final String inlineCount) throws UriSyntaxException {
    if ("allpages".equals(inlineCount)) {
      uriResult.setInlineCount(InlineCount.ALLPAGES);
    } else if ("none".equals(inlineCount)) {
      uriResult.setInlineCount(InlineCount.NONE);
    } else {
      throw new UriSyntaxException(UriSyntaxException.INVALIDVALUE.addContent(inlineCount));
    }
  }

  private void handleSystemQueryOptionSkipToken(final String skiptoken) throws UriSyntaxException {
    uriResult.setSkipToken(skiptoken);
  }

  private void handleSystemQueryOptionSkip(final String skip) throws UriSyntaxException {
    try {
      uriResult.setSkip(Integer.valueOf(skip));
    } catch (NumberFormatException e) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDVALUE.addContent(skip), e);
    }

    if (skip.startsWith("-")) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDNEGATIVEVALUE.addContent(skip));
    } else if (skip.startsWith("+")) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDVALUE.addContent(skip));
    }
  }

  private void handleSystemQueryOptionTop(final String top) throws UriSyntaxException {
    try {
      uriResult.setTop(Integer.valueOf(top));
    } catch (NumberFormatException e) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDVALUE.addContent(top), e);
    }

    if (top.startsWith("-")) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDNEGATIVEVALUE.addContent(top));
    } else if (top.startsWith("+")) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDVALUE.addContent(top));
    }
  }

  private void handleSystemQueryOptionExpand(final String expandStatement) throws UriSyntaxException,
      UriNotMatchingException, EdmException {
    ArrayList<ArrayList<NavigationPropertySegment>> expand = new ArrayList<ArrayList<NavigationPropertySegment>>();

    if (expandStatement.startsWith(",") || expandStatement.endsWith(",")) {
      throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
    }

    for (String expandItemString : expandStatement.split(",")) {
      expandItemString = expandItemString.trim();
      if ("".equals(expandItemString)) {
        throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
      }
      if (expandItemString.startsWith("/") || expandItemString.endsWith("/")) {
        throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
      }

      ArrayList<NavigationPropertySegment> expandNavigationProperties = new ArrayList<NavigationPropertySegment>();
      EdmEntitySet fromEntitySet = uriResult.getTargetEntitySet();

      for (String expandPropertyName : expandItemString.split("/")) {
        if ("".equals(expandPropertyName)) {
          throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
        }

        final EdmTyped property = fromEntitySet.getEntityType().getProperty(expandPropertyName);
        if (property == null) {
          throw new UriNotMatchingException(UriNotMatchingException.PROPERTYNOTFOUND.addContent(expandPropertyName));
        }
        if (property.getType().getKind() == EdmTypeKind.ENTITY) {
          final EdmNavigationProperty navigationProperty = (EdmNavigationProperty) property;
          fromEntitySet = fromEntitySet.getRelatedEntitySet(navigationProperty);
          NavigationPropertySegmentImpl propertySegment = new NavigationPropertySegmentImpl();
          propertySegment.setNavigationProperty(navigationProperty);
          propertySegment.setTargetEntitySet(fromEntitySet);
          expandNavigationProperties.add(propertySegment);
        } else {
          throw new UriSyntaxException(UriSyntaxException.NONAVIGATIONPROPERTY.addContent(expandPropertyName));
        }
      }
      expand.add(expandNavigationProperties);
    }
    uriResult.setExpand(expand);
  }

  private void handleSystemQueryOptionSelect(final String selectStatement) throws UriSyntaxException,
      UriNotMatchingException, EdmException {
    ArrayList<SelectItem> select = new ArrayList<SelectItem>();

    if (selectStatement.startsWith(",") || selectStatement.endsWith(",")) {
      throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
    }

    for (String selectItemString : selectStatement.split(",")) {
      selectItemString = selectItemString.trim();
      if ("".equals(selectItemString)) {
        throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
      }
      if (selectItemString.startsWith("/") || selectItemString.endsWith("/")) {
        throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
      }

      SelectItemImpl selectItem = new SelectItemImpl();
      boolean exit = false;
      EdmEntitySet fromEntitySet = uriResult.getTargetEntitySet();

      for (String selectedPropertyName : selectItemString.split("/")) {
        if ("".equals(selectedPropertyName)) {
          throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
        }

        if (exit) {
          throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(selectItemString));
        }

        if ("*".equals(selectedPropertyName)) {
          selectItem.setStar(true);
          exit = true;
          continue;
        }

        final EdmTyped property = fromEntitySet.getEntityType().getProperty(selectedPropertyName);
        if (property == null) {
          throw new UriNotMatchingException(UriNotMatchingException.PROPERTYNOTFOUND.addContent(selectedPropertyName));
        }

        switch (property.getType().getKind()) {
        case SIMPLE:
        case COMPLEX:
          selectItem.setProperty((EdmProperty) property);
          exit = true;
          break;

        case ENTITY: // navigation properties point to entities
          final EdmNavigationProperty navigationProperty = (EdmNavigationProperty) property;
          final EdmEntitySet targetEntitySet = fromEntitySet.getRelatedEntitySet(navigationProperty);

          NavigationPropertySegmentImpl navigationPropertySegment = new NavigationPropertySegmentImpl();
          navigationPropertySegment.setNavigationProperty(navigationProperty);
          navigationPropertySegment.setTargetEntitySet(targetEntitySet);
          selectItem.addNavigationPropertySegment(navigationPropertySegment);

          fromEntitySet = targetEntitySet;
          break;

        default:
          throw new UriSyntaxException(UriSyntaxException.INVALIDPROPERTYTYPE);
        }
      }
      select.add(selectItem);
    }
    uriResult.setSelect(select);
  }

  private void handleOtherQueryParameters() throws UriSyntaxException, EdmException {
    final EdmFunctionImport functionImport = uriResult.getFunctionImport();
    if (functionImport != null) {
      for (final String parameterName : functionImport.getParameterNames()) {
        final EdmParameter parameter = functionImport.getParameter(parameterName);
        final String value = otherQueryParameters.remove(parameterName);

        if (value == null) {
          if (parameter.getFacets() == null || parameter.getFacets().isNullable() == null
              || parameter.getFacets().isNullable()) {
            continue;
          } else {
            throw new UriSyntaxException(UriSyntaxException.MISSINGPARAMETER);
          }
        }

        EdmLiteral uriLiteral = parseLiteral(value, (EdmSimpleType) parameter.getType());
        uriResult.addFunctionImportParameter(parameterName, uriLiteral);
      }
    }

    uriResult.setCustomQueryOptions(otherQueryParameters);
  }

  private EdmLiteral parseLiteral(final String value, final EdmSimpleType expectedType) throws UriSyntaxException {
    EdmLiteral literal;
    try {
      literal = simpleTypeFacade.parseUriLiteral(value);
    } catch (EdmLiteralException e) {
      throw convertEdmLiteralException(e);
    }

    if (expectedType.isCompatible(literal.getType())) {
      return literal;
    } else {
      throw new UriSyntaxException(UriSyntaxException.INCOMPATIBLELITERAL.addContent(value, expectedType));
    }
  }

  private static UriSyntaxException convertEdmLiteralException(final EdmLiteralException e) {
    final MessageReference messageReference = e.getMessageReference();

    if (EdmLiteralException.LITERALFORMAT.equals(messageReference)) {
      return new UriSyntaxException(UriSyntaxException.LITERALFORMAT.addContent(messageReference.getContent()), e);
    } else if (EdmLiteralException.NOTEXT.equals(messageReference)) {
      return new UriSyntaxException(UriSyntaxException.NOTEXT.addContent(messageReference.getContent()), e);
    } else if (EdmLiteralException.UNKNOWNLITERAL.equals(messageReference)) {
      return new UriSyntaxException(UriSyntaxException.UNKNOWNLITERAL.addContent(messageReference.getContent()), e);
    } else {
      return new UriSyntaxException(ODataBadRequestException.COMMON, e);
    }
  }

  private static List<String> copyPathSegmentList(final List<PathSegment> source) {
    List<String> copy = new ArrayList<String>();

    for (final PathSegment segment : source) {
      copy.add(segment.getPath());
    }

    return copy;
  }

  private static String percentDecode(final String value) throws UriSyntaxException {
    try {
      return Decoder.decode(value);
    } catch (RuntimeException e) {
      throw new UriSyntaxException(UriSyntaxException.URISYNTAX, e);
    }
  }

  @Override
  public FilterExpression parseFilterString(final EdmEntityType entityType, final String expression)
      throws ODataMessageException {
    return new FilterParserImpl(entityType).parseFilterString(expression);
  }

  @Override
  public OrderByExpression parseOrderByString(final EdmEntityType entityType, final String expression)
      throws ODataMessageException {
    return new OrderByParserImpl(entityType).parseOrderByString(expression);
  }

  @Override
  public ExpandSelectTreeNode buildExpandSelectTree(final List<SelectItem> select,
      final List<ArrayList<NavigationPropertySegment>> expand) throws EdmException {
    return new ExpandSelectTreeCreator(select, expand).create();
  }

  @Override
  protected PathSegment buildPathSegment(String path, Map<String, List<String>> matrixParameters) {
    return new ODataPathSegmentImpl(path, matrixParameters);
  }

  @Override
  public List<KeyPredicate> getKeyFromEntityLink(final EdmEntitySet entitySet, final String entityLink,
      final URI serviceRoot) throws ODataException {
    final String relativeLink = serviceRoot == null ? entityLink :
        entityLink.startsWith(serviceRoot.toString()) ?
            entityLink.substring(serviceRoot.toString().length()) : entityLink;
    final Matcher matcher = INITIAL_SEGMENT_PATTERN.matcher(relativeLink);
    if (!matcher.matches()) {
      throw new UriNotMatchingException(UriNotMatchingException.MATCHPROBLEM.addContent(relativeLink));
    }

    final String entityContainerName = percentDecode(matcher.group(1));
    if (entityContainerName == null && !entitySet.getEntityContainer().isDefaultEntityContainer()
        || entityContainerName != null && !entityContainerName.equals(entitySet.getEntityContainer().getName())) {
      throw new UriNotMatchingException(UriNotMatchingException.CONTAINERNOTFOUND.addContent(entityContainerName));
    }

    final String entitySetName = percentDecode(matcher.group(2));
    if (!entitySetName.equals(entitySet.getName())) {
      throw new UriNotMatchingException(UriNotMatchingException.NOTFOUND.addContent(entitySetName));
    }

    final String keyPredicate = matcher.group(3);
    if (keyPredicate == null) {
      throw new UriSyntaxException(UriSyntaxException.ENTITYSETINSTEADOFENTITY.addContent(entitySetName));
    }
    return parseKey(keyPredicate, entitySet.getEntityType());
  }
}
