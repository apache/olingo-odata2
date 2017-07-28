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
package org.apache.olingo.odata2.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.Test;

/**
 *  
 */
public class ODataResponseTest extends BaseTest {

  @Test
  public void buildStatusResponseTest() {
    ODataResponse response = ODataResponse.status(HttpStatusCodes.FOUND).build();
    assertEquals(HttpStatusCodes.FOUND, response.getStatus());
  }

  @Test
  public void buildEntityResponseTest() {
    ODataResponse response = ODataResponse.entity("abc").build();
    assertNull(response.getStatus());
    assertEquals("abc", response.getEntity());
  }

  @Test
  public void buildEntityAsStreamResponseTest() throws Exception {
    ODataResponse response = ODataResponse.entity("abc").build();
    assertNull(response.getStatus());
    InputStream entityAsStream = response.getEntityAsStream();
    assertNotNull(entityAsStream);
    assertEquals("abc", StringHelper.inputStreamToString(entityAsStream));
  }

  @Test
  public void buildEntityAsStreamResponseTestCharset() throws Exception {
    ODataResponse response = ODataResponse.entity("äbc")
        .contentHeader("app/json; charset=utf-8").build();
    assertNull(response.getStatus());
    InputStream entityAsStream = response.getEntityAsStream();
    assertNotNull(entityAsStream);
    assertEquals("äbc", StringHelper.inputStreamToString(entityAsStream));
  }

  @Test
  public void buildEntityAsStreamResponseTestCharsetIso() throws Exception {
    ODataResponse response = ODataResponse.entity("äbc")
        .contentHeader("app/json; charset=iso-8859-1").build();
    assertNull(response.getStatus());
    InputStream entityAsStream = response.getEntityAsStream();
    assertNotNull(entityAsStream);
    StringHelper.Stream s = StringHelper.toStream(entityAsStream);
    assertEquals("äbc", s.asString("iso-8859-1"));
  }

  @Test
  public void buildHeaderResponseTest() {
    ODataResponse response = ODataResponse
        .header("abc", "123")
        .header("def", "456")
        .header("ghi", null)
        .build();
    assertNull(response.getStatus());
    assertEquals("123", response.getHeader("abc"));
    assertEquals("456", response.getHeader("def"));
    assertNull(response.getHeader("ghi"));
  }

  @Test
  public void contentHeader() {
    final ODataResponse response = ODataResponse.contentHeader(HttpContentType.APPLICATION_OCTET_STREAM).build();
    assertNull(response.getStatus());
    assertEquals(HttpContentType.APPLICATION_OCTET_STREAM, response.getContentHeader());
    assertTrue(response.containsHeader(HttpHeaders.CONTENT_TYPE));
    assertEquals(HttpContentType.APPLICATION_OCTET_STREAM, response.getHeader(HttpHeaders.CONTENT_TYPE));
    assertFalse(response.containsHeader(HttpHeaders.CONTENT_LENGTH));
    assertEquals(new HashSet<String>(Arrays.asList(HttpHeaders.CONTENT_TYPE)), response.getHeaderNames());
  }

  @Test
  public void completeResponse() {
    final ODataResponse response = ODataResponse.newBuilder()
        .status(HttpStatusCodes.OK)
        .header("def", "456")
        .eTag("x")
        .contentHeader(HttpContentType.TEXT_PLAIN)
        .idLiteral("id")
        .entity("body")
        .build();
    assertEquals(HttpStatusCodes.OK, response.getStatus());
    assertEquals("456", response.getHeader("def"));
    assertEquals("x", response.getETag());
    assertEquals(HttpContentType.TEXT_PLAIN, response.getContentHeader());
    assertEquals("id", response.getIdLiteral());
    assertEquals(4, response.getHeaderNames().size());
    assertEquals("body", response.getEntity());

    final ODataResponse responseCopy = ODataResponse.fromResponse(response).build();
    assertEquals(HttpStatusCodes.OK, responseCopy.getStatus());
    assertEquals("456", responseCopy.getHeader("def"));
    assertEquals("x", responseCopy.getETag());
    assertEquals(HttpContentType.TEXT_PLAIN, response.getContentHeader());
    assertEquals("id", responseCopy.getIdLiteral());
    assertEquals("body", responseCopy.getEntity());
  }
}
