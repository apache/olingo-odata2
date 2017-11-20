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

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityLinkUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetLinksUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetFunctionImportUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATransaction;
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
        result = handlePaging(query, uriParserResultView);
      }
      if (listener != null && listener.isTombstoneSupported()) {
        ODataJPATombstoneContext.setDeltaToken(listener.generateDeltaToken((List<Object>) result, query));
      }
      return result == null ? new ArrayList<Object>() : result;
    } catch (EdmException e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    } catch (InstantiationException e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    } catch (IllegalAccessException e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
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
    return readEntity(new JPAQueryBuilder(oDataJPAContext).build(uriParserResultView));
  }

  /* Process $count for Get Entity Set Request */
  @Override
  public long process(final GetEntitySetCountUriInfo resultsView)
      throws ODataJPAModelException, ODataJPARuntimeException {

    JPAQueryBuilder queryBuilder = new JPAQueryBuilder(oDataJPAContext);
    Query query = queryBuilder.build(resultsView);
    setPositionalParametersToQuery(query);
    List<?> resultList = query.getResultList();
    if (resultList != null && resultList.size() == 1) {
      return Long.valueOf(resultList.get(0).toString());
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
    Object selectedObject = readEntity(new JPAQueryBuilder(oDataJPAContext).build(uriParserResultView));
    if (selectedObject != null) {
      try{
        boolean isLocalTransaction = setTransaction();
        em.remove(selectedObject);
        em.flush(); 
        if (isLocalTransaction) {
          oDataJPAContext.getODataJPATransaction().commit();
        }
      } catch(PersistenceException e){
        em.getTransaction().rollback();
        throw ODataJPARuntimeException.throwException(
            ODataJPARuntimeException.ERROR_JPQL_DELETE_REQUEST, e);
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

  /* Common method for Read and Delete */
  private Object readEntity(final Query query) throws ODataJPARuntimeException {
    Object selectedObject = null;
    @SuppressWarnings("rawtypes")
    final List resultList = query.getResultList();
    if (!resultList.isEmpty()) {
      selectedObject = resultList.get(0);
    }
    return selectedObject;
  }

  private Object processCreate(final PostUriInfo createView, final InputStream content,
      final Map<String, Object> properties,
      final String requestedContentType) throws ODataJPAModelException,
      ODataJPARuntimeException {
    try {
      final EdmEntitySet oDataEntitySet = createView.getTargetEntitySet();
      final EdmEntityType oDataEntityType = oDataEntitySet.getEntityType();
      final JPAEntity virtualJPAEntity = new JPAEntity(oDataEntityType, oDataEntitySet, oDataJPAContext);
      Object jpaEntity = null;

      if (content != null) {
        final ODataEntityParser oDataEntityParser = new ODataEntityParser(oDataJPAContext);
        final ODataEntry oDataEntry =
            oDataEntityParser.parseEntry(oDataEntitySet, content, requestedContentType, false);
        virtualJPAEntity.create(oDataEntry);
      } else if (properties != null) {
        virtualJPAEntity.create(properties);
      } else {
        return null;
      }

      boolean isLocalTransaction = setTransaction();
      jpaEntity = virtualJPAEntity.getJPAEntity();

      em.persist(jpaEntity);
      if (em.contains(jpaEntity)) {
        if (isLocalTransaction) {
          oDataJPAContext.getODataJPATransaction().commit();
        }
        return jpaEntity;
      }
    } catch (ODataBadRequestException e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    } catch (EdmException e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    }
    return null;
  }

  private <T> Object processUpdate(PutMergePatchUriInfo updateView,
      final InputStream content, final Map<String, Object> properties, final String requestContentType)
      throws ODataJPAModelException, ODataJPARuntimeException {
    Object jpaEntity = null;
    try {
      boolean isLocalTransaction = setTransaction();
      jpaEntity = readEntity(new JPAQueryBuilder(oDataJPAContext).build(updateView));

      if (jpaEntity == null) {
        throw ODataJPARuntimeException
            .throwException(ODataJPARuntimeException.RESOURCE_NOT_FOUND, null);
      }

      final EdmEntitySet oDataEntitySet = updateView.getTargetEntitySet();
      final EdmEntityType oDataEntityType = oDataEntitySet.getEntityType();
      final JPAEntity virtualJPAEntity = new JPAEntity(oDataEntityType, oDataEntitySet, oDataJPAContext);
      virtualJPAEntity.setJPAEntity(jpaEntity);
      if (content != null) {
        final ODataEntityParser oDataEntityParser = new ODataEntityParser(oDataJPAContext);
        ODataEntry oDataEntry;
        oDataEntry = oDataEntityParser.parseEntry(oDataEntitySet, content, requestContentType, false);
        virtualJPAEntity.update(oDataEntry);
      } else if (properties != null) {
        virtualJPAEntity.update(properties);
      } else {
        return null;
      }
      em.flush();
      if (isLocalTransaction) {
        oDataJPAContext.getODataJPATransaction().commit();
      }
    } catch (ODataBadRequestException e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    } catch (EdmException e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    } catch (PersistenceException e) {
      em.getTransaction().rollback();
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
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
    if (uriParserResultView.getSkip() != null && uriParserResultView.getInlineCount() == null) {
      pageBuilder.skip(uriParserResultView.getSkip().intValue());
    }

    if (uriParserResultView.getTop() != null && uriParserResultView.getInlineCount() == null) {
      pageBuilder.top(uriParserResultView.getTop().intValue());
    }

    JPAPage page = pageBuilder.build();
    oDataJPAContext.setPaging(page);

    return page.getPagedEntities();
  }

  private List<Object> handlePaging(final Query query, final GetEntitySetUriInfo uriParserResultView) {

    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    pageBuilder.pageSize(oDataJPAContext.getPageSize())
        .query(query)
        .skipToken(uriParserResultView.getSkipToken());

    // $top/$skip with $inlinecount case handled in response builder to avoid multiple DB call
    if (uriParserResultView.getSkip() != null && uriParserResultView.getInlineCount() == null) {
      pageBuilder.skip(uriParserResultView.getSkip().intValue());
    }

    if (uriParserResultView.getTop() != null && uriParserResultView.getInlineCount() == null) {
      pageBuilder.top(uriParserResultView.getTop().intValue());
    }

    JPAPage page = pageBuilder.build();
    oDataJPAContext.setPaging(page);

    return page.getPagedEntities();

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
