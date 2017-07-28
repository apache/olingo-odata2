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
package org.apache.olingo.odata2.core.ep.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.EntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.FeedMetadata;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

public class XmlFeedConsumerTest extends AbstractXmlConsumerTest {

  public XmlFeedConsumerTest(final StreamWriterImplType type) {
    super(type);
  }

  @Test
  public void roomsFeedWithEtagEntries() throws Exception {
    InputStream stream = getFileAsStream("feed_rooms_small.xml");
    assertNotNull(stream);

    ODataFeed feed =
        EntityProvider.readFeed("application/atom+xml", MockFacade.getMockEdm().getDefaultEntityContainer()
            .getEntitySet(
                "Rooms"), stream, DEFAULT_PROPERTIES);
    assertNotNull(feed);

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
    assertNotNull(feedMetadata.getNextLink());

    List<ODataEntry> entries = feed.getEntries();
    assertEquals(3, entries.size());
    ODataEntry singleRoom = entries.get(0);
    EntryMetadata roomMetadata = singleRoom.getMetadata();
    assertNotNull(roomMetadata);

    assertEquals("W/\"1\"", roomMetadata.getEtag());
  }

  @Test
  public void readLargeEmployeesFeed() throws Exception {
    InputStream file = getFileAsStream("LargeEmployeeFeed.xml");
    assertNotNull(file);

    ODataFeed feed =
        EntityProvider.readFeed("application/atom+xml", MockFacade.getMockEdm().getDefaultEntityContainer()
            .getEntitySet(
                "Employees"), file, DEFAULT_PROPERTIES);
    assertNotNull(feed);

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
  }

  @Test
  public void readEmployeesFeedWithInlineCountValid() throws Exception {
    // prepare
    String content = readFile("feed_employees_full.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream reqContent = createContentAsStream(content);

    // execute
    XmlEntityConsumer xec = new XmlEntityConsumer();
    EntityProviderReadProperties consumerProperties = EntityProviderReadProperties.init()
        .mergeSemantic(false).build();

    ODataFeed feed = xec.readFeed(entitySet, reqContent, consumerProperties);
    assertNotNull(feed);

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);

    int inlineCount = feedMetadata.getInlineCount();
    // Null means no inlineCount found
    assertNotNull(inlineCount);

    assertEquals(6, inlineCount);
  }

  @Test(expected = EntityProviderException.class)
  public void readEmployeesFeedWithInlineCountNegative() throws Exception {
    // prepare
    String content = readFile("feed_employees_full.xml").replace("<m:count>6</m:count>", "<m:count>-1</m:count>");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream reqContent = createContentAsStream(content);

    // execute
    XmlEntityConsumer xec = new XmlEntityConsumer();
    EntityProviderReadProperties consumerProperties = EntityProviderReadProperties.init()
        .mergeSemantic(false).build();

    try {
      xec.readFeed(entitySet, reqContent, consumerProperties);
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.INLINECOUNT_INVALID, e.getMessageReference());
      throw e;
    }

