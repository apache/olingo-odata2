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
package org.apache.olingo.odata2.fit.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.core.processor.ODataSingleProcessorService;
import org.apache.olingo.odata2.ref.edm.ScenarioEdmProvider;
import org.apache.olingo.odata2.ref.model.DataContainer;
import org.apache.olingo.odata2.ref.model.Photo;
import org.apache.olingo.odata2.ref.processor.ListsProcessor;
import org.apache.olingo.odata2.ref.processor.ScenarioDataSource;
import org.apache.olingo.odata2.testutil.fit.AbstractFitTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;

/**
 * Abstract base class for tests employing the reference scenario.
 * 
 */
public class AbstractRefTest extends AbstractFitTest {

  protected static final String IMAGE_JPEG = "image/jpeg";
  protected static final String IMAGE_GIF = "image/gif";

  protected static final String EMPLOYEE_1_NAME = "Walter Winter";
  protected static final String EMPLOYEE_2_NAME = "Frederic Fall";
  protected static final String EMPLOYEE_3_NAME = "Jonathan Smith";
  protected static final String EMPLOYEE_4_NAME = "Peter Burke";
  protected static final String EMPLOYEE_5_NAME = "John Field";
  protected static final String EMPLOYEE_6_NAME = "Susan Bay";
  protected static final String MANAGER_NAME = EMPLOYEE_1_NAME;

  protected static final String EMPLOYEE_2_AGE = "32";
  protected static final String EMPLOYEE_3_AGE = "56";
  protected static final String EMPLOYEE_6_AGE = "29";

  protected static final String CITY_1_NAME = "Heidelberg";
  protected static final String CITY_2_NAME = "Walldorf";

  protected static final String BUILDING_3_NAME = "Building 3";

  protected static final String PHOTO_DEFAULT_IMAGE = Base64.encodeBase64String(new Photo(0, null, null).getImage());

  @Override
  protected ODataSingleProcessorService createService() {
    DataContainer dataContainer = new DataContainer();
    dataContainer.reset();
    ODataSingleProcessor processor = new ListsProcessor(new ScenarioDataSource(dataContainer));
    EdmProvider provider = new ScenarioEdmProvider();

    return new ODataSingleProcessorService(provider, processor) {};
  }

  protected HttpResponse callUri(
      final ODataHttpMethod httpMethod, final String uri,
      final String additionalHeader, final String additionalHeaderValue,
      final String requestBody, final String requestContentType,
      final HttpStatusCodes expectedStatusCode) throws Exception {

    HttpRequestBase request =
        httpMethod == ODataHttpMethod.GET ? new HttpGet() :
            httpMethod == ODataHttpMethod.DELETE ? new HttpDelete() :
                httpMethod == ODataHttpMethod.POST ? new HttpPost() :
                    httpMethod == ODataHttpMethod.PUT ? new HttpPut() : new HttpPatch();
    request.setURI(URI.create(getEndpoint() + uri));
    if (additionalHeader != null) {
      request.addHeader(additionalHeader, additionalHeaderValue);
    }
    if (requestBody != null) {
      ((HttpEntityEnclosingRequest) request).setEntity(new StringEntity(requestBody));
      request.setHeader(HttpHeaders.CONTENT_TYPE, requestContentType);
    }

    final HttpResponse response = getHttpClient().execute(request);

    assertNotNull(response);
    assertEquals(expectedStatusCode.getStatusCode(), response.getStatusLine().getStatusCode());

    if (expectedStatusCode == HttpStatusCodes.OK) {
      assertNotNull(response.getEntity());
      assertNotNull(response.getEntity().getContent());
    } else if (expectedStatusCode == HttpStatusCodes.CREATED) {
      assertNotNull(response.getEntity());
      assertNotNull(response.getEntity().getContent());
      assertNotNull(response.getFirstHeader(HttpHeaders.LOCATION));
    } else if (expectedStatusCode == HttpStatusCodes.NO_CONTENT) {
      assertTrue(response.getEntity() == null || response.getEntity().getContent() == null);
    }

    return response;
  }

