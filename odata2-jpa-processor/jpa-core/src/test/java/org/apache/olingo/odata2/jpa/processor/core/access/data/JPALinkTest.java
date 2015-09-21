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
package org.apache.olingo.odata2.jpa.processor.core.access.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationSegment;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;
import org.apache.olingo.odata2.core.edm.provider.EdmEntityTypeImplProv;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.JPAProcessorMockAbstract;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataContextMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataJPAContextMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.PathInfoMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.PathSegmentMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.Note;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.SalesOrderHeader;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JPALinkTest {

  private static Edm edm;
  private static SalesOrderHeader header;
  private static Note note;
  @SuppressWarnings("unused")
  private static short testCase = 0;
  private InputStream requestPayload = null;

  @BeforeClass
  public static void loadEdm() throws EntityProviderException {
    edm = EntityProvider.readMetadata(JPALinkTest.class.getClassLoader().getResourceAsStream(
        "metadata.xml"), false);
  }

  @Before
  public void setUp() {
    header = null;
    note = null;
    requestPayload = JPALinkTest.class.getClassLoader().getResourceAsStream(
        "SalesOrderNotesLink.json");
  }

  @Test
  public void testCreateLink() {
    testCase = 0;
    try {
      ODataJPAContext context = mockODataJPAContext(false);
      JPALink link = new JPALink(context);
      Field processor = JPALink.class.
          getDeclaredField("jpaProcessor");
      processor.setAccessible(true);
      processor.set(link, new JPAProcessorMock());
      link.create(mockPostURIInfo(false), requestPayload, "application/json", "application/json");
      assertEquals(2, header.getNotesDetails().size());

    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchMethodException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testDeleteLink() {
    testCase = 1;
    try {
      ODataJPAContext context = mockODataJPAContext(false);
      JPALink link = new JPALink(context);
      Field processor = JPALink.class.
          getDeclaredField("jpaProcessor");
      processor.setAccessible(true);
      processor.set(link, new JPAProcessorMock());
      link.delete(mockDeleteURIInfo(false));
      assertNull(header.getNotesDetails());

    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchMethodException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testDeleteLinkReverse() {
    testCase = 1;
    try {
      ODataJPAContext context = mockODataJPAContext(true);
      JPALink link = new JPALink(context);
      Field processor = JPALink.class.
          getDeclaredField("jpaProcessor");
      processor.setAccessible(true);
      processor.set(link, new JPAProcessorMock());
      link.delete(mockDeleteURIInfo(true));
      assertNull(note.getSalesOrderHeader());

    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchMethodException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  private ODataJPAContext mockODataJPAContext(boolean isReverse) {

    String path1 = null;
    String path2 = null;
    String path3 = null;

    if (isReverse) {
      path1 = "Notes('2')";
      path2 = "$links";
      path3 = "salesOrderHeader(1L)";
    } else {
      path1 = "SalesOrders(1L)";
      path2 = "$links";
      path3 = "NotesDetails('2')";
    }

    try {
      List<PathSegment> pathSegments = new ArrayList<PathSegment>();

      PathSegmentMock pathSegmentMock = new PathSegmentMock();
      pathSegmentMock.setPath(path1);
      pathSegments.add(pathSegmentMock);

      pathSegmentMock = new PathSegmentMock();
      pathSegmentMock.setPath(path2);
      pathSegments.add(pathSegmentMock);

      pathSegmentMock = new PathSegmentMock();
      pathSegmentMock.setPath(path3);
      pathSegments.add(pathSegmentMock);

      PathInfoMock pathInfoMock = new PathInfoMock();

      pathInfoMock.setServiceRootURI("http://localhost/so.svc");
      pathInfoMock.setPathSegments(pathSegments);

      ODataService service = EasyMock.createMock(ODataService.class);
      EasyMock.expect(service.getEntityDataModel()).andReturn(edm).anyTimes();
      EasyMock.replay(service);

      ODataContextMock odataContextMock = new ODataContextMock();
      odataContextMock.setPathInfo(pathInfoMock.mock());
      odataContextMock.setODataService(service);
      ODataJPAContext context = ODataJPAContextMock.mockODataJPAContext(odataContextMock.mock());

      return context;
    } catch (URISyntaxException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    return null;
  }

  private List<NavigationSegment> mockNavigationSegments(boolean isReverse) throws EdmException, NoSuchFieldException,
      SecurityException, IllegalArgumentException, IllegalAccessException {
    Class<?> type = null;
    String entityTypeName = null;
    String keyLiteral = null;
    EdmMultiplicity multiplicity = null;
    String navPropertyName = null;
    String toRole = null;
    String associationName = null;

    if (isReverse) {
      type = SalesOrderHeader.class;
      entityTypeName = "SalesOrder";
      keyLiteral = "1";
      multiplicity = EdmMultiplicity.ONE;
      navPropertyName = "salesOrderHeader";
      toRole = "SalesOrder";
      associationName = "Note_SalesOrder";

    } else {
      type = List.class;
      entityTypeName = "Note";
      keyLiteral = "2";
      multiplicity = EdmMultiplicity.MANY;
      navPropertyName = "NotesDetails";
      toRole = "Note";
      associationName = "Note_SalesOrder";
    }

    JPAEdmMapping mapping = new JPAEdmMappingImpl();
    mapping.setJPAType(type);

    KeyPredicate keyPredicate = EasyMock.createMock(KeyPredicate.class);
    EasyMock.expect(keyPredicate.getProperty()).andReturn(
        edm.getEntityType("SalesOrderProcessing", entityTypeName).getKeyProperties().get(0)).anyTimes();
    EasyMock.expect(keyPredicate.getLiteral()).andReturn(keyLiteral);
    EasyMock.replay(keyPredicate);
    List<KeyPredicate> keyPredicates = new ArrayList<KeyPredicate>();
    keyPredicates.add(keyPredicate);

    NavigationSegment navigationSegment = EasyMock.createMock(NavigationSegment.class);
    EasyMock.expect(navigationSegment.getKeyPredicates()).andReturn(keyPredicates);

    EdmNavigationProperty navProperty = EasyMock.createMock(EdmNavigationProperty.class);
    EasyMock.expect(navProperty.getMapping()).andReturn((EdmMapping) mapping).anyTimes();
    EasyMock.expect(navProperty.getMultiplicity()).andReturn(multiplicity).anyTimes();
    EasyMock.expect(navProperty.getName()).andReturn(navPropertyName).anyTimes();
    EasyMock.expect(navProperty.getToRole()).andReturn(toRole).anyTimes();
    EasyMock.expect(navProperty.getRelationship()).andReturn(
        edm.getAssociation("SalesOrderProcessing", associationName));
    EasyMock.replay(navProperty);

    EdmEntityType edmEntityType =
        edm.getAssociation("SalesOrderProcessing", associationName).getEnd(toRole).getEntityType();
    Field field = EdmEntityTypeImplProv.class.getDeclaredField("entityType");
    field.setAccessible(true);

    EntityType entityType = (EntityType) field.get(edmEntityType);
    entityType.setMapping((Mapping) mapping);

    EasyMock.expect(navigationSegment.getNavigationProperty()).andReturn(navProperty).anyTimes();
    EasyMock.replay(navigationSegment);

    List<NavigationSegment> navigationSegments = new ArrayList<NavigationSegment>();
    navigationSegments.add(navigationSegment);

    return navigationSegments;
  }

  private PostUriInfo mockPostURIInfo(boolean isReverse) throws ODataException, NoSuchMethodException,
      SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

    PostUriInfo uriInfo = EasyMock.createMock(UriInfo.class);
    for (EdmEntitySet edmEntitySet : edm.getEntitySets()) {
      if (edmEntitySet.getName().equals("Notes")) {
        EasyMock.expect(uriInfo.getTargetEntitySet()).andReturn(edmEntitySet).anyTimes();
        break;
      }
    }
    EasyMock.expect(((UriInfo) uriInfo).isLinks()).andReturn(true);
    EasyMock.expect(uriInfo.getNavigationSegments()).andReturn(mockNavigationSegments(isReverse)).anyTimes();
    EasyMock.replay(uriInfo);

    return uriInfo;
  }

  private DeleteUriInfo mockDeleteURIInfo(boolean isReverse) throws ODataException, NoSuchMethodException,
      SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

    DeleteUriInfo uriInfo = EasyMock.createMock(DeleteUriInfo.class);
    EasyMock.expect(uriInfo.getNavigationSegments()).andReturn(mockNavigationSegments(isReverse)).anyTimes();
    EasyMock.replay(uriInfo);

    return uriInfo;
  }

  private static class JPAProcessorMock extends JPAProcessorMockAbstract {

    @Override
    public <T> Object process(GetEntityUriInfo uriParserResultView) throws ODataJPAModelException,
        ODataJPARuntimeException {
      try {

        if (uriParserResultView.getTargetEntitySet().getName().equals("Notes")) {
          assertEquals("id", uriParserResultView.getKeyPredicates().get(0).getProperty().getName());
          header = new SalesOrderHeader();
          note = new Note();
          note.setSalesOrderHeader(header);
          return note;
        } else if (uriParserResultView.getTargetEntitySet().getName().equals("SalesOrders")) {
          assertEquals("ID", uriParserResultView.getKeyPredicates().get(0).getProperty().getName());
          header = new SalesOrderHeader();
          Note note = new Note();
          List<Note> notes = new ArrayList<Note>();
          notes.add(note);
          header.setNotesDetails(notes);
          return header;
        }

      } catch (EdmException e) {
        fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
      }
      return null;

    }

    @Override
    public List<Object> process(GetEntitySetUriInfo uriParserResultView) throws ODataJPAModelException,
        ODataJPARuntimeException {

      return null;

    }

  }

}
