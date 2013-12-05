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
import org.apache.olingo.odata2.core.annotation.util.AnnotationHelper;
import org.apache.olingo.odata2.core.annotation.util.ClassHelper;
import org.apache.olingo.odata2.core.annotation.util.AnnotationHelper.AnnotatedNavInfo;
import org.apache.olingo.odata2.core.annotation.util.AnnotationHelper.ODataAnnotationException;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

public class AnnotationInMemoryDs implements ListsDataSource {

  private static final AnnotationHelper ANNOTATION_HELPER = new AnnotationHelper();
  private final Map<String, DataStore<Object>> dataStores = new HashMap<String, DataStore<Object>>();
  private final boolean persistInMemory;

  public AnnotationInMemoryDs(String packageToScan) {
    this(packageToScan, true);
  }
  
  public AnnotationInMemoryDs(String packageToScan, boolean persistInMemory) {
    this.persistInMemory = persistInMemory;
    List<Class<?>> foundClasses = ClassHelper.loadClasses(packageToScan, new ClassHelper.ClassValidator() {
      @Override
      public boolean isClassValid(Class<?> c) {
        return null != c.getAnnotation(org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet.class);
      }
    });

    init(foundClasses);
  }

  @SuppressWarnings("unchecked")
  private void init(List<Class<?>> foundClasses) {
    for (Class<?> clz : foundClasses) {

      DataStore<Object> dhs = (DataStore<Object>) getDataStore(clz);
      org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet entitySet =
          clz.getAnnotation(org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet.class);
      dataStores.put(entitySet.name(), dhs);
    }
  }

  public <T> DataStore<T> getDataStore(Class<T> clazz) {
    return DataStore.createInMemory(clazz, persistInMemory);
  }

