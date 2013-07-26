/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.ep.entry;

import java.util.Map;

import org.apache.olingo.odata2.api.ep.entry.EntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.MediaMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.core.uri.ExpandSelectTreeNodeImpl;

/**
 * @author SAP AG
 */
public class ODataEntryImpl implements ODataEntry {

  private final Map<String, Object> data;
  private final EntryMetadata entryMetadata;
  private final MediaMetadata mediaMetadata;
  private final ExpandSelectTreeNode expandSelectTree;
  private boolean containsInlineEntry;

  public ODataEntryImpl(final Map<String, Object> data, final MediaMetadata mediaMetadata, final EntryMetadata entryMetadata, final ExpandSelectTreeNodeImpl expandSelectTree) {
    this(data, mediaMetadata, entryMetadata, expandSelectTree, false);
  }

  public ODataEntryImpl(final Map<String, Object> data, final MediaMetadata mediaMetadata, final EntryMetadata entryMetadata, final ExpandSelectTreeNode expandSelectTree, final boolean containsInlineEntry) {
    this.data = data;
    this.entryMetadata = entryMetadata;
    this.mediaMetadata = mediaMetadata;
    this.expandSelectTree = expandSelectTree;
    this.containsInlineEntry = containsInlineEntry;
  }

  @Override
  public Map<String, Object> getProperties() {
    return data;
  }

  @Override
  public MediaMetadata getMediaMetadata() {
    return mediaMetadata;
  }

  @Override
  public EntryMetadata getMetadata() {
    return entryMetadata;
  }

  @Override
  public boolean containsInlineEntry() {
    return containsInlineEntry;
  }

  @Override
  public ExpandSelectTreeNode getExpandSelectTree() {
    return expandSelectTree;
  }

  public void setContainsInlineEntry(final boolean containsInlineEntry) {
    this.containsInlineEntry = containsInlineEntry;
  }

  @Override
  public String toString() {
    return "ODataEntryImpl [data=" + data + ", "
        + "entryMetadata=" + entryMetadata + ", "
        + "mediaMetadata=" + mediaMetadata + ", "
        + "expandSelectTree=" + expandSelectTree + ", "
        + "containsInlineEntry=" + containsInlineEntry + "]";
  }
}
