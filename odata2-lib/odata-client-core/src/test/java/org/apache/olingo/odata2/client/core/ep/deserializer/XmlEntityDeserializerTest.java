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
package org.apache.olingo.odata2.client.core.ep.deserializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.EntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.MediaMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.FeedMetadata;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.client.api.ep.DeserializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntityStream;
import org.apache.olingo.odata2.client.api.ep.callback.OnDeserializeInlineContent;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *  
 */
public class XmlEntityDeserializerTest extends AbstractXmlDeserializerTest {

  public XmlEntityDeserializerTest(final StreamWriterImplType type) {
    super(type);
  }

  private static final Logger LOG = Logger.getLogger(XmlEntityDeserializerTest.class.getName());
  static {
    LOG.setLevel(Level.OFF);
  }

  public static final String EMPLOYEE_1_XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          +
          "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
          "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
          "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xml:base=\"http://localhost:19000/\"  " +
          "m:etag=\"W/&quot;1&quot;\">"
          +
          "  <id>http://localhost:19000/Employees('1')</id>"
          +
          "  <title type=\"text\">Walter Winter</title>"
          +
          "  <updated>1999-01-01T00:00:00Z</updated>"
          +
          "  <category term=\"RefScenario.Employee\" " +
          "scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>"
          +
          "  <link href=\"Employees('1')\" rel=\"edit\" title=\"Employee\"/>"
          +
          "  <link href=\"Employees('1')/$value\" rel=\"edit-media\" " +
          "type=\"application/octet-stream\" m:etag=\"mmEtag\"/>"
          +
          "  <link href=\"Employees('1')/ne_Room\" " +
          "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Room\" " +
          "type=\"application/atom+xml; type=entry\" title=\"ne_Room\"/>"
          +
          "  <link href=\"Employees('1')/ne_Manager\" " +
          "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Manager\" " +
          "type=\"application/atom+xml; type=entry\" title=\"ne_Manager\"/>"
          +
          "  <link href=\"Employees('1')/ne_Team\" " +
          "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Team\" " +
          "type=\"application/atom+xml; type=entry\" title=\"ne_Team\"/>"
          +
          "  <content type=\"application/octet-stream\" src=\"Employees('1')/$value\"/>" +
          "  <m:properties>" +
          "    <d:EmployeeId>1</d:EmployeeId>" +
          "    <d:EmployeeName>Walter Winter</d:EmployeeName>" +
          "    <d:ManagerId>1</d:ManagerId>" +
          "    <d:RoomId>1</d:RoomId>" +
          "    <d:TeamId>1</d:TeamId>" +
          "    <d:Location m:type=\"RefScenario.c_Location\">" +
          "      <d:Country>Germany</d:Country>" +
          "      <d:City m:type=\"RefScenario.c_City\">" +
          "        <d:PostalCode>69124</d:PostalCode>" +
          "        <d:CityName>Heidelberg</d:CityName>" +
          "      </d:City>" +
          "    </d:Location>" +
          "    <d:Age>52</d:Age>" +
          "    <d:EntryDate>1999-01-01T00:00:00</d:EntryDate>" +
          "    <d:ImageUrl>/SAP/PUBLIC/BC/NWDEMO_MODEL/IMAGES/Employee_1.png</d:ImageUrl>" +
          "  </m:properties>" +
          "</entry>";

  public static final String EMPLOYEE_1_ROOM_XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          +
          "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
          "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
          "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xml:base=\"http://localhost:19000/\"  " +
          "m:etag=\"W/&quot;1&quot;\">"
          +
          "  <id>http://localhost:19000/Employees('1')</id>"
          +
          "  <title type=\"text\">Walter Winter</title>"
          +
          "  <updated>1999-01-01T00:00:00Z</updated>"
          +
          "  <category term=\"RefScenario.Employee\" " +
          "scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>"
          +
          "  <link href=\"Employees('1')\" rel=\"edit\" title=\"Employee\"/>"
          +
          "  <link href=\"Employees('1')/$value\" rel=\"edit-media\" type=\"application/octet-stream\" " +
          "m:etag=\"mmEtag\"/>"
          +
          "  <link href=\"Employees('1')/ne_Room\" "
          +
          "       rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Room\" "
          +
          "       type=\"application/atom+xml; type=entry\" title=\"ne_Room\">"
          +
          "  <m:inline>"
          +
          "  <entry m:etag=\"W/1\" xml:base=\"http://some.host.com/service.root/\">"
          +
          "  <id>http://some.host.com/service.root/Rooms('1')</id><title " +
          "type=\"text\">Room 1</title><updated>2013-04-10T10:19:12Z</updated>"
          +
          "  <content type=\"application/xml\">" +
          "    <m:properties>" +
          "    <d:Id>1</d:Id>" +
          "    <d:Name>Room 1</d:Name>" +
          "    <d:Seats>1</d:Seats>" +
          "    <d:Version>1</d:Version>" +
          "    </m:properties>" +
          "    </content>" +
          "    </entry>" +
          "  </m:inline>" +
          " </link>" +
          "  <content type=\"application/octet-stream\" src=\"Employees('1')/$value\"/>" +
          "  <m:properties>" +
          "    <d:EmployeeId>1</d:EmployeeId>" +
          "    <d:EmployeeName>Walter Winter</d:EmployeeName>" +
          "  </m:properties>" +
          "</entry>";

  public static final String EMPLOYEE_1_NULL_ROOM_XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          +
          "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
          "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
          "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xml:base=\"http://localhost:19000/\"  " +
          "m:etag=\"W/&quot;1&quot;\">"
          +
          "  <id>http://localhost:19000/Employees('1')</id>"
          +
          "  <title type=\"text\">Walter Winter</title>"
          +
          "  <updated>1999-01-01T00:00:00Z</updated>"
          +
          "  <category term=\"RefScenario.Employee\" " +
          "scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>"
          +
          "  <link href=\"Employees('1')\" rel=\"edit\" title=\"Employee\"/>"
          +
          "  <link href=\"Employees('1')/$value\" rel=\"edit-media\" type=\"application/octet-stream\" " +
          "m:etag=\"mmEtag\"/>"
          +
          "  <link href=\"Employees('1')/ne_Room\" " +
          "       rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Room\" " +
          "       type=\"application/atom+xml; type=entry\" title=\"ne_Room\">" +
          "  <m:inline/>" +
          " </link>" +
          "  <content type=\"application/octet-stream\" src=\"Employees('1')/$value\"/>" +
          "  <m:properties>" +
          "    <d:EmployeeId>1</d:EmployeeId>" +
          "    <d:EmployeeName>Walter Winter</d:EmployeeName>" +
          "  </m:properties>" +
          "</entry>";

  private static final String ROOM_1_XML =
      "<?xml version='1.0' encoding='UTF-8'?>"
          +
          "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
          "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
          "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
          "xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
          +
          "  <id>http://localhost:19000/test/Rooms('1')</id>"
          +
          "  <title type=\"text\">Room 1</title>"
          +
          "  <updated>2013-01-11T13:50:50.541+01:00</updated>"
          +
          "  <category term=\"RefScenario.Room\" " +
          "scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>"
          +
          "  <link href=\"Rooms('1')\" rel=\"edit\" title=\"Room\"/>"
          +
          "  <link href=\"Rooms('1')/nr_Employees\" " +
          "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/nr_Employees\" " +
          "type=\"application/atom+xml; type=feed\" title=\"nr_Employees\"/>"
          +
          "  <link href=\"Rooms('1')/nr_Building\" " +
          "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/nr_Building\" " +
          "type=\"application/atom+xml; type=entry\" title=\"nr_Building\"/>"
          +
          "  <content type=\"application/xml\">" +
          "    <m:properties>" +
          "      <d:Id>1</d:Id>" +
          "    </m:properties>" +
          "  </content>" +
          "</entry>";

