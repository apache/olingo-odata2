package org.apache.olingo.odata2.core.ep.entry;

import java.util.Date;

import org.apache.olingo.odata2.api.ep.entry.DeletedEntryMetadata;

public class DeletedEntryMetadataImpl implements DeletedEntryMetadata {

  private String uri;
  private Date when;
  
  @Override
  public String getUri() {
    return uri;
  }

  @Override
  public Date getWhen() {
    return when;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public void setWhen(Date when) {
    this.when = when;
  }

}
