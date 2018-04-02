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
package org.apache.olingo.odata2.client.core.ep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSet;
import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.client.api.ODataClient;
import org.apache.olingo.odata2.client.api.ep.DeserializerProperties;
import org.apache.olingo.odata2.client.api.ep.Entity;
import org.apache.olingo.odata2.client.api.ep.EntityCollection;
import org.apache.olingo.odata2.client.api.ep.EntityCollectionSerializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntitySerializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntityStream;
import org.apache.olingo.odata2.core.batch.v2.BatchLineReader;
import org.apache.olingo.odata2.core.batch.v2.BatchParser;
import org.apache.olingo.odata2.core.batch.v2.Line;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

public class ProducerConsumerIntegrationTest {
  protected static final URI BASE_URI;
  private static final String PUT = "PUT";
  private static final String BOUNDARY = "batch_123";
  private static final Object CRLF = "\r\n";

  static {
    try {
      BASE_URI = new URI("http://host:80/service/");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
  private static final DeserializerProperties DEFAULT_READ_PROPERTIES 
  = DeserializerProperties.init()
      .build();
  private static final EntitySerializerProperties DEFAULT_WRITE_PROPERTIES 
  = EntitySerializerProperties
      .serviceRoot(
          BASE_URI).build();
  private static final String XML = "application/xml";
  private static final String JSON = "application/json";

  @Test
  public void produceRoomAndThenConsumeIt() throws Exception {
    EdmEntitySet roomSet = MockFacade.getMockEdm()
        .getDefaultEntityContainer().getEntitySet("Rooms");
    Entity localRoomData = new Entity();
    localRoomData.addProperty("Id", "1");
    localRoomData.addProperty("Name", "Neu \n Schwanstein蝴蝶");

    Map<String, Object> properties = execute(localRoomData, roomSet, XML);
    assertEquals("1", properties.get("Id"));
    assertEquals("Neu \n Schwanstein蝴蝶", properties.get("Name"));

    Map<String, Object> properties2 = execute(localRoomData, roomSet, JSON);
    assertEquals("1", properties2.get("Id"));
    assertEquals("Neu \n Schwanstein蝴蝶", properties2.get("Name"));
  }

  @Test
  public void produceRoomFeedAndThenConsumeIt() throws Exception {
    EdmEntitySet roomSet = MockFacade.getMockEdm()
        .getDefaultEntityContainer().getEntitySet("Rooms");
    EntityCollection roomsData = new EntityCollection();
    Entity localRoomData = new Entity();
    localRoomData.addProperty("Id", "1");
    localRoomData.addProperty("Name", "Neu \n Schwanstein蝴蝶");
    roomsData.addEntity(localRoomData);

    localRoomData = new Entity();
    localRoomData.addProperty("Id", "2");
    localRoomData.addProperty("Name", "John蝴蝶");
    roomsData.addEntity(localRoomData);
    
    List<Map<String, Object>> entries = execute1(roomsData, roomSet, XML);
    validateResults(entries);
    
    entries = execute1(roomsData, roomSet, JSON);
    validateResults(entries);
  }
  
  @Test(expected=EntityProviderException.class)
  public void negativeTests() throws Exception {
    EdmEntitySet roomSet = MockFacade.getMockEdm()
        .getDefaultEntityContainer().getEntitySet("Rooms");
    Entity localRoomData = new Entity();
    localRoomData.addProperty("Id", "1");
    localRoomData.addProperty("Name", "Neu \n Schwanstein蝴蝶");

    Map<String, Object> properties = execute(localRoomData, roomSet, "abc");
    assertEquals("1", properties.get("Id"));
    assertEquals("Neu \n Schwanstein蝴蝶", properties.get("Name"));   
  }
  
  @Test(expected=EntityProviderException.class)
  public void negativeTestsDeserializer() throws Exception {
    EdmEntitySet roomSet = MockFacade.getMockEdm()
        .getDefaultEntityContainer().getEntitySet("Rooms");
    Entity localRoomData = new Entity();
    localRoomData.addProperty("Id", "1");
    localRoomData.addProperty("Name", "Neu \n Schwanstein蝴蝶");

    Map<String, Object> properties = executeFail(localRoomData, roomSet, "abc");
    assertEquals("1", properties.get("Id"));
    assertEquals("Neu \n Schwanstein蝴蝶", properties.get("Name"));   
  }

  /**
   * @param entries
   */
  private void validateResults(List<Map<String, Object>> entries) {
    for (Map<String, Object> entryProperties : entries) {
      for (Entry<String, Object> properties : entryProperties.entrySet()) {
        if (properties.getKey().equals("Id")) {
          assertTrue(properties.getValue().toString().contains("1") || 
              properties.getValue().toString().contains("2"));
        } else {
          assertTrue(properties.getValue().toString().contains("Neu \n Schwanstein蝴蝶") || 
              properties.getValue().toString().contains("John蝴蝶"));
        }
      }
    }
  }
  
  private Map<String, Object> execute(final Entity localRoomData, final EdmEntitySet roomSet,
      final String contentType)
      throws ODataException {
    localRoomData.setWriteProperties(DEFAULT_WRITE_PROPERTIES);
    ODataResponse response = ODataClient.newInstance().createSerializer(contentType)
        .writeEntry(roomSet, localRoomData);
    InputStream content = response.getEntityAsStream();
    EntityStream entityContent = new EntityStream();
    entityContent.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityContent.setContent(content);
    ODataEntry entry = ODataClient.newInstance()
        .createDeserializer(contentType).readEntry(roomSet, entityContent);
    Map<String, Object> properties = entry.getProperties();
    return properties;
  }
  
  private Map<String, Object> executeFail(final Entity localRoomData, final EdmEntitySet roomSet,
      final String contentType)
      throws ODataException {
    localRoomData.setWriteProperties(DEFAULT_WRITE_PROPERTIES);
    ODataResponse response = ODataClient.newInstance().createSerializer(XML)
        .writeEntry(roomSet, localRoomData);
    InputStream content = response.getEntityAsStream();
    EntityStream entityContent = new EntityStream();
    entityContent.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityContent.setContent(content);
    ODataEntry entry = ODataClient.newInstance()
        .createDeserializer(contentType).readEntry(roomSet, entityContent);
    Map<String, Object> properties = entry.getProperties();
    return properties;
  }

  private List<Map<String, Object>> execute1(final EntityCollection localRoomData, final EdmEntitySet roomSet,
      final String contentType)
      throws ODataException {
    List<Map<String, Object>> propertiesList = new ArrayList<Map<String,Object>>();
    localRoomData.setCollectionProperties(EntityCollectionSerializerProperties.serviceRoot(BASE_URI).build());
    ODataResponse response = ODataClient.newInstance().createSerializer(contentType)
        .writeFeed(roomSet, localRoomData);
    InputStream content = response.getEntityAsStream();
    EntityStream entityContent = new EntityStream();
    entityContent.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityContent.setContent(content);
    ODataFeed feed = ODataClient.newInstance()
        .createDeserializer(contentType).readFeed(roomSet, entityContent);
    List<ODataEntry> entries = feed.getEntries();
    for (ODataEntry entry : entries) {
      propertiesList.add(entry.getProperties());
    }
    return propertiesList;
  }
  
  
  @Test
  public void executeWriteBatchRequestJSON() throws IOException, 
  EntityProviderException, BatchException, URISyntaxException {
    List<BatchPart> batch = new ArrayList<BatchPart>();
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("content-type", "application/json");
    BatchChangeSetPart request = BatchChangeSetPart.method(PUT)
        .uri("Employees('2')")
        .body("{\"Age\":40}")
        .headers(headers)
        .contentId("111")
        .build();
    BatchChangeSet changeSet = BatchChangeSet.newBuilder().build();
    changeSet.add(request);
    batch.add((BatchPart) changeSet);

    InputStream batchRequest = ODataClient.newInstance().createSerializer(JSON).
        readBatchRequest(batch, BOUNDARY);
    validateBatchRequest(batchRequest);
  }
  
  private void validateBatchRequest(InputStream batchRequest) throws IOException {
    BatchLineReader reader =
        new BatchLineReader(batchRequest);
    List<Line> lines = reader.toLineList();
    reader.close();
    int index = 0;
     
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
    assertTrue(lines.get(index++).toString().startsWith("Content-Type: multipart/mixed; boundary=changeset_"));
    assertEquals(CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertEquals("Content-Type: application/http" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Transfer-Encoding: binary" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Id: 111" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("PUT Employees('2') HTTP/1.1" + CRLF, lines.get(index++).toString());
    assertEquals("Content-Length: 10" + CRLF, lines.get(index++).toString());
    assertEquals("content-type: application/json" + CRLF, lines.get(index++).toString());
    assertEquals(CRLF, lines.get(index++).toString());
    assertEquals("{\"Age\":40}" + CRLF, lines.get(index++).toString());
    assertTrue(lines.get(index++).toString().startsWith("--changeset"));
    assertTrue(lines.get(index++).toString().startsWith("--batch"));
  }
  
  @Test
  public void executeWriteBatchResponseJSON() throws BatchException, EntityProviderException {
    List<BatchResponsePart> parts = new ArrayList<BatchResponsePart>();
    ODataResponse response = ODataResponse.entity("Walter Winter")
        .status(HttpStatusCodes.OK)
        .contentHeader("application/json")
        .build();
    List<ODataResponse> responses = new ArrayList<ODataResponse>(1);
    responses.add(response);
    parts.add(BatchResponsePart.responses(responses).changeSet(false).build());

    ODataResponse changeSetResponse = ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
    responses = new ArrayList<ODataResponse>(1);
    responses.add(changeSetResponse);
    parts.add(BatchResponsePart.responses(responses).changeSet(true).build());

    ODataResponse batchResponse = ODataClient.newInstance().createDeserializer(JSON).writeBatchResponse(parts);
    assertEquals(202, batchResponse.getStatus().getStatusCode());
    assertNotNull(batchResponse.getEntity());
    String body = (String) batchResponse.getEntity();

    assertTrue(body.contains("--batch"));
    assertTrue(body.contains("--changeset"));
    assertTrue(body.contains("HTTP/1.1 200 OK"));
    assertTrue(body.contains("Content-Type: application/http"));
    assertTrue(body.contains("Content-Transfer-Encoding: binary"));
    assertTrue(body.contains("Walter Winter"));
    assertTrue(body.contains("multipart/mixed; boundary=changeset"));
    assertTrue(body.contains("HTTP/1.1 204 No Content"));

    String contentHeader = batchResponse.getContentHeader();
    BatchParser parser = new BatchParser(contentHeader, true);
    List<BatchSingleResponse> result = parser.parseBatchResponse(new ByteArrayInputStream(body.getBytes()));
    assertEquals(2, result.size());
  }
  
  @Test
  public void readFunctionImportJOSNSimpleProperty1() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("MaximalAge");
    InputStream content = new ByteArrayInputStream("{\"d\":{\"MaximalAge\":42}}".getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);    
    
    final Object result = executeFunctionImport(functionImport, entityStream, JSON);
    assertEquals((short) 42, result);
  }
  
  @Test
  public void readFunctionImportXMLSimpleProperty() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("MaximalAge");
    InputStream content = new ByteArrayInputStream((
        "<?xml version='1.0' encoding='utf-8'?>"
        + "<MaximalAge xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" "
        + "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">"
        + "42</MaximalAge>").getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);    
    
    final Object result = executeFunctionImport(functionImport, entityStream, XML);
    assertEquals((short) 42, result);
  }
  
  @Test
  public void readFunctionImportJOSNSimpleProperty2() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("MaximalAge");
    InputStream content = new ByteArrayInputStream("{\"MaximalAge\":42}".getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);    
    
    final Object result = executeFunctionImport(functionImport, entityStream, JSON);
    assertEquals((short) 42, result);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void readFunctionImportJSONCollectionOfComplexProperty1() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("AllLocations");
    InputStream content = new ByteArrayInputStream((
        "{\"results\": [{\"City\": {\"PostalCode\":\"56\",\"CityName\":\"Bangalore\"},"
        + "\"Country\": \"India\"}]}").getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);    
    
    final Object result = executeFunctionImport(functionImport, entityStream, JSON);
    List<Map<String, Object>> res = (List<Map<String, Object>>) result;
    assertEquals(1, res.size());
    assertEquals("India", ((HashMap<String, Object>)res.get(0)).get("Country"));
    assertEquals(2, ((Map<String, Object>)((Map<String, Object>)res.get(0)).get("City")).size());
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void readFunctionImportJSONCollectionOfComplexProperty2() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("AllLocations");
    InputStream content = new ByteArrayInputStream((
        "{\"d\":{\"results\": [{\"City\": {\"PostalCode\":\"56\",\"CityName\":\"Bangalore\"},"
        + "\"Country\": \"India\"}]}}").getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);    
    
    final Object result = executeFunctionImport(functionImport, entityStream, JSON);
    List<Map<String, Object>> res = (List<Map<String, Object>>) result;
    assertEquals(1, res.size());
    assertEquals("India", ((HashMap<String, Object>)res.get(0)).get("Country"));
    assertEquals(2, ((Map<String, Object>)((Map<String, Object>)res.get(0)).get("City")).size());
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void readFunctionImportXMLCollectionOfComplexProperty() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("AllLocations");
    InputStream content = new ByteArrayInputStream((
        "<?xml version='1.0' encoding='utf-8'?>"
        + "<AllLocations xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" "
        + "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">"
        + "<element m:type=\"RefScenario.c_Location\">"
        + "<City m:type=\"RefScenario.c_City\">"
        + "<PostalCode>69124</PostalCode>"
        + "<CityName>Heidelberg</CityName>"
        + "</City>"
        + "<Country>Germany</Country>"
        + "</element>"
        + "<element m:type=\"RefScenario.c_Location\">"
        + "<City m:type=\"RefScenario.c_City\">"
        + "<PostalCode>69190</PostalCode>"
        + "<CityName>Walldorf</CityName>"
        + "</City>"
        + "<Country>Germany</Country>"
        + "</element>"
        + "</AllLocations>").getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);    
    
    final Object result = executeFunctionImport(functionImport, entityStream, XML);
    List<Map<String, Object>> res = (List<Map<String, Object>>) result;
    assertEquals(2, res.size());
    assertEquals("Germany", ((HashMap<String, Object>)res.get(0)).get("Country"));
    assertEquals(2, ((Map<String, Object>)((Map<String, Object>)res.get(0)).get("City")).size());
  }
  
  @Test
  public void readFunctionImportJSONSingleEntity1() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("OldestEmployee");
    InputStream content = new ByteArrayInputStream(
        ("{\"d\": {"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.Employee\","
            + "\"content_type\": \"image/jpeg\","
            + "\"media_src\":\"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/$value\","
            + "\"edit_media\":\"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/$value\""
            + "},"
            + "\"EmployeeId\": \"3\","
            + "\"EmployeeName\": \"Jonathan Smith\","
            + "\"ManagerId\": \"1\","
            + "\"RoomId\": \"2\","
            + "\"TeamId\": \"1\","
            + "\"Location\": {"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.c_Location\""
            + "},"
            + "\"City\": {"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.c_City\""
            + "},"
            + "\"PostalCode\": \"69190\","
            + "\"CityName\": \"Walldorf\""
            + "},"
            + "\"Country\": \"Germany\""
            + "},"
            + "\"Age\": 56,"
            + "\"EntryDate\": null,"
            + "\"ImageUrl\": \"Employees('3')/$value\","
            + "\"ne_Manager\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Manager\""
            + "}"
            + "},"
            + "\"ne_Team\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Team\""
            + "}"
            + "},"
            + "\"ne_Room\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Room\""
            + "}"
            + "}"
            + "}"
            + "}").getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);    
    
    final Object result = executeFunctionImport(functionImport, entityStream, JSON);
    ODataEntry entry = (ODataEntry) result;
    assertEquals(9, entry.getProperties().size());
    assertEquals("3", entry.getProperties().get("EmployeeId"));
  }
  
