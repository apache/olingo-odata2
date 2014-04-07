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
/**
 * <p>Xml Read and Write related parts</p>
 * <p>
 *   The XML package contains the interfaces which are used within the internal default implementations
 *   for consuming and producing OData XML data.
 *   To use an own XmlStreamReader or XmlStreamWriter it is necessary to implement the
 *   XmlStreamReaderFactory and/or XmlStreamWriterFactory and put the FQN of the class
 *   into the System Properties (see System.setProperties(...)) as
 *   <code>XML_STREAM_READER_FACTORY_CLASS</code> and/or <code>XML_STREAM_WRITER_FACTORY_CLASS</code>
 * </p>
 */
package org.apache.olingo.odata2.api.xml;