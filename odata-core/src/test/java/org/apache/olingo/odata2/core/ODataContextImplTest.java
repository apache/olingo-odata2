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
package org.apache.olingo.odata2.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataRequest;

/**
 *  
 */
public class ODataContextImplTest {

  ODataContextImpl context;

  @Before
  public void before() {
    ODataServiceFactory factory = mock(ODataServiceFactory.class);
    ODataRequest request = mock(ODataRequest.class);

    when(request.getMethod()).thenReturn(ODataHttpMethod.GET);
    when(request.getPathInfo()).thenReturn(new PathInfoImpl());

    context = new ODataContextImpl(request, factory);
  }

  @Test
  public void httpMethod() {
    context.setHttpMethod(ODataHttpMethod.GET.name());
    assertEquals(ODataHttpMethod.GET.name(), context.getHttpMethod());
  }

  @Test
  public void debugMode() {
    context.setDebugMode(true);
    assertTrue(context.isInDebugMode());
  }

  @Test
  public void parentContext() {

    assertFalse(context.isInBatchMode());
    assertNull(context.getBatchParentContext());

    ODataContext parentContext = mock(ODataContext.class);
    context.setBatchParentContext(parentContext);

    assertTrue(context.isInBatchMode());
    assertNotNull(context.getBatchParentContext());
  }
}
