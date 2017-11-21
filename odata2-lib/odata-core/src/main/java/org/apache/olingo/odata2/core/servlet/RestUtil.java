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
package org.apache.olingo.odata2.core.servlet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.exception.ODataUnsupportedMediaTypeException;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.Decoder;

public class RestUtil {
  // RFC 2616, 4.2: linear white space
  private static final String REG_EX_OPTIONAL_WHITESPACE = "\\s*";
  private static final String REG_EX_FIELD_VALUE_SEPARATOR = "," + REG_EX_OPTIONAL_WHITESPACE;

  // RFC 2616, 3.9: qvalue = ("0"["." 0*3DIGIT]) | ("1"["." 0*3("0")])
  private static final String REG_EX_QVALUE = "q=((?:1(?:\\.0{0,3})?)|(?:0(?:\\.[0-9]{0,3})?))";

  // RFC 2616, 14.1: the media-range parameters
  private static final String REG_EX_PARAMETER = "(?:;\\s*(?:(?:[^qQ].*)|(?:[qQ]\\s*=\\s*(?:[^01].*))))*";
  private static final Pattern REG_EX_ACCEPT =
      Pattern.compile("([a-z\\*\\s]+/[a-zA-Z\\+\\*\\-=\\s]+" + REG_EX_PARAMETER + ")");
  private static final Pattern REG_EX_ACCEPT_WITH_Q_FACTOR =
      Pattern.compile(REG_EX_ACCEPT + "(?:" + REG_EX_OPTIONAL_WHITESPACE + REG_EX_QVALUE + ")?");

  // RFC 2616, 14.4: language-range = ((1*8ALPHA *("-" 1*8ALPHA)) | "*")
  private static final Pattern REG_EX_ACCEPT_LANGUAGES =
      Pattern.compile("((?:\\*)|(?:[a-z]{1,8}(?:\\-[a-zA-Z]{1,8})?))");
  private static final Pattern REG_EX_ACCEPT_LANGUAGES_WITH_Q_FACTOR =
      Pattern.compile(REG_EX_ACCEPT_LANGUAGES + "(?:;" + REG_EX_OPTIONAL_WHITESPACE + REG_EX_QVALUE + ")?");

  private static final Pattern REG_EX_MATRIX_PARAMETER = Pattern.compile("([^=]*)(?:=(.*))?");
  private static final String ACCEPT_FORM_ENCODING = "odata-accept-forms-encoding";

  public static ContentType extractRequestContentType(final String contentType)
      throws ODataUnsupportedMediaTypeException {
    if (contentType == null || contentType.isEmpty()) {
      // RFC 2616, 7.2.1:
      // "Any HTTP/1.1 message containing an entity-body SHOULD include a
      // Content-Type header field defining the media type of that body. [...]
      // If the media type remains unknown, the recipient SHOULD treat it
      // as type "application/octet-stream"."
      return ContentType.APPLICATION_OCTET_STREAM;
    } else if (ContentType.isParseable(contentType)) {
      return ContentType.create(contentType);
    } else {
      throw new ODataUnsupportedMediaTypeException(
          ODataUnsupportedMediaTypeException.NOT_SUPPORTED_CONTENT_TYPE.addContent(contentType));
    }
  }

  /*
   * Parses query parameters.
   */
  public static Map<String, String> extractQueryParameters(final String queryString) {
    Map<String, String> queryParametersMap = new HashMap<String, String>();
    if (queryString != null && queryString.length() > 0) {
      // At first the queryString will be decoded.
      List<String> queryParameters = Arrays.asList(queryString.split("\\&"));
      for (String param : queryParameters) {
        String decodedParam = Decoder.decode(param);
        int indexOfEqualSign = decodedParam.indexOf("=");
        if (indexOfEqualSign < 0) {
          queryParametersMap.put(decodedParam, "");
        } else {
          queryParametersMap.put(decodedParam.substring(0, indexOfEqualSign), decodedParam
              .substring(indexOfEqualSign + 1));
        }
      }
    }
    return queryParametersMap;
  }

