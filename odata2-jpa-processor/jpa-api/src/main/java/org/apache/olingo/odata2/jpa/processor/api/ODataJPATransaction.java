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

import org.apache.olingo.odata2.api.ODataCallback;

/**
 * Interface for JPA-Transaction abstraction. Default implementation is Resource local, while additional
 * an override may used to insert JTA compatible transactions as well.
 *
 */
public interface ODataJPATransaction extends ODataCallback {

    /**
     * implement the start of the transaction
     */
    public void begin();

    /**
     * implement the commit of the transaction
     */
    public void commit();

    /**
     * implement the rollback of the transaction
     */
    public void rollback();

    /**
     * implement status of the transaction context
     */
    public boolean isActive();


}
