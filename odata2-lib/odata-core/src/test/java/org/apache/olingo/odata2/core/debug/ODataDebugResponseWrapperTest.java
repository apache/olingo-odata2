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
package org.apache.olingo.odata2.core.debug;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataContext.RuntimeMeasurement;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.NavigationPropertySegment;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.SelectItem;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.api.uri.expression.CommonExpression;
import org.apache.olingo.odata2.api.uri.expression.ExpressionParserException;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.Test;

/**
 * Tests for the debug information output.
 */
public class ODataDebugResponseWrapperTest extends BaseTest {

  private static final String EXPECTED = "{"
      + "\"request\":{\"method\":\"GET\",\"uri\":\"http://test/entity\",\"protocol\":null},"
      + "\"response\":{\"status\":{\"code\":200,\"info\":\"OK\"}},"
      + "\"server\":{\"version\":null}}";

  private ODataContext mockContext(final ODataHttpMethod method) throws ODataException {
    ODataContext context = mock(ODataContext.class);
    when(context.getHttpMethod()).thenReturn(method.name());
    PathInfo pathInfo = mock(PathInfo.class);
    when(pathInfo.getRequestUri()).thenReturn(URI.create("http://test/entity"));
    when(pathInfo.getServiceRoot()).thenReturn(URI.create("http://test/"));
    when(context.getPathInfo()).thenReturn(pathInfo);
    when(context.getRuntimeMeasurements()).thenReturn(null);
    return context;
  }

  private ODataResponse mockResponse(final HttpStatusCodes status, final String body, final String contentType) {
    ODataResponse response = mock(ODataResponse.class);
    when(response.getStatus()).thenReturn(status);
    when(response.getEntity()).thenReturn(body);
    if (contentType != null) {
      when(response.getHeaderNames()).thenReturn(new HashSet<String>(Arrays.asList(HttpHeaders.CONTENT_TYPE)));
      when(response.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(contentType);
      when(response.getContentHeader()).thenReturn(contentType);
    }
    return response;
  }

  private RuntimeMeasurement mockRuntimeMeasurement(final String method, final long startTime, final long stopTime) {
    RuntimeMeasurement measurement = mock(RuntimeMeasurement.class);
    when(measurement.getClassName()).thenReturn("class");
    when(measurement.getMethodName()).thenReturn(method);
    when(measurement.getTimeStarted()).thenReturn(startTime);
    when(measurement.getTimeStopped()).thenReturn(stopTime);
    return measurement;
  }

  @Test
  public void minimal() throws Exception {
    final ODataContext context = mockContext(ODataHttpMethod.PUT);
    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.NO_CONTENT, null, null);

    ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_JSON).wrapResponse();
    final String actualJson = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals(EXPECTED.replace(ODataHttpMethod.GET.name(), ODataHttpMethod.PUT.name())
        .replace(Integer.toString(HttpStatusCodes.OK.getStatusCode()),
            Integer.toString(HttpStatusCodes.NO_CONTENT.getStatusCode()))
        .replace(HttpStatusCodes.OK.getInfo(), HttpStatusCodes.NO_CONTENT.getInfo()),
        actualJson);

