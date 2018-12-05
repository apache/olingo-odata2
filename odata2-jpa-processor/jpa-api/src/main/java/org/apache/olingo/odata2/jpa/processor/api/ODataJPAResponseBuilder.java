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

import java.util.List;

import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityLinkUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetLinksUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetFunctionImportUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;

/**
 * The interface provides methods for building an OData response from a JPA Entity.
 * Implement this interface for building an OData response from a JPA Entity.
 */
public interface ODataJPAResponseBuilder {

  public void setCount(long count);

  /**
   * The method builds an OData response for an OData Query Request from a queried list of JPA Entities.
   * @param queryUriInfo is an information about the request URI
   * @param jpaEntities is an empty or non empty list of queried instances of JPA Entities
   * @param contentType of the response
   * @return an instance of type {@link org.apache.olingo.odata2.api.processor.ODataResponse}
   * @throws ODataJPARuntimeException
   */
  public ODataResponse build(final GetEntitySetUriInfo queryUriInfo, final List<Object> jpaEntities,
      final String contentType) throws ODataJPARuntimeException;

  /**
   * The method builds an OData response for an OData Read Request from a read JPA Entity
   * @param readUriInfo is an information about the request URI
   * @param jpaEntity is a null or non null instances of read JPA Entity
   * @param contentType of the response
   * @return an instance of type {@link org.apache.olingo.odata2.api.processor.ODataResponse}
   * @throws ODataJPARuntimeException
   * @throws ODataNotFoundException
   */
  public ODataResponse build(final GetEntityUriInfo readUriInfo, final Object jpaEntity,
      final String contentType) throws ODataJPARuntimeException,
      ODataNotFoundException;

  /**
   * The method builds an OData response for an OData Create Request from a created JPA entity.
   * @param postUriInfo is an information about the request URI
   * @param createdObject is a null or non null instances of JPA Entity
   * @param contentType of the response
   * @return an instance of type {@link org.apache.olingo.odata2.api.processor.ODataResponse}
   * @throws ODataJPARuntimeException
   * @throws ODataNotFoundException
   */
  public ODataResponse build(final PostUriInfo postUriInfo, final Object createdObject,
      final String contentType) throws ODataJPARuntimeException,
      ODataNotFoundException;

  /**
   * The method builds an OData response for an OData Update Request from an updated JPA Entity
   * @param putUriInfo is an information about the request URI
   * @param updatedObject is an updated instance of JPA Entity
   * @return an instance of type {@link org.apache.olingo.odata2.api.processor.ODataResponse}
   * @throws ODataJPARuntimeException
   * @throws ODataNotFoundException
   */
  public ODataResponse build(final PutMergePatchUriInfo putUriInfo, final Object updatedObject, final String contentType)
      throws ODataJPARuntimeException, ODataNotFoundException;

  /**
   * The method builds an OData response for an OData Delete Request from a deleted JPA Entity
   * @param deleteUriInfo is an information about the request URI
   * @param deletedObject is an null or non null instance of deleted JPA Entity. Null implies Entity not found.
   * @return an instance of type {@link org.apache.olingo.odata2.api.processor.ODataResponse}
   * @throws ODataJPARuntimeException
   * @throws ODataNotFoundException
   */
  public ODataResponse build(final DeleteUriInfo deleteUriInfo, final Object deletedObject)
      throws ODataJPARuntimeException, ODataNotFoundException;

  /**
   * The method builds an OData response for an OData function Import Request from a registered processor method's
   * return parameter.
   * @param functionImportUriInfo is an information about the request URI
   * @param result is a method's return parameter
   * @return an instance of type {@link org.apache.olingo.odata2.api.processor.ODataResponse}
   * @throws ODataJPARuntimeException
   */
  public ODataResponse build(final GetFunctionImportUriInfo functionImportUriInfo, final Object result)
      throws ODataJPARuntimeException;

  /**
   * The method builds an OData response for an OData function Import Request from a registered processor method's
   * return parameter. The return parameter is a collection of objects.
   * @param functionImportUriInfo is an information about the request URI
   * @param result is a method's return parameter is a collection of objects.
   * @return an instance of type {@link org.apache.olingo.odata2.api.processor.ODataResponse}
   * @throws ODataJPARuntimeException
   */
  public ODataResponse build(final GetFunctionImportUriInfo functionImportUriInfo, final List<Object> resultList,
      final String contentType)
      throws ODataJPARuntimeException, ODataNotFoundException;

  /**
   * The method builds an OData response for an OData Read Link Request from a read JPA Entity and its related JPA
   * Entities.
   * @param readLinkUriInfo is an information about the request URI
   * @param jpaEntity is a null or non null read JPA Entity and its related JPA Entities.
   * @param contentType of the response
   * @return an instance of type {@link org.apache.olingo.odata2.api.processor.ODataResponse}
   * @throws ODataNotFoundException
   * @throws ODataJPARuntimeException
   */
  public ODataResponse build(final GetEntityLinkUriInfo readLinkUriInfo, final Object jpaEntity,
      final String contentType) throws ODataNotFoundException,
      ODataJPARuntimeException;

  /**
   * The method builds an OData response for an OData Query Link Request from a queried JPA Entity and its related JPA
   * Entities.
   * @param queryLinkUriInfo is an information about the request URI
   * @param jpaEntity is an empty or non empty list of queried JPA Entities
   * @param contentType of the response
   * @return an instance of type {@link org.apache.olingo.odata2.api.processor.ODataResponse}
   * @throws ODataJPARuntimeException
   */
  public ODataResponse build(final GetEntitySetLinksUriInfo queryLinkUriInfo, final List<Object> jpaEntity,
      final String contentType) throws ODataJPARuntimeException;

  /**
   * The method builds an OData response from a count representing total number of JPA Entities
   * @param jpaEntityCount is the count value
   * @return an instance of type {@link org.apache.olingo.odata2.api.processor.ODataResponse}
   * @throws ODataJPARuntimeException
   */
  public ODataResponse build(final long jpaEntityCount)
      throws ODataJPARuntimeException;

}
