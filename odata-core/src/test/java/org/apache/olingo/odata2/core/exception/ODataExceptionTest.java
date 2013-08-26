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
package org.apache.olingo.odata2.core.exception;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmLiteralException;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataConflictException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataForbiddenException;
import org.apache.olingo.odata2.api.exception.ODataHttpException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.exception.ODataMethodNotAllowedException;
import org.apache.olingo.odata2.api.exception.ODataNotAcceptableException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.exception.ODataPreconditionFailedException;
import org.apache.olingo.odata2.api.exception.ODataPreconditionRequiredException;
import org.apache.olingo.odata2.api.exception.ODataServiceUnavailableException;
import org.apache.olingo.odata2.api.exception.ODataUnsupportedMediaTypeException;
import org.apache.olingo.odata2.api.uri.UriNotMatchingException;
import org.apache.olingo.odata2.api.uri.UriSyntaxException;
import org.apache.olingo.odata2.api.uri.expression.ExceptionVisitExpression;
import org.apache.olingo.odata2.api.uri.expression.ExpressionParserException;
import org.apache.olingo.odata2.core.uri.expression.ExpressionParserInternalError;
import org.apache.olingo.odata2.core.uri.expression.FilterParserExceptionImpl;
import org.apache.olingo.odata2.core.uri.expression.TokenizerException;
import org.apache.olingo.odata2.core.uri.expression.TokenizerExpectError;
import org.apache.olingo.odata2.testutil.fit.BaseTest;
import org.apache.olingo.odata2.testutil.helper.ODataMessageTextVerifier;
import org.junit.Test;

/**
 *  
 */
public class ODataExceptionTest extends BaseTest {

  @Test
  public void noCause() {
    ODataException exception = new ODataException("Some message.");
    assertFalse(exception.isCausedByHttpException());
  }

  @Test
  public void nullPointerExceptionCause() {
    ODataException exception = new ODataException("Some message.", new NullPointerException());
    assertFalse(exception.isCausedByHttpException());
  }

  @Test
  public void oDataContextedCause() {
    ODataException exception = new ODataException("Some message.", new ODataNotFoundException(ODataNotFoundException.ENTITY));
    assertTrue(exception.isCausedByHttpException());
  }

  @Test
  public void oDataContextedCauseLayer3() {
    ODataException exception = new ODataException("Some message.",
        new IllegalArgumentException(
            new ODataNotFoundException(ODataNotFoundException.ENTITY)));
    assertTrue(exception.isCausedByHttpException());
  }

  //The following tests verify whether all fields of type {@link MessageReference} of 
  //the tested (Exception) class are provided in the <b>i18n.properties</b> file.
  @Test
  public void messagesOfODataMessageExceptions() {
    ODataMessageTextVerifier.TestClass(ODataMessageException.class);

    ODataMessageTextVerifier.TestClass(UriNotMatchingException.class);
    ODataMessageTextVerifier.TestClass(UriSyntaxException.class);
    ODataMessageTextVerifier.TestClass(ExceptionVisitExpression.class);

    ODataMessageTextVerifier.TestClass(EdmLiteralException.class);
    ODataMessageTextVerifier.TestClass(EdmException.class);
    ODataMessageTextVerifier.TestClass(EdmSimpleTypeException.class);

    ODataMessageTextVerifier.TestClass(EntityProviderException.class);

    ODataMessageTextVerifier.TestClass(ODataHttpException.class);
    ODataMessageTextVerifier.TestClass(ODataBadRequestException.class);
    ODataMessageTextVerifier.TestClass(ODataConflictException.class);
    ODataMessageTextVerifier.TestClass(ODataForbiddenException.class);
    ODataMessageTextVerifier.TestClass(ODataNotFoundException.class);
    ODataMessageTextVerifier.TestClass(ODataMethodNotAllowedException.class);
    ODataMessageTextVerifier.TestClass(ODataNotAcceptableException.class);
    ODataMessageTextVerifier.TestClass(ODataPreconditionFailedException.class);
    ODataMessageTextVerifier.TestClass(ODataPreconditionRequiredException.class);
    ODataMessageTextVerifier.TestClass(ODataServiceUnavailableException.class);
    ODataMessageTextVerifier.TestClass(ODataUnsupportedMediaTypeException.class);
    ODataMessageTextVerifier.TestClass(ODataNotImplementedException.class);

    ODataMessageTextVerifier.TestClass(ExpressionParserException.class);
    ODataMessageTextVerifier.TestClass(FilterParserExceptionImpl.class);
    ODataMessageTextVerifier.TestClass(ExpressionParserInternalError.class);
    ODataMessageTextVerifier.TestClass(TokenizerException.class);
    ODataMessageTextVerifier.TestClass(TokenizerExpectError.class);
  }
}
