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
package org.apache.olingo.odata2.api.ep;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;

/**
 * {@link EntityProviderWriteProperties} contains all additional properties which are necessary to <b>write
 * (serialize)</b> an {@link org.apache.olingo.odata2.api.ep.entry.ODataEntry} into an specific format (e.g.
 * <code>XML</code> or <code>JSON</code> or ...).
 */
public class EntityProviderWriteProperties {

  private URI serviceRoot;
  private InlineCount inlineCountType;
  private Integer inlineCount;
  private String nextLink;
  private ExpandSelectTreeNode expandSelectTree;
  private Map<String, ODataCallback> callbacks = Collections.emptyMap();
  private URI selfLink;
  private boolean includeSimplePropertyType;
  private Map<String, Map<String, Object>> additionalLinks;
  private boolean omitJsonWrapper;
  private boolean contentOnly;
  private boolean omitETag;
  private boolean validatingFacets = true;

  private boolean isClientRequest = false;
  private boolean includeMetadataInContentOnly = false;

  private EntityProviderWriteProperties() {}

  public final boolean isOmitETag() {
    return omitETag;
  }

  public final boolean isContentOnly() {
    return contentOnly;
  }

  public final boolean isOmitJsonWrapper() {
    return omitJsonWrapper;
  }

  /**
   * Returns if type information of simple properties should be in the payload.
   * @return true if information should be in the payload.
   */
  public final boolean isIncludeSimplePropertyType() {
    return includeSimplePropertyType;
  }

  /**
   * Gets the self link from an application. May be null.
   * @return the self link
   */
  public final URI getSelfLink() {
    return selfLink;
  }

  /**
   * Gets the service root.
   * @return the service root
   */
  public final URI getServiceRoot() {
    return serviceRoot;
  }

  /**
   * Gets the type of the inlinecount request from the system query option.
   * @return the type of the inlinecount request from the system query option
   */
  public final InlineCount getInlineCountType() {
    return inlineCountType;
  }

  public final Map<String, ODataCallback> getCallbacks() {
    return callbacks;
  }

  /**
   * Gets the expand select tree data structure resulting from $expand and $select query options.
   * @return a parsed tree structure representing the $expand and $select
   */
  public final ExpandSelectTreeNode getExpandSelectTree() {
    return expandSelectTree;
  }

  /**
   * Gets the inlinecount.
   * @return the inlinecount as Integer
   * @see #getInlineCountType
   */
  public final Integer getInlineCount() {
    return inlineCount;
  }

  /**
   * Gets the next link used for server-side paging of feeds.
   * @return the next link
   */
  public final String getNextLink() {
    return nextLink;
  }

  /**
   * Gets the additional links that should be in the payload.
   * @return the additional links as Map where the navigation-property name is the key and
   * a key predicate is the value -
   * a key predicate is a Map from key-property names to their values
   */
  public final Map<String, Map<String, Object>> getAdditionalLinks() {
    return additionalLinks;
  }

  public static ODataEntityProviderPropertiesBuilder serviceRoot(final URI serviceRoot) {
    return new ODataEntityProviderPropertiesBuilder().serviceRoot(serviceRoot);
  }

  public boolean isValidatingFacets() {
    return validatingFacets;
  }

  public boolean isClientRequest() {
    return isClientRequest;
  }

  public boolean isIncludeMetadataInContentOnly() {
    return includeMetadataInContentOnly;
  }

  public static class ODataEntityProviderPropertiesBuilder {
    private final EntityProviderWriteProperties properties = new EntityProviderWriteProperties();

    /**
     * @param includeSimplePropertyType true to include simple property type information in the payload
     */
    public final ODataEntityProviderPropertiesBuilder includeSimplePropertyType(
        final boolean includeSimplePropertyType) {
      properties.includeSimplePropertyType = includeSimplePropertyType;
      return this;
    }

