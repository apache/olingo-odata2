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
package org.apache.olingo.odata2.jpa.processor.ref.listeners;

import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAQueryExtensionEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContext;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement;

public class CustomerQueryExtension extends ODataJPAQueryExtensionEntityListener {

  @Override
  public Query getQuery(GetEntitySetUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
    FilterExpression filter = uriInfo.getFilter();
    if(filter != null && filter.getExpressionString().startsWith("name")) {
      throw createApplicationError("Filter on name not allowed.", Locale.ENGLISH);
    }
    return null;
  }

  @Override
  public Query getQuery(GetEntityUriInfo uriInfo, EntityManager em) {
    Query query = null;
    JPQLContextType contextType = null;
    if (uriInfo.getNavigationSegments().size() > 0) {
      contextType = JPQLContextType.JOIN_SINGLE;
    } else {
      contextType = JPQLContextType.SELECT_SINGLE;
    }
    JPQLContext jpqlContext;
    try {
      jpqlContext = JPQLContext.createBuilder(contextType, uriInfo).build();
      query = em.createQuery(JPQLStatement.createBuilder(jpqlContext).build().toString());
    } catch (ODataJPAModelException e) {
      // Log and return null query object;
    } catch (ODataJPARuntimeException e) {
      // Log and return null query object;
    }
    return null;
  }

  @Override
  public boolean isTombstoneSupported() {
    return false;
  }

}
