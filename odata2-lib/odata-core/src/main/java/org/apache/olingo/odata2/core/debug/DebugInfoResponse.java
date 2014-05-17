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
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;

/**
 * Response debug information.
 */
public class DebugInfoResponse implements DebugInfo {

  private final ODataResponse response;
  private final String serviceRoot;
  private final HttpStatusCodes status;
  private final Map<String, String> headers;

  public DebugInfoResponse(final ODataResponse response, final String serviceRoot) {
    this.response = response;
    this.serviceRoot = serviceRoot;
    status = response.getStatus();
    headers = new HashMap<String, String>();
    for (final String name : response.getHeaderNames()) {
      headers.put(name, response.getHeader(name));
    }
  }

  @Override
  public String getName() {
    return "Response";
  }

  @Override
  public void appendJson(final JsonStreamWriter jsonStreamWriter) throws IOException {
    jsonStreamWriter.beginObject();

    if (status != null) {
      jsonStreamWriter.name("status")
          .beginObject()
          .name("code").unquotedValue(Integer.toString(status.getStatusCode())).separator()
          .namedStringValueRaw("info", status.getInfo())
          .endObject();
    }

    if (!headers.isEmpty()) {
      if (status != null) {
        jsonStreamWriter.separator();
      }
      jsonStreamWriter.name("headers");
      ODataDebugResponseWrapper.appendJsonTable(jsonStreamWriter, headers);
    }

    if (response.getContentHeader() != null && response.getEntity() != null) {
      if (status != null || !headers.isEmpty()) {
        jsonStreamWriter.separator();
      }
      jsonStreamWriter.name("body");
      new DebugInfoBody(response, serviceRoot).appendJson(jsonStreamWriter);
    }

    jsonStreamWriter.endObject();
  }

  @Override
  public void appendHtml(final Writer writer) throws IOException {
    writer.append("<h2>Status Code</h2>\n")
        .append("<p>").append(Integer.toString(status.getStatusCode())).append(' ')
        .append(status.getInfo()).append("</p>\n")
        .append("<h2>Response Headers</h2>\n");
    ODataDebugResponseWrapper.appendHtmlTable(writer, headers);
    if (response.getContentHeader() != null && response.getEntity() != null) {
      writer.append("<h2>Response Body</h2>\n");
      new DebugInfoBody(response, serviceRoot).appendHtml(writer);
    }
  }
}
