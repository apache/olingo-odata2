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
package org.apache.olingo.odata2.client.core.ep.serializer;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmConcurrencyMode;
import org.apache.olingo.odata2.api.edm.EdmCustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmTargetPath;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.client.api.edm.ClientEdm;
import org.apache.olingo.odata2.client.api.edm.EdmDataServices;
import org.apache.olingo.odata2.client.api.ep.Entity;
import org.apache.olingo.odata2.client.api.ep.EntityCollection;
import org.apache.olingo.odata2.client.api.ep.EntitySerializerProperties;
import org.apache.olingo.odata2.client.core.ep.AbstractProviderTest;
import org.apache.olingo.odata2.client.core.ep.AtomSerializerDeserializer;
import org.apache.olingo.odata2.client.core.ep.deserializer.XmlMetadataDeserializer;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.helper.XMLUnitHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import junit.framework.Assert;

public class AtomEntrySerializerTest extends AbstractProviderTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  private String buildingXPathString = "/a:entry/a:link[@href=\"Rooms('1')/nr_Building\" and @title='nr_Building']";  

  private String productXPathString = "/a:entry/a:link[@href=\"A_Product('CRPROD2')/to_Description\" "
      + "and @title='to_Description']";
  
  public AtomEntrySerializerTest(final StreamWriterImplType type) {
    super(type);
  }

  @Test
  public void contentOnly() throws Exception {
    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(false).build();

    AtomSerializerDeserializer ser = createAtomEntityProvider();
    employeeData.setWriteProperties(properties);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData);
    String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);

    assertXpathNotExists("/a:entry[@m:etag]", xmlString);
    assertXpathNotExists("/a:entry/a:id", xmlString);
    assertXpathNotExists("/a:entry/a:title", xmlString);
    assertXpathNotExists("/a:entry/a:updated", xmlString);
    assertXpathNotExists("/a:entry/a:category", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"ne_Team\"and @href=\"Employees('1')/ne_Team\"]", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"ne_Room\"and @href=\"Employees('1')/ne_Room\"]", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"ne_Manager\" and @href=\"Employees('1')/ne_Manager\"]", xmlString);
    assertXpathNotExists("/a:entry/a:content", xmlString);

    assertXpathExists("/a:entry/m:properties", xmlString);
  }

  @Test
  public void emptyRoomWithProperty() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(false)
            .build();

    Entity localRoomData = new Entity();
    localRoomData.setWriteProperties(properties);
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(entitySet, localRoomData);
    String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathNotExists("/a:entry[@m:etag]", xmlString);

    assertXpathNotExists("/a:entry/a:id", xmlString);
    assertXpathNotExists("/a:entry/a:title", xmlString);
    assertXpathNotExists("/a:entry/a:updated", xmlString);
    assertXpathNotExists("/a:entry/a:category", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"nr_Employees\"and @href=\"Rooms('1')/nr_Employees\"]", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"nr_Building\"and @href=\"Rooms('1')/nr_Building\"]", xmlString);

    assertXpathNotExists("/a:entry/a:content/m:properties/d:Name", xmlString);
  }

  @Test
  public void emptyRoomWithoutProperty() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    expectedEx.expect(EntityProviderException.class);
    expectedEx.expectMessage("Write properties are mandatory for XML.");
    Entity localRoomData = new Entity();
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ser.writeEntry(entitySet, localRoomData);
 }

  @Test
  public void nullRoom() throws Exception {
    expectedEx.expect(EntityProviderException.class);
    expectedEx.expectMessage("Entity or expanded entity cannot have null value.");
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ser.writeEntry(entitySet, null);  
  }

  @Test
  public void contentOnlyRoom() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(false)
            .build();

    Entity localRoomData = new Entity();
    localRoomData.addProperty("Name", "Neu Schwanstein");
    localRoomData.setWriteProperties(properties);
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(entitySet, localRoomData);
    String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathNotExists("/a:entry[@m:etag]", xmlString);

    assertXpathNotExists("/a:entry/a:id", xmlString);
    assertXpathNotExists("/a:entry/a:title", xmlString);
    assertXpathNotExists("/a:entry/a:updated", xmlString);
    assertXpathNotExists("/a:entry/a:category", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"nr_Employees\"and @href=\"Rooms('1')/nr_Employees\"]", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"nr_Building\"and @href=\"Rooms('1')/nr_Building\"]", xmlString);

    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
  }

  @Test(expected = EntityProviderException.class)
  public void contentOnlyRoomEmptyNullNavigationLinks() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(true)
            .build();

    Entity localRoomData = new Entity();
    localRoomData.addProperty("Name", "Neu Schwanstein");
    localRoomData.setWriteProperties(properties);
    localRoomData.addNavigation("nr_Employees", null);
    localRoomData.addNavigation("nr_Building", new Entity());
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(entitySet, localRoomData);
    String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathNotExists("/a:entry[@m:etag]", xmlString);

    assertXpathNotExists("/a:entry/a:id", xmlString);
    assertXpathNotExists("/a:entry/a:title", xmlString);
    assertXpathNotExists("/a:entry/a:updated", xmlString);
    assertXpathNotExists("/a:entry/a:category", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"nr_Employees\"and @href=\"Rooms('1')/nr_Employees\"]", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"nr_Building\"and @href=\"Rooms('1')/nr_Building\"]", xmlString);

    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
  }

  @Test
  public void contentOnlyRoomWithNavigationLink() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity buildingLink = new Entity();
    buildingLink.addProperty("Id", "1");
    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(false)
            .build();

    Entity localRoomData = new Entity();
    localRoomData.addProperty("Name", "Neu Schwanstein");
    localRoomData.addNavigation("nr_Building", buildingLink.getProperties());
    localRoomData.setWriteProperties(properties);
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(entitySet, localRoomData);
    String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathNotExists("/a:entry[@m:etag]", xmlString);

    assertXpathNotExists("/a:entry/a:id", xmlString);
    assertXpathNotExists("/a:entry/a:title", xmlString);
    assertXpathNotExists("/a:entry/a:updated", xmlString);
    assertXpathNotExists("/a:entry/a:category", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"nr_Employees\"and @href=\"Rooms('1')/nr_Employees\"]", xmlString);

    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:entry/a:link[@title=\"nr_Building\"and @href=\"Buildings('1')\"]", xmlString);
  }

  @Test(expected=EntityProviderException.class)
  public void contentOnlyRoomWithNavigationContent() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity buildingLink = new Entity();
    buildingLink.addProperty("Id", "1");
    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(true)
            .build();

    Entity localRoomData = new Entity();
    localRoomData.addProperty("Name", "Neu Schwanstein");
    localRoomData.addNavigation("nr_Building", buildingLink);
    localRoomData.setWriteProperties(properties);
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(entitySet, localRoomData);
    String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathNotExists("/a:entry[@m:etag]", xmlString);

    assertXpathNotExists("/a:entry/a:id", xmlString);
    assertXpathNotExists("/a:entry/a:title", xmlString);
    assertXpathNotExists("/a:entry/a:updated", xmlString);
    assertXpathNotExists("/a:entry/a:category", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"nr_Employees\"and @href=\"Rooms('1')/nr_Employees\"]", xmlString);

    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"nr_Building\"and @href=\"Buildings('1')\"]", xmlString);
  }
  
  @Test
  public void contentOnlyRoomWithNavigationContentWithId() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Entity buildingLink = new Entity();
    buildingLink.addProperty("Id", "1");
    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(false)
            .build();

    Entity localRoomData = new Entity();
    localRoomData.addProperty("Id", "1");
    localRoomData.addProperty("Name", "Neu Schwanstein");
    localRoomData.addNavigation("nr_Building", buildingLink);
    localRoomData.setWriteProperties(properties);
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(entitySet, localRoomData);
    String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathNotExists("/a:entry[@m:etag]", xmlString);

    assertXpathNotExists("/a:entry/a:id", xmlString);
    assertXpathNotExists("/a:entry/a:title", xmlString);
    assertXpathNotExists("/a:entry/a:updated", xmlString);
    assertXpathNotExists("/a:entry/a:category", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"nr_Employees\"and @href=\"Rooms('1')/nr_Employees\"]", xmlString);

    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"nr_Building\"and @href=\"Buildings('1')\"]", xmlString);
  }

  @Test
  public void contentOnlyWithoutKey() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("ManagerId");

    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(false)
            .build();

    Entity localEmployeeData = new Entity();
    localEmployeeData.addProperty("ManagerId", "1");
    localEmployeeData.setWriteProperties(properties);

    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(entitySet, localEmployeeData);
    String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathNotExists("/a:entry[@m:etag]", xmlString);

    assertXpathNotExists("/a:entry/a:id", xmlString);
    assertXpathNotExists("/a:entry/a:title", xmlString);
    assertXpathNotExists("/a:entry/a:updated", xmlString);
    assertXpathNotExists("/a:entry/a:category", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"ne_Manager\"]", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"ne_Team\"]", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"ne_Room\"]", xmlString);
    assertXpathNotExists("/a:entry/a:content", xmlString);

    assertXpathExists("/a:entry/m:properties", xmlString);
    assertXpathNotExists("/a:entry/m:properties/d:EmployeeId", xmlString);
    assertXpathExists("/a:entry/m:properties/d:ManagerId", xmlString);
  }

  
  @Test
  public void contentOnlyWithNavigationLink() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("ManagerId");
    Entity managerLink = new Entity();
    managerLink.addProperty("EmployeeId", "1");
    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(false)
            .build();

    Entity localEmployeeData = new Entity();
    localEmployeeData.addProperty("ManagerId", "1");
    localEmployeeData.addNavigation("ne_Manager", managerLink.getProperties());
    localEmployeeData.setWriteProperties(properties);
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(entitySet, localEmployeeData);
    String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathNotExists("/a:entry[@m:etag]", xmlString);

    assertXpathNotExists("/a:entry/a:id", xmlString);
    assertXpathNotExists("/a:entry/a:title", xmlString);
    assertXpathNotExists("/a:entry/a:updated", xmlString);
    assertXpathNotExists("/a:entry/a:category", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"ne_Team\"]", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title=\"ne_Room\"]", xmlString);
    assertXpathNotExists("/a:entry/a:content", xmlString);

    assertXpathExists("/a:entry/m:properties", xmlString);
    assertXpathNotExists("/a:entry/m:properties/d:EmployeeId", xmlString);
    assertXpathExists("/a:entry/m:properties/d:ManagerId", xmlString);

    assertXpathExists("/a:entry/a:link[@href=\"Managers('1')\" and @title=\"ne_Manager\"]", xmlString);
  }

  @Test
  public void noneSyndicationKeepInContentFalseMustNotShowInProperties() throws Exception {
    // prepare Mock
    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmCustomizableFeedMappings employeeCustomPropertyMapping = mock(EdmCustomizableFeedMappings.class);
    when(employeeCustomPropertyMapping.isFcKeepInContent()).thenReturn(Boolean.FALSE);
    when(employeeCustomPropertyMapping.getFcNsPrefix()).thenReturn("customPre");
    when(employeeCustomPropertyMapping.getFcNsUri()).thenReturn("http://customUri.com");
    EdmTyped employeeEntryDateProperty = employeesSet.getEntityType().getProperty("EmployeeName");
    when(((EdmProperty) employeeEntryDateProperty).getCustomizableFeedMappings()).thenReturn(
        employeeCustomPropertyMapping);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_ATOM_2005);
    prefixMap.put("d", Edm.NAMESPACE_D_2007_08);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    prefixMap.put("xml", Edm.NAMESPACE_XML_1998);
    prefixMap.put("customPre", "http://customUri.com");
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);

    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(employeesSet, employeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/customPre:EmployeeName", xmlString);
    assertXpathNotExists("/a:entry/m:properties/d:EmployeeName", xmlString);
  }

  @Test
  public void noneSyndicationKeepInContentTrueMustShowInProperties() throws Exception {
    // prepare Mock
    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmCustomizableFeedMappings employeeCustomPropertyMapping = mock(EdmCustomizableFeedMappings.class);
    when(employeeCustomPropertyMapping.isFcKeepInContent()).thenReturn(Boolean.TRUE);
    when(employeeCustomPropertyMapping.getFcNsPrefix()).thenReturn("customPre");
    when(employeeCustomPropertyMapping.getFcNsUri()).thenReturn("http://customUri.com");
    EdmTyped employeeEntryDateProperty = employeesSet.getEntityType().getProperty("EmployeeName");
    when(((EdmProperty) employeeEntryDateProperty).getCustomizableFeedMappings()).thenReturn(
        employeeCustomPropertyMapping);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_ATOM_2005);
    prefixMap.put("d", Edm.NAMESPACE_D_2007_08);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    prefixMap.put("xml", Edm.NAMESPACE_XML_1998);
    prefixMap.put("customPre", "http://customUri.com");
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);

    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(employeesSet, employeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/customPre:EmployeeName", xmlString);
    assertXpathExists("/a:entry/m:properties/d:EmployeeName", xmlString);
  }

  @Test
  public void noneSyndicationWithNullPrefix() throws Exception {
    // prepare Mock
    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmCustomizableFeedMappings employeeCustomPropertyMapping = mock(EdmCustomizableFeedMappings.class);
    when(employeeCustomPropertyMapping.isFcKeepInContent()).thenReturn(Boolean.TRUE);
    when(employeeCustomPropertyMapping.getFcNsUri()).thenReturn("http://customUri.com");
    EdmTyped employeeEntryDateProperty = employeesSet.getEntityType().getProperty("EmployeeName");
    when(((EdmProperty) employeeEntryDateProperty).getCustomizableFeedMappings()).thenReturn(
        employeeCustomPropertyMapping);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_ATOM_2005);
    prefixMap.put("d", Edm.NAMESPACE_D_2007_08);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    prefixMap.put("xml", Edm.NAMESPACE_XML_1998);
    prefixMap.put("customPre", "http://customUri.com");
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));

    AtomSerializerDeserializer ser = createAtomEntityProvider();
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);
    boolean thrown = false;
    try {
      ser.writeEntry(employeesSet, employeeData);
    } catch (EntityProviderException e) {
      verifyRootCause(EntityProviderProducerException.class, EntityProviderException.INVALID_NAMESPACE.getKey(), e);
      thrown = true;
    }
    if (!thrown) {
      fail("Exception should have been thrown");
    }
  }

  @Test
  public void noneSyndicationWithNullUri() throws Exception {
    // prepare Mock
    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmCustomizableFeedMappings employeeCustomPropertyMapping = mock(EdmCustomizableFeedMappings.class);
    when(employeeCustomPropertyMapping.isFcKeepInContent()).thenReturn(Boolean.TRUE);
    when(employeeCustomPropertyMapping.getFcNsPrefix()).thenReturn("customPre");
    EdmTyped employeeEntryDateProperty = employeesSet.getEntityType().getProperty("EmployeeName");
    when(((EdmProperty) employeeEntryDateProperty).getCustomizableFeedMappings()).thenReturn(
        employeeCustomPropertyMapping);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_ATOM_2005);
    prefixMap.put("d", Edm.NAMESPACE_D_2007_08);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    prefixMap.put("xml", Edm.NAMESPACE_XML_1998);
    prefixMap.put("customPre", "http://customUri.com");
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));

    AtomSerializerDeserializer ser = createAtomEntityProvider();
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);
    boolean thrown = false;
    try {
      ser.writeEntry(employeesSet, employeeData);
    } catch (EntityProviderException e) {
      verifyRootCause(EntityProviderProducerException.class, EntityProviderException.INVALID_NAMESPACE.getKey(), e);
      thrown = true;
    }
    if (!thrown) {
      fail("Exception should have been thrown");
    }
  }

  @Test
  public void noneSyndicationWithNullUriAndNullPrefix() throws Exception {
    // prepare Mock
    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmCustomizableFeedMappings employeeCustomPropertyMapping = mock(EdmCustomizableFeedMappings.class);
    when(employeeCustomPropertyMapping.isFcKeepInContent()).thenReturn(Boolean.TRUE);
    EdmTyped employeeEntryDateProperty = employeesSet.getEntityType().getProperty("EmployeeName");
    when(((EdmProperty) employeeEntryDateProperty).getCustomizableFeedMappings()).thenReturn(
        employeeCustomPropertyMapping);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_ATOM_2005);
    prefixMap.put("d", Edm.NAMESPACE_D_2007_08);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    prefixMap.put("xml", Edm.NAMESPACE_XML_1998);
    prefixMap.put("f", "http://customUri.com");
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));

    AtomSerializerDeserializer ser = createAtomEntityProvider();
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);
    boolean thrown = false;
    try {
      ser.writeEntry(employeesSet, employeeData);
    } catch (EntityProviderException e) {
      verifyRootCause(EntityProviderProducerException.class, EntityProviderException.INVALID_NAMESPACE.getKey(), e);
      thrown = true;
    }
    if (!thrown) {
      fail("Exception should have been thrown");
    }
  }

  @Test
  public void syndicationWithComplexProperty() throws Exception {
    // prepare Mock
    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmCustomizableFeedMappings employeeCustomPropertyMapping = mock(EdmCustomizableFeedMappings.class);
    when(employeeCustomPropertyMapping.isFcKeepInContent()).thenReturn(Boolean.TRUE);
    when(employeeCustomPropertyMapping.getFcNsPrefix()).thenReturn("customPre");
    when(employeeCustomPropertyMapping.getFcNsUri()).thenReturn("http://customUri.com");
    EdmTyped employeeLocationProperty = employeesSet.getEntityType().getProperty("Location");
    when(((EdmProperty) employeeLocationProperty).getCustomizableFeedMappings()).thenReturn(
        employeeCustomPropertyMapping);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_ATOM_2005);
    prefixMap.put("d", Edm.NAMESPACE_D_2007_08);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    prefixMap.put("xml", Edm.NAMESPACE_XML_1998);
    prefixMap.put("customPre", "http://customUri.com");
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);

    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(employeesSet, employeeData);
    String xmlString = verifyResponse(response);

    assertXpathNotExists("/a:entry/customPre:Location", xmlString);
    assertXpathExists("/a:entry/m:properties/d:Location", xmlString);
  }

  private void verifyRootCause(final Class<?> class1, final String key, final ODataMessageException e) {

    Throwable thrownException = e;
    Throwable lastFoundException = null;
    if (e.getClass().equals(class1)) {
      lastFoundException = e;
    }

    while (thrownException.getCause() != null) {
      thrownException = thrownException.getCause();
      if (thrownException.getClass().equals(class1)) {
        lastFoundException = thrownException;
      }
    }

    if (lastFoundException != null) {
      ODataMessageException msgException = (ODataMessageException) lastFoundException;
      assertEquals(key, msgException.getMessageReference().getKey());
    } else {
      fail("Exception of class: " + class1.getCanonicalName() + " in stacktrace not found.");
    }
  }

  @Test
  public void serializeAtomMediaResource() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);

    assertXpathExists("/a:entry/a:content", xmlString);
    assertXpathEvaluatesTo("", "/a:entry/a:content", xmlString);
    assertXpathEvaluatesTo(ContentType.APPLICATION_OCTET_STREAM.toString(), "/a:entry/a:content/@type", xmlString);
    assertXpathEvaluatesTo("Employees('1')/$value", "/a:entry/a:content/@src", xmlString);
    assertXpathExists("/a:entry/m:properties", xmlString);

    assertXpathExists("/a:entry/a:link[@href=\"Employees('1')/$value\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@rel='edit-media']", xmlString);
    assertXpathExists("/a:entry/a:link[@type='application/octet-stream']", xmlString);

    assertXpathExists("/a:entry/a:link[@href=\"Employees('1')\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@rel='edit']", xmlString);
    assertXpathExists("/a:entry/a:link[@title='Employee']", xmlString);

    verifyTagOrdering(xmlString,
        "link((?:(?!link).)*?)edit",
        "link((?:(?!link).)*?)edit-media");
  }

  private String verifyResponse(final ODataResponse response) throws IOException {
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntityProvider should not set content header", response.getContentHeader());
    String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());
    return xmlString;
  }

  @Test
  public void serializeAtomMediaResourceWithMimeType() throws IOException, XpathException, SAXException,
      XMLStreamException, FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    EntitySerializerProperties properties = EntitySerializerProperties.serviceRoot(BASE_URI)
        .includeMetadata(true).build();
    Entity localEmployeeData = new Entity();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    localEmployeeData.addProperty("EmployeeId", "1");
    localEmployeeData.addProperty("ImmageUrl", null);
    localEmployeeData.addProperty("ManagerId", "1");
    localEmployeeData.addProperty("Age", new Integer(52));
    localEmployeeData.addProperty("RoomId", "1");
    localEmployeeData.addProperty("EntryDate", date);
    localEmployeeData.addProperty("TeamId", "42");
    localEmployeeData.addProperty("EmployeeName", "Walter Winter");
    localEmployeeData.addProperty("getImageType", "abc");

    Entity locationData = new Entity();
    Entity cityData = new Entity();
    cityData.addProperty("PostalCode", "33470");
    cityData.addProperty("CityName", "Duckburg");
    locationData.addProperty("City", cityData);
    locationData.addProperty("Country", "Calisota");

    localEmployeeData.addProperty("Location", locationData);
    localEmployeeData.setWriteProperties(properties);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"),
            localEmployeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);

    assertXpathExists("/a:entry/a:content", xmlString);
    assertXpathEvaluatesTo("abc", "/a:entry/a:content/@type", xmlString);
    assertXpathEvaluatesTo("Employees('1')/$value", "/a:entry/a:content/@src", xmlString);
    assertXpathExists("/a:entry/m:properties", xmlString);
  }

  /*
   * * Test serialization of empty syndication title property. EmployeeName is set to NULL after the update (which is
   * allowed because EmployeeName has default Nullable behavior which is true).
   * Write of an empty atom title tag is allowed within RFC4287 (http://tools.ietf.org/html/rfc4287#section-4.2.14).
   **/
  @Test
  public void serializeEmployeeWithNullSyndicationTitleProperty() throws IOException, XpathException, SAXException,
      XMLStreamException, FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    EntitySerializerProperties properties = EntitySerializerProperties.serviceRoot(BASE_URI)
        .includeMetadata(true).build();
    employeeData.addProperty("EmployeeName", null);
    employeeData.setWriteProperties(properties);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:title", xmlString);
    assertXpathEvaluatesTo("", "/a:entry/a:title", xmlString);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);

    assertXpathExists("/a:entry/a:content", xmlString);
    assertXpathEvaluatesTo("Employees('1')/$value", "/a:entry/a:content/@src", xmlString);
    assertXpathExists("/a:entry/m:properties", xmlString);
  }

  @Test
  public void serializeEmployeeAndCheckOrderOfTags() throws IOException, XpathException, SAXException,
      XMLStreamException, FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    EntitySerializerProperties properties = EntitySerializerProperties.serviceRoot(BASE_URI)
        .includeMetadata(true).build();
    Entity localEmployeeData = new Entity();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    localEmployeeData.addProperty("EmployeeId", "1");
    localEmployeeData.addProperty("ImmageUrl", null);
    localEmployeeData.addProperty("ManagerId", "1");
    localEmployeeData.addProperty("Age", new Integer(52));
    localEmployeeData.addProperty("RoomId", "1");
    localEmployeeData.addProperty("EntryDate", date);
    localEmployeeData.addProperty("TeamId", "42");
    localEmployeeData.addProperty("EmployeeName", "Walter Winter");
    localEmployeeData.addProperty("getImageType", "abc");

    Entity locationData = new Entity();
    Entity cityData = new Entity();
    cityData.addProperty("PostalCode", "33470");
    cityData.addProperty("CityName", "Duckburg");
    cityData.setWriteProperties(properties);
    locationData.addProperty("City", cityData);
    locationData.addProperty("Country", "Calisota");
    locationData.setWriteProperties(properties);
    localEmployeeData.setWriteProperties(properties);

    localEmployeeData.addProperty("Location", locationData);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"),
            localEmployeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/a:content", xmlString);
    // verify self link
    assertXpathExists("/a:entry/a:link[@href=\"Employees('1')\"]", xmlString);
    // verify content media link
    assertXpathExists("/a:entry/a:link[@href=\"Employees('1')/$value\"]", xmlString);
    // verify one navigation link
    assertXpathNotExists("/a:entry/a:link[@title='ne_Manager']", xmlString);

    // verify content
    assertXpathExists("/a:entry/a:content[@type='abc']", xmlString);
    // verify properties
    assertXpathExists("/a:entry/m:properties", xmlString);
    assertXpathEvaluatesTo("8", "count(/a:entry/m:properties/*)", xmlString);

    // verify order of tags
    verifyTagOrdering(xmlString, "id", "title", "updated", "category",
        "link((?:(?!link).)*?)edit",
        "link((?:(?!link).)*?)edit-media",
        "content", "properties");
  }

  @Test
  public void serializeEmployeeAndCheckOrderOfPropertyTags() throws IOException, XpathException, SAXException,
      XMLStreamException, FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(true).build();
    employeeData.setWriteProperties(properties);
    EdmEntitySet employeeEntitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    ODataResponse response = ser.writeEntry(employeeEntitySet, employeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/a:content", xmlString);
    // verify properties
    assertXpathExists("/a:entry/m:properties", xmlString);
    assertXpathEvaluatesTo("8", "count(/a:entry/m:properties/*)", xmlString);

    // verify order of tags
    List<String> expectedPropertyNamesFromEdm = new ArrayList<String>(employeeEntitySet.getEntityType()
        .getPropertyNames());
    expectedPropertyNamesFromEdm.remove(String.valueOf("ImageUrl"));
    verifyTagOrdering(xmlString, expectedPropertyNamesFromEdm.toArray(new String[0]));
  }

  @Test
  public void serializeEmployeeAndCheckKeepInContentFalse() throws IOException, XpathException, SAXException,
      XMLStreamException, FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(true).build();
    EdmEntitySet employeeEntitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");

    // set "keepInContent" to false for EntryDate
    EdmCustomizableFeedMappings employeeUpdatedMappings = mock(EdmCustomizableFeedMappings.class);
    when(employeeUpdatedMappings.getFcTargetPath()).thenReturn(EdmTargetPath.SYNDICATION_UPDATED);
    when(employeeUpdatedMappings.isFcKeepInContent()).thenReturn(Boolean.FALSE);
    EdmTyped employeeEntryDateProperty = employeeEntitySet.getEntityType().getProperty("EntryDate");
    when(((EdmProperty) employeeEntryDateProperty).getCustomizableFeedMappings()).thenReturn(employeeUpdatedMappings);
    employeeData.setWriteProperties(properties);
    ODataResponse response = ser.writeEntry(employeeEntitySet, employeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/a:content", xmlString);
    // verify properties
    assertXpathExists("/a:entry/m:properties", xmlString);
    assertXpathEvaluatesTo("7", "count(/a:entry/m:properties/*)", xmlString);
    //
    assertXpathNotExists("/a:entry/m:properties/d:EntryDate", xmlString);

    // verify order of tags
    List<String> expectedPropertyNamesFromEdm =
        new ArrayList<String>(employeeEntitySet.getEntityType().getPropertyNames());
    expectedPropertyNamesFromEdm.remove(String.valueOf("EntryDate"));
    expectedPropertyNamesFromEdm.remove(String.valueOf("ImageUrl"));
    verifyTagOrdering(xmlString, expectedPropertyNamesFromEdm.toArray(new String[0]));
  }

  @Test(expected = EntityProviderException.class)
  public void serializeAtomEntryWithNullData() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).build();
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    roomData.setWriteProperties(properties);
    ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), null);
  }

  @Test
  public void serializeAtomEntryWithEmptyEntity() throws IOException, XpathException, SAXException,
      XMLStreamException, FactoryConfigurationError, ODataException {
    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(false).build();
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    Entity entity = new Entity();
    entity.setWriteProperties(properties);
     ODataResponse response = ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"),
        entity);
     String xmlString = verifyResponse(response);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);

    assertXpathExists("/a:entry/a:content", xmlString);
    assertXpathEvaluatesTo(ContentType.APPLICATION_XML.toString(), "/a:entry/a:content/@type", xmlString);

 
  }

  @Test
  public void serializeAtomEntry() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    final EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).build();
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    roomData.setWriteProperties(properties);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);

    assertXpathExists("/a:entry/a:content", xmlString);
    assertXpathEvaluatesTo(ContentType.APPLICATION_XML.toString(), "/a:entry/a:content/@type", xmlString);

    assertXpathExists("/a:entry/a:content/m:properties", xmlString);
  }

  @Test
  public void serializeEntryId() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathExists("/a:entry/a:id", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString() + "Employees('1')", "/a:entry/a:id/text()", xmlString);
  }

  @Test
  public void serializeEntryTitle() throws Exception {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:title", xmlString);
    assertXpathEvaluatesTo("text", "/a:entry/a:title/@type", xmlString);
    assertXpathEvaluatesTo((String) employeeData.getProperty("EmployeeName"), "/a:entry/a:title/text()", xmlString);
  }

  @Test
  public void serializeEntryUpdated() throws Exception {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:updated", xmlString);
    assertXpathEvaluatesTo("1999-01-01T00:00:00Z", "/a:entry/a:updated/text()", xmlString);
  }

  @Test
  public void serializeIds() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    photoData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos"), photoData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathExists("/a:entry/a:id", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString() + "Container2.Photos(Id=1,Type='image%2Fpng')",
        "/a:entry/a:id/text()", xmlString);
  }
  
  @Test
  public void serializeGenIds() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    EntitySerializerProperties props =
        EntitySerializerProperties.serviceRoot(
        BASE_URI).isKeyAutoGenerated(true).includeMetadata(true).build();
    Entity photo = new Entity();
  //  photo.addProperty("Id", Integer.valueOf(1));
    photo.addProperty("Name", "Mona Lisa");
    photo.addProperty("Type", "image/png");
    photo
        .addProperty(
            "ImageUrl",
            "http://www.mopo.de/image/view/2012/6/4/16548086,13385561,medRes,maxh,234,maxw,234," +
                "Parodia_Mona_Lisa_Lego_Hamburger_Morgenpost.jpg");
    Entity imageData = new Entity();
    imageData.addProperty("Image", new byte[] { 1, 2, 3, 4 });
    imageData.addProperty("getImageType", "image/png");
    photo.addProperty("Image", imageData);
    photo.addProperty("BinaryData", new byte[] { -1, -2, -3, -4 });

    photo.setWriteProperties(props);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos"), photo);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathExists("/a:entry/a:id", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString() + "Container2.Photos(Id=0,Type='image%2Fpng')",
        "/a:entry/a:id/text()", xmlString);
  }

  @Test
  public void serializeProperties() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/m:properties", xmlString);
    assertXpathEvaluatesTo((String) employeeData.getProperty("RoomId"), "/a:entry/m:properties/d:RoomId/text()",
        xmlString);
    assertXpathEvaluatesTo((String) employeeData.getProperty("TeamId"), "/a:entry/m:properties/d:TeamId/text()",
        xmlString);
  }

  
   @Test
   public void serializeWithValueEncoding() throws IOException, XpathException, SAXException, XMLStreamException,
    FactoryConfigurationError, ODataException {
    photoData.addProperty("Type", "< Ö >");
    photoData.setWriteProperties(DEFAULT_PROPERTIES);
    
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response =
    ser.writeEntry(MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos"), photoData);
    String xmlString = verifyResponse(response);
    
    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathExists("/a:entry/a:id", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString() + "Container2.Photos(Id=1,Type='%3C%20%C3%96%20%3E')",
    "/a:entry/a:id/text()", xmlString);
    assertXpathEvaluatesTo("Container2.Photos(Id=1,Type='%3C%20%C3%96%20%3E')", "/a:entry/a:link/@href", xmlString);
    }
   

  @Test
  public void serializeCategory() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    employeeData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:category", xmlString);
    assertXpathExists("/a:entry/a:category/@term", xmlString);
    assertXpathExists("/a:entry/a:category/@scheme", xmlString);
    assertXpathEvaluatesTo("RefScenario.Employee", "/a:entry/a:category/@term", xmlString);
    assertXpathEvaluatesTo(Edm.NAMESPACE_SCHEME_2007_08, "/a:entry/a:category/@scheme", xmlString);
  }   


  @Test(expected = EntityProviderException.class)
  public void serializeWithFacetsValidation() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped roomNameProperty = edm.getEntityType("RefScenario", "Room").getProperty("Name");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(3);
    when(((EdmProperty) roomNameProperty).getFacets()).thenReturn(facets);

    roomData.addProperty("Name", "1234567");
    roomData.setWriteProperties(DEFAULT_PROPERTIES);
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(edm.getDefaultEntityContainer().getEntitySet("Rooms"), roomData);
    Assert.assertNotNull(response);
  }

  @Test
  public void serializeWithoutFacetsValidation() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped roomNameProperty = edm.getEntityType("RefScenario", "Room").getProperty("Name");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(3);
    when(((EdmProperty) roomNameProperty).getFacets()).thenReturn(facets);

    String name = "1234567";
    roomData.addProperty("Name", name);
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    EntitySerializerProperties properties = EntitySerializerProperties
        .fromProperties(DEFAULT_PROPERTIES).validatingFacets(false).build();
    roomData.setWriteProperties(properties);
    ODataResponse response =
        ser.writeEntry(edm.getDefaultEntityContainer().getEntitySet("Rooms"), roomData);
    assertNotNull(response);

    assertNotNull(response.getEntity());
    String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());

    assertXpathEvaluatesTo(name, "/a:entry/a:content/m:properties/d:Name/text()", xmlString);
  }

  @Test
  public void serializeCustomMapping() throws IOException, XpathException, SAXException, XMLStreamException,
    FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    photoData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response =
    ser.writeEntry(MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos"), photoData);
    String xmlString = verifyResponse(response);
    
    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/custom:CustomProperty", xmlString);
    //assertXpathExists("/a:entry/ру:Содержание", xmlString);
  //TODO  
    //assertXpathEvaluatesTo((String) photoData.getProperty("Содержание"), 
    //"/a:entry/ру:Содержание/text()", xmlString);
    verifyTagOrdering(xmlString, "category", "Содержание", "content", "properties");
 }
   
  @Test
  public void testCustomProperties() throws Exception {
   AtomSerializerDeserializer ser = createAtomEntityProvider();
   EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");
   photoData.setWriteProperties(DEFAULT_PROPERTIES);
   ODataResponse response = ser.writeEntry(entitySet, photoData );
   String xmlString = verifyResponse(response);
    
   assertXpathExists("/a:entry", xmlString);
   assertXpathExists("/a:entry/custom:CustomProperty", xmlString);
   assertXpathNotExists("/a:entry/custom:CustomProperty/text()", xmlString);
   assertXpathEvaluatesTo("true", "/a:entry/custom:CustomProperty/@m:null", xmlString);
   verifyTagOrdering(xmlString, "category", "Содержание", "CustomProperty", "content", "properties");
  }
   

  
  @Test
  public void testKeepInContentNull() throws Exception {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");
    
    EdmProperty customProperty = (EdmProperty) entitySet.getEntityType().getProperty("CustomProperty");
    when(customProperty.getCustomizableFeedMappings().isFcKeepInContent()).thenReturn(null);
    photoData.setWriteProperties(DEFAULT_PROPERTIES);
    
    ODataResponse response = ser.writeEntry(entitySet, photoData);
    String xmlString = verifyResponse(response);
    
    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/custom:CustomProperty", xmlString);
    assertXpathNotExists("/a:entry/custom:CustomProperty/text()", xmlString);
    assertXpathEvaluatesTo("true", "/a:entry/custom:CustomProperty/@m:null", xmlString);
    assertXpathExists("/a:entry/m:properties", xmlString);
    verifyTagOrdering(xmlString, "category", "Содержание", "CustomProperty", "content", "properties");
  }
   
  @Test
  public void serializeAtomMediaResourceLinks() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    employeeData.setWriteProperties(EntitySerializerProperties.serviceRoot(
        BASE_URI).build());
    HashMap<String,Object> id = new HashMap<String, Object>();
    id.put("EmployeeId", "1");
    employeeData.addNavigation("ne_Manager", id );
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData);
    String xmlString = verifyResponse(response);

    String rel = Edm.NAMESPACE_REL_2007_08 + "ne_Manager";

    assertXpathExists("/a:entry/a:link[@href=\"Managers('1')\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@rel='" + rel + "']", xmlString);
    assertXpathExists("/a:entry/a:link[@type='application/atom+xml;type=entry']", xmlString);
    
  }

  
  @Test(expected = EntityProviderException.class)
  public void navigationLinkWithNullData() throws Exception {
    Entity roomEntity = new Entity();
    for (Entry<String, Object> entry : roomData.getProperties().entrySet()) {
      roomEntity.addProperty(entry.getKey(), entry.getValue());
    }
    roomEntity.addNavigation("nr_Building", null);
    roomEntity.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI)
        .build());
    createAtomEntityProvider().writeEntry(
        MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomEntity);

  }

  @Test
  public void navigationLinkWithEmptyData() throws Exception {
    Entity roomEntity = new Entity();
    for (Entry<String, Object> entry : roomData.getProperties().entrySet()) {
      roomEntity.addProperty(entry.getKey(), entry.getValue());
    }
    roomEntity.addNavigation("nr_Building", new Entity());
    roomEntity.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI)
        .includeMetadata(true).build());
    final ODataResponse response = createAtomEntityProvider().writeEntry(
        MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomEntity);

    final String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry/a:link[@href=\"Rooms('1')\"]", xmlString);
    assertXpathNotExists("/a:entry/a:link[@title='nr_Building']", xmlString);
    assertXpathNotExists("/a:entry/a:link[@href=\"Rooms('1')/nr_Employees\"]", xmlString);
    assertXpathNotExists("/a:entry/a:link[@href=\"Building('1')\"]", xmlString);
    assertXpathNotExists("/a:entry/a:link[@type='application/atom+xml;type=feed']", xmlString);
  }

  @Test
  public void navigationLinkToOneOfMany() throws Exception {
    Entity room = new Entity();
    for (Entry<String, Object> entry : roomData.getProperties().entrySet()) {
      room.addProperty(entry.getKey(), entry.getValue());
    }
    room.addNavigation("nr_Employees", employeeData.getProperties());
    room.setWriteProperties(
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .build());
    final ODataResponse response = createAtomEntityProvider().writeEntry(
        MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), room);
    final String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:link[@title='nr_Employees']", xmlString);
    assertXpathNotExists("/a:entry/a:link[@href=\"Rooms('1')/nr_Employees\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Employees('1')\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@type='application/atom+xml;type=feed']", xmlString);
  }

  @Test
  public void navigationLinkHashMap() throws Exception {
    Entity roomEntity = new Entity();
    for (Entry<String, Object> entry : roomData.getProperties().entrySet()) {
      roomEntity.addProperty(entry.getKey(), entry.getValue());
    }
    Map<String, Object> building = new HashMap<String, Object>();
    building.put("Id", "1");
    roomEntity.addNavigation("nr_Building", building);
    roomEntity.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI)
        .build());
    final ODataResponse response = createAtomEntityProvider().writeEntry(
        MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomEntity);

    final String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:link[@title='nr_Building']", xmlString);
    assertXpathNotExists("/a:entry/a:link[@href=\"Rooms('1')/nr_Building\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Buildings('1')\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@type='application/atom+xml;type=entry']", xmlString);
  }

  @Test
  public void serializeWithCustomSrcAttributeOnEmployee() throws Exception {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    Entity localEmployeeData = new Entity();
    for (Entry<String, Object> data : employeeData.getProperties().entrySet()) {
      localEmployeeData.addProperty(data.getKey(), data.getValue());
    }
    String mediaResourceSourceKey = "~src";
    localEmployeeData.addProperty(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmMapping mapping = employeesSet.getEntityType().getMapping();
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);
    localEmployeeData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response = ser.writeEntry(employeesSet, localEmployeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists(
        "/a:entry/a:link[@href=\"Employees('1')/$value\" and" +
            " @rel=\"edit-media\" and @type=\"application/octet-stream\"]", xmlString);
    assertXpathExists("/a:entry/a:content[@type=\"application/octet-stream\"]", xmlString);
    assertXpathExists("/a:entry/a:content[@src=\"http://localhost:8080/images/image1\"]", xmlString);
  }

  @Test
  public void serializeWithCustomSrcAndTypeAttributeOnEmployee() throws Exception {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    Entity localEmployeeData = new Entity();
    for (Entry<String, Object> data : employeeData.getProperties().entrySet()) {
      localEmployeeData.addProperty(data.getKey(), data.getValue());
    }
    String mediaResourceSourceKey = "~src";
    localEmployeeData.addProperty(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    String mediaResourceMimeTypeKey = "~type";
    localEmployeeData.addProperty(mediaResourceMimeTypeKey, "image/jpeg");
    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmMapping mapping = employeesSet.getEntityType().getMapping();
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);
    when(mapping.getMediaResourceMimeTypeKey()).thenReturn(mediaResourceMimeTypeKey);
    localEmployeeData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response = ser.writeEntry(employeesSet, localEmployeeData);
    String xmlString = verifyResponse(response);

    assertXpathExists(
        "/a:entry/a:link[@href=\"Employees('1')/$value\" and" +
            " @rel=\"edit-media\" and @type=\"image/jpeg\"]", xmlString);
    assertXpathExists("/a:entry/a:content[@type=\"image/jpeg\"]", xmlString);
    assertXpathExists("/a:entry/a:content[@src=\"http://localhost:8080/images/image1\"]", xmlString);
  }

  @Test
  public void serializeWithCustomSrcAttributeOnRoom() throws Exception {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    Entity localRoomData = new Entity();
    for (Entry<String, Object> data : roomData.getProperties().entrySet()) {
      localRoomData.addProperty(data.getKey(), data.getValue());
    }
    String mediaResourceSourceKey = "~src";
    localRoomData.addProperty(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    EdmEntityType roomType = roomsSet.getEntityType();
    EdmMapping mapping = mock(EdmMapping.class);
    when(roomType.getMapping()).thenReturn(mapping);
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);
    localRoomData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response = ser.writeEntry(roomsSet, localRoomData);
    String xmlString = verifyResponse(response);

    assertXpathNotExists(
        "/a:entry/a:link[@href=\"Rooms('1')/$value\" and" +
            " @rel=\"edit-media\" and @type=\"application/octet-stream\"]", xmlString);
    assertXpathNotExists("/a:entry/a:content[@type=\"application/octet-stream\"]", xmlString);
    assertXpathNotExists("/a:entry/a:content[@src=\"http://localhost:8080/images/image1\"]", xmlString);
  }

  @Test
  public void serializeWithCustomSrcAndTypeAttributeOnRoom() throws Exception {
    AtomSerializerDeserializer ser = createAtomEntityProvider();
    Entity localRoomData = new Entity();
    for (Entry<String, Object> data : roomData.getProperties().entrySet()) {
      localRoomData.addProperty(data.getKey(), data.getValue());
    }
    String mediaResourceSourceKey = "~src";
    localRoomData.addProperty(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    String mediaResourceMimeTypeKey = "~type";
    localRoomData.addProperty(mediaResourceMimeTypeKey, "image/jpeg");
    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    EdmEntityType roomType = roomsSet.getEntityType();
    EdmMapping mapping = mock(EdmMapping.class);
    when(roomType.getMapping()).thenReturn(mapping);
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);
    when(mapping.getMediaResourceMimeTypeKey()).thenReturn(mediaResourceMimeTypeKey);
    localRoomData.setWriteProperties(DEFAULT_PROPERTIES);
    ODataResponse response = ser.writeEntry(roomsSet, localRoomData);
    String xmlString = verifyResponse(response);

    assertXpathNotExists(
        "/a:entry/a:link[@href=\"Rooms('1')/$value\" and" +
            " @rel=\"edit-media\" and @type=\"image/jpeg\"]", xmlString);
    assertXpathNotExists("/a:entry/a:content[@type=\"image/jpeg\"]", xmlString);
    assertXpathNotExists("/a:entry/a:content[@src=\"http://localhost:8080/images/image1\"]", xmlString);
  }

  private void verifyTagOrdering(final String xmlString, final String... toCheckTags) {
    XMLUnitHelper.verifyTagOrdering(xmlString, toCheckTags);
  }

  @Test
  public void unbalancedPropertyEntryWithInlineEntry() throws Exception {

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    Entity buildingData = new Entity();
    buildingData.addProperty("Id", "1");
    buildingData.addProperty("Name", "Building1");
    roomData.addNavigation("nr_Building", buildingData);
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    roomData.setWriteProperties(properties);
    buildingData.setWriteProperties(properties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyBuilding(buildingXPathString, xmlString);
  }
  
  @Test
  public void entryWithInlineEntryAndParentProperty() throws Exception {

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    Entity buildingData = new Entity();
    buildingData.addProperty("Id", "1");
    buildingData.addProperty("Name", "Building1");
    roomData.addNavigation("nr_Building", buildingData);
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    roomData.setWriteProperties(properties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyBuilding(buildingXPathString, xmlString);
  }
  
  @Test
  public void entryWithInlineEntryDifferentProperty() throws Exception {

    Entity roomData = new Entity();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    EntitySerializerProperties inline =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(false).build();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    Entity buildingData = new Entity();
    buildingData.addProperty("Id", "1");
    buildingData.addProperty("Name", "Building1");
    buildingData.setWriteProperties(inline);
    roomData.addNavigation("nr_Building", buildingData);
    roomData.setWriteProperties(properties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    assertXpathExists(buildingXPathString, xmlString);
    assertXpathExists(buildingXPathString + "/m:inline", xmlString);
    assertXpathExists(buildingXPathString + "/m:inline/a:entry[@xml:base='" + BASE_URI + "']", xmlString);
    assertXpathExists(buildingXPathString + "/m:inline/a:entry", xmlString);
    assertXpathExists(buildingXPathString + "/m:inline/a:entry/a:content", xmlString);
    assertXpathExists(buildingXPathString + "/m:inline/a:entry/a:content/m:properties", xmlString);
    assertXpathExists(buildingXPathString + "/m:inline/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists(buildingXPathString + "/m:inline/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Seats", xmlString);
  }

  

  @Test
  public void entityWithInlineEntryWithoutId() throws Exception {

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    Entity buildingData = new Entity();
    buildingData.addProperty("Name", "Building1");
    roomData.addNavigation("nr_Building", buildingData);
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .build();
    roomData.setWriteProperties(properties);
    buildingData.setWriteProperties(properties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);
  }

  @Test
  public void entityWithEmptyInlineEntry() throws Exception {

    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    Entity buildingData = new Entity();
    roomData.addNavigation("nr_Building", buildingData);
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    roomData.setWriteProperties(properties);
    buildingData.setWriteProperties(properties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Seats", xmlString);
  }
  
  @Test(expected = EntityProviderException.class)
  public void entityWithoutIdInlineEntry() throws Exception {

    Entity roomData = new Entity();
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    Entity buildingData = new Entity();
    roomData.addNavigation("nr_Building", buildingData);
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    roomData.setWriteProperties(properties);
    buildingData.setWriteProperties(properties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

   
  }

  @Test
  public void entityWithNullInlineEntry() throws Exception {
    expectedEx.expect(EntityProviderException.class);
    expectedEx.expectMessage("Entity or expanded entity cannot have null value.");
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    roomData.setWriteProperties(properties);
    roomData.addNavigation("nr_Building", null);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

  }
  
  private InputStream createStreamReader(final String xml) throws
  XMLStreamException, UnsupportedEncodingException {
    return new ByteArrayInputStream(xml.getBytes("UTF-8"));
  }
  
  @Test
  public void deepInsertEndToEnd() throws Exception {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    String xml = readFile("metadataForDeepInsert.xml");
    InputStream reader = createStreamReader(xml);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals(1, result.getEdm().getSchemas().size());
    ClientEdm edm = result.getEdm();
    
    Entity descMap = new Entity();
    descMap.addProperty("Product", "CRPROD2");
    descMap.addProperty("Language", "ES");
    descMap.addProperty("ProductDescription", "Hola2");
    EntityCollection descList =  new EntityCollection();

    descList.addEntity(descMap);

    Entity prodCreateFakeMap = new Entity();
    prodCreateFakeMap.addProperty("Product", "CRPROD2");
    prodCreateFakeMap.addProperty("ProductType", "HALB");
    prodCreateFakeMap.addProperty("BaseUnit", "PC");

    prodCreateFakeMap.addNavigation("to_Description", descList);
   
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .build();
    prodCreateFakeMap.setWriteProperties(properties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    ODataResponse response =provider
        .writeEntry(edm.getDefaultEntityContainer().getEntitySet("A_Product"), prodCreateFakeMap);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyProduct(productXPathString , xmlString);
  }
  
  protected String readFile(final String filename) throws IOException {
    InputStream in = getFileAsStream(filename);

    byte[] tmp = new byte[8192];
    int count = in.read(tmp);
    StringBuilder b = new StringBuilder();
    while (count >= 0) {
      b.append(new String(tmp, 0, count));
      count = in.read(tmp);
    }

    return b.toString();
  }
  protected InputStream getFileAsStream(final String filename) throws IOException {
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    if (in == null) {
      throw new IOException("Requested file '" + filename + "' was not found.");
    }
    return in;
  }
  @Test
  public void entityWithInvalidInlineEntryType() throws Exception {
    expectedEx.expect(EntityProviderException.class);
    expectedEx.expectMessage("Navigation has to be either an Entity or a Map");
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    roomData.setWriteProperties(properties);
    roomData.addNavigation("nr_Building", new ArrayList<String>());
    AtomSerializerDeserializer provider = createAtomEntityProvider();
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

  }

  private void verifyBuilding(final String path, final String xmlString) throws XpathException, IOException,
      SAXException {
    assertXpathExists(path, xmlString);
    assertXpathExists(path + "/m:inline", xmlString);

    assertXpathExists(path + "/m:inline/a:entry[@xml:base='" + BASE_URI + "']", xmlString);
    assertXpathExists(path + "/m:inline/a:entry", xmlString);
    assertXpathExists(path + "/m:inline/a:entry/a:id", xmlString);
    assertXpathExists(path + "/m:inline/a:entry/a:title", xmlString);
    assertXpathExists(path + "/m:inline/a:entry/a:updated", xmlString);

    assertXpathExists(path + "/m:inline/a:entry/a:category", xmlString);
    assertXpathExists(path + "/m:inline/a:entry/a:link", xmlString);

    assertXpathExists(path + "/m:inline/a:entry/a:content", xmlString);
    assertXpathExists(path + "/m:inline/a:entry/a:content/m:properties", xmlString);
    assertXpathExists(path + "/m:inline/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists(path + "/m:inline/a:entry/a:content/m:properties/d:Name", xmlString);

    assertXpathExists("/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Seats", xmlString);

  }
  

  private void verifyProduct(final String path, final String xmlString) throws XpathException, IOException,
      SAXException {
    assertXpathExists(path, xmlString);
    assertXpathExists(path + "/m:inline", xmlString);

    assertXpathExists(path + "/m:inline/a:feed[@xml:base='" + BASE_URI + "']", xmlString);
    assertXpathExists(path + "/m:inline/a:feed", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:id", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:title", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:updated", xmlString);

    assertXpathExists(path + "/m:inline/a:feed/a:author", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:link", xmlString);
    ;
    assertXpathExists("/a:entry/a:link/m:inline/a:feed/a:entry", xmlString);
    assertXpathExists("/a:entry/a:link/m:inline/a:feed/a:entry/a:content/m:properties/d:Product", xmlString);
    assertXpathExists("/a:entry/a:link/m:inline/a:feed/a:entry/a:content/m:properties/d:Language", xmlString);
    assertXpathExists("/a:entry/a:link/m:inline/a:feed/a:entry/a:content/m:properties/d:ProductDescription", xmlString);
    
    assertXpathExists("/a:entry/a:content/m:properties/d:Product", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:BaseUnit", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:ProductType", xmlString);
  }
  
  @Test
  public void entityWithInlineEntryWithoutKeys() throws Exception {

    Entity roomData = new Entity();
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    Entity buildingData = new Entity();
    buildingData.addProperty("Name", "Building1");
    roomData.addNavigation("nr_Building", buildingData);
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).isKeyAutoGenerated(true)
            .build();
    roomData.setWriteProperties(properties);
    buildingData.setWriteProperties(properties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    ODataResponse response = provider.writeEntry(MockFacade.getMockEdm().
        getDefaultEntityContainer().getEntitySet("Rooms"), roomData);
    String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry/a:link[@href=\"Rooms('A')/nr_Building\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Rooms('A')/nr_Building\"]/m:inline", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Rooms('A')/nr_Building\"]/m:inline/a:entry/"
        + "a:content/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Rooms('A')/nr_Building\"]/m:inline/a:entry/"
        + "a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Seats", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
  }
  
  @Test
  public void entityWithInlineEntryWithoutKeysWithMetadata() throws Exception {

    Entity roomData = new Entity();
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    Entity buildingData = new Entity();
    buildingData.addProperty("Name", "Building1");
    roomData.addNavigation("nr_Building", buildingData);
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).isKeyAutoGenerated(true).includeMetadata(true)
            .build();
    roomData.setWriteProperties(properties);
    buildingData.setWriteProperties(properties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    ODataResponse response = provider.writeEntry(MockFacade.getMockEdm().
        getDefaultEntityContainer().getEntitySet("Rooms"), roomData);
    String xmlString = verifyResponse(response);
    assertXpathExists("/a:entry/a:id", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Rooms('A')/nr_Building\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Rooms('A')/nr_Building\"]/m:inline", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Rooms('A')/nr_Building\"]/m:inline/a:entry/"
        + "a:content/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Rooms('A')/nr_Building\"]/m:inline/a:entry/"
        + "a:id", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Rooms('A')/nr_Building\"]/m:inline/a:entry/"
        + "a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Seats", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
  }
  
  @Test
  public void navigationLinkToOneOfManyWithoutKeys() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    roomData.addProperty("Version", new Integer(3));
    
    employeeData = new Entity();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);
    
    Entity employeeData = new Entity();
    employeeData.addProperty("ImmageUrl", null);
    employeeData.addProperty("ManagerId", "1");
    employeeData.addProperty("Age", new Integer(52));
    employeeData.addProperty("RoomId", "1");
    employeeData.addProperty("EntryDate", date);
    employeeData.addProperty("TeamId", "42");
    employeeData.addProperty("EmployeeName", "Walter Winter");
    
    roomData.addNavigation("nr_Employees", employeeData.getProperties());
    roomData.setWriteProperties(
        EntitySerializerProperties.serviceRoot(BASE_URI).isKeyAutoGenerated(true)
            .build());
    final ODataResponse response = createAtomEntityProvider().writeEntry(
        MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);
    final String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:link[@title='nr_Employees']", xmlString);
    assertXpathNotExists("/a:entry/a:link[@href=\"Rooms('1')/nr_Employees\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Employees('A')\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@type='application/atom+xml;type=feed']", xmlString);
  }
  
  @Test
  public void contentOnlyWithoutKeyWithoutSelectedProperties() throws Exception {
    Entity employeeData = new Entity();
    employeeData.addProperty("ManagerId", "1");
    employeeData.addProperty("Age", new Integer(52));
    employeeData.addProperty("RoomId", "1");
    employeeData.addProperty("TeamId", "42");

    employeeData.setWriteProperties(
        EntitySerializerProperties.serviceRoot(BASE_URI).build());
    
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");

    try {
      createAtomEntityProvider().writeEntry(entitySet, employeeData);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'EmployeeId'"));
    }
  }
  
  @Test
  public void testWithoutCompositeKey() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");
    
    Entity photoData = new Entity();
    photoData.addProperty("Name", "Mona Lisa");
    photoData.setWriteProperties(
        EntitySerializerProperties.serviceRoot(BASE_URI).build());

    try {
      createAtomEntityProvider().writeEntry(entitySet, photoData);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'Id'"));
    }
  }
  
  @Test
  public void testWithoutCompositeKeyWithOneKeyNull() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmEntitySet entitySet = edm.getEntityContainer("Container2").getEntitySet("Photos");
    
    Entity photoData = new Entity();
    photoData.addProperty("Name", "Mona Lisa");
    photoData.addProperty("Id", Integer.valueOf(1));
    photoData.setWriteProperties(
        EntitySerializerProperties.serviceRoot(BASE_URI).build());
    
    EdmTyped typeProperty = edm.getEntityContainer("Container2").getEntitySet("Photos").
        getEntityType().getProperty("Type");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getConcurrencyMode()).thenReturn(EdmConcurrencyMode.Fixed);
    when(facets.getMaxLength()).thenReturn(3);
    when(((EdmProperty) typeProperty).getFacets()).thenReturn(facets);

    try {
      createAtomEntityProvider().writeEntry(entitySet, photoData);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'Type'"));
    }
  }
  
  @Test
  public void testExceptionWithNonNullablePropertyIsNull() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Organizations");
    EdmProperty nameProperty = (EdmProperty) entitySet.getEntityType().getProperty("Name");
    EdmFacets facets = nameProperty.getFacets();
    when(facets.isNullable()).thenReturn(new Boolean(false));
    
    Entity orgData = new Entity();
    orgData.addProperty("Id", "1");
    orgData.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).build());
    try {
      createAtomEntityProvider().writeEntry(entitySet, orgData);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'Name'"));
    }
  }
  
  @Test
  public void testExceptionWithNonNullablePropertyIsNull1() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Organizations");
    EdmProperty kindProperty = (EdmProperty) entitySet.getEntityType().getProperty("Kind");
    EdmFacets facets = kindProperty.getFacets();
    when(facets.isNullable()).thenReturn(new Boolean(false));
    
    EdmProperty nameProperty = (EdmProperty) entitySet.getEntityType().getProperty("Name");
    when(nameProperty.getFacets()).thenReturn(null);
    
    Entity orgData = new Entity();
    orgData.addProperty("Id", "1");
    orgData.addProperty("Name", "Org1");
    orgData.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).build());
    try {
      createAtomEntityProvider().writeEntry(entitySet, orgData);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'Kind'"));
    }
  }
  
  @Test
  public void testExceptionWithNonNullablePropertyIsNull2() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Organizations");
    EdmProperty kindProperty = (EdmProperty) entitySet.getEntityType().getProperty("Kind");
    EdmFacets facets = kindProperty.getFacets();
    when(facets.isNullable()).thenReturn(new Boolean(false));
    
    EdmProperty nameProperty = (EdmProperty) entitySet.getEntityType().getProperty("Name");
    EdmFacets facets1 = nameProperty.getFacets();
    when(facets1.isNullable()).thenReturn(new Boolean(false));
     
    Entity orgData = new Entity();
    orgData.addProperty("Id", "1");
    orgData.addProperty("Name", "Org1");
    orgData.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).build());
    try {
      createAtomEntityProvider().writeEntry(entitySet, orgData);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("do not allow to format the value 'Org1' for property 'Name'."));
    }
  }
}
