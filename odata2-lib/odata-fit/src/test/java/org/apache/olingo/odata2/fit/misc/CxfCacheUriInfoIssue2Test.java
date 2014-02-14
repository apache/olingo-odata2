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
package org.apache.olingo.odata2.fit.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.apache.olingo.odata2.testutil.server.TestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CxfCacheUriInfoIssue2Test {

  private TestServer server1;
  private TestServer server2;

  @Before
  public void before() {
    server1 = new TestServer("/service1", ServletType.JAXRS_SERVLET);
    server2 = new TestServer("/service2", ServletType.JAXRS_SERVLET);

    server1.setPathSplit(0);
    server2.setPathSplit(0);

    server1.startServer(CxfCacheUriInfoIssueService1Factory.class, 19000);
    server2.startServer(CxfCacheUriInfoIssueService2Factory.class, 19100);
  }

  @Test
  public void testServletContextPath() throws Exception {
    URI uri1 = URI.create(server1.getEndpoint().toASCIIString() + "$metadata");
    final HttpGet get1 = new HttpGet(uri1);
    HttpResponse r1 = new DefaultHttpClient().execute(get1);
    assertNotNull(r1);
    assertEquals(uri1, CxfCacheUriInfoIssueService1Factory.service.getProcessor().getContext().getPathInfo()
        .getRequestUri());
    assertEquals(server1.getEndpoint(), CxfCacheUriInfoIssueService1Factory.service.getProcessor().getContext()
        .getPathInfo()
        .getServiceRoot());

    URI uri2 = URI.create(server2.getEndpoint().toASCIIString() + "$metadata");
    final HttpGet get2 = new HttpGet(uri2);
    HttpResponse r2 = new DefaultHttpClient().execute(get2);
    assertNotNull(r2);
    assertEquals(uri2, CxfCacheUriInfoIssueService2Factory.service.getProcessor().getContext().getPathInfo()
        .getRequestUri());
    assertEquals(server2.getEndpoint(), CxfCacheUriInfoIssueService2Factory.service.getProcessor().getContext()
        .getPathInfo()
        .getServiceRoot());
  }

  @After
  public void after() {
    server1.stopServer();
    server2.stopServer();
  }

}
