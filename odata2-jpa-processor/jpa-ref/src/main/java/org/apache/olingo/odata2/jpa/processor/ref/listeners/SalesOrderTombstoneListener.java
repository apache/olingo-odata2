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
package org.apache.olingo.odata2.jpa.processor.ref.listeners;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PostLoad;
import javax.persistence.Query;

import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneEntityListener;
import org.apache.olingo.odata2.jpa.processor.ref.model.SalesOrderHeader;

public class SalesOrderTombstoneListener extends ODataJPATombstoneEntityListener {

  public static String ENTITY_NAME = "SalesOrderHeader";

  @PostLoad
  public void handleDelta(final Object entity) {
    SalesOrderHeader so = (SalesOrderHeader) entity;

    if(so == null || so.getCreationDate() == null) {
      return;
    } else if (so.getCreationDate().getTime().getTime() < ODataJPATombstoneContext.getDeltaTokenUTCTimeStamp()) {
      return;
    } else {
      addToDelta(entity, ENTITY_NAME);
    }
  }

  @Override
  public String generateDeltaToken(final List<Object> deltas, final Query query) {
    return String.valueOf(System.currentTimeMillis());
  }

  @Override
  public Query getQuery(final GetEntitySetUriInfo resultsView, final EntityManager em) {
    return null;
  }

}
