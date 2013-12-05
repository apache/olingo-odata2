/**
 * *****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *****************************************************************************
 */
package org.apache.olingo.odata2.ref.annotation.processor;

import java.util.Calendar;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.ODataDebugCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataErrorCallback;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.annotation.data.AnnotationInMemoryDs;
import org.apache.olingo.odata2.core.annotation.data.AnnotationValueAccess;
import org.apache.olingo.odata2.core.annotation.data.DataStore;
import org.apache.olingo.odata2.core.annotation.edm.AnnotationEdmProvider;
import org.apache.olingo.odata2.core.annotation.processor.ListsProcessor;
import org.apache.olingo.odata2.ref.annotation.model.Building;
import org.apache.olingo.odata2.ref.annotation.model.Employee;
import org.apache.olingo.odata2.ref.annotation.model.Location;
import org.apache.olingo.odata2.ref.annotation.model.Manager;
import org.apache.olingo.odata2.ref.annotation.model.Photo;
import org.apache.olingo.odata2.ref.annotation.model.ResourceHelper;
import org.apache.olingo.odata2.ref.annotation.model.Room;
import org.apache.olingo.odata2.ref.annotation.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class AnnotationPocServiceFactory extends ODataServiceFactory {

  private static boolean isInitialized = false;

  @Override
  public ODataService createService(final ODataContext context) throws ODataException {

    String modelPackage = "org.apache.olingo.odata2.ref.annotation.model";
    AnnotationEdmProvider annotationEdmProvider = new AnnotationEdmProvider(modelPackage);
    AnnotationInMemoryDs annotationScenarioDs = new AnnotationInMemoryDs(modelPackage);
    AnnotationValueAccess annotationValueAccess = new AnnotationValueAccess();

    if (!isInitialized) {
      initializeSampleData(annotationScenarioDs);
      isInitialized = true;
    }

    // Edm via Annotations and ListProcessor via AnnotationDS with AnnotationsValueAccess
    return createODataSingleProcessorService(annotationEdmProvider,
            new ListsProcessor(annotationScenarioDs, annotationValueAccess));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ODataCallback> T getCallback(final Class<? extends ODataCallback> callbackInterface) {
    return (T) (callbackInterface.isAssignableFrom(ScenarioErrorCallback.class)
            ? new ScenarioErrorCallback() : callbackInterface.isAssignableFrom(ODataDebugCallback.class)
            ? new ScenarioDebugCallback() : super.getCallback(callbackInterface));
  }

  /*
  * Helper classes and methods
  */

  /**
   * 
   */
  private final class ScenarioDebugCallback implements ODataDebugCallback {

    @Override
    public boolean isDebugEnabled() {
      return true;
    }
  }

  private class ScenarioErrorCallback implements ODataErrorCallback {

    private final Logger LOG = LoggerFactory.getLogger(ScenarioErrorCallback.class);

    @Override
    public ODataResponse handleError(final ODataErrorContext context) throws ODataApplicationException {
      if (context.getHttpStatus() == HttpStatusCodes.INTERNAL_SERVER_ERROR) {
        LOG.error("Internal Server Error", context.getException());
      }

      return EntityProvider.writeErrorDocument(context);
    }

  }

  private void initializeSampleData(AnnotationInMemoryDs dataSource) throws ODataApplicationException {
    DataStore<Team> teamDs = dataSource.getDataStore(Team.class);
    teamDs.create(createTeam("Team Alpha", true));
    teamDs.create(createTeam("Team Beta", false));
    teamDs.create(createTeam("Team Gamma", false));
    teamDs.create(createTeam("Team Omega", true));
    teamDs.create(createTeam("Team Zeta", true));

    DataStore<Building> buildingsDs = dataSource.getDataStore(Building.class);
    Building redBuilding = createBuilding("Red Building");
    buildingsDs.create(redBuilding);
    Building greenBuilding = createBuilding("Green Building");
    buildingsDs.create(greenBuilding);
    Building blueBuilding = createBuilding("Blue Building");
    buildingsDs.create(blueBuilding);
    Building yellowBuilding = createBuilding("Yellow Building");
    buildingsDs.create(yellowBuilding);

    DataStore<Photo> photoDs = dataSource.getDataStore(Photo.class);
    photoDs.create(createPhoto("Small picture", ResourceHelper.Format.GIF));
    photoDs.create(createPhoto("Medium picture", ResourceHelper.Format.PNG));
    photoDs.create(createPhoto("Big picture", ResourceHelper.Format.JPEG));
    photoDs.create(createPhoto("Huge picture", ResourceHelper.Format.BMP));

    DataStore<Room> roomDs = dataSource.getDataStore(Room.class);
    roomDs.create(createRoom("Tiny red room", 5, 1, redBuilding));
    roomDs.create(createRoom("Small red room", 20, 1, redBuilding));
    roomDs.create(createRoom("Small green room", 20, 1, greenBuilding));
    roomDs.create(createRoom("Big blue room", 40, 1, blueBuilding));
    roomDs.create(createRoom("Huge blue room", 120, 1, blueBuilding));
    roomDs.create(createRoom("Huge yellow room", 120, 1, yellowBuilding));

    DataStore<Employee> employeeDataStore = dataSource.getDataStore(Employee.class);
    employeeDataStore.create(createEmployee("first Employee",
        new Location("Norge", "8392", "Ä"), 42, null,
        photoDs.read().iterator().next().getImage(), photoDs.read().iterator().next().getImageType(),
        "http://localhost/image/first.png",
        null, teamDs.read().iterator().next(), roomDs.read().iterator().next()));
  }

  private Employee createEmployee(final String name,
      Location location, final int age, final Calendar date,
      final byte[] image, final String imageType, final String imageUrl,
      Manager manager, Team team, Room room) {
    Employee employee = new Employee();
    employee.setEmployeeName(name);
    employee.setLocation(location);
    employee.setAge(age);
    employee.setEntryDate(date);
    employee.setImage(image);
    employee.setImageType(imageType);
    employee.setImageUri(imageUrl);
    employee.setManager(manager);
    employee.setTeam(team);
    employee.setRoom(room);
    return employee;
  }

  private Team createTeam(String teamName, boolean isScrumTeam) {
    Team team = new Team();
    team.setName(teamName);
    team.setScrumTeam(isScrumTeam);
    return team;
  }

  private Building createBuilding(String buildingName) {
    Building b = new Building();
    b.setName(buildingName);
    return b;
  }

  private Photo createPhoto(String name, ResourceHelper.Format format) {
    Photo p = new Photo();
    p.setName(name);
    p.setImageUri("http://localhost/image/" + name);
    p.setType(format.name());
    p.setImageType("image/" + format.name().toLowerCase());
    p.setImage(ResourceHelper.generateImage(format));
    return p;
  }

  private Room createRoom(String name, int seats, int version, Building building) {
    Room r = new Room();
    r.setName(name);
    r.setSeats(seats);
    r.setVersion(version);
    r.setBuilding(building);
    
    building.addRoom(r);
    
    return r;
  }
}
