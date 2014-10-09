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
package org.apache.olingo.odata2.jpa.processor.core.access.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmStructuralType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.ep.entry.EntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.OnJPAWriteContent;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;

public class JPAEntity {

  private Object jpaEntity = null;
  private JPAEntity parentJPAEntity = null;
  private EdmEntityType oDataEntityType = null;
  private EdmEntitySet oDataEntitySet = null;
  private Class<?> jpaType = null;
  private HashMap<String, Method> accessModifiersWrite = null;
  private JPAEntityParser jpaEntityParser = null;
  private ODataJPAContext oDataJPAContext;
  private OnJPAWriteContent onJPAWriteContent = null;
  public HashMap<String, List<Object>> relatedJPAEntityMap = null;

  public JPAEntity(final EdmEntityType oDataEntityType, final EdmEntitySet oDataEntitySet,
      final ODataJPAContext context) {
    this.oDataEntityType = oDataEntityType;
    this.oDataEntitySet = oDataEntitySet;
    oDataJPAContext = context;
    try {
      JPAEdmMapping mapping = (JPAEdmMapping) oDataEntityType.getMapping();
      jpaType = mapping.getJPAType();
    } catch (EdmException e) {
      return;
    }
    jpaEntityParser = new JPAEntityParser();
    onJPAWriteContent = oDataJPAContext.getODataContext().getServiceFactory().getCallback(OnJPAWriteContent.class);
  }

  public void setAccessModifersWrite(final HashMap<String, Method> accessModifiersWrite) {
    this.accessModifiersWrite = accessModifiersWrite;
  }

  public void setParentJPAEntity(final JPAEntity jpaEntity) {
    parentJPAEntity = jpaEntity;
  }

  public JPAEntity getParentJPAEntity() {
    return parentJPAEntity;
  }

  public Object getJPAEntity() {
    return jpaEntity;
  }

  @SuppressWarnings("unchecked")
  private void write(final Map<String, Object> oDataEntryProperties,
      final boolean isCreate)
      throws ODataJPARuntimeException {
    try {

      EdmStructuralType structuralType = null;
      final List<String> keyNames = oDataEntityType.getKeyPropertyNames();

      if (isCreate) {
        jpaEntity = instantiateJPAEntity();
      } else if (jpaEntity == null) {
        throw ODataJPARuntimeException
            .throwException(ODataJPARuntimeException.RESOURCE_NOT_FOUND, null);
      }

      if (accessModifiersWrite == null) {
        accessModifiersWrite =
            jpaEntityParser.getAccessModifiers(jpaEntity, oDataEntityType, JPAEntityParser.ACCESS_MODIFIER_SET);
      }

      if (oDataEntityType == null || oDataEntryProperties == null) {
        throw ODataJPARuntimeException
            .throwException(ODataJPARuntimeException.GENERAL, null);
      }

      final HashMap<String, String> embeddableKeys =
          jpaEntityParser.getJPAEmbeddableKeyMap(jpaEntity.getClass().getName());
      Set<String> propertyNames = null;
      if (embeddableKeys != null) {
        setEmbeddableKeyProperty(embeddableKeys, oDataEntityType.getKeyProperties(), oDataEntryProperties,
            jpaEntity);

        propertyNames = new HashSet<String>();
        propertyNames.addAll(oDataEntryProperties.keySet());
        for (String key : embeddableKeys.keySet()) {
          propertyNames.remove(key);
        }
      } else {
        propertyNames = oDataEntryProperties.keySet();
      }

      for (String propertyName : propertyNames) {
        EdmTyped edmTyped = (EdmTyped) oDataEntityType.getProperty(propertyName);

        Method accessModifier = null;

        switch (edmTyped.getType().getKind()) {
        case SIMPLE:
          if (isCreate == false) {
            if (keyNames.contains(edmTyped.getName())) {
              continue;
            }
          }
          accessModifier = accessModifiersWrite.get(propertyName);
          setProperty(accessModifier, jpaEntity, oDataEntryProperties.get(propertyName), (EdmSimpleType) edmTyped
              .getType());

          break;
        case COMPLEX:
          structuralType = (EdmStructuralType) edmTyped.getType();
          accessModifier = accessModifiersWrite.get(propertyName);
          setComplexProperty(accessModifier, jpaEntity,
              structuralType,
              (HashMap<String, Object>) oDataEntryProperties.get(propertyName));
          break;
        case NAVIGATION:
        case ENTITY:
          if (isCreate) {
            structuralType = (EdmStructuralType) edmTyped.getType();
            EdmNavigationProperty navProperty = (EdmNavigationProperty) edmTyped;
            EdmEntitySet edmRelatedEntitySet = oDataEntitySet.getRelatedEntitySet(navProperty);
            List<ODataEntry> relatedEntries = (List<ODataEntry>) oDataEntryProperties.get(propertyName);
            if (relatedJPAEntityMap == null) {
              relatedJPAEntityMap = new HashMap<String, List<Object>>();
            }
            List<Object> relatedJPAEntities = new ArrayList<Object>();
            JPAEntity relatedEntity =
                new JPAEntity((EdmEntityType) structuralType, edmRelatedEntitySet, oDataJPAContext);
            for (ODataEntry oDataEntry : relatedEntries) {
              relatedEntity.setParentJPAEntity(this);
              relatedEntity.create(oDataEntry);
              relatedJPAEntities.add(relatedEntity.getJPAEntity());
            }
            relatedJPAEntityMap.put(navProperty.getName(), relatedJPAEntities);
          }
        default:
          continue;
        }
      }
    } catch (Exception e) {
      if (e instanceof ODataJPARuntimeException) {
        throw (ODataJPARuntimeException) e;
      }
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    }
  }

