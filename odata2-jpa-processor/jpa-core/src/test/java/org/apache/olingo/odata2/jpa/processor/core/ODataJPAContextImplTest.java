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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.core.edm.ODataJPAEdmProvider;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataJPAContextMock;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class ODataJPAContextImplTest {

  private ODataContext odataContext = null;
  private ODataJPAContext odataJPAContext = null;
  private EdmProvider edmProvider = null;
  private EntityManagerFactory emf = null;
  private EntityManager em = null;
  private ODataProcessor processor = null;

  @Before
  public void setup() {

    edmProvider = new ODataJPAEdmProvider();
    emf = EasyMock.createMock(EntityManagerFactory.class);
    em = EasyMock.createMock(EntityManager.class);
    EasyMock.expect(em.isOpen()).andReturn(false);
    EasyMock.replay(em);

    EasyMock.expect(emf.createEntityManager()).andStubReturn(em);
    EasyMock.replay(emf);

    odataContext = EasyMock.createMock(ODataContext.class);
    List<Locale> listLocale = new ArrayList<Locale>();
    listLocale.add(Locale.ENGLISH);
    listLocale.add(Locale.GERMAN);

    EasyMock.expect(odataContext.getAcceptableLanguages()).andStubReturn(listLocale);
    EasyMock.replay(odataContext);

    processor = EasyMock.createMock(ODataProcessor.class);
    EasyMock.replay(processor);

    odataJPAContext = new ODataJPAContextImpl();
    odataJPAContext.setEdmProvider(edmProvider);
    odataJPAContext.setEntityManagerFactory(emf);
    odataJPAContext.setODataContext(odataContext);
    odataJPAContext.setODataProcessor(processor);
    odataJPAContext.setPersistenceUnitName(ODataJPAContextMock.PERSISTENCE_UNIT_NAME);
    odataJPAContext.setJPAEdmMappingModel(ODataJPAContextMock.MAPPING_MODEL);
  }

  @Test
  public void testgetMethodsOfODataJPAContext() {

    assertEquals(odataJPAContext.getEdmProvider().hashCode(), edmProvider.hashCode());
    assertEquals(odataJPAContext.getEntityManagerFactory().hashCode(), emf.hashCode());
    assertEquals(odataJPAContext.getODataContext().hashCode(), odataContext.hashCode());
    assertEquals(odataJPAContext.getODataProcessor().hashCode(), processor.hashCode());
    assertEquals(odataJPAContext.getPersistenceUnitName(), ODataJPAContextMock.PERSISTENCE_UNIT_NAME);
    assertEquals(odataJPAContext.getJPAEdmMappingModel(), ODataJPAContextMock.MAPPING_MODEL);

    EntityManager em1 = odataJPAContext.getEntityManager();
    EntityManager em2 = odataJPAContext.getEntityManager();
    if (em1 != null && em2 != null) {
      assertEquals(em1.hashCode(), em2.hashCode());
    }

  }

}
