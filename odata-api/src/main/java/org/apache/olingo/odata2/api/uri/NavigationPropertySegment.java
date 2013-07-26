/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.api.uri;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;

/**
 * Navigation property segment, consisting of a navigation property and its
 * target entity set.
 * @org.apache.olingo.odata2.DoNotImplement
 * @author
 */
public interface NavigationPropertySegment {

  /**
   * Gets the navigation property.
   * @return {@link EdmNavigationProperty} navigation property
   */
  public EdmNavigationProperty getNavigationProperty();

  /**
   * Gets the target entity set.
   * @return {@link EdmEntitySet} the target entity set
   */
  public EdmEntitySet getTargetEntitySet();

}
