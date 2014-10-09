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
package org.apache.olingo.odata2.jpa.processor.ref.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.hsqldb.jdbc.JDBCBlob;

@Converter(autoApply = true)
public class BlobToByteConverter implements AttributeConverter<Blob, byte[]> {

  @Override
  public byte[] convertToDatabaseColumn(final Blob arg0) {
    if (arg0 == null) {
      return null;
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    InputStream is;
    try {
      is = arg0.getBinaryStream();
      int b;
      b = is.read();
      while (b != -1) {
        buffer.write(b);
        b = is.read();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return buffer.toByteArray();
  }

  @Override
  public Blob convertToEntityAttribute(final byte[] arg0) {
    try {
      if (arg0 == null) {
        return null;
      }
      return new JDBCBlob(arg0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
