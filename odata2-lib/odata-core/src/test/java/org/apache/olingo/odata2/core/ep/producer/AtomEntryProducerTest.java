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
package org.apache.olingo.odata2.core.ep.producer;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.apache.olingo.odata2.api.ODataCallback;
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
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteEntryContent;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackResult;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.ep.AbstractProviderTest;
import org.apache.olingo.odata2.core.ep.AtomEntityProvider;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.uri.ExpandSelectTreeCreator;
import org.apache.olingo.odata2.core.uri.UriParserImpl;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.helper.XMLUnitHelper;
import org.apache.olingo.odata2.testutil.mock.EdmTestProvider;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class AtomEntryProducerTest extends AbstractProviderTest {

  private String buildingXPathString = "/a:entry/a:link[@href=\"Rooms('1')/nr_Building\" and @title='nr_Building']";
  
  public AtomEntryProducerTest(final StreamWriterImplType type) {
    super(type);
  }

  @Test
  public void omitETagTestPropertyPresent() throws Exception {
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).omitETag(true).build();
    AtomEntityProvider ser = createAtomEntityProvider();

    Map<String, Object> localRoomData = new HashMap<String, Object>();
    localRoomData.put("Id", "1");
    localRoomData.put("Name", "Neu Schwanstein");
    localRoomData.put("Seats", new Integer(20));
    localRoomData.put("Version", new Integer(3));
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), localRoomData,
            properties);
    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry[@m:etag]", xmlString);
  }

  @Test
  public void omitETagTestPropertyNOTPresentMustNotResultInException() throws Exception {
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).omitETag(true).build();
    AtomEntityProvider ser = createAtomEntityProvider();

    Map<String, Object> localRoomData = new HashMap<String, Object>();
    localRoomData.put("Id", "1");
    localRoomData.put("Name", "Neu Schwanstein");
    localRoomData.put("Seats", new Integer(20));
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), localRoomData,
            properties);
    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry[@m:etag]", xmlString);
  }

  @Test
  public void omitETagTestNonNullablePropertyNOTPresentMustNotResultInException() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    EdmProperty versionProperty = (EdmProperty) entitySet.getEntityType().getProperty("Version");
    EdmFacets facets = versionProperty.getFacets();
    when(facets.isNullable()).thenReturn(new Boolean(false));
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("Id");
    selectedPropertyNames.add("Name");
    selectedPropertyNames.add("Seats");
    ExpandSelectTreeNode selectNode =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedPropertyNames).build();
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).omitETag(true).expandSelectTree(selectNode).build();
    AtomEntityProvider ser = createAtomEntityProvider();

    Map<String, Object> localRoomData = new HashMap<String, Object>();
    localRoomData.put("Id", "1");
    localRoomData.put("Name", "Neu Schwanstein");
    localRoomData.put("Seats", new Integer(20));
    ODataResponse response = ser.writeEntry(entitySet, localRoomData, properties);
    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry[@m:etag]", xmlString);
  }

  @Test
  public void contentOnly() throws Exception {
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).contentOnly(true).build();

    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData,
            properties);
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
  public void contentOnlyRoom() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("Name");
    ExpandSelectTreeNode expandSelectTree =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedPropertyNames).build();
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).contentOnly(true).expandSelectTree(expandSelectTree)
            .build();

    Map<String, Object> localRoomData = new HashMap<String, Object>();
    localRoomData.put("Name", "Neu Schwanstein");

    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(entitySet, localRoomData, properties);
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
  public void contentOnlyRoomSelectedOrExpandedLinksMustBeIgnored() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("Name");
    List<String> navigationPropertyNames = new ArrayList<String>();
    navigationPropertyNames.add("nr_Employees");
    navigationPropertyNames.add("nr_Building");
    ExpandSelectTreeNode expandSelectTree =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedPropertyNames).expandedLinks(
            navigationPropertyNames).build();
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).contentOnly(true).expandSelectTree(expandSelectTree)
            .build();

    Map<String, Object> localRoomData = new HashMap<String, Object>();
    localRoomData.put("Name", "Neu Schwanstein");

    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(entitySet, localRoomData, properties);
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
  public void contentOnlyRoomWithAdditionalLink() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("Name");
    ExpandSelectTreeNode expandSelectTree =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedPropertyNames).build();
    Map<String, Map<String, Object>> additinalLinks = new HashMap<String, Map<String, Object>>();
    Map<String, Object> buildingLink = new HashMap<String, Object>();
    buildingLink.put("Id", "1");
    additinalLinks.put("nr_Building", buildingLink);
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).contentOnly(true).expandSelectTree(expandSelectTree)
            .additionalLinks(additinalLinks).build();

    Map<String, Object> localRoomData = new HashMap<String, Object>();
    localRoomData.put("Name", "Neu Schwanstein");

    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(entitySet, localRoomData, properties);
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

  @Test
  public void contentOnlyWithoutKey() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("ManagerId");
    ExpandSelectTreeNode select =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedPropertyNames).build();

    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).contentOnly(true).expandSelectTree(select).build();

    Map<String, Object> localEmployeeData = new HashMap<String, Object>();
    localEmployeeData.put("ManagerId", "1");

    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(entitySet, localEmployeeData, properties);
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
  public void contentOnlySelectedOrExpandedLinksMustBeIgnored() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("ManagerId");

    List<String> expandedNavigationNames = new ArrayList<String>();
    expandedNavigationNames.add("ne_Manager");
    expandedNavigationNames.add("ne_Team");
    expandedNavigationNames.add("ne_Room");

    ExpandSelectTreeNode select =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedPropertyNames).expandedLinks(
            expandedNavigationNames).build();

    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).contentOnly(true).expandSelectTree(select).build();

    Map<String, Object> localEmployeeData = new HashMap<String, Object>();
    localEmployeeData.put("ManagerId", "1");

    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(entitySet, localEmployeeData,
            properties);
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
  public void contentOnlyWithAdditinalLink() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("ManagerId");
    ExpandSelectTreeNode select =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedPropertyNames).build();

    Map<String, Map<String, Object>> additinalLinks = new HashMap<String, Map<String, Object>>();
    Map<String, Object> managerLink = new HashMap<String, Object>();
    managerLink.put("EmployeeId", "1");
    additinalLinks.put("ne_Manager", managerLink);
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).contentOnly(true).expandSelectTree(select).additionalLinks(
            additinalLinks).build();

    Map<String, Object> localEmployeeData = new HashMap<String, Object>();
    localEmployeeData.put("ManagerId", "1");

    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(entitySet, localEmployeeData,
            properties);
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

    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(employeesSet, employeeData, DEFAULT_PROPERTIES);
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

    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(employeesSet, employeeData, DEFAULT_PROPERTIES);
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

    AtomEntityProvider ser = createAtomEntityProvider();
    boolean thrown = false;
    try {
      ser.writeEntry(employeesSet, employeeData, DEFAULT_PROPERTIES);
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

    AtomEntityProvider ser = createAtomEntityProvider();
    boolean thrown = false;
    try {
      ser.writeEntry(employeesSet, employeeData, DEFAULT_PROPERTIES);
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

    AtomEntityProvider ser = createAtomEntityProvider();
    boolean thrown = false;
    try {
      ser.writeEntry(employeesSet, employeeData, DEFAULT_PROPERTIES);
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

    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response = ser.writeEntry(employeesSet, employeeData, DEFAULT_PROPERTIES);
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
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData,
            DEFAULT_PROPERTIES);
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

    // assert navigation link order
    verifyTagOrdering(xmlString,
        "link((?:(?!link).)*?)edit",
        "link((?:(?!link).)*?)edit-media",
        "link((?:(?!link).)*?)ne_Manager",
        "link((?:(?!link).)*?)ne_Team",
        "link((?:(?!link).)*?)ne_Room");
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
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    Map<String, Object> localEmployeeData = new HashMap<String, Object>();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    localEmployeeData.put("EmployeeId", "1");
    localEmployeeData.put("ImmageUrl", null);
    localEmployeeData.put("ManagerId", "1");
    localEmployeeData.put("Age", new Integer(52));
    localEmployeeData.put("RoomId", "1");
    localEmployeeData.put("EntryDate", date);
    localEmployeeData.put("TeamId", "42");
    localEmployeeData.put("EmployeeName", "Walter Winter");
    localEmployeeData.put("getImageType", "abc");

    Map<String, Object> locationData = new HashMap<String, Object>();
    Map<String, Object> cityData = new HashMap<String, Object>();
    cityData.put("PostalCode", "33470");
    cityData.put("CityName", "Duckburg");
    locationData.put("City", cityData);
    locationData.put("Country", "Calisota");

    localEmployeeData.put("Location", locationData);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"),
            localEmployeeData,
            properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);

    assertXpathExists("/a:entry/a:content", xmlString);
    assertXpathEvaluatesTo("abc", "/a:entry/a:content/@type", xmlString);
    assertXpathEvaluatesTo("Employees('1')/$value", "/a:entry/a:content/@src", xmlString);
    assertXpathExists("/a:entry/m:properties", xmlString);
  }

  /**
   * Test serialization of empty syndication title property. EmployeeName is set to NULL after the update (which is
   * allowed because EmployeeName has default Nullable behavior which is true).
   * Write of an empty atom title tag is allowed within RFC4287 (http://tools.ietf.org/html/rfc4287#section-4.2.14).
   */
  @Test
  public void serializeEmployeeWithNullSyndicationTitleProperty() throws IOException, XpathException, SAXException,
      XMLStreamException, FactoryConfigurationError, ODataException {
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    employeeData.put("EmployeeName", null);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData,
            properties);
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
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    Map<String, Object> localEmployeeData = new HashMap<String, Object>();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    localEmployeeData.put("EmployeeId", "1");
    localEmployeeData.put("ImmageUrl", null);
    localEmployeeData.put("ManagerId", "1");
    localEmployeeData.put("Age", new Integer(52));
    localEmployeeData.put("RoomId", "1");
    localEmployeeData.put("EntryDate", date);
    localEmployeeData.put("TeamId", "42");
    localEmployeeData.put("EmployeeName", "Walter Winter");
    localEmployeeData.put("getImageType", "abc");

    Map<String, Object> locationData = new HashMap<String, Object>();
    Map<String, Object> cityData = new HashMap<String, Object>();
    cityData.put("PostalCode", "33470");
    cityData.put("CityName", "Duckburg");
    locationData.put("City", cityData);
    locationData.put("Country", "Calisota");

    localEmployeeData.put("Location", locationData);
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"),
            localEmployeeData,
            properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/a:content", xmlString);
    // verify self link
    assertXpathExists("/a:entry/a:link[@href=\"Employees('1')\"]", xmlString);
    // verify content media link
    assertXpathExists("/a:entry/a:link[@href=\"Employees('1')/$value\"]", xmlString);
    // verify one navigation link
    assertXpathExists("/a:entry/a:link[@title='ne_Manager']", xmlString);

    // verify content
    assertXpathExists("/a:entry/a:content[@type='abc']", xmlString);
    // verify properties
    assertXpathExists("/a:entry/m:properties", xmlString);
    assertXpathEvaluatesTo("9", "count(/a:entry/m:properties/*)", xmlString);

    // verify order of tags
    verifyTagOrdering(xmlString, "id", "title", "updated", "category",
        "link((?:(?!link).)*?)edit",
        "link((?:(?!link).)*?)edit-media",
        "link((?:(?!link).)*?)ne_Manager",
        "content", "properties");
  }

  @Test
  public void serializeEmployeeAndCheckOrderOfPropertyTags() throws IOException, XpathException, SAXException,
      XMLStreamException, FactoryConfigurationError, ODataException {
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    EdmEntitySet employeeEntitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    ODataResponse response = ser.writeEntry(employeeEntitySet, employeeData, properties);
    String xmlString = verifyResponse(response);

    // log.debug(xmlString);

    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/a:content", xmlString);
    // verify properties
    assertXpathExists("/a:entry/m:properties", xmlString);
    assertXpathEvaluatesTo("9", "count(/a:entry/m:properties/*)", xmlString);

    // verify order of tags
    List<String> expectedPropertyNamesFromEdm = employeeEntitySet.getEntityType().getPropertyNames();
    verifyTagOrdering(xmlString, expectedPropertyNamesFromEdm.toArray(new String[0]));
  }

  @Test
  public void serializeEmployeeAndCheckKeepInContentFalse() throws IOException, XpathException, SAXException,
      XMLStreamException, FactoryConfigurationError, ODataException {
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    EdmEntitySet employeeEntitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");

    // set "keepInContent" to false for EntryDate
    EdmCustomizableFeedMappings employeeUpdatedMappings = mock(EdmCustomizableFeedMappings.class);
    when(employeeUpdatedMappings.getFcTargetPath()).thenReturn(EdmTargetPath.SYNDICATION_UPDATED);
    when(employeeUpdatedMappings.isFcKeepInContent()).thenReturn(Boolean.FALSE);
    EdmTyped employeeEntryDateProperty = employeeEntitySet.getEntityType().getProperty("EntryDate");
    when(((EdmProperty) employeeEntryDateProperty).getCustomizableFeedMappings()).thenReturn(employeeUpdatedMappings);

    ODataResponse response = ser.writeEntry(employeeEntitySet, employeeData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/a:content", xmlString);
    // verify properties
    assertXpathExists("/a:entry/m:properties", xmlString);
    assertXpathEvaluatesTo("8", "count(/a:entry/m:properties/*)", xmlString);
    //
    assertXpathNotExists("/a:entry/m:properties/d:EntryDate", xmlString);

    // verify order of tags
    List<String> expectedPropertyNamesFromEdm =
        new ArrayList<String>(employeeEntitySet.getEntityType().getPropertyNames());
    expectedPropertyNamesFromEdm.remove(String.valueOf("EntryDate"));
    verifyTagOrdering(xmlString, expectedPropertyNamesFromEdm.toArray(new String[0]));
  }

  @Test(expected = EntityProviderException.class)
  public void serializeAtomEntryWithNullData() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    final EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    AtomEntityProvider ser = createAtomEntityProvider();
    ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), null, properties);
  }

  @Test(expected = EntityProviderException.class)
  public void serializeAtomEntryWithEmptyHashMap() throws IOException, XpathException, SAXException,
      XMLStreamException, FactoryConfigurationError, ODataException {
    final EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    AtomEntityProvider ser = createAtomEntityProvider();
    ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"),
        new HashMap<String, Object>(), properties);
  }

  @Test
  public void serializeAtomEntry() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    final EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);

    assertXpathExists("/a:entry/a:content", xmlString);
    assertXpathEvaluatesTo(ContentType.APPLICATION_XML.toString(), "/a:entry/a:content/@type", xmlString);

    assertXpathExists("/a:entry/a:content/m:properties", xmlString);
  }

  @Test
  public void serializeAtomEntryWithSimplePropertyTypeInformation() throws Exception {
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).includeSimplePropertyType(true).build();
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:content/m:properties", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Id[@m:type=\"Edm.String\"]", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Name[@m:type=\"Edm.String\"]", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Seats[@m:type=\"Edm.Int16\"]", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Version[@m:type=\"Edm.Int16\"]", xmlString);
  }

  @Test
  public void serializeEntryId() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData,
            DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathExists("/a:entry/a:id", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString() + "Employees('1')", "/a:entry/a:id/text()", xmlString);
  }

  @Test
  public void serializeEntryTitle() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData,
            DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:title", xmlString);
    assertXpathEvaluatesTo("text", "/a:entry/a:title/@type", xmlString);
    assertXpathEvaluatesTo((String) employeeData.get("EmployeeName"), "/a:entry/a:title/text()", xmlString);
  }

  @Test
  public void serializeEntryUpdated() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData,
            DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:updated", xmlString);
    assertXpathEvaluatesTo("1999-01-01T00:00:00Z", "/a:entry/a:updated/text()", xmlString);
  }

  @Test
  public void serializeIds() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos"), photoData,
            DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathExists("/a:entry/a:id", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString() + "Container2.Photos(Id=1,Type='image%2Fpng')",
        "/a:entry/a:id/text()", xmlString);
  }

  @Test
  public void serializeProperties() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData,
            DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/m:properties", xmlString);
    assertXpathEvaluatesTo((String) employeeData.get("RoomId"), "/a:entry/m:properties/d:RoomId/text()", xmlString);
    assertXpathEvaluatesTo((String) employeeData.get("TeamId"), "/a:entry/m:properties/d:TeamId/text()", xmlString);
  }

  @Test
  public void serializeWithValueEncoding() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    photoData.put("Type", "< Ö >");

    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos"), photoData,
            DEFAULT_PROPERTIES);
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
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData,
            DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:category", xmlString);
    assertXpathExists("/a:entry/a:category/@term", xmlString);
    assertXpathExists("/a:entry/a:category/@scheme", xmlString);
    assertXpathEvaluatesTo("RefScenario.Employee", "/a:entry/a:category/@term", xmlString);
    assertXpathEvaluatesTo(Edm.NAMESPACE_SCHEME_2007_08, "/a:entry/a:category/@scheme", xmlString);
  }

  @Test
  public void serializeETag() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos"), photoData,
            DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/@m:etag", xmlString);
    assertXpathEvaluatesTo("W/\"1\"", "/a:entry/@m:etag", xmlString);
    assertEquals("W/\"1\"", response.getETag());
  }

  @Test
  public void serializeETagEncoding() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped roomIdProperty = edm.getEntityType("RefScenario", "Room").getProperty("Id");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getConcurrencyMode()).thenReturn(EdmConcurrencyMode.Fixed);
    when(facets.getMaxLength()).thenReturn(3);
    when(((EdmProperty) roomIdProperty).getFacets()).thenReturn(facets);

    roomData.put("Id", "<\">");
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(edm.getDefaultEntityContainer().getEntitySet("Rooms"), roomData, DEFAULT_PROPERTIES);

    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntityProvider should not set content header", response.getContentHeader());
    assertEquals("W/\"<\">.3\"", response.getETag());

    String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());

    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/@m:etag", xmlString);
    assertXpathEvaluatesTo("W/\"<\">.3\"", "/a:entry/@m:etag", xmlString);
  }

  @Test(expected = EntityProviderException.class)
  public void serializeWithFacetsValidation() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped roomNameProperty = edm.getEntityType("RefScenario", "Room").getProperty("Name");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(3);
    when(((EdmProperty) roomNameProperty).getFacets()).thenReturn(facets);

    roomData.put("Name", "1234567");
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
      ser.writeEntry(edm.getDefaultEntityContainer().getEntitySet("Rooms"), roomData, DEFAULT_PROPERTIES);
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
    roomData.put("Name", name);
    AtomEntityProvider ser = createAtomEntityProvider();
    EntityProviderWriteProperties properties = EntityProviderWriteProperties
        .fromProperties(DEFAULT_PROPERTIES).validatingFacets(false).build();
    ODataResponse response =
        ser.writeEntry(edm.getDefaultEntityContainer().getEntitySet("Rooms"), roomData, properties);
    assertNotNull(response);


    assertNotNull(response.getEntity());
    String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());

    assertXpathEvaluatesTo(name, "/a:entry/a:content/m:properties/d:Name/text()", xmlString);
  }

  @Test
  public void serializeCustomMapping() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos"), photoData,
            DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/ру:Содержание", xmlString);
    assertXpathEvaluatesTo((String) photoData.get("Содержание"), "/a:entry/ру:Содержание/text()", xmlString);
    verifyTagOrdering(xmlString, "category", "Содержание", "content", "properties");
  }

  @Test
  public void testCustomProperties() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");

    ODataResponse response = ser.writeEntry(entitySet, photoData, DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/custom:CustomProperty", xmlString);
    assertXpathNotExists("/a:entry/custom:CustomProperty/text()", xmlString);
    assertXpathEvaluatesTo("true", "/a:entry/custom:CustomProperty/@m:null", xmlString);
    verifyTagOrdering(xmlString, "category", "Содержание", "CustomProperty", "content", "properties");
  }

  @Test
  public void testKeepInContentNull() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");

    EdmProperty customProperty = (EdmProperty) entitySet.getEntityType().getProperty("CustomProperty");
    when(customProperty.getCustomizableFeedMappings().isFcKeepInContent()).thenReturn(null);

    ODataResponse response = ser.writeEntry(entitySet, photoData, DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry", xmlString);
    assertXpathExists("/a:entry/custom:CustomProperty", xmlString);
    assertXpathNotExists("/a:entry/custom:CustomProperty/text()", xmlString);
    assertXpathEvaluatesTo("true", "/a:entry/custom:CustomProperty/@m:null", xmlString);
    assertXpathExists("/a:entry/m:properties/d:CustomProperty", xmlString);
    verifyTagOrdering(xmlString, "category", "Содержание", "CustomProperty", "content", "properties");
  }

  @Test
  public void serializeAtomMediaResourceLinks() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    AtomEntityProvider ser = createAtomEntityProvider();
    ODataResponse response =
        ser.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"), employeeData,
            DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    String rel = Edm.NAMESPACE_REL_2007_08 + "ne_Manager";

    assertXpathExists("/a:entry/a:link[@href=\"Employees('1')/ne_Manager\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@rel='" + rel + "']", xmlString);
    assertXpathExists("/a:entry/a:link[@type='application/atom+xml;type=entry']", xmlString);
    assertXpathExists("/a:entry/a:link[@title='ne_Manager']", xmlString);
  }

  @Test
  public void additionalLink() throws Exception {
    Map<String, Map<String, Object>> links = new HashMap<String, Map<String, Object>>();
    links.put("nr_Building", buildingData);
    final ODataResponse response = createAtomEntityProvider().writeEntry(
        MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData,
        EntityProviderWriteProperties.serviceRoot(BASE_URI).additionalLinks(links).build());
    final String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:link[@title='nr_Building']", xmlString);
    assertXpathNotExists("/a:entry/a:link[@href=\"Rooms('1')/nr_Building\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Buildings('1')\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@type='application/atom+xml;type=entry']", xmlString);
  }

  @Test
  public void additionalLinkToOneOfMany() throws Exception {
    Map<String, Map<String, Object>> links = new HashMap<String, Map<String, Object>>();
    links.put("nr_Employees", employeeData);
    final ODataResponse response = createAtomEntityProvider().writeEntry(
        MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData,
        EntityProviderWriteProperties.serviceRoot(BASE_URI).additionalLinks(links).build());
    final String xmlString = verifyResponse(response);

    assertXpathExists("/a:entry/a:link[@title='nr_Employees']", xmlString);
    assertXpathNotExists("/a:entry/a:link[@href=\"Rooms('1')/nr_Employees\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@href=\"Employees('1')\"]", xmlString);
    assertXpathExists("/a:entry/a:link[@type='application/atom+xml;type=feed']", xmlString);
  }

  @Test
  public void serializeWithCustomSrcAttributeOnEmployee() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    Map<String, Object> localEmployeeData = new HashMap<String, Object>(employeeData);
    String mediaResourceSourceKey = "~src";
    localEmployeeData.put(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmMapping mapping = employeesSet.getEntityType().getMapping();
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);

    ODataResponse response = ser.writeEntry(employeesSet, localEmployeeData, DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists(
        "/a:entry/a:link[@href=\"Employees('1')/$value\" and" +
            " @rel=\"edit-media\" and @type=\"application/octet-stream\"]", xmlString);
    assertXpathExists("/a:entry/a:content[@type=\"application/octet-stream\"]", xmlString);
    assertXpathExists("/a:entry/a:content[@src=\"http://localhost:8080/images/image1\"]", xmlString);
  }

  @Test
  public void serializeWithCustomSrcAndTypeAttributeOnEmployee() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    Map<String, Object> localEmployeeData = new HashMap<String, Object>(employeeData);
    String mediaResourceSourceKey = "~src";
    localEmployeeData.put(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    String mediaResourceMimeTypeKey = "~type";
    localEmployeeData.put(mediaResourceMimeTypeKey, "image/jpeg");
    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EdmMapping mapping = employeesSet.getEntityType().getMapping();
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);
    when(mapping.getMediaResourceMimeTypeKey()).thenReturn(mediaResourceMimeTypeKey);
    ODataResponse response = ser.writeEntry(employeesSet, localEmployeeData, DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathExists(
        "/a:entry/a:link[@href=\"Employees('1')/$value\" and" +
            " @rel=\"edit-media\" and @type=\"image/jpeg\"]", xmlString);
    assertXpathExists("/a:entry/a:content[@type=\"image/jpeg\"]", xmlString);
    assertXpathExists("/a:entry/a:content[@src=\"http://localhost:8080/images/image1\"]", xmlString);
  }

  @Test
  public void serializeWithCustomSrcAttributeOnRoom() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    Map<String, Object> localRoomData = new HashMap<String, Object>(roomData);
    String mediaResourceSourceKey = "~src";
    localRoomData.put(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    EdmEntityType roomType = roomsSet.getEntityType();
    EdmMapping mapping = mock(EdmMapping.class);
    when(roomType.getMapping()).thenReturn(mapping);
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);

    ODataResponse response = ser.writeEntry(roomsSet, localRoomData, DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathNotExists(
        "/a:entry/a:link[@href=\"Rooms('1')/$value\" and" +
            " @rel=\"edit-media\" and @type=\"application/octet-stream\"]", xmlString);
    assertXpathNotExists("/a:entry/a:content[@type=\"application/octet-stream\"]", xmlString);
    assertXpathNotExists("/a:entry/a:content[@src=\"http://localhost:8080/images/image1\"]", xmlString);
  }

  @Test
  public void serializeWithCustomSrcAndTypeAttributeOnRoom() throws Exception {
    AtomEntityProvider ser = createAtomEntityProvider();
    Map<String, Object> localRoomData = new HashMap<String, Object>(roomData);
    String mediaResourceSourceKey = "~src";
    localRoomData.put(mediaResourceSourceKey, "http://localhost:8080/images/image1");
    String mediaResourceMimeTypeKey = "~type";
    localRoomData.put(mediaResourceMimeTypeKey, "image/jpeg");
    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    EdmEntityType roomType = roomsSet.getEntityType();
    EdmMapping mapping = mock(EdmMapping.class);
    when(roomType.getMapping()).thenReturn(mapping);
    when(mapping.getMediaResourceSourceKey()).thenReturn(mediaResourceSourceKey);
    when(mapping.getMediaResourceMimeTypeKey()).thenReturn(mediaResourceMimeTypeKey);

    ODataResponse response = ser.writeEntry(roomsSet, localRoomData, DEFAULT_PROPERTIES);
    String xmlString = verifyResponse(response);

    assertXpathNotExists(
        "/a:entry/a:link[@href=\"Rooms('1')/$value\" and" +
            " @rel=\"edit-media\" and @type=\"image/jpeg\"]", xmlString);
    assertXpathNotExists("/a:entry/a:content[@type=\"image/jpeg\"]", xmlString);
    assertXpathNotExists("/a:entry/a:content[@src=\"http://localhost:8080/images/image1\"]", xmlString);
  }