  public static Map<String, List<String>> extractAllQueryParameters(final String queryString, String formEncoding) {
    Map<String, List<String>> allQueryParameterMap = new HashMap<String, List<String>>();
    if(Boolean.parseBoolean(formEncoding)){
      List<String> encoding = new ArrayList<String>();
      encoding.add(formEncoding);
      allQueryParameterMap.put(ACCEPT_FORM_ENCODING, encoding );
    }
    if (queryString != null && queryString.length() > 0) {
      // At first the queryString will be decoded.
      List<String> queryParameters = Arrays.asList(queryString.split("\\&"));
      for (String param : queryParameters) {
        String decodedParam = Decoder.decode(param);
        int indexOfEqualSign = decodedParam.indexOf("=");

        if (indexOfEqualSign < 0) {
          final List<String> parameterList =
              allQueryParameterMap.containsKey(decodedParam) ? allQueryParameterMap.get(decodedParam)
                  : new LinkedList<String>();
          allQueryParameterMap.put(decodedParam, parameterList);

          parameterList.add("");
        } else {
          final String key = decodedParam.substring(0, indexOfEqualSign);
          final List<String> parameterList = allQueryParameterMap.containsKey(key) ? allQueryParameterMap.get(key)
              : new LinkedList<String>();

          allQueryParameterMap.put(key, parameterList);
          parameterList.add(decodedParam.substring(indexOfEqualSign + 1));
        }
      }
    }
    return allQueryParameterMap;
  }

  /*
   * Parses Accept-Language header. Returns a list sorted by quality parameter
   */
  public static List<Locale> extractAcceptableLanguage(final String acceptableLanguageHeader) {
    List<Locale> acceptLanguages = new ArrayList<Locale>();
    TreeSet<Accept> acceptTree = getAcceptTree();
    if (acceptableLanguageHeader != null && !acceptableLanguageHeader.isEmpty()) {
      List<String> list = Arrays.asList(acceptableLanguageHeader.split(REG_EX_FIELD_VALUE_SEPARATOR));
      for (String acceptLanguage : list) {
        Matcher matcher = REG_EX_ACCEPT_LANGUAGES_WITH_Q_FACTOR.matcher(acceptLanguage);
        if (matcher.find()) {
          String language = matcher.group(1);
          double qualityFactor = matcher.group(2) != null ? Double.parseDouble(matcher.group(2)) : 1d;
          acceptTree.add(new Accept(language, qualityFactor));
        }
      }
    }
    for (Accept accept : acceptTree) {
      String languageRange = accept.getValue();
      // The languageRange has to be splitted in language tag and country tag
      int indexOfMinus = languageRange.indexOf("-");
      Locale locale;
      if (indexOfMinus < 0) {
        // no country tag
        locale = new Locale(languageRange);
      } else {
        String language = languageRange.substring(0, indexOfMinus);
        String country = languageRange.substring(indexOfMinus + 1);
        locale = new Locale(language, country);
      }
      acceptLanguages.add(locale);
    }
    return acceptLanguages;
  }

  /*
   * Parses Accept header. Returns a list of media ranges sorted by quality parameter
   */
  public static List<String> extractAcceptHeaders(final String acceptHeader) {
    TreeSet<Accept> acceptTree = getAcceptTree();
    List<String> acceptHeaders = new ArrayList<String>();
    if (acceptHeader != null && !acceptHeader.isEmpty()) {
      List<String> list = Arrays.asList(acceptHeader.split(REG_EX_FIELD_VALUE_SEPARATOR));
      for (String accept : list) {
        Matcher matcher = REG_EX_ACCEPT_WITH_Q_FACTOR.matcher(accept);
        if (matcher.find()) {
          String headerValue = matcher.group(1);
          double qualityFactor = matcher.group(2) != null ? Double.parseDouble(matcher.group(2)) : 1d;
          acceptTree.add(new Accept(headerValue, qualityFactor));
        }
      }
    }
    for (Accept accept : acceptTree) {
      acceptHeaders.add(accept.getValue());
    }
    return acceptHeaders;
  }

