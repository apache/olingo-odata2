package org.apache.olingo.odata2.core.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.ODataContextImpl;
import org.apache.olingo.odata2.core.ODataRequestHandler;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

public class ODataServlet extends HttpServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private ODataServiceFactory serviceFactory;
  private int pathSplit = 0;

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    final String factoryClassName = getInitParameter(ODataServiceFactory.FACTORY_LABEL);
    if (factoryClassName == null) {
      throw new ODataRuntimeException("config missing: org.apache.olingo.odata2.processor.factory");
    }
    try {
      serviceFactory = (ODataServiceFactory) Class.forName(factoryClassName).newInstance();
    } catch (Exception e) {
      throw new ODataRuntimeException(e);
    }
    final String pathSplitAsString = getInitParameter(ODataServiceFactory.PATH_SPLIT_LABEL);
    if (pathSplitAsString != null) {
      pathSplit = Integer.parseInt(pathSplitAsString);
    }
    String method = req.getMethod();
    if (method.equals("GET")) {
      handleRequest(req, ODataHttpMethod.GET, resp);

    } else if (method.equals("POST")) {
      handleRequest(req, ODataHttpMethod.POST, resp);

    } else if (method.equals("PUT")) {
      handleRequest(req, ODataHttpMethod.PUT, resp);

    } else if (method.equals("DELETE")) {
      handleRequest(req, ODataHttpMethod.DELETE, resp);

    } else if (method.equals("PATCH")) {
      handleRequest(req, ODataHttpMethod.PATCH, resp);
    }
    else if (method.equals("MERGE")) {
      handleRequest(req, ODataHttpMethod.MERGE, resp);
    }
  }

  private void handleRequest(final HttpServletRequest req, final ODataHttpMethod method, final HttpServletResponse resp)
      throws IOException {
    try {
      ODataRequest request = ODataRequest.method(method)
          .contentType(RestUtil.extractRequestContentType(req.getContentType()).toContentTypeString())
          .acceptHeaders(RestUtil.extractAcceptHeaders(req.getHeader("Accept")))
          .acceptableLanguages(RestUtil.extractAcceptableLanguage(req.getHeader("Accept-Language")))
          .pathInfo(RestUtil.buildODataPathInfo(req, pathSplit))
          .queryParameters(RestUtil.extractQueryParameters(req.getQueryString()))
          .requestHeaders(RestUtil.extractHeaders(req))
          .body(req.getInputStream())
          .build();
      ODataContextImpl context = new ODataContextImpl(request, serviceFactory);
      ODataService service = serviceFactory.createService(context);
      context.setService(service);
      context.setParameter(ODataContext.HTTP_SERVLET_REQUEST_OBJECT, req);
      service.getProcessor().setContext(context);
      ODataRequestHandler requestHandler = new ODataRequestHandler(serviceFactory, service, context);
      final ODataResponse odataResponse = requestHandler.handle(request);
      createResponse(resp, odataResponse);
    } catch (ODataException e) {
      throw new ODataRuntimeException(e);
    }
  }

  private void createResponse(final HttpServletResponse resp, final ODataResponse response) throws IOException {
    resp.setStatus(response.getStatus().getStatusCode());
    resp.setContentType(response.getContentHeader());
    for (String headerName : response.getHeaderNames()) {
      resp.setHeader(headerName, response.getHeader(headerName));
    }

    Object entity = response.getEntity();
    if (entity != null) {
      OutputStream out = resp.getOutputStream();
      int curByte = -1;
      if (entity instanceof InputStream) {
        while ((curByte = ((InputStream) entity).read()) != -1) {
          out.write((char) curByte);
        }
        out.flush();
        //((InputStream) entity).close();
      } else if (entity instanceof String) {
        Reader sr = new StringReader((String) entity);
        while ((curByte = sr.read()) > -1) {
          out.write(curByte);
        }
        out.flush();
      }
      // out.close();
    }
  }

}
