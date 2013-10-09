/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.olingo.odata2.ref.annotation.model.ds;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityRead;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityDataSource;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityCreate;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntitySetRead;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityUpdate;
import org.apache.olingo.odata2.ref.annotation.model.Team;

/**
 *
 */
@EntityDataSource(name="TeamDataSource", entityType = Team.class)
public class TeamDs {
  
  private static int idCounter = 1;
  private static final Map<Integer, Team> id2Team = new HashMap<Integer, Team>();

  @EntityRead
  public Team readTeam(String id) {
    return id2Team.get(Integer.valueOf(id));
  }
  
  @EntitySetRead
  public Collection<Team> readAllTeams() {
    return id2Team.values();
  }
  
  @EntityCreate
  public Team writeTeam(Team team) {
    final int id = idCounter++;
    team.setId(id);
    id2Team.put(Integer.valueOf(id), team);

    return team;
  }
  
  @EntityUpdate
  public Team update(Team team) {
    Team t = id2Team.get(Integer.valueOf(team.getId()));
    if(t != null) {
      t.setName(team.getName());
      t.setScrumTeam(team.isScrumTeam());
    }
    return team;    
  }
}
