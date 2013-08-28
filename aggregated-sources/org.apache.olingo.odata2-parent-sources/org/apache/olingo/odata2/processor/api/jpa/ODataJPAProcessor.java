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
package org.apache.olingo.odata2.processor.api.jpa;

import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.processor.api.jpa.access.JPAProcessor;
import org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPAException;
import org.apache.olingo.odata2.processor.api.jpa.factory.ODataJPAFactory;

/**
 * Extend this class and implement an OData JPA processor if the default
 * behavior of OData JPA Processor library has to be overwritten.
 * 
 *  
 * 
 * 
 */
public abstract class ODataJPAProcessor extends ODataSingleProcessor {

  /**
   * An instance of
   * {@link org.apache.olingo.odata2.processor.api.jpa.ODataJPAContext} object
   */
  protected ODataJPAContext oDataJPAContext;

  /**
   * An instance of
   * {@link org.apache.olingo.odata2.processor.api.jpa.access.JPAProcessor}. The
   * instance is created using
   * {@link org.apache.olingo.odata2.processor.api.jpa.factory.JPAAccessFactory}.
   */
  protected JPAProcessor jpaProcessor;

  public ODataJPAContext getOdataJPAContext() {
    return oDataJPAContext;
  }

  public void setOdataJPAContext(final ODataJPAContext odataJPAContext) {
    oDataJPAContext = odataJPAContext;
  }

  /**
   * Constructor
   * 
   * @param oDataJPAContext
   *            non null OData JPA Context object
   */
  public ODataJPAProcessor(final ODataJPAContext oDataJPAContext) {
    if (oDataJPAContext == null) {
      throw new IllegalArgumentException(ODataJPAException.ODATA_JPACTX_NULL);
    }
    this.oDataJPAContext = oDataJPAContext;
    jpaProcessor = ODataJPAFactory.createFactory().getJPAAccessFactory().getJPAProcessor(this.oDataJPAContext);
  }

}
