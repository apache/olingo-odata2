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

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Request;

import org.apache.olingo.odata2.api.ODataServiceFactory;

/**
 *  
 */
public class SubLocatorParameter {

  private List<jakarta.ws.rs.core.PathSegment> pathSegments;
  private jakarta.ws.rs.core.HttpHeaders httpHeaders;
  private jakarta.ws.rs.core.UriInfo uriInfo;
  private Request request;
  private int pathSplit;
  private ODataServiceFactory serviceFactory;
  private HttpServletRequest servletRequest;

  public ODataServiceFactory getServiceFactory() {
    return serviceFactory;
  }

  public void setServiceFactory(final ODataServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  public List<jakarta.ws.rs.core.PathSegment> getPathSegments() {
    return pathSegments;
  }

  public void setPathSegments(final List<jakarta.ws.rs.core.PathSegment> pathSegments) {
    this.pathSegments = pathSegments;
  }

  public jakarta.ws.rs.core.HttpHeaders getHttpHeaders() {
    return httpHeaders;
  }

  public void setHttpHeaders(final jakarta.ws.rs.core.HttpHeaders httpHeaders) {
    this.httpHeaders = httpHeaders;
  }

  public jakarta.ws.rs.core.UriInfo getUriInfo() {
    return uriInfo;
  }

  public void setUriInfo(final jakarta.ws.rs.core.UriInfo uriInfo) {
    this.uriInfo = uriInfo;
  }

  public Request getRequest() {
    return request;
  }

  public void setRequest(final Request request) {
    this.request = request;
  }

  public int getPathSplit() {
    return pathSplit;
  }

  public void setPathSplit(final int pathSplit) {
    this.pathSplit = pathSplit;
  }

  public void setServletRequest(final HttpServletRequest servletRequest) {
    this.servletRequest = servletRequest;
  }

  public HttpServletRequest getServletRequest() {
    return servletRequest;
  }
}