  @SuppressWarnings("unchecked")
  public static Map<String, List<String>> extractHeaders(final HttpServletRequest req) {
    Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
    for (Enumeration<String> headerNames = req.getHeaderNames(); headerNames.hasMoreElements();) {
      String headerName = headerNames.nextElement();
      List<String> headerValues = new ArrayList<String>();
      for (Enumeration<String> headers = req.getHeaders(headerName); headers.hasMoreElements();) {
        String value = headers.nextElement();
        headerValues.add(value);
      }
      if (requestHeaders.containsKey(headerName)) {
        requestHeaders.get(headerName).addAll(headerValues);
      } else {
        requestHeaders.put(headerName, headerValues);
      }
    }
    return requestHeaders;
  }

  public static PathInfo buildODataPathInfo(final HttpServletRequest req, final int pathSplit) throws ODataException {
    PathInfoImpl pathInfo = splitPath(req, pathSplit);

    pathInfo.setServiceRoot(buildBaseUri(req, pathInfo.getPrecedingSegments()));
    pathInfo.setRequestUri(buildRequestUri(req));
    return pathInfo;
  }

  private static URI buildBaseUri(final HttpServletRequest req, final List<PathSegment> precedingPathSegments)
      throws ODataException {
    try {
      URI baseUri;
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(req.getContextPath()).append(req.getServletPath());
      for (final PathSegment ps : precedingPathSegments) {
        if (!"".equals(ps.getPath()) && ps.getPath().length() > 0) {
          stringBuilder.append("/").append(ps.getPath());
        }
        for (final String key : ps.getMatrixParameters().keySet()) {
          List<String> matrixParameters = ps.getMatrixParameters().get(key);
          String matrixParameterString = ";" + key + "=";
          for (String matrixParam : matrixParameters) {
            matrixParameterString += Decoder.decode(matrixParam) + ",";
          }
          stringBuilder.append(matrixParameterString.substring(0, matrixParameterString.length() - 1));
        }
      }

      String path = stringBuilder.toString();
      if (!path.endsWith("/")) {
        path = path + "/";
      }
      baseUri = new URI(req.getScheme(), null, req.getServerName(), req.getServerPort(), path, null, null);
      return baseUri;
    } catch (final URISyntaxException e) {
      throw new ODataException(e);
    }
  }

  private static URI buildRequestUri(final HttpServletRequest servletRequest) {
    URI requestUri;
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(servletRequest.getRequestURL());
    String queryString = servletRequest.getQueryString();

    if (queryString != null) {
      stringBuilder.append("?").append(queryString);
    }

    String requestUriString = stringBuilder.toString();
    requestUri = URI.create(requestUriString);
    return requestUri;
  }

  private static PathInfoImpl splitPath(final HttpServletRequest servletRequest, final int pathSplit)
      throws ODataException {
    PathInfoImpl pathInfo = new PathInfoImpl();
    List<String> precedingPathSegments;
    List<String> pathSegments;

    String pathInfoString = extractPathInfo(servletRequest);
    while (pathInfoString.startsWith("/")) {
      pathInfoString = pathInfoString.substring(1);
    }
    List<String> segments = null;
    // EmptyStrings have to result in an empty list.
    // Since split will always deliver an empty string back we have to do this manually
    if (pathInfoString.isEmpty()) {
      segments = new ArrayList<String>();
    } else {
      segments = Arrays.asList(pathInfoString.split("/", -1));
    }

    if (pathSplit == 0) {
      precedingPathSegments = Collections.emptyList();
      pathSegments = segments;
    } else {
      if (segments.size() < pathSplit) {
        throw new ODataBadRequestException(ODataBadRequestException.URLTOOSHORT);
      }

      precedingPathSegments = segments.subList(0, pathSplit);
      final int pathSegmentCount = segments.size();
      pathSegments = segments.subList(pathSplit, pathSegmentCount);
    }

    // Percent-decode only the preceding path segments.
    // The OData path segments are decoded during URI parsing.
    pathInfo.setPrecedingPathSegment(convertPathSegmentList(precedingPathSegments));

    List<PathSegment> odataSegments = new ArrayList<PathSegment>();
    for (final String segment : pathSegments) {

      int index = segment.indexOf(";");
      if (index < 0) {
        odataSegments.add(new ODataPathSegmentImpl(segment, null));
      } else {
        // post condition: we do not allow matrix parameters in OData path segments
        String path = segment.substring(0, index);
        Map<String, List<String>> parameterMap = extractMatrixParameter(segment, index);
        throw new ODataNotFoundException(ODataNotFoundException.MATRIX.addContent(parameterMap.keySet(), path));
      }
    }
    pathInfo.setODataPathSegment(odataSegments);
    return pathInfo;
  }

