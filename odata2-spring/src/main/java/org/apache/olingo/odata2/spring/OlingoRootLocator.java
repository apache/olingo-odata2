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
package org.apache.olingo.odata2.spring;

import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.core.rest.ODataRootLocator;

import javax.ws.rs.Path;

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
public class OlingoRootLocator extends ODataRootLocator {

  // These next two members are exposed so that they can be injected with Spring
  private ODataServiceFactory serviceFactory;
  private int pathSplit = 0;

  @Override
  public ODataServiceFactory getServiceFactory() {
    return serviceFactory;
  }

  public void setServiceFactory(ODataServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  @Override
  public int getPathSplit() {
    return pathSplit;
  }

  public void setPathSplit(int pathSplit) {
    this.pathSplit = pathSplit;
  }
}
