/**
 * *****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *****************************************************************************
 */
package org.apache.olingo.odata2.core.annotation.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmMediaResourceContent;
import org.apache.olingo.odata2.api.annotation.edm.EdmMediaResourceMimeType;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.data.ListsDataSource;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.core.annotation.edm.AnnotationHelper;
import org.apache.olingo.odata2.core.annotation.edm.ClassHelper;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

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
      String entityTypeName = ANNOTATION_HELPER.extractEntityTypeName(clz);
      dataStores.put(entityTypeName, dhs);
    }
  }

  public <T> DataStore<T> getDataStore(Class<T> clazz) {
    return DataStore.createInMemory(clazz);
  }

  @Override
  public List<?> readData(EdmEntitySet entitySet) throws ODataNotImplementedException,
          ODataNotFoundException, EdmException, ODataApplicationException {

    DataStore<Object> holder = getDataStore(entitySet);
    if (holder != null) {
      return new ArrayList(holder.read());
    }

    throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
  }

  @Override
  public Object readData(EdmEntitySet entitySet, Map<String, Object> keys)
          throws ODataNotFoundException, EdmException, ODataApplicationException {

    DataStore<Object> store = getDataStore(entitySet);
    if (store != null) {
      Object keyInstance = store.createInstance();
      ANNOTATION_HELPER.setKeyFields(keyInstance, keys);

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

    if (targetKeys.isEmpty()) {
      String sourceName = sourceEntitySet.getEntityType().getName();
      DataStore sourceStore = dataStores.get(sourceName);

      String targetName = targetEntitySet.getEntityType().getName();
      DataStore targetStore = dataStores.get(targetName);

      AnnotationHelper.AnnotatedNavInfo navigationInfo = extractNavigationInfo(sourceStore, targetStore);

      Field sourceFieldAtTarget = navigationInfo.getToField();
      if (sourceFieldAtTarget == null) {
        throw new ODataRuntimeException("Missing source field for related data.");
      }

      Collection targetData = targetStore.read();
      List resultData = new ArrayList();
      for (Object targetInstance : targetData) {
        Object targetNavigationInstance = getValue(sourceFieldAtTarget, targetInstance);
        if(targetNavigationInstance instanceof Collection) {
          Collection c = (Collection) targetNavigationInstance;
          for (Object object : c) {
            if (ANNOTATION_HELPER.keyMatch(sourceData, object)) {
              resultData.add(targetInstance);
            }
          }
        } else if (ANNOTATION_HELPER.keyMatch(sourceData, targetNavigationInstance)) {
          resultData.add(targetInstance);
        }
      }
      
      if(resultData.isEmpty()) {
        return null;
      } else if(navigationInfo.getToMultiplicity() == EdmMultiplicity.ONE) {
        return resultData.get(0);
      } else {
        return resultData;
      }
    } else {
      throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);
    }
  }

  @Override
  public BinaryData readBinaryData(EdmEntitySet entitySet, Object mediaLinkEntryData)
          throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {

    Object data = ANNOTATION_HELPER.getValueForField(mediaLinkEntryData, EdmMediaResourceContent.class);
    Object mimeType = ANNOTATION_HELPER.getValueForField(mediaLinkEntryData, EdmMediaResourceMimeType.class);

    BinaryData db = new BinaryData((byte[])data, String.valueOf(mimeType));
    return db;
  }

  @Override
  public Object newDataObject(EdmEntitySet entitySet)
          throws ODataNotImplementedException, EdmException, ODataApplicationException {

    DataStore<Object> dataStore = getDataStore(entitySet);
    if (dataStore != null) {
      return dataStore.createInstance();
    }

    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);
  }

  @Override
  public void writeBinaryData(EdmEntitySet entitySet, Object mediaLinkEntryData, BinaryData binaryData)
          throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);

  }

  /**
   * <p>Updates a single data object identified by the specified entity set and key fields of
   * the data object.</p>
   * @param entitySet the {@link EdmEntitySet} the object must correspond to
   * @param data the data object of the new entity
   * @return updated data object instance
   * @throws org.apache.olingo.odata2.api.exception.ODataNotImplementedException
   * @throws org.apache.olingo.odata2.api.edm.EdmException
   * @throws org.apache.olingo.odata2.api.exception.ODataApplicationException
   */
  public Object updateData(EdmEntitySet entitySet, Object data)
          throws ODataNotImplementedException, EdmException, ODataApplicationException {

    DataStore<Object> dataStore = getDataStore(entitySet);
    return dataStore.update(data);
  }

  @Override
  public void deleteData(EdmEntitySet entitySet, Map<String, Object> keys)
          throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    DataStore<Object> dataStore = getDataStore(entitySet);
    Object keyInstance = dataStore.createInstance();
    ANNOTATION_HELPER.setKeyFields(keyInstance, keys);
    dataStore.delete(keyInstance);
  }

  @Override
  public void createData(EdmEntitySet entitySet, Object data)
          throws ODataNotImplementedException, EdmException, ODataApplicationException {

    DataStore<Object> dataStore = getDataStore(entitySet);
    dataStore.create(data);
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


  /**
   * Returns corresponding DataStore for EdmEntitySet or if no data store is registered an
   * ODataRuntimeException is thrown.
   * Never returns NULL.
   * 
   * @param entitySet for which the corresponding DataStore is returned
   * @return a DataStore object 
   * @throws EdmException 
   * @throws  ODataRuntimeException if no DataStore is found
   */
  private DataStore<Object> getDataStore(EdmEntitySet entitySet) throws EdmException {
    final String name = entitySet.getEntityType().getName();
    DataStore<Object> dataStore = dataStores.get(name);
    if(dataStore == null) {
      throw new ODataRuntimeException("No DataStore found for entity set '" + entitySet + "'.");
    }
    return dataStore;
  }

  private Object getValue(Field field, Object instance) {
    try {
      boolean access = field.isAccessible();
      field.setAccessible(true);
      Object value = field.get(instance);
      field.setAccessible(access);
      return value;
    } catch (IllegalArgumentException e) {
      throw new ODataRuntimeException("Error for getting value of field '"
              + field + "' at instance '" + instance + "'.", e);
    } catch (IllegalAccessException e) {
      throw new ODataRuntimeException("Error for getting value of field '"
              + field + "' at instance '" + instance + "'.", e);
    }
  }

  private AnnotationHelper.AnnotatedNavInfo extractNavigationInfo(DataStore sourceStore, DataStore targetStore) {
    Class sourceDataTypeClass = sourceStore.getDataTypeClass();
    Class targetDataTypeClass = targetStore.getDataTypeClass();
    
    return ANNOTATION_HELPER.getCommonNavigationInfo(sourceDataTypeClass, targetDataTypeClass);
  }
}