    /**
     * @param inlineCountType the inlineCountType to set
     */
    public final ODataEntityProviderPropertiesBuilder inlineCountType(final InlineCount inlineCountType) {
      properties.inlineCountType = inlineCountType;
      return this;
    }

    /**
     * @param inlineCount the inlineCount to set
     */
    public final ODataEntityProviderPropertiesBuilder inlineCount(final Integer inlineCount) {
      properties.inlineCount = inlineCount;
      return this;
    }

    /**
     * @param serviceRoot
     */
    public final ODataEntityProviderPropertiesBuilder serviceRoot(final URI serviceRoot) {
      properties.serviceRoot = serviceRoot;
      return this;
    }

    /**
     * @param nextLink Next link to render feeds with server side paging. Should usually contain a skiptoken.
     */
    public ODataEntityProviderPropertiesBuilder nextLink(final String nextLink) {
      properties.nextLink = nextLink;
      return this;
    }

    /**
     * Build properties object.
     * @return assembled properties object
     */
    public final EntityProviderWriteProperties build() {
      return properties;
    }

    /**
     * Set a expand select tree which results from $expand and $select query parameter. Usually the data structure is
     * constructed by the URI parser.
     * @param expandSelectTree data structure
     * @return properties builder
     */
    public ODataEntityProviderPropertiesBuilder expandSelectTree(final ExpandSelectTreeNode expandSelectTree) {
      properties.expandSelectTree = expandSelectTree;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder callbacks(final Map<String, ODataCallback> callbacks) {
      properties.callbacks = callbacks;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder selfLink(final URI selfLink) {
      properties.selfLink = selfLink;
      return this;
    }

    /**
     * Sets additional links from this entity to other entities.
     * @param links a Map where the navigation-property name is the key and
     * a key predicate is the value -
     * a key predicate is a Map from key-property names to their values
     * @return properties builder
     */
    public ODataEntityProviderPropertiesBuilder additionalLinks(final Map<String, Map<String, Object>> links) {
      properties.additionalLinks = links;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder omitJsonWrapper(final boolean omitJsonWrapper) {
      properties.omitJsonWrapper = omitJsonWrapper;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder contentOnly(final boolean contentOnly) {
      properties.contentOnly = contentOnly;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder omitETag(final boolean omitETag) {
      properties.omitETag = omitETag;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder validatingFacets(final boolean validatingFacets) {
      properties.validatingFacets = validatingFacets;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder clientRequest(final boolean isClientRequest) {
      properties.isClientRequest = isClientRequest;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder
        includeMetadataInContentOnly(final boolean includeMetadataInContentOnly) {
      properties.includeMetadataInContentOnly = includeMetadataInContentOnly;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder fromProperties(final EntityProviderWriteProperties properties) {
      this.properties.inlineCountType = properties.getInlineCountType();
      this.properties.inlineCount = properties.getInlineCount();
      this.properties.nextLink = properties.getNextLink();
      this.properties.expandSelectTree = properties.getExpandSelectTree();
      this.properties.callbacks = properties.getCallbacks();
      this.properties.selfLink = properties.getSelfLink();
      this.properties.includeSimplePropertyType = properties.includeSimplePropertyType;
      this.properties.additionalLinks = properties.additionalLinks;
      this.properties.omitJsonWrapper = properties.omitJsonWrapper;
      this.properties.contentOnly = properties.contentOnly;
      this.properties.omitETag = properties.omitETag;
      this.properties.validatingFacets = properties.validatingFacets;
      this.properties.isClientRequest = properties.isClientRequest;
      this.properties.includeMetadataInContentOnly = properties.includeMetadataInContentOnly;
      return this;
    }

  }

  public static ODataEntityProviderPropertiesBuilder fromProperties(final EntityProviderWriteProperties properties) {
    final ODataEntityProviderPropertiesBuilder builder =
        EntityProviderWriteProperties.serviceRoot(properties.getServiceRoot());
    return builder.fromProperties(properties);
  }
}
