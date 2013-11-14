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

import java.util.Arrays;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmMediaResourceContent;
import org.apache.olingo.odata2.api.annotation.edm.EdmMediaResourceMimeType;
import org.apache.olingo.odata2.api.annotation.edm.EdmMediaResourceSource;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;

/**
 *  
 */
@EdmEntityType(name = "Photo", namespace = ModelSharedConstants.NAMESPACE_1)
@EdmEntitySet(name = "Photos")
public class Photo {
  @EdmProperty
  @EdmKey
  private String name;
  @EdmProperty
  private String type;
  @EdmProperty
  @EdmMediaResourceMimeType
  private String mimeType;
  @EdmProperty
  @EdmMediaResourceSource
  private String imageUrl = "http://localhost/someResource.png";
  @EdmProperty(type = EdmSimpleTypeKind.Binary)
  @EdmMediaResourceContent
  private byte[] image = ResourceHelper.generateImage();

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getImageUri() {
    return imageUrl;
  }

  public void setImageUri(final String uri) {
    imageUrl = uri;
  }

  public byte[] getImage() {
    return image.clone();
  }

  public void setImage(final byte[] image) {
    this.image = image;
  }

  public String getImageType() {
    return mimeType;
  }

  public void setImageType(final String imageType) {
    this.mimeType = imageType;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 83 * hash + (this.type != null ? this.type.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Photo other = (Photo) obj;
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
      return false;
    }
    return true;
  }
  
  @Override
  public String toString() {
    return "{\"Name\":\"" + name + "\","
        + "\"Type\":\"" + type + "\","
        + "\"ImageUrl\":\"" + imageUrl + "\","
        + "\"Image\":\"" + Arrays.toString(image) + "\","
        + "\"MimeType\":\"" + mimeType + "\"";
  }
}
