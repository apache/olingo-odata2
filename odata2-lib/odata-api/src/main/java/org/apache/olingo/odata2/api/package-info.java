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
/**
 * OData Library API
 * <p>
 * OData Library is a protocol implementation of the OData V2.0 standard. For details of this standard
 * see <a href="http://odata.org">odata.org</a>.
 * <p>
 * This API is intended to implement an OData service. An OData service consists of a metadata provider
 * implementation and an OData processor implementation.
 * <p>
 * An OData service can be exposed by a web application. For the runntime one JAX-RS
 * implementation is needed and the core implementation library of this API. Apache CXF for example is
 * one such JAX-RS implementation.
 * <p>
 * Entry point to the service is a JAX-RS servlet. At this servlet init parameters for a
 * <code>ODataServiceFactory</code>
 * is configured. The parameter <code>jakarta.ws.rs.Application</code> is a default by JAX-RS and has to be present
 * always.
 * <p>
 * <pre> {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *   xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
 *   xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
 *   id="WebApp_ID" version="2.5">
 *   <display-name>Example OData Service</display-name>
 *   <servlet>
 *     <servlet-name>MyODataServlet</servlet-name>
 *     <servlet-class>org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet</servlet-class>
 *     <init-param>
 *      <param-name>jakarta.ws.rs.Application</param-name>
 *      <param-value>org.apache.olingo.odata2.core.rest.app.ODataApplication</param-value>
 *     </init-param>
 *     <init-param>
 *       <param-name>org.apache.olingo.odata2.service.factory</param-name>
 *       <param-value>com.sample.service.MyServiceFactory</param-value>
 *     </init-param>
 *     <load-on-startup>1</load-on-startup>
 *   </servlet>
 *   <servlet-mapping>
 *     <servlet-name>MyODataServlet</servlet-name>
 *     <url-pattern>/MyService.svc/*</url-pattern>
 *   </servlet-mapping>
 * </web-app>
 * } </pre>
 * <p>
 * This factory produces the service, a metadata provider and the data processor. The provider, typically
 * a derivative of the class <code>EdmProvider</code> provides the metadata of the service. The processor implements a
 * variety of service interfaces, and provides the data of the service. The processor is typically
 * a derivative of the class <code>ODataSingleProcessor</code> which can be used together with the class
 * <code>ODataSingleService</code>.
 */
package org.apache.olingo.odata2.api;

