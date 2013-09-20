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
package org.apache.olingo.odata2.core.uri;

import java.util.Collections;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationSegment;

/**
 *  
 */
public class NavigationSegmentImpl implements NavigationSegment {

  private EdmNavigationProperty navigationProperty;
  private EdmEntitySet targetEntitySet;
  private List<KeyPredicate> keyPredicates = Collections.emptyList();

  @Override
  public List<KeyPredicate> getKeyPredicates() {
    return keyPredicates;
  }

  public void setKeyPredicates(final List<KeyPredicate> keyPredicates) {
    this.keyPredicates = keyPredicates;
  }

  @Override
  public EdmNavigationProperty getNavigationProperty() {
    return navigationProperty;
  }

  public void setNavigationProperty(final EdmNavigationProperty edmNavigationProperty) {
    navigationProperty = edmNavigationProperty;
  }

  @Override
  public EdmEntitySet getEntitySet() {
    return targetEntitySet;
  }

  public void setEntitySet(final EdmEntitySet edmEntitySet) {
    targetEntitySet = edmEntitySet;
  }

  @Override
  public String toString() {
    return "Navigation Property: " + navigationProperty + ", "
        + "Target Entity Set: " + targetEntitySet + ", "
        + "Key Predicates: " + keyPredicates;
  }

}
