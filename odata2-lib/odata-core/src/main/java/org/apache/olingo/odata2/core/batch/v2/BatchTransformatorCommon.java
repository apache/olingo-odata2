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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.core.batch.BatchHelper;
import org.apache.olingo.odata2.core.batch.v2.Header.HeaderField;

public class BatchTransformatorCommon {
  public static void validateContentType(final Header headers) throws BatchException {
    List<String> contentTypes = headers.getHeaders(HttpHeaders.CONTENT_TYPE);

    if (contentTypes.isEmpty()) {
      throw new BatchException(BatchException.MISSING_CONTENT_TYPE);
    }
    if (!headers.isHeaderMatching(HttpHeaders.CONTENT_TYPE, BatchParserCommon.PATTERN_MULTIPART_MIXED)
      && !headers.isHeaderMatching(HttpHeaders.CONTENT_TYPE, BatchParserCommon.PATTERN_CONTENT_TYPE_APPLICATION_HTTP)) {
      throw new BatchException(BatchException.INVALID_CONTENT_TYPE.addContent(
          HttpContentType.MULTIPART_MIXED + " or " + HttpContentType.APPLICATION_HTTP));
    }
  }

  public static void validateContentTransferEncoding(final Header headers, final boolean isChangeRequest)
      throws BatchException {
    final HeaderField contentTransferField = headers.getHeaderField(BatchHelper.HTTP_CONTENT_TRANSFER_ENCODING);

    if (contentTransferField != null) {
      final List<String> contentTransferValues = contentTransferField.getValues();
      if (contentTransferValues.size() == 1) {
        String encoding = contentTransferValues.get(0);

        if (!BatchHelper.BINARY_ENCODING.equalsIgnoreCase(encoding)) {
          throw new BatchException(
              BatchException.INVALID_CONTENT_TRANSFER_ENCODING.addContent(contentTransferField.getLineNumber()));
        }
      } else {
        throw new BatchException(BatchException.INVALID_HEADER.addContent(contentTransferField.getLineNumber()));
      }
    } else {
      if (isChangeRequest) {
        throw new BatchException(BatchException.INVALID_CONTENT_TRANSFER_ENCODING.addContent(headers.getLineNumber()));
      }
    }
  }

  public static int getContentLength(final Header headers) throws BatchException {
    final HeaderField contentLengthField = headers.getHeaderField(HttpHeaders.CONTENT_LENGTH);

    if (contentLengthField != null && contentLengthField.getValues().size() == 1) {
      final List<String> contentLengthValues = contentLengthField.getValues();

      try {
        int contentLength = Integer.parseInt(contentLengthValues.get(0));

        if (contentLength < 0) {
          throw new BatchException(BatchException.INVALID_HEADER.addContent(contentLengthField.getValue()).addContent(
              contentLengthField.getLineNumber()));
        }

        return contentLength;
      } catch (NumberFormatException e) {
        throw new BatchException(BatchException.INVALID_HEADER.addContent(contentLengthField.getValue()).addContent(
            contentLengthField.getLineNumber()), e);
      }
    }

    return -1;
  }

  public static void validateHost(final Header headers, final String baseUri) throws BatchException {
    final HeaderField hostField = headers.getHeaderField(HttpHeaders.HOST);

    if (hostField != null) {
      if (hostField.getValues().size() > 1
          || !URI.create(baseUri).getAuthority().equalsIgnoreCase(hostField.getValues().get(0).trim())) {
        throw new BatchException(BatchException.INVALID_HEADER.addContent(hostField.getValues().get(0))
            .addContent(hostField.getLineNumber()));
      }
    }
  }

  public static class HttpResponsetStatusLine {
    private static final String REG_EX_STATUS_LINE = "(?:HTTP/[0-9]\\.[0-9])\\s([0-9]{3})\\s([\\S ]+)\\s*";
    private Line httpStatusLine;
    private String statusCode;
    private String statusInfo;

    public HttpResponsetStatusLine(final Line httpStatusLine) throws BatchException {
      this.httpStatusLine = httpStatusLine;
      parse();
    }

    private void parse() throws BatchException {
      final Pattern regexPattern = Pattern.compile(REG_EX_STATUS_LINE);
      final Matcher matcher = regexPattern.matcher(httpStatusLine.toString());

      if (matcher.find()) {
        statusCode = matcher.group(1);
        statusInfo = matcher.group(2);
      } else {
        throw new BatchException(BatchException.INVALID_STATUS_LINE.addContent(httpStatusLine.toString())
            .addContent(httpStatusLine.getLineNumber()));
      }
    }

    public String getStatusCode() {
      return statusCode;
    }

    public String getStatusInfo() {
      return statusInfo;
    }
  }

  public static class HttpRequestStatusLine {
    private static final Set<String> HTTP_BATCH_METHODS = new HashSet<String>(Arrays.asList(new String[] { "GET" }));
    private static final Set<String> HTTP_CHANGE_SET_METHODS = new HashSet<String>(Arrays.asList(new String[] { "POST",
        "PUT", "DELETE", "MERGE", "PATCH" }));
    private static final String HTTP_VERSION = "HTTP/1.1";
    
