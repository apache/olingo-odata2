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
import org.apache.olingo.odata2.api.edm.EdmAssociationSetEnd;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.provider.AssociationSetEnd;

public class EdmAssociationSetEndImplProv implements EdmAssociationSetEnd, EdmAnnotatable {

  private EdmEntitySet entitySet;
  private String role;
  private AssociationSetEnd end;

  public EdmAssociationSetEndImplProv(final AssociationSetEnd end, final EdmEntitySet entitySet) throws EdmException {
    this.end = end;
    this.entitySet = entitySet;
    role = end.getRole();
  }

  @Override
  public EdmEntitySet getEntitySet() throws EdmException {
    return entitySet;
  }

  @Override
  public String getRole() {
    return role;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return new EdmAnnotationsImplProv(end.getAnnotationAttributes(), end.getAnnotationElements());
  }
}
