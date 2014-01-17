package org.apache.olingo.odata2.fit.client.util;

import org.apache.olingo.odata2.api.commons.HttpStatusCodes;

public class HttpException extends Exception {

  private static final long serialVersionUID = 1L;

  private HttpStatusCodes httpStatusCode;

  public HttpException(HttpStatusCodes httpStatusCode, String message) {
    super(message);
    this.httpStatusCode = httpStatusCode;
  }

  public HttpStatusCodes getHttpStatusCode() {
    return httpStatusCode;
  }
}