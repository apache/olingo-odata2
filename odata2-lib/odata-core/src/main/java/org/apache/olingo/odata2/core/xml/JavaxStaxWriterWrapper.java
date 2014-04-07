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

import javax.xml.stream.XMLOutputFactory;
import java.io.OutputStream;
import java.io.Writer;

/**
 */
public class JavaxStaxWriterWrapper implements XMLStreamWriter, XMLStreamWriterFactory {
  private final javax.xml.stream.XMLStreamWriter xmlStreamWriter;

  public JavaxStaxWriterWrapper(javax.xml.stream.XMLStreamWriter xmlStreamWriter) {
    this.xmlStreamWriter = xmlStreamWriter;
  }

  public static XMLStreamWriterFactory createFactory() {
    return new JavaxStaxWriterWrapper(null);
  }

  @Override
  public XMLStreamWriter createXMLStreamWriter(Object content) throws XMLStreamException {
    if(content == null) {
      throw new IllegalArgumentException("Unsupported NULL input content.");
    }

    try {
      XMLOutputFactory xouf = XMLOutputFactory.newFactory();
      if (content instanceof OutputStream) {
        javax.xml.stream.XMLStreamWriter javaxWriter = xouf.createXMLStreamWriter((OutputStream) content);
        return new JavaxStaxWriterWrapper(javaxWriter);
      } else if (content instanceof Writer) {
        javax.xml.stream.XMLStreamWriter javaxWriter = xouf.createXMLStreamWriter((Writer) content);
        return new JavaxStaxWriterWrapper(javaxWriter);
      } else {
        throw new IllegalArgumentException("Unsupported input content with class type '" +
                content.getClass() + "'.");
      }
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

//  public static XMLStreamWriter create(Object content) throws XMLStreamException {
//    return new JavaxStaxWriterWrapper(null).createXMLStreamWriter(content);
//  }

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

  public void setDefaultNamespace(String defaultNamespace) throws XMLStreamException {
    try {
      xmlStreamWriter.setDefaultNamespace(defaultNamespace);
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
}
