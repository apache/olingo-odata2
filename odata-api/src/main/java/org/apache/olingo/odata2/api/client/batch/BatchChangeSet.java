/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.api.client.batch;

import java.util.List;

import org.apache.olingo.odata2.api.rt.RuntimeDelegate;

/**
 * A BatchChangeSet
 * <p> BatchChangeSet represents a Change Set, that consists of change requests
 */
public abstract class BatchChangeSet implements BatchPart {

  /**
   * Add a new change request to the ChangeSet
   * @param BatchChangeSetPart {@link BatchChangeSetPart}
   */
  public abstract void add(BatchChangeSetPart request);

  /**
   * Get change requests 
   * @return a list of {@link BatchChangeSetPart}
   */
  public abstract List<BatchChangeSetPart> getChangeSetParts();

  /**
   * Get new builder instance 
   * @return {@link BatchChangeSetBuilder}
   */
  public static BatchChangeSetBuilder newBuilder() {
    return BatchChangeSetBuilder.newInstance();
  }

  public static abstract class BatchChangeSetBuilder {

    protected BatchChangeSetBuilder() {}

    private static BatchChangeSetBuilder newInstance() {
      return RuntimeDelegate.createBatchChangeSetBuilder();
    }

    public abstract BatchChangeSet build();
  }
}
