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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties.ODataEntityProviderPropertiesBuilder;
import org.apache.olingo.odata2.api.ep.callback.OnWriteEntryContent;
import org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent;
import org.apache.olingo.odata2.api.ep.callback.WriteCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackResult;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.api.uri.NavigationPropertySegment;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAEntityParser;

public class JPAExpandCallBack implements OnWriteFeedContent, OnWriteEntryContent, ODataCallback {

  private URI baseUri;
  private List<ArrayList<NavigationPropertySegment>> expandList;
  private EdmEntitySet nextEntitySet = null;
  private HashMap<String, List<EdmProperty>> edmPropertyMap = new HashMap<String, List<EdmProperty>>();

  private JPAExpandCallBack(final URI baseUri, final List<ArrayList<NavigationPropertySegment>> expandList) {
    super();
    this.baseUri = baseUri;
    this.expandList = expandList;
  }

  @Override
  public WriteEntryCallbackResult retrieveEntryResult(final WriteEntryCallbackContext context)
      throws ODataApplicationException {
    WriteEntryCallbackResult result = new WriteEntryCallbackResult();
    Map<String, Object> entry = context.getEntryData();
    Map<String, Object> edmPropertyValueMap = null;
    List<EdmNavigationProperty> currentNavPropertyList = null;
    Map<String, ExpandSelectTreeNode> navigationLinks = null;
    JPAEntityParser jpaResultParser = new JPAEntityParser();
    EdmNavigationProperty currentNavigationProperty = context.getNavigationProperty();
    try {
      Object inlinedEntry = entry.get(currentNavigationProperty.getName());
      if (nextEntitySet == null) {
        nextEntitySet = context.getSourceEntitySet().getRelatedEntitySet(currentNavigationProperty);
      }
      edmPropertyValueMap = jpaResultParser.parse2EdmPropertyValueMap(inlinedEntry, nextEntitySet.getEntityType());
      result.setEntryData(edmPropertyValueMap);
      navigationLinks = context.getCurrentExpandSelectTreeNode().getLinks();
      if (navigationLinks.size() > 0) {
        currentNavPropertyList = new ArrayList<EdmNavigationProperty>();
        List<EdmNavigationProperty> nextNavProperty =
            getNextNavigationProperty(context.getSourceEntitySet().getEntityType(), context.getNavigationProperty());
        if (nextNavProperty != null) {
          currentNavPropertyList.addAll(nextNavProperty);
        }
        HashMap<String, Object> navigationMap =
            jpaResultParser.parse2EdmNavigationValueMap(inlinedEntry, currentNavPropertyList);
        if (edmPropertyValueMap != null) {
          edmPropertyValueMap.putAll(navigationMap);
        }
        result.setEntryData(edmPropertyValueMap);
      }
      result.setInlineProperties(getInlineEntityProviderProperties(context));
    } catch (EdmException e) {
      throw new ODataApplicationException(e.getMessage(), Locale.getDefault(), e);
    } catch (ODataJPARuntimeException e) {
      throw new ODataApplicationException(e.getMessage(), Locale.getDefault(), e);
    }

    return result;
  }

  private List<EdmProperty> getEdmProperties(final EdmEntitySet entitySet, final ExpandSelectTreeNode expandTreeNode)
      throws ODataApplicationException {

    try {
      String name = entitySet.getName();
      if (edmPropertyMap.containsKey(name)) {
        return edmPropertyMap.get(name);
      }
      List<EdmProperty> edmProperties = new ArrayList<EdmProperty>();
      edmProperties.addAll(expandTreeNode.getProperties());
      boolean hit = false;
      for (EdmProperty keyProperty : entitySet.getEntityType().getKeyProperties()) {
        hit = false;
        for (EdmProperty property : edmProperties) {
          if (property.getName().equals(keyProperty.getName())) {
            hit = true;
            break;
          }
        }
        if (hit == false) {
          edmProperties.add(keyProperty);
        }
      }
      edmPropertyMap.put(name, edmProperties);
      return edmProperties;
    } catch (EdmException e) {
      throw new ODataApplicationException(e.getMessage(), Locale.getDefault(), e);
    }
  }

