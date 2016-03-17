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

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmTargetPath;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallback;
import org.apache.olingo.odata2.core.edm.EdmDateTimeOffset;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

/**
 *  
 */
public class TombstoneProducer {

  private String defaultDateString;

  /**
   * Appends tombstones to an already started feed.
   * If the list is empty no elements will be appended.
   * @param writer same as in feed
   * @param eia same as in feed
   * @param properties same as in feed
   * @param deletedEntries data to be appended
   * @throws EntityProviderException
   */
  public void appendTombstones(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final EntityProviderWriteProperties properties, final List<Map<String, Object>> deletedEntries)
      throws EntityProviderException {
    try {
      for (Map<String, Object> deletedEntry : deletedEntries) {
        writer.writeStartElement(TombstoneCallback.NAMESPACE_TOMBSTONE, FormatXml.ATOM_TOMBSTONE_DELETED_ENTRY);

        appendRefAttribute(writer, eia, properties, deletedEntry);
        appendWhenAttribute(writer, eia, deletedEntry);

        writer.writeEndElement();
      }
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    } catch (EdmSimpleTypeException e) {
      throw new EntityProviderProducerException(e.getMessageReference(), e);
    }
  }

  private void appendWhenAttribute(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final Map<String, Object> deletedEntry) throws XMLStreamException, EdmSimpleTypeException {
    Object updateDate = null;
    EntityPropertyInfo updatedInfo = eia.getTargetPathInfo(EdmTargetPath.SYNDICATION_UPDATED);
    if (updatedInfo != null) {
      updateDate = deletedEntry.get(updatedInfo.getName());
    }

    if (updateDate == null) {
      appendDefaultWhenAttribute(writer);
    } else {
      appendCustomWhenAttribute(writer, updateDate, updatedInfo);
    }
  }

  private void appendCustomWhenAttribute(final XMLStreamWriter writer, final Object updateDate,
      final EntityPropertyInfo updatedInfo) throws XMLStreamException, EdmSimpleTypeException {
    EdmFacets updateFacets = updatedInfo.getFacets();
    writer.writeAttribute(FormatXml.ATOM_TOMBSTONE_WHEN, EdmDateTimeOffset.getInstance().valueToString(updateDate,
        EdmLiteralKind.DEFAULT, updateFacets));
  }

  private void appendRefAttribute(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final EntityProviderWriteProperties properties, final Map<String, Object> deletedEntry)
      throws XMLStreamException, EntityProviderException {
    String ref =
        properties.getServiceRoot().toASCIIString() + AtomEntryEntityProducer.createSelfLink(eia, deletedEntry, null);
    writer.writeAttribute(FormatXml.ATOM_TOMBSTONE_REF, ref);
  }

  private void appendDefaultWhenAttribute(final XMLStreamWriter writer) throws XMLStreamException,
      EdmSimpleTypeException {
    if (defaultDateString == null) {
      Object defaultDate = new Date();
      defaultDateString = EdmDateTimeOffset.getInstance().valueToString(defaultDate, EdmLiteralKind.DEFAULT, null);
    }
    writer.writeAttribute(FormatXml.ATOM_TOMBSTONE_WHEN, defaultDateString);

  }
}
