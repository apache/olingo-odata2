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

import java.util.List;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmServiceMetadata;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.EdmMockUtilV2;
import org.easymock.EasyMock;

public class ODataServiceMock {

  private Edm edmMock = null;
  public static final String SERVICE_ROOT = "http://apache.odata.org/OData.svc/";

  public ODataService mock() throws ODataException {
    ODataService odataService = EasyMock.createMock(ODataService.class);
    EasyMock.expect(odataService.getEntityDataModel()).andReturn(mockEdm());
    EasyMock.replay(odataService);
    return odataService;

  }

  private Edm mockEdm() {
    if (edmMock == null) {
      edmMock = new EdmMock();
    }
    return edmMock;
  }

  public static class EdmMock implements Edm {

    @Override
    public EdmEntityContainer getEntityContainer(final String name) throws EdmException {
      return EdmMockUtilV2.mockEdmEntityContainer(name);
    }

    @Override
    public EdmEntityType getEntityType(final String namespace, final String name) throws EdmException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public EdmComplexType getComplexType(final String namespace, final String name) throws EdmException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public EdmAssociation getAssociation(final String namespace, final String name) throws EdmException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public EdmServiceMetadata getServiceMetadata() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public EdmEntityContainer getDefaultEntityContainer() throws EdmException {
      return EdmMockUtilV2.mockEdmEntityContainer(null);
    }

    @Override
    public List<EdmEntitySet> getEntitySets() throws EdmException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<EdmFunctionImport> getFunctionImports() throws EdmException {
      // TODO Auto-generated method stub
      return null;
    }

  }
}
