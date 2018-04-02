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
package org.apache.olingo.odata2.client.api.edm;

import java.util.List;

import org.apache.olingo.odata2.api.edm.Edm;

/**
 * @org.apache.olingo.odata2.DoNotImplement
 * Entity Data Model (EDM)
 * <p>Interface representing a Entity Data Model as described in the Conceptual Schema Definition.
 * 
 */
public interface ClientEdm extends Edm{

  /**
   * This method <b>DOES NOT</b> support lazy loading. All schemas are loaded completely!
   *
   * @return all schemas defined for this EDM
   */
  List<EdmSchema> getSchemas();

  
}
