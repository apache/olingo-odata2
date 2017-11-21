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
package org.apache.olingo.odata2.core.servlet;

import junit.framework.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class RestUtilTest {

  @Test
  public void testExtractQueryParameters() throws Exception {
    Map<String, String> result = RestUtil.extractQueryParameters("some=value");
    Assert.assertEquals("value", result.get("some"));

    result = RestUtil.extractQueryParameters("some=value&another=v");
    Assert.assertEquals("value", result.get("some"));
    Assert.assertEquals("v", result.get("another"));

    result = RestUtil.extractQueryParameters("");
    Assert.assertTrue(result.isEmpty());
  }

  @Test
  public void testExtractAllQueryParameters() throws Exception {
    Map<String, List<String>> result = RestUtil.extractAllQueryParameters("some=value", "false");
    Assert.assertEquals("value", result.get("some").get(0));

    result = RestUtil.extractAllQueryParameters("some=value&another=v", "false");
    Assert.assertEquals("value", result.get("some").get(0));
    Assert.assertEquals("v", result.get("another").get(0));

    result = RestUtil.extractAllQueryParameters("", "false");
    Assert.assertTrue(result.isEmpty());

    result = RestUtil.extractAllQueryParameters("some=v1&another=v&some=v2", "false");
    Assert.assertEquals("v1", result.get("some").get(0));
    Assert.assertEquals("v2", result.get("some").get(1));
    Assert.assertEquals("v", result.get("another").get(0));
  }

  @Test
  public void testExtractAcceptHeaders() throws Exception {
    // NuGet 4.0 client under .NET
    List<String> result = RestUtil.extractAcceptHeaders("application/atom+xml, application/xml");
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("application/atom+xml", result.get(0));
    Assert.assertEquals("application/xml", result.get(1));

    // NuGet 4.0 client under Mono
    result = RestUtil.extractAcceptHeaders("application/atom+xml,  application/xml");
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("application/atom+xml", result.get(0));
    Assert.assertEquals("application/xml", result.get(1));

    // Chrome 56
    result = RestUtil.extractAcceptHeaders(
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
    Assert.assertEquals(5, result.size());
    Assert.assertEquals("text/html", result.get(0));
    Assert.assertEquals("application/xhtml+xml", result.get(1));
    Assert.assertEquals("application/xml", result.get(2));
    Assert.assertEquals("image/webp", result.get(3));
    Assert.assertEquals("*/*", result.get(4));
  }
}