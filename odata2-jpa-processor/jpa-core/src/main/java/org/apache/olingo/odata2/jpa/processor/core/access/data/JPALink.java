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
package org.apache.olingo.odata2.jpa.processor.core.access.data;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAProcessor;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAFactory;
import org.apache.olingo.odata2.jpa.processor.core.ODataEntityParser;

public class JPALink {

  private static final String SPACE = " ";
  private static final String ODATA_COMMAND_FILTER = "$filter";
  private static final String ODATA_OPERATOR_AND = "and";
  private static final String ODATA_OPERATOR_NE = "ne";

  private ODataJPAContext context;
  private JPAProcessor jpaProcessor;
  private ODataEntityParser parser;
  private Object targetJPAEntity;
  private Object sourceJPAEntity;

  public JPALink(final ODataJPAContext context) {
    this.context = context;
    jpaProcessor = ODataJPAFactory.createFactory().getJPAAccessFactory().getJPAProcessor(this.context);
    parser = new ODataEntityParser(this.context);
  }

  public void setSourceJPAEntity(final Object jpaEntity) {
    sourceJPAEntity = jpaEntity;
  }

  public void setTargetJPAEntity(final Object jpaEntity) {
    targetJPAEntity = jpaEntity;
  }

  public Object getTargetJPAEntity() {
    return targetJPAEntity;
  }

  public Object getSourceJPAEntity() {
    return sourceJPAEntity;
  }

  public void create(final PostUriInfo uriInfo, final InputStream content, final String requestContentType,
      final String contentType) throws ODataJPARuntimeException, ODataJPAModelException {
    modifyLink((UriInfo) uriInfo, content, requestContentType, contentType);
  }

  public void update(final PutMergePatchUriInfo putUriInfo, final InputStream content, final String requestContentType,
      final String contentType) throws ODataJPARuntimeException, ODataJPAModelException {
    modifyLink((UriInfo) putUriInfo, content, requestContentType, contentType);
  }

