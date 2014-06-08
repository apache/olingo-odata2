package org.apache.olingo.odata2.jpa.processor.ref.listeners;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PostLoad;
import javax.persistence.Query;

import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneEntityListener;
import org.apache.olingo.odata2.jpa.processor.ref.model.SalesOrderHeader;

public class SalesOrderTombstoneListener extends ODataJPATombstoneEntityListener {

  public static String ENTITY_NAME = "SalesOrderHeader";

  @PostLoad
  public void handleDelta(final Object entity) {
    SalesOrderHeader so = (SalesOrderHeader) entity;

    if (so.getCreationDate().getTime().getTime() < ODataJPATombstoneContext.getDeltaTokenUTCTimeStamp()) {
      return;
    } else {
      addToDelta(entity, ENTITY_NAME);
    }
  }

  @Override
  public String generateDeltaToken(final List<Object> deltas, final Query query) {
    return String.valueOf(System.currentTimeMillis());
  }

  @Override
  public Query getQuery(final GetEntitySetUriInfo resultsView, final EntityManager em) {
    return null;
  }

}
