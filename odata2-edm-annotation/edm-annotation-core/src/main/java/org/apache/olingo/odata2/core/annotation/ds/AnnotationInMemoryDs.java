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
package org.apache.olingo.odata2.core.annotation.ds;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.data.ListsDataSource;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.core.annotation.edm.AnnotationHelper;
import org.apache.olingo.odata2.core.annotation.edm.ClassHelper;

public class AnnotationInMemoryDs implements ListsDataSource {

  private static final AnnotationHelper ANNOTATION_HELPER = new AnnotationHelper();
  private final Map<String, DataStore<Object>> dataStores = new HashMap<String, DataStore<Object>>();

  public AnnotationInMemoryDs(String packageToScan) {
    List<Class<?>> foundClasses = ClassHelper.loadClasses(packageToScan, new ClassHelper.ClassValidator() {
      @Override
      public boolean isClassValid(Class<?> c) {
        return null != c.getAnnotation(EdmEntityType.class);
      }
    });

    init(foundClasses);
  }

  @SuppressWarnings("unchecked")
  private void init(List<Class<?>> foundClasses) {
    for (Class<?> clz : foundClasses) {

      DataStore<Object> dhs = (DataStore<Object>) DataStore.createInMemory(clz);
      EdmEntityType entityType = clz.getAnnotation(EdmEntityType.class);
      dataStores.put(entityType.name(), dhs);
    }
  }
  
  public <T> DataStore<T> getDataStore(Class<T> clazz) {
    return DataStore.createInMemory(clazz);
  }

  @Override
  public List<?> readData(EdmEntitySet entitySet) throws ODataNotImplementedException,
          ODataNotFoundException, EdmException, ODataApplicationException {

    final String name = entitySet.getEntityType().getName();

    DataStore<Object> holder = dataStores.get(name);
    if (holder != null) {
      return new ArrayList(holder.read());
    }

    throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
  }

  @Override
  public Object readData(EdmEntitySet entitySet, Map<String, Object> keys) 
      throws ODataNotFoundException, EdmException, ODataApplicationException {
    final String name = entitySet.getEntityType().getName();

    DataStore<Object> store = dataStores.get(name);
    if (store != null) {
        Object keyInstance = store.createInstance();
        setKeyFields(keyInstance, keys.values().toArray());

        Object result = store.read(keyInstance);
        if (result != null) {
          return result;
        }
    }

    throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
  }

  
  @Override
  public Object readData(EdmFunctionImport function, Map<String, Object> parameters, Map<String, Object> keys)
          throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);
  }

  @Override
  public Object readRelatedData(EdmEntitySet sourceEntitySet, Object sourceData, EdmEntitySet targetEntitySet,
          Map<String, Object> targetKeys)
          throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    final Object data;
    if (targetKeys.isEmpty()) {
      data = this.readData(targetEntitySet);
    } else {
      data = this.readData(targetEntitySet, targetKeys);
    }

    return data;
  }

  @Override
  public BinaryData readBinaryData(EdmEntitySet entitySet, Object mediaLinkEntryData)
          throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);
  }

  @Override
  public Object newDataObject(EdmEntitySet entitySet)
          throws ODataNotImplementedException, EdmException, ODataApplicationException {
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);
  }

  @Override
  public void writeBinaryData(EdmEntitySet entitySet, Object mediaLinkEntryData, BinaryData binaryData)
          throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);

  }

  @Override
  public void deleteData(EdmEntitySet entitySet, Map<String, Object> keys)
          throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);

  }

  @Override
  public void createData(EdmEntitySet entitySet, Object data)
          throws ODataNotImplementedException, EdmException, ODataApplicationException {
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);

  }

  @Override
  public void deleteRelation(EdmEntitySet sourceEntitySet, Object sourceData, EdmEntitySet targetEntitySet,
          Map<String, Object> targetKeys)
          throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);

  }

  @Override
  public void writeRelation(EdmEntitySet sourceEntitySet, Object sourceData, EdmEntitySet targetEntitySet,
          Map<String, Object> targetKeys)
          throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);
  }
  

  private <T> T setKeyFields(T instance, Object[] keyValues) {
    List<Field> fields = ANNOTATION_HELPER.getAnnotatedFields(instance, EdmKey.class);
    if (fields.size() != keyValues.length) {
      throw new IllegalStateException("Wrong amount of key properties. Expected read keys = "
              + fields + " given key predicates = " + Arrays.toString(keyValues));
    }
    
    String propertyName = ANNOTATION_HELPER.getCanonicalName(fields.get(0));
    ANNOTATION_HELPER.setValueForProperty(instance, propertyName, keyValues[0]);

    return instance;
  }
}
