/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.api.uri.info;

import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmLiteral;

/**
 * Access to the parts of the request URI that are relevant for requests
 * of function imports.
 * @org.apache.olingo.odata2.DoNotImplement
 * @author
 */
public interface GetFunctionImportUriInfo {
  /**
   * Gets the function import.
   * @return {@link EdmFunctionImport} the function import
   */
  public EdmFunctionImport getFunctionImport();

  /**
   * Gets the value of the $format system query option.
   * @return the format (as set as <code>$format</code> query parameter) or null
   */
  public String getFormat();

  /**
   * Gets the parameters of a function import as Map from parameter names to
   * their corresponding typed values, or an empty list if no function import
   * is used or no parameters are given in the URI.
   * @return Map of {@literal <String,} {@link EdmLiteral}{@literal >} function import parameters
   */
  public Map<String, EdmLiteral> getFunctionImportParameters();

  /**
   * Gets the custom query options as Map from option names to their
   * corresponding String values, or an empty list if no custom query options
   * are given in the URI.
   * @return Map of {@literal <String, String>} custom query options
   */
  public Map<String, String> getCustomQueryOptions();
}
