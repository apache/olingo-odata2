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
package org.apache.olingo.odata2.client.core.ep.deserializer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAction;
import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationEnd;
import org.apache.olingo.odata2.api.edm.EdmAssociationSet;
import org.apache.olingo.odata2.api.edm.EdmAssociationSetEnd;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmConcurrencyMode;
import org.apache.olingo.odata2.api.edm.EdmContentKind;
import org.apache.olingo.odata2.api.edm.EdmCustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmParameter;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmReferentialConstraint;
import org.apache.olingo.odata2.api.edm.EdmReferentialConstraintRole;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.client.api.edm.EdmDataServices;
import org.apache.olingo.odata2.client.api.edm.EdmSchema;
import org.apache.olingo.odata2.client.api.edm.EdmUsing;
import org.apache.olingo.odata2.client.core.edm.EdmMetadataAssociationEnd;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAnnotationAttributeImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAnnotationElementImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAnnotationsImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAssociationEndImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAssociationImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAssociationSetEndImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAssociationSetImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmComplexPropertyImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmComplexTypeImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmCustomizableFeedMappingsImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmEntityContainerImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmEntitySetImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmEntityTypeImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmFunctionImportImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmFunctionImportParameter;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmKeyImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmNamedImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmNavigationPropertyImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmOnDeleteImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmParameterImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmPropertyImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmPropertyRefImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmReferentialConstraintImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmReferentialConstraintRoleImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmSchemaImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmSimplePropertyImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmTypedImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmUsingImpl;
import org.apache.olingo.odata2.core.commons.XmlHelper;
import org.apache.olingo.odata2.core.edm.EdmSimpleTypeFacadeImpl;
import org.apache.olingo.odata2.core.ep.util.XmlMetadataConstants;

/**
 * This class deserializes Metadata document
 *
 */
public class XmlMetadataDeserializer {

  private Map<String, Set<String>> inscopeMap = new HashMap<String, Set<String>>();
  private Map<String, String> aliasNamespaceMap = new HashMap<String, String>();
  private Map<String, String> xmlNamespaceMap;
  private Map<String, String> mandatoryNamespaces;
  private Map<FullQualifiedName, EdmEntityType> entityTypesMap = new HashMap<FullQualifiedName, EdmEntityType>();
  private Map<FullQualifiedName, EdmComplexType> complexTypesMap = new HashMap<FullQualifiedName, EdmComplexType>();
  private Map<FullQualifiedName, EdmProperty> complexPropertyMap = new HashMap<FullQualifiedName, EdmProperty>();
  private Map<FullQualifiedName, EdmAssociation> associationsMap = new HashMap<FullQualifiedName, EdmAssociation>();
  private Map<String, EdmAssociationSet> associationSetMap = new HashMap<String, EdmAssociationSet>();
  private Map<String, EdmAssociationSet> tempAssociationSetMap = new HashMap<String, EdmAssociationSet>();
   private Map<String, List<EdmAssociationSetEndImpl>> associationSetEndMap = 
      new HashMap<String, List<EdmAssociationSetEndImpl>>();
  private Map<FullQualifiedName, EdmEntityContainer> containerMap =
      new HashMap<FullQualifiedName, EdmEntityContainer>();
  //Map for base type relation
  private Map<FullQualifiedName, FullQualifiedName> entityBaseTypeMap = 
      new HashMap<FullQualifiedName, FullQualifiedName>();
  private Map<FullQualifiedName, FullQualifiedName> complexBaseTypeMap = 
      new HashMap<FullQualifiedName, FullQualifiedName>();
   private List<EdmFunctionImport> edmFunctionImportList = new ArrayList<EdmFunctionImport>();
  private List<EdmEntitySet> edmEntitySetList = new ArrayList<EdmEntitySet>();
  private List<EdmNavigationProperty> navProperties = new ArrayList<EdmNavigationProperty>();
  private String currentHandledStartTagName;
  private String currentNamespace;
  private String edmNamespace = Edm.NAMESPACE_EDM_2008_09;
  private Set<String> edmNamespaces;
  private EdmEntityContainer defaultEdmEntityContainer;

