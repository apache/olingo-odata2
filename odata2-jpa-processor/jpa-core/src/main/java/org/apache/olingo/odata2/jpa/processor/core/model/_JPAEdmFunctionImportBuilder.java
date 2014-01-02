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
package org.apache.olingo.odata2.jpa.processor.core.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.annotation.edm.FunctionImport.Multiplicity;
import org.apache.olingo.odata2.api.annotation.edm.FunctionImport.ReturnType;
import org.apache.olingo.odata2.api.annotation.edm.Parameter;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.FunctionImportParameter;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmComplexTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmNameBuilder;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPATypeConvertor;

@Deprecated
public final class _JPAEdmFunctionImportBuilder {

  private JPAEdmEntityTypeView jpaEdmEntityTypeView = null;
  private JPAEdmComplexTypeView jpaEdmComplexTypeView = null;
  private JPAEdmSchemaView schemaView;

  public void setJPAEdmEntityTypeView(final JPAEdmEntityTypeView jpaEdmEntityTypeView) {
    this.jpaEdmEntityTypeView = jpaEdmEntityTypeView;
  }

  public void setSchemaView(final JPAEdmSchemaView schemaView) {
    this.schemaView = schemaView;
  }

  public void setJPAEdmComplexTypeView(final JPAEdmComplexTypeView jpaEdmComplexTypeView) {
    this.jpaEdmComplexTypeView = jpaEdmComplexTypeView;
  }

  public FunctionImport buildFunctionImport(final Method method,
      final org.apache.olingo.odata2.api.annotation.edm.FunctionImport annotation) throws ODataJPAModelException {

    if (method != null && annotation != null && annotation.returnType() != ReturnType.NONE) {
      FunctionImport functionImport = new FunctionImport();

      if (annotation.name().equals("")) {
        functionImport.setName(method.getName());
      } else {
        functionImport.setName(annotation.name());
      }

      JPAEdmMapping mapping = new JPAEdmMappingImpl();
      ((Mapping) mapping).setInternalName(method.getName());
      mapping.setJPAType(method.getDeclaringClass());
      functionImport.setMapping((Mapping) mapping);

      functionImport.setHttpMethod(annotation.httpMethod().name().toString());

      buildReturnType(functionImport, method, annotation);
      buildParameter(functionImport, method);
      return functionImport;
    }
    return null;

  }

  private void buildParameter(final FunctionImport functionImport, final Method method)
      throws ODataJPAModelException {

    Annotation[][] annotations = method.getParameterAnnotations();
    Class<?>[] parameterTypes = method.getParameterTypes();
    List<FunctionImportParameter> funcImpList = new ArrayList<FunctionImportParameter>();
    JPAEdmMapping mapping = null;
    int j = 0;
    for (Annotation[] annotationArr : annotations) {
      Class<?> parameterType = parameterTypes[j++];

      for (Annotation element : annotationArr) {
        if (element instanceof Parameter) {
          Parameter annotation = (Parameter) element;
          FunctionImportParameter functionImportParameter = new FunctionImportParameter();
          if (annotation.name().equals("")) {
            throw ODataJPAModelException.throwException(ODataJPAModelException.FUNC_PARAM_NAME_EXP.addContent(method
                .getDeclaringClass().getName(), method.getName()), null);
          } else {
            functionImportParameter.setName(annotation.name());
          }

          functionImportParameter.setType(JPATypeConvertor.convertToEdmSimpleType(parameterType, null));
          functionImportParameter.setMode(annotation.mode().toString());

          Facets facets = new Facets();
          if (annotation.facets().maxLength() > 0) {
            facets.setMaxLength(annotation.facets().maxLength());
          }
          if (annotation.facets().nullable() == false) {
            facets.setNullable(false);
          } else {
            facets.setNullable(true);
          }

          if (annotation.facets().precision() > 0) {
            facets.setPrecision(annotation.facets().precision());
          }
          if (annotation.facets().scale() >= 0) {
            facets.setScale(annotation.facets().scale());
          }

          functionImportParameter.setFacets(facets);
          mapping = new JPAEdmMappingImpl();
          mapping.setJPAType(parameterType);
          functionImportParameter.setMapping((Mapping) mapping);
          funcImpList.add(functionImportParameter);
        }
      }
    }
    if (!funcImpList.isEmpty()) {
      functionImport.setParameters(funcImpList);
    }
  }

