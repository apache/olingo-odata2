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
package org.apache.olingo.odata2.core.uri.expression;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmLiteral;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.uri.expression.CommonExpression;
import org.apache.olingo.odata2.api.uri.expression.ExpressionKind;
import org.apache.olingo.odata2.api.uri.expression.ExpressionVisitor;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;

public class PropertyExpressionImpl implements PropertyExpression {
  private String uriLiteral;
  private EdmType edmType;
  private EdmTyped edmProperty;
  private EdmLiteral edmLiteral;

  public PropertyExpressionImpl(final String uriLiteral, final EdmLiteral edmLiteral) {
    this.uriLiteral = uriLiteral;

    this.edmLiteral = edmLiteral;
    if (edmLiteral != null) {
      edmType = edmLiteral.getType();
    }
  }

  public CommonExpression setEdmProperty(final EdmTyped edmProperty) {
    // used EdmTyped because it may be a EdmProperty or a EdmNavigationProperty
    this.edmProperty = edmProperty;
    return this;
  }

  @Override
  public CommonExpression setEdmType(final EdmType edmType) {
    this.edmType = edmType;
    return this;
  }

  @Override
  public String getPropertyName() {
    if (edmProperty == null) {
      return "";
    }

    try {
      return edmProperty.getName();
    } catch (EdmException e) {
      return "";
    }
  }

  public EdmLiteral getEdmLiteral() {
    return edmLiteral;
  }

  @Override
  public EdmTyped getEdmProperty() {
    return edmProperty;
  }

  @Override
  public ExpressionKind getKind() {
    return ExpressionKind.PROPERTY;
  }

  @Override
  public String getUriLiteral() {
    return uriLiteral;
  }

  @Override
  public EdmType getEdmType() {
    return edmType;
  }

  @Override
  public Object accept(final ExpressionVisitor visitor) {
    Object ret = visitor.visitProperty(this, uriLiteral, edmProperty);
    return ret;
  }

}
