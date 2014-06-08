package org.apache.olingo.odata2.jpa.processor.ref.listeners;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContext;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement;

public class SalesOrderItemTombstoneListener extends ODataJPATombstoneEntityListener {

  @Override
  public Query getQuery(final GetEntitySetUriInfo resultsView, final EntityManager em) {
    JPQLContextType contextType = null;

    try {
      if (!resultsView.getStartEntitySet().getName().equals(resultsView.getTargetEntitySet().getName())) {
        contextType = JPQLContextType.JOIN;
      } else {
        contextType = JPQLContextType.SELECT;
      }

      JPQLContext jpqlContext = JPQLContext.createBuilder(contextType, resultsView).build();
      JPQLStatement jpqlStatement = JPQLStatement.createBuilder(jpqlContext).build();
      String deltaToken = ODataJPATombstoneContext.getDeltaToken();

      Query query = null;
      if (deltaToken != null) {
        String statement = jpqlStatement.toString();
        String[] statementParts = statement.split(JPQLStatement.KEYWORD.WHERE);
        String deltaCondition = jpqlContext.getJPAEntityAlias() + ".creationDate >= {ts '" + deltaToken + "'}";
        if (statementParts.length > 1) {
          statement =
              statementParts[0] + JPQLStatement.DELIMITER.SPACE + JPQLStatement.KEYWORD.WHERE
                  + JPQLStatement.DELIMITER.SPACE + deltaCondition + JPQLStatement.DELIMITER.SPACE
                  + JPQLStatement.Operator.AND + statementParts[1];
        } else {
          statement =
              statementParts[0] + JPQLStatement.DELIMITER.SPACE + JPQLStatement.KEYWORD.WHERE
                  + JPQLStatement.DELIMITER.SPACE + deltaCondition;
        }

        query = em.createQuery(statement);
      } else {
        query = em.createQuery(jpqlStatement.toString());
      }

      return query;
    } catch (EdmException e) {
      return null;
    } catch (ODataJPAModelException e) {
      return null;
    } catch (ODataJPARuntimeException e) {
      return null;
    }
  }

  @Override
  public String generateDeltaToken(final List<Object> deltas, final Query query) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000");

    Date date = new Date(System.currentTimeMillis());
    dateFormat.format(date);
    return dateFormat.format(date);
  }

}
