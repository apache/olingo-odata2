/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE_2_2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.annotation.processor.ref;

import org.apache.http.HttpResponse;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Before;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertFalse;

/**
 * Tests employing the reference scenario reading the metadata document in XML format
 * 
 */
public class MetadataTest extends AbstractRefXmlTest {

  public MetadataTest(final ServletType servletType) {
    super(servletType);
  }

  private static String payload;

  @Before
  public void prepare() throws Exception {
    payload = getBody(callUri("$metadata"));
  }

  @Test
  public void metadataDocument() throws Exception {
    final HttpResponse response = callUri("$metadata");
    checkMediaType(response, HttpContentType.APPLICATION_XML_UTF8);
    assertFalse(getBody(response).isEmpty());

    notFound("$invalid");
    badRequest("$metadata?$format=atom");
  }

  @Test
  public void testGeneral() throws Exception {
    assertXpathExists("/edmx:Edmx[@Version='1.0']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices[@m:DataServiceVersion='1.0']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema[@Namespace='RefScenario']", payload);
  }

  @Test
  public void testEntityTypes() throws Exception {
    // Employee
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and @m:HasStream='true']", payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and @m:HasStream='true']/edm:Key",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and " +
            "@m:HasStream='true']/edm:Key/edm:PropertyRef[@Name='EmployeeId']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and" +
            " @m:HasStream='true']/edm:Property[@Name='EmployeeId' and @Type='Edm.String' and @Nullable='false']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and" +
            " @m:HasStream='true']/edm:Property[@Name='EmployeeName' and @Type='Edm.String']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and" +
            " @m:HasStream='true']/edm:Property[@Name='Location' and @Type='RefScenario.c_Location']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and " +
            "@m:HasStream='true']/edm:Property[@Name='Age' and @Type='Edm.Int32']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and " +
            "@m:HasStream='true']/edm:Property[@Name='EntryDate' and @Type='Edm.DateTime' and " +
            "@Nullable='true']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and " +
            "@m:HasStream='true']/edm:Property[@Name='ImageUrl' and @Type='Edm.String']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and" +
            " @m:HasStream='true']/edm:NavigationProperty[@Name='ne_Manager' and " +
            "@Relationship='RefScenario.ManagerEmployees' and @FromRole='r_Employees' and @ToRole='r_Manager']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and" +
            " @m:HasStream='true']/edm:NavigationProperty[@Name='ne_Team' and " +
            "@Relationship='RefScenario.TeamEmployees' and @FromRole='r_Employees' and @ToRole='r_Team']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Employee' and " +
            "@m:HasStream='true']/edm:NavigationProperty[@Name='ne_Room' and " +
            "@Relationship='RefScenario.r_Employees_2_r_Room' and @FromRole='r_Employees' and @ToRole='r_Room']",
        payload);

    // Team
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Team' and @BaseType='RefScenario.Base']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Team' and " +
            "@BaseType='RefScenario.Base']/edm:Property[@Name='IsScrumTeam' and " +
            "@Type='Edm.Boolean' and @Nullable='true']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Team' and " +
            "@BaseType='RefScenario.Base']/edm:NavigationProperty[@Name='nt_Employees' and " +
            "@Relationship='RefScenario.TeamEmployees' and @FromRole='r_Team' and @ToRole='r_Employees']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Team' and " +
            "@BaseType='RefScenario.Base']/edm:NavigationProperty[@Name='SubTeam' and " +
            "@Relationship='RefScenario.Team_2_r_SubTeam' and @FromRole='Team' and @ToRole='r_SubTeam']",
        payload);

    // Room
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Room' and @BaseType='RefScenario.Base']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Room' and " +
            "@BaseType='RefScenario.Base']/edm:Property[@Name='Seats' and @Type='Edm.Int32']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Room' and " +
            "@BaseType='RefScenario.Base']/edm:Property[@Name='Version' and @Type='Edm.Int32']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Room' and" +
            " @BaseType='RefScenario.Base']/edm:NavigationProperty[@Name='nr_Employees' and " +
            "@Relationship='RefScenario.r_Employees_2_r_Room' and @FromRole='r_Room' and @ToRole='r_Employees']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Room' and " +
            "@BaseType='RefScenario.Base']/edm:NavigationProperty[@Name='nr_Building' and " +
            "@Relationship='RefScenario.BuildingRooms' and @FromRole='r_Rooms' and @ToRole='r_Building']",
        payload);

    // Manager
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Manager' and " +
            "@BaseType='RefScenario.Employee']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Manager' and " +
            "@BaseType='RefScenario.Employee']/edm:NavigationProperty[@Name='nm_Employees' and " +
            "@Relationship='RefScenario.ManagerEmployees' and @FromRole='r_Manager' and @ToRole='r_Employees']",
        payload);

    // Building
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Building']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Building']/edm:Key", payload);
    assertXpathExists( "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Building']/edm:Key/edm" +
                    ":PropertyRef[@Name='Id']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Building']" +
                "/edm:Property[@Name='Id' and @Type='Edm.Int32' and @Nullable='false']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Building']" +
                    "/edm:Property[@Name='Name' and @Type='Edm.String']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Building']/" +
                "edm:Property[@Name='Image' and @Type='Edm.Binary']", payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Building']" +
                "/edm:NavigationProperty[@Name='nb_Rooms' and @Relationship='RefScenario.BuildingRooms' " +
                "and @FromRole='r_Building' and @ToRole='r_Rooms']", payload);

    // Base
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Base' and @Abstract='true']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Base' and @Abstract='true']/edm:Key", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Base' and @Abstract='true']" +
            "/edm:Key/edm:PropertyRef[@Name='Id']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Base' and @Abstract='true']" +
            "/edm:Property[@Name='Id' and @Type='Edm.String' and @Nullable='false']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityType[@Name='Base' and @Abstract='true']" +
            "/edm:Property[@Name='Name' and @Type='Edm.String']",payload);
  }

  @Test
  public void testComplexTypes() throws Exception {
    // Location
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:ComplexType[@Name='c_Location']", payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:ComplexType[@Name='c_Location']/edm:Property[@Name='City' and " +
            "@Type='RefScenario.c_City']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:ComplexType[@Name='c_Location']/edm:Property[@Name='Country' " +
            "and @Type='Edm.String']",
        payload);

    // Location
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:ComplexType[@Name='c_City']", payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:ComplexType[@Name='c_City']/edm:Property[@Name='PostalCode' " +
            "and @Type='Edm.String']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:ComplexType[@Name='c_City']/edm:Property[@Name='CityName' " +
            "and @Type='Edm.String']",
        payload);
  }

  @Test
  public void testAssociation() throws Exception {
    // ManagerEmployees
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='ManagerEmployees']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='ManagerEmployees']" +
            "/edm:End[@Type='RefScenario.Employee' and @Multiplicity='*' and @Role='r_Employees']", payload);
    assertXpathExists( "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='ManagerEmployees']" +
            "/edm:End[@Type='RefScenario.Manager' and @Multiplicity='1' and @Role='r_Manager']", payload);

    // TeamEmployees
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='TeamEmployees']", payload);
    assertXpathExists( "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='TeamEmployees']" +
            "/edm:End[@Type='RefScenario.Employee' and @Multiplicity='*' and @Role='r_Employees']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='TeamEmployees']" +
            "/edm:End[@Type='RefScenario.Team' and @Multiplicity='1' and @Role='r_Team']", payload);

    // Team_2_r_SubTeam
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='Team_2_r_SubTeam']", payload);
    assertXpathExists( "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='Team_2_r_SubTeam']" +
        "/edm:End[@Type='RefScenario.Team' and @Multiplicity='1' and @Role='Team']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='Team_2_r_SubTeam']" +
        "/edm:End[@Type='RefScenario.Team' and @Multiplicity='1' and @Role='r_SubTeam']", payload);

    // RoomEmployees
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='r_Employees_2_r_Room']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='r_Employees_2_r_Room']" +
            "/edm:End[@Type='RefScenario.Employee' and @Multiplicity='*' and @Role='r_Employees']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='r_Employees_2_r_Room']" +
            "/edm:End[@Type='RefScenario.Room' and @Multiplicity='1' and @Role='r_Room']", payload);

    // BuildingRooms
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='BuildingRooms']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='BuildingRooms']" +
            "/edm:End[@Type='RefScenario.Building' and @Multiplicity='1' and @Role='r_Building']", payload);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/edm:Schema/edm:Association[@Name='BuildingRooms']" +
            "/edm:End[@Type='RefScenario.Room' and @Multiplicity='*' and @Role='r_Rooms']", payload);
  }

  @Test
  public void testEntityContainer() throws Exception {
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']", payload);

    // EntitySets
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and "
            +
            "@m:IsDefaultEntityContainer='true']/edm:EntitySet[@Name='Employees' and " +
            "@EntityType='RefScenario.Employee']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:EntitySet[@Name='Teams' and @EntityType='RefScenario.Team']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:EntitySet[@Name='Rooms' and @EntityType='RefScenario.Room']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:EntitySet[@Name='Managers' and @EntityType='RefScenario.Manager']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and "
            +
            "@m:IsDefaultEntityContainer='true']/edm:EntitySet[@Name='Buildings' and " +
            "@EntityType='RefScenario.Building']",
        payload);

    // AssociationSets
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='ManagerEmployees' and " +
            "@Association='RefScenario.ManagerEmployees']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='ManagerEmployees' and " +
            "@Association='RefScenario.ManagerEmployees']/edm:End[@EntitySet='Managers' and @Role='r_Manager']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='ManagerEmployees' and " +
            "@Association='RefScenario.ManagerEmployees']/edm:End[@EntitySet='Employees' and @Role='r_Employees']",
        payload);

    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='TeamEmployees' and " +
            "@Association='RefScenario.TeamEmployees']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='TeamEmployees' and " +
            "@Association='RefScenario.TeamEmployees']/edm:End[@EntitySet='Teams' and @Role='r_Team']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='TeamEmployees' and " +
            "@Association='RefScenario.TeamEmployees']/edm:End[@EntitySet='Employees' and @Role='r_Employees']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='Team_2_r_SubTeam' and " +
            "@Association='RefScenario.Team_2_r_SubTeam']/edm:End[@EntitySet='Teams' and @Role='Team']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='Team_2_r_SubTeam' and " +
            "@Association='RefScenario.Team_2_r_SubTeam']/edm:End[@EntitySet='Teams' and @Role='r_SubTeam']",
        payload);

    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='r_Employees_2_r_Room' and " +
            "@Association='RefScenario.r_Employees_2_r_Room']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='r_Employees_2_r_Room' and " +
            "@Association='RefScenario.r_Employees_2_r_Room']/edm:End[@EntitySet='Rooms' and @Role='r_Room']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='r_Employees_2_r_Room' and " +
            "@Association='RefScenario.r_Employees_2_r_Room']/edm:End[@EntitySet='Employees' and @Role='r_Employees']",
        payload);

    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='BuildingRooms' and " +
            "@Association='RefScenario.BuildingRooms']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='BuildingRooms' and " +
            "@Association='RefScenario.BuildingRooms']/edm:End[@EntitySet='Buildings' and @Role='r_Building']",
        payload);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/edm:Schema/edm:EntityContainer[@Name='DefaultContainer' and " +
            "@m:IsDefaultEntityContainer='true']/edm:AssociationSet[@Name='BuildingRooms' and " +
            "@Association='RefScenario.BuildingRooms']/edm:End[@EntitySet='Rooms' and @Role='r_Rooms']",
        payload);
  }
}
