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
package org.apache.olingo.odata2.jpa.processor.api;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataErrorCallback;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAErrorCallback;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAAccessFactory;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAFactory;

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
 * {@link org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext} with context
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
 *    <param-name>org.apache.olingo.odata2.service.factory</param-name>
 *    <param-value>foo.bar.sample.service.SampleProcessorFactory</param-value>
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
  private boolean setDetailErrors = false;
  private OnJPAWriteContent onJPAWriteContent = null;
  private ODataJPATransactionContext oDataJPATransactionContext = null;

  /**
   * Creates an OData Service based on the values set in
   * {@link org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext} and
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
   * {@link org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException} is thrown if
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
   * @return an instance of type {@link org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext}
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

  /**
   * The method sets the context whether a detail error message should be thrown
   * or a less detail error message should be thrown by the library.
   * @param setDetailErrors takes
   * <ul><li>true - to indicate that library should throw a detailed error message</li>
   * <li>false - to indicate that library should not throw a detailed error message</li>
   * </ul>
   * 
   */
  protected void setDetailErrors(final boolean setDetailErrors) {
    this.setDetailErrors = setDetailErrors;
  }

  /**
   * The methods sets the context with a callback implementation for JPA provider specific content.
   * For details refer to {@link org.apache.olingo.odata2.jpa.processor.api.OnJPAWriteContent}
   * @param onJPAWriteContent is an instance of type
   * {@link org.apache.olingo.odata2.jpa.processor.api.OnJPAWriteContent}
   */
  protected void setOnWriteJPAContent(final OnJPAWriteContent onJPAWriteContent) {
    this.onJPAWriteContent = onJPAWriteContent;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ODataCallback> T getCallback(final Class<? extends ODataCallback> callbackInterface) {
    if (setDetailErrors == true) {
      if (callbackInterface.isAssignableFrom(ODataErrorCallback.class)) {
        return (T) new ODataJPAErrorCallback();
      }
    }

    if (onJPAWriteContent != null) {
      if (callbackInterface.isAssignableFrom(OnJPAWriteContent.class)) {
        return (T) onJPAWriteContent;
      }
    }

      if (oDataJPATransactionContext != null) {
          if (callbackInterface.isAssignableFrom(ODataJPATransactionContext.class)) {
              return (T) oDataJPATransactionContext;
          }
      }


      return null;
  }


    /**
     * The methods sets the context with a callback implementation for JPA transaction specific content.
     * For details refer to {@link org.apache.olingo.odata2.jpa.processor.api.ODataJPATransactionContext}
     * @param oDataJPATransactionContext is an instance of type
     * {@link org.apache.olingo.odata2.jpa.processor.api.ODataJPATransactionContext}
     */
    protected void setODataJPATransactionContext(final ODataJPATransactionContext oDataJPATransactionContext) {
        this.oDataJPATransactionContext = oDataJPATransactionContext;
    }

    /**
     * Simple method to retrieve the current ODataJPATransactionContext optimized for fast access
     *
     * @return the current ODataJPATransactionContext
     */
    public ODataJPATransactionContext getoDataJPATransactionContext() {
        return oDataJPATransactionContext;
    }
}
