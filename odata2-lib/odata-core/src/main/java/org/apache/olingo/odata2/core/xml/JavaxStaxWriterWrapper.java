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
package org.apache.olingo.odata2.core.xml;

import org.apache.olingo.odata2.api.xml.XMLStreamException;
import org.apache.olingo.odata2.api.xml.XMLStreamWriter;

/**
 * Wrapper for a Java Stax XMLStreamWriter.
 */
public class JavaxStaxWriterWrapper implements XMLStreamWriter {
  private final javax.xml.stream.XMLStreamWriter xmlStreamWriter;

  public JavaxStaxWriterWrapper(javax.xml.stream.XMLStreamWriter xmlStreamWriter) {
    this.xmlStreamWriter = xmlStreamWriter;
  }

  public void writeStartDocument() throws XMLStreamException {
    try {
      xmlStreamWriter.writeStartDocument();
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  public void setPrefix(String prefix, String uri) throws XMLStreamException {
    try {
      xmlStreamWriter.setPrefix(prefix, uri);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }

  }

  public void setDefaultNamespace(String uri) throws XMLStreamException {
    try {
      xmlStreamWriter.setDefaultNamespace(uri);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  public void writeStartElement(String namespaceUri, String localName) throws XMLStreamException {
    try {
      xmlStreamWriter.writeStartElement(namespaceUri, localName);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  public void writeAttribute(String name, String value) throws XMLStreamException {
    try {
      xmlStreamWriter.writeAttribute(name, value);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  public void writeAttribute(String prefix, String namespace, String name, String value) throws XMLStreamException {
    try {
      xmlStreamWriter.writeAttribute(prefix, namespace, name, value);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  public void writeNamespace(String prefix, String namespace) throws XMLStreamException {
    try {
      xmlStreamWriter.writeNamespace(prefix, namespace);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }

  }

  public void writeStartElement(String name) throws XMLStreamException {
    try {
      xmlStreamWriter.writeStartElement(name);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }

  }

  public void writeDefaultNamespace(String namespace) throws XMLStreamException {
    try {
      xmlStreamWriter.writeDefaultNamespace(namespace);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }

  }

  public void writeEndElement() throws XMLStreamException {
    try {
      xmlStreamWriter.writeEndElement();
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  public void writeEndDocument() throws XMLStreamException {
    try {
      xmlStreamWriter.writeEndDocument();
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  public void flush() throws XMLStreamException {
    try {
      xmlStreamWriter.flush();
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  public void writeCharacters(String text) throws XMLStreamException {
    try {
      xmlStreamWriter.writeCharacters(text);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  public void writeStartElement(String prefix, String name, String namespace) throws XMLStreamException {
    try {
      xmlStreamWriter.writeStartElement(prefix, name, namespace);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  @Override
  public void writeStartDocument(String encoding, String xmlVersion) throws XMLStreamException {
    try {
      xmlStreamWriter.writeStartDocument(encoding, xmlVersion);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  @Override
  public void writeAttribute(String namespaceUri, String localName, String value) throws XMLStreamException {
    try {
      xmlStreamWriter.writeAttribute(namespaceUri, localName, value);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }

  }
}
