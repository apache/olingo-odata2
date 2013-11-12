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
package org.apache.olingo.odata2.core.annotation.processor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmMediaResourceContent;
import org.apache.olingo.odata2.api.annotation.edm.EdmMediaResourceMimeType;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.annotation.edm.ds.EntityDataSource;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetMediaResourceUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.apache.olingo.odata2.core.annotation.ds.DataSourceHolder;
import org.apache.olingo.odata2.core.annotation.edm.AnnotationHelper;
import org.apache.olingo.odata2.core.annotation.edm.ClassHelper;
import org.apache.olingo.odata2.core.annotation.processor.json.EdmAnnotationSerializer;
import org.apache.olingo.odata2.core.annotation.processor.json.JsonConsumer;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *
 */
public class AnnotationProcessor extends ODataSingleProcessor {

  private static final AnnotationHelper ANNOTATION_HELPER = new AnnotationHelper();

  private final Map<String, DataSourceHolder> dataSources = new HashMap<String, DataSourceHolder>();
  private ODataContext odataContext;

  public AnnotationProcessor(ODataContext context, String packageToScan) {
    odataContext = context;

    List<Class<?>> foundClasses = ClassHelper.loadClasses(packageToScan, new ClassHelper.ClassValidator() {
      @Override
      public boolean isClassValid(Class<?> c) {
        return null != c.getAnnotation(EntityDataSource.class);
      }
    });

    init(foundClasses);
  }

  private void init(List<Class<?>> foundClasses) {
    for (Class<?> clz : foundClasses) {
      DataSourceHolder dhs = new DataSourceHolder(clz);
      dataSources.put(dhs.getEntityName(), dhs);
    }
  }

  @Override
  public ODataResponse readEntity(GetEntityUriInfo uriInfo, String contentType) throws ODataException {
    final String name = uriInfo.getTargetType().getName();

    List<KeyPredicate> keys = uriInfo.getKeyPredicates();
    DataSourceHolder holder = dataSources.get(name);
    if (holder != null) {
      Object result = holder.readEntity(keys);
      if (result != null) {
        return createODataResponse(result, HttpStatusCodes.OK);
      }
    }

    return ODataResponse.status(HttpStatusCodes.NOT_FOUND).build();
  }

  @Override
  public ODataResponse readEntitySet(GetEntitySetUriInfo uriInfo, String contentType) throws ODataException {
    final String name = uriInfo.getTargetType().getName();

    DataSourceHolder holder = dataSources.get(name);
    if (holder != null) {
      Object result = holder.readEntitySet();
      if (result != null) {
        return createODataResponse(result, HttpStatusCodes.OK);
      }
    }

    return ODataResponse.status(HttpStatusCodes.NOT_FOUND).build();
  }

  @Override
  public ODataResponse createEntity(PostUriInfo uriInfo, InputStream content,
          String requestContentType, String contentType) throws ODataException {
    final String name = uriInfo.getTargetType().getName();

    DataSourceHolder dataSource = dataSources.get(name);
    if (dataSource != null) {
      Object instance = createInstanceFromContent(dataSource, content);

      Object result = dataSource.createEntity(instance);
      return createODataResponse(result, HttpStatusCodes.CREATED);
    }

    return ODataResponse.status(HttpStatusCodes.BAD_REQUEST).build();
  }

  @Override
  public ODataResponse deleteEntity(DeleteUriInfo uriInfo, String contentType) throws ODataException {
    final String name = uriInfo.getTargetType().getName();

    DataSourceHolder holder = dataSources.get(name);
    if (holder != null) {
      List<KeyPredicate> keys = uriInfo.getKeyPredicates();
      Object result = holder.deleteEntity(keys);
      if (result != null) {
        return ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
      }
    }

    return ODataResponse.status(HttpStatusCodes.NOT_FOUND).build();
  }

  @Override
  public ODataResponse readEntityMedia(GetMediaResourceUriInfo uriInfo, String contentType) throws ODataException {
    final String name = uriInfo.getTargetType().getName();

    List<KeyPredicate> keys = uriInfo.getKeyPredicates();
    Object result = readEntity(name, keys);
    if (result != null) {
      Object content = new AnnotationHelper().getValueForField(result, EdmMediaResourceContent.class);
      Object mimeType = new AnnotationHelper().getValueForField(result, EdmMediaResourceMimeType.class);
    return ODataResponse.status(HttpStatusCodes.OK).entity(content).contentHeader(String.valueOf(mimeType)).build();
    }

    return ODataResponse.status(HttpStatusCodes.NOT_FOUND).build();
  }
  
