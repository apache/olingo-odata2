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

import java.util.HashMap;
import java.util.Map;
import javax.persistence.Entity;

/**
 *
 */
public class DualDataStoreFactory implements DataStoreFactory {

  private final Map<String, String> properties = new HashMap<String, String>();
  
  @Override
  public DataStore<?> createDataStore(Class<?> clz) throws DataStoreException {
    return createDataStore(clz, properties);
  }

  @Override
  public DataStore<?> createDataStore(Class<?> clz, Map<String, String> properties) throws DataStoreException {
    boolean keepPersistent = Boolean.parseBoolean(properties.get(KEEP_PERSISTENT));
    return createInstance(clz, keepPersistent);
  }

  @Override
  public void setDefaultProperty(String name, String value) {
    properties.put(name, value);
  }
  
  public DataStore<?> createInstance(Class<?> clz, boolean keepPersistent) throws DataStoreException {
    if(isJpaAnnotated(clz)) {
      String persistenceName = System.getProperty(JpaAnnotationDataStore.PERSISTENCE_NAME);
      if(persistenceName == null) {
        return JpaAnnotationDataStore.createInstance(clz);
      }
      return JpaAnnotationDataStore.createInstance(clz, persistenceName);
    }
    return InMemoryDataStore.createInMemory(clz, keepPersistent);
  }

  private boolean isJpaAnnotated(Class<?> clz) {
    return clz.getAnnotation(Entity.class) != null;
  }
}
