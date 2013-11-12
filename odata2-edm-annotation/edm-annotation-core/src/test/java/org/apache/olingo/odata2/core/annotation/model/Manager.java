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
package org.apache.olingo.odata2.core.annotation.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.NavigationEnd;

/**
 *
 */
@EdmEntityType(name = "Manager", namespace = ModelSharedConstants.NAMESPACE_1, 
        entitySetName = "Managers", container = ModelSharedConstants.CONTAINER_1)
public class Manager extends Employee {

  @EdmNavigationProperty(name = "nm_Employees", relationship = "ManagerEmployees",
          to = @NavigationEnd(role = "r_Employees", type = "Employee"))
  private List<Employee> employees = new ArrayList<Employee>();

  public Manager(final int id, final String name) {
    super(id, name);
  }

  public List<Employee> getEmployees() {
    return employees;
  }
}
