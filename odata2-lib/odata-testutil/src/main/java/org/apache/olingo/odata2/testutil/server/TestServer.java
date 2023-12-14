/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.apache.olingo.odata2.testutil.server;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.testutil.fit.FitStaticServiceFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServlet;

/**
 *
 */
public class TestServer {
    private static final Logger log = LoggerFactory.getLogger(TestServer.class);

    private static final int PORT_MIN = 19000;
    private static final int PORT_MAX = 19200;
    private static final int PORT_INC = 1;

    private static final String DEFAULT_SCHEME = "http";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PATH = "/test";

    private URI endpoint; // = URI.create("http://localhost:19080/test"); // no slash at the end !!!
    private final String path;

    private int pathSplit = 0;
    private final ServletType servletType;

    public TestServer(final ServletType type) {
        this(DEFAULT_PATH, type);
    }

    public TestServer(final String path, final ServletType type) {
        if (path.startsWith("/")) {
            this.path = path;
        } else {
            this.path = "/" + path;
        }
        servletType = type;
    }

    public int getPathSplit() {
        return pathSplit;
    }

    public void setPathSplit(final int pathSplit) {
        this.pathSplit = pathSplit;
    }

    public URI getEndpoint() {
        return URI.create(endpoint + "/");
    }

    private Server server;

    public void startServer(final Class<? extends ODataServiceFactory> factoryClass, final int fixedPort) {
        try {
            final ServletContextHandler contextHandler = createContextHandler(factoryClass);
            final InetSocketAddress isa = new InetSocketAddress(DEFAULT_HOST, fixedPort);
            server = new Server(isa);

            server.setHandler(contextHandler);
            server.start();
            endpoint = new URI(DEFAULT_SCHEME, null, DEFAULT_HOST, isa.getPort(), "/abc" + path, null, null);
            log.trace("Started server at endpoint " + endpoint.toASCIIString());
        } catch (final Exception e) {
            log.error("server start failed", e);
            throw new ServerRuntimeException(e);
        }
    }

    public void startServer(final Class<? extends ODataServiceFactory> factoryClass) {
        try {
            for (int port = PORT_MIN; port <= PORT_MAX; port += PORT_INC) {
                final ServletContextHandler contextHandler = createContextHandler(factoryClass);
                try {
                    final InetSocketAddress isa = new InetSocketAddress(DEFAULT_HOST, port);
                    server = new Server(isa);

                    server.setHandler(contextHandler);
                    server.start();
                    endpoint = new URI(DEFAULT_SCHEME, null, DEFAULT_HOST, isa.getPort(), "/abc" + path, null, null);
                    log.trace("Started server at endpoint " + endpoint.toASCIIString());
                    break;
                } catch (final BindException e) {
                    log.trace("port is busy... " + port + " [" + e.getMessage() + "]");
                }
            }

            if (!server.isStarted()) {
                throw new BindException("no free port in range of [" + PORT_MIN + ".." + PORT_MAX + "]");
            }
        } catch (final Exception e) {
            log.error("server start failed", e);
            throw new ServerRuntimeException(e);
        }
    }

    private ServletContextHandler createContextHandler(final Class<? extends ODataServiceFactory> factoryClass) throws Exception {
        ServletHolder odataServletHolder = null;

        switch (servletType) {
            case JAXRS_SERVLET:
                CXFNonSpringJaxrsServlet cxfNonSpringJaxrsServlet = new CXFNonSpringJaxrsServlet();
                odataServletHolder = new ServletHolder(cxfNonSpringJaxrsServlet);
                odataServletHolder.setInitParameter("jakarta.ws.rs.Application", "org.apache.olingo.odata2.core.rest.app.ODataApplication");
                odataServletHolder.setInitParameter(ODataServiceFactory.FACTORY_LABEL, factoryClass.getCanonicalName());
                break;
            case ODATA_SERVLET:
                String odataServlet = "org.apache.olingo.odata2.core.servlet.ODataServlet";
                final HttpServlet httpServlet = (HttpServlet) Class.forName(odataServlet)
                                                                   .newInstance();
                odataServletHolder = new ServletHolder(httpServlet);
                odataServletHolder.setInitParameter(ODataServiceFactory.FACTORY_LABEL, factoryClass.getCanonicalName());
                break;
            default:
        }

        if (pathSplit > 0) {
            odataServletHolder.setInitParameter(ODataServiceFactory.PATH_SPLIT_LABEL, Integer.toString(pathSplit));
        }

        final ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/abc");
        contextHandler.addServlet(odataServletHolder, path + "/*");
        return contextHandler;
    }

    public void startServer(final ODataService service) {
        startServer(FitStaticServiceFactory.class);

        if ((server != null) && server.isStarted()) {
            FitStaticServiceFactory.bindService(this, service);
        }
    }

    public void startServer(final ODataService service, Class<? extends FitStaticServiceFactory> clazz) {
        startServer(clazz);

        if ((server != null) && server.isStarted()) {
            FitStaticServiceFactory.bindService(this, service);
        }
    }

    public void stopServer() {
        try {
            if (server != null) {
                FitStaticServiceFactory.unbindService(this);
                server.stop();
                log.trace("Stopped server at endpoint " + getEndpoint().toASCIIString());
            }
        } catch (final Exception e) {
            throw new ServerRuntimeException(e);
        }
    }
}
