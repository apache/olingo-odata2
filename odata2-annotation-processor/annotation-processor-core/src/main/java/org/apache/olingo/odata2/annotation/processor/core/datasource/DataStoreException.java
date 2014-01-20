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

import java.util.Locale;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;

/**
 *
 */
public class DataStoreException extends ODataApplicationException {

  private static final long serialVersionUID = 42L;

  public DataStoreException(final String message) {
    this(message, null);
  }

  public DataStoreException(final String message, final Throwable cause) {
    super(message, Locale.ENGLISH, cause);
  }
}
