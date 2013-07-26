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
package org.apache.olingo.odata2.core.rt;

import java.io.InputStream;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.batch.BatchResponsePart.BatchResponsePartBuilder;
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
import org.apache.olingo.odata2.api.rt.RuntimeDelegate.RuntimeDelegateInstance;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.core.ODataRequestImpl;
import org.apache.olingo.odata2.core.ODataResponseImpl;
import org.apache.olingo.odata2.core.batch.BatchResponsePartImpl;
import org.apache.olingo.odata2.core.edm.EdmSimpleTypeFacadeImpl;
import org.apache.olingo.odata2.core.edm.parser.EdmxProvider;
import org.apache.olingo.odata2.core.edm.provider.EdmImplProv;
import org.apache.olingo.odata2.core.ep.ProviderFacadeImpl;
import org.apache.olingo.odata2.core.processor.ODataSingleProcessorService;
import org.apache.olingo.odata2.core.uri.UriParserImpl;

/**
 * @author
 */
public class RuntimeDelegateImpl extends RuntimeDelegateInstance {

  @Override
  protected ODataResponseBuilder createODataResponseBuilder() {
    ODataResponseImpl r = new ODataResponseImpl();
    return r.new ODataResponseBuilderImpl();
  }

  @Override
  protected EdmSimpleType getEdmSimpleType(final EdmSimpleTypeKind edmSimpleType) {
    return EdmSimpleTypeFacadeImpl.getEdmSimpleType(edmSimpleType);
  }

  @Override
  protected UriParser getUriParser(final Edm edm) {
    return new UriParserImpl(edm);
  }

  @Override
  protected EdmSimpleTypeFacade getSimpleTypeFacade() {
    return new EdmSimpleTypeFacadeImpl();
  }

  @Override
  protected Edm createEdm(final EdmProvider provider) {
    return new EdmImplProv(provider);
  }

  @Override
  protected EntityProviderInterface createEntityProvider() {
    return new ProviderFacadeImpl();
  }

  @Override
  protected ODataService createODataSingleProcessorService(final EdmProvider provider, final ODataSingleProcessor processor) {
    return new ODataSingleProcessorService(provider, processor);
  }

  @Override
  protected EdmProvider createEdmProvider(final InputStream metadataXml, final boolean validate) throws EntityProviderException {
    return new EdmxProvider().parse(metadataXml, validate);
  }

  @Override
  protected BatchResponsePartBuilder createBatchResponsePartBuilder() {
    BatchResponsePartImpl part = new BatchResponsePartImpl();
    return part.new BatchResponsePartBuilderImpl();
  }

  @Override
  protected ODataRequestBuilder createODataRequestBuilder() {
    ODataRequestImpl request = new ODataRequestImpl();
    return request.new ODataRequestBuilderImpl();
  }

}
