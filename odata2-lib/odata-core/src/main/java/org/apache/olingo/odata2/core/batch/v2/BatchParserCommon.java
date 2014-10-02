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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.core.batch.AcceptParser;
import org.apache.olingo.odata2.core.commons.Decoder;

public class BatchParserCommon {
  private static final String REG_EX_BOUNDARY =
      "([a-zA-Z0-9_\\-\\.'\\+]{1,70})|\"([a-zA-Z0-9_\\-\\.'\\+\\s\\" +
          "(\\),/:=\\?]{1,69}[a-zA-Z0-9_\\-\\.'\\+\\(\\),/:=\\?])\""; // See RFC 2046

  private static final String REX_EX_MULTIPART_BOUNDARY = "multipart/mixed;\\s*boundary=(.+)";
  private static final String REG_EX_APPLICATION_HTTP = "application/http";
  public static final Pattern PATTERN_MULTIPART_BOUNDARY = Pattern.compile(REX_EX_MULTIPART_BOUNDARY,
      Pattern.CASE_INSENSITIVE);
  public static final Pattern PATTERN_HEADER_LINE = Pattern.compile("([a-zA-Z\\-]+):\\s?(.*)\\s*");
  public static final Pattern PATTERN_CONTENT_TYPE_APPLICATION_HTTP = Pattern.compile(REG_EX_APPLICATION_HTTP,
      Pattern.CASE_INSENSITIVE);

  public static String trimStringListToStringLength(final List<String> list, final int length) {
    final String message = stringListToString(list);
    final int lastIndex = Math.min(length, message.length());

    return (lastIndex > 0) ? message.substring(0, lastIndex) : "";
  }

  public static String stringListToString(final List<String> list) {
    StringBuilder builder = new StringBuilder();

    for (String currentLine : list) {
      builder.append(currentLine);
    }

    return builder.toString();
  }

  public static InputStream convertMessageToInputStream(final List<String> messageList, final int contentLength)
      throws BatchException {
    final String message = trimStringListToStringLength(messageList, contentLength);

    return new ByteArrayInputStream(message.getBytes());
  }

  public static InputStream convertMessageToInputStream(final List<String> messageList)
      throws BatchException {
    final String message = stringListToString(messageList);

    return new ByteArrayInputStream(message.getBytes());
  }

  // TODO Splitten von InputStream, sodass nur eine Iteration erfolgen muss
  static List<List<String>> splitMessageByBoundary(final List<String> message, final String boundary)
      throws BatchException {
    final List<List<String>> messageParts = new LinkedList<List<String>>();
    List<String> currentPart = new ArrayList<String>();
    boolean isEndReached = false;

    for (String currentLine : message) {
      if (currentLine.contains("--" + boundary + "--")) {
        removeEndingCRLFFromList(currentPart);
        messageParts.add(currentPart);
        isEndReached = true;
      } else if (currentLine.contains("--" + boundary)) {
        removeEndingCRLFFromList(currentPart);
        messageParts.add(currentPart);
        currentPart = new LinkedList<String>();
      } else {
        currentPart.add(currentLine);
      }

      if (isEndReached) {
        break;
      }
    }

    // Remove preamble
    if (messageParts.size() > 0) {
      messageParts.remove(0);
    } else {
      throw new BatchException(BatchException.MISSING_BOUNDARY_DELIMITER);
    }

    if (messageParts.size() == 0) {
      throw new BatchException(BatchException.NO_MATCH_WITH_BOUNDARY_STRING);
    }

    if (!isEndReached) {
      throw new BatchException(BatchException.MISSING_CLOSE_DELIMITER);
    }

    return messageParts;
  }

  private static void removeEndingCRLFFromList(final List<String> list) {
    if (list.size() > 0) {
      String lastLine = list.remove(list.size() - 1);
      list.add(removeEndingCRLF(lastLine));
    }
  }

