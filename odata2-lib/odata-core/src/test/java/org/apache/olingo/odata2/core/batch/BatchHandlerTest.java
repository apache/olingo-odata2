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
package org.apache.olingo.odata2.core.batch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.batch.BatchHandler;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.part.BatchProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityMediaProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySetProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySimplePropertyProcessor;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetSimplePropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Before;
import org.junit.Test;

public class BatchHandlerTest {

  private BatchHandler handler;
  private static final String CONTENT_TYPE = HttpContentType.MULTIPART_MIXED + "; boundary=batch_123";
  private static final String CRLF = "\r\n";
  private static String SERVICE_BASE = "http://localhost/odata/";
  private static String SERVICE_ROOT = null;

  @Before
  public void setupBatchHandler() throws Exception {
    ODataProcessor processor = new LocalProcessor();
    ODataService serviceMock = mock(ODataService.class);
    when(serviceMock.getBatchProcessor()).thenReturn((BatchProcessor) processor);
    when(serviceMock.getEntitySetProcessor()).thenReturn((EntitySetProcessor) processor);
    when(serviceMock.getEntitySimplePropertyProcessor()).thenReturn((EntitySimplePropertyProcessor) processor);
    when(serviceMock.getProcessor()).thenReturn(processor);
    Edm mockEdm = MockFacade.getMockEdm();
    when(serviceMock.getEntityDataModel()).thenReturn(mockEdm);
    List<String> supportedContentTypes = Arrays.asList(
        HttpContentType.APPLICATION_JSON_UTF8, HttpContentType.APPLICATION_JSON);
    when(serviceMock.getSupportedContentTypes(EntityMediaProcessor.class)).thenReturn(supportedContentTypes);
    when(serviceMock.getSupportedContentTypes(EntityProcessor.class)).thenReturn(supportedContentTypes);
    when(serviceMock.getSupportedContentTypes(EntitySimplePropertyProcessor.class)).thenReturn(supportedContentTypes);
    handler = new BatchHandlerImpl(mock(ODataServiceFactory.class), serviceMock);
  }

  @Test
  public void contentIdReferencing() throws Exception {
    SERVICE_ROOT = SERVICE_BASE;
    PathInfoImpl pathInfo = new PathInfoImpl();
    pathInfo.setServiceRoot(new URI(SERVICE_ROOT));
    pathInfo.setODataPathSegment(Collections.<PathSegment> singletonList(
        new ODataPathSegmentImpl("$batch", null)));
    EntityProviderBatchProperties properties = EntityProviderBatchProperties.init().pathInfo(pathInfo).build();
    InputStream content = readFile("/batchContentIdReferencing.batch");
    List<BatchRequestPart> parsedRequest = EntityProvider.parseBatchRequest(CONTENT_TYPE, content, properties);

    PathInfo firstPathInfo = parsedRequest.get(0).getRequests().get(0).getPathInfo();
    assertFirst(firstPathInfo);

    handler.handleBatchPart(parsedRequest.get(0));
  }

  @Test
  public void contentIdReferencingWithAdditionalSegments() throws Exception {
    SERVICE_ROOT = SERVICE_BASE + "seg1/seg2/";
    PathInfoImpl pathInfo = new PathInfoImpl();
    pathInfo.setPrecedingPathSegment(Arrays.asList(
        (PathSegment) new ODataPathSegmentImpl("seg1", null),
        (PathSegment) new ODataPathSegmentImpl("seg2", null)));
    pathInfo.setServiceRoot(new URI(SERVICE_ROOT));
    pathInfo.setODataPathSegment(Collections.<PathSegment> singletonList(
        new ODataPathSegmentImpl("$batch", null)));
    EntityProviderBatchProperties properties = EntityProviderBatchProperties.init().pathInfo(pathInfo).build();
    InputStream content = readFile("/batchContentIdReferencing.batch");
    List<BatchRequestPart> parsedRequest = EntityProvider.parseBatchRequest(CONTENT_TYPE, content, properties);

    PathInfo firstPathInfo = parsedRequest.get(0).getRequests().get(0).getPathInfo();
    assertFirst(firstPathInfo);

    handler.handleBatchPart(parsedRequest.get(0));
  }

