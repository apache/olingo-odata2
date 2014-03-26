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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Locale;

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.Test;

/**
 *  
 */
public class XmlErrorDocumentConsumerTest extends AbstractConsumerTest {

  private static final String XML_ERROR_DOCUMENT_SIMPLE =
      "<?xml version='1.0' encoding='UTF-8'?>\n" +
          "<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n" +
          "\t<code>ErrorCode</code>\n" +
          "\t<message xml:lang=\"en-US\">Message</message>\n" +
          "</error>";
  private static final String XML_ERROR_DOCUMENT_NULL_LOCALE =
      "<?xml version='1.0' encoding='UTF-8'?>\n" +
          "<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n" +
          "\t<code>ErrorCode</code>\n" +
          "\t<message xml:lang=\"\">Message</message>\n" +
          "</error>";
  private static final String XML_ERROR_DOCUMENT_INNER_ERROR =
      "<?xml version='1.0' encoding='UTF-8'?>\n" +
          "<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n" +
          "\t<code>ErrorCode</code>\n" +
          "\t<message xml:lang=\"en-US\">Message</message>\n" +
          "<innererror>Some InnerError</innererror>\n" +
          "</error>";
  private static final String XML_ERROR_DOCUMENT_INVALID_XML =
      "<?xml version='1.0' encoding='UTF-8'?>\n" +
          "<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n" +
          "\t<code>ErrorCode</CODE>\n" +
          "\t<message xml:lang=\"en-US\">Message</message>\n" +
          "</error>";
  /* error document with name 'locale' instead of 'lang' for message object */
  private static final String XML_ERROR_DOCUMENT_UNKNOWN_CONTENT =
      "<?xml version='1.0' encoding='UTF-8'?>\n" +
          "<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n" +
          "\t<code>ErrorCode</code>\n" +
          "\t<message xml:locale=\"en-US\">Message</message>\n" +
          "\t<privateMessage>Secret</privateMessage>\n" +
          "</error>";
  /* error document without value for message object */
  private static final String XML_ERROR_DOCUMENT_EMPTY_MESSAGE =
      "<?xml version='1.0' encoding='UTF-8'?>\n" +
          "<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n" +
          "\t<code>ErrorCode</code>\n" +
          "\t<message xml:lang=\"en-US\" />\n" +
          "</error>";
  private static final String XML_ERROR_DOCUMENT_MISSING_MESSAGE =
      "<?xml version='1.0' encoding='UTF-8'?>\n" +
          "<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n" +
          "\t<code>ErrorCode</code>\n" +
          "</error>";
  private static final String XML_ERROR_DOCUMENT_MISSING_CODE =
      "<?xml version='1.0' encoding='UTF-8'?>\n" +
          "<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n" +
          "\t<message xml:lang=\"en-US\">Message</message>\n" +
          "</error>";
  private static final String XML_ERROR_DOCUMENT_MISSING_ERROR =
      "<?xml version='1.0' encoding='UTF-8'?>\n" +
          "<errorForMe xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n" +
          "\t<code>ErrorCode</code>\n" +
          "\t<message xml:lang=\"en-US\">Message</message>\n" +
          "</errorForMe>";
  private XmlErrorDocumentConsumer xedc = new XmlErrorDocumentConsumer();

  @Test
  public void simpleErrorDocument() throws Exception {
    InputStream in = StringHelper.encapsulate(XML_ERROR_DOCUMENT_SIMPLE);
    ODataErrorContext error = xedc.readError(in);

    assertEquals("Wrong content type", "application/xml", error.getContentType());
    assertEquals("Wrong message", "Message", error.getMessage());
    assertEquals("Wrong error code", "ErrorCode", error.getErrorCode());
    assertEquals("Wrong locale for lang", Locale.US, error.getLocale());
  }

  @Test
  public void emptyMessage() throws EntityProviderException {
    InputStream in = StringHelper.encapsulate(XML_ERROR_DOCUMENT_EMPTY_MESSAGE);

    ODataErrorContext error = xedc.readError(in);

    assertEquals("Wrong content type", "application/xml", error.getContentType());
    assertEquals("Wrong message", "", error.getMessage());
    assertEquals("Wrong error code", "ErrorCode", error.getErrorCode());
    assertEquals("Wrong locale for lang", Locale.US, error.getLocale());
  }

