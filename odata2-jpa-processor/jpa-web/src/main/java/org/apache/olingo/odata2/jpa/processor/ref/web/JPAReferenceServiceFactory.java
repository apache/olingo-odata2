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
package org.apache.olingo.odata2.jpa.processor.ref.web;

import java.util.ResourceBundle;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.OnJPAWriteContent;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.ref.extension.OnDBWriteContent;
import org.apache.olingo.odata2.jpa.processor.ref.extension.SalesOrderProcessingExtension;
import org.apache.olingo.odata2.jpa.processor.ref.factory.JPAEntityManagerFactory;

public class JPAReferenceServiceFactory extends ODataJPAServiceFactory {
  private static final String PUNIT_NAME = "salesorderprocessing";
  private static final String MAPPING_MODEL = "SalesOrderProcessingMappingModel.xml";
  private static final String CONFIG = "serviceConfig";
  private static final String SHOW_DETAIL_ERROR = "showDetailError";
  private static final int PAGE_SIZE = 5;
  public static final OnJPAWriteContent onDBWriteContent = new OnDBWriteContent();

  @Override
  public ODataJPAContext initializeODataJPAContext()
      throws ODataJPARuntimeException {
    ODataJPAContext oDataJPAContext = getODataJPAContext();
    oDataJPAContext.setEntityManagerFactory(JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME));
    oDataJPAContext.setPersistenceUnitName(PUNIT_NAME);
    oDataJPAContext.setJPAEdmMappingModel(MAPPING_MODEL);
    oDataJPAContext
        .setJPAEdmExtension((JPAEdmExtension) new SalesOrderProcessingExtension());
    oDataJPAContext.setPageSize(PAGE_SIZE);
    oDataJPAContext.setDefaultNaming(false);
    setErrorLevel();
    setOnWriteJPAContent(onDBWriteContent);

    return oDataJPAContext;
  }

  private void setErrorLevel() {
    ResourceBundle config = ResourceBundle.getBundle(CONFIG);
    boolean error = Boolean.parseBoolean((String) config.getObject(SHOW_DETAIL_ERROR));
    setDetailErrors(error);
  }
}
