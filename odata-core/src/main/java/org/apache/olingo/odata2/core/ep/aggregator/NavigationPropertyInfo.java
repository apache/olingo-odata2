/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.ep.aggregator;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;

public final class NavigationPropertyInfo {
  private String name;
  private EdmMultiplicity multiplicity;

  static NavigationPropertyInfo create(final EdmNavigationProperty property) throws EdmException {
    NavigationPropertyInfo info = new NavigationPropertyInfo();
    info.name = property.getName();
    info.multiplicity = property.getMultiplicity();
    return info;
  }

  @Override
  public String toString() {
    return name + "; multiplicity=" + multiplicity;
  }

  public EdmMultiplicity getMultiplicity() {
    return multiplicity;
  }

  public String getName() {
    return name;
  }
}
