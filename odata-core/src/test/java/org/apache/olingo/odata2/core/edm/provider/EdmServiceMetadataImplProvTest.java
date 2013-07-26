/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.edm.provider;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySetInfo;
import org.apache.olingo.odata2.api.edm.EdmServiceMetadata;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.EdmTestProvider;

/**
 * @author
 */
public class EdmServiceMetadataImplProvTest extends BaseTest {

  private static String metadata;

  @BeforeClass
  public static void setup() throws Exception {
    EdmImplProv edmImplProv = new EdmImplProv(new EdmTestProvider());
    EdmServiceMetadata serviceMetadata = edmImplProv.getServiceMetadata();
    metadata = StringHelper.inputStreamToString(serviceMetadata.getMetadata());
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_EDM_2008_09);
    prefixMap.put("edmx", Edm.NAMESPACE_EDMX_2007_06);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    prefixMap.put("annoPrefix", "http://annoNamespace");

    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
  }

  @Test
  public void getEntitySetInfosForEmptyEdmProvider() throws Exception {
    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmServiceMetadata serviceMetadata = new EdmServiceMetadataImplProv(edmProvider);

    List<EdmEntitySetInfo> infos = serviceMetadata.getEntitySetInfos();
    assertNotNull(infos);
    assertEquals(Collections.emptyList(), infos);
  }

  @Test
  public void getEntitySetInfosForEmptyEdmProviderSchemas() throws Exception {
    List<Schema> schemas = new ArrayList<Schema>();

    EdmProvider edmProvider = mock(EdmProvider.class);
    when(edmProvider.getSchemas()).thenReturn(schemas);

    EdmServiceMetadata serviceMetadata = new EdmServiceMetadataImplProv(edmProvider);

    List<EdmEntitySetInfo> infos = serviceMetadata.getEntitySetInfos();
    assertNotNull(infos);
    assertEquals(Collections.emptyList(), infos);
  }

  @Test
  public void oneEntitySetOneContainerForInfo() throws Exception {
    String entitySetUriString = new URI("Employees").toASCIIString();

    List<EntitySet> entitySets = new ArrayList<EntitySet>();
    EntitySet entitySet = new EntitySet().setName("Employees");
    entitySets.add(entitySet);

    List<EntityContainer> entityContainers = new ArrayList<EntityContainer>();
    EntityContainer container = new EntityContainer().setDefaultEntityContainer(true).setName("Container").setEntitySets(entitySets);
    entityContainers.add(container);

    List<Schema> schemas = new ArrayList<Schema>();
    schemas.add(new Schema().setEntityContainers(entityContainers));

    EdmProvider edmProvider = mock(EdmProvider.class);
    when(edmProvider.getSchemas()).thenReturn(schemas);

    EdmServiceMetadata serviceMetadata = new EdmServiceMetadataImplProv(edmProvider);

    List<EdmEntitySetInfo> infos = serviceMetadata.getEntitySetInfos();
    assertNotNull(infos);
    assertEquals(1, infos.size());

    assertEquals(infos.get(0).getEntitySetName(), "Employees");
    assertEquals(infos.get(0).getEntityContainerName(), "Container");
    assertEquals(infos.get(0).getEntitySetUri().toASCIIString(), entitySetUriString);
    assertTrue(infos.get(0).isDefaultEntityContainer());
  }

  @Test
  public void twoEntitySetsOneContainerForInfo() throws Exception {
    List<EntitySet> entitySets = new ArrayList<EntitySet>();
    EntitySet entitySet = new EntitySet().setName("Employees");
    entitySets.add(entitySet);
    entitySets.add(entitySet);

    List<EntityContainer> entityContainers = new ArrayList<EntityContainer>();
    EntityContainer container = new EntityContainer().setDefaultEntityContainer(true).setName("Container").setEntitySets(entitySets);
    entityContainers.add(container);

    List<Schema> schemas = new ArrayList<Schema>();
    schemas.add(new Schema().setEntityContainers(entityContainers));

    EdmProvider edmProvider = mock(EdmProvider.class);
    when(edmProvider.getSchemas()).thenReturn(schemas);

    EdmServiceMetadata serviceMetadata = new EdmServiceMetadataImplProv(edmProvider);

    List<EdmEntitySetInfo> infos = serviceMetadata.getEntitySetInfos();
    assertNotNull(infos);
    assertEquals(2, infos.size());
  }

  @Test
  public void twoContainersWithOneEntitySetEachForInfo() throws Exception {
    String entitySetUriString = new URI("Employees").toASCIIString();
    String entitySetUriString2 = new URI("Container2.Employees").toASCIIString();

    List<EntitySet> entitySets = new ArrayList<EntitySet>();
    EntitySet entitySet = new EntitySet().setName("Employees");
    entitySets.add(entitySet);

    List<EntityContainer> entityContainers = new ArrayList<EntityContainer>();
    EntityContainer container = new EntityContainer().setDefaultEntityContainer(true).setName("Container").setEntitySets(entitySets);
    entityContainers.add(container);

    EntityContainer container2 = new EntityContainer().setDefaultEntityContainer(false).setName("Container2").setEntitySets(entitySets);
    entityContainers.add(container2);

    List<Schema> schemas = new ArrayList<Schema>();
    schemas.add(new Schema().setEntityContainers(entityContainers));

    EdmProvider edmProvider = mock(EdmProvider.class);
    when(edmProvider.getSchemas()).thenReturn(schemas);

    EdmServiceMetadata serviceMetadata = new EdmServiceMetadataImplProv(edmProvider);

    List<EdmEntitySetInfo> infos = serviceMetadata.getEntitySetInfos();
    assertNotNull(infos);
    assertEquals(2, infos.size());

    assertEquals(infos.get(0).getEntitySetName(), "Employees");
    assertEquals(infos.get(0).getEntityContainerName(), "Container");
    assertEquals(infos.get(0).getEntitySetUri().toASCIIString(), entitySetUriString);
    assertTrue(infos.get(0).isDefaultEntityContainer());

    assertEquals(infos.get(1).getEntitySetName(), "Employees");
    assertEquals(infos.get(1).getEntityContainerName(), "Container2");
    assertEquals(infos.get(1).getEntitySetUri().toASCIIString(), entitySetUriString2);
    assertFalse(infos.get(1).isDefaultEntityContainer());
  }

  @Test
  public void oneEntitySetsOneContainerTwoSchemadForInfo() throws Exception {
    List<EntitySet> entitySets = new ArrayList<EntitySet>();
    EntitySet entitySet = new EntitySet().setName("Employees");
    entitySets.add(entitySet);

    List<EntityContainer> entityContainers = new ArrayList<EntityContainer>();
    EntityContainer container = new EntityContainer().setDefaultEntityContainer(true).setName("Container").setEntitySets(entitySets);
    entityContainers.add(container);

    List<Schema> schemas = new ArrayList<Schema>();
    schemas.add(new Schema().setEntityContainers(entityContainers));
    schemas.add(new Schema().setEntityContainers(entityContainers));

    EdmProvider edmProvider = mock(EdmProvider.class);
    when(edmProvider.getSchemas()).thenReturn(schemas);

    EdmServiceMetadata serviceMetadata = new EdmServiceMetadataImplProv(edmProvider);

    List<EdmEntitySetInfo> infos = serviceMetadata.getEntitySetInfos();
    assertNotNull(infos);
    assertEquals(2, infos.size());
  }

  @Test
  public void dataServiceVersion() throws Exception {
    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    EdmServiceMetadata serviceMetadata = edmImplProv.getServiceMetadata();
    assertEquals("1.0", serviceMetadata.getDataServiceVersion());
  }

  @Test
  public void testSchemaStructure() throws Exception {
    assertXpathExists("/edmx:Edmx", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:ComplexType", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:Association", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityContainer", metadata);
  }

  @Test
  public void testEntityTypeStructure() throws Exception {
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType[@Name and @m:HasStream]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType[@Name and @BaseType and @m:HasStream]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType/a:Key", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType/a:Key/a:PropertyRef[@Name]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType/a:Property[@Name and @Type]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType/a:Property[@Name and @Type and @Nullable and @m:FC_TargetPath]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType/a:NavigationProperty[@Name and @Relationship and @FromRole and @ToRole]", metadata);
  }

  @Test
  public void testAnnotations() throws Exception {
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType/a:Property[@Name and @Type and @Nullable and @annoPrefix:annoName]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType/a:Property[@Name and @Type and @m:FC_TargetPath and @annoPrefix:annoName]", metadata);
  }

  @Test
  public void testComplexTypeStructure() throws Exception {
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:ComplexType[@Name]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:ComplexType/a:Property[@Name and @Type]", metadata);
  }

  @Test
  public void testEntityContainerStructure() throws Exception {
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityContainer[@Name]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityContainer/a:EntitySet[@Name and @EntityType]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityContainer/a:AssociationSet[@Name and @Association]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityContainer//a:AssociationSet/a:End[@EntitySet and @Role]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityContainer/a:FunctionImport[@Name and @ReturnType and @EntitySet and @m:HttpMethod]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityContainer/a:FunctionImport/a:Parameter[@Name and @Type]", metadata);
  }

  @Test
  public void testAssociationStructure() throws Exception {
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:Association[@Name]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:Association/a:End[@Type and @Multiplicity and @Role]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:Association/a:ReferentialConstraint/a:Principal[@Role]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:Association/a:ReferentialConstraint/a:Principal[@Role]/a:PropertyRef[@Name]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:Association/a:ReferentialConstraint/a:Dependent[@Role]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:Association/a:ReferentialConstraint/a:Dependent[@Role]/a:PropertyRef[@Name]", metadata);
  }

  @Test
  public void testRefScenarioContent() throws Exception {
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType[@Name='Employee']", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType[@Name='Base']", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType[@Name='Team']", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType[@Name='Room']", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType[@Name='Building']", metadata);
  }
}
