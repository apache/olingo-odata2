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

/**
 * <code>ODataJPATransactionContainerManaged</code> only implements the methods for
 * <code>ODataJPATransaction</code> bot does nothing for all methods because the
 * whole persistence is managed by the environment (CMP).
 */
public class ODataJPATransactionContainerManaged implements ODataJPATransaction {
  @Override
  public void begin() {
    /** do nothing for CMP */
  }

  @Override
  public void commit() {
    /** do nothing for CMP */
  }

  @Override
  public void rollback() {
    /** do nothing for CMP */
  }

  @Override
  public boolean isActive() {
    /** do nothing for CMP */
    return false;
  }
}
