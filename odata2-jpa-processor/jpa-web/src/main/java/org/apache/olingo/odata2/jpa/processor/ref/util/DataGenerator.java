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
package org.apache.olingo.odata2.jpa.processor.ref.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.odata2.jpa.processor.ref.model.Material;
import org.apache.olingo.odata2.jpa.processor.ref.model.Store;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.queries.DataModifyQuery;
import org.eclipse.persistence.queries.SQLCall;
import org.eclipse.persistence.sessions.Session;

/**
 * This is a utility class for generating and cleaning data. The generated data would be used by the application.
 * 
 * 
 */
public class DataGenerator {

  private EntityManager entityManager;

  /**
   * This is configuration property to hold comma separated names of Insert Files
   */
  private static final String SQL_INSERT_CONFIG = "SQL_Insert_Config";

  /**
   * This is key which will be used to fetch file names from SQL Insert Config File.
   */
  private static final String SQL_INSERT_FILE_NAMES_KEY = "insert_file_names";

  private static final String SQL_DELETE_CONFIG = "SQL_Cleanup";
  private static final String SQL_DELETE_STATEMENTS_KEY = "delete_queries";

  public DataGenerator(final EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * This method generates data to be used in the application. It does so by
   * reading properties file. Currently it iterates through comma separated
   * file names in file SQLInsertConfig and gets the insert statements from
   * those files in the order provided in the file.
   */
  public void generate() {
    String[] resourceSQLPropFileNames = getSQLInsertFileNames();
    if (resourceSQLPropFileNames.length > 0) { // If configuration is proper with at least one file
      Session session = ((EntityManagerImpl) entityManager).getActiveSession();
      ResourceBundle[] resourceBundleArr = new ResourceBundle[resourceSQLPropFileNames.length];
      entityManager.getTransaction().begin();

      for (int i = 0; i < resourceSQLPropFileNames.length; i++) { // For each Entity SQL property file,
        System.out.println("Reading from File - " + resourceSQLPropFileNames[i]);
        resourceBundleArr[i] = ResourceBundle.getBundle(resourceSQLPropFileNames[i]);// Get SQL statements as properties

        Set<String> keySet = resourceBundleArr[i].keySet();
        List<String> queryNames = new ArrayList<String>(keySet);
        Collections.sort(queryNames);

        for (String queryName : queryNames) {
//        while(keySet.hasMoreElements()) {
          String sqlQuery = resourceBundleArr[i].getString(queryName);
          System.out.println("Executing Query - " + sqlQuery);
          SQLCall sqlCall = new SQLCall(sqlQuery);

          DataModifyQuery query = new DataModifyQuery();
          query.setCall(sqlCall);
          session.executeQuery(query);
        }
      }
      setMaterialInStore();
      entityManager.flush();
      entityManager.getTransaction().commit();
    }

  }

  @SuppressWarnings("unchecked")
  private void setMaterialInStore() {
    Query query = entityManager.createQuery("SELECT e FROM Material e");
    List<Material> materials = (List<Material>) query.getResultList();

    query = entityManager.createQuery("SELECT e FROM Store e");
    List<Store> stores = (List<Store>) query.getResultList();

    int storeSize = stores.size();
    int i = 0;
    for (Material material : materials) {
      List<Store> storesA = Arrays.asList(stores.get(i), stores.get(i + 1));
      material.setStores(storesA);
      i++;
      if (i > storeSize - 2) {
        i = 0;
      }
      entityManager.persist(material);
    }
    entityManager.flush();
  }

  private String[] getSQLInsertFileNames() {
    ResourceBundle resourceBundle = ResourceBundle.getBundle(SQL_INSERT_CONFIG);// File names from properties
    String namesStr = resourceBundle.getString(SQL_INSERT_FILE_NAMES_KEY);
    return namesStr.split(",");
  }

  private String[] getSQLDeleteStatements() {
    ResourceBundle resourceBundle = ResourceBundle.getBundle(SQL_DELETE_CONFIG);// File names from properties
    String deleteStatements = resourceBundle.getString(SQL_DELETE_STATEMENTS_KEY);
    return deleteStatements.split(",");
  }

  /**
   * This method deletes data from JPA tables created. This method reads comma
   * separated SQL delete statements from DataDeleteSQLs properties files and
   * executes them in order.
   */
  public void clean() {
    // Delete using SQLs
    String[] deleteStatements = getSQLDeleteStatements();
    if (deleteStatements.length > 0) { // If configuration is proper with at least one delete Statements
      Session session = ((EntityManagerImpl) entityManager).getActiveSession();
      entityManager.getTransaction().begin();
      for (String deleteStatement : deleteStatements) {
        System.out.println("Cleaning - " + deleteStatement);
        SQLCall sqlCall = new SQLCall(deleteStatement);

        DataModifyQuery query = new DataModifyQuery();
        query.setCall(sqlCall);
        session.executeQuery(query);
      }
      entityManager.getTransaction().commit();
    } else {
      System.err.println("Delete configuration file doesn't have any delete statements.");
    }
  }

}