    final private Line statusLine;
    final String requestBaseUri;
    final PathInfo batchRequestPathInfo;

    private ODataHttpMethod method;
    private PathInfo pathInfo;
    private String httpVersion;

    public HttpRequestStatusLine(final Line httpStatusLine, final String baseUri, final PathInfo pathInfo)
        throws BatchException {
      statusLine = httpStatusLine;
      requestBaseUri = baseUri;
      batchRequestPathInfo = pathInfo;

      parse();
    }

    private void parse() throws BatchException {
      final String[] parts = statusLine.toString().split(" ");

      if (parts.length == 3) {
        try {
          method = parseMethod(parts[0]);
          pathInfo = parseUri(parts[1]);
          httpVersion = parseHttpVersion(parts[2]);
        } catch (IllegalArgumentException e) {
          throw new BatchException(BatchException.MISSING_METHOD.addContent(statusLine.getLineNumber()), e);
        }
      } else {
        throw new BatchException(BatchException.INVALID_REQUEST_LINE.addContent(statusLine.toString())
            .addContent(statusLine.getLineNumber()));
      }
    }

    private ODataHttpMethod parseMethod(final String method) throws BatchException {
      try {
        return ODataHttpMethod.valueOf(method.trim());
      } catch (IllegalArgumentException e) {
        throw new BatchException(BatchException.MISSING_METHOD.addContent(statusLine.getLineNumber()), e);
      }
    }

    private PathInfo parseUri(final String uri) throws BatchException {
      PathInfoImpl pInfo = new PathInfoImpl();
      pInfo.setServiceRoot(batchRequestPathInfo.getServiceRoot());
      pInfo.setPrecedingPathSegment(batchRequestPathInfo.getPrecedingSegments());

      try {
        final URI uriObject = new URI(uri);
        String relativeUri = "";
        if (uriObject.isAbsolute()) {
          if (uri.startsWith(requestBaseUri + '/')) {
            relativeUri = uri.substring(requestBaseUri.length() + 1);
          }
        } else if (uri.startsWith(batchRequestPathInfo.getServiceRoot().getRawPath())) {
          relativeUri = uri.substring(batchRequestPathInfo.getServiceRoot().getRawPath().length());
        } else {
          relativeUri = uri;
        }

        Matcher uriParts = BatchParserCommon.PATTERN_RELATIVE_URI.matcher(relativeUri);

        if (uriParts.lookingAt() && uriParts.groupCount() == 2) {
          final String odataPathSegmentsAsString = uriParts.group(1);
          final String queryParametersAsString = uriParts.group(2) != null ? uriParts.group(2) : "";

          pInfo.setODataPathSegment(parseODataPathSegments(odataPathSegmentsAsString));
          if (!odataPathSegmentsAsString.startsWith("$")) {
            String requestUri = requestBaseUri + "/" + odataPathSegmentsAsString + queryParametersAsString;
            pInfo.setRequestUri(new URI(requestUri));
          }

        } else {
          throw new BatchException(BatchException.INVALID_URI.addContent(statusLine.getLineNumber()));
        }
      } catch (URISyntaxException e) {
        throw new BatchException(BatchException.INVALID_URI.addContent(statusLine.getLineNumber()), e);
      }

      return pInfo;
    }

    private List<PathSegment> parseODataPathSegments(final String odataPathSegmentsAsString) {
      final List<PathSegment> odataPathSegments = new ArrayList<PathSegment>();
      final String[] pathParts = odataPathSegmentsAsString.split("/");

      for (final String pathSegment : pathParts) {
        odataPathSegments.add(new ODataPathSegmentImpl(pathSegment, null));
      }

      return odataPathSegments;
    }

    private String parseHttpVersion(final String httpVersion) throws BatchException {
      if (!HTTP_VERSION.equals(httpVersion.trim())) {
        throw new BatchException(BatchException.INVALID_REQUEST_LINE
                                                  .addContent(statusLine.toString())
                                                  .addContent(statusLine.getLineNumber()));
      } else {
        return HTTP_VERSION;
      }
    }

    public void validateHttpMethod(boolean isChangeSet) throws BatchException {
      Set<String> validMethods = (isChangeSet) ? HTTP_CHANGE_SET_METHODS : HTTP_BATCH_METHODS;
      
      if (!validMethods.contains(getMethod().toString())) {
        if (isChangeSet) {
          throw new BatchException(BatchException.INVALID_CHANGESET_METHOD.addContent(statusLine.getLineNumber()));
        } else {
          throw new BatchException(BatchException.INVALID_QUERY_OPERATION_METHOD
              .addContent(statusLine.getLineNumber()));
        }
      }
    }

    public ODataHttpMethod getMethod() {
      return method;
    }

    public PathInfo getPathInfo() {
      return pathInfo;
    }

    public String getHttpVersion() {
      return httpVersion;
    }

    public int getLineNumber() {
      return statusLine.getLineNumber();
    }
  }
}
