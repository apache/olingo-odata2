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
import org.apache.olingo.odata2.api.edm.EdmAssociationEnd;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;

public class EdmAssociationEndImplProv implements EdmAssociationEnd, EdmAnnotatable {

  private EdmImplProv edm;
  private AssociationEnd associationEnd;
  private EdmAnnotations annotations;

  public EdmAssociationEndImplProv(final EdmImplProv edm, final AssociationEnd associationEnd) throws EdmException {
    this.edm = edm;
    this.associationEnd = associationEnd;
  }

  @Override
  public String getRole() {
    return associationEnd.getRole();
  }

  @Override
  public EdmEntityType getEntityType() throws EdmException {
    final FullQualifiedName type = associationEnd.getType();
    EdmEntityType entityType = edm.getEntityType(type.getNamespace(), type.getName());
    if (entityType == null) {
      throw new EdmException(EdmException.COMMON);
    }
    return entityType;
  }

  @Override
  public EdmMultiplicity getMultiplicity() {
    return associationEnd.getMultiplicity();
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    if (annotations == null) {
      annotations =
          new EdmAnnotationsImplProv(associationEnd.getAnnotationAttributes(), associationEnd.getAnnotationElements());
    }
    return annotations;
  }
}