  private static final String ROOM_1_NULL_EMPLOYEE_XML =
      "<?xml version='1.0' encoding='UTF-8'?>"
          +
          "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
          "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
          "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
          "xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
          +
          "  <id>http://localhost:19000/test/Rooms('1')</id>"
          +
          "  <title type=\"text\">Room 1</title>"
          +
          "  <updated>2013-01-11T13:50:50.541+01:00</updated>"
          +
          "  <category term=\"RefScenario.Room\" " +
          "scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>"
          +
          "  <link href=\"Rooms('1')\" rel=\"edit\" title=\"Room\"/>"
          +
          "  <link href=\"Rooms('1')/nr_Employees\" " +
          "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/nr_Employees\" "
          +
          "        type=\"application/atom+xml; type=feed\" title=\"nr_Employees\">"
          +
          " <m:inline/> </link> "
          +
          "  <link href=\"Rooms('1')/nr_Building\" " +
          "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/nr_Building\" " +
          "type=\"application/atom+xml; type=entry\" title=\"nr_Building\"/>"
          +
          "  <content type=\"application/xml\">" +
          "    <m:properties>" +
          "      <d:Id>1</d:Id>" +
          "    </m:properties>" +
          "  </content>" +
          "</entry>";

  private static final String PHOTO_XML =
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
          +
          "<entry m:etag=\"W/&quot;1&quot;\" xml:base=\"http://localhost:19000/test\" "
          +
          "xmlns=\"http://www.w3.org/2005/Atom\" "
          +
          "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" "
          +
          "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\">"
          +
          "  <id>http://localhost:19000/test/Container2.Photos(Id=1,Type='image%2Fpng')</id>"
          +
          "  <title type=\"text\">Photo1</title><updated>2013-01-16T12:57:43Z</updated>"
          +
          "  <category term=\"RefScenario2.Photo\" " +
          "scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>"
          +
          "  <link href=\"Container2.Photos(Id=1,Type='image%2Fpng')\" rel=\"edit\" title=\"Photo\"/>" +
          "  <link href=\"Container2.Photos(Id=1,Type='image%2Fpng')/$value\" rel=\"edit-media\" type=\"image/png\"/>" +
          "  <ру:Содержание xmlns:ру=\"http://localhost\">Образ</ру:Содержание>" +
          "  <content type=\"image/png\" src=\"Container2.Photos(Id=1,Type='image%2Fpng')/$value\"/>" +
          "  <m:properties>" +
          "    <d:Id>1</d:Id>" +
          "    <d:Name>Photo1</d:Name>" +
          "    <d:Type>image/png</d:Type>" +
          "  </m:properties>" +
          "</entry>";

  private static final String PHOTO_XML_INVALID_MAPPING =
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
          +
          "<entry m:etag=\"W/&quot;1&quot;\" xml:base=\"http://localhost:19000/test\" "
          +
          "xmlns=\"http://www.w3.org/2005/Atom\" "
          +
          "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" "
          +
          "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\">"
          +
          "  <id>http://localhost:19000/test/Container2.Photos(Id=1,Type='image%2Fpng')</id>"
          +
          "  <title type=\"text\">Photo1</title><updated>2013-01-16T12:57:43Z</updated>"
          +
          "  <category term=\"RefScenario2.Photo\" " +
          "scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>"
          +
          "  <link href=\"Container2.Photos(Id=1,Type='image%2Fpng')\" rel=\"edit\" title=\"Photo\"/>" +
          "  <link href=\"Container2.Photos(Id=1,Type='image%2Fpng')/$value\" rel=\"edit-media\" type=\"image/png\"/>" +
          "  <ру:Содержание xmlns:ру=\"http://localhost\">Образ</ру:Содержание>" +
          "  <ig:ignore xmlns:ig=\"http://localhost\">ignore</ig:ignore>" + // 406 Bad Request
          "  <content type=\"image/png\" src=\"Container2.Photos(Id=1,Type='image%2Fpng')/$value\"/>" +
          "  <m:properties>" +
          "    <d:Id>1</d:Id>" +
          "    <d:Name>Photo1</d:Name>" +
          "    <d:Type>image/png</d:Type>" +
          "  </m:properties>" +
          "</entry>";

  @Test
  public void readContentOnlyEmployee() throws Exception {
    // prepare
    String content = readFile("EmployeeContentOnly.xml");
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream contentBody = createContentAsStream(content);
    EntityStream stream = new EntityStream();
    stream.setReadProperties(DeserializerProperties.init().build());
    stream.setContent(contentBody);
    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result =
        xec.readEntry(entitySet, stream);

    // verify
    assertEquals(9, result.getProperties().size());
  }

  @Test
  public void readDeltaLink() throws Exception {
    // prepare
    String content = readFile("feed_with_delta_link.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream reqContent = createContentAsStream(content);
    EntityStream stream = new EntityStream();
    DeserializerProperties consumerProperties = DeserializerProperties.init()
        .build();
    stream.setReadProperties(consumerProperties);
    stream.setContent(reqContent);
    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();

    ODataFeed feed = xec.readFeed(entitySet, stream);
    assertNotNull(feed);

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);

    String deltaLink = feedMetadata.getDeltaLink();
    // Null means no deltaLink found
    assertNotNull(deltaLink);

    assertEquals("http://thisisadeltalink", deltaLink);
  }

  @Test
  public void testReadSkipTag() throws Exception {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream contentBody = createContentAsStream(EMPLOYEE_1_XML
        .replace("<title type=\"text\">Walter Winter</title>",
            "<title type=\"text\"><title>Walter Winter</title></title>"));
    // execute
    EntityStream stream = new EntityStream();
    stream.setContent(contentBody);
    stream.setReadProperties(DeserializerProperties.init().build());
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    String id = result.getMetadata().getId();
    assertEquals("http://localhost:19000/Employees('1')", id);
    Map<String, Object> properties = result.getProperties();
    assertEquals(9, properties.size());
  }

  @Test
  public void readContentOnlyRoom() throws Exception {
    // prepare
    String content = readFile("RoomContentOnly.xml");
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream contentBody = createContentAsStream(content);

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    EntityStream stream = new EntityStream();
    stream.setReadProperties(DeserializerProperties.init().build());
    stream.setContent(contentBody);
    ODataEntry result =
        xec.readEntry(entitySet, stream);

    // verify
    assertEquals(4, result.getProperties().size());
  }

  @Test
  public void readContentOnlyEmployeeWithNavigationLink() throws Exception {
    // prepare
    String content = readFile("EmployeeContentOnlyWithNavigationLink.xml");
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream contentBody = createContentAsStream(content);

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    EntityStream stream = new EntityStream();
    stream.setReadProperties(DeserializerProperties.init().build());
    stream.setContent(contentBody);
    ODataEntry result =
        xec.readEntry(entitySet, stream);

    // verify
    assertEquals(9, result.getProperties().size());
    List<String> associationUris = result.getMetadata().getAssociationUris("ne_Manager");
    assertEquals(1, associationUris.size());
    assertEquals("Managers('1')", associationUris.get(0));
  }

  @Test
  public void readContentOnlyRoomWithNavigationLink() throws Exception {
    // prepare
    String content = readFile("RoomContentOnlyWithNavigationLink.xml");
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream contentBody = createContentAsStream(content);

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    EntityStream stream = new EntityStream();
    stream.setReadProperties(DeserializerProperties.init().build());
    stream.setContent(contentBody);
    ODataEntry result =
        xec.readEntry(entitySet, stream);

    // verify
    assertEquals(4, result.getProperties().size());
    List<String> associationUris = result.getMetadata().getAssociationUris("nr_Building");
    assertEquals(1, associationUris.size());
    assertEquals("Buildings('1')", associationUris.get(0));
  }

  /** Teams('1')?$expand=nt_Employees */
  @SuppressWarnings("unchecked")
  @Test
  public void readTeamWithInlineContent() throws Exception {
    // prepare
    String content = readFile("expanded_team.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream reqContent = createContentAsStream(content);

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    EntityStream stream = new EntityStream();
    stream.setReadProperties(DeserializerProperties.init().build());
    stream.setContent(reqContent);

    ODataEntry entry = xec.readEntry(entitySet, stream);
    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("Id"));
    assertEquals("Team 1", properties.get("Name"));
    assertEquals(Boolean.FALSE, properties.get("isScrumTeam"));

    assertNotNull(properties.get("nt_Employees"));
    ODataFeed employeeFeed = (ODataFeed) properties.get("nt_Employees");
    List<ODataEntry> employees = employeeFeed.getEntries();
    assertEquals(3, employees.size());

    ODataEntry employeeNo2 = employees.get(1);
    Map<String, Object> employessNo2Props = employeeNo2.getProperties();
    assertEquals("Frederic Fall", employessNo2Props.get("EmployeeName"));
    assertEquals("2", employessNo2Props.get("RoomId"));
    assertEquals(32, employessNo2Props.get("Age"));

    Map<String, Object> emp2Location = (Map<String, Object>) employessNo2Props.get("Location");

    Map<String, Object> emp2City = (Map<String, Object>) emp2Location.get("City");
    assertEquals("69190", emp2City.get("PostalCode"));
    assertEquals("Walldorf", emp2City.get("CityName"));
  }

