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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteEntryContent;
import org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackResult;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.core.ep.AbstractProviderTest;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *  
 */
public class MyCallback implements OnWriteEntryContent, OnWriteFeedContent {

  private AbstractProviderTest dataProvider;
  private URI baseUri;

  public MyCallback(final AbstractProviderTest dataProvider, final URI baseUri) {
    this.dataProvider = dataProvider;
    this.baseUri = baseUri;
  }

  @Override
  public WriteFeedCallbackResult retrieveFeedResult(final WriteFeedCallbackContext context) {
    WriteFeedCallbackResult result = new WriteFeedCallbackResult();
    try {
      if ("Rooms".equals(context.getSourceEntitySet().getName())) {
        if ("nr_Employees".equals(context.getNavigationProperty().getName())) {
          HashMap<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
          for (String navPropName : context.getSourceEntitySet().getRelatedEntitySet(context.getNavigationProperty())
              .getEntityType().getNavigationPropertyNames()) {
            callbacks.put(navPropName, this);
          }
          EntityProviderWriteProperties inlineProperties =
              EntityProviderWriteProperties.serviceRoot(baseUri).callbacks(callbacks)
                  .expandSelectTree(context.getCurrentExpandSelectTreeNode())
                  .selfLink(context.getSelfLink())
                  .validatingFacets(context.getCurrentWriteProperties().isValidatingFacets())
                  .build();

          result.setFeedData(dataProvider.getEmployeesData());
          result.setInlineProperties(inlineProperties);
        }
      } else if ("Buildings".equals(context.getSourceEntitySet().getName())) {
        EntityProviderWriteProperties inlineProperties =
            EntityProviderWriteProperties.serviceRoot(baseUri)
                .expandSelectTree(context.getCurrentExpandSelectTreeNode())
                .validatingFacets(context.getCurrentWriteProperties().isValidatingFacets())
                .selfLink(context.getSelfLink()).build();
        List<Map<String, Object>> emptyData = new ArrayList<Map<String, Object>>();
        result.setFeedData(emptyData);
        result.setInlineProperties(inlineProperties);
      }
    } catch (EdmException e) {
      throw new ODataRuntimeException("EdmException", e);
    }
    return result;
  }

  @Override
  public WriteEntryCallbackResult retrieveEntryResult(final WriteEntryCallbackContext context) {
    WriteEntryCallbackResult result = new WriteEntryCallbackResult();
    try {
      if ("Employees".equals(context.getSourceEntitySet().getName())) {
        if ("ne_Room".equals(context.getNavigationProperty().getName())) {
          HashMap<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
          for (String navPropName : context.getSourceEntitySet().getRelatedEntitySet(context.getNavigationProperty())
              .getEntityType().getNavigationPropertyNames()) {
            callbacks.put(navPropName, this);
          }
          EntityProviderWriteProperties inlineProperties =
              EntityProviderWriteProperties.serviceRoot(baseUri).callbacks(callbacks).expandSelectTree(
                  context.getCurrentExpandSelectTreeNode()).build();
          result.setEntryData(dataProvider.getRoomData());
          result.setInlineProperties(inlineProperties);
        } else if ("ne_Team".equals(context.getNavigationProperty().getName())) {
          result.setEntryData(null);
        }
      }
    } catch (EdmException e) {
      throw new ODataRuntimeException("EdmException:", e);
    }
    return result;
  }

}
