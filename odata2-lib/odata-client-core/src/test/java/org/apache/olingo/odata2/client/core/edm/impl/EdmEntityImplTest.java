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
package org.apache.olingo.odata2.client.core.edm.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationSet;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmCustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAnnotationsImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmAssociationImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmComplexTypeImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmCustomizableFeedMappingsImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmDocumentationImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmEntityContainerImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmEntitySetImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmEntityTypeImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmMappingImpl;
import org.apache.olingo.odata2.client.core.edm.Impl.EdmNavigationPropertyImpl;
import org.junit.Before;
import org.junit.Test;

public class EdmEntityImplTest {

  private EdmAnnotationsImpl annotations;
  private EdmDocumentationImpl documentation;
  private EdmImpl edm;
  private EdmEntityContainerImpl edmEntityContainer;
  private EdmEntityTypeImpl edmEntityType;
  private FullQualifiedName fqName;
  private FullQualifiedName entityTypeName;
  private EdmMapping mapping;
  private String name;
  private EdmCustomizableFeedMappings edmCustomizableFeedMappings;
  private List<EdmProperty> edmKeyProperties;
  private List<String> edmKeyPropertyNames;
  private List<EdmNavigationProperty> navigationProperties;
  private EdmComplexTypeImpl structuralType;

  @Before
  public void initialize() throws EdmException {
    annotations = new EdmAnnotationsImpl();
    documentation = new EdmDocumentationImpl();
    edm = new EdmImpl();
    edmEntityContainer = new EdmEntityContainerImpl(edm);
    edmEntityType = new EdmEntityTypeImpl();
    fqName = new FullQualifiedName("namespace", "name");
    entityTypeName = new FullQualifiedName("namespace", "name");
    mapping = new EdmMappingImpl();
    name = "name";
    edmCustomizableFeedMappings = new EdmCustomizableFeedMappingsImpl();
    edmKeyProperties = new ArrayList<EdmProperty>();
    edmKeyPropertyNames = new ArrayList<String>();
    navigationProperties = new ArrayList<EdmNavigationProperty>();
    structuralType = new EdmComplexTypeImpl();
    structuralType.setName("name");
    structuralType.setNamespace("namespace");
    structuralType.setMapping(mapping);

    Map<FullQualifiedName, EdmComplexType> edmComplexTypes = new HashMap<FullQualifiedName, EdmComplexType>();
    edmComplexTypes.put(new FullQualifiedName("namespace", "name"), structuralType);
    edm.setEdmComplexTypes(edmComplexTypes);

    Map<FullQualifiedName, EdmAssociation> associationmap = new HashMap<FullQualifiedName, EdmAssociation>();
    associationmap.put(entityTypeName, new EdmAssociationImpl());
    edm.setEdmAssociations(associationmap);
    
    Map<String, String> aliasToNamespaceInfo = new HashMap<String, String>();
    aliasToNamespaceInfo.put("alias", "namespace");
    edm.setAliasToNamespaceInfo(aliasToNamespaceInfo );
  }

  @Test
  public void entitySetTest() throws EdmException {
    EdmEntitySetImpl entity = new EdmEntitySetImpl();
    entity.setAnnotations(annotations);
    entity.setDocumentation(documentation);
    entity.setEdm(edm);
    entity.setEdmEntityContainer(edmEntityContainer);
    entity.setEdmEntityType(edmEntityType);
    entity.setEdmEntityTypeName(fqName);
    entity.setEntityTypeName(entityTypeName);
    entity.setMapping(mapping);
    entity.setName(name);
    assertNotNull(entity);
    assertNotNull(entity.getAnnotations());
    assertNotNull(entity.getDocumentation());
    assertNotNull(entity.getEdmEntityContainer());
    assertNotNull(entity.getEdmEntityType());
    assertNotNull(entity.getEntityContainer());
    assertNotNull(entity.getEntityType());
    assertNotNull(entity.getEntityTypeName());
    assertNotNull(entity.getMapping());
    assertNotNull(entity.getName());

  }

