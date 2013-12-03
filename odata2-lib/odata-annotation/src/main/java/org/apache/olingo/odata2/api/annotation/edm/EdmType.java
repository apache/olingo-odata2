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
package org.apache.olingo.odata2.api.annotation.edm;

/**
 * <p>The EdmTypes which can be used for property definition in the EDM.</p>
 * The available values are based on <code>EdmSimpleTypeKind</code> values with the additional type  
 * <code>COMPLEX</code> which can be used to explicit define a EdmProperty as complex.
 */
public enum EdmType {
  BINARY, BOOLEAN, BYTE, DATE_TIME, DATE_TIME_OFFSET, DECIMAL, DOUBLE, 
  GUID, INT16, INT32, INT64, SBYTE, SINGLE, STRING, TIME, NULL, 
  /** Only for explicit definition of a complex property. Not mappable to <code>EdmSimpleTypeKind</code> */
  COMPLEX;
}
