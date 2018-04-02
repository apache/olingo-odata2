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
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.DeletedEntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.client.api.ep.DeserializerProperties;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.feed.FeedMetadataImpl;
import org.apache.olingo.odata2.core.ep.feed.JsonFeedEntry;
import org.apache.olingo.odata2.core.ep.feed.ODataDeltaFeedImpl;
import org.apache.olingo.odata2.core.ep.util.FormatJson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 *  This class Deserializes JsonFeed payloads
 */
public class JsonFeedDeserializer {

  private JsonReader reader;
  private EntityInfoAggregator eia;
  private DeserializerProperties readProperties;
  private List<DeletedEntryMetadata> deletedEntries = new ArrayList<DeletedEntryMetadata>();
  private List<ODataEntry> entries = new ArrayList<ODataEntry>();
  private FeedMetadataImpl feedMetadata = new FeedMetadataImpl();
  private boolean resultsArrayPresent = false;
  private static final String JSONFEED = "JsonFeed";

  /**
   * 
   * @param reader
   * @param eia
   * @param readProperties
   */
  public JsonFeedDeserializer(final JsonReader reader, final EntityInfoAggregator eia,
      final DeserializerProperties readProperties) {
    this.reader = reader;
    this.eia = eia;
    this.readProperties = readProperties;
  }

  /**
   * 
   * @return ODataDeltaFeed
   * @throws EntityProviderException
   */
  public ODataDeltaFeed readFeedStandalone() throws EntityProviderException {
    try {
      readFeed();

      if (reader.peek() != JsonToken.END_DOCUMENT) {

        throw new EntityProviderException(EntityProviderException.END_DOCUMENT_EXPECTED.addContent(reader.peek()
            .toString()));
      }

    } catch (IOException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    } catch (EdmException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    } catch (IllegalStateException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
    return new ODataDeltaFeedImpl(entries, feedMetadata, deletedEntries);
  }

  /**
   * 
   * @throws IOException
   * @throws EdmException
   * @throws EntityProviderException
   */
  private void readFeed() throws IOException, EdmException, EntityProviderException {
    JsonToken peek = reader.peek();
    if (peek == JsonToken.BEGIN_ARRAY) {
      readArrayContent();
    } else {
      reader.beginObject();
      final String nextName = reader.nextName();
      if (FormatJson.D.equals(nextName)) {
        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
          readArrayContent();
        } else {
          reader.beginObject();
          readFeedContent();
          reader.endObject();
        }
      } else {
        handleName(nextName);
        readFeedContent();
      }

      reader.endObject();
    }
  }

  /**
   * 
   * @throws IOException
   * @throws EdmException
   * @throws EntityProviderException
   */
  private void readFeedContent() throws IOException, EdmException, EntityProviderException {
    while (reader.hasNext()) {
      final String nextName = reader.nextName();
      handleName(nextName);
    }

    if (!resultsArrayPresent) {
      throw new EntityProviderException(EntityProviderException.MISSING_RESULTS_ARRAY);
    }
  }

  /**
   * 
   * @param nextName
   * @throws IOException
   * @throws EdmException
   * @throws EntityProviderException
   */
  private void handleName(final String nextName) throws IOException, EdmException, EntityProviderException {
    if (FormatJson.RESULTS.equals(nextName)) {
      resultsArrayPresent = true;
      readArrayContent();

    } else if (FormatJson.COUNT.equals(nextName)) {
      readInlineCount(reader, feedMetadata);

    } else if (FormatJson.NEXT.equals(nextName)) {
      if (reader.peek() == JsonToken.STRING && feedMetadata.getNextLink() == null) {
        String nextLink = reader.nextString();
        feedMetadata.setNextLink(nextLink);
      } else {
        throw new EntityProviderException(EntityProviderException.INVALID_CONTENT.addContent(nextName).addContent(
            JSONFEED));
      }

    } else if (FormatJson.DELTA.equals(nextName)) {
      if (reader.peek() == JsonToken.STRING && feedMetadata.getDeltaLink() == null) {
        String deltaLink = reader.nextString();
        feedMetadata.setDeltaLink(deltaLink);
      } else {
        throw new EntityProviderException(EntityProviderException.INVALID_CONTENT.addContent(nextName).addContent(
            JSONFEED));
      }
    } else {
      throw new EntityProviderException(EntityProviderException.INVALID_CONTENT.addContent(nextName).addContent(
          JSONFEED));
    }
  }

  /**
   * 
   * @throws IOException
   * @throws EdmException
   * @throws EntityProviderException
   */
  private void readArrayContent() throws IOException, EdmException, EntityProviderException {
    reader.beginArray();
    while (reader.hasNext()) {
      final JsonFeedEntry entry = new JsonEntryDeserializer(reader, eia, readProperties).readFeedEntry();
      if (entry.isODataEntry()) {
        entries.add(entry.getODataEntry());
      } else {
        deletedEntries.add(entry.getDeletedEntryMetadata());
      }
    }
    reader.endArray();
  }

  /**
   * 
   * @param reader
   * @param feedMetadata
   * @throws IOException
   * @throws EntityProviderException
   */
  protected static void readInlineCount(final JsonReader reader, final FeedMetadataImpl feedMetadata)
      throws IOException, EntityProviderException {
    if (reader.peek() == JsonToken.STRING && feedMetadata.getInlineCount() == null) {
      int inlineCount;
      try {
        inlineCount = reader.nextInt();
      } catch (final NumberFormatException e) {
        throw new EntityProviderException(EntityProviderException.INLINECOUNT_INVALID.addContent(""), e);
      }
      if (inlineCount >= 0) {
        feedMetadata.setInlineCount(inlineCount);
      } else {
        throw new EntityProviderException(EntityProviderException.INLINECOUNT_INVALID.addContent(inlineCount));
      }
    } else {
      throw new EntityProviderException(EntityProviderException.INLINECOUNT_INVALID.addContent(reader.peek()));
    }
  }

  /**
   * 
   * @param name
   * @return ODataFeed
   * @throws EdmException
   * @throws EntityProviderException
   * @throws IOException
   */
  protected ODataFeed readStartedInlineFeed(final String name) throws EdmException, EntityProviderException,
      IOException {
    // consume the already started content
    handleName(name);
    // consume the rest of the entry content
    readFeedContent();
    return new ODataDeltaFeedImpl(entries, feedMetadata);
  }

  /**
   * 
   * @return ODataFeed
   * @throws EdmException
   * @throws EntityProviderException
   * @throws IOException
   */
  protected ODataFeed readInlineFeedStandalone() throws EdmException, EntityProviderException, IOException {
    readFeed();
    return new ODataDeltaFeedImpl(entries, feedMetadata);
  }

}
