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

import org.apache.http.HttpResponse;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.core.edm.provider.EdmImplProv;
import org.apache.olingo.odata2.ref.edm.ScenarioEdmProvider;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

public class URLEncodingTest extends AbstractRefTest {

  public URLEncodingTest(ServletType servletType) {
    super(servletType);
  }

  @Test
  public void encodingInQueryPartForFilter() throws Exception {
    Edm edm = new EdmImplProv(new ScenarioEdmProvider());
    final String uriString = "Employees?$format=json&$filter=EmployeeName%20eq%20'Walter%20Winter'";
    HttpResponse response = callUri(uriString);
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    ODataFeed feed = EntityProvider.readFeed(
        response.getFirstHeader("Content-Type").getValue(),
        edm.getEntityContainer(null).getEntitySet("Employees"),
        response.getEntity().getContent(),
        EntityProviderReadProperties.init().build());
    assertEquals(1, feed.getEntries().size());
  }
  
  @Test
  public void encodingInQueryPartForFilterWithAndEncoded() throws Exception {
    Edm edm = new EdmImplProv(new ScenarioEdmProvider());
    final String uriString = "Employees?$format=json&$filter=EmployeeName%20eq%20'Walter%26Winter'";
    HttpResponse response = callUri(uriString);
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    ODataFeed feed = EntityProvider.readFeed(
        response.getFirstHeader("Content-Type").getValue(),
        edm.getEntityContainer(null).getEntitySet("Employees"),
        response.getEntity().getContent(),
        EntityProviderReadProperties.init().build());
    assertEquals(0, feed.getEntries().size());
  }
  
  @Test
  public void encodingInQueryPartForFilterWithHiphonEncoded() throws Exception {
    Edm edm = new EdmImplProv(new ScenarioEdmProvider());
    final String uriString = "Employees?$format=json&$filter=EmployeeName%20eq%20'Walter%27%27Winter'";
    HttpResponse response = callUri(uriString);
    
    assertEquals(200, response.getStatusLine().getStatusCode());
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    ODataFeed feed = EntityProvider.readFeed(
        response.getFirstHeader("Content-Type").getValue(),
        edm.getEntityContainer(null).getEntitySet("Employees"),
        response.getEntity().getContent(),
        EntityProviderReadProperties.init().build());
    assertEquals(0, feed.getEntries().size());
  }
}
