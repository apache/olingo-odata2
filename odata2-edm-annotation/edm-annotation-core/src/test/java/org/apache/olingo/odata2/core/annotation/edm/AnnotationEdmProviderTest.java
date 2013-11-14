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
package org.apache.olingo.odata2.core.annotation.edm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.core.annotation.model.Building;
import org.apache.olingo.odata2.core.annotation.model.City;
import org.apache.olingo.odata2.core.annotation.model.Employee;
import org.apache.olingo.odata2.core.annotation.model.Location;
import org.apache.olingo.odata2.core.annotation.model.Manager;
import org.apache.olingo.odata2.core.annotation.model.ModelSharedConstants;
import org.apache.olingo.odata2.core.annotation.model.Photo;
import org.apache.olingo.odata2.core.annotation.model.RefBase;
import org.apache.olingo.odata2.core.annotation.model.Room;
import org.apache.olingo.odata2.core.annotation.model.Team;
import org.junit.Test;

/**
 *
 * @author d046871
 */
public class AnnotationEdmProviderTest {

  private final Collection<Class<?>> annotatedClasses = new ArrayList<Class<?>>();
  private final AnnotationEdmProvider aep;

  public AnnotationEdmProviderTest() {
    annotatedClasses.add(RefBase.class);
    annotatedClasses.add(Building.class);
    annotatedClasses.add(City.class);
    annotatedClasses.add(Employee.class);
    annotatedClasses.add(Location.class);
    annotatedClasses.add(Manager.class);
    annotatedClasses.add(Photo.class);
    annotatedClasses.add(Room.class);
    annotatedClasses.add(Team.class);

    aep = new AnnotationEdmProvider(annotatedClasses);
  }

  @Test
  public void loadAnnotatedClassesFromPackage() throws Exception {
    AnnotationEdmProvider localAep = new AnnotationEdmProvider("org.apache.olingo.odata2.core.annotation.model");

    // validate employee
    EntityType employee = localAep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Employee"));
    assertEquals("Employee", employee.getName());
    final List<PropertyRef> employeeKeys = employee.getKey().getKeys();
    assertEquals(1, employeeKeys.size());
    assertEquals("EmployeeId", employeeKeys.get(0).getName());
    assertEquals(4, employee.getProperties().size());
    assertEquals(3, employee.getNavigationProperties().size());

    List<Schema> schemas = localAep.getSchemas();
    assertEquals(1, schemas.size());
    EntityContainerInfo info = localAep.getEntityContainerInfo(ModelSharedConstants.CONTAINER_1);
    assertTrue(info.isDefaultEntityContainer());
  }

  @Test
  public void annotationProviderBasic() throws Exception {
    assertNotNull(aep);

    List<Schema> schemas = aep.getSchemas();
    assertEquals(1, schemas.size());
    EntityContainerInfo info = aep.getEntityContainerInfo(ModelSharedConstants.CONTAINER_1);
    assertTrue(info.isDefaultEntityContainer());
    
    FunctionImport funImp = aep.getFunctionImport(ModelSharedConstants.CONTAINER_1, "NoImport");
    assertNull(funImp);

    final FullQualifiedName associationFqn = new FullQualifiedName(
            ModelSharedConstants.NAMESPACE_1, "NoAssociation");
    Association noAssociation = aep.getAssociation(associationFqn);
    assertNull(noAssociation);
    
    AssociationSet noAssociationSet = aep.getAssociationSet(
            ModelSharedConstants.CONTAINER_1, associationFqn, "NoSrc", "NoSrcEntity");
    assertNull(noAssociationSet);
    
    AssociationSet asBuildingRooms = aep.getAssociationSet(
            ModelSharedConstants.CONTAINER_1, defaultFqn("BuildingRooms"), "Buildings", "r_Building");
    assertNotNull(asBuildingRooms);
    assertEquals("Buildings", asBuildingRooms.getEnd1().getEntitySet());
    assertEquals("r_Building", asBuildingRooms.getEnd1().getRole());
    assertEquals("Rooms", asBuildingRooms.getEnd2().getEntitySet());
    assertEquals("r_Room", asBuildingRooms.getEnd2().getRole());
  }

