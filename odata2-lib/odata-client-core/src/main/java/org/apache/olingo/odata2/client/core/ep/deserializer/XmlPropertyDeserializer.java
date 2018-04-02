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
package org.apache.olingo.odata2.client.core.ep.deserializer;

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
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.client.api.ep.DeserializerProperties;
import org.apache.olingo.odata2.core.ep.aggregator.EntityComplexPropertyInfo;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.aggregator.EntityTypeMapping;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

/**
 * XML property consumer.
 */
public class XmlPropertyDeserializer {

  protected static final String TRUE = "true";
  protected static final String FALSE = "false";

  /**
   * Read property of every entry in a payload
   * @param reader
   * @param property
   * @param readProperties
   * @return Map<String, Object>
   * @throws EntityProviderException
   */
  public Map<String, Object> readProperty(final XMLStreamReader reader, final EdmProperty property,
      final DeserializerProperties readProperties) throws EntityProviderException {
    return readProperty(reader, EntityInfoAggregator.create(property), readProperties);
  }

  /**
   * Read property of every entry in a payload
   * @param reader
   * @param propertyInfo
   * @param readProperties
   * @return Map<String, Object>
   * @throws EntityProviderException
   */
  public Map<String, Object> readProperty(final XMLStreamReader reader, final EntityPropertyInfo propertyInfo,
      final DeserializerProperties readProperties) throws EntityProviderException {
    final EntityTypeMapping typeMappings =
        EntityTypeMapping.create(readProperties  == null ? Collections.<String, Object> emptyMap() :
      readProperties.getTypeMappings());
    try {
      reader.next();

      Object value = readStartedElement(reader, propertyInfo.getName(), propertyInfo, typeMappings, readProperties);

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


  /**
   * Deserializes a collection of entities
   * @param reader
   * @param info
   * @param properties
   * @return List<Object>
   * @throws EntityProviderException
   */
  public List<Object> readCollection(XMLStreamReader reader, final EntityPropertyInfo info,
      final DeserializerProperties properties) throws EntityProviderException {
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

  protected Object readStartedElement(XMLStreamReader reader, final String name, //NOSONAR
      final EntityPropertyInfo propertyInfo, 
      final EntityTypeMapping typeMappings, final DeserializerProperties readProperties)
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
          throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY_VALUE.addContent(name));
        }
        reader.nextTag();
      } else if (propertyInfo.isComplex()) {
        final String typeAttribute = reader.getAttributeValue(Edm.NAMESPACE_M_2007_08, FormatXml.M_TYPE);
        if (typeAttribute != null) {
          final String expectedTypeAttributeValue =
              propertyInfo.getType().getNamespace() + Edm.DELIMITER + propertyInfo.getType().getName();
          if (!expectedTypeAttributeValue.equals(typeAttribute)) { //NOSONAR
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
          if (childProperty == null) { //NOSONAR
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
      final DeserializerProperties readProperties) throws EdmSimpleTypeException {
    final EdmSimpleType type = (EdmSimpleType) property.getType();
    return type.valueOfString(value, EdmLiteralKind.DEFAULT,
        readProperties == null || readProperties.isValidatingFacets() ? property.getFacets() : null,
        typeMapping == null ? type.getDefaultType() : typeMapping);
  }
}
