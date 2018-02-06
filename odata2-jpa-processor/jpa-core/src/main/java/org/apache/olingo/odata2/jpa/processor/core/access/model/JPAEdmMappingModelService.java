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
package org.apache.olingo.odata2.jpa.processor.core.access.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmMappingModelAccess;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.mapping.JPAAttributeMapType.JPAAttribute;
import org.apache.olingo.odata2.jpa.processor.api.model.mapping.JPAEdmMappingModel;
import org.apache.olingo.odata2.jpa.processor.api.model.mapping.JPAEmbeddableTypeMapType;
import org.apache.olingo.odata2.jpa.processor.api.model.mapping.JPAEntityTypeMapType;
import org.apache.olingo.odata2.jpa.processor.api.model.mapping.JPAPersistenceUnitMapType;
import org.apache.olingo.odata2.jpa.processor.api.model.mapping.JPARelationshipMapType.JPARelationship;

public class JPAEdmMappingModelService implements JPAEdmMappingModelAccess {

  boolean mappingModelExists = true;
  private JPAEdmMappingModel mappingModel;
  private InputStream mappingModelStream = null;
  private String mappingModelName;

  public JPAEdmMappingModelService(final ODataJPAContext ctx) {
    JPAEdmExtension ext = null;
    mappingModelName = ctx.getJPAEdmMappingModel();
    if (mappingModelName == null) {
      ext = ctx.getJPAEdmExtension();
      if (ext != null) {
        mappingModelStream = ext.getJPAEdmMappingModelStream();
      }
    }

    mappingModelExists = mappingModelName != null || mappingModelStream != null;
  }

  @Override
  public void loadMappingModel() {
    InputStream is = null;
    if (mappingModelExists) {
      JAXBContext context;
      try {
        context = JAXBContext.newInstance(JPAEdmMappingModel.class);

        Unmarshaller unmarshaller = context.createUnmarshaller();
        is = loadMappingModelInputStream();
        if (is == null) {
          mappingModelExists = false;
          return;
        }

        mappingModel = (JPAEdmMappingModel) unmarshaller.unmarshal(is);

        if (mappingModel != null) {
          mappingModelExists = true;
        }

      } catch (JAXBException e) {
        mappingModelExists = false;
        ODataJPAModelException.throwException(ODataJPAModelException.GENERAL, e);
      } finally {
        try {
          if (is != null) {
            is.close();
          }
        } catch (IOException e) {
          // do nothing
        }
      }
    }
  }

  @Override
  public boolean isMappingModelExists() {
    return mappingModelExists;
  }

  @Override
  public JPAEdmMappingModel getJPAEdmMappingModel() {
    return mappingModel;
  }

  @Override
  public String mapJPAPersistenceUnit(final String persistenceUnitName) {

    JPAPersistenceUnitMapType persistenceUnit = mappingModel.getPersistenceUnit();
    if (persistenceUnit.getName().equals(persistenceUnitName)) {
      return persistenceUnit.getEDMSchemaNamespace();
    }

    return null;
  }

  @Override
  public String mapJPAEntityType(final String jpaEntityTypeName) {

    JPAEntityTypeMapType jpaEntityTypeMap = searchJPAEntityTypeMapType(jpaEntityTypeName);
    if (jpaEntityTypeMap != null) {
      return jpaEntityTypeMap.getEDMEntityType();
    } else {
      return null;
    }
  }

  @Override
  public String mapJPAEntitySet(final String jpaEntityTypeName) {
    JPAEntityTypeMapType jpaEntityTypeMap = searchJPAEntityTypeMapType(jpaEntityTypeName);
    if (jpaEntityTypeMap != null) {
      return jpaEntityTypeMap.getEDMEntitySet();
    } else {
      return null;
    }
  }

  @Override
  public String mapJPAAttribute(final String jpaEntityTypeName, final String jpaAttributeName) {
    JPAEntityTypeMapType jpaEntityTypeMap = searchJPAEntityTypeMapType(jpaEntityTypeName);
    if (jpaEntityTypeMap != null && jpaEntityTypeMap.getJPAAttributes() != null) {
      // fixing attributes
      // removal issue
      // from mapping
      for (JPAAttribute jpaAttribute : jpaEntityTypeMap.getJPAAttributes().getJPAAttribute()) {
        if (jpaAttribute.getName().equals(jpaAttributeName)) {
          return jpaAttribute.getValue();
        }
      }
    }

    return null;
  }

