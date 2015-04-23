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
package org.apache.olingo.odata2.api.uri;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;
import org.apache.olingo.odata2.api.uri.expression.ExpressionParserException;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;

/**
 * Wrapper for UriParser functionality.
 * 
 */
public abstract class UriParser {

  /**
   * Parses path segments and query parameters for the given EDM.
   * @param edm Entity Data Model
   * @param pathSegments list of path segments
   * @param queryParameters query parameters
   * @return {@link UriInfo} information about the parsed URI
   * @throws ODataException
   */
  public static UriInfo parse(final Edm edm, final List<PathSegment> pathSegments,
      final Map<String, String> queryParameters) throws ODataException {
    return RuntimeDelegate.getUriParser(edm).parse(pathSegments, queryParameters);
  }

  /**
   * Parses path segments and query parameters.
   * This method ignores redundant system query parameters.
   * 
   * @param pathSegments list of path segments
   * @param queryParameters query parameters
   * @return {@link UriInfo} information about the parsed URI
   * @throws UriSyntaxException
   * @throws UriNotMatchingException
   * @throws EdmException
   */
  public abstract UriInfo parse(List<PathSegment> pathSegments, Map<String, String> queryParameters)
      throws UriSyntaxException, UriNotMatchingException, EdmException;

  /**
   * Parses path segments and query parameters.
   * Throws an exception if there are redundant system query parameters.
   * 
   * @param pathSegments list of path segments
   * @param queryParameters query parameters
   * @return {@link UriInfo} information about the parsed URI
   * @throws UriSyntaxException
   * @throws UriNotMatchingException
   * @throws EdmException
   */
  public abstract UriInfo parseAll(List<PathSegment> pathSegments, Map<String, List<String>> allQueryParameters)
      throws UriSyntaxException, UriNotMatchingException, EdmException;
  
  /**
   * Parses a $filter expression string and create an expression tree.
   * <p>The current expression parser supports expressions as defined in the
   * OData specification 2.0 with the following restrictions:
   * <ul>
   * <li>the methods "cast", "isof" and "replace" are not supported</li>
   * </ul></p>
   * 
   * <p>The expression parser can be used with providing an Entity Data Model (EDM)
   * and without providing it. When an EDM is provided the expression parser will be
   * as strict as possible. That means:
   * <ul>
   * <li>All properties used in the expression must be defined inside the EDM,</li>
   * <li>the types of EDM properties will be checked against the lists of allowed
   * types per method and per binary or unary operator, respectively</li>
   * </ul>
   * If no EDM is provided the expression parser performs a lax validation:
   * <ul>
   * <li>The properties used in the expression are not looked up inside the EDM
   * and the type of the expression node representing the property will be "null",</li>
   * <li>expression nodes with EDM type "null" are not considered during the parameter
   * type validation, so the return type of the parent expression node will
   * also become "null".</li>
   * </ul>
   * @param edm entity data model of the accessed OData service
   * @param edmType EDM type of the OData entity/complex type/... addressed by the URL
   * @param expression $filter expression string to be parsed
   * @return expression tree which can be traversed with help of the interfaces
   * {@link org.apache.olingo.odata2.api.uri.expression.ExpressionVisitor ExpressionVisitor} and
   * {@link org.apache.olingo.odata2.api.uri.expression.Visitable Visitable}
   * @throws ExpressionParserException thrown due to errors while parsing the $filter expression string
   * @throws ODataMessageException for extensibility
   */
  public static FilterExpression parseFilter(final Edm edm, final EdmEntityType edmType, final String expression)
      throws ExpressionParserException, ODataMessageException {
    return RuntimeDelegate.getUriParser(edm).parseFilterString(edmType, expression);
  }

  /**
   * Parses a $filter expression string and create an expression tree.
   * <p>The current expression parser supports expressions as defined in the
   * OData specification 2.0 with the following restrictions:
   * <ul>
   * <li>the methods "cast", "isof" and "replace" are not supported</li>
   * </ul></p>
   * 
   * <p>The expression parser can be used with providing an Entity Data Model (EDM)
   * and without providing it. When an EDM is provided the expression parser will be
   * as strict as possible. That means:
   * <ul>
   * <li>All properties used in the expression must be defined inside the EDM,</li>
   * <li>the types of EDM properties will be checked against the lists of allowed
   * types per method and per binary or unary operator, respectively</li>
   * </ul>
   * If no EDM is provided the expression parser performs a lax validation:
   * <ul>
   * <li>The properties used in the expression are not looked up inside the EDM
   * and the type of the expression node representing the property will be "null",</li>
   * <li>expression nodes with EDM type "null" are not considered during the parameter
   * type validation, so the return type of the parent expression node will
   * also become "null".</li>
   * </ul>
   * @param edmType EDM type of the OData entity/complex type/... addressed by the URL
   * @param expression $filter expression string to be parsed
   * @return expression tree which can be traversed with help of the interfaces
   * {@link org.apache.olingo.odata2.api.uri.expression.ExpressionVisitor ExpressionVisitor} and
   * {@link org.apache.olingo.odata2.api.uri.expression.Visitable Visitable}
   * @throws ExpressionParserException thrown due to errors while parsing the $filter expression string
   * @throws ODataMessageException for extensibility
   */
  public abstract FilterExpression parseFilterString(EdmEntityType edmType, String expression)
      throws ExpressionParserException, ODataMessageException;