    response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_HTML).wrapResponse();
    final String html = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertTrue(html.contains(HttpStatusCodes.NO_CONTENT.getInfo()));
  }

  @Test
  public void body() throws Exception {
    final ODataContext context = mockContext(ODataHttpMethod.GET);
    ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.OK, "\"test\"", HttpContentType.APPLICATION_JSON);

    ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_JSON).wrapResponse();
    String entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals(EXPECTED.replace("}},\"server",
        "},\"headers\":{\"" + HttpHeaders.CONTENT_TYPE + "\":\"" + HttpContentType.APPLICATION_JSON + "\"},"
            + "\"body\":\"test\"},\"server"),
        entity);

    wrappedResponse = mockResponse(HttpStatusCodes.OK, "test", HttpContentType.TEXT_PLAIN);
    response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_JSON).wrapResponse();
    entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals(EXPECTED.replace("}},\"server",
        "},\"headers\":{\"" + HttpHeaders.CONTENT_TYPE + "\":\"" + HttpContentType.TEXT_PLAIN + "\"},"
            + "\"body\":\"test\"},\"server"),
        entity);

    wrappedResponse = mockResponse(HttpStatusCodes.OK, null, "image/png");
    when(wrappedResponse.getEntity()).thenReturn(new ByteArrayInputStream("test".getBytes()));
    response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_JSON).wrapResponse();
    entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals(EXPECTED.replace("}},\"server",
        "},\"headers\":{\"" + HttpHeaders.CONTENT_TYPE + "\":\"image/png\"},"
            + "\"body\":\"dGVzdA==\"},\"server"),
        entity);

    when(wrappedResponse.getEntity()).thenReturn(new ByteArrayInputStream("test".getBytes()));
    response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_HTML).wrapResponse();
    entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertTrue(entity.contains("<img src=\"data:image/png;base64,dGVzdA==\" />"));
  }

  @Test
  public void headers() throws Exception {
    ODataContext context = mockContext(ODataHttpMethod.GET);
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(HttpContentType.APPLICATION_JSON));
    when(context.getRequestHeaders()).thenReturn(headers);

    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.OK, null, HttpContentType.APPLICATION_JSON);

    ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_JSON).wrapResponse();
    String entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals(EXPECTED.replace("},\"response",
        ",\"headers\":{\"" + HttpHeaders.CONTENT_TYPE + "\":\"" + HttpContentType.APPLICATION_JSON + "\"}},\"response")
        .replace("}},\"server",
            "},\"headers\":{\"" + HttpHeaders.CONTENT_TYPE + "\":\"" + HttpContentType.APPLICATION_JSON
                + "\"}},\"server"),
        entity);

    response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_HTML).wrapResponse();
    entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertTrue(entity.contains("<td class=\"name\">Content-Type</td><td class=\"value\">application/json</td>"));
  }

  @Test
  public void server() throws Exception {
    ODataContext context = mockContext(ODataHttpMethod.GET);
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    when(servletRequest.getServerPort()).thenReturn(12345);
    when(context.getParameter(ODataContext.HTTP_SERVLET_REQUEST_OBJECT)).thenReturn(servletRequest);

    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.OK, null, null);
    ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_JSON).wrapResponse();
    String entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals(EXPECTED.replace("null}}", "null,\"environment\":{\"serverPort\":\"12345\"}}}"),
        entity);

    response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_HTML).wrapResponse();
    entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertTrue(entity.contains("<td class=\"name\">serverPort</td><td class=\"value\">12345</td>"));
  }

  @Test
  public void uri() throws Exception {
    final ODataContext context = mockContext(ODataHttpMethod.GET);
    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.OK, null, null);

    UriInfo uriInfo = mock(UriInfo.class);
    final FilterExpression filter = UriParser.parseFilter(null, null, "true");
    when(uriInfo.getFilter()).thenReturn(filter);
    final OrderByExpression orderBy = UriParser.parseOrderBy(null, null, "true");
    when(uriInfo.getOrderBy()).thenReturn(orderBy);
    List<ArrayList<NavigationPropertySegment>> expand = new ArrayList<ArrayList<NavigationPropertySegment>>();
    NavigationPropertySegment segment = mock(NavigationPropertySegment.class);
    EdmNavigationProperty navigationProperty = mock(EdmNavigationProperty.class);
    when(navigationProperty.getName()).thenReturn("nav");
    when(segment.getNavigationProperty()).thenReturn(navigationProperty);
    ArrayList<NavigationPropertySegment> segments = new ArrayList<NavigationPropertySegment>();
    segments.add(segment);
    expand.add(segments);
    when(uriInfo.getExpand()).thenReturn(expand);
    SelectItem select1 = mock(SelectItem.class);
    SelectItem select2 = mock(SelectItem.class);
    EdmProperty property = mock(EdmProperty.class);
    when(property.getName()).thenReturn("property");
    when(select1.getProperty()).thenReturn(property);
    when(select2.getProperty()).thenReturn(property);
    when(select2.getNavigationPropertySegments()).thenReturn(segments);
    when(uriInfo.getSelect()).thenReturn(Arrays.asList(select1, select2));

    ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, uriInfo, null,
        ODataDebugResponseWrapper.ODATA_DEBUG_JSON).wrapResponse();
    String entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals(EXPECTED.replace("null}}", "null,"
        + "\"uri\":{\"filter\":{\"nodeType\":\"LITERAL\",\"type\":\"Edm.Boolean\",\"value\":\"true\"},"
        + "\"orderby\":{\"nodeType\":\"order collection\","
        + "\"orders\":[{\"nodeType\":\"ORDER\",\"sortorder\":\"asc\","
        + "\"expression\":{\"nodeType\":\"LITERAL\",\"type\":\"Edm.Boolean\",\"value\":\"true\"}}]},"
        + "\"expandSelect\":{\"all\":false,\"properties\":[\"property\"],"
        + "\"links\":[{\"nav\":{\"all\":false,\"properties\":[\"property\"],\"links\":[]}}]}}}}"),
        entity);

    response = new ODataDebugResponseWrapper(context, wrappedResponse, uriInfo, null,
        ODataDebugResponseWrapper.ODATA_DEBUG_HTML).wrapResponse();
    entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertTrue(entity.contains("Edm.Boolean"));
    assertTrue(entity.contains("asc"));
  }

  @Test
  public void uriWithException() throws Exception {
    final ODataContext context = mockContext(ODataHttpMethod.GET);
    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.OK, null, null);

    ExpressionParserException exception = mock(ExpressionParserException.class);
    when(exception.getMessageReference()).thenReturn(ExpressionParserException.COMMON_ERROR);
    when(exception.getStackTrace()).thenReturn(new StackTraceElement[] {
        new StackTraceElement("class", "method", "file", 42) });
    CommonExpression filterTree = mock(CommonExpression.class);
    when(filterTree.getUriLiteral()).thenReturn("wrong");
    when(exception.getFilterTree()).thenReturn(filterTree);

    ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), exception,
        ODataDebugResponseWrapper.ODATA_DEBUG_JSON).wrapResponse();
    String entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals(EXPECTED.replace("null}}", "null,"
        + "\"uri\":{\"error\":{\"expression\":\"wrong\"}},"
        + "\"stacktrace\":{\"exceptions\":[{\"class\":\"" + exception.getClass().getName() + "\","
        + "\"message\":\"Error while parsing a ODATA expression.\","
        + "\"invocation\":{\"class\":\"class\",\"method\":\"method\",\"line\":42}}],"
        + "\"stacktrace\":[{\"class\":\"class\",\"method\":\"method\",\"line\":42}]}}}"),
        entity);

    response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), exception,
        ODataDebugResponseWrapper.ODATA_DEBUG_HTML).wrapResponse();
    entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertTrue(entity.contains("wrong"));
    assertTrue(entity.contains(exception.getClass().getName()));
    assertTrue(entity.contains("42"));
  }

  @Test
  public void runtime() throws Exception {
    ODataContext context = mockContext(ODataHttpMethod.GET);
    List<RuntimeMeasurement> runtimeMeasurements = new ArrayList<RuntimeMeasurement>();
    runtimeMeasurements.add(mockRuntimeMeasurement("method", 1000, 42000));
    runtimeMeasurements.add(mockRuntimeMeasurement("inner", 2000, 5000));
    runtimeMeasurements.add(mockRuntimeMeasurement("inner", 7000, 12000));
    runtimeMeasurements.add(mockRuntimeMeasurement("inner", 13000, 16000));
    runtimeMeasurements.add(mockRuntimeMeasurement("inner2", 14000, 15000));
    runtimeMeasurements.add(mockRuntimeMeasurement("child", 17000, 21000));
    runtimeMeasurements.add(mockRuntimeMeasurement("second", 45000, 99000));
    when(context.getRuntimeMeasurements()).thenReturn(runtimeMeasurements);

    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.OK, null, null);

    ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_JSON).wrapResponse();
    String entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals(EXPECTED.replace("null}}", "null,"
        + "\"runtime\":[{\"class\":\"class\",\"method\":\"method\",\"duration\":{\"value\":41,\"unit\":\"µs\"},"
        + "\"children\":[{\"class\":\"class\",\"method\":\"inner\",\"duration\":{\"value\":8,\"unit\":\"µs\"}},"
        + "{\"class\":\"class\",\"method\":\"inner\",\"duration\":{\"value\":3,\"unit\":\"µs\"},\"children\":["
        + "{\"class\":\"class\",\"method\":\"inner2\",\"duration\":{\"value\":1,\"unit\":\"µs\"}}]},"
        + "{\"class\":\"class\",\"method\":\"child\",\"duration\":{\"value\":4,\"unit\":\"µs\"}}]},"
        + "{\"class\":\"class\",\"method\":\"second\",\"duration\":{\"value\":54,\"unit\":\"µs\"}}]}}"),
        entity);

    response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null,
        ODataDebugResponseWrapper.ODATA_DEBUG_HTML).wrapResponse();
    entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertTrue(entity.contains("54&nbsp;&micro;s"));
  }

  @Test
  public void exception() throws Exception {
    final ODataContext context = mockContext(ODataHttpMethod.GET);
    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.BAD_REQUEST, null, null);

    ODataMessageException exception = mock(ODataMessageException.class);
    when(exception.getMessageReference()).thenReturn(ODataMessageException.COMMON);
    RuntimeException innerException = mock(RuntimeException.class);
    when(innerException.getLocalizedMessage()).thenReturn("error");
    final StackTraceElement[] stackTrace = new StackTraceElement[] {
        new StackTraceElement("innerClass", "innerMethod", "innerFile", 99),
        new StackTraceElement("class", "method", "file", 42),
        new StackTraceElement("outerClass", "outerMethod", "outerFile", 11) };
    when(innerException.getStackTrace()).thenReturn(stackTrace);
    when(exception.getCause()).thenReturn(innerException);
    when(exception.getStackTrace()).thenReturn(Arrays.copyOfRange(stackTrace, 1, 3));

    ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), exception,
        ODataDebugResponseWrapper.ODATA_DEBUG_JSON).wrapResponse();
    String entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertEquals(EXPECTED
        .replace(Integer.toString(HttpStatusCodes.OK.getStatusCode()),
            Integer.toString(HttpStatusCodes.BAD_REQUEST.getStatusCode()))
        .replace(HttpStatusCodes.OK.getInfo(), HttpStatusCodes.BAD_REQUEST.getInfo())
        .replace("null}}", "null,"
            + "\"stacktrace\":{\"exceptions\":[{\"class\":\"" + exception.getClass().getName() + "\","
            + "\"message\":\"Common exception\","
            + "\"invocation\":{\"class\":\"class\",\"method\":\"method\",\"line\":42}},"
            + "{\"class\":\"" + innerException.getClass().getName() + "\",\"message\":\"error\","
            + "\"invocation\":{\"class\":\"innerClass\",\"method\":\"innerMethod\",\"line\":99}}],"
            + "\"stacktrace\":[{\"class\":\"class\",\"method\":\"method\",\"line\":42},"
            + "{\"class\":\"outerClass\",\"method\":\"outerMethod\",\"line\":11}]}}}"),
        entity);

    response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), exception,
        ODataDebugResponseWrapper.ODATA_DEBUG_HTML).wrapResponse();
    entity = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertTrue(entity.contains("Common exception"));
    assertTrue(entity.contains("42"));
    assertTrue(entity.contains("innerMethod"));
    assertTrue(entity.contains("outerMethod"));
  }
}
