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
package org.apache.olingo.odata2.api.processor.part;

import java.io.InputStream;

import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;

/**
 * Execute a OData entity request. 
 * 
 * @author SAP AG
 */
public interface EntityProcessor extends ODataProcessor {

  /**
   * Reads an entity.
   * @param contentType the content type of the response
   * @return an {@link ODataResponse} object
   * @throws ODataException
   */
  ODataResponse readEntity(GetEntityUriInfo uriInfo, String contentType) throws ODataException;

  /**
   * Checks whether an entity exists.
   * @param contentType the content type of the response
   * @return an {@link ODataResponse} object
   * @throws ODataException
   */
  ODataResponse existsEntity(GetEntityCountUriInfo uriInfo, String contentType) throws ODataException;

  /**
   * Updates an entity.
   * @param uriInfo information about the request URI
   * @param content the content of the request, containing the updated entity data
   * @param requestContentType the content type of the request body
   * @param merge if <code>true</code>, properties not present in the data are left unchanged;
   *              if <code>false</code>, they are reset
   * @param contentType the content type of the response
   * @return an {@link ODataResponse} object
   * @throws ODataException
   */
  ODataResponse updateEntity(PutMergePatchUriInfo uriInfo, InputStream content, String requestContentType, boolean merge, String contentType) throws ODataException;

  /**
   * Deletes an entity.
   * @param uriInfo  a {@link DeleteUriInfo} object with information from the URI parser
   * @param contentType the content type of the response
   * @return an {@link ODataResponse} object
   * @throws ODataException
   */
  ODataResponse deleteEntity(DeleteUriInfo uriInfo, String contentType) throws ODataException;

}
