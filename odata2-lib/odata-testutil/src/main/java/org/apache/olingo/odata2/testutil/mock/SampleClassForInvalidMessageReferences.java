/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.apache.olingo.odata2.testutil.mock;

import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataMessageException;

/**
 *
 */
public class SampleClassForInvalidMessageReferences extends ODataMessageException {
    private static final long serialVersionUID = 1L;

    public SampleClassForInvalidMessageReferences(final MessageReference messageReference) {
        super(messageReference);
    }

    public SampleClassForInvalidMessageReferences(final MessageReference messageReference, final String errorCode) {
        super(messageReference, errorCode);
    }

    public static final MessageReference EXIST = createMessageReference(SampleClassForInvalidMessageReferences.class, "EXIST");
    public static final MessageReference DOES_NOT_EXIST =
            createMessageReference(SampleClassForInvalidMessageReferences.class, "DOES_NOT_EXIST");
    public static final MessageReference EXITS_BUT_EMPTY =
            createMessageReference(SampleClassForInvalidMessageReferences.class, "EXITS_BUT_EMPTY");
}
