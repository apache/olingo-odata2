/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.apache.olingo.odata2.client.core.ep.deserializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.callback.ReadEntryResult;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.client.api.ep.DeserializerProperties;
import org.apache.olingo.odata2.client.api.ep.callback.OnDeserializeInlineContent;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.aggregator.EntityTypeMapping;
import org.apache.olingo.odata2.core.ep.entry.EntryMetadataImpl;
import org.apache.olingo.odata2.core.ep.entry.MediaMetadataImpl;
import org.apache.olingo.odata2.core.ep.entry.ODataEntryImpl;
import org.apache.olingo.odata2.core.ep.feed.FeedMetadataImpl;
import org.apache.olingo.odata2.core.ep.feed.ODataDeltaFeedImpl;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

/**
 * Atom/XML format reader/consumer for entries.
 *
 * {@link XmlEntryDeserializer} instance can be reused for several
 * {@link #readEntry(XMLStreamReader, EntityInfoAggregator, EntityProviderReadProperties)} calls but
 * be aware that the instance and their <code>readEntry*</code> methods are <b>NOT THREAD SAFE</b>.
 *
 */
public class XmlEntryDeserializer {

    private ODataEntryImpl readEntryResult;
    private Map<String, Object> properties;
    private MediaMetadataImpl mediaMetadata;
    private EntryMetadataImpl entryMetadata;
    private EntityTypeMapping typeMappings;
    private String currentHandledStartTagName;

