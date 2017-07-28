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

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.PathSegment;

/**
 *  
 */
public class PathInfoImpl implements PathInfo {

  private List<PathSegment> precedingPathSegment = Collections.emptyList();
  private List<PathSegment> odataPathSegment = Collections.emptyList();
  private URI serviceRoot;
  private URI requestUri;

  public void setODataPathSegment(final List<PathSegment> odataPathSegment) {
    this.odataPathSegment = odataPathSegment;
  }

  public void setPrecedingPathSegment(final List<PathSegment> precedingPathSegment) {
    this.precedingPathSegment = precedingPathSegment;
  }

  public void setServiceRoot(final URI uri) {
    serviceRoot = uri;
  }

  @Override
  public List<PathSegment> getPrecedingSegments() {
    return Collections.unmodifiableList(precedingPathSegment);
  }

  @Override
  public List<PathSegment> getODataSegments() {
    return Collections.unmodifiableList(odataPathSegment);
  }

  @Override
  public URI getServiceRoot() {
    return serviceRoot;
  }

  @Override
  public URI getRequestUri() {
    return requestUri;
  }

  public void setRequestUri(final URI requestUri) {
    this.requestUri = requestUri;
  }
}
