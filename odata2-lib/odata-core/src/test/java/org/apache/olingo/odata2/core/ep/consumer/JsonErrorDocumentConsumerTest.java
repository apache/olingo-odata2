/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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
import org.apache.olingo.odata2.testutil.helper.LocaleAsserter;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.Test;

/**
 *
 */
public class JsonErrorDocumentConsumerTest extends AbstractConsumerTest {

    private static final String JSON_ERROR_DOCUMENT_SIMPLE =
            "{\"error\":{\"code\":\"ErrorCode\"," + "\"message\":{\"lang\":\"en-US\",\"value\":\"Message\"}}}";
    private static final String JSON_ERROR_DOCUMENT_NULL_LOCALE =
            "{\"error\":{\"code\":\"ErrorCode\"," + "\"message\":{\"lang\":null,\"value\":\"Message\"}}}";
    private static final String JSON_ERROR_DOCUMENT_INNER_ERROR = "{\"error\":{\"code\":\"ErrorCode\","
            + "\"message\":{\"lang\":\"en-US\",\"value\":\"Message\"}, \"innererror\":\"Some InnerError\"}}";
    private static final String JSON_ERROR_DOCUMENT_INNER_ERROR_COMPLEX = "{\"error\":{\"code\":\"ErrorCode\","
            + "\"message\":{\"lang\":\"en-US\",\"value\":\"Message\"}, " + "\"innererror\":{\"moreInner\":\"More Inner Error\"}}}";
    private static final String JSON_ERROR_DOCUMENT_INNER_ERROR_COMPLEX_OBJECT =
            "{\"error\":{\"code\":\"ErrorCode\"," + "\"message\":{\"lang\":\"en-US\",\"value\":\"Message\"}, "
                    + "\"innererror\":{\"moreInner\":\"More Inner Error\",\"secondInner\":\"Second\"}}}";
    private static final String JSON_ERROR_DOCUMENT_INNER_ERROR_COMPLEX_ARRAY =
            "{\"error\":{\"code\":\"ErrorCode\"," + "\"message\":{\"lang\":\"en-US\",\"value\":\"Message\"}, "
                    + "\"innererror\":{\"innerArray\":[\"More Inner Error\",\"Second\"]}}}";
    private static final String JSON_ERROR_DOCUMENT_INVALID_JSON =
            "\"error\":{\"code\":\"ErrorCode\"," + "\"message\":{\"lang\":\"en-US\",\"value\":\"Message\"}}}";
    /* error document with name 'locale' instead of 'lang' for message object */
    private static final String JSON_ERROR_DOCUMENT_UNKNOWN_CONTENT =
            "{\"error\":{\"code\":\"ErrorCode\"," + "\"message\":{\"locale\":\"en-US\",\"value\":\"Message\"}}}";
    /* error document without value for message object */
    private static final String JSON_ERROR_DOCUMENT_INVALID_CONTENT =
            "{\"error\":{\"code\":\"ErrorCode\"," + "\"message\":{\"lang\":\"en-US\"}}}";
    private static final String JSON_ERROR_DOCUMENT_MISSING_MESSAGE = "{\"error\":{\"code\":\"ErrorCode\"}}";
    private static final String JSON_ERROR_DOCUMENT_MISSING_CODE =
            "{\"error\":{" + "\"message\":{\"lang\":\"en-US\",\"value\":\"Message\"}}}";
    private static final String JSON_ERROR_DOCUMENT_MISSING_ERROR =
            "{\"code\":\"ErrorCode\"," + "\"message\":{\"lang\":\"en-US\",\"value\":\"Message\"}}";
    private final JsonErrorDocumentConsumer jedc = new JsonErrorDocumentConsumer();

    @Test
    public void simpleErrorDocument() throws Exception {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_SIMPLE);
        ODataErrorContext error = jedc.readError(in);

