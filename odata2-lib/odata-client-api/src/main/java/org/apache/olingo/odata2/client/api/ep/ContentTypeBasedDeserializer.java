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
package org.apache.olingo.odata2.client.api.ep;

import java.io.InputStream;
import java.util.List;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;

/**
 * Interface for all none basic (content type <b>dependent</b>) deserializer methods.
 * 
 * 
 */
public interface ContentTypeBasedDeserializer {

  /**
   * Returns an ODataFeed
   * @param entitySet
   * @param content
   * @return ODataFeed
   * @throws EntityProviderException
   */
  ODataFeed readFeed(EdmEntitySet entitySet, EntityStream content)
      throws EntityProviderException;

  /**
   * Returns an ODataEntry
   * @param entitySet
   * @param content
   * @return ODataEntry
   * @throws EntityProviderException
   */
  ODataEntry readEntry(EdmEntitySet entitySet, EntityStream content)
      throws EntityProviderException;

  /**
   * Read (de-serialize) data from error document as {@link InputStream} and provide according
   * {@link ODataErrorContext}.
   * 
   * @param errorDocument error document which is read
   * @param contentType format of content in the given input stream
   * @return read error document
   * @throws EntityProviderException if reading of data (de-serialization) fails
   */
  ODataErrorContext readErrorDocument(InputStream errorDocument) throws EntityProviderException;

  /**
   * Returns an ODataResponse
   * @param batchResponseParts
   * @return ODataResponse
   * @throws BatchException
   */
  ODataResponse writeBatchResponse(final List<BatchResponsePart> batchResponseParts) 
      throws BatchException;
  
  /**
   * Returns an object
   * @param functionImport
   * @param content
   * @return
   * @throws EntityProviderException
   */
  Object readFunctionImport(final EdmFunctionImport functionImport, 
      final EntityStream content) throws EntityProviderException;
  
}
