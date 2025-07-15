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

import org.apache.olingo.odata2.api.client.batch.BatchChangeSetPart;
import org.apache.olingo.odata2.api.client.batch.BatchInputResource;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

public class BatchHelper {

  public static final String BINARY_ENCODING = "binary";
  public static final String UTF8_ENCODING = "UTF-8";
  public static final String ISO_ENCODING = "ISO-8859-1";
  private static String DEFAULT_ENCODING = "ISO-8859-1";
  public static final String HTTP_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
  public static final String HTTP_CONTENT_ID = "Content-Id";
  public static final String MIME_HEADER_CONTENT_ID = "MimeHeader-ContentId";
  public static final String REQUEST_HEADER_CONTENT_ID = "RequestHeader-ContentId";

  protected static Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);

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

  public static Charset extractCharset(ContentType contentType) {
    if (contentType != null) {
      final String charsetValue = contentType.getParameters().get(ContentType.PARAMETER_CHARSET);
      if (charsetValue == null) {
        if (contentType.isCompatible(ContentType.APPLICATION_JSON) || contentType.getSubtype().contains("xml")) {
          setDefaultValues(UTF8_ENCODING);
          return Charset.forName(UTF8_ENCODING);
        }
      } else {
        setDefaultValues(charsetValue);
        return Charset.forName(charsetValue);
      }
    }
    setDefaultValues(ISO_ENCODING);
    return Charset.forName(ISO_ENCODING);
  }

  private static Charset getCharset(String contentType) {
    ContentType ct = ContentType.parse(contentType);
    if(ct != null) {
      String charsetString = ct.getParameters().get(ContentType.PARAMETER_CHARSET);
      if (charsetString != null && Charset.isSupported(charsetString)) {
        setDefaultValues(charsetString);
        return Charset.forName(charsetString);
      } else {
        if (ct.isCompatible(ContentType.APPLICATION_JSON) || ct.getSubtype().contains("xml")) {
          setDefaultValues(UTF8_ENCODING);
          return Charset.forName(UTF8_ENCODING);
        }
      }
    }
    setDefaultValues(ISO_ENCODING);
    return Charset.forName(ISO_ENCODING);
  }
  
  private static void setDefaultValues(String contentType) {
    DEFAULT_CHARSET = Charset.forName(contentType);
    DEFAULT_ENCODING = contentType;
    
  }

  /**
   * Builder class to create the body and the header.
   */
  static class BodyBuilder {

    private static final String OLINGO_TMPDIR_PROPERTY = "olingo.tmpdir";

    public static final int DEFAULT_SIZE = 8192;
    private static final int THRESHOLD = DEFAULT_SIZE * 8;
    private final Charset CHARSET_ISO_8859_1 = Charset.forName("iso-8859-1");

    private ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_SIZE);
    private File fileBuffer = null;

    private boolean isClosed = false;

    public InputStream getContentAsStream() {
      try {
        return fileBuffer != null ?
          new DeleteOnCloseFileInputStream(fileBuffer) : new ByteArrayInputStream(getBufferContent());
      } catch (IOException exception) {
        throw new ODataRuntimeException(exception);
      }
    }

    public String getContentAsString(Charset charset) {
      if (fileBuffer != null) {
        InputStreamReader reader = null;
        try {
          reader = new InputStreamReader(new DeleteOnCloseFileInputStream(fileBuffer), charset);

          StringBuilder sb = new StringBuilder();
          char[] buffer = new char[DEFAULT_SIZE];
          int bytesCount;
          while ((bytesCount = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, bytesCount);
          }
          reader.close();

          return sb.toString();
        } catch (IOException e) {
          throw new ODataRuntimeException(e);
        } finally {
          if (reader != null) {
            try {
              reader.close();
            } catch (IOException e) {}
          }
        }
      }

      return new String(getBufferContent(), charset);
    }

    public int getLength() {
      if (fileBuffer == null) {
        return (Math.max(buffer.limit(), buffer.position()));
      }
      return (int) fileBuffer.length();
    }

    public BodyBuilder append(String string) {
      byte [] b = string.getBytes(DEFAULT_CHARSET);
      put(b);
      return this;
    }

    private void put(byte[] b) {
      put(new BatchInputResource(new ByteArrayInputStream(b), b.length));
    }

    private void put(BatchInputResource resource) {
      if(isClosed) {
        throw new RuntimeException("BodyBuilder is closed.");
      }

      if (fileBuffer == null) {
        if (buffer.remaining() < resource.size()) {
          int newSize = (buffer.limit() * 2) + resource.size();
          if (newSize > THRESHOLD) {
            fileBuffer = createTempFile();
            writeToFileBuffer(new ByteArrayInputStream(buffer.array(), 0, buffer.position()),
              resource.getInputStream());
            buffer = null;
          } else {
            buffer.flip();
            ByteBuffer tmp = ByteBuffer.allocate(newSize);
            tmp.put(buffer);
            buffer = tmp;
            writeToByteBuffer(resource.getInputStream());
          }
        } else {
          writeToByteBuffer(resource.getInputStream());
        }
      } else {
        writeToFileBuffer(resource.getInputStream());
      }
    }

    File createTempFile() {
      String tempDir = System.getProperty(OLINGO_TMPDIR_PROPERTY);
      if (tempDir == null) {
        tempDir = System.getProperty("java.io.tmpdir");
      }

      try {
        return File.createTempFile("odata", "olingo", new File(tempDir));
      } catch (IOException e) {
        throw new ODataRuntimeException(e);
      }
    }

    private void writeToByteBuffer(InputStream inputStream) {
      int bytesCount;
      try {
        for (byte[] sbuf = new byte[DEFAULT_SIZE]; (bytesCount = inputStream.read(sbuf)) != -1;) {
          buffer.put(sbuf, 0, bytesCount);
        }
      } catch (IOException e) {
        throw new ODataRuntimeException(e);
      }
    }

    private void writeToFileBuffer(InputStream... inputStreams) {
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(fileBuffer, true);
        for (InputStream inputStream : inputStreams) {
          copyStream(inputStream, fos);
        }
      } catch (IOException e) {
        throw new ODataRuntimeException(e);
      } finally {
        if (fos != null) {
          try {
            fos.close();
          } catch (IOException e) {}
        }
      }
    }

    private void copyStream(InputStream inputStream, FileOutputStream outputStream) {
      try {
        int bytesRead;
        for (byte[] sbuf = new byte[DEFAULT_SIZE]; (bytesRead = inputStream.read(sbuf)) != -1; ) {
          outputStream.write(sbuf, 0, bytesRead);
        }
        outputStream.flush();
      } catch (IOException e) {
        throw new ODataRuntimeException(e);
      }
    }

    private byte[] getBufferContent() {
      isClosed = true;
      byte[] tmp = new byte[buffer.position()];
      buffer.flip();
      buffer.get(tmp, 0, buffer.limit());
      return tmp;
    }

    public BodyBuilder append(int statusCode) {
      return append(String.valueOf(statusCode));
    }

    public BodyBuilder append(Body body) {
      put(body.getBatchInputResource());
      return this;
    }

    public String toString() {
      return new String(buffer.array(), 0, buffer.position());
    }
    
    /**
     * Fetch the calibrated length in case of binary data. 
     * Since after applying the charset the content length changes.
     * If the previously generated length is sent back then the batch response 
     * body is seen truncated
     * @param batchResponseBody
     * @return
     */
    public int calculateLength(Object batchResponseBody) {
      if (batchResponseBody != null) {
        if (batchResponseBody instanceof String) {
          if (DEFAULT_ENCODING.equalsIgnoreCase(ISO_ENCODING)) {
            try {
              return ((String) batchResponseBody).getBytes(UTF8_ENCODING).length;
            } catch (UnsupportedEncodingException e) {
              throw new ODataRuntimeException(e);
            }
          } else {
            return getLength();
          }
        } else {
          return getLength();
        }
      }
      return getLength();
    }
  }

  /**
   * Body part which is read and stored as bytes (no charset conversion).
   */
  static class Body {
    private static final int BUFFER_SIZE = 8192;
    public static final byte[] EMPTY_BYTES = new byte[0];
    private final BatchInputResource batchInputResource;

    public Body(BatchChangeSetPart response) {
      this.batchInputResource = response.getBatchInputResource();
    }

    public Body(ODataResponse response) {
      byte[] content = getBody(response);
      this.batchInputResource = new BatchInputResource(
        new ByteArrayInputStream(content), content.length);
    }

    public Body() {
      this.batchInputResource = new BatchInputResource(new ByteArrayInputStream(EMPTY_BYTES), 0);
      setDefaultValues(ISO_ENCODING);
    }

    public int getLength() {
      return batchInputResource.size();
    }

    public BatchInputResource getBatchInputResource() {
      return batchInputResource;
    }

    public boolean isEmpty() {
      return batchInputResource.size() == 0;
    }

    private byte[] getBody(final ODataResponse response) {
      if (response == null) {
        return EMPTY_BYTES;
      }
      Object entity = response.getEntity();
      if(entity == null) {
        return EMPTY_BYTES;
      } else if(entity instanceof InputStream) {
		  ReadableByteChannel ic = null;
		  WritableByteChannel oc = null;
        try {
          extractCharset(ContentType.parse(response.getHeader("Content-Type")));
          ByteArrayOutputStream output = new ByteArrayOutputStream();
          ByteBuffer inBuffer = ByteBuffer.allocate(BUFFER_SIZE);
          ic = Channels.newChannel((InputStream) entity);
          oc = Channels.newChannel(output);
          while (ic.read(inBuffer) > 0) {
            inBuffer.flip();
            oc.write(inBuffer);
            inBuffer.rewind();
          }
          return output.toByteArray();
        } catch (IOException e) {
          throw new ODataRuntimeException("Error on reading request content");
        } finally {
          try {
			  if (ic != null) {
				ic.close();  
			  }
          } catch (IOException e) {
            throw new ODataRuntimeException("Error closing the Readable Byte Channel", e);
          }
          try {
			  if (oc != null) {
				oc.close();  
			  }
          } catch (IOException e) {
            throw new ODataRuntimeException("Error closing the Writable Byte Channel", e);
          }
        }
      } else if (entity instanceof byte[]) {
        setDefaultValues(ISO_ENCODING);
        return (byte[]) entity;
      } else if(entity instanceof String) {
        setDefaultValues(UTF8_ENCODING);
        return ((String) entity).getBytes(DEFAULT_CHARSET);
      } else {
        throw new ODataRuntimeException("Error on reading request content for entity type:" + entity.getClass());
      }
    }
  }

}
