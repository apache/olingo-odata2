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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.core.ep.feed.FeedMetadataImpl;
import org.apache.olingo.odata2.core.ep.util.FormatJson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 *  
 */
public class JsonLinkConsumer {

  /**
   * Reads single link with format <code>{"d":{"uri":"http://somelink"}}</code>
   * or <code>{"uri":"http://somelink"}</code>.
   * @param reader
   * @param entitySet
   * @return link as string object
   * @throws EntityProviderException
   */
  public String readLink(final JsonReader reader, final EdmEntitySet entitySet) throws EntityProviderException {
    try {
      String result;
      reader.beginObject();
      String nextName = reader.nextName();
      final boolean wrapped = FormatJson.D.equals(nextName);
      if (wrapped) {
        reader.beginObject();
        nextName = reader.nextName();
      }
      if (FormatJson.URI.equals(nextName) && reader.peek() == JsonToken.STRING) {
        result = reader.nextString();
      } else {
        throw new EntityProviderException(EntityProviderException.INVALID_CONTENT.addContent(
            FormatJson.D + " or " + FormatJson.URI).addContent(nextName));
      }
      reader.endObject();
      if (wrapped) {
        reader.endObject();
      }

      reader.peek(); // to assert end of structure or document

      return result;
    } catch (final IOException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    } catch (final IllegalStateException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  /**
   * Reads a collection of links, optionally wrapped in a "d" object,
   * and optionally wrapped in an "results" object, where an additional "__count"
   * object could appear on the same level as the "results".
   * @param reader
   * @param entitySet
   * @return links as List of Strings
   * @throws EntityProviderException
   */
  public List<String> readLinks(final JsonReader reader, final EdmEntitySet entitySet) throws EntityProviderException {
    List<String> links = null;
    int openedObjects = 0;

    try {
      String nextName;
      if (reader.peek() == JsonToken.BEGIN_ARRAY) {
        nextName = FormatJson.RESULTS;
      } else {
        reader.beginObject();
        openedObjects++;
        nextName = reader.nextName();
      }
      if (FormatJson.D.equals(nextName)) {
        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
          nextName = FormatJson.RESULTS;
        } else {
          reader.beginObject();
          openedObjects++;
          nextName = reader.nextName();
        }
      }
      FeedMetadataImpl feedMetadata = new FeedMetadataImpl();
      if (FormatJson.COUNT.equals(nextName)) {
        JsonFeedConsumer.readInlineCount(reader, feedMetadata);
        nextName = reader.nextName();
      }
      if (FormatJson.RESULTS.equals(nextName)) {
        links = readLinksArray(reader);
      } else {
        throw new EntityProviderException(EntityProviderException.INVALID_CONTENT.addContent(FormatJson.RESULTS)
            .addContent(nextName));
      }
      if (reader.hasNext() && reader.peek() == JsonToken.NAME) {
        if (FormatJson.COUNT.equals(reader.nextName())) {
          JsonFeedConsumer.readInlineCount(reader, feedMetadata);
        } else {
          throw new EntityProviderException(EntityProviderException.INVALID_CONTENT.addContent(FormatJson.COUNT)
              .addContent(nextName));
        }
      }
      for (; openedObjects > 0; openedObjects--) {
        reader.endObject();
      }

      reader.peek(); // to assert end of document
    } catch (final IOException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    } catch (final IllegalStateException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }

    return links;
  }

  private List<String> readLinksArray(final JsonReader reader) throws IOException, EntityProviderException {
    List<String> links = new ArrayList<String>();

    reader.beginArray();
    while (reader.hasNext()) {
      reader.beginObject();
      String nextName = reader.nextName();
      if (FormatJson.URI.equals(nextName) && reader.peek() == JsonToken.STRING) {
        links.add(reader.nextString());
      } else {
        throw new EntityProviderException(EntityProviderException.INVALID_CONTENT.addContent(FormatJson.URI)
            .addContent(nextName));
      }
      reader.endObject();
    }
    reader.endArray();

    return links;
  }
}
