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

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import org.apache.olingo.odata2.api.ODataServiceFactory;
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
public class ODataRootLocator extends
    org.apache.olingo.odata2.core.rest.spring.ODataRootLocator {

  @Context
  private ServletConfig servletConfig;

  @Override
  public ODataServiceFactory getServiceFactory() {
    return createServiceFactoryFromContext(app, servletRequest,
        servletConfig);
  }

  @Override
  public void setServiceFactory(ODataServiceFactory serviceFactory) {
    // Don't do anything
  }

  @Override
  public int getPathSplit() {
    int pathSplit = 0;
    final String pathSplitAsString = servletConfig
        .getInitParameter(ODataServiceFactory.PATH_SPLIT_LABEL);
    if (pathSplitAsString != null) {
      pathSplit = Integer.parseInt(pathSplitAsString);
    }
    return pathSplit;
  }

  @Override
  public void setPathSplit(int pathSplit) {
    // Don't do anything
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
      ODataServiceFactory serviceFactory = (ODataServiceFactory) factoryClass.newInstance();
      return serviceFactory;
    } catch (Exception e) {
      throw new ODataRuntimeException("Exception during ODataServiceFactory creation occured.", e);
    }
  }
}
