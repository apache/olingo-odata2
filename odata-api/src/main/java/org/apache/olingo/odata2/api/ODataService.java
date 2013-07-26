/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.api;

import java.util.List;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
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

/**
 * Root interface for a custom OData service.
 * 
 * @author SAP AG
 *
 */
public interface ODataService {

  /**
   * @return implemented OData version of this service
   * @throws ODataException
   * @see ODataServiceVersion
   */
  String getVersion() throws ODataException;

  /**
   * @return entity data model of this service 
   * @see Edm
   * @throws ODataException
   */
  Edm getEntityDataModel() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see MetadataProcessor
   */
  MetadataProcessor getMetadataProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see ServiceDocumentProcessor
   */
  ServiceDocumentProcessor getServiceDocumentProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see EntityProcessor
   */
  EntityProcessor getEntityProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see EntitySetProcessor
   */
  EntitySetProcessor getEntitySetProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see EntityComplexPropertyProcessor
   */
  EntityComplexPropertyProcessor getEntityComplexPropertyProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see EntityLinkProcessor
   */
  EntityLinkProcessor getEntityLinkProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see EntityLinksProcessor
   */
  EntityLinksProcessor getEntityLinksProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see EntityMediaProcessor
   */
  EntityMediaProcessor getEntityMediaProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see EntitySimplePropertyProcessor
   */
  EntitySimplePropertyProcessor getEntitySimplePropertyProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see EntitySimplePropertyValueProcessor
   */
  EntitySimplePropertyValueProcessor getEntitySimplePropertyValueProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see FunctionImportProcessor
   */
  FunctionImportProcessor getFunctionImportProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see FunctionImportValueProcessor
   */
  FunctionImportValueProcessor getFunctionImportValueProcessor() throws ODataException;

  /**
   * @return a processor which handles this request 
   * @throws ODataException
   * @see BatchProcessor
   */
  BatchProcessor getBatchProcessor() throws ODataException;

  /**
   * @return root processor interface 
   * @throws ODataException
   * @see ODataProcessor
   */
  ODataProcessor getProcessor() throws ODataException;

  /**
   * @param processorFeature 
   * @return ordered list of all <code>content types</code> this service supports
   * @throws ODataException
   */
  List<String> getSupportedContentTypes(Class<? extends ODataProcessor> processorFeature) throws ODataException;
}