  @Test
  public void readFunctionImportXMLSingleEntity() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("OldestEmployee");
    InputStream content = new ByteArrayInputStream(
        ("<?xml version='1.0' encoding='utf-8'?>"
            + "<entry xmlns=\"http://www.w3.org/2005/Atom\" "
            + "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" "
            + "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" "
            + "xml:base=\"http://localhost:8083/olingo-odata2-ref-web/ReferenceScenarioNonJaxrs.svc/\">"
            + "<id>"
            + "http://localhost:8083/olingo-odata2-ref-web/ReferenceScenarioNonJaxrs.svc/Employees('3')"
            + "</id>"
            + "<title type=\"text\">Jonathan Smith</title>"
            + "<updated>2017-10-26T09:06:41.15+05:30</updated>"
            + "<category term=\"RefScenario.Employee\" "
            + "scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>"
            + "<link href=\"Employees('3')\" rel=\"edit\" title=\"Employee\"/>"
            + "<link href=\"Employees('3')/$value\" rel=\"edit-media\" "
            + "type=\"image/jpeg\"/><link href=\"Employees('3')/ne_Manager\" "
            + "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Manager\" "
            + "title=\"ne_Manager\" type=\"application/atom+xml;type=entry\"/>"
            + "<link href=\"Employees('3')/ne_Team\" "
            + "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Team\" "
            + "title=\"ne_Team\" type=\"application/atom+xml;type=entry\"/>"
            + "<link href=\"Employees('3')/ne_Room\" "
            + "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Room\" "
            + "title=\"ne_Room\" type=\"application/atom+xml;type=entry\"/>"
            + "<content type=\"image/jpeg\" src=\"Employees('3')/$value\"/>"
            + "<m:properties><d:EmployeeId>3</d:EmployeeId>"
            + "<d:EmployeeName>Jonathan Smith</d:EmployeeName>"
            + "<d:ManagerId>1</d:ManagerId><d:RoomId>2</d:RoomId>"
            + "<d:TeamId>1</d:TeamId><d:Location m:type=\"RefScenario.c_Location\">"
            + "<d:City m:type=\"RefScenario.c_City\"><d:PostalCode>69190</d:PostalCode>"
            + "<d:CityName>Walldorf</d:CityName></d:City><d:Country>Germany</d:Country>"
            + "</d:Location><d:Age>56</d:Age><d:EntryDate m:null=\"true\"/>"
            + "<d:ImageUrl>Employees('3')/$value</d:ImageUrl>"
            + "</m:properties></entry>").getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);    
    
