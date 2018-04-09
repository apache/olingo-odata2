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

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationSet;
import org.apache.olingo.odata2.api.edm.EdmAssociationSetEnd;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.client.api.edm.EdmDocumentation;

/**
 * Objects of this class represent AssociationSet
 *
 */
public class EdmAssociationSetImpl extends EdmNamedImpl implements EdmAssociationSet, EdmAnnotatable {

  private EdmEntityContainer edmEntityContainer;
  private EdmAnnotations annotations;
  private FullQualifiedName associationSetFQName;
  private EdmAssociationSetEnd end2;
  private EdmAssociationSetEnd end1;
  private EdmDocumentation documentation;

  
  public FullQualifiedName getAssociationSetFQName() {
    return associationSetFQName ;
  }
  public void setDocumentation(EdmDocumentation documentation) {
    this.documentation = documentation;
  }

  @Override
  public EdmAssociation getAssociation() throws EdmException {
    EdmAssociation association =
        edm.getAssociation(this.associationSetFQName.getNamespace(), 
            this.associationSetFQName.getName());
    if (association == null) {
      throw new EdmException(EdmException.ASSOCIATIONNOTFOUND);
    }
    return association;
  }

  /**
   * @param annotations the annotations to set
   */
  public void setAnnotations(EdmAnnotations annotations) {
    this.annotations = annotations;
  }

  /**
   * @param edmEntityContainer the edmEntityContainer to set
   */
  public void setEdmEntityContainer(EdmEntityContainer edmEntityContainer) {
    this.edmEntityContainer = edmEntityContainer;
  }

  @Override
  public EdmAssociationSetEnd getEnd(final String role) throws EdmException {
    EdmAssociationSetEnd end;
    if (end1.getRole().equals(role)) {
      end = this.end1;
    } else if (end2.getRole().equals(role)) {
      end = this.end2;
    } else {
      return null;
    }
    EdmEntitySet entitySet = edmEntityContainer.getEntitySet(((EdmAssociationSetEndImpl)end).getEntitySetName());
    if (entitySet == null) {
      throw new EdmException(EdmException.COMMON);//TODO
    }
    return end;
  }

  @Override
  public EdmEntityContainer getEntityContainer() throws EdmException {
    return edmEntityContainer;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return annotations;
  }

  public void setAssociation(FullQualifiedName associationSetFQName) {
    this.associationSetFQName = associationSetFQName;
  }
  
  public void setEnd1(EdmAssociationSetEnd end1) {
    this.end1 = end1;
  }
  
  public void setEnd2(EdmAssociationSetEnd end2) {
    this.end2 = end2;
  }

  /**
   * @return the end2
   */
  public EdmAssociationSetEnd getEnd2() {
    return end2;
  }

  /**
   * @return the end1
   */
  public EdmAssociationSetEnd getEnd1() {
    return end1;
  }
  
  @Override
  public String toString() {
    return associationSetFQName.getNamespace() + Edm.DELIMITER + associationSetFQName.getName();
  }
  
}
