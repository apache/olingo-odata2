package org.apache.olingo.odata2.client.api.uri;

/**
 * URI Segment types.
 */
public enum SegmentType {

  /**
   * Initial will be the builder state to begin with a service root url
   */
  INITIAL,
  /**
   * When we append entity set state is changed to ENTITYSET
   */
  ENTITYSET,
  /**
   * 
   */
  ENTITY,
  /**
   * When we append simple property state is changed to SIMPLEPROPERTY
   */
  SIMPLEPROPERTY,
  /**
   * When there is a key in segment
   */
  KEY,
  /**
   * When we append simple complex state is changed to COMPLEXPROPERTY
   */
  COMPLEXPROPERTY,
  /**
   * When there is navigation to many then the state is NAVIGATION_TO_MANY
   */
  NAVIGATION_TO_MANY,
  /**
   * When there is navigation to many with a key then the state is
   * NAVIGATION_TO_MANY_WITH_KEY
   */
  NAVIGATION_TO_MANY_WITH_KEY,
  /**
   * When there is navigation to one then the state is NAVIGATION_TO_ONE
   */
  NAVIGATION_TO_ONE,
  /**
   * When there is count query option
   */
  COUNT("$count"),
  /**
   * When there is a value query option
   */
  VALUE("$value"),
  /**
   * When there is a metadata uri segment
   */
  METADATA("$metadata"),
  PROPERTY,
  NAVIGATION,
  FUNCTIONIMPORT,
  FUNCTIONIMPORT_WITH_KEY,
  FUNCTIONIMPORT_MANY;

  private final String value;

  private SegmentType() {
    this.value = "";
  }

  private SegmentType(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
