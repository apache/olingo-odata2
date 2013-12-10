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
package org.apache.olingo.odata2.core.annotation.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.core.annotation.util.AnnotationHelper;
import org.apache.olingo.odata2.core.annotation.util.ClassHelper;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *
 */
public class DataStore<T> {

  private static final AnnotationHelper ANNOTATION_HELPER = new AnnotationHelper();
  private final Map<KeyElement, T> dataStore;
  private final Class<T> dataTypeClass;
  private final KeyAccess keyAccess;

  private int idCounter = 1;

  private static class InMemoryDataStore {
    private static final Map<Class<?>, DataStore<?>> c2ds = new HashMap<Class<?>, DataStore<?>>();

    @SuppressWarnings("unchecked")
    static DataStore<?> getInstance(Class<?> clz, boolean createNewInstance) {
      DataStore<?> ds = c2ds.get(clz);
      if (createNewInstance || ds == null) {
        ds = new DataStore<Object>((Class<Object>) clz);
        c2ds.put(clz, ds);
      }
      return ds;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> DataStore<T> createInMemory(Class<T> clazz) {
    return (DataStore<T>) InMemoryDataStore.getInstance(clazz, true);
  }

  @SuppressWarnings("unchecked")
  public static <T> DataStore<T> createInMemory(Class<T> clazz, boolean keepExisting) {
    return (DataStore<T>) InMemoryDataStore.getInstance(clazz, !keepExisting);
  }

  private DataStore(Map<KeyElement, T> wrapStore, Class<T> clz) {
    dataStore = Collections.synchronizedMap(wrapStore);
    dataTypeClass = clz;
    try {
      keyAccess = new KeyAccess(clz);
    } catch (DataStoreException ex) {
      // FIXME: replace exception with correct error handling
      throw new RuntimeException("");
    }
  }

  private DataStore(Class<T> clz) {
    this(new HashMap<KeyElement, T>(), clz);
  }

  public Class<T> getDataTypeClass() {
    return dataTypeClass;
  }

  public String getEntityTypeName() {
    return ANNOTATION_HELPER.extractEntityTypeName(dataTypeClass);
  }

  public T createInstance() {
    try {
      return dataTypeClass.newInstance();
    } catch (InstantiationException e) {
      throw new ODataRuntimeException("Unable to create instance of class '" + dataTypeClass + "'.", e);
    } catch (IllegalAccessException e) {
      throw new ODataRuntimeException("Unable to create instance of class '" + dataTypeClass + "'.", e);
    }
  }

  public T read(T obj) {
    KeyElement objKeys = getKeys(obj);
    return dataStore.get(objKeys);
  }

  public Collection<T> read() {
    return Collections.unmodifiableCollection(dataStore.values());
  }

  public T create(T object) throws DataStoreException {
    synchronized (dataStore) {
      KeyElement keyElement = getKeys(object);
      if (dataStore.get(keyElement) != null || !keyElement.allKeysSet()) {
        createKeys(object);
        return this.create(object);
      }
      dataStore.put(keyElement, object);
    }
    return object;
  }

  public T update(T object) {
    synchronized (dataStore) {
      KeyElement keyElement = getKeys(object);
      dataStore.remove(keyElement);
      dataStore.put(keyElement, object);
    }
    return object;
  }

  public T delete(T object) {
    synchronized (dataStore) {
      KeyElement keyElement = getKeys(object);
      return dataStore.remove(keyElement);
    }
  }

  private class KeyElement {
    private final List<Object> keyValues;
    private int cachedHashCode;

    public KeyElement(int size) {
      keyValues = new ArrayList<Object>(size);
    }

    public KeyElement() {
      this(2);
    }

    private void addValue(Object keyValue) {
      keyValues.add(keyValue);
      cachedHashCode = keyValues.hashCode();
    }
    
    boolean allKeysSet() {
      return !keyValues.contains(null);
    }

    @Override
    public int hashCode() {
      return cachedHashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final KeyElement other = (KeyElement) obj;
      if (this.keyValues != other.keyValues && (this.keyValues == null || !this.keyValues.equals(other.keyValues))) {
        return false;
      }
      return true;
    }
  }
  
  private class KeyAccess {
    List<Field> keyFields;
    
    KeyAccess(Class<?> clazz) throws DataStoreException {
      keyFields = ANNOTATION_HELPER.getAnnotatedFields(clazz, EdmKey.class);
      if (keyFields.isEmpty()) {
        throw new DataStoreException("No EdmKey annotated fields found for class " + clazz);
      }
    }
    
    KeyElement getKeyValues(T object) {
      KeyElement keyElement = new KeyElement(keyFields.size());
      for (Field field : keyFields) {
        Object keyValue = ClassHelper.getFieldValue(object, field);
        keyElement.addValue(keyValue);
      }

      return keyElement;
    }
    
    T createKeys(T object) throws DataStoreException {
      for (Field field : keyFields) {
        Object key = createKey(field);
        ClassHelper.setFieldValue(object, field, key);
      }

      return object;
    }
    
    private Object createKey(Field field) {
      Class<?> type = field.getType();

      if (type == String.class) {
        return String.valueOf(idCounter++);
      } else if (type == Integer.class || type == int.class) {
        return Integer.valueOf(idCounter++);
      } else if (type == Long.class || type == long.class) {
        return Long.valueOf(idCounter++);
      }

      throw new UnsupportedOperationException("Automated key generation for type '" + type
          + "' is not supported (caused on field '" + field + "').");
    }
  }
  
  private KeyElement getKeys(T object) {
    return keyAccess.getKeyValues(object);
  }

  private T createKeys(T object) throws DataStoreException {
    return keyAccess.createKeys(object);
  }

  

  public static class DataStoreException extends ODataApplicationException {
    private static final long serialVersionUID = 42L;

    public DataStoreException(String message) {
      this(message, null);
    }

    public DataStoreException(String message, Throwable cause) {
      super(message, Locale.ENGLISH, cause);
    }
  }
}
