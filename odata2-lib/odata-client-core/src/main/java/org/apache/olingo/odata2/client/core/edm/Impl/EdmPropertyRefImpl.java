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
package org.apache.olingo.odata2.client.core.edm.Impl;


/**
 * Objects of this class represent a reference to a property via its name
 * 
 * 
 */
public class EdmPropertyRefImpl extends EdmPropertyImpl {

  protected boolean isSimple;

  /**
   * @return <b>String</b> name of the {@link Property} this {@link EdmPropertyRefImpl} is referencing to
   */

  public void setIsSimple(boolean isSimple) {
    this.isSimple = isSimple;
  }

  @Override
  public boolean isSimple() {
    return isSimple;
  }
  @Override
  public String toString() {
      return String.format(name);
  }
}
