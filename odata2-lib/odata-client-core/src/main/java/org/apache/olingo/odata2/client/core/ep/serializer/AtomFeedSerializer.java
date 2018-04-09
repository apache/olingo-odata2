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

import java.net.URI;
import java.util.Date;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.client.api.ep.Entity;
import org.apache.olingo.odata2.client.api.ep.EntityCollection;
import org.apache.olingo.odata2.client.api.ep.EntityCollectionSerializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntitySerializerProperties;
import org.apache.olingo.odata2.core.commons.Encoder;
import org.apache.olingo.odata2.core.edm.EdmDateTimeOffset;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

/**
 * Serializes an ATOM feed.
 * 
 */
public class AtomFeedSerializer {

  private final EntityCollectionSerializerProperties properties;

  /**
   * 
   * @param properties
   */
  public AtomFeedSerializer(final EntityCollectionSerializerProperties properties) {
    this.properties = properties == null ? 
        EntityCollectionSerializerProperties.serviceRoot(null).build() : 
          properties;
  }
 
  /**
   * This serializes the xml payload feed
   * @param writer
   * @param eia
   * @param data
   * @param isInline
   * @throws EntityProviderException
   */
  public void append(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final EntityCollection data, final boolean isInline) throws EntityProviderException {
    try {
      if (properties.getServiceRoot() == null) {
        throw new EntityProviderProducerException(EntityProviderException.MANDATORY_WRITE_PROPERTY);
      }
      
      writer.writeStartElement(FormatXml.ATOM_FEED);
      if (!isInline) {
        writer.writeDefaultNamespace(Edm.NAMESPACE_ATOM_2005);
        writer.writeNamespace(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);
        writer.writeNamespace(Edm.PREFIX_D, Edm.NAMESPACE_D_2007_08);
        
      }
      writer.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, FormatXml.XML_BASE, properties.getServiceRoot()
          .toASCIIString());

      // write all atom infos (mandatory and optional)
      appendAtomMandatoryParts(writer, eia);
      appendAtomSelfLink(writer, eia);
    

      appendEntries(writer, eia, data);

     

   
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  private void appendEntries(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final EntityCollection data) throws EntityProviderException {
    AtomEntryEntitySerializer entryProvider;
    for (Entity singleEntryData : data.getEntities()) {
      entryProvider = singleEntryData.getWriteProperties() == null? data.getGlobalEntityProperties() == null ?
          new AtomEntryEntitySerializer
          (EntitySerializerProperties.serviceRoot(data.getCollectionProperties().getServiceRoot()).build()) :
            new AtomEntryEntitySerializer(data.getGlobalEntityProperties()):
            new AtomEntryEntitySerializer(singleEntryData.getWriteProperties());
       entryProvider.append(writer, eia, singleEntryData, false, true);
    }
  }


  private void appendAtomSelfLink(final XMLStreamWriter writer, final EntityInfoAggregator eia)
      throws EntityProviderException {

    URI self = properties.getSelfLink();
    String selfLink = "";
    if (self == null) {
      selfLink = createSelfLink(eia);
    } else {
      selfLink = self.toASCIIString();
    }
    try {
      writer.writeStartElement(FormatXml.ATOM_LINK);
      writer.writeAttribute(FormatXml.ATOM_HREF, selfLink);
      writer.writeAttribute(FormatXml.ATOM_REL, Edm.LINK_REL_SELF);
      writer.writeAttribute(FormatXml.ATOM_TITLE, eia.getEntitySetName());
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  private String createSelfLink(final EntityInfoAggregator eia) {
    StringBuilder sb = new StringBuilder();
    if (!eia.isDefaultEntityContainer()) {
      final String entityContainerName = Encoder.encode(eia.getEntityContainerName());
      sb.append(entityContainerName).append(Edm.DELIMITER);
    }
    final String entitySetName = Encoder.encode(eia.getEntitySetName());
    sb.append(entitySetName);
    return sb.toString();
  }

  private void appendAtomMandatoryParts(final XMLStreamWriter writer, final EntityInfoAggregator eia)
      throws EntityProviderException {
    try {
      writer.writeStartElement(FormatXml.ATOM_ID);
      writer.writeCharacters(createAtomId(eia));
      writer.writeEndElement();

      writer.writeStartElement(FormatXml.ATOM_TITLE);
      writer.writeAttribute(FormatXml.M_TYPE, FormatXml.ATOM_TEXT);
      writer.writeCharacters(eia.getEntitySetName());
      writer.writeEndElement();

      writer.writeStartElement(FormatXml.ATOM_UPDATED);

      Object updateDate = null;
      EdmFacets updateFacets = null;
      updateDate = new Date();
      writer.writeCharacters(EdmDateTimeOffset.getInstance().valueToString(updateDate, EdmLiteralKind.DEFAULT,
          updateFacets));
      writer.writeEndElement();

      writer.writeStartElement(FormatXml.ATOM_AUTHOR);
      writer.writeStartElement(FormatXml.ATOM_AUTHOR_NAME);
      writer.writeEndElement();
      writer.writeEndElement();

    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    } catch (EdmSimpleTypeException e) {
      throw new EntityProviderProducerException(e.getMessageReference(), e);
    }
  }

  private String createAtomId(final EntityInfoAggregator eia) throws EntityProviderException {
    return properties.getServiceRoot() + createSelfLink(eia);
  }
}
