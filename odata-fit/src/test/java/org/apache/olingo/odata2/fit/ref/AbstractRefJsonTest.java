/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.fit.ref;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;
import com.google.gson.reflect.TypeToken;

/**
 * @author
 */
public class AbstractRefJsonTest extends AbstractRefTest {
  public StringMap<?> getStringMap(final String body) {
    Gson gson = new Gson();
    final StringMap<?> map = gson.fromJson(body, new TypeToken<StringMap<?>>() {}.getType());
    if (map.get("d") instanceof StringMap<?>) {
      return (StringMap<?>) map.get("d");
    } else {
      return map;
    }
  }
}
