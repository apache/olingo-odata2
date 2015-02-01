package org.apache.olingo.odata2.jpa.processor.core.mock.data;

public class Note {
  private String id;
  private SalesOrderHeader salesOrderHeader;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public SalesOrderHeader getSalesOrderHeader() {
    return salesOrderHeader;
  }

  public void setSalesOrderHeader(SalesOrderHeader salesOrderHeader) {
    this.salesOrderHeader = salesOrderHeader;
  }
}
