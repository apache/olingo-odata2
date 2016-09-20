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

import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationEndView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmNameBuilder;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.List;

public class JPAEdmAssociationEnd extends JPAEdmBaseViewImpl implements JPAEdmAssociationEndView {

  private JPAEdmEntityTypeView entityTypeView = null;
  private JPAEdmPropertyView propertyView = null;
  private AssociationEnd currentAssociationEnd1 = null;
  private AssociationEnd currentAssociationEnd2 = null;
  private String[] columnNames;
  private String[] referencedColumnNames;
  private String mappedBy;
  private String ownerPropertyName;

  public JPAEdmAssociationEnd(final JPAEdmEntityTypeView entityTypeView, final JPAEdmPropertyView propertyView) {
    super(entityTypeView);
    this.entityTypeView = entityTypeView;
    this.propertyView = propertyView;
  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmAssociationEndBuilder();
    }

    return builder;
  }

  @Override
  public AssociationEnd getEdmAssociationEnd1() {
    return currentAssociationEnd1;
  }

  @Override
  public AssociationEnd getEdmAssociationEnd2() {
    return currentAssociationEnd2;
  }

  private class JPAEdmAssociationEndBuilder implements JPAEdmBuilder {

    @Override
    public void build() throws ODataJPAModelException {

      currentAssociationEnd1 = new AssociationEnd();
      currentAssociationEnd2 = new AssociationEnd();

      JPAEdmNameBuilder.build(JPAEdmAssociationEnd.this, entityTypeView, propertyView);

      currentAssociationEnd1.setRole(currentAssociationEnd1.getType().getName());
      if (currentAssociationEnd1.getType().getName().equals(currentAssociationEnd2.getType().getName())) {
        currentAssociationEnd2.setRole(currentAssociationEnd2.getType().getName() + "2");
      } else {
        currentAssociationEnd2.setRole(currentAssociationEnd2.getType().getName());
      }

      setEdmMultiplicity(propertyView.getJPAAttribute().getPersistentAttributeType());

      List<String[]> joinColumnNames = propertyView.getJPAJoinColumns();
      if (joinColumnNames != null) {
        int i = 0;
        columnNames = (String[]) Array.newInstance(String.class, joinColumnNames.size());
        referencedColumnNames = (String[]) Array.newInstance(String.class, joinColumnNames.size());
        for (String[] jc : joinColumnNames) {
          columnNames[i] = jc[0];
          referencedColumnNames[i++] = jc[1];
        }
      }
      ownerPropertyName = propertyView.getJPAAttribute().getName();
    }

    private void setEdmMultiplicity(final PersistentAttributeType type) {
      AnnotatedElement annotatedElement = (AnnotatedElement) propertyView.getJPAAttribute().getJavaMember();
      switch (type) {
      case ONE_TO_MANY:
        currentAssociationEnd1.setMultiplicity(EdmMultiplicity.ONE);
        currentAssociationEnd2.setMultiplicity(EdmMultiplicity.MANY);
        if (annotatedElement != null) {
          OneToMany reln = annotatedElement.getAnnotation(OneToMany.class);
          if (reln != null) {
            mappedBy = reln.mappedBy();
          }
        }
        break;
      case MANY_TO_MANY:
        currentAssociationEnd1.setMultiplicity(EdmMultiplicity.MANY);
        currentAssociationEnd2.setMultiplicity(EdmMultiplicity.MANY);
        if (annotatedElement != null) {
          ManyToMany reln = annotatedElement.getAnnotation(ManyToMany.class);
          if (reln != null) {
            mappedBy = reln.mappedBy();
          }
        }
        break;
      case MANY_TO_ONE:
        currentAssociationEnd1.setMultiplicity(EdmMultiplicity.MANY);
        currentAssociationEnd2.setMultiplicity(EdmMultiplicity.ONE);
        if (annotatedElement != null) {
          ManyToOne reln = annotatedElement.getAnnotation(ManyToOne.class);
          if (reln != null && reln.optional()) {
            currentAssociationEnd2.setMultiplicity(EdmMultiplicity.ZERO_TO_ONE);
          }
        }
        break;
      case ONE_TO_ONE:
        currentAssociationEnd1.setMultiplicity(EdmMultiplicity.ONE);
        currentAssociationEnd2.setMultiplicity(EdmMultiplicity.ONE);
        if (annotatedElement != null) {
          OneToOne reln = annotatedElement.getAnnotation(OneToOne.class);
          if (reln != null) {
            mappedBy = reln.mappedBy();
            if(reln.optional()) {
              currentAssociationEnd2.setMultiplicity(EdmMultiplicity.ZERO_TO_ONE);
            }
          }
        }
        break;
      default:
        break;
      }
    }
  }

  @Override
  public boolean compare(final AssociationEnd end1, final AssociationEnd end2) {
    FullQualifiedName end1Type = end1.getType();
    FullQualifiedName currentAssociationEnd1Type = currentAssociationEnd1.getType();
    FullQualifiedName end2Type = end2.getType();
    FullQualifiedName currentAssociationEnd2Type = currentAssociationEnd2.getType();

    if(end1.getMultiplicity() == null || currentAssociationEnd1.getMultiplicity() == null) {
      return false;
    }
    if(end2.getMultiplicity() == null || currentAssociationEnd2.getMultiplicity() == null) {
      return false;
    }

    boolean end1eqCurEnd1 = end1.getMultiplicity().equals(currentAssociationEnd1.getMultiplicity());
    boolean end2eqCurEnd2 = end2.getMultiplicity().equals(currentAssociationEnd2.getMultiplicity());
    if(end1Type.equals(currentAssociationEnd1Type) && end2Type.equals(currentAssociationEnd2Type)
        && end1eqCurEnd1 && end2eqCurEnd2) {
      return true;
    }

    boolean end1EqCurEnd2 = end1.getMultiplicity().equals(currentAssociationEnd2.getMultiplicity());
    boolean end2EqCurEnd1 = end2.getMultiplicity().equals(currentAssociationEnd1.getMultiplicity());
    if(end1Type.equals(currentAssociationEnd2Type) && end2Type.equals(currentAssociationEnd1Type)
        && end1EqCurEnd2 && end2EqCurEnd1) {
      return true;
    }

    boolean end1IsZeroToOne = end1.getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE;
    boolean end1IsOne = end1.getMultiplicity() == EdmMultiplicity.ONE;
    boolean end2IsZeroToOne = end2.getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE;
    boolean end2IsOne = end2.getMultiplicity() == EdmMultiplicity.ONE;

    if (end1Type.equals(currentAssociationEnd1Type) && end2Type.equals(currentAssociationEnd2Type)) {
        if ((end1IsZeroToOne && currentAssociationEnd1.getMultiplicity() == EdmMultiplicity.ONE
            || end1IsOne && currentAssociationEnd1.getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE)
            && end2eqCurEnd2) {
          return true;
        }
        if ((end2IsZeroToOne && currentAssociationEnd2.getMultiplicity() == EdmMultiplicity.ONE
            || end2IsOne && currentAssociationEnd2.getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE)
            && end1eqCurEnd1) {
          return true;
        }
    }

    if (end2Type.equals(currentAssociationEnd1Type) && end1Type.equals(currentAssociationEnd2Type)) {
      if ((end1IsZeroToOne && currentAssociationEnd2.getMultiplicity() == EdmMultiplicity.ONE
          || end1IsOne && currentAssociationEnd2.getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE)
          && end2EqCurEnd1) {
        return true;
      }
      if ((end2IsZeroToOne && currentAssociationEnd1.getMultiplicity() == EdmMultiplicity.ONE
          || end2IsOne && currentAssociationEnd1.getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE)
          && end1EqCurEnd2) {
        return true;
      }
    }

    return false;
  }

  @Override
  public String[] getJoinColumnNames() {
    return columnNames;
  }

  @Override
  public String[] getJoinColumnReferenceColumnNames() {
    return referencedColumnNames;
  }

  @Override
  public String getMappedByName() {
    return mappedBy;
  }

  @Override
  public String getOwningPropertyName() {
    return ownerPropertyName;
  }

}
