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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataHttpException;
import org.apache.olingo.odata2.api.exception.ODataInternalServerErrorException;
import org.apache.olingo.odata2.api.exception.ODataMethodNotAllowedException;
import org.apache.olingo.odata2.api.exception.ODataNotAcceptableException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.ODataContextImpl;
import org.apache.olingo.odata2.core.ODataRequestHandler;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

public class ODataServlet extends HttpServlet {

  private static final String HTTP_METHOD_OPTIONS = "OPTIONS";
  private static final String HTTP_METHOD_HEAD = "HEAD";

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    final String factoryClassName = getInitParameter(ODataServiceFactory.FACTORY_LABEL);
    if (factoryClassName == null) {
      throw new ODataRuntimeException("config missing: org.apache.olingo.odata2.processor.factory");
    }

    // We have to create the Service Factory here because otherwise we do not have access to the error callback
    ODataServiceFactory serviceFactory = createServiceFactory(req);

    String xHttpMethod = req.getHeader("X-HTTP-Method");
    String xHttpMethodOverride = req.getHeader("X-HTTP-Method-Override");
    if (xHttpMethod != null && xHttpMethodOverride != null) {
      if (!xHttpMethod.equalsIgnoreCase(xHttpMethodOverride)) {
        ODataExceptionWrapper wrapper = new ODataExceptionWrapper(req, serviceFactory);
        createResponse(resp, wrapper.wrapInExceptionResponse(
            new ODataBadRequestException(ODataBadRequestException.AMBIGUOUS_XMETHOD)));
      }
    }

