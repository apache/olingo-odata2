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
 * Entity Provider Callbacks<p>
 * These callbacks will be used to support the $expand query option. Callbacks have to implement the
 * {@link org.apache.olingo.odata2.api.ODataCallback} as a marker.
 * <br>To support an expanded entry the {@link org.apache.olingo.odata2.api.ep.callback.OnWriteEntryContent} interface
 * has to be implemented.
 * <br>To support an expanded feed the {@link org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent} interface has
 * to be implemented.
 * 
 * <p>All callbacks are registered for a navigation property in a HashMap<String as navigation property name, callback
 * for this navigation property> and will only be called if a matching $expand clause is found.
 */
package org.apache.olingo.odata2.api.ep.callback;

