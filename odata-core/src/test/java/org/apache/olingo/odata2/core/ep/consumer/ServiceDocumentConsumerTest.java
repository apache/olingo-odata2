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
package org.apache.olingo.odata2.core.ep.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmEntitySetInfo;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.servicedocument.AtomInfo;
import org.apache.olingo.odata2.api.servicedocument.Collection;
import org.apache.olingo.odata2.api.servicedocument.ExtensionElement;
import org.apache.olingo.odata2.api.servicedocument.ServiceDocument;
import org.apache.olingo.odata2.api.servicedocument.Workspace;
import org.junit.Test;

public class ServiceDocumentConsumerTest {

  @Test
  public void test() throws EntityProviderException {
    InputStream in = ClassLoader.class.getResourceAsStream("/svcExample.xml");
    assertNotNull(EntityProvider.readServiceDocument(in, "application/atom+xml"));
  }

  @Test
  public void testAtomServiceDocument() throws EntityProviderException {
    InputStream in = ClassLoader.class.getResourceAsStream("/svcExample.xml");
    ServiceDocument serviceDocument = EntityProvider.readServiceDocument(in, "application/atom+xml");
    assertNotNull(serviceDocument);
    AtomInfo atomInfo = serviceDocument.getAtomInfo();
    assertNotNull(atomInfo);
    for (Workspace workspace : atomInfo.getWorkspaces()) {
      assertEquals(10, workspace.getCollections().size());
      for (Collection collection : workspace.getCollections()) {
        assertNotNull(collection.getExtesionElements().get(0));
        assertEquals("member-title", collection.getExtesionElements().get(0).getName());
        assertEquals("foo", collection.getExtesionElements().get(0).getPrefix());
      }
    }
    for (ExtensionElement extElement : atomInfo.getExtesionElements()) {
      assertEquals(2, extElement.getAttributes().size());
    }
  }

  @Test
  public void testJson() throws EntityProviderException {
    InputStream in = ClassLoader.class.getResourceAsStream("/svcDocJson.json");
    assertNotNull(EntityProvider.readServiceDocument(in, "application/json"));
  }

  @Test(expected = EntityProviderException.class)
  public void testInvalidInputStream() throws EntityProviderException {
    InputStream in = ClassLoader.class.getResourceAsStream("/svcDocJson.json");
    EntityProvider.readServiceDocument(in, "application/atom+xml");
  }

  @Test
  public void testJsonServiceDocument() throws EntityProviderException {
    InputStream in = ClassLoader.class.getResourceAsStream("/svcDocJson.json");
    ServiceDocument serviceDoc = EntityProvider.readServiceDocument(in, "application/json");
    assertNotNull(serviceDoc);
    assertNull(serviceDoc.getAtomInfo());
    List<EdmEntitySetInfo> entitySetsInfo = serviceDoc.getEntitySetsInfo();
    assertEquals(6, entitySetsInfo.size());
    for (EdmEntitySetInfo entitySetInfo : entitySetsInfo) {
      if (!entitySetInfo.isDefaultEntityContainer()) {
        if ("Container2".equals(entitySetInfo.getEntityContainerName())) {
          assertEquals("Photos", entitySetInfo.getEntitySetName());
        } else if ("Container.Nr1".equals(entitySetInfo.getEntityContainerName())) {
          assertEquals("Employees", entitySetInfo.getEntitySetName());
        }
      }
    }
  }

  @Test
  public void testCompareJsonWithAtom() throws EntityProviderException {
    InputStream inputJson = ClassLoader.class.getResourceAsStream("/svcDocJson.json");
    ServiceDocument serviceDocJson = EntityProvider.readServiceDocument(inputJson, "application/json");
    assertNotNull(serviceDocJson);
    List<EdmEntitySetInfo> entitySetsInfoJson = serviceDocJson.getEntitySetsInfo();

    InputStream inputAtom = ClassLoader.class.getResourceAsStream("/serviceDocument.xml");
    ServiceDocument serviceDocAtom = EntityProvider.readServiceDocument(inputAtom, "application/atom+xml");
    assertNotNull(serviceDocAtom);
    List<EdmEntitySetInfo> entitySetsInfoAtom = serviceDocAtom.getEntitySetsInfo();

    assertEquals(entitySetsInfoJson.size(), entitySetsInfoAtom.size());
    for (int i = 0; i < entitySetsInfoJson.size(); i++) {
      assertEquals(entitySetsInfoJson.get(i).getEntitySetUri(), entitySetsInfoAtom.get(i).getEntitySetUri());
    }
  }
}
