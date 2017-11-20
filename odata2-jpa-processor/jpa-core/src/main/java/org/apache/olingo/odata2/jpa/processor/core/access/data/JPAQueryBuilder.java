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

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

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
import org.apache.olingo.odata2.jpa.processor.core.ODataParameterizedWhereExpressionUtil;

public class JPAQueryBuilder {

  enum UriInfoType {
    GetEntitySet,
    GetEntity,
    GetEntitySetCount,
    GetEntityCount,
    PutMergePatch,
    Delete
  }

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
      ODataJPAQueryExtensionEntityListener listener = getODataJPAQueryEntityListener((UriInfo) uriInfo);
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
      ODataJPAQueryExtensionEntityListener listener = getODataJPAQueryEntityListener((UriInfo) uriInfo);
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
      ODataJPAQueryExtensionEntityListener listener = getODataJPAQueryEntityListener((UriInfo) uriInfo);
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
      ODataJPAQueryExtensionEntityListener listener = getODataJPAQueryEntityListener((UriInfo) uriInfo);
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
      ODataJPAQueryExtensionEntityListener listener = getODataJPAQueryEntityListener((UriInfo) uriInfo);
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

    JPQLContextType contextType = determineJPQLContextType(uriParserResultView, type);
    JPQLContext jpqlContext = buildJPQLContext(contextType, uriParserResultView);
    JPQLStatement jpqlStatement = JPQLStatement.createBuilder(jpqlContext).build();

