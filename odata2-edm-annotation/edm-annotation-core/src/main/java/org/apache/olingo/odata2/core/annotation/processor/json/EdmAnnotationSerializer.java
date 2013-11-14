/**
 * *****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *****************************************************************************
 */
package org.apache.olingo.odata2.core.annotation.processor.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;

import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.core.annotation.edm.AnnotationHelper;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

public class EdmAnnotationSerializer {

  private final String baseUri;
  private static final AnnotationHelper ANNOTATION_HELPER = new AnnotationHelper();

  public EdmAnnotationSerializer(String baseUri) {
    this.baseUri = baseUri;
  }

  public InputStream serialize(Object obj) {
    byte[] buf = getContent(obj).getBytes();
    InputStream stream = new ByteArrayInputStream(buf);
    return stream;
  }

  private String getContent(Object entity) {
    if (entity == null) {
      return "NULL";
    } else if (isConsumable(entity)) {
      try {
        return handleEdmAnnotations(entity);
      } catch (Exception e) {
        throw new ODataRuntimeException("Exception with following message occured: " + e.getMessage(), e);
      }
    } else {
      return entity.toString();
    }
  }

  private String handleEdmAnnotations(Object entity)
          throws IllegalArgumentException, IllegalAccessException, IOException {
    //
    Writer writer = new StringWriter();
    JsonWriter json = new JsonWriter(writer);
    json.start();
    writeObject(entity, json);
    json.finish();
    writer.close();
    //
    return writer.toString();
  }

  private void writeObject(Object entity, JsonWriter json) throws IllegalAccessException {
    if (entity.getClass().isArray()) {
      List<Object> entities = Arrays.asList(entity);
      writeCollection(entities, json);
    } else if (Collection.class.isAssignableFrom(entity.getClass())) {
      Collection<Object> entities = (Collection<Object>) entity;
      writeCollection(entities, json);
    } else {
      writeSingleObject(entity, json);
    }
  }

  private void writeCollection(Collection<Object> entities, JsonWriter json) throws IllegalAccessException {
    json.startObject();
    json.writeName("results");
    json.startArray();
    boolean writeSeperator = false;
    for (Object object : entities) {
      if (writeSeperator) {
        json.writeSeparator();
      } else {
        writeSeperator = true;
      }
      writeSingleObject(object, json);
    }
    json.endArray();
    json.endObject();
  }

  private void writeSingleObject(Object entity, JsonWriter json) throws IllegalAccessException {
    List<Field> fields = getAllFields(entity.getClass());
    json.startObject();
    for (Field field : fields) {
      boolean written = writeEdmProperty(entity, json, field);
      if (!written) {
        writeEdmNavigationProperty(entity, json, field);
      }
    }
    json.endObject();
  }

  private boolean writeEdmNavigationProperty(Object entity, JsonWriter json, Field field)
          throws IllegalArgumentException, IllegalAccessException {
    EdmNavigationProperty navProperty = field.getAnnotation(EdmNavigationProperty.class);
    EdmEntitySet entitySet = entity.getClass().getAnnotation(EdmEntitySet.class);
    if (navProperty != null) {
      field.setAccessible(true);
      Object keyValue = extractEdmKey(entity);
      json.writeStringProperty("uri", baseUri + entitySet.name() + "('" + keyValue.toString() + "')"
              + "/" + navProperty.association());
      return true;
    }
    return false;
  }

  private boolean writeEdmProperty(Object entity, JsonWriter json, Field field) throws IllegalAccessException {
    EdmProperty property = field.getAnnotation(EdmProperty.class);
    if (property != null) {
      field.setAccessible(true);
      Object fieldValue = field.get(entity);
      String name = getName(property, field);
      EdmSimpleTypeKind defaultType = getDefaultSimpleTypeKind(field, property);
      try {
        String value = defaultType.getEdmSimpleTypeInstance().valueToString(fieldValue, EdmLiteralKind.JSON, null);
        write(defaultType, name, value, json);
      } catch (EdmSimpleTypeException e) {
        throw new IllegalArgumentException("Illegal argument for valueToString for EdmSimpleType = '"
                + property.type() + "' with message = '" + e.getMessage() + "'.");
      }
      return true;
    }
    return false;
  }

  public void write(EdmSimpleTypeKind kind, String name, String value, JsonWriter jsonStreamWriter) {
    switch (kind) {
      case String:
        jsonStreamWriter.writeStringProperty(name, value);
        break;
      case Boolean:
      case Byte:
      case SByte:
      case Int16:
      case Int32:
        jsonStreamWriter.writeRawProperty(name, value);
        break;
      case DateTime:
      case DateTimeOffset:
        // Although JSON escaping is (and should be) done in the JSON
        // serializer, we backslash-escape the forward slash here explicitly
        // because it is not required to escape it in JSON but in OData.
        jsonStreamWriter.writeRawStringProperty(name, value == null ? null : value.replace("/", "\\/"));
        break;
      default:
        jsonStreamWriter.writeRawStringProperty(name, value);
        break;
    }
  }

  private EdmSimpleTypeKind getDefaultSimpleTypeKind(Field field, EdmProperty property) {
    final EdmSimpleTypeKind type = property.type();
    if (type == EdmSimpleTypeKind.Null) {
      Class<?> fieldType = field.getType();
      if (fieldType == String.class) {
        return EdmSimpleTypeKind.String;
      } else if (fieldType == Long.class) {
        return EdmSimpleTypeKind.Int64;
      } else if (fieldType == Integer.class) {
        return EdmSimpleTypeKind.Int32;
      }
    }
    return type;
  }

  private Class<?> getType(EdmProperty property) {
    Class<?> defaultType = property.type().getEdmSimpleTypeInstance().getDefaultType();
    return defaultType;
  }

  private Object extractEdmKey(Object value) throws IllegalArgumentException, IllegalAccessException {
    Field idField = getFieldWithAnnotation(value.getClass(), EdmKey.class);
    if (idField == null) {
      return "NULL";
    }
    idField.setAccessible(true);
    return idField.get(value);
  }

  private Field getFieldWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClazz) {

    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (field.getAnnotation(annotationClazz) != null) {
        return field;
      }
    }

    Class<?> superclass = clazz.getSuperclass();
    if (superclass != Object.class) {
      return getFieldWithAnnotation(superclass, annotationClazz);
    }

    return null;
  }

  private List<Field> getAllFields(Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();
    List<Field> allFields = new ArrayList<Field>(Arrays.asList(fields));

    final Class<?> superclass = clazz.getSuperclass();
    if (superclass != Object.class) {
      allFields.addAll(getAllFields(superclass));
    }

    return allFields;
  }

  private boolean isConsumable(Object entity) {
    if (entity == null) {
      return false;
    } else if (ANNOTATION_HELPER.isEdmAnnotated(entity)) {
      return true;
    } else if (entity.getClass().isArray() || entity.getClass().isAssignableFrom(Collection.class)) {
      return true;
    } else if (Collection.class.isAssignableFrom(entity.getClass())) {
      return true;
    }
    return false;
  }

  private String getName(EdmProperty property, Field field) {
    String name = property.name();
    if (name.isEmpty()) {
      name = ANNOTATION_HELPER.getCanonicalName(field);
    }
    return name;
  }
}
