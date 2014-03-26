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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.ep.util.FormatJson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * Consuming (read / deserialization) for OData error document in JSON format.
 */
public class JsonErrorDocumentConsumer {
  /** Default used charset for reader */
  private static final String DEFAULT_CHARSET = "UTF-8";
  /**
   * Map containing language code (language - country) to Locale mapping
   * based on Locale.getAvailableLocales()
   * */
  private final static Map<String, Locale> AVAILABLE_LOCALES = new HashMap<String, Locale>();
  static {
    Locale[] locales = Locale.getAvailableLocales();
    for (Locale l : locales) {
      AVAILABLE_LOCALES.put(l.getLanguage() + "-" + l.getCountry(), l);
    }
  }

  /**
   * Deserialize / read OData error document in ODataErrorContext.
   * 
   * @param errorDocument OData error document in JSON format
   * @return created ODataErrorContext based on input stream content.
   * @throws EntityProviderException if an exception during read / deserialization occurs.
   */
  public ODataErrorContext readError(final InputStream errorDocument) throws EntityProviderException {
    JsonReader reader = createJsonReader(errorDocument);
    try {
      return parseJson(reader);
    } catch (IOException e) {
      throw new EntityProviderException(
          EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getMessage()), e);
    }
  }

  private ODataErrorContext parseJson(final JsonReader reader) throws IOException, EntityProviderException {
    ODataErrorContext errorContext;

    if (reader.hasNext()) {
      reader.beginObject();
      String currentName = reader.nextName();
      if (FormatJson.ERROR.equals(currentName)) {
        errorContext = parseError(reader);
      } else {
        throw new EntityProviderException(EntityProviderException.INVALID_STATE.addContent(
            "Invalid object with name '" + currentName + "' found."));
      }
    } else {
      throw new EntityProviderException(EntityProviderException.INVALID_STATE.addContent(
          "No content to parse found."));
    }

    errorContext.setContentType(ContentType.APPLICATION_JSON.toContentTypeString());
    return errorContext;
  }

  private ODataErrorContext parseError(final JsonReader reader) throws IOException, EntityProviderException {
    ODataErrorContext errorContext = new ODataErrorContext();
    String currentName;
    reader.beginObject();
    boolean messageFound = false;
    boolean codeFound = false;

    while (reader.hasNext()) {
      currentName = reader.nextName();
      if (FormatJson.CODE.equals(currentName)) {
        codeFound = true;
        errorContext.setErrorCode(getValue(reader));
      } else if (FormatJson.MESSAGE.equals(currentName)) {
        messageFound = true;
        parseMessage(reader, errorContext);
      } else if (FormatJson.INNER_ERROR.equals(currentName)) {
        parseInnerError(reader, errorContext);
      } else {
        throw new EntityProviderException(EntityProviderException.INVALID_STATE.addContent(
            "Invalid name '" + currentName + "' found."));
      }
    }

    if (!codeFound) {
      throw new EntityProviderException(
          EntityProviderException.MISSING_PROPERTY.addContent("Mandatory 'code' property not found.'"));
    }
    if (!messageFound) {
      throw new EntityProviderException(
          EntityProviderException.MISSING_PROPERTY.addContent("Mandatory 'message' property not found.'"));
    }

    reader.endObject();
    return errorContext;
  }

  private void parseInnerError(final JsonReader reader, final ODataErrorContext errorContext) throws IOException {
    if(reader.peek() == JsonToken.STRING) {
      // implementation for parse content as provided by JsonErrorDocumentProducer
      String innerError = reader.nextString();
      errorContext.setInnerError(innerError);
    } else if(reader.peek() == JsonToken.BEGIN_OBJECT) {
      // implementation for OData v2 Section 2.2.8.1.2 JSON Error Response
      // (RFC4627 Section 2.2 -> https://www.ietf.org/rfc/rfc4627.txt))
      // currently partial provided
      errorContext.setInnerError(readJson(reader));
    }
  }


  private String readJson(JsonReader reader) throws  IOException {
    StringBuilder sb = new StringBuilder();

    while(reader.hasNext()) {
      if(reader.peek() == JsonToken.NAME) {
        if(sb.length() > 0) {
          sb.append(",");
        }
        String name = reader.nextName();
        sb.append("\"").append(name).append("\"").append(":");
      } else if(reader.peek() == JsonToken.BEGIN_OBJECT) {
        reader.beginObject();
        sb.append("{");
        sb.append(readJson(reader));
        sb.append("}");
        reader.endObject();
      } else if(reader.peek() == JsonToken.BEGIN_ARRAY) {
        reader.beginArray();
        sb.append("[");
        sb.append(readJson(reader));
        sb.append("]");
        reader.endArray();
      } else {
        sb.append("\"");
        sb.append(reader.nextString());
        sb.append("\"");
      }
    }

    return sb.toString();
  }

  private void parseMessage(final JsonReader reader, final ODataErrorContext errorContext)
      throws IOException, EntityProviderException {

    reader.beginObject();
    boolean valueFound = false;
    boolean langFound = false;
    String currentName;

    while (reader.hasNext()) {
      currentName = reader.nextName();
      if (FormatJson.LANG.equals(currentName)) {
        langFound = true;
        String langValue = getValue(reader);
        if (langValue != null) {
          errorContext.setLocale(getLocale(langValue));
        }
      } else if (FormatJson.VALUE.equals(currentName)) {
        valueFound = true;
        errorContext.setMessage(getValue(reader));
      } else {
        throw new EntityProviderException(EntityProviderException.INVALID_STATE.addContent("Invalid name '" +
            currentName + "' found."));
      }
    }

    if (!langFound) {
      throw new EntityProviderException(
          EntityProviderException.MISSING_PROPERTY.addContent("Mandatory 'lang' property not found.'"));
    }
    if (!valueFound) {
      throw new EntityProviderException(
          EntityProviderException.MISSING_PROPERTY.addContent("Mandatory 'value' property not found.'"));
    }
    reader.endObject();
  }

  private Locale getLocale(final String langValue) {
    return AVAILABLE_LOCALES.get(langValue);
  }

  /**
   * Read the string value from the JsonReader or 'null' if no value is available.
   * 
   * @param reader to read from
   * @return the string value or 'null'
   * @throws IOException if an exception occurs
   */
  private String getValue(final JsonReader reader) throws IOException {
    JsonToken token = reader.peek();
    if (JsonToken.NULL == token) {
      reader.skipValue();
      return null;
    }
    return reader.nextString();
  }

  private JsonReader createJsonReader(final InputStream in) throws EntityProviderException {
    if (in == null) {
      throw new EntityProviderException(EntityProviderException.INVALID_STATE
          .addContent(("Got not supported NULL object as content to de-serialize.")));
    }
    try {
      return new JsonReader(new InputStreamReader(in, DEFAULT_CHARSET));
    } catch (final UnsupportedEncodingException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }
}
