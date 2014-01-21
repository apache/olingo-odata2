/*
 * Copyright 2013 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.olingo.odata2.annotation.processor.core;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.olingo.odata2.annotation.processor.core.model.Building;
import org.apache.olingo.odata2.annotation.processor.core.model.Employee;
import org.apache.olingo.odata2.annotation.processor.core.model.Manager;
import org.apache.olingo.odata2.annotation.processor.core.model.Photo;
import org.apache.olingo.odata2.annotation.processor.core.model.RefBase;
import org.apache.olingo.odata2.annotation.processor.core.model.Room;
import org.apache.olingo.odata2.annotation.processor.core.model.Team;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.junit.Test;

/**
 *
 */
public class AnnotationServiceFactoryImplTest {

  @Test
  public void createFromPackage() throws ODataException {
    AnnotationServiceFactoryImpl factory = new AnnotationServiceFactoryImpl();
    ODataService service = factory.createAnnotationService(Building.class.getPackage().getName());

    Assert.assertNotNull(service);
  }

  @Test
  public void createFromAnnotatedClasses() throws ODataException {
    AnnotationServiceFactoryImpl factory = new AnnotationServiceFactoryImpl();
    final Collection<Class<?>> annotatedClasses = new ArrayList<Class<?>>();
    annotatedClasses.add(RefBase.class);
    annotatedClasses.add(Building.class);
    annotatedClasses.add(Employee.class);
    annotatedClasses.add(Manager.class);
    annotatedClasses.add(Photo.class);
    annotatedClasses.add(Room.class);
    annotatedClasses.add(Team.class);
    ODataService service = factory.createAnnotationService(annotatedClasses);

    Assert.assertNotNull(service);
  }

  @Test(expected = ODataException.class)
  public void createFromClasses() throws ODataException {
    AnnotationServiceFactoryImpl factory = new AnnotationServiceFactoryImpl();

    final Collection<Class<?>> notAnnotatedClasses = new ArrayList<Class<?>>();
    notAnnotatedClasses.add(String.class);
    notAnnotatedClasses.add(Long.class);
    ODataService service = factory.createAnnotationService(notAnnotatedClasses);

    Assert.assertNotNull(service);
  }
}
