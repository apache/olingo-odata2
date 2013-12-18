package org.apache.olingo.odata2.jpa.processor.ref.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;

import org.apache.olingo.odata2.jpa.processor.ref.exception.InvalidPartyRoleException;

@Entity
public class EmailActivity extends Activity {

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
    boolean maxFrom = false;
    for (ActivityParty party : getParties()) {
      Short role = party.getRole();
      if (role != PartyRole.FROM.ordinal() ||
          role != PartyRole.TO.ordinal() ||
          role != PartyRole.CC.ordinal() ||
          role != PartyRole.BCC.ordinal()) {
        throw new InvalidPartyRoleException();
      }
      if (role == PartyRole.FROM.ordinal() && maxFrom == false) {
        maxFrom = true;
      } else if (role == PartyRole.FROM.ordinal() && maxFrom == true) {
        throw new InvalidPartyRoleException();
      }
    }
  }

}