  @Test
  public void readExpandedTeam() throws Exception {
    // prepare
    String content = readFile("expanded_team.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream reqContent = createContentAsStream(content);
    EntityStream stream = new EntityStream();
    stream.setReadProperties(DeserializerProperties.init().build());
    stream.setContent(reqContent);

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();

    ODataEntry entry = xec.readEntry(entitySet, stream);
    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("Id"));
    assertEquals("Team 1", properties.get("Name"));
    assertEquals(Boolean.FALSE, properties.get("isScrumTeam"));
    assertNotNull(properties.get("nt_Employees"));
    //
    assertNotNull(entry);

    ODataFeed employeeFeed = (ODataFeed) properties.get("nt_Employees");
    List<ODataEntry> employees = (List<ODataEntry>) employeeFeed.getEntries();
    assertEquals(3, employees.size());
    //
    ODataEntry employeeNo2 = employees.get(1);
    Map<String, Object> employessNo2Props = employeeNo2.getProperties();
    assertEquals("Frederic Fall", employessNo2Props.get("EmployeeName"));
    assertEquals("2", employessNo2Props.get("RoomId"));
    assertEquals(32, employessNo2Props.get("Age"));
    @SuppressWarnings("unchecked")
    Map<String, Object> emp2Location = (Map<String, Object>) employessNo2Props.get("Location");
    @SuppressWarnings("unchecked")
    Map<String, Object> emp2City = (Map<String, Object>) emp2Location.get("City");
    assertEquals("69190", emp2City.get("PostalCode"));
    assertEquals("Walldorf", emp2City.get("CityName"));
  }

  @Test
  public void readInlineBuildingEntry() throws Exception {
    // prepare
    String content = readFile("expandedBuilding.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(content);

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    OnDeserializeInlineContent callback = new Callback();
    DeserializerProperties consumerProperties = DeserializerProperties.init().callback(callback).build();

    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(consumerProperties);

    ODataEntry entry = xec.readEntry(entitySet, stream);
    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("Id"));
    assertEquals("Room 1", properties.get("Name"));
    assertEquals((short) 1, properties.get("Seats"));
    assertEquals((short) 1, properties.get("Version"));

    ODataEntry inlineBuilding = (ODataEntry) properties.get("nr_Building");

    Map<String, Object> inlineBuildingProps = inlineBuilding.getProperties();
    assertEquals("1", inlineBuildingProps.get("Id"));
    assertEquals("Building 1", inlineBuildingProps.get("Name"));
    assertNull(inlineBuildingProps.get("Image"));
    assertNull(inlineBuildingProps.get("nb_Rooms"));

    assertEquals("Rooms('1')/nr_Employees", entry.getMetadata().getAssociationUris("nr_Employees").get(0));
    assertEquals("Rooms('1')/nr_Building", entry.getMetadata().getAssociationUris("nr_Building").get(0));
  }

  /** Teams('1')?$expand=nt_Employees */
  @SuppressWarnings("unchecked")
  @Test
  public void readWithInlineContent() throws Exception {
    // prepare
    String content = readFile("expanded_team.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream reqContent = createContentAsStream(content);

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    DeserializerProperties consumerProperties = DeserializerProperties.init()
        .build();

    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(consumerProperties);

    ODataEntry entry = xec.readEntry(entitySet, stream);
    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("Id"));
    assertEquals("Team 1", properties.get("Name"));
    assertEquals(Boolean.FALSE, properties.get("isScrumTeam"));
    ODataFeed employeesFeed = (ODataFeed) properties.get("nt_Employees");
    List<ODataEntry> employees = employeesFeed.getEntries();
    assertEquals(3, employees.size());
    //

    ODataEntry employeeNo2 = employees.get(1);
    Map<String, Object> employessNo2Props = employeeNo2.getProperties();
    assertEquals("Frederic Fall", employessNo2Props.get("EmployeeName"));
    assertEquals("2", employessNo2Props.get("RoomId"));
    assertEquals(32, employessNo2Props.get("Age"));
    Map<String, Object> emp2Location = (Map<String, Object>) employessNo2Props.get("Location");
    Map<String, Object> emp2City = (Map<String, Object>) emp2Location.get("City");
    assertEquals("69190", emp2City.get("PostalCode"));
    assertEquals("Walldorf", emp2City.get("CityName"));
  }

  /** Teams('1')?$expand=nt_Employees,nt_Employees/ne_Team */
  @Test
  public void readWithDoubleInlineContent() throws Exception {
    // prepare
    String content = readFile("double_expanded_team.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream reqContent = createContentAsStream(content);

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    DeserializerProperties consumerProperties = DeserializerProperties.init().build();
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(consumerProperties);

    ODataEntry entry = xec.readEntry(entitySet, stream);
    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("Id"));
    assertEquals("Team 1", properties.get("Name"));
    assertEquals(Boolean.FALSE, properties.get("isScrumTeam"));
    //
    ODataFeed employeesFeed = (ODataFeed) properties.get("nt_Employees");
    List<ODataEntry> employees = employeesFeed.getEntries();
    assertEquals(3, employees.size());
    //
    ODataEntry employeeNo2 = employees.get(1);
    Map<String, Object> employessNo2Props = employeeNo2.getProperties();
    assertEquals("Frederic Fall", employessNo2Props.get("EmployeeName"));
    assertEquals("2", employessNo2Props.get("RoomId"));
    assertEquals(32, employessNo2Props.get("Age"));
    @SuppressWarnings("unchecked")
    Map<String, Object> emp2Location = (Map<String, Object>) employessNo2Props.get("Location");
    @SuppressWarnings("unchecked")
    Map<String, Object> emp2City = (Map<String, Object>) emp2Location.get("City");
    assertEquals("69190", emp2City.get("PostalCode"));
    assertEquals("Walldorf", emp2City.get("CityName"));

    ODataEntry inlinedTeam = (ODataEntry) employessNo2Props.get("ne_Team");
    assertEquals("1", inlinedTeam.getProperties().get("Id"));
    assertEquals("Team 1", inlinedTeam.getProperties().get("Name"));
  }

  /** Teams('1')?$expand=nt_Employees,nt_Employees/ne_Team */
  @Test
  public void readWithDoubleInlineContentAndResend() throws Exception {
    // prepare
    String content = readFile("double_expanded_team.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream reqContent = createContentAsStream(content);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    DeserializerProperties consumerProperties = DeserializerProperties.init().build();
    stream.setReadProperties(consumerProperties);

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry entry = xec.readEntry(entitySet, stream);

    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("Id"));
    assertEquals("Team 1", properties.get("Name"));
    assertEquals(Boolean.FALSE, properties.get("isScrumTeam"));
    //
    ODataFeed employeeFeed = (ODataFeed) properties.get("nt_Employees");
    List<ODataEntry> employees = (List<ODataEntry>) employeeFeed.getEntries();
    assertEquals(3, employees.size());
    //
    ODataEntry employeeNo2 = employees.get(1);
    Map<String, Object> employessNo2Props = employeeNo2.getProperties();
    assertEquals("Frederic Fall", employessNo2Props.get("EmployeeName"));
    assertEquals("2", employessNo2Props.get("RoomId"));
    assertEquals(32, employessNo2Props.get("Age"));
    @SuppressWarnings("unchecked")
    Map<String, Object> emp2Location = (Map<String, Object>) employessNo2Props.get("Location");
    @SuppressWarnings("unchecked")
    Map<String, Object> emp2City = (Map<String, Object>) emp2Location.get("City");
    assertEquals("69190", emp2City.get("PostalCode"));
    assertEquals("Walldorf", emp2City.get("CityName"));

    ODataEntry inlinedTeam = (ODataEntry) employessNo2Props.get("ne_Team");
    assertEquals("1", inlinedTeam.getProperties().get("Id"));
    assertEquals("Team 1", inlinedTeam.getProperties().get("Name"));
  }

