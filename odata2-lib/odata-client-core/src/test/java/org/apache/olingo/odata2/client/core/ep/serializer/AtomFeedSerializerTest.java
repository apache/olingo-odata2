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

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.client.api.ep.Entity;
import org.apache.olingo.odata2.client.api.ep.EntityCollection;
import org.apache.olingo.odata2.client.api.ep.EntityCollectionSerializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntitySerializerProperties;
import org.apache.olingo.odata2.client.core.ep.AbstractProviderTest;
import org.apache.olingo.odata2.client.core.ep.AtomSerializerDeserializer;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

/**
 *  
 */
public class AtomFeedSerializerTest extends AbstractProviderTest {
  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  private String employeeXPathString = "/a:entry/a:link[@href=\"Rooms('1')/nr_Employees\" and @title='nr_Employees']";

  private static final String EXP_MSG = "Write properties are mandatory for XML.";
  private static final String ERROR_MSG = "Entity or expanded entity cannot have null value.";
  
  public AtomFeedSerializerTest(final StreamWriterImplType type) {
    super(type);
  }

  private GetEntitySetUriInfo view;

  @Before
  public void before() throws Exception {
    initializeRoomData(1);

    view = mock(GetEntitySetUriInfo.class);

    EdmEntitySet set = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    when(view.getTargetEntitySet()).thenReturn(set);
  }

  @Test
  public void entityWithInlineFeed() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    EntityCollection listData = new EntityCollection();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    listData.addEntity(data);
    data.setWriteProperties(properties);

    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    data.setWriteProperties(properties);
    listData.addEntity(data);
    EntityCollectionSerializerProperties inlineProperties =
        EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build();

    listData.setCollectionProperties(inlineProperties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyEmployees(employeeXPathString, xmlString);
  }

  public void entityWithPartialInlineFeed() throws Exception {
    expectedEx.expect(EntityProviderException.class);
    expectedEx.expectMessage("Entity or expanded entity cannot have null value.");
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    EntityCollection listData = new EntityCollection();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    listData.addEntity(data);
    data.setWriteProperties(properties);

    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    data.setWriteProperties(properties);
    listData.addEntity(data);

    data = new Entity();
    data.setWriteProperties(properties);
    listData.addEntity(data);
    EntityCollectionSerializerProperties inlineProperties =
        EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build();

    listData.setCollectionProperties(inlineProperties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyEmployees(employeeXPathString, xmlString);
  }

  public void entityWithPartialNullInlineFeed() throws Exception {
    expectedEx.expect(EntityProviderException.class);
    expectedEx.expectMessage("Entity or expanded entity cannot have null value.");
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    EntityCollection listData = new EntityCollection();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).build();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    listData.addEntity(data);
    data.setWriteProperties(properties);

    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    data.setWriteProperties(properties);
    listData.addEntity(data);

    listData.addEntity(null);
    EntityCollectionSerializerProperties inlineProperties =
        EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build();

    listData.setCollectionProperties(inlineProperties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyEmployees(employeeXPathString, xmlString);
  }

  @Test(expected = EntityProviderException.class)
  public void entityWithoutIdInlineFeed() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    EntityCollection listData = new EntityCollection();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    Entity data = new Entity();
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    listData.addEntity(data);
    data.setWriteProperties(properties);
    listData.addEntity(data);
    EntityCollectionSerializerProperties inlineProperties =
        EntityCollectionSerializerProperties.serviceRoot(BASE_URI)
            .build();
    listData.setCollectionProperties(inlineProperties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyEmployees(employeeXPathString, xmlString);
  }

  @Test
  public void entityWithEmptyFeed() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    EntityCollection listData = new EntityCollection();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();

    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
  }

  @Test
  public void entityWithEmptyEntityInFeed() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    EntityCollection listData = new EntityCollection();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(false).build();
    Entity data = new Entity();
    data.setWriteProperties(properties);
    listData.addEntity(data);
    EntityCollectionSerializerProperties inlineProperties =
        EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build();
    listData.setCollectionProperties(inlineProperties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    assertXpathExists(employeeXPathString, xmlString);
    assertXpathExists(employeeXPathString + "/m:inline", xmlString);

    assertXpathExists(employeeXPathString + "/m:inline/a:feed[@xml:base='" + BASE_URI + "']", xmlString);
    assertXpathExists(employeeXPathString + "/m:inline/a:feed/a:entry", xmlString);
  }