  private void buildReturnType(final FunctionImport functionImport, final Method method,
      final org.apache.olingo.odata2.api.annotation.edm.FunctionImport annotation) throws ODataJPAModelException {
    org.apache.olingo.odata2.api.annotation.edm.FunctionImport.ReturnType returnType = annotation.returnType();
    Multiplicity multiplicity = null;

    if (returnType != ReturnType.NONE) {
      org.apache.olingo.odata2.api.edm.provider.ReturnType functionReturnType =
          new org.apache.olingo.odata2.api.edm.provider.ReturnType();
      multiplicity = annotation.multiplicity();

      if (multiplicity == Multiplicity.MANY) {
        functionReturnType.setMultiplicity(EdmMultiplicity.MANY);
      } else {
        functionReturnType.setMultiplicity(EdmMultiplicity.ONE);
      }

      if (returnType == ReturnType.ENTITY_TYPE) {
        String entitySet = annotation.entitySet();
        if (entitySet.equals("")) {
          throw ODataJPAModelException.throwException(ODataJPAModelException.FUNC_ENTITYSET_EXP, null);
        }
        functionImport.setEntitySet(entitySet);
      }

      Class<?> methodReturnType = method.getReturnType();
      if (methodReturnType == null || methodReturnType.getName().equals("void")) {
        throw ODataJPAModelException.throwException(ODataJPAModelException.FUNC_RETURN_TYPE_EXP.addContent(method
            .getDeclaringClass(), method.getName()), null);
      }
      switch (returnType) {
      case ENTITY_TYPE:
        EntityType edmEntityType = null;
        if (multiplicity == Multiplicity.ONE) {
          edmEntityType = jpaEdmEntityTypeView.searchEdmEntityType(methodReturnType.getSimpleName());
        } else if (multiplicity == Multiplicity.MANY) {
          edmEntityType = jpaEdmEntityTypeView.searchEdmEntityType(getReturnTypeSimpleName(method));
        }

        if (edmEntityType == null) {
          throw ODataJPAModelException.throwException(ODataJPAModelException.FUNC_RETURN_TYPE_ENTITY_NOT_FOUND
              .addContent(method.getDeclaringClass(), method.getName(), methodReturnType.getSimpleName()), null);
        }
        functionReturnType.setTypeName(JPAEdmNameBuilder.build(schemaView, edmEntityType.getName()));
        break;
      case SCALAR:

        EdmSimpleTypeKind edmSimpleTypeKind = JPATypeConvertor.convertToEdmSimpleType(methodReturnType, null);
        functionReturnType.setTypeName(edmSimpleTypeKind.getFullQualifiedName());

        break;
      case COMPLEX_TYPE:
        ComplexType complexType = null;
        if (multiplicity == Multiplicity.ONE) {
          complexType = jpaEdmComplexTypeView.searchEdmComplexType(methodReturnType.getName());
        } else if (multiplicity == Multiplicity.MANY) {
          complexType = jpaEdmComplexTypeView.searchEdmComplexType(getReturnTypeName(method));
        }
        if (complexType == null) {
          throw ODataJPAModelException.throwException(ODataJPAModelException.FUNC_RETURN_TYPE_ENTITY_NOT_FOUND
              .addContent(method.getDeclaringClass(), method.getName(), methodReturnType.getSimpleName()), null);
        }
        functionReturnType.setTypeName(JPAEdmNameBuilder.build(schemaView, complexType.getName()));
        break;
      default:
        break;
      }
      functionImport.setReturnType(functionReturnType);
    }
  }

  private String getReturnTypeName(final Method method) {
    try {
      ParameterizedType pt = (ParameterizedType) method.getGenericReturnType();
      Type t = pt.getActualTypeArguments()[0];
      return ((Class<?>) t).getName();
    } catch (ClassCastException e) {
      return method.getReturnType().getName();
    }
  }

  private String getReturnTypeSimpleName(final Method method) {
    try {
      ParameterizedType pt = (ParameterizedType) method.getGenericReturnType();
      Type t = pt.getActualTypeArguments()[0];
      return ((Class<?>) t).getSimpleName();
    } catch (ClassCastException e) {
      return method.getReturnType().getSimpleName();
    }
  }
}
