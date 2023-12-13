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
package org.apache.olingo.odata2.jpa.processor.core.mock;

import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;

public class ODataJPAServiceFactoryMock extends ODataJPAServiceFactory {
    private ODataContext context = null;

    public ODataJPAServiceFactoryMock(final ODataContext context) {
        this.context = context;
    }

    @Override
    public ODataJPAContext initializeODataJPAContext() throws ODataJPARuntimeException {
        ODataJPAContext oDataJPAContext = null;
        oDataJPAContext = ODataJPAContextMock.mockODataJPAContext(context);
        setOnWriteJPAContent(new OnJPAWriteContentMock());
        return oDataJPAContext;
    }

    public ODataJPAContext initializeODataJPAContextX() throws ODataJPARuntimeException {
        ODataJPAContext oDataJPAContext = null;
        oDataJPAContext = ODataJPAContextMock.mockODataJPAContext(context);
        setOnWriteJPAContent(null);
        return oDataJPAContext;
    }
}
