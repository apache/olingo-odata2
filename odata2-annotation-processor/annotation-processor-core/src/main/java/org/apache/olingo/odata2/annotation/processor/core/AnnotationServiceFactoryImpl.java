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
package org.apache.olingo.odata2.annotation.processor.core;

import java.util.Collection;

import org.apache.olingo.odata2.annotation.processor.api.AnnotationServiceFactory.AnnotationServiceFactoryInstance;
import org.apache.olingo.odata2.annotation.processor.core.datasource.AnnotationInMemoryDs;
import org.apache.olingo.odata2.annotation.processor.core.datasource.AnnotationValueAccess;
import org.apache.olingo.odata2.annotation.processor.core.edm.AnnotationEdmProvider;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;

/**
 * ODataServiceFactory implementation based on ListProcessor
 * in combination with Annotation-Support-Classes for EdmProvider, DataSource and ValueAccess.
 */
public class AnnotationServiceFactoryImpl implements AnnotationServiceFactoryInstance {

  /**
   * Create an instance which further can create an {@link ODataService}.
   * 
   * @return instance which further can create an {@link ODataService}.
   */
  public AnnotationServiceFactoryInstance createInstance() {
    return new AnnotationServiceFactoryImpl();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ODataService createAnnotationService(String modelPackage) throws ODataException {
    AnnotationEdmProvider edmProvider = new AnnotationEdmProvider(modelPackage);
    AnnotationInMemoryDs dataSource = new AnnotationInMemoryDs(modelPackage);
    AnnotationValueAccess valueAccess = new AnnotationValueAccess();

    // Edm via Annotations and ListProcessor via AnnotationDS with AnnotationsValueAccess
    return RuntimeDelegate.createODataSingleProcessorService(edmProvider,
        new ListsProcessor(dataSource, valueAccess));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ODataService createAnnotationService(Collection<Class<?>> annotatedClasses) throws ODataException {
    AnnotationEdmProvider edmProvider = new AnnotationEdmProvider(annotatedClasses);
    AnnotationInMemoryDs dataSource = new AnnotationInMemoryDs(annotatedClasses);
    AnnotationValueAccess valueAccess = new AnnotationValueAccess();

    // Edm via Annotations and ListProcessor via AnnotationDS with AnnotationsValueAccess
    return RuntimeDelegate.createODataSingleProcessorService(edmProvider,
        new ListsProcessor(dataSource, valueAccess));
  }
}
