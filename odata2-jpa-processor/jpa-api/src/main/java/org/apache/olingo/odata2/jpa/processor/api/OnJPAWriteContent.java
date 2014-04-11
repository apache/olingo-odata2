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
package org.apache.olingo.odata2.jpa.processor.api;

import java.sql.Blob;
import java.sql.Clob;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;

/**
 * <p> The interface is a call back interface that enables OData JPA Processor to get JPA provider specific
 * implementation
 * of <b>java.sql.Blob</b> and <b>java.sql.Clob</b> instances.</p>
 * <p>
 * Implement this interface if the JPA Model uses the data types java.sql.Blob and java.sql.Clob for its entity
 * properties. </p>
 * 
 * 
 */
public interface OnJPAWriteContent extends ODataCallback {

  /**
   * Implement this method to instantiate JPA provider specific implementation of java.sql.Blob instance from an array
   * of bytes.
   * @param binaryData is an array of bytes
   * @return an instance of type {@link java.sql.Blob}
   */
  public Blob getJPABlob(byte[] binaryData) throws ODataJPARuntimeException;

  /**
   * Implement this method to instantiate JPA provider specific implementation of java.sql.Clob instance from an array
   * of characters.
   * @param characterData is an array of characters
   * @return an instance of type {@link java.sql.Clob}
   */
  public Clob getJPAClob(char[] characterData) throws ODataJPARuntimeException;
}