  @Test
  public void annotationProviderGetDefaultContainer() throws Exception {
    assertNotNull(aep);

    List<Schema> schemas = aep.getSchemas();
    assertEquals(1, schemas.size());
    EntityContainerInfo info = aep.getEntityContainerInfo(null);
    assertTrue(info.isDefaultEntityContainer());
    assertEquals(ModelSharedConstants.CONTAINER_1, info.getName());
  }

  @Test
  public void schemaBasic() throws Exception {
    assertNotNull(aep);

    List<Schema> schemas = aep.getSchemas();
    assertEquals(1, schemas.size());

    Schema schema = schemas.get(0);
    List<EntityContainer> containers = schema.getEntityContainers();
    assertEquals(1, containers.size());
    EntityContainer container = containers.get(0);
    assertEquals(ModelSharedConstants.CONTAINER_1, container.getName());
    final List<EntitySet> entitySets = container.getEntitySets();
    assertEquals(5, entitySets.size());
    
    List<Association> associations = schema.getAssociations();
    assertEquals(4, associations.size());
    for (Association association : associations) {
      assertNotNull(association.getName());
      validateAssociation(association);
    }
  }
  
  private FullQualifiedName defaultFqn(String name) {
    return new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, name);
  }

  private void validateAssociation(Association association) {
    String name = association.getName();
    if(name.equals("RoomEmployees")) {
      validateAssociation(association, 
              "r_Room", EdmMultiplicity.ONE, defaultFqn("Room"),
              "r_Employee", EdmMultiplicity.MANY, defaultFqn("Employee"));
    } else if(name.equals("BuildingRooms")) {
        validateAssociation(association, 
                "r_Building", EdmMultiplicity.ONE, defaultFqn("Building"),
                "r_Room", EdmMultiplicity.MANY, defaultFqn("Room"));
    } else if(name.equals("ManagerEmployees")) {
        validateAssociation(association, 
                "r_Manager", EdmMultiplicity.ONE, defaultFqn("Manager"),
                "r_Employees", EdmMultiplicity.MANY, defaultFqn("Employee"));
    } else if(name.equals("r_Employee-r_Team")) {
        validateAssociation(association, 
                "r_Team", EdmMultiplicity.ONE, defaultFqn("Team"),
                "r_Employee", EdmMultiplicity.MANY, defaultFqn("Employee"));
    } else {
        fail("Got unknown association to validate with name '" + name + "'.");
    }
  }

  private void validateAssociation(Association association, 
          String fromRole, EdmMultiplicity fromMulti, FullQualifiedName fromType, 
          String toRole, EdmMultiplicity toMulti, FullQualifiedName toType) {

    AssociationEnd[] ends = new AssociationEnd[]{association.getEnd1(),association.getEnd2()};
    for (AssociationEnd associationEnd : ends) {
      if(associationEnd.getRole().equals(fromRole)) {
        validateAssociationEnd(associationEnd, fromRole, fromMulti, fromType);
      } else if(associationEnd.getRole().equals(toRole)) {
        validateAssociationEnd(associationEnd, toRole, toMulti, toType);
      } else {
        fail("Unexpected navigation end '" + associationEnd.getRole() 
                + "' for association with name '" + association.getName() + "'.");
      }
    }
  }

    private void validateAssociationEnd(AssociationEnd associationEnd, 
          String role, EdmMultiplicity multiplicity, FullQualifiedName type) {
    assertEquals(role, associationEnd.getRole());
    assertEquals(multiplicity, associationEnd.getMultiplicity());
    assertEquals(type, associationEnd.getType());
  }


  @Test
  public void entitySetTeams() throws Exception {
    // validate teams
    EntitySet teams = aep.getEntitySet(ModelSharedConstants.CONTAINER_1, "Teams");
    assertEquals(ModelSharedConstants.NAMESPACE_1, teams.getEntityType().getNamespace());
    assertEquals("Team", teams.getEntityType().getName());
  }

  @Test
  public void entityTypeEmployee() throws Exception {
    // validate employee
    EntityType employee = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Employee"));
    assertEquals("Employee", employee.getName());
    final List<PropertyRef> employeeKeys = employee.getKey().getKeys();
    assertEquals(1, employeeKeys.size());
    assertEquals("EmployeeId", employeeKeys.get(0).getName());
    assertEquals(4, employee.getProperties().size());
    assertEquals(3, employee.getNavigationProperties().size());

    for (NavigationProperty navigationProperty : employee.getNavigationProperties()) {
      if (navigationProperty.getName().equals("ne_Manager")) {
        validateNavProperty(navigationProperty, "ManagerEmployees", "r_Employees", "r_Manager");
      } else if (navigationProperty.getName().equals("ne_Team")) {
        validateNavProperty(navigationProperty, "r_Employee-r_Team", "r_Employee", "r_Team");
      } else if (navigationProperty.getName().equals("ne_Room")) {
        validateNavProperty(navigationProperty, "RoomEmployees", "r_Employee", "r_Room");
      } else {
        fail("Got unexpected navigation property with name '" + navigationProperty.getName() + "'.");
      }
    }
}

  @Test
  public void entityTypeTeam() throws Exception {
    // validate team
    EntityType team = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Team"));
    assertEquals("Team", team.getName());
    assertEquals("Base", team.getBaseType().getName());
    assertEquals(ModelSharedConstants.NAMESPACE_1, team.getBaseType().getNamespace());

    assertEquals(1, team.getProperties().size());
    assertEquals(1, team.getNavigationProperties().size());
    NavigationProperty navigationProperty= team.getNavigationProperties().get(0);
    validateNavProperty(navigationProperty, "r_Employee-r_Team", "r_Team", "r_Employee");
  }

  @Test
  public void entityTypeAbstractBaseType() throws Exception {
    // validate employee
    EntityType baseType = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Base"));
    assertEquals("Base", baseType.getName());
    final List<PropertyRef> keys = baseType.getKey().getKeys();
    assertEquals(1, keys.size());
    assertEquals("Id", keys.get(0).getName());
    assertEquals(2, baseType.getProperties().size());
    assertTrue(baseType.isAbstract());

    // validate base for team
    EntityType team = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Team"));
    assertEquals("Team", team.getName());
    assertEquals("Base", team.getBaseType().getName());
    assertEquals(ModelSharedConstants.NAMESPACE_1, team.getBaseType().getNamespace());
  }

  @Test
  public void complexTypeLocation() throws Exception {
    // validate employee
    EntityType employee = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Employee"));
    final List<Property> properties = employee.getProperties();
    Property location = null;
    for (Property property : properties) {
      if (property.getName().equals("Location")) {
        location = property;
      }
    }
    assertNotNull(location);
    assertEquals("Location", location.getName());

    // validate location complex type
    ComplexType locationType = aep.getComplexType(
            new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "c_Location"));
    assertEquals("c_Location", locationType.getName());
    assertEquals(2, locationType.getProperties().size());
  }

  @Test
  public void entityTypeRoomWithNavigation() throws Exception {
    // validate employee
    EntityType room = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Room"));
    assertEquals("Room", room.getName());
    assertEquals("Base", room.getBaseType().getName());
    assertEquals(2, room.getProperties().size());
    final List<NavigationProperty> navigationProperties = room.getNavigationProperties();
    assertEquals(2, navigationProperties.size());
    
    for (NavigationProperty navigationProperty : navigationProperties) {
      if(navigationProperty.getName().equals("nr_Employees")) {
        validateNavProperty(navigationProperty, "RoomEmployees", "r_Room", "r_Employee");
      } else if(navigationProperty.getName().equals("nr_Building")) {
        validateNavProperty(navigationProperty, "BuildingRooms", "r_Room", "r_Building");
      } else {
        fail("Got unexpected navigation property with name '" + navigationProperty.getName() + "'.");
      }
    }
  }
  
  private void validateNavProperty(NavigationProperty navigationProperty, String name,
          String relationship, String fromRole, String toRole) {
    if(name != null) {
      assertEquals(name, navigationProperty.getName());
    }
    FullQualifiedName fqn = new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, relationship);
    assertEquals("Wrong relationship for navigation property.", fqn, navigationProperty.getRelationship());
    assertEquals("Wrong fromRole for navigation property.", fromRole, navigationProperty.getFromRole());
    assertEquals("Wrong toRole for navigation property.", toRole, navigationProperty.getToRole());
  }
  
  private void validateNavProperty(NavigationProperty navigationProperty, 
          String relationship, String fromRole, String toRole) {
    validateNavProperty(navigationProperty, null, relationship, fromRole, toRole);
  }
}
