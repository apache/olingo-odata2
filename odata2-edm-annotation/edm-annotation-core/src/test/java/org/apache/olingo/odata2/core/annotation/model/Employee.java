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

import java.text.DateFormat;
import java.util.Calendar;
import org.apache.olingo.odata2.api.annotation.edm.EdmComplexProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.NavigationEnd;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;

/**
 *  
 */
@EdmEntityType(name="Employee", namespace=ModelSharedConstants.NAMESPACE_1)
@EdmEntitySet(name="Employees", container=ModelSharedConstants.CONTAINER_1)
public class Employee {
  private static int counter = 1;
  @EdmProperty(name="EmployeeId", type = EdmSimpleTypeKind.Int32)
  @EdmKey
  private int employeeId;
  @EdmProperty(name="EmployeeName")
  private String employeeName;
  @EdmProperty
  private int age;
  @EdmNavigationProperty(name="ne_Manager", relationship="ManagerEmployees",
          from = @NavigationEnd(role="r_Employees", multiplicity = EdmMultiplicity.MANY))//,
//          to = @NavigationEnd(type = "Manager"))
  private Manager manager;
  private Team team;
  private Room room;
  private String imageType;
  private byte[] image;
  private String imageUrl;
  private Calendar entryDate;
  @EdmComplexProperty(name="Location")
  private Location location;

  public Employee(final int employeeId, final String name) {
    this.employeeId = employeeId;
    setEmployeeName(name);
  }

  public String getId() {
    return Integer.toString(employeeId);
  }

  public void setEmployeeName(final String employeeName) {
    this.employeeName = employeeName;
  }

  public String getEmployeeName() {
    return employeeName;
  }

  public void setAge(final int age) {
    this.age = age;
  }

  public int getAge() {
    return age;
  }

  public void setManager(final Manager manager) {
    this.manager = manager;
  }

  public Manager getManager() {
    return manager;
  }

  public void setTeam(final Team team) {
    this.team = team;
  }

  public Team getTeam() {
    return team;
  }

  public void setRoom(final Room room) {
    this.room = room;
  }

  public Room getRoom() {
    return room;
  }

  public void setImageUri(final String imageUri) {
    imageUrl = imageUri;
  }

  public String getImageUri() {
    return imageUrl;
  }

  public void setLocation(final Location location) {
    this.location = location;
  }

  public Location getLocation() {
    return location;
  }

  public void setEntryDate(final Calendar date) {
    entryDate = date;
  }

  public Calendar getEntryDate() {
    return entryDate;
  }

  public void setImageType(final String imageType) {
    this.imageType = imageType;
  }

  public String getImageType() {
    return imageType;
  }

  public void setImage(final byte[] image) {
    this.image = image;
  }

  public byte[] getImage() {
    if (image == null) {
      return null;
    }
    return image.clone();
  }

  @Override
  public int hashCode() {
    return employeeId;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj
        || obj != null && getClass() == obj.getClass() && employeeId == ((Employee) obj).employeeId;
  }

  @Override
  public String toString() {
    return "{\"EmployeeId\":\"" + employeeId + "\","
        + "\"EmployeeName\":\"" + employeeName + "\","
        + "\"ManagerId\":" + (manager == null ? "null" : "\"" + manager.getId() + "\"") + ","
        + "\"RoomId\":" + (room == null ? "null" : "\"" + room.getId() + "\"") + ","
        + "\"TeamId\":" + (team == null ? "null" : "\"" + team.getId() + "\"") + ","
        + "\"Location\":"
        + (location == null ? "null" :
            "{\"City\":" + (location.getCity() == null ? "null" :
                "{\"PostalCode\":\"" + location.getCity().getPostalCode() + "\","
                    + "\"CityName\":\"" + location.getCity().getCityName() + "\"}") + ","
                + "\"Country\":\"" + location.getCountry() + "\"}") + ","
        + "\"Age\":" + age + ","
        + "\"EntryDate\":"
        + (entryDate == null ? "null" : "\"" + DateFormat.getInstance().format(entryDate.getTime()) + "\"") + ","
        + "\"ImageUrl\":\"" + imageUrl + "\"}";
  }
}
