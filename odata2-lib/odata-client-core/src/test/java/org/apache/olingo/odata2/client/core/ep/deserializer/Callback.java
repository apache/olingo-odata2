package org.apache.olingo.odata2.client.core.ep.deserializer;

import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.client.api.ep.DeserializerProperties;
import org.apache.olingo.odata2.client.api.ep.callback.OnDeserializeInlineContent;

public class Callback implements OnDeserializeInlineContent {

  @Override
  public DeserializerProperties receiveReadProperties(DeserializerProperties readProperties,
      EdmNavigationProperty navigationProperty) throws ODataApplicationException {
    Map<String, Object> typeMappings = new HashMap<String, Object>();
    return DeserializerProperties.init().addTypeMappings(typeMappings).
        callback(new Callback()).build();
  }

}
