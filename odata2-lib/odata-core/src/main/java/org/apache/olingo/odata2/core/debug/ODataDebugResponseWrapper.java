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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.core.ep.util.CircleStreamBuffer;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;
import org.apache.olingo.odata2.core.exception.MessageService;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 * Wraps an OData response into an OData response containing additional
 * information useful for support purposes.
 */
public class ODataDebugResponseWrapper {

  public static final String ODATA_DEBUG_QUERY_PARAMETER = "odata-debug";
  public static final String ODATA_DEBUG_JSON = "json";

  private final ODataContext context;
  private final ODataResponse response;
  private final UriInfo uriInfo;
  private final Exception exception;

  public ODataDebugResponseWrapper(final ODataContext context, final ODataResponse response, final UriInfo uriInfo,
      final Exception exception) {
    this.context = context;
    this.response = response;
    this.uriInfo = uriInfo;
    this.exception = exception;
  }

  public ODataResponse wrapResponse() {
    try {
      return ODataResponse.status(HttpStatusCodes.OK)
          .entity(wrapInJson(createParts()))
          .contentHeader(HttpContentType.APPLICATION_JSON)
          .build();
    } catch (final ODataException e) {
      throw new ODataRuntimeException("Should not happen", e);
    } catch (final IOException e) {
      throw new ODataRuntimeException("Should not happen", e);
    }
  }

  private List<DebugInfo> createParts() throws ODataException {
    List<DebugInfo> parts = new ArrayList<DebugInfo>();

    // body
    if (response.getContentHeader() != null && response.getEntity() != null) {
      parts.add(new DebugInfoBody(response));
    }

    // request
    parts.add(new DebugInfoRequest(context.getHttpMethod(),
        context.getPathInfo().getRequestUri(),
        context.getRequestHeaders()));

    // response
    Map<String, String> responseHeaders = new HashMap<String, String>();
    for (final String name : response.getHeaderNames()) {
      responseHeaders.put(name, response.getHeader(name));
    }
    parts.add(new DebugInfoResponse(response.getStatus(), responseHeaders));

    // URI
    if (uriInfo != null
        && (uriInfo.getFilter() != null || uriInfo.getOrderBy() != null
            || !uriInfo.getExpand().isEmpty() || !uriInfo.getSelect().isEmpty())) {
      parts.add(new DebugInfoUri(uriInfo, exception));
    }

    // runtime measurements
    if (context.getRuntimeMeasurements() != null) {
      parts.add(new DebugInfoRuntime(context.getRuntimeMeasurements()));
    }

    // exceptions
    if (exception != null) {
      final Locale locale = MessageService.getSupportedLocale(context.getAcceptableLanguages(), Locale.ENGLISH);
      parts.add(new DebugInfoException(exception, locale));
    }

    return parts;
  }

  private InputStream wrapInJson(final List<DebugInfo> parts) throws IOException {
    CircleStreamBuffer csb = new CircleStreamBuffer();
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(csb.getOutputStream(), "UTF-8"));
    JsonStreamWriter jsonStreamWriter = new JsonStreamWriter(writer);
    jsonStreamWriter.beginObject();
    boolean first = true;
    for (final DebugInfo part : parts) {
      if (!first) {
        jsonStreamWriter.separator();
      }
      first = false;
      jsonStreamWriter.name(part.getName().toLowerCase(Locale.ROOT));
      part.appendJson(jsonStreamWriter);
    }
    jsonStreamWriter.endObject();
    writer.flush();
    csb.closeWrite();
    return csb.getInputStream();
  }
}
