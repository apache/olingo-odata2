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
package org.apache.olingo.odata2.core.ep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;

import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.entry.DeletedEntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.edm.EdmDateTimeOffset;
import org.apache.olingo.odata2.core.ep.consumer.AbstractConsumerTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

/**
 *  
 */
public class ProviderFacadeImplTest extends AbstractConsumerTest {

  private static final String EMPLOYEE_1_XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
          "<entry xmlns=\"" + Edm.NAMESPACE_ATOM_2005 + "\"" +
          " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\"" +
          " xmlns:d=\"" + Edm.NAMESPACE_D_2007_08 + "\"" +
          " xml:base=\"https://some.host.com/some.service.root.segment/ReferenceScenario.svc/\">" +
          "<id>https://some.host.com/some.service.root.segment/ReferenceScenario.svc/Employees('1')</id>" +
          "<title type=\"text\">Walter Winter</title>" +
          "<updated>1999-01-01T00:00:00Z</updated>" +
          "<category term=\"RefScenario.Employee\" scheme=\"" + Edm.NAMESPACE_SCHEME_2007_08 + "\"/>" +
          "<link href=\"Employees('1')\" rel=\"edit\" title=\"Employee\"/>" +
          "<link href=\"Employees('1')/$value\" rel=\"edit-media\" type=\"application/octet-stream\"/>" +
          "<link href=\"Employees('1')/ne_Room\" rel=\"" + Edm.NAMESPACE_REL_2007_08
          + "ne_Room\" type=\"application/atom+xml; type=entry\" title=\"ne_Room\"/>" +
          "<link href=\"Employees('1')/ne_Manager\" rel=\"" + Edm.NAMESPACE_REL_2007_08
          + "ne_Manager\" type=\"application/atom+xml; type=entry\" title=\"ne_Manager\"/>" +
          "<link href=\"Employees('1')/ne_Team\" rel=\"" + Edm.NAMESPACE_REL_2007_08
          + "ne_Team\" type=\"application/atom+xml; type=entry\" title=\"ne_Team\"/>" +
          "<content type=\"application/octet-stream\" src=\"Employees('1')/$value\"/>" +
          "<m:properties>" +
          "<d:EmployeeId>1</d:EmployeeId>" +
          "<d:EmployeeName>Walter Winter</d:EmployeeName>" +
          "<d:ManagerId>1</d:ManagerId>" +
          "<d:RoomId>1</d:RoomId>" +
          "<d:TeamId>1</d:TeamId>" +
          "<d:Location m:type=\"RefScenario.c_Location\">" +
          "<d:Country>Germany</d:Country>" +
          "<d:City m:type=\"RefScenario.c_City\">" +
          "<d:PostalCode>69124</d:PostalCode>" +
          "<d:CityName>Heidelberg</d:CityName>" +
          "</d:City>" +
          "</d:Location>" +
          "<d:Age>52</d:Age>" +
          "<d:EntryDate>1999-01-01T00:00:00</d:EntryDate>" +
          "<d:ImageUrl>Employee_1.png</d:ImageUrl>" +
          "</m:properties>" +
          "</entry>";

  @Test
  public void readDeltaFeed() throws Exception {

    final String contentType = ContentType.APPLICATION_ATOM_XML_FEED.toContentTypeString();
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream content = getFileAsStream("feed_with_deleted_entries.xml");
    EntityProviderReadProperties properties = EntityProviderReadProperties.init().build();

    ODataDeltaFeed deltaFeed = new ProviderFacadeImpl().readDeltaFeed(contentType, entitySet, content, properties);
    assertNotNull(deltaFeed);
    assertNotNull(deltaFeed.getEntries());
    assertNotNull(deltaFeed.getFeedMetadata());
    assertEquals(1, deltaFeed.getEntries().size());
    assertEquals(1, deltaFeed.getDeletedEntries().size());
    assertEquals("http://host:123/odata/Rooms?$skiptoken=97", deltaFeed.getFeedMetadata().getDeltaLink());
    assertEquals("http://host:123/odata/Rooms('2')", deltaFeed.getDeletedEntries().get(0).getUri());

    Date when =
        EdmDateTimeOffset.getInstance().valueOfString("2014-01-14T18:11:06.682+01:00", EdmLiteralKind.DEFAULT, null,
            Date.class);
    assertEquals(when, deltaFeed.getDeletedEntries().get(0).getWhen());
  }

