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
package org.apache.olingo.odata2.client.core.ep;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataResponse.ODataResponseBuilder;
import org.apache.olingo.odata2.client.api.ep.ContentTypeBasedDeserializer;
import org.apache.olingo.odata2.client.api.ep.ContentTypeBasedSerializer;
import org.apache.olingo.odata2.client.api.ep.Entity;
import org.apache.olingo.odata2.client.api.ep.EntityCollection;
import org.apache.olingo.odata2.client.api.ep.EntityCollectionSerializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntitySerializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntityStream;
import org.apache.olingo.odata2.client.core.ep.deserializer.XmlEntityDeserializer;
import org.apache.olingo.odata2.client.core.ep.deserializer.XmlErrorDocumentDeserializer;
import org.apache.olingo.odata2.client.core.ep.serializer.AtomEntryEntitySerializer;
import org.apache.olingo.odata2.client.core.ep.serializer.AtomFeedSerializer;
import org.apache.olingo.odata2.core.batch.BatchRequestWriter;
import org.apache.olingo.odata2.core.batch.BatchResponseWriter;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.ContentType.ODataFormat;
import org.apache.olingo.odata2.core.commons.XmlHelper;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.util.CircleStreamBuffer;

/**
 *  This class includes methods to serialize deserialize XML Content type
 */
public class AtomSerializerDeserializer implements ContentTypeBasedSerializer, ContentTypeBasedDeserializer {

  /** Default used charset for writer and response content header */
  private static final String DEFAULT_CHARSET = ContentType.CHARSET_UTF_8;
  private static final String XML_VERSION = "1.0";

  /**
   * 
   * @throws EntityProviderException
   */
  public AtomSerializerDeserializer() throws EntityProviderException {
    this(ODataFormat.ATOM);
  }

  /**
   * 
   * @param odataFormat
   * @throws EntityProviderException
   */
  public AtomSerializerDeserializer(final ODataFormat odataFormat) throws EntityProviderException {
    if (odataFormat != ODataFormat.ATOM && odataFormat != ODataFormat.XML) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
          .addContent("Got unsupported ODataFormat '" + odataFormat + "'."));
    }
  }


  @Override
  public ODataResponse writeEntry( EdmEntitySet entitySet, Entity data
      ) throws EntityProviderException {

    CircleStreamBuffer csb = new CircleStreamBuffer();
    
    try {
      if(data == null){
        throw new EntityProviderProducerException(EntityProviderException.NULL_VALUE);
      }
      OutputStream outStream = csb.getOutputStream();
      XMLStreamWriter writer = XmlHelper.getXMLOutputFactory().createXMLStreamWriter(outStream, DEFAULT_CHARSET);
      writer.writeStartDocument(DEFAULT_CHARSET, XML_VERSION);
      EntitySerializerProperties properties = data.getWriteProperties();
      AtomEntryEntitySerializer as = new AtomEntryEntitySerializer(properties );
      EntityInfoAggregator eia = EntityInfoAggregator.create(entitySet, null);
      as.append(writer, eia, data, true, false);

      writer.flush();
      csb.closeWrite();

      ODataResponseBuilder response = ODataResponse.entity(csb.getInputStream());
      return response.build();
    } catch (EntityProviderException e) {
      csb.close();
      throw e;
    } catch (Exception e) {
      csb.close();
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  
  }

  @Override
  public ODataFeed readFeed( EdmEntitySet entitySet, EntityStream content)
      throws EntityProviderException {
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    return xec.readFeed(entitySet, content);
  }

  @Override
  public ODataEntry readEntry( EdmEntitySet entitySet, EntityStream content)
      throws EntityProviderException {

    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    return xec.readEntry(entitySet, content);
  
  }

  @Override
  public ODataErrorContext readErrorDocument(InputStream errorDocument) throws EntityProviderException {
    XmlErrorDocumentDeserializer xmlErrorDocumentConsumer = new XmlErrorDocumentDeserializer();
    return xmlErrorDocumentConsumer.readError(errorDocument);
  }

  @Override
  public ODataResponse writeFeed(EdmEntitySet entitySet, EntityCollection data) throws EntityProviderException {
    CircleStreamBuffer csb = new CircleStreamBuffer();

    try {
      if(data == null){
        throw new EntityProviderProducerException(EntityProviderException.NULL_VALUE);
      }
      
      OutputStream outStream = csb.getOutputStream();
      XMLStreamWriter writer = XmlHelper.getXMLOutputFactory().createXMLStreamWriter(outStream, DEFAULT_CHARSET);
      writer.writeStartDocument(DEFAULT_CHARSET, XML_VERSION);

      EntityCollectionSerializerProperties properties = data.getCollectionProperties();
      AtomFeedSerializer atomFeedProvider = new AtomFeedSerializer(properties);
      EntityInfoAggregator eia = EntityInfoAggregator.create(entitySet, null);
      atomFeedProvider.append(writer, eia, data, false);

      writer.flush();
      csb.closeWrite();

      return ODataResponse.entity(csb.getInputStream()).build();
    } catch (EntityProviderException e) {
      csb.close();
      throw e;
    } catch (XMLStreamException e) {
      csb.close();
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  @Override
  public InputStream readBatchRequest(List<BatchPart> batchParts, String boundary) {
    BatchRequestWriter batchWriter = new BatchRequestWriter();
    return batchWriter.writeBatchRequest(batchParts, boundary);
  }

  @Override
  public ODataResponse writeBatchResponse(List<BatchResponsePart> batchResponseParts) throws BatchException {
    BatchResponseWriter batchWriter = new BatchResponseWriter();
    return batchWriter.writeResponse(batchResponseParts);
  }

  @Override
  public Object readFunctionImport(EdmFunctionImport functionImport, EntityStream content)
      throws EntityProviderException {
    try {
      if (functionImport.getReturnType().getType().getKind() == EdmTypeKind.ENTITY) {
        return functionImport.getReturnType().getMultiplicity() == EdmMultiplicity.MANY
            ? new XmlEntityDeserializer().readFeed(functionImport.getEntitySet(), content)
            : new XmlEntityDeserializer().readEntry(functionImport.getEntitySet(), content);
      } else {
        final EntityPropertyInfo info = EntityInfoAggregator.create(functionImport);
        return functionImport.getReturnType().getMultiplicity() == EdmMultiplicity.MANY ?
          new XmlEntityDeserializer().readCollection(info, content) :
          new XmlEntityDeserializer().readProperty(info, content).get(info.getName());
      }
    } catch (final EdmException e) {
      throw new EntityProviderException(e.getMessageReference(), e);
    }
  }
}
