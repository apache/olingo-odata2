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
package org.apache.olingo.odata2.testutil.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmConcurrencyMode;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTargetPath;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.AliasInfo;
import org.apache.olingo.odata2.api.edm.provider.AnnotationAttribute;
import org.apache.olingo.odata2.api.edm.provider.AnnotationElement;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.AssociationSetEnd;
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.CustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.FunctionImportParameter;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraint;
import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraintRole;
import org.apache.olingo.odata2.api.edm.provider.ReturnType;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.api.exception.ODataException;

public class EdmTestProvider extends EdmProvider {

  public static final String NAMESPACE_1 = "RefScenario";
  public static final String NAMESPACE_2 = "RefScenario2";

  private static final FullQualifiedName ENTITY_TYPE_1_1 = new FullQualifiedName(NAMESPACE_1, "Employee");
  private static final FullQualifiedName ENTITY_TYPE_1_BASE = new FullQualifiedName(NAMESPACE_1, "Base");
  private static final FullQualifiedName ENTITY_TYPE_1_2 = new FullQualifiedName(NAMESPACE_1, "Team");
  private static final FullQualifiedName ENTITY_TYPE_1_3 = new FullQualifiedName(NAMESPACE_1, "Room");
  private static final FullQualifiedName ENTITY_TYPE_1_4 = new FullQualifiedName(NAMESPACE_1, "Manager");
  private static final FullQualifiedName ENTITY_TYPE_1_5 = new FullQualifiedName(NAMESPACE_1, "Building");
  private static final FullQualifiedName ENTITY_TYPE_2_1 = new FullQualifiedName(NAMESPACE_2, "Photo");

  private static final FullQualifiedName COMPLEX_TYPE_1 = new FullQualifiedName(NAMESPACE_1, "c_Location");
  private static final FullQualifiedName COMPLEX_TYPE_2 = new FullQualifiedName(NAMESPACE_1, "c_City");

  private static final FullQualifiedName ASSOCIATION_1_1 = new FullQualifiedName(NAMESPACE_1, "ManagerEmployees");
  private static final FullQualifiedName ASSOCIATION_1_2 = new FullQualifiedName(NAMESPACE_1, "TeamEmployees");
  private static final FullQualifiedName ASSOCIATION_1_3 = new FullQualifiedName(NAMESPACE_1, "RoomEmployees");
  private static final FullQualifiedName ASSOCIATION_1_4 = new FullQualifiedName(NAMESPACE_1, "BuildingRooms");

  private static final String ROLE_1_1 = "r_Employees";
  private static final String ROLE_1_2 = "r_Team";
  private static final String ROLE_1_3 = "r_Room";
  private static final String ROLE_1_4 = "r_Manager";
  private static final String ROLE_1_5 = "r_Building";

  private static final String ENTITY_CONTAINER_1 = "Container1";
  private static final String ENTITY_CONTAINER_2 = "Container2";

  private static final String ENTITY_SET_1_1 = "Employees";
  private static final String ENTITY_SET_1_2 = "Teams";
  private static final String ENTITY_SET_1_3 = "Rooms";
  private static final String ENTITY_SET_1_4 = "Managers";
  private static final String ENTITY_SET_1_5 = "Buildings";
  private static final String ENTITY_SET_2_1 = "Photos";

  private static final String FUNCTION_IMPORT_1 = "EmployeeSearch";
  private static final String FUNCTION_IMPORT_2 = "AllLocations";
  private static final String FUNCTION_IMPORT_3 = "AllUsedRoomIds";
  private static final String FUNCTION_IMPORT_4 = "MaximalAge";
  private static final String FUNCTION_IMPORT_5 = "MostCommonLocation";
  private static final String FUNCTION_IMPORT_6 = "ManagerPhoto";
  private static final String FUNCTION_IMPORT_7 = "OldestEmployee";
  private static final String SCHEMA_ALIAS = "Self";

  @Override
  public List<AliasInfo> getAliasInfos() {
    List<AliasInfo> aliasInfos = new ArrayList<AliasInfo>();
    aliasInfos.add(new AliasInfo().setAlias(SCHEMA_ALIAS).setNamespace(NAMESPACE_1));
    return aliasInfos;
  }