    if (req.getPathInfo() != null) {
      handle(req, resp, xHttpMethod, xHttpMethodOverride, serviceFactory);
    } else {
      handleRedirect(req, resp, serviceFactory);
    }
  }

  private void handle(final HttpServletRequest req, final HttpServletResponse resp, final String xHttpMethod,
      final String xHttpMethodOverride, ODataServiceFactory serviceFactory) throws IOException {
    String method = req.getMethod();
    if (ODataHttpMethod.GET.name().equals(method)) {
      handleRequest(req, ODataHttpMethod.GET, resp, serviceFactory);
    } else if (ODataHttpMethod.POST.name().equals(method)) {
      if (xHttpMethod == null && xHttpMethodOverride == null) {
        handleRequest(req, ODataHttpMethod.POST, resp, serviceFactory);
      } else if (xHttpMethod == null) {
        /* tunneling */
        boolean methodHandled = handleHttpTunneling(req, resp, xHttpMethodOverride, serviceFactory);
        if (!methodHandled) {
          createMethodNotAllowedResponse(req, ODataHttpException.COMMON, resp, serviceFactory);
        }
      } else {
        /* tunneling */
        boolean methodHandled = handleHttpTunneling(req, resp, xHttpMethod, serviceFactory);
        if (!methodHandled) {
          createNotImplementedResponse(req, ODataNotImplementedException.TUNNELING, resp, serviceFactory);
        }
      }

    } else if (ODataHttpMethod.PUT.name().equals(method)) {
      handleRequest(req, ODataHttpMethod.PUT, resp, serviceFactory);
    } else if (ODataHttpMethod.DELETE.name().equals(method)) {
      handleRequest(req, ODataHttpMethod.DELETE, resp, serviceFactory);
    } else if (ODataHttpMethod.PATCH.name().equals(method)) {
      handleRequest(req, ODataHttpMethod.PATCH, resp, serviceFactory);
    } else if (ODataHttpMethod.MERGE.name().equals(method)) {
      handleRequest(req, ODataHttpMethod.MERGE, resp, serviceFactory);
    } else if (HTTP_METHOD_HEAD.equals(method) || HTTP_METHOD_OPTIONS.equals(method)) {
      createNotImplementedResponse(req, ODataNotImplementedException.COMMON, resp, serviceFactory);
    } else {
      createNotImplementedResponse(req, ODataHttpException.COMMON, resp, serviceFactory);
    }
  }

  private boolean handleHttpTunneling(final HttpServletRequest req, final HttpServletResponse resp,
      final String xHttpMethod, ODataServiceFactory serviceFactory) throws IOException {
    if (ODataHttpMethod.MERGE.name().equals(xHttpMethod)) {
      handleRequest(req, ODataHttpMethod.MERGE, resp, serviceFactory);
    } else if (ODataHttpMethod.PATCH.name().equals(xHttpMethod)) {
      handleRequest(req, ODataHttpMethod.PATCH, resp, serviceFactory);
    } else if (ODataHttpMethod.DELETE.name().equals(xHttpMethod)) {
      handleRequest(req, ODataHttpMethod.DELETE, resp, serviceFactory);
    } else if (ODataHttpMethod.PUT.name().equals(xHttpMethod)) {
      handleRequest(req, ODataHttpMethod.PUT, resp, serviceFactory);
    } else if (ODataHttpMethod.GET.name().equals(xHttpMethod)) {
      handleRequest(req, ODataHttpMethod.GET, resp, serviceFactory);
    } else if (ODataHttpMethod.POST.name().equals(xHttpMethod)) {
      handleRequest(req, ODataHttpMethod.POST, resp, serviceFactory);
    } else if (HTTP_METHOD_HEAD.equals(xHttpMethod) || HTTP_METHOD_OPTIONS.equals(xHttpMethod)) {
      createNotImplementedResponse(req, ODataNotImplementedException.COMMON, resp, serviceFactory);
    } else {
      createNotImplementedResponse(req, ODataNotImplementedException.COMMON, resp, serviceFactory);
    }
    return true;
  }

  private void
      handleRequest(final HttpServletRequest req, final ODataHttpMethod method, final HttpServletResponse resp,
          ODataServiceFactory serviceFactory)
          throws IOException {
    try {
      final String pathSplitAsString = getInitParameter(ODataServiceFactory.PATH_SPLIT_LABEL);
      int pathSplit = 0;
      if (pathSplitAsString != null) {
        pathSplit = Integer.parseInt(pathSplitAsString);
      }
      if (req.getHeader(HttpHeaders.ACCEPT) != null && req.getHeader(HttpHeaders.ACCEPT).isEmpty()) {
        createNotAcceptableResponse(req, ODataNotAcceptableException.COMMON, resp, serviceFactory);
      }
      ODataRequest odataRequest = ODataRequest.method(method)
          .contentType(RestUtil.extractRequestContentType(req.getContentType()).toContentTypeString())
          .acceptHeaders(RestUtil.extractAcceptHeaders(req.getHeader(HttpHeaders.ACCEPT)))
          .acceptableLanguages(RestUtil.extractAcceptableLanguage(req.getHeader(HttpHeaders.ACCEPT_LANGUAGE)))
          .pathInfo(RestUtil.buildODataPathInfo(req, pathSplit))
          .allQueryParameters(RestUtil.extractAllQueryParameters(req.getQueryString()))
          .requestHeaders(RestUtil.extractHeaders(req))
          .body(req.getInputStream())
          .build();

      ODataContextImpl context = new ODataContextImpl(odataRequest, serviceFactory);
      context.setParameter(ODataContext.HTTP_SERVLET_REQUEST_OBJECT, req);

      ODataService service = serviceFactory.createService(context);
      if (service == null) {
        createServiceUnavailableResponse(req, ODataInternalServerErrorException.NOSERVICE, resp, serviceFactory);
      } else {
        context.setService(service);
        service.getProcessor().setContext(context);

        ODataRequestHandler requestHandler = new ODataRequestHandler(serviceFactory, service, context);
        final ODataResponse odataResponse = requestHandler.handle(odataRequest);
        createResponse(resp, odataResponse);
      }
    } catch (ODataException e) {
      ODataExceptionWrapper wrapper = new ODataExceptionWrapper(req, serviceFactory);
      createResponse(resp, wrapper.wrapInExceptionResponse(e));
    }
  }

  private ODataServiceFactory createServiceFactory(HttpServletRequest req) {
    try {
      final String factoryClassName = getInitParameter(ODataServiceFactory.FACTORY_LABEL);
      ClassLoader cl = (ClassLoader) req.getAttribute(ODataServiceFactory.FACTORY_CLASSLOADER_LABEL);
      if (cl == null) {
        return (ODataServiceFactory) Class.forName(factoryClassName).newInstance();
      } else {
        return (ODataServiceFactory) Class.forName(factoryClassName, true, cl).newInstance();
      }
    } catch (Exception e) {
      throw new ODataRuntimeException(e);
    }
  }

  private void handleRedirect(final HttpServletRequest req, final HttpServletResponse resp,
      ODataServiceFactory serviceFactory) throws IOException {
    String method = req.getMethod();
    if (ODataHttpMethod.GET.name().equals(method) ||
        ODataHttpMethod.POST.name().equals(method) ||
        ODataHttpMethod.PUT.name().equals(method) ||
        ODataHttpMethod.DELETE.name().equals(method) ||
        ODataHttpMethod.PATCH.name().equals(method) ||
        ODataHttpMethod.MERGE.name().equals(method) ||
        HTTP_METHOD_HEAD.equals(method) ||
        HTTP_METHOD_OPTIONS.equals(method)) {
      ODataResponse odataResponse = ODataResponse.status(HttpStatusCodes.TEMPORARY_REDIRECT)
          .header(HttpHeaders.LOCATION, createLocation(req))
          .build();
      createResponse(resp, odataResponse);
    } else {
      createNotImplementedResponse(req, ODataHttpException.COMMON, resp, serviceFactory);
    }

  }

  private String createLocation(final HttpServletRequest req) {
    StringBuilder location = new StringBuilder();
    String contextPath = req.getContextPath();
    if (contextPath != null) {
      location.append(contextPath);
    }
    String servletPath = req.getServletPath();
    if (servletPath != null) {
      location.append(servletPath);
    }
    location.append("/");
    return location.toString();
  }

  private void createResponse(final HttpServletResponse resp, final ODataResponse response) throws IOException {
    resp.setStatus(response.getStatus().getStatusCode());
    resp.setContentType(response.getContentHeader());
    for (String headerName : response.getHeaderNames()) {
      resp.setHeader(headerName, response.getHeader(headerName));
    }

    Object entity = response.getEntity();
    if (entity != null) {
      ServletOutputStream out = resp.getOutputStream();
      int curByte;
      int contentLength = 0;

      if (entity instanceof InputStream) {
        while ((curByte = ((InputStream) entity).read()) != -1) {
          contentLength++;
          out.write((char) curByte);
        }
        ((InputStream) entity).close();
      } else if (entity instanceof String) {
        String body = (String) entity;
        final byte[] entityBytes = body.getBytes("utf-8");
        out.write(entityBytes);
        contentLength = entityBytes.length;
      }

      if (response.getHeader(HttpHeaders.CONTENT_LENGTH) != null) {
        // Override content length
        try {
          contentLength = Integer.parseInt(response.getHeader(HttpHeaders.CONTENT_LENGTH));
        } catch (NumberFormatException e) {
          // Ignore
        }
      }

      resp.setContentLength(contentLength);
      out.flush();
      out.close();
    }
  }

  private void createNotImplementedResponse(final HttpServletRequest req, final MessageReference messageReference,
      final HttpServletResponse resp, ODataServiceFactory serviceFactory) throws IOException {
    // RFC 2616, 5.1.1: "An origin server SHOULD return the status code [...]
    // 501 (Not Implemented) if the method is unrecognized [...] by the origin server."
    ODataExceptionWrapper exceptionWrapper = new ODataExceptionWrapper(req, serviceFactory);
    ODataResponse response =
        exceptionWrapper.wrapInExceptionResponse(new ODataNotImplementedException(messageReference));
//    resp.setStatus(HttpStatusCodes.NOT_IMPLEMENTED.getStatusCode());
    createResponse(resp, response);
  }

  private void createMethodNotAllowedResponse(final HttpServletRequest req, final MessageReference messageReference,
      final HttpServletResponse resp, ODataServiceFactory serviceFactory) throws IOException {
    ODataExceptionWrapper exceptionWrapper = new ODataExceptionWrapper(req, serviceFactory);
    ODataResponse response =
        exceptionWrapper.wrapInExceptionResponse(new ODataMethodNotAllowedException(messageReference));
    createResponse(resp, response);
  }

  private void createNotAcceptableResponse(final HttpServletRequest req, final MessageReference messageReference,
      final HttpServletResponse resp, ODataServiceFactory serviceFactory) throws IOException {
    ODataExceptionWrapper exceptionWrapper = new ODataExceptionWrapper(req, serviceFactory);
    ODataResponse response =
        exceptionWrapper.wrapInExceptionResponse(new ODataNotAcceptableException(messageReference));
    createResponse(resp, response);
  }

  private void createServiceUnavailableResponse(HttpServletRequest req, MessageReference messageReference,
      HttpServletResponse resp, ODataServiceFactory serviceFactory) throws IOException {
    ODataExceptionWrapper exceptionWrapper = new ODataExceptionWrapper(req, serviceFactory);
    ODataResponse response =
        exceptionWrapper.wrapInExceptionResponse(new ODataInternalServerErrorException(messageReference));
    createResponse(resp, response);
  }

}