  @Test(expected = EntityProviderException.class)
  public void entityWithNullFeed() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).build();

    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", null);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
  }

  @Test(expected = EntityProviderException.class)
  public void entityWithNullEntityInFeed() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));

    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI).build();
    EntityCollection listData = new EntityCollection();
    listData.addEntity(null);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
  }

  @Test
  public void entityWithoutInlineEntityProperty() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    EntityCollection listData = new EntityCollection();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    listData.addEntity(data);

    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    listData.addEntity(data);
    listData.setGlobalEntityProperties(properties);
    EntityCollectionSerializerProperties inlineProperties =
        EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build();
    listData.setCollectionProperties(inlineProperties);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyEmployees(employeeXPathString, xmlString);
  }

  @Test
  public void entityWithoutInlineCollectionProperty() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    EntityCollection listData = new EntityCollection();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    data.setWriteProperties(properties);
    listData.addEntity(data);

    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    data.setWriteProperties(properties);
    listData.addEntity(data);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyEmployees(employeeXPathString, xmlString);
  }

  @Test
  public void entityWithoutPropertyInlineFeed() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    EntityCollection listData = new EntityCollection();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(false).build();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    listData.addEntity(data);

    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    listData.setGlobalEntityProperties(properties);
    listData.addEntity(data);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyEmployeesContent(employeeXPathString, xmlString);
  }

  @Test
  public void entityWithPropertyAtCollectionForInlineFeed() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    EntityCollection listData = new EntityCollection();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(false).build();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    listData.addEntity(data);

    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    listData.addEntity(data);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);

    verifyEmployeesContent(employeeXPathString, xmlString);
  }

  @Test
  public void entityWithTwoPropertyInlineFeed() throws Exception {
    Entity roomData = new Entity();
    roomData.addProperty("Id", "1");
    roomData.addProperty("Name", "Neu Schwanstein");
    roomData.addProperty("Seats", new Integer(20));
    EntityCollection listData = new EntityCollection();
    EntitySerializerProperties properties =
        EntitySerializerProperties.serviceRoot(BASE_URI)
            .includeMetadata(true).build();
    URI COLL_URI = new URI("http://host:80/service/collection/");
    EntityCollectionSerializerProperties collProperties =
        EntityCollectionSerializerProperties.serviceRoot(COLL_URI).build();
    listData.setCollectionProperties(collProperties);
    listData.setGlobalEntityProperties(properties);
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    listData.addEntity(data);

    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");

    listData.addEntity(data);
    AtomSerializerDeserializer provider = createAtomEntityProvider();
    roomData.addNavigation("nr_Employees", listData);
    roomData.setWriteProperties(properties);
    ODataResponse response =
        provider.writeEntry(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"), roomData);

    String xmlString = verifyResponse(response);
    assertXpathNotExists("/a:entry/m:properties", xmlString);
    assertXpathExists("/a:entry/a:link", xmlString);
    verifyEmployeesForCollection(employeeXPathString, xmlString, COLL_URI);
  }

  private void verifyEmployees(final String path, final String xmlString) throws XpathException, IOException,
      SAXException {
    assertXpathExists(path, xmlString);
    assertXpathExists(path + "/m:inline", xmlString);

    assertXpathExists(path + "/m:inline/a:feed[@xml:base='" + BASE_URI + "']", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:id", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:title", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:updated", xmlString);

    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:category", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:link", xmlString);

    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:content", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:EmployeeId", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:EmployeeName", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:RoomId", xmlString);

    assertXpathExists("/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Seats", xmlString);

  }

  private void verifyEmployeesContent(final String path, final String xmlString) throws XpathException, IOException,
      SAXException {
    assertXpathExists(path, xmlString);
    assertXpathExists(path + "/m:inline", xmlString);

    assertXpathExists(path + "/m:inline/a:feed[@xml:base='" + BASE_URI + "']", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:EmployeeId", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:EmployeeName", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:RoomId", xmlString);

    assertXpathExists("/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Seats", xmlString);

  }

  private void verifyEmployeesForCollection(final String path, final String xmlString, URI coll_uri)
      throws XpathException, IOException,
      SAXException {
    assertXpathExists(path, xmlString);
    assertXpathExists(path + "/m:inline", xmlString);

    assertXpathExists(path + "/m:inline/a:feed[@xml:base='" + coll_uri + "']", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:id", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:title", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:updated", xmlString);

    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:category", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:link", xmlString);

    assertXpathExists(path + "/m:inline/a:feed/a:entry/a:content", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:EmployeeId", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:EmployeeName", xmlString);
    assertXpathExists(path + "/m:inline/a:feed/a:entry/m:properties/d:RoomId", xmlString);

    assertXpathExists("/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:entry/a:content/m:properties/d:Seats", xmlString);

  }

  private String verifyResponse(final ODataResponse response) throws IOException {
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntityProvider should not set content header", response.getContentHeader());
    String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());
    return xmlString;
  }

  @Test
  public void feed() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    EntityCollection teamsData = new EntityCollection();
    Entity team1Data = new Entity();
    team1Data.addProperty("Id", "1");
    team1Data.addProperty("isScrumTeam", true);
    Entity team2Data = new Entity();
    team2Data.addProperty("Id", "2");
    team2Data.addProperty("isScrumTeam", false);
    teamsData.addEntity(team1Data);
    teamsData.addEntity(team2Data);
    teamsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build());

    final ODataResponse response = new AtomSerializerDeserializer().writeFeed(entitySet, teamsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String xmlString = verifyResponse(response);
    assertNotNull(xmlString);
    assertXpathExists("/a:feed", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:isScrumTeam", xmlString);    
  }
  
  @Test
  public void feedWithEmptyData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    EntityCollection teamsData = new EntityCollection();
    Entity team1Data = new Entity();
    team1Data.addProperty("Id", "1");
    team1Data.addProperty("isScrumTeam", true);
    Entity team2Data = new Entity();
    teamsData.addEntity(team1Data);
    teamsData.addEntity(team2Data);
    teamsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build());

    final ODataResponse response = new AtomSerializerDeserializer().writeFeed(entitySet, teamsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String xmlString = verifyResponse(response);
    assertNotNull(xmlString);
    assertTrue(xmlString.contains("<entry><content type=\"application/xml\">"
        + "<m:properties><d:Id>1</d:Id><d:isScrumTeam>true</d:isScrumTeam></m:properties>"
        + "</content></entry>"));
    assertTrue(xmlString.contains("<entry><content type=\"application/xml\"/></entry>"));
  }
  
  @Test
  public void feedWithNullData() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    
    try {
      new AtomSerializerDeserializer().writeFeed(entitySet, null);
    } catch (EntityProviderException e) {
      assertEquals(ERROR_MSG, e.getMessage());
    }
  }
  
  @Test
  public void feedWithMetadata() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    EntityCollection teamsData = new EntityCollection();
    Entity team1Data = new Entity();
    team1Data.addProperty("Id", "1");
    team1Data.addProperty("isScrumTeam", true);
    team1Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(true).build());
    Entity team2Data = new Entity();
    team2Data.addProperty("Id", "2");
    team2Data.addProperty("isScrumTeam", false);
    team2Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).includeMetadata(true).build());
    teamsData.addEntity(team1Data);
    teamsData.addEntity(team2Data);
    teamsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build());

    final ODataResponse response = new AtomSerializerDeserializer().writeFeed(entitySet, teamsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());

    final String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(xmlString);
    assertXpathExists("/a:feed", xmlString);
    assertXpathExists("/a:feed/a:entry/a:id", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:isScrumTeam", xmlString);
  }
  
  @Test
  public void feedWithGlobalEntityProperties() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    EntityCollection teamsData = new EntityCollection();
    Entity team1Data = new Entity();
    team1Data.addProperty("Id", "1");
    team1Data.addProperty("isScrumTeam", true);
    Entity team2Data = new Entity();
    team2Data.addProperty("Id", "2");
    team2Data.addProperty("isScrumTeam", false);
    teamsData.addEntity(team1Data);
    teamsData.addEntity(team2Data);
    teamsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build());
    teamsData.setGlobalEntityProperties(EntitySerializerProperties.serviceRoot(BASE_URI).
        includeMetadata(true).build());
    
    final ODataResponse response = new AtomSerializerDeserializer().writeFeed(entitySet, teamsData);
    final String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(xmlString);
    assertXpathExists("/a:feed", xmlString);
    assertXpathExists("/a:feed/a:entry/a:id", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:isScrumTeam", xmlString);
  }
  
  @Test
  public void feedWithoutCollectionProperties() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    EntityCollection teamsData = new EntityCollection();
    Entity team1Data = new Entity();
    team1Data.addProperty("Id", "1");
    team1Data.addProperty("isScrumTeam", true);
    team1Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).
        includeMetadata(true).build());
    Entity team2Data = new Entity();
    team2Data.addProperty("Id", "2");
    team2Data.addProperty("isScrumTeam", false);
    team2Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).
        includeMetadata(true).build());
    teamsData.addEntity(team1Data);
    teamsData.addEntity(team2Data);
    
    try {
      new AtomSerializerDeserializer().writeFeed(entitySet, teamsData);
    } catch (EntityProviderException e) {
      assertEquals(EXP_MSG, e.getMessage());
    }
  }
  
  @Test
  public void unbalancedPropertyFeedWithInlineFeed() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    EntityCollection roomsData = new EntityCollection();
    Entity room1Data = new Entity();
    room1Data.addProperty("Id", "1");
    room1Data.addProperty("Name", "Neu Schwanstein");
    room1Data.addProperty("Seats", new Integer(20));
    room1Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).
        validatingFacets(true).build());
    
    EntityCollection innerData = new EntityCollection();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    room1Data.addNavigation("nr_Employees", innerData);
    roomsData.addEntity(room1Data);
    
    Entity room2Data = new Entity();
    room2Data.addProperty("Id", "2");
    room2Data.addProperty("Name", "John");
    room2Data.addProperty("Seats", new Integer(10));
    room2Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).
        validatingFacets(true).build());
    
    innerData = new EntityCollection();
    data = new Entity();
    data.addProperty("EmployeeId", "2");
    data.addProperty("EmployeeName", "EmpName2");
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("EmployeeId", "2");
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    room2Data.addNavigation("nr_Employees", innerData);
    roomsData.addEntity(room2Data);
    
    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    roomsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build());
    final ODataResponse response =
        new AtomSerializerDeserializer().writeFeed(entitySet, roomsData);
    final String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(xmlString);
    verifyRoomsFeedWithInlineEmployeeFeed(xmlString, BASE_URI);
  }
  
  private void verifyRoomsFeedWithInlineEmployeeFeed(final String xmlString, URI coll_uri)
      throws XpathException, IOException,
      SAXException {
    assertXpathExists("/a:feed[@xml:base='" + coll_uri + "']", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline", xmlString);

    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:feed[@xml:base='" + coll_uri + "']", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:feed/a:entry", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:feed/a:entry/m:properties/d:EmployeeId", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:feed/a:entry/m:properties/d:RoomId", xmlString);

    assertXpathExists("/a:feed/a:entry/a:content", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Seats", xmlString);

  }
  
  @Test
  public void unbalancedPropertyFeedWithInlineFeedAndNavigationLink() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    EntityCollection roomsData = new EntityCollection();
    Entity room1Data = new Entity();
    room1Data.addProperty("Id", "1");
    room1Data.addProperty("Name", "Neu Schwanstein");
    room1Data.addProperty("Seats", new Integer(20));
    room1Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).
        validatingFacets(true).build());
    
    EntityCollection innerData = new EntityCollection();
    Entity data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("EmployeeId", "1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    room1Data.addNavigation("nr_Employees", innerData);
    Map<String, Object> link1 = new HashMap<String, Object>();
    link1.put("Id", 1);
    room1Data.addNavigation("nr_Building", link1);
    roomsData.addEntity(room1Data);
    
    Entity room2Data = new Entity();
    room2Data.addProperty("Id", "2");
    room2Data.addProperty("Name", "John");
    room2Data.addProperty("Seats", new Integer(10));
    room2Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).
        validatingFacets(true).build());
    
    innerData = new EntityCollection();
    data = new Entity();
    data.addProperty("EmployeeId", "2");
    data.addProperty("EmployeeName", "EmpName2");
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("EmployeeId", "2");
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    room2Data.addNavigation("nr_Employees", innerData);
    Map<String, Object> link2 = new HashMap<String, Object>();
    link2.put("Id", 2);
    room2Data.addNavigation("nr_Building", link2);
    roomsData.addEntity(room2Data);
    roomsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build());
    
    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new AtomSerializerDeserializer().writeFeed(entitySet, roomsData);
    
    final String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(xmlString);
    verifyRoomsFeedWithInlineEmployeeFeedWithNavigationLinks(xmlString, BASE_URI);
  }

  @Test
  public void unbalancedPropertyFeedWithInlineFeedAndNavigationLinkWithoutKey() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    EntityCollection roomsData = new EntityCollection();
    Entity room1Data = new Entity();
    room1Data.addProperty("Name", "Neu Schwanstein");
    room1Data.addProperty("Seats", new Integer(20));
    room1Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).
        validatingFacets(false).isKeyAutoGenerated(true).build());
    
    EntityCollection innerData = new EntityCollection();
    Entity data = new Entity();
    data.addProperty("EmployeeName", "EmpName1");
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("RoomId", "1");
    innerData.addEntity(data);
    
    room1Data.addNavigation("nr_Employees", innerData);
    Map<String, Object> link1 = new HashMap<String, Object>();
    link1.put("Id", 1);
    room1Data.addNavigation("nr_Building", link1);
    roomsData.addEntity(room1Data);
    
    Entity room2Data = new Entity();
    room2Data.addProperty("Name", "John");
    room2Data.addProperty("Seats", new Integer(10));
    room2Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).
        validatingFacets(false).isKeyAutoGenerated(true).build());
    
    innerData = new EntityCollection();
    data = new Entity();
    data.addProperty("EmployeeName", "EmpName2");
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    data = new Entity();
    data.addProperty("RoomId", "2");
    innerData.addEntity(data);
    
    room2Data.addNavigation("nr_Employees", innerData);
    Map<String, Object> link2 = new HashMap<String, Object>();
    link2.put("Id", 2);
    room2Data.addNavigation("nr_Building", link2);
    roomsData.addEntity(room2Data);
    roomsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build());
    
    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new AtomSerializerDeserializer().writeFeed(entitySet, roomsData);
    
    final String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(xmlString);
    verifyRoomsFeedWithInlineEmployeeFeedWithNavigationLinksWithoutKeys(xmlString, BASE_URI);
  }
  
  private void verifyRoomsFeedWithInlineEmployeeFeedWithNavigationLinks(String xmlString, URI coll_uri) 
      throws XpathException, IOException, SAXException {
    assertXpathExists("/a:feed[@xml:base='" + coll_uri + "']", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link[@href=\"Buildings('1')\"]", xmlString);

    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:feed[@xml:base='" + coll_uri + "']", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:feed/a:entry", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:feed/a:entry/m:properties/d:EmployeeId", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:feed/a:entry/m:properties/d:RoomId", xmlString);

    assertXpathExists("/a:feed/a:entry/a:content", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Seats", xmlString);
    
  }
  
  @Test
  public void unbalancedPropertyFeedWithInlineEntryAndNavigationLink() throws Exception {
    Edm edm = MockFacade.getMockEdm();
    EdmTyped imageUrlProperty = edm.getEntityType("RefScenario", "Employee").getProperty("ImageUrl");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(1);
    when(((EdmProperty) imageUrlProperty).getFacets()).thenReturn(facets);

    EntityCollection roomsData = new EntityCollection();
    Entity room1Data = new Entity();
    room1Data.addProperty("Id", "1");
    room1Data.addProperty("Name", "Neu Schwanstein");
    room1Data.addProperty("Seats", new Integer(20));
    room1Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).
        validatingFacets(true).build());
    
    Map<String, Object> link1 = new HashMap<String, Object>();
    link1.put("Id", 1);
    room1Data.addNavigation("nr_Building", link1);
    roomsData.addEntity(room1Data);
    
    Entity room2Data = new Entity();
    room2Data.addProperty("Id", "2");
    room2Data.addProperty("Name", "John");
    room2Data.addProperty("Seats", new Integer(10));
    room2Data.setWriteProperties(EntitySerializerProperties.serviceRoot(BASE_URI).
        validatingFacets(true).build());
    
    Entity data = new Entity();
    data.addProperty("Id", "2");
    data.addProperty("Name", "Team2");
    
    room2Data.addNavigation("nr_Building", data);
    roomsData.addEntity(room2Data);
    roomsData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build());
    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Rooms");
    final ODataResponse response =
        new AtomSerializerDeserializer().writeFeed(entitySet, roomsData);
    assertNotNull(response);
    assertNotNull(response.getEntity());

    final String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(xmlString);
    assertXpathExists("/a:feed[@xml:base=\"http://host:80/service/\"]", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:entry/a:content/m:properties", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:entry/a:content/m:properties/d:Id", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link[@href=\"Buildings('1')\"]", xmlString);
  }
  
  private void verifyRoomsFeedWithInlineEmployeeFeedWithNavigationLinksWithoutKeys(String xmlString, URI coll_uri) 
      throws XpathException, IOException, SAXException {
    assertXpathExists("/a:feed[@xml:base='" + coll_uri + "']", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link[@href=\"Rooms('A')/nr_Employees\"]", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link[@href=\"Buildings('1')\"]", xmlString);

    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:feed[@xml:base='" + coll_uri + "']", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:feed/a:entry", xmlString);
    assertXpathNotExists("/a:feed/a:entry/a:link/m:inline/a:feed/a:entry/m:properties/d:EmployeeId", xmlString);
    assertXpathExists("/a:feed/a:entry/a:link/m:inline/a:feed/a:entry/m:properties/d:RoomId", xmlString);

    assertXpathExists("/a:feed/a:entry/a:content", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Name", xmlString);
    assertXpathExists("/a:feed/a:entry/a:content/m:properties/d:Seats", xmlString);
    
  }
}
