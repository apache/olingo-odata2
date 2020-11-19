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
package org.apache.olingo.odata2.core.ep;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.servicedocument.ServiceDocument;

/**
 * Interface for all none basic (content type <b>dependent</b>) provider methods.
 * 
 * 
 */
public interface ContentTypeBasedEntityProvider {

  ODataFeed readFeed(EdmEntitySet entitySet, InputStream content, EntityProviderReadProperties properties)
      throws EntityProviderException;

  ODataEntry readEntry(EdmEntitySet entitySet, InputStream content, EntityProviderReadProperties properties)
      throws EntityProviderException;

  Map<String, Object>
      readProperty(EdmProperty edmProperty, InputStream content, EntityProviderReadProperties properties)
          throws EntityProviderException;

  String readLink(EdmEntitySet entitySet, InputStream content) throws EntityProviderException;

  List<String> readLinks(EdmEntitySet entitySet, InputStream content) throws EntityProviderException;

  ODataResponse writeServiceDocument(Edm edm, String serviceRoot) throws EntityProviderException;

  ODataResponse writeFeed(EdmEntitySet entitySet, List<Map<String, Object>> data,
      EntityProviderWriteProperties properties) throws EntityProviderException;

  ODataResponse writeEntry(EdmEntitySet entitySet, Map<String, Object> data, EntityProviderWriteProperties properties)
      throws EntityProviderException;

  ODataResponse writeProperty(EdmProperty edmProperty, Object value) throws EntityProviderException;

  ODataResponse writeLink(EdmEntitySet entitySet, Map<String, Object> data, EntityProviderWriteProperties properties)
      throws EntityProviderException;

  ODataResponse writeLinks(EdmEntitySet entitySet, List<Map<String, Object>> data,
      EntityProviderWriteProperties properties) throws EntityProviderException;

  ODataResponse writeFunctionImport(EdmFunctionImport functionImport, Object data,
      EntityProviderWriteProperties properties) throws EntityProviderException;

  ODataResponse writeErrorDocument(HttpStatusCodes status, String errorCode, String message, Locale locale,
      String innerError);

  ODataResponse writeErrorDocument(ODataErrorContext context);

  ServiceDocument readServiceDocument(InputStream serviceDocument) throws EntityProviderException;

  ODataDeltaFeed readDeltaFeed(EdmEntitySet entitySet, InputStream content, EntityProviderReadProperties properties)
      throws EntityProviderException;

  ODataErrorContext readErrorDocument(InputStream errorDocument) throws EntityProviderException;

  Object readFunctionImport(EdmFunctionImport functionImport, InputStream content,
      EntityProviderReadProperties properties) throws EntityProviderException;
}
