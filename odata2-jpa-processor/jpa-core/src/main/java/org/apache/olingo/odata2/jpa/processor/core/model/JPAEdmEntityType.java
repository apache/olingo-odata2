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

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.persistence.EntityListeners;

import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmMappingModelAccess;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmKeyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmNavigationPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmNameBuilder;

public class JPAEdmEntityType extends JPAEdmBaseViewImpl implements JPAEdmEntityTypeView {

  private JPAEdmSchemaView schemaView = null;
  private EntityType currentEdmEntityType = null;
  private javax.persistence.metamodel.EntityType<?> currentJPAEntityType = null;
  private EntityTypeList<EntityType> consistentEntityTypes = null;

  private HashMap<String, EntityType> consistentEntityTypeMap;

  public JPAEdmEntityType(final JPAEdmSchemaView view) {
    super(view);
    schemaView = view;
    consistentEntityTypeMap = new HashMap<String, EntityType>();
  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmEntityTypeBuilder();
    }

    return builder;
  }

  @Override
  public EntityType getEdmEntityType() {
    return currentEdmEntityType;
  }

  @Override
  public javax.persistence.metamodel.EntityType<?> getJPAEntityType() {
    return currentJPAEntityType;
  }

  @Override
  public List<EntityType> getConsistentEdmEntityTypes() {
    return consistentEntityTypes;
  }

  @Override
  public EntityType searchEdmEntityType(final String jpaEntityTypeName) {
    return consistentEntityTypeMap.get(jpaEntityTypeName);
  }

  private class JPAEdmEntityTypeBuilder implements JPAEdmBuilder {

    @SuppressWarnings("unchecked")
    @Override
    public void build() throws ODataJPAModelException, ODataJPARuntimeException {

      Collection<javax.persistence.metamodel.EntityType<?>> jpaEntityTypes = metaModel.getEntities();

      if (jpaEntityTypes == null || jpaEntityTypes.isEmpty() == true) {
        return;
      } else if (consistentEntityTypes == null) {
        consistentEntityTypes = new EntityTypeList<EntityType>();

      }

      jpaEntityTypes = sortJPAEntityTypes(jpaEntityTypes);
      for (javax.persistence.metamodel.EntityType<?> jpaEntityType : jpaEntityTypes) {
        currentEdmEntityType = new EntityType();
        currentJPAEntityType = jpaEntityType;

        // Check for need to Exclude
        if (isExcluded(JPAEdmEntityType.this)) {
          continue;
        }

        JPAEdmNameBuilder.build(JPAEdmEntityType.this);
        JPAEdmMapping jpaEdmMapping = (JPAEdmMapping) currentEdmEntityType.getMapping();
        EntityListeners entityListners = currentJPAEntityType.getJavaType().getAnnotation(EntityListeners.class);
        if (entityListners != null) {
          for (Class<EntityListeners> entityListner : entityListners.value()) {
            if (ODataJPATombstoneEntityListener.class.isAssignableFrom(entityListner)) {
              jpaEdmMapping
                  .setODataJPATombstoneEntityListener((Class<? extends ODataJPATombstoneEntityListener>)
                  (Object) entityListner);
              break;
            }
          }
        }
        JPAEdmPropertyView propertyView = new JPAEdmProperty(schemaView);
        propertyView.getBuilder().build();

        currentEdmEntityType.setProperties(propertyView.getEdmPropertyList());
        if (propertyView.getJPAEdmNavigationPropertyView() != null) {
          JPAEdmNavigationPropertyView navPropView = propertyView.getJPAEdmNavigationPropertyView();
          if (navPropView.getConsistentEdmNavigationProperties() != null
              && !navPropView.getConsistentEdmNavigationProperties().isEmpty()) {
            currentEdmEntityType.setNavigationProperties(navPropView.getConsistentEdmNavigationProperties());
          }
        }
        JPAEdmKeyView keyView = propertyView.getJPAEdmKeyView();
        currentEdmEntityType.setKey(keyView.getEdmKey());

        consistentEntityTypes.add(currentEdmEntityType);
        consistentEntityTypeMap.put(currentJPAEntityType.getName(), currentEdmEntityType);
      }

    }

    private List<javax.persistence.metamodel.EntityType<?>> sortJPAEntityTypes(
      final Collection<javax.persistence.metamodel.EntityType<?>> entities) {

      List<javax.persistence.metamodel.EntityType<?>> entityTypeList =
        new ArrayList<javax.persistence.metamodel.EntityType<?>>(entities.size());

        Iterator<javax.persistence.metamodel.EntityType<?>> itr;
        javax.persistence.metamodel.EntityType<?> smallestJpaEntity;
        javax.persistence.metamodel.EntityType<?> currentJpaEntity;
        while (!entities.isEmpty()) {
          itr = entities.iterator();
          smallestJpaEntity = itr.next();
          while (itr.hasNext()) {
            currentJpaEntity = itr.next();
            if (smallestJpaEntity.getName().compareTo(currentJpaEntity.getName()) > 0) {
              smallestJpaEntity = currentJpaEntity;
            }
          }
          entityTypeList.add(smallestJpaEntity);
          entities.remove(smallestJpaEntity);
        }

      return entityTypeList;
    }

    private boolean isExcluded(final JPAEdmEntityType jpaEdmEntityType) {
      JPAEdmMappingModelAccess mappingModelAccess = jpaEdmEntityType.getJPAEdmMappingModelAccess();
      if (mappingModelAccess != null && mappingModelAccess.isMappingModelExists()
          && mappingModelAccess.checkExclusionOfJPAEntityType(jpaEdmEntityType.getJPAEntityType().getName())) {
        return true;
      }
      return false;
    }

  }

  private class EntityTypeList<X> extends ArrayList<EntityType> {

    /**
     * 
     */
    private static final long serialVersionUID = 719079109608251592L;

    @Override
    public Iterator<EntityType> iterator() {
      return new EntityTypeListIterator<X>(size());
    }

  }

  private class EntityTypeListIterator<E> implements ListIterator<EntityType> {

    private int size = 0;
    private int pos = 0;

    public EntityTypeListIterator(final int listSize) {
      this.size = listSize;
    }

    @Override
    public void add(final EntityType e) {
      consistentEntityTypes.add(e);
      size++;
    }

    @Override
    public boolean hasNext() {
      if (pos < size) {
        return true;
      }

      return false;
    }

    @Override
    public boolean hasPrevious() {
      if (pos > 0) {
        return true;
      }
      return false;
    }

    @Override
    public EntityType next() {
      if (pos < size) {
        currentEdmEntityType = consistentEntityTypes.get(pos++);
        return currentEdmEntityType;
      }

      return null;
    }

    @Override
    public int nextIndex() {
      return pos;
    }

    @Override
    public EntityType previous() {
      if (pos > 0 && pos < size) {
        currentEdmEntityType = consistentEntityTypes.get(--pos);
        return currentEdmEntityType;
      }
      return null;
    }

    @Override
    public int previousIndex() {
      if (pos > 0) {
        return pos - 1;
      }

      return 0;
    }

    @Override
    public void remove() {
      consistentEntityTypes.remove(pos);
    }

    @Override
    public void set(final EntityType e) {
      consistentEntityTypes.set(pos, e);
    }

  }
}
