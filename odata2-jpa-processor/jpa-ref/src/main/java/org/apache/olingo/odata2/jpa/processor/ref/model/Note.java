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

import java.sql.Clob;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@IdClass(value = NoteKey.class)
@Table(name = "T_NOTE")
public class Note {

  public Note() {}

  public Note(final Calendar creationTime, final Calendar creationDate, final String createdBy) {
    super();
    this.creationTime = creationTime;
    this.creationDate = creationDate;
    this.createdBy = createdBy;

  }

  @Id
  @Temporal(TemporalType.TIME)
  private Calendar creationTime;

  @Id
  @Temporal(TemporalType.DATE)
  private Calendar creationDate;

  @Id
  private String createdBy;

  @Column
  @Lob
  @Convert(converter = org.apache.olingo.odata2.jpa.processor.ref.converter.ClobToStringConverter.class)
  private Clob text;

  @Column(name = "SO_ID")
  private long soId;

  @JoinColumn(name = "SO_ID", referencedColumnName = "SO_ID", insertable = false, updatable = false)
  @ManyToOne
  private SalesOrderHeader salesOrderHeader;

  public Calendar getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(final Calendar creationTime) {
    this.creationTime = creationTime;
  }

  public Calendar getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(final Calendar creationDate) {
    this.creationDate = creationDate;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
  }

  public Clob getText() {
    return text;
  }

  public void setText(final Clob text) {
    this.text = text;
  }

  public long getSoId() {
    return soId;
  }

  public void setSoId(final long soId) {
    this.soId = soId;
  }

  public SalesOrderHeader getSalesOrderHeader() {
    return salesOrderHeader;
  }

  public void setSalesOrderHeader(final SalesOrderHeader salesOrderHeader) {
    this.salesOrderHeader = salesOrderHeader;
    this.salesOrderHeader.getNotes().add(this);
  }
}
