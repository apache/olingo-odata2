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

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "T_SALESORDERITEM")
public class SalesOrderItem {

	public SalesOrderItem() {
	}

	public SalesOrderItem(final int quantity, final double amount,
			final double discount, final Material material) {
		super();
		this.quantity = quantity;
		this.amount = amount;
		this.discount = discount;
		this.material = material;
	}

	@EmbeddedId
	private SalesOrderItemKey salesOrderItemKey;

	@Column(name = "Material_Id", nullable = false)
	private long matId;

	@Column
	private int quantity;

	@Column
	private double amount;

	@Column
	private double discount;

	@Transient
	private double netAmount;
	
	@Column
	private boolean delivered;

	public Boolean isDelivered() {
		return delivered;
	}

	public void setDelivered(final Boolean deliveryStatus) {
		this.delivered = deliveryStatus;
	}

	@JoinColumn(name = "Material_Id", referencedColumnName = "MATERIAL_ID", insertable = false, updatable = false)
	@ManyToOne
	private Material material;

	@JoinColumn(name = "Sales_Order_Id", referencedColumnName = "SO_ID", insertable = false, updatable = false)
	@ManyToOne
	private SalesOrderHeader salesOrderHeader;

	public SalesOrderItemKey getSalesOrderItemKey() {
		return salesOrderItemKey;
	}

	public void setSalesOrderItemKey(final SalesOrderItemKey salesOrderItemKey) {
		this.salesOrderItemKey = salesOrderItemKey;
	}

	public long getMatId() {
		return matId;
	}

	public void setMatId(final long matId) {
		this.matId = matId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(final int quantity) {
		this.quantity = quantity;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(final double amount) {
		this.amount = amount;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(final double discount) {
		this.discount = discount;
	}

	public double getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(final double netAmount) {
		this.netAmount = netAmount;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(final Material material) {
		this.material = material;
	}

	public SalesOrderHeader getSalesOrderHeader() {
		return salesOrderHeader;
	}

	public void setSalesOrderHeader(final SalesOrderHeader salesOrderHeader) {
		this.salesOrderHeader = salesOrderHeader;
	}
}
