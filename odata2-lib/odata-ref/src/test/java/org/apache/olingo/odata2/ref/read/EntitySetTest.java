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
package org.apache.olingo.odata2.ref.read;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;

import org.apache.olingo.odata2.annotation.processor.core.ListsProcessor;
import org.apache.olingo.odata2.annotation.processor.core.datasource.BeanPropertyAccess;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.ref.model.DataContainer;
import org.apache.olingo.odata2.ref.processor.ScenarioDataSource;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *  
 */
public class EntitySetTest extends BaseTest {

  private static DataContainer dataContainer;
  private static ScenarioDataSource dataSource;
  private static ListsProcessor processor;

  @BeforeClass
  public static void init() {
    dataContainer = new DataContainer();
    dataContainer.reset();
    dataSource = new ScenarioDataSource(dataContainer);
    processor = new ListsProcessor(dataSource, new BeanPropertyAccess());
  }

  @Before
  public void setUp() throws Exception {
    ODataContext context = mock(ODataContext.class);
    PathInfo pathInfo = mock(PathInfo.class);
    when(pathInfo.getServiceRoot()).thenReturn(new URI("http://localhost/"));
    when(pathInfo.getRequestUri()).thenReturn(new URI("http://localhost/EntitySet"));
    when(context.getPathInfo()).thenReturn(pathInfo);

    processor.setContext(context);
  }

  private UriInfo mockUriResult(final String entitySetName) throws ODataException, URISyntaxException {
    EdmEntityType entityType = mock(EdmEntityType.class);
    when(entityType.getName()).thenReturn(entitySetName);
    EdmEntityContainer entityContainer = mock(EdmEntityContainer.class);
    when(entityContainer.isDefaultEntityContainer()).thenReturn(true);
    EdmEntitySet entitySet = mock(EdmEntitySet.class);
    when(entitySet.getName()).thenReturn(entitySetName);
    when(entitySet.getEntityType()).thenReturn(entityType);
    when(entitySet.getEntityContainer()).thenReturn(entityContainer);

    UriInfo uriResult = mock(UriInfo.class);
    when(uriResult.getStartEntitySet()).thenReturn(entitySet);
    when(uriResult.getTargetEntitySet()).thenReturn(entitySet);
    when(uriResult.getTop()).thenReturn(null);
    return uriResult;
  }

  private String readContent(final ODataResponse response) throws IOException {
    CharBuffer content = CharBuffer.allocate(1000);
    new InputStreamReader((InputStream) response.getEntity()).read(content);
    content.rewind();
    return content.toString();
  }

  @Test
  public void readEmployees() throws Exception {
    final UriInfo uriResult = mockUriResult("Employees");

    ODataResponse response =
        processor.readEntitySet(uriResult, ContentType.APPLICATION_ATOM_XML_FEED.toContentTypeString());
    assertNotNull(response);
    assertTrue(readContent(response).contains("Employee"));
  }

  @Test
  public void readTeams() throws Exception {
    final UriInfo uriResult = mockUriResult("Teams");

    ODataResponse response =
        processor.readEntitySet(uriResult, ContentType.APPLICATION_ATOM_XML_FEED.toContentTypeString());
    assertNotNull(response);
    assertTrue(readContent(response).contains("Team"));
  }

  @Test
  public void readRooms() throws Exception {
    final UriInfo uriResult = mockUriResult("Rooms");

    ODataResponse response =
        processor.readEntitySet(uriResult, ContentType.APPLICATION_ATOM_XML_FEED.toContentTypeString());
    assertNotNull(response);
    assertTrue(readContent(response).contains("Room"));
  }

  @Test
  public void readManagers() throws Exception {
    final UriInfo uriResult = mockUriResult("Managers");

    ODataResponse response =
        processor.readEntitySet(uriResult, ContentType.APPLICATION_ATOM_XML_FEED.toContentTypeString());
    assertNotNull(response);
    assertTrue(readContent(response).contains("Manager"));
  }

  @Test
  public void readBuildings() throws Exception {
    final UriInfo uriResult = mockUriResult("Buildings");

    ODataResponse response =
        processor.readEntitySet(uriResult, ContentType.APPLICATION_ATOM_XML_FEED.toContentTypeString());
    assertNotNull(response);
    assertTrue(readContent(response).contains("Building"));
  }

  @Test
  public void readPhotos() throws Exception {
    final UriInfo uriResult = mockUriResult("Photos");

    ODataResponse response =
        processor.readEntitySet(uriResult, ContentType.APPLICATION_ATOM_XML_FEED.toContentTypeString());
    assertNotNull(response);
    assertTrue(readContent(response).contains("Photo"));
  }

}
