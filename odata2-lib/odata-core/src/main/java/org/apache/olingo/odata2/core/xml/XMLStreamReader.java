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


import org.apache.olingo.odata2.api.ep.EntityProviderException;

/**
 */
public abstract class XMLStreamReader {

  public static final String XML_STREAM_READER_FACTORY_CLASS = "XMLStreamReaderFactoryClass";

  public static XMLStreamReader createXMLStreamReader(Object content) throws EntityProviderException {
    String factory = System.getProperty(XML_STREAM_READER_FACTORY_CLASS);
    if(factory != null) {
      try {
//        System.out.println("Load stream reader factory class: " + factory);
        Class factoryClass = Class.forName(factory);
        XMLStreamReaderFactory factoryInstance = (XMLStreamReaderFactory) factoryClass.newInstance();
        return factoryInstance.createXMLStreamReader(content);
      } catch (Exception e) {
        throw new EntityProviderException(
                EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getMessage()));
      }
    }
    return JavaxStaxReaderWrapper.createStreamReader(content);
  }

  public interface XMLStreamReaderFactory {
    XMLStreamReader createXMLStreamReader(Object content);
  }

  public abstract String getLocalName();

  public abstract String getNamespaceURI();

  public abstract String getNamespaceURI(int pos);

  public abstract void require(int startDocument, String namespace, String tag) throws XMLStreamException;

  public abstract int nextTag() throws XMLStreamException;

  public abstract boolean hasNext() throws XMLStreamException;

  public abstract String getAttributeValue(String o, String atomRel);

  public abstract String getAttributeValue(int pos);

  public abstract String getElementText() throws XMLStreamException;

  public abstract  boolean isStartElement();

  public abstract void next() throws XMLStreamException;

  public abstract boolean isEndElement();

  public abstract int getNamespaceCount();

  public abstract String getNamespacePrefix(int i);

  public abstract NamespaceContext getNamespaceContext();

  public abstract QName getName();

  public abstract String getNamespaceURI(String customPrefix);

  public abstract boolean hasName();

  public abstract void close() throws XMLStreamException;

  public abstract String getText();

  public abstract boolean isCharacters();

  public abstract String getAttributeLocalName(int i);

  public abstract String getAttributeNamespace(int i);

  public abstract String getAttributePrefix(int i);

  public abstract int getAttributeCount();

  public abstract String getPrefix();
}
