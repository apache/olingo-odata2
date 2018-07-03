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
package org.apache.olingo.odata2.core.ep.producer;

import java.util.*;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmFacets;
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
import org.apache.olingo.odata2.api.edm.provider.Documentation;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.FunctionImportParameter;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.OnDelete;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraint;
import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraintRole;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.api.edm.provider.Using;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.util.XmlMetadataConstants;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

public class XmlMetadataProducer {

  public static void writeMetadata(final DataServices metadata, final XMLStreamWriter xmlStreamWriter,
      Map<String, String> predefinedNamespaces) throws EntityProviderException {

    try {
      String edmxNamespace = Edm.NAMESPACE_EDMX_2007_06;
      String defaultNamespace = Edm.NAMESPACE_EDM_2008_09;

      if (predefinedNamespaces == null) {
        predefinedNamespaces = new HashMap<String, String>();
      } else {
        String predefinedEdmxNamespace = predefinedNamespaces.get(Edm.PREFIX_EDMX);
        if (predefinedEdmxNamespace != null) {
          edmxNamespace = predefinedEdmxNamespace;
          predefinedNamespaces.remove(Edm.PREFIX_EDMX);
        }
        String predefinedDefaultNamespace = predefinedNamespaces.get(null);
        if (predefinedDefaultNamespace != null) {
          defaultNamespace = predefinedDefaultNamespace;
          predefinedNamespaces.remove(null);
        }
      }

      xmlStreamWriter.writeStartDocument();
      xmlStreamWriter.setPrefix(Edm.PREFIX_EDMX, edmxNamespace);
      xmlStreamWriter.setPrefix(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);

      xmlStreamWriter.writeStartElement(edmxNamespace, "Edmx");
      xmlStreamWriter.writeNamespace(Edm.PREFIX_EDMX, edmxNamespace);
      if(metadata.getCustomEdmxVersion() == null){
        xmlStreamWriter.writeAttribute("Version", "1.0");
      }else {
        xmlStreamWriter.writeAttribute("Version", metadata.getCustomEdmxVersion());
      }

      for (Map.Entry<String, String> entry : predefinedNamespaces.entrySet()) {
        xmlStreamWriter.writeNamespace(entry.getKey(), entry.getValue());
      }

      writeAnnotationElements(metadata.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);

      xmlStreamWriter.writeStartElement(edmxNamespace, XmlMetadataConstants.EDM_DATA_SERVICES);
      xmlStreamWriter.setDefaultNamespace(defaultNamespace);
      xmlStreamWriter.writeAttribute(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08,
          XmlMetadataConstants.EDM_DATA_SERVICE_VERSION, metadata.getDataServiceVersion());
      xmlStreamWriter.writeNamespace(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);

      Collection<Schema> schemas = metadata.getSchemas();
      if (schemas != null) {
        for (Schema schema : schemas) {
          xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_SCHEMA);
          if (schema.getAlias() != null) {
            xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_SCHEMA_ALIAS, schema.getAlias());
          }
          xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_SCHEMA_NAMESPACE, schema.getNamespace());
          xmlStreamWriter.writeDefaultNamespace(defaultNamespace);

          writeAnnotationAttributes(schema.getAnnotationAttributes(), predefinedNamespaces, null, xmlStreamWriter);

          Collection<Using> usings = schema.getUsings();
          if (usings != null) {
            for (Using using : usings) {
              xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_USING);
              xmlStreamWriter.writeAttribute("Namespace", using.getNamespace());
              xmlStreamWriter.writeAttribute("Alias", using.getAlias());
              writeAnnotationAttributes(using.getAnnotationAttributes(), predefinedNamespaces, null, xmlStreamWriter);
              writeDocumentation(using.getDocumentation(), predefinedNamespaces, xmlStreamWriter);
              writeAnnotationElements(using.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);
              xmlStreamWriter.writeEndElement();
            }
          }

