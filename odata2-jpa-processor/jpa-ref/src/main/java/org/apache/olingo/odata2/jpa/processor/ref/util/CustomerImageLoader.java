package org.apache.olingo.odata2.jpa.processor.ref.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomerImageLoader {
  public static byte[] loadImage(Long customerId) {
    String name = null;
    if (customerId == 1L) {
      name = "/Customer_1.png";
    } else if (customerId == 2L) {
      name = "/Customer_2.png";
    } else {
      return null;
    }

    InputStream is = CustomerImageLoader.class.getResourceAsStream(name);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    int b = 0;
    try {
      while ((b = is.read()) != -1) {
        os.write(b);
      }
    } catch (IOException e) {
      return null;
    }
    return os.toByteArray();
  }

}
