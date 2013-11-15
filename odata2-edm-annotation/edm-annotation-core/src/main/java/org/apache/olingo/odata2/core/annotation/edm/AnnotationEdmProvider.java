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
package org.apache.olingo.odata2.core.annotation.edm;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.annotation.edm.EdmComplexType;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmMediaResourceContent;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.annotation.edm.NavigationEnd;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.AnnotationAttribute;
import org.apache.olingo.odata2.api.edm.provider.AnnotationElement;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.AssociationSetEnd;
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.api.edm.provider.Using;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 * Provider for the entity data model used in the reference scenario
 *
 */
public class AnnotationEdmProvider extends EdmProvider {

  private static final AnnotationHelper ANNOTATION_HELPER = new AnnotationHelper();

  private final List<Class<?>> annotatedClasses;
  private final Map<String, EntityContainer> name2Container = new HashMap<String, EntityContainer>();
  private final Map<String, ContainerBuilder> containerName2ContainerBuilder = new HashMap<String, ContainerBuilder>();
  private final Map<String, Schema> namespace2Schema = new HashMap<String, Schema>();
  private EntityContainer defaultContainer;
  private String DEFAULT_CONTAINER_NAME = "DefaultContainer";

  public AnnotationEdmProvider(Collection<Class<?>> annotatedClasses) {

    this.annotatedClasses = new ArrayList<Class<?>>(annotatedClasses.size());
    for (Class<?> aClass : annotatedClasses) {
      if (ANNOTATION_HELPER.isEdmAnnotated(aClass)) {
        this.annotatedClasses.add(aClass);
      }
    }

    init();
  }

  public AnnotationEdmProvider(String packageToScan) {
    this.annotatedClasses = ClassHelper.loadClasses(packageToScan, new ClassHelper.ClassValidator() {
      @Override
      public boolean isClassValid(Class<?> c) {
        return ANNOTATION_HELPER.isEdmAnnotated(c);
      }
    });
    
    init();
  }

  private void init() {
    for (Class<?> aClass : annotatedClasses) {
      updateSchema(aClass);
      handleEntityContainer(aClass);
    }

    finish();
  }

  @Override
  public Association getAssociation(FullQualifiedName edmFQName) throws ODataException {
    Schema schema = namespace2Schema.get(edmFQName.getNamespace());
    if (schema != null) {
      List<Association> associations = schema.getAssociations();
      for (Association association : associations) {
        if (association.getName().equals(edmFQName.getName())) {
          return association;
        }
      }
    }
    return null;
  }

  @Override
  public AssociationSet getAssociationSet(String entityContainer, FullQualifiedName association,
          String sourceEntitySetName, String sourceEntitySetRole) throws ODataException {
    EntityContainer container = name2Container.get(entityContainer);
    if (container != null) {
      List<AssociationSet> associations = container.getAssociationSets();
      for (AssociationSet associationSet : associations) {
        if (associationSet.getAssociation().equals(association)) {
          final AssociationSetEnd endOne = associationSet.getEnd1();
          if (endOne.getRole().equals(sourceEntitySetRole)
                  && endOne.getEntitySet().equals(sourceEntitySetName)) {
            return associationSet;
          }
          final AssociationSetEnd endTwo = associationSet.getEnd2();
          if (endTwo.getRole().equals(sourceEntitySetRole)
                  && endTwo.getEntitySet().equals(sourceEntitySetName)) {
            return associationSet;
          }
        }
      }
    }
    return null;
  }

  @Override
  public ComplexType getComplexType(FullQualifiedName edmFQName) throws ODataException {
    Schema schema = namespace2Schema.get(edmFQName.getNamespace());
    if (schema != null) {
      List<ComplexType> complexTypes = schema.getComplexTypes();
      for (ComplexType complexType : complexTypes) {
        if (complexType.getName().equals(edmFQName.getName())) {
          return complexType;
        }
      }
    }
    return null;
  }

  @Override
  public EntityContainerInfo getEntityContainerInfo(String name) throws ODataException {
    EntityContainer container = name2Container.get(name);
    if (container == null) {
      // use default container (if set)
      container = defaultContainer;
    }
    if (container != null) {
      EntityContainerInfo info = new EntityContainerInfo();
      info.setName(container.getName());
      info.setDefaultEntityContainer(container.isDefaultEntityContainer());
      info.setExtendz(container.getExtendz());
      info.setAnnotationAttributes(container.getAnnotationAttributes());
      info.setAnnotationElements(container.getAnnotationElements());

      return info;
    }

    return null;
  }

