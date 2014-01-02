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

import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmNavigationPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmNameBuilder;

public class JPAEdmNavigationProperty extends JPAEdmBaseViewImpl implements JPAEdmNavigationPropertyView {

  private JPAEdmAssociationView associationView = null;
  private NavigationProperty currentNavigationProperty = null;
  private JPAEdmPropertyView propertyView = null;
  private List<NavigationProperty> consistentNavigationProperties = null;
  private int count;

  public JPAEdmNavigationProperty(final JPAEdmAssociationView associationView, final JPAEdmPropertyView propertyView,
      final int countNumber) {
    super(associationView);
    this.associationView = associationView;
    this.propertyView = propertyView;
    count = countNumber;
    if (consistentNavigationProperties == null) {
      consistentNavigationProperties = new ArrayList<NavigationProperty>();
    }
  }

  public JPAEdmNavigationProperty(final JPAEdmSchemaView schemaView) {
    super(schemaView);
    consistentNavigationProperties = new ArrayList<NavigationProperty>();

  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmNavigationPropertyBuilder();
    }

    return builder;
  }

  private class JPAEdmNavigationPropertyBuilder implements JPAEdmBuilder {

    @Override
    public void build() throws ODataJPAModelException {

      currentNavigationProperty = new NavigationProperty();
      JPAEdmNameBuilder.build(associationView, propertyView, JPAEdmNavigationProperty.this, skipDefaultNaming, count);
      consistentNavigationProperties.add(currentNavigationProperty);
    }

  }

  @Override
  public NavigationProperty getEdmNavigationProperty() {
    return currentNavigationProperty;
  }

  @Override
  public List<NavigationProperty> getConsistentEdmNavigationProperties() {
    return consistentNavigationProperties;
  }

  @Override
  public void addJPAEdmNavigationPropertyView(final JPAEdmNavigationPropertyView view) {
    if (view != null && view.isConsistent()) {
      currentNavigationProperty = view.getEdmNavigationProperty();
      consistentNavigationProperties.add(currentNavigationProperty);

    }
  }

}
