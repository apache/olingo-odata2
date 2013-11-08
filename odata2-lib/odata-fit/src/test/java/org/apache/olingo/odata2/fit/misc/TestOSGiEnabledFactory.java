package org.apache.olingo.odata2.fit.misc;

import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestOSGiEnabledFactory {

  private static final URI endpoint = URI.create("http://localhost:19080/osgi");
  private Server server;

  @Before
  public void before() throws Exception {
    final ServletContextHandler contextHandler = createContextHandler();
    final InetSocketAddress isa = new InetSocketAddress(endpoint.getHost(), endpoint.getPort());
    server = new Server(isa);

    server.setHandler(contextHandler);
    server.start();
  }

  @After
  public void after() throws Exception {
    if (server != null) {
      server.stop();
    }
  }

  @Test
  public void run() throws Exception {
    URL url = new URL(endpoint + "/$metadata");
    URLConnection con = url.openConnection();
    con.addRequestProperty("accept", "*/*");
    Object content = con.getContent();
    Map<String, List<String>> bla = con.getHeaderFields();
    System.out.println(bla);
    System.out.println(con.getHeaderField(0));
    assertNotNull(content);

  }

  private ServletContextHandler createContextHandler() {
    final CXFNonSpringJaxrsServlet odataServlet = new CXFNonSpringJaxrsServlet();
    final ServletHolder odataServletHolder = new ServletHolder(odataServlet);
    odataServletHolder.setInitParameter("javax.ws.rs.Application",
        MyRestApplication.class.getName());

    final ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    contextHandler.addServlet(odataServletHolder, endpoint.getPath() + "/*");
    return contextHandler;
  }

}
