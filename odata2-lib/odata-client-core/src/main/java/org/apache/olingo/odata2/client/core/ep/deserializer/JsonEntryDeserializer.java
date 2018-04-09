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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.client.api.ep.DeserializerProperties;
import org.apache.olingo.odata2.client.api.ep.callback.OnDeserializeInlineContent;
import org.apache.olingo.odata2.core.edm.EdmDateTimeOffset;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.aggregator.NavigationPropertyInfo;
import org.apache.olingo.odata2.core.ep.entry.DeletedEntryMetadataImpl;
import org.apache.olingo.odata2.core.ep.entry.EntryMetadataImpl;
import org.apache.olingo.odata2.core.ep.entry.MediaMetadataImpl;
import org.apache.olingo.odata2.core.ep.entry.ODataEntryImpl;
import org.apache.olingo.odata2.core.ep.feed.JsonFeedEntry;
import org.apache.olingo.odata2.core.ep.util.FormatJson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 *  This class Deserializes JsonEntry payloads
 */
public class JsonEntryDeserializer {

  private final EntityInfoAggregator eia;
  private final JsonReader reader;
  private final DeserializerProperties readProperties;

  private ODataEntryImpl resultEntry;
  private Map<String, Object> properties;
  private MediaMetadataImpl mediaMetadata;
  private EntryMetadataImpl entryMetadata;

  private DeletedEntryMetadataImpl resultDeletedEntry;

  /**
   * 
   * @param reader
   * @param eia
   * @param readProperties
   */
  public JsonEntryDeserializer(final JsonReader reader, final EntityInfoAggregator eia,
      final DeserializerProperties readProperties) {
    this.eia = eia;
    this.readProperties = readProperties;
    this.reader = reader;
  }

  /**
   * Returns ODataEntry deserializing a single entry
   * @return ODataEntry
   * @throws EntityProviderException
   */
  public ODataEntry readSingleEntry() throws EntityProviderException {
    try {
      reader.beginObject();
      String nextName = reader.nextName();
      if (FormatJson.D.equals(nextName)) {
        reader.beginObject();
        readEntryContent();
        reader.endObject();
      } else {
        handleName(nextName);
        readEntryContent();
      }
      reader.endObject();

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

    return resultEntry;
  }

  /**
   * Returns Feed deserializing feed entry
   * @return JsonFeedEntry
   * @throws EdmException
   * @throws EntityProviderException
   * @throws IOException
   */
  public JsonFeedEntry readFeedEntry() throws EdmException, EntityProviderException, IOException {//NOSONAR
    reader.beginObject();
    readEntryContent();
    reader.endObject();

    if (resultDeletedEntry == null) {
      return new JsonFeedEntry(resultEntry);
    } else {
      return new JsonFeedEntry(resultDeletedEntry);
    }
  }

  /**
   * 
   * @throws IOException
   * @throws EdmException
   * @throws EntityProviderException
   */
  private void readEntryContent() throws IOException, EdmException, EntityProviderException {
    while (reader.hasNext()) {
      final String name = reader.nextName();
      handleName(name);
    }
  }

  /**
   * Ensure that instance field {@link #resultEntry} exists.
   * If it not already exists create an instance (as well as all other necessary objects like: {@link #properties},
   * {@link #mediaMetadata}, {@link #entryMetadata}, {@link #expandSelectTree}).
   */
  private void ensureODataEntryExists() {
    if (resultEntry == null) {
      properties = new HashMap<String, Object>();
      mediaMetadata = new MediaMetadataImpl();
      entryMetadata = new EntryMetadataImpl();
      
      resultEntry = new ODataEntryImpl(properties, mediaMetadata, entryMetadata, null);
    }
  }

  /**
   * Ensure that instance field {@link #resultDeletedEntry} exists.
   * If it not already exists create an instance.
   */
  private void ensureDeletedEntryMetadataExists() {
    if (resultDeletedEntry == null) {
      resultDeletedEntry = new DeletedEntryMetadataImpl();
    }
  }

  /**
   * 
   * @param name
   * @throws IOException
   * @throws EdmException
   * @throws EntityProviderException
   */
  private void handleName(final String name) throws IOException, EdmException, EntityProviderException {
    if (FormatJson.METADATA.equals(name)) {
      ensureODataEntryExists();
      readMetadata();
      validateMetadata();
    } else if (FormatJson.ODATA_CONTEXT.equals(name)) {
      readODataContext();
    } else {
      ensureODataEntryExists();
      EntityPropertyInfo propertyInfo = eia.getPropertyInfo(name);
      if (propertyInfo != null) {
        //TODO: put Type mapping instead of null
        Object propertyValue = new JsonPropertyDeserializer()
            .readPropertyValue(reader, propertyInfo, null, readProperties);
        if (properties.containsKey(name)) {
          throw new EntityProviderException(EntityProviderException.DOUBLE_PROPERTY.addContent(name));
        }
        properties.put(name, propertyValue);
      } else {
        readNavigationProperty(name);
      }
    }
  }
  /**
   * 
   * @throws IOException
   * @throws EntityProviderException
   */
  private void readODataContext() throws IOException, EntityProviderException {
    String contextValue = reader.nextString();
    if (contextValue == null) {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE.addContent(FormatJson.ODATA_CONTEXT)
          .addContent(FormatJson.RESULTS));
    }

    if (contextValue.startsWith(FormatJson.DELTA_CONTEXT_PREFIX)
        && contextValue.endsWith(FormatJson.DELTA_CONTEXT_POSTFIX)) {
      while (reader.hasNext()) {
        ensureDeletedEntryMetadataExists();
        String name = reader.nextName();
        String value = reader.nextString();
        if (FormatJson.ID.equals(name)) {
          resultDeletedEntry.setUri(value);
        } else if (FormatJson.DELTA_WHEN.equals(name)) {
          Date when = parseWhen(value);
          resultDeletedEntry.setWhen(when);
        }
      }
    }
  }

