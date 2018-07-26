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

import org.apache.olingo.odata2.api.ClientCallback;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.info.*;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Extend this class to build JPA Query object for a given OData request. The extended class can be registered as JPA
 * entity listeners.The implemented JPA Entity Listener classes will be called back from OData JPA Processor Library.
 */
public abstract class ODataJPAQueryExtensionEntityListener extends ODataJPATombstoneEntityListener {
  /**
   * Override this method to build JPA Query for OData request - GetEntitySet; SELECT *
   * @param uriInfo is a reference to OData request
   * @param em is a reference to {@link javax.persistence.EntityManager}
   * @return an instance of type {@link javax.persistence.Query}
   */
  public Query getQuery(GetEntitySetUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
    return null;
  }

  /**
   * Override this method to build JPA Query for OData request - GetEntity; SELECT SINGLE with key in WHERE
   * clause
   * @param uriInfo is a reference to OData request
   * @param em is a reference to {@link javax.persistence.EntityManager}
   * @return an instance of type {@link javax.persistence.Query}
   */
  public Query getQuery(GetEntityUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
    return null;
  }

  /**
   * Override this method to build JPA Query for OData request - GetEntity Count; SELECT SINGLE with key in WHERE
   * clause
   * @param uriInfo is a reference to OData request
   * @param em is a reference to {@link javax.persistence.EntityManager}
   * @return an instance of type {@link javax.persistence.Query}
   */
  public Query getQuery(GetEntityCountUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
    return null;
  }

  /**
   * Override this method to build JPA Query for OData request - GetEntitySet Count; SELECT COUNT(*)
   * @param uriInfo is a reference to OData request
   * @param em is a reference to {@link javax.persistence.EntityManager}
   * @return an instance of type {@link javax.persistence.Query}
   */
  public Query getQuery(GetEntitySetCountUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
    return null;
  }

  /**
   * Override this method to build JPA Query for OData request - Update; SELECT SINGLE with key in WHERE
   * clause
   * @param uriInfo is a reference to OData request
   * @param em is a reference to {@link javax.persistence.EntityManager}
   * @return an instance of type {@link javax.persistence.Query}
   */
  public Query getQuery(PutMergePatchUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
    return null;
  }

  /**
   * Override this method to build JPA Query for OData request - Delete; SELECT SINGLE with key in WHERE
   * clause
   * @param uriInfo is a reference to OData request
   * @param em is a reference to {@link javax.persistence.EntityManager}
   * @return an instance of type {@link javax.persistence.Query}
   */
  public Query getQuery(DeleteUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
    return null;
  }

  @Override
  public String generateDeltaToken(List<Object> deltas, Query query) {
    return null;
  }

  /**
   * Implement this method to indicate whether the extended class can handle OData Tombstone feature as well
   * @return false by default
   */
  @Override
  public boolean isTombstoneSupported() {
    return false;
  }

  protected ODataJPARuntimeException createApplicationError(String message, Locale locale) {
    return ODataJPARuntimeException.throwException(
        ODataJPARuntimeException.GENERAL, new ODataApplicationException(message, locale));
  }

  public boolean authorizeProperty(EdmEntityType entityType, EdmProperty property) {
    return true;
  }

  public void checkAuthorization(final PostUriInfo postView) throws ODataJPARuntimeException {

  }

  public void checkAuthorization(final PutMergePatchUriInfo putView) throws ODataJPARuntimeException {

  }

  public void checkAuthorization(final DeleteUriInfo deleteView) throws ODataJPARuntimeException {

  }

  public void checkEntityGetAuthorization(final EdmEntityType entityType) throws ODataJPARuntimeException {

  }

  public Map<String, Object> getDefaultFieldValues(final EdmEntityType entityType, Object data) throws ODataJPARuntimeException {
    return null;
  }

  public Map<String, Object> getCalcFieldValues(final EdmEntityType entityType, Object data) throws ODataJPARuntimeException {
    return null;
  }

  public void checkFilter(final EdmEntityType entityType, final FilterExpression filter) throws ODataJPARuntimeException {
  }

  public List<ClientCallback> getClientCallbacks() {
    return null;
  }

  public void execEvent(final UriInfo infoView, final EdmEntityType entityType, String type, Object data) throws ODataJPARuntimeException {

  }

  public Object processNew(final UriInfo infoView) {
    return null;
  }
}
