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
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.client.api.edm.EdmDocumentation;

/**
 * Objects of this class represent EdmNavigationProperty
 *
 */
public class EdmNavigationPropertyImpl extends EdmTypedImpl implements EdmNavigationProperty, EdmAnnotatable {

  private EdmAnnotations annotations;
  private FullQualifiedName relationship;
  private String fromRole;
  private String toRole;
  private EdmDocumentation documentation;
  private Mapping mapping;

  public EdmDocumentation getDocumentation() {
    return documentation;
  }

  public void setDocumentation(EdmDocumentation documentation) {
    this.documentation = documentation;
  }

  public void setMapping(Mapping mapping) {
    this.mapping = mapping;
  }


  public void setAnnotations(EdmAnnotations annotations) {
    this.annotations = annotations;
  }

  public void setFromRole(String fromRole) {
    this.fromRole = fromRole;
  }

  public void setToRole(String toRole) {
    this.toRole = toRole;
  }

  @Override
  public EdmType getType() throws EdmException {
    return edmType;
  }

  @Override
  public EdmMultiplicity getMultiplicity() throws EdmException {
    return multiplicity;
  }

  @Override
  public EdmAssociation getRelationship() throws EdmException {
    return edm.getAssociation(relationship.getNamespace(), relationship.getName());
  }
  
  public FullQualifiedName getRelationshipName() throws EdmException {
    return relationship;
  }
  
  public void setRelationshipName( FullQualifiedName relationship){
    this.relationship = relationship;
  }

  @Override
  public String getFromRole() throws EdmException {
    return fromRole;
  }

  @Override
  public String getToRole() throws EdmException {
    return toRole;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return annotations;
  }

  @Override
  public EdmMapping getMapping() throws EdmException {
    return mapping;
  }
  
  @Override
  public String toString() {
      return String.format(name);
  }

}
