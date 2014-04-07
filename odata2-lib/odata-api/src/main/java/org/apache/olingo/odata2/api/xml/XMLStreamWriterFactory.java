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

import org.apache.olingo.odata2.api.ep.EntityProviderException;

/**
 * Factory for XMLStreamWriterFactory instances.
 */
public interface XMLStreamWriterFactory{
  static final String XML_STREAM_WRITER_FACTORY_CLASS = "XML_STREAM_WRITER_FACTORY_CLASS";

  /**
   * Create XMLStreamWriterFactory for reading given content.
   *
   * @param content which will be written.
   * @return writer for given content.
   * @throws EntityProviderException if something goes wrong during initialization.
   */
  XMLStreamWriter createXMLStreamWriter(Object content) throws EntityProviderException;
}