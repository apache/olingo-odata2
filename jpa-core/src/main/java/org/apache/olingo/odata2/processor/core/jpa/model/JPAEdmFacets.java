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
package org.apache.olingo.odata2.processor.core.jpa.model;

import java.lang.reflect.AnnotatedElement;

import javax.persistence.Column;
import javax.persistence.metamodel.Attribute;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;

public class JPAEdmFacets {
  public static void setFacets(final Attribute<?, ?> jpaAttribute, final SimpleProperty edmProperty) {
    EdmSimpleTypeKind edmTypeKind = edmProperty.getType();
    Facets facets = new Facets();
    edmProperty.setFacets(facets);

    Column column = null;
    if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
      column = ((AnnotatedElement) jpaAttribute
          .getJavaMember()).getAnnotation(Column.class);
    } else {
      return;
    }

    setNullable(column, edmProperty);

    switch (edmTypeKind) {
    case Binary:
      setMaxLength(column, edmProperty);
      break;
    case DateTime:
      setPrecision(column, edmProperty);
      break;
    case DateTimeOffset:
      setPrecision(column, edmProperty);
      break;
    case Time:
      setPrecision(column, edmProperty);
      break;
    case Decimal:
      setPrecision(column, edmProperty);
      setScale(column, edmProperty);
      break;
    case String:
      setMaxLength(column, edmProperty);
      break;
    default:
      break;
    }
  }

  private static void setNullable(final Column column, final SimpleProperty edmProperty) {
    ((Facets) edmProperty.getFacets()).setNullable(column.nullable());
  }

  private static void setMaxLength(final Column column, final SimpleProperty edmProperty) {
    if (column.length() > 0) {
      ((Facets) edmProperty.getFacets()).setMaxLength(column.length());
    }
  }

  private static void setPrecision(final Column column, final SimpleProperty edmProperty) {
    if (column.precision() > 0) {
      ((Facets) edmProperty.getFacets()).setPrecision(column.precision());
    }
  }

  private static void setScale(final Column column, final SimpleProperty edmProperty) {
    if (column.scale() > 0) {
      ((Facets) edmProperty.getFacets()).setScale(column.scale());
    }
  }
}
