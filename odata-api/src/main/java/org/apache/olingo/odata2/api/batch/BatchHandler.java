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
package org.apache.olingo.odata2.api.batch;

import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;

/**
 * @author SAP AG
 */
public interface BatchHandler {
  /**
   * <p>Handles the {@link BatchPart} in a way that it results in a corresponding {@link BatchResponsePart}.</p>
   * @param batchPart the incoming batchPart
   * @return the corresponding result
   * @throws ODataException
   */
  public BatchResponsePart handleBatchPart(BatchPart batchPart) throws ODataException;

  /**
   * <p>Delegates a handling of the request {@link ODataRequest} to the request handler and provides ODataResponse {@link ODataResponse}.</p>
   * @param request the incoming request
   * @return the corresponding result
   * @throws ODataException
   */
  public ODataResponse handleRequest(ODataRequest request) throws ODataException;
}
