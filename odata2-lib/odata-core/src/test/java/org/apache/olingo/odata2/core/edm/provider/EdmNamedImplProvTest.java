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
import static org.mockito.Mockito.mock;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class EdmNamedImplProvTest extends BaseTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();
  
  @Test(expected = EdmException.class)
  public void testPropertySimple() throws Exception {

    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    SimpleProperty propertySimple = new SimpleProperty().setName("Prop;ertyName").setType(EdmSimpleTypeKind.String);
    new EdmSimplePropertyImplProv(edmImplProv, propertySimple);
  }

  @Test(expected = EdmException.class)
  public void testPropertyIllegalStartWithNumber() throws Exception {

    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    SimpleProperty propertySimple = new SimpleProperty().setName("1_PropertyName").setType(EdmSimpleTypeKind.String);
    new EdmSimplePropertyImplProv(edmImplProv, propertySimple);
    expectedEx.expect(RuntimeException.class);
    expectedEx.expectMessage("'Prop;ertyName' name pattern not valid.");
  }

  @Test
  public void testPropertyWithNumber() throws Exception {

    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    SimpleProperty propertySimple = new SimpleProperty().setName("Prop_1_Name").setType(EdmSimpleTypeKind
        .String);
    new EdmSimplePropertyImplProv(edmImplProv, propertySimple);
    assertEquals("Prop_1_Name", new EdmSimplePropertyImplProv(edmImplProv, propertySimple).getName());
  }

  @Test
  public void testPropertyUmlaut() throws Exception {
    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    SimpleProperty propertySimple = new SimpleProperty().setName("ÄropertyName").setType(EdmSimpleTypeKind.String);
    assertEquals("ÄropertyName", new EdmSimplePropertyImplProv(edmImplProv, propertySimple).getName());
  }

  @Test
  public void testPropertyUnicode() throws Exception {
    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    SimpleProperty propertySimple = new SimpleProperty().setName("\u00C0roperty\u00C1ame\u00C0\u00D5\u00D6")
        .setType(EdmSimpleTypeKind.String);
    assertEquals("ÀropertyÁameÀÕÖ", new EdmSimplePropertyImplProv(edmImplProv, propertySimple).getName());
  }

  @Test
  public void testPropertyUnicodeTwo() throws Exception {
    EdmProvider edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    SimpleProperty propertySimple = new SimpleProperty().setName("Содержание")
        .setType(EdmSimpleTypeKind.String);
    assertEquals("Содержание", new EdmSimplePropertyImplProv(edmImplProv, propertySimple).getName());
  }
}
