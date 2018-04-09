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
package org.apache.olingo.odata2.client.core.ep.deserializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.FeedMetadata;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.client.api.edm.ClientEdm;
import org.apache.olingo.odata2.client.api.edm.EdmDataServices;
import org.apache.olingo.odata2.client.api.ep.ContentTypeBasedDeserializer;
import org.apache.olingo.odata2.client.api.ep.DeserializerProperties;
import org.apache.olingo.odata2.client.api.ep.EntityStream;
import org.apache.olingo.odata2.client.core.ODataClientImpl;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

import junit.framework.Assert;

public class XmlFeedDeserializerTest extends AbstractXmlDeserializerTest {

  public XmlFeedDeserializerTest(final StreamWriterImplType type) {
    super(type);
  }

  

  @Test
  public void readLargeEmployeesFeed() throws Exception {
    InputStream file = getFileAsStream("LargeEmployeeFeed.xml");
    assertNotNull(file);
    EntityStream es = new EntityStream();
    es.setContent(file);
    es.setReadProperties(DEFAULT_PROPERTIES);
    ODataClientImpl client = new ODataClientImpl();
    ContentTypeBasedDeserializer deserializer = client.createDeserializer("application/atom+xml");
    ODataFeed feed =
        deserializer.readFeed( MockFacade.getMockEdm().getDefaultEntityContainer()
            .getEntitySet(
                "Employees"), es);
    assertNotNull(feed);

    FeedMetadata feedMetadata = feed.getFeedMetadata();
    assertNotNull(feedMetadata);
  }

  private InputStream createStreamReader(final String xml) throws
  XMLStreamException, UnsupportedEncodingException {
    return new ByteArrayInputStream(xml.getBytes("UTF-8"));
  }
  
  @Test
  public void readProductsFeedEndToEnd() throws Exception {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    String xml = readFile("metadataProducts.xml");
    InputStream reader = createStreamReader(xml);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals(1, result.getEdm().getSchemas().size());
    ClientEdm edm = result.getEdm();
    InputStream file = getFileAsStream("ProductsFeed.xml");
    assertNotNull(file);
    EntityStream es = new EntityStream();
    es.setContent(file);
    es.setReadProperties(DEFAULT_PROPERTIES);
    ODataClientImpl client = new ODataClientImpl();
    ContentTypeBasedDeserializer deserializer = client.createDeserializer("application/atom+xml");
    ODataFeed feed =
        deserializer.readFeed( edm.getEntitySets().get(0), es);
    assertNotNull(feed);
    
    List<ODataEntry> oDataEntries = feed.getEntries();
    
    for (ODataEntry entry : oDataEntries) {
      assertEquals(6, entry.getProperties().size());
      assertEquals(4, ((ODataEntry)entry.getProperties().get("Supplier")).getProperties().size());
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
    EntityStream es = new EntityStream();
    es.setContent(stream);
    es.setReadProperties(DEFAULT_PROPERTIES);
    ODataClientImpl client = new ODataClientImpl();
    ContentTypeBasedDeserializer deserializer = client.createDeserializer("application/atom+xml");
    ODataFeed feed =
        deserializer.readFeed( MockFacade.getMockEdm().getDefaultEntityContainer()
            .getEntitySet(
                "Employees"), es);
    assertNotNull(feed);
    assertEquals(2, feed.getEntries().size());

    for (ODataEntry entry : feed.getEntries()) {
      assertEquals(10, entry.getProperties().size());
      assertEquals(3, ((ODataEntry)entry.getProperties().get("ne_Team")).getProperties().size());
    }
  }
 
  @Test
  public void readEmployeesFeedWithInlineCountValid() throws Exception {
    // prepare
    String content = readFile("feed_employees_full.xml");
    assertNotNull(content);

    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
    InputStream reqContent = createContentAsStream(content);

    // execute
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    DeserializerProperties consumerProperties = DeserializerProperties.init()
        .build();

    EntityStream es = new EntityStream();
    es.setContent(reqContent);
    es.setReadProperties(consumerProperties);
    
    ODataFeed feed = xec.readFeed(entitySet, es);
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
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    DeserializerProperties consumerProperties = DeserializerProperties.init()
        .build();

    EntityStream es = new EntityStream();
    es.setContent(reqContent);
    es.setReadProperties(consumerProperties);
    try {
      xec.readFeed(entitySet,  es);
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
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    DeserializerProperties consumerProperties = DeserializerProperties.init()
        .build();

    EntityStream es = new EntityStream();
    es.setContent(reqContent);
    es.setReadProperties(consumerProperties);
    try {
      xec.readFeed(entitySet, es);
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
    EntityStream stream = new EntityStream();
    XmlEntityDeserializer xec = new XmlEntityDeserializer();
    DeserializerProperties consumerProperties = DeserializerProperties.init().build();
    stream.setContent(reqContent);
    stream.setReadProperties(consumerProperties);
    ODataDeltaFeed deltaFeed = xec.readFeed(entitySet, stream);

    assertNotNull(deltaFeed);

    assertNotNull(deltaFeed.getDeletedEntries());
    assertNotNull(deltaFeed.getEntries());

    assertEquals(1, deltaFeed.getEntries().size());
    assertEquals(1, deltaFeed.getDeletedEntries().size());
  }
}