  /**
   * Parses a $orderby expression string and creates an expression tree.
   * @param edm EDM model of the accessed OData service
   * @param edmType EDM type of the OData entity/complex type/... addressed by the URL
   * @param expression $orderby expression string to be parsed
   * @return expression tree which can be traversed with help of the interfaces
   * {@link org.apache.olingo.odata2.api.uri.expression.ExpressionVisitor ExpressionVisitor} and
   * {@link org.apache.olingo.odata2.api.uri.expression.Visitable Visitable}
   * @throws ExpressionParserException thrown due to errors while parsing the $orderby expression string
   * @throws ODataMessageException used for extensibility
   */
  public static OrderByExpression parseOrderBy(final Edm edm, final EdmEntityType edmType, final String expression)
      throws ExpressionParserException, ODataMessageException {
    return RuntimeDelegate.getUriParser(edm).parseOrderByString(edmType, expression);
  }

  /**
   * Parses a $orderby expression string and creates an expression tree.
   * @param edmType EDM type of the OData entity/complex type/... addressed by the URL
   * @param expression $orderby expression string to be parsed
   * @return expression tree which can be traversed with help of the interfaces
   * {@link org.apache.olingo.odata2.api.uri.expression.ExpressionVisitor ExpressionVisitor} and
   * {@link org.apache.olingo.odata2.api.uri.expression.Visitable Visitable}
   * @throws ExpressionParserException thrown due to errors while parsing the $orderby expression string
   * @throws ODataMessageException used for extensibility
   */
  public abstract OrderByExpression parseOrderByString(EdmEntityType edmType, String expression)
      throws ExpressionParserException, ODataMessageException;

  /**
   * Creates an optimized expression tree out of $expand and $select expressions.
   * @param select List of {@link SelectItem select items}
   * @param expand List of Lists of {@link NavigationPropertySegment navigation property segments}
   * @return expression tree of type {@link ExpandSelectTreeNode}
   * @throws EdmException
   */
  public static ExpandSelectTreeNode createExpandSelectTree(final List<SelectItem> select,
      final List<ArrayList<NavigationPropertySegment>> expand) throws EdmException {
    return RuntimeDelegate.getUriParser(null).buildExpandSelectTree(select, expand);
  }

  /**
   * Creates an optimized expression tree out of $expand and $select expressions.
   * @param select List of {@link SelectItem select items}
   * @param expand List of Lists of {@link NavigationPropertySegment navigation property segments}
   * @return expression tree of type {@link ExpandSelectTreeNode}
   * @throws EdmException
   */
  public abstract ExpandSelectTreeNode buildExpandSelectTree(List<SelectItem> select,
      List<ArrayList<NavigationPropertySegment>> expand) throws EdmException;

  /**
   * Creates an path segment object.
   * @param path path of created path segment
   * @param matrixParameters Map of Lists of matrix parameters for this path segemt
   * @return create path segment
   */
  protected abstract PathSegment buildPathSegment(String path, Map<String, List<String>> matrixParameters);


  /**
   * Creates an path segment object.
   * @param path path of created path segment
   * @param matrixParameters Map of Lists of matrix parameters for this path segemt
   * @return create path segment
   */
  public static PathSegment createPathSegment(String path, Map<String, List<String>> matrixParameters) {
    return RuntimeDelegate.getUriParser(null).buildPathSegment(path, matrixParameters);
  }

  /**
   * <p>Retrieves the key predicates from a canonical link to an entity.</p>
   * <p>A canonical link to an entity must follow the pattern
   * <code>[&lt;service root&gt;][&lt;entityContainer&gt;.]&lt;entitySet&gt;(&lt;key&gt;)</code>, i.e.,
   * it must be a relative or absolute URI consisting of an entity set (qualified
   * with an entity-container name if not in the default entity container) and a
   * syntactically valid key that identifies a single entity; example:
   * <code>http://example.server.com/service.svc/Employees('42')</code>.</p>
   * @param entitySet the entity set the entity belongs to
   * @param entityLink the link as String
   * @param serviceRoot the root URI of the service, may be <code>null</code>
   * for a relative link URI
   * @return a list of key predicates
   * @throws ODataException in case the link is malformed
   */
  public static List<KeyPredicate> getKeyPredicatesFromEntityLink(final EdmEntitySet entitySet,
      final String entityLink, final URI serviceRoot) throws ODataException {
    return RuntimeDelegate.getUriParser(null).getKeyFromEntityLink(entitySet, entityLink, serviceRoot);
  }

  /**
   * Retrieves the key predicates from a canonical link to an entity.
   * @param entitySet the entity set the entity belongs to
   * @param entityLink the link as String
   * @param serviceRoot the root URI of the service, may be <code>null</code>
   * for a relative link URI
   * @return a list of key predicates
   * @throws ODataException in case the link is malformed
   * @see #getKeyPredicatesFromEntityLink
   */
  public abstract List<KeyPredicate> getKeyFromEntityLink(EdmEntitySet entitySet, String entityLink,
      URI serviceRoot) throws ODataException;
}
