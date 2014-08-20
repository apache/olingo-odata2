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
  private static final String BOUNDARY_IDENTIFIER = "boundary=";
  private static final String REG_EX_BOUNDARY =
      "([a-zA-Z0-9_\\-\\.'\\+]{1,70})|\"([a-zA-Z0-9_\\-\\.'\\+\\s\\" +
          "(\\),/:=\\?]{1,69}[a-zA-Z0-9_\\-\\.'\\+\\(\\),/:=\\?])\""; // See RFC 2046

  private static final Pattern REG_EX_HEADER = Pattern.compile("([a-zA-Z\\-]+):\\s?(.*)\\s*");

  public static List<String> trimStringListToLength(final List<String> list, final int length) {
    final Iterator<String> iter = list.iterator();
    final List<String> result = new ArrayList<String>();
    boolean isEndReached = false;
    int currentLength = 0;

    while (!isEndReached && iter.hasNext()) {
      String currentLine = iter.next();

      if (currentLength + currentLine.length() <= length) {
        result.add(currentLine);
        currentLength += currentLine.length();
      } else {
        result.add(currentLine.substring(0, length - currentLength));
        isEndReached = true;
      }
    }

    return result;
  }

  public static String stringListToString(final List<String> list) {
    StringBuilder builder = new StringBuilder();

    for (String currentLine : list) {
      builder.append(currentLine);
    }

    return builder.toString();
  }

  public static InputStream convertMessageToInputStream(final List<String> message, final int contentLength)
      throws BatchException {
    List<String> shortenedMessage = BatchParserCommon.trimStringListToLength(message, contentLength);

    return new ByteArrayInputStream(BatchParserCommon.stringListToString(shortenedMessage).getBytes());
  }

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

  static Map<String, HeaderField> consumeHeaders(final List<String> remainingMessage) throws BatchException {
    final Map<String, HeaderField> headers = new HashMap<String, HeaderField>();
    boolean isHeader = true;
    String currentLine;
    Iterator<String> iter = remainingMessage.iterator();

    while (iter.hasNext() && isHeader) {
      currentLine = iter.next();
      Matcher headerMatcher = REG_EX_HEADER.matcher(currentLine);

      if (headerMatcher.matches() && headerMatcher.groupCount() == 2) {
        iter.remove();

        String headerName = headerMatcher.group(1).trim();
        String headerNameLowerCase = headerName.toLowerCase(Locale.ENGLISH);
        String headerValue = headerMatcher.group(2).trim();

        if (HttpHeaders.ACCEPT.equalsIgnoreCase(headerNameLowerCase)) {
          List<String> acceptHeaders = AcceptParser.parseAcceptHeaders(headerValue);
          headers.put(headerNameLowerCase, new HeaderField(headerName, acceptHeaders));
        } else if (HttpHeaders.ACCEPT_LANGUAGE.equalsIgnoreCase(headerNameLowerCase)) {
          List<String> acceptLanguageHeaders = AcceptParser.parseAcceptableLanguages(headerValue);
          headers.put(headerNameLowerCase, new HeaderField(headerName, acceptLanguageHeaders));
        } else {
          HeaderField headerField = headers.get(headerNameLowerCase);
          headerField = headerField == null ? new HeaderField(headerName) : headerField;
          headers.put(headerNameLowerCase, headerField);
          headerField.getValues().add(headerValue);
        }
      } else {
        isHeader = false;
      }
    }

    return Collections.unmodifiableMap(headers);
  }

  static void consumeBlankLine(final List<String> remainingMessage, final boolean isStrict) throws BatchException {
    if (remainingMessage.size() > 0 && "".equals(remainingMessage.get(0).trim())) {
      remainingMessage.remove(0);
    } else {
      if (isStrict) {
        throw new BatchException(BatchException.MISSING_BLANK_LINE);
      }
    }
  }

  static void consumeLastBlankLine(final List<String> message, final boolean isStrict) throws BatchException {
    if (message.size() > 0 && "".equals(message.get(message.size() - 1).trim())) {
      message.remove(message.size() - 1);
    } else {
      if (isStrict) {
        throw new BatchException(BatchException.MISSING_BLANK_LINE);
      }
    }
  }

  static String getBoundary(final String contentType) throws BatchException {
    if (contentType.contains(HttpContentType.MULTIPART_MIXED)) {
      String[] parts = contentType.split(BOUNDARY_IDENTIFIER);

      if (parts.length == 2) {
        if (parts[1].matches(REG_EX_BOUNDARY)) {
          return trimQuota(parts[1].trim());
        } else {
          throw new BatchException(BatchException.INVALID_BOUNDARY);
        }
      } else {
        throw new BatchException(BatchException.MISSING_PARAMETER_IN_CONTENT_TYPE);
      }
    } else {
      throw new BatchException(BatchException.INVALID_CONTENT_TYPE.addContent(HttpContentType.MULTIPART_MIXED));
    }
  }

  static Map<String, List<String>> parseQueryParameter(final String httpRequest) {
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
