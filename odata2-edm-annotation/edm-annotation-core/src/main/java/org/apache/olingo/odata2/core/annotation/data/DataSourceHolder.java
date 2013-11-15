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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityCreate;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityDataSource;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityDelete;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityRead;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntitySetRead;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityUpdate;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.core.annotation.edm.AnnotationHelper;

/**
 *
 */
public final class DataSourceHolder {

  private static final AnnotationHelper ANNOTATION_HELPER = new AnnotationHelper();
  private static final Object[] EMPTY_ARRAY = new Object[0];
  
  private final String name;
  private final Object dataSourceInstance;
  private final Class<?> entityTypeClass;
  private Method readMethod;
  private Method createMethod;
  private Method updateMethod;
  private Method deleteMethod;
  private Method readSetMethod;

  public DataSourceHolder(Class<?> clz) {
    EntityDataSource eds = clz.getAnnotation(EntityDataSource.class);
    entityTypeClass = eds.entityType();
    EdmEntityType entityType = entityTypeClass.getAnnotation(EdmEntityType.class);
    if (entityType == null) {
      throw new IllegalArgumentException("Missing EdmEntityType Annotation at class " + clz);
    }

    if (entityType.name().isEmpty()) {
      name = ANNOTATION_HELPER.getCanonicalName(entityTypeClass);
    } else {
      name = entityType.name();
    }
    dataSourceInstance = createInstance(clz);
    initMethods(clz);
  }

  private void initMethods(Class<?> clz) throws IllegalArgumentException, SecurityException {
    Method[] methods = clz.getDeclaredMethods();
    for (Method method : methods) {
      EntityRead ec = method.getAnnotation(EntityRead.class);
      if (ec != null) {
        readMethod = method;
      }
      EntityCreate ep = method.getAnnotation(EntityCreate.class);
      if (ep != null) {
        createMethod = method;
      }
      EntityUpdate update = method.getAnnotation(EntityUpdate.class);
      if (update != null) {
        updateMethod = method;
      }
      EntityDelete delete = method.getAnnotation(EntityDelete.class);
      if (delete != null) {
        deleteMethod = method;
      }
      EntitySetRead readSet = method.getAnnotation(EntitySetRead.class);
      if (readSet != null) {
        readSetMethod = method;
      }
    }

    validateMethods(clz);
  }

  private void validateMethods(Class<?> clz) throws IllegalArgumentException {
    //
    if (readMethod == null) {
      throw new IllegalArgumentException("Missing " + EntityRead.class
              + " annotation at " + EntityDataSource.class + " annotated class " + clz);
    }
    if (updateMethod == null) {
      throw new IllegalArgumentException("Missing " + EntityUpdate.class
              + " annotation at " + EntityDataSource.class + " annotated class " + clz);
    }
    if (createMethod == null) {
      throw new IllegalArgumentException("Missing " + EntityCreate.class
              + " annotation at " + EntityDataSource.class + " annotated class " + clz);
    }
    
    if(readSetMethod != null) {
      if(!Collection.class.isAssignableFrom(readSetMethod.getReturnType())) {
        throw new IllegalArgumentException("Read set method must have a return type which is assignable to " 
            + Collection.class + " but return type for annotated method " + readSetMethod + " is " 
            + readSetMethod.getReturnType());
      }
    }
  }

  public Object readEntity(Object[] keyValues) {
    if (readMethod.getParameterTypes().length != keyValues.length) {
      throw new IllegalStateException("Wrong amount of key properties. Expected read keys = "
              + Arrays.toString(readMethod.getParameterTypes()) + " given key predicates = " 
              + Arrays.toString(keyValues));
    }

    return invoke(readMethod, keyValues);
  }

  public Object readEntity(List<KeyPredicate> keys) {
    Object[] parameterKeys = mapParameterKeys(readMethod, keys);
    return invoke(readMethod, parameterKeys);
  }

  private Object[] mapParameterKeys(Method method, List<KeyPredicate> keys) throws IllegalStateException {
    if(method == null) {
      return EMPTY_ARRAY;
    }
    Class<?>[] pTypes = method.getParameterTypes();
    if (pTypes.length != keys.size()) {
      throw new IllegalStateException("Wrong amount of key properties. Expected read keys = "
              + Arrays.toString(pTypes) + " given key predicates = " + keys);
    }
    Object[] parameterKeys = new Object[pTypes.length];
    int i = 0;
    for (KeyPredicate keyPredicate : keys) {
      if (matches(pTypes[i], keyPredicate)) {
        parameterKeys[i] = keyPredicate.getLiteral();
      }
      i++;
    }
    return parameterKeys;
  }

  public Object createEntity(Object key) {
    return invoke(createMethod, new Object[]{key});
  }

  public Object updateEntity(Object key) {
    return invoke(updateMethod, new Object[]{key});
  }

  public Object deleteEntity(List<KeyPredicate> keys) {
    Object[] parameterKeys = mapParameterKeys(deleteMethod, keys);
    return invoke(deleteMethod, parameterKeys);
  }

  public Collection<?> readEntitySet() {
    return (Collection<?>) invoke(readSetMethod, new Object[0]);
  }

  private Object invoke(Method m, Object[] objs) {
    try {
      return m.invoke(dataSourceInstance, objs);
    } catch (Exception ex) {
      return null;
    }
  }

  public Object createEntityInstance() {
    return createInstance(this.entityTypeClass);
  }

  private static Object createInstance(Class<?> clz) {
    try {
      return clz.newInstance();
    } catch (Exception ex) {
      return null;
    }
  }

  public String getEntityName() {
    return this.name;
  }

  public Class<?> getEntityTypeClass() {
    return entityTypeClass;
  }

  @Override
  public String toString() {
    return "DataSourceHolder{" + "name=" + name + ", dataSourceInstance=" + dataSourceInstance + 
            ", entityTypeClass=" + entityTypeClass + ", consumerMethod=" + readMethod + 
            ", createMethod=" + createMethod + ", updateMethod=" + updateMethod + '}';
  }

  private boolean matches(Class<?> aClass, KeyPredicate type) {
    return true;
  }
}
