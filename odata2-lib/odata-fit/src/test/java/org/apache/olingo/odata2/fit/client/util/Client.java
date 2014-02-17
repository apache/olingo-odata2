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
package org.apache.olingo.odata2.fit.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySetInfo;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;

public class Client {

  public static final String APPLICATION_JSON = "application/json";
  public static final String APPLICATION_XML = "application/xml";
  public static final String APPLICATION_ATOM_XML = "application/atom+xml";

  public static final String METADATA = "$metadata";

  private String serviceUrl;
  private Proxy.Type protocol;
  private String proxy;
  private int port;
  private boolean useProxy;
  private String username;
  private String password;
  private boolean useAuthentication;

  private Edm edm;

  public Client(final String serviceUrl, final Proxy.Type protocol, final String proxy, final int port)
      throws IOException, ODataException,
      HttpException {
    this.serviceUrl = serviceUrl;
    this.protocol = protocol;
    this.proxy = proxy;
    this.port = port;
    useProxy = true;
    useAuthentication = false;

    edm = getEdmInternal();
  }

  public Client(final String serviceUrl) throws IOException, ODataException, HttpException {
    this.serviceUrl = serviceUrl;
    useProxy = false;
    useAuthentication = false;

    edm = getEdmInternal();
  }

  public Client(final String serviceUrl, final Proxy.Type protocol, final String proxy, final int port,
      final String username, final String password)
      throws IOException, ODataException, HttpException {
    this.serviceUrl = serviceUrl;
    this.protocol = protocol;
    this.proxy = proxy;
    this.port = port;
    useProxy = true;
    this.username = username;
    this.password = password;
    useAuthentication = true;

    edm = getEdmInternal();
  }

  public Client(final String serviceUrl, final String username, final String password) throws IOException,
      ODataException, HttpException {
    this.serviceUrl = serviceUrl;
    useProxy = false;
    this.username = username;
    this.password = password;
    useAuthentication = true;

    edm = getEdmInternal();
  }

  private Edm getEdmInternal() throws IOException, ODataException, HttpException {
    HttpURLConnection connection = connect(METADATA, null, APPLICATION_XML, "GET");
    edm = EntityProvider.readMetadata((InputStream) connection.getContent(), false);
    return edm;
  }

  private void checkStatus(final HttpURLConnection connection) throws IOException, HttpException {
    if (400 <= connection.getResponseCode() && connection.getResponseCode() <= 599) {
      HttpStatusCodes httpStatusCode = HttpStatusCodes.fromStatusCode(connection.getResponseCode());
      throw new HttpException(httpStatusCode, httpStatusCode.getStatusCode() + " " + httpStatusCode.toString());
    }
  }

  public Edm getEdm() {
    return edm;
  }

  public List<EdmEntitySetInfo> getEntitySets() throws ODataException {
    return edm.getServiceMetadata().getEntitySetInfos();
  }

  public ODataFeed readFeed(final String entityContainerName, final String entitySetName, final String contentType)
      throws IOException,
      ODataException, HttpException {
    return readFeed(entityContainerName, entitySetName, contentType, null);
  }

  public ODataFeed readFeed(final String entityContainerName, final String entitySetName, final String contentType,
      final String query)
      throws IOException, ODataException, HttpException {
    EdmEntityContainer entityContainer = edm.getEntityContainer(entityContainerName);
    String relativeUri;
    if (entityContainer.isDefaultEntityContainer()) {
      relativeUri = entitySetName;
    } else {
      relativeUri = entityContainerName + "." + entitySetName;
    }

    InputStream content = (InputStream) connect(relativeUri, query, contentType, "GET").getContent();
    return EntityProvider.readFeed(contentType, entityContainer.getEntitySet(entitySetName), content,
        EntityProviderReadProperties.init().build());
  }

  private HttpURLConnection connect(final String absoluteUri, final String contentType, final String httpMethod)
      throws IOException,
      HttpException {
    URL url = new URL(absoluteUri);
    HttpURLConnection connection;
    if (useProxy) {
      Proxy proxy = new Proxy(protocol, new InetSocketAddress(this.proxy, port));
      connection = (HttpURLConnection) url.openConnection(proxy);
    } else {
      connection = (HttpURLConnection) url.openConnection();
    }
    connection.setRequestMethod(httpMethod);
    connection.setRequestProperty("Accept", contentType);

    if (useAuthentication) {
      String authorization = "Basic ";
      authorization += new String(Base64.encodeBase64((username + ":" + password).getBytes()));
      connection.setRequestProperty("Authorization", authorization);
    }

    connection.connect();

    checkStatus(connection);

    return connection;
  }

  private HttpURLConnection connect(final String relativeUri, final String query, final String contentType,
      final String httpMethod)
      throws IOException,
      HttpException {
    URL url = new URL(serviceUrl + relativeUri + (query != null ? "?" + query : ""));
    return connect(url.toString(), contentType, httpMethod);
  }

  public ODataDeltaFeed
      readDeltaFeed(final String entityContainerName, final String entitySetName, final String contentType,
          final String deltaLink)
          throws IOException, ODataException, HttpException {

    EdmEntityContainer entityContainer = edm.getEntityContainer(entityContainerName);

    InputStream content = (InputStream) connect(deltaLink, contentType, "GET").getContent();

    return EntityProvider.readDeltaFeed(contentType, entityContainer.getEntitySet(entitySetName), content,
        EntityProviderReadProperties.init().build());
  }
}