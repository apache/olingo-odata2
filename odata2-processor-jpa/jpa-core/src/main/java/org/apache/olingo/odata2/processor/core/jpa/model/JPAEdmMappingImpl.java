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
package org.apache.olingo.odata2.processor.core.jpa.model;

import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.processor.api.jpa.model.JPAEdmMapping;

public class JPAEdmMappingImpl extends Mapping implements JPAEdmMapping {

  private String columnName = null;
  private Class<?> type = null;

  @Override
  public void setJPAColumnName(final String name) {
    columnName = name;

  }

  @Override
  public String getJPAColumnName() {
    return columnName;
  }

  @Override
  public void setJPAType(final Class<?> type) {
    this.type = type;

  }

  @Override
  public Class<?> getJPAType() {
    return type;
  }

}
