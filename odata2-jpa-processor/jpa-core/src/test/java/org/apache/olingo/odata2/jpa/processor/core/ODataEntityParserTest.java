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
package org.apache.olingo.odata2.jpa.processor.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataContextMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataJPAContextMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataServiceMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.PathInfoMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.PathSegmentMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.EdmMockUtilV2;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock;
import org.junit.Test;

public class ODataEntityParserTest {

  private ODataEntityParser parser;

  private ODataJPAContext mock(final String path) {
    ODataServiceMock serviceMock = new ODataServiceMock();
    ODataContextMock contextMock = new ODataContextMock();
    PathInfoMock pathInfoMock = new PathInfoMock();
    PathSegmentMock pathSegmentMock = new PathSegmentMock();
    ODataJPAContext odataJPAContext = null;

    try {

      pathSegmentMock.setPath(path);

      List<PathSegment> pathSegments = new ArrayList<PathSegment>();
      pathSegments.add(pathSegmentMock);
      pathInfoMock.setPathSegments(pathSegments);
      pathInfoMock.setServiceRootURI(ODataServiceMock.SERVICE_ROOT);

      contextMock.setPathInfo(pathInfoMock.mock());
      contextMock.setODataService(serviceMock.mock());

      odataJPAContext = ODataJPAContextMock.mockODataJPAContext(contextMock.mock());
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (URISyntaxException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    return odataJPAContext;
  }

  @Test
  public void testParseURISegment() {

    try {
      parser = new ODataEntityParser(mock("JPATypeMock(2)"));
      UriInfo uriInfo = parser.parseURISegment(0, 1);
      assertNotNull(uriInfo);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testParseURISegmentInvalidIndex00() {
    try {
      parser = new ODataEntityParser(mock("JPATypeMock(2)"));
      UriInfo uriInfo = parser.parseURISegment(0, 0);
      assertNull(uriInfo);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testParseURISegmentInvalidIndex01() {
    try {
      parser = new ODataEntityParser(mock("JPATypeMock(2)"));
      UriInfo uriInfo = parser.parseURISegment(-1, -1);
      assertNull(uriInfo);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testParseURISegmentInvalidIndex02() {
    try {
      parser = new ODataEntityParser(mock("JPATypeMock(2)"));
      UriInfo uriInfo = parser.parseURISegment(3, -1);
      assertNull(uriInfo);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testParseURISegmentInvalidEntityType() {
    try {
      parser = new ODataEntityParser(mock("JPATypeMockInvalid(2)"));
      parser.parseURISegment(0, 1);
      fail("Exception Expected");
    } catch (ODataJPARuntimeException e) {
      assertEquals(true, true);
    }
  }

  @Test
  public void testParseBindingLink() {
    try {
      parser = new ODataEntityParser(mock("JPATypeMock(2)"));
      UriInfo uriInfo = parser.parseBindingLink("JPATypeMock(2)", new HashMap<String, String>());
      assertNotNull(uriInfo);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

  }

  @Test
  public void testParseBindingLinkNegative() {
    try {
      parser = new ODataEntityParser(mock("JPATypeMock(2)"));
      parser.parseBindingLink("JPATypeMockInvalid(2)", new HashMap<String, String>());
      fail("Exception Expected");
    } catch (ODataJPARuntimeException e) {
      assertEquals(true, true);
    }
  }

  @Test
  public void testParseLink() {
    parser = new ODataEntityParser(mock("JPATypeMock(2)"));
    try {
      UriInfo uriInfo =
          parser.parseLink(EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false), mockURIContent(0),
              "application/json");
      assertNotNull(uriInfo);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testParseLinkWithoutServiceRoot() {
    parser = new ODataEntityParser(mock("JPATypeMock(2)"));
    try {
      UriInfo uriInfo =
          parser.parseLink(EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false), mockURIContent(1),
              "application/json");
      assertNotNull(uriInfo);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testParseLinkNegative() {
    parser = new ODataEntityParser(mock("JPATypeMock(2)"));
    try {
      parser.parseLink(EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false), mockURIContent(2),
          "application/json");
      fail("Exception Expected");
    } catch (ODataJPARuntimeException e) {
      assertEquals(true, true);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  private InputStream mockURIContent(final int variant) {
    String uri = null;
    InputStream is = null;
    switch (variant) {
    case 0:
      uri = "{ \"uri\": \"" + ODataServiceMock.SERVICE_ROOT + "JPATypeMock(2)\" }";
      break;
    case 1:
      uri = "{ \"uri\": \"JPATypeMock(2)\" }";
      break;
    case 2:
      uri = "{ \"uri\": \"" + ODataServiceMock.SERVICE_ROOT + "JPATypeMockInvalid(2)\" }";
    }

    try {
      is = new ByteArrayInputStream(uri.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    return is;

  }
}
