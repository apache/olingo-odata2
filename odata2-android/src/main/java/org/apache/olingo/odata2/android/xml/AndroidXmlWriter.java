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
package org.apache.olingo.odata2.android.xml;

import android.util.Xml;
import org.apache.olingo.odata2.api.xml.XMLStreamException;
import org.apache.olingo.odata2.api.xml.XMLStreamWriter;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Stack;

/**
 */
public class AndroidXmlWriter implements XMLStreamWriter {

  private static final String DEFAULT_ENCODING = "UTF-8";
  private static final String DEFAULT_VERSION = "1.0";

  private final XmlSerializer serializer;
  private final Stack<String> tags;
  private final Stack<String> namespaces;

  private String defaultNamespace = null;

  public AndroidXmlWriter(Object output) throws XMLStreamException {
    this(output, DEFAULT_ENCODING);
  }

  public AndroidXmlWriter(Object output, String encoding) throws XMLStreamException {
    serializer = Xml.newSerializer();
    tags = new Stack<String>();
    namespaces = new Stack<String>();

    try {
      if (output instanceof OutputStream) {
        serializer.setOutput((OutputStream) output, encoding);
      } else if (output instanceof Writer) {
        serializer.setOutput((Writer) output);
      } else {
        throw new XMLStreamException("Unsupported parameter value. " +
                "Requires an OutputStream or a Writer instance.");
      }
    } catch (IllegalStateException e) {
      wrapAndReThrow(e);
    } catch (IOException e) {
      wrapAndReThrow(e);
    }
  }

  private void wrapAndReThrow(Exception e) throws XMLStreamException {
    wrapAndReThrow("Unexpected exception with message: " + e.getMessage(), e);
  }

  private void wrapAndReThrow(String message, Exception e) throws XMLStreamException {
    throw new XMLStreamException(message, e);
  }

  private String nextTagToClose() {
    return tags.pop();
  }

  public AndroidXmlWriter setProperties(Map<String, Object> properties) throws XMLStreamException {
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      setProperty(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public AndroidXmlWriter setProperty(String name, Object value) throws XMLStreamException {
    try {
      serializer.setProperty(name, value);
    } catch (IllegalStateException e) {
      wrapAndReThrow(e);
    }
    return this;
  }


  @Override
  public void flush() throws XMLStreamException {
    try {
      if(serializer != null) {
        serializer.flush();
      }
    } catch (IOException e) {
      wrapAndReThrow("Exception during flush with message: " + e.getMessage(), e);
    }
  }

  @Override
  public void setDefaultNamespace(String uri) throws XMLStreamException {
    defaultNamespace = uri;
    writeNamespace("", defaultNamespace);
  }

  @Override
  public void setPrefix(String prefix, String uri) throws XMLStreamException {
    try {
      serializer.setPrefix(prefix, uri);
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  @Override
  public void writeStartDocument() throws XMLStreamException {
    try {
      writeStartDocument(DEFAULT_ENCODING, DEFAULT_VERSION);
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  @Override
  public void writeAttribute(String localName, String value) throws XMLStreamException {
    try {
      writeAttribute(nextNamespaceUriToWrite(), localName, value);
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  @Override
  public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
    try {
      writeAttribute(null, namespaceURI, localName, value);
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  @Override
  public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
          throws XMLStreamException {
    try {
      writeCurrentTag();
      serializer.attribute(namespaceURI, localName, value);
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  @Override
  public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
    setDefaultNamespace(namespaceURI);
  }

  @Override
  public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
    try {
      serializer.setPrefix(prefix, namespaceURI);
    } catch (IOException e) {
      wrapAndReThrow(e);
    }
  }

  @Override
  public void writeStartElement(String localName) throws XMLStreamException {
    try {
      writeStartElement(nextNamespaceUriToWrite(), localName);
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  private String nextNamespaceUriToWrite() {
    return nextNamespaceUriToWrite(true);
  }

  private String nextNamespaceUriToWrite(boolean keep) {
    if(namespaces.isEmpty()) {
      return defaultNamespace;
    }
    if(keep) {
      return namespaces.peek();
    }
    return namespaces.pop();
  }

  @Override
  public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
    try {
      String prefix = serializer.getPrefix(namespaceURI, false);
      writeStartElement(prefix, localName, namespaceURI);
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  TagHandle currentTag = null;

  @Override
  public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
    try {
      writeCurrentTag();
      //
      tags.push(localName);
      namespaces.push(namespaceURI);
      //
      currentTag = new TagHandle(localName, namespaceURI, prefix);
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  @Override
  public void writeStartDocument(String encoding, String version) throws XMLStreamException {
    try {
      serializer.startDocument(encoding, null);
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  @Override
  public void writeEndElement() throws XMLStreamException {
    try {
      writeCurrentTag();
      serializer.endTag(nextNamespaceUriToWrite(false), nextTagToClose());
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  @Override
  public void writeEndDocument() throws XMLStreamException {
    try {
      serializer.endDocument();
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  @Override
  public void writeCharacters(String text) throws XMLStreamException {
    try {
      writeCurrentTag();
      serializer.text(text);
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  private void writeCurrentTag() throws XMLStreamException {
    try {
      if(currentTag != null) {
        serializer.startTag(currentTag.namespaceUri, currentTag.name);
        currentTag = null;
      }
    } catch (Exception e) {
      wrapAndReThrow(e);
    }
  }

  private class TagHandle {
    final String name;
    final String namespaceUri;
    final String namespacePrefix;

    private TagHandle(String name, String namespaceUri, String namespacePrefix) {
      this.name = name;
      this.namespaceUri = namespaceUri;
      this.namespacePrefix = namespacePrefix;
    }
  }
}