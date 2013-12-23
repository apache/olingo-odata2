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
package org.apache.olingo.odata2.annotation.processor.ref;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

/**
 * Tests employing the reference scenario reading the service document in JSON format.
 * 
 */
public class ServiceJsonTest extends AbstractRefTest {
  @Test
  public void serviceDocumentDollarFormatJson() throws Exception {
    final HttpResponse response = callUri("?$format=json");
    // checkMediaType(response, HttpContentType.APPLICATION_JSON);
    String body = getBody(response);

    Assert.assertTrue(jsonDataResponseContains(body, "Buildings"));
    Assert.assertTrue(jsonDataResponseContains(body, "Employees"));
    Assert.assertTrue(jsonDataResponseContains(body, "Managers"));
    Assert.assertTrue(jsonDataResponseContains(body, "Photos"));
    Assert.assertTrue(jsonDataResponseContains(body, "Rooms"));
    Assert.assertTrue(jsonDataResponseContains(body, "Teams"));
  }

  private boolean jsonDataResponseContains(String content, String containingValue) {
    return content.matches("\\{\"d\":\\{\"EntitySets\":\\[.*"
        + containingValue + ".*\"\\]\\}\\}");
  }
  
  @Test
  public void serviceDocumentAcceptHeaderJson() throws Exception {
    final HttpResponse response = callUri("", HttpHeaders.ACCEPT, HttpContentType.APPLICATION_JSON);
    String body = getBody(response);

    Assert.assertTrue(jsonDataResponseContains(body, "Buildings"));
    Assert.assertTrue(jsonDataResponseContains(body, "Employees"));
    Assert.assertTrue(jsonDataResponseContains(body, "Managers"));
    Assert.assertTrue(jsonDataResponseContains(body, "Photos"));
    Assert.assertTrue(jsonDataResponseContains(body, "Rooms"));
    Assert.assertTrue(jsonDataResponseContains(body, "Teams"));
  }

  @Test
  public void serviceDocumentAcceptHeaderInvalidCharset() throws Exception {
    final HttpResponse response =
        callUri("", HttpHeaders.ACCEPT, HttpContentType.APPLICATION_XML + "; charset=iso-latin-1",
            HttpStatusCodes.NOT_ACCEPTABLE);
    final String body = getBody(response);
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_M_2007_08);
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
    assertXpathExists("/a:error", body);
    assertXpathExists("/a:error/a:code", body);
    assertXpathExists("/a:error/a:message", body);
  }
}