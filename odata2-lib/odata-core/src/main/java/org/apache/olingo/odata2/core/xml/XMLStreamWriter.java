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

/**
 * Based on XMLStreamWriter from JDK.
 */
public interface XMLStreamWriter {

  public abstract void writeStartDocument() throws XMLStreamException;

  public abstract void setPrefix(String prefixEdmx, String namespaceEdmx200706) throws XMLStreamException;

  public abstract void setDefaultNamespace(String defaultNamespace) throws XMLStreamException;

  public abstract void writeStartElement(String namespaceEdmx200706, String edmx) throws XMLStreamException;

  public abstract void writeAttribute(String name, String value) throws XMLStreamException;

  public abstract void writeAttribute(String prefix, String namespace, String name, String
          value) throws XMLStreamException;

  public abstract void writeNamespace(String prefix, String namespace) throws XMLStreamException;

  public abstract void writeStartElement(String name) throws XMLStreamException;

  public abstract void writeDefaultNamespace(String namespace) throws XMLStreamException;

  public abstract void writeEndElement() throws XMLStreamException;

  public abstract void writeEndDocument() throws XMLStreamException;

  public abstract void flush() throws XMLStreamException;

  public abstract void writeCharacters(String text) throws XMLStreamException;

  public abstract void writeStartElement(String prefix, String name, String namespace) throws XMLStreamException;
}
