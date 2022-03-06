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
package org.apache.olingo.odata2.jpa.processor.api.factory;

/**
 * The class is an abstract factory for creating default ODataJPAFactory. The
 * class's actual implementation is responsible for creating other factory
 * implementations.The class creates factories implementing interfaces
 * <ul>
 * <li>{@link org.apache.olingo.odata2.jpa.processor.api.factory.JPAAccessFactory}</li>
 * <li>{@link org.apache.olingo.odata2.jpa.processor.api.factory.JPQLBuilderFactory}</li>
 * <li>{@link org.apache.olingo.odata2.jpa.processor.api.factory.JPQLBuilderFactory}</li>
 * </ul>
 * 
 * <b>Note: </b>Extend this class only if you don't require library's default
 * factory implementation.
 * <p>
 * 
 * 
 * 
 * 
 * 
 */
public abstract class ODataJPAFactory {

  private static String IMPLEMENTATION =
      "org.apache.olingo.odata2.jpa.processor.core.factory.ODataJPAFactoryImpl";
  private static ODataJPAFactory factoryImpl;

  /**
   * Method sets the implementation of the ODataJPAFactory. This makes
   * it possible to change some default implementation like JPAAccessFactory,
   * JPQLBuilderFactory, and JPQLBuilderFactory
   */
  public static void setImplementation(Class<?> clazz) {
      IMPLEMENTATION = clazz.getName();
  }

  /**
   * Method creates a factory instance. The instance returned is singleton.
   * The instance of this factory can be used for creating other factory
   * implementations.
   * 
   * @return instance of type {@link org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAFactory} .
   */
  public static ODataJPAFactory createFactory() {
    if (factoryImpl == null) {
      try {
        Class<?> clazz = Class.forName(ODataJPAFactory.IMPLEMENTATION);

        Object object = clazz.newInstance();
        factoryImpl = (ODataJPAFactory) object;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return factoryImpl;
  }

  /**
   * The method returns a null reference to JPQL Builder Factory. Override
   * this method to return an implementation of JPQLBuilderFactory if default
   * implementation from library is not required.
   * 
   * @return instance of type {@link org.apache.olingo.odata2.jpa.processor.api.factory.JPQLBuilderFactory}
   */
  public JPQLBuilderFactory getJPQLBuilderFactory() {
    return null;
  }

  /**
   * The method returns a null reference to JPA Access Factory. Override this
   * method to return an implementation of JPAAccessFactory if default
   * implementation from library is not required.
   * 
   * @return instance of type {@link org.apache.olingo.odata2.jpa.processor.api.factory.JPQLBuilderFactory}
   */
  public JPAAccessFactory getJPAAccessFactory() {
    return null;
  }

  /**
   * The method returns a null reference to OData JPA Access Factory. Override
   * this method to return an implementation of ODataJPAAccessFactory if
   * default implementation from library is not required.
   * 
   * @return instance of type {@link org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAAccessFactory}
   */
  public ODataJPAAccessFactory getODataJPAAccessFactory() {
    return null;
  }
}