  @Override
  public List<Schema> getSchemas() throws ODataException {
    final List<Schema> schemas = new ArrayList<Schema>();

    Schema schema = new Schema();
    schema.setNamespace(NAMESPACE_1);
    schema.setAlias(SCHEMA_ALIAS);

    final List<EntityType> entityTypes = new ArrayList<EntityType>();
    entityTypes.add(getEntityType(ENTITY_TYPE_1_1));
    entityTypes.add(getEntityType(ENTITY_TYPE_1_2));
    entityTypes.add(getEntityType(ENTITY_TYPE_1_3));
    entityTypes.add(getEntityType(ENTITY_TYPE_1_4));
    entityTypes.add(getEntityType(ENTITY_TYPE_1_5));
    entityTypes.add(getEntityType(ENTITY_TYPE_1_BASE));
    schema.setEntityTypes(entityTypes);

    final List<ComplexType> complexTypes = new ArrayList<ComplexType>();
    complexTypes.add(getComplexType(COMPLEX_TYPE_1));
    complexTypes.add(getComplexType(COMPLEX_TYPE_2));
    schema.setComplexTypes(complexTypes);

    final List<Association> associations = new ArrayList<Association>();
    associations.add(getAssociation(ASSOCIATION_1_1));
    associations.add(getAssociation(ASSOCIATION_1_2));
    associations.add(getAssociation(ASSOCIATION_1_3));
    associations.add(getAssociation(ASSOCIATION_1_4));
    schema.setAssociations(associations);

    EntityContainer entityContainer = new EntityContainer();
    entityContainer.setName(ENTITY_CONTAINER_1).setDefaultEntityContainer(true);

    final List<EntitySet> entitySets = new ArrayList<EntitySet>();
    entitySets.add(getEntitySet(ENTITY_CONTAINER_1, ENTITY_SET_1_1));
    entitySets.add(getEntitySet(ENTITY_CONTAINER_1, ENTITY_SET_1_2));
    entitySets.add(getEntitySet(ENTITY_CONTAINER_1, ENTITY_SET_1_3));
    entitySets.add(getEntitySet(ENTITY_CONTAINER_1, ENTITY_SET_1_4));
    entitySets.add(getEntitySet(ENTITY_CONTAINER_1, ENTITY_SET_1_5));
    entityContainer.setEntitySets(entitySets);

    final List<AssociationSet> associationSets = new ArrayList<AssociationSet>();
    associationSets.add(getAssociationSet(ENTITY_CONTAINER_1, ASSOCIATION_1_1, ENTITY_SET_1_4, ROLE_1_4));
    associationSets.add(getAssociationSet(ENTITY_CONTAINER_1, ASSOCIATION_1_2, ENTITY_SET_1_2, ROLE_1_2));
    associationSets.add(getAssociationSet(ENTITY_CONTAINER_1, ASSOCIATION_1_3, ENTITY_SET_1_3, ROLE_1_3));
    associationSets.add(getAssociationSet(ENTITY_CONTAINER_1, ASSOCIATION_1_4, ENTITY_SET_1_5, ROLE_1_5));
    entityContainer.setAssociationSets(associationSets);

    final List<FunctionImport> functionImports = new ArrayList<FunctionImport>();
    functionImports.add(getFunctionImport(ENTITY_CONTAINER_1, FUNCTION_IMPORT_1));
    functionImports.add(getFunctionImport(ENTITY_CONTAINER_1, FUNCTION_IMPORT_2));
    functionImports.add(getFunctionImport(ENTITY_CONTAINER_1, FUNCTION_IMPORT_3));
    functionImports.add(getFunctionImport(ENTITY_CONTAINER_1, FUNCTION_IMPORT_4));
    functionImports.add(getFunctionImport(ENTITY_CONTAINER_1, FUNCTION_IMPORT_5));
    functionImports.add(getFunctionImport(ENTITY_CONTAINER_1, FUNCTION_IMPORT_6));
    functionImports.add(getFunctionImport(ENTITY_CONTAINER_1, FUNCTION_IMPORT_7));
    entityContainer.setFunctionImports(functionImports);

    schema.setEntityContainers(Arrays.asList(entityContainer));

    final List<AnnotationElement> childElements = new ArrayList<AnnotationElement>();
    childElements.add(new AnnotationElement().setName("schemaElementTest2").setText("text2").setNamespace(NAMESPACE_1));
    childElements.add(new AnnotationElement().setName("schemaElementTest3").setText("text3").setPrefix("prefix")
        .setNamespace("namespace"));
    final List<AnnotationAttribute> elementAttributes = new ArrayList<AnnotationAttribute>();
    elementAttributes.add(new AnnotationAttribute().setName("rel").setText("self"));
    elementAttributes.add(new AnnotationAttribute().setName("href").setText("http://foo").setPrefix("pre")
        .setNamespace("namespaceForAnno"));
    childElements.add(new AnnotationElement().setName("schemaElementTest4").setText("text4").setAttributes(
        elementAttributes));

    final List<AnnotationElement> schemaElements = new ArrayList<AnnotationElement>();
    schemaElements.add(new AnnotationElement().setName("schemaElementTest1").setText("text1").setChildElements(
        childElements));

    schema.setAnnotationElements(schemaElements);
    schemas.add(schema);

    schema = new Schema();
    schema.setNamespace(NAMESPACE_2);

    schema.setEntityTypes(Arrays.asList(getEntityType(ENTITY_TYPE_2_1)));

    entityContainer = new EntityContainer();
    entityContainer.setName(ENTITY_CONTAINER_2);
    entityContainer.setEntitySets(Arrays.asList(getEntitySet(ENTITY_CONTAINER_2, ENTITY_SET_2_1)));
    schema.setEntityContainers(Arrays.asList(entityContainer));

    schemas.add(schema);

    return schemas;
  }

