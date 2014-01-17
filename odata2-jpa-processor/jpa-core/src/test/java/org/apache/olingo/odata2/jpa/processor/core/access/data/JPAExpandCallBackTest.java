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
package org.apache.olingo.odata2.jpa.processor.core.access.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackResult;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.api.uri.NavigationPropertySegment;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.EdmMockUtil;
import org.junit.Test;

public class JPAExpandCallBackTest {

  @Test
  public void testRetrieveEntryResult() {
    JPAExpandCallBack callBack = getJPAExpandCallBackObject();
    WriteEntryCallbackContext writeFeedContext = EdmMockUtil.getWriteEntryCallBackContext();
    try {
      Field field = callBack.getClass().getDeclaredField("nextEntitySet");
      field.setAccessible(true);
      field.set(callBack, EdmMockUtil.mockTargetEntitySet());
      WriteEntryCallbackResult result = callBack.retrieveEntryResult(writeFeedContext);
      assertEquals(1, result.getEntryData().size());
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataApplicationException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testRetrieveFeedResult() {
    JPAExpandCallBack callBack = getJPAExpandCallBackObject();
    WriteFeedCallbackContext writeFeedContext = EdmMockUtil.getWriteFeedCallBackContext();
    try {
      Field field = callBack.getClass().getDeclaredField("nextEntitySet");
      field.setAccessible(true);
      field.set(callBack, EdmMockUtil.mockTargetEntitySet());
      WriteFeedCallbackResult result = callBack.retrieveFeedResult(writeFeedContext);
      assertEquals(2, result.getFeedData().size());
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataApplicationException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testGetCallbacks() {
    Map<String, ODataCallback> callBacks = null;
    try {
      URI baseUri =
          new URI("http://localhost:8080/org.apache.olingo.odata2.processor.ref.web/SalesOrderProcessing.svc/");
      ExpandSelectTreeNode expandSelectTreeNode = EdmMockUtil.mockExpandSelectTreeNode();
      List<ArrayList<NavigationPropertySegment>> expandList = EdmMockUtil.getExpandList();
      callBacks = JPAExpandCallBack.getCallbacks(baseUri, expandSelectTreeNode, expandList);
    } catch (URISyntaxException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertEquals(1, callBacks.size());

  }

  @Test
  public void testGetNextNavigationProperty() {
    JPAExpandCallBack callBack = getJPAExpandCallBackObject();
    List<ArrayList<NavigationPropertySegment>> expandList = EdmMockUtil.getExpandList();
    ArrayList<NavigationPropertySegment> expands = expandList.get(0);
    expands.add(EdmMockUtil.mockThirdNavigationPropertySegment());
    EdmNavigationProperty result = null;
    try {
      Field field = callBack.getClass().getDeclaredField("expandList");
      field.setAccessible(true);
      field.set(callBack, expandList);
      Class<?>[] formalParams = { EdmEntityType.class, EdmNavigationProperty.class };
      Object[] actualParams = { EdmMockUtil.mockSourceEdmEntityType(), EdmMockUtil.mockNavigationProperty() };
      Method method = callBack.getClass().getDeclaredMethod("getNextNavigationProperty", formalParams);
      method.setAccessible(true);
      result = (EdmNavigationProperty) method.invoke(callBack, actualParams);
      assertEquals("MaterialDetails", result.getName());

    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchMethodException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (InvocationTargetException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  private JPAExpandCallBack getJPAExpandCallBackObject() {
    Map<String, ODataCallback> callBacks = null;
    try {
      URI baseUri =
          new URI("http://localhost:8080/org.apache.olingo.odata2.processor.ref.web/SalesOrderProcessing.svc/");
      ExpandSelectTreeNode expandSelectTreeNode = EdmMockUtil.mockExpandSelectTreeNode();
      List<ArrayList<NavigationPropertySegment>> expandList = EdmMockUtil.getExpandList();
      callBacks = JPAExpandCallBack.getCallbacks(baseUri, expandSelectTreeNode, expandList);
    } catch (URISyntaxException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    return (JPAExpandCallBack) callBacks.get("SalesOrderLineItemDetails");
  }

}
