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
package org.apache.olingo.odata2.core.ep.producer;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.core.edm.EdmAuto;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.aggregator.EntityComplexPropertyInfo;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.util.FormatJson;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;

/**
 * Producer for writing a single simple or complex property in JSON, also usable
 * for function imports returning a single instance of a simple or complex type.
 * 
 */
public class JsonPropertyEntityProducer {

  public void append(final Writer writer, final EntityPropertyInfo propertyInfo, final Object value)
      throws EntityProviderException {
    JsonStreamWriter jsonStreamWriter = new JsonStreamWriter(writer);

    try {
      jsonStreamWriter.beginObject()
          .name(FormatJson.D)
          .beginObject();

      jsonStreamWriter.name(propertyInfo.getName());
      appendPropertyValue(jsonStreamWriter, propertyInfo.isComplex() ? (EntityComplexPropertyInfo) propertyInfo
          : propertyInfo, value, true, false);

      jsonStreamWriter.endObject()
          .endObject();
    } catch (final IOException e) {
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    } catch (final EdmException e) {
      throw new EntityProviderProducerException(e.getMessageReference(), e);
    }
  }

  protected static void appendPropertyValue(final JsonStreamWriter jsonStreamWriter,
                                            final EntityPropertyInfo propertyInfo, final Object value,
                                            boolean validatingFacets,
                                            boolean isDataBasedPropertySerialization) throws IOException, EdmException,
      EntityProviderException {
    if (propertyInfo.isComplex()) {
      if (value == null || value instanceof Map<?, ?>) {
        jsonStreamWriter.beginObject();
        appendPropertyMetadata(jsonStreamWriter, propertyInfo.getType());
        if (value == null && isDataBasedPropertySerialization) {
          jsonStreamWriter.endObject();
          return;
        }
        for (final EntityPropertyInfo childPropertyInfo : ((EntityComplexPropertyInfo) propertyInfo)
            .getPropertyInfos()) {
          final String name = childPropertyInfo.getName();
          if (isDataBasedPropertySerialization && !((Map<?,?>)value).containsKey(name)) {
            continue;
          } 
          jsonStreamWriter.separator();
          jsonStreamWriter.name(name);
          appendPropertyValue(jsonStreamWriter, childPropertyInfo,
              value == null ? null : ((Map<?, ?>) value).get(name), validatingFacets, isDataBasedPropertySerialization);
        }
        jsonStreamWriter.endObject();
      } else {
        throw new EntityProviderProducerException(EntityProviderException.ILLEGAL_ARGUMENT
            .addContent("A complex property must have a Map as data"));
      }
    } else {
      EdmSimpleType type = (EdmSimpleType) propertyInfo.getType();
      if (type instanceof EdmAuto) {
        type = ((EdmAuto) type).getType(value);
      }
      final Object contentValue = value instanceof Map ? ((Map<?, ?>) value).get(propertyInfo.getName()) : value;
      final EdmFacets facets = validatingFacets ? propertyInfo.getFacets(): null;
      String valueAsString = null;
      try {
      valueAsString = type.valueToString(contentValue, EdmLiteralKind.JSON, facets);
      } catch (EdmSimpleTypeException e) {
        throw new EntityProviderProducerException(EdmSimpleTypeException.getMessageReference(
            e.getMessageReference()).updateContent(e.getMessageReference().getContent(), 
                propertyInfo.getName()), e);
      }
      switch (EdmSimpleTypeKind.valueOf(type.getName())) {
      case String:
        jsonStreamWriter.stringValue(valueAsString);
        break;
      case Boolean:
      case Byte:
      case SByte:
      case Int16:
      case Int32:
      case Double:
      case Single:
      case Int64:
          jsonStreamWriter.unquotedValue(valueAsString);
        break;
      case DateTime:
      case DateTimeOffset:
        // Although JSON escaping is (and should be) done in the JSON
        // serializer, we backslash-escape the forward slash here explicitly
        // because it is not required to escape it in JSON but in OData.
        jsonStreamWriter.stringValueRaw(valueAsString == null ? null : valueAsString.replace("/", "\\/"));
        break;
      default:
        jsonStreamWriter.stringValueRaw(valueAsString);
        break;
      }
    }
  }

  protected static void appendPropertyMetadata(final JsonStreamWriter jsonStreamWriter, final EdmType type)
      throws IOException, EdmException {
    jsonStreamWriter.name(FormatJson.METADATA)
        .beginObject()
        .namedStringValueRaw(FormatJson.TYPE, type.getNamespace() + Edm.DELIMITER + type.getName())
        .endObject();
  }
}
