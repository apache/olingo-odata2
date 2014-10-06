package org.apache.olingo.odata2.core.batch.v2;

import java.util.List;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.core.batch.BatchHelper;
import org.apache.olingo.odata2.core.batch.v2.Header.HeaderField;

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
}
