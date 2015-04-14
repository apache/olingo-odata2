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
package org.apache.olingo.odata2.core.ep.producer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;

import org.apache.olingo.odata2.api.ODataServiceVersion;
import org.apache.olingo.odata2.api.commons.ODataHttpHeaders;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySetInfo;
import org.apache.olingo.odata2.api.edm.EdmServiceMetadata;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.ep.JsonEntityProvider;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.Test;

/**
 *  
 */
public class JsonServiceDocumentProducerTest extends BaseTest {

  @Test
  public void serviceDocumentEmpty() throws Exception {
    Edm edm = mock(Edm.class);
    EdmServiceMetadata metadata = mock(EdmServiceMetadata.class);
    when(edm.getServiceMetadata()).thenReturn(metadata);
    final ODataResponse response = new JsonEntityProvider().writeServiceDocument(edm, "http://host:80/service/");
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());
    assertEquals(ODataServiceVersion.V10, response.getHeader(ODataHttpHeaders.DATASERVICEVERSION));

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"EntitySets\":[]}}", json);
  }

  @Test
  public void serviceDocument() throws Exception {
    Edm edm = mock(Edm.class);
    EdmServiceMetadata metadata = mock(EdmServiceMetadata.class);
    EdmEntitySetInfo entitySetInfo1 = mock(EdmEntitySetInfo.class);
    when(entitySetInfo1.isDefaultEntityContainer()).thenReturn(true);
    when(entitySetInfo1.getEntitySetName()).thenReturn("EntitySet");
    EdmEntitySetInfo entitySetInfo2 = mock(EdmEntitySetInfo.class);
    when(entitySetInfo2.getEntityContainerName()).thenReturn("Container2");
    when(entitySetInfo2.getEntitySetName()).thenReturn("EntitySet2");
    when(metadata.getEntitySetInfos()).thenReturn(Arrays.asList(entitySetInfo1, entitySetInfo2));
    when(edm.getServiceMetadata()).thenReturn(metadata);
    final ODataResponse response = new JsonEntityProvider().writeServiceDocument(edm, "http://host:80/service/");
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("EntitypProvider must not set content header", response.getContentHeader());
    assertEquals(ODataServiceVersion.V10, response.getHeader(ODataHttpHeaders.DATASERVICEVERSION));

    final String json = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertNotNull(json);
    assertEquals("{\"d\":{\"EntitySets\":[\"EntitySet\",\"Container2.EntitySet2\"]}}", json);
  }
}
