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
package org.apache.olingo.odata2.core.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataInternalServerErrorException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.ODataContextImpl;
import org.apache.olingo.odata2.core.ODataRequestHandler;

/**
 *  
 */
public final class ODataSubLocator {

  private ODataServiceFactory serviceFactory;
  private ODataRequest request;

  private HttpServletRequest httpRequest;

  @GET
  public Response handleGet() throws ODataException {
    return handle(ODataHttpMethod.GET);
  }

  @PUT
  public Response handlePut() throws ODataException {
    return handle(ODataHttpMethod.PUT);
  }

  @PATCH
  public Response handlePatch() throws ODataException {
    return handle(ODataHttpMethod.PATCH);
  }

  @MERGE
  public Response handleMerge() throws ODataException {
    return handle(ODataHttpMethod.MERGE);
  }

  @DELETE
  public Response handleDelete() throws ODataException {
    return handle(ODataHttpMethod.DELETE);
  }

  @POST
  public Response handlePost(@HeaderParam("X-HTTP-Method") final String xHttpMethod) throws ODataException {
    Response response;

    if (xHttpMethod == null) {
      response = handle(ODataHttpMethod.POST);
    } else {
      /* tunneling */
      if ("MERGE".equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.MERGE);
      } else if ("PATCH".equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.PATCH);
      } else if (HttpMethod.DELETE.equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.DELETE);
      } else if (HttpMethod.PUT.equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.PUT);
      } else if (HttpMethod.GET.equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.GET);
      } else if (HttpMethod.POST.equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.POST);
      } else if (HttpMethod.HEAD.equals(xHttpMethod)) {
        response = handleHead();
      } else if (HttpMethod.OPTIONS.equals(xHttpMethod)) {
        response = handleOptions();
      } else {
        response = returnNotImplementedResponse(ODataNotImplementedException.TUNNELING);
      }
    }
    return response;
  }

  private Response returnNotImplementedResponse(final MessageReference messageReference) {
    // RFC 2616, 5.1.1: "An origin server SHOULD return the status code [...]
    // 501 (Not Implemented) if the method is unrecognized [...] by the origin server."
    return returnException(new ODataNotImplementedException(messageReference));
  }

  private Response returnException(final ODataMessageException messageException) {
    ODataContextImpl context = new ODataContextImpl(request, serviceFactory);
    context.setRequest(request);
    context.setAcceptableLanguages(request.getAcceptableLanguages());
    context.setPathInfo(request.getPathInfo());
    context.setServiceFactory(serviceFactory);
    context.setParameter(ODataContext.HTTP_SERVLET_REQUEST_OBJECT, httpRequest);
    ODataExceptionWrapper exceptionWrapper =
        new ODataExceptionWrapper(context, request.getQueryParameters(), request.getAcceptHeaders());
    ODataResponse response =
        exceptionWrapper.wrapInExceptionResponse(messageException);
    return RestUtil.convertResponse(response);
  }
  
  private Response returnNoServiceResponse(MessageReference messageReference) {
    return returnException(new ODataInternalServerErrorException(messageReference));
  }

  @OPTIONS
  public Response handleOptions() throws ODataException {
    // RFC 2616, 5.1.1: "An origin server SHOULD return the status code [...]
    // 501 (Not Implemented) if the method is unrecognized or not implemented
    // by the origin server."
    return returnNotImplementedResponse(ODataNotImplementedException.COMMON);
  }

  @HEAD
  public Response handleHead() throws ODataException {
    // RFC 2616, 5.1.1: "An origin server SHOULD return the status code [...]
    // 501 (Not Implemented) if the method is unrecognized or not implemented
    // by the origin server."
    return returnNotImplementedResponse(ODataNotImplementedException.COMMON);
  }

  private Response handle(final ODataHttpMethod method) throws ODataException {
    request = ODataRequest.fromRequest(request).method(method).build();

    ODataContextImpl context = new ODataContextImpl(request, serviceFactory);
    context.setParameter(ODataContext.HTTP_SERVLET_REQUEST_OBJECT, httpRequest);

    ODataService service = serviceFactory.createService(context);
    if(service == null){
      return returnNoServiceResponse(ODataInternalServerErrorException.NOSERVICE);
    }
    service.getProcessor().setContext(context);
    context.setService(service);

    ODataRequestHandler requestHandler = new ODataRequestHandler(serviceFactory, service, context);

    final ODataResponse odataResponse = requestHandler.handle(request);
    final Response response = RestUtil.convertResponse(odataResponse);

    return response;
  }



  public static ODataSubLocator create(final SubLocatorParameter param) throws ODataException {
    ODataSubLocator subLocator = new ODataSubLocator();

    subLocator.serviceFactory = param.getServiceFactory();
    subLocator.request = ODataRequest.acceptableLanguages(param.getHttpHeaders().getAcceptableLanguages())
        .acceptHeaders(RestUtil.extractAcceptHeaders(param))
        .body(RestUtil.contentAsStream(RestUtil.extractRequestContent(param)))
        .pathInfo(RestUtil.buildODataPathInfo(param))
        .allQueryParameters(param.getUriInfo().getQueryParameters())
        .requestHeaders(param.getHttpHeaders().getRequestHeaders())
        .contentType(RestUtil.extractRequestContentType(param).toContentTypeString())
        .build();

    subLocator.httpRequest = param.getServletRequest();

    return subLocator;
  }

  private ODataSubLocator() {
    super();
  }
}
