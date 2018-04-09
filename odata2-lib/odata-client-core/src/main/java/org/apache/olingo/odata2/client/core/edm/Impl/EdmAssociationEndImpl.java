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
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.client.core.edm.EdmMetadataAssociationEnd;
import org.apache.olingo.odata2.client.core.edm.EdmOnDelete;

/**
 * Objects of this class represent AssociationEnds
 *
 */
public class EdmAssociationEndImpl implements EdmMetadataAssociationEnd, EdmAnnotatable {

  private EdmImpl edm;
  private EdmAnnotations annotations;
  private String role;
  private EdmMultiplicity edmMultiplicity;
  private FullQualifiedName associationEndTypeName;
  private EdmOnDelete onDelete;

  @Override
  public String getRole() {
    return role;
  }

  /**
   * @param edm the edm to set
   */
  public void setEdm(EdmImpl edm) {
    this.edm = edm;
  }


  @Override
  public EdmEntityType getEntityType() throws EdmException {
    EdmEntityType entityType = edm.getEntityType(
        associationEndTypeName.getNamespace(), 
       associationEndTypeName.getName());
    if (entityType == null) {
      throw new EdmException(EdmException.ENTITYTYPEPROBLEM);
    }
    return entityType;
  }

  @Override
  public EdmMultiplicity getMultiplicity() {
    return edmMultiplicity;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return annotations;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public void setMultiplicity(EdmMultiplicity edmMultiplicity) {
    this.edmMultiplicity = edmMultiplicity;
  }
  
  public void setType(FullQualifiedName associationEndTypeName) {
    this.associationEndTypeName = associationEndTypeName;
  }
  
  public void setAnnotations(EdmAnnotations annotations) {
    this.annotations = annotations;
  }

  public void setOnDelete(EdmOnDelete onDelete) {
    this.onDelete = onDelete;
  }

  @Override
  public EdmOnDelete getOnDelete() {
    return onDelete;
  }

  @Override
  public String toString() {
      return String.format(annotations.toString());
  }
}
