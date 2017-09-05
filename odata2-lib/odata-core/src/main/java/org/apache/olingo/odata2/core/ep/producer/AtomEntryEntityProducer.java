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
package org.apache.olingo.odata2.core.ep.producer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmCustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmTargetPath;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteEntryContent;
import org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackResult;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.Encoder;
import org.apache.olingo.odata2.core.edm.EdmDateTimeOffset;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

/**
 * Serializes an ATOM entry.
 * 
 */
public class AtomEntryEntityProducer {

  private String etag;
  private String location;
  private final EntityProviderWriteProperties properties;

  public AtomEntryEntityProducer(final EntityProviderWriteProperties properties) throws EntityProviderException {
    this.properties = properties == null ? EntityProviderWriteProperties.serviceRoot(null).build() : properties;
  }

  public void append(final XMLStreamWriter writer, final EntityInfoAggregator eia, final Map<String, Object> data,
      final boolean isRootElement, final boolean isFeedPart) throws EntityProviderException {
    try {
      writer.writeStartElement(FormatXml.ATOM_ENTRY);

      if (isRootElement) {
        writer.writeDefaultNamespace(Edm.NAMESPACE_ATOM_2005);
        writer.writeNamespace(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);
        writer.writeNamespace(Edm.PREFIX_D, Edm.NAMESPACE_D_2007_08);
      }
      if (!isFeedPart) {
        writer.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, FormatXml.XML_BASE, properties.getServiceRoot()
            .toASCIIString());
      }

      if (!properties.isContentOnly() && !properties.isOmitETag()) {
        etag = createETag(eia, data);
        if (etag != null) {
          writer.writeAttribute(Edm.NAMESPACE_M_2007_08, FormatXml.M_ETAG, etag);
        }
      }
      
      String selfLink = null;
      if (!properties.isContentOnly()) {
        // write all atom infos (mandatory and optional)
        selfLink = createSelfLink(eia, data, null);
        appendAtomMandatoryParts(writer, eia, data, selfLink);
        appendAtomOptionalParts(writer, eia, data);
      }
      if (eia.getEntityType().hasStream()) {
        // write all links
        if (!properties.isContentOnly()) {
          appendAtomEditLink(writer, eia, selfLink);
          appendAtomContentLink(writer, eia, data, selfLink);
          appendAtomNavigationLinks(writer, eia, data);
        } else {
          appendAdditinalLinks(writer, eia, data);
        }
        // write properties/content
        appendCustomProperties(writer, eia, data);
        if (!properties.isContentOnly()) {
          appendAtomContentPart(writer, eia, data, selfLink);
        }
        appendProperties(writer, eia, data);
      } else {
        // write all links
        if (!properties.isContentOnly()) {
          appendAtomEditLink(writer, eia, selfLink);
          appendAtomNavigationLinks(writer, eia, data);
        } else {
          appendAdditinalLinks(writer, eia, data);
        }
        // write properties/content
        appendCustomProperties(writer, eia, data);
        writer.writeStartElement(FormatXml.ATOM_CONTENT);
        writer.writeAttribute(FormatXml.ATOM_TYPE, ContentType.APPLICATION_XML.toString());
        appendProperties(writer, eia, data);
        writer.writeEndElement();
      }

      writer.writeEndElement();

      writer.flush();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    } catch (EdmException e) {
      throw new EntityProviderProducerException(e.getMessageReference(), e);
    } catch (URISyntaxException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  private void appendAdditinalLinks(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final Map<String, Object> data)
      throws EntityProviderException, EdmException, URISyntaxException {
    final Map<String, Map<String, Object>> links = properties.getAdditionalLinks();
    if (links != null && !links.isEmpty()) {
      for (Entry<String, Map<String, Object>> entry : links.entrySet()) {
        Map<String, Object> navigationKeyMap = entry.getValue();
        final boolean isFeed =
            (eia.getNavigationPropertyInfo(entry.getKey()).getMultiplicity() == EdmMultiplicity.MANY);
        if (navigationKeyMap != null && !navigationKeyMap.isEmpty()) {
          final EntityInfoAggregator targetEntityInfo = EntityInfoAggregator.create(
              eia.getEntitySet().getRelatedEntitySet(
                  (EdmNavigationProperty) eia.getEntityType().getProperty(entry.getKey())));
          appendAtomNavigationLink(writer, createSelfLink(targetEntityInfo, navigationKeyMap, null), entry.getKey(),
              isFeed, eia, data);
        }
      }
    }
  }

  private void appendCustomProperties(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final Map<String, Object> data) throws EntityProviderException {
    List<String> noneSyndicationTargetPaths = eia.getNoneSyndicationTargetPathNames();
    for (String tpName : noneSyndicationTargetPaths) {
      EntityPropertyInfo info = eia.getTargetPathInfo(tpName);
      final String name = info.getName();
      XmlPropertyEntityProducer aps = new XmlPropertyEntityProducer(properties);
      aps.appendCustomProperty(writer, name, info, data.get(name));
    }
  }

  protected static String createETag(final EntityInfoAggregator eia, final Map<String, Object> data)
      throws EntityProviderException {
    String propertyName = "";
    try {
      String etag = null;

      Collection<EntityPropertyInfo> propertyInfos = eia.getETagPropertyInfos();
      for (EntityPropertyInfo propertyInfo : propertyInfos) {
        propertyName = propertyInfo.getName();
        EdmType edmType = propertyInfo.getType();
        if (edmType instanceof EdmSimpleType) {
          EdmSimpleType edmSimpleType = (EdmSimpleType) edmType;
          if (etag == null) {
            etag =
                edmSimpleType.valueToString(data.get(propertyInfo.getName()), EdmLiteralKind.DEFAULT, propertyInfo
                    .getFacets());
          } else {
            etag =
                etag
                    + Edm.DELIMITER
                    + edmSimpleType.valueToString(data.get(propertyInfo.getName()), EdmLiteralKind.DEFAULT,
                        propertyInfo.getFacets());
          }
        }
      }

      if (etag != null) {
        etag = "W/\"" + etag + "\"";
      }

      return etag;
    } catch (EdmSimpleTypeException e) {
      throw new EntityProviderProducerException(EdmSimpleTypeException.getMessageReference(
          e.getMessageReference()).updateContent(e.getMessageReference().getContent(), propertyName), e);
    }
  }

  private void appendAtomNavigationLinks(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final Map<String, Object> data) throws EntityProviderException, EdmException, URISyntaxException {
    for (String name : eia.getSelectedNavigationPropertyNames()) {
      final boolean isFeed = (eia.getNavigationPropertyInfo(name).getMultiplicity() == EdmMultiplicity.MANY);
      final Map<String, Map<String, Object>> links = properties.getAdditionalLinks();
      final Map<String, Object> key = links == null ? null : links.get(name);
      if (key == null || key.isEmpty()) {
        appendAtomNavigationLink(writer, createSelfLink(eia, data, name), name, isFeed, eia, data);
      } else {
        final EntityInfoAggregator targetEntityInfo = EntityInfoAggregator.create(
            eia.getEntitySet().getRelatedEntitySet((EdmNavigationProperty) eia.getEntityType().getProperty(name)));
        appendAtomNavigationLink(writer, createSelfLink(targetEntityInfo, key, null), name, isFeed, eia, data);
      }
    }
  }

  private void appendAtomNavigationLink(final XMLStreamWriter writer, final String target,
      final String navigationPropertyName, final boolean isFeed, final EntityInfoAggregator eia,
      final Map<String, Object> data) throws EntityProviderException, EdmException, URISyntaxException {
    try {
      writer.writeStartElement(FormatXml.ATOM_LINK);
      writer.writeAttribute(FormatXml.ATOM_HREF, target);
      writer.writeAttribute(FormatXml.ATOM_REL, Edm.NAMESPACE_REL_2007_08 + navigationPropertyName);
      writer.writeAttribute(FormatXml.ATOM_TITLE, navigationPropertyName);
      if (isFeed) {
        writer.writeAttribute(FormatXml.ATOM_TYPE, ContentType.APPLICATION_ATOM_XML_FEED.toString());
        appendInlineFeed(writer, navigationPropertyName, eia, data, target);
      } else {
        writer.writeAttribute(FormatXml.ATOM_TYPE, ContentType.APPLICATION_ATOM_XML_ENTRY.toString());
        appendInlineEntry(writer, navigationPropertyName, eia, data);
      }
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  private void appendInlineFeed(final XMLStreamWriter writer, final String navigationPropertyName,
      final EntityInfoAggregator eia, final Map<String, Object> data, final String self)
      throws EntityProviderException, XMLStreamException, EdmException, URISyntaxException {

    if (eia.getExpandedNavigationPropertyNames().contains(navigationPropertyName)) {
      if (properties.getCallbacks() != null && properties.getCallbacks().containsKey(navigationPropertyName)) {

        EdmNavigationProperty navProp = (EdmNavigationProperty) eia.getEntityType().getProperty(navigationPropertyName);
        WriteFeedCallbackContext context = new WriteFeedCallbackContext();
        context.setSourceEntitySet(eia.getEntitySet());
        context.setNavigationProperty(navProp);
        context.setEntryData(data);
        context.setCurrentWriteProperties(properties);
        ExpandSelectTreeNode subNode = properties.getExpandSelectTree().getLinks().get(navigationPropertyName);
        context.setCurrentExpandSelectTreeNode(subNode);
        context.setSelfLink(new URI(self));

        ODataCallback callback = properties.getCallbacks().get(navigationPropertyName);
        if (callback == null) {
          throw new EntityProviderProducerException(EntityProviderException.EXPANDNOTSUPPORTED);
        }
        WriteFeedCallbackResult result;
        try {
          result = ((OnWriteFeedContent) callback).retrieveFeedResult(context);
        } catch (ODataApplicationException e) {
          throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
        }
        List<Map<String, Object>> inlineData = result.getFeedData();
        if (inlineData == null) {
          inlineData = new ArrayList<Map<String, Object>>();
        }
        
        // This statement is used for the client use case. Flag should never be set on server side
        if (properties.isOmitInlineForNullData() && inlineData.isEmpty()) {
          return;
        }
        writer.writeStartElement(Edm.NAMESPACE_M_2007_08, FormatXml.M_INLINE);

        EntityProviderWriteProperties inlineProperties = result.getInlineProperties();
        EdmEntitySet inlineEntitySet = eia.getEntitySet().getRelatedEntitySet(navProp);
        AtomFeedProducer inlineFeedProducer = new AtomFeedProducer(inlineProperties);
        EntityInfoAggregator inlineEia =
            EntityInfoAggregator.create(inlineEntitySet, inlineProperties.getExpandSelectTree());
        inlineFeedProducer.append(writer, inlineEia, inlineData, true);

        writer.writeEndElement();
      }
    }
  }

  private void appendInlineEntry(final XMLStreamWriter writer, final String navigationPropertyName,
      final EntityInfoAggregator eia, final Map<String, Object> data) throws EntityProviderException,
      XMLStreamException, EdmException {

    if (eia.getExpandedNavigationPropertyNames().contains(navigationPropertyName)) {
      if (properties.getCallbacks() != null && properties.getCallbacks().containsKey(navigationPropertyName)) {

        EdmNavigationProperty navProp = (EdmNavigationProperty) eia.getEntityType().getProperty(navigationPropertyName);
        WriteEntryCallbackContext context = new WriteEntryCallbackContext();
        context.setSourceEntitySet(eia.getEntitySet());
        context.setCurrentWriteProperties(properties);
        context.setNavigationProperty(navProp);
        context.setEntryData(data);
        ExpandSelectTreeNode subNode = properties.getExpandSelectTree().getLinks().get(navigationPropertyName);
        context.setCurrentExpandSelectTreeNode(subNode);

        ODataCallback callback = properties.getCallbacks().get(navigationPropertyName);
        if (callback == null) {
          throw new EntityProviderProducerException(EntityProviderException.EXPANDNOTSUPPORTED);
        }
        WriteEntryCallbackResult result;
        try {
          result = ((OnWriteEntryContent) callback).retrieveEntryResult(context);
        } catch (ODataApplicationException e) {
          throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
        }
        Map<String, Object> inlineData = result.getEntryData();
        
        // This statement is used for the client use case. Flag should never be set on server side
        if (properties.isOmitInlineForNullData() && (inlineData == null || inlineData.isEmpty())) {
          return;
        }

        writer.writeStartElement(Edm.NAMESPACE_M_2007_08, FormatXml.M_INLINE);
        if (inlineData != null && !inlineData.isEmpty()) {
          EntityProviderWriteProperties inlineProperties = result.getInlineProperties();
          EdmEntitySet inlineEntitySet = eia.getEntitySet().getRelatedEntitySet(navProp);
          AtomEntryEntityProducer inlineProducer = new AtomEntryEntityProducer(inlineProperties);
          EntityInfoAggregator inlineEia =
              EntityInfoAggregator.create(inlineEntitySet, inlineProperties.getExpandSelectTree());
          inlineProducer.append(writer, inlineEia, inlineData, false, false);
        }

        writer.writeEndElement();
      }
    }

  }

  private void appendAtomEditLink(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final String selfLink) throws EntityProviderException {
    try {
      writer.writeStartElement(FormatXml.ATOM_LINK);
      writer.writeAttribute(FormatXml.ATOM_HREF, selfLink);
      writer.writeAttribute(FormatXml.ATOM_REL, Edm.LINK_REL_EDIT);
      writer.writeAttribute(FormatXml.ATOM_TITLE, eia.getEntityType().getName());
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    } catch (EdmException e) {
      throw new EntityProviderProducerException(e.getMessageReference(), e);
    }
  }

  private void appendAtomContentLink(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final Map<String, Object> data, final String selfLink) throws EntityProviderException, EdmException {
    try {
      String mediaResourceMimeType = null;
      EdmMapping entityTypeMapping = eia.getEntityType().getMapping();
      if (entityTypeMapping != null) {
        String mediaResourceMimeTypeKey = entityTypeMapping.getMediaResourceMimeTypeKey();
        if (mediaResourceMimeTypeKey != null) {
          mediaResourceMimeType = (String) data.get(mediaResourceMimeTypeKey);
        }
      }
      if (mediaResourceMimeType == null) {
        mediaResourceMimeType = ContentType.APPLICATION_OCTET_STREAM.toString();
      }

      writer.writeStartElement(FormatXml.ATOM_LINK);
      writer.writeAttribute(FormatXml.ATOM_HREF, selfLink + "/$value");
      writer.writeAttribute(FormatXml.ATOM_REL, Edm.LINK_REL_EDIT_MEDIA);
      writer.writeAttribute(FormatXml.ATOM_TYPE, mediaResourceMimeType);
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  private void appendAtomContentPart(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final Map<String, Object> data, final String selfLink) throws EntityProviderException, EdmException {
    try {

      EdmMapping entityTypeMapping = eia.getEntityType().getMapping();
      String self = null;
      String mediaResourceMimeType = null;

      if (entityTypeMapping != null) {
        String mediaResourceSourceKey = entityTypeMapping.getMediaResourceSourceKey();
        if (mediaResourceSourceKey != null) {
          self = (String) data.get(mediaResourceSourceKey);
        }
        if (self == null) {
          self = selfLink + "/$value";
        }
        String mediaResourceMimeTypeKey = entityTypeMapping.getMediaResourceMimeTypeKey();
        if (mediaResourceMimeTypeKey != null) {
          mediaResourceMimeType = (String) data.get(mediaResourceMimeTypeKey);
        }
        if (mediaResourceMimeType == null) {
          mediaResourceMimeType = ContentType.APPLICATION_OCTET_STREAM.toString();
        }
      } else {
        self = selfLink + "/$value";
        mediaResourceMimeType = ContentType.APPLICATION_OCTET_STREAM.toString();
      }

      writer.writeEmptyElement(FormatXml.ATOM_CONTENT);
      writer.writeAttribute(FormatXml.ATOM_TYPE, mediaResourceMimeType);
      writer.writeAttribute(FormatXml.ATOM_SRC, self);
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  private void appendAtomMandatoryParts(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final Map<String, Object> data, final String selfLink) throws EntityProviderException {
    try {
      writer.writeStartElement(FormatXml.ATOM_ID);
      location = properties.getServiceRoot().toASCIIString() + selfLink;
      writer.writeCharacters(location);
      writer.writeEndElement();

      writer.writeStartElement(FormatXml.ATOM_TITLE);
      writer.writeAttribute(FormatXml.ATOM_TYPE, FormatXml.ATOM_TEXT);
      EntityPropertyInfo titleInfo = eia.getTargetPathInfo(EdmTargetPath.SYNDICATION_TITLE);
      if (titleInfo != null) {
        EdmSimpleType st = (EdmSimpleType) titleInfo.getType();
        Object object = data.get(titleInfo.getName());
        String title = null;
        try {
          title = st.valueToString(object, EdmLiteralKind.DEFAULT, titleInfo.getFacets());
        } catch (final EdmSimpleTypeException e) {
          throw new EntityProviderProducerException(
              EdmSimpleTypeException.getMessageReference(e.getMessageReference()).
              updateContent(e.getMessageReference().getContent(), titleInfo.getName()), e);
        }
        if (title != null) {
          writer.writeCharacters(title);
        }
      } else {
        writer.writeCharacters(eia.getEntitySetName());
      }
      writer.writeEndElement();

      writer.writeStartElement(FormatXml.ATOM_UPDATED);

      writer.writeCharacters(getUpdatedString(eia, data));

      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    } catch (EdmSimpleTypeException e) {
      throw new EntityProviderProducerException(e.getMessageReference(), e);
    }
  }

  String getUpdatedString(final EntityInfoAggregator eia, final Map<String, Object> data)
      throws EdmSimpleTypeException, EntityProviderProducerException {
    Object updateDate = null;
    EdmFacets updateFacets = null;
    EntityPropertyInfo updatedInfo = eia.getTargetPathInfo(EdmTargetPath.SYNDICATION_UPDATED);
    if (updatedInfo != null) {
      updateDate = data.get(updatedInfo.getName());
      if (updateDate != null) {
        updateFacets = updatedInfo.getFacets();
      }
    }
    if (updateDate == null) {
      updateDate = new Date();
    }
    try {
      return EdmDateTimeOffset.getInstance().valueToString(updateDate, EdmLiteralKind.DEFAULT, updateFacets);
    } catch (final EdmSimpleTypeException e) {
      throw new EntityProviderProducerException(
          EdmSimpleTypeException.getMessageReference(e.getMessageReference()).
          updateContent(e.getMessageReference().getContent(), updatedInfo != null ? 
              updatedInfo.getName() : null), e);
    }
  }

  private String getTargetPathValue(final EntityInfoAggregator eia, final String targetPath,
      final Map<String, Object> data) throws EntityProviderException {
    EntityPropertyInfo info = null;
    try {
      info = eia.getTargetPathInfo(targetPath);
      if (info != null) {
        EdmSimpleType type = (EdmSimpleType) info.getType();
        Object value = data.get(info.getName());
        return type.valueToString(value, EdmLiteralKind.DEFAULT, info.getFacets());
      }
      return null;
    } catch (final EdmSimpleTypeException e) {
      throw new EntityProviderProducerException(
          EdmSimpleTypeException.getMessageReference(e.getMessageReference()).
          updateContent(e.getMessageReference().getContent(), info.getName()), e);
    }
  }

  private void appendAtomOptionalParts(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final Map<String, Object> data) throws EntityProviderException {
    try {
      String authorEmail = getTargetPathValue(eia, EdmTargetPath.SYNDICATION_AUTHOREMAIL, data);
      String authorName = getTargetPathValue(eia, EdmTargetPath.SYNDICATION_AUTHORNAME, data);
      String authorUri = getTargetPathValue(eia, EdmTargetPath.SYNDICATION_AUTHORURI, data);
      if (authorEmail != null || authorName != null || authorUri != null) {
        writer.writeStartElement(FormatXml.ATOM_AUTHOR);
        appendAtomOptionalPart(writer, FormatXml.ATOM_AUTHOR_NAME, authorName, false);
        appendAtomOptionalPart(writer, FormatXml.ATOM_AUTHOR_EMAIL, authorEmail, false);
        appendAtomOptionalPart(writer, FormatXml.ATOM_AUTHOR_URI, authorUri, false);
        writer.writeEndElement();
      }

      String summary = getTargetPathValue(eia, EdmTargetPath.SYNDICATION_SUMMARY, data);
      appendAtomOptionalPart(writer, FormatXml.ATOM_SUMMARY, summary, true);

      String contributorName = getTargetPathValue(eia, EdmTargetPath.SYNDICATION_CONTRIBUTORNAME, data);
      String contributorEmail = getTargetPathValue(eia, EdmTargetPath.SYNDICATION_CONTRIBUTOREMAIL, data);
      String contributorUri = getTargetPathValue(eia, EdmTargetPath.SYNDICATION_CONTRIBUTORURI, data);
      if (contributorEmail != null || contributorName != null || contributorUri != null) {
        writer.writeStartElement(FormatXml.ATOM_CONTRIBUTOR);
        appendAtomOptionalPart(writer, FormatXml.ATOM_CONTRIBUTOR_NAME, contributorName, false);
        appendAtomOptionalPart(writer, FormatXml.ATOM_CONTRIBUTOR_EMAIL, contributorEmail, false);
        appendAtomOptionalPart(writer, FormatXml.ATOM_CONTRIBUTOR_URI, contributorUri, false);
        writer.writeEndElement();
      }

      String rights = getTargetPathValue(eia, EdmTargetPath.SYNDICATION_RIGHTS, data);
      appendAtomOptionalPart(writer, FormatXml.ATOM_RIGHTS, rights, true);
      String published = getTargetPathValue(eia, EdmTargetPath.SYNDICATION_PUBLISHED, data);
      appendAtomOptionalPart(writer, FormatXml.ATOM_PUBLISHED, published, false);

      String term = eia.getEntityType().getNamespace() + Edm.DELIMITER + eia.getEntityType().getName();
      writer.writeStartElement(FormatXml.ATOM_CATEGORY);
      writer.writeAttribute(FormatXml.ATOM_CATEGORY_TERM, term);
      writer.writeAttribute(FormatXml.ATOM_CATEGORY_SCHEME, Edm.NAMESPACE_SCHEME_2007_08);
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    } catch (EdmException e) {
      throw new EntityProviderProducerException(e.getMessageReference(), e);
    }
  }

  private void appendAtomOptionalPart(final XMLStreamWriter writer, final String name, final String value,
      final boolean writeType) throws EntityProviderException {
    try {
      if (value != null) {
        writer.writeStartElement(name);
        if (writeType) {
          writer.writeAttribute(FormatXml.ATOM_TYPE, FormatXml.ATOM_TEXT);
        }
        writer.writeCharacters(value);
        writer.writeEndElement();
      }
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  static String createSelfLink(final EntityInfoAggregator eia, final Map<String, Object> data, final String extension)
      throws EntityProviderException {
    StringBuilder sb = new StringBuilder();
    if (!eia.isDefaultEntityContainer()) {
      sb.append(Encoder.encode(eia.getEntityContainerName())).append(Edm.DELIMITER);
    }
    sb.append(Encoder.encode(eia.getEntitySetName()));

    sb.append("(").append(createEntryKey(eia, data)).append(")").append(extension == null ? "" : ("/" + extension));
    return sb.toString();
  }

  private static String createEntryKey(final EntityInfoAggregator entityInfo, final Map<String, Object> data)
      throws EntityProviderException {
    final List<EntityPropertyInfo> keyPropertyInfos = entityInfo.getKeyPropertyInfos();

    StringBuilder keys = new StringBuilder();
    for (final EntityPropertyInfo keyPropertyInfo : keyPropertyInfos) {
      if (keys.length() > 0) {
        keys.append(',');
      }

      final String name = keyPropertyInfo.getName();
      if (keyPropertyInfos.size() > 1) {
        keys.append(Encoder.encode(name)).append('=');
      }

      final EdmSimpleType type = (EdmSimpleType) keyPropertyInfo.getType();
      try {
        keys.append(Encoder.encode(type.valueToString(data.get(name), EdmLiteralKind.URI,
            keyPropertyInfo.getFacets())));
      } catch (final EdmSimpleTypeException e) {
        throw new EntityProviderProducerException(
            EdmSimpleTypeException.getMessageReference(e.getMessageReference()).
            updateContent(e.getMessageReference().getContent(), name), e);
      }
    }

    return keys.toString();
  }

  private void appendProperties(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final Map<String, Object> data) throws EntityProviderException {
    try {
      if (properties.isDataBasedPropertySerialization()) {
        if (!data.isEmpty()) {
          writer.writeStartElement(Edm.NAMESPACE_M_2007_08, FormatXml.M_PROPERTIES);
          for (String propertyName : eia.getPropertyNames()) {
            if (data.containsKey(propertyName)) {
              appendPropertyNameValue(writer, eia, data, propertyName);
            }
          }
          writer.writeEndElement();
        }
      } else {
        List<String> propertyNames = eia.getSelectedPropertyNames();
        if (!propertyNames.isEmpty()) {
          writer.writeStartElement(Edm.NAMESPACE_M_2007_08, FormatXml.M_PROPERTIES);

          for (String propertyName : propertyNames) {
            appendPropertyNameValue(writer, eia, data, propertyName);
          }
          writer.writeEndElement();
        }
      }
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  /**
   * @param writer
   * @param eia
   * @param data
   * @param propertyName
   * @throws EntityProviderException
   */
  private void appendPropertyNameValue(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final Map<String, Object> data, String propertyName) throws EntityProviderException {
    EntityPropertyInfo propertyInfo = eia.getPropertyInfo(propertyName);
    if (isNotMappedViaCustomMapping(propertyInfo)) {
      Object value = data.get(propertyName);
      XmlPropertyEntityProducer aps = new XmlPropertyEntityProducer(properties);
      aps.append(writer, propertyInfo.getName(), propertyInfo, value);
    }
  }
  
  private boolean isNotMappedViaCustomMapping(final EntityPropertyInfo propertyInfo) {
    EdmCustomizableFeedMappings customMapping = propertyInfo.getCustomMapping();
    if (customMapping != null && customMapping.isFcKeepInContent() != null) {
      return customMapping.isFcKeepInContent();
    }
    return true;
  }

  public String getETag() {
    return etag;
  }

  public String getLocation() {
    return location;
  }
}
