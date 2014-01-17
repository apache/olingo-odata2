package org.apache.olingo.odata2.fit.client;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySetInfo;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.processor.part.MetadataProcessor;
import org.apache.olingo.odata2.api.uri.info.GetMetadataUriInfo;
import org.apache.olingo.odata2.core.processor.ODataSingleProcessorService;
import org.apache.olingo.odata2.fit.client.util.Client;
import org.apache.olingo.odata2.fit.client.util.HttpException;
import org.apache.olingo.odata2.ref.edm.ScenarioEdmProvider;
import org.apache.olingo.odata2.testutil.fit.AbstractFitTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ClientDeltaResponse extends AbstractFitTest {

  private Client client;

  @Before
  @Override
  public void before() {
    super.before();
    try {
      client = new Client(getEndpoint().toASCIIString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private class StubProcessor extends ODataSingleProcessor {
    
  }
  
  @Override
  protected ODataService createService() throws ODataException {
    EdmProvider provider = new ScenarioEdmProvider();
    ODataSingleProcessor processor = new StubProcessor();
    
     return new ODataSingleProcessorService(provider, processor);
  }

  @Test
  public void dummy() throws Exception {}

  @Test
  public void testEdm() throws Exception {
    Edm edm = client.getEdm();
    assertNotNull(edm);
    assertNotNull(edm.getDefaultEntityContainer());

    System.out.println(edm.getDefaultEntityContainer().getName());
  }

  @Test
  public void testEntitySets() throws Exception {
    List<EdmEntitySetInfo> sets = client.getEntitySets();
    assertNotNull(sets);
    assertEquals(6, sets.size());
  }

  @Test
  public void testEntity() throws Exception {
    ODataFeed feed = client.readFeed("Container1", "Rooms", "application/atom+xml");
    assertNotNull(feed);
  }

}