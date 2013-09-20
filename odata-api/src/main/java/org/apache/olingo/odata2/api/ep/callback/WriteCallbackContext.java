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
package org.apache.olingo.odata2.api.ep.callback;

import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;

/**
 * Wrapper for {@link WriteEntryCallbackContext} and {@link WriteFeedCallbackContext}.
 * @org.apache.olingo.odata2.DoNotImplement
 * 
 */
public abstract class WriteCallbackContext {
  private EdmEntitySet sourceEntitySet;
  private EdmNavigationProperty navigationProperty;
  private Map<String, Object> entryData;
  private ExpandSelectTreeNode currentNode;

  /**
   * Current means the node pointing to the target entity set
   * @return the current node of the expand select tree
   */
  public ExpandSelectTreeNode getCurrentExpandSelectTreeNode() {
    return currentNode;
  }

  /**
   * Do Not Call This Method!
   * @param currentNode
   */
  public void setCurrentExpandSelectTreeNode(final ExpandSelectTreeNode currentNode) {
    this.currentNode = currentNode;
  }

  /**
   * Returns entity set which contains an entry that should be expanded
   * @return source entity set
   */
  public EdmEntitySet getSourceEntitySet() {
    return sourceEntitySet;
  }

  /**
   * Do Not Call This Method!
   * @param entitySet
   */
  public void setSourceEntitySet(final EdmEntitySet entitySet) {
    sourceEntitySet = entitySet;
  }

  /**
   * Navigation property which is contained in the expand clause.
   * @return navigation property pointing to the entity which has to be expanded.
   */
  public EdmNavigationProperty getNavigationProperty() {
    return navigationProperty;
  }

  /**
   * Do Not Call This Method!
   * @param navigationProperty
   */
  public void setNavigationProperty(final EdmNavigationProperty navigationProperty) {
    this.navigationProperty = navigationProperty;
  }

  /**
   * Source entry data which was just serialized.
   * @return data of the source entry
   */
  public Map<String, Object> getEntryData() {
    return entryData;
  }

  /**
   * Do Not Call This Method!
   * @param entryData
   */
  public void setEntryData(final Map<String, Object> entryData) {
    this.entryData = entryData;
  }

  /**
   * @return the key of the current entry as a Map<String,Object>
   * @throws EntityProviderException in case of an {@link EdmException}
   */
  public Map<String, Object> extractKeyFromEntryData() throws EntityProviderException {
    HashMap<String, Object> key = new HashMap<String, Object>();
    try {
      for (String keyPropertyName : sourceEntitySet.getEntityType().getKeyPropertyNames()) {
        key.put(keyPropertyName, entryData.get(keyPropertyName));
      }
    } catch (EdmException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
    return key;
  }
}