  @Override
  public EntitySet getEntitySet(String entityContainer, String name) throws ODataException {
    EntityContainer container = name2Container.get(entityContainer);
    if (container != null) {
      List<EntitySet> entitySets = container.getEntitySets();
      for (EntitySet entitySet : entitySets) {
        if (entitySet.getName().equals(name)) {
          return entitySet;
        }
      }
    }

    return null;
  }

  @Override
  public EntityType getEntityType(FullQualifiedName edmFQName) throws ODataException {
    Schema schema = namespace2Schema.get(edmFQName.getNamespace());
    if (schema != null) {
      List<EntityType> complexTypes = schema.getEntityTypes();
      for (EntityType complexType : complexTypes) {
        if (complexType.getName().equals(edmFQName.getName())) {
          return complexType;
        }
      }
    }
    return null;
  }

  @Override
  public FunctionImport getFunctionImport(String entityContainer, String name) throws ODataException {
    EntityContainer container = name2Container.get(entityContainer);
    if (container != null) {
      List<FunctionImport> functionImports = container.getFunctionImports();
      for (FunctionImport functionImport : functionImports) {
        if (functionImport.getName().equals(name)) {
          return functionImport;
        }
      }
    }
    return null;
  }

  @Override
  public List<Schema> getSchemas() throws ODataException {
    return new ArrayList<Schema>(namespace2Schema.values());
  }

  //
  //
  //
  private Map<String, SchemaBuilder> namespace2SchemaBuilder = new HashMap<String, SchemaBuilder>();

  private void updateSchema(Class<?> aClass) {
    EdmEntityType et = aClass.getAnnotation(EdmEntityType.class);
    if (et != null) {
      updateSchema(aClass, et);
    }
    EdmComplexType ect = aClass.getAnnotation(EdmComplexType.class);
    if (ect != null) {
      updateSchema(aClass, ect);
    }
  }

  private void updateSchema(Class<?> aClass, EdmEntityType et) {
    String namespace = et.namespace();
    SchemaBuilder b = namespace2SchemaBuilder.get(namespace);
    if (b == null) {
      b = SchemaBuilder.init(namespace);
      namespace2SchemaBuilder.put(namespace, b);
    }
    TypeBuilder typeBuilder = TypeBuilder.init(et, aClass);
    b.addEntityType(typeBuilder.buildEntityType());
    b.addAssociations(typeBuilder.buildAssociations());
  }

  private void updateSchema(Class<?> aClass, EdmComplexType et) {
    String namespace = et.namespace();
    SchemaBuilder b = namespace2SchemaBuilder.get(namespace);
    if (b == null) {
      b = SchemaBuilder.init(namespace);
      namespace2SchemaBuilder.put(namespace, b);
    }
    TypeBuilder typeBuilder = TypeBuilder.init(et, aClass);
    b.addComplexType(typeBuilder.buildComplexType());
  }

  private void handleEntityContainer(Class<?> aClass) {
    EdmEntityType entityType = aClass.getAnnotation(EdmEntityType.class);
    if (entityType != null) {
      String containerName = getCanonicalContainerName(entityType);
      ContainerBuilder builder = containerName2ContainerBuilder.get(containerName);
      if (builder == null) {
        builder = ContainerBuilder.init(entityType.namespace(), containerName);
        containerName2ContainerBuilder.put(containerName, builder);
      }
      EdmEntitySet entitySet = aClass.getAnnotation(EdmEntitySet.class);
      if(entitySet != null) {
        FullQualifiedName typeName = createFqnForEntityType(aClass, entityType);
        builder.addEntitySet(createEntitySet(typeName, entitySet));
      }
    }
  }

  private EntitySet createEntitySet(FullQualifiedName typeName, EdmEntitySet entitySet) {
    return new EntitySet().setName(entitySet.name()).setEntityType(typeName);
  }

  private FullQualifiedName createFqnForEntityType(Class<?> annotatedClass, EdmEntityType entityType) {
    String name = entityType.name();
    if(name.isEmpty()) {
      return new FullQualifiedName(entityType.namespace(), annotatedClass.getSimpleName());      
    } else {
      return new FullQualifiedName(entityType.namespace(), entityType.name());
    }
  }

