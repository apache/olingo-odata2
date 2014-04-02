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
 * Created by d046871 on 02.04.14.
 */
public abstract class XMLStreamWriterFactory {
  public static final String XML_STREAM_WRITER_FACTORY_CLASS = "XML_STREAM_WRITER_FACTORY_CLASS";

  public abstract XMLStreamWriter createXMLStreamWriter(Object content) throws XMLStreamException;

  public static XMLStreamWriterFactory create() throws EntityProviderException {
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
    return new JavaxStaxWriterWrapper();
  }
}
