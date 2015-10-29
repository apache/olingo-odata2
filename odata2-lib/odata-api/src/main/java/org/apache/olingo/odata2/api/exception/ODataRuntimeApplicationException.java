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
package org.apache.olingo.odata2.api.exception;

import java.util.Locale;

import org.apache.olingo.odata2.api.commons.HttpStatusCodes;

/**
 * This class represents a translated application runtime exception. Use this exception class to display custom
 * exception
 * messages.
 * <br>If a HTTP status is given this exception will result in the set status code like an HTTP exception.
 * <br>A set status code can be used to show a substatus to a HTTP status as described in the OData protocol
 * specification.
 * 
 */
public class ODataRuntimeApplicationException extends RuntimeException {

  private static final long serialVersionUID = -7869148184447528782L;

  private String errorCode;
  private HttpStatusCodes httpStatus = HttpStatusCodes.INTERNAL_SERVER_ERROR;
  private final Locale locale;

  /**
   * Since this is a translated application exception locale must not be null.
   * @param message
   * @param locale
   */
  public ODataRuntimeApplicationException(final String message, final Locale locale) {
    super(message);
    this.locale = locale;
  }

  /**
   * Since this is a translated application exception locale must not be null.
   * <br>The status code given will be displayed at the client.
   * @param message
   * @param locale
   * @param status
   */
  public ODataRuntimeApplicationException(final String message, final Locale locale, final HttpStatusCodes status) {
    this(message, locale);
    httpStatus = status;
  }

  /**
   * Since this is a translated application exception locale must not be null.
   * <br>The status code given will be displayed at the client.
   * <br>The error code may be used as a substatus for the HTTP status code as described in the OData protocol
   * specification.
   * @param message
   * @param locale
   * @param status
   * @param errorCode
   */
  public ODataRuntimeApplicationException(final String message, final Locale locale, final HttpStatusCodes status,
      final String errorCode) {
    this(message, locale, status);
    this.errorCode = errorCode;
  }

  /**
   * Since this is a translated application exception locale must not be null.
   * <br>The status code given will be displayed at the client.
   * <br>The error code may be used as a substatus for the HTTP status code as described in the OData protocol
   * specification.
   * @param message
   * @param locale
   * @param status
   * @param errorCode
   * @param e
   */
  public ODataRuntimeApplicationException(final String message, final Locale locale, final HttpStatusCodes status,
      final String errorCode, final Throwable e) {
    super(message, e);
    this.errorCode = errorCode;
    httpStatus = status;
    this.locale = locale;
  }

  /**
   * Since this is a translated application exception locale must not be null.
   * @param message
   * @param locale
   * @param e
   */
  public ODataRuntimeApplicationException(final String message, final Locale locale, final Throwable e) {
    super(message, e);
    this.locale = locale;
  }

  /**
   * Since this is a translated application exception locale must not be null.
   * <br>The status code given will be displayed at the client.
   * @param message
   * @param locale
   * @param status
   * @param e
   */
  public ODataRuntimeApplicationException(final String message, final Locale locale, final HttpStatusCodes status,
      final Throwable e) {
    this(message, locale, e);
    httpStatus = status;
  }

  /**
   * Since this is a translated application exception locale must not be null.
   * <br>The error code may be used as a substatus for the HTTP status code as described in the OData protocol
   * specification.
   * @param message
   * @param locale
   * @param errorCode
   * @param e
   */
  public ODataRuntimeApplicationException(final String message, final Locale locale, final String errorCode,
      final Throwable e) {
    this(message, locale, e);
    this.errorCode = errorCode;

  }

  /**
   * @return {@link Locale} the locale
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Default HttpStatusCodes.INTERNAL_SERVER_ERROR
   * @return {@link HttpStatusCodes} the status code
   */
  public HttpStatusCodes getHttpStatus() {
    return httpStatus;
  }

  /**
   * Default code is null
   * @return <b>String</b>The error code displayed in the error message.
   */
  public String getCode() {
    return errorCode;
  }

}
