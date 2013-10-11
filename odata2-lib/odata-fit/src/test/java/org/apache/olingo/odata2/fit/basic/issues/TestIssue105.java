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
package org.apache.olingo.odata2.fit.basic.issues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.processor.part.MetadataProcessor;
import org.apache.olingo.odata2.api.uri.info.GetMetadataUriInfo;
import org.apache.olingo.odata2.fit.basic.AbstractBasicTest;
import org.apache.olingo.odata2.testutil.fit.FitStaticServiceFactory;
import org.junit.Test;

/**
 *  
 */
public class TestIssue105 extends AbstractBasicTest {

  @Override
  protected ODataSingleProcessor createProcessor() throws ODataException {
    final ODataSingleProcessor processor = mock(ODataSingleProcessor.class);
    when(((MetadataProcessor) processor).readMetadata(any(GetMetadataUriInfo.class), any(String.class))).thenReturn(
        ODataResponse.entity("metadata").status(HttpStatusCodes.OK).build());
    return processor;
  }

  @Test
  public void checkContextForDifferentHostNamesRequests() throws ClientProtocolException, IOException, ODataException,
      URISyntaxException {
    URI uri1 = URI.create(getEndpoint().toString() + "$metadata");

    HttpGet get1 = new HttpGet(uri1);
    HttpResponse response1 = getHttpClient().execute(get1);
    assertNotNull(response1);

    URI serviceRoot1 = getService().getProcessor().getContext().getPathInfo().getServiceRoot();
    assertEquals(uri1.getHost(), serviceRoot1.getHost());

    get1.reset();

    URI uri2 =
        new URI(uri1.getScheme(), uri1.getUserInfo(), "127.0.0.1", uri1.getPort(), uri1.getPath(), uri1.getQuery(),
            uri1.getFragment());

    HttpGet get2 = new HttpGet(uri2);
    HttpResponse response2 = getHttpClient().execute(get2);
    assertNotNull(response2);

    URI serviceRoot2 = getService().getProcessor().getContext().getPathInfo().getServiceRoot();
    assertEquals(uri2.getHost(), serviceRoot2.getHost());
  }

  @Test
  public void checkContextForDifferentWithHostHeader() throws ClientProtocolException, IOException,
      ODataException, URISyntaxException {
    try {
      FitStaticServiceFactory.bindService("123", getService());

      // 1st request: cache uri
      URI uri = URI.create(getEndpoint().toString() + "$metadata");
      HttpGet get1 = new HttpGet(uri);
      HttpResponse response1 = getHttpClient().execute(get1);
      assertNotNull(response1);

      URI serviceRoot1 = getService().getProcessor().getContext().getPathInfo().getServiceRoot();
      assertEquals(uri.getHost(), serviceRoot1.getHost());

      get1.reset();

      HttpGet get2 = new HttpGet(uri);
      get2.addHeader("Host", "bla:123");
      HttpResponse response2 = getHttpClient().execute(get2);
      assertNotNull(response2);

      URI serviceRoot2 = getService().getProcessor().getContext().getPathInfo().getServiceRoot();
      assertEquals("bla", serviceRoot2.getHost());
      assertEquals(123, serviceRoot2.getPort());

      URI requestUri = getService().getProcessor().getContext().getPathInfo().getRequestUri();
      assertEquals("bla", requestUri.getHost());
      assertEquals(123, requestUri.getPort());

    } finally {
      FitStaticServiceFactory.unbindService("123");
    }
  }

}
