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
package org.apache.olingo.odata2.core.servicedocument;

import java.util.List;

import org.apache.olingo.odata2.api.servicedocument.Categories;
import org.apache.olingo.odata2.api.servicedocument.Category;
import org.apache.olingo.odata2.api.servicedocument.Fixed;

/**
 *  
 */
public class CategoriesImpl implements Categories {
  private String href;
  private Fixed fixed;
  private String scheme;
  private List<Category> categoryList;

  @Override
  public String getHref() {
    return href;
  }

  @Override
  public Fixed getFixed() {
    return fixed;
  }

  @Override
  public String getScheme() {
    return scheme;
  }

  @Override
  public List<Category> getCategoryList() {
    return categoryList;
  }

  public CategoriesImpl setHref(final String href) {
    this.href = href;
    return this;
  }

  public CategoriesImpl setFixed(final Fixed fixed) {
    this.fixed = fixed;
    return this;
  }

  public CategoriesImpl setScheme(final String scheme) {
    this.scheme = scheme;
    return this;
  }

  public CategoriesImpl setCategoryList(final List<Category> categoryList) {
    this.categoryList = categoryList;
    return this;
  }

}
