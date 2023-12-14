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
package org.apache.olingo.odata2.jpa.processor.ref.extension;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import org.apache.olingo.odata2.jpa.processor.api.OnJPAWriteContent;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.hsqldb.jdbc.JDBCBlob;
import org.hsqldb.jdbc.JDBCClob;

public class OnDBWriteContent implements OnJPAWriteContent {

    @Override
    public Blob getJPABlob(final byte[] binaryData) throws ODataJPARuntimeException {
        try {
            return new JDBCBlob(binaryData);
        } catch (SQLException e) {
            ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
        }
        return null;
    }

    @Override
    public Clob getJPAClob(final char[] characterData) throws ODataJPARuntimeException {
        try {
            return new JDBCClob(new String(characterData));
        } catch (SQLException e) {
            ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
        }
        return null;
    }
}
