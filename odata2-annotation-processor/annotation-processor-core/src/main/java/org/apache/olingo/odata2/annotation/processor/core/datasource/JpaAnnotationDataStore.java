/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.olingo.odata2.annotation.processor.core.datasource;

import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.apache.olingo.odata2.annotation.processor.core.util.AnnotationHelper;
import org.apache.olingo.odata2.annotation.processor.core.util.AnnotationRuntimeException;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;

/**
 *
 */
public class JpaAnnotationDataStore<T> implements DataStore<T> {

  public static final String DEFAULT_PERSISTENCE_NAME = "JpaAnnotationDataStorePersistence";

  private static final AnnotationHelper ANNOTATION_HELPER = new AnnotationHelper();

  protected Class<T> dataTypeClass;
  protected EntityManager entityManager;

  public static DataStore<?> createInstance(Class<?> clz) {
    return createInstance(clz, DEFAULT_PERSISTENCE_NAME);
  }

  public static DataStore<?> createInstance(Class<?> clz, String persistenceName) {
    return new JpaAnnotationDataStore<Object>((Class<Object>) clz, persistenceName);
  }

  private JpaAnnotationDataStore(final Class<T> clz, String persistenceName) {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceName);
    entityManager = emf.createEntityManager();
    this.dataTypeClass = clz;
  }

  @Override
  public Class<T> getDataTypeClass() {
    return dataTypeClass;
  }

  @Override
  public String getEntityTypeName() {
    return ANNOTATION_HELPER.extractEntityTypeName(dataTypeClass);
  }

  @Override
  public T createInstance() {
    try {
      return dataTypeClass.newInstance();
    } catch (InstantiationException e) {
      throw new AnnotationRuntimeException("Unable to create instance of class '" + dataTypeClass + "'.", e);
    } catch (IllegalAccessException e) {
      throw new AnnotationRuntimeException("Unable to create instance of class '" + dataTypeClass + "'.", e);
    }
  }

  @Override
  public T create(T object) throws DataStoreException {
    EntityTransaction t = this.entityManager.getTransaction();
    try {
      t.begin();
      this.entityManager.persist(object);
      this.entityManager.flush();
      t.commit();
    } catch(Exception e) {
      if(t != null && t.isActive()) {
        t.rollback();
      }
    }
    
    return object;
  }

  @Override
  public T delete(T object) {
    EntityTransaction t = this.entityManager.getTransaction();
    try {
      t.begin();
      object = this.entityManager.merge(object);
      this.entityManager.remove(object);
      this.entityManager.flush();
      t.commit();

      return object;
    } catch(Exception e) {
      if(t != null && t.isActive()) {
        t.rollback();
      }
    }
    return null;
  }

  @Override
  public boolean isKeyEqualChecked(Object first, Object second) throws DataStoreException {
    return ANNOTATION_HELPER.keyMatch(first, second);
  }

  @Override
  public T read(T obj) {
    Object key = ANNOTATION_HELPER.getValueForField(obj, EdmKey.class);
    return this.entityManager.find(dataTypeClass, key);
  }

  @Override
  public Collection<T> read() {
    TypedQuery<T> query = entityManager.createQuery(
            "select t from " + dataTypeClass.getSimpleName() + " t", dataTypeClass);
    return query.getResultList();
  }

  @Override
  public T update(T object) {
    EntityTransaction t = this.entityManager.getTransaction();
    try {
      t.begin();
      object = this.entityManager.merge(object);
      this.entityManager.flush();
      t.commit();

      return object;
    } catch(Exception e) {
      if(t != null && t.isActive()) {
        t.rollback();
      }
    }
    return null;
  }
}
