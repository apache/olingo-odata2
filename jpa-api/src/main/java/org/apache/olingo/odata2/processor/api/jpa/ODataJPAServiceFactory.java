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
package org.apache.olingo.odata2.processor.api.jpa;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.processor.api.jpa.factory.ODataJPAAccessFactory;
import org.apache.olingo.odata2.processor.api.jpa.factory.ODataJPAFactory;

/**
 * <p>
 * Extend this factory class and create own instance of {@link org.apache.olingo.odata2.api.ODataService} that
 * transforms Java Persistence
 * Models into an OData Service. The factory class instantiates instances of
 * type {@link org.apache.olingo.odata2.api.edm.provider.EdmProvider} and
 * {@link org.apache.olingo.odata2.api.processor.ODataSingleProcessor}. The OData
 * JPA Processor library provides a default implementation for EdmProvider and
 * OData Single Processor.
 * </p>
 * <p>
 * The factory implementation is passed as servlet init parameter to a JAX-RS
 * runtime which will instantiate a {@link org.apache.olingo.odata2.api.ODataService} implementation using this factory.
 * </p>
 * 
 * <p>
 * <b>Mandatory:</b> Implement the abstract method initializeODataJPAContext. Fill
 * {@link org.apache.olingo.odata2.processor.api.jpa.ODataJPAContext} with context
 * values.
 * </p>
 * 
 * <b>Sample Configuration:</b>
 * 
 * <pre> {@code
 * <servlet>
 *  <servlet-name>ReferenceScenarioServlet</servlet-name>
 *  <servlet-class>org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet</servlet-class>
 *  <init-param>
 *    <param-name>javax.ws.rs.Application</param-name>
 *    <param-value>org.apache.olingo.odata2.core.rest.ODataApplication</param-value>
 *  </init-param>
 *  <init-param>
 *    <param-name>org.apache.olingo.odata2.processor.factory</param-name>
 *    <param-value>foo.bar.sample.processor.SampleProcessorFactory</param-value>
 *  </init-param>
 *  <init-param>
 *    <param-name>org.apache.olingo.odata2.path.split</param-name>
 *    <param-value>2</param-value>
 *  </init-param>
 *  <load-on-startup>1</load-on-startup>
 * </servlet>
 * } </pre>
 */

public abstract class ODataJPAServiceFactory extends ODataServiceFactory {

  private ODataJPAContext oDataJPAContext;
  private ODataContext oDataContext;

  /**
   * Creates an OData Service based on the values set in
   * {@link org.apache.olingo.odata2.processor.api.jpa.ODataJPAContext} and
   * {@link org.apache.olingo.odata2.api.processor.ODataContext}.
   */
  @Override
  public final ODataService createService(final ODataContext ctx) throws ODataException {

    oDataContext = ctx;

    // Initialize OData JPA Context
    oDataJPAContext = initializeODataJPAContext();

    validatePreConditions();

    ODataJPAFactory factory = ODataJPAFactory.createFactory();
    ODataJPAAccessFactory accessFactory = factory.getODataJPAAccessFactory();

    // OData JPA Processor
    if (oDataJPAContext.getODataContext() == null) {
      oDataJPAContext.setODataContext(ctx);
    }

    ODataSingleProcessor odataJPAProcessor = accessFactory.createODataProcessor(oDataJPAContext);

    // OData Entity Data Model Provider based on JPA
    EdmProvider edmProvider = accessFactory.createJPAEdmProvider(oDataJPAContext);

    return createODataSingleProcessorService(edmProvider, odataJPAProcessor);
  }

  private void validatePreConditions() throws ODataJPARuntimeException {

    if (oDataJPAContext.getEntityManagerFactory() == null) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.ENTITY_MANAGER_NOT_INITIALIZED, null);
    }

  }

  /**
   * Implement this method and initialize OData JPA Context. It is mandatory
   * to set an instance of type {@link javax.persistence.EntityManagerFactory} into the context. An exception of type
   * {@link org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPARuntimeException} is thrown if
   * EntityManagerFactory is not initialized. <br>
   * <br>
   * <b>Sample Code:</b> <code>
   * <p>public class JPAReferenceServiceFactory extends ODataJPAServiceFactory{</p>
   * 
   * <blockquote>private static final String PUNIT_NAME = "punit";
   * <br>
   * public ODataJPAContext initializeODataJPAContext() {
   * <blockquote>ODataJPAContext oDataJPAContext = this.getODataJPAContext();
   * <br>
   * EntityManagerFactory emf = Persistence.createEntityManagerFactory(PUNIT_NAME);
   * <br>
   * oDataJPAContext.setEntityManagerFactory(emf);
   * oDataJPAContext.setPersistenceUnitName(PUNIT_NAME);
   * <br> return oDataJPAContext;</blockquote>
   * }</blockquote>
   * } </code>
   * <p>
   * 
   * @return an instance of type {@link org.apache.olingo.odata2.processor.api.jpa.ODataJPAContext}
   * @throws ODataJPARuntimeException
   */
  public abstract ODataJPAContext initializeODataJPAContext() throws ODataJPARuntimeException;

  /**
   * @return an instance of type {@link ODataJPAContext}
   * @throws ODataJPARuntimeException
   */
  public final ODataJPAContext getODataJPAContext() throws ODataJPARuntimeException {
    if (oDataJPAContext == null) {
      oDataJPAContext = ODataJPAFactory.createFactory().getODataJPAAccessFactory().createODataJPAContext();
    }
    if (oDataContext != null) {
      oDataJPAContext.setODataContext(oDataContext);
    }
    return oDataJPAContext;

  }
}