  private void finish() {
    //
    Collection<ContainerBuilder> containers = containerName2ContainerBuilder.values();
    for (ContainerBuilder containerBuilder : containers) {
      SchemaBuilder schemaBuilder = namespace2SchemaBuilder.get(containerBuilder.getNamespace());
      containerBuilder.addAssociationSets(schemaBuilder.name2Associations.values());
      final EntityContainer container = containerBuilder.build();
      schemaBuilder.addEntityContainer(container);
      name2Container.put(container.getName(), container);
      if (container.isDefaultEntityContainer()) {
        defaultContainer = container;
      }
    }
    //
    Collection<SchemaBuilder> schemaBuilders = namespace2SchemaBuilder.values();
    for (SchemaBuilder schemaBuilder : schemaBuilders) {
      final Schema schema = schemaBuilder.build();
      namespace2Schema.put(schema.getNamespace(), schema);
    }
  }

  private String getCanonicalContainerName(EdmEntityType entity) {
    return DEFAULT_CONTAINER_NAME;
  }

  //
  //
  //
  static class TypeBuilder {

    final private String namespace;
    final private String name;
    private boolean isAbstract = false;
    private boolean isMediaResource = false;
    private FullQualifiedName baseEntityType = null;
    private final List<PropertyRef> keyProperties = new ArrayList<PropertyRef>();
    private final List<Property> properties = new ArrayList<Property>();
    private final List<NavigationProperty> navProperties = new ArrayList<NavigationProperty>();
    private final List<Association> associations = new ArrayList<Association>();

    public TypeBuilder(String namespace, String name) {
      this.namespace = namespace;
      this.name = name;
    }

    public static TypeBuilder init(EdmEntityType entity, Class<?> aClass) {
      return new TypeBuilder(entity.namespace(), entity.name()).withClass(aClass);
    }

    public static TypeBuilder init(EdmComplexType entity, Class<?> aClass) {
      return new TypeBuilder(entity.namespace(), entity.name()).withClass(aClass);
    }

    private TypeBuilder withClass(Class<?> aClass) {
      baseEntityType = createBaseEntityFqn(aClass);

      if (Modifier.isAbstract(aClass.getModifiers())) {
        this.isAbstract = true;
      }

      Field[] fields = aClass.getDeclaredFields();
      for (Field field : fields) {
        EdmProperty ep = field.getAnnotation(EdmProperty.class);
        if (ep != null) {
          properties.add(createProperty(ep, field, namespace));
          EdmKey eti = field.getAnnotation(EdmKey.class);
          if (eti != null) {
            keyProperties.add(createKeyProperty(ep, field));
          }
        }
        EdmNavigationProperty enp = field.getAnnotation(EdmNavigationProperty.class);
        if (enp != null) {
          final NavigationProperty navProperty = createNavigationProperty(namespace, enp, field);
          navProperties.add(navProperty);
          Association association = createAssociation(field, navProperty);
          associations.add(association);
        }
        EdmMediaResourceContent emrc = field.getAnnotation(EdmMediaResourceContent.class);
        if(emrc != null) {
          isMediaResource = true;
        }
      }

      return this;
    }

    public TypeBuilder addProperty(PropertyRef property) {
      keyProperties.add(property);
      return this;
    }

    public TypeBuilder addProperty(Property property) {
      properties.add(property);
      return this;
    }

    public TypeBuilder addNavigationProperty(NavigationProperty property) {
      navProperties.add(property);
      return this;
    }

    public TypeBuilder setAbstract(boolean isAbstract) {
      this.isAbstract = isAbstract;
      return this;
    }

    public ComplexType buildComplexType() {
      ComplexType complexType = new ComplexType();
      if (baseEntityType != null) {
        complexType.setBaseType(baseEntityType);
      }
      return complexType.setName(name)
              .setProperties(properties);
    }

    public EntityType buildEntityType() {
      EntityType entityType = new EntityType();
      if (baseEntityType != null) {
        entityType.setBaseType(baseEntityType);
      }
      if (!keyProperties.isEmpty()) {
        entityType.setKey(new Key().setKeys(keyProperties));
      }
      if (!navProperties.isEmpty()) {
        entityType.setNavigationProperties(navProperties);
      }
      return entityType.setName(name)
              .setAbstract(isAbstract)
              .setHasStream(isMediaResource)
              .setProperties(properties);
    }

