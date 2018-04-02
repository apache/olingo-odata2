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
package org.apache.olingo.odata2.client.api.ep;

import java.net.URI;

/**
 * {@link EntityCollectionSerializerProperties} contains all additional properties which are necessary to <b>write
 * (serialize)</b> an {@link org.apache.olingo.odata2.api.ep.entry.ODataEntry} into an specific format (e.g.
 * <code>XML</code> or <code>JSON</code> or ...).
 */
public class EntityCollectionSerializerProperties {

  private URI serviceRoot;
  private URI selfLink;
  
  private EntityCollectionSerializerProperties() {}
  
  /**
   * Gets the self link from an application. May be null.
   * @return the self link
   */
  public final URI getSelfLink() {
    return selfLink;
  }

  /**
   * Gets the service root.
   * @return the service root
   */
  public final URI getServiceRoot() {
    return serviceRoot;
  }

  /**
   * 
   * @param serviceRoot
   * @return EntityCollectionPropertiesBuilder
   */
  public static EntityCollectionPropertiesBuilder serviceRoot(final URI serviceRoot) {
    return new EntityCollectionPropertiesBuilder().serviceRoot(serviceRoot);
  }

  /**
   * This class builds the Entity collection properties
   *
   */
  public static class EntityCollectionPropertiesBuilder {
    private final EntityCollectionSerializerProperties properties = 
        new EntityCollectionSerializerProperties();

    /**
     * @param serviceRoot
     */
    public final EntityCollectionPropertiesBuilder serviceRoot(final URI serviceRoot) {
      properties.serviceRoot = serviceRoot;
      return this;
    }


    /**
     * Build properties object.
     * @return assembled properties object
     */
    public final EntityCollectionSerializerProperties build() {
      return properties;
    }

    /**
     * 
     * @param selfLink
     * @return EntityCollectionPropertiesBuilder
     */
    public EntityCollectionPropertiesBuilder selfLink(final URI selfLink) {
      properties.selfLink = selfLink;
      return this;
    }

    /**
     * 
     * @param properties
     * @return EntityCollectionPropertiesBuilder
     */
    public EntityCollectionPropertiesBuilder fromProperties
    (final EntityCollectionSerializerProperties properties) {
      this.properties.selfLink = properties.getSelfLink();
      return this;
    }

  }

  /**
   * 
   * @param properties
   * @return EntityCollectionPropertiesBuilder
   */
  public static EntityCollectionPropertiesBuilder fromProperties
  (final EntityCollectionSerializerProperties properties) {
    final EntityCollectionPropertiesBuilder builder =
        EntityCollectionSerializerProperties.serviceRoot(properties.getServiceRoot());
    return builder.fromProperties(properties);
  }
}
