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
/**
 * <h3>OData JPA Processor API Library</h3>
 * The library provides a way for the developers to create an OData Service from a Java Persistence Model.
 * The library supports Java Persistence 2.0 and is dependent on OData library.
 * 
 * To create an OData service from JPA models
 * <ol><li>extend the service factory class {@link org.apache.olingo.odata2.processor.api.jpa.ODataJPAServiceFactory}
 * and implement the methods</li>
 * <li>define a JAX-RS servlet in web.xml and configure the service factory as servlet init parameter. 
 * <p><b>See Also:</b>{@link org.apache.olingo.odata2.processor.api.jpa.ODataJPAServiceFactory}</li></ol>
 * 
 * @author SAP AG
 */
package org.apache.olingo.odata2.processor.api.jpa;