    public Collection<Association> buildAssociations() {
      return Collections.unmodifiableCollection(associations);
    }

    private PropertyRef createKeyProperty(EdmProperty et, Field field) {
      PropertyRef keyProperty = new PropertyRef();
      String entityName = et.name();
      if (entityName.isEmpty()) {
        entityName = getCanonicalName(field);
      }
      return keyProperty.setName(entityName);
    }

    private Property createProperty(EdmProperty ep, Field field, String defaultNamespace) {
      if(isAnnotatedEntity(field.getType())) {
        return createComplexProperty(field, defaultNamespace);
      } else {
        return createSimpleProperty(ep, field);
      }
    }
    

    private Property createSimpleProperty(EdmProperty ep, Field field) {
      SimpleProperty sp = new SimpleProperty();
      String entityName = ANNOTATION_HELPER.getPropertyName(field);
      sp.setName(entityName);
      //
      EdmSimpleTypeKind type = ep.type();
      if (type == EdmSimpleTypeKind.Null) {
        type = getEdmSimpleType(field.getType());
      }
      sp.setType(type);

      return sp;
    }

    private Property createComplexProperty(Field field, String defaultNamespace) {
      ComplexProperty cp = new ComplexProperty();
      // settings from property
      String entityName = ANNOTATION_HELPER.getPropertyName(field);
      cp.setName(entityName);
      
      // settings from related complex entity
      EdmComplexType ece = field.getType().getAnnotation(EdmComplexType.class);
      String complexEntityNamespace = ece.namespace();
      if (complexEntityNamespace.isEmpty()) {
        complexEntityNamespace = defaultNamespace;
      }
      cp.setType(new FullQualifiedName(complexEntityNamespace, ece.name()));

      return cp;
    }

    private NavigationProperty createNavigationProperty(String namespace, EdmNavigationProperty enp, Field field) {
      NavigationProperty navProp = new NavigationProperty();
      String entityName = ANNOTATION_HELPER.getPropertyName(field);
      navProp.setName(entityName);
      //
      NavigationEnd from = enp.from();
      String fromRole = from.role();
      if (fromRole.isEmpty()) {
        fromRole = getCanonicalRole(from, field.getDeclaringClass());
      }
      navProp.setFromRole(fromRole);

      NavigationEnd to = enp.to();
      String toRole = to.role();
      if (toRole.isEmpty()) {
        toRole = getCanonicalRole(to, field.getType());
      }
      navProp.setToRole(toRole);

      String relationshipName = enp.association();
      if(relationshipName.isEmpty()) {
        if(fromRole.compareTo(toRole) > 0) {
          relationshipName = toRole + "-" + fromRole;
        } else {
          relationshipName = fromRole + "-" + toRole;
        }
      }
      navProp.setRelationship(new FullQualifiedName(namespace, relationshipName));

      return navProp;
    }

    private String getCanonicalRole(NavigationEnd navEnd, Class<?> fallbackClass) {
        String toRole = extractEntitTypeName(navEnd.entitySet());
        if(toRole == null) {
          toRole = extractEntitTypeName(fallbackClass);
        }
        return "r_" + toRole;
    }

    private EdmSimpleTypeKind getEdmSimpleType(Class<?> type) {
      if (type == String.class) {
        return EdmSimpleTypeKind.String;
      } else if (type == int.class) {
        return EdmSimpleTypeKind.Int32;
      } else if (type == Integer.class) {
        return EdmSimpleTypeKind.Int32;
      } else if (type == Byte[].class || type == byte[].class) {
        return EdmSimpleTypeKind.Binary;
      } else {
        throw new UnsupportedOperationException("Not yet supported type '" + type + "'."); 
      }
    }

    private EdmEntityType checkForBaseEntity(Class<?> aClass) {
      Class<?> superClass = aClass.getSuperclass();
      if (superClass == Object.class) {
        return null;
      } else {
        EdmEntityType edmEntity = superClass.getAnnotation(EdmEntityType.class);
        if (edmEntity == null) {
          return checkForBaseEntity(superClass);
        } else {
          return edmEntity;
        }
      }
    }

    private FullQualifiedName createBaseEntityFqn(Class<?> aClass) {
      EdmEntityType baseEntity = checkForBaseEntity(aClass);
      if (baseEntity == null) {
        return null;
      }
      String beName = baseEntity.name();
      if (beName.isEmpty()) {
        beName = aClass.getName();
      }
      return new FullQualifiedName(baseEntity.namespace(), beName);
    }

