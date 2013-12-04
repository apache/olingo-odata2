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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "T_SALESORDERHEADER")
public class SalesOrderHeader {

  public SalesOrderHeader() {}

  public SalesOrderHeader(final Calendar creationDate, final int buyerId, final String buyerName,
      final Address buyerAddress, final String currencyCode, final double netAmount,
      final String deliveryStatus, final char[] shortText, final Character[] longText) {
    super();
    this.creationDate = creationDate;
    this.buyerId = buyerId;
    this.buyerName = buyerName;
    this.buyerAddress = buyerAddress;
    this.currencyCode = currencyCode;
    this.deliveryStatus = deliveryStatus;
    this.shortText = shortText;
    this.longText = longText;
  }

  @Id
  @Column(name = "SO_ID")
  private long soId;

  @Temporal(TemporalType.TIMESTAMP)
  private Calendar creationDate;

  @Column
  private Character status;

  public Character getStatus() {
    return status;
  }

  public void setStatus(final Character status) {
    this.status = status;
  }

  @Column(name = "SHORT_TEXT", length = 20)
  private char[] shortText;

  @Column(name = "LONG_TEXT", length = 40)
  private Character[] longText;

  @Column(name = "BUYER_ID")
  private int buyerId;

  @Column(name = "BUYER_NAME", length = 255)
  private String buyerName;

  @Embedded
  private Address buyerAddress;

  @Column(name = "CURRENCY_CODE", length = 3)
  private String currencyCode;

  @Column(name = "DELIVERY_STATUS", length = 2)
  private String deliveryStatus;

  @Column(precision = 5)
  private double grossAmount;

  @Column(precision = 8)
  private double netAmount;

  @OneToMany(mappedBy = "salesOrderHeader", cascade = CascadeType.ALL)
  private Set<SalesOrderItem> salesOrderItem = new HashSet<SalesOrderItem>();

  @OneToMany(mappedBy = "salesOrderHeader", cascade = CascadeType.ALL)
  private List<Note> notes = new ArrayList<Note>();

  public long getSoId() {
    return soId;
  }

  public void setSoId(final long soId) {
    this.soId = soId;
  }

  public Date getCreationDate() {
    if (creationDate == null) {
      return null;
    }
    long dbTime = creationDate.getTime().getTime();
    Date originalDate = new Date(dbTime + TimeZone.getDefault().getOffset(dbTime));
    return originalDate;
  }

  public void setCreationDate(final Calendar creationDate) {
    long originalTime;
    if (creationDate != null) {
      originalTime = creationDate.getTime().getTime();
    } else {
      originalTime = Calendar.getInstance(TimeZone.getDefault()).getTime().getTime();
    }
    Date newDate = new Date(originalTime - TimeZone.getDefault().getOffset(originalTime));
    Calendar newCalendar = Calendar.getInstance();
    newCalendar.setTime(newDate);
    this.creationDate = newCalendar;
  }

  public int getBuyerId() {
    return buyerId;
  }

  public void setBuyerId(final int buyerId) {
    this.buyerId = buyerId;
  }

  public String getBuyerName() {
    return buyerName;
  }

  public void setBuyerName(final String buyerName) {
    this.buyerName = buyerName;
  }

  public Address getBuyerAddress() {
    return buyerAddress;
  }

  public void setBuyerAddress(final Address buyerAddress) {
    this.buyerAddress = buyerAddress;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(final String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public String getDeliveryStatus() {
    return deliveryStatus;
  }

  public void setDeliveryStatus(final String deliveryStatus) {
    this.deliveryStatus = deliveryStatus;
  }

  public double getGrossAmount() {
    return grossAmount;
  }

  public void setGrossAmount(final double grossAmount) {
    this.grossAmount = grossAmount;
  }

  public double getNetAmount() {
    return netAmount;
  }

  public void setNetAmount(final double netAmount) {
    this.netAmount = netAmount;
  }

  public Set<SalesOrderItem> getSalesOrderItem() {
    return salesOrderItem;
  }

  public void setSalesOrderItem(final Set<SalesOrderItem> salesOrderItem) {
    this.salesOrderItem = salesOrderItem;
  }

  public List<Note> getNotes() {
    return notes;
  }

  public void setNotes(final List<Note> notes) {
    this.notes = notes;
  }

  public char[] getShortText() {
    return shortText;
  }

  public void setShortText(final char[] shortText) {
    this.shortText = shortText;
  }

  public Character[] getLongText() {
    return longText;
  }

  public void setLongText(final Character[] longText) {
    this.longText = longText;
  }

  @PostPersist
  public void defaultValues() {
    if (creationDate == null) {
      setCreationDate(creationDate);
    }
  }

}
