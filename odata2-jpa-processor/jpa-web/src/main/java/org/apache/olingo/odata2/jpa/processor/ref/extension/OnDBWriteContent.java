package org.apache.olingo.odata2.jpa.processor.ref.extension;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialException;

import org.apache.olingo.odata2.jpa.processor.api.OnJPAWriteContent;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.hsqldb.jdbc.JDBCBlob;
import org.hsqldb.jdbc.JDBCClob;

public class OnDBWriteContent implements OnJPAWriteContent {

  @Override
  public Blob getJPABlob(byte[] binaryData) throws ODataJPARuntimeException {
    try {
      return new JDBCBlob(binaryData);
    } catch (SerialException e) {
      ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    } catch (SQLException e) {
      ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    }
    return null;
  }

  @Override
  public Clob getJPAClob(char[] characterData) throws ODataJPARuntimeException {
    try {
      return new JDBCClob(new String(characterData));
    } catch (SQLException e) {
      ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
    }
    return null;
  }
}
