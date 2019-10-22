package org.apache.olingo.odata2.jpa.processor.core.access.data;

public interface VirtualClassInterface {

  public Object get(String name);

  public VirtualClassInterface set(String name, Object value);

  public Object getObject();
}