  @Override
  public EntityType getEntityType(final FullQualifiedName edmFQName) throws ODataException {
    if (NAMESPACE_1.equals(edmFQName.getNamespace())) {
      if (ENTITY_TYPE_1_1.getName().equals(edmFQName.getName())) {
        final List<Property> properties = new ArrayList<Property>();
        final ArrayList<AnnotationAttribute> annoList = new ArrayList<AnnotationAttribute>();
        annoList.add(new AnnotationAttribute().setName("annoName").setNamespace("http://annoNamespace").setPrefix(
            "annoPrefix").setText("annoText"));
        annoList.add(new AnnotationAttribute().setName("annoName2").setNamespace("http://annoNamespace").setPrefix(
            "annoPrefix").setText("annoText2"));
        properties.add(new SimpleProperty().setName("EmployeeId").setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(false))
            .setMapping(new Mapping().setInternalName("getId")).setAnnotationAttributes(annoList));
        final ArrayList<AnnotationAttribute> annoList2 = new ArrayList<AnnotationAttribute>();
        annoList2.add(new AnnotationAttribute().setName("annoName").setNamespace("http://annoNamespace").setPrefix(
            "annoPrefix").setText("annoText"));

        final List<AnnotationElement> annoElementsForSimpleProp = new ArrayList<AnnotationElement>();
        annoElementsForSimpleProp.add(new AnnotationElement().setName("propertyAnnoElement").setText("text"));
        annoElementsForSimpleProp.add(new AnnotationElement().setName("propertyAnnoElement2"));
        final SimpleProperty simpleProp =
            new SimpleProperty().setName("EmployeeName").setType(EdmSimpleTypeKind.String)
                .setCustomizableFeedMappings(new CustomizableFeedMappings()
                    .setFcTargetPath(EdmTargetPath.SYNDICATION_TITLE)).setAnnotationAttributes(annoList2)
                .setAnnotationElements(annoElementsForSimpleProp);

        properties.add(simpleProp);
        final ArrayList<AnnotationAttribute> annoList3 = new ArrayList<AnnotationAttribute>();
        annoList3.add(new AnnotationAttribute().setName("annoName").setNamespace("http://annoNamespaceNew").setPrefix(
            "annoPrefix").setText("annoTextNew"));
        properties.add(new SimpleProperty().setName("ManagerId").setType(EdmSimpleTypeKind.String)
            .setMapping(new Mapping().setInternalName("getManager.getId")).setAnnotationAttributes(annoList3));
        properties.add(new SimpleProperty().setName("RoomId").setType(EdmSimpleTypeKind.String)
            .setMapping(new Mapping().setInternalName("getRoom.getId")));
        properties.add(new SimpleProperty().setName("TeamId").setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setMaxLength(2))
            .setMapping(new Mapping().setInternalName("getTeam.getId")));
        properties.add(new ComplexProperty().setName("Location").setType(COMPLEX_TYPE_1));
        properties.add(new SimpleProperty().setName("Age").setType(EdmSimpleTypeKind.Int16));
        properties.add(new SimpleProperty().setName("EntryDate").setType(EdmSimpleTypeKind.DateTime)
            .setFacets(new Facets().setNullable(true))
            .setCustomizableFeedMappings(
                new CustomizableFeedMappings().setFcTargetPath(EdmTargetPath.SYNDICATION_UPDATED)));
        properties.add(new SimpleProperty().setName("ImageUrl").setType(EdmSimpleTypeKind.String)
            .setMapping(new Mapping().setInternalName("getImageUri")));
        final List<NavigationProperty> navigationProperties = new ArrayList<NavigationProperty>();
        navigationProperties.add(new NavigationProperty().setName("ne_Manager")
            .setRelationship(ASSOCIATION_1_1).setFromRole(ROLE_1_1).setToRole(ROLE_1_4));
        navigationProperties.add(new NavigationProperty().setName("ne_Team")
            .setRelationship(ASSOCIATION_1_2).setFromRole(ROLE_1_1).setToRole(ROLE_1_2));
        navigationProperties.add(new NavigationProperty().setName("ne_Room")
            .setRelationship(ASSOCIATION_1_3).setFromRole(ROLE_1_1).setToRole(ROLE_1_3));
        return new EntityType().setName(ENTITY_TYPE_1_1.getName())
            .setProperties(properties)
            .setHasStream(true)
            .setKey(getKey("EmployeeId"))
            .setNavigationProperties(navigationProperties)
            .setMapping(new Mapping().setMediaResourceMimeTypeKey("getImageType"));

      } else if (ENTITY_TYPE_1_BASE.getName().equals(edmFQName.getName())) {
        final List<Property> properties = new ArrayList<Property>();
        properties.add(new SimpleProperty().setName("Id").setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(false).setDefaultValue("1")));
        properties.add(new SimpleProperty().setName("Name").setType(EdmSimpleTypeKind.String)
            .setCustomizableFeedMappings(
                new CustomizableFeedMappings().setFcTargetPath(EdmTargetPath.SYNDICATION_TITLE)));
        return new EntityType().setName(ENTITY_TYPE_1_BASE.getName())
            .setAbstract(true)
            .setProperties(properties)
            .setKey(getKey("Id"));

      } else if (ENTITY_TYPE_1_2.getName().equals(edmFQName.getName())) {
        final List<Property> properties = new ArrayList<Property>();
        properties.add(new SimpleProperty().setName("isScrumTeam").setType(EdmSimpleTypeKind.Boolean)
            .setFacets(new Facets().setNullable(true))
            .setMapping(new Mapping().setInternalName("isScrumTeam")));
        final List<NavigationProperty> navigationProperties = new ArrayList<NavigationProperty>();
        navigationProperties.add(new NavigationProperty().setName("nt_Employees")
            .setRelationship(ASSOCIATION_1_2).setFromRole(ROLE_1_2).setToRole(ROLE_1_1));
        return new EntityType().setName(ENTITY_TYPE_1_2.getName())
            .setBaseType(ENTITY_TYPE_1_BASE)
            .setProperties(properties)
            .setNavigationProperties(navigationProperties);

      } else if (ENTITY_TYPE_1_3.getName().equals(edmFQName.getName())) {
        final List<Property> properties = new ArrayList<Property>();
        properties.add(new SimpleProperty().setName("Seats").setType(EdmSimpleTypeKind.Int16));
        properties.add(new SimpleProperty().setName("Version").setType(EdmSimpleTypeKind.Int16)
            .setFacets(new Facets().setConcurrencyMode(EdmConcurrencyMode.Fixed)));
        final List<NavigationProperty> navigationProperties = new ArrayList<NavigationProperty>();
        navigationProperties.add(new NavigationProperty().setName("nr_Employees")
            .setRelationship(ASSOCIATION_1_3).setFromRole(ROLE_1_3).setToRole(ROLE_1_1));
        navigationProperties.add(new NavigationProperty().setName("nr_Building")
            .setRelationship(ASSOCIATION_1_4).setFromRole(ROLE_1_3).setToRole(ROLE_1_5));
        return new EntityType().setName(ENTITY_TYPE_1_3.getName())
            .setBaseType(ENTITY_TYPE_1_BASE)
            .setProperties(properties)
            .setNavigationProperties(navigationProperties);

      } else if (ENTITY_TYPE_1_4.getName().equals(edmFQName.getName())) {
        final List<NavigationProperty> navigationProperties = new ArrayList<NavigationProperty>();
        navigationProperties.add(new NavigationProperty().setName("nm_Employees")
            .setRelationship(ASSOCIATION_1_1).setFromRole(ROLE_1_4).setToRole(ROLE_1_1));
        return new EntityType().setName(ENTITY_TYPE_1_4.getName())
            .setBaseType(ENTITY_TYPE_1_1)
            .setHasStream(true)
            .setNavigationProperties(navigationProperties)
            .setMapping(new Mapping().setMediaResourceMimeTypeKey("getImageType"));

      } else if (ENTITY_TYPE_1_5.getName().equals(edmFQName.getName())) {
        final List<Property> properties = new ArrayList<Property>();
        properties.add(new SimpleProperty().setName("Id").setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(false)));
        properties.add(new SimpleProperty().setName("Name").setType(EdmSimpleTypeKind.String)
            .setCustomizableFeedMappings(new CustomizableFeedMappings()
                .setFcTargetPath(EdmTargetPath.SYNDICATION_AUTHORNAME)));
        properties.add(new SimpleProperty().setName("Image").setType(EdmSimpleTypeKind.Binary));
        final List<NavigationProperty> navigationProperties = new ArrayList<NavigationProperty>();
        navigationProperties.add(new NavigationProperty().setName("nb_Rooms")
            .setRelationship(ASSOCIATION_1_4).setFromRole(ROLE_1_5).setToRole(ROLE_1_3));
        return new EntityType().setName(ENTITY_TYPE_1_5.getName())
            .setProperties(properties)
            .setKey(getKey("Id"))
            .setNavigationProperties(navigationProperties);
      }

    } else if (NAMESPACE_2.equals(edmFQName.getNamespace())) {
      if (ENTITY_TYPE_2_1.getName().equals(edmFQName.getName())) {
        final List<Property> properties = new ArrayList<Property>();
        properties.add(new SimpleProperty().setName("Id").setType(EdmSimpleTypeKind.Int32)
            .setFacets(new Facets().setNullable(false).setConcurrencyMode(EdmConcurrencyMode.Fixed)));
        properties.add(new SimpleProperty().setName("Name").setType(EdmSimpleTypeKind.String)
            .setCustomizableFeedMappings(
                new CustomizableFeedMappings().setFcTargetPath(EdmTargetPath.SYNDICATION_TITLE)));
        properties.add(new SimpleProperty().setName("Type").setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(false)));
        properties.add(new SimpleProperty().setName("ImageUrl").setType(EdmSimpleTypeKind.String)
            .setCustomizableFeedMappings(
                new CustomizableFeedMappings().setFcTargetPath(EdmTargetPath.SYNDICATION_AUTHORURI))
            .setMapping(new Mapping().setInternalName("getImageUri")));
        properties.add(new SimpleProperty().setName("Image").setType(EdmSimpleTypeKind.Binary)
            .setMapping(new Mapping().setMediaResourceMimeTypeKey("getType")));
        properties.add(new SimpleProperty().setName("BinaryData").setType(EdmSimpleTypeKind.Binary)
            .setFacets(new Facets().setNullable(true))
            .setMimeType("image/jpeg"));
        properties.add(new SimpleProperty().setName("Содержание").setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(true))
            .setCustomizableFeedMappings(new CustomizableFeedMappings()
                .setFcKeepInContent(false)
                .setFcNsPrefix("ру") // CYRILLIC SMALL LETTER ER + CYRILLIC SMALL LETTER U
                .setFcNsUri("http://localhost")
                .setFcTargetPath("Содержание"))
            .setMapping(new Mapping().setInternalName("getContent")));
        return new EntityType().setName(ENTITY_TYPE_2_1.getName())
            .setProperties(properties)
            .setHasStream(true)
            .setKey(getKey("Id", "Type"))
            .setMapping(new Mapping().setMediaResourceMimeTypeKey("getType"));
      }
    }

    return null;
  }

  @Override
  public ComplexType getComplexType(final FullQualifiedName edmFQName) throws ODataException {
    if (NAMESPACE_1.equals(edmFQName.getNamespace())) {
      if (COMPLEX_TYPE_1.getName().equals(edmFQName.getName())) {
        final List<Property> properties = new ArrayList<Property>();
        properties.add(new ComplexProperty().setName("City").setType(COMPLEX_TYPE_2));
        properties.add(new SimpleProperty().setName("Country").setType(EdmSimpleTypeKind.String));
        return new ComplexType().setName(COMPLEX_TYPE_1.getName()).setProperties(properties);

      } else if (COMPLEX_TYPE_2.getName().equals(edmFQName.getName())) {
        final List<Property> properties = new ArrayList<Property>();
        properties.add(new SimpleProperty().setName("PostalCode").setType(EdmSimpleTypeKind.String));
        properties.add(new SimpleProperty().setName("CityName").setType(EdmSimpleTypeKind.String));
        return new ComplexType().setName(COMPLEX_TYPE_2.getName()).setProperties(properties);
      }
    }

    return null;
  }

  @Override
  public Association getAssociation(final FullQualifiedName edmFQName) throws ODataException {
    if (NAMESPACE_1.equals(edmFQName.getNamespace())) {
      if (ASSOCIATION_1_1.getName().equals(edmFQName.getName())) {
        return new Association().setName(ASSOCIATION_1_1.getName())
            .setEnd1(
                new AssociationEnd().setType(ENTITY_TYPE_1_1).setRole(ROLE_1_1).setMultiplicity(EdmMultiplicity.MANY))
            .setEnd2(
                new AssociationEnd().setType(ENTITY_TYPE_1_4).setRole(ROLE_1_4).setMultiplicity(EdmMultiplicity.ONE));
      } else if (ASSOCIATION_1_2.getName().equals(edmFQName.getName())) {
        return new Association().setName(ASSOCIATION_1_2.getName())
            .setEnd1(
                new AssociationEnd().setType(ENTITY_TYPE_1_1).setRole(ROLE_1_1).setMultiplicity(EdmMultiplicity.MANY))
            .setEnd2(
                new AssociationEnd().setType(ENTITY_TYPE_1_2).setRole(ROLE_1_2).setMultiplicity(EdmMultiplicity.ONE));
      } else if (ASSOCIATION_1_3.getName().equals(edmFQName.getName())) {
        return new Association().setName(ASSOCIATION_1_3.getName())
            .setEnd1(
                new AssociationEnd().setType(ENTITY_TYPE_1_1).setRole(ROLE_1_1).setMultiplicity(EdmMultiplicity.MANY))
            .setEnd2(
                new AssociationEnd().setType(ENTITY_TYPE_1_3).setRole(ROLE_1_3).setMultiplicity(EdmMultiplicity.ONE));
      } else if (ASSOCIATION_1_4.getName().equals(edmFQName.getName())) {
        final List<PropertyRef> propertyRefsPrincipal = new ArrayList<PropertyRef>();
        propertyRefsPrincipal.add(new PropertyRef().setName("Id"));
        propertyRefsPrincipal.add(new PropertyRef().setName("Id2"));
        final List<PropertyRef> propertyRefsDependent = new ArrayList<PropertyRef>();
        propertyRefsDependent.add(new PropertyRef().setName("Id"));
        propertyRefsDependent.add(new PropertyRef().setName("Id2"));
        return new Association().setName(ASSOCIATION_1_4.getName())
            .setEnd1(
                new AssociationEnd().setType(ENTITY_TYPE_1_5).setRole(ROLE_1_5).setMultiplicity(EdmMultiplicity.ONE))
            .setEnd2(
                new AssociationEnd().setType(ENTITY_TYPE_1_3).setRole(ROLE_1_3).setMultiplicity(EdmMultiplicity.MANY))
            .setReferentialConstraint(
                new ReferentialConstraint().setPrincipal(
                    new ReferentialConstraintRole().setRole("BuildingToRoom").setPropertyRefs(propertyRefsPrincipal))
                    .setDependent(
                        new ReferentialConstraintRole().setRole("RoomToBuilding")
                            .setPropertyRefs(propertyRefsDependent)));
      }
    }
    return null;
  }

  @Override
  public EntityContainerInfo getEntityContainerInfo(final String name) throws ODataException {
    if ((name == null) || ENTITY_CONTAINER_1.equals(name)) {
      return new EntityContainerInfo().setName(ENTITY_CONTAINER_1).setDefaultEntityContainer(true);
    } else if (ENTITY_CONTAINER_2.equals(name)) {
      return new EntityContainerInfo().setName(name).setDefaultEntityContainer(false);
    }

    return null;
  }

  @Override
  public EntitySet getEntitySet(final String entityContainer, final String name) throws ODataException {
    if (ENTITY_CONTAINER_1.equals(entityContainer)) {
      if (ENTITY_SET_1_1.equals(name)) {
        return new EntitySet().setName(name).setEntityType(ENTITY_TYPE_1_1);
      } else if (ENTITY_SET_1_2.equals(name)) {
        return new EntitySet().setName(name).setEntityType(ENTITY_TYPE_1_2);
      } else if (ENTITY_SET_1_3.equals(name)) {
        return new EntitySet().setName(name).setEntityType(ENTITY_TYPE_1_3);
      } else if (ENTITY_SET_1_4.equals(name)) {
        return new EntitySet().setName(name).setEntityType(ENTITY_TYPE_1_4);
      } else if (ENTITY_SET_1_5.equals(name)) {
        return new EntitySet().setName(name).setEntityType(ENTITY_TYPE_1_5);
      }

    } else if (ENTITY_CONTAINER_2.equals(entityContainer)) {
      if (ENTITY_SET_2_1.equals(name)) {
        return new EntitySet().setName(name).setEntityType(ENTITY_TYPE_2_1);
      }
    }

    return null;
  }

  @Override
  public FunctionImport getFunctionImport(final String entityContainer, final String name) throws ODataException {
    if (ENTITY_CONTAINER_1.equals(entityContainer)) {
      if (FUNCTION_IMPORT_1.equals(name)) {
        final List<FunctionImportParameter> parameters = new ArrayList<FunctionImportParameter>();
        parameters.add(new FunctionImportParameter().setName("q").setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(true)));
        return new FunctionImport().setName(name)
            .setReturnType(new ReturnType().setTypeName(ENTITY_TYPE_1_1).setMultiplicity(EdmMultiplicity.MANY))
            .setEntitySet(ENTITY_SET_1_1)
            .setHttpMethod("GET")
            .setParameters(parameters);

      } else if (FUNCTION_IMPORT_2.equals(name)) {
        return new FunctionImport().setName(name)
            .setReturnType(new ReturnType().setTypeName(COMPLEX_TYPE_1).setMultiplicity(EdmMultiplicity.MANY))
            .setHttpMethod("GET");

      } else if (FUNCTION_IMPORT_3.equals(name)) {
        return new FunctionImport().setName(name)
            .setReturnType(
                new ReturnType().setTypeName(EdmSimpleTypeKind.String.getFullQualifiedName()).setMultiplicity(
                    EdmMultiplicity.MANY))
            .setHttpMethod("GET");

      } else if (FUNCTION_IMPORT_4.equals(name)) {
        return new FunctionImport().setName(name)
            .setReturnType(
                new ReturnType().setTypeName(EdmSimpleTypeKind.Int16.getFullQualifiedName()).setMultiplicity(
                    EdmMultiplicity.ONE))
            .setHttpMethod("GET");

      } else if (FUNCTION_IMPORT_5.equals(name)) {
        return new FunctionImport().setName(name)
            .setReturnType(new ReturnType().setTypeName(COMPLEX_TYPE_1).setMultiplicity(EdmMultiplicity.ONE))
            .setHttpMethod("GET");

      } else if (FUNCTION_IMPORT_6.equals(name)) {
        final List<FunctionImportParameter> parameters = new ArrayList<FunctionImportParameter>();
        parameters.add(new FunctionImportParameter().setName("Id").setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(false)));
        return new FunctionImport().setName(name)
            .setReturnType(
                new ReturnType().setTypeName(EdmSimpleTypeKind.Binary.getFullQualifiedName()).setMultiplicity(
                    EdmMultiplicity.ONE))
            .setHttpMethod("GET")
            .setParameters(parameters);

      } else if (FUNCTION_IMPORT_7.equals(name)) {
        return new FunctionImport().setName(name)
            .setReturnType(
                new ReturnType().setTypeName(new FullQualifiedName(NAMESPACE_1, "Employee")).setMultiplicity(
                    EdmMultiplicity.ZERO_TO_ONE))
            .setEntitySet(ENTITY_SET_1_1)
            .setHttpMethod("GET");

      }
    }

    return null;
  }

  @Override
  public AssociationSet getAssociationSet(final String entityContainer, final FullQualifiedName association,
      final String sourceEntitySetName, final String sourceEntitySetRole) throws ODataException {
    if (ENTITY_CONTAINER_1.equals(entityContainer)) {
      if (ASSOCIATION_1_1.equals(association)) {
        return new AssociationSet().setName(ASSOCIATION_1_1.getName())
            .setAssociation(ASSOCIATION_1_1)
            .setEnd1(new AssociationSetEnd().setRole(ROLE_1_4).setEntitySet(ENTITY_SET_1_4))
            .setEnd2(new AssociationSetEnd().setRole(ROLE_1_1).setEntitySet(ENTITY_SET_1_1));
      } else if (ASSOCIATION_1_2.equals(association)) {
        return new AssociationSet().setName(ASSOCIATION_1_2.getName())
            .setAssociation(ASSOCIATION_1_2)
            .setEnd1(new AssociationSetEnd().setRole(ROLE_1_2).setEntitySet(ENTITY_SET_1_2))
            .setEnd2(new AssociationSetEnd().setRole(ROLE_1_1).setEntitySet(ENTITY_SET_1_1));
      } else if (ASSOCIATION_1_3.equals(association)) {
        return new AssociationSet().setName(ASSOCIATION_1_3.getName())
            .setAssociation(ASSOCIATION_1_3)
            .setEnd1(new AssociationSetEnd().setRole(ROLE_1_3).setEntitySet(ENTITY_SET_1_3))
            .setEnd2(new AssociationSetEnd().setRole(ROLE_1_1).setEntitySet(ENTITY_SET_1_1));
      } else if (ASSOCIATION_1_4.equals(association)) {
        return new AssociationSet().setName(ASSOCIATION_1_4.getName())
            .setAssociation(ASSOCIATION_1_4)
            .setEnd1(new AssociationSetEnd().setRole(ROLE_1_5).setEntitySet(ENTITY_SET_1_5))
            .setEnd2(new AssociationSetEnd().setRole(ROLE_1_3).setEntitySet(ENTITY_SET_1_3));
      }
    }

    return null;
  }

  private Key getKey(final String... keyNames) {
    final List<PropertyRef> keyProperties = new ArrayList<PropertyRef>();
    for (final String keyName : keyNames) {
      keyProperties.add(new PropertyRef().setName(keyName));
    }
    return new Key().setKeys(keyProperties);
  }
}
