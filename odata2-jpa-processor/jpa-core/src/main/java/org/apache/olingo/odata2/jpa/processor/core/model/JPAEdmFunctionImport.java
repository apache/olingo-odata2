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
import java.util.HashMap;
import java.util.List;

import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport.ReturnType;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.FunctionImportParameter;
import org.apache.olingo.odata2.api.edm.provider.Mapping;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmComplexTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmFunctionImportView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmNameBuilder;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPATypeConverter;

public class JPAEdmFunctionImport extends JPAEdmBaseViewImpl implements JPAEdmFunctionImportView {

  private List<FunctionImport> consistentFunctionImportList = new ArrayList<FunctionImport>();
  private JPAEdmBuilder builder = null;
  private JPAEdmSchemaView schemaView;

  public JPAEdmFunctionImport(final JPAEdmSchemaView view) {
    super(view);
    schemaView = view;
  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmFunctionImportBuilder();
    }
    return builder;
  }

  @Override
  public List<FunctionImport> getConsistentFunctionImportList() {
    return consistentFunctionImportList;
  }

  protected class JPAEdmFunctionImportBuilder implements JPAEdmBuilder {

    private JPAEdmEntityTypeView jpaEdmEntityTypeView = null;
    private JPAEdmComplexTypeView jpaEdmComplexTypeView = null;

    @Override
    public void build() throws ODataJPAModelException, ODataJPARuntimeException {

      HashMap<Class<?>, String[]> customOperations = schemaView.getRegisteredOperations();

      jpaEdmEntityTypeView =
          schemaView.getJPAEdmEntityContainerView().getJPAEdmEntitySetView().getJPAEdmEntityTypeView();
      jpaEdmComplexTypeView = schemaView.getJPAEdmComplexTypeView();

      if (customOperations != null) {

        for (Class<?> clazz : customOperations.keySet()) {

          String[] operationNames = customOperations.get(clazz);
          Method[] methods = clazz.getMethods();
          Method method = null;

          int length = 0;
          if (operationNames != null) {
            length = operationNames.length;
          } else {
            length = methods.length;
          }

          boolean found = false;
          for (int i = 0; i < length; i++) {

            try {
              if (operationNames != null) {
                for (Method method2 : methods) {
                  if (method2.getName().equals(operationNames[i])) {
                    found = true;
                    method = method2;
                    break;
                  }
                }
                if (found == true) {
                  found = false;
                } else {
                  continue;
                }
              } else {
                method = methods[i];
              }

              FunctionImport functionImport = buildFunctionImport(method);
              if (functionImport != null) {
                consistentFunctionImportList.add(functionImport);
              }

            } catch (SecurityException e) {
              throw ODataJPAModelException.throwException(ODataJPAModelException.GENERAL, e);
            }
          }
        }
      }
    }

    private FunctionImport buildFunctionImport(final Method method) throws ODataJPAModelException {

      EdmFunctionImport edmAnnotationFunctionImport = method.getAnnotation(EdmFunctionImport.class);
      if (edmAnnotationFunctionImport != null && edmAnnotationFunctionImport.returnType() != null) {
        return buildEdmFunctionImport(method, edmAnnotationFunctionImport);
      }

      return null;
    }

    private FunctionImport buildEdmFunctionImport(final Method method,
        final EdmFunctionImport edmAnnotationFunctionImport)
        throws ODataJPAModelException {
      if (edmAnnotationFunctionImport != null && edmAnnotationFunctionImport.returnType() != null) {
        FunctionImport functionImport = new FunctionImport();

        if ("".equals(edmAnnotationFunctionImport.name())) {
          functionImport.setName(method.getName());
        } else {
          functionImport.setName(edmAnnotationFunctionImport.name());
        }

        JPAEdmMapping mapping = new JPAEdmMappingImpl();
        ((Mapping) mapping).setInternalName(method.getName());
        mapping.setJPAType(method.getDeclaringClass());
        functionImport.setMapping((Mapping) mapping);

        functionImport.setHttpMethod(edmAnnotationFunctionImport.httpMethod().name().toString());

        buildEdmReturnType(functionImport, method, edmAnnotationFunctionImport);
        buildEdmParameter(functionImport, method);

        return functionImport;
      }
      return null;
    }

    private void buildEdmParameter(final FunctionImport functionImport, final Method method)
        throws ODataJPAModelException {
      Annotation[][] annotations = method.getParameterAnnotations();
      Class<?>[] parameterTypes = method.getParameterTypes();
      List<FunctionImportParameter> funcImpList = new ArrayList<FunctionImportParameter>();
      JPAEdmMapping mapping = null;
      int j = 0;
      for (Annotation[] annotationArr : annotations) {
        Class<?> parameterType = parameterTypes[j++];

        for (Annotation element : annotationArr) {
          if (element instanceof EdmFunctionImportParameter) {
            EdmFunctionImportParameter annotation = (EdmFunctionImportParameter) element;
            FunctionImportParameter functionImportParameter = new FunctionImportParameter();
            if ("".equals(annotation.name())) {
              throw ODataJPAModelException.throwException(ODataJPAModelException.FUNC_PARAM_NAME_EXP.addContent(method
                  .getDeclaringClass().getName(), method.getName()), null);
            } else {
              functionImportParameter.setName(annotation.name());
            }

            functionImportParameter.setType(JPATypeConverter.convertToEdmSimpleType(parameterType, null));

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

    private void buildEdmReturnType(final FunctionImport functionImport, final Method method,
        final EdmFunctionImport edmAnnotationFunctionImport) throws ODataJPAModelException {
      ReturnType returnType = edmAnnotationFunctionImport.returnType();

      if (returnType != null) {
        org.apache.olingo.odata2.api.edm.provider.ReturnType functionReturnType =
            new org.apache.olingo.odata2.api.edm.provider.ReturnType();

        if (returnType.isCollection()) {
          functionReturnType.setMultiplicity(EdmMultiplicity.MANY);
        } else {
          functionReturnType.setMultiplicity(EdmMultiplicity.ONE);
        }

        if (returnType.type() == ReturnType.Type.ENTITY) {
          String entitySet = edmAnnotationFunctionImport.entitySet();
          if ("".equals(entitySet)) {
            throw ODataJPAModelException.throwException(ODataJPAModelException.FUNC_ENTITYSET_EXP, null);
          }
          functionImport.setEntitySet(entitySet);
        }

        Class<?> methodReturnType = method.getReturnType();
        if (methodReturnType == null || "void".equals(methodReturnType.getName())) {
          throw ODataJPAModelException.throwException(ODataJPAModelException.FUNC_RETURN_TYPE_EXP.addContent(method
              .getDeclaringClass(), method.getName()), null);
        }
        switch (returnType.type()) {
        case ENTITY:
          EntityType edmEntityType = null;
          if (returnType.isCollection() == false) {
            edmEntityType = jpaEdmEntityTypeView.searchEdmEntityType(methodReturnType.getSimpleName());
          } else {
            edmEntityType = jpaEdmEntityTypeView.searchEdmEntityType(getReturnTypeSimpleName(method));
          }

          if (edmEntityType == null) {
            throw ODataJPAModelException.throwException(ODataJPAModelException.FUNC_RETURN_TYPE_ENTITY_NOT_FOUND
                .addContent(method.getDeclaringClass(), method.getName(), methodReturnType.getSimpleName()), null);
          }
          functionReturnType.setTypeName(JPAEdmNameBuilder.build(schemaView, edmEntityType.getName()));
          break;
        case SIMPLE:
          EdmSimpleTypeKind edmSimpleTypeKind = JPATypeConverter.convertToEdmSimpleType(methodReturnType, null);
          functionReturnType.setTypeName(edmSimpleTypeKind.getFullQualifiedName());

          break;
        case COMPLEX:
          String embeddableTypeName = null;
          ComplexType complexType = null;
          boolean exists = false;

          if (returnType.isCollection() == false) {
            embeddableTypeName = methodReturnType.getName();
          } else {
            embeddableTypeName = getReturnTypeName(method);
          }

          complexType = jpaEdmComplexTypeView.searchEdmComplexType(embeddableTypeName);

          if (complexType == null) {// This could occure of non JPA Embeddable Types : Extension Scenario
            List<ComplexType> complexTypeList = schemaView.getEdmSchema().getComplexTypes();
            String[] complexTypeNameParts = embeddableTypeName.split("\\.");
            String complexTypeName = complexTypeNameParts[complexTypeNameParts.length - 1];
            for (ComplexType complexType1 : complexTypeList) {
              if (complexType1.getName().equals(complexTypeName)) {
                complexType = complexType1;
                exists = true;
                break;
              }
            }
            if (exists == false) {
              throw ODataJPAModelException.throwException(ODataJPAModelException.FUNC_RETURN_TYPE_ENTITY_NOT_FOUND
                  .addContent(method.getDeclaringClass(), method.getName(), methodReturnType.getSimpleName()), null);
            }
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
}