  /**
   * 
   * @param value
   * @return Date
   * @throws EntityProviderException
   */
  private Date parseWhen(final String value) throws EntityProviderException {
    try {
      return EdmDateTimeOffset.getInstance().valueOfString(value, EdmLiteralKind.JSON, null, Date.class);
    } catch (EdmSimpleTypeException e) {
      throw new EntityProviderException(EntityProviderException.INVALID_DELETED_ENTRY_METADATA
          .addContent("Unparsable format for when field value."), e);
    }
  }

  /**
   * 
   * @throws IOException
   * @throws EdmException
   * @throws EntityProviderException
   */
  private void readMetadata() throws IOException, EdmException, EntityProviderException {//NOSONAR
    String name = null;
    String value = null;
    reader.beginObject();
    while (reader.hasNext()) {
      name = reader.nextName();

      if (FormatJson.PROPERTIES.equals(name)) {
        reader.skipValue();
        continue;
      }

      value = reader.nextString();
      if (FormatJson.ID.equals(name)) {
        entryMetadata.setId(value);
      } else if (FormatJson.URI.equals(name)) {
        entryMetadata.setUri(value);
      } else if (FormatJson.TYPE.equals(name)) {
        String fullQualifiedName = eia.getEntityType().getNamespace() + Edm.DELIMITER + eia.getEntityType().getName();
        if (!fullQualifiedName.equals(value)) {
          throw new EntityProviderException(EntityProviderException.INVALID_ENTITYTYPE.addContent(fullQualifiedName)
              .addContent(value));
        }
      } else if (FormatJson.ETAG.equals(name)) {
        entryMetadata.setEtag(value);
      } else if (FormatJson.EDIT_MEDIA.equals(name)) {
        mediaMetadata.setEditLink(value);
      } else if (FormatJson.MEDIA_SRC.equals(name)) {
        mediaMetadata.setSourceLink(value);
      } else if (FormatJson.MEDIA_ETAG.equals(name)) {
        mediaMetadata.setEtag(value);
      } else if (FormatJson.CONTENT_TYPE.equals(name)) {
        mediaMetadata.setContentType(value);
      } else {
        throw new EntityProviderException(EntityProviderException.INVALID_CONTENT.addContent(name).addContent(
            FormatJson.METADATA));
      }
    }

    reader.endObject();
  }

