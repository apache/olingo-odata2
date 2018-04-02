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

import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.client.api.ep.EntityStream;
import org.apache.olingo.odata2.core.commons.XmlHelper;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;

/**
 * Xml entity (content type dependent) consumer for reading input (from <code>content</code>).
 * 
 * 
 */
public class XmlEntityDeserializer {

  /**
   * 
   * @throws EntityProviderException
   */
  public XmlEntityDeserializer() throws EntityProviderException {
    super();
  }

  /**
   * Returns an ODataDeltaFeed deserializing EntityStream
   * @param entitySet
   * @param entity
   * @return ODataDeltaFeed
   * @throws EntityProviderException
   */
  public ODataDeltaFeed readFeed(final EdmEntitySet entitySet, final EntityStream entity)
      throws EntityProviderException {
    XMLStreamReader reader = null;
    EntityProviderException cachedException = null;

    try {
      reader = XmlHelper.createStreamReader(entity.getContent());

      EntityInfoAggregator eia = EntityInfoAggregator.create(entitySet);
      XmlFeedDeserializer xfc = new XmlFeedDeserializer();
      return xfc.readFeed(reader, eia, entity.getReadProperties());
    } catch (EntityProviderException e) {
      cachedException = e;
      throw cachedException;
    } finally { //NOPMD  - suppressed
      if (reader != null) {
        try {
          reader.close();
        } catch (XMLStreamException e) {
          if (cachedException != null) {
            throw cachedException; //NOSONAR
          } else {
            throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.//NOSONAR
                addContent(e.getClass() 
                .getSimpleName()), e);
          }
        }
      }
    }
  }

  /**
   * Returns an ODataEntry deserializing EntityStream
   * @param entitySet
   * @param entity
   * @return ODataEntry
   * @throws EntityProviderException
   */
  public ODataEntry readEntry(final EdmEntitySet entitySet, final EntityStream entity)
      throws EntityProviderException {
    XMLStreamReader reader = null;
    EntityProviderException cachedException = null;

    try {
      reader = XmlHelper.createStreamReader(entity.getContent());
      EntityInfoAggregator eia = EntityInfoAggregator.create(entitySet);

      return new XmlEntryDeserializer().readEntry(reader, eia, entity.getReadProperties(), false);
    } catch (EntityProviderException e) {
      cachedException = e;
      throw cachedException;
    } finally {//NOPMD  - suppressed
      if (reader != null) {
        try {
          reader.close();
        } catch (XMLStreamException e) {
          if (cachedException != null) {
            throw cachedException; //NOSONAR
          } else {
            throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.//NOSONAR
                addContent(e.getClass() 
                .getSimpleName()), e); 
          }
        }
      }
    }
  }
  
  /**
   * 
   * @param info
   * @param entityStream
   * @return
   * @throws EntityProviderException
   */
  public Object readCollection(final EntityPropertyInfo info, EntityStream entityStream) 
      throws EntityProviderException {
    XMLStreamReader reader = null;
    EntityProviderException cachedException = null;
    try {
      reader = XmlHelper.createStreamReader(entityStream.getContent());
      return new XmlPropertyDeserializer().readCollection(reader, info, entityStream.getReadProperties());
    } catch (final EntityProviderException e) {
      cachedException = e;
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (reader != null) {
        try {
          reader.close();
        } catch (final XMLStreamException e) {
          if (cachedException != null) {
            throw cachedException;
          } else {
            throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED
                .addContent(e.getClass().getSimpleName()), e);
          }
        }
      }
    }
  }
  
  /**
   * 
   * @param propertyInfo
   * @param entityStream
   * @return
   * @throws EntityProviderException
   */
  public Map<String, Object> readProperty(final EntityPropertyInfo propertyInfo, final EntityStream entityStream)
      throws EntityProviderException {
    XMLStreamReader reader = null;
    EntityProviderException cachedException = null;
    try {
      reader = XmlHelper.createStreamReader(entityStream.getContent());
      return new XmlPropertyDeserializer().readProperty(reader, propertyInfo, entityStream.getReadProperties());
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
