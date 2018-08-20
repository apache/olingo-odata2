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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.core.ep.aggregator.EntityComplexPropertyInfo;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.aggregator.EntityTypeMapping;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

/**
 * XML property consumer.
 */
public class XmlPropertyConsumer {

  protected static final String TRUE = "true";
  protected static final String FALSE = "false";

  public Map<String, Object> readProperty(final XMLStreamReader reader, final EdmProperty property,
      final EntityProviderReadProperties readProperties) throws EntityProviderException {
    return readProperty(reader, EntityInfoAggregator.create(property), readProperties);
  }

  public Map<String, Object> readProperty(final XMLStreamReader reader, final EntityPropertyInfo propertyInfo,
      final EntityProviderReadProperties readProperties) throws EntityProviderException {
    final EntityTypeMapping typeMappings =
        EntityTypeMapping.create(readProperties == null ? Collections.<String, Object> emptyMap() :
          readProperties.getTypeMappings());
    final boolean merge = readProperties != null && readProperties.getMergeSemantic();
    try {
      reader.next();

      Object value = readStartedElement(reader, propertyInfo.getName(), propertyInfo, typeMappings, readProperties);

      if (propertyInfo.isComplex() && merge) {
        mergeWithDefaultValues(value, propertyInfo);
      }

      Map<String, Object> result = new HashMap<String, Object>();
      result.put(propertyInfo.getName(), value);
      return result;
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    } catch (EdmException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  @SuppressWarnings("unchecked")
  private void mergeWithDefaultValues(final Object value, final EntityPropertyInfo epi) throws EntityProviderException {
    if (!(value instanceof Map)) {
      throw new EntityProviderException(EntityProviderException.COMMON);
    }
    if (!epi.isComplex()) {
      throw new EntityProviderException(EntityProviderException.COMMON);
    }

    mergeComplexWithDefaultValues((Map<String, Object>) value, (EntityComplexPropertyInfo) epi);
  }

  private void mergeComplexWithDefaultValues(final Map<String, Object> complexValue,
      final EntityComplexPropertyInfo ecpi) {
    for (EntityPropertyInfo info : ecpi.getPropertyInfos()) {
      Object obj = complexValue.get(info.getName());
      if (obj == null) {
        if (info.isComplex()) {
          Map<String, Object> defaultValue = new HashMap<String, Object>();
          mergeComplexWithDefaultValues(defaultValue, ecpi);
          complexValue.put(info.getName(), defaultValue);
        } else {
          EdmFacets facets = info.getFacets();
          if (facets != null) {
            complexValue.put(info.getName(), facets.getDefaultValue());
          }
        }
      }
    }
  }

  public List<?> readCollection(XMLStreamReader reader, final EntityPropertyInfo info,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    final String collectionName = info.getName();
    final EntityTypeMapping typeMappings = EntityTypeMapping.create(
        properties == null || !properties.getTypeMappings().containsKey(collectionName) ?
            Collections.<String, Object> emptyMap() :
            Collections.<String, Object> singletonMap(FormatXml.D_ELEMENT,
                properties.getTypeMappings().get(collectionName)));
    List<Object> result = new ArrayList<Object>();
    try {
      reader.nextTag();
      reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_D_2007_08, collectionName);
      reader.nextTag();
      while (reader.isStartElement()) {
        result.add(readStartedElement(reader, FormatXml.D_ELEMENT, info, typeMappings, properties));
        reader.nextTag();
      }
      reader.require(XMLStreamConstants.END_ELEMENT, Edm.NAMESPACE_D_2007_08, collectionName);
      reader.next();
      reader.require(XMLStreamConstants.END_DOCUMENT, null, null);
      return result;
    } catch (final XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED
          .addContent(e.getClass().getSimpleName()), e);
    } catch (final EdmException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED
          .addContent(e.getClass().getSimpleName()), e);
    }
  }

  protected Object readStartedElement(XMLStreamReader reader, final String name, final EntityPropertyInfo propertyInfo,
      final EntityTypeMapping typeMappings, final EntityProviderReadProperties readProperties)
      throws EntityProviderException, EdmException {
    Object result = null;

    try {
      reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_D_2007_08, name);
      final String nullAttribute = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, FormatXml.M_NULL);

      if (!(nullAttribute == null || TRUE.equals(nullAttribute) || FALSE.equals(nullAttribute))) {
        throw new EntityProviderException(EntityProviderException.COMMON);
      }

      if (TRUE.equals(nullAttribute)) {
        if ((readProperties == null || readProperties.isValidatingFacets()) && propertyInfo.isMandatory()) {
          throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_NULL_NOT_ALLOWED.addContent(name));
        }
        reader.nextTag();
      } else if (propertyInfo.isComplex()) {
        final String typeAttribute = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, FormatXml.M_TYPE);
        if (typeAttribute != null) {
          final String expectedTypeAttributeValue =
              propertyInfo.getType().getNamespace() + Edm.DELIMITER + propertyInfo.getType().getName();
          if (!expectedTypeAttributeValue.equals(typeAttribute)) {
            throw new EntityProviderException(EntityProviderException.INVALID_COMPLEX_TYPE.addContent(
                expectedTypeAttributeValue).addContent(typeAttribute));
          }
        }

        reader.nextTag();
        Map<String, Object> name2Value = new HashMap<String, Object>();
        while (reader.hasNext() && !reader.isEndElement()) {
          final String childName = reader.getLocalName();
          final EntityPropertyInfo childProperty =
              ((EntityComplexPropertyInfo) propertyInfo).getPropertyInfo(childName);
          if (childProperty == null) {
            throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY.addContent(childName));
          }
          final Object value = readStartedElement(reader, childName, childProperty,
              typeMappings.getEntityTypeMapping(name), readProperties);
          name2Value.put(childName, value);
          reader.nextTag();
        }
        result = name2Value;
      } else {
        result = convert(propertyInfo, reader.getElementText(), typeMappings.getMappingClass(name), readProperties);
      }
      reader.require(XMLStreamConstants.END_ELEMENT, Edm.NAMESPACE_D_2007_08, name);

      return result;
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  private Object convert(final EntityPropertyInfo property, final String value, final Class<?> typeMapping,
      final EntityProviderReadProperties readProperties) throws EdmSimpleTypeException {
    final EdmSimpleType type = (EdmSimpleType) property.getType();
    return type.valueOfString(value, EdmLiteralKind.DEFAULT,
        readProperties == null || readProperties.isValidatingFacets() ? property.getFacets() : null,
        typeMapping == null ? type.getDefaultType() : typeMapping);
  }
}
