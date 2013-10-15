/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.olingo.odata2.ref.annotation.model.ds;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityCreate;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityDataSource;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityRead;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntitySetRead;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityUpdate;
import org.apache.olingo.odata2.ref.annotation.model.Photo;

/**
 *
 */
@EntityDataSource(entityType = Photo.class)
public class PhotoDs {
  private static final Map<String, Photo> photoDb = new HashMap<String, Photo>();
  
  private String createPhotoId(Photo p) {
    if(p == null) {
      return null;
    }
    return p.getName() + "-" + p.getType();
  }
  
  @EntityCreate
  @EntityUpdate
  public Photo storePhoto(Photo photo) {
    Photo p = new Photo(photo.getName(), photo.getType());
    p.setImageUri(photo.getImageUri());
    p.setImageType(photo.getImageType());
    
    photoDb.put(createPhotoId(p), p);
    
    return p;
  }
  
  @EntityRead
  public Photo loadPhoto(String name) {
    return photoDb.get(createPhotoId(new Photo(name, "PNG")));
  }

  @EntitySetRead
  public Collection<Photo> loadAllPhotos() {
    return photoDb.values();
  }
}
