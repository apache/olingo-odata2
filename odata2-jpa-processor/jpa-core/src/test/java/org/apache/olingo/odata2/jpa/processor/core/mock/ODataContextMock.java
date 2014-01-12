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
package org.apache.olingo.odata2.jpa.processor.core.mock;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.easymock.EasyMock;

public class ODataContextMock {

  private ODataService odataService;
  private PathInfo pathInfo;

  public void setODataService(final ODataService service) {
    odataService = service;
  }

  public void setPathInfo(final PathInfo pathInfo) {
    this.pathInfo = pathInfo;
  }

  public ODataContext mock() throws ODataException {
    ODataContext context = EasyMock.createMock(ODataContext.class);
    EasyMock.expect(context.getService()).andReturn(odataService).anyTimes();
    EasyMock.expect(context.getPathInfo()).andReturn(pathInfo).anyTimes();
    EasyMock.replay(context);
    return context;
  }

}
