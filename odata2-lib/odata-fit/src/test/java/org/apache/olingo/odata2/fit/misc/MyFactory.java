package org.apache.olingo.odata2.fit.misc;

import javax.ws.rs.ext.Provider;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.ref.processor.ScenarioServiceFactory;

@Provider
public class MyFactory extends ODataServiceFactory {

  @Override
  public ODataService createService(ODataContext ctx) throws ODataException {
    return new ScenarioServiceFactory().createService(ctx);
  }

  @Override
  public <T extends ODataCallback> T getCallback(Class<? extends ODataCallback> callbackInterface) {
    return new ScenarioServiceFactory().getCallback(callbackInterface);
  }

}