          HashSet<String> ignored = new HashSet<String>();

          Collection<EntityType> entityTypes = schema.getEntityTypes();
          if (entityTypes != null) {
            for (EntityType entityType : entityTypes) {
              if (entityType.showMetadata()) {
                xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ENTITY_TYPE);
                xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, entityType.getName());
                if (entityType.getBaseType() != null) {
                  xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_BASE_TYPE, entityType.getBaseType().toString());
                }
                if (entityType.isAbstract()) {
                  xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_TYPE_ABSTRACT, "true");
                }
                if (entityType.isHasStream()) {
                  xmlStreamWriter.writeAttribute(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08,
                      XmlMetadataConstants.M_ENTITY_TYPE_HAS_STREAM, "true");
                }

                writeCustomizableFeedMappings(entityType.getCustomizableFeedMappings(), xmlStreamWriter);

                writeAnnotationAttributes(entityType.getAnnotationAttributes(), predefinedNamespaces, null,
                    xmlStreamWriter);

                writeDocumentation(entityType.getDocumentation(), predefinedNamespaces, xmlStreamWriter);

                Key key = entityType.getKey();
                if (key != null) {
                  xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ENTITY_TYPE_KEY);

                  writeAnnotationAttributes(key.getAnnotationAttributes(), predefinedNamespaces, null, xmlStreamWriter);

                  Collection<PropertyRef> propertyRefs = entityType.getKey().getKeys();
                  for (PropertyRef propertyRef : propertyRefs) {
                    xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_PROPERTY_REF);

                    writeAnnotationAttributes(propertyRef.getAnnotationAttributes(), predefinedNamespaces, null,
                        xmlStreamWriter);

                    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, propertyRef.getName());

                    writeAnnotationElements(propertyRef.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);

                    xmlStreamWriter.writeEndElement();
                  }

                  writeAnnotationElements(key.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);

                  xmlStreamWriter.writeEndElement();
                }

                Collection<Property> properties = entityType.getProperties();
                if (properties != null) {
                  writeProperties(properties, predefinedNamespaces, xmlStreamWriter);
                }

                Collection<NavigationProperty> navigationProperties = entityType.getNavigationProperties();
                if (navigationProperties != null) {
                  for (NavigationProperty navigationProperty : navigationProperties) {
                    xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_NAVIGATION_PROPERTY);
                    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, navigationProperty.getName());
                    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAVIGATION_RELATIONSHIP, navigationProperty
                        .getRelationship().toString());
                    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAVIGATION_FROM_ROLE, navigationProperty
                        .getFromRole());
                    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAVIGATION_TO_ROLE, navigationProperty
                        .getToRole());

                    writeAnnotationAttributes(navigationProperty.getAnnotationAttributes(), predefinedNamespaces, null,
                        xmlStreamWriter);

                    writeDocumentation(navigationProperty.getDocumentation(), predefinedNamespaces, xmlStreamWriter);

                    writeAnnotationElements(navigationProperty.getAnnotationElements(), predefinedNamespaces,
                        xmlStreamWriter);

                    xmlStreamWriter.writeEndElement();
                  }
                }

                writeAnnotationElements(entityType.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);

                xmlStreamWriter.writeEndElement();
              } else {
                ignored.add(entityType.getName());
                ignored.add(schema.getNamespace()+"."+entityType.getName());
              }
            }
          }

          Collection<ComplexType> complexTypes = schema.getComplexTypes();
          if (complexTypes != null) {
            for (ComplexType complexType : complexTypes) {
              xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_COMPLEX_TYPE);
              xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, complexType.getName());
              if (complexType.getBaseType() != null) {
                xmlStreamWriter
                    .writeAttribute(XmlMetadataConstants.EDM_BASE_TYPE, complexType.getBaseType().toString());
              }
              if (complexType.isAbstract()) {
                xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_TYPE_ABSTRACT, "true");
              }

              writeAnnotationAttributes(complexType.getAnnotationAttributes(), predefinedNamespaces, null,
                  xmlStreamWriter);

              writeDocumentation(complexType.getDocumentation(), predefinedNamespaces, xmlStreamWriter);

              Collection<Property> properties = complexType.getProperties();
              if (properties != null) {
                writeProperties(properties, predefinedNamespaces, xmlStreamWriter);
              }

              writeAnnotationElements(complexType.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);

              xmlStreamWriter.writeEndElement();
            }
          }

          Collection<Association> associations = schema.getAssociations();
          if (associations != null) {
            for (Association association : associations) {
              if (association.getEnd1() != null && ignored.contains(association.getEnd1().getRole())) {
                continue;
              }

              if (association.getEnd2() != null && ignored.contains(association.getEnd2().getRole())) {
                continue;
              }

              xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ASSOCIATION);
              xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, association.getName());

              writeAnnotationAttributes(association.getAnnotationAttributes(), predefinedNamespaces, null,
                  xmlStreamWriter);

              writeDocumentation(association.getDocumentation(), predefinedNamespaces, xmlStreamWriter);

              writeAssociationEnd(association.getEnd1(), predefinedNamespaces, xmlStreamWriter);
              writeAssociationEnd(association.getEnd2(), predefinedNamespaces, xmlStreamWriter);

              ReferentialConstraint referentialConstraint = association.getReferentialConstraint();
              if (referentialConstraint != null) {
                xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ASSOCIATION_CONSTRAINT);
                writeAnnotationAttributes(referentialConstraint.getAnnotationAttributes(), predefinedNamespaces, null,
                    xmlStreamWriter);
                writeDocumentation(referentialConstraint.getDocumentation(), predefinedNamespaces, xmlStreamWriter);

                ReferentialConstraintRole principal = referentialConstraint.getPrincipal();
                xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ASSOCIATION_PRINCIPAL);
                xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_ROLE, principal.getRole());
                writeAnnotationAttributes(principal.getAnnotationAttributes(), predefinedNamespaces, null,
                    xmlStreamWriter);

                for (PropertyRef propertyRef : principal.getPropertyRefs()) {
                  xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_PROPERTY_REF);
                  xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, propertyRef.getName());
                  xmlStreamWriter.writeEndElement();
                }
                writeAnnotationElements(principal.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);
                xmlStreamWriter.writeEndElement();

                ReferentialConstraintRole dependent = referentialConstraint.getDependent();
                xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ASSOCIATION_DEPENDENT);
                xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_ROLE, dependent.getRole());
                writeAnnotationAttributes(dependent.getAnnotationAttributes(), predefinedNamespaces, null,
                    xmlStreamWriter);

                for (PropertyRef propertyRef : dependent.getPropertyRefs()) {
                  xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_PROPERTY_REF);
                  xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, propertyRef.getName());
                  xmlStreamWriter.writeEndElement();
                }
                writeAnnotationElements(dependent.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);
                xmlStreamWriter.writeEndElement();

                writeAnnotationElements(referentialConstraint.getAnnotationElements(), predefinedNamespaces,
                    xmlStreamWriter);
                xmlStreamWriter.writeEndElement();
              }

              writeAnnotationElements(association.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);

              xmlStreamWriter.writeEndElement();
            }
          }

          Collection<EntityContainer> entityContainers = schema.getEntityContainers();
          if (entityContainers != null) {
            for (EntityContainer entityContainer : entityContainers) {
              xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ENTITY_CONTAINER);
              xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, entityContainer.getName());
              if (entityContainer.getExtendz() != null) {
                xmlStreamWriter
                    .writeAttribute(XmlMetadataConstants.EDM_CONTAINER_EXTENDZ, entityContainer.getExtendz());
              }
              if (entityContainer.isDefaultEntityContainer()) {
                xmlStreamWriter.writeAttribute(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08,
                    XmlMetadataConstants.EDM_CONTAINER_IS_DEFAULT, "true");
              }

              writeAnnotationAttributes(entityContainer.getAnnotationAttributes(), predefinedNamespaces, null,
                  xmlStreamWriter);

              writeDocumentation(entityContainer.getDocumentation(), predefinedNamespaces, xmlStreamWriter);

              Collection<EntitySet> entitySets = entityContainer.getEntitySets();
              if (entitySets != null) {
                for (EntitySet entitySet : entitySets) {
                  if (entitySet.showMetadata()) {
                    xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ENTITY_SET);
                    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, entitySet.getName());
                    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_ENTITY_TYPE, entitySet.getEntityType()
                        .toString());

                    writeAnnotationAttributes(entitySet.getAnnotationAttributes(), predefinedNamespaces, null,
                        xmlStreamWriter);

                    writeDocumentation(entitySet.getDocumentation(), predefinedNamespaces, xmlStreamWriter);

                    writeAnnotationElements(entitySet.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);

                    xmlStreamWriter.writeEndElement();
                  } else {
                    ignored.add(entitySet.getName());
                  }
                }
              }

              Collection<AssociationSet> associationSets = entityContainer.getAssociationSets();
              if (associationSets != null) {
                for (AssociationSet associationSet : associationSets) {
                  if (associationSet.getEnd1() != null && ignored.contains(associationSet.getEnd1().getRole())) {
                    continue;
                  }

                  if (associationSet.getEnd2() != null && ignored.contains(associationSet.getEnd2().getRole())) {
                    continue;
                  }

                  xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ASSOCIATION_SET);
                  xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, associationSet.getName());
                  xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_ASSOCIATION, associationSet.getAssociation()
                      .toString());

                  writeAnnotationAttributes(associationSet.getAnnotationAttributes(), predefinedNamespaces, null,
                      xmlStreamWriter);

                  writeDocumentation(associationSet.getDocumentation(), predefinedNamespaces, xmlStreamWriter);

                  writeAssociationSetEnd(associationSet.getEnd1(), predefinedNamespaces, xmlStreamWriter);
                  writeAssociationSetEnd(associationSet.getEnd2(), predefinedNamespaces, xmlStreamWriter);

                  writeAnnotationElements(associationSet.getAnnotationElements(), predefinedNamespaces,
                      xmlStreamWriter);

                  xmlStreamWriter.writeEndElement();
                }
              }

              Collection<FunctionImport> functionImports = entityContainer.getFunctionImports();
              if (functionImports != null) {
                for (FunctionImport functionImport : functionImports) {
                  xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_FUNCTION_IMPORT);
                  xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, functionImport.getName());
                  if (functionImport.getReturnType() != null) {
                    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_FUNCTION_IMPORT_RETURN, functionImport
                        .getReturnType().toString());
                  }
                  if (functionImport.getEntitySet() != null) {
                    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_ENTITY_SET, functionImport.getEntitySet());
                  }
                  if (functionImport.getHttpMethod() != null) {
                    xmlStreamWriter.writeAttribute(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08,
                        XmlMetadataConstants.EDM_FUNCTION_IMPORT_HTTP_METHOD, functionImport.getHttpMethod());
                  }

                  writeAnnotationAttributes(functionImport.getAnnotationAttributes(), predefinedNamespaces, null,
                      xmlStreamWriter);

                  writeDocumentation(functionImport.getDocumentation(), predefinedNamespaces, xmlStreamWriter);

                  Collection<FunctionImportParameter> functionImportParameters = functionImport.getParameters();
                  if (functionImportParameters != null) {
                    for (FunctionImportParameter functionImportParameter : functionImportParameters) {
                      xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_FUNCTION_PARAMETER);
                      xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, functionImportParameter.getName());
                      xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_TYPE, functionImportParameter.getType()
                          .getFullQualifiedName().toString());
                      if (functionImportParameter.getMode() != null) {
                        xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_FUNCTION_PARAMETER_MODE,
                            functionImportParameter.getMode());
                      }

                      writeFacets(xmlStreamWriter, functionImportParameter.getFacets());

                      writeAnnotationAttributes(functionImportParameter.getAnnotationAttributes(),
                          predefinedNamespaces, null, xmlStreamWriter);

                      writeDocumentation(functionImportParameter.getDocumentation(), predefinedNamespaces,
                          xmlStreamWriter);

                      writeAnnotationElements(functionImportParameter.getAnnotationElements(), predefinedNamespaces,
                          xmlStreamWriter);

                      xmlStreamWriter.writeEndElement();
                    }
                  }

                  writeAnnotationElements(functionImport.getAnnotationElements(), predefinedNamespaces,
                      xmlStreamWriter);

                  xmlStreamWriter.writeEndElement();
                }
              }

              writeAnnotationElements(entityContainer.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);

              xmlStreamWriter.writeEndElement();
            }
          }

          writeAnnotationElements(schema.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);

          xmlStreamWriter.writeEndElement();
        }
      }

      xmlStreamWriter.writeEndElement();
      xmlStreamWriter.writeEndElement();
      xmlStreamWriter.writeEndDocument();

      xmlStreamWriter.flush();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    } catch (FactoryConfigurationError e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  private static void writeCustomizableFeedMappings(final CustomizableFeedMappings customizableFeedMappings,
      final XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
    if (customizableFeedMappings != null) {
      if (customizableFeedMappings.getFcKeepInContent() != null) {
        xmlStreamWriter.writeAttribute(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08,
            XmlMetadataConstants.M_FC_KEEP_IN_CONTENT, customizableFeedMappings.getFcKeepInContent().toString()
                .toLowerCase(Locale.ROOT));
      }
      if (customizableFeedMappings.getFcContentKind() != null) {
        xmlStreamWriter.writeAttribute(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_CONTENT_KIND,
            customizableFeedMappings.getFcContentKind().toString());
      }
      if (customizableFeedMappings.getFcNsPrefix() != null) {
        xmlStreamWriter.writeAttribute(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_PREFIX,
            customizableFeedMappings.getFcNsPrefix());
      }
      if (customizableFeedMappings.getFcNsUri() != null) {
        xmlStreamWriter.writeAttribute(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_NS_URI,
            customizableFeedMappings.getFcNsUri());
      }
      if (customizableFeedMappings.getFcSourcePath() != null) {
        xmlStreamWriter.writeAttribute(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_SOURCE_PATH,
            customizableFeedMappings.getFcSourcePath());
      }
      if (customizableFeedMappings.getFcTargetPath() != null) {
        xmlStreamWriter.writeAttribute(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_FC_TARGET_PATH,
            customizableFeedMappings.getFcTargetPath().toString());
      }
    }
  }

  private static void writeProperties(final Collection<Property> properties,
      final Map<String, String> predefinedNamespaces, final XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
    for (Property property : properties) {
      xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_PROPERTY);
      xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_NAME, property.getName());
      if (property instanceof SimpleProperty) {
        xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_TYPE, ((SimpleProperty) property).getType()
            .getFullQualifiedName().toString());
      } else if (property instanceof ComplexProperty) {
        xmlStreamWriter
            .writeAttribute(XmlMetadataConstants.EDM_TYPE, ((ComplexProperty) property).getType().toString());
      } else {
        throw new ODataRuntimeException();
      }

      writeFacets(xmlStreamWriter, property.getFacets());

      if (property.getMimeType() != null) {
        xmlStreamWriter.writeAttribute(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08, XmlMetadataConstants.M_MIMETYPE, property
            .getMimeType());
      }

      writeCustomizableFeedMappings(property.getCustomizableFeedMappings(), xmlStreamWriter);

      writeAnnotationAttributes(property.getAnnotationAttributes(), predefinedNamespaces, null, xmlStreamWriter);

      writeDocumentation(property.getDocumentation(), predefinedNamespaces, xmlStreamWriter);

      writeAnnotationElements(property.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);

      xmlStreamWriter.writeEndElement();
    }
  }

  private static void writeFacets(final XMLStreamWriter xmlStreamWriter, final EdmFacets facets)
      throws XMLStreamException {
    if (facets != null) {
      if (facets.isNullable() != null) {
        xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_PROPERTY_NULLABLE, facets.isNullable().toString()
            .toLowerCase(Locale.ROOT));
      }
      if (facets.getDefaultValue() != null) {
        xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_PROPERTY_DEFAULT_VALUE, facets.getDefaultValue());
      }
      if (facets.getMaxLength() != null) {
        xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_PROPERTY_MAX_LENGTH, facets.getMaxLength().toString());
      }
      if (facets.isFixedLength() != null) {
        xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_PROPERTY_FIXED_LENGTH, facets.isFixedLength()
            .toString().toLowerCase(Locale.ROOT));
      }
      if (facets.getPrecision() != null) {
        xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_PROPERTY_PRECISION, facets.getPrecision().toString());
      }
      if (facets.getScale() != null) {
        xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_PROPERTY_SCALE, facets.getScale().toString());
      }
      if (facets.isUnicode() != null) {
        xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_PROPERTY_UNICODE, facets.isUnicode().toString());
      }
      if (facets.getCollation() != null) {
        xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_PROPERTY_COLLATION, facets.getCollation());
      }
      if (facets.getConcurrencyMode() != null) {
        xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_PROPERTY_CONCURRENCY_MODE, facets.getConcurrencyMode()
            .toString());
      }
    }
  }

  private static void writeAssociationEnd(final AssociationEnd end, final Map<String, String> predefinedNamespaces,
      final XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
    xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ASSOCIATION_END);
    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_TYPE, end.getType().toString());
    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_ASSOCIATION_MULTIPLICITY, end.getMultiplicity().toString());
    if (end.getRole() != null) {
      xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_ROLE, end.getRole());
    }

    writeAnnotationAttributes(end.getAnnotationAttributes(), predefinedNamespaces, null, xmlStreamWriter);

    writeDocumentation(end.getDocumentation(), predefinedNamespaces, xmlStreamWriter);

    OnDelete onDelete = end.getOnDelete();
    if (onDelete != null) {
      xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ASSOCIATION_ONDELETE);
      xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_ONDELETE_ACTION, onDelete.getAction().toString());
      writeAnnotationAttributes(onDelete.getAnnotationAttributes(), predefinedNamespaces, null, xmlStreamWriter);
      writeDocumentation(onDelete.getDocumentation(), predefinedNamespaces, xmlStreamWriter);
      writeAnnotationElements(onDelete.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);
      xmlStreamWriter.writeEndElement();
    }

    writeAnnotationElements(end.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);

    xmlStreamWriter.writeEndElement();
  }

  private static void writeAssociationSetEnd(final AssociationSetEnd end,
      final Map<String, String> predefinedNamespaces, final XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
    xmlStreamWriter.writeStartElement(XmlMetadataConstants.EDM_ASSOCIATION_END);
    xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_ENTITY_SET, end.getEntitySet().toString());
    if (end.getRole() != null) {
      xmlStreamWriter.writeAttribute(XmlMetadataConstants.EDM_ROLE, end.getRole());
    }
    writeAnnotationAttributes(end.getAnnotationAttributes(), predefinedNamespaces, null, xmlStreamWriter);
    writeDocumentation(end.getDocumentation(), predefinedNamespaces, xmlStreamWriter);
    writeAnnotationElements(end.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);
    xmlStreamWriter.writeEndElement();
  }

  private static void writeDocumentation(final Documentation documentation,
      final Map<String, String> predefinedNamespaces, final XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
    if (documentation != null) {
      xmlStreamWriter.writeStartElement(XmlMetadataConstants.DOCUMENTATION);
      writeAnnotationAttributes(documentation.getAnnotationAttributes(), predefinedNamespaces, null, xmlStreamWriter);

      if (documentation.getSummary() != null) {
        xmlStreamWriter.writeStartElement(XmlMetadataConstants.SUMMARY);
        xmlStreamWriter.writeCharacters(documentation.getSummary());
        xmlStreamWriter.writeEndElement();
      }

      if (documentation.getLongDescription() != null) {
        xmlStreamWriter.writeStartElement(XmlMetadataConstants.LONG_DESCRIPTION);
        xmlStreamWriter.writeCharacters(documentation.getLongDescription());
        xmlStreamWriter.writeEndElement();
      }

      writeAnnotationElements(documentation.getAnnotationElements(), predefinedNamespaces, xmlStreamWriter);
      xmlStreamWriter.writeEndElement();
    }
  }

  private static void writeAnnotationAttributes(final Collection<AnnotationAttribute> annotationAttributes,
      final Map<String, String> predefinedNamespaces, ArrayList<String> setNamespaces,
      final XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
    if (annotationAttributes != null) {
      if (setNamespaces == null) {
        setNamespaces = new ArrayList<String>();
      }
      for (AnnotationAttribute annotationAttribute : annotationAttributes) {
        if (annotationAttribute.getNamespace() != null) {
          xmlStreamWriter.writeAttribute(annotationAttribute.getPrefix(), annotationAttribute.getNamespace(),
              annotationAttribute.getName(), annotationAttribute.getText());
          if (setNamespaces.contains(annotationAttribute.getNamespace()) == false
              && predefinedNamespaces.containsValue(annotationAttribute.getNamespace()) == false) {
            xmlStreamWriter.writeNamespace(annotationAttribute.getPrefix(), annotationAttribute.getNamespace());
            setNamespaces.add(annotationAttribute.getNamespace());
          }
        } else {
          xmlStreamWriter.writeAttribute(annotationAttribute.getName(), annotationAttribute.getText());
        }
      }
    }
  }

  private static void writeAnnotationElements(final Collection<AnnotationElement> annotationElements,
      final Map<String, String> predefinedNamespaces, final XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
    if (annotationElements != null) {
      for (AnnotationElement annotationElement : annotationElements) {
        ArrayList<String> setNamespaces = new ArrayList<String>();
        if (annotationElement.getNamespace() != null) {
          if (annotationElement.getPrefix() != null) {
            xmlStreamWriter.writeStartElement(annotationElement.getPrefix(), annotationElement.getName(),
                annotationElement.getNamespace());
            if (!predefinedNamespaces.containsValue(annotationElement.getNamespace())) {
              xmlStreamWriter.writeNamespace(annotationElement.getPrefix(), annotationElement.getNamespace());
              setNamespaces.add(annotationElement.getNamespace());
            }
          } else {
            xmlStreamWriter.writeStartElement("", annotationElement.getName(), annotationElement.getNamespace());
            if (!predefinedNamespaces.containsValue(annotationElement.getNamespace())) {
              xmlStreamWriter.writeNamespace("", annotationElement.getNamespace());
              setNamespaces.add(annotationElement.getNamespace());
            }
          }
        } else {
          xmlStreamWriter.writeStartElement(annotationElement.getName());
        }

        writeAnnotationAttributes(annotationElement.getAttributes(), predefinedNamespaces, setNamespaces,
            xmlStreamWriter);

        if (annotationElement.getChildElements() != null) {
          writeAnnotationElements(annotationElement.getChildElements(), predefinedNamespaces, xmlStreamWriter);
        } else {
          if (annotationElement.getText() != null) {
            xmlStreamWriter.writeCharacters(annotationElement.getText());
          }
        }

        xmlStreamWriter.writeEndElement();
      }
    }
  }
}