  @Override
  public String mapJPARelationship(final String jpaEntityTypeName, final String jpaRelationshipName) {
    JPAEntityTypeMapType jpaEntityTypeMap = searchJPAEntityTypeMapType(jpaEntityTypeName);
    if (jpaEntityTypeMap != null && jpaEntityTypeMap.getJPARelationships() != null) {
      for (JPARelationship jpaRealtionship : jpaEntityTypeMap.getJPARelationships().getJPARelationship()) {
        if (jpaRealtionship.getName().equals(jpaRelationshipName)) {
          return jpaRealtionship.getValue();
        }
      }
    }

    return null;
  }

  @Override
  public String mapJPAEmbeddableType(final String jpaEmbeddableTypeName) {
    JPAEmbeddableTypeMapType jpaEmbeddableType = searchJPAEmbeddableTypeMapType(jpaEmbeddableTypeName);
    if (jpaEmbeddableType != null) {
      return jpaEmbeddableType.getEDMComplexType();
    } else {
      return null;
    }
  }

  @Override
  public String mapJPAEmbeddableTypeAttribute(final String jpaEmbeddableTypeName, final String jpaAttributeName) {
    JPAEmbeddableTypeMapType jpaEmbeddableType = searchJPAEmbeddableTypeMapType(jpaEmbeddableTypeName);
    if (jpaEmbeddableType != null && jpaEmbeddableType.getJPAAttributes() != null) {
      for (JPAAttribute jpaAttribute : jpaEmbeddableType.getJPAAttributes().getJPAAttribute()) {
        if (jpaAttribute.getName().equals(jpaAttributeName)) {
          return jpaAttribute.getValue();
        }
      }
    }
    return null;
  }

  private JPAEntityTypeMapType searchJPAEntityTypeMapType(final String jpaEntityTypeName) {
    if (mappingModel != null) {
      List<JPAEntityTypeMapType> types = mappingModel.getPersistenceUnit().getJPAEntityTypes().getJPAEntityType();
      for (JPAEntityTypeMapType jpaEntityType : types) {
        if (jpaEntityType.getName().equals(jpaEntityTypeName)) {
          return jpaEntityType;
        }
      }
    }
    return null;
  }

  private JPAEmbeddableTypeMapType searchJPAEmbeddableTypeMapType(final String jpaEmbeddableTypeName) {
    if (null != mappingModel.getPersistenceUnit() && 
        null != mappingModel.getPersistenceUnit().getJPAEmbeddableTypes()) {
      for (JPAEmbeddableTypeMapType jpaEmbeddableType : mappingModel.getPersistenceUnit().getJPAEmbeddableTypes()
          .getJPAEmbeddableType()) {
        if (jpaEmbeddableType.getName().equals(jpaEmbeddableTypeName)) {
          return jpaEmbeddableType;
        }
      }
    }
    return null;
  }

  protected InputStream loadMappingModelInputStream() {
    if (mappingModelStream != null) {
      return mappingModelStream;
    }
    return JPAEdmMappingModelService.class.getClassLoader().getResourceAsStream("../../" + mappingModelName);

  }

  @Override
  public boolean checkExclusionOfJPAEntityType(final String jpaEntityTypeName) {
    JPAEntityTypeMapType type = searchJPAEntityTypeMapType(jpaEntityTypeName);
    if (type != null) {
      return type.isExclude();
    }
    return false;
  }

  @Override
  public boolean checkExclusionOfJPAAttributeType(final String jpaEntityTypeName, final String jpaAttributeName) {
    JPAEntityTypeMapType type = searchJPAEntityTypeMapType(jpaEntityTypeName);
    if (type != null && type.getJPAAttributes() != null) {
      for (JPAAttribute jpaAttribute : type.getJPAAttributes().getJPAAttribute()) {
        if (jpaAttribute.getName().equals(jpaAttributeName)) {
          return jpaAttribute.isExclude();
        }
      }
    }
    return false;
  }

  @Override
  public boolean checkExclusionOfJPAEmbeddableType(final String jpaEmbeddableTypeName) {
    JPAEmbeddableTypeMapType type = searchJPAEmbeddableTypeMapType(jpaEmbeddableTypeName);
    if (type != null) {
      return type.isExclude();
    }
    return false;
  }

  @Override
  public boolean checkExclusionOfJPAEmbeddableAttributeType(final String jpaEmbeddableTypeName,
      final String jpaAttributeName) {
    JPAEmbeddableTypeMapType type = searchJPAEmbeddableTypeMapType(jpaEmbeddableTypeName);
    if (type != null && type.getJPAAttributes() != null) {
      for (JPAAttribute jpaAttribute : type.getJPAAttributes().getJPAAttribute()) {
        if (jpaAttribute.getName().equals(jpaAttributeName)) {
          return jpaAttribute.isExclude();
        }
      }
    }
    return false;
  }
}
