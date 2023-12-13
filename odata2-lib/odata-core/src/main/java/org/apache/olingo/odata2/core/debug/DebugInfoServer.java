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
import java.util.Map;
import java.util.TreeMap;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;

/**
 * Server debug information.
 */
public class DebugInfoServer implements DebugInfo {

  private final Map<String, String> environment;

  public DebugInfoServer(final HttpServletRequest httpServletRequest) {
    environment = new TreeMap<String, String>();
    environment.put("authType", httpServletRequest.getAuthType());
    environment.put("localAddr", httpServletRequest.getLocalAddr());
    environment.put("localName", httpServletRequest.getLocalName());
    addInt("localPort", httpServletRequest.getLocalPort());
    environment.put("pathInfo", httpServletRequest.getPathInfo());
    environment.put("pathTranslated", httpServletRequest.getPathTranslated());
    environment.put("remoteAddr", httpServletRequest.getRemoteAddr());
    environment.put("remoteHost", httpServletRequest.getRemoteHost());
    addInt("remotePort", httpServletRequest.getRemotePort());
    environment.put("remoteUser", httpServletRequest.getRemoteUser());
    environment.put("scheme", httpServletRequest.getScheme());
    environment.put("serverName", httpServletRequest.getServerName());
    addInt("serverPort", httpServletRequest.getServerPort());
    environment.put("servletPath", httpServletRequest.getServletPath());
  }

  @Override
  public String getName() {
    return "Environment";
  }

  @Override
  public void appendJson(final JsonStreamWriter jsonStreamWriter) throws IOException {
    ODataDebugResponseWrapper.appendJsonTable(jsonStreamWriter, environment);
  }

  @Override
  public void appendHtml(final Writer writer) throws IOException {
    final Package pack = ODataDebugResponseWrapper.class.getPackage();
    writer.append("<h2>Library Version</h2>\n")
        .append("<p>").append(pack.getImplementationTitle())
        .append(" Version ").append(pack.getImplementationVersion()).append("</p>\n")
        .append("<h2>Server Environment</h2>\n");
    ODataDebugResponseWrapper.appendHtmlTable(writer, environment);
  }

  private void addInt(final String name, final int number) {
    environment.put(name, number == 0 ? null : Integer.toString(number));
  }
}
