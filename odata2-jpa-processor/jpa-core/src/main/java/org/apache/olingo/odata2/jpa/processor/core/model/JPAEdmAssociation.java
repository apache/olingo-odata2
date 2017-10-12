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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationEndView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmReferentialConstraintView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmNameBuilder;

public class JPAEdmAssociation extends JPAEdmBaseViewImpl implements JPAEdmAssociationView {

  private JPAEdmAssociationEndView associationEndView;

  private Association currentAssociation;
  private List<Association> consistentAssociatonList;
  private HashMap<String, Association> associationMap;
  private HashMap<String, JPAEdmAssociationEndView> associationEndMap;
  private List<JPAEdmReferentialConstraintView> inconsistentRefConstraintViewList;
  private int numberOfSimilarEndPoints;

  public JPAEdmAssociation(final JPAEdmAssociationEndView associationEndview,
      final JPAEdmEntityTypeView entityTypeView, final JPAEdmPropertyView propertyView, final int value) {
    super(associationEndview);
    associationEndView = associationEndview;
    numberOfSimilarEndPoints = value;
    init();
  }

  public JPAEdmAssociation(final JPAEdmSchemaView view) {
    super(view);
    init();
  }

  private void init() {
    isConsistent = false;
    consistentAssociatonList = new ArrayList<Association>();
    inconsistentRefConstraintViewList = new LinkedList<JPAEdmReferentialConstraintView>();
    associationMap = new HashMap<String, Association>();
    associationEndMap = new HashMap<String, JPAEdmAssociationEndView>();
  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmAssociationBuilder();
    }
    return builder;
  }

  @Override
  public Association getEdmAssociation() {
    return currentAssociation;
  }

  @Override
  public List<Association> getConsistentEdmAssociationList() {
    return consistentAssociatonList;
  }

  @Override
  public Association searchAssociation(final JPAEdmAssociationEndView view) {
    if (view != null) {
      for (Entry<String, Association> assoc : associationMap.entrySet()) {
        Association association = assoc.getValue();
        if (association != null) {
          if (view.compare(association.getEnd1(), association.getEnd2())) {
            JPAEdmAssociationEndView associationEnd = associationEndMap.get(association.getName());
            if (associationEnd.getJoinColumnNames() != null
                && associationEnd.getJoinColumnReferenceColumnNames() != null
                && view.getJoinColumnNames() != null && view.getJoinColumnReferenceColumnNames() != null) {
              if (Arrays.equals(view.getJoinColumnNames(), associationEnd.getJoinColumnNames())
                  &&
                  Arrays.equals(view.getJoinColumnReferenceColumnNames(), associationEnd
                      .getJoinColumnReferenceColumnNames())) {
                currentAssociation = association;
                return association;
              }

            }
            if (associationEnd.getMappedByName() != null) {
              if (associationEnd.getMappedByName().equals(view.getOwningPropertyName())) {
                updateAssociationEndMultiplicity(view, association);
                currentAssociation = association;
                return association;
              }
            }
            if (associationEnd.getOwningPropertyName() != null) {
              if (associationEnd.getOwningPropertyName().equals(view.getMappedByName())) {
                currentAssociation = association;
                return association;
              }
            }
          }
        }
      }
    }
    return null;
  }

  private void updateAssociationEndMultiplicity(JPAEdmAssociationEndView view, Association association) {
    if (view.getEdmAssociationEnd1().getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE
        && association.getEnd1().getMultiplicity() == EdmMultiplicity.ONE) {
      association.getEnd1().setMultiplicity(EdmMultiplicity.ZERO_TO_ONE);
    } else if (view.getEdmAssociationEnd2().getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE
        && association.getEnd2().getMultiplicity() == EdmMultiplicity.ONE) {
      association.getEnd2().setMultiplicity(EdmMultiplicity.ZERO_TO_ONE);
    } else if (view.getEdmAssociationEnd1().getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE
        && association.getEnd2().getMultiplicity() == EdmMultiplicity.ONE) {
      association.getEnd2().setMultiplicity(EdmMultiplicity.ZERO_TO_ONE);
    } else if (view.getEdmAssociationEnd2().getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE
        && association.getEnd1().getMultiplicity() == EdmMultiplicity.ONE) {
      association.getEnd1().setMultiplicity(EdmMultiplicity.ZERO_TO_ONE);
    }
  }

  @Override
  public void addJPAEdmAssociationView(final JPAEdmAssociationView associationView,
      final JPAEdmAssociationEndView associationEndView) {
    if (associationView != null) {
      currentAssociation = associationView.getEdmAssociation();
      associationMap.put(currentAssociation.getName(), currentAssociation);
      associationEndMap.put(currentAssociation.getName(), associationEndView);
      addJPAEdmRefConstraintView(associationView.getJPAEdmReferentialConstraintView());
    }
  }

  @Override
  public void addJPAEdmRefConstraintView(final JPAEdmReferentialConstraintView refView) {
    if (refView != null && refView.isExists()) {
      inconsistentRefConstraintViewList.add(refView);
    }
  }

  @Override
  public JPAEdmReferentialConstraintView getJPAEdmReferentialConstraintView() {
    if (inconsistentRefConstraintViewList.isEmpty()) {
      return null;
    }
    return inconsistentRefConstraintViewList.get(0);
  }

  private class JPAEdmAssociationBuilder implements JPAEdmBuilder {

    @Override
    public void build() throws ODataJPAModelException, ODataJPARuntimeException {

      if (associationEndView != null && searchAssociation(associationEndView) == null) {
        currentAssociation = new Association();
        currentAssociation.setEnd1(associationEndView.getEdmAssociationEnd1());
        currentAssociation.setEnd2(associationEndView.getEdmAssociationEnd2());

        JPAEdmNameBuilder.build(JPAEdmAssociation.this, numberOfSimilarEndPoints);

        associationMap.put(currentAssociation.getName(), currentAssociation);

      } else if (!inconsistentRefConstraintViewList.isEmpty()) {
        int inconsistentRefConstraintViewSize = inconsistentRefConstraintViewList.size();
        int index = 0;
        for (int i = 0; i < inconsistentRefConstraintViewSize; i++) {
          JPAEdmReferentialConstraintView view = inconsistentRefConstraintViewList.get(index);

          if (view.isExists() && !view.isConsistent()) {
            view.getBuilder().build();
          }
          if (view.isConsistent()) {
            Association newAssociation = new Association();
            copyAssociation(newAssociation, associationMap.get(view.getEdmRelationShipName()));
            newAssociation.setReferentialConstraint(view.getEdmReferentialConstraint());
            consistentAssociatonList.add(newAssociation);
            associationMap.put(view.getEdmRelationShipName(), newAssociation);
            inconsistentRefConstraintViewList.remove(index);
          } else {
            associationMap.remove(view.getEdmRelationShipName());
            index++;
          }
        }
      }

      if (associationMap.size() == consistentAssociatonList.size()) {
        isConsistent = true;
      } else {
        for (Entry<String, Association> entry : associationMap.entrySet()) {
          Association association = entry.getValue();
          if (!consistentAssociatonList.contains(association)) {
            consistentAssociatonList.add(association);
          }
        }
        isConsistent = true;
      }

    }

    private void copyAssociation(final Association copyToAssociation, final Association copyFromAssociation) {
      copyToAssociation.setEnd1(copyFromAssociation.getEnd1());
      copyToAssociation.setEnd2(copyFromAssociation.getEnd2());
      copyToAssociation.setName(copyFromAssociation.getName());
      copyToAssociation.setAnnotationAttributes(copyFromAssociation.getAnnotationAttributes());
      copyToAssociation.setAnnotationElements(copyFromAssociation.getAnnotationElements());
      copyToAssociation.setDocumentation(copyFromAssociation.getDocumentation());

    }
  }

  @Override
  public int getNumberOfAssociationsWithSimilarEndPoints(final JPAEdmAssociationEndView view) {
    int count = 0;
    AssociationEnd end1 = null;
    AssociationEnd end2 = null;
    for (Entry<String, Association> entry : associationMap.entrySet()) {
      Association association = entry.getValue();
      if (association != null) {
        end1 = association.getEnd1();
        end2 = association.getEnd2();
        if (view.compare(end1, end2)) {
          count++;
        }
      }
    }
    return count;
  }

}
