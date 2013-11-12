/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.olingo.odata2.core.annotation.processor.json;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class JsonConsumerTest {

  public static final String SIMPLE_TEAM = "{\"d\":{"
          + "\"__metadata\":{\"id\":\"https://localhost/Test.svc/Teams('1')\","
          + "\"uri\":\"https://localhost/Test.svc/Teams('1')\","
          + "\"type\":\"Test.Team\""
          + "},"
          + "\"Id\":\"1\","
          + "\"Name\":\"Team 1\","
          + "\"isScrumTeam\":false}}";

  public static final String SIMPLER_TEAM = "{"
          + "\"d\":{"
          + "\"Id\":\"1\",\"Name\":\"Team 1\",\"isScrumTeam\":false"
          + "}}";

  @Test
  public void simplerRead() throws Exception {
    InputStream content = encapsulate(SIMPLER_TEAM);
    JsonConsumer jc = new JsonConsumer(content);
    Map<String, String> name2Values = jc.read();

//    System.out.println("" + name2Values);
    Assert.assertEquals(3, name2Values.size());

    Assert.assertEquals("1", name2Values.get("Id"));
    Assert.assertEquals("Team 1", name2Values.get("Name"));
    Assert.assertEquals("false", name2Values.get("isScrumTeam"));
  }

  @Test
  public void simpleRead() throws Exception {
    InputStream content = encapsulate(SIMPLE_TEAM);
    JsonConsumer jc = new JsonConsumer(content);
    Map<String, String> name2Values = jc.read();

//    System.out.println("" + name2Values);
    Assert.assertEquals(6, name2Values.size());

    Assert.assertEquals("1", name2Values.get("Id"));
    Assert.assertEquals("Team 1", name2Values.get("Name"));
    Assert.assertEquals("false", name2Values.get("isScrumTeam"));
    
    Assert.assertEquals("https://localhost/Test.svc/Teams('1')", name2Values.get("md_id"));
    Assert.assertEquals("https://localhost/Test.svc/Teams('1')", name2Values.get("md_uri"));
    Assert.assertEquals("Test.Team", name2Values.get("md_type"));
  }

  public static InputStream encapsulate(final String content) {
    try {
      return new ByteArrayInputStream(content.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // we know that UTF-8 is supported
      throw new RuntimeException("UTF-8 MUST be supported.", e);
    }
  }
}