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
package org.apache.olingo.odata2.jpa.processor.api.jpql;

import java.util.List;

import org.apache.olingo.odata2.api.uri.KeyPredicate;

/**
 * The interface provide a view on JPQL select single context.The interface
 * provides methods for accessing the clause of a JPQL SELECT statement like
 * "SELECT". The view can be used for building JPQL statements without any
 * WHERE,JOIN,ORDERBY clauses. The clauses are built from OData read entity
 * request views.
 * 
 * 
 * @see org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement
 * 
 */
public interface JPQLSelectSingleContextView extends JPQLContextView {
  /**
   * The method returns a JPQL SELECT clause. The SELECT clause is built from
   * $select OData system Query option.
   * 
   * @return a String representing a SELECT clause in JPQL
   */
  public String getSelectExpression();

  /**
   * The method returns the list of key predicates that can be used for
   * constructing the WHERE clause in JPQL statements. The OData entity key
   * predicates are thus converted into JPA entity keys.
   * 
   * @return a list of key predicates
   */
  public List<KeyPredicate> getKeyPredicates();
}
