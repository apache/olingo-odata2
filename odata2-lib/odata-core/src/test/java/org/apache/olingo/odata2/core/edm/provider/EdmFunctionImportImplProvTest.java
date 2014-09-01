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
package org.apache.olingo.odata2.core.edm.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmParameter;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.FunctionImportParameter;
import org.apache.olingo.odata2.api.edm.provider.ReturnType;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *  
 */
public class EdmFunctionImportImplProvTest extends BaseTest {

  private static EdmFunctionImportImplProv edmFunctionImport;
  private static EdmFunctionImportImplProv edmFunctionImportWithoutParameters;
  private static EdmEntityContainerImplProv edmEntityContainer;

  @BeforeClass
  public static void getEdmEntityContainerImpl() throws Exception {
    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    EntityContainerInfo containerInfo = new EntityContainerInfo().setName("Container");
    when(edmProvider.getEntityContainerInfo("Container")).thenReturn(containerInfo);
    edmEntityContainer = new EdmEntityContainerImplProv(edmImplProv, containerInfo);

    EntitySet fooEntitySet = new EntitySet().setName("fooEntitySet");
    when(edmProvider.getEntitySet("Container", "fooEntitySet")).thenReturn(fooEntitySet);

    ReturnType fooReturnType =
        new ReturnType().setTypeName(EdmSimpleTypeKind.String.getFullQualifiedName()).setMultiplicity(
            EdmMultiplicity.ONE);

    List<FunctionImportParameter> parameters = new ArrayList<FunctionImportParameter>();
    FunctionImportParameter parameter =
        new FunctionImportParameter().setName("fooParameter1").setType(EdmSimpleTypeKind.String);
    parameters.add(parameter);

    parameter = new FunctionImportParameter().setName("fooParameter2").setType(EdmSimpleTypeKind.String);
    parameters.add(parameter);

    parameter = new FunctionImportParameter().setName("fooParameter3").setType(EdmSimpleTypeKind.String);
    parameters.add(parameter);

    FunctionImport functionImportFoo =
        new FunctionImport().setName("foo").setHttpMethod("GET").setReturnType(fooReturnType).setEntitySet(
            "fooEntitySet").setParameters(parameters);
    when(edmProvider.getFunctionImport("Container", "foo")).thenReturn(functionImportFoo);
    edmFunctionImport = new EdmFunctionImportImplProv(edmImplProv, functionImportFoo, edmEntityContainer);

    FunctionImport functionImportBar = new FunctionImport().setName("bar").setHttpMethod("GET");
    when(edmProvider.getFunctionImport("Container", "bar")).thenReturn(functionImportBar);
    edmFunctionImportWithoutParameters =
        new EdmFunctionImportImplProv(edmImplProv, functionImportBar, edmEntityContainer);
  }

  @Test
  public void functionImport() throws Exception {
    assertEquals("foo", edmFunctionImport.getName());
    assertEquals("GET", edmFunctionImport.getHttpMethod());
  }

  @Test
  public void containerName() throws Exception {
    assertEquals(edmEntityContainer, edmFunctionImport.getEntityContainer());
  }

  @Test
  public void returnType() throws Exception {
    EdmTyped returnType = edmFunctionImport.getReturnType();
    assertNotNull(returnType);
    assertEquals(EdmSimpleTypeKind.String.getFullQualifiedName().getName(), returnType.getType().getName());
    assertEquals(EdmMultiplicity.ONE, returnType.getMultiplicity());
  }

  @Test
  public void entitySet() throws Exception {
    EdmEntitySet entitySet = edmFunctionImport.getEntitySet();
    assertNotNull(entitySet);
    assertEquals("fooEntitySet", entitySet.getName());
    assertEquals(edmEntityContainer.getEntitySet("fooEntitySet"), entitySet);
  }

  @Test
  public void parameterExisting() throws Exception {
    List<String> parameterNames = edmFunctionImport.getParameterNames();
    assertTrue(parameterNames.contains("fooParameter1"));
    assertTrue(parameterNames.contains("fooParameter2"));
    assertTrue(parameterNames.contains("fooParameter3"));

    EdmParameter parameter = edmFunctionImport.getParameter("fooParameter1");
    assertNotNull(parameter);
    assertEquals("fooParameter1", parameter.getName());

    parameter = edmFunctionImport.getParameter("fooParameter1");
    assertNotNull(parameter);
    assertEquals("fooParameter1", parameter.getName());

    parameter = edmFunctionImport.getParameter("fooParameter2");
    assertNotNull(parameter);
    assertEquals("fooParameter2", parameter.getName());

    parameter = edmFunctionImport.getParameter("fooParameter3");
    assertNotNull(parameter);
    assertEquals("fooParameter3", parameter.getName());
  }


  @Test
  public void parametersAreSorted() throws Exception {
    List<String> parameterNames = edmFunctionImport.getParameterNames();
    assertEquals("fooParameter1", parameterNames.get(0));
    assertEquals("fooParameter2", parameterNames.get(1));
    assertEquals("fooParameter3", parameterNames.get(2));

    EdmParameter parameter = edmFunctionImport.getParameter("fooParameter1");
    assertNotNull(parameter);
    assertEquals("fooParameter1", parameter.getName());

    parameter = edmFunctionImport.getParameter("fooParameter1");
    assertNotNull(parameter);
    assertEquals("fooParameter1", parameter.getName());

    parameter = edmFunctionImport.getParameter("fooParameter2");
    assertNotNull(parameter);
    assertEquals("fooParameter2", parameter.getName());

    parameter = edmFunctionImport.getParameter("fooParameter3");
    assertNotNull(parameter);
    assertEquals("fooParameter3", parameter.getName());
  }

  @Test
  public void parameterNotExisting() throws Exception {
    assertNotNull(edmFunctionImportWithoutParameters.getParameterNames());

    EdmParameter parameter = edmFunctionImportWithoutParameters.getParameter("fooParameter1");
    assertNull(parameter);

    parameter = edmFunctionImportWithoutParameters.getParameter("fooParameter2");
    assertNull(parameter);

    parameter = edmFunctionImportWithoutParameters.getParameter("fooParameter3");
    assertNull(parameter);
  }

  @Test
  public void nulllReturnType() throws Exception {
    assertNull(edmFunctionImportWithoutParameters.getReturnType());
  }

  @Test
  public void parameterAnnotations() throws Exception {
    EdmParameter parameter = edmFunctionImport.getParameter("fooParameter1");
    assertNotNull(parameter);
    EdmAnnotatable annotatable = (EdmAnnotatable) parameter;
    EdmAnnotations annotations = annotatable.getAnnotations();
    assertNull(annotations.getAnnotationAttributes());
    assertNull(annotations.getAnnotationElements());
  }

  @Test
  public void getAnnotations() throws Exception {
    EdmAnnotatable annotatable = edmFunctionImport;
    EdmAnnotations annotations = annotatable.getAnnotations();
    assertNull(annotations.getAnnotationAttributes());
    assertNull(annotations.getAnnotationElements());
  }
}