//  @Test
//  public void assureGetMimeTypeWinsOverGetMediaResourceMimeTypeKey() throws Exception {
//    // Keep this test till version 1.2
//    AtomEntityProvider ser = createAtomEntityProvider();
//    Map<String, Object> localEmployeeData = new HashMap<String, Object>(employeeData);
//    String mediaResourceMimeTypeKey = "~type";
//    localEmployeeData.put(mediaResourceMimeTypeKey, "wrong");
//    String originalMimeTypeKey = "~originalType";
//    localEmployeeData.put(originalMimeTypeKey, "right");
//    EdmEntitySet employeesSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
//    EdmMapping mapping = employeesSet.getEntityType().getMapping();
//    when(mapping.getMediaResourceMimeTypeKey()).thenReturn(mediaResourceMimeTypeKey);
//    when(mapping.getMimeType()).thenReturn(originalMimeTypeKey);
//    ODataResponse response = ser.writeEntry(employeesSet, localEmployeeData, DEFAULT_PROPERTIES);
//    String xmlString = verifyResponse(response);
//
//    assertXpathExists("/a:entry/a:content[@type=\"right\"]", xmlString);
//    assertXpathNotExists("/a:entry/a:content[@type=\"wrong\"]", xmlString);
//  }

  private void verifyTagOrdering(final String xmlString, final String... toCheckTags) {
    XMLUnitHelper.verifyTagOrdering(xmlString, toCheckTags);
  }
  
  @Test
  public void unbalancedPropertyEntryWithInlineEntry() throws Exception {
    ExpandSelectTreeNode selectTree = getSelectExpandTree("Rooms('1')", "nr_Building", "nr_Building");

    Map<String, Object> roomData = new HashMap<String, Object>();
    roomData.put("Id", "1");
    roomData.put("Name", "Neu Schwanstein");
    roomData.put("Seats", new Integer(20));
    
    class EntryCallback implements OnWriteEntryContent {
      @Override
      public WriteEntryCallbackResult retrieveEntryResult(final WriteEntryCallbackContext context)
          throws ODataApplicationException {
        Map<String, Object> buildingData = new HashMap<String, Object>();
        buildingData.put("Id", "1");
        buildingData.put("Name", "Building1");
        
        WriteEntryCallbackResult result = new WriteEntryCallbackResult();
        result.setEntryData(buildingData);
        EntityProviderWriteProperties inlineProperties =
            EntityProviderWriteProperties.serviceRoot(BASE_URI).expandSelectTree(
                context.getCurrentExpandSelectTreeNode()).build();
        result.setInlineProperties(inlineProperties);
        return result;
      }
    }
    EntryCallback callback = new EntryCallback();
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", callback);
    
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).expandSelectTree(selectTree).callbacks(callbacks).
        isDataBasedPropertySerialization(true).build();
    AtomEntityProvider provider = createAtomEntityProvider();
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData,
            properties);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyBuilding(buildingXPathString, xmlString);
  }
  
  private ExpandSelectTreeNode getSelectExpandTree(final String pathSegment, final String selectString,
      final String expandString) throws Exception {

    Edm edm = RuntimeDelegate.createEdm(new EdmTestProvider());
    UriParserImpl uriParser = new UriParserImpl(edm);

    List<PathSegment> pathSegments = new ArrayList<PathSegment>();
    pathSegments.add(new ODataPathSegmentImpl(pathSegment, null));

    Map<String, String> queryParameters = new HashMap<String, String>();
    if (selectString != null) {
      queryParameters.put("$select", selectString);
    }
    if (expandString != null) {
      queryParameters.put("$expand", expandString);
    }
    UriInfo uriInfo = uriParser.parse(pathSegments, queryParameters);

    ExpandSelectTreeCreator expandSelectTreeCreator =
        new ExpandSelectTreeCreator(uriInfo.getSelect(), uriInfo.getExpand());
    ExpandSelectTreeNode expandSelectTree = expandSelectTreeCreator.create();
    assertNotNull(expandSelectTree);
    return expandSelectTree;
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
  
  @Test
  public void contentOnlyWithoutKeyWithoutSelectedProperties() throws Exception {
    HashMap<String, Object> employeeData = new HashMap<String, Object>();
    employeeData.put("ManagerId", "1");
    employeeData.put("Age", new Integer(52));
    employeeData.put("RoomId", "1");
    employeeData.put("TeamId", "42");

    List<String> selectedProperties = new ArrayList<String>();
    selectedProperties.add("ManagerId");
    selectedProperties.add("Age");
    selectedProperties.add("RoomId");
    selectedProperties.add("TeamId");
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");

    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).contentOnly(true).build();
    AtomEntityProvider ser = createAtomEntityProvider();
    try {
      ser.writeEntry(entitySet, employeeData, properties);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'EmployeeId'"));
    }
  }
  
  @Test
  public void testWithoutKey() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    List<String> selectedPropertyNames = new ArrayList<String>();
    selectedPropertyNames.add("ManagerId");
    ExpandSelectTreeNode select =
        ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(selectedPropertyNames).build();

    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).expandSelectTree(select).build();

    Map<String, Object> localEmployeeData = new HashMap<String, Object>();
    localEmployeeData.put("ManagerId", "1");

    AtomEntityProvider ser = createAtomEntityProvider();
    try {
    ser.writeEntry(entitySet, localEmployeeData, properties);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'EmployeeId'"));
    }
  }
  
  @Test
  public void testWithoutCompositeKey() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");
    
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).build();

    Map<String, Object> photoData = new HashMap<String, Object>();
    photoData.put("Name", "Mona Lisa");

    AtomEntityProvider ser = createAtomEntityProvider();
    try {
    ser.writeEntry(entitySet, photoData, properties);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("The metadata do not allow a null value for property 'Id'"));
    }
  }
  
  @Test
  public void testWithoutCompositeKeyWithOneKeyNull() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmEntitySet entitySet = edm.getEntityContainer("Container2").getEntitySet("Photos");
    
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).build();

    Map<String, Object> photoData = new HashMap<String, Object>();
    photoData.put("Name", "Mona Lisa");
    photoData.put("Id", Integer.valueOf(1));
    
    EdmTyped typeProperty = edm.getEntityContainer("Container2").getEntitySet("Photos").
        getEntityType().getProperty("Type");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getConcurrencyMode()).thenReturn(EdmConcurrencyMode.Fixed);
    when(facets.getMaxLength()).thenReturn(3);
    when(((EdmProperty) typeProperty).getFacets()).thenReturn(facets);

    AtomEntityProvider ser = createAtomEntityProvider();
    try {
    ser.writeEntry(entitySet, photoData, properties);
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
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).omitETag(true).
        isDataBasedPropertySerialization(true).build();
    AtomEntityProvider ser = createAtomEntityProvider();

    Map<String, Object> orgData = new HashMap<String, Object>();
    orgData.put("Id", "1");
    try {
    ser.writeEntry(entitySet, orgData, properties);
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
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).omitETag(true).
        isDataBasedPropertySerialization(true).build();
    AtomEntityProvider ser = createAtomEntityProvider();

    Map<String, Object> orgData = new HashMap<String, Object>();
    orgData.put("Id", "1");
    orgData.put("Name", "Org1");
    try {
    ser.writeEntry(entitySet, orgData, properties);
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
    final EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.serviceRoot(BASE_URI).omitETag(true).
        isDataBasedPropertySerialization(true).build();
    AtomEntityProvider ser = createAtomEntityProvider();

    Map<String, Object> orgData = new HashMap<String, Object>();
    orgData.put("Id", "1");
    orgData.put("Name", "Org1");
    try {
    ser.writeEntry(entitySet, orgData, properties);
    } catch (EntityProviderProducerException e) {
      assertTrue(e.getMessage().contains("do not allow to format the value 'Org1' for property 'Name'."));
    }
  }
}
