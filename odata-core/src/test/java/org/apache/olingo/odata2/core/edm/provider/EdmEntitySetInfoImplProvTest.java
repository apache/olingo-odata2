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
package org.apache.olingo.odata2.core.edm.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;

import org.apache.olingo.odata2.api.edm.EdmEntitySetInfo;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.testutil.fit.BaseTest;

public class EdmEntitySetInfoImplProvTest extends BaseTest {

  @Test
  public void entityWithDefaultContainer() throws Exception {
    String entitySetName = "Employees";
    URI entitySetUri = new URI(entitySetName);
    String entityContainerName = "Container";

    EntitySet entitySet = new EntitySet();
    entitySet.setName(entitySetName);

    EntityContainerInfo entityContainerInfo = new EntityContainerInfo();
    entityContainerInfo.setName(entityContainerName).setDefaultEntityContainer(true);

    EdmEntitySetInfo info = new EdmEntitySetInfoImplProv(entitySet, entityContainerInfo);

    assertEquals(entitySetName, info.getEntitySetName());
    assertEquals(entitySetUri.toASCIIString(), info.getEntitySetUri().toASCIIString());
    assertEquals(entityContainerName, info.getEntityContainerName());
    assertTrue(info.isDefaultEntityContainer());
  }

  @Test
  public void entityWithoutDefaultContainer() throws Exception {
    String entitySetName = "Employees";
    String entityContainerName = "Container";
    URI entitySetUri = new URI(entityContainerName + "." + entitySetName);

    EntitySet entitySet = new EntitySet();
    entitySet.setName(entitySetName);

    EntityContainerInfo entityContainerInfo = new EntityContainerInfo();
    entityContainerInfo.setName(entityContainerName).setDefaultEntityContainer(false);

    EdmEntitySetInfo info = new EdmEntitySetInfoImplProv(entitySet, entityContainerInfo);

    assertEquals(entitySetName, info.getEntitySetName());
    assertEquals(entitySetUri.toASCIIString(), info.getEntitySetUri().toASCIIString());
    assertEquals(entityContainerName, info.getEntityContainerName());
    assertFalse(info.isDefaultEntityContainer());
  }

}
