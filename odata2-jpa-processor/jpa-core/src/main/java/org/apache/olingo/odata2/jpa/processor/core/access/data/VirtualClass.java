package org.apache.olingo.odata2.jpa.processor.core.access.data;

import java.util.HashMap;

public class VirtualClass {
  private HashMap<String, Object> map = new HashMap<String, Object>();

  public Object get(String name) {
    return map.get(name);
  }

  public VirtualClass set(String name, Object value) {
    map.put(name, value);

    return this;
  }
}
