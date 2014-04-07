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
import org.apache.olingo.odata2.api.xml.*;

import java.io.OutputStream;

/**
 *
 */
public class XmlStreamFactory implements XMLStreamWriterFactory, XMLStreamReaderFactory {

  public static XMLStreamReader createStreamReader(final Object content)
          throws EntityProviderException {
    XmlStreamFactory factory = new XmlStreamFactory();
    return factory.createXMLStreamReader(content);
  }

  public static XMLStreamWriter createStreamWriter(final Object content)
          throws EntityProviderException, XMLStreamException {
    XmlStreamFactory factory = new XmlStreamFactory();
    return factory.createXMLStreamWriter(content);
  }

  public static XMLStreamWriter createStreamWriter(OutputStream content, String charset)
          throws EntityProviderException {
    XmlStreamFactory factory = new XmlStreamFactory();
    return factory.createXMLStreamWriter(content);
  }

  public XMLStreamReaderFactory createReaderFactory() throws EntityProviderException {
    String factory = System.getProperty(XML_STREAM_READER_FACTORY_CLASS);
    if(factory != null) {
      try {
        Class factoryClass = Class.forName(factory);
        return (XMLStreamReaderFactory) factoryClass.newInstance();
      } catch (Exception e) {
        throw new EntityProviderException(
                EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getMessage()));
      }
    }
    return JavaxStaxReaderWrapper.createFactory();
  }

  public XMLStreamWriterFactory createWriterFactory() throws EntityProviderException {
    String factory = System.getProperty(XML_STREAM_WRITER_FACTORY_CLASS);
    if (factory != null) {
      try {
        Class factoryClass = Class.forName(factory);
        return (XMLStreamWriterFactory) factoryClass.newInstance();
      } catch (Exception e) {
        throw new EntityProviderException(
                EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getMessage()));
      }
    }
    return JavaxStaxWriterWrapper.createFactory();
  }

  @Override
  public XMLStreamReader createXMLStreamReader(Object content) throws EntityProviderException {
    XMLStreamReaderFactory factory = createReaderFactory();
    return factory.createXMLStreamReader(content);
  }

  @Override
  public XMLStreamWriter createXMLStreamWriter(Object content) throws EntityProviderException {
    XMLStreamWriterFactory factory = createWriterFactory();
    return factory.createXMLStreamWriter(content);
  }
}