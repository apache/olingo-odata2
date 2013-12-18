package org.apache.olingo.odata2.jpa.processor.ref.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;

import org.apache.olingo.odata2.jpa.processor.ref.exception.InvalidPartyRoleException;

@Entity
public class AppointmentActivity extends Activity {

  @OneToMany(cascade = CascadeType.ALL)
  private List<ActivityParty> parties = new ArrayList<ActivityParty>();

  public List<ActivityParty> getParties() {
    return parties;
  }

  public void setParties(final List<ActivityParty> parties) {
    this.parties = parties;
  }

  @PrePersist
  public void validatePartyRoles() throws InvalidPartyRoleException {
    boolean maxOrganizer = false;
    for (ActivityParty party : getParties()) {
      Short role = party.getRole();
      if (role != PartyRole.ATTENDEE.ordinal() ||
          role != PartyRole.ORGANIZER.ordinal()) {
        throw new InvalidPartyRoleException();
      }
      if (role == PartyRole.ORGANIZER.ordinal() && maxOrganizer == false) {
        maxOrganizer = true;
      } else if (role == PartyRole.ORGANIZER.ordinal() && maxOrganizer == true) {
        throw new InvalidPartyRoleException();
      }
    }
  }

}