  public void delete(final DeleteUriInfo uriInfo) throws ODataJPARuntimeException {
    try {

      int index = context.getODataContext().getPathInfo().getODataSegments().size() - 2;

      List<String> linkSegments = new ArrayList<String>();
      String customLinkSegment = context.getODataContext().getPathInfo().getODataSegments().get(0).getPath();
      linkSegments.add(customLinkSegment);
      customLinkSegment = uriInfo.getNavigationSegments().get(0).getNavigationProperty().getName();
      linkSegments.add(customLinkSegment);

      HashMap<String, String> options = new HashMap<String, String>();
      List<KeyPredicate> keyPredicates = uriInfo.getNavigationSegments().get(0).getKeyPredicates();
      StringBuffer condition = new StringBuffer();
      String literal = null;
      KeyPredicate keyPredicate = null;
      int size = keyPredicates.size();
      for (int i = 0; i < size; i++) {
        keyPredicate = keyPredicates.get(i);

        literal = ((EdmSimpleType) keyPredicate.getProperty().getType()).toUriLiteral(keyPredicate.getLiteral());
        condition.append(keyPredicate.getProperty().getName()).append(SPACE);
        condition.append(ODATA_OPERATOR_NE).append(SPACE);
        condition.append(literal).append(SPACE);
        if (i != size - 1) {
          condition.append(ODATA_OPERATOR_AND).append(SPACE);
        }
      }
      options.put(ODATA_COMMAND_FILTER, condition.toString());

      UriInfo parsedUriInfo = parser.parseLinkSegments(linkSegments, options);
      List<Object> relatedEntities = jpaProcessor.process((GetEntitySetUriInfo) parsedUriInfo);

      parsedUriInfo = parser.parseURISegment(0, index);
      if (parsedUriInfo != null) {
        targetJPAEntity = jpaProcessor.process((GetEntityUriInfo) parsedUriInfo);
        if (targetJPAEntity == null) {
          throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.RESOURCE_X_NOT_FOUND
              .addContent(parsedUriInfo.getTargetEntitySet().getName()), null);
        }
        NavigationSegment navigationSegment = uriInfo.getNavigationSegments().get(0);
        EdmNavigationProperty navigationProperty = navigationSegment.getNavigationProperty();
        delinkJPAEntities(targetJPAEntity, relatedEntities, navigationProperty);
      }

    } catch (EdmException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
    } catch (ODataException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
    }
  }

  private void delinkJPAEntities(final Object targetJPAEntity,
      final List<Object> relatedJPAEntities,
      final EdmNavigationProperty targetNavigationProperty)
      throws ODataJPARuntimeException {

    try {
      JPAEntityParser entityParser = new JPAEntityParser();
      Method setMethod = entityParser.getAccessModifier(targetJPAEntity,
          targetNavigationProperty, JPAEntityParser.ACCESS_MODIFIER_SET);

      Method getMethod = entityParser.getAccessModifier(targetJPAEntity,
          targetNavigationProperty, JPAEntityParser.ACCESS_MODIFIER_GET);

      if (getMethod.getReturnType().getTypeParameters() != null
          && getMethod.getReturnType().getTypeParameters().length != 0) {
        setMethod.invoke(targetJPAEntity, relatedJPAEntities);
      } else {
        setMethod.invoke(targetJPAEntity, (Object) null);
      }
    } catch (IllegalAccessException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (InvocationTargetException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    }

  }

  public void save() {
    EntityManager em = context.getEntityManager();
    EntityTransaction tx = em.getTransaction();

    if (!tx.isActive()) {
      em.getTransaction().begin();
      if (sourceJPAEntity != null) {
        em.persist(sourceJPAEntity);
      }
      if (targetJPAEntity != null) {
        em.persist(targetJPAEntity);
        em.flush();
      }
      em.getTransaction().commit();
    }

  }

  public void create(final PostUriInfo uriInfo, final ODataEntry oDataEntry) throws ODataJPARuntimeException,
      ODataJPAModelException {
    try {
      EdmEntitySet targetEntitySet = uriInfo.getTargetEntitySet();
      int index = context.getODataContext().getPathInfo().getODataSegments().size() - 2;
      UriInfo parsedUriInfo = parser.parseURISegment(index, index + 1);
      Object targetJPAEntity = null;

      if (parsedUriInfo != null) {
        targetJPAEntity = jpaProcessor.process((GetEntityUriInfo) parsedUriInfo);
        if (targetJPAEntity == null) {
          throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.RESOURCE_X_NOT_FOUND
              .addContent(parsedUriInfo.getTargetEntitySet().getName()), null);
        }
        linkJPAEntities(sourceJPAEntity, targetJPAEntity, uriInfo.getNavigationSegments().get(0)
            .getNavigationProperty());
      }

      List<String> navPropertyNames;
      navPropertyNames = targetEntitySet.getEntityType().getNavigationPropertyNames();

      for (String navPropertyName : navPropertyNames) {
        List<String> links = oDataEntry.getMetadata().getAssociationUris(navPropertyName);
        if (links != null && links.isEmpty() == false) {
          for (String link : links) {
            UriInfo bindingUriInfo = parser.parseBindingLink(link, new HashMap<String, String>());
            targetJPAEntity = jpaProcessor.process((GetEntityUriInfo) bindingUriInfo);

            linkJPAEntities(targetJPAEntity, sourceJPAEntity, (EdmNavigationProperty) targetEntitySet.getEntityType()
                .getProperty(
                    navPropertyName));
          }
        }
      }
    } catch (EdmException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
    } catch (ODataException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
    }
  }

  private void modifyLink(final UriInfo uriInfo, final InputStream content, final String requestContentType,
      final String contentType)
      throws ODataJPARuntimeException, ODataJPAModelException {
    try {
      EdmEntitySet targetEntitySet = uriInfo.getTargetEntitySet();
      String targerEntitySetName = targetEntitySet.getName();
      EdmNavigationProperty navigationProperty = null;
      UriInfo getUriInfo = null;

      if (uriInfo.isLinks()) {
        getUriInfo = parser.parseLink(targetEntitySet, content, requestContentType);
        navigationProperty = uriInfo.getNavigationSegments().get(0).getNavigationProperty();
      } else {
        return;
      }

      if (!getUriInfo.getTargetEntitySet().getName().equals(targerEntitySetName))
      {
        throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.RELATIONSHIP_INVALID, null);
      }

      targetJPAEntity = jpaProcessor.process((GetEntityUriInfo) getUriInfo);
      if (targetJPAEntity != null && sourceJPAEntity == null) {
        int index = context.getODataContext().getPathInfo().getODataSegments().size() - 2;
        getUriInfo = parser.parseURISegment(0, index);
        sourceJPAEntity = jpaProcessor.process((GetEntityUriInfo) getUriInfo);
        if (sourceJPAEntity == null) {
          throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.RESOURCE_X_NOT_FOUND
              .addContent(getUriInfo.getTargetEntitySet().getName()), null);
        }
      }
      if (targetJPAEntity != null && sourceJPAEntity != null) {
        linkJPAEntities(targetJPAEntity, sourceJPAEntity, navigationProperty);
      }

    } catch (IllegalArgumentException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (EdmException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (ODataException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()), e);
    }
  }

  private void
      linkJPAEntities(final Object targetJPAEntity, final Object sourceJPAEntity,
          final EdmNavigationProperty navigationProperty)
          throws ODataJPARuntimeException {
    try {
      JPAEntityParser entityParser = new JPAEntityParser();
      Method setMethod = entityParser.getAccessModifier(sourceJPAEntity,
          navigationProperty, JPAEntityParser.ACCESS_MODIFIER_SET);

      Method getMethod = entityParser.getAccessModifier(sourceJPAEntity,
          navigationProperty, JPAEntityParser.ACCESS_MODIFIER_GET);

      if (getMethod.getReturnType().getTypeParameters() != null
          && getMethod.getReturnType().getTypeParameters().length != 0) {
        @SuppressWarnings("unchecked")
        Collection<Object> relatedEntities = (Collection<Object>) getMethod.invoke(sourceJPAEntity);
        relatedEntities.add(targetJPAEntity);
        setMethod.invoke(sourceJPAEntity, relatedEntities);
      } else {
        setMethod.invoke(sourceJPAEntity, targetJPAEntity);
      }
    } catch (IllegalAccessException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (InvocationTargetException e) {
      throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    }
  }
}
