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
package org.apache.olingo.odata2.core.ep.consumer;

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;

import java.io.InputStream;

/**
 * Consuming (read / deserialization) for OData error document in XML format.
 */
public class XmlErrorDocumentConsumer {


  /**
   * Deserialize / read OData error document in ODataErrorContext.
   *
   * @param errorDocument OData error document in XML format
   * @return created ODataErrorContext based on input stream content.
   * @throws EntityProviderException if an exception during read / deserialization occurs.
   */
  public ODataErrorContext readError(InputStream errorDocument) throws EntityProviderException {
    throw new RuntimeException("Not yet implementedØ");
  }
}
