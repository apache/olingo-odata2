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
package org.apache.olingo.odata2.core.annotation.processor.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.olingo.odata2.core.ep.util.FormatJson;

/**
 *
 */
public class JsonConsumer {

  private final JsonReader reader;
  private final Map<String, String> properties;

  public JsonConsumer(InputStream content) {
    reader = new JsonReader(new InputStreamReader(content));
    properties = new HashMap<String, String>();
  }

  public static Map<String, String> readContent(InputStream content) throws IOException {
    JsonConsumer jc = new JsonConsumer(content);
    return jc.read();
  }

  public Map<String, String> read() throws IOException {

    reader.beginObject();
    String nextName = reader.nextName();
    if (FormatJson.D.equals(nextName)) {
      reader.beginObject();
      readEntryContent();
      reader.endObject();
    } else {
      handleName(nextName);
      readEntryContent();
    }
    reader.endObject();

    return properties;
  }

  private void readEntryContent() throws IOException {
    while (reader.hasNext()) {
      final String name = reader.nextName();
      handleName(name);
    }
  }

  private void handleName(final String name) throws IOException {
    if (FormatJson.METADATA.equals(name)) {
      readMetadata();
    } else {
      readPropertyValue(name);
    }
  }

  private void readPropertyValue(final String name) throws IOException {
    final JsonToken tokenType = reader.peek();
    final String value;

    switch (tokenType) {
      case STRING:
        value = reader.nextString();
        break;
      case BOOLEAN:
        value = String.valueOf(reader.nextBoolean());
        break;
      case NULL:
        reader.nextNull();
      default:
        value = null;
    }

    properties.put(name, value);
  }

  private void readMetadata() throws IOException {
    reader.beginObject();

    while (reader.hasNext()) {
      String name = reader.nextName();

      if (FormatJson.PROPERTIES.equals(name)) {
        reader.skipValue();
        continue;
      }

      String value = reader.nextString();
      if (FormatJson.ID.equals(name)) {
        properties.put("md_" + name, value);
      } else if (FormatJson.URI.equals(name)) {
        properties.put("md_" + name, value);
      } else if (FormatJson.TYPE.equals(name)) {
        properties.put("md_" + name, value);
      } else if (FormatJson.ETAG.equals(name)) {
        properties.put("md_" + name, value);
      } else if (FormatJson.EDIT_MEDIA.equals(name)) {
        properties.put("md_" + name, value);
      } else if (FormatJson.MEDIA_SRC.equals(name)) {
        properties.put("md_" + name, value);
      } else if (FormatJson.MEDIA_ETAG.equals(name)) {
        properties.put("md_" + name, value);
      } else if (FormatJson.CONTENT_TYPE.equals(name)) {
        properties.put("md_" + name, value);
      } else {
        throw new IllegalStateException("Unknown metadata");
      }
    }

    reader.endObject();
  }

}
