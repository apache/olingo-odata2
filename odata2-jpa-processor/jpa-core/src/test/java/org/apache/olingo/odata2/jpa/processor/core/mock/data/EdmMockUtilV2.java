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
package org.apache.olingo.odata2.jpa.processor.core.mock.data;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationEnd;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock.JPARelatedTypeMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock.JPATypeEmbeddableMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock.JPATypeEmbeddableMock2;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.easymock.EasyMock;

public class EdmMockUtilV2 {

  public static interface JPAEdmMappingMock extends JPAEdmMapping, EdmMapping {

  }

  public static EdmEntityContainer mockEdmEntityContainer(final String entityName) throws EdmException {
    EdmEntityContainer entityContainer = EasyMock.createMock(EdmEntityContainer.class);
    entityContainer = EasyMock.createMock(EdmEntityContainer.class);
    EasyMock.expect(entityContainer.getEntitySet(JPATypeMock.ENTITY_NAME)).andReturn(
        mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false));
    EasyMock.expect(entityContainer.getFunctionImport(JPATypeMock.ENTITY_NAME)).andReturn(null);
    EasyMock.expect(entityContainer.getEntitySet("JPATypeMockInvalid")).andReturn(null);
    EasyMock.expect(entityContainer.getFunctionImport("JPATypeMockInvalid")).andReturn(null);
    EasyMock.replay(entityContainer);

