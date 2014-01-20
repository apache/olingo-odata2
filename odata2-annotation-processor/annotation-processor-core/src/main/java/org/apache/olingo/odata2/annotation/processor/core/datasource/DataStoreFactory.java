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

import javax.persistence.Entity;

/**
 *
 */
public class DataStoreFactory {
 
  public DataStore<?> createInstance(Class<?> clz, boolean keepPersistent) throws DataStoreException {
    if(isJpaAnnotated(clz)) {
      return JpaAnnotationDataStore.createInstance(clz);
    }
    return InMemoryDataStore.createInMemory(clz, keepPersistent);
  }

  private boolean isJpaAnnotated(Class<?> clz) {
    return clz.getAnnotation(Entity.class) != null;
  }
}
