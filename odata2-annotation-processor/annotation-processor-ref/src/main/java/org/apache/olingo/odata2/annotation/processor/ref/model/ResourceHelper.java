/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.apache.olingo.odata2.annotation.processor.ref.model;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 *
 */
public class ResourceHelper {

    public static byte[] loadAsByte(final String resource) {
        return load(resource, new byte[0]);
    }

    public static byte[] load(final String resource, final byte[] defaultResult) {
        InputStream instream = null;
        try {
            instream = Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(resource);
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
            throw new RuntimeException(e);
        } finally {
            if (instream != null) {
                try {
                    instream.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public enum Format {
        BMP, JPEG, PNG, GIF
    }

    public static byte[] generateImage() {
        return generateImage(Format.PNG);
    }

    public static byte[] generateImage(final Format format) {
        try {
            int width = 320;
            int height = 320;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
            WritableRaster raster = image.getRaster();

            int mod = format.ordinal() + 2;
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    if (((h / 32) + (w / 32)) % mod == 0) {
                        raster.setSample(w, h, 0, 0);
                    } else {
                        raster.setSample(w, h, 0, 1);
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            ImageIO.write(image, format.name(), out);
            return out.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }
}
