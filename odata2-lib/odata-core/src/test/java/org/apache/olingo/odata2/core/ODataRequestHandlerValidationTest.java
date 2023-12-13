/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.ODataHttpHeaders;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmConcurrencyMode;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.part.BatchProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityComplexPropertyProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityLinkProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityLinksProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityMediaProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySetProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySimplePropertyProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySimplePropertyValueProcessor;
import org.apache.olingo.odata2.api.processor.part.FunctionImportProcessor;
import org.apache.olingo.odata2.api.processor.part.FunctionImportValueProcessor;
import org.apache.olingo.odata2.api.processor.part.MetadataProcessor;
import org.apache.olingo.odata2.api.processor.part.ServiceDocumentProcessor;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.ContentType.ODataFormat;
import org.apache.olingo.odata2.core.uri.UriInfoImpl;
import org.apache.olingo.odata2.core.uri.UriType;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the validation of HTTP method, URI path, query options, content types, and
 * conditional-handling HTTP headers.
 */
public class ODataRequestHandlerValidationTest extends BaseTest {

    private Edm edm = null;

    @Before
    public void setEdm() throws ODataException {
        edm = MockFacade.getMockEdm();
    }

    private List<String> createPathSegments(final UriType uriType, final boolean moreNavigation, final boolean isValue) {
        List<String> segments = new ArrayList<String>();

        if (uriType == UriType.URI1 || uriType == UriType.URI15) {
            if (moreNavigation) {
                segments.add("Managers('1')");
                segments.add("nm_Employees");
            } else {
                segments.add("Employees");
            }
        } else if (uriType == UriType.URI2 || uriType == UriType.URI3 || uriType == UriType.URI4 || uriType == UriType.URI5
                || uriType == UriType.URI16 || uriType == UriType.URI17) {
            if (moreNavigation) {
                segments.add("Managers('1')");
                segments.add("nm_Employees('1')");
            } else {
                segments.add("Employees('1')");
            }
        } else if (uriType == UriType.URI6A || uriType == UriType.URI7A || uriType == UriType.URI50A) {
            segments.add("Managers('1')");
            if (moreNavigation) {
                segments.add("nm_Employees('1')");
                segments.add("ne_Manager");
            }
            if (uriType == UriType.URI7A || uriType == UriType.URI50A) {
                segments.add("$links");
            }
            segments.add("nm_Employees('1')");
        } else if (uriType == UriType.URI6B || uriType == UriType.URI7B || uriType == UriType.URI50B) {
            segments.add("Managers('1')");
            if (moreNavigation) {
                segments.add("nm_Employees('1')");
                segments.add("ne_Manager");
            }
            if (uriType == UriType.URI7B || uriType == UriType.URI50B) {
                segments.add("$links");
            }
            segments.add("nm_Employees");
        } else if (uriType == UriType.URI8) {
            segments.add("$metadata");
        } else if (uriType == UriType.URI9) {
            segments.add("$batch");
        } else if (uriType == UriType.URI10) {
            segments.add("OldestEmployee");
        } else if (uriType == UriType.URI11) {
            segments.add("AllLocations");
        } else if (uriType == UriType.URI12) {
            segments.add("MostCommonLocation");
        } else if (uriType == UriType.URI13) {
            segments.add("AllUsedRoomIds");
        } else if (uriType == UriType.URI14) {
            segments.add("MaximalAge");
        }

        if (uriType == UriType.URI3 || uriType == UriType.URI4) {
            segments.add("Location");
        }
        if (uriType == UriType.URI4) {
            segments.add("Country");
        } else if (uriType == UriType.URI5) {
            segments.add("EmployeeName");
        }

        if (uriType == UriType.URI15 || uriType == UriType.URI16 || uriType == UriType.URI50A || uriType == UriType.URI50B) {
            segments.add("$count");
        }

        if (uriType == UriType.URI17 || isValue) {
            segments.add("$value");
        }

        // self-test
        try {
            final UriInfoImpl uriInfo = (UriInfoImpl) UriParser.parse(edm, MockFacade.getPathSegmentsAsODataPathSegmentMock(segments),
                    Collections.<String, String>emptyMap());
            assertEquals(uriType, uriInfo.getUriType());
            assertEquals(uriType == UriType.URI17 || isValue, uriInfo.isValue());
        } catch (final ODataException e) {
            fail();
        }

        return segments;
    }

    private static Map<String, String> createOptions(final boolean format, final boolean filter, final boolean inlineCount,
            final boolean orderBy, final boolean skipToken, final boolean skip, final boolean top, final boolean expand,
            final boolean select) {

        Map<String, String> map = new HashMap<String, String>();

        if (format) {
            map.put("$format", ODataFormat.XML.toString());
        }
        if (filter) {
            map.put("$filter", "true");
        }
        if (inlineCount) {
            map.put("$inlinecount", "none");
        }
        if (orderBy) {
            map.put("$orderby", "Age");
        }
        if (skipToken) {
            map.put("$skiptoken", "x");
        }
        if (skip) {
            map.put("$skip", "0");
        }
        if (top) {
            map.put("$top", "0");
        }
        if (expand) {
            map.put("$expand", "ne_Team");
        }
        if (select) {
            map.put("$select", "Age");
        }

        return map;
    }

