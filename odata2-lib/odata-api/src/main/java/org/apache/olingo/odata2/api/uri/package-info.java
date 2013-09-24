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
 * <p>URI Parser Facade</p>
 * <p>The URI package has one central class {@link org.apache.olingo.odata2.api.uri.UriParser} to parse a request URI
 * as well as several interfaces that provide access to parsed parts of the URI.
 * <br>The {@link org.apache.olingo.odata2.api.uri.UriParser} class also provides the possibility to parse a filter or
 * an orderBy Statement. Both are specified in the OData Protocol Specification.
 * <br>The URI syntax is specified in the OData Protocol Specification in the form of an ABNF. </p>
 */
package org.apache.olingo.odata2.api.uri;

