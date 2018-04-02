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
package org.apache.olingo.odata2.client.core.edm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.olingo.odata2.client.core.edm.Impl.EdmMappingImpl;
import org.junit.Test;

public class EdmMappingImplTest {

  @Test
  public void mapTest() {
    EdmMappingImpl map= new EdmMappingImpl();
    map.setInternalName("name");
    map.setMediaResourceMimeTypeKey("media");
    map.setMediaResourceSourceKey("mediaKey");
    map.setObject(null);
    assertNotNull(map);
    assertEquals("name", map.getInternalName());
    assertEquals("media", map.getMediaResourceMimeTypeKey());
    assertEquals("mediaKey", map.getMediaResourceSourceKey());
    assertNull(map.getObject());
  }
}
