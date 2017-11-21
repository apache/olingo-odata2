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
package org.apache.olingo.odata2.api;

import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;

/**
 * Creates instance of custom OData service.
 * 
 * 
 */
public abstract class ODataServiceFactory {

  /**
   * Label used in web.xml to assign servlet init parameter to factory class instance.
   */
  public static final String FACTORY_LABEL = "org.apache.olingo.odata2.service.factory";

  /**
   * Label used in core to access application class loader
   */
  public static final String FACTORY_CLASSLOADER_LABEL = "org.apache.olingo.odata2.service.factory.classloader";

  /**
   * Label used in web.xml to assign servlet init parameter to factory class instance.
   */
  public static final String FACTORY_INSTANCE_LABEL = "org.apache.olingo.odata2.service.factory.instance";

  /**
   * Label used in web.xml to assign servlet init parameter for a path split (service resolution).
   */
  public static final String PATH_SPLIT_LABEL = "org.apache.olingo.odata2.path.split";
  
  /**
   * Label used in web.xml to assign servlet init parameter for a accept form encoding.
   */
  public static final String ACCEPT_FORM_ENCODING ="org.apache.olingo.odata.accept.forms.encoding";

  /**
   * Create instance of custom {@link ODataService}.
   * @param ctx OData context object
   * @return A new service instance.
   * @throws ODataException in case of error
   */
  public abstract ODataService createService(ODataContext ctx) throws ODataException;

  /**
   * Create a default service instance based on </code>ODataSingleProcessor<code>.
   * @param provider A custom <code>EdmProvider</code> implementation.
   * @param processor A custom processor implementation derived from <code>ODataSingleProcessor</code> .
   * @return A new default <code>ODataSingleProcessorService</code> instance.
   */
  public ODataService createODataSingleProcessorService(final EdmProvider provider,
      final ODataSingleProcessor processor) {
    return RuntimeDelegate.createODataSingleProcessorService(provider, processor);
  }

  /**
   * A service can return implementation classes for various callback interfaces.
   * @param callbackInterface a interface type to query for implementation
   * @return a callback implementation for this interface or null
   */
  public <T extends ODataCallback> T getCallback(final Class<T> callbackInterface) {
    return null;
  }

}
