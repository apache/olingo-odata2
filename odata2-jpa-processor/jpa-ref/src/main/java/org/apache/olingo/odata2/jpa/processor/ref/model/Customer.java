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
package org.apache.olingo.odata2.jpa.processor.ref.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "T_CUSTOMER")
@EntityListeners(org.apache.olingo.odata2.jpa.processor.ref.listeners.CustomerQueryExtension.class)
public class Customer extends CustomerBase {

  @Id
  @Column(name = "ID", nullable = false, length = 20)
  private Long id;

  @ManyToOne(optional = true)
  @JoinColumn(name = "PARENT_ID", referencedColumnName = "ID" , nullable = true)
  private Customer parent;

  @Column(name = "NAME")
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "IMPORTANCE")
  private Importance importance;

  @Embedded
  private Address address;

  @Column(name = "CREATED_AT")
  private Timestamp createdAt;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
  private List<SalesOrderHeader> orders = new ArrayList<SalesOrderHeader>();

  public List<SalesOrderHeader> getOrders() {
    return orders;
  }

  public void setOrders(List<SalesOrderHeader> orders) {
    this.orders = orders;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public Customer getParent() {
    return parent;
  }

  public void setParent(Customer parent) {
    this.parent = parent;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public Importance getImportance() {
    return importance;
  }

  public void setImportance(Importance importance) {
    this.importance = importance;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(final Address address) {
    this.address = address;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final Timestamp createdAt) {
    this.createdAt = createdAt;
  }

}
