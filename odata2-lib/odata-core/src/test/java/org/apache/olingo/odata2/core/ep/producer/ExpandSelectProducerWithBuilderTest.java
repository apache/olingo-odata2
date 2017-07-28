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

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteEntryContent;
import org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackResult;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.core.ep.AbstractProviderTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

public class ExpandSelectProducerWithBuilderTest extends AbstractProviderTest {

  public class LocalCallback implements OnWriteEntryContent {

    @Override
    public WriteEntryCallbackResult retrieveEntryResult(final WriteEntryCallbackContext context)
        throws ODataApplicationException {
      WriteEntryCallbackResult writeEntryCallbackResult = new WriteEntryCallbackResult();
      EntityProviderWriteProperties inlineProperties =
          EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).expandSelectTree(
              context.getCurrentExpandSelectTreeNode()).build();
      writeEntryCallbackResult.setInlineProperties(inlineProperties);
      Map<String, Object> buildingData = new HashMap<String, Object>();
      buildingData.put("Id", "1");
      buildingData.put("Name", "BuildingName");
      writeEntryCallbackResult.setEntryData(buildingData);
      return writeEntryCallbackResult;
    }

  }

  public ExpandSelectProducerWithBuilderTest(final StreamWriterImplType type) {
    super(type);
  }

  @Test
  public void selectOnlyProperties() throws Exception {
    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    List<String> selectedProperties = new ArrayList<String>(roomData.keySet());

    ExpandSelectTreeNode expandSelectTree =
        ExpandSelectTreeNode.entitySet(roomsSet).selectedProperties(selectedProperties).build();

    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).expandSelectTree(expandSelectTree).build();
    ODataResponse entry = EntityProvider.writeEntry("application/xml", roomsSet, roomData, properties);

    String xml = StringHelper.inputStreamToString((InputStream) entry.getEntity());
    assertXpathExists("/a:entry/a:content/m:properties", xml);
    assertXpathNotExists("/a:entry/a:link[@type]", xml);
  }

  @Test
  public void selectOnlyLinks() throws Exception {
    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    List<String> selectedNavigationProperties = new ArrayList<String>();
    selectedNavigationProperties.add("nr_Building");
    selectedNavigationProperties.add("nr_Employees");

    ExpandSelectTreeNode expandSelectTree =
        ExpandSelectTreeNode.entitySet(roomsSet).selectedLinks(selectedNavigationProperties).build();

    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).expandSelectTree(expandSelectTree).build();
    ODataResponse entry = EntityProvider.writeEntry("application/xml", roomsSet, roomData, properties);

    String xml = StringHelper.inputStreamToString((InputStream) entry.getEntity());
    assertXpathNotExists("/a:entry/a:content/m:properties", xml);
    assertXpathExists("/a:entry/a:link[@type]", xml);
  }

  @Test
  public void selectIdAndBuildingLink() throws Exception {
    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    List<String> selectedNavigationProperties = new ArrayList<String>();
    selectedNavigationProperties.add("nr_Building");

    List<String> selectedProperties = new ArrayList<String>();
    selectedProperties.add("Id");

    ExpandSelectTreeNode expandSelectTree =
        ExpandSelectTreeNode.entitySet(roomsSet).selectedProperties(selectedProperties).selectedLinks(
            selectedNavigationProperties).build();

    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).expandSelectTree(expandSelectTree).build();
    ODataResponse entry = EntityProvider.writeEntry("application/xml", roomsSet, roomData, properties);

    String xml = StringHelper.inputStreamToString((InputStream) entry.getEntity());
    assertXpathExists("/a:entry/a:content/m:properties", xml);
    assertXpathExists("/a:entry/a:link[@type]", xml);
  }

  @Test
  public void expandBuilding() throws Exception {
    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    List<String> expandedNavigationProperties = new ArrayList<String>();
    expandedNavigationProperties.add("nr_Building");

    ExpandSelectTreeNode expandSelectTree =
        ExpandSelectTreeNode.entitySet(roomsSet).expandedLinks(expandedNavigationProperties).build();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", new LocalCallback());
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).callbacks(callbacks).expandSelectTree(
            expandSelectTree).build();
    ODataResponse entry = EntityProvider.writeEntry("application/xml", roomsSet, roomData, properties);

    String xml = StringHelper.inputStreamToString((InputStream) entry.getEntity());
    assertXpathExists("/a:entry/a:content/m:properties", xml);
    assertXpathExists("/a:entry/a:link[@type]/m:inline", xml);
  }

  @Test
  public void expandBuildingAndRooms() throws Exception {
    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    List<String> expandedNavigationProperties = new ArrayList<String>();
    expandedNavigationProperties.add("nr_Building");

    final ExpandSelectTreeNode expandSelectTree =
            ExpandSelectTreeNode.entitySet(roomsSet).expandedLinks(expandedNavigationProperties).build();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", new OnWriteEntryContent(){
      @Override
      public WriteEntryCallbackResult retrieveEntryResult(final WriteEntryCallbackContext context)
              throws ODataApplicationException {
        WriteEntryCallbackResult writeEntryCallbackResult = new WriteEntryCallbackResult();

        ExpandSelectTreeNode innerExpandSelectTree;
        try {
          EdmEntitySet buildingsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Buildings");
          innerExpandSelectTree = ExpandSelectTreeNode.entitySet(buildingsSet)
                          .expandedLinks(Arrays.asList("nb_Rooms")).build();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

        Map<String, ODataCallback> innerCallbacks = new HashMap<String, ODataCallback>();
        innerCallbacks.put("nb_Rooms", new OnWriteFeedContent(){
          @SuppressWarnings("unchecked")
          @Override
          public WriteFeedCallbackResult retrieveFeedResult(WriteFeedCallbackContext context) {
            WriteFeedCallbackResult writeEntryCallbackResult = new WriteFeedCallbackResult();

            ExpandSelectTreeNode innerExpandSelectTree = context.getCurrentExpandSelectTreeNode();
            EntityProviderWriteProperties inlineProperties =
                    EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES)
                            .expandSelectTree(innerExpandSelectTree).build();
            writeEntryCallbackResult.setInlineProperties(inlineProperties);
            Map<String, Object> roomsData = new HashMap<String, Object>();
            roomsData.put("Id", "1");
            roomsData.put("Name", "MyInnerRoom");
            writeEntryCallbackResult.setFeedData(Arrays.asList(roomsData));
            return writeEntryCallbackResult;
          }
        });

        EntityProviderWriteProperties inlineProperties =
                EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES)
                        .callbacks(innerCallbacks)
                        .expandSelectTree(innerExpandSelectTree).build();
        writeEntryCallbackResult.setInlineProperties(inlineProperties);
        Map<String, Object> buildingData = new HashMap<String, Object>();
        buildingData.put("Id", "1");
        buildingData.put("Name", "BuildingName");
        writeEntryCallbackResult.setEntryData(buildingData);
        return writeEntryCallbackResult;
      }
    });

    EntityProviderWriteProperties properties =
            EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).callbacks(callbacks).expandSelectTree(
                    expandSelectTree).build();
    ODataResponse entry = EntityProvider.writeEntry("application/xml", roomsSet, roomData, properties);

    String xml = StringHelper.inputStreamToString((InputStream) entry.getEntity());
    assertXpathExists("/a:entry/a:content/m:properties", xml);
    assertXpathExists("/a:entry/a:link[@type]/m:inline", xml);
    assertXpathEvaluatesTo("Buildings", "/a:entry/a:link[@type]/m:inline/a:entry/a:title", xml);
    assertXpathEvaluatesTo("Rooms",
            "/a:entry/a:link[@type]/m:inline/a:entry/a:link[@type]/m:inline/a:feed/a:title", xml);
    assertXpathEvaluatesTo("http://host:80/service/Rooms('1')",
            "/a:entry/a:link[@type]/m:inline/a:entry/a:link[@type]/m:inline/a:feed/a:entry/a:id", xml);
  }

  @Test
  public void expandBuildingAndSelectIdFromRoom() throws Exception {
    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    List<String> expandedNavigationProperties = new ArrayList<String>();
    expandedNavigationProperties.add("nr_Building");

    List<String> selectedProperties = new ArrayList<String>();
    selectedProperties.add("Id");

    ExpandSelectTreeNode expandSelectTree =
        ExpandSelectTreeNode.entitySet(roomsSet).selectedProperties(selectedProperties).expandedLinks(
            expandedNavigationProperties).build();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", new LocalCallback());
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).callbacks(callbacks).expandSelectTree(
            expandSelectTree).build();
    ODataResponse entry = EntityProvider.writeEntry("application/xml", roomsSet, roomData, properties);

    String xml = StringHelper.inputStreamToString((InputStream) entry.getEntity());
    assertXpathExists("/a:entry/a:content/m:properties/d:Id", xml);
    assertXpathNotExists("/a:entry/a:content/m:properties/d:Name", xml);
    assertXpathExists("/a:entry/a:link[@type]/m:inline", xml);
  }

  @Test
  public void customExpandBuildingAndSelectIdFromCustomNode() throws Exception {
    EdmEntitySet roomsSet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    List<String> selectedPropertiesAtCustomProperties = new ArrayList<String>();
    selectedPropertiesAtCustomProperties.add("Id");
    EdmEntitySet buildingsSet =
        roomsSet.getRelatedEntitySet((EdmNavigationProperty) roomsSet.getEntityType().getProperty("nr_Building"));
    ExpandSelectTreeNode customNode =
        ExpandSelectTreeNode.entitySet(buildingsSet).selectedProperties(selectedPropertiesAtCustomProperties).build();

    ExpandSelectTreeNode expandSelectTree =
        ExpandSelectTreeNode.entitySet(roomsSet).customExpandedLink("nr_Building", customNode).build();

    Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
    callbacks.put("nr_Building", new LocalCallback());
    EntityProviderWriteProperties properties =
        EntityProviderWriteProperties.fromProperties(DEFAULT_PROPERTIES).callbacks(callbacks).expandSelectTree(
            expandSelectTree).build();
    ODataResponse entry = EntityProvider.writeEntry("application/xml", roomsSet, roomData, properties);

    String xml = StringHelper.inputStreamToString((InputStream) entry.getEntity());
    assertXpathExists("/a:entry/a:content/m:properties", xml);
    assertXpathExists("/a:entry/a:link[@type]/m:inline", xml);
    assertXpathExists("/a:entry/a:link[@type]/m:inline/a:entry/a:content/m:properties/d:Id", xml);
    assertXpathNotExists("/a:entry/a:link[@type]/m:inline/a:entry/a:content/m:properties/d:Name", xml);
  }

}
