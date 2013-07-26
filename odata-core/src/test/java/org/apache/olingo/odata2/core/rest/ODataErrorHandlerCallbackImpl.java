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
package org.apache.olingo.odata2.core.rest;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.processor.ODataErrorCallback;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataResponse.ODataResponseBuilder;
import org.apache.olingo.odata2.api.uri.PathInfo;

public class ODataErrorHandlerCallbackImpl implements ODataErrorCallback {

  @Override
  public ODataResponse handleError(final ODataErrorContext context) {
    ODataResponseBuilder responseBuilder = ODataResponse.entity("bla").status(HttpStatusCodes.BAD_REQUEST).contentHeader("text/html");

    if (context.getRequestUri() != null) {
      responseBuilder.header("RequestUri", context.getRequestUri().toASCIIString());
      PathInfo pathInfo = context.getPathInfo();
      if (pathInfo == null) {
        responseBuilder.header("PathInfo", "NULL");
      } else {
        responseBuilder.header("PathInfo", "TRUE");
        responseBuilder.header("PathInfo.oDataSegments", pathInfo.getODataSegments().toString());
        responseBuilder.header("PathInfo.precedingSegments", pathInfo.getPrecedingSegments().toString());
        responseBuilder.header("PathInfo.requestUri", pathInfo.getRequestUri().toString());
        responseBuilder.header("PathInfo.serviceRoot", pathInfo.getServiceRoot().toString());
      }
    }

    Map<String, List<String>> requestHeaders = context.getRequestHeaders();
    if (requestHeaders != null && requestHeaders.entrySet() != null) {
      Set<Entry<String, List<String>>> entries = requestHeaders.entrySet();
      for (Entry<String, List<String>> entry : entries) {
        responseBuilder.header(entry.getKey(), entry.getValue().toString());
      }
    }

    return responseBuilder.build();
  }

}
