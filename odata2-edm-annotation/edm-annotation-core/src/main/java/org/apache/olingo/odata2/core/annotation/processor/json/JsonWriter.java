/**
 * *****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */
package org.apache.olingo.odata2.core.annotation.processor.json;

import java.io.IOException;
import java.io.Writer;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

public class JsonWriter {

  private final Writer writer;
  private boolean firstProperty = true;

  public JsonWriter(Writer writer) {
    this.writer = writer;
  }

  public void startCallback(String functionName) {
    try {
      writer.write(encode(functionName) + "(");
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  public void endCallback() {
    try {
      writer.write(");");
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  public void startObject() {
    try {
      firstProperty = true;
      writer.write("{\n");
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  public void endObject() {
    try {
      firstProperty = false;
      writer.write("\n}");
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  public void writeName(String name) {
    try {
      writer.write("\"" + encode(name) + "\" : ");
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  public void startArray() {
    try {
      writer.write("[\n");
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  public void endArray() {
    try {
      writer.write("\n]");
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  public void writeSeparator() {
    try {
      writer.write(", ");
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  public void writeRawString(String value) {
    try {
      writer.write("\"" + value + "\"");
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  public void writeString(String value) {
    try {
      writer.write("\"" + encode(value) + "\"");
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  public void writeNull() {
    try {
      writer.write("null");
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  public void writeStringProperty(String name, String value) {
    if (value == null) {
      writeProperty(name, value, false);
    } else {
      writeProperty(name, encode(value), false);
    }
  }

  public void writeRawStringProperty(String name, String value) {
    writeProperty(name, value, false);
  }

  public void writeRawProperty(String name, String value) {
    writeProperty(name, value, true);
  }

  private void writeProperty(String name, String value, boolean raw) {
    if (firstProperty) {
      firstProperty = false;
    } else {
      writeSeparator();
    }

    // write content
    this.writeName(name);
    if (value == null) {
      writeNull();
    } else {
      try {
        if (raw) {
          writeRaw(value);
        } else {
          writeString(value);
        }
      } catch (Exception e) {
        writeString("exception for property with name '" + name
                + "' with message '" + e.getMessage()
                + "' and toString '" + value.toString() + "'.");
      }
    }

  }

  private void writeRaw(String value) {
    try {
      writer.write(value);
    } catch (IOException e) {
      throw new ODataRuntimeException(e.getMessage(), e);
    }
  }

  private String encode(String unencoded) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < unencoded.length(); i++) {
      char c = unencoded.charAt(i);
      if (c == '\\') {
        sb.append("\\\\");
      } else if (c == '"') {
        sb.append("\\\"");
      } else if (c == '\n') {
        sb.append("\\n");
      } else if (c == '\r') {
        sb.append("\\r");
      } else if (c == '\f') {
        sb.append("\\f");
      } else if (c == '\b') {
        sb.append("\\b");
      } else if (c == '\t') {
        sb.append("\\t");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public void start() {
    writeRaw("{\n\"d\":");
  }

  public void finish() {
    writeRaw("\n}");
  }
}
