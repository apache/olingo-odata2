package org.apache.olingo.odata2.jpa.processor.ref.model;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@MappedSuperclass
public abstract class Activity {

  @Column(name = "ACTIVITY_ID")
  @Id
  protected long id;

  @Column
  protected String subject;

  @Temporal(TemporalType.TIMESTAMP)
  protected Calendar creationDate;

  @Column
  protected String note;

  public Calendar getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(final Calendar creationDate) {
    this.creationDate = creationDate;
  }

  public String getNote() {
    return note;
  }

  public void setNote(final String note) {
    this.note = note;
  }

  public long getId() {
    return id;
  }

  public void setId(final long id) {
    this.id = id;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(final String subject) {
    this.subject = subject;
  }

}
