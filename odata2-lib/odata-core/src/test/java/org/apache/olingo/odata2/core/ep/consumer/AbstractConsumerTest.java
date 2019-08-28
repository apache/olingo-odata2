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

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.mock.MockFacade;

/**
 *  
 */
public abstract class AbstractConsumerTest extends BaseTest {

  protected static final EntityProviderReadProperties DEFAULT_PROPERTIES = EntityProviderReadProperties.init()
      .mergeSemantic(false).build();

  protected XMLStreamReader createReaderForTest(final String input) throws XMLStreamException {
    return createReaderForTest(input, false);
  }

  protected XMLStreamReader createReaderForTest(final String input, final boolean namespaceAware)
      throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
    factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, namespaceAware);

    XMLStreamReader streamReader = factory.createXMLStreamReader(new StringReader(input));

    return streamReader;
  }

  protected Map<String, Object> createTypeMappings(final String key, final Object value) {
    Map<String, Object> typeMappings = new HashMap<String, Object>();
    typeMappings.put(key, value);
    return typeMappings;
  }

  protected InputStream getFileAsStream(final String filename) throws IOException {
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    if (in == null) {
      throw new IOException("Requested file '" + filename + "' was not found.");
    }
    return in;
  }

  protected String readFile(final String filename) throws IOException {
    InputStream in = getFileAsStream(filename);

    byte[] tmp = new byte[8192];
    int count = in.read(tmp);
    StringBuilder b = new StringBuilder();
    while (count >= 0) {
      b.append(new String(tmp, 0, count));
      count = in.read(tmp);
    }

    return b.toString();
  }

  /**
   * Create a map with a 'String' to 'Class<?>' mapping based on given parameters.
   * Therefore parameters MUST be a set of such pairs.
   * As example an correct method call would be:
   * <p>
   * <code>
   * createTypeMappings("someKey", Integer.class, "anotherKey", Long.class);
   * </code>
   * </p>
   * 
   * @param firstKeyThenMappingClass
   * @return
   */
  protected Map<String, Object> createTypeMappings(final Object... firstKeyThenMappingClass) {
    Map<String, Object> typeMappings = new HashMap<String, Object>();
    if (firstKeyThenMappingClass.length % 2 != 0) {
      throw new IllegalArgumentException("Got odd number of parameters. Please read javadoc.");
    }
    for (int i = 0; i < firstKeyThenMappingClass.length; i += 2) {
      String key = (String) firstKeyThenMappingClass[i];
      Class<?> mappingClass = (Class<?>) firstKeyThenMappingClass[i + 1];
      typeMappings.put(key, mappingClass);
    }
    return typeMappings;
  }

  protected InputStream createContentAsStream(final String content) throws UnsupportedEncodingException {
    return new ByteArrayInputStream(content.getBytes("UTF-8"));
  }

  /**
   * 
   * @param content
   * @param replaceWhitespaces if <code>true</code> all XML not necessary whitespaces between tags are
   * @return
   * @throws UnsupportedEncodingException
   */
  protected InputStream createContentAsStream(final String content, final boolean replaceWhitespaces)
      throws UnsupportedEncodingException {
    String contentForStream = content;
    if (replaceWhitespaces) {
      contentForStream = content.replaceAll(">\\s.<", "><");
    }

    return new ByteArrayInputStream(contentForStream.getBytes("UTF-8"));
  }

  protected ODataEntry prepareAndExecuteEntry(final String fileName, final String entitySetName,
      final EntityProviderReadProperties readProperties) throws IOException, EdmException, ODataException,
      UnsupportedEncodingException, EntityProviderException {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet(entitySetName);
    String content = readFile(fileName);
    assertNotNull(content);
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataEntry result = xec.readEntry(entitySet, contentBody, readProperties);
    assertNotNull(result);
    return result;
  }

  protected ODataFeed prepareAndExecuteFeed(final String fileName, final String entitySetName,
      final EntityProviderReadProperties readProperties) throws IOException, EdmException, ODataException,
      UnsupportedEncodingException, EntityProviderException {
    // prepare
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet(entitySetName);
    String content = readFile(fileName);
    assertNotNull(content);
    InputStream contentBody = createContentAsStream(content);

    // execute
    JsonEntityConsumer xec = new JsonEntityConsumer();
    ODataFeed result = xec.readFeed(entitySet, contentBody, readProperties);
    assertNotNull(result);
    return result;
  }

  /**
   * @param inlineEntries
   * @param feed
   * @param entry
   * @param edmEntityType 
   * @throws EdmException 
   */
  protected void getExpandedData(Map<String, Object> inlineEntries, 
      ODataEntry entry, EdmType edmType) throws EdmException {
    assertNotNull(entry);
    Map<String, ExpandSelectTreeNode> expandNodes = entry.getExpandSelectTree().getLinks();
    for (Entry<String, ExpandSelectTreeNode> expand : expandNodes.entrySet()) {
      assertNotNull(expand.getKey());
      String keyName = extractKey(entry, (EdmEntityType)edmType, expand);
      if (inlineEntries.containsKey(keyName)) {
        if (inlineEntries.get(keyName) instanceof ODataFeed) {
          ODataFeed innerFeed = (ODataFeed) inlineEntries.get(keyName);
          assertNotNull(innerFeed);
          getExpandedData(inlineEntries, innerFeed,
              ((EdmNavigationProperty)((EdmEntityType)edmType).getProperty(expand.getKey())).getType());
          entry.getProperties().put(expand.getKey(), innerFeed);
        } else if (inlineEntries.get(keyName) instanceof ODataEntry) {
          ODataEntry innerEntry = (ODataEntry) inlineEntries.get(keyName);
          assertNotNull(innerEntry);
          getExpandedData(inlineEntries, innerEntry,
              ((EdmNavigationProperty)((EdmEntityType)edmType).getProperty(expand.getKey())).getType());
          entry.getProperties().put(expand.getKey(), innerEntry);
        }
      }
    }
  }
  
  /**
   * Extract key information to map the parent entry to child entry
   * @param entry
   * @param edmEntityType
   * @param expand
   * @return
   * @throws EdmException
   */
  private String extractKey(ODataEntry entry, EdmEntityType edmEntityType, Entry<String, 
      ExpandSelectTreeNode> expand) throws EdmException {
    return entry.getMetadata().getId() != null ?
        (expand.getKey() + entry.getMetadata().getId()) : 
          (expand.getKey() + edmEntityType.getKeyPropertyNames().get(0) + "=" + 
        entry.getProperties().get(edmEntityType.getKeyPropertyNames().get(0)));
  }

  /**
   * @param inlineEntries
   * @param feed
   * @param entry
   * @throws EdmException 
   */
  protected void getExpandedData(Map<String, Object> inlineEntries, 
      ODataFeed feed, EdmType edmType) throws EdmException {
    assertNotNull(feed.getEntries());
    List<ODataEntry> entries = feed.getEntries();
    for (ODataEntry entry : entries) {
      Map<String, ExpandSelectTreeNode> expandNodes = entry.getExpandSelectTree().getLinks();
      for (Entry<String, ExpandSelectTreeNode> expand : expandNodes.entrySet()) {
        assertNotNull(expand.getKey());
        String keyName = extractKey(entry, (EdmEntityType) edmType, expand);
        if (inlineEntries.containsKey(keyName)) {
          if (inlineEntries.get(keyName) instanceof ODataFeed) {
            ODataFeed innerFeed = (ODataFeed) inlineEntries.get(keyName);
            assertNotNull(innerFeed);
            getExpandedData(inlineEntries, innerFeed,
                ((EdmNavigationProperty)((EdmEntityType)edmType).getProperty(expand.getKey())).getType());
            feed.getEntries().get(feed.getEntries().indexOf(entry)).getProperties().put(expand.getKey(), innerFeed);
          } else if (inlineEntries.get(keyName) instanceof ODataEntry) {
            ODataEntry innerEntry = (ODataEntry) inlineEntries.get(keyName);
            assertNotNull(innerEntry);
            getExpandedData(inlineEntries, innerEntry,
                ((EdmNavigationProperty)((EdmEntityType)edmType).getProperty(expand.getKey())).getType());
            feed.getEntries().get(feed.getEntries().indexOf(entry)).getProperties().put(expand.getKey(), innerEntry);
          }
        }
      }
    }
  }
}
