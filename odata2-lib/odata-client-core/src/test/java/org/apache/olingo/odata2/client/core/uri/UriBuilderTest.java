package org.apache.olingo.odata2.client.core.uri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.client.api.uri.QueryOption;
import org.junit.Test;

public class UriBuilderTest {
  protected static final String SERVICE_ROOT_URI = "http://host:80/service/";
  protected static final String SERVICE_ROOT_URI_1 = "http://host:80/service";
  
  @Test
  public void testUriSimpleES() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees", uri.toASCIIString());
  }
  
  @Test
  public void testSimpleESWithCountUri1() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    appendCountSegment().
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees/$count", uri.toASCIIString());
  }
  
  @Test
  public void testSimpleESWithCountUri2() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1").
    appendNavigationSegment("nm_Employees").
    appendCountSegment().
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1')/nm_Employees/$count", uri.toASCIIString());
  }
  
  @Test
  public void testSimpleESWithCountAndFilter() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1").
    appendNavigationSegment("nm_Employees").
    appendCountSegment().
    addQueryOption(QueryOption.FILTER, "TeamId eq '1'").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1')/nm_Employees/"
        + "$count?$filter=TeamId%20eq%20'1'", uri.toASCIIString());
  }
  
  @Test
  public void testMetadataUri() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendMetadataSegment().
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/$metadata", uri.toASCIIString());
  }
  
  @Test
  public void testSimpleESWithKeyUri() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    appendKeySegment("1").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees('1')", uri.toASCIIString());
  }
  
  @Test
  public void testSimpleESWithKeyUri1() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    appendKeySegment(1).
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees(1)", uri.toASCIIString());
  }
  
  @Test
  public void testCompositeKeysUri() {
    Map<String, Object> keyMap = new LinkedHashMap<String,Object>();
    keyMap.put("Id", 4);
    keyMap.put("Type", "foo");
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Photos").
    appendKeySegment(keyMap).
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Photos(Id=4,Type='foo')", uri.toASCIIString());
  }
  
  @Test
  public void testFilterUri() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Photos").
    filter("Name eq 'Photo 1'").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Photos?$filter=Name%20eq%20'Photo%201'", uri.toASCIIString());
  }
  
  @Test
  public void testTopUri1() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Photos").
    top(2).
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Photos?$top=2", uri.toASCIIString());
  }
  
  @Test
  public void testTopUri2() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1").
    appendNavigationSegment("nm_Employees").
    addQueryOption(QueryOption.FILTER, "TeamId eq '1'").
    top(2).
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1')/nm_Employees"
        + "?$filter=TeamId%20eq%20'1'&$top=2", uri.toASCIIString());
  }
  
  @Test
  public void testSkipUri() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Photos").
    skip(2).
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Photos?$skip=2", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithQueryOptions() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Photos").
    filter("Name eq 'Photo 1'").
    top(2).
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Photos?$filter=Name%20eq%20'Photo%201'&$top=2", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithNavigationSegment2() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1").
    appendNavigationSegment("nm_Employees").
    appendKeySegment("1").
    appendNavigationSegment("ne_Team").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1')/nm_Employees('1')/ne_Team", uri.toASCIIString());
  }
  @Test
  public void testUriWithSimplePropertySegment() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    appendKeySegment("1").
    appendPropertySegment("EmployeeName").
    appendValueSegment().
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees('1')/EmployeeName/$value", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithComplexPropertySegment() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    appendKeySegment("1").
    appendPropertySegment("Location").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees('1')/Location", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithComplexPropertySegment1() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    appendKeySegment("1").
    appendPropertySegment("Location").
    appendPropertySegment("City").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees('1')/Location/City", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithOrderby() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1").
    appendNavigationSegment("nm_Employees").
    orderBy("EmployeeId").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1')/nm_Employees?$orderby=EmployeeId", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithOrderbyAndFormat() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1").
    appendNavigationSegment("nm_Employees").
    orderBy("EmployeeId").
    format("application/json").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1')/nm_Employees"
        + "?$orderby=EmployeeId&$format=application%2Fjson", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithSelect() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1").
    appendNavigationSegment("nm_Employees").
    select("EmployeeId", "EmployeeName", "RoomId", "TeamId").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1')/nm_Employees"
        + "?$select=EmployeeId%2CEmployeeName%2CRoomId%2CTeamId", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithSelectAndFilter() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1").
    appendNavigationSegment("nm_Employees").
    filter("EmployeeId eq 1").
    select("EmployeeId", "EmployeeName", "RoomId", "TeamId").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1')/nm_Employees?$filter="
        + "EmployeeId%20eq%201&$select=EmployeeId%2CEmployeeName%2CRoomId%2CTeamId", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithExpand() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1").
    expand("nm_Employees").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1')?$expand=nm_Employees", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithExpandAndFilter() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    expand("nm_Employees").
    filter("EmployeeName eq 'Walter Winter'").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers?$expand=nm_Employees&$filter="
        + "EmployeeName%20eq%20'Walter%20Winter'", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithCustomQueryOption() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    top(2).
    addCustomQueryOption("x", "y").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees?$top=2&x=y", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithCustomQueryOptionWithFormat() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    top(2).
    addCustomQueryOption("x", "y").
    format("application/json").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees?$top=2&$format=application%2Fjson&x=y", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithFilters() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    filter("EmployeeId ge '1' and EmployeeId le '10'").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers?$filter=EmployeeId%20ge%20'1'%20"
        + "and%20EmployeeId%20le%20'10'", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithDuplicateExpands() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    expand("nm_Employees").
    expand("nm_Employees").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers?$expand=nm_Employees%2Cnm_Employees", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithTwoCustomQueryOptions() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    top(2).
    addCustomQueryOption("x", "y").
    addCustomQueryOption("z", "y").
    format("application/json").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees?$top=2&$"
        + "format=application%2Fjson&x=y&z=y", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithDuplicateOrderby() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    orderBy("EmployeeId").
    orderBy("EmployeeId").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers?$orderby=EmployeeId%2CEmployeeId", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithTwoOrderby() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    orderBy("EmployeeId").
    orderBy("EmployeeName desc").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees?$orderby=EmployeeId%2CEmployeeName%20desc", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithNavigationToManyWithKeyWithSimpleProperty() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1").
    appendNavigationSegment("nm_Employees").
    appendKeySegment("1").
    appendPropertySegment("EmployeeName").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1')/nm_Employees('1')/EmployeeName", uri.toASCIIString());
  }
  
  @Test
  public void testSimpleESWithEncodedKeyUri() {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Employees").
    appendKeySegment("abc/def").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees('abc%2Fdef')", uri.toASCIIString());
  }
  
  @Test
  public void testCompositeKeysEncodedUri() {
    Map<String, Object> keyMap = new LinkedHashMap<String,Object>();
    keyMap.put("Id", 4);
    keyMap.put("Type", "foo,foo;");
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Photos").
    appendKeySegment(keyMap).
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Photos(Id=4,Type='foo%2Cfoo%3B')", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithNavigationToManyWithKeyEncoded() throws EdmException {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1()*;").
    appendNavigationSegment("nm_Employees").
    appendKeySegment("@#$%").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1%28%29%2A%3B')/nm_Employees('%40%23%24%25')", uri.toASCIIString());
  }
  
  @Test
  public void testFunctionImportWithEmptyParams() throws EdmException {
    Map<String, Object> functionImportParams = new HashMap<String, Object>();
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
        appendFunctionImportSegment("AllLocations").appendFunctionImportParameters(functionImportParams).build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/AllLocations", uri.toASCIIString());
  }
  
  @Test
  public void testFunctionImportWithNullParams() throws EdmException {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
        appendFunctionImportSegment("AllLocations").appendFunctionImportParameters(null).build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/AllLocations", uri.toASCIIString());
  }
  
  @Test
  public void testFunctionImportWithParams() throws Exception {
    Map<String, Object> functionImportParams = new HashMap<String, Object>();
    functionImportParams.put("q", "Emp1");
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
        appendFunctionImportSegment("EmployeeSearch").appendFunctionImportParameters(functionImportParams).build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/EmployeeSearch?q='Emp1'", uri.toASCIIString());
  }
  
  @Test
  public void testFunctionImportWithParamsWithNullFacets() throws Exception {
    Map<String, Object> functionImportParams = new HashMap<String, Object>();
    functionImportParams.put("Id", "1");
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
        appendFunctionImportSegment("FINullableParameter").appendFunctionImportParameters(functionImportParams).build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/FINullableParameter?Id='1'", uri.toASCIIString());
  }
  
  @Test
  public void testFunctionImportWithParamsWithFalseNullFacets() throws Exception {
    Map<String, Object> functionImportParams = new HashMap<String, Object>();
    functionImportParams.put("Id", "1");
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
        appendFunctionImportSegment("ManagerPhoto").appendFunctionImportParameters(functionImportParams).build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/ManagerPhoto?Id='1'", uri.toASCIIString());
  }
  
  @Test
  public void testFunctionImportWithMultipleParams() throws Exception {
    Map<String, Object> functionImportParams = new LinkedHashMap<String, Object>();
    functionImportParams.put("q", "1");
    functionImportParams.put("r", 1);
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
        appendFunctionImportSegment("BuildingSearch").appendFunctionImportParameters(functionImportParams).build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/BuildingSearch?q='1'&r=1", uri.toASCIIString());
  }
  
  @Test
  public void testFunctionImportWithMultipleParamsWithMoreSegments() throws Exception {
    Map<String, Object> functionImportParams = new LinkedHashMap<String, Object>();
    functionImportParams.put("q", "1");
    functionImportParams.put("r", 1);
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
        appendFunctionImportSegment("BuildingSearch").appendKeySegment("1").
        appendNavigationSegment("nb_Rooms").
        appendFunctionImportParameters(functionImportParams).build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/BuildingSearch('1')/nb_Rooms?q='1'&r=1", uri.toASCIIString());
  }
  
  @Test
  public void testFunctionImportWithMultipleParamsWithCount() throws Exception {
    Map<String, Object> functionImportParams = new LinkedHashMap<String, Object>();
    functionImportParams.put("q", "1");
    functionImportParams.put("r", 1);
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
        appendFunctionImportSegment("BuildingSearch").appendKeySegment("1").
        appendNavigationSegment("nb_Rooms").
        appendCountSegment().
        appendFunctionImportParameters(functionImportParams).build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/BuildingSearch('1')/nb_Rooms/$count?q='1'&r=1", uri.toASCIIString());
  }
  
  @Test
  public void testFunctionImportWithCount() throws Exception {
    Map<String, Object> functionImportParams = new LinkedHashMap<String, Object>();
    functionImportParams.put("q", "1");
    functionImportParams.put("r", 1);
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
        appendFunctionImportSegment("BuildingSearch").
        appendCountSegment().
        appendFunctionImportParameters(functionImportParams).build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/BuildingSearch/$count?q='1'&r=1", uri.toASCIIString());
  }
  
  @Test
  public void testFunctionImportWithMultipleParamsWithPropertySegment() throws Exception {
    Map<String, Object> functionImportParams = new LinkedHashMap<String, Object>();
    functionImportParams.put("q", "1");
    functionImportParams.put("r", 1);
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
        appendFunctionImportSegment("BuildingSearch").appendKeySegment("1").
        appendNavigationSegment("nb_Rooms").appendKeySegment("1").
        appendPropertySegment("Name").
        appendFunctionImportParameters(functionImportParams).build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/BuildingSearch('1')/nb_Rooms('1')/Name?q='1'&r=1", uri.toASCIIString());
  }
  
  @Test
  public void testFunctionImportWithKeySegment() throws Exception {
    Map<String, Object> keySegments = new LinkedHashMap<String, Object>();
    keySegments.put("Id", "1");
    keySegments.put("Type", "Internal");
    Map<String, Object> functionImportParams = new LinkedHashMap<String, Object>();
    functionImportParams.put("Id", 1);
    functionImportParams.put("Type", "Internal");
    try {
    new URIBuilderImpl(SERVICE_ROOT_URI).
        appendFunctionImportSegment("PhotoSearch").
        appendKeySegment(keySegments).
        appendFunctionImportParameters(functionImportParams).build();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "Can't specify a key at this position");
    }
  }
  
  @Test
  public void testCustomQueryWithSystemQuery() throws Exception {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).appendEntitySetSegment("Managers").
        addCustomQueryOption("x", "y").filter("EmployeeName eq 'Walter Winter'").build();
    assertEquals("http://host:80/service/Managers?$filter="
        + "EmployeeName%20eq%20'Walter%20Winter'&x=y", uri.toASCIIString());
  }
  
  @Test
  public void testUriWithOnlyCustomQueryOption() throws EdmException {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI_1).
    addCustomQueryOption("x", "y").
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service?x=y", uri.toASCIIString());
  }
  
  @Test
  public void testServiceDocument() throws Exception {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).build();
    assertEquals("http://host:80/service/", uri.toASCIIString());
  }
  
  @Test
  public void testServiceDocument1() throws Exception {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI_1).build();
    assertEquals("http://host:80/service/", uri.toASCIIString());
  }
  
  @Test
  public void addSameFilterOptionTwice() throws EdmException {
    URI uri = new URIBuilderImpl(SERVICE_ROOT_URI).
    appendEntitySetSegment("Managers").
    appendKeySegment("1").
    appendNavigationSegment("nm_Employees").
    addQueryOption(QueryOption.FILTER, "TeamId eq '1'").
    addQueryOption(QueryOption.FILTER, "TeamId eq '1'").
    top(2).
    build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Managers('1')/nm_Employees"
        + "?$filter=TeamId%20eq%20'1'%2CTeamId%20eq%20'1'&$top=2", uri.toASCIIString());
  }
}
