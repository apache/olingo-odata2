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
