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
    if (headers.containsKey(HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.ENGLISH))) {
      HeaderField contentTypeField = headers.get(HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.ENGLISH));

      if (contentTypeField.getValues().size() == 1) {
        String contentType = contentTypeField.getValues().get(0);

        if (!(HttpContentType.APPLICATION_HTTP.equalsIgnoreCase(contentType)
        || contentType.contains(HttpContentType.MULTIPART_MIXED))) {

          throw new BatchException(BatchException.INVALID_CONTENT_TYPE.addContent(HttpContentType.MULTIPART_MIXED
              + " or " + HttpContentType.APPLICATION_HTTP));
        }
      } else if (contentTypeField.getValues().size() == 0) {
        throw new BatchException(BatchException.MISSING_CONTENT_TYPE);
      } else {
        throw new BatchException(BatchException.INVALID_HEADER);
      }
    } else {
      throw new BatchException(BatchException.MISSING_CONTENT_TYPE);
    }
  }

  public static void validateContentTransferEncoding(final Map<String, HeaderField> headers, boolean isChangeRequest)
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

  public static void parsePartSyntax(final BatchBodyPart bodyPart) throws BatchException {
    int contentLength = BatchTransformatorCommon.getContentLength(bodyPart.getHeaders());
    bodyPart.parse(contentLength);
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

    return Integer.MAX_VALUE;
  }
}
