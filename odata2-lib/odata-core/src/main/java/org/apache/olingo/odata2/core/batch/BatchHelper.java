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
package org.apache.olingo.odata2.core.batch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.ODataResponseImpl;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

public class BatchHelper {

  public static final String BINARY_ENCODING = "binary";
  public static final String DEFAULT_ENCODING = "utf-8";
  public static final String HTTP_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
  public static final String HTTP_CONTENT_ID = "Content-Id";
  public static final String MIME_HEADER_CONTENT_ID = "MimeHeader-ContentId";
  public static final String REQUEST_HEADER_CONTENT_ID = "RequestHeader-ContentId";

  public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);

  protected static String generateBoundary(final String value) {
    return value + "_" + UUID.randomUUID().toString();
  }

  protected static byte[] getBytes(final String body) {
    try {
      return body.getBytes(DEFAULT_ENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new ODataRuntimeException(e);
    }
  }

  public static String convertToString(ODataResponseImpl oDataResponse) {
    Object entity = oDataResponse.getEntity();

    if(entity == null) {
      return null;
    } else if(entity instanceof String) {
      return (String) entity;
    } else if(entity instanceof byte[]) {
      String contentHeader = oDataResponse.getContentHeader();
      Charset charset = getCharset(contentHeader);
      return convertByteArray((byte[]) entity, charset);
    } else if(entity instanceof InputStream) {
      String contentHeader = oDataResponse.getContentHeader();
      Charset charset = getCharset(contentHeader);
      Body b = new Body(oDataResponse);
      return convertByteArray(b.getContent(), charset);
    }
    throw new ODataRuntimeException("Unable to convert ODataResponse entity of type '" +
        entity.getClass() + "' to String.");
  }

  public static Charset extractCharset(Map<String, String> headers) {
    String contentType = null;
    for (Map.Entry<String, String> s : headers.entrySet()) {
      if(s.getKey().equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
        contentType = s.getValue();
        break;
      }
    }

    return getCharset(contentType);
  }

  private static Charset getCharset(String contentType) {
    ContentType ct = ContentType.parse(contentType);
    if(ct != null) {
      String charsetString = ct.getParameters().get(ContentType.PARAMETER_CHARSET);
      if (charsetString != null && Charset.isSupported(charsetString)) {
        return Charset.forName(charsetString);
      }
    }
    return DEFAULT_CHARSET;
  }

  private static String convertByteArray(byte[] entity, Charset charset) {
    return new String(entity, charset);
  }

  /**
   * Builder class to create the body and the header.
   */
  static class BodyBuilder {
    public static final int DEFAULT_SIZE = 8192;
    private final Charset CHARSET_ISO_8859_1 = Charset.forName("iso-8859-1");
    private ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_SIZE);
    private boolean isClosed = false;

    public byte[] getContent() {
      isClosed = true;
      byte[] tmp = new byte[buffer.position()];
      buffer.flip();
      buffer.get(tmp, 0, buffer.limit());
      return tmp;
    }

    public InputStream getContentAsStream() {
      return new ByteArrayInputStream(getContent());
    }

    public String getContentAsString(Charset charset) {
      return new String(getContent(), charset);
    }

    public int getLength() {
      return (buffer.limit() > buffer.position() ? buffer.limit(): buffer.position());
    }

    public BodyBuilder append(String string) {
      byte [] b = string.getBytes(CHARSET_ISO_8859_1);
      put(b);
      return this;
    }

    private void put(byte[] b) {
      if(isClosed) {
        throw new RuntimeException("BodyBuilder is closed.");
      }
      if(buffer.remaining() < b.length) {
        buffer.flip();
        int newSize = (buffer.limit() * 2) + b.length;
        ByteBuffer tmp = ByteBuffer.allocate(newSize);
        tmp.put(buffer);
        buffer = tmp;
      }
      buffer.put(b);
    }

    public BodyBuilder append(int statusCode) {
      return append(String.valueOf(statusCode));
    }

    public BodyBuilder append(Body body) {
      put(body.getContent());
      return this;
    }

    public String toString() {
      return new String(buffer.array(), 0, buffer.position());
    }
  }

  /**
   * Body part which is read and stored as bytes (no charset conversion).
   */
  static class Body {
    private static final int BUFFER_SIZE = 8192;
    public static final byte[] EMPTY_BYTES = new byte[0];
    private final byte[] content;

    public Body(BatchChangeSetPart response) {
      this.content = getBody(response);
    }

    public Body(ODataResponse response) {
      this.content = getBody(response);
    }

    public Body() {
      this.content = EMPTY_BYTES;
    }

    public int getLength() {
      return content.length;
    }

    public byte[] getContent() {
      return content;
    }

    public boolean isEmpty() {
      return content.length == 0;
    }

    private byte[] getBody(final BatchChangeSetPart response) {
      if (response == null || response.getBodyAsBytes() == null) {
        return EMPTY_BYTES;
      }

      return response.getBodyAsBytes();
    }

    private byte[] getBody(final ODataResponse response) {
      if (response == null) {
        return EMPTY_BYTES;
      }
      Object entity = response.getEntity();
      if(entity == null) {
        return EMPTY_BYTES;
      } else if(entity instanceof InputStream) {
        try {
          ByteArrayOutputStream output = new ByteArrayOutputStream();
          ByteBuffer inBuffer = ByteBuffer.allocate(BUFFER_SIZE);
          ReadableByteChannel ic = Channels.newChannel((InputStream) entity);
          WritableByteChannel oc = Channels.newChannel(output);
          while (ic.read(inBuffer) > 0) {
            inBuffer.flip();
            oc.write(inBuffer);
            inBuffer.rewind();
          }
          return output.toByteArray();
        } catch (IOException e) {
          throw new ODataRuntimeException("Error on reading request content");
        }
      } else if(entity instanceof String) {
        return ((String) entity).getBytes(DEFAULT_CHARSET);
      } else {
        throw new ODataRuntimeException("Error on reading request content for entity type:" + entity.getClass());
      }
    }
  }

}
