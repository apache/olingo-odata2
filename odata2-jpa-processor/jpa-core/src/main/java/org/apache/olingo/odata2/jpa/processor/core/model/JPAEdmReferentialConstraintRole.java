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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.metamodel.Attribute;

import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraintRole;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmReferentialConstraintRoleView;

public class JPAEdmReferentialConstraintRole extends JPAEdmBaseViewImpl implements JPAEdmReferentialConstraintRoleView {

  private boolean firstBuild = true;

  private JPAEdmEntityTypeView entityTypeView;
  private JPAEdmReferentialConstraintRoleView.RoleType roleType;

  private Attribute<?, ?> jpaAttribute;
  private List<String[]> jpaColumnNames;
  private Association association;

  private boolean roleExists = false;

  private JPAEdmRefConstraintRoleBuilder builder;
  private ReferentialConstraintRole currentRole;

  public JPAEdmReferentialConstraintRole(final JPAEdmReferentialConstraintRoleView.RoleType roleType,
      final JPAEdmEntityTypeView entityTypeView, final JPAEdmPropertyView propertyView,
      final JPAEdmAssociationView associationView) {

    super(entityTypeView);
    this.entityTypeView = entityTypeView;
    this.roleType = roleType;

    jpaAttribute = propertyView.getJPAAttribute();
    jpaColumnNames = propertyView.getJPAJoinColumns();
    association = associationView.getEdmAssociation();

  }

  @Override
  public boolean isExists() {
    return roleExists;

  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmRefConstraintRoleBuilder();
    }

    return builder;
  }

  @Override
  public RoleType getRoleType() {
    return roleType;
  }

  @Override
  public ReferentialConstraintRole getEdmReferentialConstraintRole() {
    return currentRole;
  }

  @Override
  public String getJPAColumnName() {
    return null;
  }

  @Override
  public String getEdmEntityTypeName() {
    return null;
  }

  @Override
  public String getEdmAssociationName() {
    return null;
  }

  private class JPAEdmRefConstraintRoleBuilder implements JPAEdmBuilder {

    @Override
    public void build() throws ODataJPAModelException {
      if (firstBuild) {
        firstBuild();
      } else if (roleExists) {
        try {
          buildRole();
        } catch (SecurityException e) {
          throw ODataJPAModelException.throwException(ODataJPAModelException.GENERAL.addContent(e.getMessage()), e);
        } catch (NoSuchFieldException e) {
          throw ODataJPAModelException.throwException(ODataJPAModelException.GENERAL.addContent(e.getMessage()), e);
        }
      }

    }

    private void firstBuild() {
      firstBuild = false;
      isConsistent = false;

      if (jpaColumnNames == null || jpaColumnNames.isEmpty()) {
        roleExists = false;
        return;
      } else {
        roleExists = true;
      }
    }

    private void buildRole() throws SecurityException, NoSuchFieldException {

      if (currentRole == null) {
        currentRole = new ReferentialConstraintRole();
        String jpaAttributeType = null;
        EntityType edmEntityType = null;

        if (roleType == RoleType.PRINCIPAL) {
          jpaAttributeType = jpaAttribute.getJavaType().getSimpleName();
          if (jpaAttribute.isCollection()) {
            Type type =
                ((ParameterizedType) jpaAttribute.getJavaMember().getDeclaringClass().getDeclaredField(
                    jpaAttribute.getName()).getGenericType()).getActualTypeArguments()[0];
            int lastIndexOfDot = type.toString().lastIndexOf(".");
            jpaAttributeType = type.toString().substring(lastIndexOfDot + 1);
          }
          edmEntityType = entityTypeView.searchEdmEntityType(jpaAttributeType);
        } else if (roleType == RoleType.DEPENDENT) {
          edmEntityType =
              entityTypeView.searchEdmEntityType(jpaAttribute.getDeclaringType().getJavaType().getSimpleName());
        }

        List<PropertyRef> propertyRefs = new ArrayList<PropertyRef>();
        if (edmEntityType != null) {
          for (String[] columnName : jpaColumnNames) {
            for (Property property : edmEntityType.getProperties()) {
              if (columnName[0].equals(((JPAEdmMapping) property.getMapping()).getJPAColumnName()) ||
                  columnName[0].equals(property.getName()) ||
                  columnName[1].equals(((JPAEdmMapping) property.getMapping()).getJPAColumnName()) ||
                  columnName[1].equals(property.getName())) {
                PropertyRef propertyRef = new PropertyRef();
                propertyRef.setName(property.getName());
                propertyRefs.add(propertyRef);
                break;
              }
            }
          }
          currentRole.setPropertyRefs(propertyRefs);
          if (propertyRefs.isEmpty()) {
            isConsistent = false;
            return;
          }
          AssociationEnd end = association.getEnd1();
          if (end.getType().getName().equals(edmEntityType.getName())) {
            currentRole.setRole(end.getRole());
            isConsistent = true;
          } else {
            end = association.getEnd2();
            if (end.getType().getName().equals(edmEntityType.getName())) {
              currentRole.setRole(end.getRole());
              isConsistent = true;
            }
          }
        }

      }
    }
  }
}
