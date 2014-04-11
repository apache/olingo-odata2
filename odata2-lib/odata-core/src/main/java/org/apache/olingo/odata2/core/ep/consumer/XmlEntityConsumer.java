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

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties.EntityProviderReadPropertiesBuilder;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.core.commons.XmlHelper;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;

/**
 * Xml entity (content type dependent) consumer for reading input (from <code>content</code>).
 * 
 * 
 */
public class XmlEntityConsumer {

  public XmlEntityConsumer() throws EntityProviderException {
    super();
  }

  public ODataDeltaFeed readFeed(final EdmEntitySet entitySet, final InputStream content,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    XMLStreamReader reader = null;
    EntityProviderException cachedException = null;

    try {
      reader = XmlHelper.createStreamReader(content);

      EntityInfoAggregator eia = EntityInfoAggregator.create(entitySet);
      XmlFeedConsumer xfc = new XmlFeedConsumer();
      return xfc.readFeed(reader, eia, properties);
    } catch (EntityProviderException e) {
      cachedException = e;
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (reader != null) {
        try {
          reader.close();
        } catch (XMLStreamException e) {
          if (cachedException != null) {
            throw cachedException;
          } else {
            throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
                .getSimpleName()), e);
          }
        }
      }
    }
  }

  public ODataEntry readEntry(final EdmEntitySet entitySet, final InputStream content,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    XMLStreamReader reader = null;
    EntityProviderException cachedException = null;

    try {
      reader = XmlHelper.createStreamReader(content);
      EntityInfoAggregator eia = EntityInfoAggregator.create(entitySet);

      return new XmlEntryConsumer().readEntry(reader, eia, properties);
    } catch (EntityProviderException e) {
      cachedException = e;
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (reader != null) {
        try {
          reader.close();
        } catch (XMLStreamException e) {
          if (cachedException != null) {
            throw cachedException;
          } else {
            throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
                .getSimpleName()), e);
          }
        }
      }
    }
  }

  public Map<String, Object> readProperty(final EdmProperty edmProperty, final InputStream content,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    XMLStreamReader reader = null;
    EntityProviderException cachedException = null;
    XmlPropertyConsumer xec = new XmlPropertyConsumer();

    try {
      reader = XmlHelper.createStreamReader(content);
      return xec.readProperty(reader, edmProperty, properties.getMergeSemantic(), properties.getTypeMappings(),
          properties);
    } catch (EntityProviderException e) {
      cachedException = e;
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (reader != null) {
        try {
          reader.close();
        } catch (XMLStreamException e) {
          if (cachedException != null) {
            throw cachedException;
          } else {
            throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
                .getSimpleName()), e);
          }
        }
      }
    }
  }

  public Object readPropertyValue(final EdmProperty edmProperty, final InputStream content)
      throws EntityProviderException {
    return readPropertyValue(edmProperty, content, null);
  }

  public Object readPropertyValue(final EdmProperty edmProperty, final InputStream content, final Class<?> typeMapping)
      throws EntityProviderException {
    try {
      final Map<String, Object> result;
      EntityProviderReadPropertiesBuilder propertiesBuilder = EntityProviderReadProperties.init().mergeSemantic(false);
      if (typeMapping == null) {
        result = readProperty(edmProperty, content, propertiesBuilder.build());
      } else {
        Map<String, Object> typeMappings = new HashMap<String, Object>();
        typeMappings.put(edmProperty.getName(), typeMapping);
        result = readProperty(edmProperty, content, propertiesBuilder.addTypeMappings(typeMappings).build());
      }
      return result.get(edmProperty.getName());
    } catch (EdmException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  public String readLink(final EdmEntitySet entitySet, final Object content) throws EntityProviderException {
    XMLStreamReader reader = null;
    EntityProviderException cachedException = null;
    XmlLinkConsumer xlc = new XmlLinkConsumer();

    try {
      reader = XmlHelper.createStreamReader(content);
      return xlc.readLink(reader, entitySet);
    } catch (EntityProviderException e) {
      cachedException = e;
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (reader != null) {
        try {
          reader.close();
        } catch (XMLStreamException e) {
          if (cachedException != null) {
            throw cachedException;
          } else {
            throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
                .getSimpleName()), e);
          }
        }
      }
    }
  }

  public List<String> readLinks(final EdmEntitySet entitySet, final Object content) throws EntityProviderException {
    XMLStreamReader reader = null;
    EntityProviderException cachedException = null;
    XmlLinkConsumer xlc = new XmlLinkConsumer();

    try {
      reader = XmlHelper.createStreamReader(content);
      return xlc.readLinks(reader, entitySet);
    } catch (EntityProviderException e) {
      cachedException = e;
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (reader != null) {
        try {
          reader.close();
        } catch (XMLStreamException e) {
          if (cachedException != null) {
            throw cachedException;
          } else {
            throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
                .getSimpleName()), e);
          }
        }
      }
    }
  }

}
