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
package org.apache.olingo.odata2.api.edm;

/**
 * @org.apache.olingo.odata2.DoNotImplement
 * A CSDL AssociationSetEnd element
 * 
 * <p>EdmAssociationSetEnd defines one side of the relationship of two entity sets.
 * @author
 */
public interface EdmAssociationSetEnd {

  /**
   * Get the role name
   * 
   * @return String
   */
  String getRole();

  /**
   * Get the entity set
   * 
   * @return {@link EdmEntitySet}
   * @throws EdmException
   */
  EdmEntitySet getEntitySet() throws EdmException;
}
