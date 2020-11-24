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
package org.apache.olingo.odata2.core;

import java.io.InputStream;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.batch.BatchHandler;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataMethodNotAllowedException;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.part.BatchProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityComplexPropertyProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityLinkProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityLinksProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityMediaProcessor;
import org.apache.olingo.odata2.api.processor.part.EntityProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySetProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySimplePropertyProcessor;
import org.apache.olingo.odata2.api.processor.part.EntitySimplePropertyValueProcessor;
import org.apache.olingo.odata2.api.processor.part.FunctionImportProcessor;
import org.apache.olingo.odata2.api.processor.part.FunctionImportValueProcessor;
import org.apache.olingo.odata2.api.processor.part.MetadataProcessor;
import org.apache.olingo.odata2.api.processor.part.ServiceDocumentProcessor;
import org.apache.olingo.odata2.core.batch.BatchHandlerImpl;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;
import org.apache.olingo.odata2.core.uri.UriInfoImpl;

/**
 * Request dispatching according to URI type and HTTP method.
 * 
 */
public class Dispatcher {

  private final ODataService service;
  private final ODataServiceFactory serviceFactory;

  public Dispatcher(final ODataServiceFactory serviceFactory, final ODataService service) {
    this.service = service;
    this.serviceFactory = serviceFactory;
  }

