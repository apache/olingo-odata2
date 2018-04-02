package org.apache.olingo.odata2.client.core.uri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.client.api.ODataClient;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Before;
import org.junit.Test;

public class UriBuilderIntegrationTest {

  protected static final String SERVICE_ROOT_URI = "http://host:80/service/";
  private Edm edm;

  @Before
  public void getEdm() throws ODataException {
    edm = MockFacade.getMockEdm();
  }
  
  @Test
  public void constructEdmUri() throws EdmException {
    EdmEntitySet entitySet = edm.getDefaultEntityContainer().getEntitySet("Employees");
    URI uri = ODataClient.newInstance().edmUriBuilder(SERVICE_ROOT_URI).
        appendEntitySetSegment(entitySet).
        appendKeySegment((EdmProperty)entitySet.getEntityType().getProperty("EmployeeId"), "1").
        appendNavigationSegment((EdmNavigationProperty)entitySet.getEntityType().getProperty("ne_Team")).
        build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees('1')/ne_Team", uri.toASCIIString());
  }
  
  @Test
  public void constructUri() {
    URI uri = ODataClient.newInstance().uriBuilder(SERVICE_ROOT_URI).
        appendEntitySetSegment("Employees").
        appendKeySegment("1").
        appendNavigationSegment("ne_Team").
        build();
    assertNotNull(uri);
    assertEquals("http://host:80/service/Employees('1')/ne_Team", uri.toASCIIString());
  }
}
