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
package org.apache.olingo.odata2.jpa.processor.ref.extension;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;

public class SalesOrderProcessingExtension implements JPAEdmExtension {

  @Override
  public void extendJPAEdmSchema(final JPAEdmSchemaView view) {
    Schema edmSchema = view.getEdmSchema();
    edmSchema.getComplexTypes().add(getComplexType());
  }

  private ComplexType getComplexType() {
    ComplexType complexType = new ComplexType();

    List<Property> properties = new ArrayList<Property>();
    SimpleProperty property = new SimpleProperty();

    property.setName("Amount");
    property.setType(EdmSimpleTypeKind.Double);
    properties.add(property);

    property = new SimpleProperty();
    property.setName("Currency");
    property.setType(EdmSimpleTypeKind.String);
    properties.add(property);

    complexType.setName("OrderValue");
    complexType.setProperties(properties);

    return complexType;

  }

  @Override
  public void extendWithOperation(final JPAEdmSchemaView view) {
    view.registerOperations(SalesOrderHeaderProcessor.class, null);
    view.registerOperations(CustomerImageProcessor.class, null);

  }

  @Override
  public InputStream getJPAEdmMappingModelStream() {
    return null;
  }

}