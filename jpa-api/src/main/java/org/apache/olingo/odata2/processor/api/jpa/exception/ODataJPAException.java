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
package org.apache.olingo.odata2.processor.api.jpa.exception;

import java.util.Locale;

import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataException;

/**
 * The exception class is the base of OData JPA exceptions. The class also
 * provides non localized error texts that can be used for raising OData JPA
 * exceptions with non localized error texts.
 * 
 *  
 * 
 */
public abstract class ODataJPAException extends ODataException {

  protected MessageReference messageReference;

  public static final String ODATA_JPACTX_NULL = "OData JPA Context cannot be null";

  private static final long serialVersionUID = -6884673558124441214L;
  protected static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  protected ODataJPAException(final String localizedMessage, final Throwable e, final MessageReference msgRef) {
    super(localizedMessage, e);
    messageReference = msgRef;
  }

  /**
   * The method creates a Reference to Message Object
   * {@link org.apache.olingo.odata2.api.exception.MessageReference} . The message
   * text key is derived out of parameters clazz.messageReferenceKey.
   * 
   * @param clazz
   *            is name of the class extending
   *            {@link org.apache.olingo.odata2.processor.api.jpa.exception.ODataJPAException}
   * @param messageReferenceKey
   *            is the key of the message
   * @return an instance of type
   *         {@link org.apache.olingo.odata2.api.exception.MessageReference}
   */
  protected static MessageReference createMessageReference(final Class<? extends ODataJPAException> clazz, final String messageReferenceKey) {
    return MessageReference.create(clazz, messageReferenceKey);
  }

  public MessageReference getMessageReference() {
    return messageReference;
  }

}
