package org.apache.olingo.odata2.core.servlet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;

import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataUnsupportedMediaTypeException;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.Decoder;

public class RestUtil {
  private static final String REG_EX_QUALITY_FACTOR = "q=((?:1\\.0{0,3})|(?:0\\.[0-9]{0,2}[1-9]))";
  private static final String REG_EX_OPTIONAL_WHITESPACE = "\\s?";
  private static final Pattern REG_EX_ACCEPT = Pattern.compile("((?:\\*)|(?:[a-z\\*\\s]+/[a-zA-Z0-9\\+\\*\\-=;\\s]+))");
  private static final Pattern REG_EX_ACCEPT_WITH_Q_FACTOR = Pattern.compile(REG_EX_ACCEPT + "(?:;"
      + REG_EX_OPTIONAL_WHITESPACE + REG_EX_QUALITY_FACTOR + ")?");
  private static final Pattern REG_EX_ACCEPT_LANGUAGES = Pattern
      .compile("((?:[a-z]{1,8})|(?:\\*))\\-?([a-zA-Z]{1,8})?");
  private static final Pattern REG_EX_ACCEPT_LANGUAGES_WITH_Q_FACTOR = Pattern.compile(REG_EX_ACCEPT_LANGUAGES + "(?:;"
      + REG_EX_OPTIONAL_WHITESPACE + REG_EX_QUALITY_FACTOR + ")?");

  public static List<String> extractAcceptHeaders(final String header) {
    List<String> acceptHeaders = new ArrayList<String>();
    if (header != null && !header.isEmpty()) {
      Scanner acceptHeaderScanner = new Scanner(header).useDelimiter(",\\s?");
      while (acceptHeaderScanner.hasNext()) {
        if (acceptHeaderScanner.hasNext(REG_EX_ACCEPT_WITH_Q_FACTOR)) {
          acceptHeaderScanner.next(REG_EX_ACCEPT_WITH_Q_FACTOR);
          MatchResult result = acceptHeaderScanner.match();
          acceptHeaders.add(result.group(1));
        }
      }
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
        String val = headers.nextElement();
        headerValues.add(val);
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
      String path = req.getContextPath() + req.getServletPath();
      UriBuilder uriBuilder = UriBuilder.fromUri(path);
      for (final PathSegment ps : precedingPathSegments) {
        uriBuilder = uriBuilder.path(ps.getPath());
        for (final String key : ps.getMatrixParameters().keySet()) {
          final Object[] v = ps.getMatrixParameters().get(key).toArray();
          uriBuilder = uriBuilder.matrixParam(key, v);
        }
      }

      /*
       * workaround because of host name is cached by uriInfo
       */
      uriBuilder.host(req.getServerName()).port(req.getServerPort());
      uriBuilder.scheme(req.getScheme());

      String uriString = uriBuilder.build().toString();
      if (!uriString.endsWith("/")) {
        uriString = uriString + "/";
      }

      return new URI(uriString);
    } catch (final URISyntaxException e) {
      throw new ODataException(e);
    }
  }

  private static URI buildRequestUri(final HttpServletRequest servletRequest) {
    URI requestUri;

    StringBuffer buf = servletRequest.getRequestURL();
    String queryString = servletRequest.getQueryString();

    if (queryString != null) {
      buf.append("?");
      buf.append(queryString);
    }

    String requestUriString = buf.toString();

    requestUri = URI.create(requestUriString);
    return requestUri;
  }

  private static PathInfoImpl splitPath(final HttpServletRequest servletRequest, final int pathSplit)
      throws ODataException {
    /* String pathInfoString = servletRequest.getContextPath()+servletRequest.getServletPath()
     * +servletRequest.getPathInfo();*/
    //  String pathInfoString = servletRequest.getPathInfo();
    String pathInfoString = extractPathInfo(servletRequest);
    while (pathInfoString.startsWith("/")) {
      pathInfoString = pathInfoString.substring(1);
    }
    List<String> segments = Arrays.asList(pathInfoString.split("/"));
    PathInfoImpl pathInfo = new PathInfoImpl();

    List<String> precedingPathSegments;
    List<String> pathSegments;

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
      odataSegments.add(new ODataPathSegmentImpl(segment, null));
    }
    pathInfo.setODataPathSegment(odataSegments);

    return pathInfo;
  }

  private static String extractPathInfo(final HttpServletRequest servletRequest) {
    String pathInfoString;
    final String requestUri = servletRequest.getRequestURI();
    pathInfoString = requestUri;
    int index = requestUri.indexOf(servletRequest.getContextPath());

    if (index >= 0) {
      pathInfoString = pathInfoString.substring(servletRequest.getContextPath().length());
    }

    int indexServletPath = requestUri.indexOf(servletRequest.getServletPath());
    if (indexServletPath > 0) {
      pathInfoString = pathInfoString.substring(servletRequest.getServletPath().length());
    }
    return pathInfoString;
  }

  private static List<PathSegment> convertPathSegmentList(final List<String> pathSegments) {
    ArrayList<PathSegment> converted = new ArrayList<PathSegment>();
    for (final String pathSegment : pathSegments) {
      final PathSegment segment =
          new ODataPathSegmentImpl(Decoder.decode(pathSegment), null);
      converted.add(segment);
    }
    return converted;
  }

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

  public static Map<String, String> extractQueryParameters(final String queryString) {
    Map<String, String> queryParametersMap = new HashMap<String, String>();
    if (queryString != null) {
      List<String> qParameters = Arrays.asList(Decoder.decode(queryString).split("\\&"));
      for (String param : qParameters) {
        String[] p = param.split("=");
        queryParametersMap.put(p[0], p[1]);
      }
    }
    return queryParametersMap;
  }

  public static List<Locale> extractAcceptableLanguage(final String acceptableLanguageHeader) {
    List<Locale> acceptLanguages = new ArrayList<Locale>();
    if (acceptableLanguageHeader != null) {
      Scanner acceptLanguageScanner = new Scanner(acceptableLanguageHeader).useDelimiter(",\\s?");
      while (acceptLanguageScanner.hasNext()) {
        if (acceptLanguageScanner.hasNext(REG_EX_ACCEPT_LANGUAGES_WITH_Q_FACTOR)) {
          acceptLanguageScanner.next(REG_EX_ACCEPT_LANGUAGES_WITH_Q_FACTOR);
          MatchResult result = acceptLanguageScanner.match();
          String language = result.group(1);
          String country = result.group(2);
          //        //double qualityFactor = result.group(2) != null ? Double.parseDouble(result.group(2)) : 1d;
          if (country == null) {
            acceptLanguages.add(new Locale(language));
          } else {
            acceptLanguages.add(new Locale(language, country));
          }
        }
      }
      acceptLanguageScanner.close();
    }
    return acceptLanguages;
  }

}
