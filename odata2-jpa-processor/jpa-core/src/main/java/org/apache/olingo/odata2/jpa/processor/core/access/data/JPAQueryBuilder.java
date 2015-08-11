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

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAQueryExtensionEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContext;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;

public class JPAQueryBuilder {

  public enum UriInfoType {
    GetEntitySet,
    GetEntity,
    GetEntitySetCount,
    GetEntityCount,
    PutMergePatch,
    Delete
  };

  private EntityManager em = null;
  private int pageSize = 0;

  public JPAQueryBuilder(ODataJPAContext odataJPAContext) {
    this.em = odataJPAContext.getEntityManager();
    this.pageSize = odataJPAContext.getPageSize();
  }

  public JPAQueryInfo build(GetEntitySetUriInfo uriInfo) throws ODataJPARuntimeException {
    JPAQueryInfo queryInfo = new JPAQueryInfo();
    Query query = null;
    try {
      ODataJPATombstoneEntityListener listener = getODataJPATombstoneEntityListener((UriInfo) uriInfo);
      if (listener != null) {
        query = listener.getQuery(uriInfo, em);
      }
      if (query == null) {
        query = buildQuery((UriInfo) uriInfo, UriInfoType.GetEntitySet);
      } else {
        queryInfo.setTombstoneQuery(true);
      }
    } catch (Exception e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    }
    queryInfo.setQuery(query);
    return queryInfo;
  }

  public Query build(GetEntityUriInfo uriInfo) throws ODataJPARuntimeException {
    Query query = null;
    try {
      ODataJPAQueryExtensionEntityListener listener = getODataJPAQuertEntityListener((UriInfo) uriInfo);
      if (listener != null) {
        query = listener.getQuery(uriInfo, em);
      }
      if (query == null) {
        query = buildQuery((UriInfo) uriInfo, UriInfoType.GetEntity);
      }
    } catch (Exception e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    }
    return query;
  }

  public Query build(GetEntitySetCountUriInfo uriInfo) throws ODataJPARuntimeException {
    Query query = null;
    try {
      ODataJPAQueryExtensionEntityListener listener = getODataJPAQuertEntityListener((UriInfo) uriInfo);
      if (listener != null) {
        query = listener.getQuery(uriInfo, em);
      }
      if (query == null) {
        query = buildQuery((UriInfo) uriInfo, UriInfoType.GetEntitySetCount);
      }
    } catch (Exception e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    }
    return query;
  }

  public Query build(GetEntityCountUriInfo uriInfo) throws ODataJPARuntimeException {
    Query query = null;
    try {
      ODataJPAQueryExtensionEntityListener listener = getODataJPAQuertEntityListener((UriInfo) uriInfo);
      if (listener != null) {
        query = listener.getQuery(uriInfo, em);
      }
      if (query == null) {
        query = buildQuery((UriInfo) uriInfo, UriInfoType.GetEntityCount);
      }
    } catch (Exception e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    }
    return query;
  }

  public Query build(DeleteUriInfo uriInfo) throws ODataJPARuntimeException {
    Query query = null;
    try {
      ODataJPAQueryExtensionEntityListener listener = getODataJPAQuertEntityListener((UriInfo) uriInfo);
      if (listener != null) {
        query = listener.getQuery(uriInfo, em);
      }
      if (query == null) {
        query = buildQuery((UriInfo) uriInfo, UriInfoType.Delete);
      }
    } catch (Exception e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    }
    return query;
  }