  @Test
  public void contentIdReferencingWithAdditionalSegmentsAndMatrixParameter() throws Exception {
    SERVICE_ROOT = SERVICE_BASE + "seg1;v=1/seg2;v=2/";
    PathInfoImpl pathInfo = new PathInfoImpl();
    pathInfo.setPrecedingPathSegment(Arrays.asList(
        (PathSegment) new ODataPathSegmentImpl("seg1",
            Collections.singletonMap("v", Collections.singletonList("1"))),
        (PathSegment) new ODataPathSegmentImpl("seg2",
            Collections.singletonMap("v", Collections.singletonList("2")))));
    pathInfo.setServiceRoot(new URI(SERVICE_ROOT));
    pathInfo.setODataPathSegment(Collections.<PathSegment> singletonList(
        new ODataPathSegmentImpl("$batch", null)));
    EntityProviderBatchProperties properties = EntityProviderBatchProperties.init().pathInfo(pathInfo).build();
    InputStream content = readFile("/batchContentIdReferencing.batch");
    List<BatchRequestPart> parsedRequest = EntityProvider.parseBatchRequest(CONTENT_TYPE, content, properties);

    PathInfo firstPathInfo = parsedRequest.get(0).getRequests().get(0).getPathInfo();
    assertFirst(firstPathInfo);

    handler.handleBatchPart(parsedRequest.get(0));
  }

  private void assertFirst(PathInfo pathInfo) {
    assertEquals(SERVICE_ROOT + "Employees", pathInfo.getRequestUri().toString());
    assertEquals(SERVICE_ROOT, pathInfo.getServiceRoot().toString());
  }

  private InputStream readFile(String fileName) throws IOException {
    InputStream in = ClassLoader.class.getResourceAsStream(fileName);
    if (in == null) {
      throw new IOException("Requested file '" + fileName + "' was not found.");
    }

    return StringHelper.toStream(in).asStreamWithLineSeparation(CRLF);
  }

  public class LocalProcessor implements BatchProcessor, EntitySetProcessor, EntitySimplePropertyProcessor {

    private ODataContext context;

    @Override
    public void setContext(ODataContext context) throws ODataException {
      this.context = context;
    }

    @Override
    public ODataContext getContext() throws ODataException {
      return context;
    }

    @Override
    public BatchResponsePart executeChangeSet(BatchHandler handler, List<ODataRequest> requests) throws ODataException {
      List<ODataResponse> responses = new ArrayList<ODataResponse>();

      // handle create
      ODataResponse response = handler.handleRequest(requests.get(0));
      assertEquals(HttpStatusCodes.OK, response.getStatus());
      assertEquals(SERVICE_ROOT + "Employees('1')", response.getIdLiteral());
      responses.add(response);

      // handle update
      response = handler.handleRequest(requests.get(1));
      assertEquals(HttpStatusCodes.OK, response.getStatus());
      responses.add(response);

      return BatchResponsePart.responses(responses).changeSet(true).build();
    }

    @Override
    public ODataResponse createEntity(PostUriInfo uriInfo, InputStream content, String requestContentType,
        String contentType) throws ODataException {
      PathInfo pathInfo = getContext().getPathInfo();
      assertFirst(pathInfo);
      assertEquals("Employees", uriInfo.getTargetEntitySet().getName());
      return ODataResponse.newBuilder().status(HttpStatusCodes.OK).idLiteral(SERVICE_ROOT + "Employees('1')").build();
    }

    @Override
    public ODataResponse updateEntitySimpleProperty(PutMergePatchUriInfo uriInfo, InputStream content,
        String requestContentType, String contentType) throws ODataException {
      PathInfo pathInfo = getContext().getPathInfo();
      assertEquals(SERVICE_ROOT + "Employees('1')/EmployeeName", pathInfo.getRequestUri().toString());
      assertEquals(SERVICE_ROOT, pathInfo.getServiceRoot().toString());
      assertEquals("Employees", uriInfo.getTargetEntitySet().getName());
      return ODataResponse.newBuilder().status(HttpStatusCodes.OK).build();
    }

    @Override
    public ODataResponse readEntitySimpleProperty(GetSimplePropertyUriInfo uriInfo, String contentType)
        throws ODataException {
      // this method is not needed.
      return null;
    }

    @Override
    public ODataResponse readEntitySet(GetEntitySetUriInfo uriInfo, String contentType) throws ODataException {
      // this method is not needed.
      return null;
    }

    @Override
    public ODataResponse countEntitySet(GetEntitySetCountUriInfo uriInfo, String contentType) throws ODataException {
      // this method is not needed.
      return null;
    }

    @Override
    public ODataResponse executeBatch(BatchHandler handler, String contentType, InputStream content)
        throws ODataException {
      // this method is not needed.
      return null;
    }
  }
}
