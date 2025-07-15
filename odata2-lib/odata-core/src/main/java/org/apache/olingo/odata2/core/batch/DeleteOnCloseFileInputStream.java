package org.apache.olingo.odata2.core.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DeleteOnCloseFileInputStream extends FileInputStream {

  private File file;

  public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException{
    super(file);
    this.file = file;
  }

  public void close() throws IOException {
    try {
      super.close();
    } finally {
      if(file != null) {
        file.delete();
        file = null;
      }
    }
  }
}
