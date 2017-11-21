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

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class ODataServlet extends HttpServlet {

  private static final String HTTP_METHOD_OPTIONS = "OPTIONS";
  private static final String HTTP_METHOD_HEAD = "HEAD";

  /**
   * Label used in web.xml to assign servlet init parameter for a path split (service resolution).
   */
  private static final String BUFFER_SIZE = "org.apache.olingo.odata2.core.servlet.buffer.size";

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final int DEFAULT_BUFFER_SIZE = 32768;
  private static final String DEFAULT_READ_CHARSET = "utf-8";

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    // We have to create the Service Factory here because otherwise we do not have access to the error callback
    ODataServiceFactory serviceFactory = getServiceFactory(req);
    if(serviceFactory == null) {
      throw new ODataRuntimeException("Unable to get Service Factory. Check either '" +
          ODataServiceFactory.FACTORY_LABEL + "' or '" + ODataServiceFactory.FACTORY_INSTANCE_LABEL + "' config.");
    }

    String xHttpMethod = req.getHeader("X-HTTP-Method");
    String xHttpMethodOverride = req.getHeader("X-HTTP-Method-Override");
    if (xHttpMethod != null && xHttpMethodOverride != null) {
      if (!xHttpMethod.equalsIgnoreCase(xHttpMethodOverride)) {
        ODataExceptionWrapper wrapper = new ODataExceptionWrapper(req, serviceFactory);
        createResponse(resp, wrapper.wrapInExceptionResponse(
            new ODataBadRequestException(ODataBadRequestException.AMBIGUOUS_XMETHOD)));
        return;
      }
    }

    if (req.getPathInfo() != null) {
      handle(req, resp, xHttpMethod, xHttpMethodOverride, serviceFactory);
    } else {
      handleRedirect(req, resp, serviceFactory);
    }
  }

  /**
   * Get the service factory instance which is used for creation of the
   * <code>ODataService</code> which handles the processing of the request.
   *
   * @param request the http request which is processed as an OData request
   * @return an instance of an ODataServiceFactory
   */
  protected ODataServiceFactory getServiceFactory(HttpServletRequest request) {
    try {
      ODataServiceFactory factoryInstance = getODataServiceFactoryInstance(request);
      if(factoryInstance == null) {
        return createODataServiceFactory(request);
      }
      return factoryInstance;

    } catch (Exception e) {
      throw new ODataRuntimeException(e);
    }
  }

  protected void handle(final HttpServletRequest req, final HttpServletResponse resp, final String xHttpMethod,
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
    } else if (HTTP_METHOD_HEAD.equals(method)) {
      handleRequest(req, ODataHttpMethod.GET, resp, serviceFactory);
    } else if (HTTP_METHOD_OPTIONS.equals(method)) {
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
    } else if (HTTP_METHOD_HEAD.equals(xHttpMethod)) {
      handleRequest(req, ODataHttpMethod.GET, resp, serviceFactory);
    } else if (ODataHttpMethod.POST.name().equals(xHttpMethod)) {
      handleRequest(req, ODataHttpMethod.POST, resp, serviceFactory);
    } else if (HTTP_METHOD_OPTIONS.equals(xHttpMethod)) {
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
      final String formEncoding = getInitParameter(ODataServiceFactory.ACCEPT_FORM_ENCODING);
      int pathSplit = 0;
      if (pathSplitAsString != null) {
        pathSplit = Integer.parseInt(pathSplitAsString);
      }
      if (req.getHeader(HttpHeaders.ACCEPT) != null && req.getHeader(HttpHeaders.ACCEPT).isEmpty()) {
        createNotAcceptableResponse(req, ODataNotAcceptableException.COMMON, resp, serviceFactory);
        return;
      }
      ODataRequest odataRequest;
      try {
        odataRequest = ODataRequest.method(method)
          .httpMethod(req.getMethod())
          .contentType(RestUtil.extractRequestContentType(req.getContentType()).toContentTypeString())
          .acceptHeaders(RestUtil.extractAcceptHeaders(req.getHeader(HttpHeaders.ACCEPT)))
          .acceptableLanguages(RestUtil.extractAcceptableLanguage(req.getHeader(HttpHeaders.ACCEPT_LANGUAGE)))
          .pathInfo(RestUtil.buildODataPathInfo(req, pathSplit))
          .allQueryParameters(RestUtil.extractAllQueryParameters(req.getQueryString(), formEncoding))
          .requestHeaders(RestUtil.extractHeaders(req))
          .body(req.getInputStream())
          .build();
      } catch (IllegalArgumentException e) {
        throw new ODataBadRequestException(ODataBadRequestException.INVALID_REQUEST, e);
      }

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
        //
        boolean omitResponseBody = HTTP_METHOD_HEAD.equals(req.getMethod());
        createResponse(resp, odataResponse, omitResponseBody);
      }
    } catch (Exception e) {
      ODataExceptionWrapper wrapper = new ODataExceptionWrapper(req, serviceFactory);
      createResponse(resp, wrapper.wrapInExceptionResponse(e));
    }
  }

  protected void handleRedirect(final HttpServletRequest req, final HttpServletResponse resp,
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

  protected void createResponse(final HttpServletResponse resp, final ODataResponse response) throws IOException {
    createResponse(resp, response, false);
  }

  protected void createResponse(final HttpServletResponse resp, final ODataResponse response,
                                final boolean omitResponseBody)
      throws IOException {

    resp.setStatus(response.getStatus().getStatusCode());
    resp.setContentType(response.getContentHeader());
    for (String headerName : response.getHeaderNames()) {
      resp.setHeader(headerName, response.getHeader(headerName));
    }

    if(omitResponseBody) {
      return;
    }

    Object entity = response.getEntity();
    if (entity != null) {
      ServletOutputStream out = resp.getOutputStream();
      int contentLength;

      if (entity instanceof InputStream) {
        contentLength = handleStream((InputStream) entity, out);
      } else if (entity instanceof String) {
        String body = (String) entity;
        final byte[] entityBytes = body.getBytes(DEFAULT_READ_CHARSET);
        out.write(entityBytes);
        contentLength = entityBytes.length;
      } else {
        throw new IOException("Illegal entity object in ODataResponse of type '" + entity.getClass() + "'.");
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

  private int handleStream(InputStream stream, ServletOutputStream out) throws IOException {
    int contentLength = 0;
    byte[] buffer = getBuffer();

    try {
      int len;
      while ((len = stream.read(buffer)) != -1) {
        contentLength += len;
        out.write(buffer, 0, len);
      }
    } finally {
      stream.close();
    }
    return contentLength;
  }

  private byte[] getBuffer() {
    int bufferSize = DEFAULT_BUFFER_SIZE;
    String bufSizeInit = getInitParameter(BUFFER_SIZE);
    if(bufSizeInit != null) {
      try {
        bufferSize = Integer.parseInt(bufSizeInit);
        if(bufferSize <= 0) {
          bufferSize = DEFAULT_BUFFER_SIZE;
        }
      } catch (NumberFormatException ignored) {
        // this exception is ignored because if parameter is not parse able the default is used
      }
    }

    return new byte[bufferSize];
  }

  private void createNotImplementedResponse(final HttpServletRequest req, final MessageReference messageReference,
      final HttpServletResponse resp, ODataServiceFactory serviceFactory) throws IOException {
    // RFC 2616, 5.1.1: "An origin server SHOULD return the status code [...]
    // 501 (Not Implemented) if the method is unrecognized [...] by the origin server."
    ODataExceptionWrapper exceptionWrapper = new ODataExceptionWrapper(req, serviceFactory);
    ODataResponse response =
        exceptionWrapper.wrapInExceptionResponse(new ODataNotImplementedException(messageReference));
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

  /**
   * Create an instance of a ODataServiceFactory via factory class
   * from servlet init parameter ODataServiceFactory.FACTORY_LABEL
   * and ODataServiceFactory.FACTORY_CLASSLOADER_LABEL (if set).
   *
   * @see ODataServiceFactory#FACTORY_LABEL
   * @see ODataServiceFactory#FACTORY_CLASSLOADER_LABEL
   *
   * @param req http servlet request
   * @return instance of a ODataServiceFactory
   */
  private ODataServiceFactory createODataServiceFactory(HttpServletRequest req)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    final String factoryClassName = getInitParameter(ODataServiceFactory.FACTORY_LABEL);
    if(factoryClassName == null) {
      return null;
    }

    ClassLoader cl = (ClassLoader) req.getAttribute(ODataServiceFactory.FACTORY_CLASSLOADER_LABEL);
    if (cl == null) {
      return (ODataServiceFactory) Class.forName(factoryClassName).newInstance();
    } else {
      return (ODataServiceFactory) Class.forName(factoryClassName, true, cl).newInstance();
    }
  }

  /**
   * Get an instance of a ODataServiceFactory from request attribute
   * ODataServiceFactory.FACTORY_INSTANCE_LABEL
   *
   * @see ODataServiceFactory#FACTORY_INSTANCE_LABEL
   *
   * @param req http servlet request
   * @return instance of a ODataServiceFactory
   */
  private ODataServiceFactory getODataServiceFactoryInstance(HttpServletRequest req) {
    Object factory = req.getAttribute(ODataServiceFactory.FACTORY_INSTANCE_LABEL);
    if(factory == null) {
      return null;
    } else if(factory instanceof ODataServiceFactory) {
      return (ODataServiceFactory) factory;
    }
    throw new ODataRuntimeException("Invalid service factory instance of type " + factory.getClass());
  }
}