    private Association createAssociation(Field field, NavigationProperty navProperty) {
      Association association = new Association();
      EdmNavigationProperty navigation = field.getAnnotation(EdmNavigationProperty.class);

      NavigationEnd from = navigation.from();
      AssociationEnd fromEnd = new AssociationEnd();
      fromEnd.setRole(navProperty.getFromRole());
      String typeName = extractEntitTypeName(from);
      if (typeName.isEmpty()) {
        typeName = extractEntitTypeName(field.getDeclaringClass());
      }
      fromEnd.setType(new FullQualifiedName(namespace, typeName));
      fromEnd.setMultiplicity(from.multiplicity());
      association.setEnd1(fromEnd);

      NavigationEnd to = navigation.to();
      AssociationEnd toEnd = new AssociationEnd();
      toEnd.setRole(navProperty.getToRole());
      String toTypeName = extractEntitTypeName(to);
      if (toTypeName.isEmpty()) {
        toTypeName = extractEntitTypeName(field.getType());
      }
      toEnd.setType(new FullQualifiedName(namespace, toTypeName));

      EdmMultiplicity toMultiplicity = to.multiplicity();
      Class<?> toClass = field.getType();
      boolean isCollectionType = toClass.isArray() || Collection.class.isAssignableFrom(toClass);
      if (toMultiplicity == EdmMultiplicity.ONE && isCollectionType) {
        // XXX: magic, please check and or remove/refactore
        toEnd.setMultiplicity(EdmMultiplicity.MANY);
      } else {
        toEnd.setMultiplicity(toMultiplicity);
      }
      association.setEnd2(toEnd);

      String associationName = navProperty.getRelationship().getName();
      association.setName(associationName);
      return association;
    }

    private String getCanonicalName(Field field) {
      return ANNOTATION_HELPER.getCanonicalName(field);
    }
    
    private String getCanonicalName(Class<?> clazz) {
      return ANNOTATION_HELPER.getCanonicalName(clazz);
    }

    private boolean isAnnotatedEntity(Class<?> clazz) {
      boolean isComplexEntity = clazz.getAnnotation(EdmComplexType.class) != null;
      boolean isEntity = clazz.getAnnotation(EdmEntityType.class) != null;
      return isComplexEntity || isEntity;
    }

    private String extractEntitTypeName(NavigationEnd navEnd) {
      Class<?> entityTypeClass = navEnd.entitySet();
      if(entityTypeClass == Object.class) {
        return "";
      }
      EdmEntityType type = entityTypeClass.getAnnotation(EdmEntityType.class);
      if(type == null) {
        return "";
      }
      return type.name();
    }
    
    /**
     * Returns <code>NULL</code> if given class is not annotated.
     * If annotated the set entity type name is returned and if no name is set the
     * default name is generated from the simple class name.
     * 
     * @param annotatedClass
     * @return 
     */
    private String extractEntitTypeName(Class<?> annotatedClass) {
      if(annotatedClass == Object.class) {
        return null;
      }
      EdmEntityType type = annotatedClass.getAnnotation(EdmEntityType.class);
      if(type == null) {
        return null;
      }
      if(type.name().isEmpty()) {
        return getCanonicalName(annotatedClass);
      }
      return type.name();
    }
  }

  static class SchemaBuilder {

    final private String namespace;
//    private String alias;
    private final List<Using> usings = new ArrayList<Using>();
    private final List<EntityType> entityTypes = new ArrayList<EntityType>();
    private final List<ComplexType> complexTypes = new ArrayList<ComplexType>();
    private final Map<String, Association> name2Associations = new HashMap<String, Association>();
    private final List<EntityContainer> entityContainers = new ArrayList<EntityContainer>();
    private final List<AnnotationAttribute> annotationAttributes = new ArrayList<AnnotationAttribute>();
    private final List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();

    private SchemaBuilder(String namespace) {
      this.namespace = namespace;
    }

    public static SchemaBuilder init(String namespace) {
      return new SchemaBuilder(namespace);
    }

    public SchemaBuilder addEntityType(EntityType type) {
      entityTypes.add(type);
      return this;
    }

    public SchemaBuilder addEntityContainer(EntityContainer container) {
      entityContainers.add(container);
      return this;
    }

