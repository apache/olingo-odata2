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
package org.apache.olingo.odata2.core.ep.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAction;
import org.apache.olingo.odata2.api.edm.EdmConcurrencyMode;
import org.apache.olingo.odata2.api.edm.EdmContentKind;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
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
import org.apache.olingo.odata2.api.edm.provider.CustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.provider.DataServices;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.FunctionImportParameter;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.OnDelete;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraint;
import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraintRole;
import org.apache.olingo.odata2.api.edm.provider.ReturnType;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.api.edm.provider.Using;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.core.ep.util.XmlMetadataConstants;

public class XmlMetadataConsumer {

  private Map<String, Set<String>> inscopeMap = new HashMap<String, Set<String>>();
  private Map<String, String> aliasNamespaceMap = new HashMap<String, String>();
  private Map<String, String> xmlNamespaceMap;
  private Map<String, String> mandatoryNamespaces;
  private Map<FullQualifiedName, EntityType> entityTypesMap = new HashMap<FullQualifiedName, EntityType>();
  private Map<FullQualifiedName, ComplexType> complexTypesMap = new HashMap<FullQualifiedName, ComplexType>();
  private Map<FullQualifiedName, Association> associationsMap = new HashMap<FullQualifiedName, Association>();
  private Map<FullQualifiedName, EntityContainer> containerMap = new HashMap<FullQualifiedName, EntityContainer>();
  private List<NavigationProperty> navProperties = new ArrayList<NavigationProperty>();
  private String currentHandledStartTagName;
  private String currentNamespace;
  private String edmNamespace = Edm.NAMESPACE_EDM_2008_09;
  private Set<String> edmNamespaces;

  public DataServices readMetadata(final XMLStreamReader reader, final boolean validate)
      throws EntityProviderException {
    try {
      initialize();
      DataServices dataServices = new DataServices();
      List<Schema> schemas = new ArrayList<Schema>();

      while (reader.hasNext()
          && !(reader.isEndElement() && Edm.NAMESPACE_EDMX_2007_06.equals(reader.getNamespaceURI())
          && XmlMetadataConstants.EDMX_TAG.equals(reader.getLocalName()))) {
        reader.next();
        if (reader.isStartElement()) {
          extractNamespaces(reader);
          if (XmlMetadataConstants.EDM_SCHEMA.equals(reader.getLocalName())) {
            edmNamespace = reader.getNamespaceURI();
            checkEdmNamespace();
            schemas.add(readSchema(reader));
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

      if (validate) {
        validate();
      }
      dataServices.setSchemas(schemas);
      reader.close();
      return dataServices;
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }

  }

  private Schema readSchema(final XMLStreamReader reader) throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_SCHEMA);

    Schema schema = new Schema();
    List<Using> usings = new ArrayList<Using>();
    List<ComplexType> complexTypes = new ArrayList<ComplexType>();
    List<EntityType> entityTypes = new ArrayList<EntityType>();
    List<Association> associations = new ArrayList<Association>();
    List<EntityContainer> entityContainers = new ArrayList<EntityContainer>();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();

    schema.setNamespace(reader.getAttributeValue(null, XmlMetadataConstants.EDM_SCHEMA_NAMESPACE));
    inscopeMap.put(schema.getNamespace(), new HashSet<String>());
    schema.setAlias(reader.getAttributeValue(null, XmlMetadataConstants.EDM_SCHEMA_ALIAS));
    schema.setAnnotationAttributes(readAnnotationAttribute(reader));
    currentNamespace = schema.getNamespace();
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_SCHEMA.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_USING.equals(currentHandledStartTagName)) {
          usings.add(readUsing(reader, schema.getNamespace()));
        } else if (XmlMetadataConstants.EDM_ENTITY_TYPE.equals(currentHandledStartTagName)) {
          entityTypes.add(readEntityType(reader));
        } else if (XmlMetadataConstants.EDM_COMPLEX_TYPE.equals(currentHandledStartTagName)) {
          complexTypes.add(readComplexType(reader));
        } else if (XmlMetadataConstants.EDM_ASSOCIATION.equals(currentHandledStartTagName)) {
          associations.add(readAssociation(reader));
        } else if (XmlMetadataConstants.EDM_ENTITY_CONTAINER.equals(currentHandledStartTagName)) {
          entityContainers.add(readEntityContainer(reader));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (schema.getAlias() != null) {
      aliasNamespaceMap.put(schema.getAlias(), schema.getNamespace());
    }
    if (!annotationElements.isEmpty()) {
      schema.setAnnotationElements(annotationElements);
    }
    schema.setUsings(usings).setEntityTypes(entityTypes).setComplexTypes(complexTypes).setAssociations(associations)
        .setEntityContainers(entityContainers);
    return schema;
  }

  private Using readUsing(final XMLStreamReader reader, final String schemaNamespace)
      throws XMLStreamException, EntityProviderException {

    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_USING);

    Using using = new Using();
    using.setNamespace(reader.getAttributeValue(null, XmlMetadataConstants.EDM_SCHEMA_NAMESPACE));
    inscopeMap.get(schemaNamespace).add(using.getNamespace());
    using.setAlias(reader.getAttributeValue(null, XmlMetadataConstants.EDM_SCHEMA_ALIAS));
    using.setAnnotationAttributes(readAnnotationAttribute(reader));

    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
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

  private EntityContainer readEntityContainer(final XMLStreamReader reader)
      throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ENTITY_CONTAINER);
    EntityContainer container = new EntityContainer();
    List<EntitySet> entitySets = new ArrayList<EntitySet>();
    List<AssociationSet> associationSets = new ArrayList<AssociationSet>();
    List<FunctionImport> functionImports = new ArrayList<FunctionImport>();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();

