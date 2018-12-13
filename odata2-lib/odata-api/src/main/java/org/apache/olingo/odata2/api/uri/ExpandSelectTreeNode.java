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
package org.apache.olingo.odata2.api.uri;

import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;

/**
 * Expression tree node with information about selected properties and to be expanded links.
 * @org.apache.olingo.odata2.DoNotImplement
 * 
 */
public abstract class ExpandSelectTreeNode {

  /**
   * Determines whether all properties (including navigation properties) have been selected.
   */
  public abstract boolean isAll();

  /**
   * <p>Gets the list of explicitly selected {@link EdmProperty properties}.</p>
   * <p>This list does not contain any navigation properties.
   * It is empty if {@link #isAll()} returns <code>true</code>.</p>
   * @return List of selected properties
   */
  public abstract List<EdmProperty> getProperties();

  /**
   * Gets the links that have to be included or expanded.
   * @return a Map from EdmNavigationProperty Name to its related {@link ExpandSelectTreeNode};
   * if that node is <code>null</code>, a deferred link has been requested,
   * otherwise the link must be expanded with information found in that node
   */
  public abstract Map<String, ExpandSelectTreeNode> getLinks();
  
  /**
   * A list of all expanded links within the parent entity. 
   * @return {@link ExpandSelectTreeNodeBuilder} for method chaining.
   */
  public abstract List<ExpandSelectTreeNode> getExpandedList();
  
  /**
   * Creates a builder instance and sets the entitySet for this node.
   * @param entitySet on which this node is based
   * @return {@link ExpandSelectTreeNodeBuilder} to build the node
   */
  public static ExpandSelectTreeNodeBuilder entitySet(final EdmEntitySet entitySet) {
    return ExpandSelectTreeNodeBuilder.newInstance().entitySet(entitySet);
  }

  /**
   * Builder interface
   */
  public static abstract class ExpandSelectTreeNodeBuilder {

    /**
     * Uses the runtime delegate to create a new instance
     * @return instance of {@link ExpandSelectTreeNodeBuilder}
     */
    private static ExpandSelectTreeNodeBuilder newInstance() {
      return RuntimeDelegate.createExpandSelectTreeNodeBuilder();
    }

    /**
     * Sets the entitySet for this node.
     * @param entitySet must not be null
     * @return {@link ExpandSelectTreeNodeBuilder} for method chaining.
     */
    public abstract ExpandSelectTreeNodeBuilder entitySet(EdmEntitySet entitySet);

    /**
     * Will close this builder and return an {@link ExpandSelectTreeNode}. All properties and navigation properties will
     * be validated if they exist for the entity set.
     * @return {@link ExpandSelectTreeNodeBuilder} for method chaining.
     * @throws EdmException in case property or navigation property validation fails.
     */
    public abstract ExpandSelectTreeNode build() throws EdmException;

    /**
     * A list of properties which are selected. Selected means that they appear in the payload during serialization.
     * MUST NOT CONTAIN navigation properties.
     * @param selectedPropertyNames
     * @return {@link ExpandSelectTreeNodeBuilder} for method chaining.
     */
    public abstract ExpandSelectTreeNodeBuilder selectedProperties(List<String> selectedPropertyNames);

    /**
     * A list of selected links. Selected means they appear as links in the payload. If a link should be
     * expanded they navigation property does not need to appear here but can. Expanded links will win over selected
     * links.
     * @param selectedNavigationPropertyNames
     * @return {@link ExpandSelectTreeNodeBuilder} for method chaining.
     */
    public abstract ExpandSelectTreeNodeBuilder selectedLinks(List<String> selectedNavigationPropertyNames);

    /**
     * Sets a link to be expanded with a custom node. With this the inline content can either also be expanded or
     * selected. Custom nodes for a navigation properties will win over navigation properties which are also specified
     * in the expanded links list. Example: if a link A is set with a custom node and A appears in the expanded link
     * list it will be expanded with the custom node.
     * @param navigationPropertyName
     * @param expandNode must not be null
     * @return {@link ExpandSelectTreeNodeBuilder} for method chaining.
     */
    public abstract ExpandSelectTreeNodeBuilder customExpandedLink(String navigationPropertyName,
        ExpandSelectTreeNode expandNode);

    /**
     * A list of expanded links. Expanded means their content will be shown as inline entry or feed in the payload but a
     * callback MUST BE registered to get the content for the inline content. The inline content will appear with all
     * properties and links. If this is not needed use the customExpandedLink method to set a custom node for this
     * expanded link. Expanded links will win over selected links. If a custom node was set for a particular link it
     * will win over a link that is specified in this list.
     * @param navigationPropertyNames
     * @return {@link ExpandSelectTreeNodeBuilder} for method chaining.
     */
    public abstract ExpandSelectTreeNodeBuilder expandedLinks(List<String> navigationPropertyNames);
  }
}