  private Object readEntity(String entityName, List<KeyPredicate> keys) {
    DataSourceHolder holder = dataSources.get(entityName);
    if (holder != null) {
      return holder.readEntity(keys);
    }
    return null;
  }
  
  
  private ODataResponse createODataResponse(Object result, HttpStatusCodes statusCode) throws ODataException {
//    StringBuilder resultAsString = new StringBuilder("{\"d\":");
//    if (result != null) {
//      resultAsString.append(result.toString());
//    } else {
//      resultAsString.append("NULL");
//    }
//    resultAsString.append("}");
    EdmAnnotationSerializer jsonSerializer = 
            new EdmAnnotationSerializer(odataContext.getPathInfo().getServiceRoot().toASCIIString());
    InputStream resultAsString = jsonSerializer.serialize(result);
    return ODataResponse.status(statusCode)
            .contentHeader("application/json")
            .entity(resultAsString).build();
  }

  private Object createInstanceFromContent(DataSourceHolder dataSource, InputStream content) 
          throws ODataException {
    try {
      Object instance = dataSource.createEntityInstance();
      Map<String, FieldHolder> propName2Field = extractPropertyFields(dataSource.getEntityTypeClass());
      Map<String, String> contentAsMap = JsonConsumer.readContent(content);
      Set<Map.Entry<String, String>> contentEntries = contentAsMap.entrySet();
      for (Map.Entry<String, String> entry : contentEntries) {
        FieldHolder fh = propName2Field.get(entry.getKey());
        if (fh != null) {
          fh.set(instance, entry.getValue());
        }
      }
      return instance;      
    } catch (IOException e) {
      throw new ODataRuntimeException("Unexpected IOException with message: " + e.getMessage());
    }
  }

  @Override
  public ODataResponse updateEntity(PutMergePatchUriInfo uriInfo, InputStream content, 
          String requestContentType, boolean merge, String contentType) throws ODataException {
    final String name = uriInfo.getTargetType().getName();

    DataSourceHolder dataSource = dataSources.get(name);
    if (dataSource != null) {
      Object instance = createInstanceFromContent(dataSource, content);

      Object result = dataSource.updateEntity(instance);
      return createODataResponse(result, HttpStatusCodes.OK);
    }

    return ODataResponse.status(HttpStatusCodes.BAD_REQUEST).build();
  }

  private Map<String, FieldHolder> extractPropertyFields(Class<?> typeClass) {
    if (typeClass == null) {
      return Collections.emptyMap();
    }
    EdmEntityType type = typeClass.getAnnotation(EdmEntityType.class);
    if (type == null) {
      return Collections.emptyMap();
    }

    //
    Map<String, FieldHolder> name2Fields = new HashMap<String, FieldHolder>();
    Field[] fields = typeClass.getDeclaredFields();
    for (Field field : fields) {
      FieldHolder fh = FieldHolder.create(field);
      name2Fields.put(fh.propertyName, fh);
    }

    //
    Class<?> superClass = typeClass.getSuperclass();
    if (superClass != null && superClass.getAnnotation(EdmEntityType.class) != null) {
      name2Fields.putAll(extractPropertyFields(superClass));
    }
    //

    return name2Fields;
  }

  /**
   *
   */
  static final class FieldHolder {

    final String propertyName;
    final Field propertyField;

    public FieldHolder(String propertyName, Field propertyField) {
      this.propertyName = propertyName;
      this.propertyField = propertyField;
    }

    public static FieldHolder create(Field field) {
      EdmProperty ep = field.getAnnotation(EdmProperty.class);
      String name;
      if (ep != null && !ep.name().isEmpty()) {
        name = ep.name();
      } else {
        name = ANNOTATION_HELPER.getCanonicalName(field);
      }
      return new FieldHolder(name, field);
    }

    public void set(Object instance, Object value) {
      set(instance, String.valueOf(value));
    }

    public void set(Object instance, String value) {
      try {
        boolean accessible = propertyField.isAccessible();
        if (!accessible) {
          propertyField.setAccessible(true);
        }

        //
        Class<?> type = propertyField.getType();
        if (type == Boolean.class || type == boolean.class) {
          propertyField.set(instance, Boolean.valueOf(value));
        } else if (type == Integer.class || type == int.class) {
          propertyField.set(instance, Integer.valueOf(value));
        } else if (type == Long.class || type == long.class) {
          propertyField.set(instance, Integer.valueOf(value));
        } else {
          propertyField.set(instance, value);
        }
        //

        if (!accessible) {
          propertyField.setAccessible(false);
        }
      } catch (Exception ex) {
      }
    }

    @Override
    public String toString() {
      return "FieldHolder{" + "propertyName=" + propertyName + ", propertyField=" + propertyField + '}';
    }
  }
}
