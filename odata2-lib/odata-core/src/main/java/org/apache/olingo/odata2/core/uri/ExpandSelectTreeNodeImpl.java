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
package org.apache.olingo.odata2.core.uri;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *  
 */
public class ExpandSelectTreeNodeImpl extends ExpandSelectTreeNode {

  public enum AllKinds {
    IMPLICITLYTRUE(true), EXPLICITLYTRUE(true), FALSE(false);

    private boolean booleanRepresentation;

    private AllKinds(final boolean booleanRepresentation) {
      this.booleanRepresentation = booleanRepresentation;
    }

    public boolean getBoolean() {
      return booleanRepresentation;
    }
  }

  private AllKinds isAll = AllKinds.IMPLICITLYTRUE;
  private boolean isExplicitlySelected = false;
  private boolean isExpanded = false;
  private final List<EdmProperty> properties = new ArrayList<EdmProperty>();
  private final Map<String, ExpandSelectTreeNodeImpl> links = new HashMap<String, ExpandSelectTreeNodeImpl>();

  @Override
  public boolean isAll() {
    return isAll.getBoolean();
  }

  @Override
  public List<EdmProperty> getProperties() {
    return properties;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, ExpandSelectTreeNode> getLinks() {
    return (Map<String, ExpandSelectTreeNode>) ((Map<String, ? extends ExpandSelectTreeNode>) Collections
        .unmodifiableMap(links));
  }

  public void putLink(final String name, final ExpandSelectTreeNodeImpl node) {
    links.put(name, node);
  }

  public void removeLink(final String name) {
    links.remove(name);
  }

  public boolean isExplicitlySelected() {
    return isExplicitlySelected;
  }

  public void setExplicitlySelected() {
    isExplicitlySelected = true;
    setAllExplicitly();
  }

  public boolean isExpanded() {
    return isExpanded;
  }

  public void setExpanded() {
    isExpanded = true;
  }

  public void addProperty(final EdmProperty property) {
    if (property != null && isAll != AllKinds.EXPLICITLYTRUE && !properties.contains(property)) {
      properties.add(property);
      isAll = AllKinds.FALSE;
    }
  }

  public void setAllExplicitly() {
    properties.clear();
    isAll = AllKinds.EXPLICITLYTRUE;
  }

  public AllKinds getAllKind() {
    return isAll;
  }

  public void setAllKindFalse() {
    isAll = AllKinds.FALSE;
  }

  public String toJsonString() {
    try {
      StringWriter writer = new StringWriter();
      JsonStreamWriter jsonStreamWriter = new JsonStreamWriter(writer);
      jsonStreamWriter.beginObject()
          .name("all").unquotedValue(Boolean.toString(isAll())).separator()
          .name("properties")
          .beginArray();
      boolean first = true;
      for (EdmProperty property : properties) {
        if (first) {
          first = false;
        } else {
          jsonStreamWriter.separator();
        }
        jsonStreamWriter.stringValueRaw(property.getName());
      }
      jsonStreamWriter.endArray().separator()
          .name("links")
          .beginArray();
      first = true;
      for (Map.Entry<String, ExpandSelectTreeNodeImpl> entry : links.entrySet()) {
        if (first) {
          first = false;
        } else {
          jsonStreamWriter.separator();
        }
        final String nodeString = entry.getValue() == null ? null : entry.getValue().toJsonString();
        jsonStreamWriter.beginObject()
            .name(entry.getKey()).unquotedValue(nodeString)
            .endObject();
      }
      jsonStreamWriter.endArray()
          .endObject();
      writer.flush();
      return writer.toString();
    } catch (final IOException e) {
      throw new ODataRuntimeException("IOException: ", e);
    } catch (final EdmException e) {
      throw new ODataRuntimeException("EdmException: ", e);
    }
  }

  public class ExpandSelectTreeNodeBuilderImpl extends ExpandSelectTreeNodeBuilder {

    private EdmEntitySet entitySet;
    private List<String> selectedPropertyNames;
    private List<String> selectedNavigationPropertyNames;
    private Map<String, ExpandSelectTreeNode> customExpandedNavigationProperties;
    private List<String> expandedNavigationPropertyNames;

    @Override
    public ExpandSelectTreeNodeBuilder entitySet(final EdmEntitySet entitySet) {
      this.entitySet = entitySet;
      return this;
    }

    @Override
    public ExpandSelectTreeNode build() throws EdmException {
      EdmEntityType entityType = entitySet.getEntityType();
      if (selectedPropertyNames != null) {
        handleProperties(entityType);
      }

      if (selectedNavigationPropertyNames != null) {
        setAllKindFalse();
        handleLinks(entityType, selectedNavigationPropertyNames, null);
      }

      if (expandedNavigationPropertyNames != null) {
        ExpandSelectTreeNodeImpl subNode = new ExpandSelectTreeNodeImpl();
        subNode.setExplicitlySelected();
        handleLinks(entityType, expandedNavigationPropertyNames, subNode);
      }

      if (customExpandedNavigationProperties != null) {
        handleCustomLinks(entityType);
      }

      return ExpandSelectTreeNodeImpl.this;
    }

    private void handleCustomLinks(final EdmEntityType entityType) throws EdmException {
      for (Map.Entry<String, ExpandSelectTreeNode> entry : customExpandedNavigationProperties.entrySet()) {
        EdmTyped navigationProperty = entityType.getProperty(entry.getKey());
        if (navigationProperty == null) {
          throw new EdmException(EdmException.NAVIGATIONPROPERTYNOTFOUND.addContent(entry.getKey()));
        }
        if (!(navigationProperty instanceof EdmNavigationProperty)) {
          throw new EdmException(EdmException.MUSTBENAVIGATIONPROPERTY.addContent(entry.getKey()));
        }
        putLink(entry.getKey(), (ExpandSelectTreeNodeImpl) entry.getValue());
      }
    }

    private void handleLinks(final EdmEntityType entityType, final List<String> names,
        final ExpandSelectTreeNodeImpl subNode) throws EdmException {
      for (String navigationPropertyName : names) {
        EdmTyped navigationProperty = entityType.getProperty(navigationPropertyName);
        if (navigationProperty == null) {
          throw new EdmException(EdmException.NAVIGATIONPROPERTYNOTFOUND.addContent(navigationPropertyName));
        } else if (!(navigationProperty instanceof EdmNavigationProperty)) {
          throw new EdmException(EdmException.MUSTBENAVIGATIONPROPERTY.addContent(navigationPropertyName));
        }
        putLink(navigationPropertyName, subNode);
      }
    }

    private void handleProperties(final EdmEntityType entityType) throws EdmException {
      for (String propertyName : selectedPropertyNames) {
        EdmTyped property = entityType.getProperty(propertyName);
        if (property == null) {
          throw new EdmException(EdmException.PROPERTYNOTFOUND.addContent(propertyName));
        } else if (!(property instanceof EdmProperty)) {
          throw new EdmException(EdmException.MUSTBEPROPERTY.addContent(propertyName));
        }
        addProperty((EdmProperty) property);
      }
    }

    @Override
    public ExpandSelectTreeNodeBuilder selectedProperties(final List<String> selectedPropertyNames) {
      this.selectedPropertyNames = selectedPropertyNames;
      return this;
    }

    @Override
    public ExpandSelectTreeNodeBuilder selectedLinks(final List<String> selectedNavigationPropertyNames) {
      this.selectedNavigationPropertyNames = selectedNavigationPropertyNames;
      return this;
    }

    @Override
    public ExpandSelectTreeNodeBuilder
        customExpandedLink(final String navigationPropertyName, final ExpandSelectTreeNode expandNode) {
      if (expandNode == null) {
        throw new ODataRuntimeException("ExpandNode must not be null");
      }
      if (customExpandedNavigationProperties == null) {
        customExpandedNavigationProperties = new HashMap<String, ExpandSelectTreeNode>();
      }
      customExpandedNavigationProperties.put(navigationPropertyName, expandNode);
      return this;
    }

    @Override
    public ExpandSelectTreeNodeBuilder expandedLinks(final List<String> navigationPropertyNames) {
      expandedNavigationPropertyNames = navigationPropertyNames;
      return this;
    }

  }

}
