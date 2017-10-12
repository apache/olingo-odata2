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

import org.apache.olingo.odata2.core.commons.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BatchLineReader {
  private static final byte CR = '\r';
  private static final byte LF = '\n';
  private static final int EOF = -1;
  private static final int BUFFER_SIZE = 8192;
  private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");
  private static final String UTF8_CHARSET = "UTF-8";
  private static final String CONTENT_TYPE = "content-type";
  private static final String XML_SUBTYPE = "xml";
  public static final String BOUNDARY = "boundary";
  public static final String DOUBLE_DASH = "--";
  public static final String CRLF = "\r\n";
  private Charset currentCharset = DEFAULT_CHARSET;
  private String currentBoundary = null;
  private ReadState readState = new ReadState();
  private InputStream reader;
  private byte[] buffer;
  private int offset = 0;
  private int limit = 0;

  public BatchLineReader(final InputStream reader) {
    this(reader, BUFFER_SIZE);
  }

  public BatchLineReader(final InputStream reader, final int bufferSize) {
    if (bufferSize <= 0) {
      throw new IllegalArgumentException("Buffer size must be greater than zero.");
    }

    this.reader = reader;
    buffer = new byte[bufferSize];
  }

  public void close() throws IOException {
    reader.close();
  }

  public List<String> toList() throws IOException {
    final List<String> result = new ArrayList<String>();
    String currentLine = readLine();
    if(currentLine != null) {
      currentBoundary = currentLine.trim();
      result.add(currentLine);

      while ((currentLine = readLine()) != null) {
        result.add(currentLine);
      }
    }
    return result;
  }

  public List<Line> toLineList() throws IOException {
    final List<Line> result = new ArrayList<Line>();
    String currentLine = readLine();
    if(currentLine != null) {
      currentBoundary = currentLine.trim();
      int counter = 1;
      result.add(new Line(currentLine, counter++));

      while ((currentLine = readLine()) != null) {
        result.add(new Line(currentLine, counter++));
      }
    }

    return result;
  }

  private void updateCurrentCharset(String currentLine) {
    if(currentLine != null) {
      if(isContentTypeHeaderLine(currentLine)) {
        currentLine = currentLine.substring(13, currentLine.length() - 2).trim();
        ContentType ct = ContentType.parse(currentLine);
        if (ct != null) {
          String charsetString = ct.getParameters().get(ContentType.PARAMETER_CHARSET);
          if (charsetString != null) {
            currentCharset = Charset.forName(charsetString);
          } else {
            if (ct.isCompatible(ContentType.APPLICATION_JSON) || ct.getSubtype().contains(XML_SUBTYPE)) {
              currentCharset = Charset.forName(UTF8_CHARSET);
            } else {
              currentCharset = DEFAULT_CHARSET;
            }
          }
          // boundary
          String boundary = ct.getParameters().get(BOUNDARY);
          if (boundary != null) {
            currentBoundary = DOUBLE_DASH + boundary;
          }
        }
      } else if(CRLF.equals(currentLine)) {
        readState.foundLinebreak();
      } else if(isBoundary(currentLine)) {
        readState.foundBoundary();
      }
    }
  }

  private boolean isContentTypeHeaderLine(String currentLine) {
    return currentLine.toLowerCase(Locale.ENGLISH).startsWith(CONTENT_TYPE);
  }

  private boolean isBoundary(String currentLine) {
    if((currentBoundary + CRLF).equals(currentLine)) {
      return true;
    } else if((currentBoundary + DOUBLE_DASH + CRLF).equals(currentLine)) {
      return true;
    }
    return false;
  }

  String readLine() throws IOException {
    if (limit == EOF) {
      return null;
    }

    ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
    boolean foundLineEnd = false; // EOF will be considered as line ending

    while (!foundLineEnd) {
      // Is buffer refill required?
      if (limit == offset) {
        if (fillBuffer() == EOF) {
          foundLineEnd = true;
        }
      }

      if (!foundLineEnd) {
        byte currentChar = this.buffer[offset++];
        buf = grantBuffer(buf);
        buf.put(currentChar);

        if (currentChar == LF) {
          foundLineEnd = true;
        } else if (currentChar == CR) {
          foundLineEnd = true;

          // Check next byte. Consume \n if available
          // Is buffer refill required?
          if (limit == offset) {
            fillBuffer();
          }

          // Check if there is at least one character
          if (limit != EOF && this.buffer[offset] == LF) {
            buf = grantBuffer(buf);
            buf.put(LF);
            offset++;
          }
        }
      }
    }

    if(buf.position() == 0) {
      return null;
    } else {
      String currentLine;
      if(readState.isReadBody()) {
        currentLine = new String(buf.array(), 0, buf.position(), getCurrentCharset());
      } else {
        currentLine = new String(buf.array(), 0, buf.position(), DEFAULT_CHARSET);
      }
      updateCurrentCharset(currentLine);
      return currentLine;
    }
  }

  private ByteBuffer grantBuffer(ByteBuffer buffer) {
    if(!buffer.hasRemaining()) {
      buffer.flip();
      ByteBuffer tmp = ByteBuffer.allocate(buffer.limit() *2);
      tmp.put(buffer);
      buffer = tmp;
    }
    return buffer;
  }

  private int fillBuffer() throws IOException {
    limit = reader.read(buffer, 0, buffer.length);
    offset = 0;

    return limit;
  }

  private Charset getCurrentCharset() {
    return currentCharset;
  }

  /**
   * Read state indicator (whether currently the <code>body</code> or <code>header</code> part is read).
   */
  private class ReadState {
    private int state = 0;

    public void foundLinebreak() {
      state++;
    }
    public void foundBoundary() {
      state = 0;
    }
    public boolean isReadBody() {
      return state >= 2;
    }

    @Override
    public String toString() {
      return String.valueOf(state);
    }
  }
}
