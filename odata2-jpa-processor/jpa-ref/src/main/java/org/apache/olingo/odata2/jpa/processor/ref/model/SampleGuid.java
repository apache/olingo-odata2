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
package org.apache.olingo.odata2.jpa.processor.ref.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_SAMPLEGUID")
public class SampleGuid {

  public SampleGuid() {}
  
  public SampleGuid(final int id, final String name) {
    super();
    this.id = id;
    this.name = name;
  }
  
  @Column
  private int id;
  
  @Column
  private String name;
  
  @Id
  @Convert(converter=org.apache.olingo.odata2.jpa.processor.ref.converter.UUIDConverter.class)
  @GeneratedValue(generator="reco-UUID")
  @Column(name = "ExternalRecommendationUUID")
  private UUID ExternalRecommendationUUID;

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the externalRecommendationUUID
   */
  public UUID getExternalRecommendationUUID() {
    return ExternalRecommendationUUID;
  }

  /**
   * @param externalRecommendationUUID the externalRecommendationUUID to set
   */
  public void setExternalRecommendationUUID(UUID externalRecommendationUUID) {
    ExternalRecommendationUUID = externalRecommendationUUID;
  }
  
}
