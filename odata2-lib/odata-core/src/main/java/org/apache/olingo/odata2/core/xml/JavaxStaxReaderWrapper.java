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

import javax.xml.stream.XMLInputFactory;
import java.io.InputStream;

/**
 */
public class JavaxStaxReaderWrapper extends XMLStreamReader {

  /** Default used charset for reader */
  private static final String DEFAULT_CHARSET = "UTF-8";

  public static XMLStreamReader createStreamReader(final Object content) throws EntityProviderException {
    if (content == null) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
              .addContent("Got not allowed NULL parameter for creation of XMLStreamReader."));
    }
    javax.xml.stream.XMLStreamReader streamReader;
    try {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
      factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
      factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

      if (content instanceof InputStream) {
        streamReader = factory.createXMLStreamReader((InputStream) content, DEFAULT_CHARSET);
        // verify charset encoding set in content is supported (if not set UTF-8 is used as defined in
        // v'http://www.w3.org/TR/2008/REC-xml-20081126/')
        String characterEncodingInContent = streamReader.getCharacterEncodingScheme();
        if (characterEncodingInContent != null && !DEFAULT_CHARSET.equalsIgnoreCase(characterEncodingInContent)) {
          throw new EntityProviderException(EntityProviderException
                  .UNSUPPORTED_CHARACTER_ENCODING.addContent(characterEncodingInContent));
        }
      } else {
        throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
                .addContent("Found not supported content of class '" + content.getClass() + "' to de-serialize."));
      }
      return new JavaxStaxReaderWrapper(streamReader);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
              .getSimpleName()), e);
    }
  }

  private final javax.xml.stream.XMLStreamReader reader;

  public JavaxStaxReaderWrapper(javax.xml.stream.XMLStreamReader reader) {
    this.reader = reader;
  }

  public String getLocalName() {
    return reader.getLocalName();
  }

  public String getNamespaceURI() {
    return reader.getNamespaceURI();
  }

  public String getNamespaceURI(int pos) {
    return reader.getNamespaceURI(pos);
  }

  public void require(int startDocument, String namespace, String tag) throws XMLStreamException {
    try {
      reader.require(startDocument, namespace, tag);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw  new XMLStreamException(e);
    }
  }

  public int nextTag() throws XMLStreamException {
    try {
      return reader.nextTag();
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  public boolean hasNext() throws XMLStreamException {
    try {
      return reader.hasNext();
    } catch (javax.xml.stream.XMLStreamException e) {
      throw  new XMLStreamException(e);
    }
  }

  public String getAttributeValue(String o, String atomRel) {
    return reader.getAttributeValue(o, atomRel);
  }

  @Override
  public String getAttributeValue(int pos) {
    return reader.getAttributeValue(pos);
  }

  public String getElementText() throws XMLStreamException {
    try {
      return reader.getElementText();
    } catch (javax.xml.stream.XMLStreamException e) {
      throw  new XMLStreamException(e);
    }
  }

  public boolean isStartElement() {
    return reader.isStartElement();
  }

  public void next() throws XMLStreamException {
    try {
      reader.next();
    } catch (javax.xml.stream.XMLStreamException e) {
      throw  new XMLStreamException(e);
    }
  }

  public boolean isEndElement() {
    return reader.isEndElement();
  }

  public int getNamespaceCount() {
    return reader.getNamespaceCount();
  }

  public String getNamespacePrefix(int i) {
    return reader.getNamespacePrefix(i);
  }

  public NamespaceContext getNamespaceContext() {
    return new JavaxNamespaceContext(reader.getNamespaceContext());
  }

  public QName getName() {
    return new JavaxQName(reader.getName());
  }

  public String getNamespaceURI(String customPrefix) {
    return reader.getNamespaceURI(customPrefix);
  }

  public boolean hasName() {
    return reader.hasName();
  }



  @Override
  public void close() throws XMLStreamException {
    try {
      reader.close();
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new XMLStreamException(e);
    }
  }

  @Override
  public String getText() {
    return reader.getText();
  }

  @Override
  public boolean isCharacters() {
    return reader.isCharacters();
  }

  @Override
  public String getAttributeLocalName(int i) {
    return reader.getAttributeLocalName(i);
  }

  @Override
  public String getAttributeNamespace(int i) {
    return reader.getAttributeNamespace(i);
  }

  @Override
  public String getAttributePrefix(int i) {
    return reader.getAttributePrefix(i);
  }

  @Override
  public int getAttributeCount() {
    return reader.getAttributeCount();
  }

  @Override
  public String getPrefix() {
    return reader.getPrefix();
  }


  public static class JavaxNamespaceContext extends NamespaceContext {
    javax.xml.namespace.NamespaceContext namespaceContext;

    public JavaxNamespaceContext(javax.xml.namespace.NamespaceContext namespaceContext) {
      this.namespaceContext = namespaceContext;
    }

    @Override
    public String getPrefix(String customNamespaceURI) {
      return this.namespaceContext.getPrefix(customNamespaceURI);
    }
  }

  public static class JavaxQName extends QName {
    public JavaxQName(javax.xml.namespace.QName qname) {
      this(qname.getNamespaceURI());
    }

    protected JavaxQName(String namespaceURI) {
      super(namespaceURI);
    }
  }
}
