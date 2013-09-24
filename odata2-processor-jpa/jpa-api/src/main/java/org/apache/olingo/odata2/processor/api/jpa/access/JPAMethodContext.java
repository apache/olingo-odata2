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
package org.apache.olingo.odata2.processor.api.jpa.access;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPAModelException;
import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.processor.api.jpa.factory.ODataJPAFactory;
import org.apache.olingo.odata2.processor.api.jpa.jpql.JPQLContextType;

/**
 * The abstract class is a compilation of objects required for building specific
 * instances of JPA Method Context. Extend this class to implement specific
 * implementations of JPQL context types (Create,Update,Function). A JPA method
 * Context is constructed from an OData request. Depending on OData
 * CUD/FunctionImport operation performed on an Entity, a corresponding JPA
 * method context object is built. The object thus built can be used for
 * executing operations on JPA Entity/Custom processor objects. <br>
 * A default implementation is provided by the library.
 * 
 * 
 * @see org.apache.olingo.odata2.processor.api.jpa.access.JPAMethodContextView
 * @see org.apache.olingo.odata2.processor.api.jpa.jpql.JPQLContextType
 * 
 */

public abstract class JPAMethodContext implements JPAMethodContextView {

  protected Object enclosingObject;
  protected ArrayList<JPAFunction> jpaFunction;

  @Override
  /**
   * The method returns list of JPA functions that can be executed on the
   * enclosing object.
   * 
   * @return an instance of list of JPA Function
   */
  public Object getEnclosingObject() {
    return enclosingObject;
  }

  @Override
  /**
   * The method returns list of JPA functions that can be executed on the
   * enclosing object.
   * 
   * @return an instance of list of JPA Function
   */
  public List<JPAFunction> getJPAFunctionList() {
    return jpaFunction;
  }

  protected void setEnclosingObject(final Object enclosingObject) {
    this.enclosingObject = enclosingObject;
  }

  protected void setJpaFunction(final List<JPAFunction> jpaFunctionList) {
    jpaFunction = (ArrayList<JPAFunction>) jpaFunctionList;
  }

  /**
   * the method instantiates an instance of type JPAMethodContextBuilder.
   * 
   * @param contextType
   * indicates the type of JPQLContextBuilder to instantiate.
   * @param resultsView
   * is the OData request view
   * @return {@link org.apache.olingo.odata2.processor.api.jpa.access.JPAMethodContext.JPAMethodContextBuilder}
   * 
   * @throws ODataJPARuntimeException
   */
  public final static JPAMethodContextBuilder
      createBuilder(final JPQLContextType contextType, final Object resultsView) throws ODataJPARuntimeException {
    return JPAMethodContextBuilder.create(contextType, resultsView);
  }

  /**
   * The abstract class is extended by specific JPA Method Context Builder to
   * build JPA Method Context types.
   * 
   * 
   * 
   */
  public static abstract class JPAMethodContextBuilder {

    /**
     * Implement this method to build JPAMethodContext
     * 
     * @return an instance of type JPAMethodContext
     * @throws ODataJPAModelException
     * @throws ODataJPARuntimeException
     */
    public abstract JPAMethodContext build() throws ODataJPAModelException, ODataJPARuntimeException;

    protected JPAMethodContextBuilder() {}

    private static JPAMethodContextBuilder create(final JPQLContextType contextType, final Object resultsView)
        throws ODataJPARuntimeException {
      JPAMethodContextBuilder contextBuilder =
          ODataJPAFactory.createFactory().getJPQLBuilderFactory().getJPAMethodContextBuilder(contextType);

      if (contextBuilder == null) {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.ERROR_JPQLCTXBLDR_CREATE, null);
      }
      contextBuilder.setResultsView(resultsView);
      return contextBuilder;
    }

    protected abstract void setResultsView(Object resultsView);
  }
}
