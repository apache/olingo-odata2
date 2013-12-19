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
import org.apache.olingo.odata2.api.edm.EdmAssociationSet;
import org.apache.olingo.odata2.api.edm.EdmAssociationSetEnd;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.AssociationSetEnd;

public class EdmAssociationSetImplProv extends EdmNamedImplProv implements EdmAssociationSet, EdmAnnotatable {

  private AssociationSet associationSet;
  private EdmEntityContainer edmEntityContainer;
  private EdmAnnotations annotations;

  public EdmAssociationSetImplProv(final EdmImplProv edm, final AssociationSet associationSet,
      final EdmEntityContainer edmEntityContainer) throws EdmException {
    super(edm, associationSet.getName());
    this.associationSet = associationSet;
    this.edmEntityContainer = edmEntityContainer;
  }

  @Override
  public EdmAssociation getAssociation() throws EdmException {
    EdmAssociation association =
        edm.getAssociation(associationSet.getAssociation().getNamespace(), associationSet.getAssociation().getName());
    if (association == null) {
      throw new EdmException(EdmException.COMMON);
    }
    return association;
  }

  @Override
  public EdmAssociationSetEnd getEnd(final String role) throws EdmException {
    AssociationSetEnd end;

    if (associationSet.getEnd1().getRole().equals(role)) {
      end = associationSet.getEnd1();
    } else if (associationSet.getEnd2().getRole().equals(role)) {
      end = associationSet.getEnd2();
    } else {
      return null;
    }

    EdmEntitySet entitySet = edmEntityContainer.getEntitySet(end.getEntitySet());
    if (entitySet == null) {
      throw new EdmException(EdmException.COMMON);
    }

    return new EdmAssociationSetEndImplProv(end, entitySet);
  }

  @Override
  public EdmEntityContainer getEntityContainer() throws EdmException {
    return edmEntityContainer;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    if (annotations == null) {
      annotations =
          new EdmAnnotationsImplProv(associationSet.getAnnotationAttributes(), associationSet.getAnnotationElements());
    }
    return annotations;
  }
}
