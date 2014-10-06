package org.apache.olingo.odata2.fit.basic;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.testutil.fit.AbstractFitTest;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

public class NullServiceTest extends AbstractFitTest {

  public NullServiceTest(final ServletType servletType) {
    super(servletType);
  }

  @Override
  protected ODataService createService() throws ODataException {
    return null;
  }

  @Test
  public void nullServiceMustResultInODataResponse() throws Exception {
    System.out.println("The following internal Server Error is wanted if this test doesnt fail!");
    final HttpResponse response = executeGetRequest("$metadata");
    assertEquals(HttpStatusCodes.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusLine().getStatusCode());

    
    ODataErrorContext error = EntityProvider.readErrorDocument(response.getEntity().getContent(), "application/xml");
    assertEquals("Service unavailable.", error.getMessage());
  }
  
  private HttpResponse executeGetRequest(final String request) throws ClientProtocolException, IOException {
    final HttpGet get = new HttpGet(URI.create(getEndpoint().toString() + request));
    return getHttpClient().execute(get);
  }

}