  public ODataResponse dispatch(final ODataHttpMethod method, final UriInfoImpl uriInfo, final InputStream content,
      final String requestContentType, final String contentType) throws ODataException {
    switch (uriInfo.getUriType()) {
    case URI0:
      if (method == ODataHttpMethod.GET) {
        return service.getServiceDocumentProcessor().readServiceDocument(uriInfo, contentType);
      } else {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI1:
    case URI6B:
      switch (method) {
      case GET:
        return service.getEntitySetProcessor().readEntitySet(uriInfo, contentType);
      case POST:
        return service.getEntitySetProcessor().createEntity(uriInfo, content, requestContentType, contentType);
      default:
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI2:
      switch (method) {
      case GET:
        return service.getEntityProcessor().readEntity(uriInfo, contentType);
      case PUT:
        return service.getEntityProcessor().updateEntity(uriInfo, content, requestContentType, false, contentType);
      case PATCH:
      case MERGE:
        return service.getEntityProcessor().updateEntity(uriInfo, content, requestContentType, true, contentType);
      case DELETE:
        return service.getEntityProcessor().deleteEntity(uriInfo, contentType);
      default:
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI3:
      switch (method) {
      case GET:
        return service.getEntityComplexPropertyProcessor().readEntityComplexProperty(uriInfo, contentType);
      case PUT:
        return service.getEntityComplexPropertyProcessor().updateEntityComplexProperty(uriInfo, content,
            requestContentType, false, contentType);
      case PATCH:
      case MERGE:
        return service.getEntityComplexPropertyProcessor().updateEntityComplexProperty(uriInfo, content,
            requestContentType, true, contentType);
      default:
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI4:
    case URI5:
      switch (method) {
      case GET:
        if (uriInfo.isValue()) {
          return service.getEntitySimplePropertyValueProcessor().readEntitySimplePropertyValue(uriInfo, contentType);
        } else {
          return service.getEntitySimplePropertyProcessor().readEntitySimpleProperty(uriInfo, contentType);
        }
      case PUT:
      case PATCH:
      case MERGE:
        if (uriInfo.isValue()) {
          return service.getEntitySimplePropertyValueProcessor().updateEntitySimplePropertyValue(uriInfo, content,
              requestContentType, contentType);
        } else {
          return service.getEntitySimplePropertyProcessor().updateEntitySimpleProperty(uriInfo, content,
              requestContentType, contentType);
        }
      case DELETE:
        if (uriInfo.isValue()) {
          return service.getEntitySimplePropertyValueProcessor().deleteEntitySimplePropertyValue(uriInfo, contentType);
        } else {
          throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
        }
      default:
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI6A:
      switch (method) {
      case GET:
        return service.getEntityProcessor().readEntity(uriInfo, contentType);
      case PUT:
      case PATCH:
      case MERGE:
      case DELETE:
        throw new ODataBadRequestException(ODataBadRequestException.NOTSUPPORTED);
      default:
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI7A:
      switch (method) {
      case GET:
        return service.getEntityLinkProcessor().readEntityLink(uriInfo, contentType);
      case PUT:
      case PATCH:
      case MERGE:
        return service.getEntityLinkProcessor().updateEntityLink(uriInfo, content, requestContentType, contentType);
      case DELETE:
        return service.getEntityLinkProcessor().deleteEntityLink(uriInfo, contentType);
      default:
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI7B:
      switch (method) {
      case GET:
        return service.getEntityLinksProcessor().readEntityLinks(uriInfo, contentType);
      case POST:
        return service.getEntityLinksProcessor().createEntityLink(uriInfo, content, requestContentType, contentType);
      default:
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI8:
      if (method == ODataHttpMethod.GET) {
        return service.getMetadataProcessor().readMetadata(uriInfo, contentType);
      } else {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI9:
      if (method == ODataHttpMethod.POST) {
        BatchHandler handler = new BatchHandlerImpl(serviceFactory, service);
        return service.getBatchProcessor().executeBatch(handler, requestContentType, content);
      } else {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI10:
    case URI10a:
    case URI11:
    case URI12:
    case URI13:
      return service.getFunctionImportProcessor().executeFunctionImport(uriInfo, contentType);

    case URI14:
      if (uriInfo.isValue()) {
        return service.getFunctionImportValueProcessor().executeFunctionImportValue(uriInfo, contentType);
      } else {
        return service.getFunctionImportProcessor().executeFunctionImport(uriInfo, contentType);
      }

    case URI15:
      if (method == ODataHttpMethod.GET) {
        return service.getEntitySetProcessor().countEntitySet(uriInfo, contentType);
      } else {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI16:
      if (method == ODataHttpMethod.GET) {
        return service.getEntityProcessor().existsEntity(uriInfo, contentType);
      } else {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI17:
      switch (method) {
      case GET:
        return service.getEntityMediaProcessor().readEntityMedia(uriInfo, contentType);
      case PUT:
        return service.getEntityMediaProcessor().updateEntityMedia(uriInfo, content, requestContentType, contentType);
      case DELETE:
        return service.getEntityMediaProcessor().deleteEntityMedia(uriInfo, contentType);
      default:
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI50A:
      if (method == ODataHttpMethod.GET) {
        return service.getEntityLinkProcessor().existsEntityLink(uriInfo, contentType);
      } else {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    case URI50B:
      if (method == ODataHttpMethod.GET) {
        return service.getEntityLinksProcessor().countEntityLinks(uriInfo, contentType);
      } else {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.DISPATCH);
      }

    default:
      throw new ODataRuntimeException("Unknown or not implemented URI type: " + uriInfo.getUriType());
    }
  }

  protected static Class<? extends ODataProcessor> mapUriTypeToProcessorFeature(final UriInfoImpl uriInfo) {
    Class<? extends ODataProcessor> feature;

    switch (uriInfo.getUriType()) {
    case URI0:
      feature = ServiceDocumentProcessor.class;
      break;
    case URI1:
    case URI6B:
    case URI15:
      feature = EntitySetProcessor.class;
      break;
    case URI2:
    case URI6A:
    case URI16:
      feature = EntityProcessor.class;
      break;
    case URI3:
      feature = EntityComplexPropertyProcessor.class;
      break;
    case URI4:
    case URI5:
      feature = uriInfo.isValue() ? EntitySimplePropertyValueProcessor.class : EntitySimplePropertyProcessor.class;
      break;
    case URI7A:
    case URI50A:
      feature = EntityLinkProcessor.class;
      break;
    case URI7B:
    case URI50B:
      feature = EntityLinksProcessor.class;
      break;
    case URI8:
      feature = MetadataProcessor.class;
      break;
    case URI9:
      feature = BatchProcessor.class;
      break;
    case URI10:
    case URI10a:
    case URI11:
    case URI12:
    case URI13:
      feature = FunctionImportProcessor.class;
      break;
    case URI14:
      feature = uriInfo.isValue() ? FunctionImportValueProcessor.class : FunctionImportProcessor.class;
      break;
    case URI17:
      feature = EntityMediaProcessor.class;
      break;
    default:
      throw new ODataRuntimeException("Unknown or not implemented URI type: " + uriInfo.getUriType());
    }

    return feature;
  }
}