    /**
     * Deserializes payload entry
     *
     * @param reader
     * @param eia
     * @param readProperties
     * @param isInline
     * @return ODataEntry
     * @throws EntityProviderException
     */
    public ODataEntry readEntry(final XMLStreamReader reader, final EntityInfoAggregator eia, final DeserializerProperties readProperties,
            final boolean isInline) throws EntityProviderException {
        try {
            initialize(readProperties);

            if (isInline) {
                setETag(reader);
            }

            while (reader.hasNext() && !isEntryEndTag(reader)) {
                reader.nextTag();
                if (reader.isStartElement()) {
                    handleStartedTag(reader, eia, readProperties);
                }
            }

            return readEntryResult;
        } catch (XMLStreamException | EdmException e) {
            throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
                                                                                                     .getSimpleName()),
                    e);
        }
    }

    private boolean isEntryEndTag(final XMLStreamReader reader) {
        return reader.isEndElement() && Edm.NAMESPACE_ATOM_2005.equals(reader.getNamespaceURI())
                && FormatXml.ATOM_ENTRY.equals(reader.getLocalName());
    }

    /**
     * Initializes the {@link XmlEntryDeserializer} to be ready for reading an entry.
     *
     * @param readProperties
     * @throws EntityProviderException
     */
    private void initialize(final DeserializerProperties readProperties) throws EntityProviderException {
        properties = new HashMap<String, Object>();
        mediaMetadata = new MediaMetadataImpl();
        entryMetadata = new EntryMetadataImpl();

        readEntryResult = new ODataEntryImpl(properties, mediaMetadata, entryMetadata, null);
        typeMappings = EntityTypeMapping.create(readProperties.getTypeMappings());
    }

    private void handleStartedTag(final XMLStreamReader reader, final EntityInfoAggregator eia, final DeserializerProperties readProperties)
            throws EntityProviderException, XMLStreamException, EdmException {

        currentHandledStartTagName = reader.getLocalName();

        if (FormatXml.ATOM_ID.equals(currentHandledStartTagName)) {
            readId(reader);
        } else if (FormatXml.ATOM_ENTRY.equals(currentHandledStartTagName)) {
            readEntry(reader);
        } else if (FormatXml.ATOM_LINK.equals(currentHandledStartTagName)) {
            readLink(reader, eia, readProperties);
        } else if (FormatXml.ATOM_CONTENT.equals(currentHandledStartTagName)) {
            readContent(reader, eia, readProperties);
        } else if (FormatXml.M_PROPERTIES.equals(currentHandledStartTagName)) {
            readProperties(reader, eia, readProperties);
        } else {
            readCustomElement(reader, currentHandledStartTagName, eia, readProperties);
        }
    }

    private void readCustomElement(final XMLStreamReader reader, final String tagName, // NOSONAR
            final EntityInfoAggregator eia, final DeserializerProperties readProperties)
            throws EdmException, EntityProviderException, XMLStreamException { // NOSONAR
        EntityPropertyInfo targetPathInfo = eia.getTargetPathInfo(tagName);
        NamespaceContext nsctx = reader.getNamespaceContext();

        boolean skipTag = true;
        if (!Edm.NAMESPACE_ATOM_2005.equals(reader.getName()
                                                  .getNamespaceURI())) {

            if (targetPathInfo == null) {
                throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY.addContent(tagName));
            }
            final String customPrefix = targetPathInfo.getCustomMapping()
                                                      .getFcNsPrefix();
            final String customNamespaceURI = targetPathInfo.getCustomMapping()
                                                            .getFcNsUri();

            if (customPrefix != null && customNamespaceURI != null) {
                String xmlPrefix = nsctx.getPrefix(customNamespaceURI);
                String xmlNamespaceUri = reader.getNamespaceURI(customPrefix);

                if (customNamespaceURI.equals(xmlNamespaceUri) && customPrefix.equals(xmlPrefix)) { // NOSONAR
                    skipTag = false;
                    reader.require(XMLStreamConstants.START_ELEMENT, customNamespaceURI, tagName);
                    final String text = reader.getElementText();
                    reader.require(XMLStreamConstants.END_ELEMENT, customNamespaceURI, tagName);

                    final EntityPropertyInfo propertyInfo = getValidatedPropertyInfo(eia, tagName);
                    Class<?> typeMapping = typeMappings.getMappingClass(propertyInfo.getName());
                    final EdmSimpleType type = (EdmSimpleType) propertyInfo.getType();
                    EdmFacets facets = readProperties == null || readProperties.isValidatingFacets() ? propertyInfo.getFacets() : null;
                    typeMapping = typeMapping == null ? type.getDefaultType() : typeMapping;
                    final Object value = type.valueOfString(text, EdmLiteralKind.DEFAULT, facets, typeMapping);
                    properties.put(tagName, value);
                }
            }
        }

        if (skipTag) {
            skipStartedTag(reader);
        }
    }

    /**
     * Skip the tag to which the {@link XMLStreamReader} currently points. Therefore it is read until an
     * end element tag with current local name is found.
     *
     * @param reader
     * @throws XMLStreamException
     */
    private void skipStartedTag(final XMLStreamReader reader) throws XMLStreamException {
        final String name = reader.getLocalName();
        int read = 1;
        while (read > 0 && reader.hasNext()) {
            reader.next();
            if (reader.hasName() && name.equals(reader.getLocalName())) {
                if (reader.isEndElement()) {
                    read--;
                } else if (reader.isStartElement()) {
                    read++;
                }
            }
        }
    }

    private void readEntry(final XMLStreamReader reader) throws XMLStreamException {
        reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_ENTRY);
        setETag(reader);
    }

    private void setETag(final XMLStreamReader reader) {
        final String etag = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, FormatXml.M_ETAG);
        entryMetadata.setEtag(etag);
    }

    /**
     *
     * @param reader
     * @param eia
     * @param readProperties
     * @throws EntityProviderException
     * @throws XMLStreamException
     * @throws EdmException
     */
    private void readLink(final XMLStreamReader reader, final EntityInfoAggregator eia, final DeserializerProperties readProperties)
            throws EntityProviderException, XMLStreamException, EdmException {
        reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_LINK);

        final String rel = reader.getAttributeValue(null, FormatXml.ATOM_REL);
        final String uri = reader.getAttributeValue(null, FormatXml.ATOM_HREF);
        final String type = reader.getAttributeValue(null, FormatXml.ATOM_TYPE);
        final String etag = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, FormatXml.M_ETAG);

        // read to next tag to check if <link> contains any further tags
        reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_LINK);
        reader.nextTag();

        if (rel == null || uri == null) {
            throw new EntityProviderException(
                    EntityProviderException.MISSING_ATTRIBUTE.addContent(FormatXml.ATOM_HREF + "' and/or '" + FormatXml.ATOM_REL)
                                                             .addContent(FormatXml.ATOM_LINK));
        }
        if (rel.startsWith(Edm.NAMESPACE_REL_2007_08)) {
            final String navigationPropertyName = rel.substring(Edm.NAMESPACE_REL_2007_08.length());
            entryMetadata.putAssociationUri(navigationPropertyName, uri);
        } else if (rel.equals(Edm.LINK_REL_EDIT_MEDIA)) {
            mediaMetadata.setEditLink(uri);
            mediaMetadata.setEtag(etag);
        }

        if (!reader.isEndElement() && rel != null && rel.startsWith(Edm.NAMESPACE_REL_2007_08)) {
            readInlineContent(reader, eia, readProperties, type, rel);
        }
    }

    /**
     * Inline content was found and {@link XMLStreamReader} already points to <m:inline> tag.
     *
     * @param reader
     * @param eia
     * @param readProperties
     * @param atomLinkType the atom <code>type</code> attribute value of the <code>link</code> tag
     * @param atomLinkRel the atom <code>rel</code> attribute value of the <code>link</code> tag
     * @throws XMLStreamException
     * @throws EntityProviderException
     * @throws EdmException
     */
    private void readInlineContent(final XMLStreamReader reader, final EntityInfoAggregator eia,
            final DeserializerProperties readProperties, final String atomLinkType, final String atomLinkRel)
            throws XMLStreamException, EntityProviderException, EdmException {

        //
        String navigationPropertyName = atomLinkRel.substring(Edm.NAMESPACE_REL_2007_08.length());

        EdmNavigationProperty navigationProperty = (EdmNavigationProperty) eia.getEntityType()
                                                                              .getProperty(navigationPropertyName);
        EdmEntitySet entitySet = eia.getEntitySet()
                                    .getRelatedEntitySet(navigationProperty);
        EntityInfoAggregator inlineEia = EntityInfoAggregator.create(entitySet);

        final DeserializerProperties inlineProperties = createInlineProperties(readProperties, navigationProperty);

        // validations
        boolean isFeed = isInlineFeedValidated(reader, atomLinkType, navigationProperty);

        List<ODataEntry> inlineEntries = new ArrayList<ODataEntry>();

        while ((!reader.isEndElement() || !Edm.NAMESPACE_M_2007_08.equals(reader.getNamespaceURI())
                || !FormatXml.M_INLINE.equals(reader.getLocalName()))) {

            if (reader.isStartElement() && Edm.NAMESPACE_ATOM_2005.equals(reader.getNamespaceURI())
                    && FormatXml.ATOM_ENTRY.equals(reader.getLocalName())) {
                XmlEntryDeserializer xec = new XmlEntryDeserializer();
                ODataEntry inlineEntry = xec.readEntry(reader, inlineEia, inlineProperties, true);
                inlineEntries.add(inlineEntry);
            }
            // next tag
            reader.next();
        }

        updateReadProperties(navigationPropertyName, isFeed, inlineEntries);

        reader.require(XMLStreamConstants.END_ELEMENT, Edm.NAMESPACE_M_2007_08, FormatXml.M_INLINE);
    }

    /**
     * Updates the read properties ({@link #properties}) for this {@link ReadEntryResult}
     * ({@link #readEntryResult}).
     *
     * @param readProperties
     * @param navigationPropertyName
     * @param navigationProperty
     * @param isFeed
     * @param inlineEntries
     * @throws EntityProviderException
     */
    private void updateReadProperties(final String navigationPropertyName, final boolean isFeed, final List<ODataEntry> inlineEntries) {
        Object entry = extractODataEntity(isFeed, inlineEntries);
        readEntryResult.setContainsInlineEntry(true);
        properties.put(navigationPropertyName, entry);

    }



    /**
     * Get a list of {@link ODataEntry}, an empty list, a single {@link ODataEntry} or <code>NULL</code>
     * based on <code>isFeed</code> value and <code>inlineEntries</code> content.
     *
     * @param isFeed
     * @param inlineEntries
     * @return
     */
    private Object extractODataEntity(final boolean isFeed, final List<ODataEntry> inlineEntries) {
        if (isFeed) {
            return new ODataDeltaFeedImpl(inlineEntries, new FeedMetadataImpl());
        }
        if (!inlineEntries.isEmpty()) {
            return inlineEntries.get(0);
        }
        return null;
    }


    /**
     * Create {@link EntityProviderReadProperties} which can be used for reading of inline
     * properties/entrys of navigation links within this current read entry.
     *
     * @param readProperties
     * @param navigationProperty
     * @return
     * @throws EntityProviderException
     */
    private DeserializerProperties createInlineProperties(final DeserializerProperties readProperties,
            final EdmNavigationProperty navigationProperty) throws EntityProviderException {
        final OnDeserializeInlineContent callback = readProperties.getCallback();

        DeserializerProperties currentReadProperties = DeserializerProperties.initFrom(readProperties)
                                                                             .build();
        if (callback == null) {
            return currentReadProperties;
        }
        try {
            return callback.receiveReadProperties(currentReadProperties, navigationProperty);
        } catch (ODataApplicationException e) {
            throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
                                                                                                     .getSimpleName()),
                    e);
        }
    }

    /**
     * <p>
     * Inline content was found and {@link XMLStreamReader} already points to
     * <code><m:inline> tag</code>. <br/>
     * <b>ATTENTION</b>: If {@link XMLStreamReader} does not point to the <code><m:inline> tag</code> an
     * exception is thrown.
     * </p>
     * <p>
     * Check whether it is an inline <code>Feed</code> or <code>Entry</code> and validate that...
     * <ul>
     * <li>...{@link FormatXml#M_INLINE} tag is correctly set.</li>
     * <li>...based on {@link EdmMultiplicity} of {@link EdmNavigationProperty} all tags are correctly
     * set.</li>
     * <li>...{@link FormatXml#ATOM_TYPE} tag is correctly set and according to
     * {@link FormatXml#ATOM_ENTRY} or {@link FormatXml#ATOM_FEED} to following tags are available.</li>
     * </ul>
     *
     * For the case that one of above validations fail an {@link EntityProviderException} is thrown. If
     * validation was successful <code>true</code> is returned for <code>Feed</code> and
     * <code>false</code> for <code>Entry</code> multiplicity.
     * </p>
     *
     * @param reader xml content reader which already points to <code><m:inline> tag</code>
     * @param eia all necessary information about the entity
     * @param type the atom type attribute value of the <code>link</code> tag
     * @param navigationProperty the navigation property name of the entity
     * @return <code>true</code> for <code>Feed</code> and <code>false</code> for <code>Entry</code>
     * @throws EntityProviderException is thrown if at least one validation fails.
     * @throws EdmException if edm access fails
     */
    private boolean isInlineFeedValidated(final XMLStreamReader reader, final String type, final EdmNavigationProperty navigationProperty)
            throws EntityProviderException, EdmException {
        boolean isFeed = false;
        try {
            reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_M_2007_08, FormatXml.M_INLINE);
            //
            ContentType cType = ContentType.parse(type);
            if (cType == null) {
                throw new EntityProviderException(EntityProviderException.INVALID_INLINE_CONTENT.addContent("xml data"));
            }
            EdmMultiplicity navigationMultiplicity = navigationProperty.getMultiplicity();

            switch (navigationMultiplicity) {
                case MANY:
                    validateFeedTags(reader, cType);
                    isFeed = true;
                    break;
                case ONE:
                case ZERO_TO_ONE:
                    validateEntryTags(reader, cType);
                    break;
            }
        } catch (XMLStreamException e) {
            throw new EntityProviderException(EntityProviderException.INVALID_INLINE_CONTENT.addContent("xml data"), e);
        }
        return isFeed;
    }

    private void validateEntryTags(final XMLStreamReader reader, final ContentType cType)
            throws XMLStreamException, EntityProviderException {
        if (!FormatXml.ATOM_ENTRY.equals(cType.getParameters()
                                              .get(FormatXml.ATOM_TYPE))) {
            throw new EntityProviderException(EntityProviderException.INVALID_INLINE_CONTENT.addContent("entry"));
        }
        int next = reader.nextTag();
        if (XMLStreamConstants.START_ELEMENT == next) {
            reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_ENTRY);
        } else {
            reader.require(XMLStreamConstants.END_ELEMENT, Edm.NAMESPACE_M_2007_08, FormatXml.M_INLINE);
        }
    }

    private void validateFeedTags(final XMLStreamReader reader, final ContentType cType)
            throws XMLStreamException, EntityProviderException {
        if (!FormatXml.ATOM_FEED.equals(cType.getParameters()
                                             .get(FormatXml.ATOM_TYPE))) {
            throw new EntityProviderException(EntityProviderException.INVALID_INLINE_CONTENT.addContent("feed"));
        }
        int next = reader.nextTag();
        if (XMLStreamConstants.START_ELEMENT == next) {
            reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_FEED);
        } else {
            reader.require(XMLStreamConstants.END_ELEMENT, Edm.NAMESPACE_M_2007_08, FormatXml.M_INLINE);
        }
    }

    private void readContent(final XMLStreamReader reader, final EntityInfoAggregator eia, final DeserializerProperties readProperties)
            throws EntityProviderException, XMLStreamException, EdmException {
        reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_CONTENT);

        final String contentType = reader.getAttributeValue(null, FormatXml.ATOM_TYPE);
        final String sourceLink = reader.getAttributeValue(null, FormatXml.ATOM_SRC);

        reader.nextTag();

        if (reader.isStartElement() && reader.getLocalName()
                                             .equals(FormatXml.M_PROPERTIES)) {
            readProperties(reader, eia, readProperties);
        } else if (reader.isEndElement()) {
            reader.require(XMLStreamConstants.END_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_CONTENT);
        } else {
            throw new EntityProviderException(EntityProviderException.INVALID_STATE.addContent(
                    "Expected closing 'content' or starting 'properties' but found '" + reader.getLocalName() + "'."));
        }

        mediaMetadata.setContentType(contentType);
        mediaMetadata.setSourceLink(sourceLink);
    }

    private void readId(final XMLStreamReader reader) throws EntityProviderException, XMLStreamException {
        reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_ID);
        entryMetadata.setId(reader.getElementText());
        reader.require(XMLStreamConstants.END_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_ID);
    }

    private void readProperties(final XMLStreamReader reader, final EntityInfoAggregator entitySet, // NOSONAR
            final DeserializerProperties readProperties) throws XMLStreamException, EdmException, EntityProviderException {
        // validate namespace
        reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_M_2007_08, FormatXml.M_PROPERTIES);
        if (entitySet.getEntityType()
                     .hasStream()) {
            // external properties
            checkCurrentHandledStartTag(FormatXml.M_PROPERTIES);
        } else {
            // inline properties
            checkCurrentHandledStartTag(FormatXml.ATOM_CONTENT);
        }

        EntityPropertyInfo property;
        XmlPropertyDeserializer xpc = new XmlPropertyDeserializer();

        String closeTag = null;
        boolean run = true;
        reader.next();

        while (run) {
            if (reader.isStartElement() && closeTag == null) {
                closeTag = reader.getLocalName();
                if (isEdmNamespaceProperty(reader)) {
                    if (properties.containsKey(closeTag)) {
                        throw new EntityProviderException(EntityProviderException.DOUBLE_PROPERTY.addContent(closeTag));
                    }
                    property = getValidatedPropertyInfo(entitySet, closeTag);
                    final Object value = xpc.readStartedElement(reader, closeTag, property, typeMappings, readProperties);
                    properties.put(closeTag, value);
                    closeTag = null;
                }
            } else if (reader.isEndElement()) {
                if (reader.getLocalName()
                          .equals(closeTag)) {
                    closeTag = null;
                } else if (Edm.NAMESPACE_M_2007_08.equals(reader.getNamespaceURI())
                        && FormatXml.M_PROPERTIES.equals(reader.getLocalName())) {
                    run = false;
                }
            }
            reader.next();
        }
    }

    /**
     * Check if the {@link #currentHandledStartTagName} is the same as the <code>expectedTagName</code>.
     * If tag name is not as expected or if {@link #currentHandledStartTagName} is not set an
     * {@link EntityProviderException} is thrown.
     *
     * @param expectedTagName expected name for {@link #currentHandledStartTagName}
     * @throws EntityProviderException if tag name is not as expected or if
     *         {@link #currentHandledStartTagName} is <code>NULL</code>.
     */
    private void checkCurrentHandledStartTag(final String expectedTagName) throws EntityProviderException {
        if (currentHandledStartTagName == null) {
            throw new EntityProviderException(EntityProviderException.INVALID_STATE.addContent("No current handled start tag name set."));
        }
        if (!currentHandledStartTagName.equals(expectedTagName)) {
            throw new EntityProviderException(EntityProviderException.INVALID_PARENT_TAG.addContent(expectedTagName)
                                                                                        .addContent(currentHandledStartTagName));
        }
    }

    /**
     * Checks if property of currently read tag in {@link XMLStreamReader} is defined in
     * <code>edm properties namespace</code> {@value Edm#NAMESPACE_D_2007_08}.
     *
     * If no namespace uri definition is found for namespace prefix of property (<code>tag</code>) an
     * exception is thrown.
     *
     * @param reader {@link XMLStreamReader} with position at to checked tag
     * @return <code>true</code> if property is in <code>edm properties namespace</code>, otherwise
     *         <code>false</code>.
     * @throws EntityProviderException If no namespace uri definition is found for namespace prefix of
     *         property (<code>tag</code>).
     */
    private boolean isEdmNamespaceProperty(final XMLStreamReader reader) throws EntityProviderException {
        final String nsUri = reader.getNamespaceURI();
        if (nsUri == null) {
            throw new EntityProviderException(EntityProviderException.INVALID_NAMESPACE.addContent(reader.getLocalName()));
        }
        return Edm.NAMESPACE_D_2007_08.equals(nsUri);
    }

    /**
     * Get validated {@link EntityPropertyInfo} for property with given <code>name</code>. If validation
     * fails an {@link EntityProviderException} is thrown.
     *
     * Currently this is the case if no {@link EntityPropertyInfo} if found for given <code>name</code>.
     *
     * @param entitySet
     * @param name
     * @return valid {@link EntityPropertyInfo} (which is never <code>NULL</code>).
     * @throws EntityProviderException
     */
    private EntityPropertyInfo getValidatedPropertyInfo(final EntityInfoAggregator entitySet, final String name)
            throws EntityProviderException {
        EntityPropertyInfo info = entitySet.getPropertyInfo(name);
        if (info == null) {
            throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY.addContent(name));
        }
        return info;
    }
}
