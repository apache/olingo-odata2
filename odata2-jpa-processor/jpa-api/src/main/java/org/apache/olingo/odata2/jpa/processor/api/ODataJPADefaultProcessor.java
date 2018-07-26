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
package org.apache.olingo.odata2.jpa.processor.api;

import org.apache.olingo.odata2.api.batch.BatchHandler;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.SelectItem;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.*;
import org.apache.olingo.odata2.core.uri.UriInfoImpl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class ODataJPADefaultProcessor extends ODataJPAProcessor {

  public ODataJPADefaultProcessor(final ODataJPAContext oDataJPAContext) {
    super(oDataJPAContext);
  }

  @Override
  public ODataResponse readEntitySet(final GetEntitySetUriInfo uriParserResultView, final String contentType)
      throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      oDataJPAContext.setODataContext(getContext());
      List<Object> jpaEntities = jpaProcessor.process(uriParserResultView);
      if (uriParserResultView.isNew()) {
        oDataResponse =
            responseBuilder.build((GetEntityUriInfo)uriParserResultView, jpaEntities.get(0), contentType);
      } else {
        oDataResponse =
            responseBuilder.build(uriParserResultView, jpaEntities, contentType);
      }
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse readEntity(final GetEntityUriInfo uriParserResultView, final String contentType)
      throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      oDataJPAContext.setODataContext(getContext());
      Object jpaEntity = jpaProcessor.process(uriParserResultView);
      oDataResponse =
          responseBuilder.build(uriParserResultView, jpaEntity, contentType);
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse countEntitySet(final GetEntitySetCountUriInfo uriParserResultView, final String contentType)
      throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      oDataJPAContext.setODataContext(getContext());
      long jpaEntityCount = jpaProcessor.process(uriParserResultView);
      oDataResponse = responseBuilder.build(jpaEntityCount);
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse existsEntity(final GetEntityCountUriInfo uriInfo, final String contentType)
      throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      oDataJPAContext.setODataContext(getContext());
      long jpaEntityCount = jpaProcessor.process(uriInfo);
      oDataResponse = responseBuilder.build(jpaEntityCount);
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse createEntity(final PostUriInfo uriParserResultView, final InputStream content,
      final String requestContentType, final String contentType) throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      oDataJPAContext.setODataContext(getContext());
      Object createdJpaEntity = jpaProcessor.process(uriParserResultView, content, requestContentType);
      oDataResponse =
          responseBuilder.build(uriParserResultView, createdJpaEntity, contentType);
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse updateEntity(final PutMergePatchUriInfo uriParserResultView, final InputStream content,
      final String requestContentType, final boolean merge, final String contentType) throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      oDataJPAContext.setODataContext(getContext());


      EdmType type = null;

      if (!(uriParserResultView.getTargetType() instanceof EdmEntityType)) {
        type = uriParserResultView.getTargetType();
        ((UriInfoImpl) uriParserResultView).setTargetType(uriParserResultView.getTargetEntitySet().getEntityType());
      }

      Object jpaEntity = jpaProcessor.process(uriParserResultView, content, requestContentType);

      if (type != null) {
        ((UriInfoImpl) uriParserResultView).setTargetType(type);
      }

      oDataResponse = responseBuilder.build(uriParserResultView, jpaEntity, contentType);
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse deleteEntity(final DeleteUriInfo uriParserResultView, final String contentType)
      throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      oDataJPAContext.setODataContext(getContext());
      Object deletedObj = jpaProcessor.process(uriParserResultView, contentType);
      oDataResponse = responseBuilder.build(uriParserResultView, deletedObj);
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse executeFunctionImport(final GetFunctionImportUriInfo uriParserResultView,
      final String contentType) throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      oDataJPAContext.setODataContext(getContext());
      List<Object> resultEntity = jpaProcessor.process(uriParserResultView);
      oDataResponse =
          responseBuilder.build(uriParserResultView, resultEntity, contentType);
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse executeFunctionImportValue(final GetFunctionImportUriInfo uriParserResultView,
      final String contentType) throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      oDataJPAContext.setODataContext(getContext());
      List<Object> result = jpaProcessor.process(uriParserResultView);
      oDataResponse =
          responseBuilder.build(uriParserResultView, result.get(0));
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse readEntityLink(final GetEntityLinkUriInfo uriParserResultView, final String contentType)
      throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      oDataJPAContext.setODataContext(getContext());
      Object jpaEntity = jpaProcessor.process(uriParserResultView);
      oDataResponse =
          responseBuilder.build(uriParserResultView, jpaEntity, contentType);
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse readEntityLinks(final GetEntitySetLinksUriInfo uriParserResultView, final String contentType)
      throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      oDataJPAContext.setODataContext(getContext());
      List<Object> jpaEntity = jpaProcessor.process(uriParserResultView);
      oDataResponse =
          responseBuilder.build(uriParserResultView, jpaEntity, contentType);
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse createEntityLink(final PostUriInfo uriParserResultView, final InputStream content,
      final String requestContentType, final String contentType) throws ODataException {
    try {
      oDataJPAContext.setODataContext(getContext());
      jpaProcessor.process(uriParserResultView, content, requestContentType, contentType);
      return ODataResponse.newBuilder().build();
    } finally {
      close();
    }
  }

  @Override
  public ODataResponse updateEntityLink(final PutMergePatchUriInfo uriParserResultView, final InputStream content,
      final String requestContentType, final String contentType) throws ODataException {
    try {
      oDataJPAContext.setODataContext(getContext());
      jpaProcessor.process(uriParserResultView, content, requestContentType, contentType);
      return ODataResponse.newBuilder().build();
    } finally {
      close();
    }
  }

  @Override
  public ODataResponse deleteEntityLink(final DeleteUriInfo uriParserResultView, final String contentType)
      throws ODataException {
    try {
      oDataJPAContext.setODataContext(getContext());
      jpaProcessor.process(uriParserResultView, contentType);
      return ODataResponse.newBuilder().build();
    } finally {
      close();
    }
  }

  @Override
  public ODataResponse executeBatch(final BatchHandler handler, final String contentType, final InputStream content)
      throws ODataException {
    try {
      oDataJPAContext.setODataContext(getContext());

      ODataResponse batchResponse;
      List<BatchResponsePart> batchResponseParts = new ArrayList<BatchResponsePart>();
      PathInfo pathInfo = getContext().getPathInfo();
      EntityProviderBatchProperties batchProperties = EntityProviderBatchProperties.init().pathInfo(pathInfo).build();
      List<BatchRequestPart> batchParts = EntityProvider.parseBatchRequest(contentType, content, batchProperties);

      for (BatchRequestPart batchPart : batchParts) {
        batchResponseParts.add(handler.handleBatchPart(batchPart));
      }
      batchResponse = EntityProvider.writeBatchResponse(batchResponseParts);
      return batchResponse;
    } finally {
      close(true);
    }
  }

  @Override
  public BatchResponsePart executeChangeSet(final BatchHandler handler, final List<ODataRequest> requests)
      throws ODataException {
    List<ODataResponse> responses = new ArrayList<ODataResponse>();
    try {
      oDataJPAContext.getODataJPATransaction().begin();

      for (ODataRequest request : requests) {
        oDataJPAContext.setODataContext(getContext());
        ODataResponse response = handler.handleRequest(request);
        if (response.getStatus().getStatusCode() >= HttpStatusCodes.BAD_REQUEST.getStatusCode()) {
          // Rollback
          oDataJPAContext.getODataJPATransaction().rollback();
          List<ODataResponse> errorResponses = new ArrayList<ODataResponse>(1);
          errorResponses.add(response);
          return BatchResponsePart.responses(errorResponses).changeSet(false).build();
        }
        responses.add(response);
      }
      oDataJPAContext.getODataJPATransaction().commit();

      return BatchResponsePart.responses(responses).changeSet(true).build();
    } catch (Exception e) {
      throw new ODataException("Error on processing request content:" + e.getMessage(), e);
    } finally {
      close(true);
    }
  }

  @Override
  public ODataResponse readEntitySimpleProperty(GetSimplePropertyUriInfo uriInfo, String contentType) throws ODataException {
    ODataResponse oDataResponse = null;
    try {
      EdmType type = uriInfo.getTargetType();
      ((UriInfoImpl) uriInfo).setTargetType(uriInfo.getTargetEntitySet().getEntityType());
      oDataJPAContext.setODataContext(getContext());
      Object jpaEntity = jpaProcessor.process((GetEntityUriInfo) uriInfo);
      ((UriInfoImpl) uriInfo).setTargetType(type);
      oDataResponse =
          responseBuilder.build((GetEntityUriInfo) uriInfo, jpaEntity, contentType);
    } finally {
      close();
    }
    return oDataResponse;
  }

  @Override
  public ODataResponse readEntitySimplePropertyValue(final GetSimplePropertyUriInfo uriInfo, final String contentType)
      throws ODataException {
    return readEntitySimpleProperty(uriInfo, contentType);
  }

  @Override
  public ODataResponse updateEntitySimpleProperty(final PutMergePatchUriInfo uriInfo, final InputStream content,
                                                  final String requestContentType, final String contentType) throws ODataException {
    return updateEntity(uriInfo, content, requestContentType, true, contentType);
  }

  @Override
  public ODataResponse updateEntitySimplePropertyValue(PutMergePatchUriInfo uriInfo, InputStream content, String requestContentType, String contentType) throws ODataException {
    return updateEntity(uriInfo, content, requestContentType, true, contentType);
  }
}