  protected HttpResponse callUri(final String uri, final String additionalHeader, final String additionalHeaderValue,
      final HttpStatusCodes expectedStatusCode) throws Exception {
    return callUri(ODataHttpMethod.GET, uri, additionalHeader, additionalHeaderValue, null, null, expectedStatusCode);
  }

  protected HttpResponse callUri(final String uri, final String additionalHeader, final String additionalHeaderValue)
      throws Exception {
    return callUri(ODataHttpMethod.GET, uri, additionalHeader, additionalHeaderValue, null, null, HttpStatusCodes.OK);
  }

  protected HttpResponse callUri(final String uri, final HttpStatusCodes expectedStatusCode) throws Exception {
    return callUri(uri, null, null, expectedStatusCode);
  }

  protected HttpResponse callUri(final String uri) throws Exception {
    return callUri(uri, HttpStatusCodes.OK);
  }

  protected void checkUri(final String uri) throws Exception {
    assertNotNull(getBody(callUri(uri)));
  }

  protected void badRequest(final String uri) throws Exception {
    final HttpResponse response = callUri(uri, HttpStatusCodes.BAD_REQUEST);
    assertNotNull(getBody(response));
  }

  protected void notFound(final String uri) throws Exception {
    final HttpResponse response = callUri(uri, HttpStatusCodes.NOT_FOUND);
    assertNotNull(getBody(response));
  }

  protected void deleteUri(final String uri, final HttpStatusCodes expectedStatusCode)
      throws Exception, AssertionError {
    final HttpResponse response = callUri(ODataHttpMethod.DELETE, uri, null, null, null, null, expectedStatusCode);
    if (expectedStatusCode != HttpStatusCodes.NO_CONTENT) {
      response.getEntity().getContent().close();
    }
  }

  protected void deleteUriOk(final String uri) throws Exception {
    deleteUri(uri, HttpStatusCodes.NO_CONTENT);
  }

  protected HttpResponse postUri(final String uri, final String requestBody, final String requestContentType,
      final HttpStatusCodes expectedStatusCode) throws Exception {
    return callUri(ODataHttpMethod.POST, uri, null, null, requestBody, requestContentType, expectedStatusCode);
  }

  protected HttpResponse postUri(final String uri, final String requestBody, final String requestContentType,
      final String additionalHeader, final String additionalHeaderValue, final HttpStatusCodes expectedStatusCode)
      throws Exception {
    return callUri(ODataHttpMethod.POST, uri, additionalHeader, additionalHeaderValue, requestBody, requestContentType,
        expectedStatusCode);
  }

  protected void putUri(final String uri,
      final String requestBody, final String requestContentType,
      final HttpStatusCodes expectedStatusCode) throws Exception {
    final HttpResponse response =
        callUri(ODataHttpMethod.PUT, uri, null, null, requestBody, requestContentType, expectedStatusCode);
    if (expectedStatusCode != HttpStatusCodes.NO_CONTENT) {
      response.getEntity().getContent().close();
    }
  }

  protected void putUri(final String uri, final String acceptHeader,
      final String requestBody, final String requestContentType,
      final HttpStatusCodes expectedStatusCode) throws Exception {
    final HttpResponse response =
        callUri(ODataHttpMethod.PUT, uri,
            org.apache.olingo.odata2.api.commons.HttpHeaders.ACCEPT, acceptHeader, requestBody, requestContentType,
            expectedStatusCode);
    if (expectedStatusCode != HttpStatusCodes.NO_CONTENT) {
      response.getEntity().getContent().close();
    }
  }

  protected String getBody(final HttpResponse response) throws Exception {
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNotNull(response.getEntity().getContent());
    return StringHelper.inputStreamToString(response.getEntity().getContent());
  }

  protected void checkMediaType(final HttpResponse response, final String expectedMediaType) {
    assertEquals(expectedMediaType.toUpperCase(), 
        response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue().toUpperCase());
  }

  protected void checkEtag(final HttpResponse response, final String expectedEtag) {
    assertNotNull(response.getFirstHeader(HttpHeaders.ETAG));
    final String entityTag = response.getFirstHeader(HttpHeaders.ETAG).getValue();
    assertNotNull(entityTag);
    assertEquals(expectedEtag, entityTag);
  }
}
