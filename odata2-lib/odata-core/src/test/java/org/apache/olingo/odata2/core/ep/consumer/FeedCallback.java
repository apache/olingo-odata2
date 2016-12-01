package org.apache.olingo.odata2.core.ep.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.callback.OnReadInlineContent;
import org.apache.olingo.odata2.api.ep.callback.ReadEntryResult;
import org.apache.olingo.odata2.api.ep.callback.ReadFeedResult;
import org.apache.olingo.odata2.api.ep.callback.ReadResult;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;

public class FeedCallback implements OnReadInlineContent{

  private static Map<String, Object> navigationProp = new HashMap<String, Object>();
  private String id = "";
  
  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the navigationPropFeed
   */
  public Map<String, Object> getNavigationProperties() {
    return navigationProp;
  }

  @Override
  public EntityProviderReadProperties receiveReadProperties(EntityProviderReadProperties readProperties,
      EdmNavigationProperty navigationProperty) throws ODataApplicationException {
    Map<String, Object> typeMappings = new HashMap<String, Object>();
    return EntityProviderReadProperties.init().addTypeMappings(typeMappings).callback(new FeedCallback()).build();
  }

  @Override
  public void handleReadEntry(ReadEntryResult context) throws ODataApplicationException {
    this.id = context.getParentEntryId();
    handleEntry(context);
    
  }

  @Override
  public void handleReadFeed(ReadFeedResult context) throws ODataApplicationException {
    this.id = context.getParentEntryId();
    handleFeed(context);
    
  }
  
  private void handleEntry(final ReadResult context) {
    try {
      String navigationPropertyName = context.getNavigationProperty().getName();
      if (navigationPropertyName != null) {
        navigationProp.put(navigationPropertyName + id, (ODataEntry) context.getResult());
      } else {
        throw new RuntimeException("Invalid title");
      }
    } catch (EdmException e) {
      throw new RuntimeException("Invalid title");
    }
  }

  private void handleFeed(final ReadFeedResult context) {
    try {
      String navigationPropertyName = context.getNavigationProperty().getName();
      if (navigationPropertyName != null) {
        navigationProp.put(navigationPropertyName + id, (ODataFeed) context.getResult());
      } else {
        throw new RuntimeException("Invalid title");
      }
    } catch (EdmException e) {
      throw new RuntimeException("Invalid title");
    }
  }
}
