/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") throws XMLStreamException; you may not use this file except in compliance
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
 * Based on XMLStreamReader from JDK.
 */
public interface XMLStreamReader {

  public abstract String getLocalName() throws XMLStreamException;

  public abstract String getNamespaceURI() throws XMLStreamException;

  public abstract String getNamespaceURI(int index) throws XMLStreamException;

  public abstract void require(int type, String namespaceURI, String localName) throws XMLStreamException;

  public abstract int nextTag() throws XMLStreamException;

  public abstract boolean hasNext() throws XMLStreamException;

  public abstract String getAttributeValue(String namespaceURI, String localName) throws XMLStreamException;

  public abstract String getAttributeValue(int index) throws XMLStreamException;

  public abstract String getElementText() throws XMLStreamException;

  public abstract  boolean isStartElement() throws XMLStreamException;

  public abstract void next() throws XMLStreamException;

  public abstract boolean isEndElement() throws XMLStreamException;

  public abstract int getNamespaceCount() throws XMLStreamException;

  public abstract String getNamespacePrefix(int index) throws XMLStreamException;

  public abstract NamespaceContext getNamespaceContext() throws XMLStreamException;

  public abstract QName getName() throws XMLStreamException;

  public abstract String getNamespaceURI(String prefix) throws XMLStreamException;

  public abstract boolean hasName() throws XMLStreamException;

  public abstract void close() throws XMLStreamException;

  public abstract String getText() throws XMLStreamException;

  public abstract boolean isCharacters() throws XMLStreamException;

  public abstract String getAttributeLocalName(int index) throws XMLStreamException;

  public abstract String getAttributeNamespace(int index) throws XMLStreamException;

  public abstract String getAttributePrefix(int index) throws XMLStreamException;

  public abstract int getAttributeCount() throws XMLStreamException;

  public abstract String getPrefix() throws XMLStreamException;
}
