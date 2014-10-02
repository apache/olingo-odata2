package org.apache.olingo.odata2.core.batch.v2;

import java.util.List;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.core.batch.BatchHelper;

public class BatchTransformatorCommon {

  public static void validateContentType(final Header headers) throws BatchException {
    List<String> contentTypes = headers.getHeaders(HttpHeaders.CONTENT_TYPE);

    if (contentTypes.size() == 0) {
      throw new BatchException(BatchException.MISSING_CONTENT_TYPE);
    }
    if (!headers.isHeaderMatching(HttpHeaders.CONTENT_TYPE, BatchParserCommon.PATTERN_MULTIPART_BOUNDARY)
      & !headers.isHeaderMatching(HttpHeaders.CONTENT_TYPE, BatchParserCommon.PATTERN_CONTENT_TYPE_APPLICATION_HTTP)) {
      throw new BatchException(BatchException.INVALID_CONTENT_TYPE.addContent(
          HttpContentType.MULTIPART_MIXED + " or " + HttpContentType.APPLICATION_HTTP));
    }
  }

  public static void validateContentTransferEncoding(final Header headers, final boolean isChangeRequest)
      throws BatchException {
    final List<String> contentTransferEncodings = headers.getHeaders(BatchHelper.HTTP_CONTENT_TRANSFER_ENCODING);

    if (contentTransferEncodings.size() != 0) {
      if (contentTransferEncodings.size() == 1) {
        String encoding = contentTransferEncodings.get(0);

        if (!BatchHelper.BINARY_ENCODING.equalsIgnoreCase(encoding)) {
          throw new BatchException(BatchException.INVALID_CONTENT_TRANSFER_ENCODING);
        }
      } else {
        throw new BatchException(BatchException.INVALID_HEADER);
      }
    } else {
      if (isChangeRequest) {
        throw new BatchException(BatchException.INVALID_CONTENT_TRANSFER_ENCODING);
      }
    }
  }

  public static int getContentLength(final Header headers) throws BatchException {
    final List<String> contentLengths = headers.getHeaders(HttpHeaders.CONTENT_LENGTH);

    if (contentLengths.size() == 1) {
      try {
        int contentLength = Integer.parseInt(contentLengths.get(0));

        if (contentLength < 0) {
          throw new BatchException(BatchException.INVALID_HEADER);
        }

        return contentLength;
      } catch (NumberFormatException e) {
        throw new BatchException(BatchException.INVALID_HEADER, e);
      }
    }

    return -1;
  }
}
