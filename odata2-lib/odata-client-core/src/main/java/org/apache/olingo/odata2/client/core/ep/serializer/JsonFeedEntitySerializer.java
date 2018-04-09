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
package org.apache.olingo.odata2.client.core.ep.serializer;

import java.io.IOException;
import java.io.Writer;

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.client.api.ep.Entity;
import org.apache.olingo.odata2.client.api.ep.EntityCollection;
import org.apache.olingo.odata2.client.api.ep.EntityCollectionSerializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntitySerializerProperties;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.util.FormatJson;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;

/**
 * Producer for writing an entity collection (a feed) in JSON.
 * 
 */
public class JsonFeedEntitySerializer {

  private final EntityCollectionSerializerProperties properties;

  /**
   * 
   * @param properties
   * @throws EntityProviderException
   */
  public JsonFeedEntitySerializer(final EntityCollectionSerializerProperties properties) {
    this.properties = properties == null ? EntityCollectionSerializerProperties.
        serviceRoot(null).build() : properties;
  }

  /**
   * This serializes the json payload feed
   * @param writer
   * @param entityInfo
   * @param data
   * @throws EntityProviderException
   */
  public void appendAsArray(final Writer writer, final EntityInfoAggregator entityInfo,
                            final EntityCollection data) throws EntityProviderException {
    
    JsonStreamWriter jsonStreamWriter = new JsonStreamWriter(writer);
    try {
      jsonStreamWriter.beginArray();
      appendEntries(writer, entityInfo, data, jsonStreamWriter);
      jsonStreamWriter.endArray();
    } catch (final IOException e) {
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  /**
   * This serializes the json payload feed
   * @param writer
   * @param entityInfo
   * @param data
   * @param isRootElement
   * @throws EntityProviderException
   */
  public void appendAsObject(final Writer writer, final EntityInfoAggregator entityInfo,
      final EntityCollection data) throws EntityProviderException {
    if (data == null) {
      throw new EntityProviderException(EntityProviderException.NULL_VALUE);
    }
    JsonStreamWriter jsonStreamWriter = new JsonStreamWriter(writer);

    try {
      jsonStreamWriter.beginObject();

      jsonStreamWriter.name(FormatJson.RESULTS)
          .beginArray();

      appendEntries(writer, entityInfo, data, jsonStreamWriter);

      jsonStreamWriter.endArray();

      jsonStreamWriter.endObject();
    } catch (final IOException e) {
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }
  
  private void appendEntries(final Writer writer, final EntityInfoAggregator entityInfo,
      final EntityCollection data, JsonStreamWriter jsonStreamWriter) throws EntityProviderException,
      IOException {
    boolean first = true;
    for (Entity entryData : data.getEntities()) {
      if (first) {
        first = false;
      } else {
        jsonStreamWriter.separator();
      }
      EntitySerializerProperties entryProperties = entryData == null ||
          entryData.getWriteProperties() == null ? data.getGlobalEntityProperties() != null?
              data.getGlobalEntityProperties(): EntitySerializerProperties.
              serviceRoot(properties.getServiceRoot()).
              build() : entryData.getWriteProperties();
      
      JsonEntryEntitySerializer entryProducer = new JsonEntryEntitySerializer(entryProperties);
      entryProducer.append(writer, entityInfo, entryData);
    }
  }
}
