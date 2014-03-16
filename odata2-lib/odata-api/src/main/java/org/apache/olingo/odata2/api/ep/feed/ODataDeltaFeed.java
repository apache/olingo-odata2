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
package org.apache.olingo.odata2.api.ep.feed;

import java.util.List;

import org.apache.olingo.odata2.api.ep.entry.DeletedEntryMetadata;

public interface ODataDeltaFeed extends ODataFeed {

  /**
   * Delta responses can contain changed feed data and deleted entries metadata. A delta response it the result of
   * a delta link. If the feed is not a delta feed then the list of deleted entries is null.
   * @return metadata of deleted entries in case of feed is result of a delta response or null
   */
  public List<DeletedEntryMetadata> getDeletedEntries();

}