  @Test
  public void readFeed() throws Exception {
    final String contentType = ContentType.APPLICATION_ATOM_XML_FEED.toContentTypeString();
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream content = getFileAsStream("feed_employees.xml");
    EntityProviderReadProperties properties = EntityProviderReadProperties.init().build();

    ODataFeed feed = new ProviderFacadeImpl().readFeed(contentType, entitySet, content, properties);
    assertNotNull(feed);
    assertNotNull(feed.getEntries());
    assertNotNull(feed.getFeedMetadata());
    assertEquals(6, feed.getEntries().size());
  }

  @Test
  public void readEntry() throws Exception {
    final String contentType = ContentType.APPLICATION_ATOM_XML_ENTRY.toContentTypeString();
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream content = new ByteArrayInputStream(EMPLOYEE_1_XML.getBytes("UTF-8"));

    final ODataEntry result =
        new ProviderFacadeImpl().readEntry(contentType, entitySet, content, EntityProviderReadProperties.init()
            .mergeSemantic(true).build());
    assertNotNull(result);
    assertFalse(result.containsInlineEntry());
    assertNotNull(result.getExpandSelectTree());
    assertTrue(result.getExpandSelectTree().isAll());
    assertNotNull(result.getMetadata());
    assertNull(result.getMetadata().getEtag());
    assertNotNull(result.getMediaMetadata());
    assertEquals(HttpContentType.APPLICATION_OCTET_STREAM, result.getMediaMetadata().getContentType());
    assertNotNull(result.getProperties());
    assertEquals(52, result.getProperties().get("Age"));
  }

  @Test
  public void readDeltaFeedJson() throws Exception {

    final String contentType = ContentType.APPLICATION_JSON.toContentTypeString();
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream content = getFileAsStream("JsonWithDeletedEntries.json");
    EntityProviderReadProperties properties = EntityProviderReadProperties.init().build();

    ODataDeltaFeed deltaFeed = new ProviderFacadeImpl().readDeltaFeed(contentType, entitySet, content, properties);
    assertNotNull(deltaFeed);
    assertNotNull(deltaFeed.getEntries());
    assertNotNull(deltaFeed.getFeedMetadata());
    assertEquals(1, deltaFeed.getEntries().size());
    assertEquals("http://localhost:8080/ReferenceScenario.svc/Rooms?!deltatoken=4711",
        deltaFeed.getFeedMetadata().getDeltaLink());

    assertEquals(2, deltaFeed.getDeletedEntries().size());
    List<DeletedEntryMetadata> deletedEntries = deltaFeed.getDeletedEntries();
    assertEquals(2, deletedEntries.size());
    for (DeletedEntryMetadata deletedEntry : deletedEntries) {
      String uri = deletedEntry.getUri();
      if (uri.contains("Rooms('4')")) {
        assertEquals("http://host:80/service/Rooms('4')", deletedEntry.getUri());
        assertEquals(new Date(3509636760000l), deletedEntry.getWhen());
      } else if (uri.contains("Rooms('3')")) {
        assertEquals("http://host:80/service/Rooms('3')", deletedEntry.getUri());
        assertEquals(new Date(1300561560000l), deletedEntry.getWhen());
      } else {
        Assert.fail("Found unknown DeletedEntry with value: " + deletedEntry);
      }
    }
  }