  /**
   * 
   * @throws EdmException
   * @throws EntityProviderException
   */
  private void validateMetadata() throws EdmException, EntityProviderException {
    if (eia.getEntityType().hasStream()) {
      if (mediaMetadata.getSourceLink() == null) {
        throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE.addContent(FormatJson.MEDIA_SRC)
            .addContent(FormatJson.METADATA));
      }
      if (mediaMetadata.getContentType() == null) {
        throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE.addContent(FormatJson.CONTENT_TYPE)
            .addContent(FormatJson.METADATA));
      }
    } else {
      if (mediaMetadata.getContentType() != null || mediaMetadata.getEditLink() != null
          || mediaMetadata.getEtag() != null || mediaMetadata.getSourceLink() != null) {
        throw new EntityProviderException(EntityProviderException.MEDIA_DATA_NOT_INITIAL);
      }
    }
  }

  /**
   * 
   * @param navigationPropertyName
   * @throws IOException
   * @throws EntityProviderException
   * @throws EdmException
   */
  private void readNavigationProperty(final String navigationPropertyName) throws IOException, EntityProviderException,
      EdmException {
    NavigationPropertyInfo navigationPropertyInfo = eia.getNavigationPropertyInfo(navigationPropertyName);
    if (navigationPropertyInfo == null) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT.addContent(navigationPropertyName));
    }

    JsonToken peek = reader.peek();
    if (peek == JsonToken.BEGIN_OBJECT) {
      reader.beginObject();
      String name = reader.nextName();
      if (FormatJson.DEFERRED.equals(name)) {
        reader.beginObject();
        String uri = reader.nextName();
        if (FormatJson.URI.equals(uri)) {
          String value = reader.nextString(); 
          entryMetadata.putAssociationUri(navigationPropertyInfo.getName(), value);
        } else {
          throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT.addContent(uri));
        }
        reader.endObject();
      } else {
        handleInlineEntries(navigationPropertyName, name);
      }
      reader.endObject();
    } else if (peek == JsonToken.NULL) {
      reader.nextNull();
    } else {
      handleArray(navigationPropertyName);
    }
   }

  /**
   * @param navigationPropertyName
   * @throws EdmException
   * @throws EntityProviderException
   * @throws IOException
   */
  private void handleArray(final String navigationPropertyName) throws EdmException, EntityProviderException,
      IOException {
    final EdmNavigationProperty navigationProperty =
        (EdmNavigationProperty) eia.getEntityType().getProperty(navigationPropertyName);
    final EdmEntitySet inlineEntitySet = eia.getEntitySet().getRelatedEntitySet(navigationProperty);
    final EntityInfoAggregator inlineInfo = EntityInfoAggregator.create(inlineEntitySet);
    OnDeserializeInlineContent callback = readProperties.getCallback();
    DeserializerProperties inlineReadProperties;
    try {
      if (callback == null) {
        inlineReadProperties =
            DeserializerProperties.init()
                .isValidatingFacets(readProperties.isValidatingFacets())
                .build();

      } else {
        inlineReadProperties = callback.receiveReadProperties(readProperties, navigationProperty);
      }
    } catch (ODataApplicationException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
    
    ODataFeed feed = new JsonFeedDeserializer(reader, inlineInfo, inlineReadProperties).readInlineFeedStandalone();
    properties.put(navigationPropertyName, feed);
    resultEntry.setContainsInlineEntry(true);
  }

  /**
   * @param navigationPropertyName
   * @param name
   * @throws EdmException
   * @throws EntityProviderException
   * @throws IOException
   */
  private void handleInlineEntries(final String navigationPropertyName, String name) throws EdmException,
      EntityProviderException, IOException {
    EdmNavigationProperty navigationProperty =
        (EdmNavigationProperty) eia.getEntityType().getProperty(navigationPropertyName);
    EdmEntitySet inlineEntitySet = eia.getEntitySet().getRelatedEntitySet(navigationProperty);
    EntityInfoAggregator inlineEia = EntityInfoAggregator.create(inlineEntitySet);
    OnDeserializeInlineContent callback = readProperties.getCallback();
    final DeserializerProperties inlineReadProperties;
    try {
      if (callback == null) {
        inlineReadProperties =
            DeserializerProperties.init()
                .isValidatingFacets(readProperties.isValidatingFacets())
                .build();

      } else {
        inlineReadProperties = callback.receiveReadProperties(readProperties, navigationProperty);
      }
    } catch (ODataApplicationException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
    if (navigationProperty.getMultiplicity() == EdmMultiplicity.MANY) {
      JsonFeedDeserializer inlineConsumer = new JsonFeedDeserializer(reader, inlineEia, inlineReadProperties);
      ODataFeed feed = inlineConsumer.readStartedInlineFeed(name);
      properties.put(navigationPropertyName, feed);
      resultEntry.setContainsInlineEntry(true);
    } else {
      JsonEntryDeserializer inlineConsumer = new JsonEntryDeserializer(reader, inlineEia, inlineReadProperties);
      ODataEntry entry = inlineConsumer.readInlineEntry(name);
      properties.put(navigationPropertyName, entry);
      resultEntry.setContainsInlineEntry(true);
    }
  }
  
  /**
   * 
   * @param name
   * @return ODataEntry
   * @throws EdmException
   * @throws EntityProviderException
   * @throws IOException
   */
  private ODataEntry readInlineEntry(final String name) throws EdmException, EntityProviderException, IOException {
    // consume the already started content
    handleName(name);
    // consume the rest of the entry content
    readEntryContent();
    return resultEntry;
  }

}
