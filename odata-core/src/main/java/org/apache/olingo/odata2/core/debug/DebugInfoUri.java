/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.debug;

import java.io.IOException;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.expression.ExceptionVisitExpression;
import org.apache.olingo.odata2.api.uri.expression.ExpressionParserException;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;
import org.apache.olingo.odata2.core.uri.ExpandSelectTreeCreator;
import org.apache.olingo.odata2.core.uri.ExpandSelectTreeNodeImpl;
import org.apache.olingo.odata2.core.uri.expression.JsonVisitor;

/**
 *  
 */
public class DebugInfoUri implements DebugInfo {

  private final UriInfo uriInfo;
  private final ExpressionParserException exception;

  public DebugInfoUri(final UriInfo uriInfo, final Exception exception) {
    this.uriInfo = uriInfo;

    Throwable candidate = exception;
    while (candidate != null && !(candidate instanceof ExpressionParserException)) {
      candidate = candidate.getCause();
    }
    this.exception = (ExpressionParserException) candidate;
  }

  @Override
  public String getName() {
    return "URI";
  }

  @Override
  public void appendJson(final JsonStreamWriter jsonStreamWriter) throws IOException {
    jsonStreamWriter.beginObject();

    if (exception != null) {
      jsonStreamWriter.name("error")
          .beginObject();
      if (exception.getFilterTree() != null) {
        jsonStreamWriter.namedStringValue("filter", exception.getFilterTree().getUriLiteral());
      }
      jsonStreamWriter.endObject();
    }

    if (uriInfo != null) {
      if (exception != null
          && (uriInfo.getFilter() != null || uriInfo.getOrderBy() != null
              || !uriInfo.getExpand().isEmpty() || !uriInfo.getSelect().isEmpty())) {
        jsonStreamWriter.separator();
      }

      final FilterExpression filter = uriInfo.getFilter();
      if (filter != null) {
        String filterString;
        try {
          filterString = (String) filter.accept(new JsonVisitor());
        } catch (final ExceptionVisitExpression e) {
          filterString = null;
        } catch (final ODataApplicationException e) {
          filterString = null;
        }
        jsonStreamWriter.name("filter").unquotedValue(filterString);
        if (uriInfo.getOrderBy() != null
            || !uriInfo.getExpand().isEmpty() || !uriInfo.getSelect().isEmpty()) {
          jsonStreamWriter.separator();
        }
      }

      final OrderByExpression orderBy = uriInfo.getOrderBy();
      if (orderBy != null) {
        String orderByString;
        try {
          orderByString = (String) orderBy.accept(new JsonVisitor());
        } catch (final ExceptionVisitExpression e) {
          orderByString = null;
        } catch (final ODataApplicationException e) {
          orderByString = null;
        }
        jsonStreamWriter.name("orderby").unquotedValue(orderByString);
        if (!uriInfo.getExpand().isEmpty() || !uriInfo.getSelect().isEmpty()) {
          jsonStreamWriter.separator();
        }
      }

      if (!uriInfo.getExpand().isEmpty() || !uriInfo.getSelect().isEmpty()) {
        String expandSelectString;
        try {
          ExpandSelectTreeCreator expandSelectCreator = new ExpandSelectTreeCreator(uriInfo.getSelect(), uriInfo.getExpand());
          final ExpandSelectTreeNodeImpl expandSelectTree = expandSelectCreator.create();
          expandSelectString = expandSelectTree.toJsonString();
        } catch (final EdmException e) {
          expandSelectString = null;
        }
        jsonStreamWriter.name("expand/select").unquotedValue(expandSelectString);
      }
    }

    jsonStreamWriter.endObject();
  }
}