  @Override
  public List<?> readData(EdmEntitySet entitySet) throws ODataNotImplementedException,
      ODataNotFoundException, EdmException, ODataApplicationException {

    DataStore<Object> holder = getDataStore(entitySet);
    if (holder != null) {
      return new ArrayList<Object>(holder.read());
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

    DataStore<?> sourceStore = dataStores.get(sourceEntitySet.getName());
    DataStore<?> targetStore = dataStores.get(targetEntitySet.getName());

    AnnotatedNavInfo navInfo = ANNOTATION_HELPER.getCommonNavigationInfo(
        sourceStore.getDataTypeClass(), targetStore.getDataTypeClass());
    Field sourceField = navInfo.getFromField();
    if (sourceField == null) {
      throw new ODataRuntimeException("Missing source field for related data (sourceStore='" + sourceStore
              + "', targetStore='" + targetStore + "').");
    }

    Object navigationInstance = getValue(sourceField, sourceData);
    List<Object> resultData = new ArrayList<Object>();
    for (Object targetInstance : targetStore.read()) {
      if (navigationInstance instanceof Collection) {
        for (Object object : (Collection<?>) navigationInstance) {
          if (ANNOTATION_HELPER.keyMatch(targetInstance, object)) {
            resultData.add(targetInstance);
          }
        }
      } else if (ANNOTATION_HELPER.keyMatch(targetInstance, navigationInstance)) {
        resultData.add(targetInstance);
      }
    }

    if (navInfo.getToMultiplicity() == EdmMultiplicity.MANY) {
      if (targetKeys.isEmpty()) {
        return resultData;
      } else {
        for (Object result : resultData) {
          if (ANNOTATION_HELPER.keyMatch(result, targetKeys)) {
            return result;
          }
        }
        return null;
      }
    } else {
      if (resultData.isEmpty()) {
        return null;
      }
      return resultData.get(0);
    }
  }

  @Override
  public BinaryData readBinaryData(EdmEntitySet entitySet, Object mediaLinkEntryData)
      throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {

    Object data = ANNOTATION_HELPER.getValueForField(mediaLinkEntryData, EdmMediaResourceContent.class);
    Object mimeType = ANNOTATION_HELPER.getValueForField(mediaLinkEntryData, EdmMediaResourceMimeType.class);

    BinaryData db = new BinaryData((byte[]) data, String.valueOf(mimeType));
    return db;
  }

  @Override
  public Object newDataObject(EdmEntitySet entitySet)
      throws ODataNotImplementedException, EdmException, ODataApplicationException {

    DataStore<Object> dataStore = getDataStore(entitySet);
    if (dataStore != null) {
      return dataStore.createInstance();
    }

    throw new ODataRuntimeException("No DataStore found for entitySet with name: " + entitySet.getName());
  }

  @Override
  public void writeBinaryData(EdmEntitySet entitySet, Object mediaEntityInstance, BinaryData binaryData)
      throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {

    try {
      ANNOTATION_HELPER.setValueForAnnotatedField(
          mediaEntityInstance, EdmMediaResourceContent.class, binaryData.getData());
      ANNOTATION_HELPER.setValueForAnnotatedField(
          mediaEntityInstance, EdmMediaResourceMimeType.class, binaryData.getMimeType());
    } catch (ODataAnnotationException e) {
      throw new ODataRuntimeException("Invalid media resource annotation at entity set '" + entitySet.getName() 
          + "' with message '" + e.getMessage() + "'.", e);
    }
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
  public void writeRelation(EdmEntitySet sourceEntitySet, Object sourceEntity, EdmEntitySet targetEntitySet,
      Map<String, Object> targetEntityValues)
      throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    // get common data
    DataStore<Object> sourceStore = dataStores.get(sourceEntitySet.getName());
    DataStore<Object> targetStore = dataStores.get(targetEntitySet.getName());

    AnnotatedNavInfo commonNavInfo = ANNOTATION_HELPER.getCommonNavigationInfo(
        sourceStore.getDataTypeClass(), targetStore.getDataTypeClass());
    
    // get and validate source fields
    Field sourceField = commonNavInfo.getFromField();
    if (sourceField == null) {
      throw new ODataRuntimeException("Missing source field for related data (sourceStore='" + sourceStore
              + "', targetStore='" + targetStore + "').");
    }
    
    // get related target entity
    Object targetEntity = targetStore.createInstance();
    ANNOTATION_HELPER.setKeyFields(targetEntity, targetEntityValues);
    targetEntity = targetStore.read(targetEntity);
    
    // set at source
    setValueAtNavigationField(sourceEntity, sourceField, targetEntity);
    // set at target
    Field targetField = commonNavInfo.getToField();
    if(targetField != null) {
      setValueAtNavigationField(targetEntity, targetField, sourceEntity);
    }
  }

  /**
   * Set (Multiplicity != *) or add (Multiplicity == *) <code>value</code> at <code>field</code>
   * of <code>instance</code>. 
   * 
   * @param instance
   * @param field
   * @param value
   * @throws EdmException
   */
  private void setValueAtNavigationField(Object instance, Field field, Object value) 
      throws EdmException {
    Class<?> fieldTypeClass = field.getType();
    if (Collection.class.isAssignableFrom(fieldTypeClass)) {
      @SuppressWarnings("unchecked")
      Collection<Object> collection = (Collection<Object>) ANNOTATION_HELPER.getValueForField(
          instance, field.getName(), EdmNavigationProperty.class);
      if(collection == null) {
        collection = new ArrayList<Object>();
        setValue(instance, field, collection);
      }
      collection.add(value);
    } else if(fieldTypeClass.isArray()) {
      throw new ODataRuntimeException("Write relations for internal used arrays is not supported.");
    } else {
      setValue(instance, field, value);
    }
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
    final String name = entitySet.getName();
    DataStore<Object> dataStore = dataStores.get(name);
    if (dataStore == null) {
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

  private void setValue(Object instance, Field field, Object value) {
    try {
      boolean access = field.isAccessible();
      field.setAccessible(true);
      field.set(instance, value);
      field.setAccessible(access);
    } catch (IllegalArgumentException e) {
      throw new ODataRuntimeException("Error for setting value of field: '"
          + field + "' at instance: '" + instance + "'.", e);
    } catch (IllegalAccessException e) {
      throw new ODataRuntimeException("Error for setting value of field: '"
          + field + "' at instance: '" + instance + "'.", e);
    }
  }
}