  @Test
  public void localeNull() throws Exception {
    InputStream in = StringHelper.encapsulate(XML_ERROR_DOCUMENT_NULL_LOCALE);
    ODataErrorContext error = xedc.readError(in);

    assertEquals("Wrong content type", "application/xml", error.getContentType());
    assertEquals("Wrong message", "Message", error.getMessage());
    assertEquals("Wrong error code", "ErrorCode", error.getErrorCode());
    assertNull("Expected NULL for locale", error.getLocale());
  }

  @Test
  public void innerError() throws Exception {
    InputStream in = StringHelper.encapsulate(XML_ERROR_DOCUMENT_INNER_ERROR);
    ODataErrorContext error = xedc.readError(in);

    assertEquals("Wrong content type", "application/xml", error.getContentType());
    assertEquals("Wrong message", "Message", error.getMessage());
    assertEquals("Wrong error code", "ErrorCode", error.getErrorCode());
    assertEquals("Wrong inner error", "Some InnerError", error.getInnerError());
  }

  @Test(expected = EntityProviderException.class)
  public void invalidJson() throws EntityProviderException {
    InputStream in = StringHelper.encapsulate(XML_ERROR_DOCUMENT_INVALID_XML);
    try {
      xedc.readError(in);
      fail("Expected exception was not thrown");
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.INVALID_STATE, e.getMessageReference());
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void invalidEmptyDocument() throws EntityProviderException {
    InputStream in = StringHelper.encapsulate("");
    try {
      xedc.readError(in);
      fail("Expected exception was not thrown");
    } catch (EntityProviderException e) {
      assertEquals("Got wrong exception: " + e.getMessageReference().getKey(),
          EntityProviderException.INVALID_STATE, e.getMessageReference());
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void nullParameter() throws EntityProviderException {
    try {
      xedc.readError(null);
      fail("Expected exception was not thrown");
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.ILLEGAL_ARGUMENT, e.getMessageReference());
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void invalidErrorDocumentUnknown() throws EntityProviderException {
    InputStream in = StringHelper.encapsulate(XML_ERROR_DOCUMENT_UNKNOWN_CONTENT);
    try {
      xedc.readError(in);
      fail("Expected exception was not thrown");
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.INVALID_CONTENT, e.getMessageReference());
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void invalidErrorDocumentMissingError() throws EntityProviderException {
    InputStream in = StringHelper.encapsulate(XML_ERROR_DOCUMENT_MISSING_ERROR);
    try {
      xedc.readError(in);
      fail("Expected exception was not thrown");
    } catch (EntityProviderException e) {
      assertEquals("Got wrong exception: " + e.getMessageReference().getKey(),
          EntityProviderException.INVALID_STATE, e.getMessageReference());
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void invalidErrorDocumentMissingCode() throws EntityProviderException {
    InputStream in = StringHelper.encapsulate(XML_ERROR_DOCUMENT_MISSING_CODE);
    try {
      xedc.readError(in);
      fail("Expected exception was not thrown");
    } catch (EntityProviderException e) {
      assertEquals("Got wrong exception: " + e.getMessageReference().getKey(),
          EntityProviderException.MISSING_PROPERTY, e.getMessageReference());
      assertTrue(e.getMessage().contains("code"));
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void invalidErrorDocumentMissingMessage() throws EntityProviderException {
    InputStream in = StringHelper.encapsulate(XML_ERROR_DOCUMENT_MISSING_MESSAGE);
    try {
      xedc.readError(in);
      fail("Expected exception was not thrown");
    } catch (EntityProviderException e) {
      assertEquals("Got wrong exception: " + e.getMessageReference().getKey(),
          EntityProviderException.MISSING_PROPERTY, e.getMessageReference());
      assertTrue(e.getMessage().contains("message"));
      throw e;
    }
  }
}