  public Query build(PutMergePatchUriInfo uriInfo) throws ODataJPARuntimeException {
    Query query = null;
    try {
      ODataJPAQueryExtensionEntityListener listener = getODataJPAQuertEntityListener((UriInfo) uriInfo);
      if (listener != null) {
        query = listener.getQuery(uriInfo, em);
      }
      if (query == null) {
        query = buildQuery((UriInfo) uriInfo, UriInfoType.PutMergePatch);
      }
    } catch (Exception e) {
      throw ODataJPARuntimeException.throwException(
          ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
    }
    return query;
  }

  private Query buildQuery(UriInfo uriParserResultView, UriInfoType type)
      throws EdmException,
      ODataJPAModelException, ODataJPARuntimeException {

    Query query = null;
    JPQLContextType contextType = determineJPQLContextType(uriParserResultView, type);
    JPQLContext jpqlContext = buildJPQLContext(contextType, uriParserResultView);
    JPQLStatement jpqlStatement = JPQLStatement.createBuilder(jpqlContext)
        .build();
    query = em.createQuery(jpqlStatement.toString());

    return query;
  }

  public ODataJPAQueryExtensionEntityListener getODataJPAQuertEntityListener(UriInfo uriInfo) throws EdmException,
      InstantiationException, IllegalAccessException {
    ODataJPAQueryExtensionEntityListener queryListener = null;
    ODataJPATombstoneEntityListener listener = getODataJPATombstoneEntityListener(uriInfo);
    if (listener instanceof ODataJPAQueryExtensionEntityListener) {
      queryListener = (ODataJPAQueryExtensionEntityListener) listener;
    }
    return queryListener;
  }

  public ODataJPATombstoneEntityListener getODataJPATombstoneEntityListener(UriInfo uriParserResultView)
      throws InstantiationException, IllegalAccessException, EdmException {
    JPAEdmMapping mapping = (JPAEdmMapping) uriParserResultView.getTargetEntitySet().getEntityType().getMapping();
    ODataJPATombstoneEntityListener listener = null;
    if (mapping.getODataJPATombstoneEntityListener() != null) {
      listener = (ODataJPATombstoneEntityListener) mapping.getODataJPATombstoneEntityListener().newInstance();
    }
    return listener;
  }

  public JPQLContext buildJPQLContext(JPQLContextType contextType, UriInfo uriParserResultView)
      throws ODataJPAModelException, ODataJPARuntimeException {
    JPQLContext jpqlContext = null;
    if (pageSize > 0 && (contextType == JPQLContextType.SELECT || contextType == JPQLContextType.JOIN)) {
      jpqlContext = JPQLContext.createBuilder(contextType,
          uriParserResultView, true).build();
    } else {
      jpqlContext = JPQLContext.createBuilder(contextType,
          uriParserResultView).build();
    }
    return jpqlContext;
  }

  public JPQLContextType determineJPQLContextType(UriInfo uriParserResultView, UriInfoType type) {
    JPQLContextType contextType = null;

    if (uriParserResultView.getNavigationSegments().size() > 0) {
      if (type == UriInfoType.GetEntitySet) {
        contextType = JPQLContextType.JOIN;
      } else if (type == UriInfoType.Delete || type == UriInfoType.Delete || type == UriInfoType.GetEntity
          || type == UriInfoType.PutMergePatch) {
        contextType = JPQLContextType.JOIN_SINGLE;
      } else if (type == UriInfoType.GetEntitySetCount || type == UriInfoType.GetEntityCount) {
        contextType = JPQLContextType.JOIN_COUNT;
      }
    } else {
      if (type == UriInfoType.GetEntitySet) {
        contextType = JPQLContextType.SELECT;
      } else if (type == UriInfoType.Delete || type == UriInfoType.GetEntity
          || type == UriInfoType.PutMergePatch) {
        contextType = JPQLContextType.SELECT_SINGLE;
      } else if (type == UriInfoType.GetEntitySetCount || type == UriInfoType.GetEntityCount) {
        contextType = JPQLContextType.SELECT_COUNT;
      }
    }
    return contextType;
  }

  public final class JPAQueryInfo {
    private Query query = null;
    private boolean isTombstoneQuery = false;

    public Query getQuery() {
      return query;
    }

    public void setQuery(Query query) {
      this.query = query;
    }

    public boolean isTombstoneQuery() {
      return isTombstoneQuery;
    }

    public void setTombstoneQuery(boolean isTombstoneQuery) {
      this.isTombstoneQuery = isTombstoneQuery;
    }
  }
}
