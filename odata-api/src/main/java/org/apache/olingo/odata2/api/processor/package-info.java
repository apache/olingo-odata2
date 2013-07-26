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
 * Data Processor<p>
 * 
 * A data processor implements all create, read, update and delete (CRUD) methods of an OData service. A processor as 
 * part of a OData service implementation is created by the service factory and then called during request handling. 
 * In dependency of the http context (http method, requestheaders ...) and the parsed uri semantic the OData Library 
 * will call an appropriate processor method. Within this method a service can perform operations on data. In a final
 * step the data result can be transformed using a {@link org.apache.olingo.odata2.api.ep.EntityProvider} (for Json, Atom and XML) and is returned as 
 * a {@link org.apache.olingo.odata2.api.processor.ODataResponse}.
 * <p>
 * A processor gets access to context information either via method parameters or a {@link org.apache.olingo.odata2.api.processor.ODataContext} which is attached 
 * to the processor object.
 * <p>
 * A processor can support optional features {@link org.apache.olingo.odata2.api.processor.feature} and implement 
 * parts {@link org.apache.olingo.odata2.api.processor.part} which is more or less a grouping for different OData CRUD operations.
 * <p>
 * {@link org.apache.olingo.odata2.api.processor.ODataSingleProcessor} is a convenience abstract class that implements all interface parts and has default implementations 
 * for handling OData service document and metadata. Usually the {@link org.apache.olingo.odata2.api.processor.ODataSingleProcessor} is used together with a 
 * <code>ODataSingleService</code> default implementation.
 *  
 */
package org.apache.olingo.odata2.api.processor;
