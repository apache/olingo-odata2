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
package org.apache.olingo.odata2.jpa.processor.core.jpql;

import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContext;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLSelectContextView;
import org.apache.olingo.odata2.jpa.processor.core.ODataExpressionParser;
import org.apache.olingo.odata2.jpa.processor.core.ODataParameterizedWhereExpressionUtil;

public class JPQLSelectContext extends JPQLContext implements JPQLSelectContextView {

  protected String selectExpression;
  protected String orderByCollection;
  protected String whereCondition;

  protected boolean isCountOnly = false;// Support for $count

  public JPQLSelectContext(final boolean isCountOnly) {
    this.isCountOnly = isCountOnly;
  }

  protected final void setOrderByCollection(final String orderByCollection) {
    this.orderByCollection = orderByCollection;
  }

  protected final void setWhereExpression(final String filterExpression) {
    whereCondition = filterExpression;
  }

  protected final void setSelectExpression(final String selectExpression) {
    this.selectExpression = selectExpression;
  }

  @Override
  public String getSelectExpression() {
    return selectExpression;
  }

  @Override
  public String getOrderByCollection() {
    return orderByCollection;
  }

  @Override
  public String getWhereExpression() {
    return whereCondition;
  }

  public class JPQLSelectContextBuilder extends
  org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContext.JPQLContextBuilder {

    protected GetEntitySetUriInfo entitySetView;

    @Override
    public JPQLContext build() throws ODataJPAModelException, ODataJPARuntimeException {
      if (entitySetView != null) {

        try {

          if (isCountOnly) {
            setType(JPQLContextType.SELECT_COUNT);
          } else {
            setType(JPQLContextType.SELECT);
          }

          if (withPaging) {
            isPagingRequested(withPaging);
          }

          EdmEntityType entityType = entitySetView.getTargetEntitySet().getEntityType();
          EdmMapping mapping = entityType.getMapping();
          if (mapping != null) {
            setJPAEntityName(mapping.getInternalName());
          } else {
            setJPAEntityName(entityType.getName());
          }

          setJPAEntityAlias(generateJPAEntityAlias());

          setOrderByCollection(generateOrderByFileds());

          setSelectExpression(generateSelectExpression());

          setWhereExpression(generateWhereExpression());
        } catch (ODataException e) {
          throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
        }
      }

      return JPQLSelectContext.this;

    }

    @Override
    protected void setResultsView(final Object resultsView) {
      if (resultsView instanceof GetEntitySetUriInfo) {
        entitySetView = (GetEntitySetUriInfo) resultsView;
      }

    }

    /*
     * Generate Select Clause
     */
    protected String generateSelectExpression() throws EdmException {
      return getJPAEntityAlias();
    }

    /*
     * Generate Order By Clause Fields
     */
    protected String generateOrderByFileds() throws ODataJPARuntimeException, EdmException {

      if (entitySetView.getOrderBy() != null) {

        return ODataExpressionParser.parseToJPAOrderByExpression(entitySetView.getOrderBy(), getJPAEntityAlias());

      } else if (entitySetView.getTop() != null || entitySetView.getSkip() != null ||
          pagingRequested == true) {

        return ODataExpressionParser.parseKeyPropertiesToJPAOrderByExpression(entitySetView.getTargetEntitySet()
            .getEntityType().getKeyProperties(), getJPAEntityAlias());
      } else {
        return null;
      }

    }

    /*
     * Generate Where Clause Expression
     */
    protected String generateWhereExpression() throws ODataException {
      if (entitySetView.getFilter() != null) {
        String whereExpression = ODataExpressionParser.parseToJPAWhereExpression(
            entitySetView.getFilter(), getJPAEntityAlias());
        Map<String, Map<Integer, Object>> parameterizedExpressionMap = 
            new HashMap<String, Map<Integer,Object>>();
        parameterizedExpressionMap.put(whereExpression, ODataExpressionParser.getPositionalParameters());
        ODataParameterizedWhereExpressionUtil.setParameterizedQueryMap(parameterizedExpressionMap);
        ODataExpressionParser.reInitializePositionalParameters();
        return whereExpression;
      }
      ODataExpressionParser.reInitializePositionalParameters();
      return null;
    }
  }

}