    Query query = em.createQuery(normalizeMembers(em, jpqlStatement.toString()));
    Map<String, Map<Integer, Object>> parameterizedMap = ODataParameterizedWhereExpressionUtil.
        getParameterizedQueryMap();
    if (parameterizedMap != null && parameterizedMap.size() > 0) {
      for (Entry<String, Map<Integer, Object>> parameterEntry : parameterizedMap.entrySet()) {
        if (jpqlStatement.toString().contains(parameterEntry.getKey())) {
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
    return query;
  }

  

  public ODataJPAQueryExtensionEntityListener getODataJPAQueryEntityListener(UriInfo uriInfo) throws EdmException,
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
    if (mapping.getODataJPATombstoneEntityListener() != null) {
      return  (ODataJPATombstoneEntityListener) mapping.getODataJPATombstoneEntityListener().newInstance();
    }
    return null;
  }

  public JPQLContext buildJPQLContext(JPQLContextType contextType, UriInfo uriParserResultView)
      throws ODataJPAModelException, ODataJPARuntimeException {
    if (pageSize > 0 && (contextType == JPQLContextType.SELECT || contextType == JPQLContextType.JOIN)) {
      return JPQLContext.createBuilder(contextType, uriParserResultView, true).build();
    } else {
      return JPQLContext.createBuilder(contextType, uriParserResultView).build();
    }
  }

  public JPQLContextType determineJPQLContextType(UriInfo uriParserResultView, UriInfoType type) {
    JPQLContextType contextType = null;

    if (!uriParserResultView.getNavigationSegments().isEmpty()) {
      if (type == UriInfoType.GetEntitySet) {
        contextType = JPQLContextType.JOIN;
      } else if (type == UriInfoType.Delete || type == UriInfoType.GetEntity
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

  private static final Pattern NORMALIZATION_NEEDED_PATTERN = Pattern.compile(".*[\\s(](\\S+\\.\\S+\\.\\S+).*");
  private static final Pattern VALUE_NORM_PATTERN = Pattern.compile("(?:^|\\s|\\()'(([^']*)')");
  private static final Pattern JOIN_ALIAS_PATTERN = Pattern.compile(".*\\sJOIN\\s(\\S*\\s\\S*).*");

  private static String normalizeMembers(EntityManager em, String jpqlQuery) {  
    
    //check if clause values are string with x.y.z format
    //starting with quotes;
    String query = checkConditionValues(jpqlQuery);
    //remove any orderby clause parameters  with x.y.z format
    //no normalization for such clause
    query = removeExtraClause(jpqlQuery);
    // check if normalization is needed (if query contains "x.y.z" elements
    // starting with space or parenthesis)
    Matcher normalizationNeededMatcher = NORMALIZATION_NEEDED_PATTERN.matcher(query);
    if (!normalizationNeededMatcher.find()) {
      return jpqlQuery;
    }

    if (containsEmbeddedAttributes(em, jpqlQuery)) {
      return jpqlQuery;
    }
    
    String normalizedJpqlQuery = jpqlQuery;
    Map<String, String> joinAliases = new HashMap<String, String>();

    // collect information about existing joins/aliases
    Matcher joinAliasMatcher = JOIN_ALIAS_PATTERN.matcher(normalizedJpqlQuery);
    if (joinAliasMatcher.find()) {
      for (int i = 1; i <= joinAliasMatcher.groupCount(); i++) {
        String[] joinAlias = joinAliasMatcher.group(i).split(String.valueOf(JPQLStatement.DELIMITER.SPACE));
        joinAliases.put(joinAlias[0], joinAlias[1]);
      }
    }
    // normalize query
    boolean normalizationNeeded = true;
    while (normalizationNeeded) {
      String membershipToNormalize = normalizationNeededMatcher.group(1);

      // get member info
      String memberInfo = membershipToNormalize.substring(0,
          ordinalIndexOf(membershipToNormalize, JPQLStatement.DELIMITER.PERIOD, 1));

      String alias;
      if (joinAliases.containsKey(memberInfo)) {
        // use existing alias
        alias = joinAliases.get(memberInfo);
      } else {
        // create new join/alias
        alias = "R" + (joinAliases.size() + 1);

        int joinInsertPosition = normalizedJpqlQuery.indexOf(JPQLStatement.KEYWORD.WHERE);
        if (joinInsertPosition == -1) {
          joinInsertPosition = normalizedJpqlQuery.indexOf(JPQLStatement.KEYWORD.ORDERBY);
        }
        normalizedJpqlQuery = normalizedJpqlQuery.substring(0, joinInsertPosition) + JPQLStatement.KEYWORD.JOIN
            + JPQLStatement.DELIMITER.SPACE + memberInfo + JPQLStatement.DELIMITER.SPACE + alias
            + JPQLStatement.DELIMITER.SPACE + normalizedJpqlQuery.substring(joinInsertPosition);

        joinAliases.put(memberInfo, alias);
      }

      // use alias
      normalizedJpqlQuery = normalizedJpqlQuery.replaceAll(memberInfo + "\\" + JPQLStatement.DELIMITER.PERIOD,
          alias + JPQLStatement.DELIMITER.PERIOD);
      //check for values like "x.y.z"
      query = checkConditionValues(normalizedJpqlQuery);
      query = removeExtraClause(normalizedJpqlQuery);
      // check if further normalization is needed
      normalizationNeededMatcher = NORMALIZATION_NEEDED_PATTERN.matcher(query);
      normalizationNeeded = normalizationNeededMatcher.find();
    }
    
    // add distinct to avoid duplicates in result set
    return normalizedJpqlQuery.replaceFirst(
        JPQLStatement.KEYWORD.SELECT + JPQLStatement.DELIMITER.SPACE,
        JPQLStatement.KEYWORD.SELECT_DISTINCT + JPQLStatement.DELIMITER.SPACE);
  }

  /**
   * Check if the statement contains ORDERBY having x.y.z kind of format
   * It will remove those values before checking for normalization 
   * and later added back
   * */
  private static String removeExtraClause(String jpqlQuery) {
    String query = jpqlQuery;
    if(query.contains(JPQLStatement.KEYWORD.ORDERBY )){
      int index = query.indexOf(JPQLStatement.KEYWORD.ORDERBY);
      query = query.substring(0, index);  
    }
    return query;
  }
  
  /**
   * Check if the statement contains string values having x.y.z kind of format
   * It will replace those values with parameters before checking for normalization 
   * and later added back
   * */
  private static String checkConditionValues(String jpqlQuery) {
    int i=0;
    StringBuffer query= new StringBuffer();
    query.append(jpqlQuery);
    Matcher valueMatcher = VALUE_NORM_PATTERN.matcher(query);
    while (valueMatcher.find()) {
      String val = valueMatcher.group();
      int index = query.indexOf(val);
      String var = "["+ ++i +"] ";
      query.replace(index, index + val.length(), var);
      valueMatcher = VALUE_NORM_PATTERN.matcher(query);
    }
    return query.toString();
  }

  /**
   * Verify via {@link EntityManager} if one of the attributes of the selected entity
   * contains a embedded attribute.
   * Return true if at least one embedded attribute is found or false if non embedded
   * attribute is found.
   *
   * @param em according entity manager
   * @param jpqlQuery query to verify
   * @return true if at least one embedded attribute is found or false if non embedded
   * attribute is found.
   */
  private static boolean containsEmbeddedAttributes(EntityManager em, String jpqlQuery) {
    Set<EntityType<?>> types = em.getMetamodel().getEntities();
    int pos = jpqlQuery.indexOf("FROM ") + 5;
    int lastpos = jpqlQuery.indexOf(" ", pos);
    final String queriedEntity = jpqlQuery.substring(pos, lastpos);
    for (EntityType<?> type : types) {
      if(queriedEntity.equals(type.getName())) {
        Set<Attribute<?, ?>> attributes = (Set<Attribute<?, ?>>) type.getAttributes();
        for (Attribute<?, ?> attribute : attributes) {
          if(jpqlQuery.contains(attribute.getName()) &&
            attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static int ordinalIndexOf(String str, char s, int n) {
    int pos = str.indexOf(s, 0);
    while (n-- > 0 && pos != -1) {
      pos = str.indexOf(s, pos + 1);
    }
    return pos;
  }

  final class JPAQueryInfo {
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
