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
package org.apache.olingo.odata2.client.core.edm.Impl;

import org.apache.olingo.odata2.api.edm.EdmElement;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmMapping;

/**
 *  Objects of this class represent EdmElement
 */
public abstract class EdmElementImpl extends EdmTypedImpl implements EdmElement {

  private EdmFacets edmFacets;
  private EdmMapping edmMapping;

  @Override
  public EdmMapping getMapping() throws EdmException {
    return edmMapping;
  }

  public void setFacets(EdmFacets edmFacets) {
    this.edmFacets = edmFacets;
  }

  public void setMapping(EdmMapping edmMapping) {
    this.edmMapping = edmMapping;
  }

  @Override
  public EdmFacets getFacets() throws EdmException {
    return edmFacets;
  }  
  @Override
  public String toString() {
      return String.format(name);
  }
}
