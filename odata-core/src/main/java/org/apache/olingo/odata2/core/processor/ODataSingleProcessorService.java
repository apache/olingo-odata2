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
package org.apache.olingo.odata2.core.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceVersion;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.processor.feature.CustomContentType;
import org.apache.olingo.odata2.api.processor.part.BatchProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityComplexPropertyProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityLinkProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityLinksProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityMediaProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySetProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySimplePropertyProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySimplePropertyValueProcessor;
import org.apache.olingo.odata2.api.processor.part.FunctionImportProcessor;
import org.apache.olingo.odata2.api.processor.part.FunctionImportValueProcessor;
import org.apache.olingo.odata2.api.processor.part.MetadataProcessor;
import org.apache.olingo.odata2.api.processor.part.ServiceDocumentProcessor;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;

/**
 * <p>An {@link ODataService} implementation that uses {@link ODataSingleProcessor}.</p>
 * <p>Usually custom services create an instance by their implementation of
 * {@link org.apache.olingo.odata2.api.ODataServiceFactory} and populate it with their custom {@link EdmProvider}
 * and custom {@link ODataSingleProcessor} implementations.</p>
 *
 * @author SAP AG
 */
public class ODataSingleProcessorService implements ODataService {

  private final ODataSingleProcessor processor;
  private final Edm edm;

  /**
   * Construct service
   * @param provider A custom {@link EdmProvider}
   * @param processor A custom {@link ODataSingleProcessor}
   */
  public ODataSingleProcessorService(final EdmProvider provider, final ODataSingleProcessor processor) {
    this.processor = processor;
    edm = RuntimeDelegate.createEdm(provider);
  }

  /**
   * @see ODataService
   */
  @Override
  public String getVersion() throws ODataException {
    return ODataServiceVersion.V20;
  }

  /**
   * @see ODataService
   */
  @Override
  public Edm getEntityDataModel() throws ODataException {
    return edm;
  }

  /**
   * @see ODataService
   */
  @Override
  public MetadataProcessor getMetadataProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public ServiceDocumentProcessor getServiceDocumentProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntityProcessor getEntityProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntitySetProcessor getEntitySetProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntityComplexPropertyProcessor getEntityComplexPropertyProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntityLinkProcessor getEntityLinkProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntityLinksProcessor getEntityLinksProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntityMediaProcessor getEntityMediaProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntitySimplePropertyProcessor getEntitySimplePropertyProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntitySimplePropertyValueProcessor getEntitySimplePropertyValueProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public FunctionImportProcessor getFunctionImportProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public FunctionImportValueProcessor getFunctionImportValueProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public BatchProcessor getBatchProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public ODataProcessor getProcessor() throws ODataException {
    return processor;
  }

  @Override
  public List<String> getSupportedContentTypes(final Class<? extends ODataProcessor> processorFeature) throws ODataException {
    List<String> result = new ArrayList<String>();

    if (processor instanceof CustomContentType) {
      result.addAll(((CustomContentType) processor).getCustomContentTypes(processorFeature));
    }

    if (processorFeature == BatchProcessor.class) {
      result.add(HttpContentType.MULTIPART_MIXED);
    } else if (processorFeature == EntityProcessor.class) {
      result.add(HttpContentType.APPLICATION_ATOM_XML_ENTRY_UTF8);
      result.add(HttpContentType.APPLICATION_ATOM_XML_UTF8);
      result.add(HttpContentType.APPLICATION_JSON_UTF8);
      result.add(HttpContentType.APPLICATION_JSON_UTF8_VERBOSE);
      result.add(HttpContentType.APPLICATION_XML_UTF8);
    } else if (processorFeature == FunctionImportProcessor.class
        || processorFeature == EntityLinkProcessor.class
        || processorFeature == EntityLinksProcessor.class
        || processorFeature == EntitySimplePropertyProcessor.class
        || processorFeature == EntityComplexPropertyProcessor.class) {
      result.add(HttpContentType.APPLICATION_XML_UTF8);
      result.add(HttpContentType.APPLICATION_JSON_UTF8);
      result.add(HttpContentType.APPLICATION_JSON_UTF8_VERBOSE);
    } else if (processorFeature == EntityMediaProcessor.class
        || processorFeature == EntitySimplePropertyValueProcessor.class
        || processorFeature == FunctionImportValueProcessor.class) {
      result.add(HttpContentType.WILDCARD);
    } else if (processorFeature == EntitySetProcessor.class) {
      result.add(HttpContentType.APPLICATION_ATOM_XML_FEED_UTF8);
      result.add(HttpContentType.APPLICATION_ATOM_XML_UTF8);
      result.add(HttpContentType.APPLICATION_JSON_UTF8);
      result.add(HttpContentType.APPLICATION_JSON_UTF8_VERBOSE);
      result.add(HttpContentType.APPLICATION_XML_UTF8);
    } else if (processorFeature == MetadataProcessor.class) {
      result.add(HttpContentType.APPLICATION_XML_UTF8);
    } else if (processorFeature == ServiceDocumentProcessor.class) {
      result.add(HttpContentType.APPLICATION_ATOM_SVC_UTF8);
      result.add(HttpContentType.APPLICATION_JSON_UTF8);
      result.add(HttpContentType.APPLICATION_JSON_UTF8_VERBOSE);
      result.add(HttpContentType.APPLICATION_XML_UTF8);
    } else {
      throw new ODataNotImplementedException();
    }

    return result;
  }
}
