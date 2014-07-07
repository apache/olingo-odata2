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
package org.apache.olingo.odata2.jpa.processor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.AssociationSetEnd;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationSetView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntitySetView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmNameBuilder;

public class JPAEdmAssociationSet extends JPAEdmBaseViewImpl implements JPAEdmAssociationSetView {

  private JPAEdmSchemaView schemaView;
  private AssociationSet currentAssociationSet;
  private List<AssociationSet> associationSetList;
  private Association currentAssociation;

  public JPAEdmAssociationSet(final JPAEdmSchemaView view) {
    super(view);
    schemaView = view;
  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmAssociationSetBuilder();
    }

    return builder;
  }

  @Override
  public List<AssociationSet> getConsistentEdmAssociationSetList() {
    return associationSetList;
  }

  @Override
  public AssociationSet getEdmAssociationSet() {
    return currentAssociationSet;
  }

  @Override
  public Association getEdmAssociation() {
    return currentAssociation;
  }

  private class JPAEdmAssociationSetBuilder implements JPAEdmBuilder {

    @Override
    public void build() throws ODataJPAModelException {

      if (associationSetList == null) {
        associationSetList = new ArrayList<AssociationSet>();
      }

      JPAEdmAssociationView associationView = schemaView.getJPAEdmAssociationView();
      JPAEdmEntitySetView entitySetView = schemaView.getJPAEdmEntityContainerView().getJPAEdmEntitySetView();

      List<EntitySet> entitySetList = entitySetView.getConsistentEdmEntitySetList();
      if (associationView.isConsistent()) {
        for (Association association : associationView.getConsistentEdmAssociationList()) {

          currentAssociation = association;

          FullQualifiedName fQname =
              new FullQualifiedName(schemaView.getEdmSchema().getNamespace(), association.getName());
          currentAssociationSet = new AssociationSet();
          currentAssociationSet.setAssociation(fQname);

          int endCount = 0;
          for (EntitySet entitySet : entitySetList) {
            fQname = entitySet.getEntityType();

            if (fQname.equals(association.getEnd1().getType())) {
              AssociationSetEnd end = new AssociationSetEnd();
              end.setEntitySet(entitySet.getName());
              currentAssociationSet.setEnd1(end);
              end.setRole(association.getEnd1().getRole());
              endCount++;
            }

            if (fQname.equals(association.getEnd2().getType())) {
              AssociationSetEnd end = new AssociationSetEnd();
              end.setEntitySet(entitySet.getName());
              currentAssociationSet.setEnd2(end);
              end.setRole(association.getEnd2().getRole());
              endCount++;
            }

            if (endCount == 2) {
              break;
            }

          }
          if (endCount == 2) {
            JPAEdmNameBuilder.build(JPAEdmAssociationSet.this);
            associationSetList.add(currentAssociationSet);
          }

        }

      }
    }
  }
}