    return entityContainer;
  }

  public static EdmEntityType mockEdmEntityType(final String entityName, final boolean withComplexType)
      throws EdmException {

    EdmEntityType entityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(entityType.getName()).andReturn(entityName).anyTimes();
    EasyMock.expect(entityType.getKeyPropertyNames()).andReturn(mockSimpleKeyPropertyNames(entityName));
    if (withComplexType == false) {
      EasyMock.expect(entityType.getPropertyNames()).andReturn(mockPropertyNames(entityName)).anyTimes();
    } else {
      EasyMock.expect(entityType.getPropertyNames()).andReturn(mockPropertyNamesWithComplexType(entityName)).anyTimes();
    }

    EasyMock.expect(entityType.getNavigationPropertyNames()).andReturn(mockNavigationPropertyNames(entityName))
        .anyTimes();
    EasyMock.expect(entityType.getKind()).andReturn(EdmTypeKind.ENTITY);
    EasyMock.expect(entityType.getMapping()).andReturn((EdmMapping) mockEdmMapping(entityName, null, null));
    EasyMock.expect(entityType.getKeyProperties()).andReturn(mockKeyProperties(entityName)).anyTimes();
    if (entityName.equals(JPATypeMock.ENTITY_NAME)) {
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_MINT)).andReturn(
          mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_MINT)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_ENUM)).andReturn(
          mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_ENUM)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_MSTRING)).andReturn(
          mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_MSTRING)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_MDATETIME)).andReturn(
          mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_MDATETIME)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_MBLOB)).andReturn(
          mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_MBLOB)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_CLOB)).andReturn(
          mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_CLOB)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_MC)).andReturn(
          mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_MC)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_MCARRAY)).andReturn(
          mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_MCARRAY)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_MCHAR)).andReturn(
          mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_MCHAR)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_MCHARARRAY)).andReturn(
          mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_MCHARARRAY)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_MCOMPLEXTYPE)).andReturn(
          mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_MCOMPLEXTYPE)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.PROPERTY_NAME_XMLADAPTER)).andReturn(
              mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_XMLADAPTER)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.NAVIGATION_PROPERTY_X)).andReturn(
          mockEdmNavigationProperty(JPATypeMock.NAVIGATION_PROPERTY_X, EdmMultiplicity.ONE)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPATypeMock.NAVIGATION_PROPERTY_XS)).andReturn(null).anyTimes();
      
    } else if (entityName.equals(JPARelatedTypeMock.ENTITY_NAME)) {
      EasyMock.expect(entityType.getProperty(JPARelatedTypeMock.PROPERTY_NAME_MLONG)).andReturn(
          mockEdmProperty(entityName, JPARelatedTypeMock.PROPERTY_NAME_MLONG)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPARelatedTypeMock.PROPERTY_NAME_MBYTE)).andReturn(
          mockEdmProperty(entityName, JPARelatedTypeMock.PROPERTY_NAME_MBYTE)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPARelatedTypeMock.PROPERTY_NAME_MBYTEARRAY)).andReturn(
          mockEdmProperty(entityName, JPARelatedTypeMock.PROPERTY_NAME_MBYTEARRAY)).anyTimes();
      EasyMock.expect(entityType.getProperty(JPARelatedTypeMock.PROPERTY_NAME_MDOUBLE)).andReturn(
          mockEdmProperty(entityName, JPARelatedTypeMock.PROPERTY_NAME_MDOUBLE)).anyTimes();
    }
    EasyMock.replay(entityType);
    return entityType;
  }

  public static List<EdmProperty> mockKeyProperties(final String entityName) throws EdmException {
    List<EdmProperty> edmProperties = new ArrayList<EdmProperty>();
    edmProperties.add(mockEdmProperty(entityName, JPATypeMock.PROPERTY_NAME_MINT));

    return edmProperties;
  }

  public static List<String> mockNavigationPropertyNames(final String entityName) {
    List<String> propertyNames = new ArrayList<String>();
    if (JPATypeMock.ENTITY_NAME.equals(entityName)) {

      propertyNames.add(JPATypeMock.NAVIGATION_PROPERTY_X);
      propertyNames.add(JPATypeMock.NAVIGATION_PROPERTY_XS);
    }
    return propertyNames;
  }

  public static List<String> mockSimpleKeyPropertyNames(final String entityName) {
    List<String> keyPropertyNames = new ArrayList<String>();
    if (entityName.equals(JPATypeMock.ENTITY_NAME)) {
      keyPropertyNames.add(JPATypeMock.PROPERTY_NAME_MINT);
    } else if (entityName.equals(JPARelatedTypeMock.ENTITY_NAME)) {
      keyPropertyNames.add(JPARelatedTypeMock.PROPERTY_NAME_MLONG);
    }

    return keyPropertyNames;
  }

  public static List<String> mockPropertyNames(final String entityName) {
    List<String> propertyNames = new ArrayList<String>();

    if (entityName.equals(JPATypeMock.ENTITY_NAME)) {
      propertyNames.add(JPATypeMock.PROPERTY_NAME_MINT);
      propertyNames.add(JPATypeMock.PROPERTY_NAME_MDATETIME);
      propertyNames.add(JPATypeMock.PROPERTY_NAME_MSTRING);
      propertyNames.add(JPATypeMock.PROPERTY_NAME_MBLOB);
      propertyNames.add(JPATypeMock.PROPERTY_NAME_CLOB);
      propertyNames.add(JPATypeMock.PROPERTY_NAME_MC);
      propertyNames.add(JPATypeMock.PROPERTY_NAME_MCARRAY);
      propertyNames.add(JPATypeMock.PROPERTY_NAME_MCHAR);
      propertyNames.add(JPATypeMock.PROPERTY_NAME_MCHARARRAY);
      propertyNames.add(JPATypeMock.PROPERTY_NAME_ENUM);
      propertyNames.add(JPATypeMock.PROPERTY_NAME_XMLADAPTER);
    } else if (entityName.equals(JPARelatedTypeMock.ENTITY_NAME)) {
      propertyNames.add(JPARelatedTypeMock.PROPERTY_NAME_MLONG);
      propertyNames.add(JPARelatedTypeMock.PROPERTY_NAME_MBYTE);
      propertyNames.add(JPARelatedTypeMock.PROPERTY_NAME_MBYTEARRAY);
      propertyNames.add(JPARelatedTypeMock.PROPERTY_NAME_MDOUBLE);
    } else if (entityName.equals(JPATypeEmbeddableMock.ENTITY_NAME)) {
      propertyNames.add(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MSHORT);
      propertyNames.add(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MDATE);
      propertyNames.add(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MDATE1);
      propertyNames.add(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MTIME);
      propertyNames.add(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MTIMESTAMP);
      propertyNames.add(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MEMBEDDABLE);
    } else if (entityName.equals(JPATypeEmbeddableMock2.ENTITY_NAME)) {
      propertyNames.add(JPATypeMock.JPATypeEmbeddableMock2.PROPERTY_NAME_MFLOAT);
      propertyNames.add(JPATypeMock.JPATypeEmbeddableMock2.PROPERTY_NAME_MUUID);
    }

    return propertyNames;
  }

  public static List<String> mockPropertyNamesWithComplexType(final String entityName) {
    List<String> propertyNames = mockPropertyNames(entityName);
    propertyNames.add(JPATypeMock.PROPERTY_NAME_MCOMPLEXTYPE);

    return propertyNames;

  }

  public static EdmAssociationEnd mockEdmAssociatioEnd(final String navigationPropertyName, final String role)
      throws EdmException {
    EdmAssociationEnd associationEnd = EasyMock.createMock(EdmAssociationEnd.class);
    EasyMock.expect(associationEnd.getMultiplicity()).andReturn(EdmMultiplicity.ONE);
    EdmEntityType entityType = EasyMock.createMock(EdmEntityType.class);
    EasyMock.expect(entityType.getMapping()).andReturn((EdmMapping) mockEdmMapping("JPARelatedTypeMock", null, null));
    EasyMock.replay(entityType);

    EasyMock.expect(associationEnd.getEntityType()).andReturn(entityType);
    EasyMock.replay(associationEnd);
    return associationEnd;
  }

  public static EdmAssociation mockEdmAssociation(final String navigationPropertyName) throws EdmException {
    EdmAssociation edmAssociation = EasyMock.createMock(EdmAssociation.class);
    EasyMock.expect(edmAssociation.getEnd("TO")).andReturn(mockEdmAssociatioEnd(navigationPropertyName, "TO"));
    EasyMock.expect(edmAssociation.getEnd2()).andReturn(mockEdmAssociatioEnd(navigationPropertyName, "TO"));
    EasyMock.expect(edmAssociation.getEnd("FROM")).andReturn(mockEdmAssociatioEnd(navigationPropertyName, "FROM"));
    EasyMock.replay(edmAssociation);
    return edmAssociation;
  }

  public static EdmEntitySet mockEdmEntitySet(final String entityName, final boolean withComplexType)
      throws EdmException {
    EdmEntitySet entitySet = null;
    if (entityName.equals(JPATypeMock.ENTITY_NAME)) {
      entitySet = EasyMock.createMock(EdmEntitySet.class);
      EasyMock.expect(entitySet.getEntityType()).andReturn(mockEdmEntityType(entityName, withComplexType)).anyTimes();
      EasyMock.expect(entitySet.getRelatedEntitySet(EasyMock.isA(EdmNavigationProperty.class))).andReturn(
          mockEdmEntitySet(JPARelatedTypeMock.ENTITY_NAME, false)).anyTimes();
    } else if (entityName.equals(JPARelatedTypeMock.ENTITY_NAME)) {
      entitySet = EasyMock.createMock(EdmEntitySet.class);
      EasyMock.expect(entitySet.getEntityType()).andReturn(mockEdmEntityType(entityName, withComplexType)).anyTimes();
    }

    EasyMock.replay(entitySet);
    return entitySet;
  }

  public static EdmNavigationProperty mockEdmNavigationProperty(final String navigationPropertyName,
      final EdmMultiplicity multiplicity) throws EdmException {

    EdmEntityType edmEntityType = mockEdmEntityType(JPARelatedTypeMock.ENTITY_NAME, false);

    EdmNavigationProperty navigationProperty = EasyMock.createMock(EdmNavigationProperty.class);
    EasyMock.expect(navigationProperty.getType()).andReturn(edmEntityType).anyTimes();
    EasyMock.expect(navigationProperty.getMultiplicity()).andReturn(multiplicity);
    EasyMock.expect(navigationProperty.getMapping()).andReturn(
        (EdmMapping) mockEdmMapping(null, null, navigationPropertyName)).anyTimes();
    EasyMock.expect(navigationProperty.getToRole()).andReturn("TO");
    EasyMock.expect(navigationProperty.getRelationship()).andReturn(mockEdmAssociation(navigationPropertyName))
        .anyTimes();
    if (multiplicity.equals(EdmMultiplicity.ONE)) {
      EasyMock.expect(navigationProperty.getName()).andReturn(JPATypeMock.NAVIGATION_PROPERTY_X).anyTimes();
    }

    EasyMock.replay(navigationProperty);

    return navigationProperty;
  }

  public static EdmProperty mockEdmProperty(final String entityName, final String propertyName) throws EdmException {
    EdmProperty edmProperty = EasyMock.createMock(EdmProperty.class);

    if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MINT) ||
        propertyName.equals(JPATypeMock.PROPERTY_NAME_ENUM) ||
        propertyName.equals(JPATypeMock.PROPERTY_NAME_MSTRING) ||
        propertyName.equals(JPATypeMock.PROPERTY_NAME_MDATETIME) ||
        propertyName.equals(JPATypeMock.PROPERTY_NAME_MBLOB) ||
        propertyName.equals(JPATypeMock.PROPERTY_NAME_CLOB) ||
        propertyName.equals(JPATypeMock.PROPERTY_NAME_MCARRAY) ||
        propertyName.equals(JPATypeMock.PROPERTY_NAME_MC) ||
        propertyName.equals(JPATypeMock.PROPERTY_NAME_MCHAR) ||
        propertyName.equals(JPATypeMock.PROPERTY_NAME_MCHARARRAY) ||
        propertyName.equals(JPATypeMock.PROPERTY_NAME_XMLADAPTER) ||
        propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MSHORT) ||
        propertyName.equals(JPATypeMock.JPATypeEmbeddableMock2.PROPERTY_NAME_MFLOAT) ||
        propertyName.equals(JPATypeMock.JPATypeEmbeddableMock2.PROPERTY_NAME_MUUID) ||
        propertyName.equals(JPARelatedTypeMock.PROPERTY_NAME_MLONG) ||
        propertyName.equals(JPARelatedTypeMock.PROPERTY_NAME_MBYTE) ||
        propertyName.equals(JPARelatedTypeMock.PROPERTY_NAME_MDOUBLE) ||
        propertyName.equals(JPARelatedTypeMock.PROPERTY_NAME_MBYTEARRAY) ||
        propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MDATE) ||
        propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MDATE1) ||
        propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MTIME) ||
        propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MTIMESTAMP)) {

      EdmSimpleType edmType = EasyMock.createMock(EdmSimpleType.class);
      EasyMock.expect(edmProperty.getType()).andReturn(edmType).anyTimes();
      EasyMock.expect(edmProperty.getFacets()).andReturn(null).anyTimes();
      EasyMock.expect(edmType.getKind()).andReturn(EdmTypeKind.SIMPLE).anyTimes();
      if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MSTRING) ||
          propertyName.equals(JPATypeMock.PROPERTY_NAME_ENUM) ||
          propertyName.equals(JPATypeMock.PROPERTY_NAME_MCARRAY) ||
          propertyName.equals(JPATypeMock.PROPERTY_NAME_MC) ||
          propertyName.equals(JPATypeMock.PROPERTY_NAME_MCHAR) ||
          propertyName.equals(JPATypeMock.PROPERTY_NAME_MCHARARRAY)) {
        EasyMock.<Class<?>> expect(edmType.getDefaultType()).andReturn(String.class).anyTimes();
      } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MBLOB)) {
        EasyMock.<Class<?>> expect(edmType.getDefaultType()).andReturn(Blob.class).anyTimes();
      } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_XMLADAPTER)) {
    	  EasyMock.<Class<?>> expect(edmType.getDefaultType())
    	  	.andReturn(String.class).anyTimes();
      } else {
        EasyMock.<Class<?>> expect(edmType.getDefaultType()).andReturn(Integer.class).anyTimes();
      }

      EasyMock.expect(edmType.isCompatible(EasyMock.isA(EdmSimpleType.class))).andReturn(true).anyTimes();
      EasyMock.replay(edmType);
      EasyMock.expect(edmProperty.getName()).andReturn(propertyName).anyTimes();
      
      
      
      EasyMock.expect(edmProperty.getMapping()).andReturn((EdmMapping) mockEdmMapping(entityName, propertyName, null))
          .anyTimes();

    } else if (propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MEMBEDDABLE) ||
        propertyName.equals(JPATypeMock.PROPERTY_NAME_MCOMPLEXTYPE)) {
      EdmComplexType complexType = mockComplexType(propertyName);

      EasyMock.expect(edmProperty.getType()).andReturn(complexType).anyTimes();
      EasyMock.expect(edmProperty.getName()).andReturn(propertyName).anyTimes();
      EasyMock.expect(edmProperty.getMapping()).andReturn((EdmMapping) mockEdmMapping(null, propertyName, null))
          .anyTimes();
      EasyMock.expect(edmProperty.getFacets()).andReturn(null).anyTimes();

    }

    EasyMock.replay(edmProperty);
    return edmProperty;
  }

  public static EdmComplexType mockComplexType(final String complexPropertyName) throws EdmException {

    String complexTypeName = null;
    if (complexPropertyName.equals(JPATypeEmbeddableMock.PROPERTY_NAME_MEMBEDDABLE)) {
      complexTypeName = JPATypeEmbeddableMock2.ENTITY_NAME;
    } else if (complexPropertyName.equals(JPATypeMock.PROPERTY_NAME_MCOMPLEXTYPE)) {
      complexTypeName = JPATypeEmbeddableMock.ENTITY_NAME;
    }

    EdmComplexType edmComplexType = EasyMock.createMock(EdmComplexType.class);
    EasyMock.expect(edmComplexType.getKind()).andReturn(EdmTypeKind.COMPLEX);
    EasyMock.expect(edmComplexType.getPropertyNames()).andReturn(mockPropertyNames(complexTypeName)).anyTimes();
    EasyMock.expect(edmComplexType.getMapping()).andReturn((EdmMapping) mockEdmMapping(complexTypeName, null, null));

    if (complexTypeName.equals(JPATypeEmbeddableMock.ENTITY_NAME)) {
      EasyMock.expect(edmComplexType.getProperty(JPATypeEmbeddableMock.PROPERTY_NAME_MSHORT)).andReturn(
          mockEdmProperty(complexTypeName, JPATypeEmbeddableMock.PROPERTY_NAME_MSHORT)).anyTimes();
      EasyMock.expect(edmComplexType.getProperty(JPATypeEmbeddableMock.PROPERTY_NAME_MDATE)).andReturn(
          mockEdmProperty(complexTypeName, JPATypeEmbeddableMock.PROPERTY_NAME_MDATE)).anyTimes();
      EasyMock.expect(edmComplexType.getProperty(JPATypeEmbeddableMock.PROPERTY_NAME_MDATE1)).andReturn(
          mockEdmProperty(complexTypeName, JPATypeEmbeddableMock.PROPERTY_NAME_MDATE1)).anyTimes();
      EasyMock.expect(edmComplexType.getProperty(JPATypeEmbeddableMock.PROPERTY_NAME_MTIME)).andReturn(
          mockEdmProperty(complexTypeName, JPATypeEmbeddableMock.PROPERTY_NAME_MTIME)).anyTimes();
      EasyMock.expect(edmComplexType.getProperty(JPATypeEmbeddableMock.PROPERTY_NAME_MTIMESTAMP)).andReturn(
          mockEdmProperty(complexTypeName, JPATypeEmbeddableMock.PROPERTY_NAME_MTIMESTAMP)).anyTimes();
      EasyMock.expect(edmComplexType.getProperty(JPATypeEmbeddableMock.PROPERTY_NAME_MEMBEDDABLE)).andReturn(
          mockEdmProperty(complexTypeName, JPATypeEmbeddableMock.PROPERTY_NAME_MEMBEDDABLE)).anyTimes();
    } else if (complexTypeName.equals(JPATypeEmbeddableMock2.ENTITY_NAME)) {
      EasyMock.expect(edmComplexType.getProperty(JPATypeEmbeddableMock2.PROPERTY_NAME_MFLOAT)).andReturn(
          mockEdmProperty(complexTypeName, JPATypeEmbeddableMock2.PROPERTY_NAME_MFLOAT)).anyTimes();
      EasyMock.expect(edmComplexType.getProperty(JPATypeEmbeddableMock2.PROPERTY_NAME_MUUID)).andReturn(
          mockEdmProperty(complexTypeName, JPATypeEmbeddableMock2.PROPERTY_NAME_MUUID)).anyTimes();
    }

    EasyMock.replay(edmComplexType);
    return edmComplexType;
  }

  public static JPAEdmMapping mockEdmMapping(final String entityName, final String propertyName,
      final String navigationPropertyName) {
    JPAEdmMapping mapping = new JPAEdmMappingImpl();

    if (propertyName == null && entityName != null) {
      if (entityName.equals(JPATypeMock.ENTITY_NAME)) {
        mapping.setJPAType(JPATypeMock.class);
      } else if (entityName.equals(JPARelatedTypeMock.ENTITY_NAME)) {
        mapping.setJPAType(JPARelatedTypeMock.class);
      } else if (entityName.equals(JPATypeEmbeddableMock.ENTITY_NAME)) {
        mapping.setJPAType(JPATypeEmbeddableMock.class);
      } else if (entityName.equals(JPATypeEmbeddableMock2.ENTITY_NAME)) {
        mapping.setJPAType(JPATypeEmbeddableMock2.class);
      }
    } else if (entityName == null && navigationPropertyName != null) {
      mapping.setJPAType(JPARelatedTypeMock.class);
      mapping.setJPAColumnName(JPATypeMock.NAVIGATION_PROPERTY_X);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MINT)) {
      mapping.setJPAType(int.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_MINT);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MSTRING)) {
      mapping.setJPAType(String.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_MSTRING);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_ENUM)) {
      mapping.setJPAType(JPATypeMock.JPATypeMockEnum.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_ENUM);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MBLOB)) {
      mapping.setJPAType(Blob.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_MBLOB);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_CLOB)) {
      mapping.setJPAType(Clob.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_CLOB);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MC)) {
      mapping.setJPAType(char.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_MC);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MCARRAY)) {
      mapping.setJPAType(char[].class);
      ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_MCARRAY);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MCHAR)) {
      mapping.setJPAType(Character.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_MCHAR);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MCHARARRAY)) {
      mapping.setJPAType(Character[].class);
      ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_MCHARARRAY);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_XMLADAPTER)) {
        mapping.setJPAType(EntityWithXmlAdapterOnProperty.class);
        ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_XMLADAPTER);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MDATETIME)) {
      mapping.setJPAType(Calendar.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_MDATETIME);
    } else if (propertyName.equals(JPARelatedTypeMock.PROPERTY_NAME_MLONG)) {
      mapping.setJPAType(long.class);
      ((Mapping) mapping).setInternalName(JPARelatedTypeMock.PROPERTY_NAME_MLONG);
    } else if (propertyName.equals(JPARelatedTypeMock.PROPERTY_NAME_MDOUBLE)) {
      mapping.setJPAType(double.class);
      ((Mapping) mapping).setInternalName(JPARelatedTypeMock.PROPERTY_NAME_MDOUBLE);
    } else if (propertyName.equals(JPARelatedTypeMock.PROPERTY_NAME_MBYTE)) {
      mapping.setJPAType(byte.class);
      ((Mapping) mapping).setInternalName(JPARelatedTypeMock.PROPERTY_NAME_MBYTE);
    } else if (propertyName.equals(JPARelatedTypeMock.PROPERTY_NAME_MBYTEARRAY)) {
      mapping.setJPAType(byte[].class);
      ((Mapping) mapping).setInternalName(JPARelatedTypeMock.PROPERTY_NAME_MBYTEARRAY);
    } else if (propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MSHORT)) {
      mapping.setJPAType(Short.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MSHORT);
    } else if (propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MDATE)) {
      mapping.setJPAType(java.util.Date.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MDATE);
    } else if (propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MDATE1)) {
      mapping.setJPAType(java.sql.Date.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MDATE1);
    } else if (propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MTIME)) {
      mapping.setJPAType(Time.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MTIME);
    } else if (propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MTIMESTAMP)) {
      mapping.setJPAType(Timestamp.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MTIMESTAMP);
    } else if (propertyName.equals(JPATypeMock.JPATypeEmbeddableMock2.PROPERTY_NAME_MFLOAT)) {
      mapping.setJPAType(Float.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.JPATypeEmbeddableMock2.PROPERTY_NAME_MFLOAT);
    } else if (propertyName.equals(JPATypeMock.JPATypeEmbeddableMock2.PROPERTY_NAME_MUUID)) {
      mapping.setJPAType(UUID.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.JPATypeEmbeddableMock2.PROPERTY_NAME_MUUID);
    } else if (propertyName.equals(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MEMBEDDABLE)) {
      mapping.setJPAType(JPATypeEmbeddableMock2.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.JPATypeEmbeddableMock.PROPERTY_NAME_MEMBEDDABLE);
    } else if (propertyName.equals(JPATypeMock.PROPERTY_NAME_MCOMPLEXTYPE)) {
      mapping.setJPAType(JPATypeEmbeddableMock.class);
      ((Mapping) mapping).setInternalName(JPATypeMock.PROPERTY_NAME_MCOMPLEXTYPE);
    }
    return mapping;
  }
}
