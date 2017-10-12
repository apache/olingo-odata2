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
package org.apache.olingo.odata2.core.ep.producer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.util.FormatJson;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;
import org.apache.olingo.odata2.core.ep.util.JsonUtils;

public class JsonDeletedEntryEntityProducer {

  private EntityProviderWriteProperties properties;

  public JsonDeletedEntryEntityProducer(final EntityProviderWriteProperties properties) {
    this.properties = properties;
  }

  public void append(final Writer writer, final EntityInfoAggregator entityInfo,
      final List<Map<String, Object>> deletedEntries, boolean noPreviousEntries)
      throws EntityProviderException {
    JsonStreamWriter jsonStreamWriter = new JsonStreamWriter(writer);
    try {
      if (!deletedEntries.isEmpty()) {
        if(!noPreviousEntries){
          jsonStreamWriter.separator();
        }
        int counter = 0;
        for (Map<String, Object> deletedEntry : deletedEntries) {
          jsonStreamWriter.beginObject();

          String odataContextValue = JsonUtils.createODataContextValueForTombstone(entityInfo.getEntitySetName());
          String selfLink = AtomEntryEntityProducer.createSelfLink(entityInfo, deletedEntry, null);
          String idValue = properties.getServiceRoot().toASCIIString() + selfLink;

          jsonStreamWriter.namedStringValue(FormatJson.ODATA_CONTEXT, odataContextValue);
          jsonStreamWriter.separator();
          jsonStreamWriter.namedStringValue(FormatJson.ID, idValue);
          jsonStreamWriter.endObject();

          if (counter < deletedEntries.size() - 1) {
            jsonStreamWriter.separator();
          }

          counter++;
        }
      }
    } catch (final IOException e) {
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }

  }

}
