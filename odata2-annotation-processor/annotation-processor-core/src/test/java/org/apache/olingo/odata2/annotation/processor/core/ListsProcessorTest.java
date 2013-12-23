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

import org.apache.olingo.odata2.annotation.processor.core.datasource.AnnotationInMemoryDs;
import org.apache.olingo.odata2.annotation.processor.core.datasource.AnnotationValueAccess;
import org.apache.olingo.odata2.annotation.processor.core.datasource.DataSource;
import org.apache.olingo.odata2.annotation.processor.core.datasource.ValueAccess;
import org.apache.olingo.odata2.annotation.processor.core.model.Building;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ListsProcessorTest {

  @Test
  public void init() throws ODataException {
    DataSource dataSource = new AnnotationInMemoryDs(Building.class.getPackage().getName());
    ValueAccess valueAccess = new AnnotationValueAccess();
    ListsProcessor lp = new ListsProcessor(dataSource, valueAccess);
    
    Assert.assertNotNull(lp);
  }
}
