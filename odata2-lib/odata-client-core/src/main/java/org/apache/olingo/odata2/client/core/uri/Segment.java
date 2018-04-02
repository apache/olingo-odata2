package org.apache.olingo.odata2.client.core.uri;

import org.apache.olingo.odata2.client.api.uri.SegmentType;

/**
 * The objects of this class provide the uri segment type and value
 *
 */
public class Segment {

  private final SegmentType type;

  private final String value;

  /**
   * 
   * @param type
   * @param value
   */
  public Segment(final SegmentType type, final String value) {
    this.type = type;
    this.value = value;
  }

  public SegmentType getType() {
    return type;
  }

  public String getValue() {
    return value;
  }
}
