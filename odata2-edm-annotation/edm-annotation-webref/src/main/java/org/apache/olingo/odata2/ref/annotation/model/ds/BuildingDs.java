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
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityCreate;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityDataSource;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityDelete;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityRead;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntitySetRead;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityUpdate;
import org.apache.olingo.odata2.ref.annotation.model.Building;

/**
 *
 */
@EntityDataSource(name="BuildingDataSource", entityType = Building.class)
public class BuildingDs {
  
  private static int idCounter = 1;
  private final static Map<Integer, Building> id2Building = new HashMap<Integer, Building>();

  @EntityRead
  public Building read(String id) {
    return id2Building.get(Integer.valueOf(id));
  }
  
  @EntitySetRead
  public Collection<Building> read() {
    return id2Building.values();
  }
    
  @EntityCreate
  @EntityUpdate
  public Building createOrUpdate(Building building) {
    Building b = id2Building.get(Integer.valueOf(building.getId()));
    if(b != null) {
      b.setName(building.getName());
    } else {      
      final int id = idCounter++;
      b = new Building(id, building.getName());
      id2Building.put(Integer.valueOf(id), b);
    }
    return b;    
  }
  
  @EntityDelete
  public Building delete(String id) {
    return id2Building.remove(Integer.valueOf(id));
  }
}
