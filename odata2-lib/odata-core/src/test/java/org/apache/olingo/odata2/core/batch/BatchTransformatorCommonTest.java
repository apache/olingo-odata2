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
package org.apache.olingo.odata2.core.batch;

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.core.batch.v2.BatchTransformatorCommon;
import org.apache.olingo.odata2.core.batch.v2.Header;
import org.junit.Test;

public class BatchTransformatorCommonTest {

  private static final String BASE64_ENCODING = "BASE64";

  @Test
  public void testValidateContentTypeApplicationHTTP() throws BatchException {
    List<String> contentTypeValues = Arrays.asList(new String[] { HttpContentType.APPLICATION_HTTP });
    final Header headers = makeHeaders(HttpHeaders.CONTENT_TYPE, contentTypeValues);

    BatchTransformatorCommon.validateContentType(headers);
  }

  @Test
  public void testValidateContentTypeMultipartMixed() throws BatchException {
    List<String> contentTypeValues =
        Arrays.asList(new String[] { HttpContentType.MULTIPART_MIXED + "; boundary=batch_32332_32323_fdsf" });
    final Header headers = makeHeaders(HttpHeaders.CONTENT_TYPE, contentTypeValues);

    BatchTransformatorCommon.validateContentType(headers);
  }

  @Test
  public void testValidateContentTypeMultipartMixedCaseInsensitiv() throws BatchException {
    List<String> contentTypeValues =
        Arrays.asList(new String[] { "mulTiPart/MiXed; boundary=batch_32332_32323_fdsf" });
    final Header headers = makeHeaders(HttpHeaders.CONTENT_TYPE, contentTypeValues);

    BatchTransformatorCommon.validateContentType(headers);
  }

  @Test(expected = BatchException.class)
  public void testValidateContentTypeNoValue() throws BatchException {
    List<String> contentTypeValues = Arrays.asList(new String[] {});
    final Header headers = makeHeaders(HttpHeaders.CONTENT_TYPE, contentTypeValues);

    BatchTransformatorCommon.validateContentType(headers);
  }

  @Test(expected = BatchException.class)
  public void testValidateContentTypeMissingHeader() throws BatchException {
    final Header headers = new Header(1);
    
    BatchTransformatorCommon.validateContentType(headers);
  }

  @Test(expected = BatchException.class)
  public void testValidateContentTypeMultipleValues() throws BatchException {
    List<String> contentTypeValues =
        Arrays.asList(new String[] { HttpContentType.APPLICATION_HTTP, HttpContentType.MULTIPART_MIXED });
    final Header headers = makeHeaders(HttpHeaders.CONTENT_TYPE, contentTypeValues);

    BatchTransformatorCommon.validateContentType(headers);
  }

  @Test
  public void testValidateContentTransferEncoding() throws BatchException {
    List<String> contentTransferEncoding = Arrays.asList(new String[] { BatchHelper.BINARY_ENCODING });
    final Header headers = makeHeaders(BatchHelper.HTTP_CONTENT_TRANSFER_ENCODING, contentTransferEncoding);

    BatchTransformatorCommon.validateContentTransferEncoding(headers, false);
  }

  @Test(expected = BatchException.class)
  public void testValidateContentTransferEncodingMultipleValues() throws BatchException {
    List<String> contentTransferEncoding = Arrays.asList(new String[] { BatchHelper.BINARY_ENCODING, BASE64_ENCODING });
    final Header headers = makeHeaders(BatchHelper.HTTP_CONTENT_TRANSFER_ENCODING, contentTransferEncoding);

    BatchTransformatorCommon.validateContentTransferEncoding(headers, false);
  }

  @Test(expected = BatchException.class)
  public void testValidateContentTransferEncodingMissingHeader() throws BatchException {
    final Header headers = new Header(1);
    
    BatchTransformatorCommon.validateContentTransferEncoding(headers, true);
  }

  @Test(expected = BatchException.class)
  public void testValidateContentTransferEncodingMissingValue() throws BatchException {
    List<String> contentTransferEncoding = Arrays.asList(new String[] {});
    final Header headers = makeHeaders(BatchHelper.HTTP_CONTENT_TRANSFER_ENCODING, contentTransferEncoding);

    BatchTransformatorCommon.validateContentTransferEncoding(headers, true);
  }

  private Header makeHeaders(final String headerName, final List<String> values) {
    final Header headers = new Header(1);
    headers.addHeader(headerName, values, 1);

    return headers;
  }

}
