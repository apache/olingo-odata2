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
package org.apache.olingo.odata2.core.batch;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

public class BatchHelper {

  public static final String BINARY_ENCODING = "binary";

  public static final String DEFAULT_ENCODING = "utf-8";

  public static final String HTTP_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";

  public static final String HTTP_CONTENT_ID = "Content-Id";

  public static final String MIME_HEADER_CONTENT_ID = "MimeHeader-ContentId";

  public static final String REQUEST_HEADER_CONTENT_ID = "RequestHeader-ContentId";

  protected static String generateBoundary(final String value) {
    return value + "_" + UUID.randomUUID().toString();
  }

  protected static byte[] getBytes(final String body) {
    try {
      return body.getBytes(DEFAULT_ENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new ODataRuntimeException(e);
    }
  }
}
