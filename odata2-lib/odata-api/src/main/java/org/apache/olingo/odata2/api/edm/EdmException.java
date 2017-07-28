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
package org.apache.olingo.odata2.api.edm;

import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataMessageException;

/**
 * @org.apache.olingo.odata2.DoNotImplement
 * An exception for problems regarding the Entity Data Model.
 * 
 */
public class EdmException extends ODataMessageException {

  private static final long serialVersionUID = 1L;

  public static final MessageReference COMMON = createMessageReference(EdmException.class, "COMMON");
  public static final MessageReference PROVIDERPROBLEM = createMessageReference(EdmException.class, "PROVIDERPROBLEM");
  public static final MessageReference PROPERTYNOTFOUND =
      createMessageReference(EdmException.class, "PROPERTYNOTFOUND");
  public static final MessageReference NAVIGATIONPROPERTYNOTFOUND =
      createMessageReference(EdmException.class, "NAVIGATIONPROPERTYNOTFOUND");
  public static final MessageReference MUSTBENAVIGATIONPROPERTY =
      createMessageReference(EdmException.class, "MUSTBENAVIGATIONPROPERTY");
  public static final MessageReference MUSTBEPROPERTY = createMessageReference(EdmException.class, "MUSTBEPROPERTY");
  public static final MessageReference NAMINGERROR =
      createMessageReference(EdmException.class, "NAMINGERROR");
  
  public EdmException(final MessageReference messageReference) {
    super(messageReference);
  }

  public EdmException(final MessageReference messageReference, final Throwable cause) {
    super(messageReference, cause);
  }

  public EdmException(final MessageReference messageReference, final String errorCode) {
    super(messageReference, errorCode);
  }

  public EdmException(final MessageReference messageReference, final Throwable cause, final String errorCode) {
    super(messageReference, cause, errorCode);
  }

}
