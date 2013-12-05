/*
 * Copyright 2013 The Apache Software Foundation.
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

package org.apache.olingo.odata2.core.annotation.model;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmType;

/**
 *
 */
@EdmEntityType(name="Base", namespace=ModelSharedConstants.NAMESPACE_1)
public abstract class RefBase {
  @EdmProperty(name="Name")
  protected String name;
  @EdmProperty(name="Id", type = EdmType.STRING)
  @EdmKey
  protected int id;

  public RefBase(int id, String name) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return Integer.toString(id);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setId(int id) {
    this.id = id;
  }
}
