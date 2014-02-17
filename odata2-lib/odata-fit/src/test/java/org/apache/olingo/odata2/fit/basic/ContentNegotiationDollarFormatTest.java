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
package org.apache.olingo.odata2.fit.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.processor.feature.CustomContentType;
import org.apache.olingo.odata2.api.processor.part.ServiceDocumentProcessor;
import org.apache.olingo.odata2.api.uri.info.GetServiceDocumentUriInfo;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

/**
 *  
 */
public class ContentNegotiationDollarFormatTest extends AbstractBasicTest {

  public ContentNegotiationDollarFormatTest(final ServletType servletType) {
    super(servletType);
  }

  /**
   * 
   */
  private static final String CUSTOM_CONTENT_TYPE = "application/csv";
  ODataSingleProcessor processor = mock(ODataSingleProcessor.class);

  @Override
  protected ODataSingleProcessor createProcessor() throws ODataException {
    // service document
    final String contentType = HttpContentType.APPLICATION_ATOM_SVC_UTF8;
    final ODataResponse responseAtomXml =
        ODataResponse.status(HttpStatusCodes.OK).contentHeader(contentType).entity("Test passed.").build();
    when(
        ((ServiceDocumentProcessor) processor).readServiceDocument(any(GetServiceDocumentUriInfo.class),
            eq(contentType))).thenReturn(responseAtomXml);

    // csv
    final ODataResponse value =
        ODataResponse.status(HttpStatusCodes.OK).contentHeader(CUSTOM_CONTENT_TYPE).entity("any content").build();
    when(((ServiceDocumentProcessor) processor).readServiceDocument(any(GetServiceDocumentUriInfo.class), eq("csv")))
        .thenReturn(value);
    when(((CustomContentType) processor).getCustomContentTypes(ServiceDocumentProcessor.class)).thenReturn(
        Arrays.asList("csv"));

    return processor;
  }

  @Test
  public void atomFormatForServiceDocument() throws Exception {
    final HttpResponse response = executeGetRequest("?$format=atom");

    final String responseEntity = StringHelper.httpEntityToString(response.getEntity());
    assertEquals("Test passed.", responseEntity);
    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final Header header = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
    assertEquals(HttpContentType.APPLICATION_ATOM_SVC_UTF8, header.getValue());
  }

  @Test
  public void formatCustom() throws Exception {
    final HttpResponse response = executeGetRequest("?$format=csv");

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final Header header = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
    assertNotNull(header);
    assertEquals(CUSTOM_CONTENT_TYPE, header.getValue());
  }

  @Test
  public void formatNotAccepted() throws Exception {
    final HttpResponse response = executeGetRequest("?$format=csvOrSomethingElse");

    assertEquals(HttpStatusCodes.NOT_ACCEPTABLE.getStatusCode(), response.getStatusLine().getStatusCode());
  }
}