    public SchemaBuilder addComplexType(ComplexType createEntityType) {
      complexTypes.add(createEntityType);
      return this;
    }

    public void addAssociations(Collection<Association> associations) {
      for (Association association : associations) {
        final String relationshipName = association.getName();
        if (name2Associations.containsKey(relationshipName)) {
          association = mergeAssociations(name2Associations.get(relationshipName), association);
        }
        name2Associations.put(relationshipName, association);
      }
    }

    private Association mergeAssociations(Association associationOne, Association associationTwo) {
      AssociationEnd oneEnd1 = associationOne.getEnd1();
      AssociationEnd oneEnd2 = associationOne.getEnd2();
      AssociationEnd twoEnd1 = associationTwo.getEnd1();
      AssociationEnd twoEnd2 = associationTwo.getEnd2();
      AssociationEnd[] oneEnds = new AssociationEnd[]{oneEnd1, oneEnd2};

      for (AssociationEnd associationEnd : oneEnds) {
        if (associationEnd.getRole().equals(twoEnd1.getRole())) {
          if (twoEnd1.getMultiplicity() == EdmMultiplicity.MANY) {
            associationEnd.setMultiplicity(EdmMultiplicity.MANY);
          }
        } else if (associationEnd.getRole().equals(twoEnd2.getRole())) {
          if (twoEnd2.getMultiplicity() == EdmMultiplicity.MANY) {
            associationEnd.setMultiplicity(EdmMultiplicity.MANY);
          }
        }
      }

      return associationOne;
    }

    public Schema build() {
      Schema s = new Schema();
      s.setUsings(usings);
      s.setEntityTypes(entityTypes);
      s.setComplexTypes(complexTypes);
      s.setAssociations(new ArrayList<Association>(name2Associations.values()));
      s.setEntityContainers(entityContainers);
      s.setAnnotationAttributes(annotationAttributes);
      s.setAnnotationElements(annotationElements);
      s.setNamespace(namespace);
      return s;
    }
  }

  private static class ContainerBuilder {

    final private String name;
    final private String namespace;
    private boolean defaultContainer = true;
    private List<EntitySet> entitySets = new ArrayList<EntitySet>();
    private List<AssociationSet> associationSets = new ArrayList<AssociationSet>();
    private List<FunctionImport> functionImports = new ArrayList<FunctionImport>();
//    private Documentation documentation;

    private ContainerBuilder(String namespace, String containerName) {
      this.namespace = namespace;
      name = containerName;
    }

    public String getNamespace() {
      return namespace;
    }

    public static ContainerBuilder init(String namespace, String containerName) {
      return new ContainerBuilder(namespace, containerName);
    }

    public ContainerBuilder setDefaultContainer(boolean isDefault) {
      defaultContainer = isDefault;
      return this;
    }

    public ContainerBuilder addEntitySet(EntitySet entitySet) {
      entitySets.add(entitySet);
      return this;
    }
    
    public void addAssociationSets(Collection<Association> associations) {
      for (Association association : associations) {
        AssociationSet as = new AssociationSet();
        as.setName(association.getName());
        FullQualifiedName asAssociationFqn = new FullQualifiedName(namespace, association.getName());
        as.setAssociation(asAssociationFqn);

        AssociationSetEnd asEnd1 = new AssociationSetEnd();
        asEnd1.setEntitySet(getEntitySetName(association.getEnd1()));
        asEnd1.setRole(association.getEnd1().getRole());
        as.setEnd1(asEnd1);
        
        AssociationSetEnd asEnd2 = new AssociationSetEnd();
        asEnd2.setEntitySet(getEntitySetName(association.getEnd2()));
        asEnd2.setRole(association.getEnd2().getRole());
        as.setEnd2(asEnd2);
        
        associationSets.add(as);
      }
    }

    public EntityContainer build() {
      EntityContainer ec = new EntityContainer();
      ec.setName(name);
      ec.setDefaultEntityContainer(defaultContainer);
      ec.setEntitySets(entitySets);
      ec.setAssociationSets(associationSets);
      ec.setFunctionImports(functionImports);
      return ec;
    }

    
    private String getEntitySetName(AssociationEnd end) {
      for (EntitySet entitySet : entitySets) {
        if(entitySet.getEntityType().equals(end.getType())) {
          return entitySet.getName();
        }
      }
      throw new ODataRuntimeException("No entity set found for " + end.getType());
    }
  }
}
