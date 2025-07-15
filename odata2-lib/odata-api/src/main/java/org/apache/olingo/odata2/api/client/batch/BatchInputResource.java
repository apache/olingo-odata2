package org.apache.olingo.odata2.api.client.batch;

import java.io.*;

public class BatchInputResource implements Closeable {

  private final InputStream inputStream;
  private final int size;

  public BatchInputResource(InputStream inputStream, int size) {
    this.inputStream = inputStream;
    this.size = size;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public int size() {
    return size;
  }

  @Override
  public void close() throws IOException {
    if (inputStream != null) {
      inputStream.close();
    }
  }

}

