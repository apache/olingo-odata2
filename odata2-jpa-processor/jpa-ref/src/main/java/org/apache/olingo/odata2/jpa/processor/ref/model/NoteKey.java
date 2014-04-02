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

  public void setCreationTime(Calendar creationTime) {
    this.creationTime = creationTime;
  }

  public Calendar getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Calendar creationDate) {
    this.creationDate = creationDate;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public int hashCode() {
    return creationTime.hashCode() + creationDate.hashCode() + createdBy.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Note) {
      Note note = (Note) obj;

      if (!note.getCreatedBy().equals(this.getCreatedBy())) {
        return false;
      }
      if (!note.getCreationDate().equals(this.getCreationDate())) {
        return false;
      }
      if (!note.getCreationTime().equals(this.getCreationTime())) {
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
