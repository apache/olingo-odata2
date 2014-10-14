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

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.testutil.fit.AbstractFitTest;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

public class NullServiceTest extends AbstractFitTest {

  public NullServiceTest(final ServletType servletType) {
    super(servletType);
  }

  @Override
  protected ODataService createService() throws ODataException {
    return null;
  }

  @Test
  public void nullServiceMustResultInODataResponse() throws Exception {
    System.out.println("The following internal Server Error is wanted if this test doesnt fail!");
    final HttpResponse response = executeGetRequest("$metadata");
    assertEquals(HttpStatusCodes.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusLine().getStatusCode());

    
    ODataErrorContext error = EntityProvider.readErrorDocument(response.getEntity().getContent(), "application/xml");
    assertEquals("Service unavailable.", error.getMessage());
  }
  
  private HttpResponse executeGetRequest(final String request) throws ClientProtocolException, IOException {
    final HttpGet get = new HttpGet(URI.create(getEndpoint().toString() + request));
    return getHttpClient().execute(get);
  }

}
