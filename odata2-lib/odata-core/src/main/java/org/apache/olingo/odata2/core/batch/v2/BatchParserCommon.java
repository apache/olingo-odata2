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
import java.nio.charset.Charset;
import java.util.ArrayList;
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
import org.apache.olingo.odata2.core.batch.AcceptParser;
import org.apache.olingo.odata2.core.batch.BatchHelper;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.Decoder;

public class BatchParserCommon {

  private static final Pattern PATTERN_LAST_CRLF = Pattern.compile("(.*)(\r\n){1}( *)", Pattern.DOTALL);

  // Multipart boundaries are defined in RFC 2046:
  //     boundary      := 0*69<bchars> bcharsnospace
  //     bchars        := bcharsnospace / " "
  //     bcharsnospace := DIGIT / ALPHA / "'" / "(" / ")" / "+" / "_" / "," / "-" / "." / "/" / ":" / "=" / "?"
  // The first alternative is for the case that only characters are used that don't need quoting.
  private static final Pattern PATTERN_BOUNDARY = Pattern.compile(
      "((?:\\w|[-.'+]){1,70})|"
          + "\"((?:\\w|[-.'+(),/:=?]|\\s){0,69}(?:\\w|[-.'+(),/:=?]))\"");

  // HTTP header fields are defined in RFC 7230:
  //     header-field   = field-name ":" OWS field-value OWS
  //     field-name     = token
  //     field-value    = *( field-content / obs-fold )
  //     field-content  = field-vchar [ 1*( SP / HTAB ) field-vchar ]
  //     field-vchar    = VCHAR / obs-text
  //     obs-fold       = CRLF 1*( SP / HTAB )
  //     token          = 1*tchar
  //     tchar          = "!" / "#" / "$" / "%" / "&" / "'" / "*" / "+" / "-" / "." / "^" / "_" / "`" / "|" / "~"
  //                      / DIGIT / ALPHA
  // For the field-name the specification is followed strictly,
  // but for the field-value the pattern currently accepts more than specified.
  protected static final Pattern PATTERN_HEADER_LINE = Pattern.compile("((?:\\w|[!#$%\\&'*+\\-.^`|~])+):\\s?(.*)\\s*");

  public static final Pattern PATTERN_MULTIPART_MIXED = Pattern.compile("multipart/mixed(.*)",
      Pattern.CASE_INSENSITIVE);
  public static final Pattern PATTERN_CONTENT_TYPE_APPLICATION_HTTP = Pattern.compile("application/http",
      Pattern.CASE_INSENSITIVE);
  public static final Pattern PATTERN_RELATIVE_URI = Pattern.compile("([^/][^?]*)(\\?.*)?");

  private BatchParserCommon() {
    
  }
  
  public static String trimLineListToLength(final List<Line> list, final int length) {
    final String message = lineListToString(list);
    final int lastIndex = Math.min(length, message.length());

    return (lastIndex > 0) ? message.substring(0, lastIndex) : "";
  }

  public static String lineListToString(final List<Line> list) {
    StringBuilder builder = new StringBuilder();

    for (Line currentLine : list) {
      builder.append(currentLine.toString());
    }

    return builder.toString();
  }

  /**
   * Convert body in form of List of Line items into a InputStream.
   * The body is transformed with the charset set in ContentType and
   * if no charset is set with Olingo default charset (see <code>BatchHelper.DEFAULT_CHARSET</code>).
   *
   * If content length is a positive value the content is trimmed to according length.
   * Otherwise the whole content is written into the InputStream.
   *
   * @param contentType content type value
   * @param operation which is written into the InputStream
   * @param contentLength if it is a positive value the content is trimmed to according length.
   *                      Otherwise the whole content is written into the InputStream.
   * @return Content of BatchQueryOperation as InputStream in according charset and length
   * @throws BatchException if something goes wrong
   */
  public static InputStream convertToInputStream(final String contentType, final BatchQueryOperation operation,
                                                 final int contentLength)
      throws BatchException {
    Charset charset = BatchHelper.extractCharset(ContentType.parse(
        contentType));
    final String message;
    if(contentLength <= -1) {
      message = lineListToString(operation.getBody());
    } else {
      message = trimLineListToLength(operation.getBody(), contentLength);
    }
    return new ByteArrayInputStream(message.getBytes(charset));
  }

  static List<List<Line>> splitMessageByBoundary(final List<Line> message, final String boundary)
      throws BatchException {
    final List<List<Line>> messageParts = new LinkedList<List<Line>>();
    List<Line> currentPart = new ArrayList<Line>();
    boolean isEndReached = false;

    final String quotedBoundary = Pattern.quote(boundary);
    final Pattern boundaryDelimiterPattern = Pattern.compile("--" + quotedBoundary + "--[\\s ]*");
    final Pattern boundaryPattern = Pattern.compile("--" + quotedBoundary + "[\\s ]*");

    for (Line currentLine : message) {
      if (boundaryDelimiterPattern.matcher(currentLine.toString()).matches()) {
        removeEndingCRLFFromList(currentPart);
        messageParts.add(currentPart);
        isEndReached = true;
      } else if (boundaryPattern.matcher(currentLine.toString()).matches()) {
        removeEndingCRLFFromList(currentPart);
        messageParts.add(currentPart);
        currentPart = new LinkedList<Line>();
      } else {
        currentPart.add(currentLine);
      }

      if (isEndReached) {
        break;
      }
    }

    final int lineNumber = (!message.isEmpty()) ? message.get(0).getLineNumber() : 0;
    // Remove preamble
    if (!messageParts.isEmpty()) {
      messageParts.remove(0);
    } else {

      throw new BatchException(BatchException.MISSING_BOUNDARY_DELIMITER.addContent(lineNumber));
    }

    if (!isEndReached) {
      throw new BatchException(BatchException.MISSING_CLOSE_DELIMITER.addContent(lineNumber));
    }

    if (messageParts.isEmpty()) {
      throw new BatchException(BatchException.NO_MATCH_WITH_BOUNDARY_STRING
          .addContent(boundary).addContent(lineNumber));
    }

    return messageParts;
  }

