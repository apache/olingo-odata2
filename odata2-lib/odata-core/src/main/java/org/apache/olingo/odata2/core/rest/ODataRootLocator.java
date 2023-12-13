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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.Encoded;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;
import org.apache.olingo.odata2.core.rest.app.AbstractODataApplication;

/**
 * Default OData root locator responsible to handle the whole path and delegate all calls to a sub locator:<p>
 * <code>/{odata path} e.g. http://host:port/webapp/odata.svc/$metadata</code><br>
 * All path segments defined by a servlet mapping belong to the odata uri.
 * </p>
 * This behavior can be changed:<p>
 * <code>/{custom path}{odata path} e.g. http://host:port/webapp/bmw/odata.svc/$metadata</code><br>
 * The first segment defined by a servlet mapping belong to customer context and the following segments are OData
 * specific.
 * </p>
 *
 */
@Path("/")
public class ODataRootLocator {

  @Context
  private HttpHeaders httpHeaders;
  @Context
  private UriInfo uriInfo;
  @Context
  private Request request;
  @Context
  private ServletConfig servletConfig;
  @Context
  private HttpServletRequest servletRequest;

  @Context
  private Application app;

  /**
   * Default root behavior which will delegate all paths to a ODataLocator.
   * @param pathSegments URI path segments - all segments have to be OData
   * @param xHttpMethod HTTP Header X-HTTP-Method for tunneling through POST
   * @param xHttpMethodOverride HTTP Header X-HTTP-Method-Override for tunneling through POST
   * @return a locator handling OData protocol
   * @throws ODataException
   * @throws ClassNotFoundException
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  @Path("/{pathSegments: .*}")
  public Object handleRequest(
      @Encoded @PathParam("pathSegments") final List<PathSegment> pathSegments,
      @HeaderParam("X-HTTP-Method") final String xHttpMethod,
      @HeaderParam("X-HTTP-Method-Override") final String xHttpMethodOverride)
      throws ODataException, ClassNotFoundException, InstantiationException, IllegalAccessException {

    if (xHttpMethod != null && xHttpMethodOverride != null) {

      /*
       * X-HTTP-Method-Override : implemented by CXF
       * X-HTTP-Method : implemented in ODataSubLocator:handlePost
       */

      if (!xHttpMethod.equalsIgnoreCase(xHttpMethodOverride)) {
        throw new ODataBadRequestException(ODataBadRequestException.AMBIGUOUS_XMETHOD);
      }
    }

    if (servletRequest.getPathInfo() == null) {
      return handleRedirect();
    }

    ODataServiceFactory serviceFactory = getServiceFactory();

    int pathSplit = getPathSplit();

    final SubLocatorParameter param = new SubLocatorParameter();
    param.setServiceFactory(serviceFactory);
    param.setPathSegments(pathSegments);
    param.setHttpHeaders(httpHeaders);
    param.setUriInfo(uriInfo);
    param.setRequest(request);
    param.setServletRequest(servletRequest);
    param.setPathSplit(pathSplit);

    return ODataSubLocator.create(param);
  }

  public ODataServiceFactory getServiceFactory() {
    return createServiceFactoryFromContext(app, servletRequest, servletConfig);
  }

  public int getPathSplit() {
    int pathSplit = 0;
    final String pathSplitAsString = servletConfig.getInitParameter(ODataServiceFactory.PATH_SPLIT_LABEL);
    if (pathSplitAsString != null) {
      pathSplit = Integer.parseInt(pathSplitAsString);
    }
    return pathSplit;
  }

  public static ODataServiceFactory createServiceFactoryFromContext(final Application app,
      final HttpServletRequest servletRequest,
      final ServletConfig servletConfig) {
    try {
      Class<?> factoryClass;
      if (app instanceof AbstractODataApplication) {
        factoryClass = ((AbstractODataApplication) app).getServiceFactoryClass();
      } else {
        final String factoryClassName = servletConfig.getInitParameter(ODataServiceFactory.FACTORY_LABEL);
        if (factoryClassName == null) {
          throw new ODataRuntimeException("Servlet config missing: " + ODataServiceFactory.FACTORY_LABEL);
        }

        ClassLoader cl = (ClassLoader) servletRequest.getAttribute(ODataServiceFactory.FACTORY_CLASSLOADER_LABEL);
        if (cl == null) {
          factoryClass = Class.forName(factoryClassName);
        } else {
          factoryClass = Class.forName(factoryClassName, true, cl);
        }
      }
      return (ODataServiceFactory) factoryClass.newInstance();
    } catch (Exception e) {
      throw new ODataRuntimeException("Exception during ODataServiceFactory creation occured.", e);
    }
  }

  private Object handleRedirect() {
    return new ODataRedirectLocator();
  }
}