  @Test
  public void readPropertyValue() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("EntryDate");
    InputStream content = new ByteArrayInputStream("2012-02-29T01:02:03".getBytes("UTF-8"));
    final Object result = new ProviderFacadeImpl().readPropertyValue(property, content, Long.class);
    assertEquals(1330477323000L, result);
  }

  @Test
  public void readProperty() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");
    final String xml = "<Age xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">42</Age>";
    InputStream content = new ByteArrayInputStream(xml.getBytes("UTF-8"));
    final Map<String, Object> result =
        new ProviderFacadeImpl().readProperty(HttpContentType.APPLICATION_XML, property, content,
            EntityProviderReadProperties.init().build());
    assertFalse(result.isEmpty());
    assertEquals(42, result.get("Age"));
  }

  @Test
  public void readLink() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream content = new ByteArrayInputStream("{\"d\":{\"uri\":\"http://somelink\"}}".getBytes("UTF-8"));
    final String result = new ProviderFacadeImpl().readLink(HttpContentType.APPLICATION_JSON, entitySet, content);
    assertEquals("http://somelink", result);
  }

  @Test
  public void readLinks() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream content =
        new ByteArrayInputStream("{\"d\":{\"__count\":\"42\",\"results\":[{\"uri\":\"http://somelink\"}]}}"
            .getBytes("UTF-8"));
    final List<String> result =
        new ProviderFacadeImpl().readLinks(HttpContentType.APPLICATION_JSON, entitySet, content);
    assertEquals(Arrays.asList("http://somelink"), result);
  }

  @Test
  public void readErrorDocumentJson() throws EntityProviderException {
    ProviderFacadeImpl providerFacade = new ProviderFacadeImpl();
    String errorDoc = "{\"error\":{\"code\":\"ErrorCode\",\"message\":{\"lang\":\"en-US\",\"value\":\"Message\"}}}";
    ODataErrorContext errorContext = providerFacade.readErrorDocument(StringHelper.encapsulate(errorDoc),
        ContentType.APPLICATION_JSON.toContentTypeString());
    //
    assertEquals("Wrong content type", "application/json", errorContext.getContentType());
    assertEquals("Wrong message", "Message", errorContext.getMessage());
    assertEquals("Wrong error code", "ErrorCode", errorContext.getErrorCode());
    assertEquals("Wrong locale for lang", Locale.US, errorContext.getLocale());
  }

  @Test
  public void writeFeed() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    List<Map<String, Object>> propertiesList = new ArrayList<Map<String, Object>>();
    final ODataResponse result =
        new ProviderFacadeImpl().writeFeed(HttpContentType.APPLICATION_JSON, entitySet, propertiesList,
            EntityProviderWriteProperties.serviceRoot(URI.create("http://root/")).build());
    assertEquals("{\"d\":{\"results\":[]}}", StringHelper.inputStreamToString((InputStream) result.getEntity()));
  }

  @Test
  public void writeEntry() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams");
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("Id", "42");
    final ODataResponse result =
        new ProviderFacadeImpl().writeEntry(HttpContentType.APPLICATION_JSON, entitySet, properties,
            EntityProviderWriteProperties.serviceRoot(URI.create("http://root/")).build());
    assertEquals("{\"d\":{\"__metadata\":{\"id\":\"http://root/Teams('42')\","
        + "\"uri\":\"http://root/Teams('42')\",\"type\":\"RefScenario.Team\"},"
        + "\"Id\":\"42\",\"Name\":null,\"isScrumTeam\":null,"
        + "\"nt_Employees\":{\"__deferred\":{\"uri\":\"http://root/Teams('42')/nt_Employees\"}}}}",
        StringHelper.inputStreamToString((InputStream) result.getEntity()));
  }

  @Test
  public void writeProperty() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("EntryDate");
    final ODataResponse result =
        new ProviderFacadeImpl().writeProperty(HttpContentType.APPLICATION_XML, property, 987654321000L);
    assertNull("EntityProvider should not set content header", result.getContentHeader());
    assertTrue(StringHelper.inputStreamToString((InputStream) result.getEntity())
        .endsWith("\">2001-04-19T04:25:21</EntryDate>"));
  }

  @Test
  public void writeLink() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("Id", "42");
    final ODataResponse result =
        new ProviderFacadeImpl().writeLink(HttpContentType.APPLICATION_JSON, entitySet, properties,
            EntityProviderWriteProperties.serviceRoot(URI.create("http://root/")).build());
    assertEquals("{\"d\":{\"uri\":\"http://root/Rooms('42')\"}}",
        StringHelper.inputStreamToString((InputStream) result.getEntity()));
  }

  @Test
  public void writeLinks() throws Exception {
    final EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("Id", "42");
    List<Map<String, Object>> propertiesList = new ArrayList<Map<String, Object>>();
    propertiesList.add(properties);
    propertiesList.add(properties);
    final ODataResponse result =
        new ProviderFacadeImpl().writeLinks(HttpContentType.APPLICATION_JSON, entitySet, propertiesList,
            EntityProviderWriteProperties.serviceRoot(URI.create("http://root/")).build());
    assertEquals("{\"d\":[{\"uri\":\"http://root/Rooms('42')\"},{\"uri\":\"http://root/Rooms('42')\"}]}",
        StringHelper.inputStreamToString((InputStream) result.getEntity()));
  }

  @Test
  public void writeServiceDocument() throws Exception {
    final ODataResponse result =
        new ProviderFacadeImpl()
            .writeServiceDocument(HttpContentType.APPLICATION_JSON, MockFacade.getMockEdm(), "root");
    assertEquals("{\"d\":{\"EntitySets\":[]}}", StringHelper.inputStreamToString((InputStream) result.getEntity()));
  }

  @Test
  public void writePropertyValue() throws Exception {
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("EntryDate");
    final ODataResponse result = new ProviderFacadeImpl().writePropertyValue(property, 987654321000L);
    assertNull("BasicProvider should not set content header", result.getContentHeader());

    assertEquals("2001-04-19T04:25:21", StringHelper.inputStreamToString((InputStream) result.getEntity()));
  }

  @Test
  public void writeText() throws Exception {
    final ODataResponse result = new ProviderFacadeImpl().writeText("test");
    assertNull("BasicProvider should not set content header", result.getContentHeader());
    assertEquals("test", StringHelper.inputStreamToString((InputStream) result.getEntity()));
  }

  @Test
  public void writeBinary() throws Exception {
    final ODataResponse result =
        new ProviderFacadeImpl().writeBinary(HttpContentType.APPLICATION_OCTET_STREAM, new byte[] { 102, 111, 111 });
    assertEquals(HttpContentType.APPLICATION_OCTET_STREAM, result.getContentHeader());
    assertEquals("foo", StringHelper.inputStreamToString((InputStream) result.getEntity()));
  }

  @Test
  public void writeBinaryNoContent() throws Exception {
    final ODataResponse result = new ProviderFacadeImpl().writeBinary(HttpContentType.APPLICATION_OCTET_STREAM, null);
    assertNull(result.getEntity());
    assertNull(result.getContentHeader());
    assertEquals(HttpStatusCodes.NO_CONTENT, result.getStatus());
  }

  @Test
  public void writeFunctionImport() throws Exception {
    final EdmFunctionImport function =
        MockFacade.getMockEdm().getDefaultEntityContainer().getFunctionImport("MaximalAge");
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("MaximalAge", 99);
    final ODataResponse result =
        new ProviderFacadeImpl().writeFunctionImport(HttpContentType.APPLICATION_JSON, function, properties, null);
    assertEquals("{\"d\":{\"MaximalAge\":99}}", StringHelper.inputStreamToString((InputStream) result.getEntity()));
  }
}