        assertEquals("Wrong content type", "application/json", error.getContentType());
        assertEquals("Wrong message", "Message", error.getMessage());
        assertEquals("Wrong error code", "ErrorCode", error.getErrorCode());
        LocaleAsserter.assertLocale("Wrong locale for lang", error.getLocale(), Locale.US);
    }

    @Test
    public void localeNull() throws Exception {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_NULL_LOCALE);
        ODataErrorContext error = jedc.readError(in);

        assertEquals("Wrong content type", "application/json", error.getContentType());
        assertEquals("Wrong message", "Message", error.getMessage());
        assertEquals("Wrong error code", "ErrorCode", error.getErrorCode());
        assertNull("Expected NULL for locale", error.getLocale());
    }

    @Test
    public void innerError() throws Exception {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_INNER_ERROR);
        ODataErrorContext error = jedc.readError(in);

        assertEquals("Wrong content type", "application/json", error.getContentType());
        assertEquals("Wrong message", "Message", error.getMessage());
        assertEquals("Wrong error code", "ErrorCode", error.getErrorCode());
        assertEquals("Wrong inner error", "Some InnerError", error.getInnerError());
    }

    @Test
    public void innerErrorComplex() throws Exception {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_INNER_ERROR_COMPLEX);
        ODataErrorContext error = jedc.readError(in);

        assertEquals("Wrong content type", "application/json", error.getContentType());
        assertEquals("Wrong message", "Message", error.getMessage());
        assertEquals("Wrong error code", "ErrorCode", error.getErrorCode());
        assertEquals("Wrong inner error", "{\"moreInner\":\"More Inner Error\"}", error.getInnerError());
    }

    @Test
    public void innerErrorComplexObject() throws Exception {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_INNER_ERROR_COMPLEX_OBJECT);
        ODataErrorContext error = jedc.readError(in);

        assertEquals("Wrong content type", "application/json", error.getContentType());
        assertEquals("Wrong message", "Message", error.getMessage());
        assertEquals("Wrong error code", "ErrorCode", error.getErrorCode());
        assertEquals("Wrong inner error", "{\"moreInner\":\"More Inner Error\",\"secondInner\":\"Second\"}", error.getInnerError());
    }

    @Test
    public void innerErrorComplexArray() throws Exception {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_INNER_ERROR_COMPLEX_ARRAY);
        ODataErrorContext error = jedc.readError(in);

        assertEquals("Wrong content type", "application/json", error.getContentType());
        assertEquals("Wrong message", "Message", error.getMessage());
        assertEquals("Wrong error code", "ErrorCode", error.getErrorCode());
        assertEquals("Wrong inner error", "{\"innerArray\":[\"More Inner Error\"\"Second\"]}", error.getInnerError());
    }

    @Test(expected = EntityProviderException.class)
    public void invalidJson() throws EntityProviderException {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_INVALID_JSON);
        try {
            jedc.readError(in);
            fail("Expected exception was not thrown");
        } catch (EntityProviderException e) {
            assertEquals(EntityProviderException.EXCEPTION_OCCURRED, e.getMessageReference());
            throw e;
        }
    }

    @Test(expected = EntityProviderException.class)
    public void invalidEmptyDocument() throws EntityProviderException {
        InputStream in = StringHelper.encapsulate("");
        try {
            jedc.readError(in);
            fail("Expected exception was not thrown");
        } catch (EntityProviderException e) {
            assertEquals(EntityProviderException.EXCEPTION_OCCURRED, e.getMessageReference());
            throw e;
        }
    }

    @Test(expected = EntityProviderException.class)
    public void nullParameter() throws EntityProviderException {
        try {
            jedc.readError(null);
            fail("Expected exception was not thrown");
        } catch (EntityProviderException e) {
            assertEquals(EntityProviderException.INVALID_STATE, e.getMessageReference());
            throw e;
        }
    }

    @Test(expected = EntityProviderException.class)
    public void invalidErrorDocumentUnknown() throws EntityProviderException {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_UNKNOWN_CONTENT);
        try {
            jedc.readError(in);
            fail("Expected exception was not thrown");
        } catch (EntityProviderException e) {
            assertEquals(EntityProviderException.INVALID_STATE, e.getMessageReference());
            throw e;
        }
    }

    @Test(expected = EntityProviderException.class)
    public void invalidErrorDocument() throws EntityProviderException {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_INVALID_CONTENT);
        try {
            jedc.readError(in);
            fail("Expected exception was not thrown");
        } catch (EntityProviderException e) {
            assertEquals(EntityProviderException.MISSING_PROPERTY, e.getMessageReference());
            throw e;
        }
    }

    @Test(expected = EntityProviderException.class)
    public void invalidErrorDocumentMissingError() throws EntityProviderException {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_MISSING_ERROR);
        try {
            jedc.readError(in);
            fail("Expected exception was not thrown");
        } catch (EntityProviderException e) {
            assertEquals(EntityProviderException.INVALID_STATE, e.getMessageReference());
            throw e;
        }
    }

    @Test(expected = EntityProviderException.class)
    public void invalidErrorDocumentMissingCode() throws EntityProviderException {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_MISSING_CODE);
        try {
            jedc.readError(in);
            fail("Expected exception was not thrown");
        } catch (EntityProviderException e) {
            assertEquals(EntityProviderException.MISSING_PROPERTY, e.getMessageReference());
            assertTrue(e.getMessage()
                        .contains("code"));
            throw e;
        }
    }

    @Test(expected = EntityProviderException.class)
    public void invalidErrorDocumentMissingMessage() throws EntityProviderException {
        InputStream in = StringHelper.encapsulate(JSON_ERROR_DOCUMENT_MISSING_MESSAGE);
        try {
            jedc.readError(in);
            fail("Expected exception was not thrown");
        } catch (EntityProviderException e) {
            assertEquals(EntityProviderException.MISSING_PROPERTY, e.getMessageReference());
            assertTrue(e.getMessage()
                        .contains("message"));
            throw e;
        }
    }
}
