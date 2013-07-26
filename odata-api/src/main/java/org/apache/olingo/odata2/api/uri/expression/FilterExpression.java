/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.api.uri.expression;

/**
 * Represents a $filter expression in the expression tree returned by {@link org.apache.olingo.odata2.api.uri.UriParser#parseFilterString(org.apache.olingo.odata2.api.edm.EdmEntityType, String)}
 * Used to define the <b>root</b> expression node in an $filter expression tree. 
 * 
 * @author SAP AG
 */
public interface FilterExpression extends CommonExpression {

  /**
   * @return Returns the $filter expression string used to build the expression tree
   */
  String getExpressionString();

  /**
   * @return Returns the expression node representing the first <i>operator</i>,<i>method</i>,<i>literal</i> or <i>property</i> of the expression tree
   */
  CommonExpression getExpression();

}
