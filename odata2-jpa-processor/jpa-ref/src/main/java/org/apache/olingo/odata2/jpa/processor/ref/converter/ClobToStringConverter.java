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
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.hsqldb.jdbc.JDBCClob;

@Converter(autoApply = true)
public class ClobToStringConverter implements AttributeConverter<Clob, String> {

  @Override
  public String convertToDatabaseColumn(Clob clob) {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      Reader reader = clob.getCharacterStream();

      int byteRead = reader.read();
      while (byteRead != -1) {
        os.write(byteRead);
        byteRead = reader.read();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return os.toString();
  }

  @Override
  public Clob convertToEntityAttribute(String string) {
    if (string == null) {
      return null;
    }
    Clob clob = null;
    try {
      clob = new JDBCClob(string);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return clob;
  }

}
