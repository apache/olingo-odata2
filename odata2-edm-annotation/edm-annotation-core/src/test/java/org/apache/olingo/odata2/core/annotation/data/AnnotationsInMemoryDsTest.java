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
package org.apache.olingo.odata2.core.annotation.data;

import java.util.HashMap;
import java.util.Map;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.core.annotation.edm.AnnotationEdmProvider;
import org.apache.olingo.odata2.core.annotation.model.Building;
import org.apache.olingo.odata2.core.annotation.model.ModelSharedConstants;
import org.apache.olingo.odata2.core.annotation.model.Photo;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class AnnotationsInMemoryDsTest {
  
  private final AnnotationInMemoryDs datasource;
  private AnnotationEdmProvider edmProvider;
  private static final String DEFAULT_CONTAINER = ModelSharedConstants.CONTAINER_1;
  private static final String DEFAULT_NAMESPACE = ModelSharedConstants.NAMESPACE_1;

  public AnnotationsInMemoryDsTest() {
    datasource = new AnnotationInMemoryDs(Building.class.getPackage().getName());
    edmProvider = new AnnotationEdmProvider(Building.class.getPackage().getName());
  }


  @Test(expected = ODataApplicationException.class)
  public void invalidEntity() throws Exception {
    EdmEntitySet edmEntitySet = createMockedEdmEntitySet("Building");
    
    datasource.createData(edmEntitySet, this);
  }

  @Test
  public void createSimpleEntity() throws Exception {
    EdmEntitySet edmEntitySet = createMockedEdmEntitySet("Building");
    
    Building building = new Building();
    building.setName("Common Building");
    datasource.createData(edmEntitySet, building);
    
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", "1");
    
    Building read = (Building) datasource.readData(edmEntitySet, keys);
    Assert.assertEquals("Common Building", read.getName());
  }

  @Test
  public void createEntityTwoKeys() throws Exception {
    EdmEntitySet edmEntitySet = createMockedEdmEntitySet("Photo");
    
    Photo photo = new Photo();
    photo.setName("BigPicture");
    photo.setType("PNG");
    photo.setImageUri("https://localhost/image.png");
    photo.setImageType("image/png");
    datasource.createData(edmEntitySet, photo);
    
    Map<String, Object> keys = new HashMap<String, Object>();
//    keys.put("ImageFormat", "PNG");
//    keys.put("Name", "BigPicture");
    keys.put("Name", "1");
    keys.put("ImageFormat", "2");
    
    Photo read = (Photo) datasource.readData(edmEntitySet, keys);
//    Assert.assertEquals("BigPicture", read.getName());
    Assert.assertEquals("1", read.getName());
    Assert.assertEquals("2", read.getType());
    Assert.assertEquals("image/png", read.getImageType());
    Assert.assertEquals("https://localhost/image.png", read.getImageUri());
  }

  @Test
//  @Ignore("Rethink update method")
  public void createAndUpdateEntityTwoKeys() throws Exception {
    EdmEntitySet edmEntitySet = createMockedEdmEntitySet("Photo");
    
    Photo photo = new Photo();
    final String nameKeyValue = "BigPicture";
    final String typeKeyValue = "PNG";
    photo.setName(nameKeyValue);
    photo.setType(typeKeyValue);
    photo.setImageUri("https://localhost/image.png");
    photo.setImageType("image/png");
    datasource.createData(edmEntitySet, photo);
    
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Name", "1");
    keys.put("ImageFormat", "2");
    
    Photo read = (Photo) datasource.readData(edmEntitySet, keys);
    Assert.assertEquals("1", read.getName());
    Assert.assertEquals("2", read.getType());
    Assert.assertEquals("image/png", read.getImageType());
    Assert.assertEquals("https://localhost/image.png", read.getImageUri());
    

    // update
    Photo updatedPhoto = new Photo();
//    updatedPhoto.setName(nameKeyValue);
//    updatedPhoto.setType(typeKeyValue);
    updatedPhoto.setName("1");
    updatedPhoto.setType("2");
    updatedPhoto.setImageUri("https://localhost/image.jpg");
    updatedPhoto.setImageType("image/jpg");
    datasource.updateData(edmEntitySet, updatedPhoto);
    
    Map<String, Object> updatedKeys = new HashMap<String, Object>();
//    updatedKeys.put("Name", nameKeyValue);
//    updatedKeys.put("ImageFormat", typeKeyValue);
    updatedKeys.put("Name", "1");
    updatedKeys.put("ImageFormat", "2");
    
    Photo readUpdated = (Photo) datasource.readData(edmEntitySet, updatedKeys);
    Assert.assertEquals("1", readUpdated.getName());
    Assert.assertEquals("2", readUpdated.getType());
    Assert.assertEquals("image/jpg", readUpdated.getImageType());
    Assert.assertEquals("https://localhost/image.jpg", readUpdated.getImageUri());
  }

  private EdmEntitySet createMockedEdmEntitySet(String entitySetName) throws ODataException {
//    EntitySet entitySet = edmProvider.getEntitySet(DEFAULT_CONTAINER, entitySetName);
    EntityType entityType = edmProvider.getEntityType(new FullQualifiedName(DEFAULT_NAMESPACE, entitySetName));
    
    EdmEntitySet edmEntitySet = Mockito.mock(EdmEntitySet.class);
    EdmEntityType edmEntityType = Mockito.mock(EdmEntityType.class);
    Mockito.when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
    Mockito.when(edmEntityType.getName()).thenReturn(entityType.getName());
    
    return edmEntitySet;
  }
}