  /**
   * 
   * @param content
   * @param validate
   * @return EdmDataServices
   * @throws EntityProviderException
   * @throws EdmException
   */
  public EdmDataServices readMetadata(final InputStream content, final boolean validate)//NOSONAR
      throws EntityProviderException, EdmException { 
    try {
      initialize();
      EdmDataServices dataServices = new EdmDataServices();
      List<EdmSchema> schemas = new ArrayList<EdmSchema>();
      EdmImpl edm = new EdmImpl();
      XMLStreamReader reader = XmlHelper.createStreamReader(content);
      while (reader.hasNext()
          && !(reader.isEndElement() && Edm.NAMESPACE_EDMX_2007_06.equals(reader.getNamespaceURI())
              && XmlMetadataConstants.EDMX_TAG.equals(reader.getLocalName()))) {
        reader.next();
        if (reader.isStartElement()) {
          extractNamespaces(reader);
          if (XmlMetadataConstants.EDM_SCHEMA.equals(reader.getLocalName())) { //NOSONAR
            edmNamespace = reader.getNamespaceURI();
            checkEdmNamespace();
            schemas.add(readSchema(reader, edm));
          } else if (XmlMetadataConstants.EDM_DATA_SERVICES.equals(reader
              .getLocalName())) {
            dataServices.setDataServiceVersion(reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, "DataServiceVersion"));
          }
        }
      }

      if (!reader.isEndElement() || !XmlMetadataConstants.EDMX_TAG.equals(reader.getLocalName())) {
        throw new EntityProviderException(EntityProviderException.MISSING_TAG
            .addContent(XmlMetadataConstants.EDMX_TAG));
      }

      setBaseTypeForEntityType();
      setBaseTypeForComplexType();
      setDetailsForEntitySet();
      setAssociationSetForNavigations();
      if (validate) {
        validate();
      }
      edm.setEdmSchemas(schemas);
      dataServices.setClientEdm(edm);
      reader.close();
      return dataServices;
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }

  }

  private EdmSchema readSchema(final XMLStreamReader reader, EdmImpl edm) //NOSONAR
      throws XMLStreamException, EntityProviderException, EdmException { 
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_SCHEMA);
    final EdmSchemaImpl schemaImpl = new EdmSchemaImpl();
    final List<EdmUsing> usings = new ArrayList<EdmUsing>();
    final List<EdmComplexType> complexTypes = new ArrayList<EdmComplexType>();
    final List<EdmEntityType> entityTypes = new ArrayList<EdmEntityType>();
    final List<EdmAssociation> associations = new ArrayList<EdmAssociation>();
    final List<EdmEntityContainer> entityContainers = new ArrayList<EdmEntityContainer>();
    final List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    schemaImpl.setNamespace(reader.getAttributeValue(null, XmlMetadataConstants.EDM_SCHEMA_NAMESPACE));
    inscopeMap.put(schemaImpl.getNamespace(), new HashSet<String>());
    schemaImpl.setAlias(reader.getAttributeValue(null, XmlMetadataConstants.EDM_SCHEMA_ALIAS));
    schemaImpl.setAnnotationAttributes(readAnnotationAttribute(reader));
    currentNamespace = schemaImpl.getNamespace();
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
            && XmlMetadataConstants.EDM_SCHEMA.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_USING.equals(currentHandledStartTagName)) {
          usings.add(readUsing(reader, schemaImpl.getNamespace()));
        } else if (XmlMetadataConstants.EDM_ENTITY_TYPE.equals(currentHandledStartTagName)) {
          entityTypes.add(readEntityType(reader, edm));
        } else if (XmlMetadataConstants.EDM_COMPLEX_TYPE.equals(currentHandledStartTagName)) {
          complexTypes.add(readComplexType(reader, edm));
        } else if (XmlMetadataConstants.EDM_ASSOCIATION.equals(currentHandledStartTagName)) {
          associations.add(readAssociation(reader, edm));
        } else if (XmlMetadataConstants.EDM_ENTITY_CONTAINER.equals(currentHandledStartTagName)) {
          entityContainers.add(readEntityContainer(reader, (EdmImpl) edm));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (schemaImpl.getAlias() != null) {
      aliasNamespaceMap.put(schemaImpl.getAlias(), schemaImpl.getNamespace());
      edm.setAliasToNamespaceInfo(aliasNamespaceMap);
    }
    if (!annotationElements.isEmpty()) {
      schemaImpl.setAnnotationElements(annotationElements);
    }
    schemaImpl
        .setUsings(usings)
        .setEntityTypes(entityTypes)
        .setComplexTypes(complexTypes)
        .setAssociations(associations)
        .setEntityContainers(entityContainers);

    edm.setAliasToNamespaceInfo(aliasNamespaceMap)
        .setEdmAssociations(associationsMap)
        .setEdmComplexTypes(complexTypesMap)
        .setEdmEntityContainers(containerMap)
        .setDefaultEntityContainer(defaultEdmEntityContainer)
        .setEdmEntitySets(edmEntitySetList)
        .setEdmEntityTypes(entityTypesMap)
        .setEdmFunctionImports(edmFunctionImportList);


    return schemaImpl;
  }

  private void setAssociationSetForNavigations() throws EdmException {
    for(EdmEntitySet edmEntitySet:edmEntitySetList){
      List<String> navigations =  edmEntitySet.getEntityType().getNavigationPropertyNames();
      if(navigations!=null && !navigations.isEmpty()){
        for (EdmNavigationProperty navigationProperty : navProperties) {
          if (navigations.contains(navigationProperty.getName())) { //NOSONAR
            FullQualifiedName associationName = ((EdmNavigationPropertyImpl) navigationProperty).getRelationshipName();
            String toRoleName = ((EdmNavigationPropertyImpl) navigationProperty).getToRole();
            EdmAssociationEnd end = associationsMap.get(associationName).getEnd(toRoleName);
            if (end == null) {
              throw new EdmException(EdmException.ASSOCIATIONNOTFOUND);
            }
            String relation = associationName.toString();
            StringBuilder key = new StringBuilder();
            key.append(edmEntitySet.getName());
            key.append(">>");
            key.append(relation);
            key.append(">>");
            key.append(navigationProperty.getFromRole());
            ((EdmNavigationPropertyImpl) navigationProperty).setMultiplicity(end.getMultiplicity());
            associationSetMap.put(key.toString(), tempAssociationSetMap.get(relation));
          }
        }
      }
    }
  }

  private void setDetailsForEntitySet() throws EdmException {
    for (EdmEntitySet entitySet : edmEntitySetList) {
      EdmEntitySetImpl entitySetImpl = (EdmEntitySetImpl) entitySet;
      FullQualifiedName entityTypeKey = entitySetImpl.getEntityTypeName();
      if(aliasNamespaceMap.get(entitySetImpl.getEntityTypeName().getNamespace()) != null){
        entityTypeKey = new FullQualifiedName(aliasNamespaceMap.get(entitySetImpl.getEntityTypeName().getNamespace()),
            entitySetImpl.getEntityTypeName().getName());
      }
      if (entitySetImpl.getEntityType() == null && entityTypeKey != null) {
        if(entityTypesMap.get(entityTypeKey) != null){
          entitySetImpl.setEdmEntityType(entityTypesMap.get(entityTypeKey));
        }else{
          EdmEntityTypeImpl edmEntityType = new EdmEntityTypeImpl();
          edmEntityType.setName(entitySetImpl.getEntityTypeName().getName());
          edmEntityType.setNamespace(entitySetImpl.getEntityTypeName().getNamespace());
          entitySetImpl.setEdmEntityType(edmEntityType);
        }
      }
      if (associationSetEndMap.get(entitySet.getName())!=null) {
         List<EdmAssociationSetEndImpl> ends = associationSetEndMap.get(entitySet.getName());
         for(EdmAssociationSetEndImpl end:ends){
           end.setEntitySet(entitySet);
         }
      }
    }

  }

  private void setBaseTypeForComplexType() throws EdmException {
    for (Entry<FullQualifiedName, EdmComplexType> entity : complexTypesMap.entrySet()) {
      EdmComplexTypeImpl entityType = (EdmComplexTypeImpl) entity.getValue();
      if (((EdmComplexTypeImpl) entity.getValue()).getEdmBaseTypeName() != null && 
          entity.getValue().getBaseType() == null) {
        FullQualifiedName fqname = entityType.getEdmBaseTypeName();
        if(complexTypesMap.get(entityType.getEdmBaseTypeName()) != null){
          entityType.setEdmBaseType(complexTypesMap.get(fqname));
        }else if (aliasNamespaceMap.containsKey(fqname.getNamespace())) {
          FullQualifiedName changedName = new FullQualifiedName(
              aliasNamespaceMap.get(fqname.getNamespace()), fqname.getName());
          entityType.setEdmBaseType(complexTypesMap.get(changedName));
        }else{
          //Adding dummy basetype awhich will fail during validation
          EdmComplexTypeImpl newBaseType = new EdmComplexTypeImpl();
          newBaseType.setName(fqname.getName());
          newBaseType.setNamespace(fqname.getNamespace());
          ((EdmComplexTypeImpl) entity.getValue()).setEdmBaseType(newBaseType);
           break;
        }
        
      }
    }
    setBaseTypePropertiesForComplexType(complexBaseTypeMap);
  }

  private void setBaseTypeForEntityType() throws EdmException {
    for (Entry<FullQualifiedName, EdmEntityType> entity : entityTypesMap.entrySet()) {
      EdmEntityTypeImpl entityType = (EdmEntityTypeImpl) entity.getValue();
      if (entityType.getBaseTypeName() != null && entity.getValue()
          .getBaseType() == null) {
        FullQualifiedName fqname = entityType.getBaseTypeName();
        if (entityTypesMap.get(entityType.getBaseTypeName()) != null) {
          entityType.setEdmBaseType(entityTypesMap.get(fqname));
        } else if (aliasNamespaceMap.containsKey(fqname.getNamespace())) {
          FullQualifiedName changedName = new FullQualifiedName(
              aliasNamespaceMap.get(fqname.getNamespace()), fqname.getName());
          entityType.setEdmBaseType(entityTypesMap.get(changedName));
        } else {// Adding dummy basetype which will fail during validation
          EdmEntityTypeImpl newBaseType = new EdmEntityTypeImpl();
          newBaseType.setName(fqname.getName());
          newBaseType.setNamespace(fqname.getNamespace());
          entityType.setEdmBaseType(newBaseType);
          break;
        }
      }
    }
    setBaseTypePropertiesForEntityType(entityBaseTypeMap);
  }

  private void setBaseTypePropertiesForEntityType(
      Map<FullQualifiedName, FullQualifiedName> baseTypeMap) throws EdmException{
    changeAliasNamespaces(baseTypeMap);
    while(!baseTypeMap.isEmpty()){
      Iterator<Entry<FullQualifiedName, FullQualifiedName>> iterator = baseTypeMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<FullQualifiedName, FullQualifiedName> baseType = iterator.next();
        if(baseTypeMap.get(baseType.getValue()) == null){
          EdmEntityType entityType = entityTypesMap.get(baseType.getKey());
          List<String> properties = entityType.getPropertyNames();
          if(entityTypesMap.get(baseType.getValue())!=null){ //NOSONAR
            properties.addAll(entityTypesMap.get(baseType.getValue()).getPropertyNames());
          }
          iterator.remove();
        }
      }
    }
  }
  private void setBaseTypePropertiesForComplexType(
      Map<FullQualifiedName, FullQualifiedName> baseTypeMap) throws EdmException{
    changeAliasNamespaces(baseTypeMap);
    while(!baseTypeMap.isEmpty()){
      Iterator<Entry<FullQualifiedName, FullQualifiedName>> iterator = baseTypeMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<FullQualifiedName, FullQualifiedName> baseType = iterator.next();
        if(baseTypeMap.get(baseType.getValue()) == null){
          EdmComplexType entityType = complexTypesMap.get(baseType.getKey());
          List<String> properties = entityType.getPropertyNames();
          if(complexTypesMap.get(baseType.getValue())!=null){ //NOSONAR
            properties.addAll(complexTypesMap.get(baseType.getValue()).getPropertyNames());
          }
          iterator.remove();
        }
      }
    }
  }
  private void changeAliasNamespaces(Map<FullQualifiedName, FullQualifiedName> baseTypeMap) {
    for(Entry<FullQualifiedName, FullQualifiedName> entry:baseTypeMap.entrySet()){
      if (aliasNamespaceMap.containsKey(entry.getKey().getNamespace())) {
        FullQualifiedName value = baseTypeMap.get(entry.getKey());
        FullQualifiedName name = new FullQualifiedName(aliasNamespaceMap.get(entry.getKey().getNamespace()), 
            entry.getKey().getName());
        baseTypeMap.put(name, value);
        baseTypeMap.remove(entry.getKey());
      }
      if (aliasNamespaceMap.containsKey(entry.getValue().getNamespace())) {
        FullQualifiedName value =  new FullQualifiedName(aliasNamespaceMap.get(entry.getValue().getNamespace()), 
            entry.getValue().getName());
        baseTypeMap.remove(entry.getKey());
        baseTypeMap.put(entry.getKey(), value);
      }
     
    }
    
  }

  private EdmUsingImpl readUsing(final XMLStreamReader reader, final String schemaNamespace)
      throws XMLStreamException, EntityProviderException {

    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_USING);

    EdmUsingImpl using = new EdmUsingImpl();
    using.setNamespace(reader.getAttributeValue(null, XmlMetadataConstants.EDM_SCHEMA_NAMESPACE));
    inscopeMap.get(schemaNamespace).add(using.getNamespace());
    using.setAlias(reader.getAttributeValue(null, XmlMetadataConstants.EDM_SCHEMA_ALIAS));
    using.setAnnotationAttributes(readAnnotationAttribute(reader));

    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    while (reader.hasNext() && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_USING.equals(reader.getLocalName()))) {

      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        annotationElements.add(readAnnotationElement(reader));
      }
    }
    if (!annotationElements.isEmpty()) {
      using.setAnnotationElements(annotationElements);
    }

    if (using.getAlias() != null) {
      aliasNamespaceMap.put(using.getAlias(), using.getNamespace());
    }

    return using;
  }

  private EdmEntityContainer readEntityContainer(final XMLStreamReader reader, EdmImpl edm)//NOSONAR
      throws XMLStreamException, EntityProviderException, EdmException { 
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ENTITY_CONTAINER);
    EdmEntityContainerImpl container = new EdmEntityContainerImpl(edm);
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();

    List<EdmEntitySet> edmEntitySets = new ArrayList<EdmEntitySet>();
    List<EdmFunctionImport> edmFunctionImports = new ArrayList<EdmFunctionImport>();
    List<EdmAssociationSet> edmAssociationSets = new ArrayList<EdmAssociationSet>();

    container.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    if (reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.EDM_CONTAINER_IS_DEFAULT) != null) {
      container.setDefaultContainer(
          "true".equalsIgnoreCase(reader.getAttributeValue(Edm.NAMESPACE_M_2007_08,
              "IsDefaultEntityContainer")));
      defaultEdmEntityContainer = container;
    }
    container.setExtendz(reader.getAttributeValue(null, XmlMetadataConstants.EDM_CONTAINER_EXTENDZ));
    List<EdmAnnotationAttribute> annotationAttribute = readAnnotationAttribute(reader);
    EdmAnnotationsImpl annotations = new EdmAnnotationsImpl();
    annotations.setAnnotationAttributes(annotationAttribute);
    container.setAnnotations(annotations);

    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
            && XmlMetadataConstants.EDM_ENTITY_CONTAINER.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_ENTITY_SET.equals(currentHandledStartTagName)) {
          EdmEntitySetImpl entity = readEntitySet(reader);
          entity.setEdmEntityContainer(container);
          edmEntitySets.add(entity);
        } else if (XmlMetadataConstants.EDM_ASSOCIATION_SET.equals(currentHandledStartTagName)) {
          EdmAssociationSet association = readAssociationSet(reader, edm);
          edmAssociationSets.add(association);
        } else if (XmlMetadataConstants.EDM_FUNCTION_IMPORT.equals(currentHandledStartTagName)) {
          edmFunctionImports.add(readFunctionImport(reader, edm));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (!annotationElements.isEmpty()) {
      annotations.setAnnotationElements(annotationElements);
      container.setAnnotations(annotations);
    }
    if (edmFunctionImports != null && !edmFunctionImports.isEmpty()) {
      setContainerInFunctionImport(edmFunctionImports, container);
    }
    if (edmAssociationSets != null && !edmAssociationSets.isEmpty()) {
      setConatinerInAssociationSet(edmAssociationSets, container);
    }
    container.setEdmEntitySets(edmEntitySets).setEdmAssociationSets(edmAssociationSets).setEdmFunctionImports(
        edmFunctionImports);

    containerMap.put(new FullQualifiedName(currentNamespace, container.getName()),
        container);
    edmEntitySetList.addAll(edmEntitySets);
    edmFunctionImportList.addAll(edmFunctionImports);
    return container;
  }

  private void setConatinerInAssociationSet(List<EdmAssociationSet> edmAssociationSets,
      EdmEntityContainerImpl containerImpl) {
    for (EdmAssociationSet associationSet : edmAssociationSets) {
      EdmAssociationSetImpl assocationSetImpl = (EdmAssociationSetImpl) associationSet;
      assocationSetImpl.setEdmEntityContainer(containerImpl);
    }
    containerImpl.setEdmAssociationSetMap(associationSetMap);
  }

  /**
   * @param functionImports
   * @param containerImpl
   */
  private void setContainerInFunctionImport(List<EdmFunctionImport> functionImports,
      EdmEntityContainerImpl containerImpl) {
    for (EdmFunctionImport funcImport : functionImports) {
      EdmFunctionImportImpl functionImpl = (EdmFunctionImportImpl) funcImport;
      functionImpl.setEdmEntityContainer(containerImpl);
    }
  }

  private EdmFunctionImport readFunctionImport(final XMLStreamReader reader, EdmImpl edm)
      throws XMLStreamException, EntityProviderException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_FUNCTION_IMPORT);
    EdmFunctionImportImpl function = new EdmFunctionImportImpl();
    ArrayList<EdmFunctionImportParameter> functionImportParameters = new ArrayList<EdmFunctionImportParameter>();
    Map<String, ArrayList<EdmFunctionImportParameter>> functionParameters =
        new HashMap<String, ArrayList<EdmFunctionImportParameter>>();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    EdmAnnotationsImpl annotations = new EdmAnnotationsImpl();
    Map<String, EdmParameter> edmParamMap = new HashMap<String, EdmParameter>();
    List<String> parametersList =  new ArrayList<String>();
    FullQualifiedName fqName ;
    function.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    function.setHttpMethod(reader.getAttributeValue(Edm.NAMESPACE_M_2007_08,
        XmlMetadataConstants.EDM_FUNCTION_IMPORT_HTTP_METHOD));
    String entitySet = reader.getAttributeValue(null, XmlMetadataConstants.EDM_ENTITY_SET);
    function.setEntitySet(entitySet);

    String returnTypeString = reader.getAttributeValue(null, XmlMetadataConstants.EDM_FUNCTION_IMPORT_RETURN);
    EdmTypedImpl returnType = new EdmTypedImpl();
    if (returnTypeString != null) {
      if (returnTypeString.startsWith("Collection") || returnTypeString.startsWith("collection")) {
        returnTypeString = returnTypeString.substring(returnTypeString.indexOf("(") + 1, returnTypeString.length() - 1);
        fqName = extractFQName(returnTypeString);
        if(entitySet == null && entityTypesMap.get(fqName) != null){
          throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE.addContent
              ("EntitySet = "+entitySet, XmlMetadataConstants.EDM_FUNCTION_IMPORT +" = "+ function.getName()));
        }
        returnType.setMultiplicity(EdmMultiplicity.MANY);
      } else {
        fqName = extractFQName(returnTypeString);
        if(entitySet != null && entityTypesMap.get(fqName) == null) {
          throw new EntityProviderException(EntityProviderException.INVALID_ATTRIBUTE.addContent
              ("EntitySet = "+entitySet, XmlMetadataConstants.EDM_FUNCTION_IMPORT 
                  + " = "+ function.getName()));
        }
        returnType.setMultiplicity(EdmMultiplicity.ONE);
      }
      returnType.setTypeName(fqName);
      ((EdmNamedImpl) returnType).setName(fqName.getName());
      ((EdmNamedImpl) returnType).setEdm(edm);
      function.setReturnType(returnType);
    }
    annotations.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
            && XmlMetadataConstants.EDM_FUNCTION_IMPORT.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_FUNCTION_PARAMETER.equals(currentHandledStartTagName)) {
          EdmFunctionImportParameter edmFunctionImportParameter = readFunctionImportParameter(reader);
          functionImportParameters.add(edmFunctionImportParameter);
          EdmParameterImpl edmParamImpl = new EdmParameterImpl();
          edmParamImpl.setEdm(edm);
          edmParamImpl.setName(edmFunctionImportParameter.getName());
          edmParamImpl.setFacets(edmFunctionImportParameter.getFacets());
          edmParamImpl.setMapping(edmFunctionImportParameter.getMapping());
          edmParamImpl.setMultiplicity(returnType.getMultiplicity());
          edmParamImpl.setParameter(edmFunctionImportParameter);
          edmParamImpl.setTypeName(edmFunctionImportParameter.getType().getFullQualifiedName());
          edmParamImpl.setAnnotations(edmFunctionImportParameter.getAnnotations());
          parametersList.add(edmParamImpl.getName());
          edmParamMap.put(edmFunctionImportParameter.getName(), edmParamImpl);
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (!annotationElements.isEmpty()) {
      annotations.setAnnotationElements(annotationElements);
    }
    function.setAnnotations(annotations);
    functionParameters.put(function.getName(), functionImportParameters);
    function.setParameters(functionParameters);
    function.setEdmParameters(edmParamMap);
    function.setParametersList(parametersList);
    return function;
  }

  private EdmFunctionImportParameter readFunctionImportParameter(final XMLStreamReader reader)
      throws EntityProviderException, XMLStreamException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_FUNCTION_PARAMETER);
    EdmFunctionImportParameter functionParameter = new EdmFunctionImportParameter();
    EdmAnnotationsImpl annotations = new EdmAnnotationsImpl();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();

    functionParameter.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    functionParameter.setMode(reader.getAttributeValue(null, XmlMetadataConstants.EDM_FUNCTION_PARAMETER_MODE));
    String type = reader.getAttributeValue(null, XmlMetadataConstants.EDM_TYPE);
    if (type == null) {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_TYPE).addContent(XmlMetadataConstants.EDM_FUNCTION_PARAMETER));
    }
    functionParameter.setType(EdmSimpleTypeKind.valueOf(extractFQName(type).getName()));
    EdmFacets facets = readFacets(reader);
    functionParameter.setFacets(facets);
    annotations.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
            && XmlMetadataConstants.EDM_FUNCTION_PARAMETER.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        annotationElements.add(readAnnotationElement(reader));
      }
    }
    if (!annotationElements.isEmpty()) {
      annotations.setAnnotationElements(annotationElements);
    }
    functionParameter.setAnnotations(annotations);
    return functionParameter;
  }

  private EdmAssociationSet readAssociationSet(final XMLStreamReader reader, EdmImpl edm)
      throws XMLStreamException, EntityProviderException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ASSOCIATION_SET);
    EdmAssociationSetImpl associationSet = new EdmAssociationSetImpl();
    List<EdmAssociationSetEnd> ends = new ArrayList<EdmAssociationSetEnd>();
    EdmAnnotationsImpl annotation = new EdmAnnotationsImpl();
    String entitySetName = null;
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    associationSet.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    String association = reader.getAttributeValue(null, XmlMetadataConstants.EDM_ASSOCIATION);
    if (association != null) {
      associationSet.setAssociation(extractFQName(association));
    } else {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_ASSOCIATION).addContent(XmlMetadataConstants.EDM_ASSOCIATION_SET));
    }
    annotation.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
            && XmlMetadataConstants.EDM_ASSOCIATION_SET.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_ASSOCIATION_END.equals(currentHandledStartTagName)) {
          EdmAssociationSetEndImpl associationSetEnd = new EdmAssociationSetEndImpl();
          entitySetName = reader.getAttributeValue(null, XmlMetadataConstants.EDM_ENTITY_SET);
          associationSetEnd.setEntitySetName(entitySetName);
          associationSetEnd.setRole(reader.getAttributeValue(null, XmlMetadataConstants.EDM_ROLE));
          ends.add(associationSetEnd);
          List<EdmAssociationSetEndImpl> associationSetEndList;
          if(associationSetEndMap.get(entitySetName) == null){
            associationSetEndList = new ArrayList<EdmAssociationSetEndImpl>();
          }else{
            associationSetEndList= associationSetEndMap.get(entitySetName);
          }
          associationSetEndList.add(associationSetEnd);
          associationSetEndMap.put(entitySetName, associationSetEndList);
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (ends.size() != 2) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
          .addContent("Count of AssociationSet ends should be 2"));
    } else {
      associationSet.setEnd1(ends.get(0));
      associationSet.setEnd2(ends.get(1));
    }
    if (!annotationElements.isEmpty()) {
      annotation.setAnnotationElements(annotationElements);
    }
    //Updating temporary map for association post processing
    tempAssociationSetMap.put(associationSet.getAssociationSetFQName().toString(), associationSet);
    associationSet.setAnnotations(annotation);
    associationSet.setEdm(edm);
    return associationSet;
  }

  private EdmEntitySetImpl readEntitySet(final XMLStreamReader reader) throws XMLStreamException,
  EntityProviderException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ENTITY_SET);
    EdmEntitySetImpl entitySet = new EdmEntitySetImpl();
    EdmAnnotationsImpl annotations = new EdmAnnotationsImpl();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    entitySet.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    String entityType = reader.getAttributeValue(null, XmlMetadataConstants.EDM_ENTITY_TYPE);
    if (entityType != null) {
      FullQualifiedName fqName = extractFQName(entityType);
      entitySet.setEdmEntityTypeName(fqName);
    } else {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_ENTITY_TYPE).addContent(XmlMetadataConstants.EDM_ENTITY_SET));
    }
    annotations.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
            && XmlMetadataConstants.EDM_ENTITY_SET.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        annotationElements.add(readAnnotationElement(reader));
      }
    }
    if (!annotationElements.isEmpty()) {
      annotations.setAnnotationElements(annotationElements);
    }
    entitySet.setAnnotations(annotations);
    return entitySet;
  }

  private EdmAssociation readAssociation(final XMLStreamReader reader, EdmImpl edm)
      throws XMLStreamException, EntityProviderException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ASSOCIATION);

    EdmAssociationImpl association = new EdmAssociationImpl();
    association.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    association.setNamespace(currentNamespace);
    List<EdmMetadataAssociationEnd> associationEnds = new ArrayList<EdmMetadataAssociationEnd>();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    EdmAnnotationsImpl annotations = new EdmAnnotationsImpl();
    annotations.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
            && XmlMetadataConstants.EDM_ASSOCIATION.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_ASSOCIATION_END.equals(currentHandledStartTagName)) {
          associationEnds.add(readAssociationEnd(reader, edm));
        } else if (XmlMetadataConstants.EDM_ASSOCIATION_CONSTRAINT.equals(currentHandledStartTagName)) {
          association.setReferentialConstraint(readReferentialConstraint(reader));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (associationEnds.size() < 2 && associationEnds.size() > 2) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
          .addContent("Count of association ends should be 2"));
    }
    if (!annotationElements.isEmpty()) {
      annotations.setAnnotationElements(annotationElements);
    }
    association.setAnnotations(annotations);
    association.setEdm(edm);
    association.setAssociationEnds(associationEnds);
    associationsMap.put(new FullQualifiedName(currentNamespace, association.getName()), association);
    return association;
  }

  private EdmReferentialConstraint readReferentialConstraint(final XMLStreamReader reader) throws XMLStreamException,
      EntityProviderException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ASSOCIATION_CONSTRAINT);
    EdmReferentialConstraintImpl refConstraint = new EdmReferentialConstraintImpl();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    EdmAnnotationsImpl annotations = new EdmAnnotationsImpl();
    annotations.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
            && XmlMetadataConstants.EDM_ASSOCIATION_CONSTRAINT.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_ASSOCIATION_PRINCIPAL.equals(currentHandledStartTagName)) {
          reader
              .require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ASSOCIATION_PRINCIPAL);
          refConstraint.setPrincipal(readReferentialConstraintRole(reader));
        } else if (XmlMetadataConstants.EDM_ASSOCIATION_DEPENDENT.equals(currentHandledStartTagName)) {
          reader
              .require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ASSOCIATION_DEPENDENT);
          refConstraint.setDependent(readReferentialConstraintRole(reader));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (!annotationElements.isEmpty()) {
      annotations.setAnnotationElements(annotationElements);
    }
    refConstraint.setAnnotations(annotations);
    return refConstraint;
  }

  private EdmReferentialConstraintRole readReferentialConstraintRole(final XMLStreamReader reader)
      throws EntityProviderException, XMLStreamException, EdmException {
    EdmReferentialConstraintRoleImpl rcRole = new EdmReferentialConstraintRoleImpl();
    rcRole.setRoleName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_ROLE));
    List<EdmPropertyImpl> properties = new ArrayList<EdmPropertyImpl>();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    List<String> refNames = new ArrayList<String>();
    EdmAnnotationsImpl annotations = new EdmAnnotationsImpl();
    annotations.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext() && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && (XmlMetadataConstants.EDM_ASSOCIATION_PRINCIPAL.equals(reader.getLocalName())
            || XmlMetadataConstants.EDM_ASSOCIATION_DEPENDENT.equals(reader.getLocalName())))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_PROPERTY_REF.equals(currentHandledStartTagName)) {
          EdmPropertyImpl property = (EdmPropertyImpl) readPropertyRef(reader);
          properties.add(property );
          refNames.add(property.getName());
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (!annotationElements.isEmpty()) {
      annotations.setAnnotationElements(annotationElements);
    }
    rcRole.setProperty(properties);
    rcRole.setRefNames(refNames);
    rcRole.setAnnotations(annotations);
    return rcRole;
  }

  private EdmComplexType readComplexType(final XMLStreamReader reader, Edm edm)
      throws XMLStreamException, EntityProviderException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_COMPLEX_TYPE);

    EdmComplexTypeImpl complexType = new EdmComplexTypeImpl();
    complexType.setEdmTypeKind(EdmTypeKind.COMPLEX);
    List<EdmProperty> properties = new ArrayList<EdmProperty>();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    complexType.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    complexType.setNamespace(currentNamespace);
    String baseType = reader.getAttributeValue(null, XmlMetadataConstants.EDM_BASE_TYPE);
    if (baseType != null) {
      FullQualifiedName fqname = extractFQName(baseType);
      complexType.setBaseTypeName(fqname);
      complexBaseTypeMap.put(new FullQualifiedName(complexType.getNamespace(), complexType.getName()), fqname);
    }
    if (reader.getAttributeValue(null, XmlMetadataConstants.EDM_TYPE_ABSTRACT) != null) {
      complexType.setAbstract("true".equalsIgnoreCase(reader.getAttributeValue(null,
          XmlMetadataConstants.EDM_TYPE_ABSTRACT)));
    }
    EdmAnnotationsImpl annotations = new EdmAnnotationsImpl();
    annotations.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
            && XmlMetadataConstants.EDM_COMPLEX_TYPE.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_PROPERTY.equals(currentHandledStartTagName)) {
          properties.add(readProperty(reader, edm));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (!annotationElements.isEmpty()) {
      annotations.setAnnotationElements(annotationElements);
    }
    complexType.setProperties(properties);
    List<String> edmPropertyNames = new ArrayList<String>();
    for (EdmProperty name : properties) {
      edmPropertyNames.add(name.getName());
    }
    complexType.setEdmPropertyNames(edmPropertyNames);
    if (complexType.getName() != null) {
      FullQualifiedName fqName = new FullQualifiedName(currentNamespace, complexType.getName());
      complexTypesMap.put(fqName, complexType);
      if (complexPropertyMap.get(fqName) != null) {// POST processing to set the edm types for complex types
        ((EdmComplexPropertyImpl) complexPropertyMap.get(fqName)).setEdmType(complexType);
      }
    } else {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE.addContent("Name"));
    }
    return complexType;

  }

  private EdmEntityType readEntityType(final XMLStreamReader reader,
      EdmImpl edm) throws XMLStreamException, EntityProviderException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ENTITY_TYPE);
    EdmEntityTypeImpl entityType = new EdmEntityTypeImpl();
    List<EdmProperty> properties = new ArrayList<EdmProperty>();
    List<String> edmPropertyNames = new ArrayList<String>();
    List<EdmNavigationProperty> edmNavProperties = new ArrayList<EdmNavigationProperty>();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    List<String> edmNavigationPropertyNames = new ArrayList<String>();
    List<String> edmKeyPropertyNames = new ArrayList<String>();
    List<EdmProperty> edmKeyProperties = new ArrayList<EdmProperty>();
    EdmKeyImpl key = null;
    ((EdmNamedImpl) entityType).setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    entityType.setNamespace(currentNamespace);
    entityType.setEdm((EdmImpl) edm);
    String hasStream = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_ENTITY_TYPE_HAS_STREAM);
    if (hasStream != null) {
      entityType.setHasStream("true".equalsIgnoreCase(hasStream));
    }

    if (reader.getAttributeValue(null, XmlMetadataConstants.EDM_TYPE_ABSTRACT) != null) {
      entityType.setAbstract("true".equalsIgnoreCase(reader.getAttributeValue(null,
          XmlMetadataConstants.EDM_TYPE_ABSTRACT)));
    }
    String baseType = reader.getAttributeValue(null, XmlMetadataConstants.EDM_BASE_TYPE);
    if (baseType != null) {
      FullQualifiedName fqName = extractFQName(baseType);
      entityType.setBaseType(fqName);
      //Populating base type map with child and parent for multiple inheritance
      entityBaseTypeMap.put(extractFQNameFromEntityType(entityType), fqName);
    }
    entityType.setCustomizableFeedMappings(readCustomizableFeedMappings(reader));
    EdmAnnotationsImpl annotations = new EdmAnnotationsImpl();
    annotations.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
            && XmlMetadataConstants.EDM_ENTITY_TYPE.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_ENTITY_TYPE_KEY.equals(currentHandledStartTagName)) {
          key = readEntityTypeKey(reader);
        } else if (XmlMetadataConstants.EDM_PROPERTY.equals(currentHandledStartTagName)) {
          properties.add(readProperty(reader, edm));
        } else if (XmlMetadataConstants.EDM_NAVIGATION_PROPERTY.equals(currentHandledStartTagName)) {
          edmNavProperties.add(readNavigationProperty(reader, edm));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
        extractNamespaces(reader);
      }
    }
    if (!annotationElements.isEmpty()) {
      annotations.setAnnotationElements(annotationElements);
    }
    
    for (EdmProperty property : properties) {
      edmPropertyNames.add(property.getName());
      if (key != null) {        
        for(EdmProperty keyProperty: key.getKeys()){
          if(property.getName().equals(keyProperty.getName())){
            edmKeyPropertyNames.add(keyProperty.getName());
            edmKeyProperties.add(property);
          }
        }
      }
    }
    for ( EdmNavigationProperty navigations : edmNavProperties){
      edmNavigationPropertyNames.add(navigations.getName());
    }

    entityType.setAnnotations(annotations);
    entityType.setEdmKeyProperties(edmKeyProperties);
    entityType.setNavigationProperties(edmNavProperties);
    entityType.setEdmNavigationPropertyNames(edmNavigationPropertyNames);    
    entityType.setEdmKeyPropertyNames(edmKeyPropertyNames);
    entityType.setProperties(properties);
    entityType.setEdmPropertyNames(edmPropertyNames);
    if (entityType.getName() != null) {
      FullQualifiedName fqName = new FullQualifiedName(currentNamespace, entityType.getName());
      entityTypesMap.put(fqName, entityType);
    } else {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE.addContent("Name"));
    }

    return entityType;
  }

  private EdmKeyImpl readEntityTypeKey(final XMLStreamReader reader) throws XMLStreamException,
  EntityProviderException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ENTITY_TYPE_KEY);
    List<EdmProperty> keys = new ArrayList<EdmProperty>();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    List<EdmAnnotationAttribute> annotationAttributes = readAnnotationAttribute(reader);
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
            && XmlMetadataConstants.EDM_ENTITY_TYPE_KEY.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_PROPERTY_REF.equals(currentHandledStartTagName)) {
          reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_PROPERTY_REF);
          keys.add(readPropertyRef(reader));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    EdmKeyImpl key = new EdmKeyImpl().setKeys(keys).setAnnotationAttributes(annotationAttributes);
    if (!annotationElements.isEmpty()) {
      key.setAnnotationElements(annotationElements);
    }
    return key;
  }

  private EdmProperty readPropertyRef(final XMLStreamReader reader) throws XMLStreamException, 
  EntityProviderException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_PROPERTY_REF);
    EdmPropertyImpl propertyRef = new EdmPropertyRefImpl();
    propertyRef.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    EdmAnnotations annotations = new EdmAnnotationsImpl();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    ((EdmAnnotationsImpl) annotations).setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext() && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_PROPERTY_REF.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        annotationElements.add(readAnnotationElement(reader));
      }
    }
    if (!annotationElements.isEmpty()) {
      ((EdmAnnotationsImpl) annotations).setAnnotationElements(annotationElements);
    }
    propertyRef.setAnnotations(annotations);
    return propertyRef;
  }

  private EdmNavigationProperty readNavigationProperty(final XMLStreamReader reader, EdmImpl edm) 
      throws XMLStreamException, EntityProviderException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_NAVIGATION_PROPERTY);

    EdmNavigationPropertyImpl navProperty = new EdmNavigationPropertyImpl();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    navProperty.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    String relationship = reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAVIGATION_RELATIONSHIP);
    if (relationship != null) {
      FullQualifiedName fqName = extractFQName(relationship);
      navProperty.setRelationshipName(fqName);

    } else {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_NAVIGATION_RELATIONSHIP).addContent(
              XmlMetadataConstants.EDM_NAVIGATION_PROPERTY));
    }

    navProperty.setFromRole(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAVIGATION_FROM_ROLE));
    navProperty.setToRole(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAVIGATION_TO_ROLE));
    EdmAnnotations annotations = new EdmAnnotationsImpl();
    ((EdmAnnotationsImpl) annotations).setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext() && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_NAVIGATION_PROPERTY.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        annotationElements.add(readAnnotationElement(reader));
      }
    }
    if (!annotationElements.isEmpty()) {
      ((EdmAnnotationsImpl) annotations).setAnnotationElements(annotationElements);
    }
    navProperty.setAnnotations(annotations);
    navProperty.setEdm(edm);        
    navProperties.add(navProperty);
    return navProperty;
  }

  private EdmProperty readProperty(final XMLStreamReader reader, Edm edm)
      throws XMLStreamException, EntityProviderException, EdmException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_PROPERTY);
    EdmPropertyImpl property;
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    String type = reader.getAttributeValue(null, XmlMetadataConstants.EDM_TYPE);
    if (type == null) {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_TYPE).addContent(XmlMetadataConstants.EDM_PROPERTY));
    }
    FullQualifiedName fqName = extractFQName(type);

    if (EdmSimpleType.EDM_NAMESPACE.equals(fqName.getNamespace())) {
      property = readSimpleProperty(reader, fqName);
    } else {
      property = readComplexProperty(reader, fqName, edm);
    }
    property.setFacets(readFacets(reader));
    property.setCustomizableFeedMappings(readCustomizableFeedMappings(reader));
    property.setMimeType(reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_MIMETYPE));
    EdmAnnotations annotations = new EdmAnnotationsImpl();
    ((EdmAnnotationsImpl) annotations).setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext() && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_PROPERTY.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        annotationElements.add(readAnnotationElement(reader));
      }
    }
    if (!annotationElements.isEmpty()) {
      ((EdmAnnotationsImpl) annotations).setAnnotationElements(annotationElements);
    }
    property.setAnnotations(annotations);
    return property;
  }

  private EdmPropertyImpl readComplexProperty(final XMLStreamReader reader, final FullQualifiedName fqName, Edm edm)
      throws XMLStreamException, EdmException {
    EdmComplexPropertyImpl property = new EdmComplexPropertyImpl();
    property.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    property.setTypeName(fqName);
    property.setEdm((EdmImpl) edm);
    complexPropertyMap.put(fqName, property);
    return property;
  }

  private EdmPropertyImpl readSimpleProperty(final XMLStreamReader reader, final FullQualifiedName fqName)
      throws XMLStreamException, EdmException {
    EdmSimplePropertyImpl property = new EdmSimplePropertyImpl();
    property.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    property.setTypeName(fqName);
    property.setSimpleType(EdmSimpleTypeKind.valueOf(fqName.getName()));
    property.setEdmType(EdmSimpleTypeFacadeImpl.getEdmSimpleType(property.getSimpleType()));
    return property;
  }

  private EdmFacets readFacets(final XMLStreamReader reader) throws XMLStreamException {
    String isNullable = reader.getAttributeValue(null, XmlMetadataConstants.EDM_PROPERTY_NULLABLE);
    String maxLength = reader.getAttributeValue(null, XmlMetadataConstants.EDM_PROPERTY_MAX_LENGTH);
    String precision = reader.getAttributeValue(null, XmlMetadataConstants.EDM_PROPERTY_PRECISION);
    String scale = reader.getAttributeValue(null, XmlMetadataConstants.EDM_PROPERTY_SCALE);
    String isFixedLength = reader.getAttributeValue(null, XmlMetadataConstants.EDM_PROPERTY_FIXED_LENGTH);
    String isUnicode = reader.getAttributeValue(null, XmlMetadataConstants.EDM_PROPERTY_UNICODE);
    String concurrencyMode = reader.getAttributeValue(null, XmlMetadataConstants.EDM_PROPERTY_CONCURRENCY_MODE);
    String defaultValue = reader.getAttributeValue(null, XmlMetadataConstants.EDM_PROPERTY_DEFAULT_VALUE);
    String collation = reader.getAttributeValue(null, XmlMetadataConstants.EDM_PROPERTY_COLLATION);
    if (isNullable != null || maxLength != null || precision != null || scale != null || isFixedLength != null
        || isUnicode != null || concurrencyMode != null || defaultValue != null || collation != null) {
      EdmFacets facets = new Facets();
      if (isNullable != null) {
        ((Facets) facets).setNullable("true".equalsIgnoreCase(isNullable));
      }
      if (maxLength != null) {
        if (XmlMetadataConstants.EDM_PROPERTY_MAX_LENGTH_MAX_VALUE_FIRST_UPPERCASE.equals(maxLength)
            || XmlMetadataConstants.EDM_PROPERTY_MAX_LENGTH_MAX_VALUE_LOWERCASE.equals(maxLength)) {
          ((Facets) facets).setMaxLength(Integer.MAX_VALUE);
        } else {
          ((Facets) facets).setMaxLength(Integer.parseInt(maxLength));
        }
      }
      if (precision != null) {
        ((Facets) facets).setPrecision(Integer.parseInt(precision));
      }
      if (scale != null) {
        ((Facets) facets).setScale(Integer.parseInt(scale));
      }
      if (isFixedLength != null) {
        ((Facets) facets).setFixedLength("true".equalsIgnoreCase(isFixedLength));
      }
      if (isUnicode != null) {
        ((Facets) facets).setUnicode("true".equalsIgnoreCase(isUnicode));
      }
      for (int i = 0; i < EdmConcurrencyMode.values().length; i++) {
        if (EdmConcurrencyMode.values()[i].name().equalsIgnoreCase(concurrencyMode)) {
          ((Facets) facets).setConcurrencyMode(EdmConcurrencyMode.values()[i]);
        }
      }
      ((Facets) facets).setDefaultValue(defaultValue);
      ((Facets) facets).setCollation(collation);
      return facets;
    } else {
      return null;
    }
  }

  private EdmCustomizableFeedMappings readCustomizableFeedMappings(final XMLStreamReader reader) {
    String targetPath = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_TARGET_PATH);
    String sourcePath = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_SOURCE_PATH);
    String nsUri = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_NS_URI);
    String nsPrefix = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_PREFIX);
    String keepInContent = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_KEEP_IN_CONTENT);
    String contentKind = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_CONTENT_KIND);

    if (targetPath != null || sourcePath != null || nsUri != null || nsPrefix != null || keepInContent != null
        || contentKind != null) {
      EdmCustomizableFeedMappings feedMapping = new EdmCustomizableFeedMappingsImpl();
      EdmCustomizableFeedMappingsImpl feedMappingImpl = (EdmCustomizableFeedMappingsImpl) feedMapping;
      if (keepInContent != null) {
        feedMappingImpl.setFcKeepInContent("true".equals(keepInContent));
      }
      for (int i = 0; i < EdmContentKind.values().length; i++) {
        if (EdmContentKind.values()[i].name().equalsIgnoreCase(contentKind)) {
          feedMappingImpl.setFcContentKind(EdmContentKind.values()[i]);
        }
      }
      feedMappingImpl.setFcTargetPath(targetPath).setFcSourcePath(sourcePath).setFcNsUri(nsUri).setFcNsPrefix(nsPrefix);
      return feedMapping;
    } else {
      return null;
    }

  }

  private EdmMetadataAssociationEnd readAssociationEnd(final XMLStreamReader reader, 
      EdmImpl edm) throws EntityProviderException, XMLStreamException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ASSOCIATION_END);

    EdmAssociationEndImpl associationEnd = new EdmAssociationEndImpl();
    EdmAnnotationsImpl annotations = new EdmAnnotationsImpl();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    associationEnd.setRole(reader.getAttributeValue(null, XmlMetadataConstants.EDM_ROLE));
    associationEnd.setMultiplicity(EdmMultiplicity.fromLiteral(reader.getAttributeValue(null,
        XmlMetadataConstants.EDM_ASSOCIATION_MULTIPLICITY)));
    String type = reader.getAttributeValue(null, XmlMetadataConstants.EDM_TYPE);
    if (type == null) {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_TYPE).addContent(XmlMetadataConstants.EDM_ASSOCIATION_END));
    }
    associationEnd.setEdm(edm);
    associationEnd.setType(extractFQName(type));
    annotations.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext() && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_ASSOCIATION_END.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_ASSOCIATION_ONDELETE.equals(currentHandledStartTagName)) {
          EdmOnDeleteImpl onDelete = new EdmOnDeleteImpl();
          for (int i = 0; i < EdmAction.values().length; i++) {
            if (EdmAction.values()[i].name().equalsIgnoreCase(
                reader.getAttributeValue(null, XmlMetadataConstants.EDM_ONDELETE_ACTION))) {
              onDelete.setAction(EdmAction.values()[i]);
            }
          }
          associationEnd.setOnDelete(onDelete);
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (!annotationElements.isEmpty()) {
      annotations.setAnnotationElements(annotationElements);
    }
    associationEnd.setAnnotations(annotations);
    return associationEnd;
  }

  private EdmAnnotationElement readAnnotationElement(final XMLStreamReader reader) throws XMLStreamException {
    EdmAnnotationElementImpl elementImpl = new EdmAnnotationElementImpl();
    List<EdmAnnotationElement> annotationElements = new ArrayList<EdmAnnotationElement>();
    List<EdmAnnotationAttribute> annotationAttributes = new ArrayList<EdmAnnotationAttribute>();
    elementImpl.setName(reader.getLocalName());
    String elementNamespace = reader.getNamespaceURI();
    if (!edmNamespaces.contains(elementNamespace)) {
      elementImpl.setPrefix(reader.getPrefix());
      elementImpl.setNamespace(elementNamespace);
    }
    for (int i = 0; i < reader.getAttributeCount(); i++) {
      EdmAnnotationAttributeImpl annotationAttribute = new EdmAnnotationAttributeImpl();
      annotationAttribute.setText(reader.getAttributeValue(i));
      annotationAttribute.setName(reader.getAttributeLocalName(i));
      annotationAttribute.setPrefix(reader.getAttributePrefix(i));
      String namespace = reader.getAttributeNamespace(i);
      if (namespace != null && !isDefaultNamespace(namespace)) {
        annotationAttribute.setNamespace(namespace);
      }
      annotationAttributes.add(annotationAttribute);
    }
    if (!annotationAttributes.isEmpty()) {
      elementImpl.setAttributes(annotationAttributes);
    }

    boolean justRead = false;
    if (reader.hasNext()) {
      reader.next();
      justRead = true;
    }

    while (justRead && !(reader.isEndElement() && elementImpl.getName() != null
        && elementImpl.getName().equals(reader.getLocalName()))) {
      justRead = false;
      if (reader.isStartElement()) {
        annotationElements.add(readAnnotationElement(reader));
        if (reader.hasNext()) {
          reader.next();
          justRead = true;
        }
      } else if (reader.isCharacters()) {
        String elementText = "";
        do {
          justRead = false;
          elementText = elementText + reader.getText();
          if (reader.hasNext()) {
            reader.next();
            justRead = true;
          }
        } while (justRead && reader.isCharacters());
        elementImpl.setText(elementText);
      }
    }
    if (!annotationElements.isEmpty()) {
      elementImpl.setChildElements(annotationElements);
    }
    return elementImpl;
  }

  private List<EdmAnnotationAttribute> readAnnotationAttribute(final XMLStreamReader reader) {
    List<EdmAnnotationAttribute> annotationAttributes = new ArrayList<EdmAnnotationAttribute>();
    for (int i = 0; i < reader.getAttributeCount(); i++) {
      String attributeNamespace = reader.getAttributeNamespace(i);
      if (attributeNamespace != null && !isDefaultNamespace(attributeNamespace)
          && !mandatoryNamespaces.containsValue(attributeNamespace)
          && !edmNamespaces.contains(attributeNamespace)) {
        annotationAttributes.add(new EdmAnnotationAttributeImpl().setName(reader.getAttributeLocalName(i)).setPrefix(
            reader.getAttributePrefix(i)).setNamespace(attributeNamespace).setText(
                reader.getAttributeValue(i)));
      }
    }
    if (annotationAttributes.isEmpty()) {
      return null;
    }
    return annotationAttributes;
  }

  private boolean isDefaultNamespace(final String namespace) {
    return namespace.isEmpty();
  }

  private void checkMandatoryNamespacesAvailable() throws EntityProviderException {
    if (!xmlNamespaceMap.containsValue(Edm.NAMESPACE_EDMX_2007_06)) {
      throw new EntityProviderException(EntityProviderException.INVALID_NAMESPACE
          .addContent(Edm.NAMESPACE_EDMX_2007_06));
    } else if (!xmlNamespaceMap.containsValue(Edm.NAMESPACE_M_2007_08)) {
      throw new EntityProviderException(EntityProviderException.INVALID_NAMESPACE.addContent(Edm.NAMESPACE_M_2007_08));
    }
  }

  private void checkEdmNamespace() throws EntityProviderException {
    if (!edmNamespaces.contains(edmNamespace)) {
      throw new EntityProviderException(EntityProviderException.INVALID_NAMESPACE
          .addContent(XmlMetadataConstants.EDM_SCHEMA));
    }
  }

  private void extractNamespaces(final XMLStreamReader reader) throws EntityProviderException {
    int namespaceCount = reader.getNamespaceCount();
    for (int i = 0; i < namespaceCount; i++) {
      String namespacePrefix = reader.getNamespacePrefix(i);
      String namespaceUri = reader.getNamespaceURI(i);
      if (namespacePrefix == null || isDefaultNamespace(namespacePrefix)) {
        namespacePrefix = Edm.PREFIX_EDM;
      }
      //Ignoring the duplicate tags, parent tag namespace takes precedence
      if (!xmlNamespaceMap.containsKey(namespacePrefix)) {
        xmlNamespaceMap.put(namespacePrefix, namespaceUri);
      }
    }
  }

  private FullQualifiedName extractFQName(final String name)
      throws EntityProviderException {
    // Looking for the last dot
    String[] names = name.split("\\" + Edm.DELIMITER + "(?=[^\\" + Edm.DELIMITER + "]+$)");
    if (names.length != 2) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
          .addContent("Attribute should specify a namespace qualified name or an alias qualified name"));
    } else {
      return new FullQualifiedName(names[0], names[1]);
    }
  }

  private FullQualifiedName extractFQNameFromEntityType(final EdmEntityType entity)
      throws EntityProviderException, EdmException {
    return new FullQualifiedName(entity.getNamespace(), entity.getName());
  }

  private FullQualifiedName validateEntityTypeWithAlias(final FullQualifiedName aliasName)
      throws EntityProviderException {
    String namespace = aliasNamespaceMap.get(aliasName.getNamespace());
    FullQualifiedName fqName = new FullQualifiedName(namespace, aliasName.getName());
    if (!entityTypesMap.containsKey(fqName)) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT.addContent("Invalid Type"));
    }
    return fqName;
  }

  private void validateEntityTypes() throws EntityProviderException, EdmException {
    for (Map.Entry<FullQualifiedName, EdmEntityType> entityTypes : entityTypesMap.entrySet()) {
      if (entityTypes.getValue() != null && entityTypes.getKey() != null) {
        EdmEntityTypeImpl entityType = (EdmEntityTypeImpl) entityTypes.getValue();
        if (entityType.getBaseTypeName() != null) {
          FullQualifiedName baseTypeFQName = entityType.getBaseTypeName();
          EdmEntityType baseEntityType;
          if (!entityTypesMap.containsKey(baseTypeFQName)) {
            FullQualifiedName fqName = validateEntityTypeWithAlias(baseTypeFQName);
            baseEntityType = entityTypesMap.get(fqName);
          } else {
            baseEntityType = fetchLastBaseType(baseTypeFQName, entityTypesMap);
          }
          if (baseEntityType != null && 
              (baseEntityType.getKeyProperties() == null || baseEntityType.getKeyProperties().isEmpty())) {
            throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
                .addContent("Missing key for EntityType " + baseEntityType.getName()));
          }
        } else if (entityType.getKeyProperties() == null || entityType.getKeyProperties().isEmpty()) {
          throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
              .addContent("Missing key for EntityType " + entityType.getName()));
        }
      }
    }
  }

  /*
   * This method gets the last base type of the EntityType
   * which has key defined in order to validate it
   */
  private EdmEntityType fetchLastBaseType(FullQualifiedName baseTypeFQName,
      Map<FullQualifiedName, EdmEntityType> entityTypesMap2)
      throws EntityProviderException, EdmException {

    EdmEntityTypeImpl baseEntityType = null;
    while (baseTypeFQName != null) {
      baseEntityType = (EdmEntityTypeImpl) entityTypesMap2.get(baseTypeFQName);
      if(baseEntityType != null){
        if (baseEntityType.getKeyPropertyNames() != null && !baseEntityType.getKeyPropertyNames().isEmpty()) {
          break;
        } else if (baseEntityType.getBaseType() != null) {
          baseTypeFQName = baseEntityType.getBaseTypeName();
        } else if (baseEntityType.getBaseType() == null) {
          break;
        }
      }
    }
    return baseEntityType;
  }

  private FullQualifiedName validateComplexTypeWithAlias(final FullQualifiedName aliasName)
      throws EntityProviderException {
    String namespace = aliasNamespaceMap.get(aliasName.getNamespace());
    FullQualifiedName fqName = new FullQualifiedName(namespace, aliasName.getName());
    if (!complexTypesMap.containsKey(fqName)) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT.addContent("Invalid BaseType")
          .addContent(fqName));
    }
    return fqName;
  }

  private void validateComplexTypes() throws EntityProviderException, EdmException {
    for (Map.Entry<FullQualifiedName, EdmComplexType> complexTypes : complexTypesMap.entrySet()) {
      if (complexTypes.getValue() != null && complexTypes.getKey() != null) {
        EdmComplexTypeImpl complexType = (EdmComplexTypeImpl) complexTypes.getValue();
        if (complexType.getBaseType() != null) {
          FullQualifiedName baseTypeFQName = complexType.getEdmBaseTypeName();
          if (!complexTypesMap.containsKey(baseTypeFQName)) {
            validateComplexTypeWithAlias(baseTypeFQName);
          }
        }
      }
    }
  }

  private void validateRelationship() throws EntityProviderException, EdmException {
    for (EdmNavigationProperty navProperty : navProperties) {
      EdmNavigationPropertyImpl navigationImpl = (EdmNavigationPropertyImpl) navProperty;
      if (associationsMap.containsKey(navigationImpl.getRelationshipName())) {
        EdmAssociationImpl assoc = (EdmAssociationImpl) associationsMap.get(navigationImpl.getRelationshipName());
        if (!(assoc.getEnd1().getRole().equals(navProperty.getFromRole())
            ^ assoc.getEnd1().getRole().equals(navProperty.getToRole())
            && (assoc.getEnd2().getRole().equals(navProperty.getFromRole()) ^ assoc.getEnd2().getRole().equals(
                navProperty.getToRole())))) {
          throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
              .addContent("Invalid end of association"));
        }
        if (!entityTypesMap.containsKey(extractFQNameFromEntityType(assoc.getEnd1().getEntityType()))) {
          EdmEntityType entityType = assoc.getEnd1().getEntityType();
          validateEntityTypeWithAlias(extractFQNameFromEntityType(entityType));
        }
        if (!entityTypesMap.containsKey(extractFQNameFromEntityType(assoc.getEnd2().getEntityType()))) {
          EdmEntityType entityType = assoc.getEnd2().getEntityType();
          validateEntityTypeWithAlias(extractFQNameFromEntityType(entityType));
        }
      } else {
        throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT.addContent("Invalid Relationship"));
      }
    }

  }

  private void validateAssociation() throws EntityProviderException, EdmException {
    for (Map.Entry<FullQualifiedName, EdmEntityContainer> container : containerMap.entrySet()) {
      for (EdmAssociationSet associationSet : container.getValue().getAssociationSets()) {
        FullQualifiedName association = new FullQualifiedName(associationSet.getAssociation().getNamespace(),
            associationSet.getAssociation().getName());
        if (associationsMap.containsKey(association)) {
          validateAssociationEnd(((EdmAssociationSetImpl) associationSet).getEnd1(), associationsMap.get(association));
          validateAssociationEnd(((EdmAssociationSetImpl) associationSet).getEnd2(), associationsMap.get(association));
          boolean end1 = false;
          boolean end2 = false;
          for (EdmEntitySet entitySet : container.getValue().getEntitySets()) {
            EdmAssociationSetEnd associationSetEnd1 = ((EdmAssociationSetImpl) associationSet).getEnd1();
            if (entitySet.getName().equals(((EdmAssociationSetEndImpl) associationSetEnd1).getEntitySetName())) {
              end1 = true;
            }
            EdmAssociationSetEnd associationSetEnd2 = ((EdmAssociationSetImpl) associationSet).getEnd2();
            if (entitySet.getName().equals(((EdmAssociationSetEndImpl) associationSetEnd2).getEntitySetName())) {
              end2 = true;
            }
          }
          if (!(end1 && end2)) {
            throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
                .addContent("Invalid AssociationSet"));
          }
        } else {
          throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
              .addContent("Invalid AssociationSet"));
        }
      }
    }

  }

  private void validateAssociationEnd(final EdmAssociationSetEnd end, final EdmAssociation association)
      throws EntityProviderException, EdmException {
    if (!(association.getEnd1().getRole().equals(end.getRole()) ^ association
        .getEnd2().getRole().equals(end.getRole()))) {
      throw new EntityProviderException(EntityProviderException.COMMON.addContent("Invalid Association"));
    }
  }

  private void validateEntitySet() throws EntityProviderException, EdmException {
    for (Map.Entry<FullQualifiedName, EdmEntityContainer> container : containerMap.entrySet()) {
      for (EdmEntitySet entitySet : container.getValue().getEntitySets()) {
        FullQualifiedName entityType = extractFQNameFromEntityType(entitySet.getEntityType());
        if (!(entityTypesMap.containsKey(entityType))) {
          validateEntityTypeWithAlias(entityType);
        }
      }
    }
  }

  private void validate() throws EntityProviderException, EdmException {
    checkMandatoryNamespacesAvailable();
    validateEntityTypes();
    validateComplexTypes();
    validateRelationship();
    validateEntitySet();
    validateAssociation();
  }

  private void initialize() {
    xmlNamespaceMap = new HashMap<String, String>();
    mandatoryNamespaces = new HashMap<String, String>();
    mandatoryNamespaces.put(Edm.PREFIX_EDMX, Edm.NAMESPACE_EDMX_2007_06);
    mandatoryNamespaces.put(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);
    edmNamespaces = new HashSet<String>();
    edmNamespaces.add(Edm.NAMESPACE_EDM_2006_04);
    edmNamespaces.add(Edm.NAMESPACE_EDM_2007_05);
    edmNamespaces.add(Edm.NAMESPACE_EDM_2008_01);
    edmNamespaces.add(Edm.NAMESPACE_EDM_2008_09);

  }
}