  @Test
  public void entityTypeTest() throws EdmException {
    EdmEntityTypeImpl entity = new EdmEntityTypeImpl();
    entity.setAnnotations(annotations);
    entity.setAbstract(true);
    entity.setEdm(edm);
    entity.setBaseType(entityTypeName);
    entity.setCustomizableFeedMappings(edmCustomizableFeedMappings);
    entity.setEdmBaseType(edmEntityType);
    entity.setEdmKeyProperties(edmKeyProperties);
    entity.setEdmKeyPropertyNames(edmKeyPropertyNames);
    entity.setName(name);
    entity.setEdmNavigationPropertyNames(edmKeyPropertyNames);
    entity.setEdmPropertyNames(edmKeyPropertyNames);
    entity.setEdmTypeKind(EdmTypeKind.SIMPLE);
    entity.setHasStream(true);
    entity.setNamespace("namespace");
    entity.setNavigationProperties(navigationProperties);
    entity.setProperties(edmKeyProperties);
    entity.setStructuralType(structuralType);
    assertNotNull(entity);
    assertNotNull(entity.getAnnotations());
    assertNotNull(entity.getBaseType());
    assertNotNull(entity.getBaseTypeName());
    assertNotNull(entity.getCustomizableFeedMappings());
    assertNotNull(entity.getEdmBaseType());
    assertNotNull(entity.getEdmTypeKind());
    assertNotNull(entity.getKeyProperties());
    assertNotNull(entity.getKeyPropertyNames());
    assertNotNull(entity.getKind());
    assertNotNull(entity.getMapping());
    assertNotNull(entity.getName());
    assertNotNull(entity.getNamespace());
    assertNotNull(entity.getNavigationPropertyNames());
    assertNotNull(entity.getProperties());
    assertNotNull(entity.getPropertyNames());
    assertNotNull(entity.getStructuralType());

  }

  @Test
  public void entityContainerTest() throws EdmException {
    EdmEntityContainerImpl cont = new EdmEntityContainerImpl(edm);
    cont.setAnnotations(annotations);
    cont.setDefaultContainer(true);
    cont.setDocumentation(documentation);
    cont.setEdm(edm);
    cont.setEdmAssociationSets(new ArrayList<EdmAssociationSet>());
    List<EdmEntitySet> entitySets = new ArrayList<EdmEntitySet>();
    cont.setEdmEntitySets(entitySets);
    cont.setEdmExtendedEntityContainer(new EdmEntityContainerImpl(edm));
    cont.setEdmFunctionImports(new ArrayList<EdmFunctionImport>());
    cont.setEntityContainerHierachy(new ArrayList<EdmEntityContainer>());
    cont.setExtendz(name);
    cont.setName(name);
    assertNotNull(cont);
    EdmEntitySet entitySet = new EdmEntitySetImpl();
    ((EdmEntitySetImpl) entitySet).setName(name);
    EdmNavigationProperty nav = new EdmNavigationPropertyImpl();
    ((EdmNavigationPropertyImpl) nav).setEdm(edm);
    ((EdmNavigationPropertyImpl) nav).setRelationshipName(entityTypeName);
    entitySets.add(entitySet);
    assertNotNull(cont.getAssociationSets());
    assertNotNull(cont.getDocumentation());
    assertNotNull(cont.getEdm());
    assertNotNull(cont.getEdmAssociationSets());
    assertNotNull(cont.getEdmEntitySets());
    assertNotNull(cont.getEdmExtendedEntityContainer());
    assertNotNull(cont.getEdmFunctionImports());
    assertNotNull(cont.getEntitySet(name));
    assertNotNull(cont.getEntitySets());
    assertNotNull(cont.getExtendz());
    assertNull(cont.getFunctionImport(name));
    assertNotNull(cont.getName());
  }

  @Test
  public void complexTypeTest() throws EdmException {
    EdmComplexType complexType = edm.getComplexType("namespace", "name");
    assertNotNull(complexType);
    
    complexType = edm.getComplexType("alias", "name");
    assertNotNull(complexType);
  }
}