  public static String removeEndingCRLF(final String line) {
    Pattern pattern = Pattern.compile("(.*)(\r\n){1}( *)", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(line);

    if (matcher.matches()) {
      return matcher.group(1);
    } else {
      return line;
    }
  }

  public static Map<String, HeaderField> consumeHeaders(final List<String> remainingMessage) throws BatchException {
    final Map<String, HeaderField> headers = new HashMap<String, HeaderField>();
    boolean isHeader = true;
    final Iterator<String> iter = remainingMessage.iterator();
    final AcceptParser acceptParser = new AcceptParser();
    String currentLine;

    while (iter.hasNext() && isHeader) {
      currentLine = iter.next();
      Matcher headerMatcher = PATTERN_HEADER_LINE.matcher(currentLine);

      if (headerMatcher.matches() && headerMatcher.groupCount() == 2) {
        iter.remove();

        String headerName = headerMatcher.group(1).trim();
        String headerNameLowerCase = headerName.toLowerCase(Locale.ENGLISH);
        String headerValue = headerMatcher.group(2).trim();

        if (HttpHeaders.ACCEPT.equalsIgnoreCase(headerNameLowerCase)) {
          acceptParser.addAcceptHeaderValue(headerValue);
        } else if (HttpHeaders.ACCEPT_LANGUAGE.equalsIgnoreCase(headerNameLowerCase)) {
          acceptParser.addAcceptLanguageHeaderValue(headerValue);
        } else {
          addHeaderValue(headers, headerName, headerNameLowerCase, headerValue);
        }
      } else {
        isHeader = false;
      }
    }

    final List<String> acceptHeader = acceptParser.parseAcceptHeaders();
    headers.put(HttpHeaders.ACCEPT.toLowerCase(), new HeaderField(HttpHeaders.ACCEPT, acceptHeader));

    final List<String> acceptLanguageHeader = acceptParser.parseAcceptableLanguages();
    headers.put(HttpHeaders.ACCEPT_LANGUAGE.toLowerCase(), new HeaderField(HttpHeaders.ACCEPT_LANGUAGE,
        acceptLanguageHeader));

    return Collections.unmodifiableMap(headers);
  }

  private static void addHeaderValue(final Map<String, HeaderField> headers, final String headerName,
      final String headerNameLowerCase, final String headerValue) {
    HeaderField headerField = headers.get(headerNameLowerCase);
    headerField = headerField == null ? new HeaderField(headerName) : headerField;
    headers.put(headerNameLowerCase, headerField);

    for (final String singleValue : splitHeaderValuesByComma(headerValue)) {
      if (!headerField.getValues().contains(singleValue)) {
        headerField.getValues().add(singleValue);
      }
    }
  }

  private static List<String> splitHeaderValuesByComma(final String headerValue) {
    final List<String> singleValues = new ArrayList<String>();

    String[] parts = headerValue.split(",");
    for (final String value : parts) {
      singleValues.add(value.trim());
    }

    return singleValues;
  }

  public static void consumeBlankLine(final List<String> remainingMessage, final boolean isStrict)
      throws BatchException {
    if (remainingMessage.size() > 0 && "".equals(remainingMessage.get(0).trim())) {
      remainingMessage.remove(0);
    } else {
      if (isStrict) {
        throw new BatchException(BatchException.MISSING_BLANK_LINE);
      }
    }
  }

  public static String getBoundary(final String contentType) throws BatchException {
    final Matcher boundaryMatcher = PATTERN_MULTIPART_BOUNDARY.matcher(contentType);

    if (boundaryMatcher.matches()) {
      final String boundary = boundaryMatcher.group(1);
      if (boundary.matches(REG_EX_BOUNDARY)) {
        return trimQuota(boundary);
      } else {
        throw new BatchException(BatchException.INVALID_BOUNDARY);
      }
    } else {
      throw new BatchException(BatchException.INVALID_CONTENT_TYPE.addContent(HttpContentType.MULTIPART_MIXED));
    }
  }

  public static Map<String, List<String>> parseQueryParameter(final String httpRequest) {
    Map<String, List<String>> queryParameter = new HashMap<String, List<String>>();

    String[] requestParts = httpRequest.split(" ");
    if (requestParts.length == 3) {

      String[] parts = requestParts[1].split("\\?");
      if (parts.length == 2) {
        String[] parameters = parts[1].split("&");

        for (String parameter : parameters) {
          String[] parameterParts = parameter.split("=");
          String parameterName = parameterParts[0].toLowerCase(Locale.ENGLISH);

          if (parameterParts.length == 2) {
            List<String> valueList = queryParameter.get(parameterName);
            valueList = valueList == null ? new LinkedList<String>() : valueList;
            queryParameter.put(parameterName, valueList);

            String[] valueParts = parameterParts[1].split(",");
            for (String value : valueParts) {
              valueList.add(Decoder.decode(value));
            }
          }
        }
      }
    }

    return queryParameter;
  }

  public static PathInfo parseRequestUri(final String httpRequest, final PathInfo batchRequestPathInfo,
      final String baseUri)
      throws BatchException {

    final String odataPathSegmentsAsString;
    final String queryParametersAsString;

    PathInfoImpl pathInfo = new PathInfoImpl();
    pathInfo.setServiceRoot(batchRequestPathInfo.getServiceRoot());
    pathInfo.setPrecedingPathSegment(batchRequestPathInfo.getPrecedingSegments());

    String[] requestParts = httpRequest.split(" ");
    if (requestParts.length == 3) {
      String uri = requestParts[1];
      Pattern regexRequestUri;

      try {
        URI uriObject = new URI(uri);
        if (uriObject.isAbsolute()) {
          regexRequestUri = Pattern.compile(baseUri + "/([^/][^?]*)(\\?.*)?");
        } else {
          regexRequestUri = Pattern.compile("([^/][^?]*)(\\?.*)?");

        }

        Matcher uriParts = regexRequestUri.matcher(uri);

        if (uriParts.lookingAt() && uriParts.groupCount() == 2) {
          odataPathSegmentsAsString = uriParts.group(1);
          queryParametersAsString = uriParts.group(2) != null ? uriParts.group(2) : "";

          pathInfo.setODataPathSegment(parseODataPathSegments(odataPathSegmentsAsString));
          if (!odataPathSegmentsAsString.startsWith("$")) {
            String requestUri = baseUri + "/" + odataPathSegmentsAsString + queryParametersAsString;
            pathInfo.setRequestUri(new URI(requestUri));
          }

        } else {
          throw new BatchException(BatchException.INVALID_URI);
        }

      } catch (URISyntaxException e) {
        throw new BatchException(BatchException.INVALID_URI, e);
      }
    } else {
      throw new BatchException(BatchException.INVALID_REQUEST_LINE);
    }

    return pathInfo;
  }

  public static List<PathSegment> parseODataPathSegments(final String odataPathSegmentsAsString) {
    final List<PathSegment> odataPathSegments = new ArrayList<PathSegment>();
    final String[] pathParts = odataPathSegmentsAsString.split("/");

    for (final String pathSegment : pathParts) {
      odataPathSegments.add(new ODataPathSegmentImpl(pathSegment, null));
    }

    return odataPathSegments;
  }

  private static String trimQuota(String boundary) {
    if (boundary.matches("\".*\"")) {
      boundary = boundary.replace("\"", "");
    }

    return boundary;
  }

  public static Map<String, String> headerFieldMapToSingleMap(final Map<String, HeaderField> headers) {
    final Map<String, String> singleMap = new HashMap<String, String>();

    for (final String key : headers.keySet()) {
      HeaderField field = headers.get(key);
      String value = field.getValues().size() > 0 ? field.getValues().get(0) : "";
      singleMap.put(field.getFieldName(), value);
    }

    return singleMap;
  }

  public static Map<String, List<String>> headerFieldMapToMultiMap(final Map<String, HeaderField> headers) {
    final Map<String, List<String>> singleMap = new HashMap<String, List<String>>();

    for (final String key : headers.keySet()) {
      HeaderField field = headers.get(key);
      singleMap.put(field.getFieldName(), field.getValues());
    }

    return singleMap;
  }

  public static class HeaderField implements Cloneable {
    private String fieldName;
    private List<String> values;

    public HeaderField(final String fieldName) {
      this(fieldName, new ArrayList<String>());
    }

    public HeaderField(final String fieldName, final List<String> values) {
      this.fieldName = fieldName;
      this.values = values;
    }

    public String getFieldName() {
      return fieldName;
    }

    public List<String> getValues() {
      return values;
    }

    public void setValues(final List<String> values) {
      this.values = values;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      HeaderField other = (HeaderField) obj;
      if (fieldName == null) {
        if (other.fieldName != null) {
          return false;
        }
      } else if (!fieldName.equals(other.fieldName)) {
        return false;
      }
      return true;
    }

    @Override
    public HeaderField clone() {
      List<String> newValues = new ArrayList<String>();
      newValues.addAll(values);

      return new HeaderField(fieldName, newValues);
    }
  }
}
