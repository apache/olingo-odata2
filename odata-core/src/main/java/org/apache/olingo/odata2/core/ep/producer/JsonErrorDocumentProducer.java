/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.ep.producer;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import org.apache.olingo.odata2.core.ep.util.FormatJson;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;

/**
 * @author SAP AG
 */
public class JsonErrorDocumentProducer {

  public void writeErrorDocument(final Writer writer, final String errorCode, final String message, final Locale locale, final String innerError) throws IOException {
    JsonStreamWriter jsonStreamWriter = new JsonStreamWriter(writer);

    jsonStreamWriter.beginObject()
        .name(FormatJson.ERROR)
        .beginObject()
        .namedStringValue(FormatJson.CODE, errorCode).separator()
        .name(FormatJson.MESSAGE)
        .beginObject()
        .namedStringValueRaw(FormatJson.LANG,
            locale == null || locale.getLanguage() == null ? null :
                locale.getLanguage() + (locale.getCountry() == null || locale.getCountry().isEmpty() ? "" : ("-" + locale.getCountry())))
        .separator()
        .namedStringValue(FormatJson.VALUE, message)
        .endObject();
    if (innerError != null) {
      jsonStreamWriter.separator()
          .namedStringValue(FormatJson.INNER_ERROR, innerError);
    }
    jsonStreamWriter.endObject()
        .endObject();
  }
}
