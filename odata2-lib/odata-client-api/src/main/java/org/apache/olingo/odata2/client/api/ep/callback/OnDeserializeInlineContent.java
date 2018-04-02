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
package org.apache.olingo.odata2.client.api.ep.callback;

import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.client.api.ep.DeserializerProperties;

/**
 * <p>
 * Callback interface for the deep insert read calls (read of <code><m:inline></code> content).
 * Typically the {@link #receiveReadProperties(EntityProviderReadProperties, EdmNavigationProperty)} method is called
 * when an inline navigation property is found and will be read.
 * </p>
 *  
 */
public interface OnDeserializeInlineContent {

  /**
   * Receive (request) to be used {@link EntityProviderReadProperties} to read the found inline navigation property
   * (<code>><m:inline>...</m:inline></code>).
   * 
   * @param readProperties read properties which are used to read enclosing parent entity
   * @param navigationProperty emd navigation property information of found inline navigation property
   * @return read properties which are used to read (de-serialize) found inline navigation property
   * @throws ODataApplicationException
   */
  DeserializerProperties receiveReadProperties(DeserializerProperties readProperties,
      EdmNavigationProperty navigationProperty) throws ODataApplicationException;

}