  /** Teams('1')?$expand=nt_Employees,nt_Employees/ne_Team */
  @SuppressWarnings("unchecked")
  @Test
  public void readWithDoubleInlineContents() throws Exception {
    // prepare
    String content = readFile("double_expanded_team.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream reqContent = createContentAsStream(content);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry entry = xec.readEntry(entitySet, stream);

    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("Id"));
    assertEquals("Team 1", properties.get("Name"));
    assertEquals(Boolean.FALSE, properties.get("isScrumTeam"));
    ODataFeed employeesFeed = (ODataFeed) properties.get("nt_Employees");
    assertNotNull(employeesFeed);
    List<ODataEntry> employees = employeesFeed.getEntries();
    assertEquals(3, employees.size());
    ODataEntry employeeNo2 = employees.get(1);
    Map<String, Object> employessNo2Props = employeeNo2.getProperties();
    assertEquals("Frederic Fall", employessNo2Props.get("EmployeeName"));
    assertEquals("2", employessNo2Props.get("RoomId"));
    assertEquals(32, employessNo2Props.get("Age"));
    Map<String, Object> emp2Location = (Map<String, Object>) employessNo2Props.get("Location");
    Map<String, Object> emp2City = (Map<String, Object>) emp2Location.get("City");
    assertEquals("69190", emp2City.get("PostalCode"));
    assertEquals("Walldorf", emp2City.get("CityName"));

    // employees has no inlined content set
    ODataEntry inlinedTeam = (ODataEntry) employessNo2Props.get("ne_Team");
    assertNotNull(inlinedTeam);
    assertEquals("1", inlinedTeam.getProperties().get("Id"));
    assertEquals("Team 1", inlinedTeam.getProperties().get("Name"));
  }

  @Test
  public void readWithInlineContentIgnored() throws Exception {
    // prepare
    String content = readFile("expanded_team.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream reqContent = createContentAsStream(content);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry entry = xec.readEntry(entitySet, stream);

    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("Id"));
    assertEquals("Team 1", properties.get("Name"));
    assertEquals(Boolean.FALSE, properties.get("isScrumTeam"));
  }

  /**
   * Read an inline Room at an Employee
   * 
   * @throws Exception
   */
  @Test
  public void readWithInlineContentEmployeeRoomEntry() throws Exception {

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream reqContent = createContentAsStream(EMPLOYEE_1_ROOM_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry employee = xec.readEntry(entitySet, stream);

    // validate
    assertNotNull(employee);
    Map<String, Object> properties = employee.getProperties();
    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    EntryMetadata employeeMetadata = employee.getMetadata();
    assertNotNull(employeeMetadata);
    assertEquals("W/\"1\"", employeeMetadata.getEtag());

    // Inline
    ODataEntry room = (ODataEntry) properties.get("ne_Room");
    Map<String, Object> roomProperties = room.getProperties();
    assertEquals(4, roomProperties.size());
    assertEquals("1", roomProperties.get("Id"));
    assertEquals("Room 1", roomProperties.get("Name"));
    assertEquals(Short.valueOf("1"), roomProperties.get("Seats"));
    assertEquals(Short.valueOf("1"), roomProperties.get("Version"));
    EntryMetadata roomMetadata = room.getMetadata();
    assertNotNull(roomMetadata);
    assertEquals("W/1", roomMetadata.getEtag());
  }

  /**
   * Reads an inline Room at an Employee with specially formatted XML (see issue ODATAFORSAP-92).
   */
  @Test
  public void readWithInlineContentEmployeeRoomEntrySpecialXml() throws Exception {

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream reqContent = createContentAsStream(EMPLOYEE_1_ROOM_XML, true);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry entry = xec.readEntry(entitySet, stream);

    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    ODataEntry room = (ODataEntry) properties.get("ne_Room");
    Map<String, Object> roomProperties = room.getProperties();
    assertEquals(4, roomProperties.size());
    assertEquals("1", roomProperties.get("Id"));
    assertEquals("Room 1", roomProperties.get("Name"));
    assertEquals(Short.valueOf("1"), roomProperties.get("Seats"));
    assertEquals(Short.valueOf("1"), roomProperties.get("Version"));
  }

  /**
   * Reads an employee with inlined but <code>NULL</code> room navigation property
   * (which has {@link com.sap.core.odata.api.edm.EdmMultiplicity#ONE EdmMultiplicity#ONE}).
   */
  @Test
  public void readWithInlineContentEmployeeNullRoomEntry() throws Exception {

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream reqContent = createContentAsStream(EMPLOYEE_1_NULL_ROOM_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry entry = xec.readEntry(entitySet, stream);

    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    ODataEntry room = (ODataEntry) properties.get("ne_Room");
    assertNull(room);
  }

  /**
   * Reads an employee with inlined but <code>NULL</code> room navigation property
   * (which has {@link com.sap.core.odata.api.edm.EdmMultiplicity#ONE EdmMultiplicity#ONE}).
   */
  @Test
  public void readWithInlineContentEmployeeNullRoomEntrySpecialXmlFormat() throws Exception {

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream reqContent = createContentAsStream(EMPLOYEE_1_NULL_ROOM_XML, true);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry entry = xec.readEntry(entitySet, stream);

    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    ODataEntry room = (ODataEntry) properties.get("ne_Room");
    assertNull(room);
  }

  /**
   * Reads a room with inlined but <code>NULL</code> employees navigation property
   * (which has {@link com.sap.core.odata.api.edm.EdmMultiplicity#MANY EdmMultiplicity#MANY}).
   */
  @Test
  public void readWithInlineContentRoomNullEmployeesEntry() throws Exception {

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(ROOM_1_NULL_EMPLOYEE_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry entry = xec.readEntry(entitySet, stream);

    // validate
    assertNotNull(entry);
    Map<String, Object> properties = entry.getProperties();
    assertEquals("1", properties.get("Id"));
    ODataEntry room = (ODataEntry) properties.get("ne_Employees");
    assertNull(room);
  }

  /**
   * Teams('1')?$expand=nt_Employees
   * -> Remove 'feed' start and end tags around expanded/inlined employees
   * 
   * @throws Exception
   */
  @Test(expected = EntityProviderException.class)
  public void validateFeedForInlineContent() throws Exception {
    // prepare
    String content = readFile("expanded_team.xml")
        .replace("<feed xml:base=\"http://some.host.com/service.root/\">", "")
        .replace("</feed>", "");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream reqContent = createContentAsStream(content);

    // execute
    readAndExpectException(entitySet, reqContent,
        EntityProviderException.INVALID_INLINE_CONTENT.addContent("xml data"));
  }

  /**
   * Teams('1')?$expand=nt_Employees
   * -> Remove 'type' attribute at expanded/inlined employees link tag
   * 
   * @throws Exception
   */
  @Test(expected = EntityProviderException.class)
  public void validateMissingTypeAttributeForInlineContent() throws Exception {
    // prepare
    String content = readFile("expanded_team.xml")
        .replace("type=\"application/atom+xml;type=feed\"", "");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream reqContent = createContentAsStream(content);

    // execute
    readAndExpectException(entitySet, reqContent,
        EntityProviderException.INVALID_INLINE_CONTENT.addContent("xml data"));
  }

  /**
   * Teams('1')?$expand=nt_Employees
   * -> Replaced parameter 'type=feed' with 'type=entry' attribute at expanded/inlined employees link tag
   * 
   * @throws Exception
   */
  @Test(expected = EntityProviderException.class)
  public void validateWrongTypeAttributeForInlineContent() throws Exception {
    // prepare
    String content = readFile("expanded_team.xml")
        .replace("type=\"application/atom+xml;type=feed\"", "type=\"application/atom+xml;type=entry\"");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream reqContent = createContentAsStream(content);

    // execute
    readAndExpectException(entitySet, reqContent, EntityProviderException.INVALID_INLINE_CONTENT.addContent("feed"));
  }

  /**
   * Teams('1')?$expand=nt_Employees
   * -> Replaced parameter 'type=feed' with 'type=entry' attribute at expanded/inlined employees link tag
   * 
   * @throws Exception
   */
  @Test(expected = EntityProviderException.class)
  public void validateWrongTypeAttributeForInlineContentMany() throws Exception {
    // prepare
    String content = readFile("double_expanded_team.xml")
        .replace("type=\"application/atom+xml;type=entry\"", "type=\"application/atom+xml;type=feed\"");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    InputStream reqContent = createContentAsStream(content);

    // execute
    readAndExpectException(entitySet, reqContent, EntityProviderException.INVALID_INLINE_CONTENT.addContent("entry"));
  }

  /**
   * We only support <code>UTF-8</code> as character encoding.
   * 
   * @throws Exception
   */
  @Test(expected = EntityProviderException.class)
  public void validationOfWrongXmlEncodingUtf32() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-32'?>"
            +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
            +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    readAndExpectException(entitySet, reqContent, EntityProviderException.UNSUPPORTED_CHARACTER_ENCODING
        .addContent("UTF-32"));
  }

  /**
   * We only support <code>UTF-8</code> as character encoding.
   * 
   * @throws Exception
   */
  @Test(expected = EntityProviderException.class)
  public void validationOfWrongXmlEncodingIso8859_1() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='iso-8859-1'?>"
            +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
            +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    readAndExpectException(entitySet, reqContent, EntityProviderException.UNSUPPORTED_CHARACTER_ENCODING
        .addContent("iso-8859-1"));
  }

  /**
   * Character encodings are case insensitive.
   * Hence <code>uTf-8</code> should work as well as <code>UTF-8</code>.
   * 
   * @throws Exception
   */
  @Test
  public void validationCaseInsensitiveXmlEncodingUtf8() throws Exception {
    String room =
        "<?xml version='1.0' encoding='uTf-8'?>"
            +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
            +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <d:Id>1</d:Id>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(room);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);

    assertNotNull(result);
    assertEquals("1", result.getProperties().get("Id"));
  }

