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
package org.apache.olingo.odata2.ref.annotation.processor;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.ODataDebugCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.core.annotation.edm.AnnotationEdmProvider;
import org.apache.olingo.odata2.core.annotation.processor.AnnotationProcessor;
import org.apache.olingo.odata2.ref.annotation.model.Building;
import org.apache.olingo.odata2.ref.annotation.model.Team;
import org.apache.olingo.odata2.ref.annotation.model.ds.BuildingDs;
import org.apache.olingo.odata2.ref.annotation.model.ds.TeamDs;

/**
 *
 */
public class ScenarioServiceFactory extends ODataServiceFactory {

  private static boolean isInitialized = false;
  
  @Override
  public ODataService createService(final ODataContext context) throws ODataException {
    if(!isInitialized) {
      initializeSampleData();
      isInitialized = true;
    }

    return createODataSingleProcessorService(
            new AnnotationEdmProvider("org.apache.olingo.odata2.ref.annotation.model"),
            new AnnotationProcessor(context, "org.apache.olingo.odata2.ref.annotation.model"));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ODataCallback> T getCallback(final Class<? extends ODataCallback> callbackInterface) {
    return (T) (callbackInterface.isAssignableFrom(ScenarioErrorCallback.class)
            ? new ScenarioErrorCallback() : callbackInterface.isAssignableFrom(ODataDebugCallback.class)
            ? new ScenarioDebugCallback() : super.getCallback(callbackInterface));
  }


  private final class ScenarioDebugCallback implements ODataDebugCallback {

    @Override
    public boolean isDebugEnabled() {
      return true;
    }
  }
  
  
  private void initializeSampleData() {
    TeamDs teamDs = new TeamDs();
    teamDs.writeTeam(createTeam("Team Alpha", true));
    teamDs.writeTeam(createTeam("Team Beta", false));
    teamDs.writeTeam(createTeam("Team Gamma", false));
    teamDs.writeTeam(createTeam("Team Omega", true));
    teamDs.writeTeam(createTeam("Team Zeta", true));
    
    BuildingDs buildingsDs = new BuildingDs();
    buildingsDs.createOrUpdate(createBuilding("Red Building"));
    buildingsDs.createOrUpdate(createBuilding("Green Building"));
    buildingsDs.createOrUpdate(createBuilding("Blue Building"));
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
}
