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

import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty.Multiplicity;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmType;

/**
*  
*/
@EdmEntityType(name = "Team", namespace = ModelSharedConstants.NAMESPACE_1)
@EdmEntitySet(name = "Teams")
public class Team extends RefBase {
  @EdmProperty(type = EdmType.BOOLEAN)
  private Boolean isScrumTeam;
  @EdmNavigationProperty(name = "nt_Employees", association = "TeamEmployees", toMultiplicity = Multiplicity.MANY)
  private List<Employee> employees = new ArrayList<Employee>();

  public Team() {
    super(-1, null);
  }

  public Team(final int id, final String name) {
    super(id, name);
  }

  public Boolean isScrumTeam() {
    return isScrumTeam;
  }

  public void setScrumTeam(final Boolean isScrumTeam) {
    this.isScrumTeam = isScrumTeam;
  }

  public void addEmployee(final Employee e) {
    employees.add(e);
  }

  public List<Employee> getEmployees() {
    return employees;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj
        || obj != null && getClass() == obj.getClass() && id == ((Team) obj).id;
  }

  @Override
  public String toString() {
    return "{\"Id\":\"" + id + "\",\"Name\":\"" + name + "\",\"IsScrumTeam\":" + isScrumTeam + "}";
  }
}
