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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmStructuralType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.ep.entry.EntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.OnJPAWriteContent;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;

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
  private List<String> relatedJPAEntityLink = new ArrayList<String>();
  public HashMap<String, List<Object>> relatedJPAEntityMap = null;
  private EdmNavigationProperty viaNavigationProperty;

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

  public void setViaNavigationProperty(EdmNavigationProperty viaNavigationProperty) {
    this.viaNavigationProperty = viaNavigationProperty;
  }

  public EdmNavigationProperty getViaNavigationProperty() {
    return viaNavigationProperty;
  }

  public void create(final ODataEntry oDataEntry) throws ODataJPARuntimeException {

    if (oDataEntry == null) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL, null);
    }
    try {
      EntryMetadata entryMetadata = oDataEntry.getMetadata();
      Map<String, Object> oDataEntryProperties = oDataEntry.getProperties();
      if (oDataEntry.containsInlineEntry()) {
        normalizeInlineEntries(oDataEntryProperties);
      }

      if (oDataEntry.getProperties().size() > 0) {

        write(oDataEntryProperties, true);

        for (String navigationPropertyName : oDataEntityType.getNavigationPropertyNames()) {
          EdmNavigationProperty navProperty =
              (EdmNavigationProperty) oDataEntityType.getProperty(navigationPropertyName);
          if (relatedJPAEntityMap != null && relatedJPAEntityMap.containsKey(navigationPropertyName)) {
            oDataEntry.getProperties().get(navigationPropertyName);
            JPALink.linkJPAEntities(relatedJPAEntityMap.get(navigationPropertyName), jpaEntity,
                navProperty);
            continue;
          }
          // The second condition is required to ensure that there is an explicit request to link
          // two entities. Else the third condition will always be true for cases where two navigations
          // point to same entity types.
          if (parentJPAEntity != null
              && navProperty.getRelationship().equals(getViaNavigationProperty().getRelationship())) {
            List<Object> targetJPAEntities = new ArrayList<Object>();
            targetJPAEntities.add(parentJPAEntity.getJPAEntity());
            JPALink.linkJPAEntities(targetJPAEntities, jpaEntity, navProperty);
          } else if (!entryMetadata.getAssociationUris(navigationPropertyName).isEmpty()) {
            if (!relatedJPAEntityLink.contains(navigationPropertyName)) {
              relatedJPAEntityLink.add(navigationPropertyName);
            }
          }
        }
      }
      if (!relatedJPAEntityLink.isEmpty()) {
        JPALink link = new JPALink(oDataJPAContext);
        link.setSourceJPAEntity(jpaEntity);
        link.create(oDataEntitySet, oDataEntry, relatedJPAEntityLink);
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

  protected void setComplexProperty(Method accessModifier, final Object jpaEntity,
      final EdmStructuralType edmComplexType, final HashMap<String, Object> propertyValue)
      throws EdmException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      InstantiationException, ODataJPARuntimeException, NoSuchMethodException, SecurityException, SQLException {

    setComplexProperty(accessModifier, jpaEntity, edmComplexType, propertyValue, null);
  }

  protected void setProperty(final Method method, final Object entity, final Object entityPropertyValue,
      final EdmSimpleType type, boolean isNullable) throws
      IllegalAccessException, IllegalArgumentException, InvocationTargetException, ODataJPARuntimeException, 
      EdmException {

    setProperty(method, entity, entityPropertyValue, type, null, isNullable);
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

      boolean isVirtual = false;
      for (String propertyName : propertyNames) {
        EdmTyped edmTyped = (EdmTyped) oDataEntityType.getProperty(propertyName);
        if (edmTyped instanceof EdmProperty) {
          isVirtual = ((JPAEdmMappingImpl)((EdmProperty) edmTyped).getMapping()).isVirtualAccess();
        } else {
          isVirtual = false;
        }
        Method accessModifier = null;

        switch (edmTyped.getType().getKind()) {
        case SIMPLE:
          if (isCreate == false) {
            if (keyNames.contains(edmTyped.getName())) {
              continue;
            }
          }
          accessModifier = accessModifiersWrite.get(propertyName);
          EdmProperty edmProperty = (EdmProperty)oDataEntityType.getProperty(propertyName);
          boolean isNullable = edmProperty.getFacets() == null ? (keyNames.contains(propertyName)? false : true)
              : edmProperty.getFacets().isNullable() == null ? true : edmProperty.getFacets().isNullable();
          if (isVirtual) {
            setProperty(accessModifier, jpaEntity, oDataEntryProperties.get(propertyName), (EdmSimpleType) edmTyped
                .getType(), isNullable);
          } else {
            setProperty(accessModifier, jpaEntity, oDataEntryProperties.get(propertyName), (EdmSimpleType) edmTyped
                .getType(), isNullable);
          }
          break;
        case COMPLEX:
          structuralType = (EdmStructuralType) edmTyped.getType();
          accessModifier = accessModifiersWrite.get(propertyName);
          if (isVirtual) {
            setComplexProperty(accessModifier, jpaEntity,
                structuralType,
                (HashMap<String, Object>) oDataEntryProperties.get(propertyName), propertyName);
          } else {
            setComplexProperty(accessModifier, jpaEntity,
                structuralType,
                (HashMap<String, Object>) oDataEntryProperties.get(propertyName));
          }
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
            for (ODataEntry oDataEntry : relatedEntries) {
              JPAEntity relatedEntity =
                  new JPAEntity((EdmEntityType) structuralType, edmRelatedEntitySet, oDataJPAContext);
              relatedEntity.setParentJPAEntity(this);
              relatedEntity.setViaNavigationProperty(navProperty);
              relatedEntity.create(oDataEntry);
              if (oDataEntry.getProperties().size() == 0) {
                if (!oDataEntry.getMetadata().getUri().isEmpty()
                    && !relatedJPAEntityLink.contains(navProperty.getName())) {
                  relatedJPAEntityLink.add(navProperty.getName());
                }
              } else {
                relatedJPAEntities.add(relatedEntity.getJPAEntity());
              }
            }
            if (!relatedJPAEntities.isEmpty()) {
              relatedJPAEntityMap.put(navProperty.getName(), relatedJPAEntities);
            }
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

  @SuppressWarnings("unchecked")
  protected void setComplexProperty(Method accessModifier, final Object jpaEntity,
      final EdmStructuralType edmComplexType, final HashMap<String, Object> propertyValue, String propertyName)
      throws EdmException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      InstantiationException, ODataJPARuntimeException, NoSuchMethodException, SecurityException, SQLException {

    JPAEdmMapping mapping = (JPAEdmMapping) edmComplexType.getMapping();
    Object embeddableObject = mapping.getJPAType().newInstance();
    if (propertyName != null) {
    	accessModifier.invoke(jpaEntity, propertyName, embeddableObject);
    } else {
    	accessModifier.invoke(jpaEntity, embeddableObject);
    }

    HashMap<String, Method> accessModifiers =
        jpaEntityParser.getAccessModifiers(embeddableObject, edmComplexType,
            JPAEntityParser.ACCESS_MODIFIER_SET);

    for (String edmPropertyName : edmComplexType.getPropertyNames()) {
      if (propertyValue != null) {
        EdmTyped edmTyped = edmComplexType.getProperty(edmPropertyName);
        accessModifier = accessModifiers.get(edmPropertyName);
        EdmType type = edmTyped.getType();
        if (type.getKind().toString().equals(EdmTypeKind.COMPLEX.toString())) {
          setComplexProperty(accessModifier, embeddableObject, (EdmStructuralType) type,
              (HashMap<String, Object>) propertyValue.get(edmPropertyName), propertyName);
        } else {
          EdmSimpleType simpleType = (EdmSimpleType) type;
          EdmProperty edmProperty = (EdmProperty)edmComplexType.getProperty(edmPropertyName);
          boolean isNullable = edmProperty.getFacets() == null ? true
              : edmProperty.getFacets().isNullable() == null ? true : edmProperty.getFacets().isNullable();
    		  if (propertyName != null) {
            setProperty(accessModifier, embeddableObject, propertyValue.get(edmPropertyName),
                simpleType, isNullable);
          } else {
            setProperty(accessModifier, embeddableObject, propertyValue.get(edmPropertyName),
                simpleType, isNullable);
          }
        }
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void setProperty(final Method method, final Object entity, final Object entityPropertyValue,
      final EdmSimpleType type, String propertyName, boolean isNullable) throws
      IllegalAccessException, IllegalArgumentException, InvocationTargetException, ODataJPARuntimeException, 
      EdmException {
    if (entityPropertyValue != null || isNullable) {
      if (propertyName != null) {
        method.invoke(entity, propertyName, entityPropertyValue);
        return;
      }
      Class<?> parameterType = method.getParameterTypes()[0];
      if (type != null && type.getDefaultType().equals(String.class)) {
        if (parameterType.equals(String.class)) {
          method.invoke(entity, entityPropertyValue);
        } else if (parameterType.equals(char[].class)) {
          char[] characters = entityPropertyValue != null ? ((String) entityPropertyValue).toCharArray() : null;
          method.invoke(entity, characters);
        } else if (parameterType.equals(char.class)) {
          char c = entityPropertyValue != null ? ((String) entityPropertyValue).charAt(0) : '\u0000';
          method.invoke(entity, c);
        } else if (parameterType.equals(Character[].class)) {
          Character[] characters = entityPropertyValue != null ? 
              JPAEntityParser.toCharacterArray((String) entityPropertyValue) : null;
          method.invoke(entity, (Object) characters);
        } else if (parameterType.equals(Character.class)) {
          Character c = entityPropertyValue != null ? 
              Character.valueOf(((String) entityPropertyValue).charAt(0)) : null;
          method.invoke(entity, c);
        } else if (parameterType.isEnum()) {
          Enum e = entityPropertyValue != null ?
              Enum.valueOf((Class<Enum>) parameterType, (String) entityPropertyValue) : null;
          method.invoke(entity, e);
        } else {
          String setterName = method.getName();
      	  String getterName = setterName.replace("set", "get");
      	  try {
            Method getMethod = entity.getClass().getDeclaredMethod(getterName);
            if(getMethod.isAnnotationPresent(XmlJavaTypeAdapter.class)) {
              XmlAdapter xmlAdapter = getMethod.getAnnotation(XmlJavaTypeAdapter.class)
                  .value().newInstance();
              method.invoke(entity, xmlAdapter.unmarshal(entityPropertyValue));
            }
          } catch (Exception e) {
            throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL, e);
      	  }
        }
      } else if (parameterType.equals(Blob.class)) {
        if (onJPAWriteContent == null) {
          throw ODataJPARuntimeException
              .throwException(ODataJPARuntimeException.ERROR_JPA_BLOB_NULL, null);
        } else {
          method.invoke(entity, entityPropertyValue != null ? 
              onJPAWriteContent.getJPABlob((byte[]) entityPropertyValue) : null);
        }
      } else if (parameterType.equals(Clob.class)) {
        if (onJPAWriteContent == null) {
          throw ODataJPARuntimeException
              .throwException(ODataJPARuntimeException.ERROR_JPA_CLOB_NULL, null);
        } else {
          method.invoke(entity, entityPropertyValue != null ? 
              onJPAWriteContent.getJPAClob(((String) entityPropertyValue).toCharArray()) : null);
        }
      } else if (parameterType.equals(Timestamp.class)) {
        Timestamp ts = entityPropertyValue != null ? 
            new Timestamp(((Calendar) entityPropertyValue).getTimeInMillis()) : null;
        method.invoke(entity, ts);
      } else if (parameterType.equals(java.util.Date.class)) {
        Date d = entityPropertyValue != null ? ((Calendar) entityPropertyValue).getTime(): null;
        method.invoke(entity, d);
      } else if (parameterType.equals(java.sql.Date.class)) {
        java.sql.Date d = entityPropertyValue != null ? 
            new java.sql.Date(((Calendar) entityPropertyValue).getTimeInMillis()) : null;
        method.invoke(entity, d);
      } else if (parameterType.equals(java.sql.Time.class)) {
        java.sql.Time t = entityPropertyValue != null ? 
            new java.sql.Time(((Calendar) entityPropertyValue).getTimeInMillis()) : null;
        method.invoke(entity, t);
      } else {
        method.invoke(entity, entityPropertyValue);
      }
    }
  }
}