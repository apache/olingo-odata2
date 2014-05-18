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
package org.apache.olingo.odata2.jpa.processor.api;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;

/**
 * Extend this class and implement a JPA Entity Listener as specified in JSR 317 Java Persistence 2.0.
 * The class provides abstract methods that shall be implemented by JPA application to handle OData Tombstone features.
 * The implemented JPA Entity Listener classes will be called back from OData JPA Processor Library.
 */
public abstract class ODataJPATombstoneEntityListener {

  public ODataJPATombstoneEntityListener() {}

  protected final void addToDelta(final Object entity, final String entityName) {
    ODataJPATombstoneContext.addToDeltaResult(entity, entityName);
  }

  /**
   * Implement this method to create a {@link javax.persistence.Query} object. The Query object can be created from
   * OData requests. The query instance thus created can be used for handling delta JPA entities. The delta token passed
   * from OData request can
   * be accessed from {@link com.sap.core.odata.processor.api.jpa.ODataJPATombstoneContext}.
   * @param resultsView is a reference to OData request
   * @param em is a reference to {@link javax.persistence.EntityManager}
   * @return an instance of type {@link javax.persistence.Query}
   */
  public abstract Query getQuery(GetEntitySetUriInfo resultsView, EntityManager em);

  /**
   * Implement this method to create a delta token.
   * @param deltas is list of delta JPA Entities
   * @param query is an instance of type {@link javax.persistence.Query} that was used for handling delta entites
   * @return a delta token of type String
   */
  public abstract String generateDeltaToken(List<Object> deltas, Query query);

}
