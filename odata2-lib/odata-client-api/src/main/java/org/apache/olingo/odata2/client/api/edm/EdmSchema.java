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
package org.apache.olingo.odata2.client.api.edm;

import java.util.List;
import org.apache.olingo.odata2.api.edm.provider.Using;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;

/**
 * Impl Objects of this class represent a client schema
 * 
 */
public interface EdmSchema { 

  /**
   * @return <b>String</b> namespace of this {@link EdmSchema}
   */
  public String getNamespace() ;

  /**
   * @return <b>String</b> alias of this {@link EdmSchema}
   */
  public String getAlias() ;

  /**
   * @return List<{@link Using}> of this {@link EdmSchema}
   */
  public List<EdmUsing> getUsings() ;

  /**
   * @return List<{@link EntityType}> of this {@link EdmSchema}
   */
  public List<EdmEntityType> getEntityTypes() ;
  

  /**
   * @return List<{@link ComplexType}> of this {@link EdmSchema}
   */
  public List<EdmComplexType> getComplexTypes() ;

  /**
   * @return List<{@link Association}> of this {@link EdmSchema}
   */
  public List<EdmAssociation> getAssociations() ;

  /**
   * @return List<{@link EntityContainer}> of this {@link EdmSchema}
   */
  public List<EdmEntityContainer> getEntityContainers(); 

  /**
   * @return List of {@link AnnotationAttribute} annotation attributes
   */
  public List<EdmAnnotationAttribute> getAnnotationAttributes();

  /**
   * @return List of {@link AnnotationElement} annotation elements
   */
  public List<EdmAnnotationElement> getAnnotationElements() ;
}
