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

import org.apache.olingo.odata2.api.xml.XMLStreamReaderFactory;
import org.apache.olingo.odata2.api.xml.XMLStreamWriterFactory;

import java.util.HashMap;
import java.util.Map;

/**
 */
public abstract class AbstractXmlStreamFactory implements XMLStreamWriterFactory, XMLStreamReaderFactory {
  protected final Map<String, Object> readProperties = new HashMap<String, Object>();
  protected final Map<String, Object> writeProperties = new HashMap<String, Object>();

  @Override
  public XMLStreamReaderFactory setReadProperty(String name, Object value) {
    readProperties.put(name, value);
    return this;
  }

  @Override
  public XMLStreamWriterFactory setWriteProperty(String name, Object value) {
    writeProperties.put(name, value);
    return this;
  }

  protected void applyProperties(XMLStreamReaderFactory factory, Map<String, Object> readProperties) {
    for (Map.Entry<String, Object> name2Value : readProperties.entrySet()) {
      factory.setReadProperty(name2Value.getKey(), name2Value.getValue());
    }
  }

  protected void applyProperties(XMLStreamWriterFactory factory, Map<String, Object> readProperties) {
    for (Map.Entry<String, Object> name2Value : readProperties.entrySet()) {
      factory.setWriteProperty(name2Value.getKey(), name2Value.getValue());
    }
  }
}
