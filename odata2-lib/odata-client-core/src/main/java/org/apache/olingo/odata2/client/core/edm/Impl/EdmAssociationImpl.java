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

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationEnd;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmReferentialConstraint;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.client.api.edm.EdmDocumentation;
import org.apache.olingo.odata2.client.core.edm.EdmMetadataAssociationEnd;

/**
 * Objects of this class represent Asociation
 *
 */
public class EdmAssociationImpl extends EdmNamedImpl implements EdmAssociation, EdmAnnotatable {

  private String namespace;
  private EdmAnnotations annotations;
  private EdmReferentialConstraintImpl referentialConstraint;
  private List<EdmMetadataAssociationEnd> associationEnds; 
  private EdmDocumentation documentation;


  public EdmDocumentation getDocumentation() {
    return documentation;
  }



  public void setDocumentation(EdmDocumentation documentation) {
    this.documentation = documentation;
  }



  public List<EdmMetadataAssociationEnd> getAssociationEnds() {
    return associationEnds;
  }


  @Override
  public String getNamespace() throws EdmException {
    return namespace;
  }
  
  

  /**
   * @param namespace the namespace to set
   */
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @Override
  public EdmTypeKind getKind() {
    return EdmTypeKind.ASSOCIATION;
  }

  @Override
  public EdmAssociationEnd getEnd(final String role) throws EdmException {
    if (!associationEnds.isEmpty()) {
      EdmAssociationEnd end = associationEnds.get(0);
      if (role.equals(end.getRole())) {
        return end;
      }
      end = associationEnds.get(1);
      if (role.equals(end.getRole())) {
        return end;
      }
    }
    return null;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return annotations;
  }

  public EdmMultiplicity getEndMultiplicity(final String role) throws EdmException {
    EdmMetadataAssociationEnd end = getEnd1();
    if (end!=null && role.equals(end.getRole())) {
      return end.getMultiplicity();
    }
    end = getEnd2();
    if (end!=null && role.equals(end.getRole())) {
      return end.getMultiplicity();
    }

    return null;
  }

  @Override
  public EdmMetadataAssociationEnd getEnd1() throws EdmException {
    return associationEnds.isEmpty()?null:associationEnds.get(0);
  }

  @Override
  public EdmMetadataAssociationEnd getEnd2() throws EdmException {
    return  associationEnds.isEmpty()?null:associationEnds.get(1);
  }

  @Override
  public EdmReferentialConstraint getReferentialConstraint() throws EdmException {
    return this.referentialConstraint;
  }

  public void setReferentialConstraint(EdmReferentialConstraint referentialConstraint) {
    this.referentialConstraint = (EdmReferentialConstraintImpl) referentialConstraint;
  }

  public void setAnnotations(EdmAnnotations annotations) {
    this.annotations = annotations;
  }

  public void setAssociationEnds(List<EdmMetadataAssociationEnd> associationEnds) {
    this.associationEnds = associationEnds;
  } 
  
  @Override
  public String toString() {
    return namespace + Edm.DELIMITER + name;
  }

}