    Assert.fail("Exception expected");
  }

  @Test(expected = EntityProviderException.class)
  public void readEmployeesFeedWithInlineCountLetters() throws Exception {
    // prepare
    String content = readFile("feed_employees_full.xml").replace("<m:count>6</m:count>", "<m:count>AAA</m:count>");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream reqContent = createContentAsStream(content);

    // execute
    XmlEntityConsumer xec = new XmlEntityConsumer();
    EntityProviderReadProperties consumerProperties = EntityProviderReadProperties.init()
        .mergeSemantic(false).build();

    try {
      xec.readFeed(entitySet, reqContent, consumerProperties);
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.INLINECOUNT_INVALID, e.getMessageReference());
      throw e;
    }

    Assert.fail("Exception expected");
  }

  @Test
  public void readDeltaFeed() throws Exception {
    // prepare
    String content = readFile("feed_with_deleted_entries.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    InputStream reqContent = createContentAsStream(content);

    XmlEntityConsumer xec = new XmlEntityConsumer();
    EntityProviderReadProperties consumerProperties = EntityProviderReadProperties.init().build();

    ODataDeltaFeed deltaFeed = xec.readFeed(entitySet, reqContent, consumerProperties);

    assertNotNull(deltaFeed);

    assertNotNull(deltaFeed.getDeletedEntries());
    assertNotNull(deltaFeed.getEntries());

    assertEquals(1, deltaFeed.getEntries().size());
    assertEquals(1, deltaFeed.getDeletedEntries().size());
  }
  /**
   * Room has an Inline Feed Employees and Employee has an inline Entry Team
   * E.g: Rooms?$expand=nr_Employees/ne_Team
   * Empty Inline entity is also part of payload
   * @throws Exception
   */
  @Test
  public void roomsFeedWithRoomInlineEmployeesWithTeams() throws Exception {
    InputStream stream = getFileAsStream("Rooms_InlineEmployeesTeams.xml");
    assertNotNull(stream);
    FeedCallback callback = new FeedCallback();

    EntityProviderReadProperties readProperties = EntityProviderReadProperties.init()
        .mergeSemantic(false).callback(callback).build();

    ODataFeed feed =
        EntityProvider.readFeed("application/atom+xml", MockFacade.getMockEdm().getDefaultEntityContainer()
            .getEntitySet(
                "Rooms"), stream, readProperties);
    assertNotNull(feed);
    assertEquals(3, feed.getEntries().size());

    Map<String, Object> inlineEntries = callback.getNavigationProperties();
    getExpandedData(inlineEntries, feed);
    for (ODataEntry entry : feed.getEntries()) {
      assertEquals(5, entry.getProperties().size());
      for (ODataEntry innerEntry : ((ODataFeed)entry.getProperties().get("nr_Employees")).getEntries()) {
        assertEquals(10, innerEntry.getProperties().size());
        assertEquals(3, ((ODataEntry)innerEntry.getProperties().get("ne_Team")).getProperties().size());
      }
    }
  }
  
  /**
   * Rooms has an inline feed Employees and Rooms has Inline entry Buildings
   * E.g: Rooms?$expand=nr_Employees,nr_Building
   * @throws Exception
   */
  @Test
  public void roomsFeedWithRoomInlineEmployeesInlineBuildings() throws Exception {
    InputStream stream = getFileAsStream("Rooms_InlineEmployees_InlineBuildings.xml");
    assertNotNull(stream);
    FeedCallback callback = new FeedCallback();

    EntityProviderReadProperties readProperties = EntityProviderReadProperties.init()
        .mergeSemantic(false).callback(callback).build();

    ODataFeed feed =
        EntityProvider.readFeed("application/atom+xml", MockFacade.getMockEdm().getDefaultEntityContainer()
            .getEntitySet(
                "Rooms"), stream, readProperties);
    assertNotNull(feed);
    assertEquals(2, feed.getEntries().size());

    Map<String, Object> inlineEntries = callback.getNavigationProperties();
    getExpandedData(inlineEntries, feed);
    for (ODataEntry entry : feed.getEntries()) {
      assertEquals(6, entry.getProperties().size());
      for (ODataEntry employeeEntry : ((ODataFeed)entry.getProperties().get("nr_Employees")).getEntries()) {
        assertEquals(9, employeeEntry.getProperties().size());
      }
      assertEquals(3, ((ODataEntry)entry.getProperties().get("nr_Building")).getProperties().size());
    }
  }
  
  /**
   * Rooms navigate to Employees and has inline entry Teams
   * E.g: Rooms('1')/nr_Employees?$expand=ne_Team
   * @throws Exception
   */
  @Test
  public void roomsFeedWithRoomsToEmployeesInlineTeams() throws Exception {
    InputStream stream = getFileAsStream("RoomsToEmployeesWithInlineTeams.xml");
    assertNotNull(stream);
    FeedCallback callback = new FeedCallback();

    EntityProviderReadProperties readProperties = EntityProviderReadProperties.init()
        .mergeSemantic(false).callback(callback).build();

    ODataFeed feed =
        EntityProvider.readFeed("application/atom+xml", MockFacade.getMockEdm().getDefaultEntityContainer()
            .getEntitySet(
                "Employees"), stream, readProperties);
    assertNotNull(feed);
    assertEquals(2, feed.getEntries().size());

    Map<String, Object> inlineEntries = callback.getNavigationProperties();
    getExpandedData(inlineEntries, feed);
    for (ODataEntry entry : feed.getEntries()) {
      assertEquals(10, entry.getProperties().size());
      assertEquals(3, ((ODataEntry)entry.getProperties().get("ne_Team")).getProperties().size());
    }
  }
  
  /**
   * @param inlineEntries
   * @param feed
   * @param entry
   */
  private void getExpandedData(Map<String, Object> inlineEntries, ODataEntry entry) {
    assertNotNull(entry);
    Map<String, ExpandSelectTreeNode> expandNodes = entry.getExpandSelectTree().getLinks();
    for (Entry<String, ExpandSelectTreeNode> expand : expandNodes.entrySet()) {
      assertNotNull(expand.getKey());
      if (inlineEntries.containsKey(expand.getKey() + entry.getMetadata().getId())) {
        if (inlineEntries.get(expand.getKey() + entry.getMetadata().getId()) instanceof ODataFeed) {
          ODataFeed innerFeed = (ODataFeed) inlineEntries.get(expand.getKey() + entry.getMetadata().getId());
          assertNotNull(innerFeed);
          getExpandedData(inlineEntries, innerFeed);
          entry.getProperties().put(expand.getKey(), innerFeed);
        } else if (inlineEntries.get(expand.getKey() + entry.getMetadata().getId()) instanceof ODataEntry) {
          ODataEntry innerEntry = (ODataEntry) inlineEntries.get(expand.getKey() + entry.getMetadata().getId());
          assertNotNull(innerEntry);
          getExpandedData(inlineEntries, innerEntry);
          entry.getProperties().put(expand.getKey(), innerEntry);
        }
      }
    }
  }
  
  /**
   * @param inlineEntries
   * @param feed
   * @param entry
   */
  private void getExpandedData(Map<String, Object> inlineEntries, ODataFeed feed) {
    assertNotNull(feed.getEntries());
    List<ODataEntry> entries = feed.getEntries();
    for (ODataEntry entry : entries) {
      Map<String, ExpandSelectTreeNode> expandNodes = entry.getExpandSelectTree().getLinks();
      for (Entry<String, ExpandSelectTreeNode> expand : expandNodes.entrySet()) {
        assertNotNull(expand.getKey());
        if (inlineEntries.containsKey(expand.getKey() + entry.getMetadata().getId())) {
          if (inlineEntries.get(expand.getKey() + entry.getMetadata().getId()) instanceof ODataFeed) {
            ODataFeed innerFeed = (ODataFeed) inlineEntries.get(expand.getKey() + entry.getMetadata().getId());
            assertNotNull(innerFeed);
            getExpandedData(inlineEntries, innerFeed);
            feed.getEntries().get(feed.getEntries().indexOf(entry)).getProperties().put(expand.getKey(), innerFeed);
          } else if (inlineEntries.get(expand.getKey() + entry.getMetadata().getId()) instanceof ODataEntry) {
            ODataEntry innerEntry = (ODataEntry) inlineEntries.get(expand.getKey() + entry.getMetadata().getId());
            assertNotNull(innerEntry);
            getExpandedData(inlineEntries, innerEntry);
            feed.getEntries().get(feed.getEntries().indexOf(entry)).getProperties().put(expand.getKey(), innerEntry);
          }
        }
      }
    }
  }
}
