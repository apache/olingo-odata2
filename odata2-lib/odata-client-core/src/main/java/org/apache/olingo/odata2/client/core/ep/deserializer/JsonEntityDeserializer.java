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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.client.api.ep.EntityStream;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;

import com.google.gson.stream.JsonReader;

/**
 *  This class includes method for deserialization of feed and entry data
 */
public class JsonEntityDeserializer {

  /** Default used charset for reader */
  private static final String DEFAULT_CHARSET = "UTF-8";

  /**
   * Returns an ODataEntry deserializing EntityStream
   * @param entitySet
   * @param entityStream
   * @return ODataEntry
   * @throws EntityProviderException
   */
  public ODataEntry readEntry(final EdmEntitySet entitySet, final EntityStream entityStream) 
      throws EntityProviderException {
    JsonReader reader = null;
    EntityProviderException cachedException = null;

    try {
      EntityInfoAggregator eia = EntityInfoAggregator.create(entitySet);
      reader = createJsonReader(entityStream.getContent());

      return new JsonEntryDeserializer(reader, eia, entityStream.getReadProperties()).readSingleEntry();
    } catch (UnsupportedEncodingException e) {
      cachedException =
          new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
              .getSimpleName()), e);
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) { //NOPMD  - suppressed
          if (cachedException != null) { //NOSONAR
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
   * Returns an ODataFeed deserializing EntityStream
   * @param entitySet
   * @param entityStream
   * @return ODataFeed
   * @throws EntityProviderException
   */
  public ODataFeed readFeed(final EdmEntitySet entitySet, final EntityStream entityStream) 
      throws EntityProviderException {
    return readDeltaFeed(entitySet, entityStream);
  }

  /**
   * Returns an ODataDeltaFeed deserializing EntityStream
   * @param entitySet
   * @param entityStream
   * @return ODataDeltaFeed
   * @throws EntityProviderException
   */
  public ODataDeltaFeed readDeltaFeed(final EdmEntitySet entitySet, final EntityStream entityStream) 
      throws EntityProviderException {

    JsonReader reader = null;
    EntityProviderException cachedException = null;

    try {
      EntityInfoAggregator eia = EntityInfoAggregator.create(entitySet);
      reader = createJsonReader(entityStream.getContent());

      JsonFeedDeserializer jfc = new JsonFeedDeserializer(reader, eia, entityStream.getReadProperties());
      return jfc.readFeedStandalone();
    } catch (UnsupportedEncodingException e) {
      cachedException =
          new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
              .getSimpleName()), e);
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) { //NOPMD  - suppressed
          if (cachedException != null) { //NOSONAR
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
 * @param content
 * @return JsonReader
 * @throws EntityProviderException
 * @throws UnsupportedEncodingException
 */
  private JsonReader createJsonReader(final Object content) throws EntityProviderException,
      UnsupportedEncodingException {

    if (content == null) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
          .addContent("Got not supported NULL object as content to de-serialize."));
    }

    if (content instanceof InputStream) {
      return new JsonReader(new InputStreamReader((InputStream) content, DEFAULT_CHARSET));
    }
    throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
        .addContent("Found not supported content of class '" + content.getClass() + "' to de-serialize."));
  }
  
  public List<?> readCollection(final EntityPropertyInfo info, final EntityStream entityStream) 
      throws EntityProviderException {
    JsonReader reader = null;
    EntityProviderException cachedException = null;

    try {
      reader = createJsonReader(entityStream.getContent());
      return new JsonPropertyDeserializer().readCollection(reader, info, entityStream.getReadProperties());
    } catch (final UnsupportedEncodingException e) {
      cachedException = new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED
          .addContent(e.getClass().getSimpleName()), e);
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (reader != null) {
        try {
          reader.close();
        } catch (final IOException e) {
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
  
  public Map<String, Object> readProperty(final EntityPropertyInfo propertyInfo, final EntityStream entityStream) 
      throws EntityProviderException {
    JsonReader reader = null;
    EntityProviderException cachedException = null;

    try {
      reader = createJsonReader(entityStream.getContent());
      return new JsonPropertyDeserializer().readPropertyStandalone(reader, propertyInfo, 
          entityStream.getReadProperties());
    } catch (final UnsupportedEncodingException e) {
      cachedException =
          new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
              .getSimpleName()), e);
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (reader != null) {
        try {
          reader.close();
        } catch (final IOException e) {
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
