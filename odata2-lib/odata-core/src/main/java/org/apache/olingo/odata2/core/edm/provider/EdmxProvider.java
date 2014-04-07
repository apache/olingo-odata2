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

import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.xml.XMLStreamReader;
import org.apache.olingo.odata2.core.xml.XmlStreamFactory;
import org.apache.olingo.odata2.core.ep.consumer.XmlMetadataConsumer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EdmxProvider extends EdmProvider {
  private DataServices dataServices;

  public EdmxProvider parse(final InputStream in, final boolean validate) throws EntityProviderException {
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader streamReader = XmlStreamFactory.createStreamReader(in);
    dataServices = parser.readMetadata(streamReader, validate);
    return this;
  }

  @Override
  public EntityContainerInfo getEntityContainerInfo(final String name) throws ODataException {
    if (name != null) {
      for (Schema schema : dataServices.getSchemas()) {
        for (EntityContainer container : schema.getEntityContainers()) {
          if (container.getName().equals(name)) {
            return container;
          }
        }
      }
    } else {
      for (Schema schema : dataServices.getSchemas()) {
        for (EntityContainer container : schema.getEntityContainers()) {
          if (container.isDefaultEntityContainer()) {
            return container;
          }
        }
      }
    }
    return null;
  }

  @Override
  public EntityType getEntityType(final FullQualifiedName edmFQName) throws ODataException {
    for (Schema schema : dataServices.getSchemas()) {
      if (schema.getNamespace().equals(edmFQName.getNamespace())) {
        for (EntityType entityType : schema.getEntityTypes()) {
          if (entityType.getName().equals(edmFQName.getName())) {
            return entityType;
          }
        }
      }
    }
    return null;
  }

  @Override
  public ComplexType getComplexType(final FullQualifiedName edmFQName) throws ODataException {
    for (Schema schema : dataServices.getSchemas()) {
      if (schema.getNamespace().equals(edmFQName.getNamespace())) {
        for (ComplexType complexType : schema.getComplexTypes()) {
          if (complexType.getName().equals(edmFQName.getName())) {
            return complexType;
          }
        }
      }
    }
    return null;
  }

  @Override
  public Association getAssociation(final FullQualifiedName edmFQName) throws ODataException {
    for (Schema schema : dataServices.getSchemas()) {
      if (schema.getNamespace().equals(edmFQName.getNamespace())) {
        for (Association association : schema.getAssociations()) {
          if (association.getName().equals(edmFQName.getName())) {
            return association;
          }
        }
      }
    }
    return null;
  }

  @Override
  public EntitySet getEntitySet(final String entityContainer, final String name) throws ODataException {
    for (Schema schema : dataServices.getSchemas()) {
      for (EntityContainer container : schema.getEntityContainers()) {
        if (container.getName().equals(entityContainer)) {
          for (EntitySet entitySet : container.getEntitySets()) {
            if (entitySet.getName().equals(name)) {
              return entitySet;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public AssociationSet getAssociationSet(final String entityContainer, final FullQualifiedName association,
      final String sourceEntitySetName, final String sourceEntitySetRole) throws ODataException {
    for (Schema schema : dataServices.getSchemas()) {
      for (EntityContainer container : schema.getEntityContainers()) {
        if (container.getName().equals(entityContainer)) {
          for (AssociationSet associationSet : container.getAssociationSets()) {
            if (associationSet.getAssociation().equals(association)
                && ((associationSet.getEnd1().getEntitySet().equals(sourceEntitySetName) && associationSet.getEnd1()
                    .getRole().equals(sourceEntitySetRole))
                || (associationSet.getEnd2().getEntitySet().equals(sourceEntitySetName) && associationSet.getEnd2()
                    .getRole().equals(sourceEntitySetRole)))) {
              return associationSet;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public FunctionImport getFunctionImport(final String entityContainer, final String name) throws ODataException {
    for (Schema schema : dataServices.getSchemas()) {
      for (EntityContainer container : schema.getEntityContainers()) {
        if (container.getName().equals(entityContainer)) {
          for (FunctionImport function : container.getFunctionImports()) {
            if (function.getName().equals(name)) {
              return function;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public List<Schema> getSchemas() throws ODataException {
    return dataServices.getSchemas();
  }

  @Override
  public List<AliasInfo> getAliasInfos() {
    List<AliasInfo> aliasInfos = new ArrayList<AliasInfo>();
    for (Schema schema : dataServices.getSchemas()) {
      if (schema.getAlias() != null) {
        aliasInfos.add(new AliasInfo().setAlias(schema.getAlias()).setNamespace(schema.getNamespace()));
      }
    }

    return aliasInfos;
  }
}
