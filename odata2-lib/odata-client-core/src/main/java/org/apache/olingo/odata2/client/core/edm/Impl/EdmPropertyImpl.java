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

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmCustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.client.api.edm.EdmDocumentation;

/**
 * Objects of this class represent EdmProperty
 *
 */
public abstract class EdmPropertyImpl extends EdmElementImpl implements EdmProperty, EdmAnnotatable {

  private EdmFacets facets;
  private EdmCustomizableFeedMappings customizableFeedMappings;
  private String mimeType;
  private Mapping mapping;
  private EdmDocumentation documentation;
  private EdmAnnotations annotations;

  public EdmFacets getFacets() {
    return facets;
  }

  @Override
  public void setFacets(EdmFacets facets) {
    this.facets = facets;
  }
  
  public void setMapping(Mapping mapping) {
    this.mapping = mapping;
  }

  public void setDocumentation(EdmDocumentation documentation) {
    this.documentation = documentation;
  }

  public void setCustomizableFeedMappings(EdmCustomizableFeedMappings customizableFeedMappings) {
    this.customizableFeedMappings = customizableFeedMappings;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  @Override
  public EdmCustomizableFeedMappings getCustomizableFeedMappings() throws EdmException {
    return customizableFeedMappings;
  }


  public void setAnnotations(EdmAnnotations annotations) {
    this.annotations = annotations;
  }

  @Override
  public String getMimeType() throws EdmException {
    return mimeType;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return annotations;
  }
  @Override
  public String toString() {
      return String.format(name);
  }
}
