package org.apache.olingo.odata2.core.ep.producer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.util.FormatJson;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;

public class JsonDeletedEntryEntityProducer {

  private EntityProviderWriteProperties properties;

  public JsonDeletedEntryEntityProducer(EntityProviderWriteProperties properties) {
    this.properties = properties;
  }

  public void append(Writer writer, EntityInfoAggregator entityInfo, List<Map<String, Object>> deletedEntries)
      throws EntityProviderException {
    JsonStreamWriter jsonStreamWriter = new JsonStreamWriter(writer);
    try {
      if (deletedEntries.size() > 0) {
        jsonStreamWriter.separator();
        for (Map<String, Object> deletedEntry : deletedEntries) {
          jsonStreamWriter.beginObject();

          String odataContextValue = "$metadata#" + entityInfo.getEntitySetName() + "/$deletedEntity";
          String selfLink = AtomEntryEntityProducer.createSelfLink(entityInfo, deletedEntry, null);
          String idValue = properties.getServiceRoot().toASCIIString() + selfLink;

          jsonStreamWriter.namedStringValue(FormatJson.ODATA_CONTEXT, odataContextValue);
          jsonStreamWriter.separator();
          jsonStreamWriter.namedStringValue(FormatJson.ID, idValue);

          jsonStreamWriter.endObject();
        }
      }
    } catch (final IOException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }

  }

}
