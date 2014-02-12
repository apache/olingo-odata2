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
package org.apache.olingo.odata2.jpa.processor.api.model;

import java.util.List;

import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.Property;

/**
 * A view on Java Persistence embeddable types and EDM complex types. Java
 * persistence embeddable types are converted into EDM entity types. Only those
 * embeddable types that are
 * <ol>
 * <li>used in a java persistence Entity type</li>
 * <li>used as non embeddable id of a java persistence entity type</li>
 * </ol>
 * are converted into EDM complex types.
 * <p>
 * The implementation of the view provides access to EDM complex types for the
 * given JPA EDM model. The view acts as a container for consistent list of EDM
 * complex types. An EDM complex type is said to be consistent only if it used
 * in at least one of the EDM entity type.
 * 
 * 
 * <p>
 * @org.apache.olingo.odata2.DoNotImplement
 * @see org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmComplexPropertyView
 * 
 */
public interface JPAEdmComplexTypeView extends JPAEdmBaseView {

  /**
   * The method returns an EDM complex type that is currently being processed.
   * 
   * @return an instance of type {@link org.apache.olingo.odata2.api.edm.provider.ComplexType}
   */
  public ComplexType getEdmComplexType();

  /**
   * The method returns an JPA embeddable type that is currently being
   * processed.
   * 
   * @return an instance of type {@link javax.persistence.metamodel.EmbeddableType}
   */
  public javax.persistence.metamodel.EmbeddableType<?> getJPAEmbeddableType();

  /**
   * The method returns a consistent list of EDM complex types.
   * 
   * @return a list of {@link org.apache.olingo.odata2.api.edm.provider.ComplexType}
   */
  public List<ComplexType> getConsistentEdmComplexTypes();

  /**
   * The method searches for the EDM complex type with in the container for
   * the given JPA embeddable type name.
   * 
   * @param embeddableTypeName
   * is the name of JPA embeddable type
   * @return a reference to EDM complex type if found else null
   */
  public ComplexType searchEdmComplexType(String embeddableTypeName);

  /**
   * The method add a JPA EDM complex type view to the container.
   * 
   * @param view
   * @param isReferencedInKey
   * is the complex type referenced in an Entity as a key property
   * is an instance of type {@link org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmComplexTypeView}
   */
  public void addJPAEdmCompleTypeView(JPAEdmComplexTypeView view);

  /**
   * The method searches for the EDM complex type with in the container for
   * the given EDM complex type's fully qualified name.
   * 
   * @param type
   * is the fully qualified name of EDM complex type
   * @return a reference to EDM complex type if found else null
   */
  public ComplexType searchEdmComplexType(FullQualifiedName type);

  /**
   * The method expands the given EDM complex type into a list of EDM simple
   * properties.
   * 
   * @param complexType
   * is the EDM complex type to expand
   * @param expandedPropertyList
   * is the list to be populated with expanded EDM simple
   * properties
   * @param embeddablePropertyName
   * is the name of the complex property. The name is used as the
   * qualifier for the expanded simple property names.
   */
  public void expandEdmComplexType(ComplexType complexType, List<Property> expandedPropertyList,
      String embeddablePropertyName);

  /**
   * The method checks if the given EDM complex type is referenced as a Key property in any Entity
   * @param complexTypeName
   * EDM complex type name
   * @return
   * <ul><li><b>true</b> : if the complex type is referenced as an entity's key property</li>
   * <li><b>false</b> : if the complex type is not referenced as an entity's key property</li>
   * </ul>
   * 
   */
  public boolean isReferencedInKey(String complexTypeName);

  /**
   * The method sets the given EDM complex type as referenced in an Entity as a key property
   * @param complexTypeName
   * EDM complex Type name
   */
  public void setReferencedInKey(String complexTypeName);

}