    final Object result = executeFunctionImport(functionImport, entityStream, XML);
    ODataEntry entry = (ODataEntry) result;
    assertEquals(9, entry.getProperties().size());
    assertEquals("3", entry.getProperties().get("EmployeeId"));
    assertEquals("Employees('3')/ne_Manager", entry.getMetadata().getAssociationUris("ne_Manager").get(0));
    assertEquals("http://localhost:8083/olingo-odata2-ref-web/"
        + "ReferenceScenarioNonJaxrs.svc/Employees('3')", entry.getMetadata().getId());
  }
  
  @Test
  public void readFunctionImportJSONSingleEntity2() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("OldestEmployee");
    InputStream content = new ByteArrayInputStream(
        ("{"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.Employee\","
            + "\"content_type\": \"image/jpeg\","
            + "\"media_src\":\"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/$value\","
            + "\"edit_media\":\"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/$value\""
            + "},"
            + "\"EmployeeId\": \"3\","
            + "\"EmployeeName\": \"Jonathan Smith\","
            + "\"ManagerId\": \"1\","
            + "\"RoomId\": \"2\","
            + "\"TeamId\": \"1\","
            + "\"Location\": {"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.c_Location\""
            + "},"
            + "\"City\": {"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.c_City\""
            + "},"
            + "\"PostalCode\": \"69190\","
            + "\"CityName\": \"Walldorf\""
            + "},"
            + "\"Country\": \"Germany\""
            + "},"
            + "\"Age\": 56,"
            + "\"EntryDate\": null,"
            + "\"ImageUrl\": \"Employees('3')/$value\","
            + "\"ne_Manager\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Manager\""
            + "}"
            + "},"
            + "\"ne_Team\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Team\""
            + "}"
            + "},"
            + "\"ne_Room\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Room\""
            + "}"
            + "}"
            + "}").getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);    
    
    final Object result = executeFunctionImport(functionImport, entityStream, JSON);
    ODataEntry entry = (ODataEntry) result;
    assertEquals(9, entry.getProperties().size());
    assertEquals("3", entry.getProperties().get("EmployeeId"));
  }
  
  @Test
  public void readMultipleEntityJSONFunctionImport1() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("EmployeeSearch");
    InputStream content = new ByteArrayInputStream(
        ("{\"d\": {"
            + "\"results\": [{"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.Employee\","
            + "\"content_type\": \"image/jpeg\","
            + "\"media_src\":\"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/$value\","
            + "\"edit_media\":\"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/$value\""
            + "},"
            + "\"EmployeeId\": \"3\","
            + "\"EmployeeName\": \"Jonathan Smith\","
            + "\"ManagerId\": \"1\","
            + "\"RoomId\": \"2\","
            + "\"TeamId\": \"1\","
            + "\"Location\": {"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.c_Location\""
            + "},"
            + "\"City\": {"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.c_City\""
            + "},"
            + "\"PostalCode\": \"69190\","
            + "\"CityName\": \"Walldorf\""
            + "},"
            + "\"Country\": \"Germany\""
            + "},"
            + "\"Age\": 56,"
            + "\"EntryDate\": null,"
            + "\"ImageUrl\": \"Employees('3')/$value\","
            + "\"ne_Manager\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Manager\""
            + "}"
            + "},"
            + "\"ne_Team\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Team\""
            + "}"
            + "},"
            + "\"ne_Room\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Room\""
            + "}"
            + "}"
            + "}]"
            + "}"
            + "}").getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);
    final Object result = executeFunctionImport(functionImport, entityStream, JSON);
    ODataDeltaFeed feed = (ODataDeltaFeed) result;
    List<ODataEntry> entries = feed.getEntries();
    int size = entries.size();
    assertEquals(1, size);
    String id = (String) entries.get(0).getProperties().get("EmployeeId");
    assertEquals("3", id);
  }
  
  @Test
  public void readMultipleEntityJSONFunctionImport2() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("EmployeeSearch");
    InputStream content = new ByteArrayInputStream(
        ("{"
            + "\"results\": [{"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.Employee\","
            + "\"content_type\": \"image/jpeg\","
            + "\"media_src\":\"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/$value\","
            + "\"edit_media\":\"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/$value\""
            + "},"
            + "\"EmployeeId\": \"3\","
            + "\"EmployeeName\": \"Jonathan Smith\","
            + "\"ManagerId\": \"1\","
            + "\"RoomId\": \"2\","
            + "\"TeamId\": \"1\","
            + "\"Location\": {"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.c_Location\""
            + "},"
            + "\"City\": {"
            + "\"__metadata\": {"
            + "\"type\": \"RefScenario.c_City\""
            + "},"
            + "\"PostalCode\": \"69190\","
            + "\"CityName\": \"Walldorf\""
            + "},"
            + "\"Country\": \"Germany\""
            + "},"
            + "\"Age\": 56,"
            + "\"EntryDate\": null,"
            + "\"ImageUrl\": \"Employees('3')/$value\","
            + "\"ne_Manager\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Manager\""
            + "}"
            + "},"
            + "\"ne_Team\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Team\""
            + "}"
            + "},"
            + "\"ne_Room\": {"
            + "\"__deferred\": {"
            + "\"uri\": \"http://localhost:19000/abc/FunctionImportJsonTest/Employees('3')/ne_Room\""
            + "}"
            + "}"
            + "}]"
            + "}").getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);
    final Object result = executeFunctionImport(functionImport, entityStream, JSON);
    ODataDeltaFeed feed = (ODataDeltaFeed) result;
    List<ODataEntry> entries = feed.getEntries();
    int size = entries.size();
    assertEquals(1, size);
    String id = (String) entries.get(0).getProperties().get("EmployeeId");
    assertEquals("3", id);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void readMultipleEntityXMLFunctionImport() throws Exception {
    final EdmFunctionImport functionImport = MockFacade.getMockEdm().getDefaultEntityContainer()
        .getFunctionImport("EmployeeSearch");
    InputStream content = new ByteArrayInputStream(
        ("<?xml version='1.0' encoding='utf-8'?>"
            + "<feed xmlns=\"http://www.w3.org/2005/Atom\" "
            + "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" "
            + "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" "
            + "xml:base=\"http://localhost:8083/olingo-odata2-ref-web/ReferenceScenarioNonJaxrs.svc/\">"
            + "<id>http://localhost:8083/olingo-odata2-ref-web/ReferenceScenarioNonJaxrs.svc/Employees</id>"
            + "<title type=\"text\">Employees</title><updated>2017-10-26T09:17:27.113+05:30</updated>"
            + "<author><name/></author><link href=\"Employees\" rel=\"self\" title=\"Employees\"/>"
            + "<entry>"
            + "<id>http://localhost:8083/olingo-odata2-ref-web/ReferenceScenarioNonJaxrs.svc/Employees('1')</id>"
            + "<title type=\"text\">Walter Winter</title>"
            + "<updated>1999-01-01T00:00:00Z</updated><category term=\"RefScenario.Employee\" "
            + "scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>"
            + "<link href=\"Employees('1')\" rel=\"edit\" title=\"Employee\"/>"
            + "<link href=\"Employees('1')/$value\" rel=\"edit-media\" type=\"image/jpeg\"/>"
            + "<link href=\"Employees('1')/ne_Manager\" "
            + "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Manager\" "
            + "title=\"ne_Manager\" type=\"application/atom+xml;type=entry\"/>"
            + "<link href=\"Employees('1')/ne_Team\" "
            + "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Team\" "
            + "title=\"ne_Team\" type=\"application/atom+xml;type=entry\"/>"
            + "<link href=\"Employees('1')/ne_Room\" "
            + "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Room\" "
            + "title=\"ne_Room\" type=\"application/atom+xml;type=entry\"/>"
            + "<content type=\"image/jpeg\" src=\"Employees('1')/$value\"/>"
            + "<m:properties><d:EmployeeId>1</d:EmployeeId>"
            + "<d:EmployeeName>Walter Winter</d:EmployeeName>"
            + "<d:ManagerId>1</d:ManagerId><d:RoomId>1</d:RoomId><d:TeamId>1</d:TeamId>"
            + "<d:Location m:type=\"RefScenario.c_Location\"><d:City m:type=\"RefScenario.c_City\">"
            + "</d:City>"
            + "<d:Country>Germany</d:Country></d:Location><d:Age>52</d:Age>"
            + "<d:EntryDate>1999-01-01T00:00:00</d:EntryDate>"
            + "<d:ImageUrl>Employees('1')/$value</d:ImageUrl>"
            + "</m:properties></entry>"
            + "<entry>"
            + "<id>http://localhost:8083/olingo-odata2-ref-web/ReferenceScenarioNonJaxrs.svc/Employees('2')</id>"
            + "<title type=\"text\">Frederic Fall</title>"
            + "<updated>2003-07-01T00:00:00Z</updated>"
            + "<category term=\"RefScenario.Employee\" "
            + "scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>"
            + "<link href=\"Employees('2')\" rel=\"edit\" title=\"Employee\"/>"
            + "<link href=\"Employees('2')/$value\" rel=\"edit-media\" "
            + "type=\"image/jpeg\"/><link href=\"Employees('2')/ne_Manager\" "
            + "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Manager\" "
            + "title=\"ne_Manager\" type=\"application/atom+xml;type=entry\"/>"
            + "<link href=\"Employees('2')/ne_Team\" "
            + "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Team\" "
            + "title=\"ne_Team\" type=\"application/atom+xml;type=entry\"/>"
            + "<link href=\"Employees('2')/ne_Room\" "
            + "rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ne_Room\" "
            + "title=\"ne_Room\" type=\"application/atom+xml;type=entry\"/>"
            + "<content type=\"image/jpeg\" src=\"Employees('2')/$value\"/>"
            + "<m:properties><d:EmployeeId>2</d:EmployeeId>"
            + "<d:EmployeeName>Frederic Fall</d:EmployeeName>"
            + "<d:ManagerId>1</d:ManagerId><d:RoomId>2</d:RoomId>"
            + "<d:TeamId>1</d:TeamId>"
            + "<d:Location m:type=\"RefScenario.c_Location\" m:null=\"true\">"
            + "</d:Location><d:Age>32</d:Age>"
            + "<d:EntryDate>2003-07-01T00:00:00</d:EntryDate>"
            + "</m:properties></entry></feed>").getBytes("UTF-8"));
    EntityStream entityStream = new EntityStream();
    entityStream.setReadProperties(DEFAULT_READ_PROPERTIES);
    entityStream.setContent(content);
    final Object result = executeFunctionImport(functionImport, entityStream, XML);
    ODataDeltaFeed feed = (ODataDeltaFeed) result;
    List<ODataEntry> entries = feed.getEntries();
    int size = entries.size();
    assertEquals(2, size);
    assertEquals(9, entries.get(0).getProperties().size());
    assertEquals(8, entries.get(1).getProperties().size());
    assertEquals(0, ((Map<String, Object>)
        ((Map<String, Object>)entries.get(0).getProperties().get("Location")).get("City")).size());
    assertEquals(null, entries.get(1).getProperties().get("Location"));
    String id = (String) entries.get(0).getProperties().get("EmployeeId");
    assertEquals("1", id);
  }

  /**
   * @param functionImport
   * @param entityStream
   * @return
   * @throws EntityProviderException
   */
  private Object executeFunctionImport(final EdmFunctionImport functionImport, 
      EntityStream entityStream, String contentType)
      throws EntityProviderException {
    return ODataClient.newInstance().
        createDeserializer(contentType).readFunctionImport(functionImport, entityStream);
  }
}