  private static List<PathSegment> convertPathSegmentList(final List<String> pathSegments) {
    ArrayList<PathSegment> converted = new ArrayList<PathSegment>();
    for (final String segment : pathSegments) {
      int index = segment.indexOf(";");
      if (index == -1) {
        converted.add(new ODataPathSegmentImpl(Decoder.decode(segment), null));
      } else {
        String path = segment.substring(0, index);
        Map<String, List<String>> parameterMap = extractMatrixParameter(segment, index);
        converted.add(new ODataPathSegmentImpl(Decoder.decode(path), parameterMap));
      }
    }
    return converted;
  }

  private static Map<String, List<String>> extractMatrixParameter(final String segment, final int index) {
    List<String> matrixParameters = Arrays.asList(segment.substring(index + 1).split(";"));
    String matrixParameterName = "";
    String matrixParamaterValues = "";
    Map<String, List<String>> parameterMap = new HashMap<String, List<String>>();

    for (String matrixParameter : matrixParameters) {
      List<String> values = Arrays.asList("");
      Matcher matcher = REG_EX_MATRIX_PARAMETER.matcher(matrixParameter);
      if (matcher.find()) {
        matrixParameterName = matcher.group(1);
        matrixParamaterValues = matcher.group(2);
      }
      if (matrixParamaterValues != null) {
        values = Arrays.asList(matrixParamaterValues.split(","));
      }
      parameterMap.put(matrixParameterName, values);
    }
    return parameterMap;
  }

  private static String extractPathInfo(final HttpServletRequest servletRequest) {
    String pathInfoString;
    final String requestUri = servletRequest.getRequestURI();
    pathInfoString = requestUri;
    int index = requestUri.indexOf(servletRequest.getContextPath());

    if (index >= 0) {
      pathInfoString = pathInfoString.substring(servletRequest.getContextPath().length());
    }

    int indexServletPath = pathInfoString.indexOf(servletRequest.getServletPath());
    if (indexServletPath >= 0) {
      int substringFromPos = indexServletPath + servletRequest.getServletPath().length();
      pathInfoString = pathInfoString.substring(substringFromPos);
    }
    return pathInfoString;
  }

  private static TreeSet<Accept> getAcceptTree() {
    TreeSet<Accept> treeSet = new TreeSet<Accept>(new Comparator<Accept>() {
      @Override
      public int compare(final Accept header1, final Accept header2) {
        if (header1.getQuality() <= header2.getQuality()) {
          return 1;
        } else {
          return -1;
        }
      }
    });
    return treeSet;
  }

  /*
   * The class is used in order to sort headers by "q" parameter.
   * The object of this class contains a value of the Accept header or Accept-Language header and value of the
   * quality parameter.
   */
  private static class Accept {
    private double quality;
    private String value;

    public Accept(final String headerValue, final double qualityFactor) {
      value = headerValue;
      quality = qualityFactor;
    }

    public String getValue() {
      return value;
    }

    public double getQuality() {
      return quality;
    }

  }

}