  @Override
  public WriteFeedCallbackResult retrieveFeedResult(final WriteFeedCallbackContext context)
      throws ODataApplicationException {
    WriteFeedCallbackResult result = new WriteFeedCallbackResult();
    HashMap<String, Object> inlinedEntry = (HashMap<String, Object>) context.getEntryData();
    List<Map<String, Object>> edmEntityList = new ArrayList<Map<String, Object>>();
    JPAEntityParser jpaResultParser = new JPAEntityParser();
    List<EdmNavigationProperty> currentNavPropertyList = null;
    EdmNavigationProperty currentNavigationProperty = context.getNavigationProperty();
    ExpandSelectTreeNode currentExpandTreeNode = context.getCurrentExpandSelectTreeNode();

    try {
      @SuppressWarnings({ "unchecked" })
      Collection<Object> listOfItems = (Collection<Object>) inlinedEntry.get(context.getNavigationProperty().getName());
      if (nextEntitySet == null) {
        nextEntitySet = context.getSourceEntitySet().getRelatedEntitySet(currentNavigationProperty);
      }
      if (!currentExpandTreeNode.getProperties().isEmpty()) {
        edmEntityList =
            jpaResultParser.parse2EdmEntityList(listOfItems, getEdmProperties(nextEntitySet,
                currentExpandTreeNode));
      } else {
        edmEntityList = jpaResultParser.parse2EdmEntityList(listOfItems, nextEntitySet.getEntityType());
      }
      result.setFeedData(edmEntityList);

      if (currentExpandTreeNode.getLinks().size() > 0) {
        currentNavPropertyList = new ArrayList<EdmNavigationProperty>();
        List<EdmNavigationProperty> nextNavPropertyList =
            getNextNavigationProperty(context.getSourceEntitySet().getEntityType(), context.getNavigationProperty());
        if (nextNavPropertyList != null) {
          currentNavPropertyList.addAll(nextNavPropertyList);
        }
        int count = 0;
        for (Object object : listOfItems) {
          HashMap<String, Object> navigationMap =
              jpaResultParser.parse2EdmNavigationValueMap(object, currentNavPropertyList);
          edmEntityList.get(count).putAll(navigationMap);
          count++;
        }
        result.setFeedData(edmEntityList);
      }
      result.setInlineProperties(getInlineEntityProviderProperties(context));
    } catch (EdmException e) {
      throw new ODataApplicationException(e.getMessage(), Locale.getDefault(), e);
    } catch (ODataJPARuntimeException e) {
      throw new ODataApplicationException(e.getMessage(), Locale.getDefault(), e);
    }
    return result;
  }

  private List<EdmNavigationProperty> getNextNavigationProperty(final EdmEntityType sourceEntityType,
      final EdmNavigationProperty navigationProperty) throws EdmException {
    final List<EdmNavigationProperty> edmNavigationPropertyList = new ArrayList<EdmNavigationProperty>();
    for (ArrayList<NavigationPropertySegment> navPropSegments : expandList) {
      int size = navPropSegments.size();
      for (int i = 0; i < size; i++) {
        EdmNavigationProperty navProperty = navPropSegments.get(i).getNavigationProperty();
        if (testNavPropertySegment(navProperty, sourceEntityType, navigationProperty)) {
          if (i < size - 1) {
            edmNavigationPropertyList.add(navPropSegments.get(i + 1).getNavigationProperty());
          }
        }
      }
    }
    return edmNavigationPropertyList;
  }

  private boolean testNavPropertySegment(
		  final EdmNavigationProperty navProperty,
		  final EdmEntityType sourceEntityType,
		  final EdmNavigationProperty navigationProperty) throws EdmException {
	  if(navigationProperty.getFromRole().toLowerCase(Locale.ENGLISH).startsWith(
        sourceEntityType.getName().toLowerCase(Locale.ENGLISH))) {
		  final String roleNum = 
				  navigationProperty.getFromRole().substring(sourceEntityType.getName().length());
		  if(roleNum.length() > 0) {
			  try {
				  Integer.parseInt(roleNum);
			  } catch (NumberFormatException e) {
				  return false;
			  }
		  }
	  }
	  return navProperty.getName().equals(navigationProperty.getName());
  }
  
  public static Map<String, ODataCallback> getCallbacks(final URI baseUri,
      final ExpandSelectTreeNode expandSelectTreeNode, final List<ArrayList<NavigationPropertySegment>> expandList)
      throws EdmException {
    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();

    for (String navigationPropertyName : expandSelectTreeNode.getLinks().keySet()) {
      callbacks.put(navigationPropertyName, new JPAExpandCallBack(baseUri, expandList));
    }

    return callbacks;

  }

  private EntityProviderWriteProperties getInlineEntityProviderProperties(final WriteCallbackContext context)
      throws EdmException {
    ODataEntityProviderPropertiesBuilder propertiesBuilder = EntityProviderWriteProperties.serviceRoot(baseUri);
    propertiesBuilder.callbacks(getCallbacks(baseUri, context.getCurrentExpandSelectTreeNode(), expandList));
    propertiesBuilder.expandSelectTree(context.getCurrentExpandSelectTreeNode());
    return propertiesBuilder.build();
  }

}
