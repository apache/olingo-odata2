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
package org.apache.olingo.odata2.processor.api.jpa.factory;

import java.util.Locale;

import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.processor.api.jpa.ODataJPAContext;
import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPAMessageService;

/**
 * Factory interface for creating following instances
 * 
 * <p>
 * <ul>
 * <li>OData JPA Processor of type
 * {@link org.apache.olingo.odata2.api.processor.ODataSingleProcessor}</li>
 * <li>JPA EDM Provider of type
 * {@link org.apache.olingo.odata2.api.edm.provider.EdmProvider}</li>
 * <li>OData JPA Context
 * {@link org.apache.olingo.odata2.processor.api.jpa.ODataJPAContext}</li>
 * </ul>
 * </p>
 * 
 *  
 * @see org.apache.olingo.odata2.processor.api.jpa.factory.ODataJPAFactory
 */
public interface ODataJPAAccessFactory {
  /**
   * The method creates an OData JPA Processor. The processor handles runtime
   * behavior of an OData service.
   * 
   * @param oDataJPAContext
   *            an instance of type
   *            {@link org.apache.olingo.odata2.processor.api.jpa.ODataJPAContext}.
   *            The context should be initialized properly and cannot be null.
   * @return An implementation of OData JPA Processor.
   */
  public ODataSingleProcessor createODataProcessor(ODataJPAContext oDataJPAContext);

  /**
   * 
   * @param oDataJPAContext
   *            an instance of type
   *            {@link org.apache.olingo.odata2.processor.api.jpa.ODataJPAContext}.
   *            The context should be initialized properly and cannot be null.
   * @return An implementation of JPA EdmProvider. EdmProvider handles
   *         meta-data.
   */
  public EdmProvider createJPAEdmProvider(ODataJPAContext oDataJPAContext);

  /**
   * The method creates an instance of OData JPA Context. An empty instance is
   * returned.
   * 
   * @return an instance of type
   *         {@link org.apache.olingo.odata2.processor.api.jpa.ODataJPAContext}
   */
  public ODataJPAContext createODataJPAContext();

  /**
   * The method creates an instance of message service for loading language
   * dependent message text.
   * 
   * @param locale
   *            is the language in which the message service should load
   *            message texts.
   * @return an instance of type
   *         {@link org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPAMessageService}
   */
  public ODataJPAMessageService getODataJPAMessageService(Locale locale);
}
