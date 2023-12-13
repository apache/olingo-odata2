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
package org.apache.olingo.odata2.jpa.processor.core;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPATransaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class ODataJPATransactionLocalDefault implements ODataJPATransaction {

  private EntityTransaction tx = null;

  public ODataJPATransactionLocalDefault(EntityManager em) {
    this.tx = em.getTransaction();
  }

  @Override
  public void begin() {
    if (!isActive()) {
      tx.begin();
    }
  }

  @Override
  public void commit() {
    tx.commit();
  }

  @Override
  public void rollback() {
    tx.rollback();
  }

  @Override
  public boolean isActive() {
    return tx.isActive();
  }
}
