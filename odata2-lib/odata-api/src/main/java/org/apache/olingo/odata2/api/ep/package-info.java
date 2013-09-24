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
/**
 * Entity Provider<p>
 * 
 * The <b>org.apache.olingo.odata2.api.ep</b> package contains all classes related and necessary to provide an
 * {@link org.apache.olingo.odata2.api.ep.EntityProvider}.
 * <p>
 * An {@link org.apache.olingo.odata2.api.ep.EntityProvider} provides all necessary <b>read</b> and <b>write</b> methods
 * for accessing
 * the entities defined in an <code>Entity Data Model</code>.
 * Therefore this library provides (in its <code>core</code> packages) as convenience basic
 * {@link org.apache.olingo.odata2.api.ep.EntityProvider} for accessing entities in the <b>XML</b> and <b>JSON</b>
 * format.
 * <p>
 * For support of additional formats it is recommended to handle those directly within an implementation of a
 * <code>ODataProcessor</code> (it is possible but <b>not recommended</b> to implement an own
 * {@link org.apache.olingo.odata2.api.ep.EntityProvider} for support of additional formats).
 */
package org.apache.olingo.odata2.api.ep;

