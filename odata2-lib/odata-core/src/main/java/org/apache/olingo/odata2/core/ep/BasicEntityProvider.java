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
package org.apache.olingo.odata2.core.ep;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;

import org.apache.olingo.odata2.api.ODataServiceVersion;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.ODataHttpHeaders;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.DataServices;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataResponse.ODataResponseBuilder;
import org.apache.olingo.odata2.core.ep.producer.XmlMetadataProducer;
import org.apache.olingo.odata2.core.ep.util.CircleStreamBuffer;
import org.apache.olingo.odata2.api.xml.XMLStreamException;
import org.apache.olingo.odata2.api.xml.XMLStreamWriter;
import org.apache.olingo.odata2.core.xml.XmlStreamFactory;

/**
 * Provider for all basic (content type independent) entity provider methods.
 * 
 * 
 */
public class BasicEntityProvider {

  /** Default used charset for writer and response content header */
  private static final String DEFAULT_CHARSET = "utf-8";

  /**
   * Reads binary data from an input stream.
   * @param content the content input stream
   * @return the binary data
   * @throws EntityProviderException
   */
  public byte[] readBinary(final InputStream content) throws EntityProviderException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    byte[] value = new byte[Short.MAX_VALUE];
    int count;
    try {
      while ((count = content.read(value)) > 0) {
        buffer.write(value, 0, count);
      }
      content.close();
      buffer.flush();
      return buffer.toByteArray();
    } catch (IOException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  /**
   * Reads text from an input stream.
   * @param content the content input stream
   * @return text as string from <code>InputStream</code>
   * @throws EntityProviderException
   */
  public String readText(final InputStream content) throws EntityProviderException {
    BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(content, Charset.forName(DEFAULT_CHARSET)));
    StringBuilder stringBuilder = new StringBuilder();
    try {
      String line = null;
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line);
      }
      bufferedReader.close();
    } catch (IOException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
    return stringBuilder.toString();
  }

  /**
   * Reads an unformatted value of an EDM property as binary or as content type <code>text/plain</code>.
   * @param edmProperty the EDM property
   * @param content the content input stream
   * @param typeMapping
   * @return the value as the proper system data type
   * @throws EntityProviderException
   */
  public Object readPropertyValue(final EdmProperty edmProperty, final InputStream content, final Class<?> typeMapping)
      throws EntityProviderException {
    EdmSimpleType type;
    try {
      type = (EdmSimpleType) edmProperty.getType();
    } catch (EdmException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }

    if (type == EdmSimpleTypeKind.Binary.getEdmSimpleTypeInstance()) {
      return readBinary(content);
    } else {
      try {
        if (typeMapping == null) {
          return type.valueOfString(readText(content), EdmLiteralKind.DEFAULT, edmProperty.getFacets(), type
              .getDefaultType());
        } else {
          return type.valueOfString(readText(content), EdmLiteralKind.DEFAULT, edmProperty.getFacets(), typeMapping);
        }
      } catch (EdmException e) {
        throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
            .getSimpleName()), e);
      }
    }
  }

  /**
   * Write property as binary or as content type <code>text/plain</code>.
   * @param edmProperty the EDM property
   * @param value its value
   * @return resulting {@link ODataResponse} with written content
   * @throws EntityProviderException
   */
  public ODataResponse writePropertyValue(final EdmProperty edmProperty, final Object value)
      throws EntityProviderException {
    try {
      final EdmSimpleType type = (EdmSimpleType) edmProperty.getType();

      if (type == EdmSimpleTypeKind.Binary.getEdmSimpleTypeInstance()) {
        String contentType = HttpContentType.APPLICATION_OCTET_STREAM;
        Object binary = value;
        if (edmProperty.getMimeType() != null) {
          contentType = edmProperty.getMimeType();
        } else {
          if (edmProperty.getMapping() != null && edmProperty.getMapping().getMimeType() != null) {
            String mimeTypeMapping = edmProperty.getMapping().getMimeType();
            if (value instanceof Map) {
              final Map<?, ?> mappedData = (Map<?, ?>) value;
              binary = mappedData.get(edmProperty.getName());
              contentType = (String) mappedData.get(mimeTypeMapping);
            } else {
              throw new EntityProviderException(EntityProviderException.COMMON);
            }
          }
        }
        return writeBinary(contentType, (byte[]) binary);

      } else {
        return writeText(type.valueToString(value, EdmLiteralKind.DEFAULT, edmProperty.getFacets()));
      }

    } catch (EdmException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  /**
   * Write text value as content type <code>text/plain</code> with charset parameter set to {@value #DEFAULT_CHARSET}.
   * @param value the string that is written to {@link ODataResponse}
   * @return resulting {@link ODataResponse} with written text content
   * @throws EntityProviderException
   */
  public ODataResponse writeText(final String value) throws EntityProviderException {
    ODataResponseBuilder builder = ODataResponse.newBuilder();
    if (value != null) {
      ByteArrayInputStream stream;
      try {
        stream = new ByteArrayInputStream(value.getBytes(DEFAULT_CHARSET));
      } catch (UnsupportedEncodingException e) {
        throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
            .getSimpleName()), e);
      }
      builder.entity(stream);
    }

    return builder.build();
  }

  /**
   * Write binary content with content type header set to given <code>mime type</code> parameter.
   * @param mimeType MIME type which is written and used as content type header information
   * @param data data is written to {@link ODataResponse}
   * @return resulting {@link ODataResponse} with written binary content
   * @throws EntityProviderException
   */
  public ODataResponse writeBinary(final String mimeType, final byte[] data) throws EntityProviderException {
    ODataResponseBuilder builder = ODataResponse.newBuilder();
    if (data != null) {
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      builder.contentHeader(mimeType);
      builder.entity(bais);
    } else {
      builder.status(HttpStatusCodes.NO_CONTENT);
    }
    return builder.build();
  }

  /**
   * Writes the metadata in XML format. Predefined namespaces is of type Map{@literal <}prefix,namespace{@literal >} and
   * may be null or an empty Map.
   * @param schemas
   * @param predefinedNamespaces
   * @return resulting {@link ODataResponse} with written metadata content
   * @throws EntityProviderException
   */
  public ODataResponse writeMetadata(final List<Schema> schemas, final Map<String, String> predefinedNamespaces)
      throws EntityProviderException {
    ODataResponseBuilder builder = ODataResponse.newBuilder();
    String dataServiceVersion = ODataServiceVersion.V10;
    if (schemas != null) {
      dataServiceVersion = calculateDataServiceVersion(schemas);
    }
    DataServices metadata = new DataServices().setSchemas(schemas).setDataServiceVersion(dataServiceVersion);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    try {
      writer = new OutputStreamWriter(csb.getOutputStream(), DEFAULT_CHARSET);
      XMLStreamWriter xmlStreamWriter = XmlStreamFactory.createStreamWriter(writer);
      XmlMetadataProducer.writeMetadata(metadata, xmlStreamWriter, predefinedNamespaces);
    } catch (UnsupportedEncodingException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    } catch (FactoryConfigurationError e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
    builder.entity(csb.getInputStream());
    builder.header(ODataHttpHeaders.DATASERVICEVERSION, dataServiceVersion);
    return builder.build();
  }

  /**
   * Calculates the necessary data service version for the metadata serialization
   * @param schemas
   * @return DataServiceversion as String
   */
  private String calculateDataServiceVersion(final List<Schema> schemas) {

    String dataServiceVersion = ODataServiceVersion.V10;

    if (schemas != null) {
      for (Schema schema : schemas) {
        List<EntityType> entityTypes = schema.getEntityTypes();
        if (entityTypes != null) {
          for (EntityType entityType : entityTypes) {
            List<Property> properties = entityType.getProperties();
            if (properties != null) {
              for (Property property : properties) {
                if (property.getCustomizableFeedMappings() != null) {
                  if (property.getCustomizableFeedMappings().getFcKeepInContent() != null) {
                    if (!property.getCustomizableFeedMappings().getFcKeepInContent()) {
                      dataServiceVersion = ODataServiceVersion.V20;
                      return dataServiceVersion;
                    }
                  }
                }
              }
              if (entityType.getCustomizableFeedMappings() != null) {
                if (entityType.getCustomizableFeedMappings().getFcKeepInContent() != null) {
                  if (entityType.getCustomizableFeedMappings().getFcKeepInContent()) {
                    dataServiceVersion = ODataServiceVersion.V20;
                    return dataServiceVersion;
                  }
                }
              }
            }
          }
        }
      }
    }

    return dataServiceVersion;
  }
}
