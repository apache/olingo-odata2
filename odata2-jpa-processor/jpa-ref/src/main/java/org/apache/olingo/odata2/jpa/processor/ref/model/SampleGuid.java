package org.apache.olingo.odata2.jpa.processor.ref.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

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
