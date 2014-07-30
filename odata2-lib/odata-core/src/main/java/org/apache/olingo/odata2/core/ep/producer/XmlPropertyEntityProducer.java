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

import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmCustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.core.ep.aggregator.EntityComplexPropertyInfo;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

/**
 * Internal EntityProvider for simple and complex EDM properties which are pre-analyzed as {@link EntityPropertyInfo}.
 * 
 */
public class XmlPropertyEntityProducer {

  private final boolean inculdeSimplePropertyType;

  public XmlPropertyEntityProducer(final boolean inculdeSimplePropertyType) {
    this.inculdeSimplePropertyType = inculdeSimplePropertyType;
  }

  /**
   * Append {@link Object} <code>value</code> based on {@link EntityPropertyInfo} to {@link XMLStreamWriter} in an
   * already existing XML structure inside the d namespace.
   * 
   * @param writer
   * @param name Name of the outer XML tag
   * @param propertyInfo
   * @param value
   * @throws EntityProviderException
   */
  public void append(final XMLStreamWriter writer, final String name, final EntityPropertyInfo propertyInfo,
      final Object value) throws EntityProviderException {
    try {
      writer.writeStartElement(Edm.NAMESPACE_D_2007_08, name);

      if (propertyInfo.isComplex()) {
        appendProperty(writer, (EntityComplexPropertyInfo) propertyInfo, value);
      } else {
        appendProperty(writer, propertyInfo, value);
      }

      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    } catch (EdmException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  public void appendCustomProperty(final XMLStreamWriter writer, final String name,
      final EntityPropertyInfo propertyInfo, final Object value) throws EntityProviderException {
    try {
      if (!propertyInfo.isComplex()) {
        writeStartElementWithCustomNamespace(writer, propertyInfo, name);
        appendProperty(writer, propertyInfo, value);
        writer.writeEndElement();
      }
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    } catch (EdmException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  /**
   * Append {@link Object} <code>value</code> based on {@link EntityPropertyInfo} to {@link XMLStreamWriter} as a
   * stand-alone XML structure, including writing of default namespace declarations.
   * The name of the outermost XML element comes from the {@link EntityPropertyInfo}.
   * 
   * @param writer
   * @param propertyInfo
   * @param value
   * @throws EntityProviderException
   */
  public void append(final XMLStreamWriter writer, final EntityPropertyInfo propertyInfo, final Object value)
      throws EntityProviderException {
    try {
      writer.writeStartElement(propertyInfo.getName());
      writer.writeDefaultNamespace(Edm.NAMESPACE_D_2007_08);
      writer.writeNamespace(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);

      if (propertyInfo.isComplex()) {
        appendProperty(writer, (EntityComplexPropertyInfo) propertyInfo, value);
      } else {
        appendProperty(writer, propertyInfo, value);
      }

      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    } catch (EdmException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  /**
   * 
   * @param writer
   * @param propertyInfo
   * @param value
   * @throws XMLStreamException
   * @throws EdmException
   * @throws EntityProviderException
   */
  private void appendProperty(final XMLStreamWriter writer, final EntityComplexPropertyInfo propertyInfo,
      final Object value) throws XMLStreamException, EdmException, EntityProviderException {

    if (value == null) {
      writer.writeAttribute(Edm.NAMESPACE_M_2007_08, FormatXml.ATOM_NULL, FormatXml.ATOM_VALUE_TRUE);
    } else {
      writer.writeAttribute(Edm.NAMESPACE_M_2007_08, FormatXml.ATOM_TYPE, getFqnTypeName(propertyInfo));
      List<EntityPropertyInfo> propertyInfos = propertyInfo.getPropertyInfos();
      for (EntityPropertyInfo childPropertyInfo : propertyInfos) {
        Object childValue = extractChildValue(value, childPropertyInfo.getName());
        append(writer, childPropertyInfo.getName(), childPropertyInfo, childValue);
      }
    }
  }

  /**
   * Returns full qualified name of a type of a given PropertyInfo.
   * @return Full qualified name
   */
  private String getFqnTypeName(final EntityComplexPropertyInfo propertyInfo) throws EdmException {
    return propertyInfo.getType().getNamespace() + Edm.DELIMITER + propertyInfo.getType().getName();
  }

  /**
   * If <code>value</code> is a {@link Map} the element with given <code>name</code> as key is returned.
   * If <code>value</code> is NOT a {@link Map} its {@link String#valueOf(Object)} result is returned.
   * 
   * @param value
   * @param name
   * @return name or result (see above)
   */
  private Object extractChildValue(final Object value, final String name) {
    if (value instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) value;
      return map.get(name);
    }
    return String.valueOf(value);
  }

  /**
   * Appends a simple-property value to the XML stream.
   * @param writer the XML stream writer
   * @param prop property informations
   * @param value the value of the property
   * @throws XMLStreamException
   * @throws EdmException
   */
  private void appendProperty(final XMLStreamWriter writer, final EntityPropertyInfo prop, final Object value)
      throws XMLStreamException, EdmException {
    Object contentValue = value;
    String mimeType = null;
    if (prop.getMimeType() != null) {
      mimeType = prop.getMimeType();
    } else if (prop.getMapping() != null && prop.getMapping().getMediaResourceMimeTypeKey() != null) {
      mimeType = (String) extractChildValue(value, prop.getMapping().getMediaResourceMimeTypeKey());
      contentValue = extractChildValue(value, prop.getName());
    }

    if (mimeType != null) {
      writer.writeAttribute(Edm.NAMESPACE_M_2007_08, FormatXml.M_MIME_TYPE, mimeType);
    }

    final EdmSimpleType type = (EdmSimpleType) prop.getType();
    if (inculdeSimplePropertyType) {
      String fqnTypeName = type.getNamespace() + Edm.DELIMITER + type.getName();
      writer.writeAttribute(Edm.NAMESPACE_M_2007_08, FormatXml.ATOM_TYPE, fqnTypeName);
    }

    final String valueAsString = type.valueToString(contentValue, EdmLiteralKind.DEFAULT, prop.getFacets());
    if (valueAsString == null) {
      writer.writeAttribute(Edm.NAMESPACE_M_2007_08, FormatXml.ATOM_NULL, FormatXml.ATOM_VALUE_TRUE);
    } else {
      writer.writeCharacters(valueAsString);
    }
  }

  /**
   * 
   * @param writer
   * @param prop
   * @param name
   * @throws XMLStreamException
   * @throws EntityProviderException
   */
  private void writeStartElementWithCustomNamespace(final XMLStreamWriter writer, final EntityPropertyInfo prop,
      final String name) throws XMLStreamException, EntityProviderException {
    EdmCustomizableFeedMappings mapping = prop.getCustomMapping();
    String nsPrefix = mapping.getFcNsPrefix();
    String nsUri = mapping.getFcNsUri();
    if (nsUri == null || nsPrefix == null) {
      throw new EntityProviderException(EntityProviderException.INVALID_NAMESPACE.addContent(name));
    }
    writer.writeStartElement(nsPrefix, name, nsUri);
    writer.writeNamespace(nsPrefix, nsUri);
  }
}
