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
package org.apache.olingo.odata2.client.api.edm;

/**
 * Objects of this class represent the data service. They contain all schemas of the EDM as well as the
 * dataServiceVersion
 * 
 */
public class EdmDataServices {

  private ClientEdm edm;
  private String dataServiceVersion;
  private String customEdmxVersion;

 
  /**
   * Sets the Edm for this {@link EdmDataServices}
   * @param edm
   * @return {@link EdmDataServices} for method chaining
   */
  public EdmDataServices setClientEdm(final ClientEdm edm) {
    this.edm = edm;
    return this;
  }


  /**
   * Sets the data service version for this {@link EdmDataServices}
   * @param dataServiceVersion
   * @return {@link EdmDataServices} for method chaining
   */
  public EdmDataServices setDataServiceVersion(final String dataServiceVersion) {
    this.dataServiceVersion = dataServiceVersion;
    return this;
  }

  
  /**
   * Sets a custom edmx version which is used in the metadata document
   * @param customEdmxVersion
   * @return {@link ComplexType} for method chaining
   */
  public EdmDataServices setCustomEdmxVersion(String customEdmxVersion) {
    this.customEdmxVersion = customEdmxVersion;
    return this;
  }

  /**
   * @return <b>String</b> data service version
   */
  public String getDataServiceVersion() {
    return dataServiceVersion;
  }

  /**
   * @return <b>String</b> custom edmx version
   */
  public String getCustomEdmxVersion() {
    return customEdmxVersion;
  }
  
  /**
   * @return <b>Edm</b> edm
   */
  public ClientEdm getEdm() {
    return edm;
  }
}
