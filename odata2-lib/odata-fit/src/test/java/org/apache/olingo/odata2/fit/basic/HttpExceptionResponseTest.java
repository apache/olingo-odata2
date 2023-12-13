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
package org.apache.olingo.odata2.fit.basic;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathValuesEqual;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataHttpException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.core.exception.MessageService;
import org.apache.olingo.odata2.core.uri.UriInfoImpl;
import org.apache.olingo.odata2.ref.edm.ScenarioEdmProvider;
import org.apache.olingo.odata2.testutil.helper.ClassHelper;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

/**
 *  
 */
public class HttpExceptionResponseTest extends AbstractBasicTest {

  public HttpExceptionResponseTest(final ServletType servletType) {
    super(servletType);
  }

  private ODataSingleProcessor processor;

  @Override
  protected ODataSingleProcessor createProcessor() throws ODataException {
    processor = mock(ODataSingleProcessor.class);

    return processor;
  }

  @Override
  protected EdmProvider createEdmProvider() {
    final EdmProvider provider = new ScenarioEdmProvider();
    return provider;
  }

  @Test
  public void test404HttpNotFound() throws Exception {
    when(processor.readEntity(any(GetEntityUriInfo.class), any(String.class))).thenThrow(
        new ODataNotFoundException(ODataNotFoundException.ENTITY));

    final HttpResponse response = executeGetRequest("Managers('199')");
    assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatusLine().getStatusCode());

    final String content = StringHelper.inputStreamToString(response.getEntity().getContent());
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_M_2007_08);
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
    assertXpathExists("/a:error/a:code", content);
    assertXpathValuesEqual("\"" + MessageService.getMessage(Locale.ENGLISH, ODataNotFoundException.ENTITY).getText()
        + "\"", "/a:error/a:message", content);
  }
  
  @Test
  public void test400BadRequestRedundantSystemQueryOptions() throws Exception {
    HttpResponse response = executeGetRequest("Employees?$top=1&$top=3");
    assertEquals(HttpStatusCodes.BAD_REQUEST.getStatusCode(), response.getStatusLine().getStatusCode());
    
    final String content = StringHelper.inputStreamToString(response.getEntity().getContent());
    assertEquals("<?xml version='1.0' encoding='UTF-8'?><error xmlns=\"http://schemas.microsoft.com/ado/2007/"
        + "08/dataservices/metadata\"><code/><message xml:lang=\"en\">Duplicate system query parameter names: "
        + "'$top'.</message></error>", content);
  }
  
  @Test
  public void genericHttpExceptions() throws Exception {
    disableLogging();

    final List<ODataHttpException> toTestExceptions = getHttpExceptionsForTest();

    int firstKey = 1;
    for (final ODataHttpException oDataException : toTestExceptions) {
      final String key = String.valueOf(firstKey++);
      final Matcher<GetEntityUriInfo> match = new EntityKeyMatcher(key);
      when(processor.readEntity(Matchers.argThat(match), any(String.class))).thenThrow(oDataException);

      final HttpResponse response = executeGetRequest("Managers('" + key + "')");

      assertEquals("Expected status code does not match for exception type '"
          + oDataException.getClass().getSimpleName() + "'.",
          oDataException.getHttpStatus().getStatusCode(), response.getStatusLine().getStatusCode());

      final String content = StringHelper.inputStreamToString(response.getEntity().getContent());
      Map<String, String> prefixMap = new HashMap<String, String>();
      prefixMap.put("a", Edm.NAMESPACE_M_2007_08);
      XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
      assertXpathExists("/a:error/a:code", content);
    }

  }

  private List<ODataHttpException> getHttpExceptionsForTest() throws Exception {
    final List<Class<ODataHttpException>> exClasses =
        ClassHelper.getAssignableClasses("org.apache.olingo.odata2.api.exception", ODataHttpException.class);

    final MessageReference mr = MessageReference.create(ODataHttpException.class, "SIMPLE FOR TEST");
    return ClassHelper.getClassInstances(exClasses, new Class<?>[] { MessageReference.class }, new Object[] { mr });
  }

  private class EntityKeyMatcher extends BaseMatcher<GetEntityUriInfo> {

    private final String keyLiteral;

    public EntityKeyMatcher(final String keyLiteral) {
      if (keyLiteral == null) {
        throw new IllegalArgumentException("Key parameter MUST NOT be NULL.");
      }
      this.keyLiteral = keyLiteral;
    }

    @Override
    public boolean matches(final Object item) {
      if (item instanceof UriInfoImpl) {
        final UriInfoImpl upr = (UriInfoImpl) item;
        final List<KeyPredicate> keyPredicates = upr.getKeyPredicates();
        for (final KeyPredicate keyPredicate : keyPredicates) {
          if (keyLiteral.equals(keyPredicate.getLiteral())) {
            return true;
          }
        }
      }
      return false;
    }

    @Override
    public void describeTo(final Description description) {
      // description.appendText("");
    }

  }
}
