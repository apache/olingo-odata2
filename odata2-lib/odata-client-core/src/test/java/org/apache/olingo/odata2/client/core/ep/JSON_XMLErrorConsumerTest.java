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
package org.apache.olingo.odata2.client.core.ep;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.client.api.ODataClient;
import org.apache.olingo.odata2.client.core.ep.deserializer.AbstractDeserializerTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.Test;

/**
 *  
 */
public class JSON_XMLErrorConsumerTest extends AbstractDeserializerTest {

  private static final String JSON = "application/json";
  private static final String XML = "application/xml";
  
  @Test
  public void readErrorDocumentJson() throws EntityProviderException {
    ODataClient providerFacade = ODataClient.newInstance();
    String errorDoc = "{\"error\":{\"code\":\"ErrorCode\",\"message\":{\"lang\":\"en-US\",\"value\":\"Message\"}}}";
    ODataErrorContext errorContext = providerFacade.createDeserializer(JSON).
        readErrorDocument(StringHelper.encapsulate(errorDoc));
    //
    assertEquals("Wrong content type", "application/json", errorContext.getContentType());
    assertEquals("Wrong message", "Message", errorContext.getMessage());
    assertEquals("Wrong error code", "ErrorCode", errorContext.getErrorCode());
    assertEquals("Wrong locale for lang", Locale.US, errorContext.getLocale());
  }

  @Test
  public void readErrorDocumentXml() throws EntityProviderException {
    ODataClient providerFacade = ODataClient.newInstance();
    String errorDoc =
        "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n" +
            "\t<code>ErrorCode</code>\n" +
            "\t<message xml:lang=\"en-US\">Message</message>\n" +
            "</error>";
    ODataErrorContext errorContext = providerFacade.createDeserializer(XML).
        readErrorDocument(StringHelper.encapsulate(errorDoc));
    //
    assertEquals("Wrong content type", "application/xml", errorContext.getContentType());
    assertEquals("Wrong message", "Message", errorContext.getMessage());
    assertEquals("Wrong error code", "ErrorCode", errorContext.getErrorCode());
    assertEquals("Wrong locale for lang", Locale.US, errorContext.getLocale());
  }
}
