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
package org.apache.olingo.odata2.client.core.ep.serializer;

import java.io.IOException;

import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.core.ep.util.FormatJson;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;

/**
 * Producer for writing a link in JSON.
 * 
 */
public class JsonLinkEntitySerializer {

  private final EntityProviderWriteProperties properties; //NOSONAR

  /**
   * 
   * @param properties
   * @throws EntityProviderException
   */
  public JsonLinkEntitySerializer(final EntityProviderWriteProperties properties) {
    this.properties = properties == null ? EntityProviderWriteProperties.serviceRoot(null).build() : properties;
  }

  protected static void appendUri(final JsonStreamWriter jsonStreamWriter, final String uri) throws IOException {
    jsonStreamWriter.beginObject()
        .namedStringValue(FormatJson.URI, uri)
        .endObject();
  }
}
