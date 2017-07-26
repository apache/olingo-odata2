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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNamed;

/**
 * See in ABNF
 * <p>
 * <code>
 *   Note:this pattern is overly restrictive, the normative definition is type TSimpleIdentifier in OData EDM XML Schema
 *   <br/>
 *   odataIdentifier             = identifierLeadingCharacter *127identifierCharacter
 *   <br/>
 *   identifierLeadingCharacter  = ALPHA / "_"         ; plus Unicode characters from the categories L or Nl
 *   <br/>
 *   identifierCharacter         = ALPHA / "_" / DIGIT ; plus Unicode characters from the categories L, Nl, Nd, Mn,
 *   Mc, Pc, or Cf
 *   <br/>
 * </code>
 * </p>
 * And in OData V2 MC-CSDL (Release v20110610)
 * <p>
 * Section 2.2.6 SimpleIdentifier<br/>
 * SimpleIdentifier is a string-based representation. The maximum length of the identifier MUST be less than 480.
 * The below pattern represents the allowed identifiers in ECMA specification:
 * Pattern: <code>value="[\p{L}\p{Nl}][\p{L}\p{Nl}\p{Nd}\p{Mn}\p{Mc}\p{Pc}\p{Cf}]{0,}"</code>
 * </p>
 *
 */
public abstract class EdmNamedImplProv implements EdmNamed {

  private static final Pattern PATTERN_VALID_NAME = Pattern.compile(
      "\\A[_\\p{L}\\p{Nl}][_\\p{L}\\p{Nl}\\p{Nd}\\p{Mn}\\p{Mc}\\p{Pc}\\p{Cf}]{0,}\\Z");
  protected EdmImplProv edm;
  private String name;

  public EdmNamedImplProv(final EdmImplProv edm, final String name) throws EdmException {
    this.edm = edm;
    this.name = getValidatedName(name);
  }

  @Override
  public String getName() throws EdmException {
    return name;
  }

  private String getValidatedName(final String name) throws EdmException {
    Matcher matcher = PATTERN_VALID_NAME.matcher(name);
    if (matcher.matches()) {
      return name;
    }
    throw new EdmException(EdmException.NAMINGERROR.addContent(name));
  }
}