  /**
   * For none media resource if <code>properties</code> tag is not within <code>content</code> tag it results in an
   * exception.
   * 
   * OData specification v2: 2.2.6.2.2 Entity Type (as an Atom Entry Element)
   * 
   * @throws Exception
   */
  @Test(expected = EntityProviderException.class)
  public void validationOfWrongPropertiesTagPositionForNoneMediaLinkEntry() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>"
            +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\"" +
            " xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
            +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\" />" +
            "  <m:properties>" +
            "    <d:Id>1</d:Id>" +
            "  </m:properties>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    readAndExpectException(entitySet, reqContent, EntityProviderException.INVALID_PARENT_TAG.addContent("content")
        .addContent("properties"));
  }

  /**
   * For media resource if <code>properties</code> tag is within <code>content</code> tag it results in an exception.
   * 
   * OData specification v2: 2.2.6.2.2 Entity Type (as an Atom Entry Element)
   * And RFC5023 [section 4.2]
   * 
   * @throws Exception
   */
  @Test(expected = EntityProviderException.class)
  public void validationOfWrongPropertiesTagPositionForMediaLinkEntry() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>"
            +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
            +
            "  <id>http://localhost:19000/test/Employees('1')</id>" +
            "  <title type=\"text\">Walter Winter</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <d:EmployeeId>1</d:EmployeeId>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    readAndExpectException(entitySet, reqContent, EntityProviderException.INVALID_PARENT_TAG.addContent("properties")
        .addContent("content"));
  }

  @Test
  public void validationOfNamespacesSuccess() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>"
            +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
            +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <d:Id>1</d:Id>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    assertNotNull(result);
  }

  @Test
  public void validationOfNamespaceAtPropertiesSuccess() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>" +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" xml:base=\"http://localhost:19000/test/\">" +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">" +
            "      <d:Id xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\">1</d:Id>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    assertNotNull(result);
  }

  @Test(expected = EntityProviderException.class)
  public void validationOfNamespaceAtTagsMissing() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>" +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" xml:base=\"http://localhost:19000/test/\">" +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <d:Id>1</d:Id>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    readAndExpectException(entitySet, reqContent, EntityProviderException.EXCEPTION_OCCURRED
        .addContent("WstxParsingException"));
  }

  /**
   * Use different namespace prefixes for <code>metadata (m)</code> and <code>data (d)</code>.
   * 
   * @throws Exception
   */
  @Test
  public void validationOfDifferentNamespacesPrefixSuccess() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>" +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "    xmlns:meta=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "    xmlns:data=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "    xml:base=\"http://localhost:19000/test/\" " +
            "    meta:etag=\"W/&quot;1&quot;\">" +
            "" +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <meta:properties>" +
            "      <data:Id>1</data:Id>" +
            "      <data:Seats>11</data:Seats>" +
            "      <data:Name>Room 42</data:Name>" +
            "      <data:Version>4711</data:Version>" +
            "    </meta:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    assertNotNull(result);
  }

  /**
   * Add <code>unknown property</code> in own namespace which is defined in entry tag.
   * 
   * @throws Exception
   */
  @Test
  public void validationOfUnknownPropertyOwnNamespaceSuccess() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>" +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "    xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "    xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "    xmlns:more=\"http://sample.com/more\" " +
            "    xml:base=\"http://localhost:19000/test/\" " +
            "    m:etag=\"W/&quot;1&quot;\">" +
            "" +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <d:Id>1</d:Id>" +
            "      <more:somePropertyToBeIgnored>ignore me</more:somePropertyToBeIgnored>" +
            "      <d:Seats>11</d:Seats>" +
            "      <d:Name>Room 42</d:Name>" +
            "      <d:Version>4711</d:Version>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    assertNotNull(result);
  }

  /**
   * Is allowed because <code>Id</code> is in default namespace (<code>xmlns=\"http://www.w3.org/2005/Atom\"</code>)
   * 
   * @throws Exception
   */
  @Test
  public void validationOfUnknownPropertyDefaultNamespaceSuccess() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>"
            +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
            +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <Id>1</Id>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);

    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    assertNotNull(result);
  }

  /**
   * Add <code>unknown property</code> in own namespace which is defined directly in unknown tag.
   * 
   * @throws Exception
   */
  @Test
  public void validationOfUnknownPropertyInlineNamespaceSuccess() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>"
            +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" "
            +
            "    xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" "
            +
            "    xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" "
            +
            "    xml:base=\"http://localhost:19000/test/\" "
            +
            "    m:etag=\"W/&quot;1&quot;\">"
            +
            ""
            +
            "  <id>http://localhost:19000/test/Rooms('1')</id>"
            +
            "  <title type=\"text\">Room 1</title>"
            +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>"
            +
            "  <content type=\"application/xml\">"
            +
            "    <m:properties>"
            +
            "      <d:Id>1</d:Id>"
            +
            "      <more:somePropertyToBeIgnored " +
            "xmlns:more=\"http://sample.com/more\">ignore me</more:somePropertyToBeIgnored>"
            +
            "      <d:Seats>11</d:Seats>" +
            "      <d:Name>Room 42</d:Name>" +
            "      <d:Version>4711</d:Version>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    assertNotNull(result);
  }

  @Test(expected = EntityProviderException.class)
  public void validationOfNamespacesMissingXmlns() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>" +
            "<entry etag=\"W/&quot;1&quot;\">" +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <d:Id>1</d:Id>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    readAndExpectException(entitySet, reqContent, EntityProviderException.EXCEPTION_OCCURRED
        .addContent("WstxParsingException"));
  }

  /**
   * Double occurrence of <code>d:Name</code> tag must result in an exception.
   * 
   * @throws Exception
   */
  @Test(expected = EntityProviderException.class)
  public void validationOfDuplicatedPropertyException() throws Exception {
    String room =
        "<?xml version='1.0' encoding='UTF-8'?>" +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "    xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "    xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "    xml:base=\"http://localhost:19000/test/\" " +
            "    m:etag=\"W/&quot;1&quot;\">" +
            "" +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <d:Id>1</d:Id>" +
            "      <d:Seats>11</d:Seats>" +
            "      <d:Name>Room 42</d:Name>" +
            "      <d:Name>Room 42</d:Name>" +
            "      <d:Version>4711</d:Version>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(room);
    readAndExpectException(entitySet, reqContent, EntityProviderException.DOUBLE_PROPERTY.addContent("Name"));
  }

  /**
   * Double occurrence of <code>Name</code> tag within different namespace is allowed.
   * 
   * @throws Exception
   */
  @Test
  public void validationOfDoublePropertyDifferentNamespace() throws Exception {
    String room =
        "<?xml version='1.0' encoding='UTF-8'?>" +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "    xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "    xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "    xml:base=\"http://localhost:19000/test/\" " +
            "    m:etag=\"W/&quot;1&quot;\">" +
            "" +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <d:Id>1</d:Id>" +
            "      <d:Seats>11</d:Seats>" +
            "      <o:Name xmlns:o=\"http://sample.org/own\">Room 42</o:Name>" +
            "      <d:Name>Room 42</d:Name>" +
            "      <d:Version>4711</d:Version>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(room);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    assertNotNull(result);
  }

  /**
   * Double occurrence of <code>Name</code> tag within ignored/unknown property AND different namespace is allowed.
   * 
   * @throws Exception
   */
  @Test
  public void validationOfDoublePropertyDifferentTagHierachy() throws Exception {
    String room =
        "<?xml version='1.0' encoding='UTF-8'?>" +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "    xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "    xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "    xml:base=\"http://localhost:19000/test/\" " +
            "    m:etag=\"W/&quot;1&quot;\">" +
            "" +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <d:Id>1</d:Id>" +
            "      <d:Seats>11</d:Seats>" +
            "      <SomeProp>" +
            "        <Name>Room 42</Name>" +
            "      </SomeProp>" +
            "      <d:Name>Room 42</d:Name>" +
            "      <d:Version>4711</d:Version>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(room);
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    ODataEntry result = xec.readEntry(entitySet, stream);
    assertNotNull(result);
  }

  /**
   * Double occurrence of <code>d:Name</code> tag within an unknown (and hence ignored) property is allowed.
   * 
   * @throws Exception
   */
  @Test
  public void validationOfDoublePropertyDifferentTagHierachyD_Namespace() throws Exception {
    String room =
        "<?xml version='1.0' encoding='UTF-8'?>" +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "    xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "    xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "    xml:base=\"http://localhost:19000/test/\" " +
            "    m:etag=\"W/&quot;1&quot;\">" +
            "" +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <d:Id>1</d:Id>" +
            "      <d:Seats>11</d:Seats>" +
            "      <SomeProp>" +
            "        <d:Name>Room 42</d:Name>" +
            "      </SomeProp>" +
            "      <d:Name>Room 42</d:Name>" +
            "      <d:Version>4711</d:Version>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(room);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    assertNotNull(result);
  }

  @Test(expected = EntityProviderException.class)
  public void validationOfNamespacesMissingM_NamespaceAtProperties() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>"
            +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
            +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <properties>" +
            "      <d:Id>1</d:Id>" +
            "    </properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    readAndExpectException(entitySet, reqContent, EntityProviderException.EXCEPTION_OCCURRED
        .addContent("WstxParsingException"));
  }

  /**
   * Missing _d_ namespace at key property/tag (_id_) is allowed.
   * 
   * @throws Exception
   */
  @Test
  public void validationOfNamespacesMissingD_NamespaceAtKeyPropertyTag() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>"
            +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
            +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <Id>1</Id>" +
            "      <d:Seats>11</d:Seats>" +
            "      <d:Name>Room 42</d:Name>" +
            "      <d:Version>4711</d:Version>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    assertNotNull(result);
  }

  /**
   * Missing _d_ namespace at non-nullable property/tag (_Version_) is allowed.
   * @throws Exception
   */
  public void validationOfNamespacesMissingD_NamespaceAtNonNullableTag() throws Exception {
    String roomWithValidNamespaces =
        "<?xml version='1.0' encoding='UTF-8'?>"
            +
            "<entry xmlns=\"http://www.w3.org/2005/Atom\" " +
            "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" " +
            "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
            "xml:base=\"http://localhost:19000/test/\" m:etag=\"W/&quot;1&quot;\">"
            +
            "  <id>http://localhost:19000/test/Rooms('1')</id>" +
            "  <title type=\"text\">Room 1</title>" +
            "  <updated>2013-01-11T13:50:50.541+01:00</updated>" +
            "  <content type=\"application/xml\">" +
            "    <m:properties>" +
            "      <d:Seats>11</d:Seats>" +
            "      <d:Name>Room 42</d:Name>" +
            "      <Version>4711</Version>" +
            "    </m:properties>" +
            "  </content>" +
            "</entry>";

    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    final EdmProperty property = (EdmProperty) entitySet.getEntityType().getProperty("Version");
    EdmFacets facets = property.getFacets();
    Mockito.when(facets.isNullable()).thenReturn(false);

    InputStream reqContent = createContentAsStream(roomWithValidNamespaces);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    assertNotNull(result);
  }

  private void readAndExpectException(final EdmEntitySet entitySet, final InputStream reqContent,
      final MessageReference messageReference) throws ODataMessageException {
    readAndExpectException(entitySet, reqContent, true, messageReference);
  }

  private void readAndExpectException(final EdmEntitySet entitySet, final InputStream reqContent, final boolean merge,
      final MessageReference messageReference) throws ODataMessageException {
    try {
      EntityStream stream = new EntityStream();
      stream.setContent(reqContent);
      stream.setReadProperties(DeserializerProperties.init().build());

      // execute
      XmlEntityDeserializer xec = new XmlEntityDeserializer();
      ODataEntry result =
          xec.readEntry(entitySet, stream);
      assertNotNull(result);
      Assert.fail("Expected exception with MessageReference '" + messageReference.getKey() + "' was not thrown.");
    } catch (ODataMessageException e) {
      assertEquals(messageReference.getKey(), e.getMessageReference().getKey());
      // assertEquals(messageReference.getContent(), e.getMessageReference().getContent());
      throw e;
    }
  }

  @Test
  public void readEntryAtomProperties() throws Exception {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream contentBody = createContentAsStream(EMPLOYEE_1_XML);

    EntityStream stream = new EntityStream();
    stream.setContent(contentBody);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    EntryMetadata metadata = result.getMetadata();
    assertEquals("http://localhost:19000/Employees('1')", metadata.getId());
    assertEquals("W/\"1\"", metadata.getEtag());
    List<String> associationUris = metadata.getAssociationUris("ne_Room");
    assertEquals(1, associationUris.size());
    assertEquals("Employees('1')/ne_Room", associationUris.get(0));
    associationUris = metadata.getAssociationUris("ne_Manager");
    assertEquals(1, associationUris.size());
    assertEquals("Employees('1')/ne_Manager", associationUris.get(0));
    associationUris = metadata.getAssociationUris("ne_Team");
    assertEquals(1, associationUris.size());
    assertEquals("Employees('1')/ne_Team", associationUris.get(0));

    assertEquals(null, metadata.getUri());

    MediaMetadata mm = result.getMediaMetadata();
    assertEquals("Employees('1')/$value", mm.getSourceLink());
    assertEquals("mmEtag", mm.getEtag());
    assertEquals("application/octet-stream", mm.getContentType());
    assertEquals("Employees('1')/$value", mm.getEditLink());

    Map<String, Object> data = result.getProperties();
    assertEquals(9, data.size());
    assertEquals("1", data.get("EmployeeId"));
    assertEquals("Walter Winter", data.get("EmployeeName"));
    assertEquals("1", data.get("ManagerId"));
    assertEquals("1", data.get("RoomId"));
    assertEquals("1", data.get("TeamId"));
  }

  @Test
  public void readEntryLinks() throws Exception {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream contentBody = createContentAsStream(EMPLOYEE_1_XML);

    EntityStream stream = new EntityStream();
    stream.setContent(contentBody);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    List<String> associationUris = result.getMetadata().getAssociationUris("ne_Room");
    assertEquals(1, associationUris.size());
    assertEquals("Employees('1')/ne_Room", associationUris.get(0));
    associationUris = result.getMetadata().getAssociationUris("ne_Manager");
    assertEquals(1, associationUris.size());
    assertEquals("Employees('1')/ne_Manager", associationUris.get(0));
    associationUris = result.getMetadata().getAssociationUris("ne_Team");
    assertEquals(1, associationUris.size());
    assertEquals("Employees('1')/ne_Team", associationUris.get(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReadFeed() throws Exception {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    String content = readFile("feed_employees.xml");
    InputStream contentAsStream = createContentAsStream(content);
    EntityStream stream = new EntityStream();
    stream.setContent(contentAsStream);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataFeed feedResult = xec.readFeed(entitySet, stream);
    // verify feed result
    // metadata
    FeedMetadata metadata = feedResult.getFeedMetadata();
    assertNull(metadata.getInlineCount());
    assertNull(metadata.getNextLink());
    assertNull(metadata.getDeltaLink());
    // entries
    List<ODataEntry> entries = feedResult.getEntries();
    assertEquals(6, entries.size());
    // verify first employee
    ODataEntry firstEmployee = entries.get(0);
    Map<String, Object> properties = firstEmployee.getProperties();
    assertEquals(9, properties.size());

    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    assertEquals(Integer.valueOf(52), properties.get("Age"));
    Calendar entryDate = (Calendar) properties.get("EntryDate");
    assertEquals(915148800000L, entryDate.getTimeInMillis());
    assertEquals(TimeZone.getTimeZone("GMT"), entryDate.getTimeZone());
    assertEquals("Employees('1')/$value", properties.get("ImageUrl"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReadFeedWithInlineCountAndNextLink() throws Exception {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    String content = readFile("feed_employees_full.xml");
    InputStream contentAsStream = createContentAsStream(content);
    EntityStream stream = new EntityStream();
    stream.setContent(contentAsStream);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataFeed feedResult = xec.readFeed(entitySet, stream);
    // verify feed result
    // metadata
    FeedMetadata metadata = feedResult.getFeedMetadata();
    assertEquals(Integer.valueOf(6), metadata.getInlineCount());
    assertEquals("http://thisisanextlink", metadata.getNextLink());
    assertNull(metadata.getDeltaLink());
    // entries
    List<ODataEntry> entries = feedResult.getEntries();
    assertEquals(6, entries.size());
    // verify first employee
    ODataEntry firstEmployee = entries.get(0);
    Map<String, Object> properties = firstEmployee.getProperties();
    assertEquals(9, properties.size());

    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    assertEquals(Integer.valueOf(52), properties.get("Age"));
    Calendar entryDate = (Calendar) properties.get("EntryDate");
    assertEquals(915148800000L, entryDate.getTimeInMillis());
    assertEquals(TimeZone.getTimeZone("GMT"), entryDate.getTimeZone());
    assertEquals("Employees('1')/$value", properties.get("ImageUrl"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReadEntry() throws Exception {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream contentBody = createContentAsStream(EMPLOYEE_1_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(contentBody);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(9, properties.size());

    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    assertEquals(Integer.valueOf(52), properties.get("Age"));
    Calendar entryDate = (Calendar) properties.get("EntryDate");
    assertEquals(915148800000L, entryDate.getTimeInMillis());
    assertEquals(TimeZone.getTimeZone("GMT"), entryDate.getTimeZone());
    assertEquals("/SAP/PUBLIC/BC/NWDEMO_MODEL/IMAGES/Employee_1.png", properties.get("ImageUrl"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReadEntryWithLargeProperty() throws Exception {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    String newName = StringHelper.generateData(81920);
    InputStream contentBody = createContentAsStream(EMPLOYEE_1_XML.replaceAll("Walter Winter", newName));
    EntityStream stream = new EntityStream();
    stream.setContent(contentBody);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(9, properties.size());

    assertEquals("1", properties.get("EmployeeId"));
    assertEquals(newName, properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    assertEquals(Integer.valueOf(52), properties.get("Age"));
    Calendar entryDate = (Calendar) properties.get("EntryDate");
    assertEquals(915148800000L, entryDate.getTimeInMillis());
    assertEquals(TimeZone.getTimeZone("GMT"), entryDate.getTimeZone());
    assertEquals("/SAP/PUBLIC/BC/NWDEMO_MODEL/IMAGES/Employee_1.png", properties.get("ImageUrl"));
  }

  /**
   * Missing 'key' properties are allowed for validation against Edm model.
   * @throws Exception
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testReadEntryMissingKeyProperty() throws Exception {
    // prepare
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream contentBody = createContentAsStream(EMPLOYEE_1_XML.replace("<d:EmployeeId>1</d:EmployeeId>", ""));
    EntityStream stream = new EntityStream();
    stream.setContent(contentBody);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(8, properties.size());

    assertNull(properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    assertEquals(Integer.valueOf(52), properties.get("Age"));
    Calendar entryDate = (Calendar) properties.get("EntryDate");
    assertEquals(915148800000L, entryDate.getTimeInMillis());
    assertEquals(TimeZone.getTimeZone("GMT"), entryDate.getTimeZone());
    assertEquals("/SAP/PUBLIC/BC/NWDEMO_MODEL/IMAGES/Employee_1.png", properties.get("ImageUrl"));
  }

  @Test
  public void readEntryNullProperty() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    final String content = EMPLOYEE_1_XML.replace("<d:EntryDate>1999-01-01T00:00:00</d:EntryDate>",
        "<d:EntryDate m:null='true' />");
    InputStream contentBody = createContentAsStream(content);

    EntityStream stream = new EntityStream();
    stream.setContent(contentBody);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    final Map<String, Object> properties = result.getProperties();
    assertEquals(9, properties.size());
    assertTrue(properties.containsKey("EntryDate"));
    assertNull(properties.get("EntryDate"));
  }

  @Test(expected = EntityProviderException.class)
  public void readEntryTooManyValues() throws Exception {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    String content =
        EMPLOYEE_1_XML.replace("<d:Age>52</d:Age>",
            "<d:Age>52</d:Age><d:SomeUnknownTag>SomeUnknownValue</d:SomeUnknownTag>");
    InputStream contentBody = createContentAsStream(content);

    // execute
    try {
      EntityStream stream = new EntityStream();
      stream.setContent(contentBody);
      stream.setReadProperties(DeserializerProperties.init().build());

      // execute
      XmlEntityDeserializer xec = new XmlEntityDeserializer();
      xec.readEntry(entitySet, stream);
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.INVALID_PROPERTY.getKey(), e.getMessageReference().getKey());
      assertEquals("SomeUnknownTag", e.getMessageReference().getContent().get(0));
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReadEntryWithMerge() throws Exception {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    String content = EMPLOYEE_1_XML.replace("<d:Age>52</d:Age>", "");
    InputStream contentBody = createContentAsStream(content);
    EntityStream stream = new EntityStream();
    stream.setContent(contentBody);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(8, properties.size());

    // removed property
    assertNull(properties.get("Age"));

    // available properties
    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    Calendar entryDate = (Calendar) properties.get("EntryDate");
    assertEquals(915148800000L, entryDate.getTimeInMillis());
    assertEquals(TimeZone.getTimeZone("GMT"), entryDate.getTimeZone());
    assertEquals("/SAP/PUBLIC/BC/NWDEMO_MODEL/IMAGES/Employee_1.png", properties.get("ImageUrl"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReadEntryWithMergeAndMappings() throws Exception {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    String content = EMPLOYEE_1_XML.replace("<d:Age>52</d:Age>", "");
    InputStream contentBody = createContentAsStream(content);

    EntityStream stream = new EntityStream();
    stream.setContent(contentBody);
    OnDeserializeInlineContent callback = new Callback();
    DeserializerProperties consumerProperties = DeserializerProperties.init().addTypeMappings(
        createTypeMappings("Age", Long.class, // test unused type mapping
            "EntryDate", Date.class)).callback(callback).build();

    stream.setReadProperties(consumerProperties);

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(8, properties.size());

    // removed property
    assertNull(properties.get("Age"));

    // available properties
    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    assertEquals(new Date(915148800000l), properties.get("EntryDate"));
    assertEquals("/SAP/PUBLIC/BC/NWDEMO_MODEL/IMAGES/Employee_1.png", properties.get("ImageUrl"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReadEntryRequest() throws Exception {
    XmlEntityDeserializer xec = new XmlEntityDeserializer();

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    EntityStream stream = new EntityStream();
    InputStream content = createContentAsStream(EMPLOYEE_1_XML);
    stream.setContent(content);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(9, properties.size());

    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    assertEquals(Integer.valueOf(52), properties.get("Age"));
    Calendar entryDate = (Calendar) properties.get("EntryDate");
    assertEquals(915148800000L, entryDate.getTimeInMillis());
    assertEquals(TimeZone.getTimeZone("GMT"), entryDate.getTimeZone());
    assertEquals("/SAP/PUBLIC/BC/NWDEMO_MODEL/IMAGES/Employee_1.png", properties.get("ImageUrl"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReadEntryRequestNullMapping() throws Exception {
    XmlEntityDeserializer xec = new XmlEntityDeserializer();

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream content = createContentAsStream(EMPLOYEE_1_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(content);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(9, properties.size());

    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    assertEquals(Integer.valueOf(52), properties.get("Age"));
    Calendar entryDate = (Calendar) properties.get("EntryDate");
    assertEquals(915148800000L, entryDate.getTimeInMillis());
    assertEquals(TimeZone.getTimeZone("GMT"), entryDate.getTimeZone());
    assertEquals("/SAP/PUBLIC/BC/NWDEMO_MODEL/IMAGES/Employee_1.png", properties.get("ImageUrl"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReadEntryRequestEmptyMapping() throws Exception {
    XmlEntityDeserializer xec = new XmlEntityDeserializer();

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream content = createContentAsStream(EMPLOYEE_1_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(content);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(9, properties.size());

    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    assertEquals(Integer.valueOf(52), properties.get("Age"));
    Calendar entryDate = (Calendar) properties.get("EntryDate");
    assertEquals(915148800000L, entryDate.getTimeInMillis());
    assertEquals(TimeZone.getTimeZone("GMT"), entryDate.getTimeZone());
    assertEquals("/SAP/PUBLIC/BC/NWDEMO_MODEL/IMAGES/Employee_1.png", properties.get("ImageUrl"));
  }

  @Test(expected = EntityProviderException.class)
  public void testReadEntryRequestInvalidMapping() throws Exception {
    XmlEntityDeserializer xec = new XmlEntityDeserializer();

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream content = createContentAsStream(EMPLOYEE_1_XML);

    EntityStream stream = new EntityStream();
    stream.setContent(content);
    stream.setReadProperties(DeserializerProperties.init().addTypeMappings(
        createTypeMappings("EmployeeName", Integer.class)).build());

    // execute
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(9, properties.size());
  }

  @Test
  public void testReadEntryRequestObjectMapping() throws Exception {
    XmlEntityDeserializer xec = new XmlEntityDeserializer();

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream content = createContentAsStream(EMPLOYEE_1_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(content);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(9, properties.size());

    assertEquals("1", properties.get("EmployeeId"));
    Object o = properties.get("EmployeeName");
    assertTrue(o instanceof String);
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReadEntryRequestWithMapping() throws Exception {
    XmlEntityDeserializer xec = new XmlEntityDeserializer();

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream content = createContentAsStream(EMPLOYEE_1_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(content);
    stream.setReadProperties(DeserializerProperties.init().addTypeMappings(createTypeMappings("Age", Short.class,
        "Heidelberg", String.class,
        "EntryDate", Long.class)).build());

    // execute
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    Map<String, Object> properties = result.getProperties();
    assertEquals(9, properties.size());

    assertEquals("1", properties.get("EmployeeId"));
    assertEquals("Walter Winter", properties.get("EmployeeName"));
    assertEquals("1", properties.get("ManagerId"));
    assertEquals("1", properties.get("RoomId"));
    assertEquals("1", properties.get("TeamId"));
    Map<String, Object> location = (Map<String, Object>) properties.get("Location");
    assertEquals(2, location.size());
    assertEquals("Germany", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals(2, city.size());
    assertEquals("69124", city.get("PostalCode"));
    assertEquals("Heidelberg", city.get("CityName"));
    assertEquals(Short.valueOf("52"), properties.get("Age"));
    assertEquals(Long.valueOf(915148800000L), properties.get("EntryDate"));
    assertEquals("/SAP/PUBLIC/BC/NWDEMO_MODEL/IMAGES/Employee_1.png", properties.get("ImageUrl"));
  }

  @Test
  public void readCustomizableFeedMappings() throws Exception {
    XmlEntityDeserializer xec = new XmlEntityDeserializer();

    EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");
    InputStream reqContent = createContentAsStream(PHOTO_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    EntryMetadata entryMetadata = result.getMetadata();
    assertEquals("http://localhost:19000/test/Container2.Photos(Id=1,Type='image%2Fpng')", entryMetadata.getId());

    Map<String, Object> data = result.getProperties();
    assertEquals("Образ", data.get("Содержание"));
    assertEquals("Photo1", data.get("Name"));
    assertEquals("image/png", data.get("Type"));
    assertNull(data.get("ignore"));
  }

  @Test
  public void readCustomizableFeedMappingsWithMergeSemantic() throws Exception {
    XmlEntityDeserializer xec = new XmlEntityDeserializer();

    EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");
    InputStream reqContent = createContentAsStream(PHOTO_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    EntryMetadata entryMetadata = result.getMetadata();
    assertEquals("http://localhost:19000/test/Container2.Photos(Id=1,Type='image%2Fpng')", entryMetadata.getId());

    Map<String, Object> data = result.getProperties();
    assertEquals("Photo1", data.get("Name"));
    assertEquals("image/png", data.get("Type"));
    // ignored customizable feed mapping
    assertNotNull(data.get("Содержание"));
    assertNull(data.get("ignore"));
  }

  @Test(expected = EntityProviderException.class)
  public void readCustomizableFeedMappingsBadRequest() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getEntityContainer("Container2").getEntitySet("Photos");
    InputStream reqContent = createContentAsStream(PHOTO_XML_INVALID_MAPPING);

    readAndExpectException(entitySet, reqContent, false,
        EntityProviderException.INVALID_PROPERTY.addContent("ignore"));
  }

  @Test
  public void readIncompleteEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(ROOM_1_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    // execute
    ODataEntry result = xec.readEntry(entitySet, stream);
    final EntryMetadata entryMetadata = result.getMetadata();
    assertEquals("http://localhost:19000/test/Rooms('1')", entryMetadata.getId());
    assertEquals("W/\"1\"", entryMetadata.getEtag());
    assertNull(entryMetadata.getUri());

    final MediaMetadata mediaMetadata = result.getMediaMetadata();
    assertEquals(HttpContentType.APPLICATION_XML, mediaMetadata.getContentType());
    assertNull(mediaMetadata.getSourceLink());
    assertNull(mediaMetadata.getEditLink());
    assertNull(mediaMetadata.getEtag());

    final Map<String, Object> properties = result.getProperties();
    assertEquals(1, properties.size());
    assertEquals("1", properties.get("Id"));
    assertFalse(properties.containsKey("Seats"));
  }

  @Test
  public void readIncompleteEntryMerge() throws Exception {
    XmlEntityDeserializer xec = new XmlEntityDeserializer();

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(ROOM_1_XML);
    EntityStream stream = new EntityStream();
    stream.setContent(reqContent);
    stream.setReadProperties(DeserializerProperties.init().build());

    // execute
    ODataEntry result = xec.readEntry(entitySet, stream);
    // verify
    EntryMetadata entryMetadata = result.getMetadata();
    assertEquals("http://localhost:19000/test/Rooms('1')", entryMetadata.getId());
    assertEquals("W/\"1\"", entryMetadata.getEtag());
    assertEquals(null, entryMetadata.getUri());

    MediaMetadata mediaMetadata = result.getMediaMetadata();
    assertEquals("application/xml", mediaMetadata.getContentType());
    assertEquals(null, mediaMetadata.getSourceLink());
    assertEquals(null, mediaMetadata.getEditLink());
    assertEquals(null, mediaMetadata.getEtag());

    Map<String, Object> properties = result.getProperties();
    assertEquals(1, properties.size());
    assertEquals("1", properties.get("Id"));

    assertEquals("Rooms('1')/nr_Building", result.getMetadata().getAssociationUris("nr_Building").get(0));
    assertEquals("Rooms('1')/nr_Employees", result.getMetadata().getAssociationUris("nr_Employees").get(0));
  }

}