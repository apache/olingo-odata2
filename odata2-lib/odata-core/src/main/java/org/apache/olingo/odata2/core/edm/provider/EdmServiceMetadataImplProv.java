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
package org.apache.olingo.odata2.core.edm.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.ODataServiceVersion;
import org.apache.olingo.odata2.api.edm.EdmEntitySetInfo;
import org.apache.olingo.odata2.api.edm.EdmServiceMetadata;
import org.apache.olingo.odata2.api.edm.provider.DataServices;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.core.ep.producer.XmlMetadataProducer;
import org.apache.olingo.odata2.core.ep.util.CircleStreamBuffer;

/**
 *  
 */
public class EdmServiceMetadataImplProv implements EdmServiceMetadata {

  private EdmProvider edmProvider;
  private String dataServiceVersion;
  private List<Schema> schemas;
  private List<EdmEntitySetInfo> entitySetInfos;

  public EdmServiceMetadataImplProv(final EdmProvider edmProvider) {
    this.edmProvider = edmProvider;
  }

  @Override
  public InputStream getMetadata() throws ODataException {
    if (schemas == null) {
      schemas = edmProvider.getSchemas();
    }

    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    EntityProviderException cachedException = null;
    DataServices metadata = new DataServices().setSchemas(schemas).setDataServiceVersion(getDataServiceVersion());

    try {
      writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
      XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
      XmlMetadataProducer.writeMetadata(metadata, xmlStreamWriter, null);
      return csb.getInputStream();
    } catch (XMLStreamException e) {
      cachedException = new EntityProviderException(EntityProviderException.COMMON, e);
      throw cachedException;
    } catch (UnsupportedEncodingException e) {
      cachedException = new EntityProviderException(EntityProviderException.COMMON, e);
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          if (cachedException != null) {
            throw cachedException;
          } else {
            throw new EntityProviderException(EntityProviderException.COMMON, e);
          }
        }
      }
    }
  }

  @Override
  public String getDataServiceVersion() throws ODataException {
    if (schemas == null) {
      schemas = edmProvider.getSchemas();
    }

    if (dataServiceVersion == null) {
      dataServiceVersion = ODataServiceVersion.V10;

      for (Schema schema : listOrEmptyList(schemas)) {
        List<EntityType> entityTypes = listOrEmptyList(schema.getEntityTypes());
        for (EntityType entityType : entityTypes) {
          List<Property> properties = listOrEmptyList(entityType.getProperties());
          for (Property property : properties) {
            if (property.getCustomizableFeedMappings() != null) {
              if (property.getCustomizableFeedMappings().getFcKeepInContent() != null) {
                if (!property.getCustomizableFeedMappings().getFcKeepInContent()) {
                  dataServiceVersion = ODataServiceVersion.V20;
                  return dataServiceVersion;
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

  @Override
  public List<EdmEntitySetInfo> getEntitySetInfos() throws ODataException {
    if (entitySetInfos == null) {
      entitySetInfos = new ArrayList<EdmEntitySetInfo>();

      if (schemas == null) {
        schemas = edmProvider.getSchemas();
      }

      for (Schema schema : schemas) {
        for (EntityContainer entityContainer : listOrEmptyList(schema.getEntityContainers())) {
          for (EntitySet entitySet : listOrEmptyList(entityContainer.getEntitySets())) {
            EdmEntitySetInfo entitySetInfo = new EdmEntitySetInfoImplProv(entitySet, entityContainer);
            entitySetInfos.add(entitySetInfo);
          }
        }
      }

    }

    return entitySetInfos;
  }

  /**
   * Return original list if parameter is not NULL or an empty list.
   *
   * @param list list which is checked and probably returned
   * @param <T> type of list
   * @return original list if parameter is not NULL or an empty list
   */
  private <T> List<T> listOrEmptyList(List<T> list) {
    if(list == null) {
      return Collections.emptyList();
    }
    return list;
  }
}
