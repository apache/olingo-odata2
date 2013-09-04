/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.jpa.processor.ref.extension;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.olingo.odata2.api.annotation.edm.Facets;
import org.apache.olingo.odata2.api.annotation.edm.FunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.FunctionImport.Multiplicity;
import org.apache.olingo.odata2.api.annotation.edm.FunctionImport.ReturnType;
import org.apache.olingo.odata2.api.annotation.edm.Parameter;
import org.apache.olingo.odata2.api.annotation.edm.Parameter.Mode;
import org.apache.olingo.odata2.api.annotation.edmx.HttpMethod;
import org.apache.olingo.odata2.api.annotation.edmx.HttpMethod.Name;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.jpa.processor.ref.model.Address;
import org.apache.olingo.odata2.jpa.processor.ref.model.SalesOrderHeader;
import org.apache.olingo.odata2.jpa.processor.ref.model.SalesOrderItem;

public class SalesOrderHeaderProcessor {

  private EntityManager em;

  public SalesOrderHeaderProcessor() {
    em = Persistence.createEntityManagerFactory("salesorderprocessing")
        .createEntityManager();
  }

  @SuppressWarnings("unchecked")
  @FunctionImport(name = "FindAllSalesOrders", entitySet = "SalesOrders", returnType = ReturnType.ENTITY_TYPE, multiplicity = Multiplicity.MANY)
  public List<SalesOrderHeader> findAllSalesOrders(
      @Parameter(name = "DeliveryStatusCode", facets = @Facets(maxLength = 2)) final String status) {

    Query q = em
        .createQuery("SELECT E1 from SalesOrderHeader E1 WHERE E1.deliveryStatus = '"
            + status + "'");
    List<SalesOrderHeader> soList = (List<SalesOrderHeader>) q
        .getResultList();
    return soList;
  }

  @FunctionImport(name = "CheckATP", returnType = ReturnType.SCALAR, multiplicity = Multiplicity.ONE, httpMethod = @HttpMethod(name = Name.GET))
  public boolean checkATP(
      @Parameter(name = "SoID", facets = @Facets(nullable = false), mode = Mode.IN) final Long soID,
      @Parameter(name = "LiId", facets = @Facets(nullable = false), mode = Mode.IN) final Long lineItemID) {
    if (soID == 2L) {
      return false;
    } else {
      return true;
    }
  }

  @FunctionImport(returnType = ReturnType.ENTITY_TYPE, entitySet = "SalesOrders")
  public SalesOrderHeader calculateNetAmount(
      @Parameter(name = "SoID", facets = @Facets(nullable = false)) final Long soID)
      throws ODataException {

    if (soID <= 0L) {
      throw new ODataException("Invalid SoID");
    }

    Query q = em
        .createQuery("SELECT E1 from SalesOrderHeader E1 WHERE E1.soId = "
            + soID + "l");
    if (q.getResultList().isEmpty()) {
      return null;
    }
    SalesOrderHeader so = (SalesOrderHeader) q.getResultList().get(0);
    double amount = 0;
    for (SalesOrderItem soi : so.getSalesOrderItem()) {
      amount = amount
          + (soi.getAmount() * soi.getDiscount() * soi.getQuantity());
    }
    so.setNetAmount(amount);
    return so;
  }

  @SuppressWarnings("unchecked")
  @FunctionImport(returnType = ReturnType.COMPLEX_TYPE)
  public Address getAddress(
      @Parameter(name = "SoID", facets = @Facets(nullable = false)) final Long soID) {
    Query q = em
        .createQuery("SELECT E1 from SalesOrderHeader E1 WHERE E1.soId = "
            + soID + "l");
    List<SalesOrderHeader> soList = (List<SalesOrderHeader>) q
        .getResultList();
    if (!soList.isEmpty()) {
      return soList.get(0).getBuyerAddress();
    } else {
      return null;
    }
  }

  /*
   * This method will not be transformed into Function Import Function Import
   * with return type as void is not supported yet.
   */
  @FunctionImport(returnType = ReturnType.NONE)
  public void process(
      @Parameter(name = "SoID", facets = @Facets(nullable = false)) final Long soID) {
    return;
  }

}
