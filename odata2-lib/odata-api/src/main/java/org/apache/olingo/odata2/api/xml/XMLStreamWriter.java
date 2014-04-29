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
package org.apache.olingo.odata2.api.xml;

/**
 * Based on XMLStreamWriter from JDK.
 */
public interface XMLStreamWriter {

  void writeStartDocument() throws XMLStreamException;

  void setPrefix(String prefix, String uri) throws XMLStreamException;

  void setDefaultNamespace(String uri) throws XMLStreamException;

  void writeAttribute(String localName, String value) throws XMLStreamException;

  void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException;

  void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException;

  void writeDefaultNamespace(String namespaceURI) throws XMLStreamException;

  void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException;

  void writeStartElement(String localName) throws XMLStreamException;

  void writeStartElement(String namespaceURI, String localName) throws XMLStreamException;

  void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException;

  void writeStartDocument(String encoding, String version) throws XMLStreamException;

  void writeEndElement() throws XMLStreamException;

  void writeEndDocument() throws XMLStreamException;

  void writeCharacters(String text) throws XMLStreamException;

  void flush() throws XMLStreamException;
}
