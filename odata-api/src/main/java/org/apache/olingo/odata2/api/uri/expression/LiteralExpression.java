/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.api.uri.expression;

/**
 * Represents a literal expression node in the expression tree returned by the methods:
 * <li>{@link org.apache.olingo.odata2.api.uri.UriParser#parseFilterString(org.apache.olingo.odata2.api.edm.EdmEntityType, String) }</li>
 * <li>{@link org.apache.olingo.odata2.api.uri.UriParser#parseOrderByString(org.apache.olingo.odata2.api.edm.EdmEntityType, String) }</li> 
 * <br>
 * <br>
 * <p>A literal expression node is inserted in the expression tree for any token witch is no
 * valid <i>operator</i>, <i>method</i> or <i>property</i>.
 * <br>
 * <br>
 * <p><b>For example</b> the filter "$filter=age eq 12" will result in an expression tree
 * with a literal expression node for "12".
 * <br>
 * <br>
 * @author
 */
public interface LiteralExpression extends CommonExpression {

}
