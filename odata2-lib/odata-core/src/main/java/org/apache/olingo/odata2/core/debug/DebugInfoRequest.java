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
package org.apache.olingo.odata2.core.debug;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;

/**
 * Request debug information.
 */
public class DebugInfoRequest implements DebugInfo {

  private final String method;
  private final URI uri;
  private final String protocol;
  private final Map<String, List<String>> headers;

  public DebugInfoRequest(final String method, final URI uri, final String protocol,
      final Map<String, List<String>> headers) {
    this.method = method;
    this.uri = uri;
    this.protocol = protocol;
    this.headers = headers;
  }

  @Override
  public String getName() {
    return "Request";
  }

  @Override
  public void appendJson(final JsonStreamWriter jsonStreamWriter) throws IOException {
    jsonStreamWriter.beginObject()
        .namedStringValueRaw("method", method.toString()).separator()
        .namedStringValue("uri", uri.toString()).separator()
        .namedStringValue("protocol", protocol);

    if (!headers.isEmpty()) {
      jsonStreamWriter.separator()
          .name("headers")
          .beginObject();
      boolean first = true;
      for (final Entry<String, List<String>> header : headers.entrySet()) {
        for (final String value : header.getValue()) {
          if (value == null) {
            continue;
          }
          if (!first) {
            jsonStreamWriter.separator();
          }
          first = false;
          jsonStreamWriter.namedStringValue(header.getKey(), value);
        }
      }
      jsonStreamWriter.endObject();
    }

    jsonStreamWriter.endObject();
  }

  @Override
  public void appendHtml(final Writer writer) throws IOException {
    writer.append("<h2>Request Method</h2>\n")
        .append("<p>").append(method).append("</p>\n")
        .append("<h2>Request URI</h2>\n")
        .append("<p>").append(ODataDebugResponseWrapper.escapeHtml(uri.toString())).append("</p>\n")
        .append("<h2>Request Protocol</h2>\n")
        .append("<p>").append(protocol).append("</p>\n");
    writer.append("<h2>Request Headers</h2>\n")
        .append("<table>\n<thead>\n")
        .append("<tr><th class=\"name\">Name</th><th class=\"value\">Value</th></tr>\n")
        .append("</thead>\n<tbody>\n");
    for (final Entry<String, List<String>> header : headers.entrySet()) {
      for (final String value : header.getValue()) {
        if (value != null) {
          writer.append("<tr><td class=\"name\">").append(header.getKey()).append("</td>")
              .append("<td class=\"value\">").append(ODataDebugResponseWrapper.escapeHtml(value))
              .append("</td></tr>\n");
        }
      }
    }
    writer.append("</tbody>\n</table>\n");
  }
}
