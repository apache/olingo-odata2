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

import java.io.Serializable;
import java.util.Calendar;

public class NoteKey implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

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

  @Override
  public int hashCode() {
    return creationTime.hashCode() + creationDate.hashCode() + createdBy.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Note) {
      Note note = (Note) obj;

      if (!note.getCreatedBy().equals(getCreatedBy())) {
        return false;
      }
      if (!note.getCreationDate().equals(getCreationDate())) {
        return false;
      }
      if (!note.getCreationTime().equals(getCreationTime())) {
        return false;
      }
      return true;
    }
    return false;
  }

  private Calendar creationTime;
  private Calendar creationDate;
  private String createdBy;

}
