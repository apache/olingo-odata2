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
package org.apache.olingo.odata2.jpa.processor.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAPaging;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;

/**
 * This class does the compilation of context objects required for OData JPA
 * Runtime. The context object should be properly initialized with values else
 * the behavior of processor and EDM provider can result in exception.
 * 
 * Following are the mandatory parameter to be set into the context object
 * <ol>
 * <li>Persistence Unit Name</li>
 * <li>An instance of Java Persistence Entity Manager Factory</li>
 * </ol>
 * 
 * <br>
 * @org.apache.olingo.odata2.DoNotImplement
 * @see org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAFactory
 * @see org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAAccessFactory
 * 
 */
public interface ODataJPAContext {

  /**
   * The method gets the Java Persistence Unit Name set into the context.
   * 
   * @return Java Persistence Unit Name
   */
  public String getPersistenceUnitName();

  /**
   * The method sets the Java Persistence Unit Name into the context.
   * 
   * @param pUnitName
   * is the Java Persistence Unit Name.
   * 
   */
  public void setPersistenceUnitName(String pUnitName);

  /**
   * The method gets the OData Processor for JPA from the context.
   * 
   * @return OData JPA Processor
   */
  public ODataProcessor getODataProcessor();

  /**
   * The method sets the OData Processor for JPA into the context.
   * 
   * @param processor
   * is the specific implementation of {@link org.apache.olingo.odata2.jpa.processor.api.ODataJPAProcessor} for
   * processing OData service requests.
   */
  public void setODataProcessor(ODataProcessor processor);

  /**
   * The method gets the EDM provider for JPA from the context.
   * 
   * @return EDM provider
   */
  public EdmProvider getEdmProvider();

  /**
   * The method sets EDM provider into the context
   * 
   * @param edmProvider
   * is the specific implementation of {@link org.apache.olingo.odata2.api.edm.provider.EdmProvider} for
   * transforming Java persistence models to Entity Data Model
   * 
   */
  public void setEdmProvider(EdmProvider edmProvider);

  /**
   * The method gets the Java Persistence Entity Manager factory from the
   * context. <br>
   * <b>CAUTION:-</b> Don't use the Entity Manager Factory to instantiate
   * Entity Managers. Instead get reference to Entity Manager using
   * {@link org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext#getEntityManager()}
   * 
   * @return an instance of Java Persistence Entity Manager Factory
   */
  public EntityManagerFactory getEntityManagerFactory();

  /**
   * The method sets the Java Persistence Entity Manager factory into the
   * context.
   * 
   * @param emf
   * is of type {@linkjakarta.persistence.EntityManagerFactory}
   * 
   */
  public void setEntityManagerFactory(EntityManagerFactory emf);

  /**
   * The method gets OData Context into the context.
   * 
   * @return OData Context
   */
  public ODataContext getODataContext();

  /**
   * The method sets OData context into the context.
   * 
   * @param ctx
   * is an OData context of type {@link org.apache.olingo.odata2.api.processor.ODataContext}
   */
  public void setODataContext(ODataContext ctx);

  /**
   * The method sets the JPA EDM mapping model name into the context. JPA EDM
   * mapping model is an XML document based on JPAEDMMappingModel.xsd
   * 
   * @param name
   * is the name of JPA EDM mapping model
   */
  public void setJPAEdmMappingModel(String name);

  /**
   * The method gets the JPA EDM mapping model name from the context.
   * 
   * @return name of JPA EDM mapping model
   */
  public String getJPAEdmMappingModel();

  /**
   * The method sets the Entity Manager into the Context
   * @param em
   */
  public void setEntityManager(EntityManager em);

  /**
   * The method returns an instance of type entity manager. The entity manager
   * thus returns a single persistence context for the current OData request.
   * Hence all entities that are accessed within JPA processor are managed by
   * single entity manager.
   * 
   * @return an instance of type {@linkjakarta.persistence.EntityManager}
   */
  public EntityManager getEntityManager();

  /**
   * The method sets the JPA Edm Extension instance into the context. There
   * can be at most only one extension for a context. Invoking the method
   * several times overwrites already set extension instance in the context.
   * 
   * @param jpaEdmExtension
   * is an instance of type {@link org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension}
   * 
   */
  public void setJPAEdmExtension(JPAEdmExtension jpaEdmExtension);

  /**
   * The method returns the JPA Edm Extension instance set into the context.
   * 
   * @return an instance of type
   * {@link org.apache.olingo.odata2.jpa.processor.api.model.mapping.JPAEmbeddableTypeMapType}
   */
  public JPAEdmExtension getJPAEdmExtension();

  /**
   * The method sets into the context whether the library should consider default naming for
   * <ul><li>EdmProperty</li>
   * <li>EdmComplexProperty</li>
   * <li>EdmNavigationProperty</li></ul>
   * 
   * @param defaultNaming is a boolean value that indicates if set to
   * <ul><li>true - default naming is considered in case no mapping is provided.</li>
   * <li>false - default naming is not considered in case no mapping is provided. The
   * name provided in JPA Entity Model is considered.</li>
   * </ul>
   */
  public void setDefaultNaming(boolean defaultNaming);

  /**
   * The method returns whether the library should consider default naming for
   * <ul><li>EdmProperty</li>
   * <li>EdmComplexProperty</li>
   * <li>EdmNavigationProperty</li></ul>
   * 
   * @return
   * <ul><li>true - default naming is considered in case no mapping is provided.</li>
   * <li>false - default naming is not considered in case no mapping is provided. The
   * name provided in JPA Entity Model is considered.</li>
   * </ul>
   */
  public boolean getDefaultNaming();

  /**
   * The method gets the server side page size to the context
   * @return the page size
   */
  public int getPageSize();

  /**
   * The method sets the server side page size to the context
   * @param size
   */
  public void setPageSize(int size);

  /**
   * The method sets the server side paging object
   * @param paging an instance of type {@link org.apache.olingo.odata2.jpa.processor.api.access.JPAPaging}
   */
  public void setPaging(JPAPaging paging);

  /**
   * The method returns the server side paging object
   * @return an instance of type {@link org.apache.olingo.odata2.jpa.processor.api.access.JPAPaging}
   */
  public JPAPaging getPaging();

  /**
   * The method returns the ODataJPATransaction.
   * @return ODataJPATransaction
   */
  public ODataJPATransaction getODataJPATransaction();

  /**
   * Set the state whether the underlying entity manager is container managed (or not).
   * (Default is <code>false</code>)
   *
   * @param containerManaged <code>true</code> for container managed entity manager
   */
  void setContainerManaged(boolean containerManaged);

  /**
   * The method returns <code>true</code> if the underlying entity manager is container managed.
   * (Default is <code>false</code>)
   *
   * @return <code>true</code> if the underlying entity manger is container manged.
   */
  boolean isContainerManaged();
}
