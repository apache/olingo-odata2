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

import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityCreate;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityDelete;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityRead;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntitySetRead;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityUpdate;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.core.annotation.edm.AnnotationHelper;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *
 */
public class DataStore<T> {

  private static final AnnotationHelper ANNOTATION_HELPER = new AnnotationHelper();
  private final List<T> dataStore;
  private final Class<T> dataTypeClass;

  private int idCounter = 1;
  
  private static class InMemoryDataStore {
    private static final Map<Class<?>, DataStore<?>> c2ds = new HashMap<Class<?>, DataStore<?>>();
    @SuppressWarnings("unchecked")
    static DataStore<?> getInstance(Class<?> clz) {
      DataStore<?> ds = c2ds.get(clz);
      if(ds == null) {
        ds = new DataStore<Object>((Class<Object>) clz);
        c2ds.put(clz, ds);
      }
      return ds;
    }
  }
  
  @SuppressWarnings("unchecked")
  public static <T> DataStore<T> createInMemory(Class<T> clazz) {
    return (DataStore<T>) InMemoryDataStore.getInstance(clazz);
  }
  
  private DataStore(List<T> wrapStore, Class<T> clz) {
    dataStore = wrapStore;
    dataTypeClass = clz;
  }

  private DataStore(Class<T> clz) {
    this(new ArrayList<T>(), clz);
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
  
  @EntityRead
  public T read(T obj) {
    List<Object> objKeys = getKeys(obj);
    for (T stored : dataStore) {
      if (objKeys.equals(getKeys(stored))) {
        return stored;
      }
    }
    return null;
  }

  @EntitySetRead
  public Collection<T> read() {
    return Collections.unmodifiableCollection(dataStore);
  }

  @EntityCreate
  public T create(T object) throws DataStoreException {
    createKeys(object);
    dataStore.add(object);
    return object;
  }

  @EntityUpdate
  public T update(T object) {
    T stored = read(object);
    dataStore.remove(stored);
    dataStore.add(object);
    return object;
  }

  @EntityDelete
  public T delete(T object) {
    T stored = read(object);
    if(stored != null) {    
      dataStore.remove(stored);
    }
    return stored;
  }
  
  private List<Object> getKeys(T object) {
    Map<String, Object> keys = ANNOTATION_HELPER.getValueForAnnotatedFields(object, EdmKey.class);
    
    // XXX: list should be in a defined order -> better to create an 'Key' object which is comparable 
    List<Object> keyList = new ArrayList(keys.values());
    return keyList;
  }
  
  private T createKeys(T object) throws DataStoreException {
    List<Field> fields = ANNOTATION_HELPER.getAnnotatedFields(object, EdmKey.class);
    if(fields.isEmpty()) {
      throw new DataStoreException("No EdmKey annotated fields found for class " + object.getClass());
    }
    Map<String, Object> fieldName2KeyValue = new HashMap<String, Object>();
    
    for (Field field : fields) {
      Object key = createKey(field);
      fieldName2KeyValue.put(ANNOTATION_HELPER.getCanonicalName(field), key);
    }
    
    ANNOTATION_HELPER.setValuesToAnnotatedFields(fieldName2KeyValue, object, EdmKey.class);
    
    return object;
  }

  private Object createKey(Field field) {
    Class<?> type = field.getType();
    
    if(type == String.class) {
      return String.valueOf(idCounter++);
    } else if(type == Integer.class || type == int.class) {
      return Integer.valueOf(idCounter++);
    } else if(type == Long.class || type == long.class) {
      return Long.valueOf(idCounter++);
    }
    
    throw new UnsupportedOperationException("Automated key generation for type '" + type
            + "' is not supported (caused on field '" + field + "').");
  }
  
  public static class DataStoreException extends ODataApplicationException {

    public DataStoreException(String message) {
      this(message, null);
    }

    public DataStoreException(String message, Throwable cause) {
      super(message, Locale.ENGLISH, cause);
    }
  }
}
