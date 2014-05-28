/*
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
 */
package org.apache.olingo.odata2.core;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.junit.Test;

public class ODataRequestTest {

  @Test
  public void testInsensitveHeaders() {

    Map<String, List<String>> headers = new HashMap<String, List<String>>();

    headers.put("lower", Arrays.asList("lower"));
    headers.put("UPPER", Arrays.asList("UPPER"));
    headers.put("mIxEd", Arrays.asList("mIxEd"));

    ODataRequest r1 = ODataRequest.requestHeaders(headers).build();
    verifyHeader(r1);

    ODataRequest r2 = ODataRequest.fromRequest(r1).build();
    verifyHeader(r2);

  }

  void verifyHeader(ODataRequest r) {
    assertEquals("lower", r.getRequestHeaderValue("lower"));
    assertEquals("lower", r.getRequestHeaderValue("LOWER"));
    assertEquals("lower", r.getRequestHeaderValue("Lower"));

    assertEquals("UPPER", r.getRequestHeaderValue("upper"));
    assertEquals("UPPER", r.getRequestHeaderValue("UPPER"));
    assertEquals("UPPER", r.getRequestHeaderValue("Upper"));

    assertEquals("mIxEd", r.getRequestHeaderValue("mixed"));
    assertEquals("mIxEd", r.getRequestHeaderValue("MIXED"));
    assertEquals("mIxEd", r.getRequestHeaderValue("mIxEd"));

    Map<String, List<String>> map = r.getRequestHeaders();

    assertEquals("lower", map.get("lower").get(0));
    assertEquals("lower", map.get("LOWER").get(0));
    assertEquals("lower", map.get("Lower").get(0));

    assertEquals("UPPER", map.get("upper").get(0));
    assertEquals("UPPER", map.get("UPPER").get(0));
    assertEquals("UPPER", map.get("Upper").get(0));

    assertEquals("mIxEd", map.get("mixed").get(0));
    assertEquals("mIxEd", map.get("MIXED").get(0));
    assertEquals("mIxEd", map.get("mIxEd").get(0));    
  }
}
