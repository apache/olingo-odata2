/*
 * Copyright 2013 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.olingo.odata2.annotation.processor.core.datasource;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.olingo.odata2.annotation.processor.core.datasource.DataSource.BinaryData;
import org.apache.olingo.odata2.annotation.processor.core.edm.AnnotationEdmProvider;
import org.apache.olingo.odata2.annotation.processor.core.model.Building;
import org.apache.olingo.odata2.annotation.processor.core.model.City;
import org.apache.olingo.odata2.annotation.processor.core.model.Employee;
import org.apache.olingo.odata2.annotation.processor.core.model.Location;
import org.apache.olingo.odata2.annotation.processor.core.model.Manager;
import org.apache.olingo.odata2.annotation.processor.core.model.ModelSharedConstants;
import org.apache.olingo.odata2.annotation.processor.core.model.Photo;
import org.apache.olingo.odata2.annotation.processor.core.model.RefBase;
import org.apache.olingo.odata2.annotation.processor.core.model.Room;
import org.apache.olingo.odata2.annotation.processor.core.model.Team;
import org.apache.olingo.odata2.annotation.processor.core.util.AnnotationHelper;
import org.apache.olingo.odata2.annotation.processor.core.util.AnnotationRuntimeException;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class AnnotationsDataSourceTest {

  final static Set<Class<?>> ANNOTATED_MODEL_CLASSES = new HashSet<Class<?>>();
  final static Set<Class<?>> ANNOTATED_ENTITY_SET_CLASSES = new HashSet<Class<?>>();

  static {
    ANNOTATED_ENTITY_SET_CLASSES.add(Building.class);
    ANNOTATED_ENTITY_SET_CLASSES.add(Employee.class);
    ANNOTATED_ENTITY_SET_CLASSES.add(Manager.class);
    ANNOTATED_ENTITY_SET_CLASSES.add(Photo.class);
    ANNOTATED_ENTITY_SET_CLASSES.add(Room.class);
    ANNOTATED_ENTITY_SET_CLASSES.add(Team.class);
    ANNOTATED_MODEL_CLASSES.addAll(ANNOTATED_ENTITY_SET_CLASSES);
    ANNOTATED_MODEL_CLASSES.add(Location.class);
    ANNOTATED_MODEL_CLASSES.add(City.class);
    ANNOTATED_MODEL_CLASSES.add(RefBase.class);
  }

  private final AnnotationDataSource datasource;
  private final AnnotationEdmProvider edmProvider;
  private static final String DEFAULT_CONTAINER = ModelSharedConstants.CONTAINER_1;
  private final DualDataStoreFactory dataStoreFactory;

  public AnnotationsDataSourceTest() throws ODataException {
    dataStoreFactory = new DualDataStoreFactory();
    dataStoreFactory.setDefaultProperty(DataStoreFactory.KEEP_PERSISTENT, Boolean.FALSE.toString());
    datasource = new AnnotationDataSource(Building.class.getPackage().getName(), dataStoreFactory);
    edmProvider = new AnnotationEdmProvider(Building.class.getPackage().getName());
  }

  @Test
  public void initFromPackage() throws Exception {
    AnnotationDataSource ds = new AnnotationDataSource(Building.class.getPackage().getName());
    Assert.assertNotNull(ds);
  }

  @Test(expected = IllegalArgumentException.class)
  public void initFromNotExistingPackage() throws Exception {
    AnnotationDataSource ds = new AnnotationDataSource("does.not.exist");
    Assert.assertNotNull(ds);
  }

  @Test
  public void initFromPackageWithoutAnnotatedClasses() throws Exception {
    AnnotationDataSource ds = new AnnotationDataSource(this.getClass().getPackage().getName());
    Assert.assertNotNull(ds);
  }

  @Test
  public void initFromClassCollectionEntitySets() throws Exception {
    AnnotationDataSource ds = new AnnotationDataSource(ANNOTATED_ENTITY_SET_CLASSES);
    Assert.assertNotNull(ds);
  }

  @Test
  public void initFromClassCollectionModel() throws Exception {
    AnnotationDataSource ds = new AnnotationDataSource(ANNOTATED_MODEL_CLASSES);
    Assert.assertNotNull(ds);
  }

  @Test(expected = ODataException.class)
  public void initFromClassCollectionNotAnnotated() throws Exception {
    Set<Class<?>> annotatedClassesAndMore = new HashSet<Class<?>>(ANNOTATED_ENTITY_SET_CLASSES);
    annotatedClassesAndMore.add(String.class);
    annotatedClassesAndMore.add(Object.class);
    AnnotationDataSource ds = new AnnotationDataSource(annotatedClassesAndMore);
    Assert.assertNotNull(ds);
  }

  @Test
  @Ignore
  public void multiThreadedSyncOnBuildingsTest() throws Exception {
    final EdmEntitySet edmEntitySet = createMockedEdmEntitySet("Buildings");
    CountDownLatch latch;

    List<Thread> threads = new ArrayList<Thread>();
    int max = 500;

    latch = new CountDownLatch(max);
    for (int i = 0; i < max; i++) {
      threads.add(createBuildingThread(latch, datasource, edmEntitySet, String.valueOf("10")));
    }

    for (Thread thread : threads) {
      thread.start();
    }

    latch.await(60, TimeUnit.SECONDS);

    DataStore<Building> ds = datasource.getDataStore(Building.class);
    Collection<Building> buildings = ds.read();
    Assert.assertEquals(max, buildings.size());
  }

  @org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet
  @org.apache.olingo.odata2.api.annotation.edm.EdmEntityType
  private static class SimpleEntity {

    @EdmKey
    @EdmProperty
    public Integer id;
    @EdmProperty
    public String name;
  }

  @Test
  @Ignore
  public void multiThreadedSyncCreateReadTest() throws Exception {
    Collection<Class<?>> ac = new ArrayList<Class<?>>();
    ac.add(SimpleEntity.class);
    final AnnotationDataSource localDs = new AnnotationDataSource(SimpleEntity.class.getPackage().getName());
    final AnnotationEdmProvider localProvider = new AnnotationEdmProvider(ac);
    final EdmEntitySet edmEntitySet = createMockedEdmEntitySet(localProvider, "SimpleEntitySet");
    final CountDownLatch latch;

    List<Thread> threads = new ArrayList<Thread>();
    int max = 500;
    latch = new CountDownLatch(max);
    for (int i = 0; i < max; i++) {
      Runnable run = new Runnable() {
        @Override
        public void run() {
          SimpleEntity se = new SimpleEntity();
          se.id = Integer.valueOf(String.valueOf(System.currentTimeMillis()).substring(8));
          se.name = "Name: " + System.currentTimeMillis();
          try {
            localDs.createData(edmEntitySet, se);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          } finally {
            latch.countDown();
          }
        }
      };

      threads.add(new Thread(run));
    }

    for (Thread thread : threads) {
      thread.start();
    }

    latch.await(60, TimeUnit.SECONDS);

    DataStore<SimpleEntity> ds = localDs.getDataStore(SimpleEntity.class);
    Collection<SimpleEntity> buildings = ds.read();
    Assert.assertEquals(max, buildings.size());
  }

  private Thread createBuildingThread(final CountDownLatch latch, final DataSource datasource,
      final EdmEntitySet edmEntitySet, final String id) {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        Building building = new Building();
        building.setName("Common Building - " + System.currentTimeMillis());
        building.setId(id);
        try {
          datasource.createData(edmEntitySet, building);
        } catch (Exception ex) {
          ex.printStackTrace();
          throw new RuntimeException(ex);
        } finally {
          latch.countDown();
        }
      }
    };

    return new Thread(run);
  }

  @Test
  public void readBinaryData() throws Exception {
    EdmEntitySet entitySet = createMockedEdmEntitySet("Photos");

    DataStore<Photo> photoDataStore = datasource.getDataStore(Photo.class);
    Photo photo = new Photo();
    photo.setName("SomePic");
    photo.setType("PNG");
    byte[] image = "binary".getBytes(Charset.defaultCharset());
    photo.setImage(image);
    photo.setImageType("image/png");
    photoDataStore.create(photo);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Name", "SomePic");
    keys.put("ImageFormat", "PNG");
    Photo toReadPhoto = (Photo) datasource.readData(entitySet, keys);

    // execute
    BinaryData readBinaryData = datasource.readBinaryData(entitySet, toReadPhoto);

    // validate
    Assert.assertEquals("binary", new String(readBinaryData.getData(), Charset.defaultCharset()));
    Assert.assertArrayEquals(image, readBinaryData.getData());
    Assert.assertEquals("image/png", readBinaryData.getMimeType());
  }

  @Test
  public void readBinaryDataDirect() throws Exception {
    EdmEntitySet entitySet = createMockedEdmEntitySet("Photos");

    DataStore<Photo> photoDataStore = datasource.getDataStore(Photo.class);
    Photo photo = new Photo();
    photo.setName("SomePic");
    photo.setType("PNG");
    byte[] image = "binary".getBytes(Charset.defaultCharset());
    photo.setImage(image);
    photo.setImageType("image/png");
    photoDataStore.create(photo);

    Photo toReadPhoto = new Photo();
    toReadPhoto.setName("SomePic");
    toReadPhoto.setType("PNG");
    toReadPhoto.setImage(null);
    toReadPhoto.setImageType(null);

    BinaryData readBinaryData = datasource.readBinaryData(entitySet, toReadPhoto);

    Assert.assertEquals("binary", new String(readBinaryData.getData(), Charset.defaultCharset()));
    Assert.assertArrayEquals(image, readBinaryData.getData());
    Assert.assertEquals("image/png", readBinaryData.getMimeType());
  }

  @Test
  public void writeBinaryData() throws Exception {
    EdmEntitySet entitySet = createMockedEdmEntitySet("Photos");

    DataStore<Photo> photoDataStore = datasource.getDataStore(Photo.class);

    Photo toWritePhoto = new Photo();
    toWritePhoto.setName("SomePic");
    toWritePhoto.setType("PNG");
    photoDataStore.create(toWritePhoto);
    byte[] image = "binary".getBytes(Charset.defaultCharset());
    String mimeType = "image/png";
    BinaryData writeBinaryData = new BinaryData(image, mimeType);
    // execute
    datasource.writeBinaryData(entitySet, toWritePhoto, writeBinaryData);

    // validate
    Photo photoKey = new Photo();
    photoKey.setName("SomePic");
    photoKey.setType("PNG");
    Photo storedPhoto = photoDataStore.read(photoKey);
    Assert.assertEquals("binary", new String(storedPhoto.getImage(), Charset.defaultCharset()));
    Assert.assertArrayEquals(image, storedPhoto.getImage());
    Assert.assertEquals("image/png", storedPhoto.getImageType());
  }

  @Test(expected = ODataNotFoundException.class)
  public void writeBinaryDataNotFound() throws Exception {
    EdmEntitySet entitySet = createMockedEdmEntitySet("Photos");

    Photo toWritePhoto = new Photo();
    toWritePhoto.setName("SomePic");
    toWritePhoto.setType("PNG");
    byte[] image = "binary".getBytes(Charset.defaultCharset());
    String mimeType = "image/png";
    BinaryData writeBinaryData = new BinaryData(image, mimeType);
    // execute
    datasource.writeBinaryData(entitySet, toWritePhoto, writeBinaryData);
  }

  @Test
  public void newDataObject() throws Exception {
    EdmEntitySet roomsEntitySet = createMockedEdmEntitySet("Rooms");
    Room room = (Room) datasource.newDataObject(roomsEntitySet);

    Assert.assertNotNull(room);
  }

  @Test
  public void readEntity() throws Exception {
    EdmEntitySet buildingsEntitySet = createMockedEdmEntitySet("Buildings");
    EdmEntitySet roomsEntitySet = createMockedEdmEntitySet("Rooms");

    Building building = new Building();
    building.setName("Common Building");

    final int roomsCount = 3;
    List<Room> rooms = new ArrayList<Room>();
    for (int i = 0; i < roomsCount; i++) {
      Room room = new Room(i, "Room " + i);
      room.setBuilding(building);
      datasource.createData(roomsEntitySet, room);
      rooms.add(room);
    }

    building.getRooms().addAll(rooms);
    datasource.createData(buildingsEntitySet, building);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", "1");

    // execute
    Object relatedData = datasource.readData(buildingsEntitySet, keys);

    // validate
    Building readBuilding = (Building) relatedData;
    Assert.assertEquals("Common Building", readBuilding.getName());
    Assert.assertEquals("1", readBuilding.getId());

    Collection<Room> relatedRooms = readBuilding.getRooms();
    Assert.assertEquals(roomsCount, relatedRooms.size());
    for (Room room : relatedRooms) {
      Assert.assertNotNull(room.getId());
      Assert.assertTrue(room.getName().matches("Room \\d*"));
      Assert.assertEquals("Common Building", room.getBuilding().getName());
    }
  }

  @Test(expected = AnnotationRuntimeException.class)
  public void readUnknownEntity() throws Exception {
    EdmEntitySet unknownEntitySet = Mockito.mock(EdmEntitySet.class);
    Mockito.when(unknownEntitySet.getName()).thenReturn("UnknownEntity");
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", "1");

    // execute
    datasource.readData(unknownEntitySet, keys);
  }

  @Test(expected = AnnotationRuntimeException.class)
  public void readUnknownEntities() throws Exception {
    EdmEntitySet unknownEntitySet = Mockito.mock(EdmEntitySet.class);
    Mockito.when(unknownEntitySet.getName()).thenReturn("UnknownEntity");

    // execute
    datasource.readData(unknownEntitySet);
  }

  @Test
  public void readEntities() throws Exception {
    EdmEntitySet roomsEntitySet = createMockedEdmEntitySet("Rooms");

    Building building = new Building();
    building.setName("Common Building");

    final int roomsCount = 11;
    List<Room> rooms = new ArrayList<Room>();
    for (int i = 0; i < roomsCount; i++) {
      Room room = new Room(i, "Room " + i);
      room.setBuilding(building);
      datasource.createData(roomsEntitySet, room);
      rooms.add(room);
    }

    // execute
    Object relatedData = datasource.readData(roomsEntitySet);

    // validate
    @SuppressWarnings("unchecked")
    Collection<Room> relatedRooms = (Collection<Room>) relatedData;
    Assert.assertEquals(roomsCount, relatedRooms.size());
    for (Room room : relatedRooms) {
      Assert.assertNotNull(room.getId());
      Assert.assertTrue(room.getName().matches("Room \\d*"));
      Assert.assertEquals("Common Building", room.getBuilding().getName());
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void readRelatedEntities() throws Exception {
    EdmEntitySet buildingsEntitySet = createMockedEdmEntitySet("Buildings");
    EdmEntitySet roomsEntitySet = createMockedEdmEntitySet("Rooms");

    Building building = new Building();
    building.setName("Common Building");

    final int roomsCount = 10;
    List<Room> rooms = new ArrayList<Room>();
    for (int i = 0; i < roomsCount; i++) {
      Room room = new Room(i, "Room " + i);
      room.setBuilding(building);
      datasource.createData(roomsEntitySet, room);
      rooms.add(room);
    }

    building.getRooms().addAll(rooms);
    datasource.createData(buildingsEntitySet, building);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", "1");

    Building read = (Building) datasource.readData(buildingsEntitySet, keys);
    Assert.assertEquals("Common Building", read.getName());
    Assert.assertEquals("1", read.getId());

    // execute
    Object relatedData = datasource.readRelatedData(
        buildingsEntitySet, building, roomsEntitySet, Collections.EMPTY_MAP);

    // validate
    Assert.assertTrue("Result is no collection.", relatedData instanceof Collection);
    Collection<Room> relatedRooms = (Collection<Room>) relatedData;
    Assert.assertEquals(roomsCount, relatedRooms.size());
    for (Room room : relatedRooms) {
      Assert.assertNotNull(room.getId());
      Assert.assertTrue(room.getName().matches("Room \\d*"));
      Assert.assertEquals("Common Building", room.getBuilding().getName());
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void readRelatedEntity() throws Exception {
    EdmEntitySet buildingsEntitySet = createMockedEdmEntitySet("Buildings");
    EdmEntitySet roomsEntitySet = createMockedEdmEntitySet("Rooms");

    Building building = new Building();
    building.setName("Common Building");

    Room room = new Room(12, "Room 12");
    room.setBuilding(building);
    datasource.createData(roomsEntitySet, room);
    datasource.createData(buildingsEntitySet, building);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", Integer.valueOf(12));
    Room read = (Room) datasource.readData(roomsEntitySet, keys);
    Assert.assertEquals("Room 12", read.getName());
    Assert.assertEquals("12", read.getId());

    // execute
    Object relatedData = datasource.readRelatedData(
        roomsEntitySet, room, buildingsEntitySet, Collections.EMPTY_MAP);

    // validate
    Assert.assertTrue("Result is no room.", relatedData instanceof Building);
    Building b = (Building) relatedData;
    Assert.assertEquals("Common Building", b.getName());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void readRelatedEntityWithNull() throws Exception {
    EdmEntitySet buildingsEntitySet = createMockedEdmEntitySet("Buildings");
    EdmEntitySet roomsEntitySet = createMockedEdmEntitySet("Rooms");

    Building building = new Building();
    building.setName("Common Building");
    datasource.createData(buildingsEntitySet, building);

    Room room = new Room(12, "Room 12");
    room.setBuilding(null);
    datasource.createData(roomsEntitySet, room);
    //
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", Integer.valueOf(12));
    Room read = (Room) datasource.readData(roomsEntitySet, keys);
    Assert.assertEquals("Room 12", read.getName());
    Assert.assertEquals("12", read.getId());

    // execute
    Object relatedData = datasource.readRelatedData(
        roomsEntitySet, room, buildingsEntitySet, Collections.EMPTY_MAP);

    // validate
    Assert.assertNull("Related data must be null.", relatedData);
  }

  @Test
  public void readRelatedTargetEntity() throws Exception {
    EdmEntitySet buildingsEntitySet = createMockedEdmEntitySet("Buildings");
    EdmEntitySet roomsEntitySet = createMockedEdmEntitySet("Rooms");

    Building building = new Building();
    building.setName("Common Building");

    final int roomsCount = 10;
    List<Room> rooms = new ArrayList<Room>();
    for (int i = 0; i < roomsCount; i++) {
      Room room = new Room(i, "Room " + i);
      room.setBuilding(building);
      datasource.createData(roomsEntitySet, room);
      rooms.add(room);
    }

    building.getRooms().addAll(rooms);
    datasource.createData(buildingsEntitySet, building);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", "1");

    Building read = (Building) datasource.readData(buildingsEntitySet, keys);
    Assert.assertEquals("Common Building", read.getName());
    Assert.assertEquals("1", read.getId());

    // execute
    Map<String, Object> targetKeys = new HashMap<String, Object>();
    targetKeys.put("Id", 3);
    Object relatedData = datasource.readRelatedData(
        buildingsEntitySet, building, roomsEntitySet, targetKeys);

    // validate
    Assert.assertTrue("Result is no Room.", relatedData instanceof Room);
    Room relatedRoom = (Room) relatedData;
    Assert.assertEquals("3", relatedRoom.getId());
    Assert.assertEquals("Room 3", relatedRoom.getName());
    Assert.assertEquals("Common Building", relatedRoom.getBuilding().getName());
  }

  @Test
  public void createSimpleEntity() throws Exception {
    EdmEntitySet edmEntitySet = createMockedEdmEntitySet("Buildings");

    Building building = new Building();
    building.setName("Common Building");
    datasource.createData(edmEntitySet, building);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", "1");

    Building read = (Building) datasource.readData(edmEntitySet, keys);
    Assert.assertEquals("Common Building", read.getName());
    Assert.assertEquals("1", read.getId());
  }

  @Test
  public void createSimpleEntityWithOwnKey() throws Exception {
    EdmEntitySet edmEntitySet = createMockedEdmEntitySet("Buildings");

    Building building = new Building();
    building.setName("Common Building");
    AnnotationHelper ah = new AnnotationHelper();
    ah.setValueForProperty(building, "Id", "42");
    datasource.createData(edmEntitySet, building);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", "42");

    Building read = (Building) datasource.readData(edmEntitySet, keys);
    Assert.assertEquals("Common Building", read.getName());
    Assert.assertEquals("42", read.getId());
  }

  @Test
  public void createSimpleEntityWithDuplicateKey() throws Exception {
    EdmEntitySet edmEntitySet = createMockedEdmEntitySet("Buildings");
    AnnotationHelper ah = new AnnotationHelper();

    Building building = new Building();
    building.setName("Common Building");
    ah.setValueForProperty(building, "Id", "42");
    datasource.createData(edmEntitySet, building);
    //
    Building buildingDuplicate = new Building();
    buildingDuplicate.setName("Duplicate Building");
    ah.setValueForProperty(buildingDuplicate, "Id", "42");
    datasource.createData(edmEntitySet, buildingDuplicate);

    Map<String, Object> keys42 = new HashMap<String, Object>();
    keys42.put("Id", "42");
    Building read42 = (Building) datasource.readData(edmEntitySet, keys42);
    Assert.assertEquals("Common Building", read42.getName());
    Assert.assertEquals("42", read42.getId());

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", "1");
    Building read = (Building) datasource.readData(edmEntitySet, keys);
    Assert.assertEquals("Duplicate Building", read.getName());
    Assert.assertEquals("1", read.getId());
  }

  @Test
  public void createEntityTwoKeys() throws Exception {
    EdmEntitySet edmEntitySet = createMockedEdmEntitySet("Photos");

    Photo photo = new Photo();
    photo.setName("BigPicture");
    photo.setType("PNG");
    photo.setImageUri("https://localhost/image.png");
    photo.setImageType("image/png");
    datasource.createData(edmEntitySet, photo);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("ImageFormat", "PNG");
    keys.put("Name", "BigPicture");

    Photo read = (Photo) datasource.readData(edmEntitySet, keys);
    Assert.assertEquals("BigPicture", read.getName());
    Assert.assertEquals("PNG", read.getType());
    Assert.assertEquals("image/png", read.getImageType());
    Assert.assertEquals("https://localhost/image.png", read.getImageUri());
  }

  @Test
  public void createAndUpdateEntityTwoKeys() throws Exception {
    EdmEntitySet edmEntitySet = createMockedEdmEntitySet("Photos");

    Photo photo = new Photo();
    final String nameKeyValue = "BigPicture";
    final String typeKeyValue = "PNG";
    photo.setName(nameKeyValue);
    photo.setType(typeKeyValue);
    photo.setImageUri("https://localhost/image.png");
    photo.setImageType("image/png");
    datasource.createData(edmEntitySet, photo);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Name", "BigPicture");
    keys.put("ImageFormat", "PNG");

    Photo read = (Photo) datasource.readData(edmEntitySet, keys);
    Assert.assertEquals("BigPicture", read.getName());
    Assert.assertEquals("PNG", read.getType());
    Assert.assertEquals("image/png", read.getImageType());
    Assert.assertEquals("https://localhost/image.png", read.getImageUri());

    // update
    Photo updatedPhoto = new Photo();
    updatedPhoto.setName(nameKeyValue);
    updatedPhoto.setType(typeKeyValue);
    updatedPhoto.setImageUri("https://localhost/image.jpg");
    updatedPhoto.setImageType("image/jpg");
    datasource.updateData(edmEntitySet, updatedPhoto);

    Map<String, Object> updatedKeys = new HashMap<String, Object>();
    updatedKeys.put("Name", nameKeyValue);
    updatedKeys.put("ImageFormat", typeKeyValue);

    Photo readUpdated = (Photo) datasource.readData(edmEntitySet, updatedKeys);
    Assert.assertEquals("BigPicture", readUpdated.getName());
    Assert.assertEquals("PNG", readUpdated.getType());
    Assert.assertEquals("image/jpg", readUpdated.getImageType());
    Assert.assertEquals("https://localhost/image.jpg", readUpdated.getImageUri());
  }

  @Test
  public void deleteSimpleEntity() throws Exception {
    EdmEntitySet edmEntitySet = createMockedEdmEntitySet("Buildings");
    DataStore<Building> datastore = datasource.getDataStore(Building.class);

    Building building = new Building();
    building.setName("Common Building");
    datastore.create(building);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", "1");

    Building read = (Building) datasource.readData(edmEntitySet, keys);
    Assert.assertEquals("Common Building", read.getName());
    Assert.assertEquals("1", read.getId());

    //
    datasource.deleteData(edmEntitySet, keys);

    // validate
    try {
      Building readAfterDelete = (Building) datasource.readData(edmEntitySet, keys);
      Assert.fail("Expected " + ODataNotFoundException.class + "was not thrown for '" + readAfterDelete + "'.");
    } catch (ODataNotFoundException e) {}
  }

  @Test(expected = AnnotationRuntimeException.class)
  public void unknownEntitySetForEntity() throws Exception {
    String entitySetName = "Unknown";
    FullQualifiedName entityType = new FullQualifiedName(DEFAULT_CONTAINER, entitySetName);

    EdmEntitySet edmEntitySet = Mockito.mock(EdmEntitySet.class);
    Mockito.when(edmEntitySet.getName()).thenReturn(entitySetName);
    EdmEntityType edmEntityType = Mockito.mock(EdmEntityType.class);
    Mockito.when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
    Mockito.when(edmEntityType.getName()).thenReturn(entityType.getName());

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("Id", "1");
    //
    datasource.readData(edmEntitySet, keys);
  }

  @Test(expected = AnnotationRuntimeException.class)
  public void unknownEntitySetForEntities() throws Exception {
    String entitySetName = "Unknown";
    FullQualifiedName entityType = new FullQualifiedName(DEFAULT_CONTAINER, entitySetName);

    EdmEntitySet edmEntitySet = Mockito.mock(EdmEntitySet.class);
    Mockito.when(edmEntitySet.getName()).thenReturn(entitySetName);
    EdmEntityType edmEntityType = Mockito.mock(EdmEntityType.class);
    Mockito.when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
    Mockito.when(edmEntityType.getName()).thenReturn(entityType.getName());

    //
    datasource.readData(edmEntitySet);
  }

  @Test
  public void writeRelations() throws Exception {
    DataStore<Building> buildingStore = InMemoryDataStore.createInMemory(Building.class, true);
    DataStore<Room> roomStore = InMemoryDataStore.createInMemory(Room.class, true);

    EdmEntitySet buildingsEntitySet = createMockedEdmEntitySet("Buildings");
    EdmEntitySet roomsEntitySet = createMockedEdmEntitySet("Rooms");

    Building building = new Building();
    building.setName("Common Building");
    Building created = buildingStore.create(building);

    Room room = new Room(42, "Room with Number");
    room.setSeats(123);
    ;
    room.setVersion(4711);
    roomStore.create(room);

    Map<String, Object> targetEntityKeyValues = new HashMap<String, Object>();
    targetEntityKeyValues.put("Id", 42);

    // execute
    datasource.writeRelation(buildingsEntitySet, building, roomsEntitySet, targetEntityKeyValues);

    // validate
    Building readBuilding = buildingStore.read(created);
    Room readRoom = roomStore.read(new Room(42, ""));

    List<Room> readRooms = readBuilding.getRooms();
    Assert.assertEquals(1, readRooms.size());
    Assert.assertEquals(readRoom, readRooms.get(0));

    Assert.assertEquals("42", readRoom.getId());
    Assert.assertEquals(123, readRoom.getSeats());
    Assert.assertEquals(4711, readRoom.getVersion());
    Assert.assertEquals(readBuilding, readRoom.getBuilding());
  }

  private EdmEntitySet createMockedEdmEntitySet(final String entitySetName) throws ODataException {
    return createMockedEdmEntitySet(edmProvider, entitySetName);
  }

  private EdmEntitySet createMockedEdmEntitySet(final AnnotationEdmProvider edmProvider, final String entitySetName)
      throws ODataException {
    EntitySet entitySet = edmProvider.getEntitySet(DEFAULT_CONTAINER, entitySetName);
    FullQualifiedName entityType = entitySet.getEntityType();

    EdmEntitySet edmEntitySet = Mockito.mock(EdmEntitySet.class);
    Mockito.when(edmEntitySet.getName()).thenReturn(entitySetName);
    EdmEntityType edmEntityType = Mockito.mock(EdmEntityType.class);
    Mockito.when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
    Mockito.when(edmEntityType.getName()).thenReturn(entityType.getName());

    return edmEntitySet;
  }
}
