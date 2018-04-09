package org.apache.olingo.odata2.client.api.uri;

/**
 * This is a list of query options
 *
 */
public enum QueryOption {

  /**
   * The $count system query option allows clients to request a count of the matching resources included with the
   * resources in the response. The $count query option has a Boolean value of true or false.
   */
  COUNT("$count"),
  /**
   * This option indicates entities associated with the EntityType instance or EntitySet, identified by the resource
   * path section of the URI, and MUST be represented inline in the data service's response.
   */
  EXPAND("$expand"),
  /**
   * This option specifies the media type acceptable in a response. If present, this value SHOULD take precedence over
   * value(s) specified in an Accept request header.
   */
  FORMAT("$format"),
  /**
   * This option is used to specify that a subset of the properties of the entities identified by the path of the
   * request URI and $expand query option SHOULD be returned in the response from the data service.
   */
  SELECT("$select"),
  /**
   * This option specifies the sort properties and sort direction (ascending or descending) that the data service MUST
   * use to order the entities in the EntitySet, identified by the resource path section of the URI.
   */
  ORDERBY("$orderby"),
  /**
   * This option specifies a positive integer N that is the maximum number of entities in the EntitySet, identified by
   * the resource path section of the URI, that the data service MUST return.
   */
  TOP("$top"),
  /**
   * This option specifies a positive integer N that represents the number of entities, counted from the first entity in
   * the EntitySet and ordered as specified by the $orderby option, that the data service should skip when returning the
   * entities in the EntitySet, which is identified by the resource path section of the URI. The data service SHOULD
   * return all subsequent entities, starting from the one in position N+1.
   */
  SKIP("$skip"),
  /**
   * The value of a $skiptoken query option is an opaque token which identifies an index into the collection
   * of entities identified by the URI containing the $skiptoken parameter.
   */
  SKIPTOKEN("$skiptoken"),
  /**
   * This option specifies a predicate used to filter the elements from the EntitySet identified by the resource path
   * section of the URI.
   */
  FILTER("$filter");
  
  private final String value;
  
  private QueryOption() {
    this.value = "";
  } 
  
  private QueryOption(final String value) {
    this.value = value;
  }
  
  public String getValue() {
    return value;
  }
}
