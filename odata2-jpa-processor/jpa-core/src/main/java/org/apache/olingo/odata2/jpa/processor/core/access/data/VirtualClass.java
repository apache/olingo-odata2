package org.apache.olingo.odata2.jpa.processor.core.access.data;

import java.util.HashMap;

public class VirtualClass implements VirtualClassInterface {
  private HashMap<String, Object> map = new HashMap<String, Object>();

  public Object get(String name) {
    return map.get(name.toLowerCase());
  }

  public VirtualClass set(String name, Object value) {
    map.put(name.toLowerCase(), value);

    return this;
  }

  @Override
  public Object getObject() {
    return map;
  }
}
