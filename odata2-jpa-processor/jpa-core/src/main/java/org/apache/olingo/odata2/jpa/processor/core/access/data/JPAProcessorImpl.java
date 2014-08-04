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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
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
import org.apache.olingo.odata2.jpa.processor.api.access.JPAFunction;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAMethodContext;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAProcessor;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContext;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.core.ODataEntityParser;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAPage.JPAPageBuilder;

public class JPAProcessorImpl implements JPAProcessor {

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

    if (uriParserResultView.getFunctionImport() != null) {
      return (List<Object>) process((GetFunctionImportUriInfo) uriParserResultView);
    }
    InlineCount inlineCount = uriParserResultView.getInlineCount();
    Integer top = uriParserResultView.getTop();
    if (top != null && top.intValue() == 0 && inlineCount != null && inlineCount.equals(InlineCount.ALLPAGES)) {
      return new ArrayList<Object>();
    }
    JPQLContextType contextType = null;
    try {
      if (!uriParserResultView.getStartEntitySet().getName()
          .equals(uriParserResultView.getTargetEntitySet().getName())) {
        contextType = JPQLContextType.JOIN;
      } else {
        contextType = JPQLContextType.SELECT;
      }

      JPQLContext jpqlContext = null;

      if (oDataJPAContext.getPageSize() > 0) {
        jpqlContext = JPQLContext.createBuilder(contextType,
            uriParserResultView, true).build();
      } else {
        jpqlContext = JPQLContext.createBuilder(contextType,
            uriParserResultView).build();
      }

      JPQLStatement jpqlStatement = JPQLStatement.createBuilder(jpqlContext)
          .build();
      Map<String, String> customQueryOptions = uriParserResultView.getCustomQueryOptions();
      String deltaToken = null;
      if (customQueryOptions != null) {
        deltaToken = uriParserResultView.getCustomQueryOptions().get("!deltatoken");
      }
      if (deltaToken != null) {
        ODataJPATombstoneContext.setDeltaToken(deltaToken);
      }

      Query query = null;
      List<Object> result = null;

      JPAEdmMapping mapping = (JPAEdmMapping) uriParserResultView.getTargetEntitySet().getEntityType().getMapping();
      ODataJPATombstoneEntityListener listener = null;
      if (mapping.getODataJPATombstoneEntityListener() != null) {
        listener = (ODataJPATombstoneEntityListener) mapping.getODataJPATombstoneEntityListener().newInstance();
        query = listener.getQuery(uriParserResultView, em);
      }
      if (query == null) {
        query = em.createQuery(jpqlStatement.toString());
        if (listener != null) {
          query.getResultList();
          List<Object> deltaResult =
              (List<Object>) ODataJPATombstoneContext.getDeltaResult(((EdmMapping) mapping).getInternalName());
          result = handlePaging(deltaResult, uriParserResultView);
        } else {
          result = handlePaging(query, uriParserResultView);
        }
      } else {
        result = handlePaging(query, uriParserResultView);
      }

      // Set New Token
      if (listener != null) {
        ODataJPATombstoneContext.setDeltaToken(listener.generateDeltaToken((List<Object>) result, query));
      }

      return result == null ? new ArrayList<Object>() : result;

    } catch (Exception e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);

    }
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

  /* Process Get Entity Request (Read) */
  @Override
  public <T> Object process(GetEntityUriInfo uriParserResultView)
      throws ODataJPAModelException, ODataJPARuntimeException {

    JPQLContextType contextType = null;
    try {
      if (uriParserResultView instanceof GetEntityUriInfo) {
        uriParserResultView = ((GetEntityUriInfo) uriParserResultView);
        if (!((GetEntityUriInfo) uriParserResultView).getStartEntitySet().getName()
            .equals(((GetEntityUriInfo) uriParserResultView).getTargetEntitySet().getName())) {
          contextType = JPQLContextType.JOIN_SINGLE;
        } else {
          contextType = JPQLContextType.SELECT_SINGLE;
        }
      }
    } catch (EdmException e) {
      ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.GENERAL, e);
    }

    return readEntity(uriParserResultView, contextType);
  }

  /* Process $count for Get Entity Set Request */
  @Override
  public long process(final GetEntitySetCountUriInfo resultsView)
      throws ODataJPAModelException, ODataJPARuntimeException {

    JPQLContextType contextType = null;
    try {
      if (!resultsView.getStartEntitySet().getName()
          .equals(resultsView.getTargetEntitySet().getName())) {
        contextType = JPQLContextType.JOIN_COUNT;
      } else {
        contextType = JPQLContextType.SELECT_COUNT;
      }
    } catch (EdmException e) {
      ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.GENERAL, e);
    }

    JPQLContext jpqlContext = JPQLContext.createBuilder(contextType,
        resultsView).build();

    JPQLStatement jpqlStatement = JPQLStatement.createBuilder(jpqlContext)
        .build();
    Query query = null;
    try {

      query = em.createQuery(jpqlStatement.toString());
      List<?> resultList = query.getResultList();
      if (resultList != null && resultList.size() == 1) {
        return Long.valueOf(resultList.get(0).toString());
      }
    } catch (IllegalArgumentException e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    }
    return 0;
  }

  /* Process $count for Get Entity Request */
  @Override
  public long process(final GetEntityCountUriInfo resultsView) throws ODataJPAModelException, ODataJPARuntimeException {

    JPQLContextType contextType = null;
    try {
      if (!resultsView.getStartEntitySet().getName()
          .equals(resultsView.getTargetEntitySet().getName())) {
        contextType = JPQLContextType.JOIN_COUNT;
      } else {
        contextType = JPQLContextType.SELECT_COUNT;
      }
    } catch (EdmException e) {
      ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.GENERAL, e);
    }

    JPQLContext jpqlContext = JPQLContext.createBuilder(contextType,
        resultsView).build();

    JPQLStatement jpqlStatement = JPQLStatement.createBuilder(jpqlContext)
        .build();
    Query query = null;
    try {

      query = em.createQuery(jpqlStatement.toString());
      List<?> resultList = query.getResultList();
      if (resultList != null && resultList.size() == 1) {
        return Long.valueOf(resultList.get(0).toString());
      }
    } catch (IllegalArgumentException e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
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

      em.getTransaction().begin();
      jpaEntity = virtualJPAEntity.getJPAEntity();

      em.persist(jpaEntity);
      if (em.contains(jpaEntity)) {
        em.getTransaction().commit();

        return jpaEntity;

      }
    } catch (Exception e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_CREATE_REQUEST, e);
    }
    return null;
  }

  public <T> Object processUpdate(PutMergePatchUriInfo updateView,
      final InputStream content, final Map<String, Object> properties, final String requestContentType)
      throws ODataJPAModelException, ODataJPARuntimeException {
    JPQLContextType contextType = null;
    Object jpaEntity = null;
    try {
      em.getTransaction().begin();
      if (updateView instanceof PutMergePatchUriInfo) {
        updateView = ((PutMergePatchUriInfo) updateView);
        if (!((PutMergePatchUriInfo) updateView).getStartEntitySet().getName()
            .equals(((PutMergePatchUriInfo) updateView).getTargetEntitySet().getName())) {
          contextType = JPQLContextType.JOIN_SINGLE;
        } else {
          contextType = JPQLContextType.SELECT_SINGLE;
        }
      }

      jpaEntity = readEntity(updateView, contextType);

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
        final ODataEntry oDataEntry = oDataEntityParser.parseEntry(oDataEntitySet, content, requestContentType, false);
        virtualJPAEntity.update(oDataEntry);
      } else if (properties != null) {
        virtualJPAEntity.update(properties);
      } else {
        return null;
      }
      em.flush();
      em.getTransaction().commit();
    } catch (Exception e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_UPDATE_REQUEST, e);
    }

    return jpaEntity;
  }

  /* Process Delete Entity Request */
  @Override
  public Object process(DeleteUriInfo uriParserResultView, final String contentType)
      throws ODataJPAModelException, ODataJPARuntimeException {
    JPQLContextType contextType = null;
    try {
      if (uriParserResultView instanceof DeleteUriInfo) {
        if (((UriInfo) uriParserResultView).isLinks()) {
          return deleteLink(uriParserResultView);
        }
        uriParserResultView = ((DeleteUriInfo) uriParserResultView);
        if (!((DeleteUriInfo) uriParserResultView).getStartEntitySet().getName()
            .equals(((DeleteUriInfo) uriParserResultView).getTargetEntitySet().getName())) {
          contextType = JPQLContextType.JOIN_SINGLE;
        } else {
          contextType = JPQLContextType.SELECT_SINGLE;
        }
      }
    } catch (EdmException e) {
      ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.GENERAL, e);
    }

    Object selectedObject = readEntity(uriParserResultView, contextType);
    if (selectedObject != null) {
      try {
        em.getTransaction().begin();
        em.remove(selectedObject);
        em.flush();
        em.getTransaction().commit();

      } catch (Exception e) {
        throw ODataJPARuntimeException.throwException(
            ODataJPARuntimeException.ERROR_JPQL_DELETE_REQUEST, e);
      }
    }
    return selectedObject;
  }

  private Object deleteLink(final DeleteUriInfo uriParserResultView) throws ODataJPARuntimeException {
    JPALink link = new JPALink(oDataJPAContext);
    link.delete(uriParserResultView);
    link.save();
    return link.getTargetJPAEntity();
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

  /* Common method for Read and Delete */
  private Object readEntity(final Object uriParserResultView, final JPQLContextType contextType)
      throws ODataJPAModelException, ODataJPARuntimeException {

    Object selectedObject = null;

    if (uriParserResultView instanceof DeleteUriInfo || uriParserResultView instanceof GetEntityUriInfo
        || uriParserResultView instanceof PutMergePatchUriInfo) {

      JPQLContext selectJPQLContext = JPQLContext.createBuilder(
          contextType, uriParserResultView).build();

      JPQLStatement selectJPQLStatement = JPQLStatement.createBuilder(
          selectJPQLContext).build();
      Query query = null;
      try {
        query = em.createQuery(selectJPQLStatement.toString());
        if (!query.getResultList().isEmpty()) {
          selectedObject = query.getResultList().get(0);
        }
      } catch (IllegalArgumentException e) {
        throw ODataJPARuntimeException.throwException(
            ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
      }
    }
    return selectedObject;
  }

  @Override
  public void process(final PutMergePatchUriInfo putUriInfo,
      final InputStream content, final String requestContentType, final String contentType)
      throws ODataJPARuntimeException, ODataJPAModelException {

    JPALink link = new JPALink(oDataJPAContext);
    link.update(putUriInfo, content, requestContentType, contentType);
    link.save();

  }
}
