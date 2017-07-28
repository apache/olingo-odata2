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
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.commons.XmlHelper;
import org.apache.olingo.odata2.core.ep.BasicEntityProvider;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

/**
 * Response body debug information.
 */
public class DebugInfoBody implements DebugInfo {

  private final ODataResponse response;
  private final String serviceRoot;
  private final boolean isXml;
  private final boolean isJson;
  private final boolean isText;
  private final boolean isImage;

  public DebugInfoBody(final ODataResponse response, final String serviceRoot) {
    this.response = response;
    this.serviceRoot = serviceRoot;
    final String contentType = response.getContentHeader();
    isXml = contentType.contains("xml");
    isJson = !isXml && contentType.startsWith(HttpContentType.APPLICATION_JSON);
    isText = isXml || isJson || contentType.startsWith("text/")
        || contentType.startsWith(HttpContentType.APPLICATION_HTTP)
        || contentType.startsWith(HttpContentType.MULTIPART_MIXED);
    isImage = !isText && contentType.startsWith("image/");
  }

  @Override
  public String getName() {
    return "Body";
  }

  @Override
  public void appendJson(final JsonStreamWriter jsonStreamWriter) throws IOException {
    if (isJson) {
      jsonStreamWriter.unquotedValue(getContentString());
    } else if (isText) {
      jsonStreamWriter.stringValue(getContentString());
    } else {
      jsonStreamWriter.stringValueRaw(getContentString());
    }
  }

  private String getContentString() {
    if (response.getEntity() instanceof String) {
      return (String) response.getEntity();
    } else if (response.getEntity() instanceof InputStream) {
      InputStream input = (InputStream) response.getEntity();
      try {
        return isText ?
            new BasicEntityProvider().readText(input) :
            Base64.encodeBase64String(new BasicEntityProvider().readBinary((input)));
      } catch (final EntityProviderException e) {
        return null;
      }
    } else {
      throw new ClassCastException("Unsupported content entity class: " + response.getEntity().getClass().getName());
    }
  }

  @Override
  public void appendHtml(final Writer writer) throws IOException {
    final String body = getContentString();
    if (isImage) {
      writer.append("<img src=\"data:").append(response.getContentHeader()).append(";base64,")
          .append(body)
          .append("\" />\n");
    } else {
      writer.append("<pre class=\"code").append(isXml ? " xml" : isJson ? " json" : "").append("\">\n")
          .append(isXml || isJson ?
              addLinks(ODataDebugResponseWrapper.escapeHtml(isXml ? formatXml(body) : formatJson(body)), isXml) :
              ODataDebugResponseWrapper.escapeHtml(body))
          .append("</pre>\n");
    }
  }

  private String formatXml(final String xml) throws IOException {
    try {
      final TransformerFactory transformerFactory = XmlHelper.getTransformerFactory();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      StreamResult outputTarget = new StreamResult(new StringWriter());
      transformer.transform(new StreamSource(new StringReader(xml)), outputTarget);
      return outputTarget.getWriter().toString();
    } catch (final TransformerException e) {
      return xml;
    }
  }

  private String formatJson(final String json) {
    return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(new JsonParser().parse(json));
  }

  private String addLinks(final String source, final boolean isXml) {
    final String debugOption = ODataDebugResponseWrapper.ODATA_DEBUG_QUERY_PARAMETER + "="
        + ODataDebugResponseWrapper.ODATA_DEBUG_HTML;
    final String urlPattern = "("
        + (isXml ? "(?:href|src|base)=" : "\"(?:uri|media_src|edit_media|__next)\":\\p{Space}*")
        + "\")(.+?)\"";
    return (isXml ? source.replaceAll("(xmlns(?::\\p{Alnum}+)?=\")(.+?)\"", "$1<span class=\"ns\">$2</span>\"") :
        source)
        .replaceAll(urlPattern, "$1<a href=\"" + serviceRoot + "$2?" + debugOption + "\">$2</a>\"")
        .replaceAll("(<a href=\"" + Pattern.quote(serviceRoot) + ')' + Pattern.quote(serviceRoot), "$1")
        .replaceAll("<a href=\"(.+?)\\?(.+?)\\?" + debugOption, "<a href=\"$1?$2&amp;" + debugOption)
        .replaceAll("&amp;amp;", "&amp;");
  }
}
