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
package org.apache.olingo.odata2.jpa.processor.core.callback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallbackResult;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.easymock.EasyMock;
import org.junit.Test;

public class JPATombstoneCallBackTest {

  @Test
  public void Test() {

    EdmEntitySet edmEntitySet = EasyMock.createMock(EdmEntitySet.class);
    try {
      EasyMock.expect(edmEntitySet.getName()).andReturn("SalesOrder");
      EasyMock.replay(edmEntitySet);
    } catch (EdmException e) {
      fail("Not Expected");
    }
    GetEntitySetUriInfo resultsView = EasyMock.createMock(GetEntitySetUriInfo.class);
    EasyMock.expect(resultsView.getTargetEntitySet()).andReturn(edmEntitySet);
    EasyMock.replay(resultsView);
    JPATombstoneCallBack tombStoneCallBack = new JPATombstoneCallBack("/sample/", resultsView, "1");

    TombstoneCallbackResult result = tombStoneCallBack.getTombstoneCallbackResult();
    assertEquals("/sample/SalesOrder?!deltatoken=1", result.getDeltaLink());
  }

  @Test
  public void TestNull() {

    JPATombstoneCallBack tombStoneCallBack = new JPATombstoneCallBack(null, null, null);

    TombstoneCallbackResult result = tombStoneCallBack.getTombstoneCallbackResult();
    assertEquals("?!deltatoken=", result.getDeltaLink());
  }
}
