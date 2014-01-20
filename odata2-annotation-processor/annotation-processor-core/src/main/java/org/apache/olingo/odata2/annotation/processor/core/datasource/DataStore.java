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

/**
 *
 * @author michael
 */
public interface DataStore<T> {

  T create(final T object) throws DataStoreException;

  T createInstance();

  T delete(final T object);

  Class<T> getDataTypeClass();

  String getEntityTypeName();

  /**
   * Are the key values equal for both instances.
   * If all compared key values are <code>null</code> this also means equal.
   *
   * @param first first instance to check for key equal
   * @param second second instance to check for key equal
   * @return <code>true</code> if object instance have equal keys set.
   */
  boolean isKeyEqual(final T first, final T second);

  /**
   * Are the key values equal for both instances.
   * If all compared key values are <code>null</code> this also means equal.
   * Before object (keys) are compared it is validated that both object instance are NOT null
   * and that both are from the same class as this {@link DataStore} (see {@link #dataTypeClass}).
   * For the equal check on {@link #dataTypeClass} instances without validation see
   * {@link #isKeyEqual(Object, Object)}.
   *
   * @param first first instance to check for key equal
   * @param second second instance to check for key equal
   * @return <code>true</code> if object instance have equal keys set.
   */
  boolean isKeyEqualChecked(Object first, Object second) throws DataStoreException;

  T read(final T obj);

  Collection<T> read();

  T update(final T object);
}