  private static void removeEndingCRLFFromList(final List<Line> list) {
    if (!list.isEmpty()) {
      Line lastLine = list.remove(list.size() - 1);
      list.add(removeEndingCRLF(lastLine));
    }
  }

  public static Line removeEndingCRLF(final Line line) {
    Pattern pattern = PATTERN_LAST_CRLF;
    Matcher matcher = pattern.matcher(line.toString());

    if (matcher.matches()) {
      return new Line(matcher.group(1), line.getLineNumber());
    } else {
      return line;
    }
  }

  public static Header consumeHeaders(final List<Line> remainingMessage) throws BatchException {
    final int headerLineNumber = !remainingMessage.isEmpty() ? remainingMessage.get(0).getLineNumber() : 0;
    final Header headers = new Header(headerLineNumber);
    final Iterator<Line> iter = remainingMessage.iterator();
    final AcceptParser acceptParser = new AcceptParser();
    Line currentLine;
    int acceptLineNumber = 0;
    int acceptLanguageLineNumber = 0;
    boolean isHeader = true;

    while (iter.hasNext() && isHeader) {
      currentLine = iter.next();
      final Matcher headerMatcher = PATTERN_HEADER_LINE.matcher(currentLine.toString());

      if (headerMatcher.matches() && headerMatcher.groupCount() == 2) {
        iter.remove();

        String headerName = headerMatcher.group(1).trim();
        String headerValue = headerMatcher.group(2).trim();

        if (HttpHeaders.ACCEPT.equalsIgnoreCase(headerName)) {
          acceptParser.addAcceptHeaderValue(headerValue);
          acceptLineNumber = currentLine.getLineNumber();
        } else if (HttpHeaders.ACCEPT_LANGUAGE.equalsIgnoreCase(headerName)) {
          acceptParser.addAcceptLanguageHeaderValue(headerValue);
          acceptLanguageLineNumber = currentLine.getLineNumber();
        } else {
          headers.addHeader(headerName, Header.splitValuesByComma(headerValue), currentLine.getLineNumber());
        }
      } else {
        isHeader = false;
      }
    }

    headers.addHeader(HttpHeaders.ACCEPT, acceptParser.parseAcceptHeaders(), acceptLineNumber);
    headers.addHeader(HttpHeaders.ACCEPT_LANGUAGE, acceptParser.parseAcceptableLanguages(), acceptLanguageLineNumber);

    return headers;
  }

  public static void consumeBlankLine(final List<Line> remainingMessage, final boolean isStrict)
      throws BatchException {
    if (!remainingMessage.isEmpty() && remainingMessage.get(0).toString().matches("\\s*\r\n\\s*")) {
      remainingMessage.remove(0);
    } else {
      if (isStrict) {
        final int lineNumber = (!remainingMessage.isEmpty()) ? remainingMessage.get(0).getLineNumber() : 0;
        throw new BatchException(BatchException.MISSING_BLANK_LINE.addContent("[None]").addContent(lineNumber));
      }
    }
  }

  public static String getBoundary(final String contentType, final int line) throws BatchException {
    if (contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/mixed")) {
      final String[] parameter = contentType.split(";");

      for (final String pair : parameter) {

        final String[] attrValue = pair.split("=");
        if (attrValue.length == 2 && "boundary".equals(attrValue[0].trim().toLowerCase(Locale.ENGLISH))) {
          if (PATTERN_BOUNDARY.matcher(attrValue[1]).matches()) {
            return trimQuota(attrValue[1].trim());
          } else {
            throw new BatchException(BatchException.INVALID_BOUNDARY.addContent(line));
          }
        }

      }
    }
    throw new BatchException(BatchException.INVALID_CONTENT_TYPE.addContent(HttpContentType.MULTIPART_MIXED));
  }

  private static String trimQuota(String boundary) {
    if (boundary.matches("\".*\"")) {
      boundary = boundary.replace("\"", "");
    }

    return boundary;
  }

  public static Map<String, List<String>> parseQueryParameter(final Line httpRequest) {
    Map<String, List<String>> queryParameter = new HashMap<String, List<String>>();

    String[] requestParts = httpRequest.toString().split(" ");
    if (requestParts.length == 3) {

      String[] parts = requestParts[1].split("\\?");
      if (parts.length == 2) {
        String[] parameters = parts[1].split("&");

        for (String parameter : parameters) {
          String[] parameterParts = parameter.split("=");
          String parameterName = parameterParts[0];

          if (parameterParts.length == 2) {
            List<String> valueList = queryParameter.get(parameterName);
            valueList = valueList == null ? new LinkedList<String>() : valueList;
            queryParameter.put(parameterName, valueList);
            valueList.add(Decoder.decode(parameterParts[1]));
          }
        }
      }
    }

    return queryParameter;
  }
}
