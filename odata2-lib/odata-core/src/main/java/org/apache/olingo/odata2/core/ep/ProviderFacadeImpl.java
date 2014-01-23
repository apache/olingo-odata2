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
import java.util.Map;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.ep.EntityProvider.EntityProviderInterface;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataNotAcceptableException;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.servicedocument.ServiceDocument;
import org.apache.olingo.odata2.core.batch.BatchRequestParser;
import org.apache.olingo.odata2.core.batch.BatchRequestWriter;
import org.apache.olingo.odata2.core.batch.BatchResponseParser;
import org.apache.olingo.odata2.core.batch.BatchResponseWriter;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.edm.provider.EdmImplProv;
import org.apache.olingo.odata2.core.edm.provider.EdmxProvider;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *  
 */
public class ProviderFacadeImpl implements EntityProviderInterface {

  private static BasicEntityProvider create() throws EntityProviderException {
    return new BasicEntityProvider();
  }

  private static ContentTypeBasedEntityProvider create(final String contentType) throws EntityProviderException {
    return create(ContentType.createAsCustom(contentType));
  }

  private static ContentTypeBasedEntityProvider create(final ContentType contentType) throws EntityProviderException {
    try {
      switch (contentType.getODataFormat()) {
      case ATOM:
      case XML:
        return new AtomEntityProvider(contentType.getODataFormat());
      case JSON:
        return new JsonEntityProvider();
      default:
        throw new ODataNotAcceptableException(ODataNotAcceptableException.NOT_SUPPORTED_CONTENT_TYPE
            .addContent(contentType));
      }
    } catch (final ODataNotAcceptableException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  @Override
  public ODataResponse writeErrorDocument(final ODataErrorContext context) {
    try {
      return create(context.getContentType()).writeErrorDocument(context.getHttpStatus(), context.getErrorCode(),
          context.getMessage(), context.getLocale(), context.getInnerError());
    } catch (EntityProviderException e) {
      throw new ODataRuntimeException(e);
    }
  }

  @Override
  public ODataResponse writeServiceDocument(final String contentType, final Edm edm, final String serviceRoot)
      throws EntityProviderException {
    return create(contentType).writeServiceDocument(edm, serviceRoot);
  }

  @Override
  public ODataResponse writePropertyValue(final EdmProperty edmProperty, final Object value)
      throws EntityProviderException {
    return create().writePropertyValue(edmProperty, value);
  }

  @Override
  public ODataResponse writeText(final String value) throws EntityProviderException {
    return create().writeText(value);
  }

  @Override
  public ODataResponse writeBinary(final String mimeType, final byte[] data) throws EntityProviderException {
    return create().writeBinary(mimeType, data);
  }

  @Override
  public ODataResponse writeFeed(final String contentType, final EdmEntitySet entitySet,
      final List<Map<String, Object>> data, final EntityProviderWriteProperties properties)
      throws EntityProviderException {
    return create(contentType).writeFeed(entitySet, data, properties);
  }

  @Override
  public ODataResponse writeEntry(final String contentType, final EdmEntitySet entitySet,
      final Map<String, Object> data, final EntityProviderWriteProperties properties) throws EntityProviderException {
    return create(contentType).writeEntry(entitySet, data, properties);
  }

  @Override
  public ODataResponse writeProperty(final String contentType, final EdmProperty edmProperty, final Object value)
      throws EntityProviderException {
    return create(contentType).writeProperty(edmProperty, value);
  }

  @Override
  public ODataResponse writeLink(final String contentType, final EdmEntitySet entitySet,
      final Map<String, Object> data, final EntityProviderWriteProperties properties) throws EntityProviderException {
    return create(contentType).writeLink(entitySet, data, properties);
  }

  @Override
  public ODataResponse writeLinks(final String contentType, final EdmEntitySet entitySet,
      final List<Map<String, Object>> data, final EntityProviderWriteProperties properties)
      throws EntityProviderException {
    return create(contentType).writeLinks(entitySet, data, properties);
  }

  @Override
  public ODataResponse writeFunctionImport(final String contentType, final EdmFunctionImport functionImport,
      final Object data, final EntityProviderWriteProperties properties) throws EntityProviderException {
    return create(contentType).writeFunctionImport(functionImport, data, properties);
  }

  @Override
  public ODataFeed readFeed(final String contentType, final EdmEntitySet entitySet, final InputStream content,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    return create(contentType).readFeed(entitySet, content, properties);
  }

  @Override
  public ODataDeltaFeed readDeltaFeed(final String contentType, final EdmEntitySet entitySet,
      final InputStream content,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    return create(contentType).readDeltaFeed(entitySet, content, properties);
  }

  @Override
  public ODataEntry readEntry(final String contentType, final EdmEntitySet entitySet, final InputStream content,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    return create(contentType).readEntry(entitySet, content, properties);
  }

  @Override
  public Map<String, Object> readProperty(final String contentType, final EdmProperty edmProperty,
      final InputStream content, final EntityProviderReadProperties properties) throws EntityProviderException {
    return create(contentType).readProperty(edmProperty, content, properties);
  }

  @Override
  public Object readPropertyValue(final EdmProperty edmProperty, final InputStream content, final Class<?> typeMapping)
      throws EntityProviderException {
    return create().readPropertyValue(edmProperty, content, typeMapping);
  }

  @Override
  public List<String> readLinks(final String contentType, final EdmEntitySet entitySet, final InputStream content)
      throws EntityProviderException {
    return create(contentType).readLinks(entitySet, content);
  }

  @Override
  public String readLink(final String contentType, final EdmEntitySet entitySet, final InputStream content)
      throws EntityProviderException {
    return create(contentType).readLink(entitySet, content);
  }

  @Override
  public byte[] readBinary(final InputStream content) throws EntityProviderException {
    return create().readBinary(content);
  }

  @Override
  public ODataResponse writeMetadata(final List<Schema> schemas, final Map<String, String> predefinedNamespaces)
      throws EntityProviderException {
    return create().writeMetadata(schemas, predefinedNamespaces);
  }

  @Override
  public Edm readMetadata(final InputStream inputStream, final boolean validate) throws EntityProviderException {
    EdmProvider provider = new EdmxProvider().parse(inputStream, validate);
    return new EdmImplProv(provider);
  }

  @Override
  public ServiceDocument readServiceDocument(final InputStream serviceDocument, final String contentType)
      throws EntityProviderException {
    return create(contentType).readServiceDocument(serviceDocument);
  }

  @Override
  public List<BatchRequestPart> parseBatchRequest(final String contentType, final InputStream content,
      final EntityProviderBatchProperties properties) throws BatchException {
    List<BatchRequestPart> batchParts = new BatchRequestParser(contentType, properties).parse(content);
    return batchParts;
  }

  @Override
  public ODataResponse writeBatchResponse(final List<BatchResponsePart> batchResponseParts) throws BatchException {
    BatchResponseWriter batchWriter = new BatchResponseWriter();
    return batchWriter.writeResponse(batchResponseParts);
  }

  @Override
  public InputStream writeBatchRequest(final List<BatchPart> batchParts, final String boundary) {
    BatchRequestWriter batchWriter = new BatchRequestWriter();
    return batchWriter.writeBatchRequest(batchParts, boundary);
  }

  @Override
  public List<BatchSingleResponse> parseBatchResponse(final String contentType, final InputStream content)
      throws BatchException {
    List<BatchSingleResponse> responses = new BatchResponseParser(contentType).parse(content);
    return responses;
  }

}
