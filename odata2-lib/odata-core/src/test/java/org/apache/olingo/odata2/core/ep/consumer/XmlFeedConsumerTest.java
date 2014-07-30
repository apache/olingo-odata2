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
}
