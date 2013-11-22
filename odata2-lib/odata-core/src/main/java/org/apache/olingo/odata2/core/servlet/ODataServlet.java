package org.apache.olingo.odata2.core.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletException;
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
  public void init() throws ServletException {
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
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    handleRequest(req, ODataHttpMethod.GET, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    handleRequest(req, ODataHttpMethod.POST, resp);
  }

  @Override
  protected void doPut(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    handleRequest(req, ODataHttpMethod.PUT, resp);
  }

  private void handleRequest(final HttpServletRequest req, final ODataHttpMethod method, final HttpServletResponse resp)
      throws IOException {
    try {
      ODataRequest request = ODataRequest.method(method)
          .contentType(RestUtil.extractRequestContentType(req.getContentType()).toContentTypeString())
          .acceptHeaders(RestUtil.extractAcceptHeaders(req.getHeader("Accept")))
          .acceptableLanguages(new ArrayList<Locale>())
          .pathInfo(RestUtil.buildODataPathInfo(req, pathSplit))
          .queryParameters(new HashMap<String, String>())
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
  }

}
