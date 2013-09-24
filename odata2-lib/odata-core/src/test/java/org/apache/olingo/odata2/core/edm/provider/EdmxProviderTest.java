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
package org.apache.olingo.odata2.core.edm.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.testutil.mock.EdmTestProvider;
import org.junit.Test;

public class EdmxProviderTest {

  @Test
  public void testEntityType() throws EntityProviderException, ODataException, XMLStreamException {
    Edm edm = createEdm();

    assertNotNull(edm);
    FullQualifiedName fqNameEmployee = new FullQualifiedName("RefScenario", "Employee");
    EdmProvider testProvider = new EdmTestProvider();
    EdmImplProv edmImpl = (EdmImplProv) edm;
    EntityType employee = edmImpl.getEdmProvider().getEntityType(fqNameEmployee);
    EntityType testEmployee = testProvider.getEntityType(fqNameEmployee);
    assertEquals(testEmployee.getName(), employee.getName());
    assertEquals(testEmployee.isHasStream(), employee.isHasStream());
    assertEquals(testEmployee.getProperties().size(), employee.getProperties().size());
    assertEquals(testEmployee.getNavigationProperties().size(), employee.getNavigationProperties().size());

  }

  @Test
  public void testAssociation() throws EntityProviderException, ODataException, XMLStreamException {
    Edm edm = createEdm();
    assertNotNull(edm);

    FullQualifiedName fqNameAssociation = new FullQualifiedName("RefScenario", "BuildingRooms");
    EdmProvider testProvider = new EdmTestProvider();
    EdmImplProv edmImpl = (EdmImplProv) edm;
    Association association = edmImpl.getEdmProvider().getAssociation(fqNameAssociation);
    Association testAssociation = testProvider.getAssociation(fqNameAssociation);
    assertEquals(testAssociation.getName(), association.getName());
    assertEquals(testAssociation.getEnd1().getMultiplicity(), association.getEnd1().getMultiplicity());
    assertEquals(testAssociation.getEnd2().getRole(), association.getEnd2().getRole());
    assertEquals(testAssociation.getEnd1().getType(), association.getEnd1().getType());

  }

  @Test
  public void testAssociationSet() throws EntityProviderException, ODataException, XMLStreamException {
    EdmProvider testProvider = new EdmTestProvider();
    Edm edm = createEdm();
    assertNotNull(edm);

    FullQualifiedName fqNameAssociation = new FullQualifiedName("RefScenario", "ManagerEmployees");
    EdmImplProv edmImpl = (EdmImplProv) edm;
    AssociationSet associationSet =
        edmImpl.getEdmProvider().getAssociationSet("Container1", fqNameAssociation, "Managers", "r_Manager");
    AssociationSet testAssociationSet =
        testProvider.getAssociationSet("Container1", fqNameAssociation, "Managers", "r_Manager");
    assertEquals(testAssociationSet.getName(), associationSet.getName());
    assertEquals(testAssociationSet.getEnd1().getEntitySet(), associationSet.getEnd1().getEntitySet());
    assertEquals(testAssociationSet.getEnd2().getEntitySet(), associationSet.getEnd2().getEntitySet());
    assertEquals(testAssociationSet.getEnd2().getRole(), associationSet.getEnd2().getRole());

  }

  @Test
  public void testSchema() throws EntityProviderException, ODataException, XMLStreamException {
    EdmProvider testProvider = new EdmTestProvider();
    Edm edm = createEdm();
    assertNotNull(edm);

    EdmImplProv edmImpl = (EdmImplProv) edm;
    List<Schema> schemas = edmImpl.getEdmProvider().getSchemas();
    List<Schema> testSchemas = testProvider.getSchemas();
    assertEquals(testSchemas.size(), schemas.size());

    if (!schemas.isEmpty() && !testSchemas.isEmpty()) {
      Schema schema = schemas.get(0);
      Schema testSchema = testSchemas.get(0);
      assertEquals(testSchema.getEntityContainers().size(), schema.getEntityContainers().size());
      assertEquals(testSchema.getEntityTypes().size(), schema.getEntityTypes().size());
      assertEquals(testSchema.getComplexTypes().size(), schema.getComplexTypes().size());
    }
  }

  @Test
  public void testContainer() throws EntityProviderException, ODataException, XMLStreamException {
    EdmProvider testProvider = new EdmTestProvider();
    Edm edm = createEdm();
    assertNotNull(edm);

    EdmImplProv edmImpl = (EdmImplProv) edm;
    EntityContainerInfo container = edmImpl.getEdmProvider().getEntityContainerInfo("Container2");
    EntityContainerInfo testContainer = testProvider.getEntityContainerInfo("Container2");
    assertEquals(testContainer.getName(), container.getName());
    assertEquals(testContainer.isDefaultEntityContainer(), container.isDefaultEntityContainer());

    container = edmImpl.getEdmProvider().getEntityContainerInfo(null);
    testContainer = testProvider.getEntityContainerInfo(null);
    assertNotNull(container);
    assertEquals(testContainer.getName(), container.getName());
    assertEquals(testContainer.isDefaultEntityContainer(), container.isDefaultEntityContainer());
  }

  @Test
  public void testEntitySets() throws EntityProviderException, ODataException {
    Edm edm = createEdm();
    assertNotNull(edm);

    List<EdmEntitySet> entitySets = edm.getEntitySets();
    assertEquals(6, entitySets.size());
  }

  @Test
  public void testFunctionImports() throws EntityProviderException, ODataException {
    Edm edm = createEdm();
    assertNotNull(edm);

    List<EdmFunctionImport> functionImports = edm.getFunctionImports();
    assertEquals(7, functionImports.size());
  }

  private Edm createEdm() throws EntityProviderException, ODataException {
    EdmProvider testProvider = new EdmTestProvider();
    ODataResponse response = EntityProvider.writeMetadata(testProvider.getSchemas(), null);
    InputStream in = (InputStream) response.getEntity();
    return EntityProvider.readMetadata(in, true);

  }

}