  public void create(final ODataEntry oDataEntry) throws ODataJPARuntimeException {
    if (oDataEntry == null) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL, null);
    }
    Map<String, Object> oDataEntryProperties = oDataEntry.getProperties();
    if (oDataEntry.containsInlineEntry()) {
      normalizeInlineEntries(oDataEntryProperties);
    }
    write(oDataEntryProperties, true);

    EntryMetadata entryMetadata = oDataEntry.getMetadata();
    List<String> leftNavPrpNames = new ArrayList<String>();
    try {
      for (String navigationPropertyName : oDataEntityType.getNavigationPropertyNames()) {
        List<String> links = entryMetadata.getAssociationUris(navigationPropertyName);
        if (links.isEmpty()) {
          continue;
        } else {
          EdmNavigationProperty navProperty =
              (EdmNavigationProperty) oDataEntityType.getProperty(navigationPropertyName);
          if (relatedJPAEntityMap != null && relatedJPAEntityMap.containsKey(navigationPropertyName)) {
            JPALink.linkJPAEntities(relatedJPAEntityMap.get(navigationPropertyName), jpaEntity,
                navProperty);
          } else if (parentJPAEntity != null
              &&
              parentJPAEntity.getEdmEntitySet().getName().equals(
                  oDataEntitySet.getRelatedEntitySet(navProperty).getName())) {
            List<Object> targetJPAEntities = new ArrayList<Object>();
            targetJPAEntities.add(parentJPAEntity.getJPAEntity());
            JPALink.linkJPAEntities(targetJPAEntities, jpaEntity, navProperty);
          } else {
            leftNavPrpNames.add(navigationPropertyName);
          }
        }
      }
      if (!leftNavPrpNames.isEmpty()) {
        JPALink link = new JPALink(oDataJPAContext);
        link.setSourceJPAEntity(jpaEntity);
        link.create(oDataEntitySet, oDataEntry, leftNavPrpNames);
      }
    } catch (EdmException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    } catch (ODataJPAModelException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    }
  }

  public EdmEntitySet getEdmEntitySet() {
    return oDataEntitySet;
  }

  public void create(final Map<String, Object> oDataEntryProperties) throws ODataJPARuntimeException {
    normalizeInlineEntries(oDataEntryProperties);
    write(oDataEntryProperties, true);
  }

  public void update(final ODataEntry oDataEntry) throws ODataJPARuntimeException {
    if (oDataEntry == null) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL, null);
    }
    Map<String, Object> oDataEntryProperties = oDataEntry.getProperties();
    if (oDataEntry.containsInlineEntry()) {
      normalizeInlineEntries(oDataEntryProperties);
    }
    write(oDataEntryProperties, false);
    JPALink link = new JPALink(oDataJPAContext);
    link.setSourceJPAEntity(jpaEntity);
    try {
      link.create(oDataEntitySet, oDataEntry, oDataEntityType.getNavigationPropertyNames());
    } catch (EdmException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    } catch (ODataJPAModelException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    }
  }

  public void update(final Map<String, Object> oDataEntryProperties) throws ODataJPARuntimeException {
    normalizeInlineEntries(oDataEntryProperties);
    write(oDataEntryProperties, false);
  }

  public void setJPAEntity(final Object jpaEntity) {
    this.jpaEntity = jpaEntity;
  }

  @SuppressWarnings("unchecked")
  protected void setComplexProperty(Method accessModifier, final Object jpaEntity,
      final EdmStructuralType edmComplexType, final HashMap<String, Object> propertyValue)
      throws EdmException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      InstantiationException, ODataJPARuntimeException, NoSuchMethodException, SecurityException, SQLException {

    JPAEdmMapping mapping = (JPAEdmMapping) edmComplexType.getMapping();
    Object embeddableObject = mapping.getJPAType().newInstance();
    accessModifier.invoke(jpaEntity, embeddableObject);

    HashMap<String, Method> accessModifiers =
        jpaEntityParser.getAccessModifiers(embeddableObject, edmComplexType, JPAEntityParser.ACCESS_MODIFIER_SET);

    for (String edmPropertyName : edmComplexType.getPropertyNames()) {
      EdmTyped edmTyped = (EdmTyped) edmComplexType.getProperty(edmPropertyName);
      accessModifier = accessModifiers.get(edmPropertyName);
      if (edmTyped.getType().getKind().toString().equals(EdmTypeKind.COMPLEX.toString())) {
        EdmStructuralType structualType = (EdmStructuralType) edmTyped.getType();
        setComplexProperty(accessModifier, embeddableObject, structualType, (HashMap<String, Object>) propertyValue
            .get(edmPropertyName));
      } else {
        setProperty(accessModifier, embeddableObject, propertyValue.get(edmPropertyName), (EdmSimpleType) edmTyped
            .getType());
      }
    }
  }

  protected void setProperty(final Method method, final Object entity, final Object entityPropertyValue,
      final EdmSimpleType type) throws
      IllegalAccessException, IllegalArgumentException, InvocationTargetException, ODataJPARuntimeException {
    if (entityPropertyValue != null) {
      Class<?> parameterType = method.getParameterTypes()[0];
      if (type != null && type.getDefaultType().equals(String.class)) {
        if (parameterType.equals(String.class)) {
          method.invoke(entity, entityPropertyValue);
        } else if (parameterType.equals(char[].class)) {
          char[] characters = ((String) entityPropertyValue).toCharArray();
          method.invoke(entity, characters);
        } else if (parameterType.equals(char.class)) {
          char c = ((String) entityPropertyValue).charAt(0);
          method.invoke(entity, c);
        } else if (parameterType.equals(Character[].class)) {
          Character[] characters = JPAEntityParser.toCharacterArray((String) entityPropertyValue);
          method.invoke(entity, (Object) characters);
        } else if (parameterType.equals(Character.class)) {
          Character c = Character.valueOf(((String) entityPropertyValue).charAt(0));
          method.invoke(entity, c);
        }
      } else if (parameterType.equals(Blob.class)) {
        if (onJPAWriteContent == null) {
          throw ODataJPARuntimeException
              .throwException(ODataJPARuntimeException.ERROR_JPA_BLOB_NULL, null);
        } else {
          method.invoke(entity, onJPAWriteContent.getJPABlob((byte[]) entityPropertyValue));
        }
      } else if (parameterType.equals(Clob.class)) {
        if (onJPAWriteContent == null) {
          throw ODataJPARuntimeException
              .throwException(ODataJPARuntimeException.ERROR_JPA_CLOB_NULL, null);
        } else {
          method.invoke(entity, onJPAWriteContent.getJPAClob(((String) entityPropertyValue).toCharArray()));
        }
      } else if (parameterType.equals(Timestamp.class)) {
        Timestamp ts = new Timestamp(((Calendar) entityPropertyValue).getTimeInMillis());
        method.invoke(entity, ts);
      } else if (parameterType.equals(java.util.Date.class)) {
        method.invoke(entity, ((Calendar) entityPropertyValue).getTime());
      } else if (parameterType.equals(java.sql.Date.class)) {
        long timeInMs = ((Calendar) entityPropertyValue).getTimeInMillis();
        method.invoke(entity, new java.sql.Date(timeInMs));
      } else if (parameterType.equals(java.sql.Time.class)) {
        long timeInMs = ((Calendar) entityPropertyValue).getTimeInMillis();
        method.invoke(entity, new java.sql.Time(timeInMs));
      } else {
        method.invoke(entity, entityPropertyValue);
      }
    }
  }

  protected void setEmbeddableKeyProperty(final HashMap<String, String> embeddableKeys,
      final List<EdmProperty> oDataEntryKeyProperties,
      final Map<String, Object> oDataEntryProperties, final Object entity)
      throws ODataJPARuntimeException, EdmException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, InstantiationException {

    HashMap<String, Object> embeddableObjMap = new HashMap<String, Object>();
    List<EdmProperty> leftODataEntryKeyProperties = new ArrayList<EdmProperty>();
    HashMap<String, String> leftEmbeddableKeys = new HashMap<String, String>();

    for (EdmProperty edmProperty : oDataEntryKeyProperties) {
      if (oDataEntryProperties.containsKey(edmProperty.getName()) == false) {
        continue;
      }

      String edmPropertyName = edmProperty.getName();
      String embeddableKeyNameComposite = embeddableKeys.get(edmPropertyName);
      if (embeddableKeyNameComposite == null) {
        continue;
      }
      String embeddableKeyNameSplit[] = embeddableKeyNameComposite.split("\\.");
      String methodPartName = null;
      Method method = null;
      Object embeddableObj = null;

      if (embeddableObjMap.containsKey(embeddableKeyNameSplit[0]) == false) {
        methodPartName = embeddableKeyNameSplit[0];
        method = jpaEntityParser.getAccessModifierSet(entity, methodPartName);
        embeddableObj = method.getParameterTypes()[0].newInstance();
        method.invoke(entity, embeddableObj);
        embeddableObjMap.put(embeddableKeyNameSplit[0], embeddableObj);
      } else {
        embeddableObj = embeddableObjMap.get(embeddableKeyNameSplit[0]);
      }

      if (embeddableKeyNameSplit.length == 2) {
        methodPartName = embeddableKeyNameSplit[1];
        method = jpaEntityParser.getAccessModifierSet(embeddableObj, methodPartName);
        Object simpleObj = oDataEntryProperties.get(edmProperty.getName());
        method.invoke(embeddableObj, simpleObj);
      } else if (embeddableKeyNameSplit.length > 2) { // Deeply nested
        leftODataEntryKeyProperties.add(edmProperty);
        leftEmbeddableKeys
            .put(edmPropertyName, embeddableKeyNameComposite.split(embeddableKeyNameSplit[0] + ".", 2)[1]);
      }
    }
  }

  protected Object instantiateJPAEntity() throws InstantiationException, IllegalAccessException {
    if (jpaType == null) {
      throw new InstantiationException();
    }

    return jpaType.newInstance();
  }

  private void normalizeInlineEntries(final Map<String, Object> oDataEntryProperties) throws ODataJPARuntimeException {
    List<ODataEntry> entries = null;
    try {
      for (String navigationPropertyName : oDataEntityType.getNavigationPropertyNames()) {
        Object inline = oDataEntryProperties.get(navigationPropertyName);
        if (inline instanceof ODataFeed) {
          entries = ((ODataFeed) inline).getEntries();
        } else if (inline instanceof ODataEntry) {
          entries = new ArrayList<ODataEntry>();
          entries.add((ODataEntry) inline);
        }
        if (entries != null) {
          oDataEntryProperties.put(navigationPropertyName, entries);
          entries = null;
        }
      }
    } catch (EdmException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    }
  }
}