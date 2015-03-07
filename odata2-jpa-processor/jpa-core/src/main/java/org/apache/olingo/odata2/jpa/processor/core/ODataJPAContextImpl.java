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
package org.apache.olingo.odata2.jpa.processor.core;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATransaction;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAPaging;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;

public class ODataJPAContextImpl implements ODataJPAContext {

  private String pUnitName;
  private EntityManagerFactory emf;
  private EntityManager em;
  private ODataContext odataContext;
  private ODataProcessor processor;
  private EdmProvider edmProvider;
  private String jpaEdmMappingModelName;
  private JPAEdmExtension jpaEdmExtension;
  private int pageSize = 0;
  private JPAPaging jpaPaging;
  private static final ThreadLocal<ODataContext> oDataContextThreadLocal = new ThreadLocal<ODataContext>();
  private boolean defaultNaming = true;
  private ODataJPATransaction transaction = null;

  @Override
  public String getPersistenceUnitName() {
    return pUnitName;
  }

  @Override
  public void setPersistenceUnitName(final String pUnitName) {
    this.pUnitName = pUnitName;
  }

  @Override
  public EntityManagerFactory getEntityManagerFactory() {
    return emf;
  }

  @Override
  public void setEntityManagerFactory(final EntityManagerFactory emf) {
    this.emf = emf;
  }

  @Override
  public void setODataContext(final ODataContext ctx) {
    odataContext = ctx;
    setContextInThreadLocal(odataContext);
  }

  @Override
  public ODataContext getODataContext() {
    return odataContext;
  }

  @Override
  public void setODataProcessor(final ODataProcessor processor) {
    this.processor = processor;
  }

  @Override
  public ODataProcessor getODataProcessor() {
    return processor;
  }

  @Override
  public void setEdmProvider(final EdmProvider edmProvider) {
    this.edmProvider = edmProvider;
  }

  @Override
  public EdmProvider getEdmProvider() {
    return edmProvider;
  }

  @Override
  public void setJPAEdmMappingModel(final String name) {
    jpaEdmMappingModelName = name;

  }

  @Override
  public String getJPAEdmMappingModel() {
    return jpaEdmMappingModelName;
  }

  public static void setContextInThreadLocal(final ODataContext ctx) {
    oDataContextThreadLocal.set(ctx);
  }

  public static void unsetContextInThreadLocal() {
    oDataContextThreadLocal.remove();
  }

  public static ODataContext getContextInThreadLocal() {
    return (ODataContext) oDataContextThreadLocal.get();
  }

  @Override
  public EntityManager getEntityManager() {
    if (em == null) {
      em = emf.createEntityManager();
    }

    return em;
  }

  @Override
  public void setJPAEdmExtension(final JPAEdmExtension jpaEdmExtension) {
    this.jpaEdmExtension = jpaEdmExtension;

  }

  @Override
  public JPAEdmExtension getJPAEdmExtension() {
    return jpaEdmExtension;
  }

  @Override
  public void setDefaultNaming(final boolean defaultNaming) {
    this.defaultNaming = defaultNaming;
  }

  @Override
  public boolean getDefaultNaming() {
    return defaultNaming;
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  @Override
  public void setPageSize(final int size) {
    pageSize = size;
  }

  @Override
  public void setPaging(final JPAPaging paging) {
    jpaPaging = paging;
  }

  @Override
  public JPAPaging getPaging() {
    return jpaPaging;
  }

  @Override
  public ODataJPATransaction getODataJPATransaction() {
    if (transaction == null) {
      transaction = odataContext.getServiceFactory().getCallback(ODataJPATransaction.class);
      // Fallback to RESOURCE_LOCAL based transaction
      if (transaction == null) {
        transaction = new ODataJPATransactionLocalDefault(getEntityManager());
      }
    }
    return transaction;
  }
}
