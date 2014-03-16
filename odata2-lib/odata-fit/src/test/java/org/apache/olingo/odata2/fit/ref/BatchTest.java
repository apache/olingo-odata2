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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

/**
 * 
 *  
 */
public class BatchTest extends AbstractRefTest {

  public BatchTest(final ServletType servletType) {
    super(servletType);
  }

  @Test
  public void testSimpleBatch() throws Exception {
    String responseBody = execute("/simple.batch");
    assertFalse(responseBody
        .contains("<error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">"));
    assertTrue(responseBody.contains("<edmx:Edmx Version=\"1.0\""));
  }

  @Test
  public void testChangeSetBatch() throws Exception {
    String responseBody = execute("/changeset.batch");
    assertTrue(responseBody.contains("Frederic Fall MODIFIED"));
  }

  @Test
  public void testContentIdReferencing() throws Exception {
    String responseBody = execute("/batchWithContentId.batch");
    assertTrue(responseBody.contains("HTTP/1.1 201 Created"));
    assertTrue(responseBody.contains("HTTP/1.1 204 No Content"));
    assertTrue(responseBody.contains("HTTP/1.1 200 OK"));
    assertTrue(responseBody.contains("\"EmployeeName\":\"Frederic Fall MODIFIED\""));
    assertTrue(responseBody.contains("\"Age\":40"));
  }

  @Test
  public void testContentIdEchoing() throws Exception {
    String responseBody = execute("/batchWithContentId.batch");
    assertTrue(responseBody.contains("Content-Id: 1"));
    assertTrue(responseBody.contains("Content-Id: 2"));
    assertTrue(responseBody.contains("Content-Id: 3"));
    assertTrue(responseBody.contains("Content-Id: 4"));
    assertTrue(responseBody.contains("Content-Id: AAA"));
    assertTrue(responseBody.contains("Content-Id: newEmployee"));
  }

  @Test
  public void testGPPG() throws Exception {
    HttpResponse response = execute("/batchWithContentIdPart2.batch", "batch_cf90-46e5-1246");
    String responseBody = StringHelper.inputStreamToString(response.getEntity().getContent(), true);

    assertContentContainValues(responseBody,
        "{\"d\":{\"EmployeeName\":\"Frederic Fall\"}}",
        "HTTP/1.1 201 Created",
        "Content-Id: employee",
        "Content-Type: application/json;odata=verbose",
        "\"EmployeeId\":\"7\",\"EmployeeName\":\"Employee 7\",",
        "HTTP/1.1 204 No Content",
        "Content-Id: AAA",
        "{\"d\":{\"EmployeeName\":\"Robert Fall\"}}");

    // validate that response for PUT does not contains a Content Type
    int indexNoContent = responseBody.indexOf("HTTP/1.1 204 No Content");
    int indexBoundary = responseBody.indexOf("--changeset_", indexNoContent);

    int indexContentType = responseBody.indexOf("Content-Type:", indexNoContent);
    Assert.assertTrue(indexBoundary < indexContentType);
  }

  @Test
  public void testErrorBatch() throws Exception {
    String responseBody = execute("/error.batch");
    assertTrue(responseBody.contains("HTTP/1.1 404 Not Found"));
  }

  /**
   * Validate that given <code>content</code> contains all <code>values</code> in the given order.
   * 
   * @param content
   * @param containingValues
   */
  private void assertContentContainValues(final String content, final String... containingValues) {
    int index = -1;
    for (String value : containingValues) {
      int newIndex = content.indexOf(value, index);
      Assert.assertTrue("Value '" + value + "' not found after index position '" + index + "'.", newIndex >= 0);
      index = newIndex;
    }
  }

  private String execute(final String batchResource) throws Exception {
    HttpResponse response = execute(batchResource, "batch_123");

    String responseBody = StringHelper.inputStreamToString(response.getEntity().getContent(), true);
    return responseBody;
  }

  private HttpResponse execute(final String batchResource, final String boundary) throws IOException,
      UnsupportedEncodingException, ClientProtocolException {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "$batch"));
    post.setHeader("Content-Type", "multipart/mixed;boundary=" + boundary);

    String body = StringHelper.inputStreamToString(this.getClass().getResourceAsStream(batchResource), true);
    HttpEntity entity = new StringEntity(body);
    post.setEntity(entity);
    HttpResponse response = getHttpClient().execute(post);

    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    return response;
  }
}