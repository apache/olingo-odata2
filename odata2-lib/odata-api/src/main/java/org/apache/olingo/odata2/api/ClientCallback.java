package org.apache.olingo.odata2.api;

import java.util.LinkedList;
import java.util.List;

public class ClientCallback {
  private String function;
  private List<Object> params = new LinkedList<Object>();

  public ClientCallback(String function) {
    this.function = function;
  }

  public void addParam(Object... values) {
    for (Object o : values) {
      params.add(o);
    }
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public List<Object> getParams() {
    return params;
  }

  public void setParams(List<Object> params) {
    this.params = params;
  }
}
