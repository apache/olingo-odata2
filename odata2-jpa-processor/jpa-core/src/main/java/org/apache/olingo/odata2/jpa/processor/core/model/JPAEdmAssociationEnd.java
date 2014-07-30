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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.List;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;

import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationEndView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmNameBuilder;

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

      String end1Role = currentAssociationEnd1.getType().getName();
      String end2Role = currentAssociationEnd2.getType().getName();

      if (end1Role.equals(end2Role)) {
        end1Role = end1Role + "1";
        end2Role = end2Role + "2";
      }

      currentAssociationEnd1.setRole(end1Role);
      currentAssociationEnd2.setRole(end2Role);

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
        break;
      case ONE_TO_ONE:
        currentAssociationEnd1.setMultiplicity(EdmMultiplicity.ONE);
        currentAssociationEnd2.setMultiplicity(EdmMultiplicity.ONE);
        if (annotatedElement != null) {
          OneToOne reln = annotatedElement.getAnnotation(OneToOne.class);
          if (reln != null) {
            mappedBy = reln.mappedBy();
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
    if ((end1.getType().equals(currentAssociationEnd1.getType())
        && end2.getType().equals(currentAssociationEnd2.getType())
        && end1.getMultiplicity().equals(currentAssociationEnd1.getMultiplicity()) && end2.getMultiplicity().equals(
        currentAssociationEnd2.getMultiplicity()))
        || (end1.getType().equals(currentAssociationEnd2.getType())
            && end2.getType().equals(currentAssociationEnd1.getType())
            && end1.getMultiplicity().equals(currentAssociationEnd2.getMultiplicity()) && end2.getMultiplicity()
            .equals(currentAssociationEnd1.getMultiplicity()))) {
      return true;
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
