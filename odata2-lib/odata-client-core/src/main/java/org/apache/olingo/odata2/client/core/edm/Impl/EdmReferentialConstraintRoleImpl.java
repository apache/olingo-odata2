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

import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmReferentialConstraintRole;

/**
 * Objects of this class represent EdmReferentialConstraintRole
 *
 */
public class EdmReferentialConstraintRoleImpl implements EdmReferentialConstraintRole, EdmAnnotatable {
  protected List<String> refNames;
  private EdmReferentialConstraintRole role;
  private EdmAnnotations annotations;
  private String roleName;
  private List<EdmPropertyImpl> property;


  public List<String> getRefNames() {
    return refNames;
  }

  public void setRefNames(List<String> refNames) {
    this.refNames = refNames;
  }

  public String getRoleName() {
    return roleName;
  }

  @Override
  public String getRole() {
    return roleName;
  }

  /**
   * @return the propertyRefs
   */
  public List<EdmPropertyImpl> getProperty() {
    return property;
  }

  @Override
  public List<String> getPropertyRefNames() {
    return refNames;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return annotations;
  }

  /**
   * @param role the role to set
   */
  public void setRole(EdmReferentialConstraintRole role) {
    this.role = role;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
    
  }

  public void setProperty(List<EdmPropertyImpl> property) {
    this.property = property;
    
  }

  public void setAnnotations(EdmAnnotations annotations) {
   this.annotations = annotations;
    
  }  @Override
  public String toString() {
    return String.format(role.getRole());
}
}
