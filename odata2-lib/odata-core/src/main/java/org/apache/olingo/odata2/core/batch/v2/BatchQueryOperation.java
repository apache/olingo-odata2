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
package org.apache.olingo.odata2.core.batch.v2;

import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.core.batch.v2.BatchParserCommon.HeaderField;

public class BatchQueryOperation implements BatchPart {

  protected final boolean isStrict;
  protected String httpStatusLine;
  protected Map<String, HeaderField> headers;
  protected List<String> body;
  protected int bodySize;
  protected List<String> message;

  public BatchQueryOperation(final List<String> message, final boolean isStrict) {
    this.isStrict = isStrict;
    this.message = message;
  }

  public BatchQueryOperation parse() throws BatchException {
    httpStatusLine = consumeHttpStatusLine(message);
    headers = BatchParserCommon.consumeHeaders(message);
    BatchParserCommon.consumeBlankLine(message, isStrict);
    body = message;

    return this;
  }

  protected String consumeHttpStatusLine(final List<String> message) throws BatchException {
    if (message.size() > 0 && !message.get(0).trim().equals("")) {
      String method = message.get(0);
      message.remove(0);

      return method;
    } else {
      throw new BatchException(BatchException.INVALID_QUERY_OPERATION_METHOD);
    }
  }

  public String getHttpStatusLine() {
    return httpStatusLine;
  }

  public List<String> getBody() {
    return body;
  }

  public int getBodySize() {
    return bodySize;
  }

  @Override
  public Map<String, HeaderField> getHeaders() {
    return headers;
  }

  @Override
  public boolean isStrict() {
    return isStrict;
  }
}
