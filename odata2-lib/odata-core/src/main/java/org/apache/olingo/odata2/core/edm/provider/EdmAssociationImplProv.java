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
package org.apache.olingo.odata2.core.edm.provider;

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationEnd;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmReferentialConstraint;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraint;

public class EdmAssociationImplProv extends EdmNamedImplProv implements EdmAssociation, EdmAnnotatable {

  private Association association;
  private String namespace;
  private EdmAnnotations annotations;
  private EdmReferentialConstraintImplProv referentialConstraint;

  public EdmAssociationImplProv(final EdmImplProv edm, final Association association, final String namespace)
      throws EdmException {
    super(edm, association.getName());
    this.association = association;
    this.namespace = namespace;
  }

  @Override
  public String getNamespace() throws EdmException {
    return namespace;
  }

  @Override
  public EdmTypeKind getKind() {
    return EdmTypeKind.ASSOCIATION;
  }

  @Override
  public EdmAssociationEnd getEnd(final String role) throws EdmException {
    AssociationEnd end = association.getEnd1();
    if (end.getRole().equals(role)) {
      return new EdmAssociationEndImplProv(edm, end);
    }
    end = association.getEnd2();
    if (end.getRole().equals(role)) {
      return new EdmAssociationEndImplProv(edm, end);
    }

    return null;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    if (annotations == null) {
      annotations =
          new EdmAnnotationsImplProv(association.getAnnotationAttributes(), association.getAnnotationElements());
    }
    return annotations;
  }

  public EdmMultiplicity getEndMultiplicity(final String role) {
    if (association.getEnd1().getRole().equals(role)) {
      return association.getEnd1().getMultiplicity();
    }

    if (association.getEnd2().getRole().equals(role)) {
      return association.getEnd2().getMultiplicity();
    }

    return null;
  }

  @Override
  public EdmAssociationEnd getEnd1() throws EdmException {
    AssociationEnd end = association.getEnd1();
    return new EdmAssociationEndImplProv(edm, end);
  }

  @Override
  public EdmAssociationEnd getEnd2() throws EdmException {
    AssociationEnd end = association.getEnd2();
    return new EdmAssociationEndImplProv(edm, end);
  }

  @Override
  public EdmReferentialConstraint getReferentialConstraint() throws EdmException {
    if(referentialConstraint == null){
      ReferentialConstraint refConstraint = association.getReferentialConstraint();
      if(refConstraint != null){
        referentialConstraint = new EdmReferentialConstraintImplProv(refConstraint);
      }
    }
    return referentialConstraint;
  }
}
