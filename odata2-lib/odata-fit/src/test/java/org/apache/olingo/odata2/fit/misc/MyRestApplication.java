package org.apache.olingo.odata2.fit.misc;

import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.core.rest.app.AbstractODataApplication;

public class MyRestApplication extends AbstractODataApplication {

  @Override
  public Class<? extends ODataServiceFactory> getServiceFactoryClass() {
    return MyFactory.class;
  }

}
