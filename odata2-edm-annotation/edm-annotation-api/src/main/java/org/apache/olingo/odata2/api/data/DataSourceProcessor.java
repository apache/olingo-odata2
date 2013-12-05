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
package org.apache.olingo.odata2.api.data;

import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;

/**
 * Abstract class for implementation of the centralized parts of OData processing,
 * allowing to use the simplified {@link DataSource} and {@link ValueAccess} for the
 * actual data handling.
 * <br/>
 * Extend this class and implement a DataSourceProcessor if the default implementation
 * (<code>ListProcessor</code> found in <code>annotation-processor-core module</code>) has to be overwritten.
 */
public abstract class DataSourceProcessor extends ODataSingleProcessor {

  protected final DataSource dataSource;
  protected final ValueAccess valueAccess;
  
  /**
   * Initialize a {@link DataSourceProcessor} in combination with given {@link DataSource} (providing data objects)
   * and {@link ValueAccess} (accessing values of data objects).
   * 
   * @param dataSource used for accessing the data objects
   * @param valueAccess for accessing the values provided by the data objects
   */
  public DataSourceProcessor(final DataSource dataSource, final ValueAccess valueAccess) {
    this.dataSource = dataSource;
    this.valueAccess = valueAccess;
  }
}
