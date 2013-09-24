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
package org.apache.olingo.odata2.api.rt;

import java.io.InputStream;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.batch.BatchResponsePart.BatchResponsePartBuilder;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet.BatchChangeSetBuilder;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart.BatchChangeSetPartBuilder;
import org.apache.olingo.odata2.api.client.batch.BatchQueryPart.BatchQueryPartBuilder;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeFacade;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.ep.EntityProvider.EntityProviderInterface;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataRequest.ODataRequestBuilder;
import org.apache.olingo.odata2.api.processor.ODataResponse.ODataResponseBuilder;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.UriParser;

/**
 * Provides access to core implementation classes for interfaces. This class is used
 * by internal abstract API implementations and it is not intended to be used by others.
 * 
 * @org.apache.olingo.odata2.DoNotImplement
 * 
 */
public abstract class RuntimeDelegate {

  private static final String IMPLEMENTATION = "org.apache.olingo.odata2.core.rt.RuntimeDelegateImpl";

  /**
   * Create a runtime delegate instance from the core library. The core
   * library (org.apache.olingo.odata2.core.jar) needs to be included into the classpath
   * of the using application.
   * @return an implementation object
   */
  private static RuntimeDelegateInstance getInstance() {
    RuntimeDelegateInstance delegate;

    try {
      final Class<?> clazz = Class.forName(RuntimeDelegate.IMPLEMENTATION);

      /*
       * We explicitly do not use the singleton pattern to keep the server state free
       * and avoid class loading issues also during hot deployment.
       */
      final Object object = clazz.newInstance();
      delegate = (RuntimeDelegateInstance) object;

    } catch (final Exception e) {
      throw new RuntimeDelegateException(e);
    }
    return delegate;
  }

  /**
   * An implementation is available in the core library.
   * @org.apache.olingo.odata2.DoNotImplement
   */
  public static abstract class RuntimeDelegateInstance {

    protected abstract ODataResponseBuilder createODataResponseBuilder();

    protected abstract EdmSimpleType getEdmSimpleType(EdmSimpleTypeKind edmSimpleTypeKind);

    protected abstract UriParser getUriParser(Edm edm);

    protected abstract EdmSimpleTypeFacade getSimpleTypeFacade();

    protected abstract Edm createEdm(EdmProvider provider);

    protected abstract EntityProviderInterface createEntityProvider();

    protected abstract ODataService createODataSingleProcessorService(EdmProvider provider,
        ODataSingleProcessor processor);

    protected abstract EdmProvider createEdmProvider(InputStream metadataXml, boolean validate)
        throws EntityProviderException;

    protected abstract BatchResponsePartBuilder createBatchResponsePartBuilder();

    protected abstract ODataRequestBuilder createODataRequestBuilder();

    protected abstract BatchChangeSetBuilder createBatchChangeSetBuilder();

    protected abstract BatchQueryPartBuilder createBatchQueryRequestBuilder();

    protected abstract BatchChangeSetPartBuilder createBatchChangeSetRequest();

  }

  /**
   * Returns a simple type object for given type kind.
   * @param edmSimpleTypeKind type kind
   * @return an implementation object
   */
  public static EdmSimpleType getEdmSimpleType(final EdmSimpleTypeKind edmSimpleTypeKind) {
    return RuntimeDelegate.getInstance().getEdmSimpleType(edmSimpleTypeKind);
  }

  /**
   * Returns an implementation of the EDM simple-type facade.
   * @return an implementation object
   */
  public static EdmSimpleTypeFacade getSimpleTypeFacade() {
    return RuntimeDelegate.getInstance().getSimpleTypeFacade();
  }

  /**
   * Returns a builder for creating response objects with variable parameter sets.
   * @return an implementation object
   */
  public static ODataResponseBuilder createODataResponseBuilder() {
    return RuntimeDelegate.getInstance().createODataResponseBuilder();
  }

  /**
   * Creates and returns an entity data model.
   * @param provider a provider implemented by the OData service
   * @return an implementation object
   */
  public static Edm createEdm(final EdmProvider provider) {
    return RuntimeDelegate.getInstance().createEdm(provider);
  }

  /**
   * Returns an parser which can parse OData uris based on metadata.
   * @param edm metadata of the implemented service
   * @return an implementation object
   */
  public static UriParser getUriParser(final Edm edm) {
    return RuntimeDelegate.getInstance().getUriParser(edm);
  }

  /**
   * Creates and returns a http entity provider.
   * @return an implementation object
   */
  public static EntityProviderInterface createEntityProvider() {
    return RuntimeDelegate.getInstance().createEntityProvider();
  }

  /**
   * Creates and returns a single processor service.
   * @param provider a provider implementation for the metadata of the OData service
   * @param processor a single data processor implementation of the OData service
   * @return a implementation object
   */
  public static ODataService createODataSingleProcessorService(final EdmProvider provider,
      final ODataSingleProcessor processor) {
    return RuntimeDelegate.getInstance().createODataSingleProcessorService(provider, processor);
  }

  /**
   * Creates and returns an edm provider.
   * @param metadataXml a metadata xml input stream (means the metadata document)
   * @param validate true if semantic checks for metadata input stream shall be done
   * @return an instance of EdmProvider
   */
  public static EdmProvider createEdmProvider(final InputStream metadataXml, final boolean validate)
      throws EntityProviderException {
    return RuntimeDelegate.getInstance().createEdmProvider(metadataXml, validate);
  }

  private static class RuntimeDelegateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RuntimeDelegateException(final Exception e) {
      super(e);
    }
  }

  public static BatchResponsePartBuilder createBatchResponsePartBuilder() {
    return RuntimeDelegate.getInstance().createBatchResponsePartBuilder();
  }

  public static ODataRequestBuilder createODataRequestBuilder() {
    return RuntimeDelegate.getInstance().createODataRequestBuilder();
  }

  public static BatchChangeSetBuilder createBatchChangeSetBuilder() {
    return RuntimeDelegate.getInstance().createBatchChangeSetBuilder();
  }

  public static BatchQueryPartBuilder createBatchQueryPartBuilder() {
    return RuntimeDelegate.getInstance().createBatchQueryRequestBuilder();
  }

  public static BatchChangeSetPartBuilder createBatchChangeSetPartBuilder() {
    return RuntimeDelegate.getInstance().createBatchChangeSetRequest();
  }
}
