package org.apache.olingo.odata2.core.batch.v2;

import java.util.Locale;
import java.util.Map;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.core.batch.BatchHelper;
import org.apache.olingo.odata2.core.batch.v2.BatchParserCommon.HeaderField;

public class BatchTransformatorCommon {

  public static void validateContentType(final Map<String, HeaderField> headers) throws BatchException {
    final HeaderField contentTypeField = headers.get(HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.ENGLISH));
    if (contentTypeField != null) {
      if (contentTypeField.getValues().size() == 1) {
        final String contentType = contentTypeField.getValues().get(0);

        if (!BatchParserCommon.PATTERN_MULTIPART_BOUNDARY.matcher(contentType).matches()
            && !BatchParserCommon.PATTERN_CONTENT_TYPE_APPLICATION_HTTP.matcher(contentType).matches()) {
          throw new BatchException(BatchException.INVALID_CONTENT_TYPE.addContent(HttpContentType.MULTIPART_MIXED
              + " or " + HttpContentType.APPLICATION_HTTP));
        }
      } else {
        throw new BatchException(BatchException.INVALID_HEADER);
      }
    } else {
      throw new BatchException(BatchException.MISSING_CONTENT_TYPE);
    }
  }

  public static void validateContentTransferEncoding(final Map<String, HeaderField> headers,
      final boolean isChangeRequest)
      throws BatchException {
    if (headers.containsKey(BatchHelper.HTTP_CONTENT_TRANSFER_ENCODING.toLowerCase(Locale.ENGLISH))) {
      HeaderField encodingField = headers.get(BatchHelper.HTTP_CONTENT_TRANSFER_ENCODING.toLowerCase(Locale.ENGLISH));

      if (encodingField.getValues().size() == 1) {
        String encoding = encodingField.getValues().get(0);

        if (!BatchHelper.BINARY_ENCODING.equalsIgnoreCase(encoding)) {
          throw new BatchException(BatchException.INVALID_CONTENT_TRANSFER_ENCODING);
        }
      } else if (encodingField.getValues().size() == 0) {
        throw new BatchException(BatchException.INVALID_CONTENT_TRANSFER_ENCODING);
      } else {
        throw new BatchException(BatchException.INVALID_HEADER);
      }
    } else {
      if (isChangeRequest) {
        throw new BatchException(BatchException.INVALID_CONTENT_TRANSFER_ENCODING);
      }
    }
  }

  public static int getContentLength(final Map<String, HeaderField> headers) throws BatchException {

    if (headers.containsKey(HttpHeaders.CONTENT_LENGTH.toLowerCase(Locale.ENGLISH))) {
      try {
        int contentLength =
            Integer.parseInt(headers.get(HttpHeaders.CONTENT_LENGTH.toLowerCase(Locale.ENGLISH)).getValues().get(0));

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
