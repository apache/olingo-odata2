/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
/**
 * Exception Classes used in the OData library as well as the implementing application
 * <p>APPLICATION DEVELOPERS: Please use {@link org.apache.olingo.odata2.api.exception.ODataApplicationException} for custom exceptions.
 * 
 * <p><b>Exception handling:</b>
 * <br>Inside the OData library an ExceptionMapper exists which can transform any exception into an OData error format. 
 * The ExceptionMapper behaves after the following algorithm:
 * <br>1. The cause of the exception will be determined by looking into the stack trace.
 * <br>1.1. If the cause is an ODataApplicationException meaning that somewhere in the stack an ODataApplicationException is found the 
 * ExceptionMapper will take the following information from the ApplicationException and transform it into an OData error: 
 * message text, Locale, Inner Error and Error Code. There will be no altering of information for the ODataApplicationException.
 * <br>1.2. If no ODataApplicationException is found in the stack the cause can be three different types of exceptions: ODataHttpException, ODataMessageException or an uncaught RuntimeException.
 * <br>The ExceptionMapper will process them in the following order: 1. ODataHttpException, 2. ODataMessageException, 3 Other Exceptions.
 * <br>1.2.1. ODataHttpExceptions will be transformed as follows: If an error code is set it will be displayed. The HTTP status code will be derived from the ODataHttpException. The message text and its language depend on the AcceptLanguageHeaders. 
 * The first supported language which is found in the Headers will result in the language of the message and the response. 
 * <br>1.2.1. ODataMessageException will be transformed as follows: If an error code is set it will be displayed. The HTTP status code will be 500.
 * The message text and its language depend on the AcceptLanguageHeaders. The first supported language which is found in the Headers will result in the language of the message and the response. 
 * <br>1.2.1 Runtime Exceptions will be transformed as follows: No error code will be set. HTTP status will be 500. Message text will be taken from the exception and the language for the response will be English as default. 
 * <p><b>Exception Hierarchy</b>
 * <br> {@link org.apache.olingo.odata2.api.exception.ODataException}
 * <br> *{@link org.apache.olingo.odata2.api.exception.ODataApplicationException}
 * <br> *{@link org.apache.olingo.odata2.api.exception.ODataMessageException}
 * <br> ** {@link org.apache.olingo.odata2.api.edm.EdmException}
 * <br> ** {@link org.apache.olingo.odata2.api.ep.EntityProviderException}
 * <br> ** {@link org.apache.olingo.odata2.api.uri.expression.ExceptionVisitExpression}
 * <br> ** {@link org.apache.olingo.odata2.api.exception.ODataHttpException}
 * <br> *** {@link org.apache.olingo.odata2.api.exception.ODataConflictException}
 * <br> *** {@link org.apache.olingo.odata2.api.exception.ODataForbiddenException}
 * <br> *** {@link org.apache.olingo.odata2.api.exception.ODataMethodNotAllowedException}
 * <br> *** {@link org.apache.olingo.odata2.api.exception.ODataNotAcceptableException}
 * <br> *** {@link org.apache.olingo.odata2.api.exception.ODataNotImplementedException}
 * <br> *** {@link org.apache.olingo.odata2.api.exception.ODataPreconditionFailedException}
 * <br> *** {@link org.apache.olingo.odata2.api.exception.ODataPreconditionRequiredException}
 * <br> *** {@link org.apache.olingo.odata2.api.exception.ODataServiceUnavailableException}
 * <br> *** {@link org.apache.olingo.odata2.api.exception.ODataUnsupportedMediaTypeException}
 * <br> *** {@link org.apache.olingo.odata2.api.exception.ODataNotFoundException}
 * <br> **** {@link org.apache.olingo.odata2.api.uri.UriNotMatchingException}
 * <br> *** {@link org.apache.olingo.odata2.api.exception.ODataBadRequestException}
 * <br> **** {@link org.apache.olingo.odata2.api.uri.expression.ExpressionParserException}
 * <br> **** {@link org.apache.olingo.odata2.api.uri.UriSyntaxException}
 */
package org.apache.olingo.odata2.api.exception;
