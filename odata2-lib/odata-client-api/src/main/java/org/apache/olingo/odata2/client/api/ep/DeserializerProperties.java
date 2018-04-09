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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.client.api.ep.callback.OnDeserializeInlineContent;

/**
 * <p>The {@link EntityProviderReadProperties} contains all necessary settings
 * to read an entity with the {@link EntityProvider}.</p>
 * <p>The main settings are
 * <ul>
 * <li>the <code>type mappings</code></li>
 * <li>and <code>validatingFacets</code></li>
 * </ul>
 * </p>
 */
public class DeserializerProperties{
  /** Callback which is necessary if entity contains inlined navigation properties. */
  private OnDeserializeInlineContent callback;
  /**
   * typeMappings contains mappings from <code>edm property name</code> to <code>java class</code> which should be used
   * for a type mapping during reading of content. If according <code>edm property</code> can not be read
   * into given <code>java class</code> an {@link EntityProviderException} is thrown.
   * Supported mappings are documented in {@link org.apache.olingo.odata2.api.edm.EdmSimpleType}.
   */
  private final Map<String, Object> typeMappings;//entity
  
  /** whether the constraints expressed in properties' facets are validated */
  private boolean validatingFacets = true;//entity

  private final Map<String, String> validatedPrefix2NamespaceUri;//Collection

  private DeserializerProperties() {
    typeMappings = new HashMap<String, Object>();
    validatedPrefix2NamespaceUri = new HashMap<String, String>();
  }

  /**
   * 
   * @return DeserializerPropertiesBuilder
   */
  public static DeserializerPropertiesBuilder init() {
    return new DeserializerPropertiesBuilder();
  }

  /**
   * 
   * @param properties
   * @return DeserializerPropertiesBuilder
   */
  public static DeserializerPropertiesBuilder initFrom
  (final DeserializerProperties properties) {
    return new DeserializerPropertiesBuilder(properties);
  }
  /**
   * 
   * @return TypeMappings
   */
  public Map<String, Object> getTypeMappings() {
    return Collections.unmodifiableMap(typeMappings);
  }

  public Map<String, String> getValidatedPrefixNamespaceUris() {
    return Collections.unmodifiableMap(validatedPrefix2NamespaceUri);
  }


  public boolean isValidatingFacets() {
    return validatingFacets;
  }
  
  public OnDeserializeInlineContent getCallback() {
    return callback;
  }

  /**
   * Builder for {@link EntityProviderReadProperties}.
   */
  public static class DeserializerPropertiesBuilder {
    private final DeserializerProperties properties = new DeserializerProperties();

    public DeserializerPropertiesBuilder() {}
    /**
     * 
     * @param propertiesFrom
     */
    public DeserializerPropertiesBuilder(final DeserializerProperties propertiesFrom) {
      addValidatedPrefixes(propertiesFrom.validatedPrefix2NamespaceUri);
      addTypeMappings(propertiesFrom.typeMappings);
      properties.validatingFacets = propertiesFrom.validatingFacets;
      properties.callback = propertiesFrom.callback;
    }

    /**
     * 
     * @param prefix2NamespaceUri
     * @return DeserializerPropertiesBuilder
     */
    public DeserializerPropertiesBuilder addValidatedPrefixes
    (final Map<String, String> prefix2NamespaceUri) {
      if (prefix2NamespaceUri != null) {
        properties.validatedPrefix2NamespaceUri.putAll(prefix2NamespaceUri);
      }
      return this;
    }

    /**
     * 
     * @param typeMappings
     * @return DeserializerPropertiesBuilder
     */
    public DeserializerPropertiesBuilder addTypeMappings(final Map<String, Object> typeMappings) {
      if (typeMappings != null) {
        properties.typeMappings.putAll(typeMappings);
      }
      return this;
    }
    
    /**
     * 
     * @param callback
     * @return DeserializerPropertiesBuilder
     */
    public DeserializerPropertiesBuilder callback(final OnDeserializeInlineContent callback) {
      properties.callback = callback;
      return this;
    }

    /**
     * 
     * @param validatingFacets
     * @return DeserializerPropertiesBuilder
     */
    public DeserializerPropertiesBuilder isValidatingFacets(final boolean validatingFacets) {
      properties.validatingFacets = validatingFacets;
      return this;
    }

    /**
     * 
     * @return DeserializerProperties
     */
    public DeserializerProperties build() {
      return properties;
    }
  }
}
