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

import com.google.gson.JsonElement;
import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.*;
import org.apache.olingo.odata2.core.edm.provider.EdmEntityTypeImplProv;
import org.apache.olingo.odata2.core.uri.KeyPredicateImpl;
import org.apache.olingo.odata2.core.uri.UriInfoImpl;
import org.apache.olingo.odata2.jpa.processor.api.*;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAFunction;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAMethodContext;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAProcessor;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.core.ODataEntityParser;
import org.apache.olingo.odata2.jpa.processor.core.ODataParameterizedWhereExpressionUtil;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAPage.JPAPageBuilder;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAQueryBuilder.JPAQueryInfo;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

public class JPAProcessorImpl implements JPAProcessor {

  private static final String DELTATOKEN = "!deltatoken";
  ODataJPAContext oDataJPAContext;
  EntityManager em;

  public JPAProcessorImpl(final ODataJPAContext oDataJPAContext) {
    this.oDataJPAContext = oDataJPAContext;
    em = oDataJPAContext.getEntityManager();
  }

  /* Process Function Import Request */
  @SuppressWarnings("unchecked")
  @Override
  public List<Object> process(final GetFunctionImportUriInfo uriParserResultView)
      throws ODataJPAModelException, ODataJPARuntimeException {

    JPAMethodContext jpaMethodContext = JPAMethodContext.createBuilder(
        JPQLContextType.FUNCTION, uriParserResultView).build();

    List<Object> resultObj = null;

    try {

      JPAFunction jpaFunction = jpaMethodContext.getJPAFunctionList()
          .get(0);
      Method method = jpaFunction.getFunction();
      Object[] args = jpaFunction.getArguments();

      if (uriParserResultView.getFunctionImport().getReturnType()
          .getMultiplicity().equals(EdmMultiplicity.MANY)) {

        resultObj = (List<Object>) method.invoke(
            jpaMethodContext.getEnclosingObject(), args);
      } else {
        resultObj = new ArrayList<Object>();
        Object result = method.invoke(
            jpaMethodContext.getEnclosingObject(), args);
        resultObj.add(result);
      }

    } catch (EdmException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    } catch (IllegalAccessException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    } catch (IllegalArgumentException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getMessage()), e);
    } catch (InvocationTargetException e) {
      throw ODataJPARuntimeException
          .throwException(ODataJPARuntimeException.GENERAL
              .addContent(e.getTargetException().getMessage()), e.getTargetException());
    }

    return resultObj;
  }

  /* Process Get Entity Set Request (Query) */
  @Override
  public List<Object> process(final GetEntitySetUriInfo uriParserResultView)
      throws ODataJPAModelException, ODataJPARuntimeException {

    if (uriParserResultView.isNew()) {
      List<Object> result = new ArrayList<Object>();
      result.add(processNew((UriInfo) uriParserResultView));

      return result;
    }

    List<Object> result = null;
    if (uriParserResultView.getFunctionImport() != null) {
      return (List<Object>) process((GetFunctionImportUriInfo) uriParserResultView);
    }

    InlineCount inlineCount = uriParserResultView.getInlineCount();
    Integer top = uriParserResultView.getTop() == null ? 1 : uriParserResultView.getTop().intValue();
    boolean hasNoAllPages = inlineCount == null ? true : !inlineCount.equals(InlineCount.ALLPAGES);
    if (top.intValue() == 0 && hasNoAllPages) {
      return new ArrayList<Object>();
    }

    try {
      JPAEdmMapping mapping = (JPAEdmMapping) uriParserResultView.getTargetEntitySet().getEntityType().getMapping();
      JPAQueryBuilder queryBuilder = new JPAQueryBuilder(oDataJPAContext);
      JPAQueryInfo queryInfo = queryBuilder.build(uriParserResultView);
      Query query = queryInfo.getQuery();
      setPositionalParametersToQuery(query);
      ODataJPATombstoneEntityListener listener =
          queryBuilder.getODataJPATombstoneEntityListener((UriInfo) uriParserResultView);
      Map<String, String> customQueryOptions = uriParserResultView.getCustomQueryOptions();
      String deltaToken = null;
      if (customQueryOptions != null) {
        deltaToken = uriParserResultView.getCustomQueryOptions().get(DELTATOKEN);
      }
      if (deltaToken != null) {
        ODataJPATombstoneContext.setDeltaToken(deltaToken);
      }
      if (listener != null && (!queryInfo.isTombstoneQuery() && listener.isTombstoneSupported())) {
        query.getResultList();
        List<Object> deltaResult =
            (List<Object>) ODataJPATombstoneContext.getDeltaResult(((EdmMapping) mapping).getInternalName());
        result = handlePaging(deltaResult, uriParserResultView);
      } else {
        result = handlePaging(query, uriParserResultView, queryBuilder);
      }
      if (listener != null && listener.isTombstoneSupported()) {
        ODataJPATombstoneContext.setDeltaToken(listener.generateDeltaToken((List<Object>) result, query));
      }
      return result == null ? new ArrayList<Object>() : result;
    } catch (EdmException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param query
   */
  private void setPositionalParametersToQuery(Query query) {
    Map<String, Map<Integer, Object>> parameterizedMap = ODataParameterizedWhereExpressionUtil.
        getParameterizedQueryMap();
    if (parameterizedMap != null && parameterizedMap.size() > 0) {
      for (Entry<String, Map<Integer, Object>> parameterEntry : parameterizedMap.entrySet()) {
        if (ODataParameterizedWhereExpressionUtil.getJPQLStatement().contains(parameterEntry.getKey())) {
          Map<Integer, Object> positionalParameters = parameterEntry.getValue();
          for (Entry<Integer, Object> param : positionalParameters.entrySet()) {
            if (param.getValue() instanceof Calendar || param.getValue() instanceof Timestamp) {
              query.setParameter(param.getKey(), (Calendar) param.getValue(), TemporalType.TIMESTAMP);
            } else if (param.getValue() instanceof Time) {
              query.setParameter(param.getKey(), (Time) param.getValue(), TemporalType.TIME);
            } else {
              query.setParameter(param.getKey(), param.getValue());
            }
          }
          parameterizedMap.remove(parameterEntry.getKey());
          ODataParameterizedWhereExpressionUtil.setJPQLStatement(null);
          break;
        }
      }
    }
  }

  /* Process Get Entity Request (Read) */
  @Override
  public <T> Object process(GetEntityUriInfo uriParserResultView)
      throws ODataJPAModelException, ODataJPARuntimeException {
    if (uriParserResultView.isNew()) {
      return processNew((UriInfo) uriParserResultView);
    }
    return readEntity(new JPAQueryBuilder(oDataJPAContext).build(uriParserResultView), (UriInfo) uriParserResultView);
  }

  /* Process $count for Get Entity Set Request */
  @Override
  public long process(final GetEntitySetCountUriInfo resultsView)
      throws ODataJPAModelException, ODataJPARuntimeException {

    JPAQueryBuilder queryBuilder = new JPAQueryBuilder(oDataJPAContext);
    Query query = queryBuilder.build(resultsView);
    setPositionalParametersToQuery(query);
    List<?> resultList = query.getResultList();
    if (resultList != null && resultList.size() > 0) {
      try {
      return Long.valueOf(resultList.get(0).toString());
      } catch(Exception e) {
        return resultList.size();
      }
    }

    return 0;
  }

  /* Process $count for Get Entity Request */
  @Override
  public long process(final GetEntityCountUriInfo resultsView) throws ODataJPAModelException, ODataJPARuntimeException {

    JPAQueryBuilder queryBuilder = new JPAQueryBuilder(oDataJPAContext);
    Query query = queryBuilder.build(resultsView);
    setPositionalParametersToQuery(query);
    List<?> resultList = query.getResultList();
    if (resultList != null && resultList.size() == 1) {
      return Long.valueOf(resultList.get(0).toString());
    }
    return 0;
  }

  /* Process Create Entity Request */
  @Override
  public Object process(final PostUriInfo createView, final InputStream content,
      final String requestedContentType) throws ODataJPAModelException,
      ODataJPARuntimeException {
    return processCreate(createView, content, null, requestedContentType);
  }

  @Override
  public Object process(final PostUriInfo createView, final Map<String, Object> content)
      throws ODataJPAModelException, ODataJPARuntimeException {
    return processCreate(createView, null, content, null);
  }

  /* Process Update Entity Request */
  @Override
  public Object process(final PutMergePatchUriInfo updateView,
      final InputStream content, final String requestContentType)
      throws ODataJPAModelException, ODataJPARuntimeException {
    return processUpdate(updateView, content, null, requestContentType);
  }

  @Override
  public Object process(final PutMergePatchUriInfo updateView, final Map<String, Object> content)
      throws ODataJPAModelException, ODataJPARuntimeException {
    return processUpdate(updateView, null, content, null);
  }

  /* Process Delete Entity Request */
  @Override
  public Object process(DeleteUriInfo uriParserResultView, final String contentType)
      throws ODataJPAModelException, ODataJPARuntimeException {

    if (uriParserResultView instanceof DeleteUriInfo) {
      if (((UriInfo) uriParserResultView).isLinks()) {
        return deleteLink(uriParserResultView);
      }
    }
    JPAQueryBuilder queryBuilder = new JPAQueryBuilder(oDataJPAContext);
    ODataJPAQueryExtensionEntityListener listener = null;
    try {
      listener = queryBuilder.getODataJPAQueryEntityListener((UriInfo) uriParserResultView);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (listener != null) {
      listener.checkAuthorization(uriParserResultView);
    }

    ((UriInfoImpl) uriParserResultView).setRawEntity(true);

    Object selectedObject = readEntity(new JPAQueryBuilder(oDataJPAContext).build(uriParserResultView), (UriInfo) uriParserResultView, true);
    if (selectedObject != null) {
      try {
        final EdmEntitySet oDataEntitySet = uriParserResultView.getTargetEntitySet();
        final EdmEntityType oDataEntityType = oDataEntitySet.getEntityType();

        boolean isLocalTransaction = setTransaction();
        if (listener != null) {
          listener.execEvent(((UriInfoImpl) uriParserResultView), oDataEntityType, "beforeDelete", selectedObject);
        }

        boolean override = listener.overrideDelete((UriInfo) uriParserResultView, selectedObject);
        if (!override) {
          em.remove(selectedObject);
          em.flush();
        }

        if (listener != null) {
          listener.execEvent(((UriInfoImpl) uriParserResultView), oDataEntityType, "afterDelete", selectedObject);
        }

        if (!override) {
          if (isLocalTransaction) {
            oDataJPAContext.getODataJPATransaction().commit();
          }
        }
      } catch (Exception e) {
        em.getTransaction().rollback();
        throw new RuntimeException(e);
      }
    }
    return selectedObject;
  }

  /* Process Get Entity Link Request */
  @Override
  public Object process(final GetEntityLinkUriInfo uriParserResultView)
      throws ODataJPAModelException, ODataJPARuntimeException {

    return this.process((GetEntityUriInfo) uriParserResultView);
  }

  /* Process Get Entity Set Link Request */
  @Override
  public List<Object> process(final GetEntitySetLinksUriInfo uriParserResultView)
      throws ODataJPAModelException, ODataJPARuntimeException {
    return this.process((GetEntitySetUriInfo) uriParserResultView);
  }

  @Override
  public void process(final PostUriInfo uriInfo,
      final InputStream content, final String requestContentType, final String contentType)
      throws ODataJPARuntimeException, ODataJPAModelException {
    JPALink link = new JPALink(oDataJPAContext);
    link.create(uriInfo, content, requestContentType, contentType);
    link.save();
  }

  @Override
  public void process(final PutMergePatchUriInfo putUriInfo,
      final InputStream content, final String requestContentType, final String contentType)
      throws ODataJPARuntimeException, ODataJPAModelException {

    JPALink link = new JPALink(oDataJPAContext);
    link.update(putUriInfo, content, requestContentType, contentType);
    link.save();

  }

  private Object readEntity(final Query query, final UriInfo uriInfo) throws ODataJPARuntimeException {
    return readEntity(query, uriInfo, false);
  }

  /* Common method for Read and Delete */
  private Object readEntity(final Query query, final UriInfo uriInfo, final boolean rawEntity) throws ODataJPARuntimeException {
    Object selectedObject = null;
    @SuppressWarnings("rawtypes")
    List resultList = query.getResultList();

    if (!rawEntity) {
      resultList = normalizeList(resultList, uriInfo);
    }

    if (!resultList.isEmpty()) {
      selectedObject = resultList.get(0);
    }
    return selectedObject;
  }

  private Object processNew(final UriInfo newView) throws ODataJPAModelException, ODataJPARuntimeException {
    Object jpaEntity = null;

    try {
      final EdmEntitySet oDataEntitySet = newView.getTargetEntitySet();
      final EdmEntityType oDataEntityType = oDataEntitySet.getEntityType();

      jpaEntity = ((JPAEdmMappingImpl) oDataEntityType.getMapping()).getJPAType().newInstance();

      if (((JPAEdmMappingImpl) oDataEntityType.getMapping()).isVirtualAccess()) {

        JPAQueryBuilder queryBuilder = new JPAQueryBuilder(oDataJPAContext);
        ODataJPAQueryExtensionEntityListener listener = null;
        try {
          listener = queryBuilder.getODataJPAQueryEntityListener((UriInfo) newView);
        } catch (Exception e) {
          e.printStackTrace();
        }

        if (listener != null) {
          Object newObj = listener.processNew(newView);
          if (newObj != null) {
            jpaEntity = newObj;
          }
        }

      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return jpaEntity;

  }

  //Caso o target seja diferente do internal, significa que é a fonte de dados, precisa pegar as permissoes da fonte de dados.
  private void recreateJPAEntityIfTargetIsNotEntitySet(final PostUriInfo createView, final JPAEntity virtualJPAEntity) throws EdmException, ODataJPARuntimeException {
    EdmEntityType edmEntityType = createView.getTargetEntitySet().getEntityType();
    EdmEntityType edmEntityTypeBase = createView.getEntityContainer().getEntitySet(createView.getTargetEntitySet()
        .getEntityType().getMapping().getInternalName()).getEntityType();

    if (edmEntityType != null && edmEntityTypeBase != null && !edmEntityType.equals(edmEntityTypeBase) ) {
      JPAEntityParser jpaResultParser = new JPAEntityParser(oDataJPAContext, (UriInfo) createView);
      HashMap<String, Object> edmPropertyValueMap  = jpaResultParser.parse2EdmPropertyValueMap(virtualJPAEntity.getJPAEntity(), edmEntityType);
      virtualJPAEntity.create(edmPropertyValueMap);
    }
  }

  private Object processCreate(final PostUriInfo createView, final InputStream content,
      final Map<String, Object> properties,
      final String requestedContentType) throws ODataJPAModelException,
      ODataJPARuntimeException {
    try {
      final EdmEntitySet oDataEntitySet = createView.getTargetEntitySet();
      final EdmEntityType oDataEntityType = oDataEntitySet.getEntityType();

      JPAQueryBuilder queryBuilder = new JPAQueryBuilder(oDataJPAContext);
      ODataJPAQueryExtensionEntityListener listener = null;
      try {
        listener = queryBuilder.getODataJPAQueryEntityListener((UriInfo) createView);
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (listener != null) {
        listener.checkAuthorization(createView);
      }

      final JPAEntity virtualJPAEntity = new JPAEntity(oDataEntityType, oDataEntitySet, oDataJPAContext);
      Object jpaEntity = null;

      if (content != null) {
        final ODataEntityParser oDataEntityParser = new ODataEntityParser(oDataJPAContext);
        final ODataEntry oDataEntry = oDataEntityParser.parseEntry((UriInfo) createView, oDataEntitySet, content, requestedContentType, false);
        virtualJPAEntity.create(oDataEntry);
        recreateJPAEntityIfTargetIsNotEntitySet(createView, virtualJPAEntity);
      } else if (properties != null) {
        virtualJPAEntity.create(properties);
      } else {
        return null;
      }


      boolean isLocalTransaction = setTransaction();
      jpaEntity = virtualJPAEntity.getJPAEntity();

      Object tempEntity = jpaEntity;

      boolean manymany = false;

      if (createView.getNavigationSegments().size() > 0) {
        UriInfoImpl clone = ((UriInfoImpl) createView).getClone();
        clone.setTargetEntitySet(createView.getStartEntitySet());
        clone.getNavigationSegments().clear();
        clone.setTargetType(clone.getTargetEntitySet().getEntityType());
        Object relatedEntity = readEntity(new JPAQueryBuilder(oDataJPAContext).build((PutMergePatchUriInfo) clone), (UriInfo) clone);

        if (createView.getNavigationSegments().size() == 1) {

          NavigationProperty navigationProperty = ((EdmEntityTypeImplProv) createView.getStartEntitySet().getEntityType()).getNavigationProperties().get(createView.getNavigationSegments().get(0).getNavigationProperty().getName());

          if (navigationProperty.getRelationship().getName().toLowerCase().contains("many_many")) {
            manymany = true;
          }

          String property;
          if (manymany) {
            JPAEdmMappingImpl mapping = ((JPAEdmMappingImpl) navigationProperty.getMapping());
            property = mapping.getInternalName();

            try {
              Field field = relatedEntity.getClass().getDeclaredField(property);
              field.setAccessible(true);
              List list = (List) field.get(relatedEntity);
              list.add(jpaEntity);

              jpaEntity = relatedEntity;

            } catch (Exception e) {
              e.printStackTrace();
            }

          } else {
            property = createView.getNavigationSegments().get(0).getNavigationProperty().getRelationship().getReferentialConstraint().getDependent().getPropertyRefNames().get(0);

            try {
              Field field = jpaEntity.getClass().getDeclaredField(property);
              field.setAccessible(true);
              field.set(jpaEntity, relatedEntity);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }

      if (manymany) {
        if (listener != null) {
          listener.execEvent(((UriInfoImpl) createView), oDataEntityType, "beforeInsert", jpaEntity);
        }

        em.merge(jpaEntity);
      } else {
        if (listener != null) {
          listener.execEvent(((UriInfoImpl) createView), oDataEntityType, "beforeInsert", tempEntity);
        }

        Object resultEntity = listener.overridePost((UriInfo) createView, jpaEntity);

        if (resultEntity != null) {
          return resultEntity;
        }

        em.persist(jpaEntity);
      }

      if (em.contains(jpaEntity)) {

        if (manymany) {
          jpaEntity = tempEntity;
        }

        EdmEntityType edmEntityType = createView.getEntityContainer().getEntitySet(createView.getTargetEntitySet()
            .getEntityType().getMapping().getInternalName()).getEntityType();

        JPAEntityParser jpaResultParser = new JPAEntityParser(oDataJPAContext, (UriInfo) createView);

        List<KeyPredicate> predicates = new ArrayList<KeyPredicate>();

        ((UriInfoImpl) createView).setKeyPredicates(predicates);
        ((UriInfoImpl) createView).setRawEntity(false);

        em.flush();
        em.clear();

        HashMap<String, Object> edmPropertyValueMap = jpaResultParser.parse2EdmPropertyValueMap(jpaEntity, edmEntityType);

        for (EdmProperty key : createView.getTargetEntitySet().getEntityType().getKeyProperties()) {
          final EdmSimpleType type = (EdmSimpleType) key.getType();
          final EdmFacets facets = key.getFacets();
          Object value = edmPropertyValueMap.get(key.getName());
          String literal = type.valueToString(value, EdmLiteralKind.DEFAULT, facets);
          KeyPredicateImpl predicate = new KeyPredicateImpl(literal, key);
          predicates.add(predicate);
          createView.getNavigationSegments().clear();
        }

        ((UriInfoImpl) createView).composeWhere(false);
        Object resultEntity = readEntity(new JPAQueryBuilder(oDataJPAContext).build((GetEntityUriInfo) createView), (UriInfo) createView);
        ((UriInfoImpl) createView).composeWhere(true);

        if (resultEntity == null) {
          throw new RuntimeException("Entity not found after insert, check your query and default values");
        }

        if (listener != null) {
          listener.execEvent(((UriInfoImpl) createView), oDataEntityType, "afterInsert", resultEntity);
        }

        if (isLocalTransaction) {
          oDataJPAContext.getODataJPATransaction().commit();
        }

        return resultEntity;
      }
    } catch (ODataBadRequestException e) {
      throw new RuntimeException(e);
    } catch (EdmException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  private <T> Object processUpdate(PutMergePatchUriInfo updateView,
      final InputStream content, final Map<String, Object> properties, final String requestContentType)
      throws ODataJPAModelException, ODataJPARuntimeException {
    Object jpaEntity = null;
    try {
      JPAQueryBuilder queryBuilder = new JPAQueryBuilder(oDataJPAContext);
      ODataJPAQueryExtensionEntityListener listener = null;
      try {
        listener = queryBuilder.getODataJPAQueryEntityListener((UriInfo) updateView);
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (listener != null) {
        listener.checkAuthorization(updateView);
      }

      boolean isLocalTransaction = setTransaction();
      ((UriInfoImpl) updateView).setRawEntity(true);

      boolean canOverride = listener.canOverridePut((UriInfo) updateView);

      if (!canOverride) {
        jpaEntity = readEntity(queryBuilder.build(updateView), (UriInfo) updateView, true);

        if (jpaEntity == null) {
          throw ODataJPARuntimeException
              .throwException(ODataJPARuntimeException.RESOURCE_NOT_FOUND, null);
        }
      } else {
        jpaEntity = new VirtualClass();
      }

      final EdmEntitySet oDataEntitySet = updateView.getTargetEntitySet();
      final EdmEntityType oDataEntityType = oDataEntitySet.getEntityType();

      final JPAEntity virtualJPAEntity = new JPAEntity(oDataEntityType, oDataEntitySet, oDataJPAContext);
      virtualJPAEntity.setJPAEntity(jpaEntity);
      if (content != null) {
        final ODataEntityParser oDataEntityParser = new ODataEntityParser(oDataJPAContext);
        ODataEntry oDataEntry;
        oDataEntry = oDataEntityParser.parseEntry((UriInfo) updateView, oDataEntitySet, content, requestContentType, false);
        if (!canOverride) {
          virtualJPAEntity.update(oDataEntry);
        } else {
          virtualJPAEntity.create(oDataEntry);
          jpaEntity = virtualJPAEntity.getJPAEntity();
        }
      } else if (properties != null) {
        virtualJPAEntity.update(properties);
      } else {
        return null;
      }

      Object overridePut = null;
      if (listener != null) {
        listener.execEvent(((UriInfoImpl) updateView), oDataEntityType, "beforeUpdate", jpaEntity);
        overridePut = listener.overridePut((UriInfo) updateView, jpaEntity);
      }

      ((UriInfoImpl) updateView).setRawEntity(false);
      if (overridePut != null) {
        jpaEntity = overridePut;
      } else {
        em.flush();
        em.clear();

        ((UriInfoImpl) updateView).composeWhere(false);
        jpaEntity = readEntity(queryBuilder.build(updateView), (UriInfo) updateView);
        ((UriInfoImpl) updateView).composeWhere(true);

        if (listener != null) {
          listener.execEvent(((UriInfoImpl) updateView), oDataEntityType, "afterUpdate", jpaEntity);
        }

        if (isLocalTransaction) {
          oDataJPAContext.getODataJPATransaction().commit();
        }
      }

    } catch (ODataBadRequestException e) {
      throw new RuntimeException(e);
    } catch (EdmException e) {
      throw new RuntimeException(e);
    } catch (PersistenceException e) {
      em.getTransaction().rollback();
      throw new RuntimeException(e);
    }

    return jpaEntity;
  }

  private Object deleteLink(final DeleteUriInfo uriParserResultView) throws ODataJPARuntimeException {
    JPALink link = new JPALink(oDataJPAContext);
    link.delete(uriParserResultView);
    link.save();
    return link.getTargetJPAEntity();
  }

  private List<Object> handlePaging(final List<Object> result, final GetEntitySetUriInfo uriParserResultView) {
    if (result == null) {
      return null;
    }
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    pageBuilder.pageSize(oDataJPAContext.getPageSize())
        .entities(result)
        .skipToken(uriParserResultView.getSkipToken());

    // $top/$skip with $inlinecount case handled in response builder to avoid multiple DB call
    if (uriParserResultView.getSkip() != null) {
      pageBuilder.skip(uriParserResultView.getSkip().intValue());
    }

    if (uriParserResultView.getTop() != null) {
      pageBuilder.top(uriParserResultView.getTop().intValue());
    }

    JPAPage page = pageBuilder.build();
    oDataJPAContext.setPaging(page);

    return page.getPagedEntities();
  }

  private List<Object> handlePaging(final Query query, final GetEntitySetUriInfo uriParserResultView, JPAQueryBuilder queryBuilder) {

    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    pageBuilder.pageSize(oDataJPAContext.getPageSize())
        .query(query)
        .skipToken(uriParserResultView.getSkipToken());

    // $top/$skip with $inlinecount case handled in response builder to avoid multiple DB call
    if (uriParserResultView.getSkip() != null) {
      pageBuilder.skip(uriParserResultView.getSkip().intValue());
    }

    if (uriParserResultView.getTop() != null) {
      pageBuilder.top(uriParserResultView.getTop().intValue());
    }

    JPAPage page = pageBuilder.build();
    oDataJPAContext.setPaging(page);

    List<Object> entities = normalizeList(page.getPagedEntities(), (UriInfo) uriParserResultView);

    return entities;

  }

  private List normalizeList(List entities, final UriInfo uriParserResultView) {
    if (entities != null && uriParserResultView != null && !entities.isEmpty()) {
      try {

        if (((JPAEdmMappingImpl) ((EdmEntityTypeImplProv) uriParserResultView.getTargetType()).getMapping()).isVirtualAccess()) {
          List<String> names = uriParserResultView.getTargetEntitySet().getEntityType().getPropertyNames();

          List<Object> newEntities = new ArrayList<Object>(entities.size());
          for (Object obj : entities) {
            VirtualClassInterface entity;

            if (obj instanceof JsonElement) {
              entity = new VirtualClassWrapper(obj);
            }
            else if (obj instanceof VirtualClassInterface) {
              entity = (VirtualClassInterface) obj;
            } else {
              entity = new VirtualClass();
              if (obj.getClass().isArray()) {
                int i = 0;

                for (Object o : (Object[]) obj) {
                  String key = names.get(i);
                  entity.set(key, o);
                  i++;
                }
              } else {
                String key = names.get(0);
                entity.set(key, obj);
              }
            }

            newEntities.add(entity);

          }
          entities = newEntities;
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    return entities;

  }

  private boolean setTransaction() {
    ODataJPATransaction transaction = oDataJPAContext.getODataJPATransaction();
    if (!transaction.isActive()) {
      transaction.begin();
      return true;
    }
    return false;
  }
}
