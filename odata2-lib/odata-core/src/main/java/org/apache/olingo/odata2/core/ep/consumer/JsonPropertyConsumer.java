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
package org.apache.olingo.odata2.core.ep.consumer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.core.ep.aggregator.EntityComplexPropertyInfo;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.util.FormatJson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * JSON property consumer.
 */
public class JsonPropertyConsumer {

  public Map<String, Object> readPropertyStandalone(final JsonReader reader, final EdmProperty property,
      final EntityProviderReadProperties readProperties) throws EntityProviderException {
    try {
      EntityPropertyInfo entityPropertyInfo = EntityInfoAggregator.create(property);
      Map<String, Object> typeMappings = readProperties == null ? null : readProperties.getTypeMappings();
      Map<String, Object> result = new HashMap<String, Object>();

      reader.beginObject();
      String nextName = reader.nextName();
      if (FormatJson.D.equals(nextName)) {
        reader.beginObject();
        nextName = reader.nextName();
        handleName(reader, typeMappings, entityPropertyInfo, readProperties, result, nextName);
        reader.endObject();
      } else {
        handleName(reader, typeMappings, entityPropertyInfo, readProperties, result, nextName);
      }
      reader.endObject();

      if (reader.peek() != JsonToken.END_DOCUMENT) {
        throw new EntityProviderException(EntityProviderException.END_DOCUMENT_EXPECTED.addContent(reader.peek()
            .toString()));
      }

      return result;
    } catch (final IOException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    } catch (final IllegalStateException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  private void handleName(final JsonReader reader, final Map<String, Object> typeMappings,
      final EntityPropertyInfo entityPropertyInfo, final EntityProviderReadProperties readProperties,
      final Map<String, Object> result, final String nextName) throws EntityProviderException {
    if (!entityPropertyInfo.getName().equals(nextName)) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT.addContent(nextName));
    }
    Object mapping = null;
    if (typeMappings != null) {
      mapping = typeMappings.get(nextName);
    }
    Object propertyValue = readPropertyValue(reader, entityPropertyInfo, mapping, readProperties);
    result.put(nextName, propertyValue);
  }

  protected Object readPropertyValue(final JsonReader reader, final EntityPropertyInfo entityPropertyInfo,
      final Object typeMapping, final EntityProviderReadProperties readProperties) throws EntityProviderException {
    try {
      return entityPropertyInfo.isComplex() ?
          readComplexProperty(reader, (EntityComplexPropertyInfo) entityPropertyInfo, typeMapping, readProperties) :
          readSimpleProperty(reader, entityPropertyInfo, typeMapping, readProperties);
    } catch (final EdmException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    } catch (final IOException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  private Object readSimpleProperty(final JsonReader reader, final EntityPropertyInfo entityPropertyInfo,
      final Object typeMapping, final EntityProviderReadProperties readProperties)
      throws EdmException, EntityProviderException, IOException {
    final EdmSimpleType type = (EdmSimpleType) entityPropertyInfo.getType();
    Object value = null;
    final JsonToken tokenType = reader.peek();
    if (tokenType == JsonToken.NULL) {
      reader.nextNull();
    } else {
      switch (EdmSimpleTypeKind.valueOf(type.getName())) {
      case Boolean:
        if (tokenType == JsonToken.BOOLEAN) {
          value = reader.nextBoolean();
          value = value.toString();
        } else {
          throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY_VALUE
              .addContent(entityPropertyInfo.getName()));
        }
        break;
      case Byte:
      case SByte:
      case Int16:
      case Int32:
        if (tokenType == JsonToken.NUMBER) {
          value = reader.nextInt();
          value = value.toString();
        } else {
          throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY_VALUE
              .addContent(entityPropertyInfo.getName()));
        }
        break;
      case Single:
        if (tokenType == JsonToken.STRING) {
          value = reader.nextString();
        } else if (tokenType == JsonToken.NUMBER) {
          value = reader.nextDouble();
          value = value.toString();
        } else {
          throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY_VALUE
              .addContent(entityPropertyInfo.getName()));
        }
        break;
      default:
        if (tokenType == JsonToken.STRING) {
          value = reader.nextString();
        } else {
          throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY_VALUE
              .addContent(entityPropertyInfo.getName()));
        }
        break;
      }
    }

    final Class<?> typeMappingClass = typeMapping == null ? type.getDefaultType() : (Class<?>) typeMapping;
    final EdmFacets facets = readProperties == null || readProperties.isValidatingFacets() ?
        entityPropertyInfo.getFacets() : null;
    return type.valueOfString((String) value, EdmLiteralKind.JSON, facets, typeMappingClass);
  }

  @SuppressWarnings("unchecked")
  private Object readComplexProperty(final JsonReader reader, final EntityComplexPropertyInfo complexPropertyInfo,
      final Object typeMapping, final EntityProviderReadProperties readProperties)
      throws EdmException, EntityProviderException, IOException {
    if (reader.peek().equals(JsonToken.NULL)) {
      reader.nextNull();
      if ((readProperties == null || readProperties.isValidatingFacets()) && complexPropertyInfo.isMandatory()) {
        throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY_VALUE.addContent(complexPropertyInfo
            .getName()));
      }
      return null;
    }

    reader.beginObject();
    Map<String, Object> data = new HashMap<String, Object>();

    Map<String, Object> mapping;
    if (typeMapping != null) {
      if (typeMapping instanceof Map) {
        mapping = (Map<String, Object>) typeMapping;
      } else {
        throw new EntityProviderException(EntityProviderException.INVALID_MAPPING.addContent(complexPropertyInfo
            .getName()));
      }
    } else {
      mapping = new HashMap<String, Object>();
    }

    while (reader.hasNext()) {
      String childName = reader.nextName();
      if (FormatJson.METADATA.equals(childName)) {
        reader.beginObject();
        childName = reader.nextName();
        if (!FormatJson.TYPE.equals(childName)) {
          throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE.addContent(FormatJson.TYPE)
              .addContent(FormatJson.METADATA));
        }
        String actualTypeName = reader.nextString();
        String expectedTypeName =
            complexPropertyInfo.getType().getNamespace() + Edm.DELIMITER + complexPropertyInfo.getType().getName();
        if (!expectedTypeName.equals(actualTypeName)) {
          throw new EntityProviderException(EntityProviderException.INVALID_ENTITYTYPE.addContent(expectedTypeName)
              .addContent(actualTypeName));
        }
        reader.endObject();
      } else {
        EntityPropertyInfo childPropertyInfo = complexPropertyInfo.getPropertyInfo(childName);
        if (childPropertyInfo == null) {
          throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY.addContent(childName));
        }
        Object childData = readPropertyValue(reader, childPropertyInfo, mapping.get(childName), readProperties);
        if (data.containsKey(childName)) {
          throw new EntityProviderException(EntityProviderException.DOUBLE_PROPERTY.addContent(childName));
        }
        data.put(childName, childData);
      }
    }
    reader.endObject();
    return data;
  }
}
