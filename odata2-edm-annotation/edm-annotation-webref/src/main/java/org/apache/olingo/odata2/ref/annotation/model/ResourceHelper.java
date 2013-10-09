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
package org.apache.olingo.odata2.ref.annotation.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author d046871
 */
class ResourceHelper {

  public static byte[] loadAsByte(String resource) {
    return load(resource, new byte[0]);
  }

  public static byte[] load(String resource, byte[] defaultResult) {
    InputStream instream = null;
    try {
      instream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
      if (instream == null) {
        return defaultResult;
      }
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      int b = 0;
      while ((b = instream.read()) != -1) {
        stream.write(b);
      }

      return stream.toByteArray();
    } catch (IOException e) {
      throw new ModelException(e);
    } finally {
      if(instream != null) {
        try {
          instream.close();
        } catch (IOException ex) { }
      }
    }
  }
}
