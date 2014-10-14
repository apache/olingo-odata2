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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "T_STORE")
public class Store {

  public Store() {}

  public Store(final String storeName, final Address storeAddress) {
    super();
    this.storeName = storeName;
    this.storeAddress = storeAddress;
  }

  @Id
  @Column(name = "STORE_ID")
  private long storeId;

  @Column(name = "STORE_NAME", unique = true)
  private String storeName;

  @Embedded
  private Address storeAddress;

  @ManyToMany(mappedBy = "stores")
  private List<Material> materials = new ArrayList<Material>();

  public long getStoreId() {
    return storeId;
  }

  public void setStoreId(final long storeId) {
    this.storeId = storeId;
  }

  public String getStoreName() {
    return storeName;
  }

  public void setStoreName(final String storeName) {
    this.storeName = storeName;
  }

  public Address getStoreAddress() {
    return storeAddress;
  }

  public void setStoreAddress(final Address storeAddress) {
    this.storeAddress = storeAddress;
  }

  public List<Material> getMaterials() {
    return materials;
  }

  public void setMaterials(final List<Material> materials) {
    this.materials = materials;
    Iterator<Material> itr = materials.iterator();
    while (itr.hasNext()) {
      itr.next().getStores().add(this);
    }
  }
}