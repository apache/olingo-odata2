package org.apache.olingo.odata2.core.rest.app;

import org.apache.olingo.odata2.api.ODataServiceFactory;

public abstract class AbstractODataApplication extends ODataApplication {

  public abstract Class<? extends ODataServiceFactory> getServiceFactoryClass();

}