    container.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    if (reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.EDM_CONTAINER_IS_DEFAULT) != null) {
      container.setDefaultEntityContainer("true".equalsIgnoreCase(reader.getAttributeValue(Edm.NAMESPACE_M_2007_08,
          "IsDefaultEntityContainer")));
    }
    container.setExtendz(reader.getAttributeValue(null, XmlMetadataConstants.EDM_CONTAINER_EXTENDZ));
    container.setAnnotationAttributes(readAnnotationAttribute(reader));

    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_ENTITY_CONTAINER.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_ENTITY_SET.equals(currentHandledStartTagName)) {
          entitySets.add(readEntitySet(reader));
        } else if (XmlMetadataConstants.EDM_ASSOCIATION_SET.equals(currentHandledStartTagName)) {
          associationSets.add(readAssociationSet(reader));
        } else if (XmlMetadataConstants.EDM_FUNCTION_IMPORT.equals(currentHandledStartTagName)) {
          functionImports.add(readFunctionImport(reader));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (!annotationElements.isEmpty()) {
      container.setAnnotationElements(annotationElements);
    }
    container.setEntitySets(entitySets).setAssociationSets(associationSets).setFunctionImports(functionImports);

    containerMap.put(new FullQualifiedName(currentNamespace, container.getName()), container);
    return container;
  }

  private FunctionImport readFunctionImport(final XMLStreamReader reader)
      throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_FUNCTION_IMPORT);
    FunctionImport function = new FunctionImport();
    List<FunctionImportParameter> functionParameters = new ArrayList<FunctionImportParameter>();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();

    function.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    function.setHttpMethod(reader.getAttributeValue(Edm.NAMESPACE_M_2007_08,
        XmlMetadataConstants.EDM_FUNCTION_IMPORT_HTTP_METHOD));
    function.setEntitySet(reader.getAttributeValue(null, XmlMetadataConstants.EDM_ENTITY_SET));

    String returnTypeString = reader.getAttributeValue(null, XmlMetadataConstants.EDM_FUNCTION_IMPORT_RETURN);
    if (returnTypeString != null) {
      ReturnType returnType = new ReturnType();
      if (returnTypeString.startsWith("Collection") || returnTypeString.startsWith("collection")) {
        returnType.setMultiplicity(EdmMultiplicity.MANY);
        returnTypeString = returnTypeString.substring(returnTypeString.indexOf("(") + 1, returnTypeString.length() - 1);
      } else {
        returnType.setMultiplicity(EdmMultiplicity.ONE);
      }
      FullQualifiedName fqName = extractFQName(returnTypeString);
      returnType.setTypeName(fqName);
      function.setReturnType(returnType);
    }
    function.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_FUNCTION_IMPORT.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_FUNCTION_PARAMETER.equals(currentHandledStartTagName)) {
          functionParameters.add(readFunctionImportParameter(reader));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (!annotationElements.isEmpty()) {
      function.setAnnotationElements(annotationElements);
    }
    function.setParameters(functionParameters);
    return function;
  }

  private FunctionImportParameter readFunctionImportParameter(final XMLStreamReader reader)
      throws EntityProviderException, XMLStreamException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_FUNCTION_PARAMETER);
    FunctionImportParameter functionParameter = new FunctionImportParameter();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();

    functionParameter.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    functionParameter.setMode(reader.getAttributeValue(null, XmlMetadataConstants.EDM_FUNCTION_PARAMETER_MODE));
    String type = reader.getAttributeValue(null, XmlMetadataConstants.EDM_TYPE);
    if (type == null) {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_TYPE).addContent(XmlMetadataConstants.EDM_FUNCTION_PARAMETER));
    }
    functionParameter.setType(EdmSimpleTypeKind.valueOf(extractFQName(type).getName()));
    functionParameter.setFacets(readFacets(reader));
    functionParameter.setAnnotationAttributes(readAnnotationAttribute(reader));
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
      functionParameter.setAnnotationElements(annotationElements);
    }
    return functionParameter;
  }

  private AssociationSet readAssociationSet(final XMLStreamReader reader)
      throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ASSOCIATION_SET);
    AssociationSet associationSet = new AssociationSet();
    List<AssociationSetEnd> ends = new ArrayList<AssociationSetEnd>();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();

    associationSet.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    String association = reader.getAttributeValue(null, XmlMetadataConstants.EDM_ASSOCIATION);
    if (association != null) {
      associationSet.setAssociation(extractFQName(association));
    } else {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_ASSOCIATION).addContent(XmlMetadataConstants.EDM_ASSOCIATION_SET));
    }
    associationSet.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_ASSOCIATION_SET.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_ASSOCIATION_END.equals(currentHandledStartTagName)) {
          AssociationSetEnd associationSetEnd = new AssociationSetEnd();
          associationSetEnd.setEntitySet(reader.getAttributeValue(null, XmlMetadataConstants.EDM_ENTITY_SET));
          associationSetEnd.setRole(reader.getAttributeValue(null, XmlMetadataConstants.EDM_ROLE));
          ends.add(associationSetEnd);
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (ends.size() != 2) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
          .addContent("Count of AssociationSet ends should be 2"));
    } else {
      associationSet.setEnd1(ends.get(0)).setEnd2(ends.get(1));
    }
    if (!annotationElements.isEmpty()) {
      associationSet.setAnnotationElements(annotationElements);
    }
    return associationSet;
  }

  private EntitySet readEntitySet(final XMLStreamReader reader) throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ENTITY_SET);
    EntitySet entitySet = new EntitySet();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    entitySet.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    String entityType = reader.getAttributeValue(null, XmlMetadataConstants.EDM_ENTITY_TYPE);
    if (entityType != null) {
      FullQualifiedName fqName = extractFQName(entityType);
      entitySet.setEntityType(fqName);
    } else {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_ENTITY_TYPE).addContent(XmlMetadataConstants.EDM_ENTITY_SET));
    }
    entitySet.setAnnotationAttributes(readAnnotationAttribute(reader));
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
      entitySet.setAnnotationElements(annotationElements);
    }
    return entitySet;
  }

  private Association readAssociation(final XMLStreamReader reader) throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ASSOCIATION);

    Association association = new Association();
    association.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    List<AssociationEnd> associationEnds = new ArrayList<AssociationEnd>();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    association.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_ASSOCIATION.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_ASSOCIATION_END.equals(currentHandledStartTagName)) {
          associationEnds.add(readAssociationEnd(reader));
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
      association.setAnnotationElements(annotationElements);
    }
    association.setEnd1(associationEnds.get(0)).setEnd2(associationEnds.get(1));
    associationsMap.put(new FullQualifiedName(currentNamespace, association.getName()), association);
    return association;
  }

  private ReferentialConstraint readReferentialConstraint(final XMLStreamReader reader) throws XMLStreamException,
      EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ASSOCIATION_CONSTRAINT);
    ReferentialConstraint refConstraint = new ReferentialConstraint();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    refConstraint.setAnnotationAttributes(readAnnotationAttribute(reader));
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
      refConstraint.setAnnotationElements(annotationElements);
    }
    return refConstraint;
  }

  private ReferentialConstraintRole readReferentialConstraintRole(final XMLStreamReader reader)
      throws EntityProviderException, XMLStreamException {
    ReferentialConstraintRole rcRole = new ReferentialConstraintRole();
    rcRole.setRole(reader.getAttributeValue(null, XmlMetadataConstants.EDM_ROLE));
    List<PropertyRef> propertyRefs = new ArrayList<PropertyRef>();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    rcRole.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext() && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && (XmlMetadataConstants.EDM_ASSOCIATION_PRINCIPAL.equals(reader.getLocalName())
        || XmlMetadataConstants.EDM_ASSOCIATION_DEPENDENT.equals(reader.getLocalName())))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_PROPERTY_REF.equals(currentHandledStartTagName)) {
          propertyRefs.add(readPropertyRef(reader));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (!annotationElements.isEmpty()) {
      rcRole.setAnnotationElements(annotationElements);
    }
    rcRole.setPropertyRefs(propertyRefs);
    return rcRole;
  }

  private ComplexType readComplexType(final XMLStreamReader reader) throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_COMPLEX_TYPE);

    ComplexType complexType = new ComplexType();
    List<Property> properties = new ArrayList<Property>();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    complexType.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    String baseType = reader.getAttributeValue(null, XmlMetadataConstants.EDM_BASE_TYPE);
    if (baseType != null) {
      complexType.setBaseType(extractFQName(baseType));
    }
    if (reader.getAttributeValue(null, XmlMetadataConstants.EDM_TYPE_ABSTRACT) != null) {
      complexType.setAbstract("true".equalsIgnoreCase(reader.getAttributeValue(null,
          XmlMetadataConstants.EDM_TYPE_ABSTRACT)));
    }
    complexType.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_COMPLEX_TYPE.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_PROPERTY.equals(currentHandledStartTagName)) {
          properties.add(readProperty(reader));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
      }
    }
    if (!annotationElements.isEmpty()) {
      complexType.setAnnotationElements(annotationElements);
    }
    complexType.setProperties(properties);
    if (complexType.getName() != null) {
      FullQualifiedName fqName = new FullQualifiedName(currentNamespace, complexType.getName());
      complexTypesMap.put(fqName, complexType);
    } else {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE.addContent("Name"));
    }
    return complexType;

  }

  private EntityType readEntityType(final XMLStreamReader reader) throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ENTITY_TYPE);
    EntityType entityType = new EntityType();
    List<Property> properties = new ArrayList<Property>();
    List<NavigationProperty> navPropertiesList = new ArrayList<NavigationProperty>();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    Key key = null;

    entityType.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
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
      entityType.setBaseType(extractFQName(baseType));
    }
    entityType.setCustomizableFeedMappings(readCustomizableFeedMappings(reader));
    entityType.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_ENTITY_TYPE.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_ENTITY_TYPE_KEY.equals(currentHandledStartTagName)) {
          key = readEntityTypeKey(reader);
        } else if (XmlMetadataConstants.EDM_PROPERTY.equals(currentHandledStartTagName)) {
          properties.add(readProperty(reader));
        } else if (XmlMetadataConstants.EDM_NAVIGATION_PROPERTY.equals(currentHandledStartTagName)) {
          navPropertiesList.add(readNavigationProperty(reader));
        } else {
          annotationElements.add(readAnnotationElement(reader));
        }
        extractNamespaces(reader);
      }
    }
    if (!annotationElements.isEmpty()) {
      entityType.setAnnotationElements(annotationElements);
    }
    entityType.setKey(key).setProperties(properties).setNavigationProperties(navPropertiesList);
    if (entityType.getName() != null) {
      FullQualifiedName fqName = new FullQualifiedName(currentNamespace, entityType.getName());
      entityTypesMap.put(fqName, entityType);
    } else {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE.addContent("Name"));
    }
    return entityType;
  }

  private Key readEntityTypeKey(final XMLStreamReader reader) throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ENTITY_TYPE_KEY);
    List<PropertyRef> keys = new ArrayList<PropertyRef>();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    List<AnnotationAttribute> annotationAttributes = readAnnotationAttribute(reader);
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
    Key key = new Key().setKeys(keys).setAnnotationAttributes(annotationAttributes);
    if (!annotationElements.isEmpty()) {
      key.setAnnotationElements(annotationElements);
    }
    return key;
  }

  private PropertyRef readPropertyRef(final XMLStreamReader reader) throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_PROPERTY_REF);
    PropertyRef propertyRef = new PropertyRef();
    propertyRef.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    propertyRef.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext() && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_PROPERTY_REF.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        annotationElements.add(readAnnotationElement(reader));
      }
    }
    if (!annotationElements.isEmpty()) {
      propertyRef.setAnnotationElements(annotationElements);
    }
    return propertyRef;
  }

  private NavigationProperty readNavigationProperty(final XMLStreamReader reader) throws XMLStreamException,
      EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_NAVIGATION_PROPERTY);

    NavigationProperty navProperty = new NavigationProperty();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    navProperty.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    String relationship = reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAVIGATION_RELATIONSHIP);
    if (relationship != null) {
      FullQualifiedName fqName = extractFQName(relationship);
      navProperty.setRelationship(fqName);

    } else {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_NAVIGATION_RELATIONSHIP).addContent(
              XmlMetadataConstants.EDM_NAVIGATION_PROPERTY));
    }

    navProperty.setFromRole(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAVIGATION_FROM_ROLE));
    navProperty.setToRole(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAVIGATION_TO_ROLE));
    navProperty.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext() && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_NAVIGATION_PROPERTY.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        annotationElements.add(readAnnotationElement(reader));
      }
    }
    if (!annotationElements.isEmpty()) {
      navProperty.setAnnotationElements(annotationElements);
    }
    navProperties.add(navProperty);
    return navProperty;
  }

  private Property readProperty(final XMLStreamReader reader) throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_PROPERTY);
    Property property;
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    String type = reader.getAttributeValue(null, XmlMetadataConstants.EDM_TYPE);
    if (type == null) {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_TYPE).addContent(XmlMetadataConstants.EDM_PROPERTY));
    }
    FullQualifiedName fqName = extractFQName(type);

    if (EdmSimpleType.EDM_NAMESPACE.equals(fqName.getNamespace())) {
      property = readSimpleProperty(reader, fqName);
    } else {
      property = readComplexProperty(reader, fqName);
    }
    property.setFacets(readFacets(reader));
    property.setCustomizableFeedMappings(readCustomizableFeedMappings(reader));
    property.setMimeType(reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_MIMETYPE));
    property.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext() && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_PROPERTY.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        annotationElements.add(readAnnotationElement(reader));
      }
    }
    if (!annotationElements.isEmpty()) {
      property.setAnnotationElements(annotationElements);
    }
    return property;
  }

  private Property readComplexProperty(final XMLStreamReader reader, final FullQualifiedName fqName)
      throws XMLStreamException {
    ComplexProperty property = new ComplexProperty();
    property.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    property.setType(fqName);
    return property;
  }

  private Property readSimpleProperty(final XMLStreamReader reader, final FullQualifiedName fqName)
      throws XMLStreamException {
    SimpleProperty property = new SimpleProperty();
    property.setName(reader.getAttributeValue(null, XmlMetadataConstants.EDM_NAME));
    property.setType(EdmSimpleTypeKind.valueOf(fqName.getName()));
    return property;
  }

  private Facets readFacets(final XMLStreamReader reader) throws XMLStreamException {
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
      Facets facets = new Facets();
      if (isNullable != null) {
        facets.setNullable("true".equalsIgnoreCase(isNullable));
      }
      if (maxLength != null) {
        if (XmlMetadataConstants.EDM_PROPERTY_MAX_LENGTH_MAX_VALUE_FIRST_UPPERCASE.equals(maxLength)
            || XmlMetadataConstants.EDM_PROPERTY_MAX_LENGTH_MAX_VALUE_LOWERCASE.equals(maxLength)) {
          facets.setMaxLength(Integer.MAX_VALUE);
        } else {
          facets.setMaxLength(Integer.parseInt(maxLength));
        }
      }
      if (precision != null) {
        facets.setPrecision(Integer.parseInt(precision));
      }
      if (scale != null) {
        facets.setScale(Integer.parseInt(scale));
      }
      if (isFixedLength != null) {
        facets.setFixedLength("true".equalsIgnoreCase(isFixedLength));
      }
      if (isUnicode != null) {
        facets.setUnicode("true".equalsIgnoreCase(isUnicode));
      }
      for (int i = 0; i < EdmConcurrencyMode.values().length; i++) {
        if (EdmConcurrencyMode.values()[i].name().equalsIgnoreCase(concurrencyMode)) {
          facets.setConcurrencyMode(EdmConcurrencyMode.values()[i]);
        }
      }
      facets.setDefaultValue(defaultValue);
      facets.setCollation(collation);
      return facets;
    } else {
      return null;
    }
  }

  private CustomizableFeedMappings readCustomizableFeedMappings(final XMLStreamReader reader) {
    String targetPath = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_TARGET_PATH);
    String sourcePath = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_SOURCE_PATH);
    String nsUri = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_NS_URI);
    String nsPrefix = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_PREFIX);
    String keepInContent = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_KEEP_IN_CONTENT);
    String contentKind = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_CONTENT_KIND);

    if (targetPath != null || sourcePath != null || nsUri != null || nsPrefix != null || keepInContent != null
        || contentKind != null) {
      CustomizableFeedMappings feedMapping = new CustomizableFeedMappings();
      if (keepInContent != null) {
        feedMapping.setFcKeepInContent("true".equals(keepInContent));
      }
      for (int i = 0; i < EdmContentKind.values().length; i++) {
        if (EdmContentKind.values()[i].name().equalsIgnoreCase(contentKind)) {
          feedMapping.setFcContentKind(EdmContentKind.values()[i]);
        }
      }
      feedMapping.setFcTargetPath(targetPath).setFcSourcePath(sourcePath).setFcNsUri(nsUri).setFcNsPrefix(nsPrefix);
      return feedMapping;
    } else {
      return null;
    }

  }

  private AssociationEnd readAssociationEnd(final XMLStreamReader reader) throws EntityProviderException,
      XMLStreamException {
    reader.require(XMLStreamConstants.START_ELEMENT, edmNamespace, XmlMetadataConstants.EDM_ASSOCIATION_END);

    AssociationEnd associationEnd = new AssociationEnd();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    associationEnd.setRole(reader.getAttributeValue(null, XmlMetadataConstants.EDM_ROLE));
    associationEnd.setMultiplicity(EdmMultiplicity.fromLiteral(reader.getAttributeValue(null,
        XmlMetadataConstants.EDM_ASSOCIATION_MULTIPLICITY)));
    String type = reader.getAttributeValue(null, XmlMetadataConstants.EDM_TYPE);
    if (type == null) {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent(XmlMetadataConstants.EDM_TYPE).addContent(XmlMetadataConstants.EDM_ASSOCIATION_END));
    }
    associationEnd.setType(extractFQName(type));
    associationEnd.setAnnotationAttributes(readAnnotationAttribute(reader));
    while (reader.hasNext() && !(reader.isEndElement() && edmNamespace.equals(reader.getNamespaceURI())
        && XmlMetadataConstants.EDM_ASSOCIATION_END.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extractNamespaces(reader);
        currentHandledStartTagName = reader.getLocalName();
        if (XmlMetadataConstants.EDM_ASSOCIATION_ONDELETE.equals(currentHandledStartTagName)) {
          OnDelete onDelete = new OnDelete();
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
      associationEnd.setAnnotationElements(annotationElements);
    }
    return associationEnd;
  }

  private AnnotationElement readAnnotationElement(final XMLStreamReader reader) throws XMLStreamException {
    AnnotationElement aElement = new AnnotationElement();
    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    List<AnnotationAttribute> annotationAttributes = new ArrayList<AnnotationAttribute>();
    aElement.setName(reader.getLocalName());
    String elementNamespace = reader.getNamespaceURI();
    if (!edmNamespaces.contains(elementNamespace)) {
      aElement.setPrefix(reader.getPrefix());
      aElement.setNamespace(elementNamespace);
    }
    for (int i = 0; i < reader.getAttributeCount(); i++) {
      AnnotationAttribute annotationAttribute = new AnnotationAttribute();
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
      aElement.setAttributes(annotationAttributes);
    }

    boolean justRead = false;
    if (reader.hasNext()) {
      reader.next();
      justRead = true;
    }

    while (justRead && !(reader.isEndElement() && aElement.getName() != null
        && aElement.getName().equals(reader.getLocalName()))) {
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
        aElement.setText(elementText);
      }
    }
    if (!annotationElements.isEmpty()) {
      aElement.setChildElements(annotationElements);
    }
    return aElement;
  }

  private List<AnnotationAttribute> readAnnotationAttribute(final XMLStreamReader reader) {
    List<AnnotationAttribute> annotationAttributes = new ArrayList<AnnotationAttribute>();
    for (int i = 0; i < reader.getAttributeCount(); i++) {
      String attributeNamespace = reader.getAttributeNamespace(i);
      if (attributeNamespace != null && !isDefaultNamespace(attributeNamespace)
          && !mandatoryNamespaces.containsValue(attributeNamespace)
          && !edmNamespaces.contains(attributeNamespace)) {
        annotationAttributes.add(new AnnotationAttribute().setName(reader.getAttributeLocalName(i)).
            setPrefix(reader.getAttributePrefix(i)).setNamespace(attributeNamespace).setText(
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

  private FullQualifiedName validateEntityTypeWithAlias(final FullQualifiedName aliasName)
      throws EntityProviderException {
    String namespace = aliasNamespaceMap.get(aliasName.getNamespace());
    FullQualifiedName fqName = new FullQualifiedName(namespace, aliasName.getName());
    if (!entityTypesMap.containsKey(fqName)) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT.addContent("Invalid Type"));
    }
    return fqName;
  }

  private void validateEntityTypes() throws EntityProviderException {
    for (Map.Entry<FullQualifiedName, EntityType> entityTypes : entityTypesMap.entrySet()) {
      if (entityTypes.getValue() != null && entityTypes.getKey() != null) {
        EntityType entityType = entityTypes.getValue();
        if (entityType.getBaseType() != null) {
          FullQualifiedName baseTypeFQName = entityType.getBaseType();
          EntityType baseEntityType;
          if (!entityTypesMap.containsKey(baseTypeFQName)) {
            FullQualifiedName fqName = validateEntityTypeWithAlias(baseTypeFQName);
            baseEntityType = entityTypesMap.get(fqName);
          } else {
            baseEntityType = fetchLastBaseType(baseTypeFQName,entityTypesMap);
          }
          if (baseEntityType != null && baseEntityType.getKey() == null) {
            throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
                .addContent("Missing key for EntityType " + baseEntityType.getName()));
          }
        } else if (entityType.getKey() == null) {
          throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
              .addContent("Missing key for EntityType " + entityType.getName()));
        }
      }
    }
  }

  
  /* This method gets the last base type of the EntityType 
   * which has key defined in order to validate it*/
   private EntityType fetchLastBaseType
   (FullQualifiedName baseTypeFQName, Map<FullQualifiedName, EntityType> entityTypesMap) 
       throws EntityProviderException {
     
     EntityType baseEntityType = null ;
     while(baseTypeFQName!=null){
       baseEntityType = entityTypesMap.get(baseTypeFQName);
       if(baseEntityType.getKey()!=null){
         break;
       }else if(baseEntityType !=null && baseEntityType.getBaseType() !=null){
           baseTypeFQName = baseEntityType.getBaseType();
       }else if(baseEntityType.getBaseType() == null){
         break;
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

  private void validateComplexTypes() throws EntityProviderException {
    for (Map.Entry<FullQualifiedName, ComplexType> complexTypes : complexTypesMap.entrySet()) {
      if (complexTypes.getValue() != null && complexTypes.getKey() != null) {
        ComplexType complexType = complexTypes.getValue();
        if (complexType.getBaseType() != null) {
          FullQualifiedName baseTypeFQName = complexType.getBaseType();
          if (!complexTypesMap.containsKey(baseTypeFQName)) {
            validateComplexTypeWithAlias(baseTypeFQName);
          }
        }
      }
    }
  }

  private void validateRelationship() throws EntityProviderException {
    for (NavigationProperty navProperty : navProperties) {
      if (associationsMap.containsKey(navProperty.getRelationship())) {
        Association assoc = associationsMap.get(navProperty.getRelationship());
        if (!(assoc.getEnd1().getRole().equals(navProperty.getFromRole())
            ^ assoc.getEnd1().getRole().equals(navProperty.getToRole())
            && (assoc.getEnd2().getRole().equals(navProperty.getFromRole()) ^ assoc.getEnd2().getRole().equals(
            navProperty.getToRole())))) {
          throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
              .addContent("Invalid end of association"));
        }
        if (!entityTypesMap.containsKey(assoc.getEnd1().getType())) {
          validateEntityTypeWithAlias(assoc.getEnd1().getType());
        }
        if (!entityTypesMap.containsKey(assoc.getEnd2().getType())) {
          validateEntityTypeWithAlias(assoc.getEnd2().getType());
        }
      } else {
        throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT.addContent("Invalid Relationship"));
      }
    }

  }

  private void validateAssociation() throws EntityProviderException {
    for (Map.Entry<FullQualifiedName, EntityContainer> container : containerMap.entrySet()) {
      for (AssociationSet associationSet : container.getValue().getAssociationSets()) {
        FullQualifiedName association = associationSet.getAssociation();
        if (associationsMap.containsKey(association)) {
          validateAssociationEnd(associationSet.getEnd1(), associationsMap.get(association));
          validateAssociationEnd(associationSet.getEnd2(), associationsMap.get(association));
          boolean end1 = false;
          boolean end2 = false;
          for (EntitySet entitySet : container.getValue().getEntitySets()) {
            if (entitySet.getName().equals(associationSet.getEnd1().getEntitySet())) {
              end1 = true;
            }
            if (entitySet.getName().equals(associationSet.getEnd2().getEntitySet())) {
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

  private void validateAssociationEnd(final AssociationSetEnd end, final Association association)
      throws EntityProviderException {
    if (!(association.getEnd1().getRole().equals(end.getRole()) ^ association
        .getEnd2().getRole().equals(end.getRole()))) {
      throw new EntityProviderException(EntityProviderException.COMMON.addContent("Invalid Association"));
    }
  }

  private void validateEntitySet() throws EntityProviderException {
    for (Map.Entry<FullQualifiedName, EntityContainer> container : containerMap.entrySet()) {
      for (EntitySet entitySet : container.getValue().getEntitySets()) {
        FullQualifiedName entityType = entitySet.getEntityType();
        if (!(entityTypesMap.containsKey(entityType))) {
          validateEntityTypeWithAlias(entityType);
        }
      }
    }
  }

  private void validate() throws EntityProviderException {
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
