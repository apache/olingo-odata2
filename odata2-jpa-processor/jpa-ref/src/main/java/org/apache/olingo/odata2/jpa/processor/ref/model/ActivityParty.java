package org.apache.olingo.odata2.jpa.processor.ref.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ActivityParty {

  @Id
  @Column(name = "PARTY_ID")
  private long id;

  @Column(name = "ACTIVITY_ID")
  private long activityId;

  @Column
  private String name;
  @Column
  private short role;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public short getRole() {
    return role;
  }

  public void setRole(final short role) {
    this.role = role;
  }

  public long getId() {
    return id;
  }

  public void setId(final long id) {
    this.id = id;
  }

}
