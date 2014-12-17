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
package org.apache.olingo.odata2.core.servlet;

import java.lang.reflect.Field;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.core.rest.ODataServiceFactoryImpl;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class ODataServletTest {

  private HttpServletRequest reqMock;
  private HttpServletResponse respMock;
  private ServletConfig configMock;

  public ODataServletTest() {
    reqMock = Mockito.mock(HttpServletRequest.class);
    respMock = Mockito.mock(HttpServletResponse.class);
    configMock = Mockito.mock(ServletConfig.class);
  }

  @Test
  public void handleRedirect() throws Exception {
    ODataServlet servlet = new ODataServlet();
    prepareServlet(servlet);
    prepareRequest(reqMock);
    servlet.service(reqMock, respMock);

    Mockito.verify(respMock).setStatus(HttpStatusCodes.TEMPORARY_REDIRECT.getStatusCode());
    Mockito.verify(respMock).setHeader(HttpHeaders.LOCATION, "/context-path/servlet-path/");
  }

  @Test
  public void handleRedirectWoServletPath() throws Exception {
    ODataServlet servlet = new ODataServlet();
    prepareServlet(servlet);
    prepareRequest(reqMock, "/context-path", null);
    servlet.service(reqMock, respMock);

    Mockito.verify(respMock).setStatus(HttpStatusCodes.TEMPORARY_REDIRECT.getStatusCode());
    Mockito.verify(respMock).setHeader(HttpHeaders.LOCATION, "/context-path/");
  }

  @Test
  public void handleRedirectWoContextPath() throws Exception {
    ODataServlet servlet = new ODataServlet();
    prepareServlet(servlet);
    prepareRequest(reqMock, null, "/servlet-path");
    servlet.service(reqMock, respMock);

    Mockito.verify(respMock).setStatus(HttpStatusCodes.TEMPORARY_REDIRECT.getStatusCode());
    Mockito.verify(respMock).setHeader(HttpHeaders.LOCATION, "/servlet-path/");
  }

  @Test
  public void handleRedirectWoPath() throws Exception {
    ODataServlet servlet = new ODataServlet();
    prepareServlet(servlet);
    prepareRequest(reqMock, null, null);
    servlet.service(reqMock, respMock);

    Mockito.verify(respMock).setStatus(HttpStatusCodes.TEMPORARY_REDIRECT.getStatusCode());
    Mockito.verify(respMock).setHeader(HttpHeaders.LOCATION, "/");
  }

  private void prepareRequest(final HttpServletRequest req, final String contextPath, final String servletPath) {
    Mockito.when(req.getMethod()).thenReturn("GET");
    Mockito.when(req.getContextPath()).thenReturn(contextPath);
    Mockito.when(req.getServletPath()).thenReturn(servletPath);
  }

  private void prepareRequest(final HttpServletRequest req) {
    prepareRequest(req, "/context-path", "/servlet-path");
  }

  private void prepareServlet(final GenericServlet servlet) throws Exception {
    // private transient ServletConfig config;
    Field configField = GenericServlet.class.getDeclaredField("config");
    configField.setAccessible(true);
    configField.set(servlet, configMock);

    String factoryClassName = ODataServiceFactoryImpl.class.getName();
    Mockito.when(configMock.getInitParameter(ODataServiceFactory.FACTORY_LABEL)).thenReturn(factoryClassName);
  }
}