    private ODataRequest mockODataRequest(final ODataHttpMethod method, final List<String> pathSegments,
            final Map<String, String> queryParameters, final String httpHeaderName, final String httpHeaderValue,
            final String requestContentType) throws ODataException {
        ODataRequest request = mock(ODataRequest.class);
        when(request.getMethod()).thenReturn(method);
        PathInfo pathInfo = mock(PathInfo.class);
        List<PathSegment> segments = new ArrayList<PathSegment>();
        for (final String pathSegment : pathSegments) {
            PathSegment segment = mock(PathSegment.class);
            when(segment.getPath()).thenReturn(pathSegment);
            segments.add(segment);
        }
        when(pathInfo.getODataSegments()).thenReturn(segments);
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getQueryParameters()).thenReturn(queryParameters == null ? Collections.<String, String>emptyMap() : queryParameters);
        when(request.getAllQueryParameters()).thenReturn(
                queryParameters == null ? Collections.<String, List<String>>emptyMap() : convertToMultiMap(queryParameters));
        when(request.getContentType()).thenReturn(requestContentType == null ? HttpContentType.APPLICATION_JSON : requestContentType);
        when(request.getRequestHeaderValue(httpHeaderName)).thenReturn(httpHeaderValue);
        if (httpHeaderName == HttpHeaders.ACCEPT) {
            when(request.getAcceptHeaders()).thenReturn(Arrays.asList(httpHeaderValue));
        }
        when(request.getBody()).thenReturn(mock(InputStream.class));
        return request;
    }

    private Map<String, List<String>> convertToMultiMap(final Map<String, String> queryParameters) {
        Map<String, List<String>> multiMap = new HashMap<String, List<String>>();

        for (final String key : queryParameters.keySet()) {
            List<String> parameterList = new LinkedList<String>();
            parameterList.add(queryParameters.get(key));

            multiMap.put(key, parameterList);
        }

        return multiMap;
    }

    private ODataService mockODataService(final ODataServiceFactory serviceFactory) throws ODataException {
        ODataService service = DispatcherTest.getMockService();
        when(service.getEntityDataModel()).thenReturn(edm);
        when(service.getProcessor()).thenReturn(mock(ODataProcessor.class));
        when(serviceFactory.createService(any(ODataContext.class))).thenReturn(service);

        when(service.getSupportedContentTypes(BatchProcessor.class)).thenReturn(Arrays.asList(HttpContentType.MULTIPART_MIXED));

        final List<String> jsonAndXml = Arrays.asList(HttpContentType.APPLICATION_JSON, HttpContentType.APPLICATION_JSON_VERBOSE,
                HttpContentType.APPLICATION_JSON_UTF8, HttpContentType.APPLICATION_JSON_UTF8_VERBOSE, HttpContentType.APPLICATION_XML_UTF8);
        List<String> atomEntryAndJsonAndXml = new ArrayList<String>();
        atomEntryAndJsonAndXml.add(HttpContentType.APPLICATION_ATOM_XML_ENTRY_UTF8);
        atomEntryAndJsonAndXml.add(HttpContentType.APPLICATION_ATOM_XML_UTF8);
        atomEntryAndJsonAndXml.addAll(jsonAndXml);
        when(service.getSupportedContentTypes(EntityProcessor.class)).thenReturn(atomEntryAndJsonAndXml);

        when(service.getSupportedContentTypes(FunctionImportProcessor.class)).thenReturn(jsonAndXml);
        when(service.getSupportedContentTypes(EntityLinkProcessor.class)).thenReturn(jsonAndXml);
        when(service.getSupportedContentTypes(EntityLinksProcessor.class)).thenReturn(jsonAndXml);
        when(service.getSupportedContentTypes(EntitySimplePropertyProcessor.class)).thenReturn(jsonAndXml);
        when(service.getSupportedContentTypes(EntityComplexPropertyProcessor.class)).thenReturn(jsonAndXml);

        final List<String> wildcard = Arrays.asList(HttpContentType.WILDCARD);
        when(service.getSupportedContentTypes(EntityMediaProcessor.class)).thenReturn(wildcard);
        when(service.getSupportedContentTypes(EntitySimplePropertyValueProcessor.class)).thenReturn(wildcard);
        when(service.getSupportedContentTypes(FunctionImportValueProcessor.class)).thenReturn(wildcard);

        List<String> atomFeedAndJsonAndXml = new ArrayList<String>();
        atomFeedAndJsonAndXml.add(HttpContentType.APPLICATION_ATOM_XML_FEED_UTF8);
        atomFeedAndJsonAndXml.add(HttpContentType.APPLICATION_ATOM_XML_UTF8);
        atomFeedAndJsonAndXml.addAll(jsonAndXml);
        when(service.getSupportedContentTypes(EntitySetProcessor.class)).thenReturn(atomFeedAndJsonAndXml);

        when(service.getSupportedContentTypes(MetadataProcessor.class)).thenReturn(Arrays.asList(HttpContentType.APPLICATION_XML_UTF8));

        List<String> atomSvcAndJsonAndXml = new ArrayList<String>();
        atomSvcAndJsonAndXml.add(HttpContentType.APPLICATION_ATOM_SVC_UTF8);
        atomSvcAndJsonAndXml.addAll(jsonAndXml);
        when(service.getSupportedContentTypes(ServiceDocumentProcessor.class)).thenReturn(atomSvcAndJsonAndXml);

        return service;
    }

    private void executeAndValidateRequest(final ODataHttpMethod method, final List<String> pathSegments,
            final Map<String, String> queryParameters, final String httpHeaderName, final String httpHeaderValue,
            final String requestContentType, final HttpStatusCodes expectedStatusCode) throws ODataException {

        ODataServiceFactory serviceFactory = mock(ODataServiceFactory.class);
        final ODataService service = mockODataService(serviceFactory);
        when(serviceFactory.createService(any(ODataContext.class))).thenReturn(service);

        final ODataRequest request =
                mockODataRequest(method, pathSegments, queryParameters, httpHeaderName, httpHeaderValue, requestContentType);
        final ODataContextImpl context = new ODataContextImpl(request, serviceFactory);

        final ODataResponse response = new ODataRequestHandler(serviceFactory, service, context).handle(request);
        assertNotNull(response);
        assertEquals(expectedStatusCode == null ? HttpStatusCodes.PAYMENT_REQUIRED : expectedStatusCode, response.getStatus());
    }

    private void executeAndValidateRequest(final ODataHttpMethod method, final UriType uriType, final String requestContentType,
            final HttpStatusCodes expectedStatusCode) throws ODataException {
        executeAndValidateRequest(method, createPathSegments(uriType, false, false), null, null, null, requestContentType,
                expectedStatusCode);
    }

    private void executeAndValidateHeaderRequest(final ODataHttpMethod method, final UriType uriType, final String httpHeaderName,
            final String httpHeaderValue, final HttpStatusCodes expectedStatusCode) throws ODataException {
        executeAndValidateRequest(method, createPathSegments(uriType, false, false), null, httpHeaderName, httpHeaderValue, null,
                expectedStatusCode);
    }

    private void checkAcceptHeader(final UriType uriType, final String acceptHeader, final HttpStatusCodes expectedStatusCode)
            throws ODataException {
        executeAndValidateHeaderRequest(ODataHttpMethod.GET, uriType, HttpHeaders.ACCEPT, acceptHeader, expectedStatusCode);
    }

    private void checkValueContentType(final ODataHttpMethod method, final UriType uriType, final String requestContentType)
            throws Exception {
        executeAndValidateRequest(method, createPathSegments(uriType, false, true), null, null, null, requestContentType, null);
    }

    private void wrongRequest(final ODataHttpMethod method, final List<String> pathSegments, final Map<String, String> queryParameters)
            throws ODataException {
        executeAndValidateRequest(method, pathSegments, queryParameters, null, null, null, HttpStatusCodes.METHOD_NOT_ALLOWED);
    }

    private void wrongOptions(final ODataHttpMethod method, final UriType uriType, final boolean format, final boolean filter,
            final boolean inlineCount, final boolean orderBy, final boolean skipToken, final boolean skip, final boolean top,
            final boolean expand, final boolean select) throws ODataException {
        wrongRequest(method, createPathSegments(uriType, false, false),
                createOptions(format, filter, inlineCount, orderBy, skipToken, skip, top, expand, select));
    }

    private void wrongFunctionHttpMethod(final ODataHttpMethod method, final UriType uriType) throws ODataException {
        wrongRequest(method, uriType == UriType.URI10a ? Arrays.asList("EmployeeSearch") : createPathSegments(uriType, false, false), null);
    }

    private void wrongProperty(final ODataHttpMethod method, final boolean ofComplex, final Boolean key) throws ODataException {
        EdmProperty property = (EdmProperty) (ofComplex ? edm.getComplexType("RefScenario", "c_Location")
                                                             .getProperty("Country")
                : edm.getEntityType("RefScenario", "Employee")
                     .getProperty("Age"));
        EdmFacets facets = mock(EdmFacets.class);
        when(facets.isNullable()).thenReturn(false);
        when(property.getFacets()).thenReturn(facets);

        List<String> pathSegments;
        if (ofComplex) {
            pathSegments = createPathSegments(UriType.URI4, false, true);
        } else {
            pathSegments = createPathSegments(UriType.URI2, false, false);
            pathSegments.add(key ? "EmployeeId" : "Age");
            pathSegments.add("$value");
        }

        wrongRequest(method, pathSegments, null);
    }

    private void wrongNavigationPath(final ODataHttpMethod method, final UriType uriType, final HttpStatusCodes expectedStatusCode)
            throws ODataException {
        executeAndValidateRequest(method, createPathSegments(uriType, true, false), null, null, null, null, expectedStatusCode);
    }

    private void wrongRequestContentType(final ODataHttpMethod method, final UriType uriType, final boolean isValue,
            final ContentType requestContentType) throws ODataException {
        executeAndValidateRequest(method, createPathSegments(uriType, false, isValue), null, null, null,
                requestContentType.toContentTypeString(), HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE);
    }

    private void wrongRequestContentType(final ODataHttpMethod method, final UriType uriType, final ContentType requestContentType)
            throws ODataException {
        wrongRequestContentType(method, uriType, false, requestContentType);
    }

    @Test
    public void dataServiceVersion() throws Exception {
        executeAndValidateHeaderRequest(ODataHttpMethod.GET, UriType.URI0, ODataHttpHeaders.DATASERVICEVERSION, "1.0", null);
        executeAndValidateHeaderRequest(ODataHttpMethod.GET, UriType.URI0, ODataHttpHeaders.DATASERVICEVERSION, "2.0", null);

        executeAndValidateHeaderRequest(ODataHttpMethod.GET, UriType.URI0, ODataHttpHeaders.DATASERVICEVERSION, "3.0",
                HttpStatusCodes.BAD_REQUEST);
        executeAndValidateHeaderRequest(ODataHttpMethod.GET, UriType.URI0, ODataHttpHeaders.DATASERVICEVERSION, "4.2",
                HttpStatusCodes.BAD_REQUEST);
        executeAndValidateHeaderRequest(ODataHttpMethod.GET, UriType.URI0, ODataHttpHeaders.DATASERVICEVERSION, "42",
                HttpStatusCodes.BAD_REQUEST);
        executeAndValidateHeaderRequest(ODataHttpMethod.GET, UriType.URI0, ODataHttpHeaders.DATASERVICEVERSION, "test.2.0",
                HttpStatusCodes.BAD_REQUEST);
    }

    @Test
    public void allowedMethods() throws Exception {
        executeAndValidateRequest(ODataHttpMethod.GET, UriType.URI0, null, null);
        executeAndValidateRequest(ODataHttpMethod.GET, UriType.URI1, null, null);
        executeAndValidateRequest(ODataHttpMethod.POST, UriType.URI1, HttpContentType.APPLICATION_JSON, null);
        executeAndValidateRequest(ODataHttpMethod.GET, UriType.URI2, null, null);
        executeAndValidateRequest(ODataHttpMethod.GET, UriType.URI3, null, null);
        executeAndValidateRequest(ODataHttpMethod.PATCH, UriType.URI3, HttpContentType.APPLICATION_JSON, null);
        executeAndValidateRequest(ODataHttpMethod.MERGE, UriType.URI3, HttpContentType.APPLICATION_JSON, null);
        executeAndValidateRequest(ODataHttpMethod.GET, UriType.URI4, null, null);
        executeAndValidateRequest(ODataHttpMethod.POST, UriType.URI9, HttpContentType.MULTIPART_MIXED, null);
        executeAndValidateRequest(ODataHttpMethod.GET, UriType.URI15, null, null);
        executeAndValidateRequest(ODataHttpMethod.GET, UriType.URI17, null, null);
    }

    @Test
    public void notAllowedMethod() throws Exception {
        wrongRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI0, false, false), null);
        wrongRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI1, false, false), null);
        wrongRequest(ODataHttpMethod.POST, createPathSegments(UriType.URI2, false, false), null);
        wrongRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI3, false, false), null);
        wrongRequest(ODataHttpMethod.POST, createPathSegments(UriType.URI4, false, false), null);
        wrongRequest(ODataHttpMethod.POST, createPathSegments(UriType.URI5, false, false), null);
        wrongRequest(ODataHttpMethod.POST, createPathSegments(UriType.URI6A, false, false), null);
        wrongRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI6B, false, false), null);
        wrongRequest(ODataHttpMethod.POST, createPathSegments(UriType.URI7A, false, false), null);
        wrongRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI7B, false, false), null);
        wrongRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI8, false, false), null);
        wrongRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI9, false, false), null);
        wrongRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI15, false, false), null);
        wrongRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI16, false, false), null);
        wrongRequest(ODataHttpMethod.PATCH, createPathSegments(UriType.URI17, false, false), null);
        wrongRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI50A, false, false), null);
        wrongRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI50B, false, false), null);
    }

    @Test
    public void notAllowedOptions() throws Exception {
        wrongOptions(ODataHttpMethod.POST, UriType.URI1, true, false, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, true, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, true, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, true, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, false, true, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, false, false, true, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, false, false, false, true, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, false, false, false, false, true, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, false, false, false, false, false, true);

        wrongOptions(ODataHttpMethod.PUT, UriType.URI2, true, false, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.PUT, UriType.URI2, false, false, false, false, false, false, false, true, false);
        wrongOptions(ODataHttpMethod.PUT, UriType.URI2, false, false, false, false, false, false, false, false, true);
        wrongOptions(ODataHttpMethod.PATCH, UriType.URI2, true, false, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.PATCH, UriType.URI2, false, false, false, false, false, false, false, true, false);
        wrongOptions(ODataHttpMethod.PATCH, UriType.URI2, false, false, false, false, false, false, false, false, true);
        wrongOptions(ODataHttpMethod.DELETE, UriType.URI2, true, false, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.DELETE, UriType.URI2, false, true, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.DELETE, UriType.URI2, false, false, false, false, false, false, false, true, false);
        wrongOptions(ODataHttpMethod.DELETE, UriType.URI2, false, false, false, false, false, false, false, false, true);

        wrongOptions(ODataHttpMethod.PUT, UriType.URI3, true, false, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.PATCH, UriType.URI3, true, false, false, false, false, false, false, false, false);

        wrongOptions(ODataHttpMethod.PUT, UriType.URI4, true, false, false, false, false, false, false, false, false);

        wrongOptions(ODataHttpMethod.PUT, UriType.URI5, true, false, false, false, false, false, false, false, false);

        wrongOptions(ODataHttpMethod.POST, UriType.URI6B, true, false, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, true, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, true, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, true, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, false, true, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, false, false, true, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, false, false, false, true, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, false, false, false, false, true, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, false, false, false, false, false, true);

        wrongOptions(ODataHttpMethod.PUT, UriType.URI7A, true, false, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.PUT, UriType.URI7A, false, true, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.DELETE, UriType.URI7A, true, false, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.DELETE, UriType.URI7A, false, true, false, false, false, false, false, false, false);

        wrongOptions(ODataHttpMethod.POST, UriType.URI7B, true, false, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, true, false, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, false, true, false, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, false, false, true, false, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, false, false, false, true, false, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, false, false, false, false, true, false, false, false);
        wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, false, false, false, false, false, true, false, false);

        wrongOptions(ODataHttpMethod.PUT, UriType.URI17, false, true, false, false, false, false, false, false, false);
        executeAndValidateRequest(ODataHttpMethod.PUT, createPathSegments(UriType.URI17, false, false),
                createOptions(true, false, false, false, false, false, false, false, false), null, null, null, HttpStatusCodes.BAD_REQUEST);
        executeAndValidateRequest(ODataHttpMethod.DELETE, createPathSegments(UriType.URI17, false, false),
                createOptions(true, false, false, false, false, false, false, false, false), null, null, null, HttpStatusCodes.BAD_REQUEST);
        wrongOptions(ODataHttpMethod.DELETE, UriType.URI17, false, true, false, false, false, false, false, false, false);
    }

    @Test
    public void functionImportWrongHttpMethod() throws Exception {
        wrongFunctionHttpMethod(ODataHttpMethod.POST, UriType.URI10a);
        wrongFunctionHttpMethod(ODataHttpMethod.PUT, UriType.URI10);
        wrongFunctionHttpMethod(ODataHttpMethod.POST, UriType.URI11);
        wrongFunctionHttpMethod(ODataHttpMethod.PATCH, UriType.URI12);
        wrongFunctionHttpMethod(ODataHttpMethod.POST, UriType.URI13);
        wrongFunctionHttpMethod(ODataHttpMethod.PUT, UriType.URI14);
    }

    @Test
    public void wrongProperty() throws Exception {
        wrongProperty(ODataHttpMethod.DELETE, true, false);

        wrongProperty(ODataHttpMethod.PUT, false, true);
        wrongProperty(ODataHttpMethod.PATCH, false, true);
        wrongProperty(ODataHttpMethod.DELETE, false, true);
        wrongProperty(ODataHttpMethod.DELETE, false, false);
    }

    @Test
    public void wrongNavigationPath() throws Exception {
        wrongNavigationPath(ODataHttpMethod.PUT, UriType.URI3, HttpStatusCodes.BAD_REQUEST);
        wrongNavigationPath(ODataHttpMethod.PATCH, UriType.URI3, HttpStatusCodes.BAD_REQUEST);

        wrongNavigationPath(ODataHttpMethod.PUT, UriType.URI4, HttpStatusCodes.BAD_REQUEST);
        wrongNavigationPath(ODataHttpMethod.PATCH, UriType.URI4, HttpStatusCodes.BAD_REQUEST);
        wrongNavigationPath(ODataHttpMethod.DELETE, UriType.URI4, HttpStatusCodes.METHOD_NOT_ALLOWED);

        wrongNavigationPath(ODataHttpMethod.PUT, UriType.URI5, HttpStatusCodes.BAD_REQUEST);
        wrongNavigationPath(ODataHttpMethod.PATCH, UriType.URI5, HttpStatusCodes.BAD_REQUEST);
        wrongNavigationPath(ODataHttpMethod.DELETE, UriType.URI5, HttpStatusCodes.METHOD_NOT_ALLOWED);

        wrongNavigationPath(ODataHttpMethod.PUT, UriType.URI7A, HttpStatusCodes.BAD_REQUEST);
        wrongNavigationPath(ODataHttpMethod.PATCH, UriType.URI7A, HttpStatusCodes.BAD_REQUEST);
        wrongNavigationPath(ODataHttpMethod.DELETE, UriType.URI7A, HttpStatusCodes.BAD_REQUEST);

        wrongNavigationPath(ODataHttpMethod.POST, UriType.URI6B, HttpStatusCodes.BAD_REQUEST);

        wrongNavigationPath(ODataHttpMethod.POST, UriType.URI7B, HttpStatusCodes.BAD_REQUEST);

        wrongNavigationPath(ODataHttpMethod.PUT, UriType.URI17, HttpStatusCodes.BAD_REQUEST);
        wrongNavigationPath(ODataHttpMethod.DELETE, UriType.URI17, HttpStatusCodes.BAD_REQUEST);
    }

    @Test
    public void requestAcceptHeader() throws Exception {
        checkAcceptHeader(UriType.URI0, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI1, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI2, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI3, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI4, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI5, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI6A, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI6B, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI7A, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI7B, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI8, HttpContentType.APPLICATION_XML, null);
        checkAcceptHeader(UriType.URI9, HttpContentType.APPLICATION_XML, HttpStatusCodes.METHOD_NOT_ALLOWED);
        checkAcceptHeader(UriType.URI10, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI11, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI12, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI13, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI14, HttpContentType.APPLICATION_JSON, null);
        checkAcceptHeader(UriType.URI15, HttpContentType.TEXT_PLAIN, null);
        checkAcceptHeader(UriType.URI16, HttpContentType.TEXT_PLAIN, null);
        checkAcceptHeader(UriType.URI17, HttpContentType.APPLICATION_OCTET_STREAM, null);
        checkAcceptHeader(UriType.URI50A, HttpContentType.TEXT_PLAIN, null);
        checkAcceptHeader(UriType.URI50B, HttpContentType.TEXT_PLAIN, null);

        checkAcceptHeader(UriType.URI8, HttpContentType.APPLICATION_JSON, HttpStatusCodes.NOT_ACCEPTABLE);
    }

    @Test
    public void requestContentType() throws Exception {
        executeAndValidateRequest(ODataHttpMethod.PUT, UriType.URI2, HttpContentType.APPLICATION_XML, null);
        executeAndValidateRequest(ODataHttpMethod.PATCH, UriType.URI2, HttpContentType.APPLICATION_XML, null);
        executeAndValidateRequest(ODataHttpMethod.MERGE, UriType.URI2, HttpContentType.APPLICATION_XML, null);

        executeAndValidateRequest(ODataHttpMethod.PUT, UriType.URI3, HttpContentType.APPLICATION_XML, null);
        executeAndValidateRequest(ODataHttpMethod.PATCH, UriType.URI3, HttpContentType.APPLICATION_XML, null);
        executeAndValidateRequest(ODataHttpMethod.MERGE, UriType.URI3, HttpContentType.APPLICATION_XML, null);

        executeAndValidateRequest(ODataHttpMethod.PUT, UriType.URI4, HttpContentType.APPLICATION_XML, null);
        executeAndValidateRequest(ODataHttpMethod.PATCH, UriType.URI4, HttpContentType.APPLICATION_XML, null);
        executeAndValidateRequest(ODataHttpMethod.MERGE, UriType.URI4, HttpContentType.APPLICATION_XML, null);

        executeAndValidateRequest(ODataHttpMethod.PUT, UriType.URI5, HttpContentType.APPLICATION_XML, null);
        executeAndValidateRequest(ODataHttpMethod.PATCH, UriType.URI5, HttpContentType.APPLICATION_XML, null);
        executeAndValidateRequest(ODataHttpMethod.MERGE, UriType.URI5, HttpContentType.APPLICATION_XML, null);

        executeAndValidateRequest(ODataHttpMethod.PUT, UriType.URI6A, HttpContentType.APPLICATION_XML, HttpStatusCodes.BAD_REQUEST);
        executeAndValidateRequest(ODataHttpMethod.PATCH, UriType.URI6A, HttpContentType.APPLICATION_XML, HttpStatusCodes.BAD_REQUEST);
        executeAndValidateRequest(ODataHttpMethod.MERGE, UriType.URI6A, HttpContentType.APPLICATION_XML, HttpStatusCodes.BAD_REQUEST);

        executeAndValidateRequest(ODataHttpMethod.POST, UriType.URI6B, HttpContentType.APPLICATION_XML, null);

        executeAndValidateRequest(ODataHttpMethod.PUT, UriType.URI7A, HttpContentType.APPLICATION_XML, null);
        executeAndValidateRequest(ODataHttpMethod.PATCH, UriType.URI7A, HttpContentType.APPLICATION_XML, null);
        executeAndValidateRequest(ODataHttpMethod.MERGE, UriType.URI7A, HttpContentType.APPLICATION_XML, null);

        executeAndValidateRequest(ODataHttpMethod.POST, UriType.URI7B, HttpContentType.APPLICATION_XML, null);

        executeAndValidateRequest(ODataHttpMethod.POST, UriType.URI9, HttpContentType.MULTIPART_MIXED, null);
    }

    @Test
    public void requestContentTypeMediaResource() throws Exception {
        executeAndValidateRequest(ODataHttpMethod.POST, UriType.URI1, "image/jpeg", null);
        executeAndValidateRequest(ODataHttpMethod.PUT, UriType.URI17, "image/jpeg", null);
    }

    @Test
    public void requestContentTypeFunctionImport() throws Exception {
        EdmFunctionImport function = edm.getDefaultEntityContainer()
                                        .getFunctionImport("MaximalAge");
        when(function.getHttpMethod()).thenReturn(ODataHttpMethod.PUT.name());
        executeAndValidateRequest(ODataHttpMethod.PUT, UriType.URI14, null, null);
        executeAndValidateRequest(ODataHttpMethod.PUT, UriType.URI14, HttpContentType.WILDCARD, null);
        checkValueContentType(ODataHttpMethod.PUT, UriType.URI14, null);
        checkValueContentType(ODataHttpMethod.PUT, UriType.URI14, HttpContentType.WILDCARD);

        function = edm.getDefaultEntityContainer()
                      .getFunctionImport("OldestEmployee");
        when(function.getHttpMethod()).thenReturn(ODataHttpMethod.POST.name());
        executeAndValidateRequest(ODataHttpMethod.POST, UriType.URI10, null, null);
    }

    @Test
    public void requestValueContentType() throws Exception {
        checkValueContentType(ODataHttpMethod.PUT, UriType.URI4, HttpContentType.TEXT_PLAIN);
        checkValueContentType(ODataHttpMethod.DELETE, UriType.URI4, HttpContentType.TEXT_PLAIN);
        checkValueContentType(ODataHttpMethod.PATCH, UriType.URI4, HttpContentType.TEXT_PLAIN);
        checkValueContentType(ODataHttpMethod.MERGE, UriType.URI4, HttpContentType.TEXT_PLAIN);
        checkValueContentType(ODataHttpMethod.PUT, UriType.URI4, HttpContentType.TEXT_PLAIN_UTF8);
        checkValueContentType(ODataHttpMethod.DELETE, UriType.URI4, HttpContentType.TEXT_PLAIN_UTF8);
        checkValueContentType(ODataHttpMethod.PATCH, UriType.URI4, HttpContentType.TEXT_PLAIN_UTF8);
        checkValueContentType(ODataHttpMethod.MERGE, UriType.URI4, HttpContentType.TEXT_PLAIN_UTF8);

        checkValueContentType(ODataHttpMethod.PUT, UriType.URI5, HttpContentType.TEXT_PLAIN);
        checkValueContentType(ODataHttpMethod.DELETE, UriType.URI5, HttpContentType.TEXT_PLAIN);
        checkValueContentType(ODataHttpMethod.PATCH, UriType.URI5, HttpContentType.TEXT_PLAIN);
        checkValueContentType(ODataHttpMethod.MERGE, UriType.URI5, HttpContentType.TEXT_PLAIN);

        checkValueContentType(ODataHttpMethod.PUT, UriType.URI17, HttpContentType.TEXT_PLAIN);
        checkValueContentType(ODataHttpMethod.DELETE, UriType.URI17, HttpContentType.TEXT_PLAIN);
    }

    @Test
    public void requestBinaryValueContentType() throws Exception {
        EdmProperty property = (EdmProperty) edm.getEntityType("RefScenario", "Employee")
                                                .getProperty("EmployeeName");
        when(property.getType()).thenReturn(EdmSimpleTypeKind.Binary.getEdmSimpleTypeInstance());
        checkValueContentType(ODataHttpMethod.PUT, UriType.URI5, HttpContentType.TEXT_PLAIN);
        when(property.getMimeType()).thenReturn("image/png");
        checkValueContentType(ODataHttpMethod.PUT, UriType.URI5, "image/png");
    }

    @Test
    public void wrongRequestContentType() throws Exception {
        wrongRequestContentType(ODataHttpMethod.POST, UriType.URI1, ContentType.WILDCARD);

        wrongRequestContentType(ODataHttpMethod.PUT, UriType.URI2, ContentType.APPLICATION_ATOM_SVC);
        wrongRequestContentType(ODataHttpMethod.PUT, UriType.URI2, ContentType.APPLICATION_ATOM_SVC_CS_UTF_8);
        wrongRequestContentType(ODataHttpMethod.PUT, UriType.URI2, ContentType.APPLICATION_ATOM_SVC);
        wrongRequestContentType(ODataHttpMethod.PUT, UriType.URI2, ContentType.APPLICATION_ATOM_SVC_CS_UTF_8);

        ODataHttpMethod[] methodsToTest = {ODataHttpMethod.PUT, ODataHttpMethod.PATCH, ODataHttpMethod.MERGE};

        for (ODataHttpMethod oDataHttpMethod : methodsToTest) {
            wrongRequestContentType(oDataHttpMethod, UriType.URI2, ContentType.create("image/jpeg"));

            wrongRequestContentType(oDataHttpMethod, UriType.URI3, ContentType.TEXT_PLAIN);

            wrongRequestContentType(oDataHttpMethod, UriType.URI4, ContentType.TEXT_PLAIN);

            wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_ATOM_SVC);
            wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_ATOM_SVC_CS_UTF_8);
            wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_XML);
            wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_XML_CS_UTF_8);
            wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_ATOM_XML);
            wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_ATOM_XML_CS_UTF_8);
            wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_JSON);
            wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_JSON_CS_UTF_8);
            wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.create("image/jpeg"));

            wrongRequestContentType(oDataHttpMethod, UriType.URI6A, ContentType.APPLICATION_ATOM_SVC);

            wrongRequestContentType(oDataHttpMethod, UriType.URI7A, ContentType.APPLICATION_ATOM_SVC);
        }

        wrongRequestContentType(ODataHttpMethod.POST, UriType.URI7B, ContentType.APPLICATION_ATOM_SVC);

        wrongRequestContentType(ODataHttpMethod.POST, UriType.URI9, ContentType.APPLICATION_OCTET_STREAM);
    }

    @Test
    public void unsupportedRequestContentTypeNoMediaResource() throws Exception {
        EdmEntityType entityType = edm.getDefaultEntityContainer()
                                      .getEntitySet("Employees")
                                      .getEntityType();
        when(entityType.hasStream()).thenReturn(false);

        wrongRequestContentType(ODataHttpMethod.POST, UriType.URI1, ContentType.APPLICATION_ATOM_SVC);
        wrongRequestContentType(ODataHttpMethod.POST, UriType.URI1, ContentType.APPLICATION_ATOM_SVC_CS_UTF_8);
        wrongRequestContentType(ODataHttpMethod.POST, UriType.URI1, ContentType.APPLICATION_OCTET_STREAM);
        wrongRequestContentType(ODataHttpMethod.POST, UriType.URI6B, ContentType.APPLICATION_ATOM_SVC);
    }

    @Test
    public void conditionalHandling() throws Exception {
        EdmProperty property = (EdmProperty) (edm.getEntityType("RefScenario", "Employee")
                                                 .getProperty("EmployeeId"));
        EdmFacets facets = mock(EdmFacets.class);
        when(facets.getConcurrencyMode()).thenReturn(EdmConcurrencyMode.Fixed);
        when(property.getFacets()).thenReturn(facets);

        executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI2, HttpHeaders.IF_MATCH, "W/\"1\"", null);
        executeAndValidateHeaderRequest(ODataHttpMethod.PATCH, UriType.URI2, HttpHeaders.IF_MATCH, "W/\"1\"", null);
        executeAndValidateHeaderRequest(ODataHttpMethod.MERGE, UriType.URI2, HttpHeaders.IF_MATCH, "W/\"1\"", null);
        executeAndValidateHeaderRequest(ODataHttpMethod.DELETE, UriType.URI2, HttpHeaders.IF_MATCH, "W/\"1\"", null);

        executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI3, HttpHeaders.IF_MATCH, "W/\"1\"", null);
        executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI4, HttpHeaders.IF_MATCH, "W/\"1\"", null);
        executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI5, HttpHeaders.IF_MATCH, "W/\"1\"", null);
        // executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI6A, HttpHeaders.IF_MATCH,
        // "W/\"1\"", null);
        executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI17, HttpHeaders.IF_MATCH, "W/\"1\"", null);

        executeAndValidateHeaderRequest(ODataHttpMethod.POST, UriType.URI1, HttpHeaders.IF_MATCH, "W/\"1\"", null);
        executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI7A, HttpHeaders.IF_MATCH, "W/\"1\"", null);

        executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI2, null, null, HttpStatusCodes.PRECONDITION_REQUIRED);
        executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI3, null, null, HttpStatusCodes.PRECONDITION_REQUIRED);
        executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI4, null, null, HttpStatusCodes.PRECONDITION_REQUIRED);
        executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI5, null, null, HttpStatusCodes.PRECONDITION_REQUIRED);
        // executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI6A, null, null,
        // HttpStatusCodes.PRECONDITION_REQUIRED);
        executeAndValidateHeaderRequest(ODataHttpMethod.PUT, UriType.URI17, null, null, HttpStatusCodes.PRECONDITION_REQUIRED);
    }
}
