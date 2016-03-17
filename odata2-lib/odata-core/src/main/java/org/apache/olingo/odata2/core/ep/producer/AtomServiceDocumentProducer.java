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

import java.io.Writer;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySetInfo;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmServiceMetadata;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

/**
 * Writes the OData service document in XML.
 * 
 */
public class AtomServiceDocumentProducer {

  private static final String DEFAULT_CHARSET = ContentType.CHARSET_UTF_8;
  private static final String XML_VERSION = "1.0";
  private final Edm edm;
  private final String serviceRoot;

  public AtomServiceDocumentProducer(final Edm edm, final String serviceRoot) {
    this.edm = edm;
    this.serviceRoot = serviceRoot;
  }

  public void writeServiceDocument(final Writer writer) throws EntityProviderException {

    EdmServiceMetadata serviceMetadata = edm.getServiceMetadata();

    try {
      XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);

      xmlStreamWriter.writeStartDocument(DEFAULT_CHARSET, XML_VERSION);
      xmlStreamWriter.setPrefix(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998);
      xmlStreamWriter.setPrefix(Edm.PREFIX_ATOM, Edm.NAMESPACE_ATOM_2005);
      xmlStreamWriter.setDefaultNamespace(Edm.NAMESPACE_APP_2007);

      xmlStreamWriter.writeStartElement(FormatXml.APP_SERVICE);
      xmlStreamWriter.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, FormatXml.XML_BASE, serviceRoot);
      xmlStreamWriter.writeDefaultNamespace(Edm.NAMESPACE_APP_2007);
      xmlStreamWriter.writeNamespace(Edm.PREFIX_ATOM, Edm.NAMESPACE_ATOM_2005);

      xmlStreamWriter.writeStartElement(FormatXml.APP_WORKSPACE);
      xmlStreamWriter.writeStartElement(Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_TITLE);
      xmlStreamWriter.writeCharacters(FormatXml.ATOM_TITLE_DEFAULT);
      xmlStreamWriter.writeEndElement();

      List<EdmEntitySetInfo> entitySetInfos = serviceMetadata.getEntitySetInfos();
      for (EdmEntitySetInfo info : entitySetInfos) {
        xmlStreamWriter.writeStartElement(FormatXml.APP_COLLECTION);
        xmlStreamWriter.writeAttribute(FormatXml.ATOM_HREF, info.getEntitySetUri().toASCIIString());
        xmlStreamWriter.writeStartElement(Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_TITLE);
        xmlStreamWriter.writeCharacters(info.getEntitySetName());
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndElement();
      }

      xmlStreamWriter.writeEndElement();
      xmlStreamWriter.writeEndElement();
      xmlStreamWriter.writeEndDocument();

      xmlStreamWriter.flush();
    } catch (FactoryConfigurationError e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    } catch (EdmException e) {
      throw new EntityProviderProducerException(e.getMessageReference(), e);
    } catch (ODataException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }
}
