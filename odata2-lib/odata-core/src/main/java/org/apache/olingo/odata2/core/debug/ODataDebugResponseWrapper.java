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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataResponse.ODataResponseBuilder;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.expression.ExpressionParserException;
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
  public static final String ODATA_DEBUG_HTML = "html";
  public static final String ODATA_DEBUG_DOWNLOAD = "download";

  private final ODataContext context;
  private final ODataResponse response;
  private final UriInfo uriInfo;
  private final Exception exception;
  private final boolean isJson;
  private final boolean isDownload;

  public ODataDebugResponseWrapper(final ODataContext context, final ODataResponse response, final UriInfo uriInfo,
      final Exception exception, final String debugValue) {
    this.context = context;
    this.response = response;
    this.uriInfo = uriInfo;
    this.exception = exception;
    isJson = ODATA_DEBUG_JSON.equals(debugValue);
    isDownload = ODATA_DEBUG_DOWNLOAD.equals(debugValue);
  }

  public ODataResponse wrapResponse() {
    try {
      final List<DebugInfo> parts = createParts();
      ODataResponseBuilder builder = ODataResponse.status(HttpStatusCodes.OK)
          .entity(isJson ? wrapInJson(parts) : wrapInHtml(parts))
          .contentHeader(isJson ? HttpContentType.APPLICATION_JSON_UTF8 : HttpContentType.TEXT_HTML);
      if (isDownload) {
        builder.header("Content-Disposition", "attachment; filename=OData-Response."
            + new Date().toString().replace(' ', '_').replace(':', '.') + ".html");
      }
      return builder.build();
    } catch (final ODataException e) {
      throw new ODataRuntimeException("Should not happen", e);
    } catch (final IOException e) {
      throw new ODataRuntimeException("Should not happen", e);
    }
  }

  private List<DebugInfo> createParts() throws ODataException {
    List<DebugInfo> parts = new ArrayList<DebugInfo>();

    // request
    final HttpServletRequest servletRequest =
        (HttpServletRequest) context.getParameter(ODataContext.HTTP_SERVLET_REQUEST_OBJECT);
    final String protocol = servletRequest == null ? null : servletRequest.getProtocol();
    parts.add(new DebugInfoRequest(context.getHttpMethod(), context.getPathInfo().getRequestUri(), protocol,
        context.getRequestHeaders()));

    // response
    parts.add(new DebugInfoResponse(response, context.getPathInfo().getServiceRoot().toASCIIString()));

    // server
    if (servletRequest != null) {
      parts.add(new DebugInfoServer(servletRequest));
    }

    // URI
    Throwable candidate = exception;
    while (candidate != null && !(candidate instanceof ExpressionParserException)) {
      candidate = candidate.getCause();
    }
    final ExpressionParserException expressionParserException = (ExpressionParserException) candidate;
    if (uriInfo != null
        && (uriInfo.getFilter() != null || uriInfo.getOrderBy() != null
            || !uriInfo.getExpand().isEmpty() || !uriInfo.getSelect().isEmpty())
        || expressionParserException != null && expressionParserException.getFilterTree() != null) {
      parts.add(new DebugInfoUri(uriInfo, expressionParserException));
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
    jsonStreamWriter.beginObject()
        .name(parts.get(0).getName().toLowerCase(Locale.ROOT));
    parts.get(0).appendJson(jsonStreamWriter);
    jsonStreamWriter.separator()
        .name(parts.get(1).getName().toLowerCase(Locale.ROOT));
    parts.get(1).appendJson(jsonStreamWriter);
    jsonStreamWriter.separator()
        .name("server")
        .beginObject()
        .namedStringValueRaw("version", ODataDebugResponseWrapper.class.getPackage().getImplementationVersion());
    for (final DebugInfo part : parts.subList(2, parts.size())) {
      jsonStreamWriter.separator()
          .name(part.getName().toLowerCase(Locale.ROOT));
      part.appendJson(jsonStreamWriter);
    }
    jsonStreamWriter.endObject()
        .endObject();
    writer.flush();
    csb.closeWrite();
    return csb.getInputStream();
  }

  private InputStream wrapInHtml(final List<DebugInfo> parts) throws IOException {
    StringWriter writer = new StringWriter();
    PathInfo pathInfo = null;
    try {
      pathInfo = context.getPathInfo();
    } catch (final ODataException e) {}

    writer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n")
        .append("  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n")
        .append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n")
        .append("<head>\n")
        .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n")
        .append("<title>")
        .append(pathInfo == null ? "" :
            escapeHtml(pathInfo.getServiceRoot().relativize(pathInfo.getRequestUri()).getPath()))
        .append("</title>\n")
        .append("<style type=\"text/css\">\n")
        .append("body { font-family: Arial, sans-serif; font-size: 13px;\n")
        .append("       line-height: 16px; margin: 0;\n")
        .append("       background-color: #eeeeee; color: #333333; }\n")
        .append(".header { float: left; }\n")
        .append(".header a { line-height: 22px; padding: 10px 18px;\n")
        .append("            text-decoration: none; color: #333333; }\n")
        .append(":target, .header:nth-last-child(2) { background-color: #cccccc; }\n")
        .append(":target ~ .header:nth-last-child(2) { background-color: inherit; }\n")
        .append(".header:focus, .header:hover,\n")
        .append("  .header:nth-last-child(2):focus, .header:nth-last-child(2):hover\n")
        .append("    { background-color: #999999; }\n")
        .append(".section { position: absolute; top: 42px; min-width: 100%;\n")
        .append("           padding-top: 18px; border-top: 1px solid #dddddd; }\n")
        .append(".section > * { margin-left: 18px; }\n")
        .append(":target + .section, .section:last-child { display: block; }\n")
        .append(".section, :target + .section ~ .section { display: none; }\n")
        .append("h1 { font-size: 18px; font-weight: normal; margin: 10px 0; }\n")
        .append("h2 { font-size: 15px; }\n")
        .append("h2:not(:first-child) { margin-top: 2em; }\n")
        .append("table { border-collapse: collapse; border-spacing: 0;\n")
        .append("        margin-top: 1.5em; }\n")
        .append("table, thead { border-width: 1px 0; border-style: solid;\n")
        .append("               border-color: #dddddd; text-align: left; }\n")
        .append("th.name, td.name { padding: 1ex 2em 1ex 0; }\n")
        .append("tbody > tr:hover { background-color: #cccccc; }\n")
        .append(".code { font-family: \"Courier New\", monospace; }\n")
        .append(".code, .tree li { line-height: 15px; }\n")
        .append(".code a { text-decoration: underline; color: #666666; }\n")
        .append(".xml .ns { font-style: italic; color: #999999; }\n")
        .append("ul, .tree { list-style-type: none; }\n")
        .append("div > ul.expr, div > .expand, .tree { padding-left: 0; }\n")
        .append(".expr, .expand, .null, .numeric { padding-left: 1.5em; }\n")
        .append("</style>\n")
        .append("</head>\n")
        .append("<body>\n");
    char count = '0';
    for (final DebugInfo part : parts) {
      writer.append("<div class=\"header\" id=\"sec").append(++count).append("\">\n")
          .append("<h1><a href=\"#sec").append(count).append("\">")
          .append(part.getName())
          .append("</a></h1>\n")
          .append("</div>\n")
          .append("<div class=\"section\">\n");
      part.appendHtml(writer);
      writer.append("</div>\n");
    }
    writer.append("</body>\n")
        .append("</html>\n")
        .close();
    byte[] bytes = writer.toString().getBytes("UTF-8");
    return new ByteArrayInputStream(bytes);
  }

  protected static String escapeHtml(final String value) {
    return value == null ? null : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  protected static void appendJsonTable(final JsonStreamWriter jsonStreamWriter, final Map<String, String> entries)
      throws IOException {
    jsonStreamWriter.beginObject();
    boolean first = true;
    for (final Entry<String, String> entry : entries.entrySet()) {
      final String value = entries.get(entry.getKey());
      if (value == null) {
        continue;
      }
      if (!first) {
        jsonStreamWriter.separator();
      }
      first = false;
      jsonStreamWriter.namedStringValue(entry.getKey(), value);
    }
    jsonStreamWriter.endObject();
  }

  protected static void appendHtmlTable(final Writer writer, final Map<String, String> entries) throws IOException {
    writer.append("<table>\n<thead>\n")
        .append("<tr><th class=\"name\">Name</th><th class=\"value\">Value</th></tr>\n")
        .append("</thead>\n<tbody>\n");
    for (final Entry<String, String> entry : entries.entrySet()) {
      final String value = entry.getValue();
      if (value != null) {
        writer.append("<tr><td class=\"name\">").append(entry.getKey()).append("</td>")
            .append("<td class=\"value\">")
            .append(ODataDebugResponseWrapper.escapeHtml(value))
            .append("</td></tr>\n");
      }
    }
    writer.append("</tbody>\n</table>\n");
  }
}
